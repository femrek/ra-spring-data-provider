package dev.femrek.reactadmindataprovider.unit;

/**
 * DTO for creating a new Post.
 */
@SuppressWarnings("unused")
class PostCreateDTO {
    private String title;
    private String content;
    private Long userId;
    private String status;

    public PostCreateDTO() {
    }

    public PostCreateDTO(String title, String content, Long userId, String status) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.status = status;
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

