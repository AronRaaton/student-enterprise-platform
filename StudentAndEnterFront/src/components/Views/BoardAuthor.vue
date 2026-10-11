<template>
  <div class="notice-approval">
    <h1>公告审核</h1>
    <div class="notice-list">
      <div v-for="notice in announcements" :key="notice.id" class="notice-item">
        <h2>{{ notice.title }}</h2>
        <p>{{ notice.content }}</p>
        <span class="date">{{ notice.publishDate }}</span>
        <button @click="approveAnnouncement(notice.id)">审核通过</button>
        <button @click="rejectAnnouncement(notice.id)">拒绝</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      announcements: [
        { id: 1, title: '春季招聘会', content: '我们即将举办春季招聘会，请各位同学积极参与！', publishDate: '2025-03-01' },
        { id: 2, title: '技术分享会', content: '本周五将有一场技术分享会，欢迎同学们报名参加。', publishDate: '2025-03-05' }
      ]
    };
  },
  methods: {
    approveAnnouncement(id) {
      console.log(`公告ID ${id} 已通过审核`);
    },
    rejectAnnouncement(id) {
      console.log(`公告ID ${id} 已被拒绝`);
    }
  }
}
</script>

<style scoped>
.notice-approval {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.notice-list {
  max-height: 500px;
  overflow-y: auto;
}

.notice-item {
  background: #fff;
  padding: 1rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

button {
  padding: 0.5rem 1rem;
  margin: 0.5rem;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #40a9ff;
}
</style>
