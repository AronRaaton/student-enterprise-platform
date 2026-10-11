<template>
  <div class="student-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">{{ student.name }} - 学生详情</h1>
    </div>

    <!-- 学生信息卡片 -->
    <div class="profile-section">
      <div class="profile-header">
        <div class="student-avatar">
          {{ student.name ? student.name.charAt(0) : 'S' }}
        </div>
        <div class="student-basic-info">
          <h2 class="student-name">{{ student.name }}</h2>
          <p class="student-school">{{ student.school || '未知学校' }} - {{ student.major || '未知专业' }}</p>
        </div>
      </div>

      <div class="info-grid">
        <div class="info-item">
          <div class="info-label">学号</div>
          <div class="info-value">{{ student.studentNumber || student.studentId }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">学校</div>
          <div class="info-value">{{ student.school || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">专业</div>
          <div class="info-value">{{ student.major || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">邮箱</div>
          <div class="info-value">{{ student.email || '未提供' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">入学年份</div>
          <div class="info-value">{{ student.enrollmentYear || '未知' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">完成任务数</div>
          <div class="info-value">{{ completedTasks.length || 0 }}</div>
        </div>
      </div>

      <!-- 简历模块 -->
      <div class="file-module" v-if="student.resumeFile">
        <h3 class="module-title">简历</h3>
        <div class="file-content">
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon pdf-icon">📄</span>
              <span class="file-name">{{ getFileName(student.resumeFile) }}</span>
            </div>
            <div class="file-actions">
              <a :href="`http://localhost:8080/api/files/download/${student.resumeFile}?preview=true`" target="_blank" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </a>
              <a :href="`http://localhost:8080/api/files/download/${student.resumeFile}`" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 成绩单模块 -->
      <div class="file-module" v-if="student.transcriptFile">
        <h3 class="module-title">成绩单</h3>
        <div class="file-content">
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon pdf-icon">📊</span>
              <span class="file-name">{{ getFileName(student.transcriptFile) }}</span>
            </div>
            <div class="file-actions">
              <a :href="`http://localhost:8080/api/files/download/${student.transcriptFile}?preview=true`" target="_blank" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </a>
              <a :href="`http://localhost:8080/api/files/download/${student.transcriptFile}`" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 作品集模块 -->
      <div class="file-module" v-if="hasPortfolioFiles">
        <h3 class="module-title">作品集</h3>
        <div class="file-content">
          <div class="portfolio-preview">
            <p class="portfolio-count">已上传 {{ portfolioFiles.length }} 个文件</p>
            <button @click="showPortfolioModal = true" class="manage-portfolio-btn">
              <span class="manage-icon">🎨</span> 查看作品集
            </button>
          </div>
        </div>
      </div>

      <!-- 企业查看学生详情时显示收藏学生和与学生沟通按钮 -->
      <div class="action-buttons" v-if="showActionButtons">
        <button class="chat-button" @click="goToChatWithStudent">
          <span class="icon-chat">💬</span> 与学生沟通
        </button>
        <button
          class="favorite-button"
          :class="{ 'is-favorited': isFavorited }"
          @click="toggleFavorite"
        >
          <span class="icon-star" v-if="isFavorited">⭐</span>
          <span class="icon-star-outline" v-else>☆</span>
          {{ isFavorited ? '已收藏' : '收藏学生' }}
        </button>
      </div>
    </div>

    <!-- 已完成任务部分 -->
    <div class="completed-tasks-section">
      <h2 class="section-title">已完成任务</h2>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="completedTasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>该学生暂未完成任何任务</p>
      </div>

      <div v-else class="task-list">
        <div v-for="submission in completedTasks" :key="submission.submissionId" class="task-card">
          <div class="task-header">
            <h3 class="task-title">{{ submission.task.summary }}</h3>
            <div class="task-rating">
              <span v-for="star in 5" :key="star" class="star" :class="{ active: submission.rating >= star }">★</span>
              <span class="rating-value" v-if="submission.rating">{{ submission.rating }}/5</span>
            </div>
          </div>

          <div class="task-content">
            <p class="task-description">{{ truncateText(submission.task.content, 150) }}</p>
            <div class="task-meta">
              <span class="task-tech">{{ submission.task.techStack }}</span>
              <span class="task-date">完成时间: {{ formatDate(submission.reviewTime) }}</span>
            </div>
          </div>

          <div class="task-footer">
            <button class="view-details-button active-btn" @click="viewTaskDetails(submission.task.taskId)">查看任务详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 已完成技术挑战部分 -->
    <div class="completed-challenges-section">
      <h2 class="section-title">已完成技术挑战</h2>

      <div v-if="loadingChallenges" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="completedChallenges.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <p>该学生暂未完成任何技术挑战</p>
      </div>

      <div v-else class="challenge-list">
        <div v-for="challenge in completedChallenges" :key="challenge.challengeId" class="challenge-card">
          <div class="challenge-header">
            <h3 class="challenge-title">{{ challenge.title }}</h3>
            <div class="challenge-rating">
              <span v-for="star in 5" :key="star" class="star" :class="{ active: challenge.rating >= star }">★</span>
              <span class="rating-value" v-if="challenge.rating">{{ challenge.rating }}/5</span>
            </div>
          </div>

          <div class="challenge-content">
            <p class="challenge-description">{{ truncateText(challenge.description, 150) }}</p>
            <div class="challenge-meta">
              <span class="challenge-tech">{{ challenge.techStack }}</span>
              <span class="challenge-date">完成时间: {{ formatDate(challenge.completionDate) }}</span>
            </div>
          </div>

          <div class="challenge-footer">
            <button class="view-details-button active-btn" @click="viewChallengeDetails(challenge.challengeId)">查看挑战详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 作品集查看模态框 -->
    <div v-if="showPortfolioModal" class="portfolio-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ student.name }} 的作品集</h2>
          <button class="close-button" @click="showPortfolioModal = false">×</button>
        </div>

        <div class="modal-body">
          <div v-if="portfolioFiles.length === 0" class="empty-state">
            <p>暂无作品集文件</p>
          </div>

          <div v-else class="portfolio-files">
            <div v-for="(file, index) in portfolioFiles" :key="index" class="portfolio-item">
              <div class="file-info">
                <span class="file-type-icon">{{ getFileTypeIcon(file) }}</span>
                <span class="file-name">{{ getFileName(file) }}</span>
              </div>

              <div class="file-actions">
                <a :href="`http://localhost:8080/api/files/download/${file}`" class="action-btn download-btn">
                  <span class="action-icon">⬇️</span> 下载
                </a>
                <a v-if="isViewableFile(file)" :href="`http://localhost:8080/api/files/download/${file}?preview=true`" target="_blank" class="action-btn view-btn">
                  <span class="action-icon">🔍</span> 查看
                </a>
              </div>
            </div>
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
      student: {},
      isFavorited: false,
      enterpriseId: null,
      completedTasks: [],
      completedChallenges: [],
      loading: true,
      loadingChallenges: true,
      userRole: localStorage.getItem('userRole') || '',
      isCurrentStudent: false,
      portfolioFiles: [],
      showPortfolioModal: false,
      studentId: null,
    };
  },
  computed: {
    showActionButtons() {
      // 企业查看学生详情时显示按钮
      return this.userRole === 'enterprise';
    },
    hasPortfolioFiles() {
      return this.portfolioFiles && this.portfolioFiles.length > 0;
    }
  },
  created() {
    const studentId = this.$route.params.studentId;
    this.studentId = studentId;
    this.enterpriseId = localStorage.getItem('userId');
    this.userRole = localStorage.getItem('userRole') || '';

    // 检查是否为当前学生
    this.checkIfCurrentStudent();

    // 并行加载数据
    Promise.all([
      this.fetchStudentDetails(studentId),
      this.checkIfFavorited(studentId),
      this.fetchCompletedTasks(studentId)
    ]).then(() => {
      // 获取作品集文件
      this.fetchPortfolioFiles();
    }).finally(() => {
      this.loading = false;
    });

    // 加载完成的技术挑战
    this.fetchCompletedChallenges(studentId);
  },
  methods: {
    fetchStudentDetails(studentId) {
      return axios.get(`http://localhost:8080/api/login/student/${studentId}`)
        .then(response => {
          this.student = response.data;
        })
        .catch(error => {
          console.error('无法获取学生信息:', error);
        });
    },

    fetchCompletedTasks(studentId) {
      return axios.get(`http://localhost:8080/api/students/tasks/completed/${studentId}`)
        .then(response => {
          this.completedTasks = response.data;
        })
        .catch(error => {
          console.error('无法获取已完成任务:', error);
          this.completedTasks = [];
        });
    },

    fetchCompletedChallenges(studentId) {
      return axios.get(`http://localhost:8080/api/challenges/student/${studentId}/completed`)
        .then(response => {
          this.completedChallenges = response.data;
          this.loadingChallenges = false;
        })
        .catch(error => {
          console.error('无法获取已完成技术挑战:', error);
          this.completedChallenges = [];
          this.loadingChallenges = false;
        });
    },
    goToChatWithStudent() {
      // 跳转到聊天页面
      console.log('跳转到聊天页面，学生ID:', this.student.studentId);
      this.$router.push({
        path: `/chat/${this.student.studentId}`,
        name: 'ChatE2S',
        params: { studentId: this.student.studentId }
      });
    },

    goBack() {
      this.$router.go(-1);
    },

    viewTaskDetails(taskId) {
      this.$router.push({ name: 'TaskDetailForEnter', params: { taskId } });
    },

    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },


    // 检查学生是否被收藏
    checkIfFavorited(studentId) {
      if (!this.enterpriseId) return;

      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/favorites`)
        .then(response => {
          const favorites = response.data;
          this.isFavorited = favorites.some(student => student.studentId == studentId);
        })
        .catch(error => {
          console.error('无法获取收藏学生列表:', error);
        });
    },

    // 收藏/取消收藏学生
    toggleFavorite() {
      if (!this.enterpriseId) {
        alert('请先登录企业账号');
        return;
      }

      const url = `http://localhost:8080/api/enterprises/${this.enterpriseId}/favorites/${this.student.studentId}`;

      if (this.isFavorited) {
        // 取消收藏
        axios.delete(url)
          .then(() => {
            this.isFavorited = false;
            alert('已取消收藏学生');
          })
          .catch(error => {
            console.error('取消收藏失败:', error);
            alert('取消收藏失败，请重试');
          });
      } else {
        // 添加收藏
        axios.post(url)
          .then(() => {
            this.isFavorited = true;
            alert('已成功收藏学生');
          })
          .catch(error => {
            console.error('收藏学生失败:', error);
            alert('收藏学生失败，请重试');
          });
      }
    },

    // 查看技术挑战详情
    viewChallengeDetails(challengeId) {
      this.$router.push({ name: 'TechnicalChallengeDetail', params: { challengeId } });
    },

    // 截断文本
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    // 检查是否为当前学生
    checkIfCurrentStudent() {
      const currentUserId = localStorage.getItem('userId');
      this.isCurrentStudent = currentUserId === this.studentId.toString() && this.userRole === 'student';
    },

    // 获取学生的作品集文件
    fetchPortfolioFiles() {
      if (this.student.portfolioFiles) {
        this.portfolioFiles = this.student.portfolioFiles.split(',');
      } else {
        this.portfolioFiles = [];
      }
    },

    // 上传简历
    uploadResume(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 检查文件类型
      if (file.type !== 'application/pdf') {
        alert('简历只能上传PDF格式文件');
        return;
      }

      const formData = new FormData();
      formData.append('file', file);
      formData.append('studentId', this.studentId);

      axios.post('http://localhost:8080/api/files/student/resume', formData)
        .then(response => {
          this.student.resumeFile = response.data.fileName;
          alert('简历上传成功');
        })
        .catch(error => {
          console.error('简历上传失败:', error);
          alert('简历上传失败，请重试');
        });
    },

    // 上传成绩单
    uploadTranscript(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 检查文件类型
      if (file.type !== 'application/pdf') {
        alert('成绩单只能上传PDF格式文件');
        return;
      }

      const formData = new FormData();
      formData.append('file', file);
      formData.append('studentId', this.studentId);

      axios.post('http://localhost:8080/api/files/student/transcript', formData)
        .then(response => {
          this.student.transcriptFile = response.data.fileName;
          alert('成绩单上传成功');
        })
        .catch(error => {
          console.error('成绩单上传失败:', error);
          alert('成绩单上传失败，请重试');
        });
    },

    // 上传作品集
    uploadPortfolio(event) {
      const file = event.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);
      formData.append('studentId', this.studentId);

      axios.post('http://localhost:8080/api/files/student/portfolio', formData)
        .then(response => {
          // 更新作品集文件列表
          this.portfolioFiles.push(response.data.fileName);

          // 更新学生信息
          if (!this.student.portfolioFiles) {
            this.student.portfolioFiles = response.data.fileName;
          } else {
            this.student.portfolioFiles += ',' + response.data.fileName;
          }

          alert('作品集上传成功');
        })
        .catch(error => {
          console.error('作品集上传失败:', error);
          alert('作品集上传失败，请重试');
        });
    },

    // 删除作品集文件
    deletePortfolioFile(fileName, index) {
      if (confirm('确定要删除该文件吗？')) {
        axios.delete(`http://localhost:8080/api/files/${fileName}`, {
          params: {
            studentId: this.studentId,
            fileType: 'portfolio'
          }
        })
        .then(() => {
          // 从列表中移除文件
          this.portfolioFiles.splice(index, 1);

          // 更新学生信息
          this.student.portfolioFiles = this.portfolioFiles.join(',');

          alert('文件删除成功');
        })
        .catch(error => {
          console.error('文件删除失败:', error);
          alert('文件删除失败，请重试');
        });
      }
    },

    // 获取文件名
    getFileName(fileName) {
      // 去除UUID前缀，只显示原始文件名
      const parts = fileName.split('-');
      if (parts.length > 1) {
        return parts.slice(1).join('-');
      }
      return fileName;
    },

    // 获取文件类型图标
    getFileTypeIcon(fileName) {
      const extension = fileName.split('.').pop().toLowerCase();

      switch (extension) {
        case 'pdf': return '📄';
        case 'doc': case 'docx': return '📝';
        case 'xls': case 'xlsx': return '📃';
        case 'ppt': case 'pptx': return '📄';
        case 'jpg': case 'jpeg': case 'png': case 'gif': return '🖼';
        case 'zip': case 'rar': case '7z': return '📦';
        case 'txt': return '📄';
        default: return '📁';
      }
    },

    // 获取文件类型类名
    getFileTypeClass(fileName) {
      const extension = fileName.split('.').pop().toLowerCase();

      switch (extension) {
        case 'pdf': return 'file-pdf';
        case 'doc': case 'docx': return 'file-word';
        case 'xls': case 'xlsx': return 'file-excel';
        case 'ppt': case 'pptx': return 'file-powerpoint';
        case 'jpg': case 'jpeg': case 'png': case 'gif': return 'file-image';
        case 'zip': case 'rar': case '7z': return 'file-archive';
        case 'txt': return 'file-text';
        default: return 'file-other';
      }
    },

    // 检查文件是否可以直接在浏览器中查看
    isViewableFile(fileName) {
      const extension = fileName.split('.').pop().toLowerCase();
      const viewableExtensions = ['pdf', 'jpg', 'jpeg', 'png', 'gif'];
      return viewableExtensions.includes(extension);
    },
  },
};
</script>

<style scoped>
.student-detail-container {
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

/* 个人资料区域样式 */
.profile-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.student-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  margin-right: 20px;
}

.student-basic-info {
  flex: 1;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #303133;
}

.student-school {
  margin: 0;
  font-size: 16px;
  color: #606266;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.info-item {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.info-value {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

button {
  padding: 10px 20px;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-button {
  background-color: #409EFF;
}

.chat-button:hover {
  background-color: #66b1ff;
}

.favorite-button {
  background-color: #f59e0b;
}

.favorite-button:hover {
  background-color: #d97706;
}

.favorite-button.is-favorited {
  background-color: #10b981;
}

.favorite-button.is-favorited:hover {
  background-color: #059669;
}

.icon-chat, .icon-star, .icon-star-outline {
  margin-right: 5px;
  font-size: 18px;
}

/* 已完成任务区域样式 */
.completed-tasks-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
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
  padding: 30px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-state p {
  color: #909399;
  margin-bottom: 0;
}

.task-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.task-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px;
  background-color: #f0f9eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.task-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.task-rating {
  display: flex;
  align-items: center;
}

.star {
  font-size: 16px;
  color: #dcdfe6;
  margin-right: 2px;
}

.star.active {
  color: #e6a23c;
}

.rating-value {
  margin-left: 5px;
  font-size: 14px;
  color: #606266;
}

.task-content {
  padding: 15px;
}

.task-description {
  margin-top: 0;
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.task-tech, .task-date {
  font-size: 12px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.task-footer {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.view-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-button:hover {
  background-color: #66b1ff;
}

/* 响应式样式 */
@media (max-width: 768px) {
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

  .profile-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-avatar {
    margin-bottom: 15px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .task-list {
    grid-template-columns: 1fr;
  }
}

/* 技术挑战部分样式 */
.completed-challenges-section {
  margin-top: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.challenge-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.challenge-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.challenge-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.challenge-header {
  padding: 15px;
  background-color: #ecf5ff;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.challenge-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
}

.challenge-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.challenge-content {
  padding: 15px;
  flex-grow: 1;
}

.challenge-description {
  color: #606266;
  margin-bottom: 15px;
  line-height: 1.5;
}

.challenge-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  font-size: 14px;
  color: #909399;
}

.challenge-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.challenge-links {
  display: flex;
  gap: 10px;
}

.solution-link, .file-link {
  color: #409EFF;
  text-decoration: none;
  font-size: 14px;
}

.solution-link:hover, .file-link:hover {
  text-decoration: underline;
}

.view-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 5px 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

/* 学生文件相关样式 */
.student-files {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.files-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.file-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.file-link {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.file-link:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.file-icon {
  margin-right: 5px;
  font-size: 16px;
}

.resume-link {
  background-color: #f0f9eb;
  color: #67c23a;
  border-color: #e1f3d8;
}

.resume-link:hover {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.transcript-link {
  background-color: #fdf6ec;
  color: #e6a23c;
  border-color: #faecd8;
}

.transcript-link:hover {
  background-color: #e6a23c;
  color: white;
  border-color: #e6a23c;
}

.portfolio-link {
  background-color: #f2f6fc;
  color: #909399;
  border-color: #ebeef5;
}

.portfolio-link:hover {
  background-color: #909399;
  color: white;
  border-color: #909399;
}

/* 文件上传相关样式 */
.file-upload-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

/* 文件模块样式 */
.file-module {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.module-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.file-content {
  padding: 5px;
}

.file-preview {
  background-color: #f9f9f9;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  width: 100%;
}

.file-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.pdf-icon {
  color: #e74c3c;
}

.file-name {
  font-size: 15px;
  color: #333;
  word-break: break-all;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-start;
  align-items: center;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 6px 10px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
  width: 80px;
  height: 36px;
  box-sizing: border-box;
  font-weight: 500;
}

.portfolio-preview {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.portfolio-count {
  margin-bottom: 15px;
  color: #606266;
}

.manage-portfolio-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px 16px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.manage-portfolio-btn:hover {
  background-color: #66b1ff;
}

.view-details-button.active-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-details-button.active-btn:hover {
  background-color: #66b1ff;
}

.upload-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 15px;
}

.upload-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.upload-item {
  position: relative;
  display: flex;
  align-items: center;
}

.upload-label {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-label:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.upload-icon {
  margin-right: 5px;
  font-size: 16px;
}

.file-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
}

.file-status {
  margin-left: 10px;
  font-size: 14px;
  color: #67c23a;
}

/* 作品集模态框样式 */
.portfolio-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}

.empty-portfolio {
  text-align: center;
  padding: 30px 0;
  color: #909399;
}

.portfolio-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.portfolio-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
  box-sizing: border-box;
  margin-bottom: 10px;
}

.portfolio-item:hover {
  background-color: #f0f2f5;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

.file-info {
  display: flex;
  align-items: center;
  width: 50%;
  min-width: 0;
  height: 32px;
  margin-bottom: 0;
}

.file-type-icon {
  margin-right: 10px;
  font-size: 20px;
}

.file-name {
  font-size: 14px;
  color: #303133;
}

.file-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  width: 50%;
  justify-content: flex-end;
  align-items: center;
}



/* 文件类型颜色 */
.file-pdf { color: #f56c6c; }
.file-word { color: #409EFF; }
.file-excel { color: #67c23a; }
.file-powerpoint { color: #e6a23c; }
.file-image { color: #909399; }
.file-archive { color: #909399; }
.file-text { color: #909399; }
.file-other { color: #909399; }
</style>
