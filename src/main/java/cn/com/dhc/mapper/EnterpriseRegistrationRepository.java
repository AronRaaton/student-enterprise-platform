//package cn.com.dhc.mapper;
//
//import cn.com.dhc.Entity.EnterpriseRegistration;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface EnterpriseRegistrationRepository extends JpaRepository<EnterpriseRegistration, Long> {
//
//    List<EnterpriseRegistration> findByStatus(EnterpriseRegistration.RegistrationStatus status);
//
//    boolean existsByEmail(String email);
//
//    EnterpriseRegistration findByEmail(String email);
//}
