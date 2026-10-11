<template>
  <div class="liked-posts-container module-page module-list">
    <div class="page-header">
      <button class="back-btn" @click="goBack">返回</button>
      <h1 class="page-title">我喜欢的帖子</h1>
    </div>

    <div class="posts-container">
      <div v-if="loading" class="loading-indicator">
        加载中...
      </div>
      <div v-else-if="posts.length === 0" class="empty-state">
        <div class="empty-icon">💔</div>
        <p>您还没有喜欢任何帖子</p>
        <button class="action-btn" @click="goToForum">浏览论坛</button>
      </div>
      <div v-else class="posts-list">
        <div
          v-for="post in posts"
          :key="post.postId"
          class="post-card"
          @click="viewPostDetails(post.postId)"
        >
          <div class="post-header">
            <h3 class="post-title">
              <span v-if="post.isPinned" class="pin-icon">📌</span>
              {{ post.title }}
            </h3>
            <div class="post-meta">
              <span class="post-author">{{ post.authorName }}</span>
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
          <div class="post-content-preview">{{ truncateContent(post.content) }}</div>
          <div class="post-footer">
            <div class="post-category" v-if="post.categoryDisplayName">
              <span class="category-tag">{{ post.categoryDisplayName }}</span>
              <span class="subcategory-tag" v-if="post.subcategoryDisplayName">{{ post.subcategoryDisplayName }}</span>
            </div>
            <div class="post-stats">
              <span class="views">👁️ {{ post.viewCount }}</span>
              <span class="likes">❤️ {{ post.likeCount }}</span>
              <span class="comments">💬 {{ post.commentCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination-controls" v-if="totalPages > 1">
        <button
          class="pagination-btn"
          :disabled="currentPage === 0"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button
          class="pagination-btn"
          :disabled="currentPage >= totalPages - 1"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'LikedPosts',
  data() {
    return {
      posts: [],
      loading: true,
      currentPage: 0,
      totalPages: 0,
      userId: '',
      userType: ''
    };
  },
  created() {
    this.getUserInfo();
    this.fetchLikedPosts();
  },
  methods: {
    getUserInfo() {
      // 首先检查URL查询参数中是否有用户信息
      const queryUserType = this.$route.query.userType;
      const queryUserId = this.$route.query.userId;

      if (queryUserType && queryUserId) {
        // 优先使用URL中的用户信息
        this.userType = queryUserType;
        this.userId = queryUserId;
        console.log('从 URL 查询参数获取用户信息:', { userType: this.userType, userId: this.userId });
        return;
      }

      // 如果 URL 中没有用户信息，则从本地存储获取
      const userRole = localStorage.getItem('userRole');
      const userId = localStorage.getItem('userId');
      const role = localStorage.getItem('role');

      if (userRole === 'student') {
        this.userType = 'STUDENT';
        this.userId = userId;
      } else if (userRole === 'enterprise') {
        this.userType = 'ENTERPRISE';
        this.userId = userId;
      } else if (role === 'student') {
        this.userType = 'STUDENT';
        this.userId = userId;
      } else if (role === 'enterprise') {
        this.userType = 'ENTERPRISE';
        this.userId = userId;
      }

      if (!this.userId || !this.userType) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }

      console.log('从本地存储获取用户信息:', { userType: this.userType, userId: this.userId });
    },
    async fetchLikedPosts() {
      this.loading = true;
      try {
        const response = await axios.get('http://localhost:8080/api/forum/user/liked-posts', {
          params: {
            userId: this.userId,
            userType: this.userType,
            page: this.currentPage,
            size: 10
          }
        });

        this.posts = response.data.content;
        this.totalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取喜欢的帖子失败:', error);
      } finally {
        this.loading = false;
      }
    },
    changePage(page) {
      this.currentPage = page;
      this.fetchLikedPosts();
    },
    viewPostDetails(postId) {
      this.$router.push({
        name: 'ForumPost',
        params: { postId },
        query: {
          userType: this.userType,
          userId: this.userId
        }
      });
    },
    goToForum() {
      this.$router.push({
        name: 'ForumHome',
        query: {
          userType: this.userType,
          userId: this.userId
        }
      });
    },
    goBack() {
      this.$router.go(-1);
    },
    truncateContent(content) {
      if (content.length > 100) {
        return content.substring(0, 100) + '...';
      }
      return content;
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
.liked-posts-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  margin-right: 15px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #e0e0e0;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.posts-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.loading-indicator {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.post-header {
  margin-bottom: 12px;
}

.post-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
}

.pin-icon {
  margin-right: 8px;
}

.post-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.post-author {
  margin-right: 16px;
  font-weight: 500;
}

.post-content-preview {
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-category {
  display: flex;
  gap: 8px;
}

.category-tag, .subcategory-tag {
  background-color: #f0f2f5;
  color: #606266;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 16px;
}

.pagination-btn {
  background-color: #f0f2f5;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn:not(:disabled):hover {
  background-color: #e0e0e0;
}

.page-info {
  font-size: 14px;
  color: #606266;
}
</style>
