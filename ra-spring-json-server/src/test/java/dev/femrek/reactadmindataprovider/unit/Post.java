package dev.femrek.reactadmindataprovider.unit;

import jakarta.persistence.*;

/**
 * Post entity for testing getManyReference operations.
 * Each post belongs to a user (many-to-one relationship).
 */
@Entity
@Table(name = "posts")
@SuppressWarnings("unused")
class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(nullable = false)
    private Long userId;

    private String status;

    public Post() {
    }

    public Post(String title, String content, Long userId, String status) {
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

