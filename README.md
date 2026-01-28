# React Admin Spring Data Provider

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot library that simplifies building [React Admin][react-admin] backends. This library provides a standardized REST API implementation that works with the [ra-spring-data-provider][ra-spring-data-provider] data provider, which is based on the [ra-data-json-server][ra-data-json-server] protocol.

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

- **Drop-in Controllers**: Extend `RAController` to automatically handle all React Admin data provider operations
- **Service Interface**: Implement `IRAService` to define your business logic
- **Dedicated Data Provider**: Use the compatible [ra-spring-data-provider] data provider on client side to full compatibility.
- **Advanced Features**: Built-in support on your API design for pagination, sorting, filtering, and global search

## Features

- ✅ **Complete CRUD Operations**: GET, CREATE, UPDATE, DELETE operations with efficient bulk support
- ✅ **Pagination & Sorting**: Built-in support for paginated responses and multi-field sorting
- ✅ **Advanced Filtering**: Field-specific filters and global search queries
- ✅ **Type-Safe**: Generics support with separate DTOs for requests and responses
- ✅ **Flexible Data Mapping**: Use DTOs to control exactly what data is exposed in your API

## Requirements

- **Java**: 17 or higher
- **Spring Boot**: 4 or higher recommended
- **Maven**: 3.6+ (or Gradle)

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.femrek</groupId>
    <artifactId>ra-spring-json-server</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```gradle
implementation 'dev.femrek:ra-spring-json-server:1.2.0'
```

## Quick Start

This library uses efficient bulk operations with single requests containing multiple ID parameters.

<details>
<summary>Click to expand setup instructions</summary>

#### 1. Install ra-spring-data-provider

Install the compatible React Admin data provider:

```bash
npm install ra-spring-data-provider
```

or

```bash
yarn add ra-spring-data-provider
```

This package is specifically designed to work with the Spring Boot backend created using this library.

#### 2. Configure React Admin Frontend

```javascript
import { Admin, Resource, ListGuesser } from "react-admin";
import raSpringDataProvider from "ra-spring-data-provider";

const dataProvider = raSpringDataProvider("http://localhost:8080/api");

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="users" list={ListGuesser} />
  </Admin>
);

export default App;
```

#### 3. Create Your Entity

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
}
```

#### 4. Create Your Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

#### 5. Create DTOs

```java
// UserCreateDTO.java - for creating users
public class UserCreateDTO {
    private String name;
    private String email;
    private String role;
    // getters and setters
}

