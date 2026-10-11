<template>
  <div class="task-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="backToHome">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">任务详情</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 任务详情内容 -->
    <div v-else class="content-container">
      <!-- 任务标题和基本信息 -->
      <div class="task-header">
        <h2 class="task-title">{{ task.summary }}</h2>
        <div class="task-meta">
          <div class="meta-item">
            <span class="meta-label">发布企业:</span>
            <span
              class="meta-value enterprise-link"
              @click="viewEnterpriseDetails(task.enterprise ? task.enterprise.enterpriseId : null)"
            >
              {{ task.enterprise ? task.enterprise.name : '未知企业' }}
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">发布时间:</span>
            <span class="meta-value">{{ formatDate(task.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">已接取人数:</span>
            <span class="meta-value">{{ assignmentCount }} 人</span>
          </div>
          <div class="meta-item" v-if="taskRating > 0">
            <span class="meta-label">任务评分:</span>
            <span class="meta-value rating-value">
              <star-display :rating="taskRating" :size="'small'" />
            </span>
          </div>
        </div>
      </div>

      <!-- 任务详细信息 -->
      <div class="task-info-section">
        <div class="info-card">
          <h3 class="section-title">技术栈</h3>
          <div class="tech-stack-tags">
            <span v-for="(tech, index) in techStackArray" :key="index" class="tech-tag">{{ tech }}</span>
          </div>
        </div>

        <div class="info-card">
          <h3 class="section-title">难度级别</h3>
          <div class="difficulty-rating">
            <StarRatingDisplay :value="task.difficulty || 0" />
          </div>
        </div>

        <div class="info-card">
          <h3 class="section-title">任务描述</h3>
          <div class="task-content">
            <p>{{ task.content }}</p>
          </div>
        </div>

        <div class="info-card">
          <h3 class="section-title">任务要求</h3>
          <div class="task-requirements">
            <p>{{ task.requirements || '暂无特殊要求' }}</p>
          </div>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="task-actions">
        <!-- 学生操作按钮 -->
        <div v-if="userRole === 'student'" class="student-actions">
          <button
            v-if="showClaimButton"
            class="claim-button"
            @click="claimTask"
            :disabled="claimLoading"
          >
            <span v-if="claimLoading" class="loading-icon"></span>
            <span v-else><span class="plus-icon">+</span> 接取任务</span>
            <!-- 增加点击效果 -->
            <span class="ripple"></span>
          </button>
          <div v-if="showClaimedMessage" class="claimed-actions">
            <div class="claimed-message">
              <span class="check-icon">✔</span> 您已接取该任务
            </div>
            <div class="claimed-buttons">
              <button class="submit-button" @click="goToSubmission">提交任务</button>
              <!-- 评分按钮 - 只在任务完成时显示 -->
              <button
                v-if="hasCompletedTask"
                class="rate-button"
                @click="openRatingDialog"
              >
                评分任务
              </button>
            </div>
          </div>
        </div>

        <!-- 企业操作按钮 -->
        <div v-if="userRole === 'enterprise'" class="enterprise-actions">
          <button class="view-submissions-btn" @click="viewSubmissions">查看提交情况</button>
          <button class="delete-btn" @click="deleteTask">删除任务</button>
        </div>
      </div>

      <!-- 任务评价列表 -->
      <div class="ratings-section" v-if="userRole === 'enterprise' || hasCompletedTask">
        <h2 class="section-title">任务评价</h2>
        <ratings-list
          :target-id="$route.params.taskId"
          target-type="task"
          title="学生评价"
        />
      </div>
    </div>
  </div>

  <!-- 评分对话框 -->
  <rating-dialog
    :show="showRatingDialog"
    :target-id="$route.params.taskId"
    target-type="task"
    title="为任务评分"
    @close="showRatingDialog = false"
    @rated="onTaskRated"
  />
</template>

<script>
import axios from "axios";
import StarRatingDisplay from "../Common/StarRatingDisplay.vue";
import StarDisplay from "../Common/StarDisplay.vue";
import RatingDialog from "../Common/RatingDialog.vue";
import RatingsList from "../Common/RatingsList.vue";
import { ElMessage } from 'element-plus';

export default {
  components: {
    StarRatingDisplay,
    StarDisplay,
    RatingDialog,
    RatingsList
  },
  props: {
    role: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      task: {},
      assignmentCount: 0,
      loading: true,
      userRole: '',
      isTaskClaimed: false,
      claimLoading: false,
      studentId: null,
      enterpriseId: null,
      isRetaken: false, // 标记任务是否被重新领取
      taskRating: 0, // 任务评分
      hasCompletedTask: false, // 是否已完成任务
      showRatingDialog: false // 是否显示评分对话框
    };
  },
  computed: {
    techStackArray() {
      if (!this.task.techStack) return [];
      return this.task.techStack.split(',').map(tech => tech.trim()).filter(tech => tech);
    },
    // 添加计算属性来确定是否显示接取按钮
    showClaimButton() {
      return this.userRole === 'student' && !this.isTaskClaimed;
    },
    showClaimedMessage() {
      return this.userRole === 'student' && this.isTaskClaimed;
    },
    // 判断是否为企业用户
    isEnterprise() {
      return this.userRole === 'enterprise';
    }
  },
  created() {
    const taskId = this.$route.params.taskId;
    const userId = localStorage.getItem("userId");

    // 从路由查询参数、props或本地存储中获取用户角色
    const roleFromQuery = this.$route.query.role;
    const roleFromProps = this.role;
    const roleFromStorage = localStorage.getItem('userRole');

    // 确定用户角色
    this.userRole = roleFromQuery || roleFromProps || roleFromStorage || 'student';

    // 根据角色设置相应的ID
    if (this.userRole === 'student') {
      this.studentId = userId;

      // 检查任务是否在重新领取列表中
      const retakenTasks = JSON.parse(localStorage.getItem('retakenTasks') || '[]');
      this.isRetaken = retakenTasks.includes(String(taskId));
    } else if (this.userRole === 'enterprise') {
      this.enterpriseId = userId;
    }

    console.log('初始化任务详情页面:', {
      taskId: taskId,
      userId: userId,
      userRole: this.userRole
    });

    // 加载数据
    const promises = [
      this.fetchTaskDetails(taskId),
      this.fetchTaskAssignmentCount(taskId),
      this.fetchTaskRating(taskId)
    ];

    // 如果是学生，检查是否已接取任务
    if (this.userRole === 'student' && this.studentId) {
      promises.push(this.checkIfTaskClaimed(taskId));
    }

    Promise.all(promises).finally(() => {
      this.loading = false;
    });
  },
  methods: {
    fetchTaskDetails(taskId) {
      return axios
        .get(`http://localhost:8080/api/tasks/${taskId}`)
        .then((response) => {
          const task = response.data;
          console.log('Task data:', task);
          this.task = task;
        })
        .catch((error) => {
          console.error("无法获取任务详情:", error);
        });
    },
    fetchTaskAssignmentCount(taskId) {
      return axios
        .get(`http://localhost:8080/api/tasks/${taskId}/assignment-count`)
        .then(response => {
          this.assignmentCount = response.data;
        })
        .catch(error => {
          console.error("无法获取任务接取人数:", error);
        });
    },
    checkIfTaskClaimed(taskId) {
      if (!this.studentId) {
        console.log('没有学生ID，无法检查任务是否已接取');
        return Promise.resolve();
      }

      console.log('检查任务是否已接取:', {
        studentId: this.studentId,
        taskId: taskId,
        isRetaken: this.isRetaken
      });

      return axios
        .get(`http://localhost:8080/api/students/tasks/${this.studentId}`)
        .then(response => {
          const tasks = response.data;
          console.log('学生已接取的任务:', tasks);

          // 确保比较的是字符串类型
          const taskIdStr = String(taskId);
          this.isTaskClaimed = tasks.some(task => String(task.taskId) === taskIdStr);

          console.log('任务是否已接取:', this.isTaskClaimed);

          // 如果任务已接取，检查是否已完成
          if (this.isTaskClaimed) {
            this.checkTaskCompletion();
          }

          // 如果任务已被重新领取且已接取，获取当前提交状态
          if (this.isRetaken && this.isTaskClaimed) {
            return axios.get(`http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${taskId}`)
              .then(submissionResponse => {
                const submission = submissionResponse.data;
                console.log('重新领取的任务当前提交状态:', submission);

                // 只有当任务状态不是已完成时，才自动跳转到提交页面
                if (this.isRetaken && submission.status !== 'COMPLETED') {
                  console.log('任务已被重新领取且未完成，自动跳转到提交页面');
                  this.$router.push(`/task-submission/${taskId}`);
                } else if (submission.status === 'COMPLETED') {
                  console.log('任务已完成，不自动跳转到提交页面');
                  // 从重新领取列表中移除已完成的任务
                  const retakenTasks = JSON.parse(localStorage.getItem('retakenTasks') || '[]');
                  const updatedTasks = retakenTasks.filter(id => id !== String(taskId));
                  localStorage.setItem('retakenTasks', JSON.stringify(updatedTasks));
                  this.isRetaken = false;
                }
                return Promise.resolve();
              })
              .catch(error => {
                console.error('无法获取任务提交状态:', error);
                return Promise.resolve();
              });
          }

          return Promise.resolve();
        })
        .catch(error => {
          console.error("无法检查任务是否已接取:", error);
          // 出错时默认为未接取，确保按钮显示
          this.isTaskClaimed = false;
          return Promise.resolve();
        });
    },
    claimTask(event) {
      if (!this.studentId) {
        alert("请先登录");
        return;
      }

      // 创建波纹效果
      if (event) {
        const button = event.currentTarget;
        const ripple = button.querySelector('.ripple');

        if (ripple) {
          // 重置动画
          ripple.style.animation = 'none';
          setTimeout(() => {
            ripple.style.animation = '';
          }, 10);

          // 设置波纹位置
          const rect = button.getBoundingClientRect();
          const size = Math.max(rect.width, rect.height) * 2;
          const x = event.clientX - rect.left - size / 2;
          const y = event.clientY - rect.top - size / 2;

          ripple.style.width = `${size}px`;
          ripple.style.height = `${size}px`;
          ripple.style.left = `${x}px`;
          ripple.style.top = `${y}px`;
        }
      }

      const taskId = this.$route.params.taskId;
      this.claimLoading = true;

      // 首先检查学生是否已经完成过该任务
      this.checkIfTaskCompletedBefore(taskId)
        .then(completedSubmission => {
          if (completedSubmission) {
            // 如果已经完成过，使用自动提交API
            return this.autoReclaimCompletedTask(taskId, completedSubmission);
          } else {
            // 如果没有完成过，使用普通接取API
            return axios.post(`http://localhost:8080/api/tasks/${taskId}/assign/${this.studentId}`);
          }
        })
        .then((response) => {
          this.isTaskClaimed = true;
          this.assignmentCount++;
          // 使用ElementPlus的ElMessage
          ElMessage({
            message: '您已成功接取任务，请点击"提交任务"按钮进入提交页面！',
            type: 'success',
            duration: 3000
          });
        })
        .catch(error => {
          console.error("接取任务失败:", error);
          console.error('错误详情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            statusText: error.response?.statusText
          });
          // 使用ElementPlus的ElMessage
          ElMessage({
            message: '接取任务失败，请重试。' + (error.response?.data?.message || error.message),
            type: 'error',
            duration: 3000
          });
        })
        .finally(() => {
          this.claimLoading = false;
        });
    },
    // 检查学生是否已经完成过该任务
    checkIfTaskCompletedBefore(taskId) {
      return axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks`)
        .then(response => {
          const completedTasks = response.data;
          console.log('学生已完成的任务:', completedTasks);

          // 查找该任务的已完成提交记录
          const completedSubmission = completedTasks.find(submission =>
            submission.task && String(submission.task.taskId) === String(taskId) &&
            submission.status === 'COMPLETED'
          );

          console.log('该任务的已完成提交记录:', completedSubmission);
          return completedSubmission;
        })
        .catch(error => {
          console.error('获取已完成任务失败:', error);
          return null;
        });
    },
    // 自动重新提交已完成的任务
    autoReclaimCompletedTask(taskId, completedSubmission) {
      console.log('开始重新接取任务:', {
        taskId: taskId,
        studentId: this.studentId
      });

      // 使用后端的重新接取API
      return axios.post(`http://localhost:8080/api/students/tasks/${taskId}/retake/${this.studentId}`)
        .then(response => {
          console.log('重新接取任务成功:', response.data);

          // 返回重置后的提交记录
          return response.data;
        })
        .catch(error => {
          console.error('重新接取任务失败:', error);
          console.error('错误详情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            statusText: error.response?.statusText
          });
          throw error; // 将错误向上抛出，以便外部捕获
        });
    },
    backToHome() {
      // 根据用户角色返回不同的页面
      if (this.userRole === 'enterprise') {
        this.$router.push({ name: 'enterprise-home' });
      } else {
        this.$router.go(-1);
      }
    },
    viewEnterpriseDetails(enterpriseId) {
      if (!enterpriseId) return;
      this.$router.push({ name: 'EnterpriseDetailForStudent', params: { enterpriseId } });
    },
    goToSubmission() {
      this.$router.push({ name: 'TaskSubmission', params: { taskId: this.$route.params.taskId } });
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },

    // 获取任务评分
    async fetchTaskRating(taskId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/ratings/tasks/${taskId}/average`);
        this.taskRating = response.data.averageRating || 0;
      } catch (error) {
        console.error('获取任务评分失败:', error);
        this.taskRating = 0;
      }
    },

    // 检查学生是否已完成任务
    async checkTaskCompletion() {
      if (!this.studentId || !this.isTaskClaimed) return;

      try {
        const response = await axios.get(`http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.$route.params.taskId}`);
        this.hasCompletedTask = response.data && response.data.status === 'COMPLETED';
      } catch (error) {
        console.error('检查任务完成状态失败:', error);
        this.hasCompletedTask = false;
      }
    },

    // 打开评分对话框
    openRatingDialog() {
      this.showRatingDialog = true;
    },

    // 处理任务评分完成事件
    onTaskRated(ratingData) {
      ElMessage.success('评分成功！');
      // 刷新任务评分
      this.fetchTaskRating(this.$route.params.taskId);
    },

    // 企业特有方法 - 删除任务
    deleteTask() {
      if (!confirm('确定要删除该任务吗？此操作不可恢复。')) {
        return;
      }

      axios.delete(`http://localhost:8080/api/enterprises/${this.enterpriseId}/tasks/${this.task.taskId}`)
        .then(() => {
          ElMessage.success('任务删除成功');
          this.$router.push({ name: 'enterprise-home' });
        })
        .catch(error => {
          console.error('删除任务失败:', error);
          ElMessage.error('删除任务失败，请重试');
        });
    },

    // 企业特有方法 - 查看提交情况
    viewSubmissions() {
      const taskId = this.$route.params.taskId;
      this.$router.push({ name: 'TaskSubmissions', params: { taskId } });
    }
  }
}
</script>

