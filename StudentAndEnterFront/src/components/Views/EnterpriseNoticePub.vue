<template>
  <div class="notice-publish-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">发布公告</h1>
    </div>

    <!-- 公告表单 -->
    <div class="form-container">
      <div class="form-section">
        <h2 class="section-title">公告信息</h2>

        <!-- 拒绝原因显示区域 -->
        <div v-if="isEditingRejected" class="rejection-feedback">
          <h3>拒绝原因</h3>
          <p>{{ announcement.feedback || '管理员没有提供拒绝原因' }}</p>
        </div>

        <div class="form-group">
          <label for="title">公告标题 <span class="required">*</span></label>
          <input
            type="text"
            id="title"
            v-model="announcement.title"
            placeholder="请输入公告标题（简洁明了）"
            :class="{ 'error': validationErrors.title }"
          />
          <span v-if="validationErrors.title" class="error-message">{{ validationErrors.title }}</span>
        </div>

        <div class="form-group">
          <label for="content">公告内容 <span class="required">*</span></label>
          <textarea
            id="content"
            v-model="announcement.content"
            rows="10"
            placeholder="请输入公告内容..."
            :class="{ 'error': validationErrors.content }"
          ></textarea>
          <span v-if="validationErrors.content" class="error-message">{{ validationErrors.content }}</span>
        </div>
      </div>

      <div class="form-actions">
        <button class="cancel-btn" @click="goBack">取消</button>
        <button
          class="draft-btn"
          @click="saveDraft"
          :disabled="publishing || saving"
        >
          <span v-if="saving" class="loading-icon"></span>
          <span v-else>保存为草稿</span>
        </button>
        <button
          class="submit-btn"
          @click="submitAnnouncement"
          :disabled="publishing || saving"
        >
          <span v-if="publishing" class="loading-icon"></span>
          <span v-else>发布公告</span>
        </button>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccess" class="success-message">
      <div class="success-icon">✓</div>
      <h3>公告发布成功！</h3>
      <p>您可以在企业主页查看已发布的公告</p>
      <div class="success-actions">
        <button class="primary-btn" @click="goToHome">返回主页</button>
        <button class="secondary-btn" @click="publishAnother">继续发布</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      announcement: {
        title: '',
        content: '',
        noticeId: null,
        feedback: ''
      },
      validationErrors: {
        title: '',
        content: ''
      },
      publishing: false,
      saving: false,
      showSuccess: false,
      enterpriseId: null,
      hasDraft: false,
      isEditingRejected: false
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');

    // 检查是否是编辑被拒绝的公告或草稿
    if (this.$route.query.edit === 'rejected') {
      this.loadEditingAnnouncement('rejected');
    } else if (this.$route.query.edit === 'draft') {
      this.loadEditingAnnouncement('draft');
    } else {
      this.checkForDraft();
    }
  },
  methods: {
    validateForm() {
      let isValid = true;
      this.validationErrors = {
        title: '',
        content: ''
      };

      if (!this.announcement.title.trim()) {
        this.validationErrors.title = '请输入公告标题';
        isValid = false;
      } else if (this.announcement.title.length > 100) {
        this.validationErrors.title = '公告标题不能超过100个字符';
        isValid = false;
      }

      if (!this.announcement.content.trim()) {
        this.validationErrors.content = '请输入公告内容';
        isValid = false;
      } else if (this.announcement.content.length > 2000) {
        this.validationErrors.content = '公告内容不能超过2000个字符';
        isValid = false;
      }

      return isValid;
    },
    // 加载要编辑的公告（被拒绝或草稿）
    loadEditingAnnouncement(type) {
      const editingAnnouncement = localStorage.getItem('editingAnnouncement');
      if (editingAnnouncement) {
        try {
          const announcementData = JSON.parse(editingAnnouncement);
          this.announcement = {
            title: announcementData.title || '',
            content: announcementData.content || '',
            noticeId: announcementData.noticeId,
            feedback: announcementData.feedback || ''
          };

          if (type === 'rejected') {
            this.isEditingRejected = true;
            console.log('加载到被拒绝的公告:', this.announcement);
          } else if (type === 'draft') {
            this.hasDraft = true;
            console.log('加载到草稿公告:', this.announcement);
          }

          // 清除localStorage中的数据
          localStorage.removeItem('editingAnnouncement');
        } catch (e) {
          console.error('解析公告数据失败:', e);
        }
      }
    },

    // 检查是否有草稿
    checkForDraft() {
      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements/draft`)
        .then(response => {
          // 如果有草稿，加载草稿内容
          this.announcement = {
            title: response.data.title,
            content: response.data.content,
            noticeId: response.data.noticeId
          };
          this.hasDraft = true;
          console.log('加载到草稿:', this.announcement);
        })
        .catch(error => {
          if (error.response && error.response.status === 404) {
            console.log('没有找到草稿');
          } else {
            console.error('获取草稿失败:', error);
          }
        });
    },

    // 保存为草稿
    saveDraft() {
      if (!this.validateForm()) {
        // 滚动到第一个错误
        const firstError = document.querySelector('.error');
        if (firstError) {
          firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return;
      }

      this.saving = true;

      // 如果已经有草稿，更新草稿
      if (this.hasDraft && this.announcement.noticeId) {
        axios.put(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements/draft/${this.announcement.noticeId}`, {
          title: this.announcement.title,
          content: this.announcement.content
        })
          .then(response => {
            alert('草稿已更新');
            this.announcement.noticeId = response.data.noticeId;
          })
          .catch(error => {
            console.error('更新草稿失败:', error);
            alert('更新草稿失败，请重试');
          })
          .finally(() => {
            this.saving = false;
          });
      } else {
        // 创建新草稿
        axios.post(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements/draft`, {
          title: this.announcement.title,
          content: this.announcement.content
        })
          .then(response => {
            alert('草稿已保存');
            this.announcement.noticeId = response.data.noticeId;
            this.hasDraft = true;
          })
          .catch(error => {
            console.error('保存草稿失败:', error);
            alert('保存草稿失败，请重试');
          })
          .finally(() => {
            this.saving = false;
          });
      }
    },

    // 发布公告
    submitAnnouncement() {
      if (!this.validateForm()) {
        // 滚动到第一个错误
        const firstError = document.querySelector('.error');
        if (firstError) {
          firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return;
      }

      this.publishing = true;

      // 如果是从草稿发布，则提交草稿进行审核
      if (this.hasDraft && this.announcement.noticeId) {
        axios.post(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements/draft/${this.announcement.noticeId}/submit`)
          .then(response => {
            this.showSuccess = true;
            this.hasDraft = false;
            this.announcement.noticeId = null;
          })
          .catch(error => {
            console.error('提交草稿失败:', error);
            alert('提交草稿失败，请重试');
          })
          .finally(() => {
            this.publishing = false;
          });
      } else {
        // 直接创建新公告
        axios.post(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements`, {
          title: this.announcement.title,
          content: this.announcement.content
        })
          .then(response => {
            this.showSuccess = true;
          })
          .catch(error => {
            console.error('发布公告失败:', error);
            alert('发布公告失败，请重试');
          })
          .finally(() => {
            this.publishing = false;
          });
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    goToHome() {
      this.$router.push({ name: 'enterprise-home' });
    },
    publishAnother() {
      this.announcement = {
        title: '',
        content: '',
        noticeId: null
      };
      this.hasDraft = false;
      this.showSuccess = false;
    }
  },
};
</script>

<style scoped>
.notice-publish-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
  position: relative;
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
  margin-top: 0;
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

/* 表单容器 */
.form-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
}

.form-section {
  margin-bottom: 30px;
}

.rejection-feedback {
  background-color: #fef0f0;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
  border-left: 4px solid #f56c6c;
}

.rejection-feedback h3 {
  color: #f56c6c;
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 10px;
}

.rejection-feedback p {
  color: #606266;
  margin: 0;
  line-height: 1.5;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409EFF;
}

.form-group input.error,
.form-group textarea.error {
  border-color: #f56c6c;
}

.error-message {
  display: block;
  font-size: 12px;
  color: #f56c6c;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.draft-btn {
  background-color: #909399;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  margin-top: 0;
}

.draft-btn:hover:not(:disabled) {
  background-color: #a6a9ad;
}

.draft-btn:disabled {
  background-color: #c8c9cc;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f0f2f5;
  color: #606266;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 0;
}

.cancel-btn:hover {
  background-color: #e6e8eb;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  margin-top: 0;
}

.submit-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.loading-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 成功提示 */
.success-message {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 30px;
  text-align: center;
  width: 90%;
  max-width: 400px;
  z-index: 1000;
}

.success-icon {
  width: 60px;
  height: 60px;
  background-color: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
  margin: 0 auto 20px;
}

.success-message h3 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 10px 0;
}

.success-message p {
  font-size: 14px;
  color: #606266;
  margin: 0 0 20px 0;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.primary-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 0;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  background-color: #f0f2f5;
  color: #606266;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 0;
}

.secondary-btn:hover {
  background-color: #e6e8eb;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .form-container {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .submit-btn, .cancel-btn {
    width: 100%;
  }
}
</style>
