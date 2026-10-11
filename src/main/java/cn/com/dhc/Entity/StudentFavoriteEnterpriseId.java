package cn.com.dhc.Entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
public class StudentFavoriteEnterpriseId implements Serializable {

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "enterprise_id")
    private Long enterpriseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentFavoriteEnterpriseId that = (StudentFavoriteEnterpriseId) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(enterpriseId, that.enterpriseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, enterpriseId);
    }
}
