package dev.femrek.reactadmindataprovider.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface defining REST endpoints compatible with ra-spring-data-provider.
 * This controller provides a unified API for CRUD operations that seamlessly integrates
 * with ra-spring-data-provider's expectations, including support for pagination,
 * sorting, filtering, and bulk operations.
 *
 * @param <T>  the Response DTO type for this resource
 * @param <C>  the Create DTO type for this resource
 * @param <ID> the type of the entity's identifier
 */
public interface IRAController<T, C, ID> {
    /**
     * Retrieves a paginated list of entities with support for sorting and filtering.
     * This endpoint implements ra-data-json-server's <b>getList</b> operation.
     *
     * <p>This method returns a subset of entities based on the pagination parameters (_start and _end).
     * The results can be sorted by any field in ascending or descending order. Custom filters can be
     * applied through additional query parameters passed in allParams.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/posts?_start=0&_end=10&_sort=title&_order=ASC&status=published</pre>
     *
     * <p>The response must include an <code>X-Total-Count</code> header containing the total number
     * of entities matching the filter criteria (not just the current page). This header is essential
     * for ra-data-json-server to calculate pagination correctly.</p>
     *
     * <p><b>Response headers:</b></p>
     * <ul>
     *   <li><code>X-Total-Count</code>: Total number of entities matching the filter</li>
     *   <li><code>Access-Control-Expose-Headers</code>: Must include "X-Total-Count"</li>
     * </ul>
     *
     * @param _start    the starting index for pagination (0-based, inclusive)
     * @param _end      the ending index for pagination (0-based, exclusive)
     * @param _sort     the field name to sort by (default: "id")
     * @param _order    the sort direction, either "ASC" or "DESC" (default: "ASC")
     * @param _embed    optional parameter to embed related resources (implementation-specific)
     * @param allParams map containing all query parameters, including custom filters
     * @return ResponseEntity containing a list of entities for the requested page with X-Total-Count header
     */
    @GetMapping
    ResponseEntity<List<T>> getList(
            @RequestParam int _start,
            @RequestParam int _end,
            @RequestParam(required = false, defaultValue = "id") String _sort,
            @RequestParam(required = false, defaultValue = "ASC") String _order,
            @RequestParam(required = false) String _embed,
            @RequestParam Map<String, String> allParams
    );

    /**
     * Retrieves multiple specific entities by their unique identifiers.
     * This endpoint implements ra-data-json-server's <b>getMany</b> operation.
     *
     * <p>Unlike getList, this operation does not use pagination. It simply returns all entities
     * with the specified IDs. This is commonly used when the client needs to fetch multiple
     * specific records, such as when displaying relationships or selected items.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/posts/many?id=1&id=5&id=12</pre>
     *
     * <p>The response contains only the entities whose IDs were provided in the request.
     * If an ID doesn't exist, it is typically omitted from the response (rather than returning an error).
     * The order of returned entities may not match the order of requested IDs.</p>
     *
     * <p><b>Note:</b> This endpoint does not return pagination headers since all requested
     * entities are returned in a single response.</p>
     *
     * @param id list of entity identifiers to retrieve
     * @return ResponseEntity containing a list of entities with the specified IDs
     */
    @GetMapping("/many")
    ResponseEntity<List<T>> getMany(@RequestParam List<ID> id);

