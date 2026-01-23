import { DataProvider } from "ra-core";

/**
 * Creates a React Admin data provider for Spring Boot REST APIs
 *
 * @param apiUrl - The base URL of your Spring Boot API (e.g., 'http://localhost:8081/api')
 * @param httpClient - Optional custom HTTP client function (defaults to fetchUtils.fetchJson)
 * @returns A React Admin DataProvider configured for Spring Boot
 *
 * @example
 * import raSpringDataProvider from 'ra-spring-data-provider';
 *
 * const dataProvider = raSpringDataProvider('http://localhost:8081/api');
 *
 * const App = () => (
 *   <Admin dataProvider={dataProvider}>
 *     <Resource name="users" list={UserList} />
 *   </Admin>
 * );
 */
declare const raSpringDataProvider: (
  apiUrl: string,
  httpClient?: (
    url: string,
    options?: any,
  ) => Promise<{ headers: Headers; json: any }>,
) => DataProvider;

export default raSpringDataProvider;
