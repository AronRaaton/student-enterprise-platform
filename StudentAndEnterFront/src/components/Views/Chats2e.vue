<template>
  <div class="chat-container">
    <!-- 顶部导航栏 -->
    <div class="chat-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="enterprise-name">{{ enterpriseInfo.name || '与企业聊天' }}</h1>
    </div>

    <div class="chat-content">
      <!-- 左侧聊天区域 -->
      <div class="chat-area">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-chat">
            <div class="empty-icon">💬</div>
            <p>暂无消息记录，开始聊天吧！</p>
          </div>
          <div v-else>
            <div
              v-for="(message, index) in messages"
              :key="index"
              :class="['message', message.senderType === 'STUDENT' ? 'sent' : 'received']"
            >
              <div class="message-content">
                <p>{{ message.content }}</p>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="message-input">
          <textarea
            v-model="newMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
            rows="3"
          ></textarea>
          <button class="send-button" @click="sendMessage" :disabled="!newMessage.trim()">
            发送
          </button>
        </div>
      </div>

      <!-- 右侧企业信息区域 -->
      <div class="enterprise-info-panel">
        <div class="info-section">
          <h2>企业信息</h2>
          <div class="info-card">
            <div class="enterprise-avatar">{{ enterpriseInfo.name ? enterpriseInfo.name.charAt(0) : 'E' }}</div>
            <div class="enterprise-details">
              <p><strong>名称：</strong>{{ enterpriseInfo.name || '未知' }}</p>
              <p><strong>类型：</strong>{{ enterpriseInfo.type || '未知' }}</p>
              <p><strong>成立年份：</strong>{{ enterpriseInfo.foundingYear || '未知' }}</p>
              <p><strong>员工数量：</strong>{{ enterpriseInfo.employeeCount || '未知' }}</p>
              <p><strong>所在地：</strong>{{ enterpriseInfo.location || '未知' }}</p>
              <p><strong>详细地址：</strong>{{ enterpriseInfo.address || '未知' }}</p>
              <p><strong>企业邮箱：</strong>{{ enterpriseInfo.email || '未知' }}</p>
            </div>
          </div>
        </div>

        <div class="tasks-section">
          <h2>我的任务 ({{ completedTasks.length }})</h2>
          <div v-if="completedTasks.length === 0" class="empty-tasks">
            <p>暂无已完成任务</p>
          </div>
          <div v-else class="task-list">
            <div v-for="task in completedTasks" :key="task.submissionId" class="task-item">
              <div class="task-title">{{ task.task.summary }}</div>
              <div class="task-meta">
                <span class="task-date">完成时间: {{ formatDate(task.reviewTime) }}</span>
                <span v-if="task.rating" class="task-rating">
                  评分:
                  <span v-for="star in 5" :key="star" :class="['star', { active: task.rating >= star }]">★</span>
                </span>
              </div>
              <button class="view-btn" @click="viewTaskDetails(task.task.taskId)">查看详情</button>
            </div>
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
      enterpriseId: null,
      studentId: null,
      enterpriseInfo: {},
      completedTasks: [],
      messages: [],
      newMessage: '',
      usePolling: true,
      pollingInterval: null
    };
  },
  created() {
    this.enterpriseId = this.$route.params.enterpriseId;
    this.studentId = localStorage.getItem('userId');

    this.fetchEnterpriseInfo();
    this.fetchCompletedTasks();
    this.fetchChatHistory();
    this.startPolling();

    // 标记消息为已读
    this.markMessagesAsRead();
  },
  beforeUnmount() {
    this.stopPolling();
  },
  methods: {
    // 获取企业信息
    fetchEnterpriseInfo() {
      axios.get(`http://localhost:8080/api/login/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.enterpriseInfo = response.data;
        })
        .catch(error => {
          console.error('获取企业信息失败:', error);
          // 尝试使用备用API
          axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}`)
            .then(response => {
              this.enterpriseInfo = response.data;
            })
            .catch(err => {
              console.error('备用API也失败:', err);
              // 设置一个默认的企业信息
              this.enterpriseInfo = {
                name: `企业 ${this.enterpriseId}`,
                type: '未知',
                foundingYear: '未知',
                employeeCount: '未知',
                location: '未知'
              };
            });
        });
    },

    // 获取已完成任务
    fetchCompletedTasks() {
      axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks`)
        .then(response => {
          // 过滤出当前企业的已完成任务
          this.completedTasks = response.data.filter(task =>
            task.task && task.task.enterprise &&
            task.task.enterprise.enterpriseId == this.enterpriseId
          );
        })
        .catch(error => {
          console.error('获取已完成任务失败:', error);
        });
    },

    // 获取聊天历史记录
    fetchChatHistory() {
      axios.get(`http://localhost:8080/api/chat/history/${this.studentId}/${this.enterpriseId}`)
        .then(response => {
          // 获取所有消息并按时间排序
          const allMessages = response.data.sort((a, b) => {
            return new Date(a.timestamp) - new Date(b.timestamp);
          });

          // 只显示最新的20条消息
          this.messages = allMessages.slice(-20);

          // 如果有未读消息，标记为已读
          if (allMessages.some(msg => msg.receiverId === this.studentId && !msg.read)) {
            this.markMessagesAsRead();
          }

          this.$nextTick(() => {
            this.scrollToBottom();
          });
        })
        .catch(error => {
          console.error('获取聊天历史记录失败:', error);
        });
    },

    // 开始轮询
    startPolling() {
      console.log('开始轮询模式');
      this.pollingInterval = setInterval(() => {
        this.fetchChatHistory();
      }, 3000); // 每3秒轮询一次
    },

    // 停止轮询
    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
      }
    },

    // 发送消息
    sendMessage() {
      if (!this.newMessage.trim()) return;

      try {
        // 创建消息对象
        const now = new Date();
        const message = {
          senderId: this.studentId,
          senderName: '学生用户', // 实际应用中应该从用户信息中获取
          senderType: 'STUDENT',
          receiverId: this.enterpriseId,
          content: this.newMessage.trim(),
          // 使用简单的时间字符串，避免时区问题
          timestamp: now.getFullYear() + '-' +
                    String(now.getMonth() + 1).padStart(2, '0') + '-' +
                    String(now.getDate()).padStart(2, '0') + ' ' +
                    String(now.getHours()).padStart(2, '0') + ':' +
                    String(now.getMinutes()).padStart(2, '0') + ':' +
                    String(now.getSeconds()).padStart(2, '0'),
          read: false, // 明确设置为未读状态
          id: 'local_' + now.getTime() // 添加一个本地ID
        };

        // 使用HTTP发送消息
        axios.post('http://localhost:8080/api/chat/send', message)
          .then(response => {
            // 添加到本地消息列表
            this.messages.push(message);
            this.newMessage = '';
            this.$nextTick(() => {
              this.scrollToBottom();
            });
          })
          .catch(error => {
            console.error('发送消息失败:', error);
            alert('发送消息失败，请重试');
          });
      } catch (e) {
        console.error('发送消息失败:', e);
        alert('发送消息失败，请重试');
      }
    },

    // 标记消息为已读
    markMessagesAsRead() {
      console.log('尝试标记消息为已读:', this.studentId, this.enterpriseId);
      axios.post(`http://localhost:8080/api/chat/mark-read/${this.studentId}/${this.enterpriseId}`)
        .then(() => {
          console.log('成功标记消息为已读');
          // 通知学生主页重置未读消息数量
          if (window.parent && window.parent.postMessage) {
            window.parent.postMessage({ type: 'RESET_UNREAD_COUNT' }, '*');
          }
        })
        .catch(error => {
          console.error('标记消息为已读失败:', error);
        });
    },

    // 滚动到底部
    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    // 查看任务详情
    viewTaskDetails(taskId) {
      this.$router.push({ name: 'task-detail', params: { taskId } });
    },

    // 返回上一页
    goBack() {
      this.$router.go(-1);
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },

    // 格式化时间
    formatTime(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.chat-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏样式 */
.chat-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
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

.enterprise-name {
  flex: 1;
  font-size: 20px;
  color: #303133;
  margin: 0 0 0 20px;
}

/* 聊天内容区域 */
.chat-content {
  display: flex;
  flex: 1;
  gap: 20px;
}

/* 左侧聊天区域 */
.chat-area {
  flex: 3;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 240px);
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 15px;
  display: flex;
}

