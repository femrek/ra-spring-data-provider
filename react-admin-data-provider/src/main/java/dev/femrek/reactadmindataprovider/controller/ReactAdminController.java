package dev.femrek.reactadmindataprovider.controller;

import dev.femrek.reactadmindataprovider.service.IReactAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public abstract class ReactAdminController<T, ID extends Serializable> implements IReactAdminController<T, ID> {
    protected abstract IReactAdminService<T, ID> getService();

    @Override
    public ResponseEntity<List<T>> getList(
            int _start,
            int _end,
            String _sort,
            String _order,
            List<ID> id,
            String q,
            Map<String, String> allParams
    ) {
        // 1. Handle "getMany" (Fetch by specific IDs)
        if (id != null && !id.isEmpty()) {
            return ResponseEntity.ok(getService().findAllById(id));
        }

        // 2. Calculate Pagination
        int pageSize = _end - _start;
        if (pageSize <= 0) pageSize = 10;
        int pageNumber = _start / pageSize;

        Sort sort = Sort.by(Sort.Direction.fromString(_order), _sort);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // 3. Clean up filters
        // We remove the protocol params so only actual filters (like "status", "authorId") remain
        List.of("_start", "_end", "_sort", "_order", "id", "q").forEach(allParams.keySet()::remove);

        // 4. Fetch Data
        Page<T> pageResult = getService().findWithFilters(allParams, q, pageable);

        // 5. Set Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(pageResult.getTotalElements()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Total-Count");

        return new ResponseEntity<>(pageResult.getContent(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<T> getOne(ID id) {
        return ResponseEntity.ok(getService().findById(id));
    }

    @Override
    public ResponseEntity<T> create(T entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().save(entity));
    }

    @Override
    public ResponseEntity<T> update(ID id, Map<String, Object> fields) {
        return ResponseEntity.ok(getService().update(id, fields));
    }

    @Override
    public ResponseEntity<List<ID>> updateMany(List<ID> ids, Map<String, Object> fields) {
        getService().updateAll(ids, fields);
        return ResponseEntity.ok(ids);
    }

    @Override
    public ResponseEntity<Void> delete(ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteMany(List<ID> ids) {
        getService().deleteAllById(ids);
        return ResponseEntity.noContent().build();
    }
}