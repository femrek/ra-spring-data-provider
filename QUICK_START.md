# Quick Start Guide

This guide will help you set up a React Admin backend using the ra-spring-json-server library. This library uses efficient bulk operations with single requests containing multiple ID parameters.

## Prerequisites

- Java 17 or higher
- Spring Boot (4 or higher recommended)
- Maven or Gradle
- Node.js and npm/yarn (for frontend)

## Setup Instructions

### 1. Install ra-spring-data-provider

Install the compatible React Admin data provider:

```bash
npm install ra-spring-data-provider
```

or

```bash
yarn add ra-spring-data-provider
```

This package is specifically designed to work with the Spring Boot backend created using this library.

### 2. Configure React Admin Frontend

```javascript
import { Admin, Resource, ListGuesser } from "react-admin";
import raSpringDataProvider from "ra-spring-data-provider";

const dataProvider = raSpringDataProvider("http://localhost:8080/api");

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource name="users" list={ListGuesser} />
  </Admin>
);

export default App;
```

### 3. Create Your Entity

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String role;
}
```

### 4. Create Your Repository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

### 5. Create DTOs

```java
// UserCreateDTO.java - for creating users
public class UserCreateDTO {
    private String name;
    private String email;
    private String role;
    // getters and setters
}

// UserResponseDTO.java - for returning user data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    // getters and setters
}
```

### 6. Implement the Service

```java
@Service
public class UserService implements IRAService<UserResponseDTO, UserCreateDTO, Long> {
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
                // Extract and apply global search query (q parameter)
                String q = filters.remove("q");
                if (q != null && !q.isEmpty()) {
                    String pattern = "%" + q.toLowerCase() + "%";
                    Predicate namePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), pattern);
                    Predicate emailPredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")), pattern);
                    predicates.add(criteriaBuilder.or(namePredicate, emailPredicate));
                }

                // Apply remaining field filters
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
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        return dto;
    }

    @Override
    public UserResponseDTO create(UserCreateDTO data) {
        User user = new User();
        user.setName(data.getName());
        user.setEmail(data.getEmail());
        user.setRole(data.getRole());

        User savedUser = userRepository.save(user);
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(savedUser.getId());
        dto.setName(savedUser.getName());
        dto.setEmail(savedUser.getEmail());
        dto.setRole(savedUser.getRole());
        return dto;
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
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(updatedUser.getId());
        dto.setName(updatedUser.getName());
        dto.setEmail(updatedUser.getEmail());
        dto.setRole(updatedUser.getRole());
        return dto;
    }

    @Override
    public Void deleteById(Long id) {
        userRepository.deleteById(id);
        return null;
    }

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

    @Override
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> deletedIds = StreamSupport.stream(ids.spliterator(), false)
                .toList();

        // Delete all users by their IDs
        userRepository.deleteAllById(ids);

        return deletedIds;
    }
}
```

### 7. Create Your Controller

```java
@RestController
@RequestMapping("/api/users") // resource name: users
@CrossOrigin(origins = "*")
public class UserController extends RAController<UserResponseDTO, UserCreateDTO, Long> {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected IRAService<UserResponseDTO, UserCreateDTO, Long> getService() {
        return userService;
    }
}
```

### 8. Configure CORS (if needed)

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Range", "X-Total-Count"); // for pagination
            }
        };
    }
}
```

## Next Steps

- Explore the [full documentation](README.md) for advanced usage
- Learn about [advanced filtering](README.md#advanced-filtering)
- Check out the [API endpoints](README.md#api-endpoints)
- Review the [integration tests](https://github.com/femrek/ra-spring-json-server/tree/main/ra-spring-json-server/src/test/java/dev/femrek/reactadmindataprovider/integration) for more examples
