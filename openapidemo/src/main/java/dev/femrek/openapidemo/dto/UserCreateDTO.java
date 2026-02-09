package dev.femrek.openapidemo.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating a new User.
 * This class contains the required fields for user creation,
 * excluding the auto-generated ID field.
 */
@Setter
@Getter
@SuppressWarnings("unused")
public class UserCreateDTO {
    private String name;
    private String email;
    private String role;
}
