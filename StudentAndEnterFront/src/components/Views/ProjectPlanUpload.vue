<template>
  <div class="plan-upload-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">上传项目计划书</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 上传表单 -->
    <div v-else class="upload-content">
      <!-- 项目信息 -->
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">项目方向:</span>
            <span class="meta-value">{{ project.projectDirection }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">当前步骤:</span>
            <span class="meta-value">{{ project.currentStep }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">参与学生数:</span>
            <span class="meta-value">{{ project.approvedCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤说明 -->
      <div class="step-info">
        <h3 class="step-title">项目立项与资源整合</h3>
        <p class="step-description">
          在这一步骤中，您需要上传项目计划书，详细说明项目的资源整合、资金安排、设备配置、人才分工等内容。
          学生将根据您提供的计划书来确定是否继续参与项目。
        </p>
      </div>

      <!-- 已有计划书 -->
      <div v-if="project.projectPlan" class="existing-plan">
        <h3 class="section-title">当前计划书</h3>
        <div class="plan-content">
          <p>{{ project.projectPlan }}</p>
        </div>

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

        <div class="plan-actions">
          <button class="edit-btn" @click="editExistingPlan">修改计划书</button>
        </div>
      </div>

      <!-- 上传表单 -->
      <div v-if="!project.projectPlan || showEditForm" class="upload-form">
        <h3 class="section-title">{{ project.projectPlan ? '修改计划书' : '上传计划书' }}</h3>

        <div class="form-group">
          <label for="projectPlan">计划书内容 <span class="required">*</span></label>
          <textarea
            id="projectPlan"
            v-model="projectPlan"
            placeholder="请详细描述项目计划，包括资源整合、资金安排、设备配置、人才分工等内容"
            class="form-control"
            rows="12"
            :class="{ 'is-invalid': submitted && !projectPlan }"
          ></textarea>
          <div v-if="submitted && !projectPlan" class="invalid-feedback">
            请输入计划书内容
          </div>
        </div>

        <div class="form-group">
          <label for="file">上传计划书文件</label>
          <div class="file-upload-container">
            <input
              type="file"
              id="file"
              ref="fileInput"
              @change="handleFileUpload"
              style="display: none"
            />
            <div class="file-upload-box" @click="triggerFileInput">
              <div v-if="!selectedFile" class="upload-placeholder">
                <span class="upload-icon">📎</span>
                <span>点击上传文件</span>
              </div>
              <div v-else class="selected-file">
                <span class="file-icon">📄</span>
                <span class="file-name">{{ selectedFile.name }}</span>
                <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
                <button class="remove-file-btn" @click.stop="removeFile">×</button>
              </div>
            </div>
            <p class="file-hint">支持PDF、Word、Excel、图片等格式，最大30MB</p>
          </div>
        </div>

        <div class="form-actions">
          <button
            class="submit-btn"
            @click="uploadPlan"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting">提交中...</span>
            <span v-else>{{ project.projectPlan ? '更新计划书' : '上传计划书' }}</span>
          </button>
        </div>
      </div>

      <!-- 提交成功提示 -->
      <div v-if="showSuccess" class="success-message">
        <div class="success-icon">✓</div>
        <h3>上传成功</h3>
        <p>您的项目计划书已成功上传，学生将根据计划书内容决定是否继续参与项目</p>
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
      projectPlan: '',
      loading: true,
      submitted: false,
      isSubmitting: false,
      showSuccess: false,
      showEditForm: false,
      selectedFile: null
    };
  },
  computed: {
    projectId() {
      return this.$route.params.projectId;
    }
  },
  created() {
    this.fetchProjectDetails();
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
          alert(`当前项目不在第2步，无法上传计划书`);
          this.$router.push({
            name: 'ProjectDetail',
            params: { projectId: this.projectId }
          });
          return;
        }

        // 如果已有计划书，预填充表单
        if (this.project.projectPlan) {
          this.projectPlan = this.project.projectPlan;
        }
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    editExistingPlan() {
      this.showEditForm = true;
    },
    // 触发文件输入框的点击
    triggerFileInput() {
      this.$refs.fileInput.click();
    },

    // 处理文件上传
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        // 检查文件大小，限制为30MB
        if (file.size > 30 * 1024 * 1024) {
          alert('文件大小不能超过30MB');
          this.$refs.fileInput.value = '';
          return;
        }
        this.selectedFile = file;
      }
    },

    // 移除选择的文件
    removeFile() {
      this.selectedFile = null;
      this.$refs.fileInput.value = '';
    },

    // 上传文件到服务器
    async uploadFile() {
      if (!this.selectedFile) {
        return null;
      }

      try {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        // 发送文件上传请求
        const response = await axios.post('http://localhost:8080/api/files/project-plan', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        // 返回服务器生成的文件名
        return response.data.fileName;
      } catch (error) {
        console.error('文件上传失败:', error);
        throw new Error('文件上传失败: ' + (error.response?.data?.error || error.message));
      }
    },

    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes < 1024) {
        return bytes + ' B';
      } else if (bytes < 1024 * 1024) {
        return (bytes / 1024).toFixed(2) + ' KB';
      } else {
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
      }
    },

    async uploadPlan() {
      this.submitted = true;

      // 表单验证
      if (!this.projectPlan) {
        return;
      }

      this.isSubmitting = true;

      try {
        // 如果有文件，先上传文件
        let uploadedFileName = null;
        if (this.selectedFile) {
          try {
            uploadedFileName = await this.uploadFile();
          } catch (error) {
            alert('文件上传失败: ' + error.message);
            this.isSubmitting = false;
            return;
          }
        }

        await axios.put(
          `http://localhost:8080/api/projects/${this.projectId}/plan`,
          {
            projectPlan: this.projectPlan,
            uploadedFile: uploadedFileName
          }
        );

        this.showSuccess = true;
        this.showEditForm = false;

        // 刷新项目详情
        this.fetchProjectDetails();
      } catch (error) {
        console.error('上传计划书失败:', error);
        alert('上传计划书失败，请稍后重试: ' + (error.response?.data?.error || error.message));
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
    }
  }
};
</script>

<style scoped>
.plan-upload-container {
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

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.existing-plan {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.plan-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  white-space: pre-line;
  color: #606266;
  line-height: 1.6;
}

.plan-actions {
  display: flex;
  justify-content: flex-end;
}

.edit-btn {
  padding: 8px 16px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.upload-form {
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

.required {
  color: #f56c6c;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #409EFF;
}

.is-invalid {
  border-color: #f56c6c;
}

.invalid-feedback {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-btn {
  padding: 10px 30px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
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

.action-btn {
  padding: 8px 16px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background-color: #e6e8eb;
}

.file-section {
  margin-top: 20px;
  margin-bottom: 20px;
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

.file-upload-container {
  margin-top: 10px;
}

.file-upload-box {
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.file-upload-box:hover {
  border-color: #409EFF;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #909399;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

.selected-file {
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-size {
  color: #909399;
  margin-left: 5px;
}

.remove-file-btn {
  background: none;
  border: none;
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  margin-left: 10px;
}

.file-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
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
}
</style>