.message.sent {
  justify-content: flex-end;
}

.message.received {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
  position: relative;
}

.message.sent .message-content {
  background-color: #ecf5ff;
  color: #409EFF;
}

.message.received .message-content {
  background-color: #f2f6fc;
  color: #606266;
}

.message-content p {
  margin: 0;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  display: block;
  text-align: right;
  margin-top: 5px;
}

.message-input {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}

.message-input textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
}

.message-input textarea:focus {
  outline: none;
  border-color: #409EFF;
}

.send-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 0 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.send-button:hover:not(:disabled) {
  background-color: #66b1ff;
}

.send-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 右侧企业信息区域 */
.enterprise-info-panel {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
  max-height: calc(100vh - 180px);
}

.info-section, .tasks-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.info-section h2, .tasks-section h2 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.info-card {
  display: flex;
  align-items: flex-start;
  gap: 15px;
}

.enterprise-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.enterprise-details p {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.empty-tasks {
  color: #909399;
  text-align: center;
  padding: 15px 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  position: relative;
}

.task-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.task-rating {
  display: flex;
  align-items: center;
}

.star {
  color: #dcdfe6;
  margin-right: 2px;
}

.star.active {
  color: #e6a23c;
}

.view-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-btn:hover {
  background-color: #66b1ff;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-content {
    flex-direction: column;
  }

  .enterprise-info-panel {
    max-height: none;
  }

  .messages-container {
    max-height: 400px;
  }
}
</style>
