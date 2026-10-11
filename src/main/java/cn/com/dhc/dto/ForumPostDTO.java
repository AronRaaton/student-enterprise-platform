package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDTO {
    private Long postId;
    private String title;
    private String content;
    private String authorId;
    private String authorType;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isPinned;
    private String status;
    private String category;
    private String subcategory;
    private String categoryDisplayName;
    private String subcategoryDisplayName;
    private Boolean isLikedByCurrentUser;
}
