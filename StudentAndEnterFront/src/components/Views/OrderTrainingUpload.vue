<template>
  <div class="order-training-upload-container module-page module-form">
    <div class="header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h2>上传{{ stepName }}文件</h2>
    </div>

    <div class="content-container">
      <div class="form-container">
        <div class="form-group">
          <label for="title">标题 <span class="required">*</span></label>
          <input
            id="title"
            v-model="uploadData.title"
            class="form-control"
            placeholder="请输入标题"
            :class="{ 'is-invalid': submitted && !uploadData.title }"
          />
          <div v-if="submitted && !uploadData.title" class="invalid-feedback">
            请输入标题
          </div>
        </div>

        <div class="form-group">
          <label for="content">内容 <span class="required">*</span></label>
          <textarea
            id="content"
            v-model="uploadData.content"
            class="form-control"
            rows="10"
            placeholder="请输入内容"
            :class="{ 'is-invalid': submitted && !uploadData.content }"
          ></textarea>
          <div v-if="submitted && !uploadData.content" class="invalid-feedback">
            请输入内容
          </div>
        </div>

        <div class="form-group">
          <label for="file">上传文件 <span class="required">*</span></label>
          <div class="file-upload-container">
            <input
              type="file"
              id="file"
              ref="fileInput"
              @change="handleFileChange"
              class="file-input"
            />
            <div class="file-upload-btn" @click="triggerFileInput">
              <span class="upload-icon">📎</span> 选择文件
            </div>
            <span class="file-name">{{ selectedFileName || '未选择文件' }}</span>
          </div>
          <div v-if="submitted && !selectedFile" class="invalid-feedback">
            请上传文件
          </div>
          <small class="form-text text-muted">支持PDF、Word、PPT等格式，文件大小不超过30MB</small>
        </div>

        <!-- 已上传文件预览 -->
        <div v-if="selectedFile" class="file-preview">
          <div class="file-info">
            <span class="file-icon">📄</span>
            <span class="file-name">{{ selectedFileName }}</span>
          </div>
          <div class="file-actions">
            <button class="action-btn remove-btn" @click="removeFile">
              <span class="remove-icon">🗑️</span> 移除
            </button>
          </div>
        </div>

        <div class="form-actions">
          <button class="submit-btn" @click="submitUpload" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'OrderTrainingUpload',
  data() {
    return {
      projectId: null,
      stepNumber: null,
      stepName: '',
      uploadData: {
        title: '',
        content: '',
        fileUrl: ''
      },
      selectedFile: null,
      selectedFileName: '',
      submitted: false,
      submitting: false
    };
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.stepNumber = parseInt(this.$route.params.stepNumber);
    this.stepName = this.$route.params.stepName || `步骤${this.stepNumber}`;
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.selectedFile = file;
        this.selectedFileName = file.name;
      }
    },
    removeFile() {
      this.selectedFile = null;
      this.selectedFileName = '';
      this.uploadData.fileUrl = '';
      // 重置文件输入框
      this.$refs.fileInput.value = '';
    },
    async uploadFile() {
      if (!this.selectedFile) {
        return '';
      }

      const formData = new FormData();
      formData.append('file', this.selectedFile);

      try {
        const response = await axios.post('http://localhost:8080/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        return response.data;
      } catch (error) {
        console.error('文件上传失败:', error);
        throw error;
      }
    },
    async submitUpload() {
      this.submitted = true;

      // 表单验证
      if (!this.uploadData.title || !this.uploadData.content || !this.selectedFile) {
        return;
      }

      this.submitting = true;

      try {
        // 上传文件
        this.uploadData.fileUrl = await this.uploadFile();

        // 提交数据
        await axios.post(`http://localhost:8080/api/projects/${this.projectId}/order-training/step/${this.stepNumber}`, {
          title: this.uploadData.title,
          content: this.uploadData.content,
          fileUrl: this.uploadData.fileUrl,
          userId: localStorage.getItem('userId')
        });

        // 如果是企业用户且不是最后一步，自动更新项目步骤
        if (localStorage.getItem('userRole') === 'enterprise' && this.stepNumber < 4) {
          await axios.put(`http://localhost:8080/api/projects/${this.projectId}`, {
            currentStep: this.stepNumber + 1
          });
        }

        // 提交成功，返回项目详情页
        this.$router.push({
          name: 'OrderTrainingDetail',
          params: { projectId: this.projectId }
        });

        // 显示成功消息
        alert('提交成功！');
      } catch (error) {
        console.error('提交失败:', error);
        alert('提交失败，请重试');
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
.order-training-upload-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.back-btn {
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 20px;
  display: flex;
  align-items: center;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
}

.form-group {
  margin-bottom: 25px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #303133;
}

.required {
  color: #F56C6C;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #409EFF;
}

.form-control.is-invalid {
  border-color: #F56C6C;
}

.invalid-feedback {
  color: #F56C6C;
  font-size: 12px;
  margin-top: 5px;
}

.form-text {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.file-upload-container {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.file-input {
  display: none;
}

.file-upload-btn {
  display: inline-flex;
  align-items: center;
  padding: 8px 15px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.file-upload-btn:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
}

.upload-icon {
  margin-right: 5px;
}

.file-name {
  margin-left: 10px;
  color: #606266;
  word-break: break-all;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f2f5;
  padding: 10px 15px;
  border-radius: 6px;
  margin-top: 15px;
}

.file-info {
  display: flex;
  align-items: center;
}

.file-icon {
  font-size: 20px;
  margin-right: 10px;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.remove-btn {
  background-color: #fef0f0;
  color: #f56c6c;
}

.remove-btn:hover {
  background-color: #fde2e2;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  padding: 12px 30px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
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

@media (max-width: 768px) {
  .file-preview {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-actions {
    margin-top: 10px;
  }
}
</style>
