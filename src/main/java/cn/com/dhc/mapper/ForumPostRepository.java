package cn.com.dhc.mapper;

import cn.com.dhc.Entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // Find all active posts, ordered by pinned status and creation date
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> findAllActivePosts(Pageable pageable);

    // Find posts by author
    Page<ForumPost> findByAuthorIdAndAuthorTypeAndStatusOrderByCreatedAtDesc(
            String authorId, String authorType, String status, Pageable pageable);

    // Search posts by title or content
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // Find popular posts (by view count)
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.viewCount DESC, p.createdAt DESC")
    List<ForumPost> findPopularPosts(Pageable pageable);

    // Find posts with most likes
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' " +
           "ORDER BY p.likeCount DESC, p.createdAt DESC")
    List<ForumPost> findMostLikedPosts(Pageable pageable);

    // Find posts by category
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' AND p.category = :category " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> findByCategory(@Param("category") String category, Pageable pageable);

    // Find posts by category and subcategory
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' AND p.category = :category AND p.subcategory = :subcategory " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> findByCategoryAndSubcategory(
            @Param("category") String category,
            @Param("subcategory") String subcategory,
            Pageable pageable);

    // Search posts by category and keyword
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' AND p.category = :category AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> searchPostsByCategory(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Search posts by category, subcategory and keyword
    @Query("SELECT p FROM ForumPost p WHERE p.status = 'ACTIVE' AND p.category = :category AND p.subcategory = :subcategory AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.isPinned DESC, p.createdAt DESC")
    Page<ForumPost> searchPostsByCategoryAndSubcategory(
            @Param("category") String category,
            @Param("subcategory") String subcategory,
            @Param("keyword") String keyword,
            Pageable pageable);
}
