<template>
  <div class="chat-container">
    <h1>腾讯</h1>
    <div class="chat-box">
      <div v-for="message in messages" :key="message.id" class="message-item" :class="message.sender">
        <span>{{ message.sender }}:</span> <p>{{ message.content }}</p>
      </div>
    </div>
    <div class="chat-input">
      <input v-model="newMessage" type="text" placeholder="输入消息..." @keyup.enter="sendMessage"/>
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      newMessage: '',
      messages: [
        { id: 1, sender: '学生', content: '你好，企业！我已经完成了任务。' }
      ]
    };
  },
  methods: {
    sendMessage() {
      if (this.newMessage.trim()) {
        this.messages.push({
          id: this.messages.length + 1,
          sender: '学生',
          content: this.newMessage
        });
        this.newMessage = '';
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.chat-box {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 8px;
}

.message-item {
  margin-bottom: 1rem;
}

.message-item span {
  font-weight: bold;
  margin-right: 0.5rem;
}

.message-item.p {
  background: #fff;
  padding: 0.8rem;
  border-radius: 5px;
  max-width: 70%;
}

.message-item.企业 {
  text-align: left;
}

.message-item.学生 {
  text-align: right;
}

.chat-input {
  display: flex;
  gap: 10px;
}

.chat-input input {
  flex: 1;
  padding: 0.8rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.chat-input button {
  padding: 0.8rem 1.5rem;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
