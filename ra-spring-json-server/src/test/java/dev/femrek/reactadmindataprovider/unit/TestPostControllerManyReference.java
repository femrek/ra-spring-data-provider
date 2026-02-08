package dev.femrek.reactadmindataprovider.unit;

import okhttp3.*;
import okhttp3.MediaType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for getManyReference operation.
 * Tests the ability to retrieve posts that belong to specific users.
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestPostControllerManyReference {
    @LocalServerPort
    private int port;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Test data IDs
    private static Long userId1;
    private static Long userId2;
    private static Long userId3;
    private static Long postId1User1;
    private static Long postId2User1;
    private static Long postId3User1;
    private static Long postId4User1;
    private static Long postId5User1;
    private static Long postId1User2;

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    private HttpUrl baseHttpUrl() {
        HttpUrl result = HttpUrl.parse(baseUrl());
        assertNotNull(result);
        return result;
    }

    @BeforeAll
    static void setupTestData(@Autowired UserRepository userRepository, @Autowired PostRepository postRepository) {
        // Clean up existing data
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        User user1 = new User("Alice Johnson", "alice.johnson@example.com", "author");
        User user2 = new User("Bob Smith", "bob.smith@example.com", "author");
        User user3 = new User("Charlie Brown", "charlie.brown@example.com", "author");

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);

        userId1 = user1.getId();
        userId2 = user2.getId();
        userId3 = user3.getId();

        // Create posts for user1 (5 posts)
        Post post1User1 = new Post("Introduction to Java", "Java is a programming language...", userId1, "published");
        Post post2User1 = new Post("Advanced Java Techniques", "In this post, we explore...", userId1, "published");
        Post post3User1 = new Post("Java Best Practices", "Following best practices...", userId1, "draft");
        Post post4User1 = new Post("Java Performance Tips", "Optimize your Java code...", userId1, "published");
        Post post5User1 = new Post("Java Security Guide", "Security is crucial...", userId1, "published");

        post1User1 = postRepository.save(post1User1);
        post2User1 = postRepository.save(post2User1);
        post3User1 = postRepository.save(post3User1);
        post4User1 = postRepository.save(post4User1);
        post5User1 = postRepository.save(post5User1);

        postId1User1 = post1User1.getId();
        postId2User1 = post2User1.getId();
        postId3User1 = post3User1.getId();
        postId4User1 = post4User1.getId();
        postId5User1 = post5User1.getId();

        // Create posts for user2 (2 posts)
        Post post1User2 = new Post("Python for Beginners", "Python is easy to learn...", userId2, "published");
        Post post2User2 = new Post("Python Data Science", "Data science with Python...", userId2, "published");

        post1User2 = postRepository.save(post1User2);
        post2User2 = postRepository.save(post2User2);

        postId1User2 = post1User2.getId();

        // Create post for user3 (1 post)
        Post post1User3 = new Post("JavaScript Fundamentals", "Learn JavaScript basics...", userId3, "draft");
        post1User3 = postRepository.save(post1User3);
    }

    // ==================== GET MANY REFERENCE Tests ====================

    @Test
    @Order(1)
    @DisplayName("GET /api/posts/of/userId/{userId} - Get all posts for user1 (5 posts)")
    void testGetManyReferenceUser1AllPosts() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.header("X-Total-Count"));
            assertEquals("5", response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(5, posts.size());

            // Verify all posts belong to user1
            for (Map<String, Object> post : posts) {
                assertEquals(userId1.intValue(), ((Number) post.get("userId")).intValue());
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/posts/of/userId/{userId} - Get posts for user2 (2 posts)")
    void testGetManyReferenceUser2() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId2.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("2", response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(2, posts.size());

            // Verify all posts belong to user2
            for (Map<String, Object> post : posts) {
                assertEquals(userId2.intValue(), ((Number) post.get("userId")).intValue());
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/posts/of/userId/{userId} - Get posts for user3 (1 post)")
    void testGetManyReferenceUser3() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId3.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("1", response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(1, posts.size());
            assertEquals(userId3.intValue(), ((Number) posts.get(0).get("userId")).intValue());
        }
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/posts/of/userId/{userId} - Pagination: Get first 2 posts for user1")
    void testGetManyReferenceWithPagination() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "2")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("5", response.header("X-Total-Count")); // Total is still 5
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(2, posts.size()); // But we only get 2
        }
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/posts/of/userId/{userId} - Pagination: Get next 2 posts for user1 (offset)")
    void testGetManyReferenceWithPaginationOffset() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "2")
                .addQueryParameter("_end", "4")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("5", response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(2, posts.size());
        }
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/posts/of/userId/{userId} - Sorting: Get posts sorted by title DESC")
    void testGetManyReferenceWithSorting() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "title")
                .addQueryParameter("_order", "DESC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(5, posts.size());

            // Verify descending order by title
            for (int i = 0; i < posts.size() - 1; i++) {
                String currentTitle = (String) posts.get(i).get("title");
                String nextTitle = (String) posts.get(i + 1).get("title");
                assertTrue(currentTitle.compareTo(nextTitle) >= 0);
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/posts/of/userId/{userId}?status=published - Filter by additional field (status)")
    void testGetManyReferenceWithAdditionalFilter() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("status", "published")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("4", response.header("X-Total-Count")); // User1 has 4 published posts
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(4, posts.size());

            // Verify all returned posts are published and belong to user1
            for (Map<String, Object> post : posts) {
                assertEquals("published", post.get("status"));
                assertEquals(userId1.intValue(), ((Number) post.get("userId")).intValue());
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/posts/of/userId/{userId}?status=draft - Filter draft posts for user1")
    void testGetManyReferenceDraftPosts() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("status", "draft")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("1", response.header("X-Total-Count")); // User1 has 1 draft post
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(1, posts.size());
            assertEquals("draft", posts.get(0).get("status"));
        }
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/posts/of/userId/99999 - Get posts for non-existent user returns empty list")
    void testGetManyReferenceNonExistentUser() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment("99999")
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("0", response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertTrue(posts.isEmpty());
        }
    }

    @Test
    @Order(10)
    @DisplayName("GET /api/posts/of/userId/{userId}?_start=0&_end=3&_sort=id&_order=ASC - Verify pagination headers are correct")
    void testGetManyReferencePaginationHeaders() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "1")
                .addQueryParameter("_end", "4")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());

            // Verify X-Total-Count header
            String totalCount = response.header("X-Total-Count");
            assertNotNull(totalCount);
            assertEquals("5", totalCount);

            // Verify Access-Control-Expose-Headers includes X-Total-Count
            String exposedHeaders = response.header("Access-Control-Expose-Headers");
            assertNotNull(exposedHeaders);
            assertTrue(exposedHeaders.contains("X-Total-Count"));

            assertNotNull(response.body());
            List<Map<String, Object>> posts = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(3, posts.size()); // _start=1 to _end=4 means 3 items
        }
    }

    @Test
    @Order(11)
    @DisplayName("Create new post and verify it appears in getManyReference")
    void testCreatePostAndVerifyInManyReference() throws IOException {
        // Get initial count
        HttpUrl countUrl = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId2.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        int initialCount;
        try (Response response = client.newCall(new Request.Builder().url(countUrl).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            initialCount = Integer.parseInt(countHeader);
        }

        // Create new post for user2
        Map<String, Object> newPost = new HashMap<>();
        newPost.put("title", "New Python Post");
        newPost.put("content", "Content about Python");
        newPost.put("userId", userId2);
        newPost.put("status", "published");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(newPost), JSON);
        Request createRequest = new Request.Builder()
                .url(baseUrl() + "/posts")
                .post(body)
                .build();

        try (Response response = client.newCall(createRequest).execute()) {
            assertEquals(201, response.code());
        }

        // Verify the new count
        try (Response response = client.newCall(new Request.Builder().url(countUrl).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            int newCount = Integer.parseInt(countHeader);
            assertEquals(initialCount + 1, newCount);
        }
    }

    /**
     * Provides all user1 post IDs for parameterized testing
     */
    static Stream<Long> provideUser1PostIds() {
        return Stream.of(postId1User1, postId2User1, postId3User1, postId4User1, postId5User1);
    }

    @ParameterizedTest
    @MethodSource("provideUser1PostIds")
    @Order(12)
    @DisplayName("Update post userId and verify it moves to different user's reference - Test with multiple posts")
    void testUpdatePostUserIdAndVerifyManyReference(Long postIdToUpdate) throws IOException {
        // Get initial counts
        HttpUrl user1Url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId1.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "100")
                .build();

        HttpUrl user3Url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId3.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "100")
                .build();

        int user1InitialCount;
        int user3InitialCount;

        try (Response response = client.newCall(new Request.Builder().url(user1Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            user1InitialCount = Integer.parseInt(countHeader);
        }

        try (Response response = client.newCall(new Request.Builder().url(user3Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            user3InitialCount = Integer.parseInt(countHeader);
        }

        // Update a post from user1 to user3
        Map<String, Object> update = new HashMap<>();
        update.put("userId", userId3);

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(update), JSON);
        Request updateRequest = new Request.Builder()
                .url(baseUrl() + "/posts/" + postIdToUpdate)
                .put(body)
                .build();

        try (Response response = client.newCall(updateRequest).execute()) {
            assertEquals(200, response.code(), "Failed to update post with ID: " + postIdToUpdate);
        }

        // Verify counts changed
        try (Response response = client.newCall(new Request.Builder().url(user1Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            int user1NewCount = Integer.parseInt(countHeader);
            assertEquals(user1InitialCount - 1, user1NewCount,
                    "User1 count should decrease by 1 after moving post " + postIdToUpdate);
        }

        try (Response response = client.newCall(new Request.Builder().url(user3Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            int user3NewCount = Integer.parseInt(countHeader);
            assertEquals(user3InitialCount + 1, user3NewCount,
                    "User3 count should increase by 1 after receiving post " + postIdToUpdate);
        }

        // Move the post back to user1 for subsequent test iterations
        Map<String, Object> revertUpdate = new HashMap<>();
        revertUpdate.put("userId", userId1);
        RequestBody revertBody = RequestBody.create(objectMapper.writeValueAsString(revertUpdate), JSON);
        Request revertRequest = new Request.Builder()
                .url(baseUrl() + "/posts/" + postIdToUpdate)
                .put(revertBody)
                .build();

        try (Response response = client.newCall(revertRequest).execute()) {
            assertEquals(200, response.code(), "Failed to revert post " + postIdToUpdate + " back to user1");
        }
    }

    @Test
    @Order(13)
    @DisplayName("Delete post and verify it's removed from getManyReference")
    void testDeletePostAndVerifyManyReference() throws IOException {
        // Get initial count for user2
        HttpUrl user2Url = baseHttpUrl().newBuilder()
                .addPathSegment("posts")
                .addPathSegment("of")
                .addPathSegment("userId")
                .addPathSegment(userId2.toString())
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "100")
                .build();

        int initialCount;
        try (Response response = client.newCall(new Request.Builder().url(user2Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            initialCount = Integer.parseInt(countHeader);
        }

        // Delete a post
        Request deleteRequest = new Request.Builder()
                .url(baseUrl() + "/posts/" + postId1User2)
                .delete()
                .build();

        try (Response response = client.newCall(deleteRequest).execute()) {
            assertEquals(204, response.code());
        }

        // Verify count decreased
        try (Response response = client.newCall(new Request.Builder().url(user2Url).get().build()).execute()) {
            String countHeader = response.header("X-Total-Count");
            assertNotNull(countHeader);
            int newCount  = Integer.parseInt(countHeader);
            assertEquals(initialCount - 1, newCount);
        }
    }
}

