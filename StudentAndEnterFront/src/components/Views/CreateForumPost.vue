<template>
  <div class="create-post-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">{{ isEditing ? '编辑帖子' : '发布新帖子' }}</h1>
      <div></div> <!-- 占位，保持布局平衡 -->
    </div>

    <!-- 帖子表单 -->
    <div class="content-container">
      <div class="post-form">
        <div class="form-group">
          <label for="title">标题</label>
          <input
            type="text"
            id="title"
            v-model="postTitle"
            placeholder="请输入帖子标题"
            maxlength="100"
          />
          <div class="char-count">{{ postTitle.length }}/100</div>
        </div>

        <div class="form-group">
          <label for="category">分类</label>
          <div class="category-selection">
            <select id="category" v-model="selectedCategory" @change="onCategoryChange">
              <option value="">请选择分类</option>
              <option v-for="category in categories" :key="category.value" :value="category.value">
                {{ category.label }}
              </option>
            </select>

            <select
              id="subcategory"
              v-model="selectedSubcategory"
              :disabled="!selectedCategory || subcategories.length === 0"
            >
              <option value="">请选择子分类</option>
              <option v-for="subcategory in subcategories" :key="subcategory.value" :value="subcategory.value">
                {{ subcategory.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="content">内容</label>
          <textarea
            id="content"
            v-model="postContent"
            placeholder="请输入帖子内容"
            rows="15"
          ></textarea>
        </div>

        <div class="form-actions">
          <button class="cancel-btn" @click="goBack">取消</button>
          <button
            class="submit-btn"
            :disabled="!isFormValid"
            @click="submitPost"
          >
            {{ isEditing ? '保存修改' : '发布帖子' }}
          </button>
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
      postTitle: '',
      postContent: '',

      // 用户信息
      userId: '',
      userType: '',

      // 编辑模式
      isEditing: false,
      postId: null,

      // 分类
      selectedCategory: '',
      selectedSubcategory: '',
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
    isFormValid() {
      return this.postTitle.trim() && this.postContent.trim() && this.selectedCategory;
    },
    subcategories() {
      return this.selectedCategory ? (this.subcategoriesMap[this.selectedCategory] || []) : [];
    }
  },
  created() {
    // 获取用户信息
    this.getUserInfo();

    // 检查是否是编辑模式
    if (this.$route.name === 'EditForumPost' && this.$route.params.postId) {
      this.isEditing = true;
      this.postId = this.$route.params.postId;
      this.fetchPost();
    }
  },
  methods: {
    getUserInfo() {
      // 从本地存储获取用户信息
      const userRole = localStorage.getItem('userRole');
      const userId = localStorage.getItem('userId');

      if (userRole === 'student') {
        this.userType = 'STUDENT';
        this.userId = userId;

        // 尝试从student对象中获取，以兼容旧代码
        if (!this.userId) {
          const student = JSON.parse(localStorage.getItem('student'));
          if (student) {
            this.userId = student.studentId;
          }
        }
      } else if (userRole === 'enterprise') {
        this.userType = 'ENTERPRISE';
        this.userId = userId;

        // 尝试从enterprise对象中获取，以兼容旧代码
        if (!this.userId) {
          const enterprise = JSON.parse(localStorage.getItem('enterprise'));
          if (enterprise) {
            this.userId = enterprise.enterpriseId;
          }
        }
      }

      console.log('用户信息:', { userRole: this.userType, userId: this.userId });
    },
    async fetchPost() {
      try {
        const response = await axios.get(`http://localhost:8080/api/forum/posts/${this.postId}`, {
          params: {
            currentUserId: this.userId,
            currentUserType: this.userType
          }
        });

        const post = response.data;

        // 检查是否是帖子作者
        if (post.authorId !== this.userId || post.authorType !== this.userType) {
          this.$router.push({ name: 'ForumHome' });
          return;
        }

        this.postTitle = post.title;
        this.postContent = post.content;
        this.selectedCategory = post.category;
        this.selectedSubcategory = post.subcategory;
      } catch (error) {
        console.error('获取帖子失败:', error);
        this.$router.push({ name: 'ForumHome' });
      }
    },
    async submitPost() {
      if (!this.isFormValid) return;

      try {
        const request = {
          title: this.postTitle,
          content: this.postContent,
          authorId: this.userId,
          authorType: this.userType,
          category: this.selectedCategory,
          subcategory: this.selectedSubcategory
        };

        let response;

        if (this.isEditing) {
          response = await axios.put(`http://localhost:8080/api/forum/posts/${this.postId}`, request);
        } else {
          response = await axios.post(`http://localhost:8080/api/forum/posts`, request);
        }

        // 跳转到帖子详情页
        this.$router.push({
          name: 'ForumPost',
          params: { postId: response.data.postId }
        });
      } catch (error) {
        console.error('发布帖子失败:', error);
        alert('发布失败，请稍后重试');
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    onCategoryChange() {
      // 当分类改变时重置子分类
      this.selectedSubcategory = '';
    }
  }
};
</script>

<style scoped>
.create-post-container {
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

.content-container {
  flex: 1;
  padding: 1.5rem;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.post-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
  position: relative;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  resize: vertical;
}

.category-selection {
  display: flex;
  gap: 1rem;
}

.category-selection select {
  flex: 1;
}

.category-selection select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.char-count {
  position: absolute;
  right: 0;
  top: 0;
  font-size: 0.8rem;
  color: #666;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

.cancel-btn,
.submit-btn {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #666;
}

.submit-btn {
  background-color: #1677ff;
  color: white;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
