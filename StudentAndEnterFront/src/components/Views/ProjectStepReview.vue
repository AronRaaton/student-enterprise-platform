<template>
  <div class="step-review-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">审核项目步骤 {{ stepNumber }}</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 审核内容 -->
    <div v-else class="review-content">
      <!-- 项目信息 -->
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">项目方向:</span>
            <span class="meta-value">{{ project.projectDirection }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">当前步骤:</span>
            <span class="meta-value">{{ project.currentStep }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">最大参与人数:</span>
            <span class="meta-value">{{ project.maxParticipants }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤说明 -->
      <div class="step-info">
        <h3 class="step-title">{{ getStepTitle(stepNumber) }}</h3>
        <p class="step-description">{{ getStepDescription(stepNumber) }}</p>
      </div>

      <!-- 学生信息 -->
      <div class="student-info-section">
        <h3 class="section-title">提交学生信息</h3>
        <div class="student-info">
          <div class="student-avatar" @click="viewStudentProfile" title="点击查看学生详情">
            {{ submission.studentName ? submission.studentName.charAt(0) : 'S' }}
          </div>
          <div class="student-details">
            <h4 class="student-name" @click="viewStudentProfile" title="点击查看学生详情" style="cursor: pointer">
              {{ submission.studentName }}
            </h4>
            <p class="student-school">{{ submission.studentSchool }}</p>
            <button class="view-profile-btn" @click="viewStudentProfile">查看学生详情</button>
          </div>
        </div>
      </div>

      <!-- 第3步的历史提交列表 -->
      <div v-if="stepNumber === 3 && !loading && batchSubmissions.length > 0" class="batch-submissions">
        <div class="batch-header">
          <h3 class="section-title">学生提交历史</h3>
          <button class="toggle-batch-btn" @click="showBatchHistory = !showBatchHistory">
            {{ showBatchHistory ? '隐藏历史提交' : '显示历史提交' }}
          </button>
        </div>

        <!-- 历史提交列表 -->
        <div v-if="showBatchHistory" class="batch-list">
          <div v-for="batch in batchSubmissions" :key="batch.batchNumber"
               class="batch-item"
               :class="{ 'active': selectedBatchNumber === batch.batchNumber, 'rejected': batch.status === 'REJECTED' }"
               @click="selectBatch(batch.batchNumber)">
            <div class="batch-info">
              <span class="batch-number">提交 {{ batch.batchNumber }}</span>
              <span v-if="batch.briefDescription" class="brief-description">【{{ batch.briefDescription }}】</span>
              <span class="batch-status" :class="getStatusClass(batch.status)">
                {{ getStatusText(batch.status) }}
              </span>
            </div>
            <div class="batch-time">提交时间: {{ formatDate(batch.submissionTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 提交内容 -->
      <div class="submission-details">
        <h3 class="section-title">
          <span v-if="stepNumber === 3">提交 {{ submission.batchNumber }} 内容
            <span v-if="submission.briefDescription" class="brief-description">【{{ submission.briefDescription }}】</span>
          </span>
          <span v-else>提交内容</span>
        </h3>

        <div class="detail-item">
          <h4 class="detail-label">提交时间:</h4>
          <p class="detail-value">{{ formatDate(submission.submissionTime) }}</p>
        </div>

        <div class="detail-item">
          <h4 class="detail-label">提交状态:</h4>
          <p class="detail-value status" :class="getStatusClass(submission.status)">
            {{ getStatusText(submission.status) }}
          </p>
        </div>

        <div class="detail-item">
          <h4 class="detail-label">内容描述:</h4>
          <p class="detail-value content">{{ submission.content }}</p>
        </div>

        <!-- 上传的文件 -->
        <div v-if="submission.uploadedFile" class="detail-item">
          <h4 class="detail-label">上传的文件:</h4>
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ getFileName(submission.uploadedFile) }}</span>
            </div>
            <div class="file-actions">
              <button v-if="isViewableFile(submission.uploadedFile)" @click="viewFile(submission.uploadedFile)" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </button>
              <button @click="downloadFile(submission.uploadedFile)" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </button>
            </div>
          </div>
        </div>

        <div v-if="submission.feedback" class="detail-item">
          <h4 class="detail-label">反馈信息:</h4>
          <p class="detail-value">{{ submission.feedback }}</p>
        </div>
      </div>

      <!-- 审核表单 -->
      <div v-if="submission.status === 'PENDING'" class="review-form">
        <h3 class="section-title">审核决定</h3>

        <div class="form-group">
          <label for="feedback">反馈信息</label>
          <textarea
            id="feedback"
            v-model="feedback"
            placeholder="请输入对学生的反馈信息"
            class="form-control"
            rows="4"
          ></textarea>
        </div>

        <div class="review-actions">
          <button class="reject-btn" @click="rejectSubmission">拒绝提交</button>
          <button class="approve-btn" @click="approveSubmission">批准提交</button>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="bottom-actions">
        <button class="back-to-list-btn" @click="goToProjectDetails">返回项目详情</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      project: {},
      submission: {},
      batchSubmissions: [], // 所有批次提交
      loading: true,
      feedback: '',
      showBatchHistory: false, // 是否显示批次历史
      selectedBatchNumber: null // 当前选中的批次号
    };
  },
  computed: {
    projectId() {
      return this.$route.params.projectId;
    },
    stepNumber() {
      return parseInt(this.$route.params.stepNumber);
    },
    studentId() {
      return this.$route.params.studentId;
    }
  },
  created() {
    this.fetchProjectDetails();

    // 如果是第3步，获取所有批次提交
    if (this.stepNumber === 3) {
      this.fetchBatchSubmissions().then(() => {
        // 如果有指定批次号，选中该批次
        const queryBatchNumber = this.$route.query.batchNumber;
        if (queryBatchNumber && this.batchSubmissions.length > 0) {
          const batchNumber = parseInt(queryBatchNumber);
          this.selectBatch(batchNumber);
        }
      });
    } else {
      this.fetchSubmissionDetails();
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}`);
        this.project = response.data;
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请稍后重试');
      }
    },
    // 获取所有批次提交（第3步）
    async fetchBatchSubmissions() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submissions/${this.studentId}/batches`
        );
        this.batchSubmissions = response.data;

        // 如果有批次提交，默认显示最新的一个
        if (this.batchSubmissions.length > 0) {
          // 按批次号降序排序
          this.batchSubmissions.sort((a, b) => b.batchNumber - a.batchNumber);
          this.submission = this.batchSubmissions[0];
          this.selectedBatchNumber = this.submission.batchNumber;
        }
      } catch (error) {
        console.error('获取批次提交失败:', error);
        alert('获取批次提交失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },

    // 选择批次
    selectBatch(batchNumber) {
      // 找到对应批次的提交
      const submission = this.batchSubmissions.find(s => s.batchNumber === batchNumber);
      if (submission) {
        this.submission = submission;
        this.selectedBatchNumber = batchNumber;
        this.feedback = ''; // 清空反馈
      }
    },

    // 获取单个提交详情（非第3步）
    async fetchSubmissionDetails() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submissions/${this.studentId}`
        );
        this.submission = response.data;
      } catch (error) {
        console.error('获取提交详情失败:', error);
        alert('获取提交详情失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    async approveSubmission() {
      try {
        // 构建请求URL和参数
        let url = `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/review/${this.studentId}`;
        let params = { status: 'APPROVED' };

        // 如果是第3步，添加批次号参数
        if (this.stepNumber === 3 && this.selectedBatchNumber) {
          params.batchNumber = this.selectedBatchNumber;
        }

        await axios.put(
          url,
          {
            feedback: this.feedback || '提交已通过，继续保持！'
          },
          {
            params: params
          }
        );

        // 刷新数据
        if (this.stepNumber === 3) {
          // 刷新批次提交列表
          await this.fetchBatchSubmissions();
        } else {
          // 刷新提交详情
          this.fetchSubmissionDetails();
        }

        // 刷新项目详情
        this.fetchProjectDetails();

        alert('已批准该提交');
      } catch (error) {
        console.error('批准提交失败:', error);
        alert('批准提交失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      }
    },
    async rejectSubmission() {
      if (!this.feedback) {
        alert('请输入拒绝理由');
        return;
      }

      try {
        // 构建请求URL和参数
        let url = `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/review/${this.studentId}`;
        let params = { status: 'REJECTED' };

        // 如果是第3步，添加批次号参数
        if (this.stepNumber === 3 && this.selectedBatchNumber) {
          params.batchNumber = this.selectedBatchNumber;
        }

        await axios.put(
          url,
          {
            feedback: this.feedback
          },
          {
            params: params
          }
        );

        // 刷新数据
        if (this.stepNumber === 3) {
          // 刷新批次提交列表
          await this.fetchBatchSubmissions();
        } else {
          // 刷新提交详情
          this.fetchSubmissionDetails();
        }

        // 刷新项目详情
        this.fetchProjectDetails();

        alert('已拒绝该提交');
      } catch (error) {
        console.error('拒绝提交失败:', error);
        alert('拒绝提交失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      }
    },
    viewStudentProfile() {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId: this.studentId }
      });
    },
    goToProjectDetails() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.projectId }
      });
    },
    // 获取步骤标题
    getStepTitle(stepNumber) {
      switch (stepNumber) {
        case 1:
          return '需求分析与问题识别';
        case 2:
          return '项目立项与资源整合';
        case 3:
          return '项目实施与动态监控';
        case 4:
          return '成果转化与市场推广';
        default:
          return '未知步骤';
      }
    },
    // 获取步骤描述
    getStepDescription(stepNumber) {
      switch (stepNumber) {
        case 1:
          return '明确企业技术需求、市场痛点或科研机构的技术供给方向，通过调研、行业分析或技术研讨确定合作目标。学生需提交可转化的科研成果（如专利技术、实验数据）。';
        case 2:
          return '制定合作计划，整合资金、设备、人才等资源，明确各方角色分工。企业上传计划书，学生确认是否继续参与。';
        case 3:
          return '按计划推进研发、试验或生产。企业提供试验场地和市场反馈，学生负责技术攻关。学生可分批次提交内容。';
        case 4:
          return '将技术成果转化为产品或服务，推向市场并验证商业化价值。企业提供技术成果的相关资料和相关产品服务的市场表现市场信息等资料。';
        default:
          return '未知步骤描述';
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },
    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 'PENDING':
          return 'status-pending';
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        default:
          return '';
      }
    },
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已通过';
        case 'REJECTED':
          return '已拒绝';
        default:
          return '未知状态';
      }
    },
    // 获取文件名
    getFileName(fileString) {
      if (!fileString) return '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileString);
        return fileInfo.name || '未知文件';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        // 从路径中提取文件名，去除UUID前缀
        const fileName = fileString.split('/').pop() || fileString;
        if (fileName.includes('_')) {
          return fileName.substring(fileName.indexOf('_') + 1);
        }
        return fileName;
      }
    },
    // 判断文件是否可以在浏览器中预览
    isViewableFile(fileString) {
      if (!fileString) return false;

      let fileName = '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileString);
        fileName = fileInfo.name || '';
      } catch (e) {
        // 如果不是JSON，则是直接存储的文件名
        fileName = fileString.split('/').pop() || fileString;
      }

      // 检查文件扩展名
      const lowerFileName = fileName.toLowerCase();
      return lowerFileName.endsWith('.pdf') ||
             lowerFileName.endsWith('.txt') ||
             lowerFileName.endsWith('.jpg') ||
             lowerFileName.endsWith('.jpeg') ||
             lowerFileName.endsWith('.png');
    },
    // 查看文件
    viewFile(fileData) {
      // 构建URL
      let fileUrl = '';

      // 先尝试解析JSON，兼容旧的数据格式
      try {
        const fileInfo = JSON.parse(fileData);
        // 如果是JSON，则使用文件名属性
        fileUrl = `http://localhost:8080/api/files/download/${fileInfo.name}?preview=true`;
      } catch (e) {
        // 如果不是JSON，则直接使用文件名
        fileUrl = `http://localhost:8080/api/files/download/${fileData}?preview=true`;
      }

      // 打开新窗口预览文件
      window.open(fileUrl, '_blank');
    },
    // 下载文件
    async downloadFile(fileData) {
      try {
        // 构建URL
        let fileUrl = '';

        // 先尝试解析JSON，兼容旧的数据格式
        try {
          const fileInfo = JSON.parse(fileData);
          // 如果是JSON，则使用文件名属性
          fileUrl = `http://localhost:8080/api/files/download/${fileInfo.name}`;
        } catch (e) {
          // 如果不是JSON，则直接使用文件名
          fileUrl = `http://localhost:8080/api/files/download/${fileData}`;
        }

        // 使用fetch API获取文件
        const response = await fetch(fileUrl);

        if (!response.ok) {
          throw new Error(`下载失败: ${response.status} ${response.statusText}`);
        }

        // 获取blob数据
        const blob = await response.blob();

        // 创建下载链接
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;

        // 设置文件名
        let fileName = this.getFileName(fileData);
        a.download = fileName;

        // 触发下载
        document.body.appendChild(a);
        a.click();

        // 清理
        window.URL.revokeObjectURL(downloadUrl);
        document.body.removeChild(a);
      } catch (error) {
        console.error('下载文件失败:', error);
        alert('下载文件失败: ' + error.message);
      }
    }
  }
};
</script>

