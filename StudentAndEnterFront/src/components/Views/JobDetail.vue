<template>
  <div class="job-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">岗位详情</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 岗位详情内容 -->
    <div v-else class="job-content">
      <!-- 左侧内容 -->
      <div class="job-main">
        <!-- 岗位标题和基本信息 -->
        <div class="job-header">
          <h1 class="job-title">{{ job.title }}</h1>
          <div class="position-title" v-if="job.positionTitle">
            <span class="position-label">职位名称:</span> {{ job.positionTitle }}
          </div>
          <div class="job-meta">
            <div class="salary-tag">{{ formatSalaryRange(job.minSalary, job.maxSalary) }}</div>
            <div class="location-tag">{{ job.city }}</div>
            <div class="type-tag">{{ job.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</div>
            <div class="education-tag" v-if="job.educationRequirement">{{ job.educationRequirement }}</div>
          </div>
        </div>

        <!-- 岗位详情 -->
        <div class="job-details">
          <div class="detail-section">
            <h2 class="section-title">岗位描述</h2>
            <div class="section-content description-content">
              <p v-if="job.description" v-html="formatDescription(job.description)"></p>
              <p v-else class="no-content">暂无岗位描述</p>
            </div>
          </div>

          <div class="detail-section">
            <h2 class="section-title">岗位要求</h2>
            <div class="section-content requirements-content">
              <p v-if="job.requirements" v-html="formatDescription(job.requirements)"></p>
              <p v-else class="no-content">暂无岗位要求</p>
            </div>
          </div>

          <div class="detail-section" v-if="job.techStack">
            <h2 class="section-title">技术栈要求</h2>
            <div class="section-content tech-stack-content">
              <div class="tech-tags">
                <span class="tech-tag" v-for="(tag, index) in techStackArray" :key="index">{{ tag }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h2 class="section-title">发布时间</h2>
            <div class="section-content">
              <p>{{ formatDate(job.postTime) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息栏 -->
      <div class="job-sidebar">
        <!-- 企业信息卡片 -->
        <div class="company-card">
          <h3 class="card-title">企业信息</h3>
          <div class="company-info" @click="viewEnterpriseDetails(job.enterpriseId)">
            <div class="company-logo">
              <span>{{ job.enterpriseName ? job.enterpriseName.charAt(0) : 'E' }}</span>
            </div>
            <div class="company-details">
              <h3 class="company-name">{{ job.enterpriseName }}</h3>
              <div class="company-meta">
                <span v-if="job.enterpriseIndustry">{{ job.enterpriseIndustry }}</span>
              </div>
            </div>
          </div>

          <div class="company-data">
            <div class="company-data-item">
              <span class="data-label">企业规模:</span>
              <span class="data-value">{{ formatCompanySize(job.enterpriseEmployeeCount) }}</span>
            </div>
            <div class="company-data-item" v-if="job.enterpriseWebsite">
              <span class="data-label">公司网址:</span>
              <a :href="job.enterpriseWebsite" target="_blank" class="data-value website-link">{{ job.enterpriseWebsite }}</a>
            </div>
            <div class="company-data-item" v-if="job.enterpriseLocation">
              <span class="data-label">公司地址:</span>
              <span class="data-value">{{ job.enterpriseLocation }}</span>
            </div>
          </div>
        </div>

        <!-- 操作卡片 -->
        <div class="action-card">
          <h3 class="card-title">申请岗位</h3>
          <p class="card-desc">对该岗位感兴趣？立即申请或联系企业了解更多信息！</p>

          <!-- 学生角色显示申请按钮 -->
          <div v-if="userRole === 'student'">
            <!-- 已申请状态 -->
            <div v-if="hasApplied" class="application-status">
              <div class="status-badge" :class="getApplicationStatusClass(applicationStatus)">
                {{ getApplicationStatusText(applicationStatus) }}
              </div>
              <p class="status-desc">您已申请该岗位，请等待企业回复</p>
            </div>

            <!-- 未申请状态 -->
            <button v-else class="apply-btn" @click="applyForJob">申请岗位</button>
          </div>

          <button class="contact-btn" @click="contactEnterprise">联系企业</button>
          <button class="favorite-btn" @click="toggleFavorite" v-if="userRole === 'student'">
            <span class="favorite-icon">{{ isFavorited ? '★' : '☆' }}</span>
            {{ isFavorited ? '取消收藏' : '收藏岗位' }}
          </button>
        </div>

        <!-- 相似岗位卡片 -->
        <div class="similar-jobs-card">
          <h3 class="card-title">相似岗位推荐</h3>
          <div v-if="similarJobs.length === 0" class="no-similar-jobs">
            <p>暂无相似岗位推荐</p>
          </div>
          <div v-else class="similar-job-list">
            <div v-for="similarJob in similarJobs" :key="similarJob.jobId" class="similar-job-item" @click="viewJobDetails(similarJob.jobId)">
              <div class="similar-job-info">
                <h4 class="similar-job-title">{{ similarJob.title }}</h4>
                <div class="similar-job-company">{{ similarJob.enterpriseName }}</div>
              </div>
              <div class="similar-job-salary">{{ formatSalaryRange(similarJob.minSalary, similarJob.maxSalary) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      job: {},
      loading: true,
      isFavorited: false,
      similarJobs: [],
      userId: localStorage.getItem('userId'),
      userRole: localStorage.getItem('userRole') || '',
      hasApplied: false,
      applicationStatus: null
    };
  },
  computed: {
    jobId() {
      return this.$route.params.jobId;
    },
    techStackArray() {
      if (!this.job || !this.job.techStack) return [];
      return this.job.techStack.split(',').map(tag => tag.trim()).filter(tag => tag);
    }
  },
  created() {
    this.fetchJobDetails();
    this.checkIfFavorited();
    this.checkIfApplied();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchJobDetails() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/jobs/${this.jobId}`);
        this.job = response.data;

        // 获取相似岗位
        this.fetchSimilarJobs();
      } catch (error) {
        console.error('获取岗位详情失败:', error);
        alert('获取岗位详情失败，请重试');
      } finally {
        this.loading = false;
      }
    },
    async fetchSimilarJobs() {
      try {
        // 使用相似岗位的API端点
        const response = await axios.get(`http://localhost:8080/api/jobs/${this.jobId}/similar`);
        this.similarJobs = response.data;
      } catch (error) {
        console.error('获取相似岗位失败:', error);
        this.similarJobs = [];
      }
    },
    async checkIfFavorited() {
      if (this.userRole !== 'student') return;

      try {
        const response = await axios.get(`http://localhost:8080/api/students/${this.userId}/favorites/jobs`);
        this.isFavorited = response.data.some(job => job.jobId === parseInt(this.jobId));
      } catch (error) {
        console.error('检查收藏状态失败:', error);
      }
    },
    async toggleFavorite() {
      if (this.userRole !== 'student') return;

      try {
        if (this.isFavorited) {
          await axios.delete(`http://localhost:8080/api/students/${this.userId}/favorites/jobs/${this.jobId}`);
        } else {
          await axios.post(`http://localhost:8080/api/students/${this.userId}/favorites/jobs/${this.jobId}`);
        }

        this.isFavorited = !this.isFavorited;
      } catch (error) {
        console.error('更新收藏状态失败:', error);
        alert('操作失败，请重试');
      }
    },
    contactEnterprise() {
      if (this.userRole !== 'student') return;

      this.$router.push({
        name: 'ChatS2E',
        params: { enterpriseId: this.job.enterpriseId }
      });
    },

    // 检查是否已申请岗位
    async checkIfApplied() {
      if (this.userRole !== 'student') return;

      try {
        const response = await axios.get(`http://localhost:8080/api/job-applications/check`, {
          params: {
            jobId: this.jobId,
            studentId: this.userId
          }
        });

        this.hasApplied = response.data.hasApplied;

        if (this.hasApplied) {
          // 获取申请状态
          const applications = await axios.get(`http://localhost:8080/api/job-applications/student/${this.userId}`);
          const application = applications.data.find(app => app.jobId === parseInt(this.jobId));
          if (application) {
            this.applicationStatus = application.status;
          }
        }
      } catch (error) {
        console.error('检查申请状态失败:', error);
      }
    },

    // 申请岗位
    async applyForJob() {
      if (this.userRole !== 'student' || this.hasApplied) return;

      try {
        await axios.post(`http://localhost:8080/api/job-applications/${this.jobId}/apply?studentId=${this.userId}`);

        this.hasApplied = true;
        this.applicationStatus = 'PENDING';

        alert('申请成功！请等待企业审核');
      } catch (error) {
        console.error('申请岗位失败:', error);
        alert('申请失败: ' + (error.response?.data?.error || '未知错误'));
      }
    },

    // 获取申请状态的CSS类名
    getApplicationStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        case 'PENDING':
        default:
          return 'status-pending';
      }
    },

    // 获取申请状态的文本
    getApplicationStatusText(status) {
      if (!status) return '待审核';

      switch(status) {
        case 'APPROVED':
          return '已通过';
        case 'REJECTED':
          return '已拒绝';
        case 'PENDING':
        default:
          return '待审核';
      }
    },
    viewEnterpriseDetails(enterpriseId) {
      this.$router.push({
        name: 'EnterpriseDetail',
        params: { enterpriseId }
      });
    },
    viewJobDetails(jobId) {
      this.$router.push({
        name: 'JobDetail',
        params: { jobId }
      });
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

      // 如果薪资大于等于10000，显示为XXk
      if (salary >= 10000) {
        return `${(salary / 1000).toFixed(0)}k`;
      }

      // 否则显示为XXXX元
      return `${salary}元`;
    },
    formatCompanySize(size) {
      if (!size) return '未知规模';

      if (size < 1000) {
        return '1000人以下';
      } else if (size < 5000) {
        return '1000-5000人';
      } else {
        return '5000人以上';
      }
    },
    formatDescription(text) {
      if (!text) return '';

      // 将换行符转换为HTML换行
      return text.replace(/\n/g, '<br>');
    }
  }
};
</script>

