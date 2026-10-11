<template>
  <div class="enterprise-detail-container module-page module-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">企业详情</h1>
    </div>

    <!-- 企业资料卡片 -->
    <div class="profile-section">
      <div class="profile-header">
        <div class="enterprise-logo">
          {{ enterprise.name ? enterprise.name.charAt(0) : 'E' }}
        </div>
        <div class="enterprise-basic-info">
          <h2 class="enterprise-name">{{ enterprise.name }}</h2>
          <p class="enterprise-industry">{{ enterprise.industry || '未知行业' }}</p>
        </div>
        <div class="action-buttons-container" v-if="userRole === 'student'">
          <button
            class="favorite-button"
            :class="{ 'is-favorite': isFavorite }"
            @click="toggleFavorite"
          >
            <span class="favorite-icon">{{ isFavorite ? '★' : '☆' }}</span>
            {{ isFavorite ? '已收藏' : '收藏企业' }}
          </button>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">企业规模</div>
          <div class="info-value">{{ getCompanySize(enterprise.size) }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">所在地区</div>
          <div class="info-value">{{ enterprise.location || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">联系邮箱</div>
          <div class="info-value">{{ enterprise.email || '未提供' }}</div>
        </div>
        <div class="info-item" v-if="enterprise.website">
          <div class="info-label">企业网站</div>
          <div class="info-value">
            <a :href="enterprise.website" target="_blank" class="website-link">{{ enterprise.website }}</a>
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">成立时间</div>
          <div class="info-value">{{ enterprise.foundingYear || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">企业评分</div>
          <div class="info-value rating-value">
            <star-display :rating="enterpriseRating" :size="'small'" :show-value="true" />
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">发布任务数</div>
          <div class="info-value">{{ tasks.length || 0 }}</div>
        </div>
      </div>

      <div class="enterprise-description" v-if="enterprise.description">
        <h3 class="description-title">企业简介</h3>
        <div class="description-content">
          {{ enterprise.description }}
        </div>
      </div>
    </div>

    <!-- 企业发布的任务 -->
    <div class="tasks-section">
      <h2 class="section-title">企业发布的任务</h2>

      <div v-if="loadingTasks" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="tasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>该企业暂未发布任何任务</p>
      </div>

      <div v-else class="tasks-grid">
        <div v-for="task in tasks" :key="task.taskId" class="task-card">
          <div class="task-header">
            <h3 class="task-title">{{ task.summary }}</h3>
            <span class="task-date">{{ formatDate(task.publishTime) }}</span>
          </div>

          <div class="task-content">
            <p>{{ truncateText(task.content, 100) }}</p>
            <div class="task-tech" v-if="task.techStack">技术栈: {{ task.techStack }}</div>
          </div>

          <div class="task-footer">
            <span class="task-status" :class="getTaskStatusClass(task.status)">
              {{ getTaskStatusText(task.status) }}
            </span>
            <button class="view-button" @click="viewTaskDetails(task.taskId)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 企业发布的职位 -->
    <div class="published-jobs-section">
      <h2 class="section-title">企业发布的职位</h2>

      <div v-if="loadingJobs" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="jobs.length === 0" class="empty-state">
        <div class="empty-icon">💼</div>
        <p>该企业暂未发布任何职位</p>
      </div>

      <div v-else class="jobs-grid">
        <div v-for="job in jobs" :key="job.jobId" class="job-card">
          <div class="job-header">
            <h3 class="job-title">{{ job.positionTitle || job.title }}</h3>
            <span class="job-date">{{ formatDate(job.publishDate) }}</span>
          </div>

          <div class="job-details">
            <div class="job-detail-item">
              {{ formatSalaryRange(job.minSalary, job.maxSalary) }}
            </div>
            <div class="job-detail-item">
              {{ job.city || enterprise.location || '地点未知' }}
            </div>
            <div class="job-detail-item" v-if="job.educationRequirement">
              {{ job.educationRequirement }}
            </div>
          </div>

          <div class="job-description">
            <p>{{ truncateText(job.description, 100) }}</p>
          </div>

          <div class="job-footer">
            <button class="view-button" @click="viewJobDetails(job.jobId)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 企业评价列表 -->
    <div class="ratings-section">
      <h2 class="section-title">企业评价</h2>
      <div class="ratings-actions">
        <button class="rate-button" v-if="userRole === 'student'" @click="openRatingDialog">
          <span class="rate-icon">⭐</span> 评分与评价
        </button>
      </div>
      <ratings-list
        :target-id="$route.params.enterpriseId"
        target-type="enterprise"
        title="学生评价"
      />
    </div>
  </div>

  <!-- 评分对话框 -->
  <rating-dialog
    :show="showRatingDialog"
    :target-id="$route.params.enterpriseId"
    target-type="enterprise"
    title="为企业评分与评价"
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
      enterprise: {},
      tasks: [],
      jobs: [],
      isFavorite: false,
      studentId: null,
      loadingTasks: true,
      loadingJobs: true,
      userRole: localStorage.getItem('userRole') || '',
      enterpriseRating: 0,
      showRatingDialog: false
    };
  },
  created() {
    const enterpriseId = this.$route.params.enterpriseId;
    this.studentId = localStorage.getItem('userId');

    // 并行加载数据
    Promise.all([
      this.fetchEnterpriseDetails(enterpriseId),
      this.fetchEnterpriseTasks(enterpriseId),
      this.fetchEnterpriseJobs(enterpriseId),
      this.fetchEnterpriseRating(enterpriseId)
    ]).then(() => {
      // 如果是学生角色，检查是否已收藏该企业
      if (this.userRole === 'student') {
        this.checkIfFavorite(enterpriseId);
      }
    });
  },
  methods: {
    fetchEnterpriseDetails(enterpriseId) {
      return axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}`)
        .then(response => {
          this.enterprise = response.data;
        })
        .catch(error => {
          console.error('无法获取企业信息:', error);
        });
    },

    fetchEnterpriseTasks(enterpriseId) {
      return axios.get(`http://localhost:8080/api/enterprises/tasks/${enterpriseId}`)
        .then(response => {
          // 只显示状态为“招募中”的任务，如果没有status字段则默认显示
          this.tasks = response.data.filter(task => !task.status || task.status === 'OPEN');
          console.log('获取到企业任务:', response.data.length, '显示任务:', this.tasks.length);
          this.loadingTasks = false;
        })
        .catch(error => {
          console.error('无法获取企业任务:', error);
          this.loadingTasks = false;
        });
    },

    fetchEnterpriseJobs(enterpriseId) {
      return axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}/jobs`)
        .then(response => {
          this.jobs = response.data;
          this.loadingJobs = false;
        })
        .catch(error => {
          console.error('无法获取企业职位:', error);
          this.loadingJobs = false;
          return [];
        });
    },

    checkIfFavorite(enterpriseId) {
      if (!this.studentId) return Promise.resolve();

      return axios.get(`http://localhost:8080/api/students/${this.studentId}/favorites/enterprises`)
        .then(response => {
          const favorites = response.data;
          this.isFavorite = favorites.some(fav => fav.enterpriseId === parseInt(enterpriseId));
        })
        .catch(error => {
          console.error('无法检查收藏状态:', error);
        });
    },

    toggleFavorite() {
      if (!this.studentId) {
        alert('请先登录');
        return;
      }

      const enterpriseId = this.$route.params.enterpriseId;

      if (this.isFavorite) {
        // 取消收藏
        axios.delete(`http://localhost:8080/api/students/${this.studentId}/favorites/enterprises/${enterpriseId}`)
          .then(() => {
            this.isFavorite = false;
          })
          .catch(error => {
            console.error('取消收藏失败:', error);
            alert('取消收藏失败，请重试');
          });
      } else {
        // 添加收藏
        axios.post(`http://localhost:8080/api/students/${this.studentId}/favorites/enterprises/${enterpriseId}`)
          .then(() => {
            this.isFavorite = true;
          })
          .catch(error => {
            console.error('添加收藏失败:', error);
            alert('添加收藏失败，请重试');
          });
      }
    },

    goBack() {
      this.$router.go(-1);
    },

    viewTaskDetails(taskId) {
      this.$router.push({ name: 'TaskDetail', params: { taskId } });
    },

    viewJobDetails(jobId) {
      this.$router.push({ name: 'JobDetail', params: { jobId } });
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

    formatSalaryRange(minSalary, maxSalary) {
      if (!minSalary && !maxSalary) return '薪资面议';

      minSalary = minSalary || 0;
      maxSalary = maxSalary || 0;

      if (minSalary === maxSalary) {
        return this.formatSalary(minSalary);
      }

      return `${this.formatSalary(minSalary)} - ${this.formatSalary(maxSalary)}`;
    },

    formatSalary(salary) {
      if (!salary) return '0';

      // 如果薪资大于等于10000，显示为XXk
      if (salary >= 10000) {
        return `${(salary / 1000).toFixed(0)}k`;
      }

      // 否则显示为XXXX元
      return `${salary}元`;
    },

    getCompanySize(size) {
      switch (size) {
        case 'SMALL': return '小型企业 (1-50人)';
        case 'MEDIUM': return '中型企业 (51-200人)';
        case 'LARGE': return '大型企业 (201-1000人)';
        case 'VERY_LARGE': return '超大型企业 (1000人以上)';
        default: return '未知规模';
      }
    },

    // 获取企业评分
    async fetchEnterpriseRating(enterpriseId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/ratings/enterprises/${enterpriseId}/average`);
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
      this.fetchEnterpriseRating(this.$route.params.enterpriseId);
    },

    getTaskStatusText(status) {
      switch (status) {
        case 'OPEN': return '招募中';
        case 'IN_PROGRESS': return '进行中';
        case 'COMPLETED': return '已完成';
        case 'CLOSED': return '已关闭';
        default: return '招募中'; // 默认显示招募中
      }
    },

    getTaskStatusClass(status) {
      switch (status) {
        case 'OPEN': return 'status-open';
        case 'IN_PROGRESS': return 'status-progress';
        case 'COMPLETED': return 'status-completed';
        case 'CLOSED': return 'status-closed';
        default: return 'status-open'; // 默认使用招募中的样式
      }
    },

    // 获取企业评分
    async fetchEnterpriseRating(enterpriseId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/ratings/enterprises/${enterpriseId}/average`);
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
      alert('评分成功！感谢您的反馈。');
      // 刷新企业评分
      this.fetchEnterpriseRating(this.$route.params.enterpriseId);
    }
  }
};
</script>

