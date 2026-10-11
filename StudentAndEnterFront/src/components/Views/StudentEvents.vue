<template>
  <div class="events-container">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>企业活动</h1>
      </div>
      <div class="filter-container">
        <select v-model="typeFilter" class="filter-select">
          <option value="all">全部活动</option>
          <option value="CAMPUS_VISIT">校园参观</option>
          <option value="ANNIVERSARY">周年庆典</option>
          <option value="WORKSHOP">工作坊</option>
          <option value="SEMINAR">研讨会</option>
          <option value="CAREER_FAIR">招聘会</option>
          <option value="OTHER">其他活动</option>
        </select>
        <select v-model="statusFilter" class="filter-select">
          <option value="all">全部状态</option>
          <option value="UPCOMING">即将开始</option>
          <option value="ONGOING">进行中</option>
          <option value="COMPLETED">已结束</option>
        </select>
      </div>
    </div>

    <div class="events-list">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredEvents.length === 0" class="empty-state">
        <div class="empty-icon">🎪</div>
        <p>暂无符合条件的活动</p>
      </div>

      <div v-else class="event-cards">
        <div v-for="event in filteredEvents" :key="event.eventId" class="event-card">
          <div class="event-header">
            <span class="event-type-tag" :class="getTypeClass(event.eventType)">
              {{ getTypeText(event.eventType) }}
            </span>
            <span class="event-status" :class="getStatusClass(event.status)">
              {{ getStatusText(event.status) }}
            </span>
          </div>

          <h2 class="event-title">{{ event.title }}</h2>

          <div class="event-info">
            <p class="event-enterprise">
              <span class="info-icon">🏢</span> {{ event.enterprise.name }}
            </p>
            <p class="event-location">
              <span class="info-icon">📍</span> {{ event.location }}
            </p>
            <p class="event-time">
              <span class="info-icon">🕒</span> {{ formatDateTime(event.startTime) }}
            </p>
            <p class="event-deadline">
              <span class="info-icon">⏰</span> 报名截止: {{ formatDateTime(event.registrationDeadline) }}
            </p>
          </div>

          <div class="event-description">
            {{ truncateText(event.description, 100) }}
          </div>

          <div class="event-actions">
            <button class="view-details-btn" @click="viewEventDetails(event.eventId)">查看详情</button>
            <button
              v-if="event.status === 'UPCOMING' && isBeforeDeadline(event.registrationDeadline)"
              class="register-btn"
              @click="registerForEvent(event.eventId)"
              :disabled="isRegistered(event.eventId)"
            >
              {{ isRegistered(event.eventId) ? '已报名' : '立即报名' }}
            </button>
          </div>
        </div>
      </div>

      <div class="pagination-container" v-if="totalPages > 1">
        <button
          class="pagination-btn"
          :disabled="currentPage === 0"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button
          class="pagination-btn"
          :disabled="currentPage === totalPages - 1"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'StudentEvents',
  data() {
    return {
      events: [],
      registeredEvents: [],
      loading: true,
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      typeFilter: 'all',
      statusFilter: 'all',
      studentId: null
    };
  },
  computed: {
    filteredEvents() {
      let filtered = this.events;

      if (this.typeFilter !== 'all') {
        filtered = filtered.filter(event => event.eventType === this.typeFilter);
      }

      if (this.statusFilter !== 'all') {
        filtered = filtered.filter(event => event.status === this.statusFilter);
      }

      return filtered;
    }
  },
  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchEvents();
    this.fetchRegisteredEvents();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    fetchEvents() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/events?page=${this.currentPage}&size=${this.pageSize}`)
        .then(response => {
          this.events = response.data.content;
          this.totalPages = response.data.totalPages;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取活动列表失败:', error);
          this.loading = false;
        });
    },
    fetchRegisteredEvents() {
      if (!this.studentId) return;

      axios.get(`http://localhost:8080/api/events/student/${this.studentId}`)
        .then(response => {
          this.registeredEvents = response.data;
        })
        .catch(error => {
          console.error('获取已报名活动失败:', error);
        });
    },
    changePage(page) {
      this.currentPage = page;
      this.fetchEvents();
    },
    viewEventDetails(eventId) {
      this.$router.push({ name: 'EventDetail', params: { eventId } });
    },
    registerForEvent(eventId) {
      if (!this.studentId) {
        alert('请先登录');
        return;
      }

      if (this.isRegistered(eventId)) {
        alert('您已经报名过该活动');
        return;
      }

      axios.post(`http://localhost:8080/api/events/${eventId}/register?studentId=${this.studentId}`)
        .then(response => {
          alert('报名成功');
          this.fetchRegisteredEvents();
        })
        .catch(error => {
          alert('报名失败: ' + (error.response?.data || '未知错误'));
        });
    },
    isRegistered(eventId) {
      return this.registeredEvents.some(event => event.eventId === eventId);
    },
    isBeforeDeadline(deadline) {
      return new Date() < new Date(deadline);
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return date.toLocaleString('zh-CN');
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    getTypeText(type) {
      switch (type) {
        case 'CAMPUS_VISIT': return '校园参观';
        case 'ANNIVERSARY': return '周年庆典';
        case 'WORKSHOP': return '工作坊';
        case 'SEMINAR': return '研讨会';
        case 'CAREER_FAIR': return '招聘会';
        case 'OTHER': return '其他活动';
        default: return '未知类型';
      }
    },
    getTypeClass(type) {
      switch (type) {
        case 'CAMPUS_VISIT': return 'type-campus-visit';
        case 'ANNIVERSARY': return 'type-anniversary';
        case 'WORKSHOP': return 'type-workshop';
        case 'SEMINAR': return 'type-seminar';
        case 'CAREER_FAIR': return 'type-career-fair';
        case 'OTHER': return 'type-other';
        default: return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'UPCOMING': return '即将开始';
        case 'ONGOING': return '进行中';
        case 'COMPLETED': return '已结束';
        case 'CANCELLED': return '已取消';
        default: return '未知状态';
      }
    },
    getStatusClass(status) {
      switch (status) {
        case 'UPCOMING': return 'status-upcoming';
        case 'ONGOING': return 'status-ongoing';
        case 'COMPLETED': return 'status-completed';
        case 'CANCELLED': return 'status-cancelled';
        default: return '';
      }
    }
  }
};
</script>

