<template>
  <div class="job-management-container module-page module-list">
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">岗位管理</h1>
    </div>

    <div class="action-bar">
      <button class="add-job-btn" @click="showAddJobModal">发布新岗位</button>
      <button class="view-applications-btn" @click="viewJobApplications">查看岗位申请</button>
    </div>

    <div class="job-list-container">
      <div class="job-tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'all' }]"
          @click="activeTab = 'all'">
          所有岗位
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'open' }]"
          @click="activeTab = 'open'">
          开放中
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'closed' }]"
          @click="activeTab = 'closed'">
          已关闭
        </button>
      </div>

      <div class="job-list">
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <div v-else-if="filteredJobs.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <p>暂无岗位信息</p>
          <button class="add-job-btn" @click="showAddJobModal">发布新岗位</button>
        </div>

        <div v-else>
          <div v-for="job in filteredJobs" :key="job.jobId" class="job-card">
            <div class="job-header">
              <h2 class="job-title">{{ job.title }}</h2>
              <div class="job-status" :class="job.status === 'OPEN' ? 'status-open' : 'status-closed'">
                {{ job.status === 'OPEN' ? '开放中' : '已关闭' }}
              </div>
            </div>

            <div class="job-info">
              <div class="info-item">
                <span class="info-label">城市:</span>
                <span class="info-value">{{ job.city }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">薪资范围:</span>
                <span class="info-value">{{ formatSalaryRange(job.minSalary, job.maxSalary) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">类型:</span>
                <span class="info-value">{{ job.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">发布时间:</span>
                <span class="info-value">{{ job.formattedPostTime }}</span>
              </div>
            </div>

            <div class="job-description">
              <p>{{ truncateText(job.description, 150) }}</p>
            </div>

            <div class="job-tech-stack" v-if="job.techStack">
              <div class="tech-tags small">
                <span class="tech-tag" v-for="(tag, index) in getTechStackArray(job.techStack).slice(0, 5)" :key="index">{{ tag }}</span>
                <span class="tech-tag more" v-if="getTechStackArray(job.techStack).length > 5">+{{ getTechStackArray(job.techStack).length - 5 }}</span>
              </div>
            </div>

            <div class="job-actions">
              <button class="action-btn view-btn" @click="viewJobDetails(job)">查看详情</button>
              <button class="action-btn edit-btn" @click="editJob(job)">编辑</button>
              <button
                v-if="job.status === 'OPEN'"
                class="action-btn close-btn"
                @click="updateJobStatus(job.jobId, 'CLOSED')">
                关闭招聘
              </button>
              <button
                v-else
                class="action-btn reopen-btn"
                @click="updateJobStatus(job.jobId, 'OPEN')">
                重新开放
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑岗位的模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑岗位' : '发布新岗位' }}</h2>
          <button class="close-modal-btn" @click="closeModal">&times;</button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="submitJobForm">
            <div class="form-group">
              <label for="title">岗位标题 <span class="required">*</span></label>
              <input
                id="title"
                v-model="jobForm.title"
                type="text"
                required
                placeholder="例如：Java开发工程师"
              />
            </div>

            <div class="form-group">
              <label for="positionTitle">职位名称 <span class="required">*</span></label>
              <input
                id="positionTitle"
                v-model="jobForm.positionTitle"
                type="text"
                required
                placeholder="例如：高级Java开发工程师"
              />
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label for="city">工作城市 <span class="required">*</span></label>
                <input
                  id="city"
                  v-model="jobForm.city"
                  type="text"
                  required
                  placeholder="例如：北京"
                />
              </div>

              <div class="form-group half">
                <label for="minSalary">最低薪资 (元/月) <span class="required">*</span></label>
                <input
                  id="minSalary"
                  v-model="jobForm.minSalary"
                  type="number"
                  min="0"
                  required
                  placeholder="例如：8000"
                />
              </div>

              <div class="form-group half">
                <label for="maxSalary">最高薪资 (元/月) <span class="required">*</span></label>
                <input
                  id="maxSalary"
                  v-model="jobForm.maxSalary"
                  type="number"
                  min="0"
                  required
                  placeholder="例如：15000"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="jobType">岗位类型 <span class="required">*</span></label>
              <select id="jobType" v-model="jobForm.jobType" required>
                <option value="INTERNSHIP">实习</option>
                <option value="FULL_TIME">全职</option>
              </select>
            </div>

            <div class="form-group">
              <label for="educationRequirement">学历要求 <span class="required">*</span></label>
              <select id="educationRequirement" v-model="jobForm.educationRequirement" required>
                <option value="高中及以上">高中及以上</option>
                <option value="大专及以上">大专及以上</option>
                <option value="本科及以上">本科及以上</option>
                <option value="硕士及以上">硕士及以上</option>
                <option value="博士及以上">博士及以上</option>
              </select>
            </div>

            <div class="form-group">
              <label for="description">岗位描述 <span class="required">*</span></label>
              <textarea
                id="description"
                v-model="jobForm.description"
                rows="5"
                required
                placeholder="请详细描述岗位职责和工作内容"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="requirements">岗位要求 <span class="required">*</span></label>
              <textarea
                id="requirements"
                v-model="jobForm.requirements"
                rows="5"
                required
                placeholder="请详细描述岗位要求和技能需求"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="techStack">技术栈要求 <span class="required">*</span></label>
              <TechStackSelector
                v-model="jobForm.techStack"
                @update:modelValue="value => jobForm.techStack = value"
              />
              <div class="form-tip">点击“+”添加技术栈，可选择常用技术或自定义输入</div>
            </div>

            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeModal">取消</button>
              <button type="submit" class="submit-btn" :disabled="submitting">
                {{ submitting ? '提交中...' : (isEditing ? '保存修改' : '发布岗位') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 岗位详情模态框 -->
    <div v-if="showDetailsModal" class="modal-overlay" @click="closeDetailsModal">
      <div class="modal-content details-modal" @click.stop>
        <div class="modal-header">
          <h2>岗位详情</h2>
          <button class="close-modal-btn" @click="closeDetailsModal">&times;</button>
        </div>

        <div class="modal-body">
          <div v-if="selectedJob" class="job-details">
            <h1 class="job-title">{{ selectedJob.title }}</h1>

            <div class="job-meta">
              <div class="meta-item">
                <span class="meta-label">企业:</span>
                <span class="meta-value">{{ selectedJob.enterpriseName }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">城市:</span>
                <span class="meta-value">{{ selectedJob.city }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">薪资范围:</span>
                <span class="meta-value">{{ formatSalaryRange(selectedJob.minSalary, selectedJob.maxSalary) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">类型:</span>
                <span class="meta-value">{{ selectedJob.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">状态:</span>
                <span class="meta-value" :class="selectedJob.status === 'OPEN' ? 'status-open' : 'status-closed'">
                  {{ selectedJob.status === 'OPEN' ? '开放中' : '已关闭' }}
                </span>
              </div>
              <div class="meta-item">
                <span class="meta-label">发布时间:</span>
                <span class="meta-value">{{ selectedJob.formattedPostTime }}</span>
              </div>
            </div>

            <div class="details-section">
              <h3>岗位描述</h3>
              <div class="section-content">
                <p>{{ selectedJob.description }}</p>
              </div>
            </div>

            <div class="details-section">
              <h3>岗位要求</h3>
              <div class="section-content">
                <p>{{ selectedJob.requirements }}</p>
              </div>
            </div>

            <div class="details-section" v-if="selectedJob.techStack">
              <h3>技术栈要求</h3>
              <div class="section-content">
                <div class="tech-tags">
                  <span class="tech-tag" v-for="(tag, index) in getTechStackArray(selectedJob.techStack)" :key="index">{{ tag }}</span>
                </div>
              </div>
            </div>

            <div class="details-actions">
              <button class="action-btn edit-btn" @click="editFromDetails(selectedJob)">编辑</button>
              <button
                v-if="selectedJob.status === 'OPEN'"
                class="action-btn close-btn"
                @click="updateStatusFromDetails(selectedJob.jobId, 'CLOSED')">
                关闭招聘
              </button>
              <button
                v-else
                class="action-btn reopen-btn"
                @click="updateStatusFromDetails(selectedJob.jobId, 'OPEN')">
                重新开放
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import TechStackSelector from '@/components/Common/TechStackSelector.vue';

export default {
  components: {
    TechStackSelector
  },
  data() {
    return {
      enterpriseId: null,
      jobs: [],
      loading: true,
      activeTab: 'all',
      showModal: false,
      showDetailsModal: false,
      isEditing: false,
      submitting: false,
      selectedJob: null,
      jobForm: {
        title: '',
        city: '',
        salary: '',
        jobType: 'FULL_TIME',
        description: '',
        requirements: '',
        techStack: ''
      }
    };
  },

  computed: {
    filteredJobs() {
      if (this.activeTab === 'all') {
        return this.jobs;
      } else if (this.activeTab === 'open') {
        return this.jobs.filter(job => job.status === 'OPEN');
      } else {
        return this.jobs.filter(job => job.status === 'CLOSED');
      }
    }
  },

  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchJobs();
  },

  methods: {
    // 获取企业的所有岗位
    fetchJobs() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/jobs/enterprise/${this.enterpriseId}`)
        .then(response => {
          this.jobs = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取岗位失败:', error);
          this.loading = false;
          alert('获取岗位信息失败，请重试');
        });
    },

    // 显示添加岗位模态框
    showAddJobModal() {
      this.isEditing = false;
      this.resetJobForm();
      this.showModal = true;
    },

    // 显示编辑岗位模态框
    editJob(job) {
      this.isEditing = true;
      this.jobForm = {
        jobId: job.jobId,
        title: job.title,
        positionTitle: job.positionTitle || job.title,
        city: job.city,
        minSalary: job.minSalary || 0,
        maxSalary: job.maxSalary || 0,
        jobType: job.jobType,
        educationRequirement: job.educationRequirement || '本科及以上',
        description: job.description,
        requirements: job.requirements,
        techStack: job.techStack || ''
      };
      this.showModal = true;
    },

    // 从详情页编辑
    editFromDetails(job) {
      this.closeDetailsModal();
      this.editJob(job);
    },

    // 关闭模态框
    closeModal() {
      this.showModal = false;
      this.resetJobForm();
    },

    // 重置表单
    resetJobForm() {
      this.jobForm = {
        title: '',
        positionTitle: '',
        city: '',
        minSalary: 0,
        maxSalary: 0,
        jobType: 'FULL_TIME',
        educationRequirement: '本科及以上',
        description: '',
        requirements: '',
        techStack: ''
      };
    },

    // 提交岗位表单
    submitJobForm() {
      this.submitting = true;

      // 准备提交的数据
      const jobData = {
        ...this.jobForm,
        enterpriseId: this.enterpriseId
      };

      // 根据是否编辑模式选择API
      const apiCall = this.isEditing
        ? axios.put(`http://localhost:8080/api/jobs/${this.jobForm.jobId}`, jobData)
        : axios.post('http://localhost:8080/api/jobs', jobData);

      apiCall
        .then(response => {
          this.submitting = false;
          this.closeModal();
          this.fetchJobs();
          alert(this.isEditing ? '岗位更新成功' : '岗位发布成功');
        })
        .catch(error => {
          console.error('提交岗位失败:', error);
          this.submitting = false;
          alert(`提交失败: ${error.response?.data?.error || '未知错误'}`);
        });
    },

    // 更新岗位状态
    updateJobStatus(jobId, status) {
      if (!confirm(`确定要${status === 'OPEN' ? '重新开放' : '关闭'}这个岗位吗？`)) {
        return;
      }

      axios.put(`http://localhost:8080/api/jobs/${jobId}/status?status=${status}`)
        .then(response => {
          this.fetchJobs();
          alert(status === 'OPEN' ? '岗位已重新开放' : '岗位已关闭');
        })
        .catch(error => {
          console.error('更新岗位状态失败:', error);
          alert(`更新状态失败: ${error.response?.data?.error || '未知错误'}`);
        });
    },

    // 从详情页更新状态
    updateStatusFromDetails(jobId, status) {
      this.closeDetailsModal();
      this.updateJobStatus(jobId, status);
    },

    // 查看岗位详情
    viewJobDetails(job) {
      this.selectedJob = job;
      this.showDetailsModal = true;
    },

    // 关闭详情模态框
    closeDetailsModal() {
      this.showDetailsModal = false;
      this.selectedJob = null;
    },

    // 截断文本
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    // 格式化薪资范围
    formatSalaryRange(minSalary, maxSalary) {
      if (!minSalary && !maxSalary) return '面议';

      minSalary = minSalary || 0;
      maxSalary = maxSalary || 0;

      if (minSalary === maxSalary) {
        return this.formatSalary(minSalary);
      }

      return `${this.formatSalary(minSalary)} - ${this.formatSalary(maxSalary)}`;
    },

    // 格式化薪资
    formatSalary(salary) {
      if (!salary) return '0';

      // 如果薪资大于等于10000，显示为XXk
      if (salary >= 10000) {
        return `${(salary / 1000).toFixed(0)}k`;
      }

      // 否则显示为XXXX元
      return `${salary}元`;
    },

    // 将技术栈字符串转换为数组
    getTechStackArray(techStack) {
      if (!techStack) return [];
      return techStack.split(',').map(tag => tag.trim()).filter(tag => tag);
    },

    // 返回上一页
    goBack() {
      this.$router.go(-1);
    },

    // 查看岗位申请
    viewJobApplications() {
      this.$router.push({ name: 'enterprise-job-applications' });
    }
  }
};
</script>

<style scoped>
.job-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  background-color: #f4f4f5;
  color: #606266;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  margin-right: 15px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #e9e9eb;
}

.back-icon {
  margin-right: 5px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.add-job-btn, .view-applications-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
  margin-right: 10px;
}

.view-applications-btn {
  background-color: #67c23a;
}

.add-job-btn:hover {
  background-color: #66b1ff;
}

.view-applications-btn:hover {
  background-color: #5daf34;
}

.job-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  position: relative;
}

.tab-btn.active {
  color: #409EFF;
  font-weight: 500;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #409EFF;
}

.job-card {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.job-title {
  font-size: 18px;
  margin: 0;
  color: #303133;
}

.job-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-open {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.job-info {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.info-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.info-label {
  color: #909399;
  margin-right: 5px;
}

.info-value {
  color: #606266;
}

.job-description {
  margin-bottom: 15px;
  color: #606266;
  line-height: 1.5;
}

.job-actions {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-left: 10px;
  transition: background-color 0.3s;
}

.view-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.view-btn:hover {
  background-color: #d9ecff;
}

.edit-btn {
  background-color: #f4f4f5;
  color: #909399;
}

.edit-btn:hover {
  background-color: #e9e9eb;
}

.close-btn {
  background-color: #fef0f0;
  color: #f56c6c;
}

.close-btn:hover {
  background-color: #fde2e2;
}

.reopen-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.reopen-btn:hover {
  background-color: #e1f3d8;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s ease-in-out infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 模态框样式 */
.modal-overlay {
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

.modal-content {
  background-color: white;
  border-radius: 4px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.details-modal {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.close-modal-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #909399;
}

.modal-body {
  padding: 20px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group.half {
  flex: 1;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
}

.required {
  color: #f56c6c;
}

input, select, textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #409EFF;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.cancel-btn {
  background-color: #f4f4f5;
  color: #909399;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 岗位详情样式 */
.job-details .job-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #303133;
}

.job-meta {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 30px;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.meta-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.meta-label {
  color: #909399;
  margin-right: 5px;
  font-weight: 500;
}

.meta-value {
  color: #606266;
}

.details-section {
  margin-bottom: 30px;
}

.details-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.tech-tags.small {
  gap: 6px;
  margin-top: 5px;
  margin-bottom: 10px;
}

.tech-tag {
  background-color: #f0f2f5;
  color: #1677ff;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  display: inline-block;
}

.tech-tags.small .tech-tag {
  padding: 4px 8px;
  font-size: 12px;
}

.tech-tag.more {
  background-color: #e6f7ff;
  color: #1677ff;
}

.details-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}
</style>
