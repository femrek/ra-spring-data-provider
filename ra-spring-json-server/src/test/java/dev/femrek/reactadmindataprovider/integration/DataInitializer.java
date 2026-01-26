package dev.femrek.reactadmindataprovider.integration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Initializes the database with sample users for integration testing.
 * This component only runs when the 'test' or 'dev' profile is active to prevent data loss in production.
 */
@Component
@Profile({"test", "dev"})
class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Clear existing data
        userRepository.deleteAll();

        // Add sample users for testing using batch insert
        User user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setRole("admin");

        User user2 = new User();
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setRole("user");

        User user3 = new User();
        user3.setName("Bob Johnson");
        user3.setEmail("bob.johnson@example.com");
        user3.setRole("user");

        User user4 = new User();
        user4.setName("Alice Williams");
        user4.setEmail("alice.williams@example.com");
        user4.setRole("admin");

        User user5 = new User();
        user5.setName("Charlie Brown");
        user5.setEmail("charlie.brown@example.com");
        user5.setRole("moderator");

        // Batch insert all users in a single transaction
        userRepository.saveAll(java.util.List.of(user1, user2, user3, user4, user5));

        System.out.println("✅ Initialized database with " + userRepository.count() + " sample users");
    }
}

