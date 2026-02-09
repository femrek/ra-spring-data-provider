package dev.femrek.openapidemo.service;

import dev.femrek.openapidemo.dto.UserCreateDTO;
import dev.femrek.openapidemo.dto.UserResponseDTO;
import dev.femrek.openapidemo.entity.User;
import dev.femrek.openapidemo.repository.UserRepository;
import dev.femrek.reactadmindataprovider.service.IRAService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for User operations.
 */
@Service
@RequiredArgsConstructor
public class UserService implements IRAService<UserResponseDTO, UserCreateDTO, Long> {

    private final UserRepository userRepository;

    @Override
    public Page<UserResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
        Specification<User> spec = buildSpecification(filters);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::convertToResponseDTO);
    }

    @Override
    public Page<UserResponseDTO> findWithTargetAndFilters(
            String target,
            String targetId,
            Map<String, String> filters,
            Pageable pageable
    ) {
        Specification<User> spec = buildSpecification(filters);

        // Add target reference filter
        if (target != null && targetId != null) {
            spec = spec.and((root, query, criteriaBuilder) -> {
                if ("role".equals(target)) {
                    return criteriaBuilder.equal(root.get("role"), targetId);
                }
                return criteriaBuilder.conjunction();
            });
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::convertToResponseDTO);
    }

    @Override
    public List<UserResponseDTO> findAllById(Iterable<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        return users.stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return convertToResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO create(UserCreateDTO data) {
        User user = new User();
        user.setName(data.getName());
        user.setEmail(data.getEmail());
        user.setRole(data.getRole());

        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO update(Long id, Map<String, Object> fields) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        fields.forEach((key, value) -> {
            switch (key) {
                case "name" -> user.setName((String) value);
                case "email" -> user.setEmail((String) value);
                case "role" -> user.setRole((String) value);
            }
        });

        User updatedUser = userRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public List<Long> updateMany(Iterable<Long> ids, Map<String, Object> fields) {
        List<Long> updatedIds = new ArrayList<>();

        for (Long id : ids) {
            try {
                update(id, fields);
                updatedIds.add(id);
            } catch (EntityNotFoundException e) {
                // Skip if not found
            }
        }

        return updatedIds;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();

        for (Long id : ids) {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                deletedIds.add(id);
            }
        }

        return deletedIds;
    }

    /**
     * Converts a User entity to UserResponseDTO.
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    /**
     * Builds a JPA Specification based on the provided filters.
     */
    @SuppressWarnings("unused")
    private Specification<User> buildSpecification(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            if (filters == null || filters.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            filters.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    switch (key) {
                        case "name" -> predicates.add(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("name")),
                                        "%" + value.toLowerCase() + "%"
                                )
                        );
                        case "email" -> predicates.add(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(root.get("email")),
                                        "%" + value.toLowerCase() + "%"
                                )
                        );
                        case "role" -> predicates.add(
                                criteriaBuilder.equal(root.get("role"), value)
                        );
                        case "id" -> predicates.add(
                                criteriaBuilder.equal(root.get("id"), Long.parseLong(value))
                        );
                    }
                }
            });

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}


