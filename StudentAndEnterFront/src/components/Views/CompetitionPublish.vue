<template>
  <div class="competition-publish-container module-page module-form">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>{{ isEditing ? '编辑竞赛' : '发布企业竞赛' }}</h1>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="showSuccess" class="success-container">
      <div class="success-icon">✅</div>
      <h2>{{ isEditing ? '竞赛更新成功' : '竞赛发布成功' }}</h2>
      <div class="success-actions">
        <button class="action-button" @click="goToCompetitions">返回竞赛列表</button>
        <button class="action-button" @click="publishAnother">发布另一个竞赛</button>
      </div>
    </div>

    <div v-else class="form-container">
      <form @submit.prevent="submitCompetition" class="competition-form">
        <div class="form-group">
          <label for="title">竞赛标题 <span class="required">*</span></label>
          <input
            type="text"
            id="title"
            v-model="competition.title"
            required
            :class="{ 'error': submitted && !competition.title }"
          >
          <div v-if="submitted && !competition.title" class="error-message">请输入竞赛标题</div>
        </div>

        <div class="form-group">
          <label for="description">竞赛描述 <span class="required">*</span></label>
          <textarea
            id="description"
            v-model="competition.description"
            rows="5"
            required
            :class="{ 'error': submitted && !competition.description }"
          ></textarea>
          <div v-if="submitted && !competition.description" class="error-message">请输入竞赛描述</div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="startDate">开始日期 <span class="required">*</span></label>
            <input
              type="date"
              id="startDate"
              v-model="competition.startDate"
              required
              :class="{ 'error': submitted && !competition.startDate }"
            >
            <div v-if="submitted && !competition.startDate" class="error-message">请选择开始日期</div>
          </div>

          <div class="form-group">
            <label for="endDate">结束日期 <span class="required">*</span></label>
            <input
              type="date"
              id="endDate"
              v-model="competition.endDate"
              required
              :min="competition.startDate"
              :class="{ 'error': submitted && (!competition.endDate || competition.endDate < competition.startDate) }"
            >
            <div v-if="submitted && !competition.endDate" class="error-message">请选择结束日期</div>
            <div v-if="submitted && competition.endDate < competition.startDate" class="error-message">结束日期不能早于开始日期</div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="registrationDeadline">报名截止日期 <span class="required">*</span></label>
            <input
              type="date"
              id="registrationDeadline"
              v-model="competition.registrationDeadline"
              required
              :max="competition.startDate"
              :class="{ 'error': submitted && (!competition.registrationDeadline || competition.registrationDeadline > competition.startDate) }"
            >
            <div v-if="submitted && !competition.registrationDeadline" class="error-message">请选择报名截止日期</div>
            <div v-if="submitted && competition.registrationDeadline > competition.startDate" class="error-message">报名截止日期不能晚于开始日期</div>
          </div>

          <div class="form-group">
            <label for="maxParticipants">最大参与人数</label>
            <input
              type="number"
              id="maxParticipants"
              v-model="competition.maxParticipants"
              min="1"
              max="1000"
            >
          </div>
        </div>

        <div class="form-group">
          <label for="rewards">奖励说明 <span class="required">*</span></label>
          <textarea
            id="rewards"
            v-model="competition.rewards"
            rows="3"
            required
            :class="{ 'error': submitted && !competition.rewards }"
          ></textarea>
          <div v-if="submitted && !competition.rewards" class="error-message">请输入奖励说明</div>
        </div>

        <div class="form-group">
          <label for="requirements">参赛要求 <span class="required">*</span></label>
          <textarea
            id="requirements"
            v-model="competition.requirements"
            rows="3"
            required
            :class="{ 'error': submitted && !competition.requirements }"
          ></textarea>
          <div v-if="submitted && !competition.requirements" class="error-message">请输入参赛要求</div>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-button" @click="goBack">取消</button>
          <button type="submit" class="submit-button" :disabled="isSubmitting">
            {{ isSubmitting ? '提交中...' : (isEditing ? '更新竞赛' : '发布竞赛') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CompetitionPublish',
  data() {
    return {
      competition: {
        title: '',
        description: '',
        startDate: '',
        endDate: '',
        registrationDeadline: '',
        maxParticipants: 100,
        rewards: '',
        requirements: '',
        status: 'OPEN'
      },
      loading: false,
      submitted: false,
      isSubmitting: false,
      showSuccess: false,
      isEditing: false,
      competitionId: null,
      enterpriseId: null
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');

    // 检查是否是编辑模式
    if (this.$route.query.edit && this.$route.query.competitionId) {
      this.isEditing = true;
      this.competitionId = this.$route.query.competitionId;
      this.fetchCompetitionDetails();
    }
  },
  methods: {
    fetchCompetitionDetails() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/competitions/${this.competitionId}`)
        .then(response => {
          const competition = response.data;

          // 格式化日期为 YYYY-MM-DD 格式，适用于表单输入
          this.competition = {
            ...competition,
            startDate: this.formatDateForInput(competition.startDate),
            endDate: this.formatDateForInput(competition.endDate),
            registrationDeadline: this.formatDateForInput(competition.registrationDeadline)
          };

          this.loading = false;
        })
        .catch(error => {
          console.error('获取竞赛详情失败:', error);
          alert('获取竞赛详情失败，请重试');
          this.loading = false;
          this.goBack();
        });
    },
    formatDateForInput(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toISOString().split('T')[0]; // 返回 YYYY-MM-DD 格式
    },
    submitCompetition() {
      this.submitted = true;

      // 基本字段验证
      if (!this.competition.title || !this.competition.description ||
          !this.competition.startDate || !this.competition.endDate ||
          !this.competition.registrationDeadline || !this.competition.rewards ||
          !this.competition.requirements) {
        return;
      }

      // 日期验证
      if (this.competition.endDate < this.competition.startDate) {
        return;
      }

      if (this.competition.registrationDeadline > this.competition.startDate) {
        return;
      }

      this.isSubmitting = true;

      // 根据日期自动设置状态
      const now = new Date();
      const startDate = new Date(this.competition.startDate);
      const endDate = new Date(this.competition.endDate);

      if (now < startDate) {
        this.competition.status = 'OPEN';
      } else if (now > endDate) {
        this.competition.status = 'CLOSED';
      } else {
        this.competition.status = 'ONGOING';
      }

      if (this.isEditing) {
        // 编辑模式，使用 PUT 请求更新竞赛
        axios.put(`http://localhost:8080/api/competitions/${this.competitionId}`, this.competition)
          .then(response => {
            console.log('竞赛更新成功:', response.data);
            this.showSuccess = true;
          })
          .catch(error => {
            console.error('竞赛更新失败:', error);
            alert('竞赛更新失败，请稍后重试');
          })
          .finally(() => {
            this.isSubmitting = false;
          });
      } else {
        // 创建模式，使用 POST 请求创建竞赛
        // 修复：将enterprise对象设置为null，让后端通过查询参数来设置企业
        const competitionData = {
          ...this.competition,
          enterprise: null
        };
        axios.post(`http://localhost:8080/api/competitions?enterpriseId=${this.enterpriseId}`, competitionData)
          .then(response => {
            console.log('竞赛发布成功:', response.data);
            this.showSuccess = true;
          })
          .catch(error => {
            console.error('竞赛发布失败:', error);
            alert('竞赛发布失败，请稍后重试');
          })
          .finally(() => {
            this.isSubmitting = false;
          });
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    goToCompetitions() {
      this.$router.push({ name: 'EnterpriseCompetitions' });
    },
    publishAnother() {
      this.competition = {
        title: '',
        description: '',
        startDate: '',
        endDate: '',
        registrationDeadline: '',
        maxParticipants: 100,
        rewards: '',
        requirements: '',
        status: 'OPEN'
      };
      this.submitted = false;
      this.showSuccess = false;
      this.isEditing = false;
      this.competitionId = null;
    }
  }
};
</script>

<style scoped>
.competition-publish-container {
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

.competition-form {
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
