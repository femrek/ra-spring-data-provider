package dev.femrek.reactadmindataprovider.controller;

import dev.femrek.reactadmindataprovider.service.IRAService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract base controller providing standard CRUD operations for resources. This automatically calls related service
 * methods.
 * <p>
 * Extend this class and implement the {@link #getService()} method to provide the specific service for your resource.
 *
 * @param <T>  the Response DTO type for this resource
 * @param <C>  the Create DTO type for this resource
 * @param <ID> the type of the entity's identifier
 */
public abstract class RAController<T, C, ID> implements IRAController<T, C, ID> {
    private static final Log log = LogFactory.getLog(RAController.class);

    protected abstract IRAService<T, C, ID> getService();

    private static final List<String> RESERVED_PARAMS = List.of(
            "_start", "_end", "_sort", "_order", "_embed", "id"
    );

    @Override
    public ResponseEntity<List<T>> getList(
            int _start,
            int _end,
            String _sort,
            String _order,
            String _embed,
            List<ID> id,
            Map<String, String> allParams
    ) {
        // 1. Handle "getMany" (Fetch by specific IDs)
        if (id != null && !id.isEmpty()) {
            return ResponseEntity.ok(getService().findAllById(id));
        }

        // 2. Validate Pagination Parameters if "getList"
        if (_start < 0 || _end < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "_start and _end parameters are null or smaller than 0. These parameters are required for `getList` operation.");
        } else if (_end <= _start) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "_end parameter must be greater than _start parameter.");
        }

        // 3. Calculate Pagination
        int pageSize = _end - _start;
        int pageNumber = _start / pageSize;
        Sort sort = Sort.by(Sort.Direction.fromString(_order), _sort);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // 4. Handle _embed Parameter
        if (_embed != null) {
            log.warn("_embed parameter is not supported and will be ignored.");
        }

        // 5. Fetch Data
        RESERVED_PARAMS.forEach(allParams.keySet()::remove);
        Page<T> pageResult = getService().findWithFilters(allParams, pageable);

        // 6. Set Headers
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
    public ResponseEntity<?> create(C data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().create(data));
    }

    @Override
    public ResponseEntity<?> update(ID id, Map<String, Object> fields) {
        return ResponseEntity.ok(getService().update(id, fields));
    }

    @Override
    public ResponseEntity<List<ID>> updateMany(List<ID> id, Map<String, Object> fields) {
        List<ID> ids = id != null ? id : Collections.emptyList();
        List<ID> updatedIds = getService().updateMany(ids, fields);
        return ResponseEntity.ok(updatedIds);
    }

    @Override
    public ResponseEntity<?> delete(ID id) {
        getService().deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<ID>> deleteMany(List<ID> id) {
        List<ID> ids = id != null ? id : Collections.emptyList();
        List<ID> deletedIds = getService().deleteMany(ids);
        return ResponseEntity.ok(deletedIds);
    }
}
