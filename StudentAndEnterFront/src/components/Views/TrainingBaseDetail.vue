<template>
  <div class="training-base-container module-page module-detail">
    <div class="header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h2>实训基地项目详情</h2>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载项目详情中...</p>
    </div>

    <div v-else class="content-container">
      <div class="project-header">
        <h3 class="project-title">{{ project.title }}</h3>
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
          <span class="info-value">{{ project.collaborationType }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">开始日期:</span>
          <span class="info-value">{{ formatDate(project.startDate) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">结束日期:</span>
          <span class="info-value">{{ formatDate(project.endDate) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">实训地点:</span>
          <span class="info-value">{{ project.location || '未指定' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">实训周期:</span>
          <span class="info-value">{{ project.timeCycle || '未指定' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">最大参与人数:</span>
          <span class="info-value">{{ project.maxParticipants }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">已批准人数:</span>
          <span class="info-value">{{ project.approvedCount || 0 }}</span>
        </div>
      </div>

      <div class="project-description">
        <h4>项目描述</h4>
        <div class="description-content">{{ project.description }}</div>
      </div>

      <div class="training-plan">
        <h4>培训计划</h4>
        <div class="plan-content">{{ project.trainingPlan }}</div>
      </div>

      <div class="resources">
        <h4>提供资源</h4>
        <div class="resources-content">{{ project.resources }}</div>
      </div>

      <div class="required-skills">
        <h4>所需技能</h4>
        <div class="skills-content">{{ project.requiredSkills }}</div>
      </div>

      <div class="support">
        <h4>支持方式</h4>
        <div class="support-content">{{ project.support }}</div>
      </div>

      <!-- 学生视图 - 实训报告部分 -->
      <div v-if="isStudent && isParticipant" class="training-report-section">
        <h4>实训报告</h4>
        <div v-if="trainingReport" class="report-info">
          <div class="report-header">
            <h5>{{ trainingReport.title }}</h5>
            <span class="report-date">提交时间: {{ formatDate(trainingReport.submissionTime) }}</span>
          </div>
          <div class="report-content">{{ trainingReport.content }}</div>
          
          <!-- 显示上传的文件 -->
          <div v-if="trainingReport.uploadedFile" class="report-file">
            <h5>附件:</h5>
            <div class="file-preview">
              <div class="file-info">
                <span class="file-icon">📄</span>
                <span class="file-name">{{ getFileName(trainingReport.uploadedFile) }}</span>
              </div>
              <div class="file-actions">
                <a @click="downloadFile(trainingReport.uploadedFile)" class="action-btn download-btn" style="cursor: pointer">
                  <span class="action-icon">⬇️</span> 下载
                </a>
                <a v-if="isViewableFile(trainingReport.uploadedFile)" @click="viewFile(trainingReport.uploadedFile)" class="action-btn view-btn" style="cursor: pointer">
                  <span class="action-icon">🔍</span> 查看
                </a>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="isProjectEnded" class="report-actions">
          <button class="action-btn upload-report-btn" @click="uploadTrainingReport">
            <span class="upload-icon">📁</span> 上传实训报告
          </button>
        </div>
        
        <div v-else class="report-not-available">
          <span class="info-icon">ℹ️</span> 实训还未结束，请结束后再来提交实训报告
        </div>
      </div>

      <!-- 企业视图 - 实训报告列表 -->
      <div v-if="isEnterprise" class="training-reports-list">
        <h4>学生实训报告</h4>
        <div v-if="loadingReports" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载实训报告中...</p>
        </div>
        
        <div v-else-if="trainingReports.length === 0" class="empty-reports">
          <span class="info-icon">ℹ️</span> 暂无学生提交实训报告
        </div>
        
        <div v-else class="reports-list">
          <div v-for="report in trainingReports" :key="report.id" class="report-item">
            <div class="report-item-header">
              <div class="student-info">
                <div class="student-avatar" @click="viewStudentDetails(report.studentId)" title="点击查看学生详情">
                  {{ report.studentName ? report.studentName.charAt(0) : 'S' }}
                </div>
                <div class="student-details">
                  <h5 class="student-name" @click="viewStudentDetails(report.studentId)" title="点击查看学生详情" style="cursor: pointer">
                    {{ report.studentName }}
                  </h5>
                  <p class="student-school">{{ report.studentSchool }}</p>
                </div>
              </div>
              <span class="report-date">{{ formatDate(report.submissionTime) }}</span>
            </div>
            
            <div class="report-item-content">
              <h5>{{ report.title }}</h5>
              <p>{{ report.content }}</p>
              
              <!-- 显示上传的文件 -->
              <div v-if="report.uploadedFile" class="report-file">
                <h5>附件:</h5>
                <div class="file-preview">
                  <div class="file-info">
                    <span class="file-icon">📄</span>
                    <span class="file-name">{{ getFileName(report.uploadedFile) }}</span>
                  </div>
                  <div class="file-actions">
                    <a @click="downloadFile(report.uploadedFile)" class="action-btn download-btn" style="cursor: pointer">
                      <span class="action-icon">⬇️</span> 下载
                    </a>
                    <a v-if="isViewableFile(report.uploadedFile)" @click="viewFile(report.uploadedFile)" class="action-btn view-btn" style="cursor: pointer">
                      <span class="action-icon">🔍</span> 查看
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="project-actions">
        <!-- 学生视图 - 与企业交流按钮 -->
        <button
          v-if="isStudent && isParticipant"
          class="action-btn chat-btn"
          @click="chatWithEnterprise"
        >
          <span class="chat-icon">💬</span> 与企业交流
        </button>
        
        <!-- 企业视图 - 完成项目按钮 -->
        <button
          v-if="isEnterprise && project.status === 'OPEN'"
          class="action-btn complete-btn"
          @click="completeProject"
        >
          <span class="complete-icon">✓</span> 完成项目
        </button>
        
        <!-- 企业视图 - 查看申请按钮 -->
        <button
          v-if="isEnterprise"
          class="action-btn applications-btn"
          @click="viewApplications"
        >
          <span class="applications-icon">👥</span> 查看申请
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'TrainingBaseDetail',
  data() {
    return {
      project: {},
      loading: true,
      loadingReports: false,
      trainingReports: [],
      trainingReport: null,
      userRole: localStorage.getItem('userRole') || '',
      userId: localStorage.getItem('userId'),
      isParticipant: false
    };
  },
  computed: {
    isStudent() {
      return this.userRole === 'student';
    },
    isEnterprise() {
      return this.userRole === 'enterprise';
    },
    projectId() {
      return this.$route.params.projectId;
    },
    isProjectEnded() {
      // 检查项目是否已结束（当前日期大于结束日期）
      if (!this.project.endDate) return false;
      const today = new Date();
      const endDate = new Date(this.project.endDate);
      return today > endDate;
    }
  },
  created() {
    this.fetchProjectDetails();
    
    if (this.isStudent) {
      this.checkIfParticipant();
    } else if (this.isEnterprise) {
      this.fetchTrainingReports();
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}`);
        this.project = response.data;
        
        // 如果是学生且是参与者，获取自己的实训报告
        if (this.isStudent && this.isParticipant) {
          this.fetchStudentTrainingReport();
        }
      } catch (error) {
        console.error('获取项目详情失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async checkIfParticipant() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/applications/student/${this.userId}`);
        // 找到当前项目的申请
        const application = response.data.find(app => app.projectId === parseInt(this.projectId));
        
        // 如果申请存在且状态为已批准，则是参与者
        this.isParticipant = application && application.status === 'APPROVED';
        
        // 如果是参与者，获取实训报告
        if (this.isParticipant) {
          this.fetchStudentTrainingReport();
        }
      } catch (error) {
        console.error('检查参与状态失败:', error);
      }
    },
    async fetchTrainingReports() {
      this.loadingReports = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/training-reports`);
        this.trainingReports = response.data;
      } catch (error) {
        console.error('获取实训报告失败:', error);
      } finally {
        this.loadingReports = false;
      }
    },
    async fetchStudentTrainingReport() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/training-report/${this.userId}`);
        this.trainingReport = response.data;
      } catch (error) {
        // 如果没有找到报告，不做任何处理
        console.log('没有找到学生实训报告');
      }
    },
    async completeProject() {
      try {
        await axios.put(`http://localhost:8080/api/projects/${this.projectId}/status`, null, {
          params: { status: 'COMPLETED' }
        });
        
        // 刷新项目详情
        this.fetchProjectDetails();
        alert('项目已成功完成！');
      } catch (error) {
        console.error('完成项目失败:', error);
        alert('完成项目失败，请重试');
      }
    },
    chatWithEnterprise() {
      if (!this.project.enterpriseId) {
        alert('无法获取企业信息，请稍后再试');
        return;
      }

      // 跳转到聊天页面
      this.$router.push({
        name: 'ChatS2E',
        params: { enterpriseId: this.project.enterpriseId }
      });
    },
    uploadTrainingReport() {
      // 跳转到实训报告上传页面
      this.$router.push({
        name: 'TrainingReportUpload',
        params: { projectId: this.projectId }
      });
    },
    viewApplications() {
      // 跳转到项目详情页面的申请列表标签
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.projectId },
        query: { tab: 'applications' }
      });
    },
    viewStudentDetails(studentId) {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId }
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
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
    // 获取文件名
    getFileName(fileString) {
      if (!fileString) return '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileString);
        return fileInfo.name || '未知文件';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        // 从路径中提取文件名，去除UUID前缀
        const fileName = fileString.split('/').pop() || fileString;
        if (fileName.includes('_')) {
          return fileName.substring(fileName.indexOf('_') + 1);
        }
        return fileName;
      }
    },
    // 判断文件是否可以在浏览器中预览
    isViewableFile(fileString) {
      if (!fileString) return false;

      let fileName = '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileString);
        fileName = fileInfo.name || '';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        fileName = fileString.split('/').pop() || fileString;
      }

      // 检查文件扩展名
      const lowerFileName = fileName.toLowerCase();
      return lowerFileName.endsWith('.pdf') ||
             lowerFileName.endsWith('.txt') ||
             lowerFileName.endsWith('.jpg') ||
             lowerFileName.endsWith('.jpeg') ||
             lowerFileName.endsWith('.png');
    },
    // 查看文件（预览）
    viewFile(fileData) {
      // 构建URL
      let fileUrl = '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileData);
        // 如果是JSON，则使用文件名属性
        fileUrl = `http://localhost:8080/api/files/download/${fileInfo.name}?preview=true`;
      } catch (e) {
        // 如果不是JSON，则直接使用文件名
        fileUrl = `http://localhost:8080/api/files/download/${fileData}?preview=true`;
      }

      // 打开新窗口预览文件
      window.open(fileUrl, '_blank');
    },
    // 下载文件
    async downloadFile(fileData) {
      try {
        // 构建URL
        let fileUrl = '';

        // 先尝试解析JSON，兼容旧的数据格式
        try {
          const fileInfo = JSON.parse(fileData);
          // 如果是JSON，则使用文件名属性
          fileUrl = `http://localhost:8080/api/files/download/${fileInfo.name}`;
        } catch (e) {
          // 如果不是JSON，则直接使用文件名
          fileUrl = `http://localhost:8080/api/files/download/${fileData}`;
        }

        // 使用fetch API获取文件
        const response = await fetch(fileUrl);

        if (!response.ok) {
          throw new Error(`下载失败: ${response.status} ${response.statusText}`);
        }

        // 获取blob数据
        const blob = await response.blob();

        // 创建URL对象
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;

        // 设置文件名
        let fileName = this.getFileName(fileData);
        a.download = fileName;

        // 触发下载
        document.body.appendChild(a);
        a.click();

        // 清理
        window.URL.revokeObjectURL(downloadUrl);
        document.body.removeChild(a);
      } catch (error) {
        console.error('下载文件失败:', error);
        alert('下载文件失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.training-base-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.back-btn {
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

.back-btn:hover {
  background-color: #ecf5ff;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
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

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.project-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.project-status {
  padding: 6px 12px;
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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.project-description,
.training-plan,
.resources,
.required-skills,
.support,
.training-report-section,
.training-reports-list {
  margin-bottom: 30px;
}

.project-description h4,
.training-plan h4,
.resources h4,
.required-skills h4,
.support h4,
.training-report-section h4,
.training-reports-list h4 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.description-content,
.plan-content,
.resources-content,
.skills-content,
.support-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.report-info {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.report-header h5 {
  font-size: 16px;
  color: #303133;
  margin: 0;
}

.report-date {
  font-size: 14px;
  color: #909399;
}

.report-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
  margin-bottom: 15px;
}

.report-file h5 {
  font-size: 16px;
  color: #303133;
  margin: 10px 0;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f2f5;
  padding: 10px 15px;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
}

.file-icon {
  font-size: 20px;
  margin-right: 10px;
}

.file-name {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.download-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.download-btn:hover {
  background-color: #d9ecff;
}

.view-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.view-btn:hover {
  background-color: #e1f3d8;
}

.action-icon {
  margin-right: 5px;
}

.report-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.upload-report-btn {
  background-color: #67c23a;
  color: white;
  display: flex;
  align-items: center;
}

.upload-report-btn:hover {
  background-color: #85ce61;
}

.upload-icon {
  margin-right: 5px;
}

.report-not-available,
.empty-reports {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.info-icon {
  margin-right: 10px;
  font-size: 18px;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.report-item {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.report-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.student-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.student-details {
  display: flex;
  flex-direction: column;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.student-school {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.report-item-content h5 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.report-item-content p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
  margin-bottom: 15px;
}

.project-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.chat-btn {
  background-color: #2196F3;
  color: white;
}

.chat-btn:hover {
  background-color: #0b7dda;
}

.chat-icon {
  margin-right: 5px;
}

.complete-btn {
  background-color: #67c23a;
  color: white;
}

.complete-btn:hover {
  background-color: #85ce61;
}

.complete-icon {
  margin-right: 5px;
}

.applications-btn {
  background-color: #909399;
  color: white;
}

.applications-btn:hover {
  background-color: #a6a9ad;
}

.applications-icon {
  margin-right: 5px;
}
</style>
