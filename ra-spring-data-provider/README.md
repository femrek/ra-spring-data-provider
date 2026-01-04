# React Admin Spring Data Provider

A React Admin data provider for Spring Data REST APIs.

## Installation

```bash
npm install ra-spring-data-provider
# or
yarn add ra-spring-data-provider
```

## Usage

```tsx
import { Admin, Resource } from 'react-admin';
import { springDataProvider } from 'ra-spring-data-provider';

const dataProvider = springDataProvider({
  apiUrl: 'http://localhost:8080/api',
});

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="users" />
    <Resource name="posts" />
  </Admin>
);

export default App;
```

## Features

- ✅ Full CRUD support (getList, getOne, getMany, getManyReference, create, update, updateMany, delete, deleteMany)
- ✅ Pagination with Spring Data's page/size parameters
- ✅ Sorting support
- ✅ Filtering support
- ✅ Spring Data REST HAL format support (`_embedded`)
- ✅ Automatic handling of Spring's 0-indexed pages
- ✅ Custom HTTP client support

## API

### `springDataProvider(options)`

Creates a data provider for Spring Data REST APIs.

#### Options

- `apiUrl` (required): The base URL of your Spring Data REST API
- `httpClient` (optional): Custom HTTP client function

#### Example with custom HTTP client

```tsx
import { fetchUtils } from 'react-admin';
import { springDataProvider } from 'ra-spring-data-provider';

const httpClient = (url: string, options: any = {}) => {
  if (!options.headers) {
    options.headers = new Headers({ Accept: 'application/json' });
  }
  // Add authentication token
  options.headers.set('Authorization', `Bearer ${localStorage.getItem('token')}`);
  return fetchUtils.fetchJson(url, options);
};

const dataProvider = springDataProvider({
  apiUrl: 'http://localhost:8080/api',
  httpClient,
});
```

## Spring Data REST Compatibility

This provider is designed to work with Spring Data REST's default conventions:

- Resources are accessed at `/{resource}` endpoints
- Pagination uses `page` (0-indexed) and `size` parameters
- Sorting uses the format `sort=field,direction`
- Spring Data REST HAL format with `_embedded` responses is supported
- Standard HTTP methods (GET, POST, PUT, DELETE)

### Example Spring Data REST Response

```json
{
  "_embedded": {
    "users": [
      { "id": 1, "name": "John" },
      { "id": 2, "name": "Jane" }
    ]
  },
  "page": {
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "number": 0
  }
}
```

## Development

```bash
# Install dependencies
npm install

# Build the library
npm run build

# Lint
npm run lint
```

## License

MIT
