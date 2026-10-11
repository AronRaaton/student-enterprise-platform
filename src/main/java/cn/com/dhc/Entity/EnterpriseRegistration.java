//package cn.com.dhc.Entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "enterprise_registration")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class EnterpriseRegistration {
//
//    public enum RegistrationStatus {
//        PENDING,    // 待审核
//        APPROVED,   // 已批准
//        REJECTED    // 已拒绝
//    }
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String email;
//    private String password;
//    private String name;
//    private String legalRepresentative;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    // 营业执照文件路径
//    private String businessLicensePath;
//
//    // 其他证明材料文件路径，以逗号分隔
//    @Column(columnDefinition = "TEXT")
//    private String additionalDocumentsPaths;
//
//    @Enumerated(EnumType.STRING)
//    private RegistrationStatus status = RegistrationStatus.PENDING;
//
//    private LocalDateTime createTime = LocalDateTime.now();
//    private LocalDateTime reviewTime;
//
//    // 审核意见
//    @Column(columnDefinition = "TEXT")
//    private String reviewComment;
//
//    // 审核人ID
//    private Long reviewerId;
//}
