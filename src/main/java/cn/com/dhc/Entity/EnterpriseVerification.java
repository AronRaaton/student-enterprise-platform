package cn.com.dhc.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseVerification {

    public enum VerificationStatus {
        PENDING,    // 待审核
        APPROVED,   // 已批准
        REJECTED    // 已拒绝
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    @Column(name = "business_license")
    private String businessLicense = "";

    @Column(name = "legal_representative")
    private String legalRepresentative = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "submit_time")
    private LocalDateTime submitTime = LocalDateTime.now();

    @Column(name = "verify_time")
    private LocalDateTime verifyTime;

    @Column(name = "verifier_id")
    private Long verifierId;
}
