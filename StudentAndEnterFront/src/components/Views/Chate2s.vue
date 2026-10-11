<template>
  <div class="chat-container module-page module-chat">
    <!-- 顶部导航栏 -->
    <div class="chat-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="student-name">{{ studentInfo.name || '与学生聊天' }}</h1>
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
              :class="['message', message.senderType === 'ENTERPRISE' ? 'sent' : 'received', message.pending ? 'pending' : '']"
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

      <!-- 右侧学生信息区域 -->
      <div class="student-info-panel">
        <div class="info-section">
          <h2>学生信息</h2>
          <div class="info-card">
            <div class="student-avatar">{{ studentInfo.name ? studentInfo.name.charAt(0) : 'S' }}</div>
            <div class="student-details">
              <p><strong>姓名：</strong>{{ studentInfo.name || '未知' }}</p>
              <p><strong>学号：</strong>{{ studentInfo.studentNumber || studentInfo.studentId || '未知' }}</p>
              <p><strong>学校：</strong>{{ studentInfo.school || '未知' }}</p>
              <p><strong>入学年份：</strong>{{ studentInfo.enrollmentYear || '未知' }}</p>
              <p><strong>专业：</strong>{{ studentInfo.major || '未知' }}</p>
              <p><strong>邮箱：</strong>{{ studentInfo.email || '未知' }}</p>
              <p><strong>完成任务数：</strong>{{ studentInfo.completedTasks || 0 }}</p>
            </div>
          </div>
        </div>

        <div class="tasks-section">
          <h2>待审核任务 ({{ pendingTasks.length }})</h2>
          <div v-if="pendingTasks.length === 0" class="empty-tasks">
            <p>暂无待审核任务</p>
          </div>
          <div v-else class="task-list">
            <div v-for="task in pendingTasks" :key="task.submissionId" class="task-item">
              <div class="task-title">{{ task.task.summary }}</div>
              <div class="task-meta">
                <span class="task-date">提交时间: {{ formatDate(task.submissionTime) }}</span>
              </div>
              <button class="view-btn" @click="viewSubmission(task.task.taskId)">查看详情</button>
            </div>
          </div>

          <h2>已完成任务 ({{ completedTasks.length }})</h2>
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
              <button class="view-btn" @click="viewSubmission(task.task.taskId)">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
// 使用全局变量而不是导入模块
// import SockJS from 'sockjs-client';
// import Stomp from 'webstomp-client';

