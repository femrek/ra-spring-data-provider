import {
  DataProvider,
  GetListParams,
  GetOneParams,
  GetManyParams,
  GetManyReferenceParams,
  UpdateParams,
  UpdateManyParams,
  CreateParams,
  DeleteParams,
  DeleteManyParams,
} from "react-admin";

export interface SpringDataProviderOptions {
  apiUrl: string;
  httpClient?: (
    url: string,
    options?: any
  ) => Promise<{ json: any; status: number; headers: Headers }>;
}

/**
 * Maps React Admin queries to a Spring Data REST API
 *
 * @example
 * import { springDataProvider } from 'ra-spring-data-provider';
 * const dataProvider = springDataProvider({ apiUrl: 'http://localhost:8080/api' });
 */
export const springDataProvider = (
  options: SpringDataProviderOptions
): DataProvider => {
  const { apiUrl, httpClient = fetchJson } = options;

  return {
    getList: async (resource: string, params: GetListParams) => {
      const { page, perPage } = params.pagination!;
      const { field, order } = params.sort!;
      const query = {
        page: page - 1, // Spring Data uses 0-indexed pages
        size: perPage,
        sort: `${field},${order.toLowerCase()}`,
        ...flattenFilters(params.filter),
      };

      const url = `${apiUrl}/${resource}?${stringify(query)}`;
      const { json } = await httpClient(url);

      return {
        data: json._embedded?.[resource] || json.content || [],
        total: json.page?.totalElements || json.totalElements || 0,
      };
    },

    getOne: async (resource: string, params: GetOneParams) => {
      const url = `${apiUrl}/${resource}/${params.id}`;
      const { json } = await httpClient(url);
      return { data: json };
    },

    getMany: async (resource: string, params: GetManyParams) => {
      const query = {
        id: params.ids,
      };
      const url = `${apiUrl}/${resource}/search/findByIdIn?${stringify(query)}`;

      try {
        const { json } = await httpClient(url);
        return {
          data: json._embedded?.[resource] || json || [],
        };
      } catch (error) {
        // Fallback: fetch items individually
        const promises = params.ids.map((id) =>
          httpClient(`${apiUrl}/${resource}/${id}`).then(({ json }) => json)
        );
        const data = await Promise.all(promises);
        return { data };
      }
    },

    getManyReference: async (
      resource: string,
      params: GetManyReferenceParams
    ) => {
      const { page, perPage } = params.pagination!;
      const { field, order } = params.sort!;
      const query = {
        page: page - 1,
        size: perPage,
        sort: `${field},${order.toLowerCase()}`,
        [params.target]: params.id,
        ...flattenFilters(params.filter),
      };

      const url = `${apiUrl}/${resource}?${stringify(query)}`;
      const { json } = await httpClient(url);

      return {
        data: json._embedded?.[resource] || json.content || [],
        total: json.page?.totalElements || json.totalElements || 0,
      };
    },

    create: async (resource: string, params: CreateParams) => {
      const url = `${apiUrl}/${resource}`;
      const { json } = await httpClient(url, {
        method: "POST",
        body: JSON.stringify(params.data),
      });
      return { data: json };
    },

    update: async (resource: string, params: UpdateParams) => {
      const url = `${apiUrl}/${resource}/${params.id}`;
      const { json } = await httpClient(url, {
        method: "PUT",
        body: JSON.stringify(params.data),
      });
      return { data: json };
    },

    updateMany: async (resource: string, params: UpdateManyParams) => {
      const promises = params.ids.map((id) =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: "PUT",
          body: JSON.stringify(params.data),
        })
      );
      await Promise.all(promises);
      return { data: params.ids };
    },

    delete: async (resource: string, params: DeleteParams) => {
      const url = `${apiUrl}/${resource}/${params.id}`;
      await httpClient(url, {
        method: "DELETE",
      });
      return { data: params.previousData as any };
    },

    deleteMany: async (resource: string, params: DeleteManyParams) => {
      const promises = params.ids.map((id) =>
        httpClient(`${apiUrl}/${resource}/${id}`, {
          method: "DELETE",
        })
      );
      await Promise.all(promises);
      return { data: params.ids };
    },
  };
};

/**
 * Default HTTP client using fetch API
 */
async function fetchJson(url: string, options: any = {}) {
  const requestHeaders = new Headers({
    "Content-Type": "application/json",
    ...options.headers,
  });

  const response = await fetch(url, {
    ...options,
    headers: requestHeaders,
  });

  const text = await response.text();
  const json = text ? JSON.parse(text) : {};

  if (!response.ok) {
    throw new Error(json.message || response.statusText);
  }

  return {
    status: response.status,
    headers: response.headers,
    json,
  };
}

/**
 * Flatten nested filter objects for query parameters
 */
function flattenFilters(filter: any): Record<string, any> {
  const result: Record<string, any> = {};

  Object.keys(filter).forEach((key) => {
    const value = filter[key];
    if (typeof value === "object" && value !== null && !Array.isArray(value)) {
      // Handle nested objects (e.g., { name: { like: 'John' } })
      Object.keys(value).forEach((subKey) => {
        result[`${key}.${subKey}`] = value[subKey];
      });
    } else {
      result[key] = value;
    }
  });

  return result;
}

/**
 * Convert object to query string
 */
function stringify(params: Record<string, any>): string {
  return Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null)
    .map((key) => {
      const value = params[key];
      if (Array.isArray(value)) {
        return value
          .map((v) => `${encodeURIComponent(key)}=${encodeURIComponent(v)}`)
          .join("&");
      }
      return `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
    })
    .join("&");
}
