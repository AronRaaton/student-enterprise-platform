package cn.com.dhc.Entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@IdClass(EnterpriseFavoriteId.class)
public class EnterpriseFavorite {
    @Id
    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDateTime createdTime;
}