<style scoped>
.enterprise-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  background: none;
  border: none;
  display: flex;
  align-items: center;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  margin-right: 20px;
}

.back-icon {
  font-size: 20px;
  margin-right: 5px;
}

.page-title {
  font-size: 24px;
  margin: 0;
}

.profile-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.enterprise-logo {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #4a6cf7;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 20px;
}

.enterprise-basic-info {
  flex: 1;
}

.enterprise-name {
  font-size: 24px;
  margin: 0 0 5px 0;
}

.enterprise-industry {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.action-buttons-container {
  margin-left: 20px;
  display: flex;
  gap: 10px;
}

.rate-button {
  background-color: #e6a23c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.3s;
}

.rate-button:hover {
  background-color: #f0b95a;
}

.rate-icon {
  margin-right: 5px;
}

.rating-value {
  display: flex;
  align-items: center;
}

.favorite-button {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #f0f2f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.favorite-button.is-favorite {
  background-color: #fff7e6;
  color: #fa8c16;
}

.favorite-icon {
  margin-right: 5px;
  font-size: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

.info-item {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.info-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.info-value {
  font-size: 16px;
  font-weight: 500;
}

.website-link {
  color: #4a6cf7;
  text-decoration: none;
}

.website-link:hover {
  text-decoration: underline;
}

.enterprise-description {
  margin-top: 20px;
}

.description-title {
  font-size: 18px;
  margin-bottom: 10px;
}

.description-content {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
}

.section-title {
  font-size: 22px;
  margin-bottom: 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #4a6cf7;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.tasks-grid, .jobs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.task-card, .job-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.task-header, .job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.task-title, .job-title {
  font-size: 18px;
  margin: 0;
  flex: 1;
}

.task-date, .job-date {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.task-content, .job-description {
  flex: 1;
  margin-bottom: 15px;
}

.task-footer, .job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-open {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-progress {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-completed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-closed {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.view-button {
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
}

.job-details {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
}

.job-detail-item {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.detail-icon {
  margin-right: 5px;
}

.published-jobs-section, .ratings-section {
  margin-top: 30px;
}

.ratings-actions {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-end;
}

.rate-button {
  display: inline-flex;
  align-items: center;
  padding: 8px 15px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #fff; /* 白色文字，但在深色背景上更清晰 */
  font-weight: 500;
  transition: all 0.3s;
}

.rate-button:hover {
  background-color: #66b1ff;
  color: #fff;
}

.rate-icon {
  margin-right: 5px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tasks-grid, .jobs-grid {
    grid-template-columns: 1fr;
  }

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .enterprise-logo {
    margin-bottom: 15px;
  }

  .favorite-button-container {
    margin-left: 0;
    margin-top: 15px;
  }
}

@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
