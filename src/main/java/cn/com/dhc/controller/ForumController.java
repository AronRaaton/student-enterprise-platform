package cn.com.dhc.controller;

import cn.com.dhc.dto.*;
import cn.com.dhc.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Add CORS support
public class ForumController {

    private final ForumService forumService;

    // Post endpoints

    @GetMapping("/posts")
    public ResponseEntity<Page<ForumPostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.getAllPosts(page, size, currentUserId, currentUserType));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ForumPostDTO> getPostById(
            @PathVariable Long postId,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.getPostById(postId, currentUserId, currentUserType));
    }

    @PostMapping("/posts")
    public ResponseEntity<ForumPostDTO> createPost(@RequestBody CreateForumPostRequest request) {
        return ResponseEntity.ok(forumService.createPost(request));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<ForumPostDTO> updatePost(
            @PathVariable Long postId,
            @RequestBody CreateForumPostRequest request) {
        return ResponseEntity.ok(forumService.updatePost(postId, request));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam String userId,
            @RequestParam String userType) {
        forumService.deletePost(postId, userId, userType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts/search")
    public ResponseEntity<Page<ForumPostDTO>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.searchPosts(keyword, page, size, currentUserId, currentUserType));
    }

    @GetMapping("/posts/popular")
    public ResponseEntity<List<ForumPostDTO>> getPopularPosts(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.getPopularPosts(limit, currentUserId, currentUserType));
    }

    @GetMapping("/posts/category/{category}")
    public ResponseEntity<Page<ForumPostDTO>> getPostsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.getPostsByCategory(category, page, size, currentUserId, currentUserType));
    }

    @GetMapping("/posts/category/{category}/subcategory/{subcategory}")
    public ResponseEntity<Page<ForumPostDTO>> getPostsByCategoryAndSubcategory(
            @PathVariable String category,
            @PathVariable String subcategory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.getPostsByCategoryAndSubcategory(category, subcategory, page, size, currentUserId, currentUserType));
    }

    @GetMapping("/posts/category/{category}/search")
    public ResponseEntity<Page<ForumPostDTO>> searchPostsByCategory(
            @PathVariable String category,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.searchPostsByCategory(category, keyword, page, size, currentUserId, currentUserType));
    }

    @GetMapping("/posts/category/{category}/subcategory/{subcategory}/search")
    public ResponseEntity<Page<ForumPostDTO>> searchPostsByCategoryAndSubcategory(
            @PathVariable String category,
            @PathVariable String subcategory,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String currentUserId,
            @RequestParam String currentUserType) {
        return ResponseEntity.ok(forumService.searchPostsByCategoryAndSubcategory(category, subcategory, keyword, page, size, currentUserId, currentUserType));
    }

    // Comment endpoints

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<ForumCommentDTO>> getCommentsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(forumService.getCommentsForPost(postId));
    }

    @PostMapping("/comments")
    public ResponseEntity<ForumCommentDTO> createComment(@RequestBody CreateForumCommentRequest request) {
        return ResponseEntity.ok(forumService.createComment(request));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ForumCommentDTO> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(forumService.updateComment(
                commentId,
                request.get("content"),
                request.get("userId"),
                request.get("userType")));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String userId,
            @RequestParam String userType) {
        forumService.deleteComment(commentId, userId, userType);
        return ResponseEntity.ok().build();
    }

    // Like endpoints

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ForumPostDTO> toggleLike(
            @PathVariable Long postId,
            @RequestParam String userId,
            @RequestParam String userType) {
        return ResponseEntity.ok(forumService.toggleLike(postId, userId, userType));
    }

    @GetMapping("/user/liked-posts")
    public ResponseEntity<Page<ForumPostDTO>> getLikedPosts(
            @RequestParam String userId,
            @RequestParam String userType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(forumService.getLikedPostsByUser(userId, userType, page, size));
    }
}
