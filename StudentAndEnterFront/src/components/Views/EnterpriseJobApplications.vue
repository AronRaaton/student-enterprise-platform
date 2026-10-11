<template>
  <div class="job-applications-container module-page module-list">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <i class="el-icon-arrow-left"></i> 返回
      </button>
      <h1 class="page-title">岗位申请管理</h1>
    </div>

    <div class="content-wrapper">
      <!-- 左侧岗位列表 -->
      <div class="jobs-list">
        <h2 class="section-title">我的岗位</h2>
        <div v-if="loading" class="loading-spinner">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else-if="jobs.length === 0" class="no-data">
          <p>暂无发布的岗位</p>
          <button class="action-btn" @click="createNewJob">发布新岗位</button>
        </div>
        <div v-else class="job-items">
          <div
            v-for="job in jobs"
            :key="job.jobId"
            class="job-item"
            :class="{ active: selectedJobId === job.jobId }"
            @click="selectJob(job.jobId)"
          >
            <div class="job-item-header">
              <h3 class="job-title">{{ job.title }}</h3>
              <span class="application-count" v-if="job.applicationCount > 0">
                {{ job.applicationCount }}个申请
              </span>
            </div>
            <div class="job-item-details">
              <p class="job-tech-stack">
                <span class="label">技术栈:</span> {{ job.techStack || '未指定' }}
              </p>
              <p class="job-salary">
                <span class="label">薪资:</span> {{ formatSalaryRange(job.minSalary, job.maxSalary) }}
              </p>
              <p class="job-date">
                <span class="label">发布时间:</span> {{ formatDate(job.createdTime) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧申请列表 -->
      <div class="applications-panel">
        <div v-if="!selectedJobId" class="no-job-selected">
          <p>请选择左侧的岗位查看申请</p>
        </div>
        <div v-else>
          <div class="panel-header">
            <h2 class="section-title">{{ selectedJob ? selectedJob.title : '' }} - 申请列表</h2>
            <div class="filter-controls">
              <select v-model="statusFilter" class="status-filter">
                <option value="ALL">所有状态</option>
                <option value="PENDING">待审核</option>
                <option value="APPROVED">已通过</option>
                <option value="REJECTED">已拒绝</option>
              </select>
            </div>
          </div>

          <div v-if="loadingApplications" class="loading-spinner">
            <div class="spinner"></div>
            <p>加载申请中...</p>
          </div>
          <div v-else-if="filteredApplications.length === 0" class="no-data">
            <p>暂无{{ statusFilter === 'ALL' ? '' : getStatusText(statusFilter) }}申请</p>
          </div>
          <div v-else class="applications-list">
            <div
              v-for="application in filteredApplications"
              :key="application.applicationId"
              class="application-item"
              :class="{ 'application-pending': application.status === 'PENDING' }"
            >
              <div class="application-header">
                <div class="student-info" @click="viewStudentDetails(application.studentId)">
                  <div class="student-avatar">
                    {{ application.studentName ? application.studentName.charAt(0) : 'S' }}
                  </div>
                  <div class="student-details">
                    <h3 class="student-name">{{ application.studentName }}</h3>
                    <p class="student-school">{{ application.studentSchool }} - {{ application.studentMajor }}</p>
                  </div>
                </div>
                <div class="application-status">
                  <span class="status-badge" :class="getStatusClass(application.status)">
                    {{ getStatusText(application.status) }}
                  </span>
                  <p class="application-date">申请时间: {{ application.formattedApplicationTime }}</p>
                </div>
              </div>

              <!-- 学生技能图谱 -->
              <div class="student-skills" v-if="application.skills && application.skills.length > 0">
                <h4 class="skills-title">技能图谱</h4>
                <div class="skills-chart">
                  <SkillRadarChart :skills="application.skills" />
                </div>
                <button class="view-profile-btn" @click="viewStudentDetails(application.studentId)">查看详情</button>
              </div>

              <!-- 申请操作按钮 -->
              <div class="application-actions" v-if="application.status === 'PENDING'">
                <button class="approve-btn" @click="approveApplication(application.applicationId)">通过申请</button>
                <button class="reject-btn" @click="rejectApplication(application.applicationId)">拒绝申请</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import SkillRadarChart from '@/components/Common/SkillRadarChart.vue';

export default {
  components: {
    SkillRadarChart
  },
  data() {
    return {
      loading: true,
      loadingApplications: false,
      jobs: [],
      applications: [],
      selectedJobId: null,
      selectedJob: null,
      statusFilter: 'ALL',
      enterpriseId: localStorage.getItem('userId')
    };
  },
  computed: {
    filteredApplications() {
      if (this.statusFilter === 'ALL') {
        return this.applications;
      }
      return this.applications.filter(app => app.status === this.statusFilter);
    }
  },
  created() {
    this.fetchJobs();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchJobs() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/jobs/enterprise/${this.enterpriseId}`);
        this.jobs = response.data;

        // 获取每个岗位的申请数量
        for (const job of this.jobs) {
          try {
            const appResponse = await axios.get(`http://localhost:8080/api/job-applications/job/${job.jobId}`);
            job.applicationCount = appResponse.data.length;
          } catch (error) {
            console.error(`获取岗位 ${job.jobId} 的申请数量失败:`, error);
            job.applicationCount = 0;
          }
        }

        // 如果有岗位，默认选择第一个
        if (this.jobs.length > 0) {
          this.selectJob(this.jobs[0].jobId);
        }
      } catch (error) {
        console.error('获取岗位列表失败:', error);
        this.jobs = [];
      } finally {
        this.loading = false;
      }
    },
    async selectJob(jobId) {
      this.selectedJobId = jobId;
      this.selectedJob = this.jobs.find(job => job.jobId === jobId);
      await this.fetchApplications(jobId);
    },
    async fetchApplications(jobId) {
      this.loadingApplications = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/job-applications/job/${jobId}`);
        this.applications = response.data;
      } catch (error) {
        console.error('获取岗位申请失败:', error);
        this.applications = [];
      } finally {
        this.loadingApplications = false;
      }
    },
    async approveApplication(applicationId) {
      try {
        await axios.put(`http://localhost:8080/api/job-applications/${applicationId}/review?status=APPROVED`, {});
        // 更新本地数据
        const index = this.applications.findIndex(app => app.applicationId === applicationId);
        if (index !== -1) {
          this.applications[index].status = 'APPROVED';
          this.applications[index].reviewTime = new Date();
          this.applications[index].formattedReviewTime = this.formatDate(new Date());
        }
        alert('已通过该申请');
      } catch (error) {
        console.error('审核申请失败:', error);
        alert('操作失败，请重试');
      }
    },
    async rejectApplication(applicationId) {
      try {
        await axios.put(`http://localhost:8080/api/job-applications/${applicationId}/review?status=REJECTED`, {});
        // 更新本地数据
        const index = this.applications.findIndex(app => app.applicationId === applicationId);
        if (index !== -1) {
          this.applications[index].status = 'REJECTED';
          this.applications[index].reviewTime = new Date();
          this.applications[index].formattedReviewTime = this.formatDate(new Date());
        }
        alert('已拒绝该申请');
      } catch (error) {
        console.error('审核申请失败:', error);
        alert('操作失败，请重试');
      }
    },
    viewStudentDetails(studentId) {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId }
      });
    },
    createNewJob() {
      this.$router.push({ name: 'CreateJob' });
    },
    getStatusClass(status) {
      switch (status) {
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        case 'PENDING':
        default:
          return 'status-pending';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'APPROVED':
          return '已通过';
        case 'REJECTED':
          return '已拒绝';
        case 'PENDING':
          return '待审核';
        default:
          return status;
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    formatSalaryRange(minSalary, maxSalary) {
      if (!minSalary && !maxSalary) return '面议';

      minSalary = minSalary || 0;
      maxSalary = maxSalary || 0;

      if (minSalary === maxSalary) {
        return this.formatSalary(minSalary);
      }

      return `${this.formatSalary(minSalary)} - ${this.formatSalary(maxSalary)}`;
    },
    formatSalary(salary) {
      if (!salary) return '0';
      return salary >= 1000 ? `${(salary / 1000).toFixed(1)}k` : salary;
    }
  }
};
</script>

