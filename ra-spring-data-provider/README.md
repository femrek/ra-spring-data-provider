# ra-spring-data-provider

A [React Admin](https://marmelab.com/react-admin/) data provider for Spring Boot REST APIs.

This package provides a data provider that follows JSON Server API conventions, specifically adapted for Spring Boot backends. It supports efficient bulk operations and is designed to work seamlessly with Spring Boot controllers implementing the IRAController interface from [ra-spring-json-server] library.

## Installation

```bash
npm install ra-spring-data-provider
# or
yarn add ra-spring-data-provider
```

## Usage

```jsx
import * as React from "react";
import { Admin, Resource } from "react-admin";
import raSpringDataProvider from "ra-spring-data-provider";

const dataProvider = jsonServerProvider("http://localhost:8080/api");

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="users" list={ListGuesser} />
  </Admin>
);

export default App;
```

## API Mapping

This data provider uses the JSON Server API format to communicate with the backend. Your Spring Boot API should follow these conventions:

| React Admin Method | HTTP Method | URL Example                                                   |
| ------------------ | ----------- | ------------------------------------------------------------- |
| `getList`          | `GET`       | `http://api.url/users?_sort=name&_order=ASC&_start=0&_end=24` |
| `getOne`           | `GET`       | `http://api.url/users/123`                                    |
| `getMany`          | `GET`       | `http://api.url/users?id=123&id=456`                          |
| `getManyReference` | `GET`       | `http://api.url/users?authorId=345`                           |
| `create`           | `POST`      | `http://api.url/users`                                        |
| `update`           | `PUT`       | `http://api.url/users/123`                                    |
| `updateMany`       | `PUT`       | `http://api.url/users?id=123&id=456` (with data in body)      |
| `delete`           | `DELETE`    | `http://api.url/users/123`                                    |
| `deleteMany`       | `DELETE`    | `http://api.url/users?id=123&id=456`                          |

## Backend Requirements

To use this data provider, your Spring Boot backend needs to implement JSON Server API conventions with proper query parameter handling and response headers.

**Recommended:** Use the **[ra-spring-json-server]** library which provides ready-to-use Spring Boot controller interfaces that are fully compatible with this data provider.

```xml
<!-- Maven -->
<dependency>
    <groupId>dev.femrek</groupId>
    <artifactId>ra-spring-json-server</artifactId>
    <version>0.2.1</version>
</dependency>
```

The library handles all the required endpoints, query parameters, headers, and bulk operations automatically.

### API Requirements

Your backend should support:

1. **Include the `X-Total-Count` header** in responses to `getList`, `getMany`, and `getManyReference` requests for pagination to work correctly.
2. **Support query parameters** for filtering, sorting, and pagination:
   - `_sort`: field name to sort by
   - `_order`: `ASC` or `DESC`
   - `_start`: index of first item to return
   - `_end`: index of last item to return
   - `id`: for filtering by multiple IDs (can appear multiple times)
   - Other filter parameters as needed
3. **Bulk operations** should accept multiple `id` parameters in a single request:
   - `updateMany`: `PUT /resource?id=1&id=2` with data in body
   - `deleteMany`: `DELETE /resource?id=1&id=2`

## Development

### Running the Example

The package includes an example React Admin application in the `example/` directory:

```bash
npm install
npm run dev
```

The example app will start on `http://localhost:3000` and expects a Spring Boot backend running on `http://localhost:8081`.

### Running Integration Tests

You can run the complete integration test suite from the project root:

```bash
cd .. && ./run-integration-tests.sh
```

This script will start the Spring Boot backend, run the tests, and clean up automatically.

## Test Cases

The tests cover:

- ✅ Display users list
- ✅ Create a new user
- ✅ Edit an existing user
- ✅ Delete a user
- ✅ Filter/search users
- ✅ Sort users

## Requirements

- Node.js 18+
- Java 17+

[ra-spring-json-server]: https://github.com/femrek/ra-spring-json-server
