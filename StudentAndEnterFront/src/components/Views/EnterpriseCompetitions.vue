<template>
  <div class="competitions-container module-page module-list">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>企业竞赛管理</h1>
      </div>
      <div class="header-right">
        <button class="publish-button" @click="publishCompetition">发布新竞赛</button>
      </div>
    </div>

    <div class="filter-section">
      <select v-model="statusFilter" class="filter-select">
        <option value="all">全部竞赛</option>
        <option value="OPEN">报名中</option>
        <option value="ONGOING">进行中</option>
        <option value="CLOSED">已结束</option>
      </select>
    </div>

    <div class="competitions-list">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="competitions.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <p>暂无竞赛信息</p>
        <button class="create-button" @click="publishCompetition">创建第一个竞赛</button>
      </div>

      <div v-else class="competition-cards">
        <div v-for="competition in filteredCompetitions" :key="competition.competitionId" class="competition-card">
          <div class="competition-header">
            <h2 class="competition-title">{{ competition.title }}</h2>
            <span class="competition-status" :class="getStatusClass(competition.status)">
              {{ getStatusText(competition.status) }}
            </span>
          </div>

          <div class="competition-dates">
            <div class="date-item">
              <span class="date-label">开始日期:</span>
              <span class="date-value">{{ formatDate(competition.startDate) }}</span>
            </div>
            <div class="date-item">
              <span class="date-label">结束日期:</span>
              <span class="date-value">{{ formatDate(competition.endDate) }}</span>
            </div>
            <div class="date-item">
              <span class="date-label">报名截止:</span>
              <span class="date-value">{{ formatDate(competition.registrationDeadline) }}</span>
            </div>
          </div>

          <div class="competition-description">
            <p>{{ truncateText(competition.description, 100) }}</p>
          </div>

          <div class="competition-stats">
            <div class="stat-item">
              <span class="stat-label">已报名:</span>
              <span class="stat-value">{{ competition.registrations ? competition.registrations.length : 0 }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">最大人数:</span>
              <span class="stat-value">{{ competition.maxParticipants }}</span>
            </div>
          </div>

          <div class="competition-actions">
            <button class="action-button view-button" @click="viewCompetitionDetails(competition.competitionId)">
              查看详情
            </button>
            <button class="action-button edit-button" @click="editCompetition(competition.competitionId)">
              编辑
            </button>
            <button class="action-button delete-button" @click="confirmDelete(competition)">
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
        <p>您确定要删除竞赛 "{{ competitionToDelete.title }}" 吗？此操作不可撤销。</p>
        <div class="dialog-buttons">
          <button class="cancel-button" @click="showDeleteConfirm = false">取消</button>
          <button class="confirm-button" @click="deleteCompetition">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'EnterpriseCompetitions',
  data() {
    return {
      competitions: [],
      loading: true,
      enterpriseId: null,
      statusFilter: 'all',
      showDeleteConfirm: false,
      competitionToDelete: null
    };
  },
  computed: {
    filteredCompetitions() {
      if (this.statusFilter === 'all') {
        return this.competitions;
      }
      return this.competitions.filter(competition => competition.status === this.statusFilter);
    }
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchCompetitions();
  },
  methods: {
    fetchCompetitions() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/competitions/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.competitions = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取竞赛列表失败:', error);
          this.loading = false;
        });
    },
    goBack() {
      this.$router.go(-1);
    },
    publishCompetition() {
      this.$router.push({ name: 'CompetitionPublish' });
    },
    viewCompetitionDetails(competitionId) {
      this.$router.push({ name: 'CompetitionDetail', params: { competitionId } });
    },
    editCompetition(competitionId) {
      this.$router.push({
        name: 'CompetitionPublish',
        query: { edit: true, competitionId }
      });
    },
    confirmDelete(competition) {
      this.competitionToDelete = competition;
      this.showDeleteConfirm = true;
    },
    deleteCompetition() {
      if (!this.competitionToDelete) return;

      axios.delete(`http://localhost:8080/api/competitions/${this.competitionToDelete.competitionId}`)
        .then(() => {
          this.competitions = this.competitions.filter(
            comp => comp.competitionId !== this.competitionToDelete.competitionId
          );
          this.showDeleteConfirm = false;
          this.competitionToDelete = null;
        })
        .catch(error => {
          console.error('删除竞赛失败:', error);
          alert('删除竞赛失败，请稍后重试');
        });
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      if (text.length <= maxLength) return text;
      return text.substring(0, maxLength) + '...';
    },
    getStatusText(status) {
      const statusMap = {
        'OPEN': '报名中',
        'ONGOING': '进行中',
        'CLOSED': '已结束',
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
.competitions-container {
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

.competition-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.competition-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 20px;
  background-color: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.competition-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.competition-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  flex: 1;
}

.competition-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  white-space: nowrap;
}

.status-open {
  background-color: #e1f3d8;
  color: #67c23a;
}

.status-ongoing {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
}

.competition-dates {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 15px;
  font-size: 14px;
}

.date-item {
  display: flex;
  align-items: center;
}

.date-label {
  color: #909399;
  margin-right: 5px;
}

.date-value {
  color: #606266;
  font-weight: 500;
}

.competition-description {
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.competition-stats {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  font-size: 14px;
}

.stat-label {
  color: #909399;
  margin-right: 5px;
}

.stat-value {
  color: #606266;
  font-weight: 500;
}

.competition-actions {
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
