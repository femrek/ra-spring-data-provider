package dev.femrek.reactadmindataprovider.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
@Tag(name = "RA Controller", description = "Generic REST controller for ra-spring-data-provider compatibility" +
        "Add @Tag annotation to your controller implementation to provide API documentation details specific to your resource.")
public interface IRAController<T, C, ID> {
    /**
     * Retrieves a paginated list of entities with support for sorting and filtering.
     * This endpoint implements ra-spring-data-provider's <b>getList</b> operation.
     *
     * <p>This method returns a subset of entities based on the pagination parameters (_start and _end).
     * The results can be sorted by any field in ascending or descending order. Custom filters can be
     * applied through additional query parameters passed in allParams.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/posts?_start=0&amp;_end=10&amp;_sort=title&amp;_order=ASC&amp;status=published</pre>
     *
     * <p>The response must include an <code>X-Total-Count</code> header containing the total number
     * of entities matching the filter criteria (not just the current page). This header is essential
     * for ra-spring-data-provider to calculate pagination correctly.</p>
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
    @Operation(
            summary = "GetList: Get paginated list of entities with filtering",
            description = """
                    Retrieves a paginated list of entities with support for sorting and filtering.
                    Implements ra-spring-data-provider's getList operation.
                    
                    This method returns a subset of entities based on the pagination parameters (_start and _end).
                    The results can be sorted by any field in ascending or descending order. Custom filters can be
                    applied through additional query parameters passed in allParams.
                    
                    The response includes an X-Total-Count header containing the total number of entities
                    matching the filter criteria (not just the current page). This header is essential
                    for ra-spring-data-provider to calculate pagination correctly.
                    
                    Example: GET /api/posts?_start=0&_end=10&_sort=title&_order=ASC&status=published
                    """,
            operationId = "getList"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<T>> getList(
            @Parameter(description = "Starting index for pagination (0-based, inclusive)", required = true, example = "0")
            @RequestParam(name = "_start") int _start,
            @Parameter(description = "Ending index for pagination (0-based, exclusive)", required = true, example = "10")
            @RequestParam(name = "_end") int _end,
            @Parameter(description = "Field name to sort by", example = "id")
            @RequestParam(name = "_sort", required = false, defaultValue = "id") String _sort,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(name = "_order", required = false, defaultValue = "ASC") String _order,
            @Parameter(description = "Optional parameter to embed related resources (implementation-specific)")
            @RequestParam(name = "_embed", required = false) String _embed,
            @Parameter(description = "Additional query parameters for filtering by entity fields")
            @RequestParam Map<String, String> allParams
    );

    /**
     * Retrieves multiple specific entities by their unique identifiers.
     * This endpoint implements ra-spring-data-provider's <b>getMany</b> operation.
     *
     * <p>Unlike getList, this operation does not use pagination. It simply returns all entities
     * with the specified IDs. This is commonly used when the client needs to fetch multiple
     * specific records, such as when displaying relationships or selected items.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/posts/many?id=1&amp;id=5&amp;id=12</pre>
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
    @Operation(
            summary = "GetMany: Get multiple entities by IDs",
            description = """
                    Retrieves multiple specific entities by their unique identifiers.
                    Implements ra-spring-data-provider's getMany operation.
                    
                    Unlike getList, this operation does not use pagination. It simply returns all entities
                    with the specified IDs. This is commonly used when the client needs to fetch multiple
                    specific records, such as when displaying relationships or selected items.
                    
                    If an ID doesn't exist, it is typically omitted from the response rather than returning an error.
                    The order of returned entities may not match the order of requested IDs.
                    
                    Example: GET /api/posts/many?id=1&id=5&id=12
                    """,
            operationId = "getMany"
    )
    @GetMapping(value = "/many", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<T>> getMany(
            @Parameter(description = "List of entity IDs to retrieve", required = true, example = "[1, 5, 12]")
            @RequestParam(name = "id") List<ID> id
    );

    /**
     * Retrieves a paginated list of entities that reference another specific entity.
     * This endpoint implements ra-spring-data-provider's <b>getManyReference</b> operation.
     *
     * <p>This operation is used to fetch entities related to a specific record. For example,
     * retrieving all comments for a particular post, or all orders for a specific customer.
     * Unlike getList, the filter is based on a reference relationship rather than arbitrary criteria.</p>
     *
     * <p><b>Example request:</b></p>
     * <pre>GET /api/comments/of/postId/123?_start=0&amp;_end=10&amp;_sort=createdAt&amp;_order=DESC</pre>
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
     * with X-Total-Count header
     */
    @Operation(
            summary = "GetManyReference: Get entities that reference another entity",
            description = """
                    Retrieves a paginated list of entities that reference another specific entity.
                    Implements ra-spring-data-provider's getManyReference operation.
                    
                    This operation is used to fetch entities related to a specific record. For example,
                    retrieving all comments for a particular post, or all orders for a specific customer.
                    Unlike getList, the filter is based on a reference relationship rather than arbitrary criteria.
                    
                    The response includes an X-Total-Count header containing the total number of entities
                    that reference the specified target entity. This is essential for pagination in React Admin.
                    
                    Example: GET /api/comments/of/postId/123?_start=0&_end=10&_sort=createdAt&_order=DESC
                    This retrieves comments where the postId field equals 123, paginated and sorted.
                    """,
            operationId = "getManyReference"
    )
    @GetMapping(value = "/of/{target}/{targetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<T>> getManyReference(
            @Parameter(description = "Name of the field that references the target entity", required = true, example = "userId")
            @PathVariable(name = "target") String target,
            @Parameter(description = "ID of the target entity being referenced", required = true, example = "123")
            @PathVariable(name = "targetId") String targetId,
            @Parameter(description = "Starting index for pagination (0-based, inclusive)", required = true, example = "0")
            @RequestParam(name = "_start") int _start,
            @Parameter(description = "Ending index for pagination (0-based, exclusive)", required = true, example = "10")
            @RequestParam(name = "_end") int _end,
            @Parameter(description = "Field name to sort by", example = "id")
            @RequestParam(name = "_sort", required = false, defaultValue = "id") String _sort,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(name = "_order", required = false, defaultValue = "ASC") String _order,
            @Parameter(description = "Optional parameter to embed related resources (implementation-specific)")
            @RequestParam(name = "_embed", required = false) String _embed,
            @Parameter(description = "Additional query parameters for filtering")
            @RequestParam Map<String, String> allParams
    );

