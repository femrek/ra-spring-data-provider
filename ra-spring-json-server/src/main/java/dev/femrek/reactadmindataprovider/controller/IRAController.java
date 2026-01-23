package dev.femrek.reactadmindataprovider.controller;

import dev.femrek.reactadmindataprovider.jsonserver.controller.IRAControllerJS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface IRAController<T, ID> extends IRAControllerJS<T, ID> {
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
