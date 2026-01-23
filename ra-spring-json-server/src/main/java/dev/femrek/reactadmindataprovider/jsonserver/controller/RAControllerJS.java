package dev.femrek.reactadmindataprovider.jsonserver.controller;

import dev.femrek.reactadmindataprovider.jsonserver.service.IRAServiceJS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public abstract class RAControllerJS<T, ID> implements IRAControllerJS<T, ID> {
    private static final Log log = LogFactory.getLog(RAControllerJS.class);

    private static final List<String> RESERVED_PARAMS = List.of(
            "_start", "_end", "_sort", "_order", "q", "id", "_embed"
    );

    protected abstract IRAServiceJS<T, ID> getService();

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
        // check if _embed is present to avoid issues with JPA specifications
        if (allParams.containsKey("_embed")) {
            log.warn("The '_embed' parameter is not supported and will be ignored.");
        }

        // We remove the protocol params so only actual filters (like "status", "authorId") remain
        RESERVED_PARAMS.forEach(allParams.keySet()::remove);

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
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().create(entity));
    }

    @Override
    public ResponseEntity<T> update(ID id, Map<String, Object> fields) {
        return ResponseEntity.ok(getService().update(id, fields));
    }

    @Override
    public ResponseEntity<Void> delete(ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
