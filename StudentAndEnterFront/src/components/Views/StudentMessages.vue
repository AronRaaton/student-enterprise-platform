<template>
  <div class="messages-container">
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
        <h3>暂无企业联系人</h3>
        <p>完成企业任务后，将显示与企业的聊天框</p>
      </div>

      <div v-else class="conversations-list">
        <div
          v-for="conversation in conversations"
          :key="conversation.enterpriseId"
          class="conversation-item"
          :class="{ 'unread': conversation.unreadCount > 0 }"
          @click="goToChat(conversation.enterpriseId)"
        >
          <div class="enterprise-avatar">{{ conversation.enterpriseName.charAt(0) }}</div>
          <div class="conversation-content">
            <div class="conversation-header">
              <h3 class="enterprise-name">{{ conversation.enterpriseName }}</h3>
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
      studentId: null
    };
  },
  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchConversations();
  },
  methods: {
    fetchConversations() {
      this.loading = true;

      // 获取学生已完成的任务，找出相关企业
      axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks`)
        .then(response => {
          const completedTasks = response.data;

          // 提取企业ID
          const enterpriseIds = [...new Set(
            completedTasks
              .filter(task => task.task && task.task.enterprise)
              .map(task => task.task.enterprise.enterpriseId)
          )];

          // 如果没有完成任何企业任务，则显示空状态
          if (enterpriseIds.length === 0) {
            this.conversations = [];
            this.loading = false;
            return;
          }

          // 获取每个企业的最后一条消息
          const promises = enterpriseIds.map(enterpriseId => {
            return axios.get(`http://localhost:8080/api/chat/history/${this.studentId}/${enterpriseId}`)
              .then(response => {
                const messages = response.data;

                // 即使没有消息记录，也创建一个对话项
                if (messages.length === 0) {
                  // 获取企业信息
                  return axios.get(`http://localhost:8080/api/login/enterprise/${enterpriseId}`)
                    .then(response => {
                      const enterprise = response.data;

                      return {
                        enterpriseId,
                        enterpriseName: enterprise.name || `企业 ${enterpriseId}`,
                        lastMessage: '暂无消息',
                        lastMessageTime: new Date(),
                        unreadCount: 0
                      };
                    })
                    .catch(error => {
                      console.error(`获取企业 ${enterpriseId} 信息失败:`, error);
                      // 即使获取企业信息失败，也返回一个对话项
                      return {
                        enterpriseId,
                        enterpriseName: `企业 ${enterpriseId}`,
                        lastMessage: '暂无消息',
                        lastMessageTime: new Date(),
                        unreadCount: 0
                      };
                    });
                }

                // 获取最后一条消息
                const lastMessage = messages[messages.length - 1];

                // 计算未读消息数量
                const unreadCount = messages.filter(
                  msg => msg.receiverId === this.studentId && !msg.read
                ).length;

                // 获取企业信息
                return axios.get(`http://localhost:8080/api/login/enterprise/${enterpriseId}`)
                  .then(response => {
                    const enterprise = response.data;

                    return {
                      enterpriseId,
                      enterpriseName: enterprise.name || `企业 ${enterpriseId}`,
                      lastMessage: lastMessage.content,
                      lastMessageTime: lastMessage.timestamp,
                      unreadCount
                    };
                  })
                  .catch(error => {
                    console.error(`获取企业 ${enterpriseId} 信息失败:`, error);
                    // 即使获取企业信息失败，也返回一个对话项
                    return {
                      enterpriseId,
                      enterpriseName: `企业 ${enterpriseId}`,
                      lastMessage: lastMessage.content,
                      lastMessageTime: lastMessage.timestamp,
                      unreadCount
                    };
                  });
              });
          });

          // 等待所有请求完成
          Promise.all(promises)
            .then(results => {
              // 按最后消息时间排序，不再过滤掉结果，因为现在所有结果都是有效的
              this.conversations = results
                .filter(result => result !== null) // 保留过滤以防止意外情况
                .sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime));

              this.loading = false;
            });
        })
        .catch(error => {
          console.error('获取对话列表失败:', error);
          this.loading = false;
        });
    },
    goToChat(enterpriseId) {
      // 在跳转前标记消息为已读
      axios.post(`http://localhost:8080/api/chat/mark-read/${this.studentId}/${enterpriseId}`)
        .catch(error => {
          console.error('标记消息为已读失败:', error);
        });

      this.$router.push(`/student/chat/${enterpriseId}`);

      // 重置当前会话的未读消息数量
      const conversationIndex = this.conversations.findIndex(conv => conv.enterpriseId === enterpriseId);
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

.enterprise-avatar {
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

.enterprise-name {
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

  .enterprise-avatar {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}
</style>
