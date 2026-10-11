<template>
  <div class="navbar">
    <div class="navbar-left">
      <button class="back-btn">返回</button>
    </div>
    <div class="navbar-center">
      <h1 class="navbar-title">系统管理后台</h1>
    </div>
    <div class="navbar-right">
      <span class="user-name">张三</span>
      <button class="logout-btn">注销</button>
    </div>
  </div>
</template>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #87CEFA;  /* 浅蓝色背景 */
  padding: 0.5rem 2rem;  /* 缩短纵向高度 */
  color: white;
  border-radius: 8px;
  min-height: 3rem;  /* 添加最小高度，防止被压缩 */
}

.navbar-left, .navbar-center, .navbar-right {
  display: flex;
  align-items: center;
}

.navbar-left button, .navbar-right button {
  padding: 0.5rem 1rem;
  background: white;
  color: #87CEFA;  /* 浅蓝色按钮 */
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.navbar-left button:hover, .navbar-right button:hover {
  background: #40a9ff;
}

.navbar-title {
  font-size: 1.2rem;
  text-align: center;  /* 确保标题在中间 */
  flex-grow: 1;  /* 让标题区分配剩余空间 */
}

.navbar-right {
  display: flex;
  align-items: center;
}

.navbar-right span {
  margin-right: 1rem;
}
</style>
