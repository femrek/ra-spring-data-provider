# ra-spring-data-provider

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

The [React Admin](https://marmelab.com/react-admin/) data provider for [ra-spring-json-server].

This package provides a data provider that follows JSON Server API conventions, specifically adapted for Spring Boot backends. It supports efficient bulk operations and is designed to work seamlessly with Spring Boot controllers implementing the `IRAController` interface from [ra-spring-json-server] library.

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
| `updateMany`       | `PUT`       | `http://api.url/users?id=123&id=456`                          |
| `delete`           | `DELETE`    | `http://api.url/users/123`                                    |
| `deleteMany`       | `DELETE`    | `http://api.url/users?id=123&id=456`                          |

## Backend Requirements

For the Spring Boot backend implementation, use the **[ra-spring-json-server]** library which provides all the necessary endpoints and configurations to work with this data provider.

## Development

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

## License

This project is dual-licensed under:

- [Apache License 2.0](LICENSE_APACHE)
- [MIT License](LICENSE_MIT)

You may choose either license for your use of this library.

[ra-spring-json-server]: https://github.com/femrek/ra-spring-json-server
