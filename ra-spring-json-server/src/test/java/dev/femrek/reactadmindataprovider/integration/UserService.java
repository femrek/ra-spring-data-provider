package dev.femrek.reactadmindataprovider.integration;

import dev.femrek.reactadmindataprovider.service.IRAService;
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
class UserService implements IRAService<UserResponseDTO, UserCreateDTO, Long> {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply field-specific filters
            if (filters != null) {
                // Apply global search query (q parameter)
                String q = filters.remove("q");
                if (q != null && !q.isEmpty()) {
                    String searchPattern = "%" + q.toLowerCase() + "%";
                    Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
                    Predicate emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern);
                    Predicate rolePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), searchPattern);

                    predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, rolePredicate));
                }

                // Apply field filters
                filters.forEach((field, value) -> {
                    if (value != null && !value.isEmpty()) {
                        predicates.add(criteriaBuilder.equal(root.get(field), value));
                    }
                });
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> entities = userRepository.findAll(spec, pageable);
        return entities.map(entity -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setEmail(entity.getEmail());
            dto.setRole(entity.getRole());
            return dto;
        });
    }

    @Override
    public List<UserResponseDTO> findAllById(Iterable<Long> ids) {
        List<User> entities = userRepository.findAllById(ids);
        List<UserResponseDTO> results = new ArrayList<>();
        for (User entity : entities) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(entity.getId());
            dto.setName(entity.getName());
            dto.setEmail(entity.getEmail());
            dto.setRole(entity.getRole());
            results.add(dto);
        }
        return results;
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        UserResponseDTO result = new UserResponseDTO();
        result.setId(entity.getId());
        result.setName(entity.getName());
        result.setEmail(entity.getEmail());
        result.setRole(entity.getRole());
        return result;
    }

    @Override
    public UserResponseDTO create(UserCreateDTO data) {
        User user = new User();
        user.setName(data.getName());
        user.setEmail(data.getEmail());
        user.setRole(data.getRole());

        User savedUser = userRepository.save(user);
        UserResponseDTO result = new UserResponseDTO();
        result.setId(savedUser.getId());
        result.setName(savedUser.getName());
        result.setEmail(savedUser.getEmail());
        result.setRole(savedUser.getRole());
        return result;
    }

    @Override
    public UserResponseDTO update(Long id, Map<String, Object> fields) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

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

        User updatedUser = userRepository.save(user);
        UserResponseDTO result = new UserResponseDTO();
        result.setId(updatedUser.getId());
        result.setName(updatedUser.getName());
        result.setEmail(updatedUser.getEmail());
        result.setRole(updatedUser.getRole());
        return result;
    }

    @Override
    public Void deleteById(Long id) {
        userRepository.deleteById(id);
        return null;
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