<style scoped>
.job-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 15px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #ecf5ff;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  flex-grow: 1;
}

/* 加载中状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 岗位内容布局 */
.job-content {
  display: flex;
  gap: 30px;
}

.job-main {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.job-sidebar {
  width: 300px;
}

/* 岗位标题和基本信息 */
.job-header {
  padding: 30px;
  border-bottom: 1px solid #eaeaea;
}

.job-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.position-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 15px;
}

.position-label {
  font-weight: bold;
  color: #303133;
}

.job-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.salary-tag, .location-tag, .type-tag, .education-tag {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  margin-right: 10px;
  margin-bottom: 10px;
}

.salary-tag {
  background-color: #f0f9eb;
  color: #67c23a;
}

.location-tag {
  background-color: #ecf5ff;
  color: #409EFF;
}

.type-tag {
  background-color: #f4f4f5;
  color: #909399;
}

.education-tag {
  background-color: #f0f9eb;
  color: #67c23a;
}

/* 企业信息 */
.company-info {
  display: flex;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #eaeaea;
  cursor: pointer;
  transition: background-color 0.3s;
}

.company-info:hover {
  background-color: #f5f7fa;
}

.company-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 15px;
}

.company-details {
  flex: 1;
}

.company-name {
  font-size: 18px;
  color: #303133;
  margin: 0 0 5px 0;
}

