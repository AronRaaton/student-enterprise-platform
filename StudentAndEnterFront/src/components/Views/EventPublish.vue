<template>
  <div class="event-publish-container module-page module-form">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>{{ isEditing ? '编辑活动' : '发布企业活动' }}</h1>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="showSuccess" class="success-container">
      <div class="success-icon">✅</div>
      <h2>{{ isEditing ? '活动更新成功' : '活动发布成功' }}</h2>
      <div class="success-actions">
        <button class="action-button" @click="goToEvents">返回活动列表</button>
        <button class="action-button" @click="publishAnother">发布另一个活动</button>
      </div>
    </div>

    <div v-else class="form-container">
      <form @submit.prevent="submitEvent" class="event-form">
        <div class="form-group">
          <label for="title">活动标题 <span class="required">*</span></label>
          <input
            type="text"
            id="title"
            v-model="event.title"
            required
            :class="{ 'error': submitted && !event.title }"
          >
          <div v-if="submitted && !event.title" class="error-message">请输入活动标题</div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="eventType">活动类型 <span class="required">*</span></label>
            <select
              id="eventType"
              v-model="event.eventType"
              required
              :class="{ 'error': submitted && !event.eventType }"
            >
              <option value="">请选择活动类型</option>
              <option value="CAMPUS_VISIT">校园参观</option>
              <option value="ANNIVERSARY">周年庆典</option>
              <option value="WORKSHOP">工作坊</option>
              <option value="SEMINAR">研讨会</option>
              <option value="CAREER_FAIR">招聘会</option>
              <option value="OTHER">其他活动</option>
            </select>
            <div v-if="submitted && !event.eventType" class="error-message">请选择活动类型</div>
          </div>

          <div class="form-group">
            <label for="location">活动地点 <span class="required">*</span></label>
            <input
              type="text"
              id="location"
              v-model="event.location"
              required
              :class="{ 'error': submitted && !event.location }"
            >
            <div v-if="submitted && !event.location" class="error-message">请输入活动地点</div>
          </div>
        </div>

        <div class="form-group">
          <label for="description">活动描述 <span class="required">*</span></label>
          <textarea
            id="description"
            v-model="event.description"
            rows="5"
            required
            :class="{ 'error': submitted && !event.description }"
          ></textarea>
          <div v-if="submitted && !event.description" class="error-message">请输入活动描述</div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startTime">开始时间 <span class="required">*</span></label>
            <input
              type="datetime-local"
              id="startTime"
              v-model="event.startTime"
              required
              :class="{ 'error': submitted && !event.startTime }"
            >
            <div v-if="submitted && !event.startTime" class="error-message">请选择开始时间</div>
          </div>

          <div class="form-group">
            <label for="endTime">结束时间 <span class="required">*</span></label>
            <input
              type="datetime-local"
              id="endTime"
              v-model="event.endTime"
              required
              :min="event.startTime"
              :class="{ 'error': submitted && (!event.endTime || event.endTime < event.startTime) }"
            >
            <div v-if="submitted && !event.endTime" class="error-message">请选择结束时间</div>
            <div v-if="submitted && event.endTime < event.startTime" class="error-message">结束时间不能早于开始时间</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="registrationDeadline">报名截止时间 <span class="required">*</span></label>
            <input
              type="datetime-local"
              id="registrationDeadline"
              v-model="event.registrationDeadline"
              required
              :max="event.startTime"
              :class="{ 'error': submitted && (!event.registrationDeadline || event.registrationDeadline > event.startTime) }"
            >
            <div v-if="submitted && !event.registrationDeadline" class="error-message">请选择报名截止时间</div>
            <div v-if="submitted && event.registrationDeadline > event.startTime" class="error-message">报名截止时间不能晚于开始时间</div>
          </div>

          <div class="form-group">
            <label for="maxParticipants">最大参与人数</label>
            <input
              type="number"
              id="maxParticipants"
              v-model="event.maxParticipants"
              min="1"
              max="1000"
            >
          </div>
        </div>

        <div class="form-group">
          <label for="benefits">参与福利</label>
          <textarea
            id="benefits"
            v-model="event.benefits"
            rows="3"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-button" @click="goBack">取消</button>
          <button type="submit" class="submit-button" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : (isEditing ? '更新活动' : '发布活动') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'EventPublish',
  data() {
    return {
      event: {
        title: '',
        description: '',
        eventType: '',
        location: '',
        startTime: '',
        endTime: '',
        registrationDeadline: '',
        maxParticipants: 100,
        benefits: '',
        status: 'UPCOMING'
      },
      loading: false,
      submitted: false,
      isSubmitting: false,
      showSuccess: false,
      isEditing: false,
      eventId: null,
      enterpriseId: null
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');

    // 检查是否是编辑模式
    if (this.$route.query.edit && this.$route.query.eventId) {
      this.isEditing = true;
      this.eventId = this.$route.query.eventId;
      this.fetchEventDetails();
    }
  },
  methods: {
    fetchEventDetails() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/events/${this.eventId}`)
        .then(response => {
          const event = response.data;

          // 格式化日期时间为 YYYY-MM-DDThh:mm 格式，适用于表单输入
          this.event = {
            ...event,
            startTime: this.formatDateTimeForInput(event.startTime),
            endTime: this.formatDateTimeForInput(event.endTime),
            registrationDeadline: this.formatDateTimeForInput(event.registrationDeadline)
          };

          this.loading = false;
        })
        .catch(error => {
          console.error('获取活动详情失败:', error);
          alert('获取活动详情失败，请重试');
          this.loading = false;
          this.goBack();
        });
    },
    formatDateTimeForInput(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      // 格式化为 YYYY-MM-DDThh:mm 格式
      return date.toISOString().slice(0, 16);
    },
    submitEvent() {
      this.submitted = true;

      // 基本字段验证
      if (!this.event.title || !this.event.description || !this.event.eventType ||
          !this.event.location || !this.event.startTime || !this.event.endTime ||
          !this.event.registrationDeadline) {
        return;
      }

      // 日期验证
      if (this.event.endTime < this.event.startTime) {
        return;
      }

      if (this.event.registrationDeadline > this.event.startTime) {
        return;
      }

      this.isSubmitting = true;

      // 根据时间自动设置状态
      const now = new Date();
      const startTime = new Date(this.event.startTime);
      const endTime = new Date(this.event.endTime);

      if (now < startTime) {
        this.event.status = 'UPCOMING';
      } else if (now > endTime) {
        this.event.status = 'COMPLETED';
      } else {
        this.event.status = 'ONGOING';
      }

      if (this.isEditing) {
        // 编辑模式，使用 PUT 请求更新活动
        axios.put(`http://localhost:8080/api/events/${this.eventId}`, this.event)
          .then(response => {
            console.log('活动更新成功:', response.data);
            this.showSuccess = true;
          })
          .catch(error => {
            console.error('活动更新失败:', error);
            alert('活动更新失败，请稍后重试');
          })
          .finally(() => {
            this.isSubmitting = false;
          });
      } else {
        // 创建模式，使用 POST 请求创建活动
        // 修复：将enterprise对象设置为null，让后端通过查询参数来设置企业
        const eventData = {
          ...this.event,
          enterprise: null
        };
        axios.post(`http://localhost:8080/api/events?enterpriseId=${this.enterpriseId}`, eventData)
          .then(response => {
            console.log('活动发布成功:', response.data);
            this.showSuccess = true;
          })
          .catch(error => {
            console.error('活动发布失败:', error);
            alert('活动发布失败，请稍后重试');
          })
          .finally(() => {
            this.isSubmitting = false;
          });
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    goToEvents() {
      this.$router.push({ name: 'EnterpriseEvents' });
    },
    publishAnother() {
      this.event = {
        title: '',
        description: '',
        eventType: '',
        location: '',
        startTime: '',
        endTime: '',
        registrationDeadline: '',
        maxParticipants: 100,
        benefits: '',
        status: 'UPCOMING'
      };
      this.submitted = false;
      this.showSuccess = false;
      this.isEditing = false;
      this.eventId = null;
    }
  }
};
</script>

<style scoped>
.event-publish-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
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

.success-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.action-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.action-button:hover {
  background-color: #66b1ff;
}

.form-container {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.event-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-row .form-group {
  flex: 1;
}

label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

input, textarea, select {
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.3s;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: #409eff;
}

input.error, textarea.error, select.error {
  border-color: #f56c6c;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 10px;
}

.cancel-button {
  padding: 10px 20px;
  background-color: #f4f4f5;
  color: #606266;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.submit-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.submit-button:hover {
  background-color: #66b1ff;
}

.submit-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}
</style>
