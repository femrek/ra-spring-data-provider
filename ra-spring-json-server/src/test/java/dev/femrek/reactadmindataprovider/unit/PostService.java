package dev.femrek.reactadmindataprovider.unit;

import dev.femrek.reactadmindataprovider.service.IRAService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Post entity.
 * Supports all CRUD operations including filtering by userId for getManyReference.
 */
@Service
class PostService implements IRAService<PostResponseDTO, PostCreateDTO, Long> {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Page<PostResponseDTO> findWithFilters(Map<String, String> filters, Pageable pageable) {
        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                // Apply global search query (q parameter)
                String q = filters.remove("q");
                if (q != null && !q.isEmpty()) {
                    String searchPattern = "%" + q.toLowerCase() + "%";
                    Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                    Predicate contentPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), searchPattern);
                    predicates.add(criteriaBuilder.or(titlePredicate, contentPredicate));
                }

                // Apply field filters (including userId for getManyReference)
                filters.forEach((field, value) -> {
                    if (value != null && !value.isEmpty()) {
                        if (field.equals("userId")) {
                            predicates.add(criteriaBuilder.equal(root.get(field), Long.parseLong(value)));
                        } else {
                            predicates.add(criteriaBuilder.equal(root.get(field), value));
                        }
                    }
                });
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Post> page = postRepository.findAll(spec, pageable);
        return page.map(this::toResponseDTO);
    }

    @Override
    public Page<PostResponseDTO> findWithTargetAndFilters(String target, String targetId, Map<String, String> filters, Pageable pageable) {
        // Add the target filter to the filters map
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(target, targetId);
        return findWithFilters(filters, pageable);
    }

    @Override
    public List<PostResponseDTO> findAllById(Iterable<Long> ids) {
        return postRepository.findAllById(ids).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public PostResponseDTO findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return toResponseDTO(post);
    }

    @Override
    public PostResponseDTO create(PostCreateDTO createDTO) {
        Post post = new Post();
        post.setTitle(createDTO.getTitle());
        post.setContent(createDTO.getContent());
        post.setUserId(createDTO.getUserId());
        post.setStatus(createDTO.getStatus());

        Post saved = postRepository.save(post);
        return toResponseDTO(saved);
    }

    @Override
    public PostResponseDTO update(Long id, Map<String, Object> fields) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        fields.forEach((field, value) -> {
            switch (field) {
                case "title" -> post.setTitle((String) value);
                case "content" -> post.setContent((String) value);
                case "userId" -> post.setUserId(value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString()));
                case "status" -> post.setStatus((String) value);
            }
        });

        Post updated = postRepository.save(post);
        return toResponseDTO(updated);
    }

    @Override
    public List<Long> updateMany(Iterable<Long> ids, Map<String, Object> fields) {
        List<Long> idList = new ArrayList<>();
        ids.forEach(idList::add);

        if (idList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Post> posts = postRepository.findAllById(idList);
        posts.forEach(post ->
            fields.forEach((field, value) -> {
                switch (field) {
                    case "title" -> post.setTitle((String) value);
                    case "content" -> post.setContent((String) value);
                    case "userId" -> post.setUserId(value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString()));
                    case "status" -> post.setStatus((String) value);
                }
            })
        );

        postRepository.saveAll(posts);
        return idList;
    }

    @Override
    public void deleteById(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    @Override
    public List<Long> deleteMany(Iterable<Long> ids) {
        List<Long> idList = new ArrayList<>();
        ids.forEach(idList::add);

        if (idList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Post> posts = postRepository.findAllById(idList);
        postRepository.deleteAll(posts);
        return posts.stream().map(Post::getId).toList();
    }

    private PostResponseDTO toResponseDTO(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                post.getStatus()
        );
    }
}



