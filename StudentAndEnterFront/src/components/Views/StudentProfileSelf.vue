<template>
  <div class="student-detail-container">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">我的个人资料</h1>
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
          <div class="info-value">{{ completedTasks.length }}</div>
        </div>
      </div>

      <!-- 简历模块 -->
      <div class="file-module">
        <h3 class="module-title">我的简历</h3>
        <div class="file-content">
          <div v-if="student.resumeFile" class="file-preview">
            <div class="file-info">
              <span class="file-icon pdf-icon">📄</span>

              <!-- 非编辑状态显示名称 -->
              <div v-if="!editingResumeName" class="file-name-container">
                <span class="file-name">{{ resumeCustomName }}</span>
                <button @click="startEditResumeName" class="edit-name-btn">
                  <span class="edit-icon">✏️</span>
                </button>
              </div>

              <!-- 编辑状态显示输入框 -->
              <div v-else class="file-name-edit">
                <input
                  type="text"
                  v-model="resumeCustomName"
                  class="name-input"
                  @keyup.enter="saveResumeName"
                  @keyup.esc="cancelEditResumeName"
                  @blur="saveResumeName"
                  ref="resumeNameInput"
                  placeholder="输入简历名称，回车保存"
                />
              </div>
            </div>
            <div class="file-actions">
              <a :href="`http://localhost:8080/api/files/download/${student.resumeFile}?preview=true`" target="_blank" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </a>
              <a :href="`http://localhost:8080/api/files/download/${student.resumeFile}`" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </a>
              <button @click="deleteResume" class="action-btn delete-btn">
                <span class="action-icon">🗑️</span> 删除
              </button>
            </div>
          </div>
          <div v-else class="file-upload-area">
            <p class="upload-message">您还没有上传简历</p>
            <label for="resume-upload" class="upload-btn">
              <span class="upload-icon">📁</span> 上传简历 (PDF格式)
            </label>
          </div>
          <input type="file" id="resume-upload" @change="uploadResume" accept="application/pdf" class="file-input" />
        </div>
      </div>

      <!-- 成绩单模块 -->
      <div class="file-module">
        <h3 class="module-title">我的成绩单</h3>
        <div class="file-content">
          <div v-if="student.transcriptFile" class="file-preview">
            <div class="file-info">
              <span class="file-icon pdf-icon">📊</span>

              <!-- 非编辑状态显示名称 -->
              <div v-if="!editingTranscriptName" class="file-name-container">
                <span class="file-name">{{ transcriptCustomName }}</span>
                <button @click="startEditTranscriptName" class="edit-name-btn">
                  <span class="edit-icon">✏️</span>
                </button>
              </div>

              <!-- 编辑状态显示输入框 -->
              <div v-else class="file-name-edit">
                <input
                  type="text"
                  v-model="transcriptCustomName"
                  class="name-input"
                  @keyup.enter="saveTranscriptName"
                  @keyup.esc="cancelEditTranscriptName"
                  @blur="saveTranscriptName"
                  ref="transcriptNameInput"
                  placeholder="输入成绩单名称，回车保存"
                />
              </div>
            </div>
            <div class="file-actions">
              <a :href="`http://localhost:8080/api/files/download/${student.transcriptFile}?preview=true`" target="_blank" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </a>
              <a :href="`http://localhost:8080/api/files/download/${student.transcriptFile}`" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </a>
              <button @click="deleteTranscript" class="action-btn delete-btn">
                <span class="action-icon">🗑️</span> 删除
              </button>
            </div>
          </div>
          <div v-else class="file-upload-area">
            <p class="upload-message">您还没有上传成绩单</p>
            <label for="transcript-upload" class="upload-btn">
              <span class="upload-icon">📁</span> 上传成绩单 (PDF格式)
            </label>
          </div>
          <input type="file" id="transcript-upload" @change="uploadTranscript" accept="application/pdf" class="file-input" />
        </div>
      </div>

      <!-- 作品集模块 -->
      <div class="file-module">
        <h3 class="module-title">我的作品集</h3>
        <div class="file-content">
          <div v-if="portfolioFiles.length > 0" class="portfolio-preview">
            <p class="portfolio-count">已上传 {{ portfolioFiles.length }} 个文件</p>

            <!-- 作品集列表显示 -->
            <div class="portfolio-list-preview">
              <div v-for="(item, index) in portfolioItems.slice(0, 5)" :key="index" class="portfolio-item-preview">
                <!-- 左侧显示文件名称 -->
                <div class="file-info-preview">
                  <span class="file-type-icon" :class="getFileTypeClass(item.fileName)">
                    {{ getFileTypeIcon(item.fileName) }}
                  </span>
                  <span class="file-name-preview">{{ item.customName }}</span>
                </div>

                <!-- 右侧显示操作按钮 -->
                <div class="file-actions-preview">
                  <a :href="`http://localhost:8080/api/files/download/${item.fileName}`" class="action-btn-small download-btn">
                    <span class="action-icon">⬇️</span>
                  </a>
                  <a v-if="isViewableFile(item.fileName)" :href="`http://localhost:8080/api/files/download/${item.fileName}?preview=true`" target="_blank" class="action-btn-small view-btn">
                    <span class="action-icon">🔍</span>
                  </a>
                  <button @click="startEditPortfolioName(portfolioItems.indexOf(item))" class="action-btn-small edit-btn">
                    <span class="action-icon">✏️</span>
                  </button>
                </div>
              </div>

              <!-- 显示更多文件的提示 -->
              <div v-if="portfolioFiles.length > 5" class="more-files-preview">
                还有 {{ portfolioFiles.length - 5 }} 个文件未显示
              </div>
            </div>

            <button @click="showPortfolioModal = true" class="manage-portfolio-btn">
              <span class="manage-icon">🎨</span> 管理作品集
            </button>
          </div>
          <div v-else class="file-upload-area">
            <p class="upload-message">您还没有上传作品集文件</p>
            <button @click="showPortfolioModal = true" class="upload-btn">
              <span class="upload-icon">📁</span> 上传作品集
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 已完成任务列表 -->
    <div class="completed-tasks-section">
      <h2 class="section-title">已完成任务</h2>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="completedTasks.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>您还没有完成任何任务</p>
      </div>

      <div v-else class="tasks-grid">
        <div v-for="submission in completedTasks" :key="submission.submissionId" class="task-card">
          <div class="task-header">
            <h3 class="task-title">{{ submission.task.summary }}</h3>
            <span class="task-date">{{ formatDate(submission.submissionTime) }}</span>
          </div>
          <div class="task-content">
            <p>{{ truncateText(submission.task.content, 100) }}</p>
          </div>
          <div class="task-footer">
            <button class="view-button" @click="viewTaskDetails(submission.task.taskId)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 已完成技术挑战列表 -->
    <div class="completed-challenges-section">
      <h2 class="section-title">已完成技术挑战</h2>

      <div v-if="loadingChallenges" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="completedChallenges.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <p>您还没有完成任何技术挑战</p>
      </div>

      <div v-else class="challenges-grid">
        <div v-for="submission in completedChallenges" :key="submission.submissionId" class="challenge-card">
          <div class="challenge-header">
            <h3 class="challenge-title">{{ submission.challenge.title }}</h3>
            <span class="challenge-date">{{ formatDate(submission.submissionTime) }}</span>
          </div>
          <div class="challenge-content">
            <p>{{ truncateText(submission.challenge.description, 100) }}</p>
          </div>
          <div class="challenge-footer">
            <div class="challenge-links">
              <a v-if="submission.solutionUrl" :href="submission.solutionUrl" target="_blank" class="solution-link">解决方案链接</a>
              <a v-if="submission.uploadedFile" :href="`http://localhost:8080/api/files/download/${submission.uploadedFile}`" class="file-link">下载文件</a>
            </div>
            <button class="view-btn" @click="viewChallengeDetails(submission.challengeId)">查看挑战详情</button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 作品集模态框 -->
  <div v-if="showPortfolioModal" class="portfolio-modal">
    <div class="modal-content">
      <div class="modal-header">
        <h2>作品集</h2>
        <button class="close-button" @click="showPortfolioModal = false">&times;</button>
      </div>
      <div class="modal-body">
        <div v-if="portfolioFiles.length === 0" class="empty-portfolio">
          <p>暂无作品集文件</p>
        </div>
        <div v-else class="portfolio-list">
          <div v-for="(item, index) in portfolioItems" :key="index" class="portfolio-item">
            <!-- 左侧显示文件名称 -->
            <div class="file-info">
              <span class="file-type-icon" :class="getFileTypeClass(item.fileName)">
                {{ getFileTypeIcon(item.fileName) }}
              </span>

              <!-- 非编辑状态显示名称 -->
              <div v-if="editingPortfolioIndex !== index" class="file-name-container">
                <span class="file-name">{{ item.customName }}</span>
                <button @click="startEditPortfolioName(index)" class="edit-name-btn">
                  <span class="edit-icon">✏️</span>
                </button>
              </div>

              <!-- 编辑状态显示输入框 -->
              <div v-else class="file-name-edit">
                <input
                  type="text"
                  v-model="editingPortfolioName"
                  class="name-input"
                  @keyup.enter="savePortfolioName"
                  @keyup.esc="cancelEditPortfolioName"
                  @blur="savePortfolioName"
                  ref="nameInput"
                  placeholder="输入作品名称，回车保存"
                />
              </div>
            </div>

            <!-- 右侧显示操作按钮 -->
            <div class="file-actions">
              <a :href="`http://localhost:8080/api/files/download/${item.fileName}`" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </a>
              <a v-if="isViewableFile(item.fileName)" :href="`http://localhost:8080/api/files/download/${item.fileName}?preview=true`" target="_blank" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </a>
              <button @click="deletePortfolioFile(item.fileName, index)" class="action-btn delete-btn">
                <span class="action-icon">🗑️</span> 删除
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <div class="modal-footer-left">
          <label for="portfolio-upload-modal" class="upload-btn">
            <span class="upload-icon">📁</span> 上传新文件
          </label>
          <input type="file" id="portfolio-upload-modal" @change="uploadPortfolio" class="file-input" />
        </div>
        <div class="modal-footer-right" v-if="editingPortfolioIndex >= 0">
          <button @click="deletePortfolioFile(portfolioItems[editingPortfolioIndex].fileName, editingPortfolioIndex)" class="delete-btn">
            <span class="action-icon">🗑️</span> 删除当前文件
          </button>
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
      completedTasks: [],
      completedChallenges: [],
      loading: true,
      loadingChallenges: true,
      portfolioFiles: [],
      portfolioItems: [], // 存储作品集文件信息，包括文件名和自定义名称
      showPortfolioModal: false,
      studentId: null,
      editingPortfolioIndex: -1, // 当前正在编辑的作品集索引
      editingPortfolioName: '', // 编辑中的作品集名称
      resumeCustomName: '', // 简历自定义名称
      transcriptCustomName: '', // 成绩单自定义名称
      editingResumeName: false, // 是否正在编辑简历名称
      editingTranscriptName: false, // 是否正在编辑成绩单名称
    };
  },
  created() {
    const studentId = this.$route.params.studentId;
    this.studentId = studentId;

    // 并行加载数据
    Promise.all([
      this.fetchStudentDetails(studentId),
      this.fetchCompletedTasks(studentId)
    ]).then(() => {
      // 获取作品集文件
      this.fetchPortfolioFiles();
      // 加载简历和成绩单的自定义名称
      this.loadCustomFileNames();
    }).finally(() => {
      this.loading = false;
    });

    // 加载完成的技术挑战
    this.fetchCompletedChallenges(studentId);
  },

  // 监听编辑状态变化，自动聚焦输入框
  watch: {
    editingPortfolioIndex(newVal) {
      if (newVal !== -1) {
        // 使用 nextTick 确保 DOM 已经更新
        this.$nextTick(() => {
          if (this.$refs.nameInput) {
            this.$refs.nameInput.focus();
          }
        });
      }
    }
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

    // 查看技术挑战详情
    viewChallengeDetails(challengeId) {
      this.$router.push({ name: 'TechnicalChallengeDetail', params: { challengeId } });
    },

    // 截断文本
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    // 获取学生的作品集文件
    fetchPortfolioFiles() {
      if (this.student.portfolioFiles) {
        this.portfolioFiles = this.student.portfolioFiles.split(',');

        // 初始化作品集项目
        this.portfolioItems = this.portfolioFiles.map(fileName => {
          // 尝试从 localStorage 获取自定义名称
          const customName = localStorage.getItem(`portfolio_${this.studentId}_${fileName}`);

          return {
            fileName: fileName,
            customName: customName || this.getFileName(fileName) // 如果没有自定义名称，使用原始文件名
          };
        });
      } else {
        this.portfolioFiles = [];
        this.portfolioItems = [];
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
          const fileName = response.data.fileName;
          const originalName = this.getFileName(fileName);

          // 设置简历文件名
          this.student.resumeFile = fileName;
          this.resumeCustomName = originalName;

          // 将自定义名称保存到 localStorage
          localStorage.setItem(`resume_${this.studentId}_${fileName}`, originalName);

          // 提示用户编辑名称
          this.editingResumeName = true;

          alert('简历上传成功，您可以编辑简历名称');
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
          const fileName = response.data.fileName;
          const originalName = this.getFileName(fileName);

          // 设置成绩单文件名
          this.student.transcriptFile = fileName;
          this.transcriptCustomName = originalName;

          // 将自定义名称保存到 localStorage
          localStorage.setItem(`transcript_${this.studentId}_${fileName}`, originalName);

          // 提示用户编辑名称
          this.editingTranscriptName = true;

          alert('成绩单上传成功，您可以编辑成绩单名称');
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
          const fileName = response.data.fileName;
          const originalName = this.getFileName(fileName);

          // 更新作品集文件列表
          this.portfolioFiles.push(fileName);

          // 添加到作品集项目中
          this.portfolioItems.push({
            fileName: fileName,
            customName: originalName
          });

          // 更新学生信息
          if (!this.student.portfolioFiles) {
            this.student.portfolioFiles = fileName;
          } else {
            this.student.portfolioFiles += ',' + fileName;
          }

          // 提示用户编辑名称
          this.editingPortfolioIndex = this.portfolioItems.length - 1;
          this.editingPortfolioName = originalName;

          alert('作品集上传成功，您可以编辑作品名称');
        })
        .catch(error => {
          console.error('作品集上传失败:', error);
          alert('作品集上传失败，请重试');
        });
    },

    // 删除简历
    deleteResume() {
      if (confirm('确定要删除您的简历吗？')) {
        const resumeFile = this.student.resumeFile;
        axios.delete(`http://localhost:8080/api/files/${resumeFile}`, {
          params: {
            studentId: this.studentId,
            fileType: 'resume'
          }
        })
        .then(() => {
          // 清除简历文件字段
          this.student.resumeFile = null;

          // 从 localStorage 中删除自定义名称
          localStorage.removeItem(`resume_${this.studentId}_${resumeFile}`);
          this.resumeCustomName = '';
          this.editingResumeName = false;

          alert('简历删除成功');
        })
        .catch(error => {
          console.error('简历删除失败:', error);
          alert('简历删除失败，请重试');
        });
      }
    },

    // 删除成绩单
    deleteTranscript() {
      if (confirm('确定要删除您的成绩单吗？')) {
        const transcriptFile = this.student.transcriptFile;
        axios.delete(`http://localhost:8080/api/files/${transcriptFile}`, {
          params: {
            studentId: this.studentId,
            fileType: 'transcript'
          }
        })
        .then(() => {
          // 清除成绩单文件字段
          this.student.transcriptFile = null;

          // 从 localStorage 中删除自定义名称
          localStorage.removeItem(`transcript_${this.studentId}_${transcriptFile}`);
          this.transcriptCustomName = '';
          this.editingTranscriptName = false;

          alert('成绩单删除成功');
        })
        .catch(error => {
          console.error('成绩单删除失败:', error);
          alert('成绩单删除失败，请重试');
        });
      }
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
          this.portfolioItems.splice(index, 1);

          // 更新学生信息
          this.student.portfolioFiles = this.portfolioFiles.join(',');

          // 从 localStorage 中删除自定义名称
          localStorage.removeItem(`portfolio_${this.studentId}_${fileName}`);

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
        case 'doc': case 'docx': return 'file-doc';
        case 'xls': case 'xlsx': return 'file-excel';
        case 'ppt': case 'pptx': return 'file-ppt';
        case 'jpg': case 'jpeg': case 'png': case 'gif': return 'file-image';
        case 'zip': case 'rar': case '7z': return 'file-archive';
        case 'txt': return 'file-text';
        default: return 'file-generic';
      }
    },

    // 判断文件是否可以在浏览器中查看
    isViewableFile(fileName) {
      const extension = fileName.split('.').pop().toLowerCase();
      const viewableExtensions = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'txt'];
      return viewableExtensions.includes(extension);
    },

    // 开始编辑作品名称
    startEditPortfolioName(index) {
      this.editingPortfolioIndex = index;
      this.editingPortfolioName = this.portfolioItems[index].customName;
    },

    // 保存作品名称
    savePortfolioName() {
      if (this.editingPortfolioIndex >= 0) {
        // 更新作品名称
        const item = this.portfolioItems[this.editingPortfolioIndex];
        item.customName = this.editingPortfolioName || this.getFileName(item.fileName);

        // 将自定义名称保存到 localStorage
        localStorage.setItem(`portfolio_${this.studentId}_${item.fileName}`, item.customName);

        // 重置编辑状态
        this.editingPortfolioIndex = -1;
        this.editingPortfolioName = '';
      }
    },

    // 取消编辑作品名称
    cancelEditPortfolioName() {
      this.editingPortfolioIndex = -1;
      this.editingPortfolioName = '';
    },

    // 加载简历和成绩单的自定义名称
    loadCustomFileNames() {
      if (this.student.resumeFile) {
        const resumeName = localStorage.getItem(`resume_${this.studentId}_${this.student.resumeFile}`);
        this.resumeCustomName = resumeName || this.getFileName(this.student.resumeFile);
      }

      if (this.student.transcriptFile) {
        const transcriptName = localStorage.getItem(`transcript_${this.studentId}_${this.student.transcriptFile}`);
        this.transcriptCustomName = transcriptName || this.getFileName(this.student.transcriptFile);
      }
    },

    // 开始编辑简历名称
    startEditResumeName() {
      this.editingResumeName = true;
      this.$nextTick(() => {
        if (this.$refs.resumeNameInput) {
          this.$refs.resumeNameInput.focus();
        }
      });
    },

    // 保存简历名称
    saveResumeName() {
      if (this.resumeCustomName.trim() === '') {
        this.resumeCustomName = this.getFileName(this.student.resumeFile);
      }
      localStorage.setItem(`resume_${this.studentId}_${this.student.resumeFile}`, this.resumeCustomName);
      this.editingResumeName = false;
    },

    // 取消编辑简历名称
    cancelEditResumeName() {
      this.resumeCustomName = localStorage.getItem(`resume_${this.studentId}_${this.student.resumeFile}`) || this.getFileName(this.student.resumeFile);
      this.editingResumeName = false;
    },

    // 开始编辑成绩单名称
    startEditTranscriptName() {
      this.editingTranscriptName = true;
      this.$nextTick(() => {
        if (this.$refs.transcriptNameInput) {
          this.$refs.transcriptNameInput.focus();
        }
      });
    },

    // 保存成绩单名称
    saveTranscriptName() {
      if (this.transcriptCustomName.trim() === '') {
        this.transcriptCustomName = this.getFileName(this.student.transcriptFile);
      }
      localStorage.setItem(`transcript_${this.studentId}_${this.student.transcriptFile}`, this.transcriptCustomName);
      this.editingTranscriptName = false;
    },

    // 取消编辑成绩单名称
    cancelEditTranscriptName() {
      this.transcriptCustomName = localStorage.getItem(`transcript_${this.studentId}_${this.student.transcriptFile}`) || this.getFileName(this.student.transcriptFile);
      this.editingTranscriptName = false;
    }
  }
};
</script>

