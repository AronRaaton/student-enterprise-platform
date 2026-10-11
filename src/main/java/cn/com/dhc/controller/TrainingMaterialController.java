package cn.com.dhc.controller;

import cn.com.dhc.dto.TrainingChapterDTO;
import cn.com.dhc.dto.TrainingFileDTO;
import cn.com.dhc.dto.TrainingMaterialDTO;
import cn.com.dhc.service.TrainingMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingMaterialController {

    private final TrainingMaterialService trainingMaterialService;

    /**
     * 创建培训资料
     */
    @PostMapping
    public ResponseEntity<?> createTrainingMaterial(@RequestBody TrainingMaterialDTO materialDTO) {
        try {
            TrainingMaterialDTO createdMaterial = trainingMaterialService.createTrainingMaterial(materialDTO);
            return ResponseEntity.ok(createdMaterial);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "创建培训资料失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 添加培训章节
     */
    @PostMapping("/chapter")
    public ResponseEntity<?> addChapter(@RequestBody TrainingChapterDTO chapterDTO) {
        try {
            TrainingChapterDTO createdChapter = trainingMaterialService.addChapter(chapterDTO);
            return ResponseEntity.ok(createdChapter);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "添加培训章节失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 上传培训文件
     */
    @PostMapping("/file/{chapterId}")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long chapterId,
            @RequestParam("file") MultipartFile file) {
        try {
            TrainingFileDTO uploadedFile = trainingMaterialService.uploadFile(chapterId, file);
            return ResponseEntity.ok(uploadedFile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "上传培训文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取培训资料详情
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<?> getTrainingMaterial(@PathVariable Long materialId) {
        try {
            TrainingMaterialDTO material = trainingMaterialService.getTrainingMaterialById(materialId);
            if (material != null) {
                return ResponseEntity.ok(material);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "培训资料不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取培训资料失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取企业的所有培训资料
     */
    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<List<TrainingMaterialDTO>> getEnterpriseTrainingMaterials(@PathVariable Long enterpriseId) {
        List<TrainingMaterialDTO> materials = trainingMaterialService.getEnterpriseTrainingMaterials(enterpriseId);
        return ResponseEntity.ok(materials);
    }

    /**
     * 获取特定行业类型的培训资料
     */
    @GetMapping("/industry/{industryType}")
    public ResponseEntity<List<TrainingMaterialDTO>> getTrainingMaterialsByIndustryType(@PathVariable String industryType) {
        List<TrainingMaterialDTO> materials = trainingMaterialService.getTrainingMaterialsByIndustryType(industryType);
        return ResponseEntity.ok(materials);
    }

    /**
     * 获取所有行业类型
     */
    @GetMapping("/industry-types")
    public ResponseEntity<List<String>> getAllIndustryTypes() {
        List<String> industryTypes = trainingMaterialService.getAllIndustryTypes();
        return ResponseEntity.ok(industryTypes);
    }

    /**
     * 搜索培训资料
     */
    @GetMapping("/search")
    public ResponseEntity<List<TrainingMaterialDTO>> searchTrainingMaterials(@RequestParam String keyword) {
        List<TrainingMaterialDTO> materials = trainingMaterialService.searchTrainingMaterials(keyword);
        return ResponseEntity.ok(materials);
    }

    /**
     * 获取所有培训资料
     */
    @GetMapping
    public ResponseEntity<List<TrainingMaterialDTO>> getAllTrainingMaterials() {
        List<TrainingMaterialDTO> materials = trainingMaterialService.getAllTrainingMaterials();
        return ResponseEntity.ok(materials);
    }

    /**
     * 删除培训资料
     */
    @DeleteMapping("/{materialId}")
    public ResponseEntity<?> deleteTrainingMaterial(@PathVariable Long materialId) {
        try {
            trainingMaterialService.deleteTrainingMaterial(materialId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "培训资料删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "删除培训资料失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 删除培训章节
     */
    @DeleteMapping("/chapter/{chapterId}")
    public ResponseEntity<?> deleteTrainingChapter(@PathVariable Long chapterId) {
        try {
            trainingMaterialService.deleteTrainingChapter(chapterId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "培训章节删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "删除培训章节失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 删除培训文件
     */
    @DeleteMapping("/file/{fileId}")
    public ResponseEntity<?> deleteTrainingFile(@PathVariable Long fileId) {
        try {
            trainingMaterialService.deleteTrainingFile(fileId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "培训文件删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "删除培训文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 更新培训资料
     */
    @PutMapping("/{materialId}")
    public ResponseEntity<?> updateTrainingMaterial(
            @PathVariable Long materialId,
            @RequestBody TrainingMaterialDTO materialDTO) {
        try {
            TrainingMaterialDTO updatedMaterial = trainingMaterialService.updateTrainingMaterial(materialId, materialDTO);
            return ResponseEntity.ok(updatedMaterial);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新培训资料失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 更新培训章节
     */
    @PutMapping("/chapter/{chapterId}")
    public ResponseEntity<?> updateTrainingChapter(
            @PathVariable Long chapterId,
            @RequestBody TrainingChapterDTO chapterDTO) {
        try {
            TrainingChapterDTO updatedChapter = trainingMaterialService.updateChapter(chapterId, chapterDTO);
            return ResponseEntity.ok(updatedChapter);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "更新培训章节失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
