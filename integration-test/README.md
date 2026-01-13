# Integration Tests

This folder contains end-to-end integration tests for the React Admin Spring Boot library.

## Structure

- `src/` - React Admin application with ra-data-json-server
- `tests/` - Playwright test cases
- `package.json` - Node.js dependencies
- `playwright.config.js` - Playwright configuration

## Running Tests

### Automated (Recommended)

From the project root, run:

```bash
./run-integration-tests.sh
```

This script will:

1. Install npm dependencies
2. Install Playwright browsers
3. Start the Spring Boot test application on port 8081
4. Run Playwright tests against the React Admin UI
5. Clean up processes on exit

### Manual

1. Start the Spring Boot test server:

   ```bash
   mvn exec:java -Ptest-run
   ```

2. In another terminal, navigate to `integration-test/`:

   ```bash
   cd integration-test
   npm install
   npm run dev
   ```

3. In a third terminal, run tests:
   ```bash
   cd integration-test
   npm test
   ```

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
- Maven 3.6+
- Java 17+
