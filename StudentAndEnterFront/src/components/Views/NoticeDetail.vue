<template>
  <div class="notice-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="backToHome">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">公告详情</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 公告内容 -->
    <div v-else class="notice-content">
      <div class="notice-header">
        <h1 class="notice-title">{{ notice.title }}</h1>
        <div class="notice-meta">
          <div class="meta-item">
            <span class="meta-label">发布企业:</span>
            <span class="meta-value">{{ notice.enterprise ? notice.enterprise.name : '未知企业' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">发布时间:</span>
            <span class="meta-value">{{ formatDate(notice.createdTime) }}</span>
          </div>
        </div>
      </div>

      <div class="notice-body">
        <p>{{ notice.content }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      notice: {},
      loading: true,
    };
  },
  created() {
    const noticeId = this.$route.params.noticeId;
    this.fetchNoticeDetails(noticeId);
  },
  methods: {
    fetchNoticeDetails(noticeId) {
      this.loading = true;
      axios
        .get(`http://localhost:8080/api/notice/${noticeId}`)
        .then((response) => {
          // 直接使用返回的公告数据
          const notice = response.data;
          console.log('Notice data:', notice); // 调试日志
          this.notice = notice;
          this.loading = false;
        })
        .catch((error) => {
          console.error("无法获取公告详情:", error);
          this.loading = false;
        });
    },
    backToHome() {
      // 返回上一页，而不是固定返回学生主页
      this.$router.go(-1);
    },

    formatDate(date) {
      if (!date) return '未知时间';
      const dateObj = new Date(date);
      return dateObj.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
  },
};
</script>

<style scoped>
.notice-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #1677ff;
  cursor: pointer;
  font-size: 16px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: rgba(22, 119, 255, 0.1);
}

.back-icon {
  margin-right: 8px;
  font-size: 18px;
}

.page-title {
  margin: 0 auto;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 加载中状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(22, 119, 255, 0.1);
  border-radius: 50%;
  border-top-color: #1677ff;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 公告内容样式 */
.notice-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.notice-header {
  padding: 24px 32px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f8f9fa;
}

.notice-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.notice-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  font-weight: 500;
  margin-right: 8px;
}

.meta-value {
  color: #909399;
}

.notice-body {
  padding: 32px;
  line-height: 1.8;
  font-size: 16px;
  color: #303133;
}

.notice-body p {
  margin: 0 0 16px;
  white-space: pre-line;
}
</style>
