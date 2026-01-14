package dev.femrek.reactadmindataprovider.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IReactAdminService<T, ID extends Serializable> {
    /**
     * Finds entities matching the given filters and global search query.
     *
     * @param filters  A map of field names to values (e.g., "status" -> "active").
     * @param q        The global search string (optional).
     * @param pageable Pagination and sorting information.
     * @return A page of entities.
     */
    Page<T> findWithFilters(Map<String, String> filters, String q, Pageable pageable);

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
     * Saves a new entity or updates an existing one.
     *
     * @param entity The entity to save.
     * @return The saved entity.
     */
    T save(T entity);

    /**
     * Updates specific fields of an existing entity.
     *
     * @param id     The ID of the entity to update.
     * @param fields A map of field names to their new values.
     * @return The updated entity.
     */
    T update(ID id, Map<String, Object> fields);

    /**
     * Updates specific fields of multiple entities.
     *
     * @param ids    The collection of entity IDs to update.
     * @param fields A map of field names to their new values.
     * @return A list of IDs of the updated entities.
     */
    List<ID> updateAll(Iterable<ID> ids, Map<String, Object> fields);

    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete.
     */
    void deleteById(ID id);

    /**
     * Deletes multiple entities by their IDs.
     *
     * @param ids The collection of entity IDs to delete.
     */
    void deleteAllById(Iterable<ID> ids);
}