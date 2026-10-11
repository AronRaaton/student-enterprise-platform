<template>
  <div class="enterprise-all-tasks-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">我发布的任务</h1>
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索任务..."
          class="search-input"
          @input="filterTasks"
        />
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="content-container">
      <div class="action-bar">
        <button class="publish-button" @click="goToTaskPublish">发布新任务</button>
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无任务</h3>
        <p>您还没有发布任何任务</p>
        <button class="action-btn" @click="goToTaskPublish">发布任务</button>
      </div>

      <div v-else class="tasks-grid">
        <div v-for="task in filteredTasks" :key="task.taskId" class="task-card">
          <div class="task-header">
            <h3 class="task-title">{{ task.summary || task.title }}</h3>
            <span class="task-date">{{ formatDate(task.createTime) }}</span>
          </div>

          <div class="task-content">
            <p>{{ truncateText(task.content || task.requirements, 100) }}</p>
          </div>

          <div class="task-stats">
            <div class="stat-item">
              <span class="stat-label">接取人数:</span>
              <span class="stat-value">{{ task.assignmentCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">待审核:</span>
              <span class="stat-value">{{ task.submittedCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">已通过:</span>
              <span class="stat-value">{{ task.completedCount || 0 }}</span>
            </div>
          </div>

          <div class="task-footer">
            <span class="task-tech">{{ task.techStack || '技术栈未指定' }}</span>
            <div class="task-actions">
              <button class="action-btn small" @click="viewTaskDetails(task.taskId)">详情</button>
              <button class="action-btn small" @click="viewCompletionDetails(task.taskId)">提交情况</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      tasks: [],
      filteredTasks: [],
      searchQuery: "",
      loading: true,
      enterpriseId: null
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchTasks();
  },
  methods: {
    fetchTasks() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/tasks/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.tasks = response.data;
          // 获取每个任务的提交统计信息
          this.tasks.forEach(task => {
            this.fetchTaskStats(task.taskId);
          });
          this.filteredTasks = [...this.tasks];
          this.loading = false;
        })
        .catch(error => {
          console.error('无法获取任务列表:', error);
          this.loading = false;
        });
    },
    fetchTaskStats(taskId) {
      axios.get(`http://localhost:8080/api/tasks/${taskId}/stats`)
        .then(response => {
          const stats = response.data;
          const taskIndex = this.tasks.findIndex(t => t.taskId === taskId);
          if (taskIndex !== -1) {
            this.tasks[taskIndex] = {
              ...this.tasks[taskIndex],
              assignmentCount: stats.totalCount || 0,
              submittedCount: stats.submittedCount || 0,
              completedCount: stats.completedCount || 0
            };
            // 更新过滤后的任务列表
            this.filterTasks();
          }
        })
        .catch(error => {
          console.error(`无法获取任务 ${taskId} 的统计信息:`, error);
        });
    },
    viewTaskDetails(taskId) {
      this.$router.push({ name: 'TaskDetailForEnter', params: { taskId } });
    },
    viewCompletionDetails(taskId) {
      this.$router.push({ name: 'TaskSubmissions', params: { taskId } });
    },
    goToTaskPublish() {
      this.$router.push({ name: 'TaskPublish' });
    },
    goBack() {
      this.$router.go(-1);
    },
    filterTasks() {
      if (!this.searchQuery.trim()) {
        this.filteredTasks = [...this.tasks];
        return;
      }

      const query = this.searchQuery.toLowerCase();
      this.filteredTasks = this.tasks.filter(task => {
        return (
          (task.summary && task.summary.toLowerCase().includes(query)) ||
          (task.content && task.content.toLowerCase().includes(query)) ||
          (task.techStack && task.techStack.toLowerCase().includes(query))
        );
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }
  }
};
</script>

<style scoped>
.enterprise-all-tasks-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

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

.search-container {
  position: absolute;
  right: 0;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 200px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #409EFF;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.publish-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.publish-button:hover {
  background-color: #66b1ff;
}

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
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

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
  margin-bottom: 15px;
}

.empty-state h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #303133;
}

.empty-state p {
  color: #606266;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.action-btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.task-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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

.task-stats {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background-color: #f0f9eb;
  border-top: 1px solid #e1f3d8;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #67c23a;
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

.task-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .back-button {
    position: static;
    margin-bottom: 10px;
  }

  .page-title {
    text-align: left;
  }

  .search-container {
    position: static;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }
}
</style>
