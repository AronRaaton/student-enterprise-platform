package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByEmail(String email);

    boolean existsByEmail(String email);

    Student findByEmailAndPassword(String attr0, String attr1);

    Student findByStudentId(Long studentId);


//    @Query("SELECT s FROM Student s ORDER BY s.collectedCount DESC LIMIT :topN")
//    List<Student> findTopByCollectedCount(@Param("topN") int topN);

}