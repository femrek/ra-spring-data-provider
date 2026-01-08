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

    List<T> findAllById(Iterable<ID> ids);

    T findById(ID id);

    T save(T entity);

    T update(ID id, Map<String, Object> fields);

    List<T> updateAll(Iterable<ID> ids, Map<String, Object> fields);

    void deleteById(ID id);

    void deleteAllById(Iterable<ID> ids);
}