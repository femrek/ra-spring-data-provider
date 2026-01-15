# React Admin Spring Data Provider

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2%2B-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot library that provides a standardized REST API implementation compatible
with [React Admin](https://marmelab.com/react-admin/)'s data provider protocol
[ra-data-json-server](https://github.com/marmelab/react-admin/blob/master/packages/ra-data-json-server/README.md).
This library simplifies the process of building React Admin backends with Spring Boot.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage](#usage)
    - [Basic Setup](#basic-setup)
    - [Service Implementation](#service-implementation)
    - [Advanced Filtering](#advanced-filtering)
- [API Endpoints](#api-endpoints)
- [License](#license)

## Overview

This library bridges the gap between Spring Boot applications and React Admin frontends by providing:

- **Drop-in Controllers**: Extend `ReactAdminController` to automatically handle all React Admin data provider
  operations
- **Service Interface**: Implement `IReactAdminService` to define your business logic
- **Standard Protocol**: Full compatibility with React Admin's `ra-data-json-server` data provider
- **Advanced Features**: Built-in support for pagination, sorting, filtering, and global search

## Features

- ✅ **Complete CRUD Operations**: GET, CREATE, UPDATE, DELETE operations as ra-data-json-server specifies
- ✅ **Pagination & Sorting**: Built-in support for paginated responses and multi-field sorting
- ✅ **Advanced Filtering**: Field-specific filters and global search queries
- ✅ **Type-Safe**: Generics support for any entity type

## Requirements

- **Java**: 17 or higher
- **Spring Boot**: 3.2 or higher (compatible with 3.x series)
- **Maven**: 3.6+ (or Gradle)

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml

<dependency>
    <groupId>dev.femrek</groupId>
    <artifactId>ra-json-spring-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```gradle
implementation 'dev.femrek:ra-json-spring-api:0.0.1-SNAPSHOT'
```

## Quick Start

### 1. Create Your Entity

```java

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String role;

    // Getters and setters...
}
```

### 2. Create Your Repository

```java

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

### 3. Implement the Service

```java

@Service
public class UserService implements IReactAdminService<User, Long> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<User> findWithFilters(Map<String, String> filters, String q, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply field-specific filters
            if (filters != null) {
                filters.forEach((field, value) -> {
                    if (value != null && !value.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get(field), value));
                    }
                });
            }

            // Apply global search
            if (q != null && !q.isEmpty()) {
                String pattern = "%" + q.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), pattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), pattern);
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public List<User> findAllById(Iterable<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User save(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public User update(Long id, Map<String, Object> fields) {
        User user = findById(id);
        if (user == null) return null;

        fields.forEach((key, value) -> {
            switch (key) {
                case "name" -> user.setName((String) value);
                case "email" -> user.setEmail((String) value);
                case "role" -> user.setRole((String) value);
            }
        });

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
```

### 4. Create Your Controller

```java

@RestController
@RequestMapping("/api/users") // resource name
@CrossOrigin(origins = "*")
public class UserController extends ReactAdminController<User, Long> {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IReactAdminService<User, Long> getService() {
        return userService;
    }
}
```

### 5. Configure CORS (if needed)

```java

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Range", "X-Total-Count"); // for pagination
            }
        };
    }
}
```

### 6. Configure React Admin Frontend

```javascript
import { Admin, Resource, ListGuesser } from 'react-admin';
import jsonServerProvider from 'ra-data-json-server';

const dataProvider = jsonServerProvider('http://localhost:8080/api');

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="users" list={ListGuesser} />
  </Admin>
);

export default App;
```

## Usage

### Basic Setup

The library provides two main components:

1. **`ReactAdminController<T, ID>`**: Abstract controller that handles HTTP requests
2. **`IReactAdminService<T, ID>`**: Service interface for business logic

Your implementation needs to:

- Extend `ReactAdminController` for your entity
- Implement `IReactAdminService` for your business logic
- Return your service implementation from the `getService()` method

### Service Implementation

The `IReactAdminService` interface requires you to implement:

- **`findWithFilters()`**: Query with filters, search, pagination, and sorting
- **`findAllById()`**: Fetch multiple records by IDs
- **`findById()`**: Fetch a single record
- **`save()`**: Create or update a record
- **`update()`**: Partial update of a record
- **`deleteById()`**: Delete a single record

### Advanced Filtering

The `findWithFilters()` method receives:

- **`filters`**: Map of field names to filter values (e.g., `{"role": "admin"}`)
- **`q`**: Global search string (search across multiple fields)
- **`pageable`**: Spring Data Pageable with pagination and sorting info

Example implementation with JPA Specifications:

```java

@Override
public Page<User> findWithFilters(Map<String, String> filters, String q, Pageable pageable) {
    Specification<User> spec = (root, query, criteriaBuilder) -> {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        // Field-specific filters
        if (filters != null) {
            if (filters.containsKey("role")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("role"), filters.get("role")));
            }
            if (filters.containsKey("status")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"), filters.get("status")));
            }
        }

        // Global search
        if (q != null && !q.isEmpty()) {
            String pattern = "%" + q.toLowerCase() + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            ));
        }

        return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    return repository.findAll(spec, pageable);
}
```

## API Endpoints

The controller automatically provides these endpoints. These are also the ra-data-json-server end-points:

| Method | Endpoint                | React Admin Method | Description                     |
|--------|-------------------------|--------------------|---------------------------------|
| GET    | `/{resource}`           | `getList`          | Get paginated list with filters |
| GET    | `/{resource}?id=1&id=2` | `getMany`          | Get multiple records by IDs     |
| GET    | `/{resource}/{id}`      | `getOne`           | Get single record               |
| POST   | `/{resource}`           | `create`           | Create new record               |
| PUT    | `/{resource}/{id}`      | `update`           | Update single record            |
| DELETE | `/{resource}/{id}`      | `delete`           | Delete single record            |
| PUT    | same as `update`        | `updateMany`       | Update multiple records         |
| DELETE | same as `delete`        | `deleteMany`       | Delete multiple records         |

**Note:** Update many and delete many operations are handled via multiple requests to the above endpoints.

### Query Parameters

- `_start`: Start index for pagination (default: 0)
- `_end`: End index for pagination (default: 10)
- `_sort`: Field to sort by (default: "id")
- `_order`: Sort order (`ASC` or `DESC`, default: "ASC")
- `_embed`: May be sent by React Admin but is ignored.
- `id`: Array of IDs (for getMany, updateMany, deleteMany operations)
- `q`: Global search query
- Any other params are treated as field filters

### Development Setup

```bash
# Clone the repository
git clone https://github.com/femrek/ra-spring-data-provider.git
cd ra-spring-data-provider

# Build the project
mvn clean install

# Run tests
mvn test

# Run integration tests
./run-integration-tests.sh
```

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [React Admin](https://marmelab.com/react-admin/) - Frontend framework for building admin interfaces.
    - [ra-data-json-server](https://github.com/marmelab/react-admin/blob/master/packages/ra-data-json-server/README.md) -
      The data provider protocol specification provided by React Admin.
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework that this library provides integration for.

## Resources

- [React Admin Documentation](https://marmelab.com/react-admin/Tutorial.html)
- [React Admin Data Provider Documentation](https://marmelab.com/react-admin/DataProviders.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
