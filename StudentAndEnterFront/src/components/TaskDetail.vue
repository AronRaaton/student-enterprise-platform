<template>
  <div class="task-detail-container">
    <!-- 任务概要 -->
    <h2 class="task-title">任务概要：任务名称</h2>

    <!-- 任务描述 -->
    <div class="task-description">
      <h3 class="detail-heading">任务描述</h3>
      <textarea class="description-text" readonly>
        这里是任务的详细描述，包含任务的具体内容、完成要求、预计时间等。学生可以根据任务描述来判断是否符合自己的能力，并选择是否接受任务。
      </textarea>
    </div>

    <!-- 任务技术栈 -->
    <div class="task-tech-stack">
      <h3 class="detail-heading">任务技术栈</h3>
      <p class="detail-content">Vue.js, Node.js, Express, MongoDB</p>
    </div>
  </div>
</template>

<style scoped>
.task-detail-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f9f9f9;
}

.task-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 1.5rem;
}

.task-description {
  margin-bottom: 2rem;
}

.detail-heading {
  font-size: 1.5rem;
  font-weight: bold;
  color: #000000;
  margin-bottom: 0.5rem;
}

.description-text {
  width: 100%;
  height: 40vh;  /* 占据屏幕40%的纵向宽度 */
  padding: 1rem;
  font-size: 1.1rem;
  color: #000;  /* 任务描述文字为黑色 */
  background-color: #f4f4f4;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;  /* 禁止修改大小 */
  white-space: pre-line;  /* 保持换行 */
  outline: none;
}

.task-tech-stack {
  margin-bottom: 2rem;
}

.detail-content {
  font-size: 1.1rem;
  color: #000;  /* 技术栈文字为黑色 */
  line-height: 1.6;
}
</style>
