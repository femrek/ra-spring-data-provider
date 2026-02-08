package dev.femrek.reactadmindataprovider.integration;

/**
 * Response DTO for Post entity.
 */
@SuppressWarnings("unused")
class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String status;

    public PostResponseDTO() {
    }

    public PostResponseDTO(Long id, String title, String content, Long userId, String status) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

