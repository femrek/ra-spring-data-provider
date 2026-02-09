package dev.femrek.openapidemo.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for User responses.
 * This class contains all user fields including the ID,
 * used for API responses when returning user data.
 */
@Setter
@Getter
@SuppressWarnings("unused")
public class UserResponseDTO {
    /**
     * Gets the user's unique identifier.
     */
    private Long id;
    /**
     * Gets the user's name.
     */
    private String name;
    /**
     * Gets the user's email address.
     */
    private String email;
    /**
     * Gets the user's role.
     */
    private String role;
}

