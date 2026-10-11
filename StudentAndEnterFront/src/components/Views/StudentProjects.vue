<template>
  <div class="student-projects-container">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">项目合作</h1>
    </div>

    <!-- 项目列表 -->
    <div class="projects-content">
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="projects.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无可申请的项目</h3>
        <p>目前没有企业发布的开放项目，请稍后再查看</p>
      </div>

      <!-- 项目列表 -->
      <div v-else class="projects-list">
        <div v-for="project in projects" :key="project.projectId" class="project-card">
          <div class="project-header">
            <h2 class="project-title">{{ project.title }}</h2>
            <span class="project-status" :class="getStatusClass(project.status)">
              {{ getStatusText(project.status) }}
            </span>
          </div>

          <div class="project-info">
            <div class="info-item">
              <span class="info-label">企业:</span>
              <span class="info-value">{{ project.enterpriseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">项目方向:</span>
              <span class="info-value">{{ project.projectDirection }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ formatDate(project.createTime) }}</span>
            </div>
          </div>

          <div class="project-description">
            <p>{{ truncateText(project.description, 200) }}</p>
          </div>

          <div class="project-stats">
            <div class="stat-item">
              <span class="stat-value">{{ project.approvedCount || 0 }}</span>
              <span class="stat-label">已批准</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ project.maxParticipants }}</span>
              <span class="stat-label">最大人数</span>
            </div>
          </div>

          <div class="project-actions">
            <button class="action-btn" @click="viewProjectDetails(project.projectId)">查看详情</button>

            <!-- 已参与的项目显示进入按钮 -->
            <template v-if="isParticipating(project.projectId)">
              <!-- 实训基地项目专用按钮 -->
              <button
                v-if="project.collaborationType === '实训基地'"
                class="action-btn special-btn"
                @click="enterTrainingBaseProject(project.projectId)"
              >
                实训基地详情
              </button>
              <!-- 订单培养项目专用按钮 -->
              <button
                v-else-if="project.collaborationType === '订单培养'"
                class="action-btn special-btn"
                @click="enterOrderTrainingProject(project.projectId)"
              >
                进入项目
              </button>
              <!-- 通用进入项目按钮 -->
              <button
                v-else
                class="action-btn enter-btn"
                @click="enterProject(project.projectId)"
              >
                进入项目
              </button>
            </template>

            <!-- 未参与的项目显示申请按钮 -->
            <button
              v-else
              class="action-btn apply-btn"
              @click="applyForProject(project.projectId)"
              :disabled="hasApplied(project.projectId)"
            >
              <span v-if="hasApplied(project.projectId)">已申请</span>
              <span v-else-if="isRejected(project.projectId)">重新申请</span>
              <span v-else>申请参与</span>
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
      projects: [],
      loading: true,
      studentId: null,
      applications: []
    };
  },
  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchProjects();
    this.fetchStudentApplications();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'student-home' });
    },
    async fetchProjects() {
      this.loading = true;

      try {
        const response = await axios.get('http://localhost:8080/api/projects', {
          params: { page: 0, size: 100 }
        });
        this.projects = response.data.content || [];
      } catch (error) {
        console.error('获取项目列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchStudentApplications() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/applications/student/${this.studentId}`);
        this.applications = response.data;
      } catch (error) {
        console.error('获取学生申请记录失败:', error);
      }
    },
    hasApplied(projectId) {
      // 找到当前项目的申请
      const application = this.applications.find(app => app.projectId === projectId);

      // 如果没有申请或申请被拒绝，则可以申请
      return application && (application.status === 'PENDING' || application.status === 'APPROVED');
    },

    isRejected(projectId) {
      // 找到当前项目的申请
      const application = this.applications.find(app => app.projectId === projectId);

      // 如果申请存在且被拒绝，返回true
      return application && application.status === 'REJECTED';
    },

    isParticipating(projectId) {
      // 找到当前项目的申请
      const application = this.applications.find(app => app.projectId === projectId);

      // 如果申请存在且状态为已批准，则是参与者
      return application && application.status === 'APPROVED';
    },

    enterProject(projectId) {
      // 跳转到项目详情页面，并添加参数标记已参与
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId },
        query: { participating: 'true' }
      });
    },
    enterTrainingBaseProject(projectId) {
      // 跳转到实训基地项目详情页面
      this.$router.push({
        name: 'TrainingBaseDetail',
        params: { projectId }
      });
    },
    enterOrderTrainingProject(projectId) {
      // 跳转到订单培养项目详情页面
      this.$router.push({
        name: 'OrderTrainingDetail',
        params: { projectId }
      });
    },
    viewProjectDetails(projectId) {
      // 获取项目信息
      const project = this.projects.find(p => p.projectId === projectId);

      // 根据项目类型跳转到不同的页面
      if (project && project.collaborationType && project.collaborationType.includes('实训基地')) {
        // 如果是实训基地项目，跳转到实训基地详情页面
        this.$router.push({ name: 'TrainingBaseDetail', params: { projectId } });
      } else if (project && project.collaborationType === '订单培养') {
        // 如果是订单培养项目，跳转到订单培养详情页面
        this.$router.push({ name: 'OrderTrainingDetail', params: { projectId } });
      } else {
        // 其他类型项目跳转到通用项目详情页面
        this.$router.push({ name: 'ProjectDetail', params: { projectId } });
      }
    },
    applyForProject(projectId) {
      if (this.hasApplied(projectId)) {
        return;
      }

      this.$router.push({
        name: 'ProjectApplication',
        params: { projectId }
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
.student-projects-container {
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

/* 项目列表 */
.projects-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.project-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.project-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.project-title {
  font-size: 20px;
  color: #303133;
  margin: 0;
}

.project-status {
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

.project-info {
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
}

.project-description {
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.project-stats {
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

.project-actions {
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

.special-btn {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.special-btn:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.action-btn:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  border-color: #e4e7ed;
  cursor: not-allowed;
}

.apply-btn {
  background-color: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.apply-btn:hover:not(:disabled) {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.enter-btn {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.enter-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}
</style>
