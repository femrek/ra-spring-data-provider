package dev.femrek.reactadmindataprovider.service;

import java.util.List;
import java.util.Map;

import dev.femrek.reactadmindataprovider.jsonserver.service.IRAServiceJS;

/**
 * Extended service interface for React Admin data provider with additional bulk operations.
 * <p>
 * This interface extends {@link IRAServiceJS} to provide batch update and delete operations
 * required by the react-admin data provider's extended JSON server format.
 *
 * @param <T>  The entity type.
 * @param <ID> The type of the entity's identifier.
 */
public interface IRAService<T, ID> extends IRAServiceJS<T, ID> {
    /**
     * Updates multiple entities with the same field values.
     *
     * @param ids    The collection of entity IDs to update.
     * @param fields A map of field names to their new values to apply to all entities.
     * @return A list of IDs of the updated entities.
     */
    List<ID> updateMany(Iterable<ID> ids, Map<String, Object> fields);

    /**
     * Deletes multiple entities by their IDs.
     *
     * @param ids The collection of entity IDs to delete.
     * @return A list of IDs of the deleted entities.
     */
    List<ID> deleteMany(Iterable<ID> ids);
}
