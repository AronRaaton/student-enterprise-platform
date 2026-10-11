<template>
  <div class="project-application-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">申请参与项目</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 项目信息 -->
    <div v-else class="application-content">
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
            <span class="meta-label">所需技能:</span>
            <span class="meta-value">{{ project.requiredSkills }}</span>
          </div>
        </div>
      </div>

      <!-- 申请表单 -->
      <div class="application-form">
        <h3 class="form-title">申请信息</h3>

        <div class="form-group">
          <label for="applicationReason">申请理由 <span class="required">*</span></label>
          <textarea
            id="applicationReason"
            v-model="application.applicationReason"
            placeholder="请详细描述您为什么想参与此项目，以及您的相关经验和技能"
            class="form-control"
            rows="6"
            :class="{ 'is-invalid': submitted && !application.applicationReason }"
          ></textarea>
          <div v-if="submitted && !application.applicationReason" class="invalid-feedback">
            请输入申请理由
          </div>
        </div>

        <div class="form-group">
          <label for="relevantExperience">相关经验</label>
          <textarea
            id="relevantExperience"
            v-model="application.relevantExperience"
            placeholder="请描述您与项目相关的经验（可选）"
            class="form-control"
            rows="4"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="contactInfo">联系方式</label>
          <input
            type="text"
            id="contactInfo"
            v-model="application.contactInfo"
            placeholder="请提供您的联系方式，如邮箱或电话（可选）"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label for="fileUpload">上传文件</label>
          <div class="file-upload-container">
            <input
              type="file"
              id="fileUpload"
              ref="fileInput"
              @change="handleFileUpload"
              class="file-input"
            />
            <div class="file-upload-button" @click="triggerFileInput">
              <span class="upload-icon">+</span> 选择文件
            </div>
            <div v-if="selectedFile" class="selected-file">
              <span class="file-name">{{ selectedFile.name }}</span>
              <button type="button" class="remove-file" @click="removeFile">×</button>
            </div>
          </div>
          <small class="form-text text-muted">可上传简历、作品集或其他相关文件，支持PDF、Word、PPT等格式，最大大小30MB</small>
        </div>

        <div class="form-group">
          <label for="notes">备注</label>
          <textarea
            id="notes"
            v-model="application.notes"
            placeholder="请输入其他您想要补充的信息（可选）"
            class="form-control"
            rows="3"
          ></textarea>
        </div>

        <div class="form-actions">
          <button class="cancel-btn" @click="goBack">取消</button>
          <button class="submit-btn" @click="submitApplication" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </div>

      <!-- 成功提示 -->
      <div v-if="showSuccess" class="success-message">
        <div class="success-content">
          <div class="success-icon">✓</div>
          <h2>申请提交成功！</h2>
          <p>您的申请已成功提交，请等待企业审核</p>
          <div class="success-actions">
            <button class="primary-btn" @click="viewProjects">查看所有项目</button>
            <button class="secondary-btn" @click="viewProjectDetails">查看项目详情</button>
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
      project: {},
      application: {
        studentId: null,
        projectId: null,
        applicationReason: '',
        relevantExperience: '',
        contactInfo: '',
        status: 'PENDING',
        uploadedFile: '',
        notes: ''
      },
      selectedFile: null,
      loading: true,
      submitted: false,
      isSubmitting: false,
      showSuccess: false
    };
  },
  created() {
    this.application.studentId = localStorage.getItem('userId');
    this.application.projectId = this.$route.params.projectId;
    this.fetchProjectDetails();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      this.loading = true;

      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.application.projectId}`);
        this.project = response.data;
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    // 触发文件输入框的点击
    triggerFileInput() {
      // 通过程序触发隐藏的文件输入框的点击事件
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
      this.application.uploadedFile = '';
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
        const response = await axios.post('http://localhost:8080/api/files/project-application', formData, {
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

    async submitApplication() {
      this.submitted = true;

      // 表单验证
      if (!this.application.applicationReason) {
        return;
      }

      this.isSubmitting = true;

      try {
        // 如果有文件，先上传文件到服务器
        if (this.selectedFile) {
          try {
            const fileName = await this.uploadFile();
            this.application.uploadedFile = fileName;
          } catch (error) {
            alert('文件上传失败: ' + error.message);
            this.isSubmitting = false;
            return;
          }
        }

        // 修改请求格式，将studentId作为请求参数传递
        await axios.post(
          `http://localhost:8080/api/projects/${this.application.projectId}/apply?studentId=${this.application.studentId}`,
          {
            applicationReason: this.application.applicationReason,
            uploadedFile: this.application.uploadedFile,
            notes: this.application.notes,
            relevantExperience: this.application.relevantExperience,
            contactInfo: this.application.contactInfo
          }
        );
        this.showSuccess = true;
      } catch (error) {
        console.error('提交申请失败:', error);
        alert('提交申请失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      } finally {
        this.isSubmitting = false;
      }
    },
    viewProjects() {
      this.$router.push({ name: 'StudentProjects' });
    },
    viewProjectDetails() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.application.projectId }
      });
    }
  }
};
</script>

<style scoped>
.project-application-container {
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

/* 申请内容 */
.application-content {
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

/* 申请表单 */
.application-form {
  margin-top: 20px;
}

.form-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
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

.required {
  color: #f56c6c;
  margin-left: 4px;
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

.form-control.is-invalid {
  border-color: #f56c6c;
}

.invalid-feedback {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #e9ebef;
}

.submit-btn {
  padding: 12px 25px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 成功提示样式 */
.success-message {
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

.success-content {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.success-icon {
  width: 60px;
  height: 60px;
  background-color: #67c23a;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
  margin: 0 auto 20px;
}

.success-content h2 {
  color: #303133;
  margin-bottom: 10px;
}

.success-content p {
  color: #606266;
  margin-bottom: 25px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.primary-btn {
  padding: 10px 20px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background-color: #e9ebef;
}

/* 文件上传样式 */
.file-upload-container {
  position: relative;
  margin-bottom: 10px;
}

.file-input {
  position: absolute;
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  overflow: hidden;
  z-index: -1;
}

.file-upload-button {
  display: inline-flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none; /* 防止文本被选中 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.file-upload-button:hover {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

.upload-icon {
  font-size: 18px;
  margin-right: 5px;
}

.selected-file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding: 8px 12px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.file-name {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
  flex: 1;
}

.remove-file {
  background: none;
  border: none;
  color: #909399;
  font-size: 18px;
  cursor: pointer;
  padding: 0 5px;
  transition: color 0.3s;
}

.remove-file:hover {
  color: #f56c6c;
}

.form-text {
  font-size: 12px;
  margin-top: 5px;
}
</style>
