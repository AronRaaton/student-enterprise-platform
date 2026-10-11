package cn.com.dhc.controller;

import cn.com.dhc.dto.RatingDTO;
import cn.com.dhc.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 任务评分相关接口
    @PostMapping("/tasks/{taskId}")
    public ResponseEntity<?> rateTask(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.parseLong(requestBody.get("studentId").toString());
            Integer rating = Integer.parseInt(requestBody.get("rating").toString());
            String comment = (String) requestBody.get("comment");

            RatingDTO ratingDTO = ratingService.rateTask(taskId, studentId, rating, comment);
            return ResponseEntity.ok(ratingDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskRatings(@PathVariable Long taskId) {
        try {
            List<RatingDTO> ratings = ratingService.getTaskRatings(taskId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/tasks/{taskId}/students/{studentId}")
    public ResponseEntity<?> getStudentTaskRating(
            @PathVariable Long taskId,
            @PathVariable Long studentId) {
        try {
            RatingDTO rating = ratingService.getStudentTaskRating(taskId, studentId);
            if (rating == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "未找到评分");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/tasks/{taskId}/average")
    public ResponseEntity<?> getTaskAverageRating(@PathVariable Long taskId) {
        try {
            Double averageRating = ratingService.getTaskAverageRating(taskId);
            Map<String, Object> response = new HashMap<>();
            response.put("taskId", taskId);
            response.put("averageRating", averageRating != null ? averageRating : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取平均评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 企业评分相关接口
    @PostMapping("/enterprises/{enterpriseId}")
    public ResponseEntity<?> rateEnterprise(
            @PathVariable Long enterpriseId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.parseLong(requestBody.get("studentId").toString());
            Integer rating = Integer.parseInt(requestBody.get("rating").toString());
            String comment = (String) requestBody.get("comment");

            RatingDTO ratingDTO = ratingService.rateEnterprise(enterpriseId, studentId, rating, comment);
            return ResponseEntity.ok(ratingDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/enterprises/{enterpriseId}")
    public ResponseEntity<?> getEnterpriseRatings(@PathVariable Long enterpriseId) {
        try {
            List<RatingDTO> ratings = ratingService.getEnterpriseRatings(enterpriseId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/enterprises/{enterpriseId}/students/{studentId}")
    public ResponseEntity<?> getStudentEnterpriseRating(
            @PathVariable Long enterpriseId,
            @PathVariable Long studentId) {
        try {
            RatingDTO rating = ratingService.getStudentEnterpriseRating(enterpriseId, studentId);
            if (rating == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "未找到评分");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(rating);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/enterprises/{enterpriseId}/average")
    public ResponseEntity<?> getEnterpriseAverageRating(@PathVariable Long enterpriseId) {
        try {
            Double averageRating = ratingService.getEnterpriseAverageRating(enterpriseId);
            Map<String, Object> response = new HashMap<>();
            response.put("enterpriseId", enterpriseId);
            response.put("averageRating", averageRating != null ? averageRating : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取平均评分失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
