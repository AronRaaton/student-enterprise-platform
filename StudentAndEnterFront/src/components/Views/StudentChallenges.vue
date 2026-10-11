<template>
  <div class="student-challenges-container">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">技术挑战</h1>
    </div>

    <!-- 挑战列表 -->
    <div class="challenges-content">
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="challenges.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <h3>暂无可参与的技术挑战</h3>
        <p>目前没有企业发布的开放挑战，请稍后再查看</p>
      </div>

      <!-- 挑战列表 -->
      <div v-else class="challenges-list">
        <div v-for="challenge in challenges" :key="challenge.challengeId" class="challenge-card">
          <div class="challenge-header">
            <h2 class="challenge-title">{{ challenge.title }}</h2>
            <span class="challenge-status" :class="getStatusClass(challenge.status)">
              {{ getStatusText(challenge.status) }}
            </span>
          </div>
          
          <div class="challenge-info">
            <div class="info-item">
              <span class="info-label">企业:</span>
              <span class="info-value">{{ challenge.enterpriseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">难度:</span>
              <span class="info-value">
                <span v-for="star in 5" :key="star" class="difficulty-star" :class="{ active: star <= challenge.difficulty }">★</span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ formatDate(challenge.createTime) }}</span>
            </div>
          </div>
          
          <div class="challenge-description">
            <p>{{ truncateText(challenge.description, 200) }}</p>
          </div>
          
          <div class="challenge-reward">
            <h3>奖励:</h3>
            <p>{{ challenge.rewardDescription }}</p>
          </div>
          
          <div class="challenge-actions">
            <button class="action-btn" @click="viewChallengeDetails(challenge.challengeId)">查看详情</button>
            <button 
              class="action-btn submit-btn" 
              @click="submitSolution(challenge.challengeId)"
              :disabled="hasSubmitted(challenge.challengeId)"
            >
              {{ hasSubmitted(challenge.challengeId) ? '已提交' : '提交解决方案' }}
            </button>
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
      challenges: [],
      loading: true,
      studentId: null,
      submissions: []
    };
  },
  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchChallenges();
    this.fetchStudentSubmissions();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'student-home' });
    },
    async fetchChallenges() {
      this.loading = true;
      
      try {
        const response = await axios.get('http://localhost:8080/api/challenges', {
          params: { page: 0, size: 100 }
        });
        this.challenges = response.data.content || [];
      } catch (error) {
        console.error('获取挑战列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchStudentSubmissions() {
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/submissions/student/${this.studentId}`);
        this.submissions = response.data;
      } catch (error) {
        console.error('获取学生提交记录失败:', error);
      }
    },
    hasSubmitted(challengeId) {
      return this.submissions.some(sub => sub.challengeId === challengeId);
    },
    viewChallengeDetails(challengeId) {
      this.$router.push({ name: 'TechnicalChallengeDetail', params: { challengeId } });
    },
    submitSolution(challengeId) {
      if (this.hasSubmitted(challengeId)) {
        return;
      }
      
      this.$router.push({ 
        name: 'TechnicalChallengeSubmission', 
        params: { challengeId }
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
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
    }
  }
};
</script>

<style scoped>
.student-challenges-container {
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
  color: #909399;
}

.empty-state h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.empty-state p {
  color: #606266;
  margin-bottom: 20px;
  max-width: 400px;
}

/* 挑战列表 */
.challenges-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.challenge-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.challenge-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.challenge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.challenge-title {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.challenge-status {
  padding: 4px 10px;
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

.challenge-info {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  color: #909399;
  margin-right: 5px;
}

.info-value {
  color: #606266;
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

.challenge-description {
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.challenge-reward {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.challenge-reward h3 {
  font-size: 16px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 10px;
}

.challenge-reward p {
  color: #606266;
  margin: 0;
}

.challenge-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
}

.action-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.action-btn:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  border-color: #e4e7ed;
  cursor: not-allowed;
}

.submit-btn {
  background-color: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.submit-btn:hover:not(:disabled) {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}
</style>