<style scoped>
.task-detail-container {
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

/* 加载中状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

/* 内容区域样式 */
.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

/* 任务标题和基本信息样式 */
.task-header {
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.task-title {
  font-size: 24px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #909399;
  margin-right: 5px;
}

.meta-value {
  color: #606266;
  font-weight: 500;
}

.enterprise-link {
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.enterprise-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 任务详细信息样式 */
.task-info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.tech-stack-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-tag {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.difficulty-rating {
  margin-top: 10px;
}

.task-content, .task-requirements {
  color: #606266;
  line-height: 1.6;
}

/* 操作按钮区域样式 */
.task-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.claim-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
  overflow: hidden;
}

.claim-button:hover {
  background-color: #66b1ff;
}

.claim-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.loading-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-right: 5px;
}

.ripple {
  position: absolute;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

.claimed-buttons {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.rate-button {
  background-color: #e6a23c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.rate-button:hover {
  background-color: #f0b95a;
}

.rating-value {
  display: flex;
  align-items: center;
}

.claimed-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.claimed-message {
  display: flex;
  align-items: center;
  color: #67c23a;
  font-weight: 500;
}

.submit-button {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-button:hover {
  background-color: #85ce61;
}

.plus-icon {
  margin-right: 5px;
  font-weight: bold;
}

.student-actions, .enterprise-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.enterprise-actions {
  display: flex;
  flex-direction: row;
  gap: 15px;
  justify-content: center;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.delete-btn:hover {
  background-color: #f78989;
}

.view-submissions-btn {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.view-submissions-btn:hover {
  background-color: #85ce61;
}

.check-icon {
  margin-right: 8px;
  font-weight: bold;
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

/* 响应式样式 */
@media (max-width: 768px) {
  .task-meta {
    flex-direction: column;
    gap: 10px;
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
