<template>
  <div class="forum-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">交流论坛</h1>
      <div></div> <!-- 占位，保持布局平衡 -->
    </div>

    <!-- 主要内容区域 -->
    <div class="content-area">
        <div class="forum-header">
          <div class="forum-actions">
            <div class="search-box">
              <input
                type="text"
                v-model="searchQuery"
                placeholder="搜索帖子..."
                @keyup.enter="searchPosts"
              />
              <button class="search-btn" @click="searchPosts">搜索</button>
            </div>
            <div class="action-buttons">
              <button class="liked-posts-btn" @click="goToLikedPosts">我喜欢的帖子</button>
              <button class="create-post-btn" @click="goToCreatePost">发布帖子</button>
            </div>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div class="category-tabs">
            <div
              class="category-tab"
              :class="{ active: activeCategory === null }"
              @click="setCategory(null)"
            >
              全部
            </div>
            <div
              v-for="category in categories"
              :key="category.value"
              class="category-tab"
              :class="{ active: activeCategory === category.value }"
              @click="setCategory(category.value)"
            >
              {{ category.label }}
            </div>
          </div>

          <!-- 子分类 -->
          <div class="subcategory-tabs" v-if="activeCategory && subcategories.length > 0">
            <div
              class="subcategory-tab"
              :class="{ active: activeSubcategory === null }"
              @click="setSubcategory(null)"
            >
              全部
            </div>
            <div
              v-for="subcategory in subcategories"
              :key="subcategory.value"
              class="subcategory-tab"
              :class="{ active: activeSubcategory === subcategory.value }"
              @click="setSubcategory(subcategory.value)"
            >
              {{ subcategory.label }}
            </div>
          </div>
        </div>

        <div class="forum-content">
          <!-- 热门帖子 -->
          <div class="popular-posts-section" v-if="!isSearching && !activeCategory">
            <h2 class="section-title">热门帖子</h2>
            <div class="popular-posts-list">
              <div
                v-for="post in popularPosts"
                :key="post.postId"
                class="popular-post-card"
                @click="viewPostDetails(post.postId)"
              >
                <div class="post-title">{{ post.title }}</div>
                <div class="post-meta">
                  <span class="post-author">{{ post.authorName }}</span>
                  <span class="post-stats">
                    <span class="views">👁️ {{ post.viewCount }}</span>
                    <span class="likes">❤️ {{ post.likeCount }}</span>
                    <span class="comments">💬 {{ post.commentCount }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 所有帖子 -->
          <div class="all-posts-section">
            <h2 class="section-title">{{ isSearching ? '搜索结果' : '所有帖子' }}</h2>

            <div v-if="loading" class="loading-container">
              <div class="loading-spinner"></div>
              <p>加载中...</p>
            </div>

            <div v-else-if="posts.length === 0" class="empty-state">
              <div class="empty-icon">📝</div>
              <h3>{{ isSearching ? '没有找到相关帖子' : '暂无帖子' }}</h3>
              <p>{{ isSearching ? '尝试使用其他关键词搜索' : '成为第一个发帖的人吧！' }}</p>
              <button v-if="!isSearching" class="action-btn" @click="goToCreatePost">发布帖子</button>
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
      </div>
    </div>
、
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      // 用户信息
      userId: '',
      userType: '',
      userName: '',

      // 帖子数据
      posts: [],
      popularPosts: [],
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      loading: true,

      // 搜索
      searchQuery: '',
      isSearching: false,

      // 分类
      activeCategory: null,
      activeSubcategory: null,
      categories: [
        { value: 'EMPLOYMENT', label: '就业相关' },
        { value: 'EVENTS', label: '最新事件' },
        { value: 'COMMUNITY', label: '互动社区' },
        { value: 'RESOURCES', label: '资源分享' }
      ],
      subcategoriesMap: {
        'EMPLOYMENT': [
          { value: 'INTERNSHIP', label: '实习经验分享' },
          { value: 'JOB', label: '最新岗位招聘' },
          { value: 'COOPERATION', label: '校企合作意向征集' }
        ],
        'EVENTS': [
          { value: 'TECHNOLOGY', label: '前沿技术' },
          { value: 'NEWS', label: '最新新闻' },
          { value: 'ACHIEVEMENTS', label: '产学研成果展示' }
        ],
        'COMMUNITY': [
          { value: 'QA', label: '校企问答专区' },
          { value: 'DEBATE', label: '行业趋势辩论' }
        ],
        'RESOURCES': []
      }
    };
  },
  computed: {
    subcategories() {
      return this.activeCategory ? (this.subcategoriesMap[this.activeCategory] || []) : [];
    }
  },
  created() {
    // 获取用户信息
    this.getUserInfo();

    // 获取帖子数据
    this.fetchPosts();
    this.fetchPopularPosts();
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

      // 优先使用 userRole，如果不存在则使用 role
      const effectiveRole = userRole || role;

      if (effectiveRole === 'student') {
        this.userType = 'STUDENT';
        this.userId = userId;

        // 尝试从student对象中获取，以兼容旧代码
        if (!this.userId) {
          const student = JSON.parse(localStorage.getItem('student'));
          if (student) {
            this.userId = student.studentId;
            this.userName = student.name;
          }
        }
      } else if (effectiveRole === 'enterprise') {
        this.userType = 'ENTERPRISE';
        this.userId = userId;

        // 尝试从enterprise对象中获取，以兼容旧代码
        if (!this.userId) {
          const enterprise = JSON.parse(localStorage.getItem('enterprise'));
          if (enterprise) {
            this.userId = enterprise.enterpriseId;
            this.userName = enterprise.name;
          }
        }
      }

      console.log('从本地存储获取用户信息:', { effectiveRole, userType: this.userType, userId: this.userId, userName: this.userName });
    },
    async fetchPosts() {
      this.loading = true;
      try {
        let url = 'http://localhost:8080/api/forum/posts';
        let params = {
          page: this.currentPage,
          size: this.pageSize,
          currentUserId: this.userId,
          currentUserType: this.userType
        };

        // 如果有搜索查询
        if (this.isSearching && this.searchQuery) {
          if (this.activeCategory) {
            if (this.activeSubcategory) {
              // 按分类和子分类搜索
              url = `http://localhost:8080/api/forum/posts/category/${this.activeCategory}/subcategory/${this.activeSubcategory}/search`;
            } else {
              // 只按分类搜索
              url = `http://localhost:8080/api/forum/posts/category/${this.activeCategory}/search`;
            }
            params.keyword = this.searchQuery;
          } else {
            // 全局搜索
            url = 'http://localhost:8080/api/forum/posts/search';
            params.keyword = this.searchQuery;
          }
        }
        // 如果有分类过滤
        else if (this.activeCategory) {
          if (this.activeSubcategory) {
            // 按分类和子分类过滤
            url = `http://localhost:8080/api/forum/posts/category/${this.activeCategory}/subcategory/${this.activeSubcategory}`;
          } else {
            // 只按分类过滤
            url = `http://localhost:8080/api/forum/posts/category/${this.activeCategory}`;
          }
        }

        const response = await axios.get(url, { params });

        this.posts = response.data.content;
        this.totalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取帖子失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchPopularPosts() {
      try {
        const response = await axios.get(`http://localhost:8080/api/forum/posts/popular`, {
          params: {
            limit: 5,
            currentUserId: this.userId,
            currentUserType: this.userType
          }
        });

        this.popularPosts = response.data;
      } catch (error) {
        console.error('获取热门帖子失败:', error);
      }
    },
    async searchPosts() {
      if (!this.searchQuery.trim()) {
        this.isSearching = false;
        this.fetchPosts();
        return;
      }

      this.isSearching = true;
      this.currentPage = 0;
      this.fetchPosts();
    },

    // 设置当前分类
    setCategory(category) {
      this.activeCategory = category;
      this.activeSubcategory = null;
      this.currentPage = 0;
      this.fetchPosts();
    },

    // 设置当前子分类
    setSubcategory(subcategory) {
      this.activeSubcategory = subcategory;
      this.currentPage = 0;
      this.fetchPosts();
    },
    changePage(page) {
      this.currentPage = page;
      if (this.isSearching) {
        this.searchPosts();
      } else {
        this.fetchPosts();
      }
    },
    goToCreatePost() {
      this.$router.push({ name: 'CreateForumPost' });
    },
    goToLikedPosts() {
      this.$router.push({
        name: 'LikedPosts',
        query: {
          userType: this.userType,
          userId: this.userId
        }
      });
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
    },
    // 导航方法
    goBack() {
      this.$router.go(-1);
    },

  }
};
</script>

