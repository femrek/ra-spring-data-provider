package dev.femrek.reactadmindataprovider.controller;

import dev.femrek.reactadmindataprovider.jsonserver.controller.RAControllerJS;
import dev.femrek.reactadmindataprovider.service.IRAService;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class RAController<T, ID>
        extends RAControllerJS<T, ID>
        implements IRAController<T, ID> {
    @Override
    protected abstract IRAService<T, ID> getService();

    @Override
    public ResponseEntity<List<ID>> updateMany(List<ID> id, Map<String, Object> fields) {
        List<ID> ids = id != null ? id : Collections.emptyList();
        List<ID> updatedIds = getService().updateMany(ids, fields);
        return ResponseEntity.ok(updatedIds);
    }

    @Override
    public ResponseEntity<List<ID>> deleteMany(List<ID> id) {
        List<ID> ids = id != null ? id : Collections.emptyList();
        List<ID> deletedIds = getService().deleteMany(ids);
        return ResponseEntity.ok(deletedIds);
    }
}
