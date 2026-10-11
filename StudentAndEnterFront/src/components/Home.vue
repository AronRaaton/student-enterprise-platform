<template>
  <div class="oj-container">
    <header class="header">
      <h1>可选任务列表</h1>
      <div class="search-box">
        <input type="text" placeholder="输入任务编号或标题">
      </div>
    </header>

    <div class="main-content">
      <!-- 左侧任务列表 -->
      <div class="task-list">
        <h2 class="section-title">任务列表</h2>
        <div class="sub-title">List Of Tasks</div>

        <table class="task-table">
          <thead>
          <tr>
            <th>编号</th>
            <th>任务名称</th>
            <th>发布公司</th>
            <th>简介</th>
            <th>技术栈</th>
            <th>完成人数</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="task in tasks" :key="task.id">
            <td>{{ task.id }}</td>
            <td class="title">{{ task.title }}</td>
            <td>{{ task.company }}</td>
            <td>
              <a class="clickable" @click="showDetail(task)">{{ task.description }}</a>
            </td>
            <td>
              <span class="tech-tag" v-for="tech in task.techStack" :key="tech">
                {{ tech }}
              </span>
            </td>
            <td>{{ task.completedCount }}</td>
            <td>
              <button class="detail-btn" @click="showDetail(task)">详情</button>
            </td>
          </tr>
          </tbody>
        </table>

        <div class="pagination">
          <button>上一页</button>
          <span>1/5</span>
          <button>下一页</button>
        </div>
      </div>

      <!-- 右侧公告栏 -->
      <div class="sidebar">
        <div class="notice-board">
          <h3>公告栏</h3>
          <div class="notice-item" v-for="notice in notices" :key="notice.date">
            <div class="notice-title">{{ notice.title }}</div>
            <div class="notice-date">{{ notice.date }}</div>
          </div>
          <div class="more-notice">了解更多公告</div>
        </div>
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
          id: 1000,
          title: '在线评测系统开发',
          company: '字节跳动',
          description: '开发新一代在线编程评测平台',
          techStack: ['Vue3', 'Spring Boot', 'MySQL'],
          completedCount: 45
        },
        {
          id: 1001,
          title: '智能推荐算法实现',
          company: '阿里巴巴',
          description: '基于机器学习的推荐系统开发',
          techStack: ['Python', 'TensorFlow', 'Spark'],
          completedCount: 32
        },
        {
          id: 1002,
          title: '云存储系统优化',
          company: '腾讯云',
          description: '分布式文件存储系统性能优化',
          techStack: ['Go', 'Kubernetes', 'Redis'],
          completedCount: 28
        }
      ],
      notices: [
        { title: '2024夏季校园招聘计划', date: '2024-05-20' },
        { title: '技术文档编写规范更新', date: '2024-05-18' },
        { title: '开发者社区建设计划', date: '2024-05-15' }
      ]
    }
  },
  methods: {
    showDetail(task) {
      // 此处可添加跳转逻辑
      alert(`查看任务详情：\n${task.title}\n${task.description}`)
    }
  }
}
</script>

<style scoped>
.oj-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 30px;
  text-align: center;
}

.search-box input {
  width: 600px;
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 16px;
}

.main-content {
  display: grid;
  grid-template-columns: 4fr 1fr;
  gap: 40px;
}

.task-table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  table-layout: fixed;
}

.task-table th,
.task-table td {
  padding: 15px;
  border: 1px solid #eee;
  text-align: left;
  vertical-align: top;
}

.task-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.title {
  font-weight: 500;
  color: #1677ff;
}

.tech-tag {
  display: inline-block;
  padding: 4px 12px;
  margin: 2px;
  background: #f0f6ff;
  border-radius: 15px;
  color: #1677ff;
  font-size: 0.9em;
}

.clickable {
  color: #1677ff;
  cursor: pointer;
  text-decoration: underline;
}

.clickable:hover {
  color: #0958d9;
}

.detail-btn {
  padding: 6px 12px;
  background: #1677ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.detail-btn:hover {
  background: #0958d9;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.pagination button {
  padding: 8px 20px;
  background: #f0f0f0;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
}

.sidebar {
  background: #ffffff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.notice-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.notice-title {
  font-weight: 500;
  margin-bottom: 5px;
}

.notice-date {
  color: #8c8c8c;
  font-size: 0.9em;
}

.more-notice {
  margin-top: 15px;
  color: #1677ff;
  cursor: pointer;
  text-align: center;
}
</style>