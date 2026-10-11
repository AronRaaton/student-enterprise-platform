<template>
  <div class="challenge-submission-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">提交解决方案</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 挑战信息 -->
    <div v-else class="submission-content">
      <div class="challenge-info">
        <h2 class="challenge-title">{{ challenge.title }}</h2>
        <div class="challenge-meta">
          <div class="meta-item">
            <span class="meta-label">企业:</span>
            <span class="meta-value">{{ challenge.enterpriseName }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">难度:</span>
            <span class="meta-value">
              <span v-for="star in 5" :key="star" class="difficulty-star" :class="{ active: star <= challenge.difficulty }">★</span>
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">截止时间:</span>
            <span class="meta-value">{{ challenge.deadline ? formatDate(challenge.deadline) : '无截止日期' }}</span>
          </div>
        </div>
      </div>

      <!-- 提交表单 -->
      <div class="submission-form">
        <h3 class="form-title">解决方案</h3>
        
        <div class="form-group">
          <label for="solutionDescription">解决方案描述 <span class="required">*</span></label>
          <textarea
            id="solutionDescription"
            v-model="submission.solutionDescription"
            placeholder="请详细描述您的解决方案，包括思路、实现方法和结果"
            class="form-control"
            rows="8"
            :class="{ 'is-invalid': submitted && !submission.solutionDescription }"
          ></textarea>
          <div v-if="submitted && !submission.solutionDescription" class="invalid-feedback">
            请输入解决方案描述
          </div>
        </div>

        <div class="form-group">
          <label for="solutionUrl">解决方案链接</label>
          <input
            type="text"
            id="solutionUrl"
            v-model="submission.solutionUrl"
            placeholder="请提供解决方案的链接，如GitHub仓库、在线演示等（可选）"
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label for="file">上传文件</label>
          <div class="file-upload">
            <input
              type="file"
              id="file"
              ref="fileInput"
              @change="handleFileChange"
              class="file-input"
            />
            <div class="file-upload-ui">
              <button class="file-select-btn" @click="triggerFileInput">选择文件</button>
              <span class="file-name">{{ fileName || '未选择文件' }}</span>
            </div>
          </div>
          <p class="file-hint">支持上传代码、文档、图片等文件（最大20MB）</p>
        </div>

        <div class="form-actions">
          <button class="cancel-btn" @click="goBack">取消</button>
          <button class="submit-btn" @click="submitSolution" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : '提交解决方案' }}
          </button>
        </div>
      </div>

      <!-- 成功提示 -->
      <div v-if="showSuccess" class="success-message">
        <div class="success-content">
          <div class="success-icon">✓</div>
          <h2>解决方案提交成功！</h2>
          <p>您的解决方案已成功提交，请等待企业审核</p>
          <div class="success-actions">
            <button class="primary-btn" @click="viewChallenges">查看所有挑战</button>
            <button class="secondary-btn" @click="viewChallengeDetails">查看挑战详情</button>
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
      challenge: {},
      submission: {
        studentId: null,
        challengeId: null,
        solutionDescription: '',
        solutionUrl: '',
      },
      file: null,
      fileName: '',
      loading: true,
      submitted: false,
      isSubmitting: false,
      showSuccess: false
    };
  },
  created() {
    this.submission.studentId = localStorage.getItem('userId');
    this.submission.challengeId = this.$route.params.challengeId;
    this.fetchChallengeDetails();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchChallengeDetails() {
      this.loading = true;
      
      try {
        const response = await axios.get(`http://localhost:8080/api/challenges/${this.submission.challengeId}`);
        this.challenge = response.data;
      } catch (error) {
        console.error('获取挑战详情失败:', error);
        alert('获取挑战详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        // 检查文件大小（20MB限制）
        if (file.size > 20 * 1024 * 1024) {
          alert('文件大小不能超过20MB');
          this.$refs.fileInput.value = '';
          this.file = null;
          this.fileName = '';
          return;
        }
        
        this.file = file;
        this.fileName = file.name;
      } else {
        this.file = null;
        this.fileName = '';
      }
    },
    async submitSolution() {
      this.submitted = true;

      // 表单验证
      if (!this.submission.solutionDescription) {
        return;
      }

      this.isSubmitting = true;

      try {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('studentId', this.submission.studentId);
        formData.append('solutionDescription', this.submission.solutionDescription);
        
        if (this.submission.solutionUrl) {
          formData.append('solutionUrl', this.submission.solutionUrl);
        }
        
        if (this.file) {
          formData.append('file', this.file);
        }
        
        // 发送请求
        await axios.post(
          `http://localhost:8080/api/challenges/${this.submission.challengeId}/submit`, 
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }
        );
        
        this.showSuccess = true;
      } catch (error) {
        console.error('提交解决方案失败:', error);
        alert('提交解决方案失败，请稍后重试');
      } finally {
        this.isSubmitting = false;
      }
    },
    viewChallenges() {
      this.$router.push({ name: 'StudentChallenges' });
    },
    viewChallengeDetails() {
      this.$router.push({ 
        name: 'TechnicalChallengeDetail', 
        params: { challengeId: this.submission.challengeId }
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.challenge-submission-container {
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

/* 提交内容 */
.submission-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.challenge-info {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.challenge-title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 20px 0;
}

.challenge-meta {
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
  display: flex;
  align-items: center;
}

.difficulty-star {
  font-size: 16px;
  color: #dcdfe6;
  margin-right: 2px;
}

.difficulty-star.active {
  color: #f7ba2a;
}

/* 提交表单 */
.submission-form {
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

/* 文件上传 */
.file-upload {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.file-input {
  display: none;
}

.file-upload-ui {
  display: flex;
  align-items: center;
  width: 100%;
}

.file-select-btn {
  padding: 8px 16px;
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  margin-right: 15px;
}

.file-select-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

.file-name {
  color: #606266;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-hint {
  margin: 5px 0 0 0;
  font-size: 12px;
  color: #909399;
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
</style>
