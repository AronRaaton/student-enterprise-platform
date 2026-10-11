<template>
  <div class="all-tasks-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">任务列表</h1>
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
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredTasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <h3>暂无任务</h3>
        <p>当前没有可用的任务</p>
      </div>

      <div v-else class="tasks-grid">
        <div v-for="task in filteredTasks" :key="task.taskId" class="task-card">
          <div class="task-header">
            <h3 class="task-title">{{ task.summary }}</h3>
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
                <span class="info-label">发布时间:</span>
                <span class="info-value">{{ formatDate(task.createTime) }}</span>
              </p>
            </div>

            <div class="task-description">
              <p>{{ truncateText(task.content || task.requirements, 150) }}</p>
            </div>
          </div>

          <div class="task-actions">
            <button class="view-button" @click="viewTaskDetails(task.taskId)">查看详情</button>
            <button class="apply-button" @click="applyForTask(task.taskId)">申请任务</button>
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
      claimedTaskIds: [], // 存储已领取的任务ID
      searchQuery: "",
      loading: true,
      studentId: null
    };
  },
  created() {
    this.studentId = localStorage.getItem("userId");

    // 先获取已领取的任务，然后获取所有任务
    if (this.studentId) {
      this.fetchClaimedTasks().then(() => {
        this.fetchAllTasks();
      });
    } else {
      this.fetchAllTasks();
    }
  },
  methods: {
    // 获取学生已领取的任务
    fetchClaimedTasks() {
      return axios
        .get(`http://localhost:8080/api/students/tasks/${this.studentId}`)
        .then((response) => {
          // 提取已领取任务的ID
          this.claimedTaskIds = response.data.map(task => task.taskId);
          console.log('已领取的任务IDs:', this.claimedTaskIds);
        })
        .catch((error) => {
          console.error("无法获取已领取任务:", error);
          this.claimedTaskIds = [];
        });
    },

    // 获取所有任务，并过滤掉已领取的任务
    fetchAllTasks() {
      this.loading = true;
      axios
        .get("http://localhost:8080/api/tasks/all")
        .then((response) => {
          // 过滤掉已领取的任务
          const allTasks = response.data;
          console.log('所有任务:', allTasks);
          console.log('已领取的任务IDs:', this.claimedTaskIds);

          // 将任务ID转换为字符串进行比较，确保类型一致
          const claimedTaskIdsStr = this.claimedTaskIds.map(id => id.toString());
          this.tasks = allTasks.filter(task => !claimedTaskIdsStr.includes(task.taskId.toString()));
          this.filteredTasks = [...this.tasks];
          this.loading = false;
        })
        .catch((error) => {
          console.error("无法获取所有任务:", error);
          this.loading = false;
        });
    },
    viewTaskDetails(taskId) {
      this.$router.push({ name: "TaskDetail", params: { taskId } });
    },
    goBack() {
      this.$router.go(-1);
    },
    filterTasks() {
      if (!this.searchQuery.trim()) {
        // 已经在fetchAllTasks中过滤了已领取的任务
        this.filteredTasks = [...this.tasks];
        return;
      }

      const query = this.searchQuery.toLowerCase();
      this.filteredTasks = this.tasks.filter(task => {
        return (
          (task.summary && task.summary.toLowerCase().includes(query)) ||
          (task.content && task.content.toLowerCase().includes(query)) ||
          (task.techStack && task.techStack.toLowerCase().includes(query)) ||
          (task.enterprise && task.enterprise.name && task.enterprise.name.toLowerCase().includes(query))
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
    },
    applyForTask(taskId) {
      const studentId = localStorage.getItem("userId");
      if (!studentId) {
        alert("请先登录");
        return;
      }

      // 使用正确的API端点接取任务
      axios.post(`http://localhost:8080/api/tasks/${taskId}/assign/${studentId}`)
        .then(() => {
          alert("申请任务成功！");

          // 更新已领取任务列表
          this.claimedTaskIds.push(parseInt(taskId));

          // 从当前列表中移除该任务
          this.tasks = this.tasks.filter(task => task.taskId.toString() !== taskId.toString());
          this.filteredTasks = this.filteredTasks.filter(task => task.taskId.toString() !== taskId.toString());

          // 跳转到已接取任务页面
          this.$router.push({ name: 'AllAssignedTasks' });
        })
        .catch(error => {
          console.error("申请任务失败:", error);
          alert("申请任务失败，请重试");
        });
    }
  },
};
</script>

<style scoped>
.all-tasks-container {
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
}

.task-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
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
  display: flex;
  justify-content: space-between;
}

.view-button, .apply-button {
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  border: none;
  font-size: 14px;
}

.view-button {
  background-color: #ecf5ff;
  color: #409EFF;
}

.view-button:hover {
  background-color: #d9ecff;
}

.apply-button {
  background-color: #409EFF;
  color: white;
}

.apply-button:hover {
  background-color: #66b1ff;
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
    margin-bottom: 15px;
  }

  .search-container {
    position: static;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}
</style>
