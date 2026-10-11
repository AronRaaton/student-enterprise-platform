package cn.com.dhc.service;

import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path fileStoragePath;

    @PostConstruct
    public void init() {
        try {
            // 使用绝对路径创建上传目录
            this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
            System.out.println("文件上传目录路径: " + this.fileStoragePath);

            // 创建目录（如果不存在）
            if (!Files.exists(this.fileStoragePath)) {
                Files.createDirectories(this.fileStoragePath);
                System.out.println("创建了新的上传目录: " + this.fileStoragePath);
            } else {
                System.out.println("上传目录已存在: " + this.fileStoragePath);
            }

            // 检查目录权限
            if (!Files.isWritable(this.fileStoragePath)) {
                System.out.println("警告: 上传目录不可写: " + this.fileStoragePath);
            }

            // 列出目录中的文件（用于调试）
            System.out.println("当前上传目录中的文件:");
            try (var files = Files.list(this.fileStoragePath)) {
                files.forEach(path -> System.out.println(" - " + path.getFileName()));
            }
        } catch (IOException e) {
            System.err.println("初始化文件上传目录失败: " + e.getMessage());
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null) {
            System.err.println("文件对象为null");
            throw new RuntimeException("文件对象为null");
        }

        if (file.isEmpty()) {
            System.err.println("文件内容为空，大小为0字节");
            throw new RuntimeException("文件内容为空，大小为0字节");
        }

        // 检查文件大小
        if (file.getSize() <= 0) {
            System.err.println("文件大小异常: " + file.getSize() + " 字节");
            throw new RuntimeException("文件大小异常: " + file.getSize() + " 字节");
        }

        // 规范化文件名
        String originalFileName = "unknown";
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
            originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        }

        System.out.println("开始存储文件: " + originalFileName + ", 大小: " + file.getSize() + " 字节");

        // 检查文件名是否包含无效字符
        if (originalFileName.contains("..")) {
            System.err.println("文件名包含无效路径序列: " + originalFileName);
            throw new RuntimeException("文件名包含无效路径序列 " + originalFileName);
        }

        // 生成唯一文件名，避免覆盖
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        System.out.println("生成唯一文件名: " + uniqueFileName);

        try {
            // 确保上传目录存在
            if (!Files.exists(this.fileStoragePath)) {
                Files.createDirectories(this.fileStoragePath);
                System.out.println("创建上传目录: " + this.fileStoragePath);
            }

            // 检查目录权限
            if (!Files.isWritable(this.fileStoragePath)) {
                System.err.println("上传目录不可写: " + this.fileStoragePath);
                throw new RuntimeException("上传目录不可写: " + this.fileStoragePath);
            }

            // 复制文件到目标位置
            Path targetLocation = this.fileStoragePath.resolve(uniqueFileName);
            System.out.println("文件存储路径: " + targetLocation.toAbsolutePath());

            try {
                // 获取输入流
                if (file.getInputStream() == null) {
                    System.err.println("无法获取文件输入流");
                    throw new RuntimeException("无法获取文件输入流");
                }

                // 复制文件
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("文件复制完成");
            } catch (IOException e) {
                System.err.println("复制文件失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("复制文件失败: " + e.getMessage(), e);
            }

            // 验证文件是否成功保存
            if (Files.exists(targetLocation)) {
                long savedSize = Files.size(targetLocation);
                System.out.println("文件成功保存: " + uniqueFileName + ", 大小: " + savedSize + " 字节");

                // 检查文件大小是否与原始文件一致
                if (savedSize != file.getSize()) {
                    System.out.println("警告: 保存的文件大小 " + savedSize + " 与原始文件大小 " + file.getSize() + " 不一致");
                }
            } else {
                System.err.println("文件似乎没有成功保存: " + targetLocation);
                throw new RuntimeException("文件似乎没有成功保存: " + targetLocation);
            }

            return uniqueFileName;
        } catch (IOException e) {
            System.err.println("存储文件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("无法存储文件 " + originalFileName + ": " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("存储文件时发生未预期的错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("存储文件时发生未预期的错误: " + e.getMessage(), e);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            System.out.println("加载文件路径: " + filePath.toAbsolutePath());

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                System.out.println("文件不存在: " + filePath.toAbsolutePath());
                throw new RuntimeException("文件未找到 " + fileName);
            }

            // 检查文件是否可读
            if (!Files.isReadable(filePath)) {
                System.out.println("文件不可读: " + filePath.toAbsolutePath());
                throw new RuntimeException("文件不可读 " + fileName);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                System.out.println("成功加载文件: " + fileName + ", 大小: " + Files.size(filePath) + " 字节");
                return resource;
            } else {
                System.out.println("资源不存在: " + fileName);
                throw new RuntimeException("文件未找到 " + fileName);
            }
        } catch (MalformedURLException e) {
            System.out.println("无效的URL: " + e.getMessage());
            throw new RuntimeException("文件未找到 " + fileName, e);
        } catch (IOException e) {
            System.out.println("读取文件失败: " + e.getMessage());
            throw new RuntimeException("读取文件失败 " + fileName, e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStoragePath.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("无法删除文件 " + fileName, e);
        }
    }

    // 存储学生文件（简历、成绩单、作品集）
    public String storeStudentFile(MultipartFile file, String fileType) {
        if (file == null) {
            System.err.println("文件对象为null");
            throw new RuntimeException("文件对象为null");
        }

        if (file.isEmpty()) {
            System.err.println("文件内容为空");
            throw new RuntimeException("文件内容为空");
        }

        // 检查文件类型（简历和成绩单只能是PDF）
        if (("resume".equals(fileType) || "transcript".equals(fileType)) &&
            (file.getContentType() == null || !file.getContentType().equals("application/pdf"))) {
            System.err.println("简历和成绩单只能上传PDF格式文件");
            throw new RuntimeException("简历和成绩单只能上传PDF格式文件");
        }

        // 规范化文件名
        String originalFileName = "unknown";
        if (file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
            originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        }

        // 检查文件名是否包含无效字符
        if (originalFileName.contains("..")) {
            System.err.println("文件名包含无效路径序列: " + originalFileName);
            throw new RuntimeException("文件名包含无效路径序列 " + originalFileName);
        }

        // 生成唯一文件名，避免覆盖
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        System.out.println("生成唯一文件名: " + uniqueFileName);

        try {
            // 确保上传目录存在
            if (!Files.exists(this.fileStoragePath)) {
                Files.createDirectories(this.fileStoragePath);
                System.out.println("创建上传目录: " + this.fileStoragePath);
            }

            // 复制文件到目标位置
            Path targetLocation = this.fileStoragePath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件存储成功: " + uniqueFileName);

            return uniqueFileName;
        } catch (IOException e) {
            System.err.println("存储文件失败: " + e.getMessage());
            throw new RuntimeException("存储文件失败: " + e.getMessage(), e);
        }
    }
}
