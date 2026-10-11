<template>
  <div class="plan-review-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">审阅项目计划</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 审阅内容 -->
    <div v-else class="review-content">
      <!-- 项目信息 -->
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">企业:</span>
            <span class="meta-value">{{ project.enterpriseName }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">项目方向:</span>
            <span class="meta-value">{{ project.projectDirection }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">当前步骤:</span>
            <span class="meta-value">{{ project.currentStep }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤说明 -->
      <div class="step-info">
        <h3 class="step-title">项目立项与资源整合</h3>
        <p class="step-description">制定合作计划，整合资金、设备、人才等资源，明确各方角色分工。</p>
      </div>

      <!-- 项目计划书 -->
      <div v-if="project.projectPlan" class="project-plan-section">
        <h3 class="section-title">项目计划书</h3>
        <div class="project-plan-content">
          <p>{{ project.projectPlan }}</p>
        </div>
        
        <!-- 显示计划书文件 -->
        <div v-if="project.projectPlanFile" class="file-section">
          <h4>计划书文件:</h4>
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ getFileName(project.projectPlanFile) }}</span>
            </div>
            <div class="file-actions">
              <button @click="downloadFile(project.projectPlanFile)" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </button>
              <button v-if="isViewableFile(project.projectPlanFile)" @click="viewFile(project.projectPlanFile)" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-plan">
        <div class="empty-icon">📝</div>
        <h3>暂无计划书</h3>
        <p>企业尚未上传项目计划书，请稍后再查看</p>
      </div>

      <!-- 审阅表单 -->
      <div v-if="project.projectPlan && !existingSubmission" class="review-form">
        <h3 class="section-title">审阅意见</h3>
        
        <div class="form-group">
          <label for="feedback">您对计划书的意见或建议 (可选)</label>
          <textarea
            id="feedback"
            v-model="feedback"
            placeholder="请输入您对项目计划书的意见或建议"
            class="form-control"
            rows="6"
          ></textarea>
        </div>
        
        <div class="review-actions">
          <button
            class="reject-btn"
            @click="rejectPlan"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting && action === 'reject'">处理中...</span>
            <span v-else>不同意计划</span>
          </button>
          <button
            class="approve-btn"
            @click="approvePlan"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting && action === 'approve'">处理中...</span>
            <span v-else>同意计划</span>
          </button>
        </div>
      </div>

      <!-- 已审阅状态 -->
      <div v-if="existingSubmission" class="existing-review">
        <h3 class="section-title">您的审阅结果</h3>
        <div class="review-status" :class="getStatusClass(existingSubmission.status)">
          {{ getStatusText(existingSubmission.status) }}
        </div>
        
        <div class="review-time">
          审阅时间: {{ formatDate(existingSubmission.submissionTime) }}
        </div>
        
        <div v-if="existingSubmission.content" class="feedback-section">
          <h4>您的意见:</h4>
          <p>{{ existingSubmission.content }}</p>
        </div>
      </div>

      <!-- 提交成功提示 -->
      <div v-if="showSuccess" class="success-message">
        <div class="success-icon">✓</div>
        <h3>审阅成功</h3>
        <p>您已成功提交审阅意见</p>
        <div class="success-actions">
          <button class="action-btn" @click="viewProjectDetails">返回项目详情</button>
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
      project: {},
      existingSubmission: null,
      feedback: '',
      loading: true,
      isSubmitting: false,
      action: null,
      showSuccess: false
    };
  },
  computed: {
    projectId() {
      return this.$route.params.projectId;
    }
  },
  created() {
    this.fetchProjectDetails();
    this.checkExistingSubmission();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}`);
        this.project = response.data;
        
        // 检查当前步骤是否为第2步
        if (this.project.currentStep !== 2) {
          alert(`当前项目不在第2步，无法审阅计划书`);
          this.$router.push({
            name: 'ProjectDetail',
            params: { projectId: this.projectId }
          });
        }
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请稍后重试');
      }
    },
    async checkExistingSubmission() {
      try {
        const studentId = localStorage.getItem('userId');
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/2/submissions/${studentId}`
        );
        this.existingSubmission = response.data;
      } catch (error) {
        // 如果没有找到提交记录，不做任何处理
        console.log('没有找到审阅记录，可以进行审阅');
      } finally {
        this.loading = false;
      }
    },
    // 同意计划
    async approvePlan() {
      this.isSubmitting = true;
      this.action = 'approve';
      
      try {
        const studentId = localStorage.getItem('userId');
        
        // 提交步骤，状态为APPROVED
        await axios.post(
          `http://localhost:8080/api/projects/${this.projectId}/steps/2/submit?studentId=${studentId}`,
          {
            content: this.feedback || '同意项目计划',
            uploadedFile: '',
            status: 'APPROVED'
          }
        );
        
        this.showSuccess = true;
      } catch (error) {
        console.error('审阅失败:', error);
        alert('审阅失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },
    // 不同意计划
    async rejectPlan() {
      if (!this.feedback) {
        alert('请输入不同意的原因');
        return;
      }
      
      this.isSubmitting = true;
      this.action = 'reject';
      
      try {
        const studentId = localStorage.getItem('userId');
        
        // 提交步骤，状态为REJECTED
        await axios.post(
          `http://localhost:8080/api/projects/${this.projectId}/steps/2/submit?studentId=${studentId}`,
          {
            content: this.feedback,
            uploadedFile: '',
            status: 'REJECTED'
          }
        );
        
        this.showSuccess = true;
      } catch (error) {
        console.error('审阅失败:', error);
        alert('审阅失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },
    viewProjectDetails() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.projectId }
      });
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
    // 查看文件
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

        // 创建下载链接
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
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    // 获取状态样式类
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
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已同意计划';
        case 'REJECTED':
          return '已拒绝计划';
        default:
          return '未知状态';
      }
    }
  }
};
</script>

