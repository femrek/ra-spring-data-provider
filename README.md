# React Admin Spring Data Provider

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot library that simplifies building [React Admin][react-admin] backends. This library provides a standardized REST API implementation that works with the [ra-spring-data-provider][ra-spring-data-provider] data provider (recommended for efficient bulk operations) and is also compatible with [ra-data-json-server][ra-data-json-server].

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
- **Special Data Provider**: Use compatible data provider [ra-spring-data-provider][ra-spring-data-provider] on client side.
- **Standard Data Provider Support**: Also, you can use [ra-data-json-server][ra-data-json-server] as data provider on client side.
- **Advanced Features**: Built-in support on your API design for pagination, sorting, filtering, and global search

## Features

- ✅ **Complete CRUD Operations**: GET, CREATE, UPDATE, DELETE operations as ra-data-json-server specifies
- ✅ **Pagination & Sorting**: Built-in support for paginated responses and multi-field sorting
- ✅ **Advanced Filtering**: Field-specific filters and global search queries
- ✅ **Type-Safe**: Generics support for any entity type

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
    <version>1.1.1</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```gradle
implementation 'dev.femrek:ra-spring-json-server:1.1.1'
```

## Quick Start

### a. ra-spring-data-provider (Recommended)

This approach uses efficient bulk operations with single requests containing multiple ID parameters.

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

#### 5. Implement the Service

```java
@Service
public class UserService implements IRAService<User, Long> {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    @Override
    public List<Long> updateMany(Iterable<Long> ids, Map<String, Object> fields) {
        List<Long> updatedIds = new ArrayList<>();

        // Find all users by their IDs
        List<User> users = userRepository.findAllById(ids);

        // Update each user with the provided fields
        for (User user : users) {
            fields.forEach((key, value) -> {
                switch (key) {
                    case "name" -> user.setName((String) value);
                    case "email" -> user.setEmail((String) value);
                    case "role" -> user.setRole((String) value);
                }
            });
            User savedUser = userRepository.save(user);
            updatedIds.add(savedUser.getId());
        }

        return updatedIds;
    }

    @Override
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();

        // Collect the IDs before deletion
        for (Long id : ids) {
            deletedIds.add(id);
        }

        // Delete all users by their IDs
        userRepository.deleteAllById(ids);

        return deletedIds;
    }
}
```

#### 6. Create Your Controller

```java
@RestController
@RequestMapping("/api/users") // resource name: users
@CrossOrigin(origins = "*")
public class UserController extends RAController<User, Long> {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IRAService<User, Long> getService() {
        return userService;
    }
}
```

#### 7. Configure CORS (if needed)

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

### b. ra-data-json-server

This approach uses the standard JSON Server protocol where bulk operations send multiple individual requests. So, [starting with ra-spring-data-provider](#a-ra-spring-data-provider-recommended) is recommened.

<details>
<summary>Click to expand setup instructions</summary>

#### 1. Install ra-data-json-server

Install the React Admin JSON Server data provider:

```bash
npm install ra-data-json-server
```

or

```bash
yarn add ra-data-json-server
```

#### 2. Configure React Admin Frontend

```javascript
import { Admin, Resource, ListGuesser } from "react-admin";
import jsonServerProvider from "ra-data-json-server";

const dataProvider = jsonServerProvider("http://localhost:8080/api");

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

#### 5. Implement the Service

```java
@Service
public class UserService implements IRAServiceJS<User, Long> {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

#### 6. Create Your Controller

```java
@RestController
@RequestMapping("/api/users") // resource name: users
@CrossOrigin(origins = "*")
public class UserController extends RAControllerJS<User, Long> {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IRAServiceJS<User, Long> getService() {
        return userService;
    }
}
```

#### 7. Configure CORS (if needed)

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

1. **`RAController<T, ID>`**: Abstract controller that handles HTTP requests
2. **`IRAService<T, ID>`**: Service interface for business logic

Your implementation needs to:

- Extend `RAController` for your entity
- Implement `IRAService` for your business logic
- Return your service implementation from the `getService()` method

### Service Implementation

The `IRAService` interface requires you to implement:

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

`RAContoller` automatically provides these endpoints. These are also the ra-spring-data-provider end-points:

| Method | Endpoint                | React Admin Method | Description                     |
| ------ | ----------------------- | ------------------ | ------------------------------- |
| GET    | `/{resource}`           | `getList`          | Get paginated list with filters |
| GET    | `/{resource}?id=1&id=2` | `getMany`          | Get multiple records by IDs     |
| GET    | `/{resource}/{id}`      | `getOne`           | Get single record               |
| POST   | `/{resource}`           | `create`           | Create new record               |
| PUT    | `/{resource}/{id}`      | `update`           | Update single record            |
| DELETE | `/{resource}/{id}`      | `delete`           | Delete single record            |
| PUT    | `/{resource}?id=1&id=2` | `updateMany`       | Update multiple records (bulk)  |
| DELETE | `/{resource}?id=1&id=2` | `deleteMany`       | Delete multiple records (bulk)  |

**Note:** The [ra-spring-data-provider][ra-spring-data-provider] sends single requests with multiple `id` query parameters for bulk operations (updateMany and deleteMany), making them more efficient than the [ra-data-json-server][ra-data-json-server] approach, which sends individual requests for each record.

**Note:** For [ra-data-json-server] clients `IRAControllerJS` should be inherited instead of `IRAController`

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
