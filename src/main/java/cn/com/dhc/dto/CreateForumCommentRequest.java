package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateForumCommentRequest {
    private Long postId;
    private String content;
    private String authorId;
    private String authorType;
    private Long parentCommentId;
}