<style scoped>
.forum-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #1677ff;
  cursor: pointer;
  font-size: 1rem;
}

.back-icon {
  margin-right: 0.5rem;
  font-size: 1.2rem;
}

.page-title {
  font-size: 1.5rem;
  margin: 0;
}

.content-area {
  flex: 1;
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.forum-title {
  font-size: 1.8rem;
  margin: 0;
}

.forum-actions {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.search-box {
  display: flex;
  align-items: center;
}

.search-box input {
  padding: 0.5rem;
  border: 1px solid #d9d9d9;
  border-radius: 4px 0 0 4px;
  width: 200px;
}

.search-btn {
  padding: 0.5rem 1rem;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.create-post-btn {
  padding: 0.5rem 1rem;
  background-color: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.liked-posts-btn {
  padding: 0.5rem 1rem;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.forum-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.section-title {
  font-size: 1.3rem;
  margin-top: 0;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.popular-posts-section {
  margin-bottom: 2rem;
}

.popular-posts-list {
  display: flex;
  gap: 1rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.popular-post-card {
  min-width: 250px;
  background-color: #f9f9f9;
  border-radius: 6px;
  padding: 1rem;
  cursor: pointer;
  transition: transform 0.3s;
}

.popular-post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-title {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #666;
}

.post-stats {
  display: flex;
  gap: 0.5rem;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.post-card {
  background-color: white;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 1rem;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.post-header .post-title {
  font-size: 1.2rem;
  margin: 0;
}

.pin-icon {
  margin-right: 0.5rem;
}

.post-content-preview {
  color: #666;
  margin-bottom: 1rem;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: #999;
}

.post-category {
  display: flex;
  gap: 0.5rem;
}

.category-tag {
  background-color: #e6f7ff;
  color: #1677ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.subcategory-tag {
  background-color: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

/* 分类导航样式 */
.category-nav {
  margin-bottom: 1.5rem;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.category-tab {
  padding: 0.5rem 1rem;
  background-color: #f0f0f0;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.category-tab:hover {
  background-color: #e6f7ff;
}

.category-tab.active {
  background-color: #1677ff;
  color: white;
}

.subcategory-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
  margin-left: 1rem;
}

.subcategory-tab {
  padding: 0.3rem 0.8rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.subcategory-tab:hover {
  background-color: #e6f7ff;
}

.subcategory-tab.active {
  background-color: #e6f7ff;
  color: #1677ff;
  border: 1px solid #1677ff;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.action-btn {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 1.5rem;
  gap: 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9rem;
}
</style>
