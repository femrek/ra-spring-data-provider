# React Admin Spring Data Provider

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot library that simplifies building [React Admin][react-admin] backends. This library provides a standardized REST API implementation that works with the [ra-spring-data-provider][ra-spring-data-provider] data provider, which is based on the [ra-data-json-server][ra-data-json-server] protocol.

Get started quickly with our [Quick Start Guide][quick-start].

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
- ✅ **OpenAPI Integration**: Automatically generated API documentation via OpenAPI annotations. Fully customizable — extend or add annotations in your controllers to enrich the generated documentation.

## Requirements

- **Java**: 17 or higher
- **Spring Boot**: 4 or higher recommended
- **Maven**: 3.6+ (or Gradle)

## Installation

### Backend

Add the dependency to your `pom.xml` if you use Maven:

```xml
<dependency>
    <groupId>dev.femrek</groupId>
    <artifactId>ra-spring-json-server</artifactId>
    <version>2.0.2</version>
</dependency>
```

Add the dependency to your `build.gradle` if you use Gradle:

```gradle
implementation 'dev.femrek:ra-spring-json-server:2.0.2'
```

### Frontend

Run following shell script to add the data provider:

```bash
npm install ra-spring-data-provider
```

## Quick Start

Ready to get started? Check out the **[Quick Start Guide][quick-start]** for step-by-step instructions to set up your first React Admin backend with this library.

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

- **`findWithFilters()`**: Query with filters, pagination, and sorting - returns Page<ResponseDTO>
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

| Method | Endpoint                                       | React Admin Method | Description                     |
| ------ | ---------------------------------------------- | ------------------ | ------------------------------- |
| GET    | `/{resource}`                                  | `getList`          | Get paginated list with filters |
| GET    | `/{resource}/many?id=1&id=2`                   | `getMany`          | Get multiple records by IDs     |
| GET    | `/{resource}/of/{target}/{targetId}?id=1&id=2` | `getManyReference` | Get records by reference        |
| GET    | `/{resource}/{id}`                             | `getOne`           | Get single record               |
| POST   | `/{resource}`                                  | `create`           | Create new record               |
| PUT    | `/{resource}/{id}`                             | `update`           | Update single record            |
| PUT    | `/{resource}?id=1&id=2`                        | `updateMany`       | Update multiple records (bulk)  |
| DELETE | `/{resource}/{id}`                             | `delete`           | Delete single record            |
| DELETE | `/{resource}?id=1&id=2`                        | `deleteMany`       | Delete multiple records (bulk)  |

### Query Parameters

#### getList, getManyReference

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
git clone https://github.com/femrek/ra-spring-data-provider.git
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
- [Spring Boot][spring-boot] - Backend framework that this library provides integration for.

## Resources

- [React Admin Documentation][react-admin-docs]
- [React Admin Data Provider Documentation][react-admin-dataproviders]
- [Spring Boot Documentation][spring-boot-docs]

<!-- Link Definitions -->

[react-admin]: https://marmelab.com/react-admin/
[ra-spring-data-provider]: https://github.com/femrek/ra-spring-data-provider/tree/main/ra-spring-data-provider
[ra-data-json-server]: https://github.com/marmelab/react-admin/tree/master/packages/ra-data-json-server
[spring-boot]: https://spring.io/projects/spring-boot
[license-apache]: LICENSE_APACHE
[license-mit]: LICENSE_MIT
[quick-start]: QUICK_START.md
[react-admin-docs]: https://marmelab.com/react-admin/Tutorial.html
[react-admin-dataproviders]: https://marmelab.com/react-admin/DataProviders.html
[spring-boot-docs]: https://spring.io/projects/spring-boot
[integration-test-files]: https://github.com/femrek/ra-spring-data-provider/tree/main/ra-spring-json-server/src/test/java/dev/femrek/reactadmindataprovider/integration