// UserResponseDTO.java - for returning user data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    // getters and setters
}
```

#### 6. Implement the Service

```java
@Service
public class UserService implements IRAService<UserResponseDTO, UserCreateDTO, Long> {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply field-specific filters
            if (filters != null) {
                // Extract and apply global search query (q parameter)
                String q = filters.remove("q");
                if (q != null && !q.isEmpty()) {
                    String pattern = "%" + q.toLowerCase() + "%";
                    Predicate namePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), pattern);
                    Predicate emailPredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")), pattern);
                    predicates.add(criteriaBuilder.or(namePredicate, emailPredicate));
                }

                // Apply remaining field filters
                filters.forEach((field, value) -> {
                    if (value != null && !value.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get(field), value));
                    }
                });
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> entities = userRepository.findAll(spec, pageable);
        return entities.map(entity -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setEmail(entity.getEmail());
            dto.setRole(entity.getRole());
            return dto;
        });
    }

    @Override
    public List<UserResponseDTO> findAllById(Iterable<Long> ids) {
        List<User> entities = userRepository.findAllById(ids);
        List<UserResponseDTO> results = new ArrayList<>();
        for (User entity : entities) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setEmail(entity.getEmail());
            dto.setRole(entity.getRole());
            results.add(dto);
        }
        return results;
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        return dto;
    }

    @Override
    public UserResponseDTO create(UserCreateDTO data) {
        User user = new User();
        user.setName(data.getName());
        user.setEmail(data.getEmail());
        user.setRole(data.getRole());

        User savedUser = userRepository.save(user);
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(savedUser.getId());
        dto.setName(savedUser.getName());
        dto.setEmail(savedUser.getEmail());
        dto.setRole(savedUser.getRole());
        return dto;
    }

    @Override
    public UserResponseDTO update(Long id, Map<String, Object> fields) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        fields.forEach((key, value) -> {
            switch (key) {
                case "name":
                    user.setName((String) value);
                    break;
                case "email":
                    user.setEmail((String) value);
                    break;
                case "role":
                    user.setRole((String) value);
                    break;
            }
        });

        User updatedUser = userRepository.save(user);
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(updatedUser.getId());
        dto.setName(updatedUser.getName());
        dto.setEmail(updatedUser.getEmail());
        dto.setRole(updatedUser.getRole());
        return dto;
    }

    @Override
    public Void deleteById(Long id) {
        userRepository.deleteById(id);
        return null;
    }

    @Override
    public List<Long> updateMany(Iterable<Long> ids, Map<String, Object> fields) {
        List<Long> updatedIds = new ArrayList<>();

        // Find all users by their IDs
        List<User> users = userRepository.findAllById(ids);

        // Update each user with the provided fields
        for (User user : users) {
            fields.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        user.setName((String) value);
                        break;
                    case "email":
                        user.setEmail((String) value);
                        break;
                    case "role":
                        user.setRole((String) value);
                        break;
                }
            });
            User savedUser = userRepository.save(user);
            updatedIds.add(savedUser.getId());
        }

        return updatedIds;
    }

    @Override
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> deletedIds = StreamSupport.stream(ids.spliterator(), false)
                .toList();

        // Delete all users by their IDs
        userRepository.deleteAllById(ids);

        return deletedIds;
    }
}
```

#### 7. Create Your Controller

```java
@RestController
@RequestMapping("/api/users") // resource name: users
@CrossOrigin(origins = "*")
public class UserController extends RAController<UserResponseDTO, UserCreateDTO, Long> {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IRAService<UserResponseDTO, UserCreateDTO, Long> getService() {
        return userService;
    }
}
```

#### 8. Configure CORS (if needed)

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

</details>

## Usage

### Basic Setup

The library provides two main components:

1. **`RAController<ResponseDTO, CreateDTO, ID>`**: Abstract controller that handles HTTP requests
2. **`IRAService<ResponseDTO, CreateDTO, ID>`**: Service interface for business logic

Your implementation needs to:

- Create DTOs for request (CreateDTO) and response (ResponseDTO) data
- Extend `RAController` with your DTO types
- Implement `IRAService` for your business logic
- Return your service implementation from the `getService()` method

### Service Implementation

The `IRAService` interface requires you to implement:

- **`findWithFilters()`**: Query with filters (including `q` for search), pagination, and sorting - returns Page<ResponseDTO>
- **`findAllById()`**: Fetch multiple records by IDs - returns List<ResponseDTO>
- **`findById()`**: Fetch a single record - returns ResponseDTO
- **`create()`**: Create a new record from CreateDTO - returns ResponseDTO
- **`update()`**: Partial update of a record using a Map of fields - returns ResponseDTO
- **`deleteById()`**: Delete a single record - returns Void
- **`updateMany()`**: Bulk update multiple records - returns List<ID>
- **`deleteMany()`**: Bulk delete multiple records - returns List<ID>

### Advanced Filtering

The `findWithFilters()` method receives:

- **`filters`**: Map of field names to filter values (e.g., `{"role": "admin"}`) - may include a `"q"` key for global search, which must be handled manually in the service as shown in the Quick Start section.
- **`pageable`**: Spring Data Pageable with pagination and sorting info

Example implementation with JPA Specifications:

```java

