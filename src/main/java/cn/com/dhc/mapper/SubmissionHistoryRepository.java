package cn.com.dhc.mapper;

import cn.com.dhc.Entity.SubmissionHistory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionHistoryRepository extends JpaRepository<SubmissionHistory, Long> {

    // 根据submission.submissionId查询历史记录
    List<SubmissionHistory> findBySubmission_SubmissionId(Long submissionId);

    // 根据submission.submissionId查询历史记录，并排序
    List<SubmissionHistory> findBySubmission_SubmissionId(Long submissionId, Sort sort);

    // 根据submission.submissionId和状态查询历史记录
    List<SubmissionHistory> findBySubmission_SubmissionIdAndStatus(Long submissionId, String status);

    // 根据submission.submissionId和提交类型查询历史记录
    List<SubmissionHistory> findBySubmission_SubmissionIdAndSubmissionType(Long submissionId, String submissionType);

    // 查询最新的一条历史记录
    SubmissionHistory findFirstBySubmission_SubmissionIdOrderByCreatedTimeDesc(Long submissionId);

    // 根据submission.submissionId删除历史记录
    void deleteBySubmission_SubmissionId(Long submissionId);
}
