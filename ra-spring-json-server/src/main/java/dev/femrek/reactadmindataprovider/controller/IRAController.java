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
     * Retrieves a list of entities with support for pagination, sorting, filtering, and multiple query modes.
     * This unified endpoint handles three ra-data-json-server operations:
     * <ol>
     *   <li><b>getList</b>: Standard paginated list with sorting and filtering</li>
     *   <li><b>getMany</b>: Retrieves multiple specific entities when 'id' parameter is present</li>
     *   <li><b>getManyReference</b>: Retrieves entities referencing another entity (via custom filters in allParams)</li>
     * </ol>
     *
     * <p>The response should include appropriate headers for ra-data-json-server compatibility,
     * particularly the Content-Range header for pagination support (e.g., "Content-Range: entities 0-9/100").</p>
     *
     * @param _start    the starting index for pagination (0-based) (required for getList)
     * @param _end      the ending index for pagination (exclusive) (required for getList)
     * @param _sort     the field name to sort by (default: "id")
     * @param _order    the sort direction, either "ASC" or "DESC" (default: "ASC")
     * @param id        optional list of specific entity IDs to retrieve (for getMany operation)
     * @param allParams map containing all query parameters, used for custom filtering and getManyReference
     * @return ResponseEntity containing a list of entities matching the criteria
     */
    @GetMapping
    ResponseEntity<List<T>> getList(
            @RequestParam(required = false, defaultValue = "-1") int _start,
            @RequestParam(required = false, defaultValue = "-1") int _end,
            @RequestParam(required = false, defaultValue = "id") String _sort,
            @RequestParam(required = false, defaultValue = "ASC") String _order,
            @RequestParam(required = false) String _embed,
            @RequestParam(required = false) List<ID> id,
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
