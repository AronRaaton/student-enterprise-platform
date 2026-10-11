<template>
  <div class="messages-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">我的消息</h1>
    </div>

    <div class="content-container">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="conversations.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <h3>暂无消息</h3>
        <p>等待学生完成任务后与您联系</p>
      </div>

      <div v-else class="conversations-list">
        <div
          v-for="conversation in conversations"
          :key="conversation.studentId"
          class="conversation-item"
          :class="{ 'unread': conversation.unreadCount > 0 }"
          @click="goToChat(conversation.studentId)"
        >
          <div class="student-avatar">{{ conversation.studentName.charAt(0) }}</div>
          <div class="conversation-content">
            <div class="conversation-header">
              <h3 class="student-name">{{ conversation.studentName }}</h3>
              <span class="last-time">{{ formatTime(conversation.lastMessageTime) }}</span>
            </div>
            <p class="last-message">{{ conversation.lastMessage }}</p>
          </div>
          <div v-if="conversation.unreadCount > 0" class="unread-badge">
            {{ conversation.unreadCount }}
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
      conversations: [],
      loading: true,
      enterpriseId: null
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchConversations();
  },
  methods: {
    fetchConversations() {
      this.loading = true;

      // 直接使用备用方法获取学生并构建会话列表
      this.fetchStudentsAndMessages();
    },

    // 获取所有学生并构建会话列表
    fetchStudentsAndMessages() {
      this.loading = true;

      // 直接使用收藏的学生列表，避免使用可能出错的enterprise-students端点
      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/favorites`)
        .then(response => {
          const favoritedStudents = response.data || [];

          // 即使没有收藏的学生，也尝试获取所有聊天记录
          this.fetchAllChatHistory(favoritedStudents);
        })
        .catch(error => {
          console.error('获取收藏学生列表失败:', error);
          // 即使获取收藏学生失败，也尝试获取所有聊天记录
          this.fetchAllChatHistory([]);
        });
    },

    // 获取所有聊天记录
    fetchAllChatHistory(knownStudents) {
      // 直接使用未读消息端点获取所有消息发送者
      axios.get(`http://localhost:8080/api/chat/unread/${this.enterpriseId}`)
        .then(response => {
          // 即使没有未读消息，也尝试获取已知学生的聊天记录
          this.processKnownStudents(knownStudents);
        })
        .catch(error => {
          console.error('获取未读消息失败:', error);
          // 即使获取未读消息失败，也尝试获取已知学生的聊天记录
          this.processKnownStudents(knownStudents);
        });
    },

    // 处理已知学生的聊天记录
    processKnownStudents(students) {
      if (!students || students.length === 0) {
        // 如果没有已知学生，尝试获取所有学生
        axios.get(`http://localhost:8080/api/admin/students?page=0&size=50`)
          .then(response => {
            // 处理分页响应，提取content字段
            const allStudents = response.data && response.data.content ? response.data.content : [];
            this.fetchChatHistoryForStudents(allStudents);
          })
          .catch(error => {
            console.error('获取所有学生失败:', error);
            // 即使获取所有学生失败，也尝试获取学生1-10的聊天记录
            const fallbackStudents = [];
            for (let i = 1; i <= 10; i++) {
              fallbackStudents.push({ studentId: i, name: `学生 ${i}` });
            }
            this.fetchChatHistoryForStudents(fallbackStudents);
          });
      } else {
        // 如果有已知学生，直接获取它们的聊天记录
        this.fetchChatHistoryForStudents(students);
      }
    },

    // 获取学生的聊天记录
    fetchChatHistoryForStudents(students) {
      if (!students || students.length === 0) {
        this.conversations = [];
        this.loading = false;
        return;
      }

      // 获取每个学生的聊天记录
      const promises = students.map(student => {
        return axios.get(`http://localhost:8080/api/chat/history/${this.enterpriseId}/${student.studentId}`)
          .then(response => {
            const messages = response.data || [];

            // 如果没有消息记录，返回null
            if (messages.length === 0) {
              return null;
            }

            // 获取最后一条消息
            const lastMessage = messages[messages.length - 1];

            // 计算未读消息数量 - 只计算学生发送给企业的未读消息
            const unreadCount = messages.filter(
              msg => msg.receiverId === this.enterpriseId && msg.senderType === 'STUDENT' && !msg.read
            ).length;

            return {
              studentId: student.studentId,
              studentName: student.name || `学生 ${student.studentId}`,
              lastMessage: lastMessage.content,
              lastMessageTime: lastMessage.timestamp,
              unreadCount
            };
          })
          .catch(error => {
            console.error(`获取与学生 ${student.studentId} 的聊天记录失败:`, error);
            return null; // 如果出错返回null
          });
      });

      // 等待所有请求完成
      Promise.all(promises)
        .then(results => {
          // 过滤掉null结果，只保留有消息的学生
          const validResults = results.filter(result => result !== null);

          // 按最后消息时间排序
          this.conversations = validResults
            .sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime));

          this.loading = false;
        })
        .catch(error => {
          console.error('处理会话列表失败:', error);
          this.loading = false;
          this.conversations = [];
        });
    },
    goToChat(studentId) {
      // 在跳转前尝试标记消息为已读
      axios.post(`http://localhost:8080/api/chat/mark-read/${this.enterpriseId}/${studentId}`)
        .catch(error => {
          console.error('标记消息为已读失败:', error);
        });

      // 使用正确的路由路径
      this.$router.push(`/chat/${studentId}`);

      // 重置当前会话的未读消息数量
      const conversationIndex = this.conversations.findIndex(conv => conv.studentId === studentId);
      if (conversationIndex !== -1) {
        this.conversations[conversationIndex].unreadCount = 0;
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    formatTime(dateString) {
      if (!dateString) return '';

      const now = new Date();
      const date = new Date(dateString);

      // 如果是今天的消息，只显示时间
      if (date.toDateString() === now.toDateString()) {
        return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
      }

      // 如果是昨天的消息，显示"昨天"
      const yesterday = new Date(now);
      yesterday.setDate(now.getDate() - 1);
      if (date.toDateString() === yesterday.toDateString()) {
        return '昨天';
      }

      // 如果是今年的消息，显示月-日
      if (date.getFullYear() === now.getFullYear()) {
        return `${date.getMonth() + 1}月${date.getDate()}日`;
      }

      // 其他情况显示年-月-日
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.messages-container {
  max-width: 800px;
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
  margin-bottom: 30px;
  position: relative;
}

.back-button {
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

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  min-height: 500px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
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
  height: 300px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #909399;
}

.empty-state h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.empty-state p {
  color: #606266;
}

.conversations-list {
  display: flex;
  flex-direction: column;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
  position: relative;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.unread {
  background-color: #ecf5ff;
}

.conversation-item.unread:hover {
  background-color: #d9ecff;
}

.student-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  margin-right: 15px;
  flex-shrink: 0;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.student-name {
  font-size: 16px;
  color: #303133;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.last-time {
  font-size: 12px;
  color: #909399;
}

.last-message {
  font-size: 14px;
  color: #606266;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  margin-left: 10px;
}

@media (max-width: 768px) {
  .messages-container {
    padding: 10px;
  }

  .content-container {
    padding: 15px;
  }

  .student-avatar {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}
</style>
