package cn.com.dhc.Entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "student_follow_enterprise")
@Data
public class StudentFollowEnterprise implements Serializable {

    @EmbeddedId
    private StudentFollowEnterpriseId id;

    @Column(name = "student_id", insertable = false, updatable = false)
    private Long studentId;

    @Column(name = "enterprise_id", insertable = false, updatable = false)
    private Long enterpriseId;
}
