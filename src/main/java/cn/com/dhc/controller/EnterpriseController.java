package cn.com.dhc.controller;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.service.EnterpriseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enterprises")
@RequiredArgsConstructor
public class EnterpriseController {
    private final EnterpriseService enterpriseService;
    private StudentRepository studentRepository;

    // 收藏学生
    @PostMapping("/{enterpriseId}/favorites/{studentId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable Long enterpriseId,
            @PathVariable Long studentId
    ) {
        enterpriseService.addToFavorites(enterpriseId, studentId);
        return ResponseEntity.ok().build();
    }

    // 取消收藏
    @DeleteMapping("/{enterpriseId}/favorites/{studentId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable Long enterpriseId,
            @PathVariable Long studentId
    ) {
        enterpriseService.removeFromFavorites(enterpriseId, studentId);
        return ResponseEntity.ok().build();
    }

    // 获取收藏列表
    @GetMapping("/{enterpriseId}/favorites")
    public ResponseEntity<List<Student>> getFavorites(
            @PathVariable Long enterpriseId
    ) {
        return ResponseEntity.ok(enterpriseService.getFavoriteStudents(enterpriseId));
    }

    @GetMapping("/{enterpriseId}")
    public ResponseEntity<Enterprise> getEnterprise(
            @PathVariable Long enterpriseId
    ) {
        return ResponseEntity.ok(enterpriseService.getEnterprise(enterpriseId));
    }

    // Removed duplicate endpoint that was causing conflicts
    // This functionality is now available at /api/enterprise-students/{enterpriseId}

//    @GetMapping("/students/popular")
//    public ResponseEntity<List<Student>> getPopularStudents(
//            @RequestParam(defaultValue = "10") int topN
//    ) {
//        return ResponseEntity.ok(studentRepository.findTopByCollectedCount(topN));
//    }
}
