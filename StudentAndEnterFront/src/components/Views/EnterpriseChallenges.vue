<template>
  <div class="enterprise-challenges-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">技术挑战管理</h1>
      <button class="publish-button" @click="goToPublish">
        <span class="publish-icon">+</span> 发布新挑战
      </button>
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
        <h3>暂无技术挑战</h3>
        <p>您还没有发布任何技术挑战，点击下方按钮发布第一个挑战</p>
        <button class="action-btn" @click="goToPublish">发布挑战</button>
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
              <span class="info-label">难度:</span>
              <span class="info-value">
                <span v-for="star in 5" :key="star" class="difficulty-star" :class="{ active: star <= challenge.difficulty }">★</span>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ formatDate(challenge.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">截止时间:</span>
              <span class="info-value">{{ challenge.deadline ? formatDate(challenge.deadline) : '无截止日期' }}</span>
            </div>
          </div>
          
          <div class="challenge-description">
            <p>{{ truncateText(challenge.description, 200) }}</p>
          </div>
          
          <div class="challenge-stats">
            <div class="stat-item">
              <span class="stat-value">{{ challenge.submissionCount || 0 }}</span>
              <span class="stat-label">提交数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ challenge.approvedCount || 0 }}</span>
              <span class="stat-label">已通过</span>
            </div>
          </div>
          
          <div class="challenge-actions">
            <button class="action-btn" @click="viewChallengeDetails(challenge.challengeId)">查看详情</button>
            <button class="action-btn" @click="viewSubmissions(challenge.challengeId)">
              查看提交 
              <span v-if="getPendingCount(challenge)" class="badge">{{ getPendingCount(challenge) }}</span>
            </button>
            <button 
              class="action-btn" 
              :class="{ 'danger': challenge.status === 'OPEN', 'success': challenge.status !== 'OPEN' }"
              @click="toggleChallengeStatus(challenge)"
            >
              {{ challenge.status === 'OPEN' ? '关闭挑战' : '开放挑战' }}
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
      pendingSubmissions: {}
    };
  },
  created() {
    this.fetchChallenges();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'enterprise-home' });
    },
    goToPublish() {
      this.$router.push({ name: 'TechnicalChallengePublish' });
    },
    async fetchChallenges() {
      this.loading = true;
      const enterpriseId = localStorage.getItem('userId');
      
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/enterprise/${enterpriseId}`);
        this.challenges = response.data;
        
        // 获取每个挑战的待审核提交数量
        for (const challenge of this.challenges) {
          this.fetchPendingSubmissions(challenge.challengeId);
        }
      } catch (error) {
        console.error('获取挑战列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchPendingSubmissions(challengeId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/${challengeId}/submissions/pending`);
        this.$set(this.pendingSubmissions, challengeId, response.data.length);
      } catch (error) {
        console.error(`获取挑战 ${challengeId} 的待审核提交失败:`, error);
        this.$set(this.pendingSubmissions, challengeId, 0);
      }
    },
    getPendingCount(challenge) {
      return this.pendingSubmissions[challenge.challengeId] || 0;
    },
    viewChallengeDetails(challengeId) {
      this.$router.push({ name: 'TechnicalChallengeDetail', params: { challengeId } });
    },
    viewSubmissions(challengeId) {
      this.$router.push({ 
        name: 'TechnicalChallengeDetail', 
        params: { challengeId },
        query: { tab: 'submissions' }
      });
    },
    async toggleChallengeStatus(challenge) {
      const newStatus = challenge.status === 'OPEN' ? 'CLOSED' : 'OPEN';
      
      try {
        await axios.put(`http://localhost:8080/api/challenges/${challenge.challengeId}/status`, null, {
          params: { status: newStatus }
        });
        
        // 更新本地挑战状态
        const index = this.challenges.findIndex(c => c.challengeId === challenge.challengeId);
        if (index !== -1) {
          this.challenges[index].status = newStatus;
        }
      } catch (error) {
        console.error('更新挑战状态失败:', error);
        alert('更新挑战状态失败，请稍后重试');
      }
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
.enterprise-challenges-container {
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

.publish-button {
  display: flex;
  align-items: center;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.publish-button:hover {
  background-color: #66b1ff;
}

.publish-icon {
  margin-right: 5px;
  font-size: 18px;
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

.challenge-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
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
  position: relative;
}

.action-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.action-btn.danger {
  background-color: #fff0f0;
  color: #f56c6c;
  border-color: #fde2e2;
}

.action-btn.danger:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.action-btn.success {
  background-color: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.action-btn.success:hover {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
}
</style>
