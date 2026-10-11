<template>
  <div class="enterprise-profile-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">企业资料</h1>
    </div>

    <!-- 企业信息卡片 -->
    <div class="profile-section">
      <div class="profile-header">
        <div class="enterprise-logo">
          {{ enterprise.name ? enterprise.name.charAt(0) : 'E' }}
        </div>
        <div class="enterprise-basic-info">
          <h2 class="enterprise-name">{{ enterprise.name }}</h2>
          <p class="enterprise-industry">{{ enterprise.industry || '未知行业' }}</p>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">企业类型</div>
          <div class="info-value">{{ enterprise.type || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">职工人数</div>
          <div class="info-value">{{ enterprise.employeeCount || '未知' }} 人</div>
        </div>
        <div class="info-item">
          <div class="info-label">成立年份</div>
          <div class="info-value">{{ enterprise.foundingYear || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">地址</div>
          <div class="info-value">{{ enterprise.location || '未提供' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">企业评分</div>
          <div class="info-value rating-value">
            <star-display :rating="enterpriseRating" :size="'small'" :show-value="true" />
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">网址</div>
          <div class="info-value">
            <a v-if="enterprise.website" :href="enterprise.website" target="_blank">{{ enterprise.website }}</a>
            <span v-else>未提供</span>
          </div>
        </div>
        <div class="info-item">
          <div class="info-label">邮箱</div>
          <div class="info-value">{{ enterprise.email || '未提供' }}</div>
        </div>
      </div>

      <!-- 企业简介 -->
      <div class="enterprise-description">
        <h3 class="description-title">企业简介</h3>
        <div class="description-content">
          <p>{{ enterprise.description || '暂无企业简介' }}</p>
        </div>
      </div>

      <!-- 编辑按钮 -->
      <div class="edit-section">
        <button class="edit-button" @click="showEditModal = true">
          <span class="edit-icon">✏️</span> 编辑企业资料
        </button>
      </div>
    </div>

    <!-- 已发布任务列表 -->
    <div class="published-tasks-section">
      <h2 class="section-title">已发布任务</h2>

      <div v-if="loadingTasks" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="tasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>您还没有发布任何任务</p>
        <button class="action-button" @click="goToPublishTask">发布新任务</button>
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
            <span class="task-status" :class="getTaskStatusClass(task.status)">
              {{ getTaskStatusText(task.status) }}
            </span>
            <button class="view-button" @click="viewTaskDetails(task.taskId)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 学生评价列表 -->
    <ratings-list
      :target-id="$route.params.enterpriseId"
      target-type="enterprise"
      title="学生评价"
    />

    <!-- 编辑企业资料模态框 -->
    <div v-if="showEditModal" class="edit-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>编辑企业资料</h2>
          <button class="close-button" @click="showEditModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateEnterpriseProfile">
            <div class="form-group">
              <label for="name">企业名称</label>
              <input type="text" id="name" v-model="editForm.name" required>
            </div>
            <div class="form-group">
              <label for="type">企业类型</label>
              <input type="text" id="type" v-model="editForm.type">
            </div>
            <div class="form-group">
              <label for="employeeCount">职工人数</label>
              <input type="number" id="employeeCount" v-model="editForm.employeeCount" min="0">
            </div>
            <div class="form-group">
              <label for="foundingYear">成立年份</label>
              <input type="number" id="foundingYear" v-model="editForm.foundingYear" min="1900" :max="new Date().getFullYear()">
            </div>
            <div class="form-group">
              <label for="location">地址</label>
              <input type="text" id="location" v-model="editForm.location">
            </div>
            <div class="form-group">
              <label for="website">网址</label>
              <input type="url" id="website" v-model="editForm.website" placeholder="https://www.example.com">
            </div>
            <div class="form-group">
              <label for="email">邮箱</label>
              <input type="email" id="email" v-model="editForm.email">
            </div>
            <div class="form-group">
              <label for="description">企业简介</label>
              <textarea id="description" v-model="editForm.description" rows="5"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="cancel-button" @click="showEditModal = false">取消</button>
              <button type="submit" class="save-button" :disabled="updating">
                {{ updating ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import StarDisplay from '../Common/StarDisplay.vue';
import RatingsList from '../Common/RatingsList.vue';

export default {
  components: {
    StarDisplay,
    RatingsList
  },
  data() {
    return {
      enterprise: {},
      tasks: [],
      loadingTasks: true,
      showEditModal: false,
      enterpriseRating: 0,
      editForm: {
        name: '',
        type: '',
        employeeCount: 0,
        foundingYear: new Date().getFullYear(),
        location: '',
        website: '',
        email: '',
        description: ''
      },
      updating: false
    };
  },
  created() {
    const enterpriseId = this.$route.params.enterpriseId;
    this.fetchEnterpriseDetails(enterpriseId);
    this.fetchEnterpriseTasks(enterpriseId);
    this.fetchEnterpriseRating(enterpriseId);
  },
  methods: {
    fetchEnterpriseDetails(enterpriseId) {
      axios.get(`http://localhost:8080/api/login/enterprise/${enterpriseId}`)
        .then(response => {
          this.enterprise = response.data;

          // 初始化编辑表单
          this.editForm = {
            name: this.enterprise.name || '',
            type: this.enterprise.type || '',
            employeeCount: this.enterprise.employeeCount || 0,
            foundingYear: this.enterprise.foundingYear || new Date().getFullYear(),
            location: this.enterprise.location || '',
            website: this.enterprise.website || '',
            email: this.enterprise.email || '',
            description: this.enterprise.description || ''
          };
        })
        .catch(error => {
          console.error('无法获取企业信息:', error);
          // 尝试使用备用API
          axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}`)
            .then(response => {
              this.enterprise = response.data;

              // 初始化编辑表单
              this.editForm = {
                name: this.enterprise.name || '',
                type: this.enterprise.type || '',
                employeeCount: this.enterprise.employeeCount || 0,
                foundingYear: this.enterprise.foundingYear || new Date().getFullYear(),
                location: this.enterprise.location || '',
                website: this.enterprise.website || '',
                email: this.enterprise.email || '',
                description: this.enterprise.description || ''
              };
            })
            .catch(err => {
              console.error('备用API也失败:', err);
            });
        });
    },

    fetchEnterpriseTasks(enterpriseId) {
      axios.get(`http://localhost:8080/api/tasks/enterprise/${enterpriseId}`)
        .then(response => {
          this.tasks = response.data;
          this.loadingTasks = false;
        })
        .catch(error => {
          console.error('无法获取企业任务:', error);
          this.loadingTasks = false;
        });
    },

    goBack() {
      this.$router.go(-1);
    },

    viewTaskDetails(taskId) {
      this.$router.push({ name: 'TaskDetail', params: { taskId } });
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



    getTaskStatusText(status) {
      switch (status) {
        case 'OPEN': return '招募中';
        case 'IN_PROGRESS': return '进行中';
        case 'COMPLETED': return '已完成';
        case 'CLOSED': return '已关闭';
        default: return '未知状态';
      }
    },

    getTaskStatusClass(status) {
      switch (status) {
        case 'OPEN': return 'status-open';
        case 'IN_PROGRESS': return 'status-progress';
        case 'COMPLETED': return 'status-completed';
        case 'CLOSED': return 'status-closed';
        default: return '';
      }
    },

    goToPublishTask() {
      this.$router.push({ name: 'PublishTask' });
    },

    updateEnterpriseProfile() {
      this.updating = true;

      axios.put(`http://localhost:8080/api/enterprises/${this.enterprise.enterpriseId}`, this.editForm)
        .then(response => {
          // 更新本地企业信息
          this.enterprise = { ...this.enterprise, ...this.editForm };
          this.showEditModal = false;
          alert('企业资料更新成功');
        })
        .catch(error => {
          console.error('更新企业资料失败:', error);
          alert('更新企业资料失败，请重试');
        })
        .finally(() => {
          this.updating = false;
        });
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
    }
  }
};
</script>

<style scoped>
.rating-value {
  display: flex;
  align-items: center;
}
.enterprise-profile-container {
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

.edit-section {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.edit-button {
  display: inline-flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.edit-icon {
  margin-right: 5px;
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

.action-button {
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 15px;
  margin-top: 15px;
  cursor: pointer;
  font-size: 14px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.task-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.task-title {
  font-size: 18px;
  margin: 0;
  flex: 1;
}

.task-date {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.task-content {
  flex: 1;
  margin-bottom: 15px;
}

.task-footer {
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

.edit-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-button {
  padding: 8px 15px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-button {
  padding: 8px 15px;
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-button:disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}

/* 评价区域样式 */
.ratings-section {
  margin-top: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.ratings-description {
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  background-color: #f8f9fa;
  padding: 10px 15px;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}
</style>