package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ForumLike;
import cn.com.dhc.Entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {

    // Check if a user has liked a post
    Optional<ForumLike> findByPostPostIdAndUserIdAndUserType(Long postId, String userId, String userType);

    // Count likes for a post
    @Query("SELECT COUNT(l) FROM ForumLike l WHERE l.post.postId = :postId")
    Long countLikesByPostId(@Param("postId") Long postId);

    // Delete a like
    void deleteByPostPostIdAndUserIdAndUserType(Long postId, String userId, String userType);

    // Find all posts liked by a user
    @Query("SELECT l.post FROM ForumLike l WHERE l.userId = :userId AND l.userType = :userType AND l.post.status = 'ACTIVE' ORDER BY l.createdAt DESC")
    Page<ForumPost> findPostsLikedByUser(@Param("userId") String userId, @Param("userType") String userType, Pageable pageable);
}