<style scoped>
.plan-review-container {
  max-width: 1000px;
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
  font-size: 20px;
  margin-right: 5px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.project-info {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.project-title {
  font-size: 22px;
  color: #303133;
  margin: 0 0 15px 0;
}

.project-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
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

.step-info {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.step-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 15px 0;
}

.step-description {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.project-plan-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.project-plan-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-line;
  color: #606266;
  line-height: 1.6;
}

.file-section {
  margin-top: 20px;
}

.file-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f5f7fa;
  padding: 12px 15px;
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
}

.file-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.view-btn:hover {
  background-color: #e1f3d8;
}

.download-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.download-btn:hover {
  background-color: #d9ecff;
}

.action-icon {
  margin-right: 5px;
}

.empty-plan {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 40px 20px;
  margin-bottom: 30px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.empty-plan h3 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 10px 0;
}

.empty-plan p {
  color: #909399;
  margin: 0;
}

.review-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.form-control {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.3s;
  resize: vertical;
}

.form-control:focus {
  outline: none;
  border-color: #409EFF;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.approve-btn, .reject-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.approve-btn {
  background-color: #67c23a;
  color: white;
}

.approve-btn:hover {
  background-color: #85ce61;
}

.reject-btn {
  background-color: #f56c6c;
  color: white;
}

.reject-btn:hover {
  background-color: #f78989;
}

.approve-btn:disabled, .reject-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.existing-review {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.review-status {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 15px;
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

.review-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.feedback-section {
  margin-top: 15px;
}

.feedback-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.feedback-section p {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.success-message {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
  text-align: center;
}

.success-icon {
  width: 60px;
  height: 60px;
  line-height: 60px;
  background-color: #f0f9eb;
  color: #67c23a;
  font-size: 30px;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.success-message h3 {
  font-size: 22px;
  color: #303133;
  margin: 0 0 15px 0;
}

.success-message p {
  color: #606266;
  margin: 0 0 20px 0;
}

.success-actions {
  margin-top: 20px;
}

@media (max-width: 768px) {
  .project-meta {
    grid-template-columns: 1fr;
  }

  .file-preview {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-actions {
    margin-top: 10px;
  }

  .review-actions {
    flex-direction: column;
  }
}
</style>
