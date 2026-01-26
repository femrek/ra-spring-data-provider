package dev.femrek.reactadmindataprovider.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Generic interface for CRUD operations on entities with support for filtering, pagination, and bulk operations.
 * <p>
 * Use an implementation of this interface in your controller implementations to handle standard data operations.
 *
 * @param <T>  The entity type.
 * @param <ID> The type of the entity's identifier.
 */
@SuppressWarnings("UnusedReturnValue")
public interface IRAService<T, C, ID> {
    /**
     * Finds entities matching the given filters and global search query.
     *
     * @param filters  A map of field names to values (e.g., "status" -> "active").
     * @param pageable Pagination and sorting information.
     * @return A page of entities.
     */
    Page<T> findWithFilters(Map<String, String> filters, Pageable pageable);

    /**
     * Retrieves all entities by their IDs.
     *
     * @param ids The collection of entity IDs to retrieve.
     * @return A list of entities matching the given IDs.
     */
    List<T> findAllById(Iterable<ID> ids);

    /**
     * Retrieves a single entity by its ID.
     *
     * @param id The ID of the entity to retrieve.
     * @return The entity with the given ID, or null if not found.
     */
    T findById(ID id);

    /**
     * Creates a new entity.
     *
     * @param data The entity data to save.
     * @return The saved entity.
     */
    Object create(C data);

    /**
     * Updates specific fields of an existing entity.
     *
     * @param id     The ID of the entity to update.
     * @param fields A map of field names to their new values.
     * @return The updated entity.
     */
    Object update(ID id, Map<String, Object> fields);

    /**
     * Updates multiple entities with the same field values.
     *
     * @param ids    The collection of entity IDs to update.
     * @param fields A map of field names to their new values to apply to all entities.
     * @return A list of IDs of the updated entities.
     */
    List<ID> updateMany(Iterable<ID> ids, Map<String, Object> fields);

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete.
     */
    Object deleteById(ID id);

    /**
     * Deletes multiple entities by their IDs.
     *
     * @param ids The collection of entity IDs to delete.
     * @return A list of IDs of the deleted entities.
     */
    List<ID> deleteMany(Iterable<ID> ids);
}
