package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_material")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;
    
    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "industry_type", nullable = false)
    private String industryType;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();
    
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
    
    @OneToMany(mappedBy = "trainingMaterial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingChapter> chapters = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enterprise_id", insertable = false, updatable = false)
    private Enterprise enterprise;
}
