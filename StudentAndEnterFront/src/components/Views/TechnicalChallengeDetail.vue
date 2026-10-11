<template>
  <div class="challenge-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">技术挑战详情</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 挑战详情 -->
    <div v-else class="challenge-content">
      <div class="challenge-header">
        <h2 class="challenge-title">{{ challenge.title }}</h2>
        <span class="challenge-status" :class="getStatusClass(challenge.status)">
          {{ getStatusText(challenge.status) }}
        </span>
      </div>

      <div class="challenge-meta">
        <div class="meta-item">
          <span class="meta-label">企业:</span>
          <span class="meta-value">{{ challenge.enterpriseName }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">难度:</span>
          <span class="meta-value">
            <span v-for="star in 5" :key="star" class="difficulty-star" :class="{ active: star <= challenge.difficulty }">★</span>
          </span>
        </div>
        <div class="meta-item">
          <span class="meta-label">发布时间:</span>
          <span class="meta-value">{{ formatDate(challenge.createTime) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">截止时间:</span>
          <span class="meta-value">{{ challenge.deadline ? formatDate(challenge.deadline) : '无截止日期' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">提交数:</span>
          <span class="meta-value">{{ challenge.submissionCount || 0 }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">已通过:</span>
          <span class="meta-value">{{ challenge.approvedCount || 0 }}</span>
        </div>
      </div>

      <div class="challenge-section">
        <h3 class="section-title">挑战描述</h3>
        <div class="section-content">
          <p>{{ challenge.description }}</p>
        </div>
      </div>

      <div class="challenge-section">
        <h3 class="section-title">所需技能</h3>
        <div class="section-content">
          <p>{{ challenge.requiredSkills }}</p>
        </div>
      </div>

      <div class="challenge-section">
        <h3 class="section-title">奖励</h3>
        <div class="section-content">
          <p>{{ challenge.rewardDescription }}</p>
        </div>
      </div>

      <!-- 学生视图 - 提交按钮 -->
      <div v-if="isStudent && challenge.status === 'OPEN'" class="challenge-actions">
        <button 
          class="action-btn submit-btn" 
          @click="submitSolution"
          :disabled="hasSubmitted"
        >
          {{ hasSubmitted ? '已提交' : '提交解决方案' }}
        </button>
      </div>

      <!-- 企业视图 - 提交列表 -->
      <div v-if="isEnterprise && activeTab === 'submissions'" class="challenge-submissions">
        <h3 class="section-title">提交列表</h3>
        
        <div v-if="loadingSubmissions" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载提交列表中...</p>
        </div>
        
        <div v-else-if="submissions.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p>暂无学生提交</p>
        </div>
        
        <div v-else class="submissions-list">
          <div v-for="submission in submissions" :key="submission.submissionId" class="submission-card">
            <div class="submission-header">
              <div class="student-info">
                <div class="student-avatar">{{ submission.studentName ? submission.studentName.charAt(0) : 'S' }}</div>
                <div class="student-details">
                  <h4 class="student-name">{{ submission.studentName }}</h4>
                  <p class="student-school">{{ submission.studentSchool }}</p>
                </div>
              </div>
              <span class="submission-status" :class="getSubmissionStatusClass(submission.status)">
                {{ getSubmissionStatusText(submission.status) }}
              </span>
            </div>
            
            <div class="submission-content">
              <h4>解决方案:</h4>
              <p>{{ submission.solutionDescription }}</p>
              
              <div v-if="submission.solutionUrl || submission.uploadedFile" class="submission-links">
                <a v-if="submission.solutionUrl" :href="submission.solutionUrl" target="_blank" class="link-btn">查看解决方案链接</a>
                <a v-if="submission.uploadedFile" :href="`http://localhost:8080/api/files/download/${submission.uploadedFile}`" class="link-btn">下载提交文件</a>
              </div>
            </div>
            
            <div class="submission-meta">
              <span class="submission-date">提交时间: {{ formatDate(submission.submitTime) }}</span>
            </div>
            
            <div v-if="submission.status === 'PENDING'" class="submission-actions">
              <div class="rating-selector">
                <span class="rating-label">评分:</span>
                <div class="rating-stars">
                  <span 
                    v-for="star in 5" 
                    :key="star" 
                    class="rating-star" 
                    :class="{ active: star <= selectedRating[submission.submissionId] }"
                    @click="setRating(submission.submissionId, star)"
                  >★</span>
                </div>
              </div>
              
              <div class="feedback-input">
                <label for="feedback">反馈:</label>
                <textarea 
                  id="feedback" 
                  v-model="feedbackText[submission.submissionId]" 
                  placeholder="请输入对学生的反馈"
                  rows="3"
                ></textarea>
              </div>
              
              <div class="review-actions">
                <button class="action-btn approve-btn" @click="approveSubmission(submission.submissionId)">通过</button>
                <button class="action-btn reject-btn" @click="rejectSubmission(submission.submissionId)">拒绝</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      challenge: {},
      loading: true,
      loadingSubmissions: false,
      submissions: [],
      userRole: localStorage.getItem('userRole') || '',
      userId: localStorage.getItem('userId'),
      hasSubmitted: false,
      activeTab: this.$route.query.tab || 'details',
      selectedRating: {},
      feedbackText: {}
    };
  },
  computed: {
    isStudent() {
      return this.userRole === 'student';
    },
    isEnterprise() {
      return this.userRole === 'enterprise';
    },
    challengeId() {
      return this.$route.params.challengeId;
    }
  },
  created() {
    this.fetchChallengeDetails();
    
    if (this.isStudent) {
      this.checkIfSubmitted();
    } else if (this.isEnterprise && this.activeTab === 'submissions') {
      this.fetchSubmissions();
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchChallengeDetails() {
      this.loading = true;
      
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/${this.challengeId}`);
        this.challenge = response.data;
      } catch (error) {
        console.error('获取挑战详情失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async checkIfSubmitted() {
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/submissions/student/${this.userId}`);
        this.hasSubmitted = response.data.some(sub => sub.challengeId === parseInt(this.challengeId));
      } catch (error) {
        console.error('检查提交状态失败:', error);
      }
    },
    async fetchSubmissions() {
      this.loadingSubmissions = true;
      
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/${this.challengeId}/submissions`);
        this.submissions = response.data;
        
        // 初始化评分和反馈
        this.submissions.forEach(submission => {
          this.selectedRating[submission.submissionId] = 3;
          this.feedbackText[submission.submissionId] = '';
        });
      } catch (error) {
        console.error('获取提交列表失败:', error);
      } finally {
        this.loadingSubmissions = false;
      }
    },
    submitSolution() {
      if (this.hasSubmitted) {
        return;
      }
      
      this.$router.push({ 
        name: 'TechnicalChallengeSubmission', 
        params: { challengeId: this.challengeId }
      });
    },
    setRating(submissionId, rating) {
      this.selectedRating[submissionId] = rating;
    },
    async approveSubmission(submissionId) {
      const rating = this.selectedRating[submissionId];
      const feedback = this.feedbackText[submissionId] || '恭喜你通过了技术挑战！';
      
      try {
        await axios.put(`http://localhost:8080/api/challenges/submissions/${submissionId}/review`, {
          feedback
        }, {
          params: { 
            status: 'APPROVED',
            rating
          }
        });
        
        // 更新提交列表
        this.fetchSubmissions();
        // 更新挑战详情
        this.fetchChallengeDetails();
      } catch (error) {
        console.error('通过提交失败:', error);
        alert('通过提交失败，请重试');
      }
    },
    async rejectSubmission(submissionId) {
      const feedback = this.feedbackText[submissionId];
      if (!feedback) {
        alert('请输入拒绝理由');
        return;
      }
      
      try {
        await axios.put(`http://localhost:8080/api/challenges/submissions/${submissionId}/review`, {
          feedback
        }, {
          params: { 
            status: 'REJECTED',
            rating: 0
          }
        });
        
        // 更新提交列表
        this.fetchSubmissions();
      } catch (error) {
        console.error('拒绝提交失败:', error);
        alert('拒绝提交失败，请重试');
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    getStatusClass(status) {
      switch (status) {
        case 'OPEN':
          return 'status-open';
        case 'CLOSED':
          return 'status-closed';
        case 'COMPLETED':
          return 'status-completed';
        default:
          return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'OPEN':
          return '开放中';
        case 'CLOSED':
          return '已关闭';
        case 'COMPLETED':
          return '已完成';
        default:
          return '未知状态';
      }
    },
    getSubmissionStatusClass(status) {
      switch (status) {
        case 'PENDING':
          return 'status-pending';
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        default:
          return '';
      }
    },
    getSubmissionStatusText(status) {
      switch (status) {
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已通过';
        case 'REJECTED':
          return '已拒绝';
        default:
          return '未知状态';
      }
    }
  }
};
</script>

<style scoped>
.challenge-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 15px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #ecf5ff;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  flex-grow: 1;
}

