package dev.femrek.reactadmindataprovider.integration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Post entity.
 */
@Repository
interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
}

