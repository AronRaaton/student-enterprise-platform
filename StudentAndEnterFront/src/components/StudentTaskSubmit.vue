<template>
  <div class="submit-task-container">
    <h2 class="page-title">提交任务</h2>

    <div class="task-info">
      <h3 class="task-title">任务标题：任务名称</h3>
      <p class="task-summary">任务简介：这里是任务的简短介绍。</p>
      <p class="task-description">任务内容：这里是任务的详细描述，包含任务要求和具体内容。</p>
    </div>

    <div class="submit-area">
      <h3 class="submit-title">提交内容</h3>
      <textarea class="submit-text" placeholder="请输入任务完成的文字描述..." rows="6"></textarea>

      <div class="file-upload">
        <h4>或上传文件</h4>
        <input type="file" class="file-input" />
      </div>

      <button class="submit-button">提交任务</button>
    </div>
  </div>
</template>

<style scoped>
.submit-task-container {
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

.task-info {
  margin-bottom: 2rem;
}

.task-title {
  font-size: 1.5rem;
  color: #333;
}

.task-summary,
.task-description {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 1rem;
}

.submit-area {
  margin-top: 2rem;
}

.submit-title {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 1rem;
}

.submit-text {
  width: 100%;
  padding: 1rem;
  font-size: 1.1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: none;
  margin-bottom: 1.5rem;
}

.file-upload {
  margin-bottom: 1.5rem;
}

.file-input {
  font-size: 1rem;
}

.submit-button {
  padding: 0.75rem 2rem;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.1rem;
}

.submit-button:hover {
  background-color: #007bff;
}
</style>
