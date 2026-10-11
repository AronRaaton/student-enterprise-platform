<template>
  <div class="task-list-container">
    <h2 class="page-title">已领取任务</h2>

    <div class="task-card">
      <h3 class="task-title">任务标题：任务1</h3>
      <p class="task-summary">任务简介：这是任务1的简介，简短介绍任务的目标和要求。</p>
      <p class="task-tech-stack"><strong>技术栈：</strong>Vue.js, Node.js</p>
      <p class="task-status">
        <strong>提交状态：</strong>
        <span class="status-pending">未提交</span>
      </p>
    </div>

    <div class="task-card">
      <h3 class="task-title">任务标题：任务2</h3>
      <p class="task-summary">任务简介：这是任务2的简介，简短介绍任务的目标和要求。</p>
      <p class="task-tech-stack"><strong>技术栈：</strong>React, Express</p>
      <p class="task-status">
        <strong>提交状态：</strong>
        <span class="status-submitted">已提交</span>
      </p>
    </div>

    <div class="task-card">
      <h3 class="task-title">任务标题：任务3</h3>
      <p class="task-summary">任务简介：这是任务3的简介，简短介绍任务的目标和要求。</p>
      <p class="task-tech-stack"><strong>技术栈：</strong>Angular, MongoDB</p>
      <p class="task-status">
        <strong>提交状态：</strong>
        <span class="status-modified">待修改</span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.task-list-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f9f9f9;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 2rem;
}

.task-card {
  background-color: white;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-title {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.task-summary {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 0.5rem;
}

.task-tech-stack {
  font-size: 1rem;
  color: #000;
  margin-bottom: 0.5rem;
}

.task-status {
  font-size: 1rem;
}

.status-pending {
  color: #f39c12; /* Orange */
}

.status-submitted {
  color: #2ecc71; /* Green */
}

.status-modified {
  color: #e74c3c; /* Red */
}
</style>
