<template>
  <div class="all-notices-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">公告中心</h1>
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索公告..."
          class="search-input"
          @input="filterNotices"
        />
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="content-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredNotices.length === 0" class="empty-state">
        <div class="empty-icon">📢</div>
        <h3>暂无公告</h3>
        <p>当前没有可用的公告</p>
      </div>

      <div v-else class="notices-grid">
        <div v-for="notice in filteredNotices" :key="notice.noticeId" class="notice-card" @click="viewNoticeDetails(notice.noticeId)">
          <div class="notice-header">
            <h3 class="notice-title">{{ notice.title }}</h3>
            <span class="notice-date">{{ formatDate(notice.createdTime) }}</span>
          </div>

          <div class="notice-content">
            <p class="enterprise-info">
              <span class="info-label">发布企业:</span>
              <span class="info-value">{{ notice.enterprise && notice.enterprise.name || '未知企业' }}</span>
            </p>
            <div class="notice-text">
              <p>{{ truncateText(notice.content, 200) }}</p>
            </div>
          </div>

          <div class="notice-footer">
            <button class="view-button">查看详情</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      notices: [],
      filteredNotices: [],
      searchQuery: "",
      loading: true
    };
  },
  created() {
    this.fetchAllNotices();
  },
  methods: {
    fetchAllNotices() {
      this.loading = true;
      axios
        .get("http://localhost:8080/api/login/notice/all")
        .then((response) => {
          this.notices = response.data;
          this.filteredNotices = [...this.notices];
          this.loading = false;
        })
        .catch((error) => {
          console.error("无法获取公告:", error);
          this.loading = false;
        });
    },
    viewNoticeDetails(noticeId) {
      this.$router.push({ name: "notice-detail", params: { noticeId } });
    },
    goBack() {
      this.$router.go(-1);
    },
    filterNotices() {
      if (!this.searchQuery.trim()) {
        this.filteredNotices = [...this.notices];
        return;
      }

      const query = this.searchQuery.toLowerCase();
      this.filteredNotices = this.notices.filter(notice => {
        return (
          (notice.title && notice.title.toLowerCase().includes(query)) ||
          (notice.content && notice.content.toLowerCase().includes(query)) ||
          (notice.enterprise && notice.enterprise.name && notice.enterprise.name.toLowerCase().includes(query))
        );
      });
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }
  },
};
</script>

<style scoped>
.all-notices-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

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

.search-container {
  position: absolute;
  right: 0;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 200px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #409EFF;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
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
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 50px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-state h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #303133;
}

.empty-state p {
  color: #606266;
  margin-bottom: 20px;
}

.notices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.notice-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  display: flex;
  flex-direction: column;
  height: 100%;
  cursor: pointer;
}

.notice-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.notice-header {
  padding: 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
}

.notice-date {
  font-size: 12px;
  color: #909399;
  background-color: #f4f4f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.notice-content {
  padding: 15px;
  flex: 1;
}

.enterprise-info {
  margin-bottom: 10px;
}

.info-label {
  color: #909399;
  margin-right: 5px;
}

.info-value {
  color: #606266;
  font-weight: 500;
}

.notice-text {
  color: #606266;
  line-height: 1.5;
}

.notice-footer {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.view-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
}

.view-button:hover {
  background-color: #66b1ff;
}

@media (max-width: 768px) {
  .notices-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .back-button {
    position: static;
    margin-bottom: 15px;
  }

  .page-title {
    text-align: left;
    margin-bottom: 15px;
  }

  .search-container {
    position: static;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }
}
</style>