    /**
     * Retrieves a paginated list of entities that reference another specific entity.
     * This endpoint implements ra-data-json-server's <b>getManyReference</b> operation.
     *
     * <p>This operation is used to fetch entities related to a specific record. For example,
     * retrieving all comments for a particular post, or all orders for a specific customer.
     * Unlike getList, the filter is based on a reference relationship rather than arbitrary criteria.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/comments/of/postId/123?_start=0&_end=10&_sort=createdAt&_order=DESC</pre>
     *
     * <p>This would retrieve comments where the postId field equals 123, paginated and sorted.</p>
     *
     * <p>The response must include an <code>X-Total-Count</code> header containing the total number
     * of entities that reference the specified target entity. This is essential for pagination
     * in the React Admin interface.</p>
     *
     * <p><b>Response headers:</b></p>
     * <ul>
     *   <li><code>X-Total-Count</code>: Total number of entities referencing the target entity</li>
     *   <li><code>Access-Control-Expose-Headers</code>: Must include "X-Total-Count"</li>
     * </ul>
     *
     * @param target    the name of the field that references the target entity (e.g., "postId", "userId")
     * @param targetId  the ID of the target entity being referenced (e.g., "123")
     * @param _start    the starting index for pagination (0-based, inclusive)
     * @param _end      the ending index for pagination (0-based, exclusive)
     * @param _sort     the field name to sort by (default: "id")
     * @param _order    the sort direction, either "ASC" or "DESC" (default: "ASC")
     * @param _embed    optional parameter to embed related resources (implementation-specific)
     * @param allParams map containing all query parameters, which may include additional filters
     * @return ResponseEntity containing a paginated list of entities that reference the target entity,
     *         with X-Total-Count header
     */
    @GetMapping("/of/{target}/{targetId}")
    ResponseEntity<List<T>> getManyReference(
            @PathVariable String target,
            @PathVariable String targetId,
            @RequestParam int _start,
            @RequestParam int _end,
            @RequestParam(required = false, defaultValue = "id") String _sort,
            @RequestParam(required = false, defaultValue = "ASC") String _order,
            @RequestParam(required = false) String _embed,
            @RequestParam Map<String, String> allParams
    );

    /**
     * Retrieves a single entity by its identifier.
     * This endpoint implements ra-data-json-server's getOne operation.
     *
     * @param id the unique identifier of the entity to retrieve
     * @return ResponseEntity containing the requested entity
     */
    @GetMapping("/{id}")
    ResponseEntity<T> getOne(@PathVariable ID id);

    /**
     * Creates a new entity.
     * This endpoint implements ra-data-json-server's create operation.
     *
     * @param data the request body for the new entity to create
     * @return ResponseEntity containing the created entity with generated ID and any server-side defaults,
     * typically with HTTP status 201 Created
     */
    @PostMapping
    ResponseEntity<T> create(@RequestBody C data);

    /**
     * Updates an existing entity with the provided fields.
     * This endpoint implements ra-data-json-server's update operation with support for partial updates.
     *
     * @param id     the unique identifier of the entity to update
     * @param fields map of field names to new values; only provided fields should be updated
     * @return ResponseEntity containing the updated entity
     */
    @PutMapping("/{id}")
    ResponseEntity<T> update(@PathVariable ID id, @RequestBody Map<String, Object> fields);

    /**
     * Deletes a single entity by its identifier.
     * This endpoint implements ra-data-json-server's delete operation.
     *
     * @param id the unique identifier of the entity to delete
     * @return ResponseEntity with no content (204 No Content)
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable ID id);

    /**
     * Updates multiple entities with the same field values in a single operation.
     * This endpoint implements ra-data-json-server's updateMany operation for bulk updates.
     *
     * <p><b>Note:</b> In standard ra-data-json-server implementations, individual PUT requests are often
     * sent for each record. This endpoint is primarily used when custom bulk actions are configured
     * or when optimizing for batch operations.</p>
     *
     * @param id     list of entity identifiers to update (optional, defaults to empty list)
     * @param fields map of field names to new values; these fields will be updated for all specified entities
     * @return ResponseEntity containing a list of updated entity IDs
     */
    @PutMapping
    ResponseEntity<List<ID>> updateMany(@RequestParam(required = false) List<ID> id, @RequestBody Map<String, Object> fields);

    /**
     * Deletes multiple entities in a single operation.
     * This endpoint implements ra-data-json-server's deleteMany operation for bulk deletions.
     *
     * @param id list of entity identifiers to delete (optional, defaults to empty list)
     * @return ResponseEntity containing a list of deleted entity IDs
     */
    @DeleteMapping
    ResponseEntity<List<ID>> deleteMany(@RequestParam(required = false) List<ID> id);
}
