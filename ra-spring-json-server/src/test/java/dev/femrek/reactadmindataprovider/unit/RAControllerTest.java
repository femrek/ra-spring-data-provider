package dev.femrek.reactadmindataprovider.unit;

import okhttp3.*;
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
 * Integration tests for RAController using real HTTP requests via OkHttp.
 * Tests all endpoints.
 * No mocking is used - all tests make actual HTTP calls to the running Spring Boot server.
 */
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RAControllerTest {
    @LocalServerPort
    private int port;
    private final UserRepository userRepository;

    @Autowired
    public RAControllerTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final okhttp3.MediaType JSON = okhttp3.MediaType.get("application/json; charset=utf-8");

    private static Long createdUserId1;
    private static Long createdUserId2;
    private static Long createdUserId3;
    private static Long createdUserId4;
    private static Long createdUserId5;

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
    @DisplayName("POST /api/users - Create first user")
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
    @DisplayName("POST /api/users - Create additional users for bulk tests")
    void testCreateAdditionalUsers() throws IOException {
        String[][] users = {
                {"Jane Smith", "jane.smith@example.com", "user"},
                {"Bob Johnson", "bob.johnson@example.com", "moderator"},
                {"Alice Williams", "alice.williams@example.com", "user"},
                {"Charlie Brown", "charlie.brown@example.com", "user"}
        };

        Long[] userIds = new Long[4];

        for (int i = 0; i < users.length; i++) {
            Map<String, String> user = new HashMap<>();
            user.put("name", users[i][0]);
            user.put("email", users[i][1]);
            user.put("role", users[i][2]);

            RequestBody body = RequestBody.create(objectMapper.writeValueAsString(user), JSON);
            Request request = new Request.Builder().url(baseUrl()).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                assertEquals(201, response.code());
                assertNotNull(response.body());
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = objectMapper.readValue(response.body().string(), Map.class);
                userIds[i] = ((Number) responseBody.get("id")).longValue();
            }
        }

        createdUserId2 = userIds[0];
        createdUserId3 = userIds[1];
        createdUserId4 = userIds[2];
        createdUserId5 = userIds[3];
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

    // ==================== GET LIST (GET /) Tests ====================

    @Test
    @Order(4)
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
            assertTrue(responseBody.size() >= 5, "Should have at least 5 users");
        }
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/users?role=user - Get list with field filter")
    void testGetListWithFilter() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("role", "user")
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
            // Verify all returned users have role "user"
            for (Map<String, Object> user : responseBody) {
                assertEquals("user", user.get("role"));
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/users?q=Alice - Get list with global search")
    void testGetListWithGlobalSearch() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("_start", "0")
                .addQueryParameter("_end", "10")
                .addQueryParameter("_sort", "id")
                .addQueryParameter("_order", "ASC")
                .addQueryParameter("q", "Alice")
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
            assertTrue(responseBody.get(0).get("name").toString().contains("Alice"));
        }
    }

    // ==================== GET MANY (GET /?id=1&id=2) Tests ====================

    @Test
    @Order(7)
    @DisplayName("GET /api/users?id=1&id=2&id=3 - Get multiple users by IDs")
    void testGetMany() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", createdUserId1.toString())
                .addQueryParameter("id", createdUserId2.toString())
                .addQueryParameter("id", createdUserId3.toString())
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Map<String, Object>> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );
            assertEquals(3, responseBody.size());
        }
    }

    // ==================== UPDATE (PUT /{id}) Tests ====================

    @Test
    @Order(8)
    @DisplayName("PUT /api/users/{id} - Update a single user")
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
    @Order(9)
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

    // ==================== UPDATE MANY (PUT /?id=1&id=2) Tests ====================

    @Test
    @Order(10)
    @DisplayName("PUT /api/users?id=2&id=3 - Update multiple users with same values")
    void testUpdateMany() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("role", "premium_user");

        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", createdUserId2.toString())
                .addQueryParameter("id", createdUserId3.toString())
                .build();

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Number> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertEquals(2, responseBody.size());
            assertTrue(responseBody.contains(createdUserId2.intValue()));
            assertTrue(responseBody.contains(createdUserId3.intValue()));
        }

        // Verify the updates were applied
        Request getRequest2 = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId2)
                .get()
                .build();

        try (Response response = client.newCall(getRequest2).execute()) {
            assertNotNull(response.body());
            @SuppressWarnings("unchecked")
            Map<String, Object> user2 = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals("premium_user", user2.get("role"));
        }

        Request getRequest3 = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId3)
                .get()
                .build();

        try (Response response = client.newCall(getRequest3).execute()) {
            assertNotNull(response.body());
            @SuppressWarnings("unchecked")
            Map<String, Object> user3 = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals("premium_user", user3.get("role"));
        }
    }

    @Test
    @Order(11)
    @DisplayName("PUT /api/users?id=4&id=5 - Update multiple users with same role")
    void testUpdateManyMultipleFields() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("role", "vip_user");

        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", createdUserId4.toString())
                .addQueryParameter("id", createdUserId5.toString())
                .build();

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Number> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertEquals(2, responseBody.size());
        }

        // Verify the role was updated for both users
        Request getRequest4 = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId4)
                .get()
                .build();

        try (Response response = client.newCall(getRequest4).execute()) {
            assertNotNull(response.body());
            @SuppressWarnings("unchecked")
            Map<String, Object> user = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals("vip_user", user.get("role"));
        }

        Request getRequest5 = new Request.Builder()
                .url(baseUrl() + "/" + createdUserId5)
                .get()
                .build();

        try (Response response = client.newCall(getRequest5).execute()) {
            assertNotNull(response.body());
            @SuppressWarnings("unchecked")
            Map<String, Object> user = objectMapper.readValue(response.body().string(), Map.class);
            assertEquals("vip_user", user.get("role"));
        }
    }

    @Test
    @Order(12)
    @DisplayName("PUT /api/users - Update many with empty ID list returns empty list")
    void testUpdateManyEmptyList() throws IOException {
        Map<String, String> updates = new HashMap<>();
        updates.put("role", "test");

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(updates), JSON);
        Request request = new Request.Builder()
                .url(baseUrl())
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Number> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertTrue(responseBody.isEmpty());
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

    // ==================== DELETE MANY (DELETE /?id=1&id=2) Tests ====================

    @Test
    @Order(14)
    @DisplayName("DELETE /api/users?id=4&id=5 - Delete multiple users")
    void testDeleteMany() throws IOException {
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", createdUserId4.toString())
                .addQueryParameter("id", createdUserId5.toString())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Number> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertEquals(2, responseBody.size());
            assertTrue(responseBody.contains(createdUserId4.intValue()));
            assertTrue(responseBody.contains(createdUserId5.intValue()));
        }

        // Verify the users are deleted
        Request getRequest4 = new Request.Builder().url(baseUrl() + "/" + createdUserId4).get().build();
        try (Response response = client.newCall(getRequest4).execute()) {
            assertEquals(500, response.code());
        }

        Request getRequest5 = new Request.Builder().url(baseUrl() + "/" + createdUserId5).get().build();
        try (Response response = client.newCall(getRequest5).execute()) {
            assertEquals(500, response.code());
        }
    }

    @Test
    @Order(15)
    @DisplayName("DELETE /api/users - Delete many with empty ID list returns empty list")
    void testDeleteManyEmptyList() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl())
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());

            List<Number> responseBody = objectMapper.readValue(
                    response.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertTrue(responseBody.isEmpty());
        }
    }

    @Test
    @Order(16)
    @DisplayName("DELETE /api/users?id=1&id=2&id=3 - Delete multiple users at once")
    void testDeleteManyBulk() throws IOException {
        // Create 3 users specifically for bulk deletion
        Long[] deleteIds = new Long[3];
        for (int i = 0; i < 3; i++) {
            Map<String, String> user = new HashMap<>();
            user.put("name", "Bulk Delete " + (i + 1));
            user.put("email", "bulkdelete" + (i + 1) + "@example.com");
            user.put("role", "user");

            RequestBody createBody = RequestBody.create(objectMapper.writeValueAsString(user), JSON);
            Request createRequest = new Request.Builder().url(baseUrl()).post(createBody).build();

            try (Response createResponse = client.newCall(createRequest).execute()) {
                assertNotNull(createResponse.body());
                @SuppressWarnings("unchecked")
                Map<String, Object> created = objectMapper.readValue(createResponse.body().string(), Map.class);
                deleteIds[i] = ((Number) created.get("id")).longValue();
            }
        }

        // Delete all 3 users
        HttpUrl url = baseHttpUrl().newBuilder()
                .addQueryParameter("id", deleteIds[0].toString())
                .addQueryParameter("id", deleteIds[1].toString())
                .addQueryParameter("id", deleteIds[2].toString())
                .build();

        Request deleteRequest = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response deleteResponse = client.newCall(deleteRequest).execute()) {
            assertEquals(200, deleteResponse.code());
            assertNotNull(deleteResponse.body());

            List<Number> responseBody = objectMapper.readValue(
                    deleteResponse.body().string(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Number.class)
            );
            assertEquals(3, responseBody.size());
        }

        // Verify all 3 are deleted
        for (Long deleteId : deleteIds) {
            Request getRequest = new Request.Builder().url(baseUrl() + "/" + deleteId).get().build();
            try (Response response = client.newCall(getRequest).execute()) {
                assertEquals(500, response.code());
            }
        }
    }

    // ==================== Edge Cases and Error Handling Tests ====================

    @Test
    @Order(17)
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

    @Test
    @Order(18)
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
    @Order(19)
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
    @Order(20)
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

    @Test
    @Order(21)
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
}

