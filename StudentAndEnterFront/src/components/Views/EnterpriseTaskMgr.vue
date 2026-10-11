<template>
  <div class="task-management">
    <h1>管理任务</h1>
    <div class="task-list">
      <div v-for="task in tasks" :key="task.id" class="task-item">
        <h2>{{ task.title }}</h2>
        <p><strong>技术栈:</strong> {{ task.techStack }}</p>
        <p><strong>发布时间:</strong> {{ task.publishDate }}</p>
        <p><strong>发布企业:</strong> {{ task.enterpriseName }}</p>
        <p>{{ task.summary }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      tasks: [
        {
          id: 1,
          title: '前端开发任务',
          summary: '开发一个响应式网站',
          techStack: 'Vue.js, HTML, CSS',
          publishDate: '2025-03-01',
          enterpriseName: '腾讯科技'
        },
        // 更多任务数据...
      ]
    };
  }
}
</script>

<style scoped>
.task-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.task-item {
  background: #fff;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-item h2 {
  font-size: 1.5rem;
  color: #333;
}

.task-item p {
  font-size: 1rem;
  margin: 0.5rem 0;
}
</style>