<style scoped>
.events-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.back-button {
  display: flex;
  align-items: center;
  padding: 8px 15px;
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 15px;
  font-size: 14px;
}

.back-icon {
  margin-right: 5px;
  font-size: 16px;
}

.back-button:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: white;
  font-size: 14px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.event-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.event-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s;
}

.event-card:hover {
  transform: translateY(-5px);
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.event-type-tag {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.type-campus-visit {
  background-color: #e1f3d8;
  color: #67c23a;
}

.type-anniversary {
  background-color: #ffd2d2;
  color: #f56c6c;
}

.type-workshop {
  background-color: #d9ecff;
  color: #409eff;
}

.type-seminar {
  background-color: #e6a23c1a;
  color: #e6a23c;
}

.type-career-fair {
  background-color: #f0f9eb;
  color: #67c23a;
}

.type-other {
  background-color: #f4f4f5;
  color: #909399;
}

.event-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.status-upcoming {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-ongoing {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-completed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
}

.event-title {
  font-size: 18px;
  margin: 0 0 12px 0;
}

.event-info {
  margin-bottom: 12px;
}

.event-info p {
  margin: 5px 0;
  display: flex;
  align-items: center;
}

.info-icon {
  margin-right: 8px;
}

.event-description {
  margin-bottom: 16px;
  color: #606266;
  flex-grow: 1;
}

.event-actions {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
}

.view-details-btn, .register-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.view-details-btn {
  background-color: #ecf5ff;
  color: #409eff;
}

.view-details-btn:hover {
  background-color: #d9ecff;
}

.register-btn {
  background-color: #409eff;
  color: white;
}

.register-btn:hover {
  background-color: #66b1ff;
}

.register-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.page-info {
  margin: 0 16px;
}
</style>
