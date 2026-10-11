<template>
  <div class="enterprise-detail">
    <h1>企业详情</h1>
    <div class="detail-content">
      <h2>{{ enterprise.name }}</h2>
      <p><strong>企业ID:</strong> {{ enterprise.enterpriseId }}</p>
      <p><strong>注册邮箱:</strong> {{ enterprise.email }}</p>
      <p><strong>注册时间:</strong> {{ enterprise.registerDate }}</p>
      <p><strong>发布任务:</strong> {{ enterprise.publishedTasks }}</p>
      <p><strong>平均评分:</strong> {{ enterprise.avgRating }}</p>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      enterprise: {
        enterpriseId: 'E1001',
        name: 'Example Collaboration Lab',
        email: 'demo@example.invalid',
        registerDate: '2022-12-15',
        publishedTasks: 12,
        avgRating: 4.5
      }
    };
  }
}
</script>

<style scoped>
.enterprise-detail {
  padding: 2rem;
}

.detail-content {
  background: #fff;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.detail-content h2 {
  font-size: 2rem;
  color: #333;
}

.detail-content p {
  font-size: 1rem;
  margin: 0.5rem 0;
}
</style>