/* 加载中状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 挑战内容 */
.challenge-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.challenge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.challenge-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.challenge-status {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.status-open {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-completed {
  background-color: #ecf5ff;
  color: #409EFF;
}

.challenge-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.meta-value {
  color: #303133;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.difficulty-star {
  font-size: 16px;
  color: #dcdfe6;
  margin-right: 2px;
}

.difficulty-star.active {
  color: #f7ba2a;
}

.challenge-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.section-content {
  color: #606266;
  line-height: 1.6;
}

.challenge-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
}

.submit-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 提交列表 */
.challenge-submissions {
  margin-top: 30px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #909399;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.submission-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
}

.student-details {
  display: flex;
  flex-direction: column;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.student-school {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.submission-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-approved {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.submission-content {
  margin-bottom: 15px;
}

.submission-content h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.submission-content p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

.submission-links {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.link-btn {
  padding: 6px 12px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.link-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.submission-meta {
  margin-bottom: 15px;
  font-size: 14px;
  color: #909399;
}

.submission-actions {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-top: 15px;
  border-top: 1px solid #eaeaea;
}

.rating-selector {
  display: flex;
  align-items: center;
}

.rating-label {
  margin-right: 10px;
  color: #606266;
}

.rating-stars {
  display: flex;
}

.rating-star {
  font-size: 20px;
  color: #dcdfe6;
  cursor: pointer;
  margin-right: 5px;
  transition: color 0.3s;
}

.rating-star:hover,
.rating-star.active {
  color: #f7ba2a;
}

.feedback-input {
  display: flex;
  flex-direction: column;
}

.feedback-input label {
  margin-bottom: 5px;
  color: #606266;
}

.feedback-input textarea {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.approve-btn {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.approve-btn:hover {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.reject-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.reject-btn:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}
</style>
