<template>
  <div class="competitions-container">
    <div class="page-header">
      <div class="header-left">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span> 返回
        </button>
        <h1>竞赛列表</h1>
      </div>
      <div class="filter-container">
        <select v-model="statusFilter" class="filter-select">
          <option value="all">全部竞赛</option>
          <option value="OPEN">报名中</option>
          <option value="ONGOING">进行中</option>
          <option value="CLOSED">已结束</option>
        </select>
      </div>
    </div>

    <div class="competitions-list">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="competitions.length === 0" class="empty-state">
        <div class="empty-icon">🏆</div>
        <p>暂无竞赛信息</p>
      </div>

      <div v-else class="competition-cards">
        <div v-for="competition in filteredCompetitions" :key="competition.competitionId" class="competition-card">
          <div class="competition-header">
            <h2 class="competition-title">{{ competition.title }}</h2>
            <span class="competition-status" :class="getStatusClass(competition.status)">
              {{ getStatusText(competition.status) }}
            </span>
          </div>

          <div class="competition-info">
            <p class="competition-enterprise">
              <span class="info-label">主办方:</span> {{ competition.enterprise.name }}
            </p>
            <p class="competition-dates">
              <span class="info-label">时间:</span> {{ formatDate(competition.startDate) }} - {{ formatDate(competition.endDate) }}
            </p>
            <p class="competition-deadline">
              <span class="info-label">报名截止:</span> {{ formatDate(competition.registrationDeadline) }}
            </p>
          </div>

          <div class="competition-description">
            {{ truncateText(competition.description, 150) }}
          </div>

          <div class="competition-actions">
            <button class="view-details-btn" @click="viewCompetitionDetails(competition.competitionId)">查看详情</button>
            <button
              v-if="competition.status === 'OPEN'"
              class="register-btn"
              @click="registerForCompetition(competition.competitionId)"
              :disabled="isRegistered(competition.competitionId)"
            >
              {{ isRegistered(competition.competitionId) ? '已报名' : '立即报名' }}
            </button>
            <button
              v-if="isRegistered(competition.competitionId)"
              class="submit-btn"
              @click="viewCompetitionDetails(competition.competitionId)"
            >
              提交作品
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
  name: 'StudentCompetitions',
  data() {
    return {
      competitions: [],
      registeredCompetitions: [],
      loading: true,
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      statusFilter: 'all',
      studentId: null
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
    this.studentId = localStorage.getItem('userId');
    this.fetchCompetitions();
    this.fetchRegisteredCompetitions();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    fetchCompetitions() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/competitions?page=${this.currentPage}&size=${this.pageSize}`)
        .then(response => {
          this.competitions = response.data.content;
          this.totalPages = response.data.totalPages;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取竞赛列表失败:', error);
          this.loading = false;
        });
    },
    fetchRegisteredCompetitions() {
      if (!this.studentId) return;

      axios.get(`http://localhost:8080/api/competitions/student/${this.studentId}`)
        .then(response => {
          this.registeredCompetitions = response.data;
        })
        .catch(error => {
          console.error('获取已报名竞赛失败:', error);
        });
    },
    changePage(page) {
      this.currentPage = page;
      this.fetchCompetitions();
    },
    viewCompetitionDetails(competitionId) {
      this.$router.push({ name: 'CompetitionDetail', params: { competitionId } });
    },
    registerForCompetition(competitionId) {
      if (!this.studentId) {
        alert('请先登录');
        return;
      }

      if (this.isRegistered(competitionId)) {
        alert('您已经报名过该竞赛');
        return;
      }

      axios.post(`http://localhost:8080/api/competitions/${competitionId}/register?studentId=${this.studentId}`)
        .then(response => {
          alert('报名成功');
          this.fetchRegisteredCompetitions();
        })
        .catch(error => {
          alert('报名失败: ' + (error.response?.data || '未知错误'));
        });
    },
    isRegistered(competitionId) {
      return this.registeredCompetitions.some(comp => comp.competitionId === competitionId);
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    getStatusText(status) {
      switch (status) {
        case 'OPEN': return '报名中';
        case 'ONGOING': return '进行中';
        case 'CLOSED': return '已结束';
        case 'CANCELLED': return '已取消';
        default: return '未知状态';
      }
    },
    getStatusClass(status) {
      switch (status) {
        case 'OPEN': return 'status-open';
        case 'ONGOING': return 'status-ongoing';
        case 'CLOSED': return 'status-closed';
        case 'CANCELLED': return 'status-cancelled';
        default: return '';
      }
    }
  }
};
</script>

<style scoped>
.competitions-container {
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

.competition-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.competition-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s;
}

.competition-card:hover {
  transform: translateY(-5px);
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.competition-title {
  font-size: 18px;
  margin: 0;
  flex: 1;
}

.competition-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.status-open {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-ongoing {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
}

.competition-info {
  margin-bottom: 12px;
}

.info-label {
  font-weight: bold;
  color: #606266;
}

.competition-description {
  margin-bottom: 16px;
  color: #606266;
  flex-grow: 1;
}

.competition-actions {
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

.submit-btn {
  background-color: #67c23a;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #85ce61;
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