.company-meta {
  font-size: 14px;
  color: #909399;
}

.view-company {
  color: #909399;
  font-size: 18px;
}

/* 岗位详情 */
.job-details {
  padding: 30px;
}

.detail-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.section-content {
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

.description-content, .requirements-content {
  white-space: pre-line;
}

.tech-stack-content {
  margin-top: 10px;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tech-tag {
  background-color: #f0f2f5;
  color: #1677ff;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  display: inline-block;
}

.no-content {
  color: #909399;
  font-style: italic;
}

/* 右侧操作栏 */
.company-card, .action-card, .similar-jobs-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

/* 企业信息卡片样式 */
.company-data {
  margin-top: 20px;
  border-top: 1px solid #eaeaea;
  padding-top: 15px;
}

.company-data-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}

.data-label {
  color: #909399;
  font-size: 14px;
  margin-right: 10px;
  min-width: 70px;
}

.data-value {
  color: #303133;
  font-size: 14px;
  word-break: break-all;
}

.website-link {
  color: #409EFF;
  text-decoration: none;
}

.website-link:hover {
  text-decoration: underline;
}

.card-title {
  font-size: 18px;
  color: #303133;
  margin: 0 0 15px 0;
}

.card-desc {
  color: #606266;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.5;
}

.contact-btn, .favorite-btn, .apply-btn {
  width: 100%;
  padding: 12px 0;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  margin-bottom: 10px;
}

.apply-btn {
  background-color: #67c23a;
  color: white;
  border: none;
  font-weight: bold;
}

.apply-btn:hover {
  background-color: #5daf34;
}

.application-status {
  margin-bottom: 15px;
}

.status-badge {
  display: inline-block;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 8px;
}

.status-pending {
  background-color: #e6a23c;
  color: white;
}

.status-approved {
  background-color: #67c23a;
  color: white;
}

.status-rejected {
  background-color: #f56c6c;
  color: white;
}

.status-desc {
  font-size: 14px;
  color: #606266;
  margin: 5px 0;
}

.contact-btn {
  background-color: #409EFF;
  color: white;
  border: none;
}

.contact-btn:hover {
  background-color: #66b1ff;
}

.favorite-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-btn:hover {
  background-color: #d9ecff;
}

.favorite-icon {
  margin-right: 5px;
  font-size: 18px;
}

/* 相似岗位 */
.no-similar-jobs {
  color: #909399;
  text-align: center;
  padding: 20px 0;
}

.similar-job-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.similar-job-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-radius: 4px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: background-color 0.3s;
}

.similar-job-item:hover {
  background-color: #ecf5ff;
}

.similar-job-info {
  flex: 1;
}

.similar-job-title {
  font-size: 16px;
  color: #303133;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.similar-job-company {
  font-size: 14px;
  color: #909399;
}

.similar-job-salary {
  font-size: 14px;
  color: #67c23a;
  font-weight: 500;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .job-content {
    flex-direction: column;
  }

  .job-sidebar {
    width: 100%;
  }
}
</style>
