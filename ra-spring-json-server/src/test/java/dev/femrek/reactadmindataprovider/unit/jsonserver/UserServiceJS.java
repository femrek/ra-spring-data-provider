package dev.femrek.reactadmindataprovider.unit.jsonserver;

import dev.femrek.reactadmindataprovider.jsonserver.service.IRAServiceJS;
import dev.femrek.reactadmindataprovider.unit.User;
import dev.femrek.reactadmindataprovider.unit.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceJS implements IRAServiceJS<User, Long> {

    @Autowired
    private UserRepository userRepository;

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
}

