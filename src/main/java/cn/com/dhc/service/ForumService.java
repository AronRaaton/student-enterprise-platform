package cn.com.dhc.service;

import cn.com.dhc.Entity.*;
import cn.com.dhc.dto.*;
import cn.com.dhc.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final ForumLikeRepository forumLikeRepository;
    private final StudentRepository studentRepository;
    private final EnterpriseRepository enterpriseRepository;

    // Post methods

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getAllPosts(int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.findAllActivePosts(pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional
    public ForumPostDTO getPostById(Long postId, String currentUserId, String currentUserType) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Increment view count
        post.setViewCount(post.getViewCount() + 1);
        forumPostRepository.save(post);

        return convertToPostDTO(post, currentUserId, currentUserType);
    }

    @Transactional
    public ForumPostDTO createPost(CreateForumPostRequest request) {
        ForumPost post = new ForumPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(request.getAuthorId());
        post.setAuthorType(request.getAuthorType());
        post.setCategory(request.getCategory());
        post.setSubcategory(request.getSubcategory());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        ForumPost savedPost = forumPostRepository.save(post);

        return convertToPostDTO(savedPost, request.getAuthorId(), request.getAuthorType());
    }

    @Transactional
    public ForumPostDTO updatePost(Long postId, CreateForumPostRequest request) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Check if the user is the author
        if (!post.getAuthorId().equals(request.getAuthorId()) ||
            !post.getAuthorType().equals(request.getAuthorType())) {
            throw new RuntimeException("You are not authorized to update this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setSubcategory(request.getSubcategory());
        post.setUpdatedAt(LocalDateTime.now());

        ForumPost updatedPost = forumPostRepository.save(post);

        return convertToPostDTO(updatedPost, request.getAuthorId(), request.getAuthorType());
    }

    @Transactional
    public void deletePost(Long postId, String userId, String userType) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Check if the user is the author
        if (!post.getAuthorId().equals(userId) || !post.getAuthorType().equals(userType)) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        post.setStatus("DELETED");
        forumPostRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> searchPosts(String keyword, int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.searchPosts(keyword, pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional(readOnly = true)
    public List<ForumPostDTO> getPopularPosts(int limit, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(0, limit);
        List<ForumPost> posts = forumPostRepository.findPopularPosts(pageable);

        return posts.stream()
                .map(post -> convertToPostDTO(post, currentUserId, currentUserType))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getPostsByCategory(String category, int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.findByCategory(category, pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getPostsByCategoryAndSubcategory(String category, String subcategory, int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.findByCategoryAndSubcategory(category, subcategory, pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> searchPostsByCategory(String category, String keyword, int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.searchPostsByCategory(category, keyword, pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> searchPostsByCategoryAndSubcategory(String category, String subcategory, String keyword, int page, int size, String currentUserId, String currentUserType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> posts = forumPostRepository.searchPostsByCategoryAndSubcategory(category, subcategory, keyword, pageable);

        return posts.map(post -> convertToPostDTO(post, currentUserId, currentUserType));
    }

    @Transactional(readOnly = true)
    public Page<ForumPostDTO> getLikedPostsByUser(String userId, String userType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ForumPost> likedPosts = forumLikeRepository.findPostsLikedByUser(userId, userType, pageable);

        return likedPosts.map(post -> convertToPostDTO(post, userId, userType));
    }

    // Comment methods

    @Transactional(readOnly = true)
    public List<ForumCommentDTO> getCommentsForPost(Long postId) {
        List<ForumComment> rootComments = forumCommentRepository.findByPostPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);

        return rootComments.stream()
                .map(this::convertToCommentDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional
    public ForumCommentDTO createComment(CreateForumCommentRequest request) {
        ForumPost post = forumPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + request.getPostId()));

        ForumComment comment = new ForumComment();
        comment.setPost(post);
        comment.setContent(request.getContent());
        comment.setAuthorId(request.getAuthorId());
        comment.setAuthorType(request.getAuthorType());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // Set parent comment if it exists
        if (request.getParentCommentId() != null) {
            ForumComment parentComment = forumCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + request.getParentCommentId()));
            comment.setParentComment(parentComment);
        }

        ForumComment savedComment = forumCommentRepository.save(comment);

        return convertToCommentDTO(savedComment);
    }

    @Transactional
    public ForumCommentDTO updateComment(Long commentId, String content, String userId, String userType) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if the user is the author
        if (!comment.getAuthorId().equals(userId) || !comment.getAuthorType().equals(userType)) {
            throw new RuntimeException("You are not authorized to update this comment");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        ForumComment updatedComment = forumCommentRepository.save(comment);

        return convertToCommentDTO(updatedComment);
    }

    @Transactional
    public void deleteComment(Long commentId, String userId, String userType) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if the user is the author
        if (!comment.getAuthorId().equals(userId) || !comment.getAuthorType().equals(userType)) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        forumCommentRepository.delete(comment);
    }

    // Like methods

    @Transactional
    public ForumPostDTO toggleLike(Long postId, String userId, String userType) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<ForumLike> existingLike = forumLikeRepository.findByPostPostIdAndUserIdAndUserType(
                postId, userId, userType);

        if (existingLike.isPresent()) {
            // Unlike
            forumLikeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // Like
            ForumLike like = new ForumLike();
            like.setPost(post);
            like.setUserId(userId);
            like.setUserType(userType);
            like.setCreatedAt(LocalDateTime.now());
            forumLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        ForumPost updatedPost = forumPostRepository.save(post);

        return convertToPostDTO(updatedPost, userId, userType);
    }

    // Helper methods

    private ForumPostDTO convertToPostDTO(ForumPost post, String currentUserId, String currentUserType) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getAuthorId());
        dto.setAuthorType(post.getAuthorType());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setIsPinned(post.getIsPinned());
        dto.setStatus(post.getStatus());
        dto.setCategory(post.getCategory());
        dto.setSubcategory(post.getSubcategory());

        // Set category and subcategory display names
        if (post.getCategory() != null) {
            try {
                ForumPost.Category category = ForumPost.Category.valueOf(post.getCategory());
                dto.setCategoryDisplayName(category.getDisplayName());

                if (post.getSubcategory() != null) {
                    switch (category) {
                        case EMPLOYMENT:
                            ForumPost.EmploymentSubcategory empSubcategory = ForumPost.EmploymentSubcategory.valueOf(post.getSubcategory());
                            dto.setSubcategoryDisplayName(empSubcategory.getDisplayName());
                            break;
                        case EVENTS:
                            ForumPost.EventsSubcategory eventsSubcategory = ForumPost.EventsSubcategory.valueOf(post.getSubcategory());
                            dto.setSubcategoryDisplayName(eventsSubcategory.getDisplayName());
                            break;
                        case COMMUNITY:
                            ForumPost.CommunitySubcategory communitySubcategory = ForumPost.CommunitySubcategory.valueOf(post.getSubcategory());
                            dto.setSubcategoryDisplayName(communitySubcategory.getDisplayName());
                            break;
                        default:
                            dto.setSubcategoryDisplayName(post.getSubcategory());
                    }
                }
            } catch (IllegalArgumentException e) {
                // If category is not in enum, use the raw value
                dto.setCategoryDisplayName(post.getCategory());
                if (post.getSubcategory() != null) {
                    dto.setSubcategoryDisplayName(post.getSubcategory());
                }
            }
        }

        // Set author name based on author type
        if ("STUDENT".equals(post.getAuthorType())) {
            Student student = studentRepository.findByStudentId(Long.valueOf(post.getAuthorId()));
            if (student != null) {
                dto.setAuthorName(student.getName());
            }
        } else if ("ENTERPRISE".equals(post.getAuthorType())) {
            Enterprise enterprise = enterpriseRepository.findByEnterpriseId(Long.valueOf(post.getAuthorId()));
            if (enterprise != null) {
                dto.setAuthorName(enterprise.getName());
            }
        }

        // Set comment count
        Long commentCount = forumCommentRepository.countCommentsByPostId(post.getPostId());
        dto.setCommentCount(commentCount.intValue());

        // Check if the current user has liked this post
        boolean isLiked = forumLikeRepository.findByPostPostIdAndUserIdAndUserType(
                post.getPostId(), currentUserId, currentUserType).isPresent();
        dto.setIsLikedByCurrentUser(isLiked);

        return dto;
    }

    private ForumCommentDTO convertToCommentDTO(ForumComment comment) {
        ForumCommentDTO dto = new ForumCommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPost().getPostId());
        dto.setContent(comment.getContent());
        dto.setAuthorId(comment.getAuthorId());
        dto.setAuthorType(comment.getAuthorType());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getCommentId());
        }

        // Set author name based on author type
        if ("STUDENT".equals(comment.getAuthorType())) {
            Student student = studentRepository.findByStudentId(Long.valueOf(comment.getAuthorId()));
            if (student != null) {
                dto.setAuthorName(student.getName());
            }
        } else if ("ENTERPRISE".equals(comment.getAuthorType())) {
            Enterprise enterprise = enterpriseRepository.findByEnterpriseId(Long.valueOf(comment.getAuthorId()));
            if (enterprise != null) {
                dto.setAuthorName(enterprise.getName());
            }
        }

        return dto;
    }

    private ForumCommentDTO convertToCommentDTOWithChildren(ForumComment comment) {
        ForumCommentDTO dto = convertToCommentDTO(comment);

        // Recursively add child comments
        List<ForumComment> childComments = forumCommentRepository.findByParentCommentCommentIdOrderByCreatedAtAsc(comment.getCommentId());
        List<ForumCommentDTO> childDTOs = childComments.stream()
                .map(this::convertToCommentDTO)
                .collect(Collectors.toList());

        dto.setChildComments(childDTOs);

        return dto;
    }
}
