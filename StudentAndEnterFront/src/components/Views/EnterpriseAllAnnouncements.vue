<template>
  <div class="enterprise-all-announcements-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">我发布的公告</h1>
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索公告..."
          class="search-input"
          @input="filterAnnouncements"
        />
      </div>
    </div>

    <!-- 状态统计 -->
    <div class="status-statistics">
      <div class="stat-card" @click="filterByStatus(null)" :class="{ active: currentStatusFilter === null }">
        <div class="stat-value">{{ announcements.length }}</div>
        <div class="stat-label">全部</div>
      </div>
      <div class="stat-card" @click="filterByStatus('DRAFT')" :class="{ active: currentStatusFilter === 'DRAFT' }">
        <div class="stat-value">{{ getStatusCount('DRAFT') }}</div>
        <div class="stat-label">草稿</div>
      </div>
      <div class="stat-card" @click="filterByStatus('PENDING')" :class="{ active: currentStatusFilter === 'PENDING' }">
        <div class="stat-value">{{ getStatusCount('PENDING') }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card" @click="filterByStatus('APPROVED')" :class="{ active: currentStatusFilter === 'APPROVED' }">
        <div class="stat-value">{{ getStatusCount('APPROVED') }}</div>
        <div class="stat-label">已通过</div>
      </div>
      <div class="stat-card" @click="filterByStatus('REJECTED')" :class="{ active: currentStatusFilter === 'REJECTED' }">
        <div class="stat-value">{{ getStatusCount('REJECTED') }}</div>
        <div class="stat-label">已拒绝</div>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="content-container">
      <div class="action-bar">
        <button class="publish-button" @click="goToAnnouncementPublish">发布新公告</button>
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredAnnouncements.length === 0" class="empty-state">
        <div class="empty-icon">📢</div>
        <h3>暂无公告</h3>
        <p>您还没有发布任何公告</p>
        <button class="action-btn" @click="goToAnnouncementPublish">发布公告</button>
      </div>

      <div v-else class="announcements-grid">
        <div v-for="announcement in displayedAnnouncements" :key="announcement.noticeId" class="announcement-card">
          <div class="announcement-header">
            <h3 class="announcement-title">{{ announcement.title }}</h3>
            <span class="announcement-date">{{ formatDate(announcement.createdTime || announcement.createTime) }}</span>
          </div>
          <div class="announcement-content">
            <p>{{ truncateText(announcement.content, 150) }}</p>
          </div>
          <div class="announcement-footer">
            <span class="announcement-status" :class="getStatusClass(announcement.status)">
              {{ getStatusText(announcement.status) }}
            </span>
            <button v-if="announcement.status === 'REJECTED'" class="action-btn small" @click="editRejectedAnnouncement(announcement)">编辑重新提交</button>
            <button v-else-if="announcement.status === 'DRAFT'" class="action-btn small draft-edit-btn" @click="editDraftAnnouncement(announcement)">前往发布</button>
            <button v-else class="action-btn small" @click="viewAnnouncementDetails(announcement.noticeId)">查看详情</button>
            <button class="delete-btn small" @click.stop="deleteAnnouncement(announcement.noticeId)">删除</button>
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
      announcements: [],
      filteredAnnouncements: [],
      searchQuery: "",
      loading: true,
      enterpriseId: null,
      currentStatusFilter: null
    };
  },
  computed: {
    displayedAnnouncements() {
      return this.filteredAnnouncements;
    }
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchAnnouncements();
  },
  methods: {
    fetchAnnouncements() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements`)
        .then(response => {
          this.announcements = response.data;
          this.filteredAnnouncements = [...this.announcements];
          this.loading = false;
        })
        .catch(error => {
          console.error('无法获取公告列表:', error);
          this.loading = false;
        });
    },
    viewAnnouncementDetails(noticeId) {
      this.$router.push({ name: 'notice-detail', params: { noticeId } });
    },

    editRejectedAnnouncement(announcement) {
      // 将被拒绝的公告信息保存到localStorage，然后跳转到编辑页面
      localStorage.setItem('editingAnnouncement', JSON.stringify({
        noticeId: announcement.noticeId,
        title: announcement.title,
        content: announcement.content,
        feedback: announcement.feedback,
        status: announcement.status
      }));
      this.$router.push({ name: 'enterprise-notice-pub', query: { edit: 'rejected' } });
    },

    editDraftAnnouncement(announcement) {
      // 将草稿公告信息保存到localStorage，然后跳转到编辑页面
      localStorage.setItem('editingAnnouncement', JSON.stringify({
        noticeId: announcement.noticeId,
        title: announcement.title,
        content: announcement.content,
        status: announcement.status
      }));
      this.$router.push({ name: 'enterprise-notice-pub', query: { edit: 'draft' } });
    },

    deleteAnnouncement(noticeId) {
      if (!confirm('确定要删除该公告吗？此操作不可恢复。')) {
        return;
      }

      axios.delete(`http://localhost:8080/api/enterprises/${this.enterpriseId}/announcements/${noticeId}`)
        .then(() => {
          alert('公告删除成功');
          // 从列表中移除该公告
          this.announcements = this.announcements.filter(a => a.noticeId !== noticeId);
          this.filterAnnouncements();
        })
        .catch(error => {
          console.error('删除公告失败:', error);
          alert('删除公告失败，请重试');
        });
    },
    goToAnnouncementPublish() {
      this.$router.push({ name: 'enterprise-notice-pub' });
    },
    goBack() {
      this.$router.go(-1);
    },
    filterAnnouncements() {
      // 先应用状态筛选
      let result = [...this.announcements];

      if (this.currentStatusFilter) {
        result = result.filter(announcement => announcement.status === this.currentStatusFilter);
      }

      // 再应用搜索筛选
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(announcement => {
          return (
            (announcement.title && announcement.title.toLowerCase().includes(query)) ||
            (announcement.content && announcement.content.toLowerCase().includes(query))
          );
        });
      }

      this.filteredAnnouncements = result;
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    filterByStatus(status) {
      this.currentStatusFilter = status;
      this.filterAnnouncements();
    },

    getStatusCount(status) {
      return this.announcements.filter(announcement => announcement.status === status).length;
    },

    getStatusClass(status) {
      if (!status) return '';
      switch (status) {
        case 'DRAFT':
          return 'status-draft';
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
      if (!status) return '未知状态';
      switch (status) {
        case 'DRAFT':
          return '草稿';
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已通过';
        case 'REJECTED':
          return '已拒绝';
        default:
          return '未知状态';
      }
    }
  }
};
</script>

<style scoped>
.enterprise-all-announcements-container {
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

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.publish-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.publish-button:hover {
  background-color: #66b1ff;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  text-align: center;
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

.action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.action-btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.draft-edit-btn {
  background-color: #909399;
}

.draft-edit-btn:hover {
  background-color: #a6a9ad;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-left: 10px;
}

.delete-btn:hover {
  background-color: #f78989;
}

.delete-btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.announcements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.announcement-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.announcement-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.announcement-header {
  padding: 15px;
  background-color: #ecf5ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.announcement-date {
  font-size: 12px;
  color: #909399;
}

.announcement-content {
  padding: 15px;
  color: #606266;
  font-size: 14px;
  min-height: 100px;
}

.announcement-footer {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-draft {
  background-color: #f4f4f5;
  color: #909399;
}

.status-pending {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-approved {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

/* 状态统计样式 */
.status-statistics {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border: 2px solid #409EFF;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .back-button {
    position: static;
    margin-bottom: 10px;
  }

  .page-title {
    text-align: left;
  }

  .search-container {
    position: static;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .announcements-grid {
    grid-template-columns: 1fr;
  }
}
</style>
