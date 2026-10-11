<template>
  <div class="create-notice">
    <h1>发布公告</h1>
    <form @submit.prevent="submitAnnouncement">
      <div class="form-group">
        <label for="title">公告标题</label>
        <input v-model="notice.title" type="text" id="title" placeholder="请输入公告标题" required />
      </div>
      <div class="form-group">
        <label for="content">公告内容</label>
        <textarea v-model="notice.content" id="content" placeholder="请输入公告内容" required rows="10"></textarea>
      </div>
      <div class="form-group">
        <label for="coverImage">上传封面</label>
        <input type="file" id="coverImage" @change="handleFileUpload"/>
      </div>
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '发布中...' : '发布公告' }}</button>
    </form>

    <!-- 添加提示消息 -->
    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      notice: {
        title: '',
        content: ''
      },
      isSubmitting: false,
      message: '',
      messageType: 'success',
      coverImage: null
    };
  },
  methods: {
    async submitAnnouncement() {
      if (!this.notice.title || !this.notice.content) {
        this.showMessage('请填写完整的公告信息', 'error');
        return;
      }

      this.isSubmitting = true;
      const enterpriseId = localStorage.getItem('userId');

      try {
        const response = await axios.post(
          `http://localhost:8080/api/enterprises/${enterpriseId}/announcements`,
          this.notice
        );

        this.showMessage('公告发布成功！', 'success');
        this.resetForm();
        
        // 可选：3秒后返回企业主页
        setTimeout(() => {
          this.$router.push({ name: 'enterprise-home' });
        }, 3000);
      } catch (error) {
        console.error('发布公告失败:', error);
        this.showMessage('发布公告失败，请稍后重试', 'error');
      } finally {
        this.isSubmitting = false;
      }
    },

    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.coverImage = file;
        // 如果需要预览图片，可以在这里添加预览功能
      }
    },

    showMessage(text, type = 'success') {
      this.message = text;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
      }, 3000);
    },

    resetForm() {
      this.notice = {
        title: '',
        content: ''
      };
      this.coverImage = null;
      if (document.getElementById('coverImage')) {
        document.getElementById('coverImage').value = '';
      }
    }
  }
};
</script>

<style scoped>
.create-notice {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
}

input, textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

textarea {
  resize: vertical;
}

button {
  padding: 0.8rem 2rem;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #40a9ff;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 4px;
  text-align: center;
}

.success {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.error {
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
}
</style>