    /**
     * Retrieves a single entity by its identifier.
     * This endpoint implements ra-spring-data-provider's getOne operation.
     *
     * @param id the unique identifier of the entity to retrieve
     * @return ResponseEntity containing the requested entity
     */
    @Operation(
            summary = "GetOne: Get single entity by ID",
            description = """
                    Retrieves a single entity by its unique identifier.
                    Implements ra-spring-data-provider's getOne operation.
                    """
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> getOne(
            @Parameter(description = "Unique identifier of the entity to retrieve", required = true, example = "1")
            @PathVariable(name = "id") ID id
    );

    /**
     * Creates a new entity.
     * This endpoint implements ra-spring-data-provider's create operation.
     *
     * @param data the request body for the new entity to create
     * @return ResponseEntity containing the created entity with generated ID and any server-side defaults,
     * typically with HTTP status 201 Created
     */
    @Operation(
            summary = "Create: Create a new entity",
            description = """
                    Creates a new entity with the provided data.
                    Implements ra-spring-data-provider's create operation.
                    Returns the created entity with generated ID and server-side defaults.
                    """
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> create(
            @Parameter(description = "Entity data to create", required = true)
            @RequestBody C data
    );

    /**
     * Updates an existing entity with the provided fields.
     * This endpoint implements ra-spring-data-provider's update operation with support for partial updates.
     *
     * @param id     the unique identifier of the entity to update
     * @param fields map of field names to new values; only provided fields should be updated
     * @return ResponseEntity containing the updated entity
     */
    @Operation(
            summary = "Update: Update an existing entity",
            description = """
                    Updates an existing entity with the provided field values.
                    Implements ra-spring-data-provider's update operation with support for partial updates.
                    Only the fields provided in the request body will be updated.
                    """
    )
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<T> update(
            @Parameter(description = "Unique identifier of the entity to update", required = true, example = "1")
            @PathVariable(name = "id") ID id,
            @Parameter(description = "Map of field names to new values for partial update", required = true)
            @RequestBody Map<String, Object> fields
    );

    /**
     * Deletes a single entity by its identifier.
     * This endpoint implements ra-spring-data-provider's delete operation.
     *
     * @param id the unique identifier of the entity to delete
     * @return ResponseEntity with no content (204 No Content)
     */
    @Operation(
            summary = "Delete: Delete a single entity",
            description = """
                    Deletes a single entity by its unique identifier.
                    Implements ra-spring-data-provider's delete operation.
                    """
    )
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the entity to delete", required = true, example = "1")
            @PathVariable(name = "id") ID id
    );

    /**
     * Updates multiple entities with the same field values in a single operation.
     * This endpoint implements ra-spring-data-provider's updateMany operation for bulk updates.
     *
     * <p><b>Note:</b> In standard ra-spring-data-provider implementations, individual PUT requests are often
     * sent for each record. This endpoint is primarily used when custom bulk actions are configured
     * or when optimizing for batch operations.</p>
     *
     * @param id     list of entity identifiers to update (optional, defaults to empty list)
     * @param fields map of field names to new values; these fields will be updated for all specified entities
     * @return ResponseEntity containing a list of updated entity IDs
     */
    @Operation(
            summary = "UpdateMany: Update multiple entities",
            description = """
                    Updates multiple entities with the same field values in a single operation.
                    Implements ra-spring-data-provider's updateMany operation for bulk updates.
                    Returns a list of updated entity IDs.
                    """
    )
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ID>> updateMany(
            @Parameter(description = "List of entity IDs to update", example = "[1, 2, 3]")
            @RequestParam(name = "id", required = false) List<ID> id,
            @Parameter(description = "Map of field names to new values for bulk update", required = true)
            @RequestBody Map<String, Object> fields
    );

    /**
     * Deletes multiple entities in a single operation.
     * This endpoint implements ra-spring-data-provider's deleteMany operation for bulk deletions.
     *
     * @param id list of entity identifiers to delete (optional, defaults to empty list)
     * @return ResponseEntity containing a list of deleted entity IDs
     */
    @Operation(
            summary = "DeleteMany: Delete multiple entities",
            description = """
                    Deletes multiple entities in a single operation.
                    Implements ra-spring-data-provider's deleteMany operation for bulk deletions.
                    Returns a list of deleted entity IDs.
                    """
    )
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ID>> deleteMany(
            @Parameter(description = "List of entity IDs to delete", example = "[1, 2, 3]")
            @RequestParam(name = "id", required = false) List<ID> id
    );
}
