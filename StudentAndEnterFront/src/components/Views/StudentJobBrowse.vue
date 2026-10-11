<template>
  <div class="job-browse-container">
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">招聘信息</h1>
    </div>

    <div class="search-filter-container">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索职位、技能或公司..."
          @keyup.enter="searchJobs"
        />
        <button class="search-btn" @click="searchJobs">
          <i class="search-icon">🔍</i>
        </button>
      </div>

      <div class="filters">
        <div class="filter-group">
          <label for="cityFilter">城市</label>
          <select id="cityFilter" v-model="cityFilter" @change="applyFilters">
            <option value="">全部城市</option>
            <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="jobTypeFilter">岗位类型</label>
          <select id="jobTypeFilter" v-model="jobTypeFilter" @change="applyFilters">
            <option value="">全部类型</option>
            <option value="FULL_TIME">全职</option>
            <option value="INTERNSHIP">实习</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="salaryFilter">薪资范围</label>
          <select id="salaryFilter" v-model="salaryFilter" @change="applyFilters">
            <option value="">全部薪资</option>
            <option value="0-5000">5k以下</option>
            <option value="5000-10000">5k-10k</option>
            <option value="10000-15000">10k-15k</option>
            <option value="15000-20000">15k-20k</option>
            <option value="20000-999999">20k以上</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="companySizeFilter">企业规模</label>
          <select id="companySizeFilter" v-model="companySizeFilter" @change="applyFilters">
            <option value="">全部规模</option>
            <option value="small">1000人以下</option>
            <option value="medium">1000-5000人</option>
            <option value="large">5000人以上</option>
          </select>
        </div>

        <button class="reset-btn" @click="resetFilters">重置筛选</button>
      </div>
    </div>

    <!-- 推荐岗位部分 -->
    <div class="recommended-jobs-section" v-if="recommendedJobs.length > 0">
      <h2 class="section-title">推荐岗位</h2>
      <p class="section-description">根据您的技能图谱推荐的岗位</p>

      <div class="recommended-jobs-list">
        <div v-for="job in recommendedJobs" :key="job.jobId" class="job-card recommended" @click="viewJobDetails(job)">
          <div class="job-header">
            <h2 class="job-title">{{ job.title }}</h2>
            <div class="job-salary">{{ formatSalaryRange(job.minSalary, job.maxSalary) }}</div>
          </div>

          <div class="job-company">{{ job.enterpriseName }}</div>

          <div class="job-tags">
            <span class="job-tag location">{{ job.city }}</span>
            <span class="job-tag type">{{ job.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</span>
            <span class="job-tag recommended-tag">推荐</span>
          </div>

          <div v-if="job.techStack" class="tech-stack-tags">
            <span v-for="(tag, index) in getTechStackArray(job.techStack)" :key="index" class="tech-tag">{{ tag }}</span>
          </div>

          <div class="job-description">
            <p>{{ truncateText(job.description, 100) }}</p>
          </div>

          <div class="job-footer">
            <span class="post-time">发布于 {{ job.formattedPostTime }}</span>
            <button class="view-details-btn" @click.stop="viewJobDetails(job)">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <div class="job-list-container">
      <h2 class="section-title">全部岗位</h2>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="jobs.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无符合条件的岗位信息</p>
        <button class="reset-btn" @click="resetFilters">重置筛选</button>
      </div>

      <div v-else class="job-list">
        <div v-for="job in jobs" :key="job.jobId" class="job-card" @click="viewJobDetails(job)">
          <div class="job-header">
            <h2 class="job-title">{{ job.title }}</h2>
            <div class="job-salary">{{ formatSalaryRange(job.minSalary, job.maxSalary) }}</div>
          </div>

          <div class="job-company">{{ job.enterpriseName }}</div>

          <div class="job-tags">
            <span class="job-tag location">{{ job.city }}</span>
            <span class="job-tag type">{{ job.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</span>
          </div>

          <div v-if="job.techStack" class="tech-stack-tags">
            <span v-for="(tag, index) in getTechStackArray(job.techStack)" :key="index" class="tech-tag">{{ tag }}</span>
          </div>

          <div class="job-description">
            <p>{{ truncateText(job.description, 100) }}</p>
          </div>

          <div class="job-footer">
            <span class="post-time">发布于 {{ job.formattedPostTime }}</span>
            <button class="view-details-btn" @click.stop="viewJobDetails(job)">查看详情</button>
          </div>
        </div>
      </div>

      <div v-if="jobs.length > 0 && !loading" class="pagination">
        <button
          class="page-btn prev"
          :disabled="currentPage === 0"
          @click="changePage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页</span>
        <button
          class="page-btn next"
          :disabled="currentPage >= totalPages - 1"
          @click="changePage(currentPage + 1)"
        >
          下一页
        </button>
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
            <div class="job-salary-detail">{{ formatSalaryRange(selectedJob.minSalary, selectedJob.maxSalary) }}</div>

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
                <span class="meta-label">类型:</span>
                <span class="meta-value">{{ selectedJob.jobType === 'INTERNSHIP' ? '实习' : '全职' }}</span>
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
                <div class="tech-stack-tags modal-tech-tags">
                  <span v-for="(tag, index) in getTechStackArray(selectedJob.techStack)" :key="index" class="tech-tag">{{ tag }}</span>
                </div>
              </div>
            </div>

            <div class="details-actions">
              <button class="contact-btn" @click="contactEnterprise(selectedJob.enterpriseId)">
                联系企业
              </button>
              <button class="view-enterprise-btn" @click="viewEnterpriseProfile(selectedJob.enterpriseId)">
                查看企业主页
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

export default {
  data() {
    return {
      jobs: [],
      loading: true,
      searchKeyword: '',
      cityFilter: '',
      jobTypeFilter: '',
      salaryFilter: '',
      companySizeFilter: '',
      cities: [],
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      showDetailsModal: false,
      selectedJob: null,
      studentId: null,
      recommendedJobs: [],
      skills: [] // 学生技能数据
    };
  },

  created() {
    this.studentId = localStorage.getItem('userId');
    this.fetchJobs();
    this.fetchCities();
    this.fetchStudentSkills();
  },

  methods: {
    // 获取所有开放岗位
    fetchJobs() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/jobs?page=${this.currentPage}&size=${this.pageSize}`)
        .then(response => {
          this.jobs = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取岗位失败:', error);
          this.loading = false;
          alert('获取岗位信息失败，请重试');
        });
    },

    // 获取所有城市（用于筛选）
    fetchCities() {
      // 这里可以从后端获取所有城市，或者使用一个预定义的城市列表
      // 为了简单起见，这里使用一个预定义的列表
      this.cities = ['北京', '上海', '广州', '深圳', '杭州', '南京', '成都', '武汉', '西安', '重庆'];
    },

    // 获取学生技能数据
    fetchStudentSkills() {
      if (!this.studentId) {
        console.warn('没有学生ID，无法获取技能数据');
        return;
      }

      // 获取学生已完成的任务，用于生成技能数据
      axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks`)
        .then(response => {
          const completedTasks = response.data || [];
          console.log('已完成的任务:', completedTasks);

          // 生成技能数据
          this.generateSkillsData(completedTasks);

          // 获取技能数据后获取推荐岗位
          this.fetchRecommendedJobs();
        })
        .catch(error => {
          console.error('获取已完成任务失败:', error);
          this.skills = [];
        });
    },

    // 根据已完成任务生成技能数据
    generateSkillsData(completedTasks) {
      // 安全检查
      if (!completedTasks || !Array.isArray(completedTasks) || completedTasks.length === 0) {
        console.log('没有已完成的任务，无法生成技能数据');
        this.skills = [];
        return;
      }

      try {
        // 创建技能映射，用于计算每个技能的平均评分
        const skillMap = {};

        // 遍历所有已完成的任务
        completedTasks.forEach(taskSubmission => {
          const task = taskSubmission.task;
          if (!task || !task.techStack) return;

          // 将技术栈字符串拆分为数组
          const techStackArray = task.techStack.split(',').map(tech => tech.trim());

          // 获取任务评分（如果有）
          const rating = taskSubmission.rating || task.difficulty || 3;

          // 更新每个技能的评分
          techStackArray.forEach(tech => {
            if (!tech) return;

            if (!skillMap[tech]) {
              skillMap[tech] = { total: 0, count: 0 };
            }

            skillMap[tech].total += rating;
            skillMap[tech].count += 1;
          });
        });

        // 转换为技能数组格式
        const skillsArray = Object.keys(skillMap).map(tech => {
          // 计算平均评分，并限制在1-5之间
          const avgRating = skillMap[tech].total / skillMap[tech].count;
          const level = Math.min(5, Math.max(1, Math.round(avgRating)));

          return {
            name: tech,
            level: level
          };
        });

        // 按评分降序排序，并限制最多显示8个技能
        this.skills = skillsArray
          .sort((a, b) => b.level - a.level)
          .slice(0, 8);

        console.log('生成的技能数据', this.skills);
      } catch (error) {
        console.error('生成技能数组时出错:', error);
        this.skills = [];
      }
    },

    // 获取推荐岗位
    fetchRecommendedJobs() {
      if (!this.studentId || this.skills.length === 0) {
        console.warn('没有学生ID或技能数据，无法获取推荐岗位');
        return;
      }

      // 提取学生的技能关键词
      const skillKeywords = this.skills.map(skill => skill.name).join(',');

      // 获取开放的岗位，并根据技能关键词进行筛选
      axios.get('http://localhost:8080/api/jobs', {
        params: { page: 0, size: 20 }
      })
        .then(response => {
          const allJobs = response.data.content || [];

          // 简单的推荐算法：匹配岗位技术栈与学生技能
          this.recommendedJobs = allJobs
            .filter(job => {
              if (!job.techStack) return false;

              // 检查岗位技术栈是否包含学生的任何技能
              return this.skills.some(skill =>
                job.techStack.toLowerCase().includes(skill.name.toLowerCase())
              );
            })
            .slice(0, 3); // 只显示前3个推荐岗位

          // 格式化发布时间
          this.recommendedJobs.forEach(job => {
            job.formattedPostTime = this.formatDate(job.postTime);
          });

          console.log('推荐岗位:', this.recommendedJobs);
        })
        .catch(error => {
          console.error('获取推荐岗位失败:', error);
          this.recommendedJobs = [];
        });
    },

    // 搜索岗位
    searchJobs() {
      if (!this.searchKeyword.trim()) {
        this.fetchJobs();
        return;
      }

      this.loading = true;
      this.currentPage = 0;

      axios.get(`http://localhost:8080/api/jobs/search?keyword=${this.searchKeyword}&page=${this.currentPage}&size=${this.pageSize}`)
        .then(response => {
          this.jobs = response.data.content;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.loading = false;
        })
        .catch(error => {
          console.error('搜索岗位失败:', error);
          this.loading = false;
          alert('搜索失败，请重试');
        });
    },

    // 应用筛选条件
    applyFilters() {
      this.loading = true;
      this.currentPage = 0;

      // 解析薪资范围
      let minSalary = null;
      let maxSalary = null;
      if (this.salaryFilter) {
        const [min, max] = this.salaryFilter.split('-');
        minSalary = parseInt(min);
        maxSalary = parseInt(max);
      }

      axios.get('http://localhost:8080/api/jobs/advanced-filter', {
        params: {
          city: this.cityFilter || null,
          minSalary: minSalary,
          maxSalary: maxSalary,
          companySize: this.companySizeFilter || null,
          jobType: this.jobTypeFilter || null
        }
      })
      .then(response => {
        this.jobs = response.data;
        this.totalPages = 1; // 筛选API不支持分页
        this.totalElements = response.data.length;
        this.loading = false;
      })
      .catch(error => {
        console.error('筛选岗位失败:', error);
        this.loading = false;
        alert('筛选失败，请重试');
      });
    },

    // 重置筛选条件
    resetFilters() {
      this.searchKeyword = '';
      this.cityFilter = '';
      this.jobTypeFilter = '';
      this.salaryFilter = '';
      this.companySizeFilter = '';
      this.currentPage = 0;
      this.fetchJobs();
    },

    // 切换页面
    changePage(page) {
      if (page < 0 || page >= this.totalPages) return;

      this.currentPage = page;

      if (this.searchKeyword) {
        this.searchJobs();
      } else {
        this.fetchJobs();
      }
    },

    // 查看岗位详情
    viewJobDetails(job) {
      // 导航到岗位详情页面
      this.$router.push({ name: 'JobDetail', params: { jobId: job.jobId } });
    },

    // 关闭详情模态框
    closeDetailsModal() {
      this.showDetailsModal = false;
      this.selectedJob = null;
    },

    // 联系企业
    contactEnterprise(enterpriseId) {
      this.closeDetailsModal();
      this.$router.push({ name: 'chat-with-enterprise', params: { enterpriseId } });
    },

    // 查看企业主页
    viewEnterpriseProfile(enterpriseId) {
      this.closeDetailsModal();
      this.$router.push({ name: 'enterprise-profile', params: { enterpriseId } });
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
    }
  }
};
</script>

<style scoped>
.job-browse-container {
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

.search-filter-container {
  margin-bottom: 20px;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.search-box {
  display: flex;
  margin-bottom: 15px;
}

.search-box input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px 0 0 4px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #409EFF;
}

.search-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 0 15px;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
}

.search-icon {
  font-style: normal;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.filter-group {
  margin-right: 20px;
  margin-bottom: 10px;
}

.filter-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #606266;
}

.filter-group select {
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.reset-btn, .advanced-filter-btn {
  background-color: #f4f4f5;
  color: #909399;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-left: 10px;
}

.reset-btn:hover {
  background-color: #e6e8eb;
}

.advanced-filter-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.advanced-filter-btn:hover {
  background-color: #409EFF;
  color: white;
}

.job-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.job-card {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.job-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.15);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.job-title {
  font-size: 18px;
  margin: 0;
  color: #303133;
  flex: 1;
}

.job-salary {
  color: #f56c6c;
  font-weight: 500;
  white-space: nowrap;
  margin-left: 10px;
}

.job-company {
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.job-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
  margin-bottom: 5px;
}

.job-tag.location {
  background-color: #ecf5ff;
  color: #409EFF;
}

.job-tag.type {
  background-color: #f0f9eb;
  color: #67c23a;
}

.tech-stack-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  margin-bottom: 10px;
}

.tech-tag {
  background-color: #f0f2f5;
  color: #1677ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: inline-block;
}

.modal-tech-tags {
  margin-top: 0;
}

.modal-tech-tags .tech-tag {
  font-size: 14px;
  padding: 6px 12px;
}

.job-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  height: 60px;
  overflow: hidden;
}

.job-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-time {
  color: #909399;
  font-size: 12px;
}

.view-details-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
}

.page-btn {
  background-color: #f4f4f5;
  color: #606266;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  margin: 0 15px;
  color: #606266;
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
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
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

/* 岗位详情样式 */
.job-details .job-title {
  font-size: 24px;
  margin-bottom: 10px;
  color: #303133;
}

.job-salary-detail {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 20px;
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

.details-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  gap: 20px;
}

.contact-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.view-enterprise-btn {
  background-color: #f4f4f5;
  color: #606266;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* 推荐岗位样式 */
.recommended-jobs-section {
  margin-bottom: 30px;
  background-color: #f0f9ff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 5px;
}

.section-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 20px;
}

.recommended-jobs-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.job-card.recommended {
  border-left: 4px solid #409EFF;
  background-color: #fff;
}

.job-tag.recommended-tag {
  background-color: #409EFF;
  color: white;
}
</style>
