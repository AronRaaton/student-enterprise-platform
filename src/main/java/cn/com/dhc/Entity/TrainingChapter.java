package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_chapter")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingChapter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long chapterId;
    
    @Column(name = "material_id", nullable = false)
    private Long materialId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", insertable = false, updatable = false)
    private TrainingMaterial trainingMaterial;
    
    @OneToMany(mappedBy = "trainingChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingFile> files = new ArrayList<>();
}
