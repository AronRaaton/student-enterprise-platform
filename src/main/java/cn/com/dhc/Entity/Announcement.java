package cn.com.dhc.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long announcementId;

    @NotBlank
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    @JsonIgnore
    private Enterprise enterprise;

    private LocalDateTime createdTime;
    private LocalDateTime reviewedTime;
    private String feedback;
}
