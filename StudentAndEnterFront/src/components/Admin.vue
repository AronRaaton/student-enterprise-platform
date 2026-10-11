<template>
  <div class="admin-container">
    <!-- 导航菜单 -->
    <nav class="admin-nav">
      <h1>系统管理后台</h1>
      <div class="nav-items">
        <button @click="currentView='users'">用户管理</button>
        <button @click="currentView='tasks'">任务监控</button>
        <button @click="currentView='logs'">操作日志</button>
      </div>
    </nav>

    <!-- 用户管理 -->
    <div v-show="currentView === 'users'" class="user-management">
      <!-- 学生列表 -->
      <div class="user-section">
        <h2>学生用户列表 ({{ students.length }})</h2>
        <table class="wide-table">
          <thead>
          <tr>
            <th>学号</th>
            <th>姓名</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th>完成任务</th>
            <th>关注企业</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="s in students" :key="s.id">
            <td>{{ s.studentNumber }}</td>
            <td>{{ s.name }}</td>
            <td>{{ s.email }}</td>
            <td>{{ s.registerDate }}</td>
            <td>{{ s.completedTasks }}</td>
            <td>{{ s.followedEnterprises }}</td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- 企业列表 -->
      <div class="user-section">
        <h2>企业用户列表 ({{ enterprises.length }})</h2>
        <table class="wide-table">
          <thead>
          <tr>
            <th>企业ID</th>
            <th>企业名称</th>
            <th>注册邮箱</th>
            <th>注册时间</th>
            <th>发布任务</th>
            <th>平均评分</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="e in enterprises" :key="e.id">
            <td>{{ e.enterpriseId }}</td>
            <td>{{ e.name }}</td>
            <td>{{ e.email }}</td>
            <td>{{ e.registerDate }}</td>
            <td>{{ e.publishedTasks }}</td>
            <td>{{ e.avgRating }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 其他管理视图 -->
    <div v-show="currentView === 'tasks'" class="task-monitor">
      <!-- 任务监控内容 -->
    </div>

    <div v-show="currentView === 'logs'" class="log-viewer">
      <!-- 日志查看内容 -->
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentView: 'users',
      students: [
        {
          id: 1,
          studentNumber: 'S2023001',
          name: 'Demo Student A',
          email: 'demo@example.invalid',
          registerDate: '2023-03-01',
          completedTasks: 5,
          followedEnterprises: 3
        },
        // 更多模拟数据...
      ],
      enterprises: [
        {
          id: 1,
          enterpriseId: 'E1001',
          name: 'Example Collaboration Lab',
          email: 'demo@example.invalid',
          registerDate: '2022-12-15',
          publishedTasks: 12,
          avgRating: 4.5
        },
        // 更多模拟数据...
      ]
    }
  }
}
</script>

<style scoped>
.admin-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 2rem;
}

.admin-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.nav-items button {
  margin-left: 1rem;
  padding: 0.5rem 1rem;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.wide-table {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
  table-layout: auto;
}

.wide-table th,
.wide-table td {
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  text-align: left;
  white-space: nowrap;
}

.wide-table th {
  background-color: #fafafa;
  font-weight: 600;
}

.user-section {
  margin-bottom: 3rem;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-section h2 {
  color: #333;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #eee;
}
</style>