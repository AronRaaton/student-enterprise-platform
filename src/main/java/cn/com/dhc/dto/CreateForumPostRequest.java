package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateForumPostRequest {
    private String title;
    private String content;
    private String authorId;
    private String authorType;
    private String category;
    private String subcategory;
}