export default {
  data() {
    return {
      studentId: null,
      enterpriseId: null,
      studentInfo: {},
      pendingTasks: [],
      completedTasks: [],
      messages: [],
      newMessage: '',
      stompClient: null,
      connected: false,
      usePolling: false,
      pollingInterval: null,
      lastMessageId: null
    };
  },
  created() {
    this.studentId = this.$route.params.studentId;
    this.enterpriseId = localStorage.getItem('userId');

    this.fetchStudentInfo();
    this.fetchStudentTasks();
    this.fetchChatHistory();
    this.connectToWebSocket();

    // 标记消息为已读
    this.markMessagesAsRead();

    // 尝试发送本地存储的未发送消息
    this.trySendPendingMessages();

    // 添加网络状态监听
    window.addEventListener('online', this.handleNetworkChange);
    window.addEventListener('offline', this.handleNetworkChange);
  },

  beforeUnmount() {
    this.disconnectWebSocket();
    this.stopPolling();

    // 移除网络状态监听
    window.removeEventListener('online', this.handleNetworkChange);
    window.removeEventListener('offline', this.handleNetworkChange);
  },
  methods: {
    // 获取学生信息
    fetchStudentInfo() {
      axios.get(`http://localhost:8080/api/login/student/${this.studentId}`)
        .then(response => {
          this.studentInfo = response.data;
        })
        .catch(error => {
          console.error('获取学生信息失败:', error);
        });
    },

    // 获取学生任务情况
    fetchStudentTasks() {
      axios.get(`http://localhost:8080/api/chat/student-tasks/${this.studentId}`)
        .then(response => {
          this.pendingTasks = response.data.pendingSubmissions || [];
          this.completedTasks = response.data.completedSubmissions || [];
        })
        .catch(error => {
          console.error('获取学生任务情况失败:', error);
        });
    },

    // 获取聊天历史记录
    fetchChatHistory() {
      axios.get(`http://localhost:8080/api/chat/history/${this.enterpriseId}/${this.studentId}`)
        .then(response => {
          // 获取所有消息并按时间排序

          // 标记消息为已读
          this.markMessagesAsRead();
          const allMessages = response.data.sort((a, b) => {
            return new Date(a.timestamp) - new Date(b.timestamp);
          });

          // 只显示最新的20条消息
          this.messages = allMessages.slice(-20);

          // 如果有新消息，标记为已读
          if (allMessages.some(msg => msg.receiverId === this.enterpriseId && !msg.read)) {
            this.markMessagesAsRead();
          }

          // 加载本地存储的未发送消息
          this.loadPendingMessages();

          this.$nextTick(() => {
            this.scrollToBottom();
          });
        })
        .catch(error => {
          console.error('获取聊天历史记录失败:', error);

          // 即使服务器请求失败，也加载本地存储的未发送消息
          this.loadPendingMessages();
        });
    },

    // 连接WebSocket
    connectToWebSocket() {
      // 检查全局变量是否存在
      if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.error('SockJS 或 Stomp 客户端不可用，切换到轮询模式');
        this.usePolling = true;
        this.startPolling();
        return;
      }

      try {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);

        // 禁用控制台日志
        this.stompClient.debug = () => {};

        this.stompClient.connect(
          {},
          frame => {
            this.connected = true;
            console.log('Connected to WebSocket:', frame);

            // 订阅个人消息
            this.stompClient.subscribe(`/user/${this.enterpriseId}/queue/messages`, message => {
              try {
                const receivedMessage = JSON.parse(message.body);
                this.messages.push(receivedMessage);

                // 如果是学生发送的消息，标记为已读
                if (receivedMessage.senderType === 'STUDENT' && receivedMessage.receiverId === this.enterpriseId) {
                  this.markMessagesAsRead();
                }

                this.$nextTick(() => {
                  this.scrollToBottom();
                });
              } catch (e) {
                console.error('解析消息失败:', e);
              }
            });
          },
          error => {
            console.error('WebSocket连接失败:', error);
            this.connected = false;
            // 切换到轮询模式
            this.usePolling = true;
            this.startPolling();
          }
        );
      } catch (e) {
        console.error('WebSocket初始化失败:', e);
        // 切换到轮询模式
        this.usePolling = true;
        this.startPolling();
      }
    },

    // 使用轮询模式获取消息
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

    // 断开WebSocket连接
    disconnectWebSocket() {
      if (this.stompClient) {
        this.stompClient.disconnect();
        this.connected = false;
      }
    },

    // 发送消息
    sendMessage() {
      if (!this.newMessage.trim()) return;

      try {
        // 创建消息对象
        const now = new Date();
        const message = {
          senderId: this.enterpriseId,
          senderName: '企业用户', // 实际应用中应该从用户信息中获取
          senderType: 'ENTERPRISE',
          receiverId: this.studentId,
          content: this.newMessage.trim(),
          // 使用简单的时间字符串，避免时区问题
          timestamp: now.getFullYear() + '-' +
                    String(now.getMonth() + 1).padStart(2, '0') + '-' +
                    String(now.getDate()).padStart(2, '0') + ' ' +
                    String(now.getHours()).padStart(2, '0') + ':' +
                    String(now.getMinutes()).padStart(2, '0') + ':' +
                    String(now.getSeconds()).padStart(2, '0'),
          read: false,
          id: 'local_' + now.getTime(), // 添加一个本地ID
          pending: true // 标记为待发送状态
        };

        // 先添加到本地消息列表，给用户即时反馈
        this.messages.push(message);
        const currentMessage = this.newMessage;
        this.newMessage = '';
        this.$nextTick(() => {
          this.scrollToBottom();
        });

        // 尝试发送消息到服务器
        const sendToServer = async () => {
          try {
            console.log('开始发送消息到服务器:', message);

            const response = await axios.post('http://localhost:8080/api/chat/send', message);
            console.log('消息发送成功，响应:', response.data);

            // 发送成功，更新消息状态
            const index = this.messages.findIndex(msg => msg.id === message.id);
            if (index !== -1) {
              this.messages[index].pending = false;
              this.messages[index].id = response.data.id || message.id;
              console.log('更新消息状态成功，ID从', message.id, '变为', this.messages[index].id);
            }

            return true;
          } catch (error) {
            console.error('发送消息失败:', error);

            // 显示详细错误信息
            if (error.response) {
              // 服务器响应了错误状态码
              console.error('服务器响应错误:', error.response.status, error.response.data);
            } else if (error.request) {
              // 请求已发送但没有收到响应
              console.error('没有收到服务器响应:', error.request);
            } else {
              // 请求设置时发生错误
              console.error('请求错误:', error.message);
            }

            return false;
          }
        };

        // 尝试发送消息，如果失败，则存储到本地
        sendToServer().then(success => {
          if (!success) {
            // 将未发送成功的消息存储到本地
            const pendingMessages = JSON.parse(localStorage.getItem(`pendingMessages_${this.enterpriseId}_${this.studentId}`) || '[]');
            pendingMessages.push({
              ...message,
              timestamp: message.timestamp.toString() // 存储前转换时间格式
            });
            localStorage.setItem(`pendingMessages_${this.enterpriseId}_${this.studentId}`, JSON.stringify(pendingMessages));

            // 显示友好的错误提示
            const messageElement = document.createElement('div');
            messageElement.className = 'network-error-message';
            messageElement.textContent = '网络错误: 消息已保存到本地，将在网络恢复时自动发送';
            messageElement.style.color = '#f56c6c';
            messageElement.style.textAlign = 'center';
            messageElement.style.padding = '10px';
            messageElement.style.margin = '10px 0';
            messageElement.style.fontSize = '12px';
            messageElement.style.backgroundColor = '#fef0f0';
            messageElement.style.borderRadius = '4px';

            const container = this.$refs.messagesContainer;
            if (container) {
              container.appendChild(messageElement);
              setTimeout(() => {
                container.removeChild(messageElement);
              }, 5000);
            }
          }
        });

      } catch (e) {
        console.error('发送消息失败:', e);
        // 将消息保存到本地
        alert('发送消息失败，消息已保存到本地，将在网络恢复时自动发送');
      }
    },

    // 滚动到底部
    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    // 查看任务提交详情
    viewSubmission(taskId) {
      // 先尝试跳转到学生的特定提交
      axios.get(`http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${taskId}`)
        .then(response => {
          // 如果有提交记录，就展开该提交
          const submissionId = response.data.submissionId;
          // 跳转到提交列表页面，并传递要展开的提交ID
          this.$router.push({
            name: 'TaskSubmissions',
            params: { taskId },
            query: { expandSubmission: submissionId }
          });
        })
        .catch(error => {
          console.error('获取学生提交记录失败:', error);
          // 如果获取失败，则跳转到普通的提交列表页面
          this.$router.push({ name: 'TaskSubmissions', params: { taskId } });
        });
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
    },

    // 标记消息为已读
    markMessagesAsRead() {
      axios.post(`http://localhost:8080/api/chat/mark-read/${this.enterpriseId}/${this.studentId}`)
        .catch(error => {
          console.error('标记消息为已读失败:', error);
        });
    },

    // 处理网络状态变化
    handleNetworkChange(event) {
      if (event.type === 'online') {
        console.log('网络连接恢复，尝试发送未发送的消息');
        this.trySendPendingMessages();
      } else if (event.type === 'offline') {
        console.log('网络断开，消息将保存到本地');
      }
    },

    // 加载本地存储的未发送消息
    loadPendingMessages() {
      const storageKey = `pendingMessages_${this.enterpriseId}_${this.studentId}`;
      const pendingMessages = JSON.parse(localStorage.getItem(storageKey) || '[]');

      if (pendingMessages.length === 0) {
        return;
      }

      // 将本地存储的消息添加到消息列表中
      pendingMessages.forEach(message => {
        // 将字符串时间转换为日期对象
        const messageObj = {
          ...message,
          timestamp: new Date(message.timestamp),
          pending: true // 标记为待发送状态
        };

        // 检查消息是否已存在
        const exists = this.messages.some(msg => msg.id === message.id);
        if (!exists) {
          this.messages.push(messageObj);
        }
      });

      // 按时间排序
      this.messages.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    },

    // 尝试发送本地存储的未发送消息
    trySendPendingMessages() {
      const storageKey = `pendingMessages_${this.enterpriseId}_${this.studentId}`;
      const pendingMessages = JSON.parse(localStorage.getItem(storageKey) || '[]');

      if (pendingMessages.length === 0) {
        return;
      }

      console.log(`尝试发送 ${pendingMessages.length} 条未发送的消息`);

      // 创建一个成功发送的消息数组
      const successfulMessages = [];

      // 使用Promise.all并行发送所有消息
      const sendPromises = pendingMessages.map(message => {
        // 将字符串时间转换回日期对象
        const messageToSend = {
          ...message,
          timestamp: new Date(message.timestamp),
          pending: undefined // 移除本地标记
        };

        return axios.post('http://localhost:8080/api/chat/send', messageToSend)
          .then(() => {
            // 标记消息发送成功
            successfulMessages.push(message);
            return true;
          })
          .catch(error => {
            console.error('重新发送消息失败:', error);
            return false;
          });
      });

      Promise.all(sendPromises).then(() => {
        if (successfulMessages.length > 0) {
          // 从本地存储中移除成功发送的消息
          const remainingMessages = pendingMessages.filter(msg =>
            !successfulMessages.some(successMsg => successMsg.id === msg.id)
          );

          localStorage.setItem(storageKey, JSON.stringify(remainingMessages));

          // 更新UI中的消息状态
          this.messages.forEach(msg => {
            if (msg.pending && successfulMessages.some(successMsg => successMsg.id === msg.id)) {
              msg.pending = false;
            }
          });

          console.log(`成功发送了 ${successfulMessages.length} 条消息，还有 ${remainingMessages.length} 条未发送`);

          // 如果有消息发送成功，显示提示
          if (successfulMessages.length > 0) {
            const messageElement = document.createElement('div');
            messageElement.className = 'network-success-message';
            messageElement.textContent = `成功发送了 ${successfulMessages.length} 条离线消息`;
            messageElement.style.color = '#67c23a';
            messageElement.style.textAlign = 'center';
            messageElement.style.padding = '10px';
            messageElement.style.margin = '10px 0';
            messageElement.style.fontSize = '12px';
            messageElement.style.backgroundColor = '#f0f9eb';
            messageElement.style.borderRadius = '4px';

            const container = this.$refs.messagesContainer;
            if (container) {
              container.appendChild(messageElement);
              setTimeout(() => {
                container.removeChild(messageElement);
              }, 3000);
            }
          }
        }
      });
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

.student-name {
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

.message.pending .message-content {
  opacity: 0.7;
  position: relative;
}

.message.pending .message-content::after {
  content: '•••'; /* 三个点 */
  position: absolute;
  right: 10px;
  bottom: 10px;
  font-size: 12px;
  color: #909399;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0% { opacity: 0.2; }
  50% { opacity: 1; }
  100% { opacity: 0.2; }
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

/* 右侧学生信息区域 */
.student-info-panel {
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

.student-avatar {
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

.student-details p {
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

  .student-info-panel {
    max-height: none;
  }

  .messages-container {
    max-height: 400px;
  }
}
</style>