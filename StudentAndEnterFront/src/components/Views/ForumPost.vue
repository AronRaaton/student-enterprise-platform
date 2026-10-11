<template>
  <div class="forum-post-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">帖子详情</h1>
      <div class="header-actions">
        <button v-if="isAuthor" class="edit-button" @click="editPost">编辑</button>
        <button v-if="isAuthor" class="delete-button" @click="confirmDeletePost">删除</button>
      </div>
    </div>

    <!-- 帖子内容 -->
    <div class="content-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="!post" class="error-state">
        <div class="error-icon">❌</div>
        <h3>无法加载帖子</h3>
        <p>该帖子可能已被删除或不存在</p>
        <button class="action-btn" @click="goToForum">返回论坛</button>
      </div>

      <div v-else class="post-content">
        <div class="post-header">
          <div class="post-category-tags" v-if="post.categoryDisplayName">
            <span class="category-tag">{{ post.categoryDisplayName }}</span>
            <span class="subcategory-tag" v-if="post.subcategoryDisplayName">{{ post.subcategoryDisplayName }}</span>
          </div>
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-meta">
            <div class="author-info">
              <div class="author-avatar">{{ post.authorName ? post.authorName.charAt(0) : 'U' }}</div>
              <div class="author-details">
                <span class="author-name">{{ post.authorName }}</span>
                <span class="author-type">{{ post.authorType === 'STUDENT' ? '学生' : '企业' }}</span>
              </div>
            </div>
            <div class="post-time">
              <div>发布于: {{ formatDate(post.createdAt) }}</div>
              <div v-if="post.updatedAt !== post.createdAt">更新于: {{ formatDate(post.updatedAt) }}</div>
            </div>
          </div>
        </div>

        <div class="post-body">
          <div class="post-text" v-html="formatContent(post.content)"></div>
        </div>

        <div class="post-footer">
          <div class="post-stats">
            <span class="views">👁️ {{ post.viewCount }} 浏览</span>
            <span
              class="likes"
              :class="{ 'liked': post.isLikedByCurrentUser }"
              @click="toggleLike"
            >
              {{ post.isLikedByCurrentUser ? '❤️' : '🤍' }} {{ post.likeCount }} 喜欢
            </span>
            <span class="comments">💬 {{ post.commentCount }} 评论</span>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div v-if="post" class="comments-section">
        <h3 class="section-title">评论区</h3>

        <!-- 评论输入框 -->
        <div class="comment-form">
          <textarea
            v-model="newComment"
            placeholder="写下你的评论..."
            rows="3"
          ></textarea>
          <button
            class="submit-comment-btn"
            :disabled="!newComment.trim()"
            @click="submitComment"
          >
            发表评论
          </button>
        </div>

        <!-- 评论列表 -->
        <div v-if="comments.length === 0" class="empty-comments">
          <p>暂无评论，成为第一个评论的人吧！</p>
        </div>

        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.commentId" class="comment-card">
            <div class="comment-header">
              <div class="comment-author">
                <div class="author-avatar">{{ comment.authorName ? comment.authorName.charAt(0) : 'U' }}</div>
                <div class="author-details">
                  <span class="author-name">{{ comment.authorName }}</span>
                  <span class="author-type">{{ comment.authorType === 'STUDENT' ? '学生' : '企业' }}</span>
                </div>
              </div>
              <div class="comment-time">{{ formatDate(comment.createdAt) }}</div>
            </div>

            <div class="comment-content">{{ comment.content }}</div>

            <div class="comment-actions">
              <button class="reply-btn" @click="showReplyForm(comment.commentId)">回复</button>
              <button
                v-if="isCommentAuthor(comment)"
                class="delete-btn"
                @click="confirmDeleteComment(comment.commentId)"
              >
                删除
              </button>
            </div>

            <!-- 回复表单 -->
            <div v-if="replyingTo === comment.commentId" class="reply-form">
              <div class="reply-to-info" v-if="replyToAuthorName && isReplyingToChildComment">
                回复给: <span class="reply-to-name">@{{ replyToAuthorName }}</span>
              </div>
              <textarea
                v-model="replyContent"
                :placeholder="isReplyingToChildComment ? `回复 @${replyToAuthorName}...` : `发表评论...`"
                rows="2"
              ></textarea>
              <div class="reply-actions">
                <button class="cancel-btn" @click="cancelReply">取消</button>
                <button
                  class="submit-btn"
                  :disabled="!replyContent.trim()"
                  @click="submitReply(comment.commentId)"
                >
                  回复
                </button>
              </div>
            </div>

            <!-- 子评论 -->
            <div v-if="comment.childComments && comment.childComments.length > 0" class="child-comments">
              <div
                v-for="childComment in comment.childComments"
                :key="childComment.commentId"
                class="child-comment"
              >
                <div class="comment-header">
                  <div class="comment-author">
                    <div class="author-avatar small">{{ childComment.authorName ? childComment.authorName.charAt(0) : 'U' }}</div>
                    <div class="author-details">
                      <span class="author-name">{{ childComment.authorName }}</span>
                      <span class="author-type">{{ childComment.authorType === 'STUDENT' ? '学生' : '企业' }}</span>
                    </div>
                  </div>
                  <div class="comment-time">{{ formatDate(childComment.createdAt) }}</div>
                </div>

                <div class="comment-content">{{ childComment.content }}</div>

                <div class="comment-actions">
                  <button class="reply-btn" @click="showReplyForm(comment.commentId, childComment.authorName)">回复</button>
                  <button
                    v-if="isCommentAuthor(childComment)"
                    class="delete-btn"
                    @click="confirmDeleteComment(childComment.commentId)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认删除对话框 -->
    <div v-if="showDeleteConfirm" class="delete-confirm-modal">
      <div class="modal-content">
        <h3>确认删除</h3>
        <p>{{ deleteType === 'post' ? '确定要删除这个帖子吗？此操作不可撤销。' : '确定要删除这条评论吗？此操作不可撤销。' }}</p>
        <div class="modal-actions">
          <button class="cancel-btn" @click="cancelDelete">取消</button>
          <button class="confirm-btn" @click="confirmDelete">确认删除</button>
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
      // 帖子数据
      post: null,
      comments: [],
      loading: true,

      // 用户信息
      userId: '',
      userType: '',
      userName: '',

      // 评论相关
      newComment: '',
      replyingTo: null,
      replyContent: '',
      replyToAuthorName: '',
      isReplyingToChildComment: false,

      // 删除确认
      showDeleteConfirm: false,
      deleteType: '', // 'post' or 'comment'
      deleteItemId: null
    };
  },
  computed: {
    isAuthor() {
      if (!this.post || !this.userId || !this.userType) return false;
      return this.post.authorId === this.userId && this.post.authorType === this.userType;
    }
  },
  created() {
    // 获取用户信息
    this.getUserInfo();

    // 获取帖子数据
    this.fetchPost();
    this.fetchComments();
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

      // 优先使用userRole，如果不存在则使用role
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
    async fetchPost() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/forum/posts/${this.$route.params.postId}`, {
          params: {
            currentUserId: this.userId,
            currentUserType: this.userType
          }
        });

        this.post = response.data;
      } catch (error) {
        console.error('获取帖子失败:', error);
        this.post = null;
      } finally {
        this.loading = false;
      }
    },
    async fetchComments() {
      try {
        const response = await axios.get(`http://localhost:8080/api/forum/posts/${this.$route.params.postId}/comments`);
        this.comments = response.data;
      } catch (error) {
        console.error('获取评论失败:', error);
      }
    },
    async toggleLike() {
      try {
        const response = await axios.post(`http://localhost:8080/api/forum/posts/${this.post.postId}/like`, null, {
          params: {
            userId: this.userId,
            userType: this.userType
          }
        });

        this.post = response.data;
      } catch (error) {
        console.error('点赞失败:', error);
      }
    },
    async submitComment() {
      if (!this.newComment.trim()) return;

      try {
        const request = {
          postId: this.post.postId,
          content: this.newComment,
          authorId: this.userId,
          authorType: this.userType
        };

        await axios.post(`http://localhost:8080/api/forum/comments`, request);

        // 清空评论框并刷新评论
        this.newComment = '';
        this.fetchComments();
        this.fetchPost(); // 更新评论计数
      } catch (error) {
        console.error('发表评论失败:', error);
      }
    },
    showReplyForm(commentId, authorName = null) {
      this.replyingTo = commentId;
      this.replyContent = '';

      // 如果提供了作者名称，表示是回复子评论
      if (authorName) {
        this.replyToAuthorName = authorName;
        this.isReplyingToChildComment = true;
      } else {
        // 如果没有提供作者名称，表示是回复主评论
        const comment = this.comments.find(c => c.commentId === commentId);
        if (comment) {
          this.replyToAuthorName = comment.authorName;
          this.isReplyingToChildComment = false;
        }
      }
    },
    cancelReply() {
      this.replyingTo = null;
      this.replyContent = '';
      this.replyToAuthorName = '';
      this.isReplyingToChildComment = false;
    },
    async submitReply(parentCommentId) {
      if (!this.replyContent.trim()) return;

      try {
        let finalContent = this.replyContent;

        // 只有当回复子评论时才添加@用户名
        if (this.isReplyingToChildComment && this.replyToAuthorName) {
          // 检查内容是否已经包含@用户名
          if (!finalContent.startsWith(`@${this.replyToAuthorName}`)) {
            finalContent = `@${this.replyToAuthorName} ${finalContent}`;
          }
        }

        const request = {
          postId: this.post.postId,
          content: finalContent,
          authorId: this.userId,
          authorType: this.userType,
          parentCommentId: parentCommentId
        };

        await axios.post(`http://localhost:8080/api/forum/comments`, request);

        // 清空回复框并刷新评论
        this.replyingTo = null;
        this.replyContent = '';
        this.replyToAuthorName = '';
        this.isReplyingToChildComment = false;
        this.fetchComments();
        this.fetchPost(); // 更新评论计数
      } catch (error) {
        console.error('回复评论失败:', error);
      }
    },
    isCommentAuthor(comment) {
      return comment.authorId === this.userId && comment.authorType === this.userType;
    },
    confirmDeletePost() {
      this.showDeleteConfirm = true;
      this.deleteType = 'post';
      this.deleteItemId = this.post.postId;
    },
    confirmDeleteComment(commentId) {
      this.showDeleteConfirm = true;
      this.deleteType = 'comment';
      this.deleteItemId = commentId;
    },
    cancelDelete() {
      this.showDeleteConfirm = false;
      this.deleteType = '';
      this.deleteItemId = null;
    },
    async confirmDelete() {
      try {
        if (this.deleteType === 'post') {
          await axios.delete(`http://localhost:8080/api/forum/posts/${this.deleteItemId}`, {
            params: {
              userId: this.userId,
              userType: this.userType
            }
          });

          // 返回论坛首页
          this.goToForum();
        } else if (this.deleteType === 'comment') {
          await axios.delete(`http://localhost:8080/api/forum/comments/${this.deleteItemId}`, {
            params: {
              userId: this.userId,
              userType: this.userType
            }
          });

          // 刷新评论
          this.fetchComments();
          this.fetchPost(); // 更新评论计数
        }
      } catch (error) {
        console.error('删除失败:', error);
      } finally {
        this.showDeleteConfirm = false;
        this.deleteType = '';
        this.deleteItemId = null;
      }
    },
    editPost() {
      this.$router.push({
        name: 'EditForumPost',
        params: { postId: this.post.postId }
      });
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
    formatContent(content) {
      // 简单的文本格式化，将换行符转换为 <br>
      return content.replace(/\n/g, '<br>');
    },
    goBack() {
      this.$router.go(-1);
    },
    goToForum() {
      this.$router.push({ name: 'ForumHome' });
    }
  }
};
</script>

