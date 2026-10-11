<template>
  <div class="event-detail-container module-page module-detail">
    <div class="back-button-container">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="!event" class="error-container">
      <p>活动信息不存在或已被删除</p>
    </div>

    <div v-else class="event-content">
      <div class="event-header">
        <div class="event-title-container">
          <span class="event-type-tag" :class="getTypeClass(event.eventType)">
            {{ getTypeText(event.eventType) }}
          </span>
          <h1 class="event-title">{{ event.title }}</h1>
        </div>
        <span class="event-status" :class="getStatusClass(event.status)">
          {{ getStatusText(event.status) }}
        </span>
      </div>

      <div class="event-info-card">
        <div class="info-section">
          <div class="info-item">
            <span class="info-icon">🏢</span>
            <span class="info-label">主办方:</span>
            <span class="info-value">{{ event.enterprise.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-icon">📍</span>
            <span class="info-label">地点:</span>
            <span class="info-value">{{ event.location }}</span>
          </div>
          <div class="info-item">
            <span class="info-icon">🕒</span>
            <span class="info-label">活动时间:</span>
            <span class="info-value">{{ formatDateTime(event.startTime) }} - {{ formatDateTime(event.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="info-icon">⏰</span>
            <span class="info-label">报名截止:</span>
            <span class="info-value">{{ formatDateTime(event.registrationDeadline) }}</span>
          </div>
          <div class="info-item">
            <span class="info-icon">👥</span>
            <span class="info-label">参与人数上限:</span>
            <span class="info-value">{{ event.maxParticipants }} 人</span>
          </div>
        </div>

        <div class="action-section">
          <button 
            v-if="event.status === 'UPCOMING' && isBeforeDeadline(event.registrationDeadline)" 
            class="register-btn" 
            @click="registerForEvent"
            :disabled="isRegistered"
          >
            {{ isRegistered ? '已报名' : '立即报名' }}
          </button>
          <div v-if="isRegistered" class="registration-status">
            <span class="status-icon">✓</span>
            <span class="status-text">已成功报名</span>
          </div>
        </div>
      </div>

      <div class="event-details">
        <div class="detail-section">
          <h2 class="section-title">活动介绍</h2>
          <div class="section-content" v-html="formatDescription(event.description)"></div>
        </div>

        <div class="detail-section" v-if="event.benefits">
          <h2 class="section-title">活动福利</h2>
          <div class="section-content" v-html="formatDescription(event.benefits)"></div>
        </div>
      </div>

      <!-- 企业信息卡片 -->
      <div class="enterprise-card">
        <h2 class="section-title">主办企业</h2>
        <div class="enterprise-info">
          <div class="enterprise-avatar">
            {{ event.enterprise.name.charAt(0) }}
          </div>
          <div class="enterprise-details">
            <h3 class="enterprise-name">{{ event.enterprise.name }}</h3>
            <p class="enterprise-type" v-if="event.enterprise.type">{{ event.enterprise.type }}</p>
            <p class="enterprise-location" v-if="event.enterprise.location">
              <span class="info-icon">📍</span> {{ event.enterprise.location }}
            </p>
            <p class="enterprise-website" v-if="event.enterprise.website">
              <span class="info-icon">🔗</span>
              <a :href="event.enterprise.website" target="_blank">{{ event.enterprise.website }}</a>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'EventDetail',
  data() {
    return {
      event: null,
      loading: true,
      studentId: null,
      isRegistered: false
    };
  },
  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchEventDetails();
  },
  methods: {
    fetchEventDetails() {
      const eventId = this.$route.params.eventId;
      
      // 获取活动详情
      axios.get(`http://localhost:8080/api/events/${eventId}`)
        .then(response => {
          this.event = response.data;
          
          // 如果已登录，检查是否已报名
          if (this.studentId) {
            this.checkRegistrationStatus();
          } else {
            this.loading = false;
          }
        })
        .catch(error => {
          console.error('获取活动详情失败:', error);
          this.loading = false;
        });
    },
    checkRegistrationStatus() {
      const eventId = this.$route.params.eventId;
      
      // 获取活动的所有报名记录
      axios.get(`http://localhost:8080/api/events/${eventId}/registrations`)
        .then(response => {
          const registrations = response.data;
          
          // 查找当前学生的报名记录
          this.isRegistered = registrations.some(reg => 
            reg.student && reg.student.studentId === parseInt(this.studentId)
          );
          
          this.loading = false;
        })
        .catch(error => {
          console.error('检查报名状态失败:', error);
          this.loading = false;
        });
    },
    registerForEvent() {
      if (!this.studentId) {
        alert('请先登录');
        return;
      }
      
      if (this.isRegistered) {
        alert('您已经报名过该活动');
        return;
      }
      
      const eventId = this.$route.params.eventId;
      
      axios.post(`http://localhost:8080/api/events/${eventId}/register?studentId=${this.studentId}`)
        .then(response => {
          alert('报名成功');
          this.isRegistered = true;
        })
        .catch(error => {
          alert('报名失败: ' + (error.response?.data || '未知错误'));
        });
    },
    isBeforeDeadline(deadline) {
      return new Date() < new Date(deadline);
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return date.toLocaleString('zh-CN');
    },
    formatDescription(text) {
      if (!text) return '';
      return text.replace(/\n/g, '<br>');
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
    },
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.event-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
}

.back-icon {
  margin-right: 5px;
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

.error-container {
  text-align: center;
  padding: 40px;
  color: #f56c6c;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.event-title-container {
  display: flex;
  flex-direction: column;
}

.event-type-tag {
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 4px;
  font-weight: bold;
  display: inline-block;
  margin-bottom: 10px;
  align-self: flex-start;
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

.event-title {
  margin: 0;
  font-size: 24px;
}

.event-status {
  font-size: 14px;
  padding: 6px 12px;
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

.event-info-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.info-section {
  flex: 1;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.info-icon {
  margin-right: 8px;
}

.info-label {
  font-weight: bold;
  color: #606266;
  margin-right: 8px;
}

.action-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.register-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
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

.registration-status {
  display: flex;
  align-items: center;
  color: #67c23a;
}

.status-icon {
  margin-right: 5px;
  font-weight: bold;
}

.event-details {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-content {
  line-height: 1.6;
  color: #606266;
}

.enterprise-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.enterprise-info {
  display: flex;
  align-items: center;
}

.enterprise-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 20px;
}

.enterprise-details {
  flex: 1;
}

.enterprise-name {
  margin: 0 0 5px 0;
  font-size: 18px;
}

.enterprise-type, .enterprise-location, .enterprise-website {
  margin: 5px 0;
  color: #606266;
}

.enterprise-website a {
  color: #409eff;
  text-decoration: none;
}

.enterprise-website a:hover {
  text-decoration: underline;
}
</style>
