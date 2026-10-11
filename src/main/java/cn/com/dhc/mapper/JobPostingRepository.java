package cn.com.dhc.mapper;

import cn.com.dhc.Entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 根据企业ID查找所有岗位
    List<JobPosting> findByEnterprise_EnterpriseId(Long enterpriseId);

    // 根据企业ID和状态查找岗位
    List<JobPosting> findByEnterprise_EnterpriseIdAndStatus(Long enterpriseId, String status);

    // 根据岗位类型查找岗位
    List<JobPosting> findByJobTypeAndStatus(String jobType, String status);

    // 根据城市查找岗位
    List<JobPosting> findByCityAndStatus(String city, String status);

    // 根据城市和岗位类型查找岗位
    List<JobPosting> findByCityAndJobTypeAndStatus(String city, String jobType, String status);

    // 分页查询所有开放的岗位
    Page<JobPosting> findByStatus(String status, Pageable pageable);

    // 搜索岗位（标题、描述、要求中包含关键词）
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.requirements) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobPosting> searchJobs(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);

    // 根据薪资范围筛选岗位
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND " +
           "(j.minSalary >= :minSalary OR j.minSalary IS NULL) AND " +
           "(j.maxSalary <= :maxSalary OR j.maxSalary IS NULL)")
    List<JobPosting> findBySalaryRangeAndStatus(
        @Param("minSalary") Integer minSalary,
        @Param("maxSalary") Integer maxSalary,
        @Param("status") String status);

    // 根据城市和薪资范围筛选岗位
    @Query("SELECT j FROM JobPosting j WHERE j.status = :status AND j.city = :city AND " +
           "(j.minSalary >= :minSalary OR j.minSalary IS NULL) AND " +
           "(j.maxSalary <= :maxSalary OR j.maxSalary IS NULL)")
    List<JobPosting> findByCityAndSalaryRangeAndStatus(
        @Param("city") String city,
        @Param("minSalary") Integer minSalary,
        @Param("maxSalary") Integer maxSalary,
        @Param("status") String status);

    // 根据企业规模筛选岗位
    @Query("SELECT j FROM JobPosting j JOIN j.enterprise e WHERE j.status = :status AND " +
           "e.employeeCount >= :minEmployeeCount AND e.employeeCount <= :maxEmployeeCount")
    List<JobPosting> findByCompanySizeAndStatus(
        @Param("minEmployeeCount") Integer minEmployeeCount,
        @Param("maxEmployeeCount") Integer maxEmployeeCount,
        @Param("status") String status);

    // 高级筛选：根据城市、薪资范围、企业规模和岗位类型筛选岗位
    @Query("SELECT j FROM JobPosting j JOIN j.enterprise e WHERE j.status = :status AND " +
           "(:city IS NULL OR j.city = :city) AND " +
           "(:minSalary IS NULL OR j.minSalary >= :minSalary OR j.minSalary IS NULL) AND " +
           "(:maxSalary IS NULL OR j.maxSalary <= :maxSalary OR j.maxSalary IS NULL) AND " +
           "(:minEmployeeCount IS NULL OR e.employeeCount >= :minEmployeeCount) AND " +
           "(:maxEmployeeCount IS NULL OR e.employeeCount <= :maxEmployeeCount) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType)")
    List<JobPosting> advancedFilterJobs(
        @Param("city") String city,
        @Param("minSalary") Integer minSalary,
        @Param("maxSalary") Integer maxSalary,
        @Param("minEmployeeCount") Integer minEmployeeCount,
        @Param("maxEmployeeCount") Integer maxEmployeeCount,
        @Param("jobType") String jobType,
        @Param("status") String status);

    // 根据城市和岗位类型查找岗位
}
