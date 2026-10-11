package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumCommentDTO {
    private Long commentId;
    private Long postId;
    private String content;
    private String authorId;
    private String authorType;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentCommentId;
    private List<ForumCommentDTO> childComments = new ArrayList<>();
}
