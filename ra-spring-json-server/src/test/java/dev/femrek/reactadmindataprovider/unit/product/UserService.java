package dev.femrek.reactadmindataprovider.unit.product;

import dev.femrek.reactadmindataprovider.service.IRAService;
import dev.femrek.reactadmindataprovider.unit.User;
import dev.femrek.reactadmindataprovider.unit.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Extended service implementation for User entity with bulk operations support.
 * This service implements all standard CRUD operations plus updateMany and deleteMany operations
 * for batch processing of entities.
 */
@Service
public class UserService implements IRAService<User, Long> {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<User> findWithFilters(Map<String, String> filters, String q, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply field-specific filters
            if (filters != null) {
                filters.forEach((field, value) -> {
                    if (value != null && !value.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get(field), value));
                    }
                });
            }

            // Apply global search query (q parameter)
            if (q != null && !q.isEmpty()) {
                String searchPattern = "%" + q.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate rolePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), searchPattern);

                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, rolePredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    @Override
    public List<User> findAllById(Iterable<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User create(User entity) {
        return userRepository.save(entity);
    }

    @Override
    public User update(Long id, Map<String, Object> fields) {
        User user = findById(id);

        fields.forEach((key, value) -> {
            switch (key) {
                case "name":
                    user.setName((String) value);
                    break;
                case "email":
                    user.setEmail((String) value);
                    break;
                case "role":
                    user.setRole((String) value);
                    break;
            }
        });

        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Updates multiple users with the same field values.
     *
     * @param ids    The collection of user IDs to update.
     * @param fields A map of field names to their new values to apply to all users.
     * @return A list of IDs of the updated users.
     */
    @Override
    public List<Long> updateMany(Iterable<Long> ids, Map<String, Object> fields) {
        List<Long> updatedIds = new ArrayList<>();

        // Find all users by their IDs
        List<User> users = userRepository.findAllById(ids);

        // Update each user with the provided fields
        for (User user : users) {
            fields.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        user.setName((String) value);
                        break;
                    case "email":
                        user.setEmail((String) value);
                        break;
                    case "role":
                        user.setRole((String) value);
                        break;
                }
            });
            User savedUser = userRepository.save(user);
            updatedIds.add(savedUser.getId());
        }

        return updatedIds;
    }

    /**
     * Deletes multiple users by their IDs.
     *
     * @param ids The collection of user IDs to delete.
     * @return A list of IDs of the deleted users.
     */
    @Override
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> deletedIds = StreamSupport.stream(ids.spliterator(), false)
                .toList();

        // Delete all users by their IDs
        userRepository.deleteAllById(ids);

        return deletedIds;
    }
}

