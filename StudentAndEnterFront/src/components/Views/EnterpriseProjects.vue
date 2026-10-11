<template>
  <div class="enterprise-projects-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">项目合作管理</h1>
      <button class="publish-button" @click="goToPublish">
        <span class="publish-icon">+</span> 发布新项目
      </button>
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
        <h3>暂无项目合作</h3>
        <p>您还没有发布任何项目合作，点击下方按钮发布第一个项目</p>
        <button class="action-btn" @click="goToPublish">发布项目</button>
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
              <span class="info-label">项目方向:</span>
              <span class="info-value">{{ project.projectDirection }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">合作类型:</span>
              <span class="info-value">{{ project.collaborationType || '未指定' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ formatDate(project.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">截止时间:</span>
              <span class="info-value">{{ project.endDate ? formatDate(project.endDate) : '无截止日期' }}</span>
            </div>
          </div>

          <div class="project-description">
            <p>{{ truncateText(project.description, 200) }}</p>
          </div>

          <div class="project-stats">
            <div class="stat-item">
              <span class="stat-value">{{ project.applicationCount || 0 }}</span>
              <span class="stat-label">申请数</span>
            </div>
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
            <button class="action-btn edit-btn" @click="editProject(project)">编辑项目</button>
            <button class="action-btn" @click="viewApplications(project.projectId)">
              查看申请
              <span v-if="getPendingCount(project)" class="badge">{{ getPendingCount(project) }}</span>
            </button>
            <!-- 订单培养项目专用按钮 -->
            <button
              v-if="project.collaborationType === '订单培养'"
              class="action-btn special-btn"
              @click="viewOrderTrainingDetail(project.projectId)"
            >
              订单培养详情
            </button>
            <!-- 实训基地项目专用按钮 -->
            <button
              v-if="project.collaborationType === '实训基地'"
              class="action-btn special-btn"
              @click="viewTrainingBaseDetail(project.projectId)"
            >
              实训基地详情
            </button>
            <!-- 产学研项目专用按钮 -->
            <button
              v-if="project.collaborationType === '产学研合作'"
              class="action-btn special-btn"
              @click="viewResearchDetail(project.projectId)"
            >
              产学研详情
            </button>
            <button
              class="action-btn"
              :class="{ 'danger': project.status === 'OPEN', 'success': project.status !== 'OPEN' }"
              @click="toggleProjectStatus(project)"
            >
              {{ project.status === 'OPEN' ? '关闭项目' : '开放项目' }}
            </button>
            <button
              class="action-btn delete-btn"
              @click="deleteProject(project)"
            >
              删除项目
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
      pendingApplications: {}
    };
  },
  created() {
    this.fetchProjects();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'enterprise-home' });
    },
    goToPublish() {
      this.$router.push({ name: 'ProjectCollaborationPublish' });
    },
    async fetchProjects() {
      this.loading = true;
      const enterpriseId = localStorage.getItem('userId');

      try {
        const response = await axios.get(`http://localhost:8080/api/projects/enterprise/${enterpriseId}`);
        this.projects = response.data;

        // 获取每个项目的待审核申请数量
        for (const project of this.projects) {
          this.fetchPendingApplications(project.projectId);
        }
      } catch (error) {
        console.error('获取项目列表失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchPendingApplications(projectId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${projectId}/applications/pending`);
        this.pendingApplications[projectId] = response.data.length;
      } catch (error) {
        console.error(`获取项目 ${projectId} 的待审核申请失败:`, error);
        this.pendingApplications[projectId] = 0;
      }
    },
    getPendingCount(project) {
      return this.pendingApplications[project.projectId] || 0;
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
    viewApplications(projectId) {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId },
        query: { tab: 'applications' }
      });
    },

    // 编辑项目
    editProject(project) {
      this.$router.push({
        name: 'ProjectCollaborationPublish',
        query: {
          edit: true,
          projectId: project.projectId
        }
      });
    },
    // 查看订单培养项目详情
    viewOrderTrainingDetail(projectId) {
      this.$router.push({
        name: 'OrderTrainingDetail',
        params: { projectId }
      });
    },
    // 查看实训基地项目详情
    viewTrainingBaseDetail(projectId) {
      this.$router.push({
        name: 'TrainingBaseDetail',
        params: { projectId }
      });
    },
    // 查看产学研项目详情
    viewResearchDetail(projectId) {
      // 目前跳转到普通项目详情页，后续可以实现专门的产学研详情页
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId }
      });
    },
    async toggleProjectStatus(project) {
      const newStatus = project.status === 'OPEN' ? 'CLOSED' : 'OPEN';

      try {
        await axios.put(`http://localhost:8080/api/projects/${project.projectId}/status`, null, {
          params: { status: newStatus }
        });

        // 更新本地项目状态
        const index = this.projects.findIndex(p => p.projectId === project.projectId);
        if (index !== -1) {
          this.projects[index].status = newStatus;
        }
      } catch (error) {
        console.error('更新项目状态失败:', error);
        alert('更新项目状态失败，请稍后重试');
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
    },

    async deleteProject(project) {
      if (!confirm('确定要删除项目 "' + project.title + '" 吗？\n\n注意：删除后将无法恢复，相关的申请和提交记录也将被删除。')) {
        return;
      }

      const enterpriseId = localStorage.getItem('userId');

      try {
        await axios.delete(`http://localhost:8080/api/projects/enterprise/${enterpriseId}/project/${project.projectId}`);

        // 从列表中移除该项目
        this.projects = this.projects.filter(p => p.projectId !== project.projectId);

        alert('项目删除成功');
      } catch (error) {
        console.error('删除项目失败:', error);
        alert('删除项目失败，请重试: ' + (error.response?.data?.error || error.message));
      }
    }
  }
};
</script>

<style scoped>
.enterprise-projects-container {
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

.delete-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border-color: #fde2e2;
}

.delete-btn:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.special-btn {
  background-color: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.special-btn:hover {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}
</style>
