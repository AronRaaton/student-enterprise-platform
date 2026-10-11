<template>
  <div class="order-training-container module-page module-detail">
    <div class="header">
      <button class="back-btn" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h2>订单培养项目详情</h2>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载项目详情中...</p>
    </div>

    <div v-else class="content-container">
      <div class="project-header">
        <h3 class="project-title">{{ project.title }}</h3>
        <span class="project-status" :class="getStatusClass(project.status)">
          {{ getStatusText(project.status) }}
        </span>
      </div>

      <div class="project-info">
        <div class="info-item">
          <span class="info-label">项目方向:</span>
          <span class="info-value">{{ project.direction }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">合作类型:</span>
          <span class="info-value">{{ project.collaborationType }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">开始日期:</span>
          <span class="info-value">{{ formatDate(project.startDate) }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">结束日期:</span>
          <span class="info-value">{{ formatDate(project.endDate) }}</span>
        </div>
      </div>

      <div class="project-description">
        <h4>项目描述</h4>
        <div class="description-content">{{ project.description }}</div>
      </div>

      <div class="order-training-process">
        <h4>订单培养流程</h4>
        <div class="process-steps">
          <div class="process-step" :class="{ 'active': currentStep >= 1, 'completed': currentStep > 1 }">
            <div class="step-number">1</div>
            <div class="step-content">
              <h5>需求分析与人才规划</h5>
              <p>企业提出人才需求，学校与企业共同制定培养方案</p>
              <div v-if="isEnterprise && currentStep === 1" class="step-actions">
                <button class="action-btn" @click="uploadRequirementAnalysis">上传需求分析</button>
              </div>
              <div v-if="isStudent && currentStep === 1 && requirementAnalysisFile" class="file-preview">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ getFileName(requirementAnalysisFile) }}</span>
                </div>
                <div class="file-actions">
                  <button class="action-btn view-btn" @click="viewFile(requirementAnalysisFile)">
                    <span class="view-icon">👁️</span> 查看
                  </button>
                  <button class="action-btn download-btn" @click="downloadFile(requirementAnalysisFile)">
                    <span class="download-icon">⬇️</span> 下载
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="process-step" :class="{ 'active': currentStep >= 2, 'completed': currentStep > 2 }">
            <div class="step-number">2</div>
            <div class="step-content">
              <h5>培养方案制定</h5>
              <p>学校根据企业需求，制定专业课程和实践环节</p>
              <div v-if="isEnterprise && currentStep === 2" class="step-actions">
                <button class="action-btn" @click="uploadTrainingPlan">上传培养方案</button>
              </div>
              <div v-if="isStudent && currentStep === 2 && trainingPlanFile" class="file-preview">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ getFileName(trainingPlanFile) }}</span>
                </div>
                <div class="file-actions">
                  <button class="action-btn view-btn" @click="viewFile(trainingPlanFile)">
                    <span class="view-icon">👁️</span> 查看
                  </button>
                  <button class="action-btn download-btn" @click="downloadFile(trainingPlanFile)">
                    <span class="download-icon">⬇️</span> 下载
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="process-step" :class="{ 'active': currentStep >= 3, 'completed': currentStep > 3 }">
            <div class="step-number">3</div>
            <div class="step-content">
              <h5>培养实施</h5>
              <p>学生按照培养方案进行学习，企业参与指导</p>
              <div v-if="isEnterprise && currentStep === 3" class="step-actions">
                <button class="action-btn" @click="uploadImplementationGuide">上传实施指导</button>
              </div>
              <div v-if="isStudent && currentStep === 3 && implementationGuideFile" class="file-preview">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ getFileName(implementationGuideFile) }}</span>
                </div>
                <div class="file-actions">
                  <button class="action-btn view-btn" @click="viewFile(implementationGuideFile)">
                    <span class="view-icon">👁️</span> 查看
                  </button>
                  <button class="action-btn download-btn" @click="downloadFile(implementationGuideFile)">
                    <span class="download-icon">⬇️</span> 下载
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="process-step" :class="{ 'active': currentStep >= 4, 'completed': currentStep > 4 }">
            <div class="step-number">4</div>
            <div class="step-content">
              <h5>评估与就业</h5>
              <p>对培养效果进行评估，优秀学生可优先就业</p>
              <div v-if="isStudent && currentStep === 4 && !evaluationReportFile" class="step-actions">
                <button class="action-btn" @click="uploadEvaluationReport">上传学习总结</button>
              </div>
              <div v-if="evaluationReportFile" class="file-preview">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ getFileName(evaluationReportFile) }}</span>
                </div>
                <div class="file-actions">
                  <button class="action-btn view-btn" @click="viewFile(evaluationReportFile)">
                    <span class="view-icon">👁️</span> 查看
                  </button>
                  <button class="action-btn download-btn" @click="downloadFile(evaluationReportFile)">
                    <span class="download-icon">⬇️</span> 下载
                  </button>
                </div>
              </div>
              <div v-if="isEnterprise && currentStep === 4 && evaluationReportFile" class="step-actions">
                <button class="action-btn" @click="completeProject">完成项目</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="communication-section">
        <button class="chat-btn" @click="chatWithOtherParty">
          <span class="chat-icon">💬</span> 
          {{ isStudent ? '与企业交流' : '与学生交流' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'OrderTrainingDetail',
  data() {
    return {
      projectId: null,
      project: {},
      loading: true,
      userRole: '',
      userId: '',
      currentStep: 1,
      requirementAnalysisFile: '',
      trainingPlanFile: '',
      implementationGuideFile: '',
      evaluationReportFile: ''
    };
  },
  computed: {
    isStudent() {
      return this.userRole === 'student';
    },
    isEnterprise() {
      return this.userRole === 'enterprise';
    }
  },
  created() {
    this.projectId = this.$route.params.projectId;
    this.userRole = localStorage.getItem('userRole');
    this.userId = localStorage.getItem('userId');
    this.fetchProjectDetails();
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}`);
        this.project = response.data;
        
        // 设置当前步骤
        this.currentStep = this.project.currentStep || 1;
        
        // 获取各步骤文件
        this.fetchStepFiles();
      } catch (error) {
        console.error('获取项目详情失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async fetchStepFiles() {
      try {
        // 获取需求分析文件
        const step1Response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/order-training/step/1`);
        if (step1Response.data && step1Response.data.fileUrl) {
          this.requirementAnalysisFile = step1Response.data.fileUrl;
        }
        
        // 获取培养方案文件
        if (this.currentStep >= 2) {
          const step2Response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/order-training/step/2`);
          if (step2Response.data && step2Response.data.fileUrl) {
            this.trainingPlanFile = step2Response.data.fileUrl;
          }
        }
        
        // 获取实施指导文件
        if (this.currentStep >= 3) {
          const step3Response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/order-training/step/3`);
          if (step3Response.data && step3Response.data.fileUrl) {
            this.implementationGuideFile = step3Response.data.fileUrl;
          }
        }
        
        // 获取评估报告文件
        if (this.currentStep >= 4) {
          const step4Response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/order-training/step/4`);
          if (step4Response.data && step4Response.data.fileUrl) {
            this.evaluationReportFile = step4Response.data.fileUrl;
          }
        }
      } catch (error) {
        console.error('获取步骤文件失败:', error);
      }
    },
    async uploadRequirementAnalysis() {
      this.$router.push({
        name: 'OrderTrainingUpload',
        params: { 
          projectId: this.projectId,
          stepNumber: 1,
          stepName: '需求分析与人才规划'
        }
      });
    },
    async uploadTrainingPlan() {
      this.$router.push({
        name: 'OrderTrainingUpload',
        params: { 
          projectId: this.projectId,
          stepNumber: 2,
          stepName: '培养方案制定'
        }
      });
    },
    async uploadImplementationGuide() {
      this.$router.push({
        name: 'OrderTrainingUpload',
        params: { 
          projectId: this.projectId,
          stepNumber: 3,
          stepName: '培养实施'
        }
      });
    },
    async uploadEvaluationReport() {
      this.$router.push({
        name: 'OrderTrainingUpload',
        params: { 
          projectId: this.projectId,
          stepNumber: 4,
          stepName: '评估与就业'
        }
      });
    },
    async completeProject() {
      try {
        await axios.put(`http://localhost:8080/api/projects/${this.projectId}/status`, null, {
          params: { status: 'COMPLETED' }
        });
        
        // 刷新项目详情
        this.fetchProjectDetails();
        alert('项目已成功完成！');
      } catch (error) {
        console.error('完成项目失败:', error);
        alert('完成项目失败，请重试');
      }
    },
    chatWithOtherParty() {
      if (this.isStudent) {
        // 学生与企业交流
        this.$router.push({
          name: 'ChatS2E',
          params: { enterpriseId: this.project.enterpriseId }
        });
      } else {
        // 企业与学生交流
        // 这里需要获取参与项目的学生ID
        // 简化处理，假设已经有了学生ID
        if (this.project.studentIds && this.project.studentIds.length > 0) {
          this.$router.push({
            name: 'ChatE2S',
            params: { studentId: this.project.studentIds[0] }
          });
        } else {
          alert('暂无学生参与该项目');
        }
      }
    },
    getStatusClass(status) {
      switch (status) {
        case 'OPEN':
          return 'status-open';
        case 'IN_PROGRESS':
          return 'status-in-progress';
        case 'COMPLETED':
          return 'status-completed';
        case 'CLOSED':
          return 'status-closed';
        default:
          return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'OPEN':
          return '开放中';
        case 'IN_PROGRESS':
          return '进行中';
        case 'COMPLETED':
          return '已完成';
        case 'CLOSED':
          return '已关闭';
        default:
          return '未知状态';
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未设置';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
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
    async viewFile(fileData) {
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

        // 创建URL对象
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
.order-training-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.back-btn {
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 20px;
  display: flex;
  align-items: center;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.project-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.project-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.status-open {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-in-progress {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.project-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 25px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
  min-width: 80px;
}

.info-value {
  color: #303133;
}

.project-description {
  margin-bottom: 30px;
}

.project-description h4 {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #303133;
}

.description-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}

.order-training-process {
  margin-bottom: 30px;
}

.order-training-process h4 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
}

.process-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.process-step {
  display: flex;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  opacity: 0.7;
  transition: all 0.3s;
}

.process-step.active {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  opacity: 1;
  border-left: 4px solid #409EFF;
}

.process-step.completed {
  border-left: 4px solid #67c23a;
}

.step-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #909399;
  color: white;
  border-radius: 50%;
  font-weight: bold;
  margin-right: 20px;
}

.process-step.active .step-number {
  background-color: #409EFF;
}

.process-step.completed .step-number {
  background-color: #67c23a;
}

.step-content {
  flex: 1;
}

.step-content h5 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.step-content p {
  margin: 0 0 15px 0;
  color: #606266;
}

.step-actions {
  margin-top: 15px;
}

.action-btn {
  padding: 8px 15px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f2f5;
  padding: 10px 15px;
  border-radius: 6px;
  margin-top: 15px;
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
  word-break: break-all;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.view-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.view-btn:hover {
  background-color: #d9ecff;
}

.download-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.download-btn:hover {
  background-color: #e1f3d8;
}

.communication-section {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.chat-btn {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
}

.chat-btn:hover {
  background-color: #66b1ff;
}

.chat-icon {
  margin-right: 8px;
  font-size: 18px;
}

@media (max-width: 768px) {
  .project-info {
    grid-template-columns: 1fr;
  }

  .file-preview {
    flex-direction: column;
    align-items: flex-start;
  }

  .file-actions {
    margin-top: 10px;
  }
}
</style>
