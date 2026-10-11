<template>
  <div class="announcement-container">
    <h1>公告</h1>
    <div class="announcement-list">
      <div v-for="announcement in announcements" :key="announcement.id" class="announcement-item">
        <h2>{{ announcement.title }}</h2>
        <p>{{ announcement.content }}</p>
        <span class="date">{{ announcement.publishDate }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      announcements: [
        {
          id: 1,
          title: '春季招聘会',
          content: '我们即将举办春季招聘会，请各位同学积极参与！',
          publishDate: '2025-03-01'
        },
        {
          id: 2,
          title: '技术分享会',
          content: '本周五将有一场技术分享会，欢迎同学们报名参加。',
          publishDate: '2025-03-05'
        },
        // 更多公告数据...
      ]
    };
  }
}
</script>

<style scoped>
.announcement-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.announcement-list {
  margin-top: 2rem;
  max-height: 500px; /* 允许滚动 */
  overflow-y: auto;
}

.announcement-item {
  background: #fff;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-left: 5px solid #1890ff; /* 增加左边蓝色边框 */
}

.announcement-item h2 {
  font-size: 1.5rem;
  color: #333;
}

.date {
  font-size: 0.9rem;
  color: #888;
  margin-top: 0.5rem;
}
</style>