@Override
public Page<UserResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
    Specification<User> spec = (root, query, criteriaBuilder) -> {
        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

        if (filters != null) {
            // Extract and handle global search query
            String q = filters.remove("q");
            if (q != null && !q.isEmpty()) {
                String pattern = "%" + q.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
                ));
            }

            // Field-specific filters
            if (filters.containsKey("role")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("role"), filters.get("role")));
            }
            if (filters.containsKey("status")) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"), filters.get("status")));
            }
        }

        return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    Page<User> entities = repository.findAll(spec, pageable);
    return entities.map(entity -> {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        return dto;
    });
}
```

## API Endpoints

`RAContoller` automatically provides these endpoints. These are also the ra-spring-data-provider end-points:

| Method | Endpoint                | React Admin Method | Description                     |
| ------ | ----------------------- | ------------------ | ------------------------------- |
| GET    | `/{resource}`           | `getList`          | Get paginated list with filters |
| GET    | `/{resource}?id=1&id=2` | `getMany`          | Get multiple records by IDs     |
| GET    | `/{resource}/{id}`      | `getOne`           | Get single record               |
| POST   | `/{resource}`           | `create`           | Create new record               |
| PUT    | `/{resource}/{id}`      | `update`           | Update single record            |
| PUT    | `/{resource}?id=1&id=2` | `updateMany`       | Update multiple records (bulk)  |
| DELETE | `/{resource}/{id}`      | `delete`           | Delete single record            |
| DELETE | `/{resource}?id=1&id=2` | `deleteMany`       | Delete multiple records (bulk)  |

### Query Parameters

#### getList

- `_start`: Start index for pagination (required)
- `_end`: End index for pagination (required)
- `_sort`: Field to sort by (default: "id")
- `_order`: Sort order (`ASC` or `DESC`, default: "ASC")
- `_embed`: May be sent by React Admin but is ignored.
- Any other params are treated as field filters

#### getMany, updateMany & deleteMany

- `id`: Array of IDs (for getMany, updateMany, deleteMany operations)

### Development Setup

```bash
# Clone the repository
git clone https://github.com/femrek/ra-spring-json-server.git
cd ra-spring-json-server

# Build the project
cd ra-spring-json-server
mvn clean install

# Run unit tests
mvn test

# Run integration tests
cd ..
cd ra-spring-data-provider
npm i
cd ..
./run-integration-tests.sh
```

## License

This project is dual-licensed under:

- [Apache License 2.0][license-apache] - see the [LICENSE_APACHE](LICENSE_APACHE) file
- [MIT License][license-mit] - see the [LICENSE_MIT](LICENSE_MIT) file

You may choose either license for your use of this library.

## Acknowledgments

- [React Admin][react-admin] - Frontend framework for building admin interfaces.
  - [ra-data-json-server][ra-data-json-server] -
    The data provider protocol specification provided by React Admin.
- [Spring Boot][spring-boot] - Backend framework that this library provides integration for.

## Resources

- [React Admin Documentation][react-admin-docs]
- [React Admin Data Provider Documentation][react-admin-dataproviders]
- [Spring Boot Documentation][spring-boot-docs]

<!-- Link Definitions -->

[react-admin]: https://marmelab.com/react-admin/
[ra-spring-data-provider]: https://github.com/femrek/ra-spring-json-server/tree/main/ra-spring-data-provider
[ra-data-json-server]: https://github.com/marmelab/react-admin/tree/master/packages/ra-data-json-server
[spring-boot]: https://spring.io/projects/spring-boot
[license-apache]: LICENSE_APACHE
[license-mit]: LICENSE_MIT
[react-admin-docs]: https://marmelab.com/react-admin/Tutorial.html
[react-admin-dataproviders]: https://marmelab.com/react-admin/DataProviders.html
[spring-boot-docs]: https://spring.io/projects/spring-boot
[integration-test-files]: https://github.com/femrek/ra-spring-json-server/tree/main/ra-spring-json-server/src/test/java/dev/femrek/reactadmindataprovider/integration