<style scoped>
.student-detail-container {
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

.student-avatar {
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

.student-basic-info {
  flex: 1;
}

.student-name {
  font-size: 24px;
  margin: 0 0 5px 0;
}

.student-school {
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

/* 文件模块样式 */
.file-module {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.module-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.file-content {
  min-height: 100px;
}

/* 文件预览区域 */
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
  gap: 5px;
  padding: 8px 12px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
}

.view-btn {
  background-color: #e3f2fd;
  color: #1976d2;
}

.download-btn {
  background-color: #e8f5e9;
  color: #388e3c;
}

.delete-btn {
  background-color: #ffebee;
  color: #d32f2f;
}

.view-btn:hover {
  background-color: #bbdefb;
}

.download-btn:hover {
  background-color: #c8e6c9;
}

.delete-btn:hover {
  background-color: #ffcdd2;
}

.action-icon {
  font-size: 16px;
}

/* 文件上传区域 */
.file-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 6px;
  border: 2px dashed #ddd;
}

.upload-message {
  margin-bottom: 15px;
  color: #666;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 10px 15px;
  background-color: #4a6cf7;
  color: white;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.upload-btn:hover {
  background-color: #3a5bd9;
}

.upload-icon {
  font-size: 16px;
}

.file-input {
  display: none;
}

/* 作品集预览 */
.portfolio-preview {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.portfolio-count {
  margin-bottom: 15px;
  color: #555;
  font-weight: 500;
}

/* 作品集列表预览 */
.portfolio-list-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.portfolio-item-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background-color: white;
  border-radius: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.portfolio-item-preview:hover {
  background-color: #f0f2f5;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.file-info-preview {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0; /* 防止内容溢出 */
}

.file-name-preview {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 10px;
}

.file-actions-preview {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
}

.edit-btn {
  background-color: #f0f2f5;
  color: #666;
}

.edit-btn:hover {
  background-color: #e0e0e0;
}

.more-files-preview {
  padding: 8px 12px;
  background-color: #eee;
  border-radius: 4px;
  font-size: 13px;
  color: #555;
  text-align: center;
  margin-top: 5px;
}

.manage-portfolio-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 10px 15px;
  background-color: #ff9800;
  color: white;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.manage-portfolio-btn:hover {
  background-color: #f57c00;
}

.manage-icon {
  font-size: 16px;
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

.tasks-grid, .challenges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.task-card, .challenge-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.task-header, .challenge-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.task-title, .challenge-title {
  font-size: 18px;
  margin: 0;
  flex: 1;
}

.task-date, .challenge-date {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.task-content, .challenge-content {
  flex: 1;
  margin-bottom: 15px;
}

.task-footer, .challenge-footer {
  display: flex;
  justify-content: flex-end;
}

.view-button, .view-btn {
  background-color: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
}

.challenge-links {
  display: flex;
  gap: 10px;
  margin-right: auto;
}

.solution-link, .file-link {
  font-size: 14px;
  color: #4a6cf7;
  text-decoration: none;
}

.portfolio-modal {
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
  max-width: 700px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
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
  flex: 1;
}

.empty-portfolio {
  text-align: center;
  padding: 20px;
  color: #666;
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

.portfolio-item .file-info {
  display: flex;
  align-items: center;
  width: 50%; /* 固定宽度，左侧占据50% */
  min-width: 0; /* 防止内容溢出 */
  height: 32px; /* 与按钮高度一致，确保垂直居中 */
  margin-bottom: 0;
}

.file-type-icon {
  margin-right: 12px;
  font-size: 24px;
  flex-shrink: 0;
}

.file-name-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.edit-name-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 2px 5px;
  font-size: 12px;
  opacity: 0.5;
  transition: opacity 0.2s;
}

.file-name-container:hover .edit-name-btn {
  opacity: 1;
}

.file-name-edit {
  flex: 1;
  min-width: 0;
  width: 100%;
}

.name-input {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.name-input:focus {
  border-color: #4a6cf7;
  outline: none;
  box-shadow: 0 0 0 2px rgba(74, 108, 247, 0.2);
}

.portfolio-item .file-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  width: 50%; /* 固定宽度，右侧占据50% */
  justify-content: flex-end; /* 右对齐 */
  align-items: center; /* 垂直居中对齐 */
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
  width: 80px; /* 固定宽度，确保所有按钮宽度一致 */
  height: 36px; /* 固定高度，确保所有按钮高度一致 */
  box-sizing: border-box; /* 确保内边距和边框不会增加元素的宽度 */
  font-weight: 500;
}

.view-btn {
  background-color: #e3f2fd;
  color: #1976d2;
}

.download-btn {
  background-color: #e8f5e9;
  color: #388e3c;
}

.delete-btn {
  background-color: #ffebee;
  color: #d32f2f;
}

.view-btn:hover {
  background-color: #bbdefb;
}

.download-btn:hover {
  background-color: #c8e6c9;
}

.delete-btn:hover {
  background-color: #ffcdd2;
}

.action-icon {
  font-size: 16px;
}

.modal-footer {
  padding: 18px 24px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f8f9fa;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}

.modal-footer-left {
  text-align: left;
}

.modal-footer-right {
  text-align: right;
}

.modal-footer .upload-btn {
  background-color: #4a6cf7;
  color: white;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.modal-footer .upload-btn:hover {
  background-color: #3a5bd9;
}

.modal-footer .delete-btn {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.modal-footer .delete-btn:hover {
  background-color: #ffcdd2;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tasks-grid, .challenges-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .file-links {
    flex-direction: column;
  }
}
</style>