package cn.com.dhc.controller;

import cn.com.dhc.Entity.TaskSubmission;
import cn.com.dhc.mapper.TaskSubmissionRepository;
import cn.com.dhc.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TaskSubmissionRepository taskSubmissionRepository;

    @Autowired
    private cn.com.dhc.mapper.StudentRepository studentRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam("submissionId") Long submissionId,
            HttpServletRequest request) {

        System.out.println("收到文件上传请求，提交ID: " + submissionId);

        // 打印请求头信息，便于调试
        try {
            Enumeration<String> headerNames = request.getHeaderNames();
            System.out.println("请求头信息:");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println(headerName + ": " + request.getHeader(headerName));
            }
        } catch (Exception e) {
            System.out.println("获取请求头失败: " + e.getMessage());
        }

        System.out.println("请求内容类型: " + (file != null ? file.getContentType() : "null"));

        // 检查请求参数
        System.out.println("请求参数: submissionId=" + submissionId);

        // 检查请求类型
        System.out.println("请求类型: " + request.getMethod());
        System.out.println("请求内容类型: " + request.getContentType());

        // 检查文件是否为空
        if (file == null) {
            System.out.println("文件对象为null，提交ID: " + submissionId);
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件对象为null");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.isEmpty()) {
            System.out.println("文件内容为空，提交ID: " + submissionId);
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件内容为空");
            return ResponseEntity.badRequest().body(response);
        }

        System.out.println("文件信息: 名称=" + file.getOriginalFilename() + ", 大小=" + file.getSize() + " 字节");

        try {
            // 查找提交记录
            Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
            if (!submissionOpt.isPresent()) {
                System.out.println("提交记录不存在，提交ID: " + submissionId);
                Map<String, String> response = new HashMap<>();
                response.put("error", "提交记录不存在");
                return ResponseEntity.badRequest().body(response);
            }

            TaskSubmission submission = submissionOpt.get();
            System.out.println("找到提交记录: " + submission.getSubmissionId() + ", 当前上传文件: " + submission.getUploadedFile());

            // 如果之前有上传的文件，先删除
            if (submission.getUploadedFile() != null && !submission.getUploadedFile().isEmpty()) {
                System.out.println("删除之前的文件: " + submission.getUploadedFile());
                try {
                    fileStorageService.deleteFile(submission.getUploadedFile());
                } catch (Exception e) {
                    System.out.println("删除之前的文件失败: " + e.getMessage());
                    // 继续执行，不让删除失败影响上传
                }
            }

            // 保存文件
            String fileName;
            try {
                fileName = fileStorageService.storeFile(file);
                System.out.println("文件存储成功: " + fileName);
            } catch (Exception e) {
                System.err.println("文件存储失败: " + e.getMessage());
                e.printStackTrace();
                Map<String, String> response = new HashMap<>();
                response.put("error", "文件存储失败: " + e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // 更新提交记录，使用uploadedFile字段而不是fileUrl
            submission.setUploadedFile(fileName);
            try {
                taskSubmissionRepository.save(submission);
                System.out.println("提交记录更新成功，提交ID: " + submissionId + ", 文件名: " + fileName);
            } catch (Exception e) {
                System.err.println("更新提交记录失败: " + e.getMessage());
                e.printStackTrace();
                Map<String, String> response = new HashMap<>();
                response.put("error", "更新提交记录失败: " + e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // 返回成功响应
            Map<String, String> response = new HashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("fileDownloadUri", "/api/files/download/" + fileName);
            System.out.println("文件上传成功，响应: " + response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("文件上传失败，未预期的错误: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName,
                                        @RequestParam(value = "preview", required = false, defaultValue = "false") boolean preview,
                                        HttpServletRequest request) {
        System.out.println("收到文件下载请求: " + fileName + ", preview=" + preview);

        try {
            // 加载文件作为Resource
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            // 检查文件是否存在且可访问
            if (!resource.exists()) {
                System.out.println("文件不存在: " + fileName);
                Map<String, String> error = new HashMap<>();
                error.put("error", "文件不存在");
                error.put("fileName", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            if (!resource.isReadable()) {
                System.out.println("文件不可读: " + fileName);
                Map<String, String> error = new HashMap<>();
                error.put("error", "文件不可读");
                error.put("fileName", fileName);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // 尝试确定文件的内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                System.out.println("文件类型: " + contentType + ", 文件名: " + fileName);
            } catch (IOException ex) {
                System.out.println("无法确定文件类型: " + ex.getMessage());
            }

            // 如果无法确定内容类型，则默认为二进制流
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 检查是否是PDF文件并且请求预览
            boolean isPdf = contentType.equals("application/pdf") || fileName.toLowerCase().endsWith(".pdf");

            // 获取文件大小
            long contentLength = 0;
            try {
                contentLength = resource.contentLength();
                System.out.println("文件大小: " + contentLength + " 字节");
            } catch (IOException e) {
                System.out.println("无法获取文件大小: " + e.getMessage());
            }

            // 构建响应
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0");

            if (contentLength > 0) {
                responseBuilder.contentLength(contentLength);
            }

            // 获取原始文件名，去除UUID前缀
            String originalFileName = fileName;
            if (fileName.contains("_")) {
                originalFileName = fileName.substring(fileName.indexOf("_") + 1);
            }

            // 如果是PDF文件且请求预览，则使用inline头，否则使用attachment头
            if (isPdf && preview) {
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + originalFileName + "\"");
                System.out.println("设置为PDF预览模式，文件名: " + originalFileName);
            } else {
                responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"");
                System.out.println("设置为下载模式，文件名: " + originalFileName);
            }

            return responseBuilder.body(resource);
        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("fileName", fileName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<Map<String, String>> getSubmissionFile(@PathVariable Long submissionId) {
        Optional<TaskSubmission> submissionOpt = taskSubmissionRepository.findById(submissionId);
        if (!submissionOpt.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "提交记录不存在");
            return ResponseEntity.badRequest().body(response);
        }

        TaskSubmission submission = submissionOpt.get();
        String fileUrl = submission.getFileUrl(); // 外部链接
        String uploadedFile = submission.getUploadedFile(); // 上传的文件

        // 记录日志，便于调试
        System.out.println("获取提交文件信息: submissionId=" + submissionId
            + ", fileUrl=" + fileUrl + ", uploadedFile=" + uploadedFile);

        Map<String, String> response = new HashMap<>();

        // 首先检查是否有上传的文件
        if (uploadedFile != null && !uploadedFile.isEmpty()) {
            try {
                // 检查文件是否存在
                Path filePath = Paths.get(System.getProperty("user.dir"), "uploads").resolve(uploadedFile).normalize();
                System.out.println("检查文件路径: " + filePath.toAbsolutePath());

                if (Files.exists(filePath)) {
                    System.out.println("文件存在: " + filePath);
                } else {
                    System.out.println("文件不存在: " + filePath);
                }

                // 尝试加载文件，确认它存在
                fileStorageService.loadFileAsResource(uploadedFile);

                // 文件存在，返回下载信息
                String originalFileName = uploadedFile.contains("_") ?
                    uploadedFile.substring(uploadedFile.indexOf("_") + 1) : uploadedFile;

                response.put("fileName", originalFileName);
                response.put("fileDownloadUri", "/api/files/download/" + uploadedFile);
                // 添加文件类型信息
                response.put("fileType", getFileType(originalFileName));
                // 添加原始文件名信息
                response.put("originalFileName", uploadedFile);
                System.out.println("返回上传文件下载信息: " + response);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                System.out.println("加载上传文件失败: " + e.getMessage());
                e.printStackTrace();
                // 如果上传文件加载失败，继续检查外部链接
            }
        }

        // 然后检查是否有外部链接
        if (fileUrl != null && !fileUrl.isEmpty()) {
            // 外部链接
            if (fileUrl.startsWith("http") || fileUrl.startsWith("https")) {
                response.put("fileName", "外部链接");
                response.put("externalUrl", fileUrl);
                System.out.println("返回外部链接信息: " + response);
                return ResponseEntity.ok(response);
            }
            // 其他情况（可能是简单的文件描述）
            else {
                response.put("fileName", fileUrl);
                response.put("fileUrl", fileUrl);
                System.out.println("返回普通文件链接: " + response);
                return ResponseEntity.ok(response);
            }
        }

        // 如果既没有上传文件也没有外部链接
        response.put("error", "该提交没有附件");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 根据文件名获取文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }

        String lowerCaseFileName = fileName.toLowerCase();
        if (lowerCaseFileName.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerCaseFileName.endsWith(".doc") || lowerCaseFileName.endsWith(".docx")) {
            return "word";
        } else if (lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".xlsx")) {
            return "excel";
        } else if (lowerCaseFileName.endsWith(".ppt") || lowerCaseFileName.endsWith(".pptx")) {
            return "powerpoint";
        } else if (lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg") ||
                  lowerCaseFileName.endsWith(".png") || lowerCaseFileName.endsWith(".gif")) {
            return "image";
        } else if (lowerCaseFileName.endsWith(".zip") || lowerCaseFileName.endsWith(".rar") ||
                  lowerCaseFileName.endsWith(".7z")) {
            return "archive";
        } else if (lowerCaseFileName.endsWith(".txt")) {
            return "text";
        } else {
            return "other";
        }
    }

    // 上传学生简历
    @PostMapping("/student/resume")
    public ResponseEntity<?> uploadStudentResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentId") Long studentId) {
        try {
            // 检查学生是否存在
            Optional<cn.com.dhc.Entity.Student> studentOpt = studentRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 存储文件
            String fileName = fileStorageService.storeStudentFile(file, "resume");

            // 更新学生信息
            cn.com.dhc.Entity.Student student = studentOpt.get();
            student.setResumeFile(fileName);
            studentRepository.save(student);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "简历上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "简历上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 上传学生成绩单
    @PostMapping("/student/transcript")
    public ResponseEntity<?> uploadStudentTranscript(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentId") Long studentId) {
        try {
            // 检查学生是否存在
            Optional<cn.com.dhc.Entity.Student> studentOpt = studentRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 存储文件
            String fileName = fileStorageService.storeStudentFile(file, "transcript");

            // 更新学生信息
            cn.com.dhc.Entity.Student student = studentOpt.get();
            student.setTranscriptFile(fileName);
            studentRepository.save(student);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "成绩单上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "成绩单上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 上传学生作品集
    @PostMapping("/student/portfolio")
    public ResponseEntity<?> uploadStudentPortfolio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentId") Long studentId) {
        try {
            // 检查学生是否存在
            Optional<cn.com.dhc.Entity.Student> studentOpt = studentRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // 存储文件
            String fileName = fileStorageService.storeStudentFile(file, "portfolio");

            // 更新学生信息
            cn.com.dhc.Entity.Student student = studentOpt.get();
            String portfolioFiles = student.getPortfolioFiles();
            if (portfolioFiles == null || portfolioFiles.isEmpty()) {
                student.setPortfolioFiles(fileName);
            } else {
                student.setPortfolioFiles(portfolioFiles + "," + fileName);
            }
            studentRepository.save(student);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "作品集上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "作品集上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    // 上传项目申请文件
    @PostMapping("/project-application")
    public ResponseEntity<?> uploadProjectApplicationFile(
            @RequestParam("file") MultipartFile file) {
        try {
            // 存储文件
            String fileName = fileStorageService.storeFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "文件上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 上传项目步骤文件
    @PostMapping("/project-step")
    public ResponseEntity<?> uploadProjectStepFile(
            @RequestParam("file") MultipartFile file) {
        try {
            // 存储文件
            String fileName = fileStorageService.storeFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "文件上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 上传项目计划书文件
    @PostMapping("/project-plan")
    public ResponseEntity<?> uploadProjectPlanFile(
            @RequestParam("file") MultipartFile file) {
        try {
            // 存储文件
            String fileName = fileStorageService.storeFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("message", "文件上传成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 删除文件
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileName,
            @RequestParam("studentId") Long studentId,
            @RequestParam("fileType") String fileType) {
        try {
            // 检查学生是否存在
            Optional<cn.com.dhc.Entity.Student> studentOpt = studentRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            cn.com.dhc.Entity.Student student = studentOpt.get();

            // 根据文件类型处理
            if ("resume".equals(fileType)) {
                // 检查简历文件是否存在且属于该学生
                if (student.getResumeFile() == null || !student.getResumeFile().equals(fileName)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "文件不存在或不属于该学生");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                }

                // 删除文件
                fileStorageService.deleteFile(fileName);

                // 更新学生信息
                student.setResumeFile(null);
                studentRepository.save(student);
            } else if ("transcript".equals(fileType)) {
                // 检查成绩单文件是否存在且属于该学生
                if (student.getTranscriptFile() == null || !student.getTranscriptFile().equals(fileName)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "文件不存在或不属于该学生");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                }

                // 删除文件
                fileStorageService.deleteFile(fileName);

                // 更新学生信息
                student.setTranscriptFile(null);
                studentRepository.save(student);
            } else if ("portfolio".equals(fileType)) {
                // 检查作品集文件是否存在且属于该学生
                String portfolioFiles = student.getPortfolioFiles();
                if (portfolioFiles == null || !portfolioFiles.contains(fileName)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "文件不存在或不属于该学生");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                }

                // 删除文件
                fileStorageService.deleteFile(fileName);

                // 更新学生信息
                List<String> fileList = new ArrayList<>(Arrays.asList(portfolioFiles.split(",")));
                fileList.remove(fileName);
                student.setPortfolioFiles(String.join(",", fileList));
                studentRepository.save(student);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "无效的文件类型");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "文件删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "文件删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 获取学生的所有作品集文件
    @GetMapping("/student/portfolio/{studentId}")
    public ResponseEntity<?> getStudentPortfolioFiles(@PathVariable Long studentId) {
        try {
            // 检查学生是否存在
            Optional<cn.com.dhc.Entity.Student> studentOpt = studentRepository.findById(studentId);
            if (!studentOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "学生不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            cn.com.dhc.Entity.Student student = studentOpt.get();
            String portfolioFiles = student.getPortfolioFiles();

            List<String> fileList = new ArrayList<>();
            if (portfolioFiles != null && !portfolioFiles.isEmpty()) {
                fileList = Arrays.asList(portfolioFiles.split(","));
            }

            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "获取作品集失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
