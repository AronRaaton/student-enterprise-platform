<template>
  <div class="assigned-tasks-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">我的已接取任务</h1>
    </div>

    <!-- 任务列表 -->
    <div class="content-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="assignedTasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无已接取任务</h3>
        <p>您还没有接取任何任务，可以去任务列表查看可用任务</p>
        <button class="primary-button" @click="goToAllTasks">浏览任务列表</button>
      </div>

      <div v-else class="tasks-grid">
        <div v-for="task in assignedTasks" :key="task.taskId" class="task-card" @click="goToSubmission(task.taskId)">
          <div class="task-header">
            <h3 class="task-title">{{ task.summary }}</h3>
            <span class="task-status" :class="getStatusClass(task)">{{ getStatusText(task) }}</span>
          </div>

          <div class="task-content">
            <div class="task-info">
              <p class="enterprise-info">
                <span class="info-label">发布企业:</span>
                <span class="info-value">{{ task.enterprise && task.enterprise.name || '未知企业' }}</span>
              </p>
              <p class="tech-stack">
                <span class="info-label">技术栈:</span>
                <span class="info-value">{{ task.techStack }}</span>
              </p>
              <p class="task-date">
                <span class="info-label">接取时间:</span>
                <span class="info-value">{{ formatDate(task.assignedTime || task.createTime) }}</span>
              </p>
            </div>

            <div class="task-description">
              <p>{{ truncateText(task.content, 150) }}</p>
            </div>
          </div>

          <div class="task-actions">
            <div class="button-group">
              <button class="view-button" @click.stop="viewTaskDetails(task.taskId)">查看详情</button>
              <button class="submit-button" @click.stop="goToSubmission(task.taskId)">提交任务</button>
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
      assignedTasks: [],
      loading: true
    };
  },
  created() {
    const studentId = localStorage.getItem("userId");
    if (studentId) {
      this.fetchAssignedTasks(studentId);
    } else {
      this.loading = false;
    }
  },
  methods: {
    fetchAssignedTasks(studentId) {
      this.loading = true;
      axios
        .get(`http://localhost:8080/api/students/tasks/${studentId}`)
        .then(async (response) => {
          const tasks = response.data;

          // 获取每个任务的提交状态
          const tasksWithStatus = await Promise.all(tasks.map(async (task) => {
            try {
              // 获取学生对该任务的提交记录
              const submissionResponse = await axios.get(
                `http://localhost:8080/api/students/tasks/submissions/${studentId}/tasks/${task.taskId}`
              );

              // 将提交状态添加到任务对象中
              console.log(`任务 ${task.taskId} 的提交状态:`, submissionResponse.data.status);
              return {
                ...task,
                status: submissionResponse.data.status,
                submission: submissionResponse.data  // 保存整个提交对象，以便于后续处理
              };
            } catch (error) {
              console.error(`无法获取任务 ${task.taskId} 的提交状态:`, error);
              return task;
            }
          }));

          // 打印所有任务的状态信息，便于调试
          tasksWithStatus.forEach(task => {
            console.log(`任务 ${task.taskId} 的最终状态:`, task.status);
          });

          this.assignedTasks = tasksWithStatus;
          this.loading = false;
        })
        .catch((error) => {
          console.error("无法获取已接取任务:", error);
          this.loading = false;
        });
    },
    viewTaskDetails(taskId) {
      this.$router.push({ name: "TaskDetail", params: { taskId } });
    },

    goToSubmission(taskId) {
      this.$router.push({ name: "TaskSubmission", params: { taskId } });
    },
    goBack() {
      this.$router.go(-1);
    },
    goToAllTasks() {
      this.$router.push({ name: "all-task" });
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
    getStatusClass(task) {
      if (!task) return 'status-pending';

      // 获取任务提交状态
      const submissionStatus = this.getTaskSubmissionStatus(task);

      switch(submissionStatus) {
        case 'COMPLETED':
          return 'status-completed';
        case 'SUBMITTED':
          return 'status-in-progress';
        case 'NEEDS_REVISION':
          return 'status-rejected';
        case 'NOT_SUBMITTED':
        default:
          return 'status-pending';
      }
    },
    getStatusText(task) {
      if (!task) return '进行中';

      // 获取任务提交状态
      const submissionStatus = this.getTaskSubmissionStatus(task);

      switch(submissionStatus) {
        case 'COMPLETED':
          return '已完成';
        case 'SUBMITTED':
          return '待审核';
        case 'NEEDS_REVISION':
          return '需要修改';
        case 'NOT_SUBMITTED':
          return '未提交';
        default:
          return '进行中';
      }
    },
    // 获取任务的提交状态
    getTaskSubmissionStatus(task) {
      console.log('获取任务状态，任务对象:', task);

      // 如果任务有提交状态属性，直接返回
      if (task && task.status) {
        console.log('任务状态从 task.status 获取:', task.status);
        return task.status;
      }

      // 如果任务有提交属性，返回提交的状态
      if (task && task.submission && task.submission.status) {
        console.log('任务状态从 task.submission.status 获取:', task.submission.status);
        return task.submission.status;
      }

      // 默认返回未提交状态
      console.log('未找到任务状态，返回默认值 NOT_SUBMITTED');
      return 'NOT_SUBMITTED';
    }
  }
};
</script>

<style scoped>
.assigned-tasks-container {
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

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
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
  text-align: center;
  padding: 50px 0;
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

.primary-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.primary-button:hover {
  background-color: #66b1ff;
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
  display: flex;
  flex-direction: column;
  height: 100%;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
}

.task-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-in-progress {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.task-content {
  padding: 15px;
  flex: 1;
}

.task-info {
  margin-bottom: 15px;
}

.info-label {
  color: #909399;
  margin-right: 5px;
}

.info-value {
  color: #606266;
  font-weight: 500;
}

.enterprise-info, .tech-stack, .task-date {
  margin: 5px 0;
}

.task-description {
  color: #606266;
  line-height: 1.5;
}

.task-actions {
  padding: 15px;
  border-top: 1px solid #ebeef5;
}

.button-group {
  display: flex;
  justify-content: space-between;
}

.view-button, .submit-button {
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-button {
  background-color: #409EFF;
  color: white;
}

.view-button:hover {
  background-color: #66b1ff;
}

.submit-button {
  background-color: #67c23a;
  color: white;
}

.submit-button:hover {
  background-color: #85ce61;
}

/* 任务状态样式 */
.task-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-in-progress {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

@media (max-width: 768px) {
  .tasks-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .back-button {
    position: static;
    margin-bottom: 15px;
  }

  .page-title {
    text-align: left;
  }
}
</style>
