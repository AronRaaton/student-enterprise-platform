package cn.com.dhc.controller;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import cn.com.dhc.service.StudentFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentFavoriteController {
    private final StudentFavoriteService studentFavoriteService;
    private final StudentRepository studentRepository;
    private final EnterpriseRepository enterpriseRepository;

    // 获取学生收藏的企业列表
    @GetMapping("/{studentId}/favorites/enterprises")
    public ResponseEntity<List<Enterprise>> getFavoriteEnterprises(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentFavoriteService.getFavoriteEnterprises(studentId));
    }

    // 学生收藏企业
    @PostMapping("/{studentId}/favorites/enterprises/{enterpriseId}")
    public ResponseEntity<Void> addFavoriteEnterprise(
            @PathVariable Long studentId,
            @PathVariable Long enterpriseId
    ) {
        studentFavoriteService.addFavoriteEnterprise(studentId, enterpriseId);
        return ResponseEntity.ok().build();
    }

    // 学生取消收藏企业
    @DeleteMapping("/{studentId}/favorites/enterprises/{enterpriseId}")
    public ResponseEntity<Void> removeFavoriteEnterprise(
            @PathVariable Long studentId,
            @PathVariable Long enterpriseId
    ) {
        studentFavoriteService.removeFavoriteEnterprise(studentId, enterpriseId);
        return ResponseEntity.ok().build();
    }
}