<style scoped>
.step-review-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

/* 批次提交相关样式 */
.batch-submissions {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.toggle-batch-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  background-color: #ecf5ff;
  color: #409EFF;
}

.toggle-batch-btn:hover {
  background-color: #d9ecff;
}

.batch-list {
  margin-bottom: 20px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #eaeaea;
  border-radius: 4px;
}

.batch-item {
  padding: 12px 15px;
  border-bottom: 1px solid #eaeaea;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-item:last-child {
  border-bottom: none;
}

.batch-item:hover {
  background-color: #f5f7fa;
}

.batch-item.active {
  background-color: #ecf5ff;
}

.batch-item.rejected {
  background-color: #fff8f8;
}

.batch-info {
  display: flex;
  align-items: center;
}

.batch-number {
  font-weight: 500;
  margin-right: 10px;
}

.brief-description {
  color: #409EFF;
  font-size: 14px;
  margin-right: 10px;
  font-weight: 500;
}

.batch-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.batch-time {
  color: #909399;
  font-size: 12px;
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
  font-size: 20px;
  margin-right: 5px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.project-info {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
}

.project-title {
  font-size: 22px;
  color: #303133;
  margin: 0 0 15px 0;
}

.project-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.meta-value {
  color: #303133;
  font-weight: 500;
}

.step-info {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.step-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 15px 0;
}

.step-description {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

/* 学生信息 */
.student-info-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.student-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.student-details {
  flex: 1;
}

.student-name {
  font-size: 18px;
  color: #303133;
  margin: 0 0 5px 0;
}

.student-school {
  color: #606266;
  margin: 0 0 10px 0;
}

.view-profile-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-profile-btn:hover {
  background-color: #409EFF;
  color: white;
  border-color: #409EFF;
}

/* 提交详情 */
.submission-details {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-label {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.detail-value {
  color: #606266;
  margin: 0;
  line-height: 1.6;
}

.detail-value.content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-line;
}

.status {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: bold;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-approved {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f5f7fa;
  padding: 12px 15px;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
}

.file-icon {
  font-size: 20px;
  margin-right: 10px;
}

.file-name {
  font-size: 14px;
  color: #303133;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.view-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.view-btn:hover {
  background-color: #e1f3d8;
}

.download-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.download-btn:hover {
  background-color: #d9ecff;
}

.action-icon {
  margin-right: 5px;
}

/* 审核表单 */
.review-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #409EFF;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.approve-btn, .reject-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.approve-btn {
  background-color: #67c23a;
  color: white;
}

.approve-btn:hover {
  background-color: #5daf34;
}

.reject-btn {
  background-color: #f56c6c;
  color: white;
}

.reject-btn:hover {
  background-color: #e64242;
}

/* 底部按钮 */
.bottom-actions {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.back-to-list-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-to-list-btn:hover {
  background-color: #e6e8eb;
}

@media (max-width: 768px) {
  .project-meta {
    grid-template-columns: 1fr;
  }

  .student-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-avatar {
    margin-bottom: 15px;
  }

  .file-preview {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-actions {
    margin-top: 10px;
  }

  .review-actions {
    flex-direction: column;
    gap: 10px;
  }

  .approve-btn, .reject-btn {
    width: 100%;
  }
}
</style>
