package cn.com.dhc.service;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.TrainingChapter;
import cn.com.dhc.Entity.TrainingFile;
import cn.com.dhc.Entity.TrainingMaterial;
import cn.com.dhc.dto.TrainingChapterDTO;
import cn.com.dhc.dto.TrainingFileDTO;
import cn.com.dhc.dto.TrainingMaterialDTO;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.repository.TrainingChapterRepository;
import cn.com.dhc.repository.TrainingFileRepository;
import cn.com.dhc.repository.TrainingMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingMaterialService {

    private final TrainingMaterialRepository trainingMaterialRepository;
    private final TrainingChapterRepository trainingChapterRepository;
    private final TrainingFileRepository trainingFileRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final FileStorageService fileStorageService;

    /**
     * 创建培训资料
     */
    @Transactional
    public TrainingMaterialDTO createTrainingMaterial(TrainingMaterialDTO materialDTO) {
        TrainingMaterial material = new TrainingMaterial();
        material.setEnterpriseId(materialDTO.getEnterpriseId());
        material.setTitle(materialDTO.getTitle());
        material.setDescription(materialDTO.getDescription()); // 描述可以为空
        material.setIndustryType(materialDTO.getIndustryType());
        material.setStatus("ACTIVE");

        TrainingMaterial savedMaterial = trainingMaterialRepository.save(material);

        return convertToDTO(savedMaterial);
    }

    /**
     * 更新培训资料
     */
    @Transactional
    public TrainingMaterialDTO updateTrainingMaterial(Long materialId, TrainingMaterialDTO materialDTO) {
        Optional<TrainingMaterial> materialOpt = trainingMaterialRepository.findById(materialId);
        if (materialOpt.isPresent()) {
            TrainingMaterial material = materialOpt.get();
            material.setTitle(materialDTO.getTitle());
            material.setDescription(materialDTO.getDescription());
            material.setIndustryType(materialDTO.getIndustryType());
            material.setUpdateTime(LocalDateTime.now());

            TrainingMaterial updatedMaterial = trainingMaterialRepository.save(material);
            return convertToDTO(updatedMaterial);
        } else {
            throw new RuntimeException("培训资料不存在");
        }
    }

    /**
     * 添加培训章节
     */
    @Transactional
    public TrainingChapterDTO addChapter(TrainingChapterDTO chapterDTO) {
        TrainingChapter chapter = new TrainingChapter();
        chapter.setMaterialId(chapterDTO.getMaterialId());
        chapter.setTitle(chapterDTO.getTitle());
        chapter.setDescription(chapterDTO.getDescription()); // 描述可以为空
        chapter.setContent(chapterDTO.getContent()); // 内容可以为空
        chapter.setSequence(chapterDTO.getSequence());

        TrainingChapter savedChapter = trainingChapterRepository.save(chapter);

        return convertToChapterDTO(savedChapter);
    }

    /**
     * 更新培训章节
     */
    @Transactional
    public TrainingChapterDTO updateChapter(Long chapterId, TrainingChapterDTO chapterDTO) {
        Optional<TrainingChapter> chapterOpt = trainingChapterRepository.findById(chapterId);
        if (chapterOpt.isPresent()) {
            TrainingChapter chapter = chapterOpt.get();
            chapter.setTitle(chapterDTO.getTitle());
            chapter.setDescription(chapterDTO.getDescription());
            chapter.setContent(chapterDTO.getContent());

            TrainingChapter updatedChapter = trainingChapterRepository.save(chapter);
            return convertToChapterDTO(updatedChapter);
        } else {
            throw new RuntimeException("培训章节不存在");
        }
    }

    /**
     * 上传培训文件
     */
    @Transactional
    public TrainingFileDTO uploadFile(Long chapterId, MultipartFile file) {
        String filePath = fileStorageService.storeFile(file);

        TrainingFile trainingFile = new TrainingFile();
        trainingFile.setChapterId(chapterId);
        trainingFile.setFileName(file.getOriginalFilename());
        trainingFile.setFilePath(filePath);
        trainingFile.setFileType(file.getContentType());

        TrainingFile savedFile = trainingFileRepository.save(trainingFile);

        return convertToFileDTO(savedFile);
    }

    /**
     * 获取培训资料详情
     */
    public TrainingMaterialDTO getTrainingMaterialById(Long materialId) {
        Optional<TrainingMaterial> materialOpt = trainingMaterialRepository.findById(materialId);
        if (materialOpt.isPresent()) {
            TrainingMaterial material = materialOpt.get();
            TrainingMaterialDTO dto = convertToDTO(material);

            // 获取章节
            List<TrainingChapter> chapters = trainingChapterRepository.findByMaterialIdOrderBySequenceAsc(materialId);
            List<TrainingChapterDTO> chapterDTOs = new ArrayList<>();

            for (TrainingChapter chapter : chapters) {
                TrainingChapterDTO chapterDTO = convertToChapterDTO(chapter);

                // 获取文件
                List<TrainingFile> files = trainingFileRepository.findByChapterId(chapter.getChapterId());
                List<TrainingFileDTO> fileDTOs = files.stream()
                        .map(this::convertToFileDTO)
                        .collect(Collectors.toList());

                chapterDTO.setFiles(fileDTOs);
                chapterDTOs.add(chapterDTO);
            }

            dto.setChapters(chapterDTOs);
            return dto;
        }
        return null;
    }

    /**
     * 获取企业的所有培训资料
     */
    public List<TrainingMaterialDTO> getEnterpriseTrainingMaterials(Long enterpriseId) {
        List<TrainingMaterial> materials = trainingMaterialRepository.findByEnterpriseIdAndStatus(enterpriseId, "ACTIVE");
        return materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取特定行业类型的培训资料
     */
    public List<TrainingMaterialDTO> getTrainingMaterialsByIndustryType(String industryType) {
        List<TrainingMaterial> materials = trainingMaterialRepository.findByIndustryTypeAndStatus(industryType, "ACTIVE");
        return materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有行业类型
     */
    public List<String> getAllIndustryTypes() {
        return trainingMaterialRepository.findAllIndustryTypes();
    }

    /**
     * 搜索培训资料
     */
    public List<TrainingMaterialDTO> searchTrainingMaterials(String keyword) {
        List<TrainingMaterial> materials = trainingMaterialRepository.searchByKeyword(keyword);
        return materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有培训资料
     */
    public List<TrainingMaterialDTO> getAllTrainingMaterials() {
        List<TrainingMaterial> materials = trainingMaterialRepository.findByStatus("ACTIVE");
        return materials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 删除培训资料
     */
    @Transactional
    public void deleteTrainingMaterial(Long materialId) {
        Optional<TrainingMaterial> materialOpt = trainingMaterialRepository.findById(materialId);
        if (materialOpt.isPresent()) {
            TrainingMaterial material = materialOpt.get();
            material.setStatus("DELETED"); // 软删除，只改变状态
            trainingMaterialRepository.save(material);
        } else {
            throw new RuntimeException("培训资料不存在");
        }
    }

    /**
     * 删除培训章节
     */
    @Transactional
    public void deleteTrainingChapter(Long chapterId) {
        Optional<TrainingChapter> chapterOpt = trainingChapterRepository.findById(chapterId);
        if (chapterOpt.isPresent()) {
            // 先删除章节相关的所有文件
            List<TrainingFile> files = trainingFileRepository.findByChapterId(chapterId);
            trainingFileRepository.deleteAll(files);

            // 再删除章节
            trainingChapterRepository.deleteById(chapterId);
        } else {
            throw new RuntimeException("培训章节不存在");
        }
    }

    /**
     * 删除培训文件
     */
    @Transactional
    public void deleteTrainingFile(Long fileId) {
        trainingFileRepository.deleteById(fileId);
    }

    /**
     * 将实体转换为DTO
     */
    private TrainingMaterialDTO convertToDTO(TrainingMaterial material) {
        TrainingMaterialDTO dto = new TrainingMaterialDTO();
        dto.setMaterialId(material.getMaterialId());
        dto.setEnterpriseId(material.getEnterpriseId());
        dto.setTitle(material.getTitle());
        dto.setDescription(material.getDescription());
        dto.setIndustryType(material.getIndustryType());
        dto.setStatus(material.getStatus());
        dto.setCreateTime(material.getCreateTime());
        dto.setUpdateTime(material.getUpdateTime());

        // 获取企业名称
        Optional<Enterprise> enterpriseOpt = enterpriseRepository.findById(material.getEnterpriseId());
        enterpriseOpt.ifPresent(enterprise -> dto.setEnterpriseName(enterprise.getName()));

        return dto;
    }

    /**
     * 将章节实体转换为DTO
     */
    private TrainingChapterDTO convertToChapterDTO(TrainingChapter chapter) {
        TrainingChapterDTO dto = new TrainingChapterDTO();
        dto.setChapterId(chapter.getChapterId());
        dto.setMaterialId(chapter.getMaterialId());
        dto.setTitle(chapter.getTitle());
        dto.setDescription(chapter.getDescription());
        dto.setContent(chapter.getContent());
        dto.setSequence(chapter.getSequence());
        return dto;
    }

    /**
     * 将文件实体转换为DTO
     */
    private TrainingFileDTO convertToFileDTO(TrainingFile file) {
        TrainingFileDTO dto = new TrainingFileDTO();
        dto.setFileId(file.getFileId());
        dto.setChapterId(file.getChapterId());
        dto.setFileName(file.getFileName());
        dto.setFilePath(file.getFilePath());
        dto.setFileType(file.getFileType());
        dto.setUploadTime(file.getUploadTime());
        return dto;
    }
}