<style scoped>
.forum-post-container {
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

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.edit-button, .delete-button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.edit-button {
  background-color: #1677ff;
  color: white;
}

.delete-button {
  background-color: #ff4d4f;
  color: white;
}

.content-container {
  flex: 1;
  padding: 1.5rem;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
}

.loading-container, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 1.5rem;
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

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #ff4d4f;
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

.post-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.post-header {
  margin-bottom: 1.5rem;
}

.post-category-tags {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.category-tag {
  background-color: #e6f7ff;
  color: #1677ff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.subcategory-tag {
  background-color: #f0f0f0;
  color: #666;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.post-title {
  font-size: 1.8rem;
  margin-top: 0;
  margin-bottom: 1rem;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.author-avatar {
  width: 40px;
  height: 40px;
  background-color: #1677ff;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
  font-size: 1.2rem;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: bold;
}

.author-type {
  font-size: 0.8rem;
  color: #666;
}

.post-time {
  font-size: 0.9rem;
  color: #666;
  text-align: right;
}

.post-body {
  margin-bottom: 1.5rem;
}

.post-text {
  line-height: 1.6;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.post-stats {
  display: flex;
  gap: 1.5rem;
  color: #666;
}

.likes {
  cursor: pointer;
}

.likes.liked {
  color: #ff4d4f;
}

.comments-section {
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

.comment-form {
  margin-bottom: 1.5rem;
}

.comment-form textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
  margin-bottom: 0.5rem;
}

.submit-comment-btn {
  padding: 0.5rem 1rem;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  float: right;
}

.submit-comment-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-comments {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-card {
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 1.5rem;
}

.comment-card:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.comment-author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.comment-time {
  font-size: 0.8rem;
  color: #666;
}

.comment-content {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
}

.reply-btn, .delete-btn {
  background: none;
  border: none;
  color: #1677ff;
  cursor: pointer;
  font-size: 0.9rem;
  padding: 0;
}

.delete-btn {
  color: #ff4d4f;
}

.reply-form {
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  padding-left: 2rem;
}

.reply-to-info {
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.reply-to-name {
  color: #1677ff;
  font-weight: bold;
}

.reply-form textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  resize: vertical;
  margin-bottom: 0.5rem;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.cancel-btn, .submit-btn {
  padding: 0.3rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
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

.child-comments {
  margin-top: 1rem;
  padding-left: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.child-comment {
  background-color: #f9f9f9;
  padding: 0.8rem;
  border-radius: 4px;
}

.author-avatar.small {
  width: 30px;
  height: 30px;
  font-size: 1rem;
}

.delete-confirm-modal {
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

.modal-content {
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal-content h3 {
  margin-top: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
}

.modal-actions button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions .cancel-btn {
  background-color: #f0f0f0;
  color: #666;
}

.modal-actions .confirm-btn {
  background-color: #ff4d4f;
  color: white;
}
</style>
