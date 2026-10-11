<template>
  <div class="task-detail-container module-page module-detail">
    <header class="header">
      <h1>{{ task.summary }}</h1>
    </header>
    <div class="task-details">
      <p><strong>任务描述：</strong>{{ task.summary }}</p>
      <p><strong>技术栈：</strong>{{ task.techStack}}</p>
      <p><strong>要求：</strong>{{ task.requirements }}</p>
      <p><strong>接取人数：</strong>{{ assignmentCount }}</p>
    </div>

    <div class="action-buttons">
      <button @click="backToHome">返回主页</button>
      <button class="view-submissions-btn" @click="viewSubmissions">查看提交情况</button>
      <button class="delete-btn" @click="deleteTask">删除任务</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      task: {},
      assignmentCount: 0,
      enterpriseId: null
    };
  },
  created() {
    const taskId = this.$route.params.taskId;
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchTaskDetails(taskId);
    this.fetchTaskAssignmentCount(taskId);
  },
  methods: {
    fetchTaskDetails(taskId) {
      axios
          .get(`http://localhost:8080/api/tasks/${taskId}`)
          .then((response) => {
            this.task = response.data;
          })
          .catch((error) => {
            console.error("无法获取任务详情:", error);
          });
    },
    fetchTaskAssignmentCount(taskId) {
      axios.get(`http://localhost:8080/api/tasks/${taskId}/assignment-count`)
        .then(response => {
          this.assignmentCount = response.data;
        })
        .catch(error => {
          console.error("无法获取任务接取人数:", error);
        });
    },
    backToHome() {
      this.$router.push({ name: "enterprise-home" });
    },

    deleteTask() {
      if (!confirm('确定要删除该任务吗？此操作不可恢复。')) {
        return;
      }

      axios.delete(`http://localhost:8080/api/enterprises/${this.enterpriseId}/tasks/${this.task.taskId}`)
        .then(() => {
          alert('任务删除成功');
          this.$router.push({ name: 'enterprise-home' });
        })
        .catch(error => {
          console.error('删除任务失败:', error);
          alert('删除任务失败，请重试');
        });
    },
    viewSubmissions() {
      const taskId = this.$route.params.taskId;
      this.$router.push({ name: "TaskSubmissions", params: { taskId } });
    },
  },
};
</script>

<style scoped>
.task-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 2rem;
  color: #333;
}

.task-details {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.task-details p {
  font-size: 1.1rem;
  color: #555;
  margin: 10px 0;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

button {
  padding: 10px 20px;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #0958d9;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.delete-btn:hover {
  background-color: #f78989;
}

.view-submissions-btn {
  background-color: #67c23a;
}

.view-submissions-btn:hover {
  background-color: #85ce61;
}
</style>
