package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;
    
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_type", nullable = false)
    private String fileType;
    
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", insertable = false, updatable = false)
    private TrainingChapter trainingChapter;
}
