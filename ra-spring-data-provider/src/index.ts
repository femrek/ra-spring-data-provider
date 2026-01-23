import queryString from "query-string";
import { fetchUtils, DataProvider } from "ra-core";

/**
 * Creates a React Admin data provider for Spring Boot REST APIs following JSON Server conventions.
 *
 * This data provider is designed to work with Spring Boot controllers that implement the
 * IRAController interface, supporting JSON Server-style query parameters and response formats.
 *
 * @param apiUrl - The base URL of your Spring Boot API (e.g., 'http://localhost:8081/api')
 * @param httpClient - Optional custom HTTP client function (defaults to fetchUtils.fetchJson)
 *
 * @returns A React Admin DataProvider instance
 *
 * @example
 * ```tsx
 * import { Admin, Resource } from 'react-admin';
 * import raSpringDataProvider from 'ra-spring-data-provider';
 *
 * const dataProvider = raSpringDataProvider('http://localhost:8081/api');
 *
 * const App = () => (
 *   <Admin dataProvider={dataProvider}>
 *     <Resource name="users" list={UserList} edit={UserEdit} create={UserCreate} />
 *   </Admin>
 * );
 * ```
 *
 * @remarks
 * **API Requirements:**
 * - GET endpoints must return X-Total-Count header for pagination
 * - List queries use _start, _end, _sort, _order query parameters
 * - Bulk operations (updateMany, deleteMany) use multiple id query parameters
 * - CORS must expose the X-Total-Count header
 *
 * **Supported Operations:**
 * - `getList`: GET /resource?_start=0&_end=10&_sort=id&_order=ASC
 * - `getOne`: GET /resource/123
 * - `getMany`: GET /resource?id=123&id=456&id=789
 * - `getManyReference`: GET /resource?author_id=12&_start=0&_end=10
 * - `create`: POST /resource with JSON body
 * - `update`: PUT /resource/123 with JSON body
 * - `updateMany`: PUT /resource?id=123&id=456 with JSON body (bulk update)
 * - `delete`: DELETE /resource/123
 * - `deleteMany`: DELETE /resource?id=123&id=456 (bulk delete)
 *
 * **Spring Boot Adaptations:**
 * - Bulk operations (updateMany, deleteMany) send single requests with multiple id parameters
 * - updateMany sends data fields in request body to update all specified records
 * - This differs from standard ra-data-json-server which sends individual requests for bulk operations
 *
 * **Embedded Resources:**
 * Use the `meta.embed` parameter to request related records:
 * ```tsx
 * useGetOne('posts', { id: 1, meta: { embed: 'author' } })
 * ```
 */
export default (
  apiUrl: string,
  httpClient = fetchUtils.fetchJson,
): DataProvider => ({
  getList: async (resource, params) => {
    const { page, perPage } = params.pagination || {};
    const { field, order } = params.sort || {};
    const query = {
      ...fetchUtils.flattenObject(params.filter),
      _sort: field,
      _order: order,
      _start:
        page != null && perPage != null ? (page - 1) * perPage : undefined,
      _end: page != null && perPage != null ? page * perPage : undefined,
      _embed: params?.meta?.embed,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;

    const { headers, json } = await httpClient(url, {
      signal: params?.signal,
    });
    if (!headers.has("x-total-count")) {
      throw new Error(
        "The X-Total-Count header is missing in the HTTP Response. The jsonServer Data Provider expects responses for lists of resources to contain this header with the total number of results to build the pagination. If you are using CORS, did you declare X-Total-Count in the Access-Control-Expose-Headers header?",
      );
    }
    const totalString = headers.get("x-total-count")!.split("/").pop();
    if (totalString == null) {
      throw new Error(
        "The X-Total-Count header is invalid in the HTTP Response.",
      );
    }
    return { data: json, total: parseInt(totalString, 10) };
  },

  getOne: async (resource, params) => {
    let url = `${apiUrl}/${resource}/${params.id}`;
    if (params?.meta?.embed) {
      url += `?_embed=${params.meta.embed}`;
    }
    const { json } = await httpClient(url, { signal: params?.signal });
    return { data: json };
  },

  getMany: async (resource, params) => {
    const query = {
      id: params.ids,
      _embed: params?.meta?.embed,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;
    const { json } = await httpClient(url, { signal: params?.signal });
    return { data: json };
  },

  getManyReference: async (resource, params) => {
    const { page, perPage } = params.pagination;
    const { field, order } = params.sort;
    const query = {
      ...fetchUtils.flattenObject(params.filter),
      [params.target]: params.id,
      _sort: field,
      _order: order,
      _start: (page - 1) * perPage,
      _end: page * perPage,
      _embed: params?.meta?.embed,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;

    const { headers, json } = await httpClient(url, {
      signal: params?.signal,
    });

    if (!headers.has("x-total-count")) {
      throw new Error(
        "The X-Total-Count header is missing in the HTTP Response. The jsonServer Data Provider expects responses for lists of resources to contain this header with the total number of results to build the pagination. If you are using CORS, did you declare X-Total-Count in the Access-Control-Expose-Headers header?",
      );
    }
    const totalString = headers.get("x-total-count")!.split("/").pop();
    if (totalString == null) {
      throw new Error(
        "The X-Total-Count header is invalid in the HTTP Response.",
      );
    }
    return { data: json, total: parseInt(totalString, 10) };
  },

  update: async (resource, params) => {
    const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "PUT",
      body: JSON.stringify(params.data),
    });
    return { data: json };
  },

  // Spring Boot bulk update: PUT /resource?id=1&id=2&id=3 with data in body
  updateMany: async (resource, params) => {
    const query = {
      id: params.ids,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;
    const { json } = await httpClient(url, {
      method: "PUT",
      body: JSON.stringify(params.data),
    });
    return { data: json };
  },

  create: async (resource, params) => {
    const { json } = await httpClient(`${apiUrl}/${resource}`, {
      method: "POST",
      body: JSON.stringify(params.data),
    });
    return { data: { ...params.data, ...json } as any };
  },

  delete: async (resource, params) => {
    const { json } = await httpClient(`${apiUrl}/${resource}/${params.id}`, {
      method: "DELETE",
    });
    return { data: json };
  },

  // Spring Boot bulk delete: DELETE /resource?id=1&id=2&id=3
  deleteMany: async (resource, params) => {
    const query = {
      id: params.ids,
    };
    const url = `${apiUrl}/${resource}?${queryString.stringify(query)}`;
    const { json } = await httpClient(url, {
      method: "DELETE",
    });
    return { data: json };
  },
});
