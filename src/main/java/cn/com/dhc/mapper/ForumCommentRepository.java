package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ForumComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    
    // Find all comments for a post, ordered by creation date
    List<ForumComment> findByPostPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);
    
    // Find all child comments for a parent comment
    List<ForumComment> findByParentCommentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    // Find comments by author
    Page<ForumComment> findByAuthorIdAndAuthorTypeOrderByCreatedAtDesc(
            String authorId, String authorType, Pageable pageable);
    
    // Count comments for a post
    @Query("SELECT COUNT(c) FROM ForumComment c WHERE c.post.postId = :postId")
    Long countCommentsByPostId(@Param("postId") Long postId);
}
