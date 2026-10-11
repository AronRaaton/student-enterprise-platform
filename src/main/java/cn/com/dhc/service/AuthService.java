package cn.com.dhc.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.EnterpriseVerification;
import cn.com.dhc.Entity.Student;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.EnterpriseVerificationRepository;
import cn.com.dhc.mapper.StudentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final StudentRepository studentRepository;
	private final EnterpriseRepository enterpriseRepository;
	private final EnterpriseVerificationRepository enterpriseVerificationRepository;

	// 可选的邮件发送服务
	private final JavaMailSender mailSender;

	@Value("${file.upload.dir:/tmp/uploads}")
    private String uploadDir;



	/**
	 * 验证学信网验证码
	 * 注意：当前实现不验证验证码的真实性，只要求提供验证码
	 */
	private boolean verifyChsiCode(String verificationCode) {
		// 当前实现不验证验证码的真实性，只要求提供验证码
		return verificationCode != null && !verificationCode.trim().isEmpty();
	}

	/**
	 * 注册学生账号
	 */
	@Transactional
	public Student registerStudent(String email, String password, String name, String verificationCode,
			String school, String studentNumber, String major, Integer enrollmentYear) {
		// 检查邮箱是否已注册
		if (studentRepository.existsByEmail(email)) {
			throw new RuntimeException("邮箱已被注册");
		}

		// 验证学信网验证码
		if (!verifyChsiCode(verificationCode)) {
			throw new RuntimeException("请提供学信网验证码");
		}

		// 创建学生对象
		Student student = new Student();
		student.setEmail(email);
		student.setPassword(password);
		student.setName(name);
		student.setSchool(school);
		student.setStudentNumber(studentNumber);
		student.setMajor(major);
		student.setEnrollmentYear(enrollmentYear);
		student.setRegisterDate(LocalDateTime.now());
		student.setCompletedTasks(0);

		// 保存学生对象
		Student savedStudent = studentRepository.save(student);

		return savedStudent;
	}

	/**
	 * 创建企业账号
	 */
	@Transactional
	public Enterprise registerEnterprise(String email, String password, String name) {
		// 检查邮箱是否已注册
		if (enterpriseRepository.existsByEmail(email)) {
			throw new RuntimeException("邮箱已被注册");
		}

		// 创建企业账号
		Enterprise enterprise = new Enterprise();
		enterprise.setEmail(email);
		enterprise.setPassword(password);
		enterprise.setName(name);
		enterprise.setRegisterDate(LocalDateTime.now());
		enterprise.setRating(0.0);

		// 保存企业账号
		return enterpriseRepository.save(enterprise);
	}

	/**
	 * 创建企业账号并提交验证申请
	 */
	@Transactional
	public EnterpriseVerification registerEnterpriseWithVerification(String email, String password, String name,
			String legalRepresentative, String description, MultipartFile businessLicense) throws IOException {
		try {
			System.out.println("开始企业注册流程，邮箱: " + email + ", 名称: " + name);

			// 检查邮箱是否已注册
			if (enterpriseRepository.existsByEmail(email)) {
				throw new RuntimeException("邮箱已被注册");
			}

			// 创建企业账号
			Enterprise enterprise = new Enterprise();
			enterprise.setEmail(email);
			enterprise.setPassword(password);
			enterprise.setName(name);
			enterprise.setDescription(description);
			enterprise.setRegisterDate(LocalDateTime.now());
			enterprise.setRating(0.0);

			// 初始化其他必要字段，防止非空约束问题
			enterprise.setType("");
			enterprise.setLocation("");
			enterprise.setEmployeeCount(0);
			enterprise.setFoundingYear(LocalDateTime.now().getYear());

			// 保存企业账号
			System.out.println("保存企业账号");
			Enterprise savedEnterprise = enterpriseRepository.save(enterprise);
			System.out.println("企业账号保存成功，ID: " + savedEnterprise.getEnterpriseId());

			// 创建企业验证申请
			EnterpriseVerification verification = new EnterpriseVerification();
			verification.setEnterprise(savedEnterprise);
			verification.setLegalRepresentative(legalRepresentative);
			verification.setVerificationStatus(EnterpriseVerification.VerificationStatus.PENDING);
			verification.setSubmitTime(LocalDateTime.now());

			// 保存营业执照文件
			if (businessLicense != null && !businessLicense.isEmpty()) {
				System.out.println("开始保存营业执照文件");
				String businessLicensePath = saveFile(businessLicense, "business_license");
				verification.setBusinessLicense(businessLicensePath);
				System.out.println("营业执照文件保存成功，路径: " + businessLicensePath);
			}

			// 保存验证申请
			System.out.println("保存企业验证申请");
			EnterpriseVerification savedVerification = enterpriseVerificationRepository.save(verification);
			System.out.println("企业验证申请保存成功，ID: " + savedVerification.getVerificationId());

			return savedVerification;
		} catch (Exception e) {
			System.err.println("企业注册过程中发生错误: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 提交企业验证申请
	 */
	@Transactional
	public EnterpriseVerification submitEnterpriseVerification(Long enterpriseId,
			String legalRepresentative, MultipartFile businessLicense) throws IOException {
		// 获取企业信息
		Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
				.orElseThrow(() -> new RuntimeException("企业不存在"));

		// 创建企业验证申请对象
		EnterpriseVerification verification = new EnterpriseVerification();
		verification.setEnterprise(enterprise);
		verification.setLegalRepresentative(legalRepresentative);
		verification.setVerificationStatus(EnterpriseVerification.VerificationStatus.PENDING);
		verification.setSubmitTime(LocalDateTime.now());

		// 保存营业执照文件
		if (businessLicense != null && !businessLicense.isEmpty()) {
			String businessLicensePath = saveFile(businessLicense, "business_license");
			verification.setBusinessLicense(businessLicensePath);
		}

		// 保存验证申请
		return enterpriseVerificationRepository.save(verification);
	}

	/**
	 * 保存文件并返回文件路径
	 */
	private String saveFile(MultipartFile file, String prefix) throws IOException {
		try {
			System.out.println("开始保存文件，上传目录: " + uploadDir);

			// 创建上传目录
			File uploadDirFile = new File(uploadDir);
			System.out.println("上传目录绝对路径: " + uploadDirFile.getAbsolutePath());

			if (!uploadDirFile.exists()) {
				boolean created = uploadDirFile.mkdirs();
				System.out.println("创建目录结果: " + created);
				if (!created) {
					// 如果无法创建目录，尝试使用系统临时目录
					uploadDirFile = new File(System.getProperty("java.io.tmpdir"));
					System.out.println("使用系统临时目录: " + uploadDirFile.getAbsolutePath());
				}
			}

			// 检查目录是否可写
			if (!uploadDirFile.canWrite()) {
				System.out.println("目录不可写，尝试使用系统临时目录");
				uploadDirFile = new File(System.getProperty("java.io.tmpdir"));
				System.out.println("使用系统临时目录: " + uploadDirFile.getAbsolutePath());
			}

			// 生成唯一文件名
			String originalFilename = file.getOriginalFilename();
			System.out.println("原始文件名: " + originalFilename);

			String extension = "";
			if (originalFilename != null && originalFilename.contains(".")) {
				extension = originalFilename.substring(originalFilename.lastIndexOf("."));
			}
			String filename = prefix + "_" + UUID.randomUUID().toString() + extension;
			System.out.println("生成的文件名: " + filename);

			// 保存文件
			File destFile = new File(uploadDirFile, filename);
			System.out.println("文件路径: " + destFile.getAbsolutePath());

			// 使用Files.copy方法保存文件，如果文件已存在则替换
			Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("文件保存成功");

			// 返回相对路径，以便于后续访问
			return filename;
		} catch (IOException e) {
			System.err.println("保存文件失败: " + e.getMessage());
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			System.err.println("保存文件时发生意外错误: " + e.getMessage());
			e.printStackTrace();
			throw new IOException("保存文件时发生意外错误", e);
		}
	}

	/**
	 * 获取待审核的企业验证申请
	 */
	public List<EnterpriseVerification> getPendingEnterpriseVerifications() {
		return enterpriseVerificationRepository.findByVerificationStatus(EnterpriseVerification.VerificationStatus.PENDING);
	}

	/**
	 * 审核通过企业验证申请
	 */
	@Transactional
	public EnterpriseVerification approveEnterpriseVerification(Long verificationId, Long adminId) {
		EnterpriseVerification verification = enterpriseVerificationRepository.findById(verificationId)
				.orElseThrow(() -> new RuntimeException("验证申请不存在"));

		// 检查状态
		if (verification.getVerificationStatus() != EnterpriseVerification.VerificationStatus.PENDING) {
			throw new RuntimeException("该申请已处理");
		}

		// 更新申请状态
		verification.setVerificationStatus(EnterpriseVerification.VerificationStatus.APPROVED);
		verification.setVerifyTime(LocalDateTime.now());
		verification.setVerifierId(adminId);

		return enterpriseVerificationRepository.save(verification);
	}

	/**
	 * 拒绝企业验证申请并删除企业信息
	 */
	@Transactional
	public EnterpriseVerification rejectEnterpriseVerification(Long verificationId, Long adminId) {
		System.out.println("开始拒绝企业验证申请，ID: " + verificationId);

		EnterpriseVerification verification = enterpriseVerificationRepository.findById(verificationId)
				.orElseThrow(() -> new RuntimeException("验证申请不存在"));

		// 检查状态
		if (verification.getVerificationStatus() != EnterpriseVerification.VerificationStatus.PENDING) {
			throw new RuntimeException("该申请已处理");
		}

		// 获取企业信息
		Enterprise enterprise = verification.getEnterprise();
		if (enterprise == null) {
			throw new RuntimeException("企业信息不存在");
		}

		Long enterpriseId = enterprise.getEnterpriseId();
		System.out.println("将删除企业信息，ID: " + enterpriseId);

		// 更新申请状态
		verification.setVerificationStatus(EnterpriseVerification.VerificationStatus.REJECTED);
		verification.setVerifyTime(LocalDateTime.now());
		verification.setVerifierId(adminId);

		// 保存验证申请状态
		EnterpriseVerification savedVerification = enterpriseVerificationRepository.save(verification);

		// 删除企业信息
		enterpriseRepository.deleteById(enterpriseId);
		System.out.println("企业信息已删除，ID: " + enterpriseId);

		return savedVerification;
	}

	/**
	 * 获取企业的验证状态
	 */
	public EnterpriseVerification getEnterpriseVerificationStatus(Long enterpriseId) {
		return enterpriseVerificationRepository.findTopByEnterpriseEnterpriseIdOrderBySubmitTimeDesc(enterpriseId);
	}
}