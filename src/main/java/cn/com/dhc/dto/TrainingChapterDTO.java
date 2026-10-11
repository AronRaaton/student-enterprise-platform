package cn.com.dhc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingChapterDTO {
    private Long chapterId;
    private Long materialId;
    private String title;
    private String description;
    private String content;
    private Integer sequence;
    private List<TrainingFileDTO> files = new ArrayList<>();
}
