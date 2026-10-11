<template>
  <div class="application-review-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">审核项目申请</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 申请详情 -->
    <div v-else class="review-content">
      <!-- 项目信息 -->
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">项目方向:</span>
            <span class="meta-value">{{ project.projectDirection }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">发布时间:</span>
            <span class="meta-value">{{ formatDate(project.createTime) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">已批准人数:</span>
            <span class="meta-value">{{ project.approvedCount || 0 }} / {{ project.maxParticipants }}</span>
          </div>
        </div>
      </div>

      <!-- 学生信息 -->
      <div class="student-info-section">
        <h3 class="section-title">申请学生信息</h3>
        <div class="student-info">
          <div class="student-avatar" @click="viewStudentProfile" title="点击查看学生详情">
            {{ application.studentName ? application.studentName.charAt(0) : 'S' }}
          </div>
          <div class="student-details">
            <h4 class="student-name" @click="viewStudentProfile" title="点击查看学生详情" style="cursor: pointer">
              {{ application.studentName }}
            </h4>
            <p class="student-school">{{ application.studentSchool }}</p>
            <button class="view-profile-btn" @click="viewStudentProfile">查看学生详情</button>
          </div>
        </div>
      </div>

      <!-- 申请详情 -->
      <div class="application-details">
        <h3 class="section-title">申请详情</h3>

        <div class="detail-item">
          <h4 class="detail-label">申请理由:</h4>
          <p class="detail-value">{{ application.applicationReason }}</p>
        </div>

        <div v-if="application.relevantExperience" class="detail-item">
          <h4 class="detail-label">相关经验:</h4>
          <p class="detail-value">{{ application.relevantExperience }}</p>
        </div>

        <div v-if="application.contactInfo" class="detail-item">
          <h4 class="detail-label">联系方式:</h4>
          <p class="detail-value">{{ application.contactInfo }}</p>
        </div>

        <div class="detail-item">
          <h4 class="detail-label">申请时间:</h4>
          <p class="detail-value">{{ formatDate(application.applyTime) }}</p>
        </div>

        <div class="detail-item">
          <h4 class="detail-label">申请状态:</h4>
          <p class="detail-value status" :class="getStatusClass(application.status)">
            {{ getStatusText(application.status) }}
          </p>
        </div>

        <!-- 上传的文件 -->
        <div v-if="application.uploadedFile" class="detail-item">
          <h4 class="detail-label">上传的文件:</h4>
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ getFileName(application.uploadedFile) }}</span>
            </div>
            <div class="file-actions">
              <button v-if="isPdfFile(application.uploadedFile)" @click="viewFile(application.uploadedFile)" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </button>
              <button @click="downloadFile(application.uploadedFile)" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </button>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div v-if="application.notes" class="detail-item">
          <h4 class="detail-label">备注信息:</h4>
          <p class="detail-value">{{ application.notes }}</p>
        </div>
      </div>

      <!-- 审核表单 -->
      <div v-if="application.status === 'PENDING'" class="review-form">
        <h3 class="section-title">审核决定</h3>

        <div class="form-group">
          <label for="feedback">反馈信息</label>
          <textarea
            id="feedback"
            v-model="feedback"
            placeholder="请输入对学生的反馈信息"
            class="form-control"
            rows="4"
          ></textarea>
        </div>

        <div class="review-actions">
          <button class="reject-btn" @click="rejectApplication">拒绝申请</button>
          <button
            class="approve-btn"
            @click="approveApplication"
            :disabled="project.approvedCount >= project.maxParticipants"
          >
            批准申请
          </button>
        </div>

        <div v-if="project.approvedCount >= project.maxParticipants" class="warning-message">
          <span class="warning-icon">⚠️</span>
          <span>项目已达到最大参与人数，无法批准更多申请</span>
        </div>
      </div>

      <!-- 已审核状态 -->
      <div v-else class="review-result">
        <h3 class="section-title">审核结果</h3>

        <div class="result-status" :class="getStatusClass(application.status)">
          {{ getStatusText(application.status) }}
        </div>

        <div v-if="application.feedback" class="feedback-section">
          <h4>反馈信息:</h4>
          <p>{{ application.feedback }}</p>
        </div>

        <div class="review-time">
          <span>审核时间: {{ formatDate(application.reviewTime) }}</span>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="bottom-actions">
        <button class="back-to-list-btn" @click="goToProjectDetails">返回项目详情</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      application: {},
      project: {},
      loading: true,
      feedback: '',
      applicationId: this.$route.params.applicationId
    };
  },
  created() {
    this.fetchApplicationDetails();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchApplicationDetails() {
      this.loading = true;

      try {
        // 获取申请详情
        const response = await axios.get(`http://localhost:8080/api/projects/applications/${this.applicationId}`);
        this.application = response.data;

        // 获取项目详情
        const projectResponse = await axios.get(`http://localhost:8080/api/projects/${this.application.projectId}`);
        this.project = projectResponse.data;
      } catch (error) {
        console.error('获取申请详情失败:', error);
        alert('获取申请详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    async approveApplication() {
      if (this.project.approvedCount >= this.project.maxParticipants) {
        alert('项目已达到最大参与人数，无法批准更多申请');
        return;
      }

      try {
        await axios.put(`http://localhost:8080/api/projects/applications/${this.applicationId}/review`, {
          feedback: this.feedback || '恭喜您，申请已通过！欢迎加入项目团队。'
        }, {
          params: { status: 'APPROVED' }
        });

        // 刷新申请详情
        this.fetchApplicationDetails();
      } catch (error) {
        console.error('批准申请失败:', error);
        alert('批准申请失败，请稍后重试');
      }
    },
    async rejectApplication() {
      if (!this.feedback) {
        alert('请输入拒绝理由');
        return;
      }

      try {
        await axios.put(`http://localhost:8080/api/projects/applications/${this.applicationId}/review`, {
          feedback: this.feedback
        }, {
          params: { status: 'REJECTED' }
        });

        // 刷新申请详情
        this.fetchApplicationDetails();
      } catch (error) {
        console.error('拒绝申请失败:', error);
        alert('拒绝申请失败，请稍后重试');
      }
    },
    viewStudentProfile() {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId: this.application.studentId }
      });
    },
    goToProjectDetails() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.application.projectId },
        query: { tab: 'applications' }
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    getStatusClass(status) {
      switch (status) {
        case 'PENDING':
          return 'status-pending';
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        default:
          return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已批准';
        case 'REJECTED':
          return '已拒绝';
        default:
          return '未知状态';
      }
    },
    // 获取文件名
    getFileName(fileData) {
      if (!fileData) return '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileData);
        return fileInfo.name || '未知文件';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        // 从路径中提取文件名，去除UUID前缀
        const fileName = fileData.split('/').pop() || fileData;
        if (fileName.includes('_')) {
          return fileName.substring(fileName.indexOf('_') + 1);
        }
        return fileName;
      }
    },
    // 检查是否是PDF文件
    isPdfFile(fileData) {
      if (!fileData) return false;

      let fileName = '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileData);
        fileName = fileInfo.name || '';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        fileName = fileData;
      }

      return fileName.toLowerCase().endsWith('.pdf');
    },
    // 查看文件（PDF预览）
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
        let fileName;

        try {
          const fileInfo = JSON.parse(fileData);
          fileName = fileInfo.name;
        } catch (e) {
          // 如果不是JSON格式，尝试直接使用文件名
          fileName = fileData;
        }

        // 构建下载URL
        const url = `http://localhost:8080/api/files/download/${fileName}`;

        // 使用fetch API获取文件
        const response = await fetch(url);

        if (!response.ok) {
          throw new Error(`下载失败: ${response.status} ${response.statusText}`);
        }

        // 获取blob数据
        const blob = await response.blob();

        // 创建下载链接
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = this.getFileName(fileData);
        document.body.appendChild(link);

        // 触发下载
        link.click();

        // 清理
        setTimeout(() => {
          window.URL.revokeObjectURL(downloadUrl);
          document.body.removeChild(link);
        }, 100);
      } catch (error) {
        console.error('下载文件失败:', error);
        alert('下载文件失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.application-review-container {
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

/* 审核内容 */
.review-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.project-info {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.project-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 20px 0;
}

.project-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.meta-value {
  color: #303133;
  font-weight: 500;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

/* 学生信息 */
.student-info-section {
  margin-bottom: 30px;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.student-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.student-details {
  flex: 1;
}

.student-name {
  font-size: 18px;
  color: #303133;
  margin: 0 0 5px 0;
}

.student-school {
  color: #606266;
  margin: 0 0 10px 0;
}

.view-profile-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-profile-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

/* 申请详情 */
.application-details {
  margin-bottom: 30px;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-label {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.detail-value {
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.status {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: bold;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-approved {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 审核表单 */
.review-form {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #606266;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-control:focus {
  border-color: #409EFF;
  outline: none;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.reject-btn {
  padding: 10px 20px;
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.reject-btn:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.approve-btn {
  padding: 10px 20px;
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.approve-btn:hover:not(:disabled) {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.approve-btn:disabled {
  background-color: #f5f7fa;
  color: #c0c4cc;
  border-color: #e4e7ed;
  cursor: not-allowed;
}

.warning-message {
  margin-top: 15px;
  padding: 10px;
  background-color: #fdf6ec;
  color: #e6a23c;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.warning-icon {
  margin-right: 10px;
}

/* 已审核状态 */
.review-result {
  margin-bottom: 30px;
}

.result-status {
  font-size: 18px;
  font-weight: bold;
  padding: 10px 20px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 20px;
}

.feedback-section {
  margin-bottom: 20px;
}

.feedback-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.feedback-section p {
  color: #606266;
  margin: 0;
  line-height: 1.6;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-time {
  color: #909399;
  font-size: 14px;
}

/* 文件预览样式 */
.file-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-top: 10px;
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
  color: #606266;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.view-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.view-btn:hover {
  background-color: #409EFF;
  color: white;
}

.download-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.download-btn:hover {
  background-color: #67c23a;
  color: white;
}

.action-icon {
  margin-right: 5px;
}

/* 底部按钮 */
.bottom-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.back-to-list-btn {
  padding: 10px 20px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-to-list-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}
</style>