<style scoped>
.job-applications-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  margin-right: 15px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.jobs-list, .applications-panel {
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.job-items {
  max-height: 600px;
  overflow-y: auto;
}

.job-item {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.job-item:hover {
  border-color: #C0C4CC;
}

.job-item.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.job-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.job-title {
  font-size: 16px;
  color: #303133;
  margin: 0;
  font-weight: bold;
}

.application-count {
  background-color: #409EFF;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.job-item-details p {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.label {
  color: #909399;
  margin-right: 5px;
}

.no-job-selected, .no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.action-btn {
  margin-top: 15px;
  padding: 8px 15px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-filter {
  padding: 8px 12px;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  background-color: white;
  color: #606266;
}

.applications-list {
  max-height: 600px;
  overflow-y: auto;
}

.application-item {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
}

.application-pending {
  border-left: 4px solid #E6A23C;
}

.application-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.student-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.student-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  margin-right: 15px;
}

.student-name {
  font-size: 16px;
  color: #303133;
  margin: 0 0 5px 0;
}

.student-school {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.application-status {
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 5px;
}

.status-pending {
  background-color: #E6A23C;
  color: white;
}

.status-approved {
  background-color: #67C23A;
  color: white;
}

.status-rejected {
  background-color: #F56C6C;
  color: white;
}

.application-date {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.student-skills {
  margin: 10px 0;
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.skills-title {
  font-size: 14px;
  color: #303133;
  margin: 0 5px 0 0;
  white-space: nowrap;
  min-width: 70px;
}

.skills-chart {
  height: 150px;
  flex: 1;
  margin-left: -10px;
}

.application-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
}

.approve-btn, .reject-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.approve-btn {
  background-color: #67C23A;
  color: white;
}

.reject-btn {
  background-color: #F56C6C;
  color: white;
}

.view-profile-btn {
  padding: 5px 10px;
  border: 1px solid #409EFF;
  background-color: white;
  color: #409EFF;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  margin-left: auto;
  white-space: nowrap;
  align-self: flex-end;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #409EFF;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
