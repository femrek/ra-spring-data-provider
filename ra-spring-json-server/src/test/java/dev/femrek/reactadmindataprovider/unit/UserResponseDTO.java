package dev.femrek.reactadmindataprovider.unit;

/**
 * Data Transfer Object for User responses.
 * This class contains all user fields including the ID,
 * used for API responses when returning user data.
 */
@SuppressWarnings("unused")
class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;

    /**
     * Gets the user's unique identifier.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's role.
     *
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
}

