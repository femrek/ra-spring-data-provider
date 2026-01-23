# Integration Tests

Playwright test suite for the React Admin data provider with Spring Boot backend.

## Test Files

### users.spec.js (8 tests)

Basic CRUD operations:

- Display, create, edit, delete users
- Bulk update and bulk delete
- Filter and sort

### data-provider.spec.js (10 tests)

Data provider methods:

- getList, getOne, getMany, getManyReference
- create, update, updateMany
- delete, deleteMany
- Pagination and sorting

### error-handling.spec.js (13 tests)

Error cases and edge conditions:

- Empty states
- Validation errors (required fields, email format)
- Long inputs and special characters
- Concurrent operations
- Non-existent records

### performance.spec.js (10 tests)

Performance benchmarks:

- Page load (< 5s), pagination (< 3s)
- Sorting and filtering (< 3s)
- Bulk operations (< 10s)
- Concurrent requests

### ui-ux.spec.js (17 tests)

UI components and accessibility:

- Page titles, buttons, forms
- Navigation and dialogs
- Loading states and notifications
- Keyboard navigation

### smoke.spec.js (1 test)

Basic application health check

**Total: 58 tests**

## Running Tests

```bash
# All tests
npm test

# Specific file
npm test users.spec.js

# With UI
npm test:ui

# Debug mode
npm test:headed
```

## Requirements

- Spring Boot backend running on `http://localhost:8081/api`
- Vite dev server (auto-starts on `http://localhost:3000`)
- Chromium browser (auto-installed with Playwright)

## Setup

```bash
npm install
npx playwright install chromium
```

## Configuration

- **Base URL**: `http://localhost:3000`
- **Timeout**: 60 seconds per test
- **Workers**: 1 (sequential)
- **Retries**: 0 locally, 2 in CI
- **Reporter**: HTML report in `playwright-report/`
