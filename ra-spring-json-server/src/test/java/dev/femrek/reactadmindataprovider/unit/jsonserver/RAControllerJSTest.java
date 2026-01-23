package dev.femrek.reactadmindataprovider.unit.jsonserver;

import dev.femrek.reactadmindataprovider.unit.TestApplication;
import dev.femrek.reactadmindataprovider.unit.UserRepository;
import okhttp3.*;
import okhttp3.MediaType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ReactAdminController using real HTTP requests via OkHttp.
 * No mocking is used - all tests make actual HTTP calls to the running Spring Boot server.
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RAControllerJSTest {
    @LocalServerPort
    private int port;
    private final UserRepository userRepository;

    @Autowired
    public RAControllerJSTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static Long createdUserId1;
    private static Long createdUserId2;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/users";
    }

    private HttpUrl baseHttpUrl() {
        HttpUrl result = HttpUrl.parse(baseUrl());
        assertNotNull(result);
        return result;
    }

    @BeforeEach
    void setUp() {
        // Clean database before the first test
        if (createdUserId1 == null) {
            userRepository.deleteAll();
        }
    }

    // ==================== CREATE (POST) Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /api/users - Create a new user")
    void testCreate() throws IOException {
        Map<String, String> newUser = new HashMap<>();
        newUser.put("name", "John Doe");
        newUser.put("email", "john.doe@example.com");
        newUser.put("role", "admin");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(newUser), JSON);
        Request request = new Request.Builder()
                .url(baseUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(201, response.code());
            assertNotNull(response.body());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
            assertNotNull(responseBody.get("id"));
            assertEquals("John Doe", responseBody.get("name"));
            assertEquals("john.doe@example.com", responseBody.get("email"));
            assertEquals("admin", responseBody.get("role"));

            createdUserId1 = ((Number) responseBody.get("id")).longValue();
        }
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/users - Create additional users for list tests")
    void testCreateAdditionalUsers() throws IOException {
        Map<String, String> user2 = new HashMap<>();
        user2.put("name", "Jane Smith");
        user2.put("email", "jane.smith@example.com");
        user2.put("role", "user");

        RequestBody body2 = RequestBody.create(objectMapper.writeValueAsString(user2), JSON);
        Request request2 = new Request.Builder().url(baseUrl()).post(body2).build();

        try (Response response2 = client.newCall(request2).execute()) {
            assertEquals(201, response2.code());
            assertNotNull(response2.body());
            String body = response2.body().string();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody2 = objectMapper.readValue(body, Map.class);
            createdUserId2 = ((Number) responseBody2.get("id")).longValue();
        }

        Map<String, String> user3 = new HashMap<>();
        user3.put("name", "Bob Johnson");
        user3.put("email", "bob.johnson@example.com");
        user3.put("role", "moderator");

        RequestBody body3 = RequestBody.create(objectMapper.writeValueAsString(user3), JSON);
        Request request3 = new Request.Builder().url(baseUrl()).post(body3).build();

        try (Response response3 = client.newCall(request3).execute()) {
            assertEquals(201, response3.code());
        }
    }

    // ==================== GET ONE (GET /{id}) Tests ====================

    @Test
    @Order(3)
    @DisplayName("GET /api/users/{id} - Get a single user by ID")
    void testGetOne() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId1)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals(createdUserId1.intValue(), ((Number) responseBody.get("id")).intValue());
            assertEquals("John Doe", responseBody.get("name"));
            assertEquals("john.doe@example.com", responseBody.get("email"));
            assertEquals("admin", responseBody.get("role"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/users/{id} - Get non-existent user returns error")
    void testGetOneNotFound() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl() + "/99999")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(500, response.code());
        }
    }

    // ==================== GET LIST (GET /) Tests ====================

    @Test
    @Order(5)
    @DisplayName("GET /api/users - Get list with pagination")
    void testGetListWithPagination() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.header("X-Total-Count"));
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertTrue(responseBody.size() >= 3);
        }
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/users - Get list with sorting by name DESC")
    void testGetListWithSorting() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "name")
                .addQueryParameter("_order", "DESC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertFalse(responseBody.isEmpty());
        }
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/users?role=admin - Get list with field filter")
    void testGetListWithFilter() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("role", "admin")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertFalse(responseBody.isEmpty());
            assertEquals("admin", responseBody.get(0).get("role"));
        }
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/users?q=John - Get list with global search")
    void testGetListWithGlobalSearch() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("q", "John")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertFalse(responseBody.isEmpty());
            assertTrue(responseBody.get(0).get("name").toString().contains("John"));
        }
    }

    // ==================== GET MANY (GET /?id=1&id=2) Tests ====================

    @Test
    @Order(9)
    @DisplayName("GET /api/users?id=1&id=2 - Get multiple users by IDs")
    void testGetMany() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", createdUserId1.toString())
                .addQueryParameter("id", createdUserId2.toString())
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(2, responseBody.size());
        }
    }

    // ==================== UPDATE (PUT /{id}) Tests ====================

    @Test
    @Order(10)
    @DisplayName("PUT /api/users/{id} - Update a user")
    void testUpdate() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("role", "superadmin");
        updates.put("name", "John Doe Updated");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId1)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals(createdUserId1.intValue(), ((Number) responseBody.get("id")).intValue());
            assertEquals("John Doe Updated", responseBody.get("name"));
            assertEquals("superadmin", responseBody.get("role"));
        }
    }

    @Test
    @Order(11)
    @DisplayName("PUT /api/users/{id} - Partial update (only one field)")
    void testPartialUpdate() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("email", "john.updated@example.com");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId1)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals("john.updated@example.com", responseBody.get("email"));
            assertEquals("John Doe Updated", responseBody.get("name")); // Should retain previous value
        }
    }

    // ==================== DELETE (DELETE /{id}) Tests ====================

    @Test
    @Order(13)
    @DisplayName("DELETE /api/users/{id} - Delete a single user")
    void testDelete() throws IOException {
        // Create a user specifically for deletion
        Map<String, String> userToDelete = new HashMap<>();
        userToDelete.put("name", "Delete Me");
        userToDelete.put("email", "delete.me@example.com");
        userToDelete.put("role", "user");

        RequestBody createBody = RequestBody.create(objectMapper.writeValueAsString(userToDelete), JSON);
        Request createRequest = new Request.Builder().url(baseUrl()).post(createBody).build();

        long deleteId;
        try (Response createResponse = client.newCall(createRequest).execute()) {
            assertNotNull(createResponse.body());
            String body = createResponse.body().string();
            @SuppressWarnings("unchecked")
            Map<String, Object> created = objectMapper.readValue(body, Map.class);
            deleteId = ((Number) created.get("id")).longValue();
        }

        // Delete the user
        Request deleteRequest = new Request.Builder()
                .url(baseUrl() + "/" + deleteId)
                .delete()
                .build();

        try (Response deleteResponse = client.newCall(deleteRequest).execute()) {
            assertEquals(204, deleteResponse.code());
        }

        // Verify the user is deleted
        Request getRequest = new Request.Builder().url(baseUrl() + "/" + deleteId).get().build();
        try (Response getResponse = client.newCall(getRequest).execute()) {
            assertEquals(500, getResponse.code());
        }
    }

    // ==================== Edge Cases and Error Handling Tests ====================

    @Test
    @Order(15)
    @DisplayName("GET /api/users - Empty pagination parameters use defaults")
    void testGetListWithDefaultParameters() throws IOException {
        Request request = new Request.Builder().url(baseUrl()).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertFalse(responseBody.isEmpty());
        }
    }

    @Test
    @Order(16)
    @DisplayName("PUT /api/users/{id} - Update non-existent user returns error")
    void testUpdateNonExistentUser() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("role", "admin");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(baseUrl() + "/99999")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(500, response.code());
        }
    }

    @Test
    @Order(17)
    @DisplayName("GET /api/users - Verify X-Total-Count header is present")
    void testGetListWithTotalCountHeader() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            String totalCount = response.header("X-Total-Count");
            assertNotNull(totalCount);
            assertTrue(totalCount.matches("\\d+"));
        }
    }

    @Test
    @Order(18)
    @DisplayName("GET /api/users - Test pagination with offset")
    void testGetListWithOffset() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "1")
                .addQueryParameter("_end", "3")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertTrue(responseBody.size() <= 2);
        }
    }
}

