package dev.femrek.reactadmindataprovider.jsonserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface defining REST endpoints compatible with ra-data-json-server.
 * This controller provides a unified API for CRUD operations that seamlessly integrates
 * with ra-data-json-server's expectations, including support for pagination,
 * sorting, filtering, and bulk operations.
 *
 * @param <T>  the entity type managed by this controller
 * @param <ID> the entity identifier type, must be Serializable
 */
public interface IRAControllerJS<T, ID> {
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
     * @param _start    the starting index for pagination (0-based, default: 0)
     * @param _end      the ending index for pagination (exclusive, default: 10)
     * @param _sort     the field name to sort by (default: "id")
     * @param _order    the sort direction, either "ASC" or "DESC" (default: "ASC")
     * @param id        optional list of specific entity IDs to retrieve (for getMany operation)
     * @param q         optional search query string for text-based filtering across multiple fields
     * @param allParams map containing all query parameters, used for custom filtering and getManyReference
     * @return ResponseEntity containing a list of entities matching the criteria
     */
    @GetMapping
    ResponseEntity<List<T>> getList(
            @RequestParam(required = false, defaultValue = "0") int _start,
            @RequestParam(required = false, defaultValue = "10") int _end,
            @RequestParam(required = false, defaultValue = "id") String _sort,
            @RequestParam(required = false, defaultValue = "ASC") String _order,
            @RequestParam(required = false) List<ID> id,
            @RequestParam(required = false) String q,
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
     * @param entity the entity to create, provided in the request body
     * @return ResponseEntity containing the created entity with generated ID and any server-side defaults,
     * typically with HTTP status 201 Created
     */
    @PostMapping
    ResponseEntity<T> create(@RequestBody T entity);

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
}
