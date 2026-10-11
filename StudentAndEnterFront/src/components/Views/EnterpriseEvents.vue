<template>
  <div class="events-container module-page module-list">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>企业活动管理</h1>
      </div>
      <div class="header-right">
        <button class="publish-button" @click="publishEvent">发布新活动</button>
      </div>
    </div>

    <div class="filter-section">
      <select v-model="typeFilter" class="filter-select">
        <option value="all">全部类型</option>
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

    <div class="events-list">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="events.length === 0" class="empty-state">
        <div class="empty-icon">🎪</div>
        <p>暂无活动信息</p>
        <button class="create-button" @click="publishEvent">创建第一个活动</button>
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

          <div class="event-details">
            <div class="detail-item">
              <span class="detail-icon">📅</span>
              <span class="detail-text">开始时间: {{ formatDateTime(event.startTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-icon">📆</span>
              <span class="detail-text">结束时间: {{ formatDateTime(event.endTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-icon">📍</span>
              <span class="detail-text">地点: {{ event.location }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-icon">👥</span>
              <span class="detail-text">已报名: {{ event.registrations ? event.registrations.length : 0 }}/{{ event.maxParticipants }}</span>
            </div>
          </div>

          <div class="event-description">
            <p>{{ truncateText(event.description, 100) }}</p>
          </div>

          <div class="event-actions">
            <button class="action-button view-button" @click="viewEventDetails(event.eventId)">
              查看详情
            </button>
            <button class="action-button edit-button" @click="editEvent(event.eventId)">
              编辑
            </button>
            <button class="action-button delete-button" @click="confirmDelete(event)">
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认删除对话框 -->
    <div v-if="showDeleteConfirm" class="delete-confirm-overlay">
      <div class="delete-confirm-dialog">
        <h3>确认删除</h3>
        <p>您确定要删除活动 "{{ eventToDelete.title }}" 吗？此操作不可撤销。</p>
        <div class="dialog-buttons">
          <button class="cancel-button" @click="showDeleteConfirm = false">取消</button>
          <button class="confirm-button" @click="deleteEvent">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'EnterpriseEvents',
  data() {
    return {
      events: [],
      loading: true,
      enterpriseId: null,
      typeFilter: 'all',
      statusFilter: 'all',
      showDeleteConfirm: false,
      eventToDelete: null
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
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchEvents();
  },
  methods: {
    fetchEvents() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/events/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.events = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取活动列表失败:', error);
          this.loading = false;
        });
    },
    goBack() {
      this.$router.go(-1);
    },
    publishEvent() {
      this.$router.push({ name: 'EventPublish' });
    },
    viewEventDetails(eventId) {
      this.$router.push({ name: 'EventDetail', params: { eventId } });
    },
    editEvent(eventId) {
      this.$router.push({
        name: 'EventPublish',
        query: { edit: true, eventId }
      });
    },
    confirmDelete(event) {
      this.eventToDelete = event;
      this.showDeleteConfirm = true;
    },
    deleteEvent() {
      if (!this.eventToDelete) return;

      axios.delete(`http://localhost:8080/api/events/${this.eventToDelete.eventId}`)
        .then(() => {
          this.events = this.events.filter(
            evt => evt.eventId !== this.eventToDelete.eventId
          );
          this.showDeleteConfirm = false;
          this.eventToDelete = null;
        })
        .catch(error => {
          console.error('删除活动失败:', error);
          alert('删除活动失败，请稍后重试');
        });
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return date.toLocaleString('zh-CN');
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
    getTypeText(type) {
      const typeMap = {
        'CAMPUS_VISIT': '校园参观',
        'ANNIVERSARY': '周年庆典',
        'WORKSHOP': '工作坊',
        'SEMINAR': '研讨会',
        'CAREER_FAIR': '招聘会',
        'OTHER': '其他活动'
      };
      return typeMap[type] || type;
    },
    getTypeClass(type) {
      return `type-${type.toLowerCase()}`;
    },
    getStatusText(status) {
      const statusMap = {
        'UPCOMING': '即将开始',
        'ONGOING': '进行中',
        'COMPLETED': '已结束',
        'CANCELLED': '已取消'
      };
      return statusMap[status] || status;
    },
    getStatusClass(status) {
      return `status-${status.toLowerCase()}`;
    }
  }
};
</script>

<style scoped>
.events-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
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

.publish-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.publish-button:hover {
  background-color: #66b1ff;
}

.filter-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: white;
  font-size: 14px;
  width: 150px;
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
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.create-button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.event-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.event-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 20px;
  background-color: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.event-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.event-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.event-type-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.type-campus_visit {
  background-color: #e6f7ff;
  color: #1890ff;
}

.type-anniversary {
  background-color: #fff7e6;
  color: #fa8c16;
}

.type-workshop {
  background-color: #f6ffed;
  color: #52c41a;
}

.type-seminar {
  background-color: #f9f0ff;
  color: #722ed1;
}

.type-career_fair {
  background-color: #fff2e8;
  color: #fa541c;
}

.type-other {
  background-color: #f4f4f5;
  color: #909399;
}

.event-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-upcoming {
  background-color: #e1f3d8;
  color: #67c23a;
}

.status-ongoing {
  background-color: #e6f7ff;
  color: #1890ff;
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
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
}

.event-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.detail-icon {
  margin-right: 8px;
  font-size: 16px;
}

.event-description {
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.event-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.action-button {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.view-button {
  background-color: #ecf5ff;
  color: #409eff;
}

.view-button:hover {
  background-color: #d9ecff;
}

.edit-button {
  background-color: #f0f9eb;
  color: #67c23a;
}

.edit-button:hover {
  background-color: #e1f3d8;
}

.delete-button {
  background-color: #fef0f0;
  color: #f56c6c;
}

.delete-button:hover {
  background-color: #fde2e2;
}

.delete-confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.delete-confirm-dialog {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.delete-confirm-dialog h3 {
  margin-top: 0;
  color: #f56c6c;
}

.dialog-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-button {
  padding: 8px 15px;
  background-color: #f4f4f5;
  color: #606266;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.confirm-button {
  padding: 8px 15px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>
