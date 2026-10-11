<template>
  <div class="enterprise-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">企业详情</h1>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else class="content-container">
      <!-- 企业基本信息 -->
      <div class="info-card">
        <div class="enterprise-header">
          <div class="enterprise-avatar">{{ enterprise.name ? enterprise.name.charAt(0) : 'E' }}</div>
          <div class="enterprise-name">{{ enterprise.name || '未知企业' }}</div>
        </div>
        <div class="enterprise-info">
          <div class="info-item">
            <span class="info-label">企业类型：</span>
            <span class="info-value">{{ enterprise.type || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">成立时间：</span>
            <span class="info-value">{{ enterprise.foundingYear || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">企业规模：</span>
            <span class="info-value">{{ getEnterpriseSize(enterprise.size) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">所在地区：</span>
            <span class="info-value">{{ enterprise.location || '未知' }}</span>
          </div>
          <div class="info-item" v-if="enterpriseRating > 0">
            <span class="info-label">企业评分：</span>
            <span class="info-value rating-value">
              <star-display :rating="enterpriseRating" :size="'small'" />
            </span>
          </div>
        </div>
        <div class="enterprise-description">
          <h3>企业简介</h3>
          <p>{{ enterprise.description || '暂无企业简介' }}</p>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button class="chat-button" @click="goToChatWithEnterprise">
            <span class="chat-icon">💬</span> 与企业沟通
          </button>
          <button class="rate-button" @click="openRatingDialog">
            <span class="rate-icon">⭐</span> 评分企业
          </button>
        </div>
      </div>

      <!-- 企业发布的任务 -->
      <div class="tasks-section">
        <h2>企业发布的任务</h2>
        <div v-if="tasks.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>该企业暂未发布任务</p>
        </div>
        <div v-else class="tasks-grid">
          <div v-for="task in tasks" :key="task.taskId" class="task-card">
            <div class="task-header">
              <h3 class="task-title">{{ task.summary }}</h3>
              <span class="task-date">{{ formatDate(task.createTime) }}</span>
            </div>
            <div class="task-content">
              <p>{{ truncateText(task.content, 100) }}</p>
            </div>
            <div class="task-footer">
              <span class="task-tech">{{ task.techStack || '技术栈未指定' }}</span>
              <button class="view-btn" @click="viewTaskDetails(task.taskId)">查看详情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 企业发布的公告 -->
      <div class="announcements-section">
        <h2>企业发布的公告</h2>
        <div v-if="announcements.length === 0" class="empty-state">
          <div class="empty-icon">📢</div>
          <p>该企业暂未发布公告</p>
        </div>
        <div v-else class="announcements-list">
          <div v-for="announcement in announcements" :key="announcement.noticeId" class="announcement-card">
            <div class="announcement-header">
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <span class="announcement-date">{{ formatDate(announcement.createdTime) }}</span>
            </div>
            <div class="announcement-content">
              <p>{{ truncateText(announcement.content, 150) }}</p>
            </div>
            <div class="announcement-footer">
              <button class="view-btn" @click="viewAnnouncementDetails(announcement.noticeId)">查看详情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 企业评价列表 -->
      <div class="ratings-section">
        <h2>企业评价</h2>
        <ratings-list
          :target-id="enterpriseId"
          target-type="enterprise"
          title="学生评价"
        />
      </div>
    </div>
  </div>

  <!-- 评分对话框 -->
  <rating-dialog
    :show="showRatingDialog"
    :target-id="enterpriseId"
    target-type="enterprise"
    title="为企业评分"
    @close="showRatingDialog = false"
    @rated="onEnterpriseRated"
  />
</template>

<script>
import axios from 'axios';
import StarDisplay from '../Common/StarDisplay.vue';
import RatingDialog from '../Common/RatingDialog.vue';
import RatingsList from '../Common/RatingsList.vue';

export default {
  components: {
    StarDisplay,
    RatingDialog,
    RatingsList
  },
  data() {
    return {
      enterpriseId: null,
      enterprise: {},
      tasks: [],
      announcements: [],
      loading: true,
      hasCompletedTask: false,
      studentId: null,
      enterpriseRating: 0,
      showRatingDialog: false
    };
  },
  created() {
    this.enterpriseId = this.$route.params.enterpriseId;
    this.studentId = localStorage.getItem('userId');
    this.fetchEnterpriseDetails();
    this.fetchEnterpriseTasks();
    this.fetchEnterpriseAnnouncements();
    this.checkCompletedTasks();
    this.fetchEnterpriseRating();
  },
  methods: {
    fetchEnterpriseDetails() {
      axios.get(`http://localhost:8080/api/login/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.enterprise = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取企业详情失败:', error);
          this.loading = false;
        });
    },
    fetchEnterpriseTasks() {
      axios.get(`http://localhost:8080/api/tasks/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.tasks = response.data;
        })
        .catch(error => {
          console.error('获取企业任务失败:', error);
        });
    },
    fetchEnterpriseAnnouncements() {
      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements`)
        .then(response => {
          this.announcements = response.data;
        })
        .catch(error => {
          console.error('获取企业公告失败:', error);
        });
    },
    checkCompletedTasks() {
      axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.hasCompletedTask = response.data.length > 0;
        })
        .catch(error => {
          console.error('检查已完成任务失败:', error);
        });
    },
    viewTaskDetails(taskId) {
      this.$router.push({ name: 'task-detail', params: { taskId } });
    },
    viewAnnouncementDetails(noticeId) {
      this.$router.push({ name: 'notice-detail', params: { noticeId } });
    },
    goToChatWithEnterprise() {
      this.$router.push({ name: 'ChatS2E', params: { enterpriseId: this.enterpriseId } });
    },
    goBack() {
      this.$router.go(-1);
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    getEnterpriseSize(size) {
      if (!size) return '未知';
      const sizeMap = {
        'SMALL': '小型企业 (50人以下)',
        'MEDIUM': '中型企业 (50-500人)',
        'LARGE': '大型企业 (500人以上)'
      };
      return sizeMap[size] || size;
    },

    // 获取企业评分
    async fetchEnterpriseRating() {
      try {
        const response = await axios.get(`http://localhost:8080/api/ratings/enterprises/${this.enterpriseId}/average`);
        this.enterpriseRating = response.data.averageRating || 0;
      } catch (error) {
        console.error('获取企业评分失败:', error);
        this.enterpriseRating = 0;
      }
    },

    // 打开评分对话框
    openRatingDialog() {
      this.showRatingDialog = true;
    },

    // 处理企业评分完成事件
    onEnterpriseRated(ratingData) {
      alert('评分成功！');
      // 刷新企业评分
      this.fetchEnterpriseRating();
    },

    // 跳转到与企业聊天页面
    goToChatWithEnterprise() {
      this.$router.push({
        name: 'ChatS2E',
        params: { enterpriseId: this.enterpriseId }
      });
    }
  }
};
</script>

<style scoped>
.enterprise-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  background-color: transparent;
  border: none;
  display: flex;
  align-items: center;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  flex: 1;
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.content-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 企业信息卡片 */
.info-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
  position: relative;
}

.enterprise-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.enterprise-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: bold;
  margin-right: 20px;
}

.enterprise-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.enterprise-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.info-value {
  color: #303133;
}

.enterprise-description {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.enterprise-description h3 {
  font-size: 18px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 10px;
}

.enterprise-description p {
  color: #606266;
  line-height: 1.6;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.chat-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
}

.chat-button:hover {
  background-color: #66b1ff;
}

.chat-icon {
  margin-right: 8px;
}

/* 任务和公告部分 */
.tasks-section, .announcements-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.tasks-section h2, .announcements-section h2 {
  font-size: 20px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.task-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px;
  background-color: #ecf5ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.task-date {
  font-size: 12px;
  color: #909399;
}

.task-content {
  padding: 15px;
  color: #606266;
  font-size: 14px;
}

.task-footer {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-tech {
  font-size: 12px;
  color: #909399;
  background-color: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.view-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-btn:hover {
  background-color: #66b1ff;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.chat-button,
.rate-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-button {
  background-color: #409EFF;
  color: white;
}

.chat-button:hover {
  background-color: #66b1ff;
}

.rate-button {
  background-color: #e6a23c;
  color: white;
}

.rate-button:hover {
  background-color: #f0b95a;
}

.chat-icon,
.rate-icon {
  margin-right: 8px;
}

.rating-value {
  display: flex;
  align-items: center;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.announcement-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.announcement-date {
  font-size: 12px;
  color: #909399;
}

.announcement-content {
  color: #606266;
  font-size: 14px;
  margin-bottom: 15px;
}

.announcement-footer {
  display: flex;
  justify-content: flex-end;
}

/* 评价区域样式 */
.ratings-section {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.ratings-section h2 {
  font-size: 20px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .enterprise-info {
    grid-template-columns: 1fr;
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }
}
</style>
