<template>
  <div class="step-submission-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">提交项目步骤 {{ stepNumber }}</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 提交表单 -->
    <div v-else class="submission-content">
      <!-- 项目信息 -->
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">企业:</span>
            <span class="meta-value">{{ project.enterpriseName }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">项目方向:</span>
            <span class="meta-value">{{ project.projectDirection }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">当前步骤:</span>
            <span class="meta-value">{{ project.currentStep }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤说明 -->
      <div class="step-info">
        <h3 class="step-title">{{ getStepTitle(stepNumber) }}</h3>
        <p class="step-description">{{ getStepDescription(stepNumber) }}</p>
      </div>

      <!-- 第2步显示项目计划书 -->
      <div v-if="stepNumber === 2 && project.projectPlan" class="project-plan-section">
        <h3 class="section-title">项目计划书</h3>
        <div class="project-plan-content">
          <p>{{ project.projectPlan }}</p>
        </div>

        <!-- 显示计划书文件 -->
        <div v-if="project.projectPlanFile" class="file-section">
          <h4>计划书文件:</h4>
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ getFileName(project.projectPlanFile) }}</span>
            </div>
            <div class="file-actions">
              <button @click="downloadFile(project.projectPlanFile)" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </button>
              <button v-if="isViewableFile(project.projectPlanFile)" @click="viewFile(project.projectPlanFile)" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </button>
            </div>
          </div>
        </div>

        <div class="plan-review-hint">
          <p>请仔细阅读企业提供的项目计划书，并在下方提交您的意见或确认。</p>
        </div>
      </div>

      <!-- 第3步的历史提交列表 -->
      <div v-if="stepNumber === 3 && batchSubmissions.length > 0" class="batch-submissions">
        <div class="batch-header">
          <h3 class="section-title">您的提交历史</h3>
          <button class="toggle-batch-btn" @click="showBatchHistory = !showBatchHistory">
            {{ showBatchHistory ? '隐藏历史提交' : '显示历史提交' }}
          </button>
          <button class="new-batch-btn" @click="createNewBatch">创建新提交</button>
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
              <span v-if="batch.isFinalSubmission" class="final-submission-tag-small">最终</span>
              <span class="batch-status" :class="getStatusClass(batch.status)">
                {{ getStatusText(batch.status) }}
              </span>
            </div>
            <div class="batch-time">提交时间: {{ formatDate(batch.submissionTime) }}</div>
          </div>
        </div>

        <!-- 当前选中的提交详情 -->
        <div v-if="existingSubmission && !creatingNewBatch" class="existing-submission">
          <div class="submission-header">
            <h4 class="batch-title">提交 {{ existingSubmission.batchNumber }} 详情
              <span v-if="existingSubmission.briefDescription" class="brief-description">【{{ existingSubmission.briefDescription }}】</span>
              <span v-if="existingSubmission.isFinalSubmission" class="final-submission-tag-small">最终</span>
            </h4>
            <div class="submission-status" :class="getStatusClass(existingSubmission.status)">
              {{ getStatusText(existingSubmission.status) }}
            </div>
          </div>

          <div class="submission-time">
            提交时间: {{ formatDate(existingSubmission.submissionTime) }}
            <span v-if="existingSubmission.isFinalSubmission" class="final-submission-tag">最终提交</span>
          </div>

          <div v-if="existingSubmission.feedback" class="feedback-section">
            <h4>企业反馈:</h4>
            <p>{{ existingSubmission.feedback }}</p>
          </div>

          <div class="content-section">
            <h4>提交内容:</h4>
            <p>{{ existingSubmission.content }}</p>
          </div>

          <div v-if="existingSubmission.uploadedFile" class="file-section">
            <h4>上传的文件:</h4>
            <div class="file-preview">
              <div class="file-info">
                <span class="file-icon">📄</span>
                <span class="file-name">{{ getFileName(existingSubmission.uploadedFile) }}</span>
              </div>
              <div class="file-actions">
                <button @click="downloadFile(existingSubmission.uploadedFile)" class="action-btn download-btn">
                  <span class="action-icon">⬇️</span> 下载
                </button>
                <button v-if="isViewableFile(existingSubmission.uploadedFile)" @click="viewFile(existingSubmission.uploadedFile)" class="action-btn view-btn">
                  <span class="action-icon">🔍</span> 查看
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 非第3步的已有提交 -->
      <div v-if="stepNumber !== 3 && existingSubmission" class="existing-submission">
        <h3 class="section-title">您的提交</h3>
        <div class="submission-status" :class="getStatusClass(existingSubmission.status)">
          {{ getStatusText(existingSubmission.status) }}
        </div>

        <div class="submission-time">
          提交时间: {{ formatDate(existingSubmission.submissionTime) }}
          <span v-if="existingSubmission.isFinalSubmission" class="final-submission-tag">最终提交</span>
        </div>

        <div v-if="existingSubmission.feedback" class="feedback-section">
          <h4>企业反馈:</h4>
          <p>{{ existingSubmission.feedback }}</p>
        </div>

        <div class="content-section">
          <h4>提交内容:</h4>
          <p>{{ existingSubmission.content }}</p>
        </div>

        <div v-if="existingSubmission.uploadedFile" class="file-section">
          <h4>上传的文件:</h4>
          <div class="file-preview">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ getFileName(existingSubmission.uploadedFile) }}</span>
            </div>
            <div class="file-actions">
              <button @click="downloadFile(existingSubmission.uploadedFile)" class="action-btn download-btn">
                <span class="action-icon">⬇️</span> 下载
              </button>
              <button v-if="isViewableFile(existingSubmission.uploadedFile)" @click="viewFile(existingSubmission.uploadedFile)" class="action-btn view-btn">
                <span class="action-icon">🔍</span> 查看
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 提交表单 -->
      <div v-if="!existingSubmission || existingSubmission.status === 'REJECTED' || (stepNumber === 3 && creatingNewBatch)" class="submission-form">
        <h3 class="section-title">
          <span v-if="stepNumber === 3 && creatingNewBatch">创建新提交</span>
          <span v-else-if="existingSubmission && stepNumber === 3">重新提交</span>
          <span v-else-if="existingSubmission">重新提交</span>
          <span v-else>提交内容</span>
        </h3>

        <div v-if="stepNumber === 3" class="form-group">
          <label for="briefDescription">内容简述 <span class="required">*</span> (五个字以内)</label>
          <input
            id="briefDescription"
            v-model="submission.briefDescription"
            class="form-control"
            maxlength="5"
            placeholder="请输入内容简述"
            :class="{ 'is-invalid': submitted && !submission.briefDescription && stepNumber === 3 }"
          />
          <div v-if="submitted && !submission.briefDescription && stepNumber === 3" class="invalid-feedback">
            请输入内容简述
          </div>
          <small class="form-text text-muted">请简要描述本次提交的内容，最多5个字符</small>
        </div>

        <!-- 第3步添加最终提交选项 -->
        <div v-if="stepNumber === 3" class="form-group final-submission-option">
          <div class="checkbox-container">
            <input
              type="checkbox"
              id="isFinalSubmission"
              v-model="submission.isFinalSubmission"
            />
            <label for="isFinalSubmission" class="checkbox-label">
              <strong>标记为最终提交</strong> - 如果企业通过此提交，项目将直接进入第4步
            </label>
          </div>
        </div>

        <div class="form-group">
          <label for="content">内容描述 <span class="required">*</span></label>
          <textarea
            id="content"
            v-model="submission.content"
            :placeholder="stepNumber === 2 ? '请对项目计划书进行评价或确认，如有建议请在此提出' : '请详细描述您的工作内容和成果'"
            class="form-control"
            rows="8"
            :class="{ 'is-invalid': submitted && !submission.content }"
          ></textarea>
          <div v-if="submitted && !submission.content" class="invalid-feedback">
            请输入内容描述
          </div>
        </div>

        <!-- 非第2步才显示文件上传区域 -->
        <div v-if="stepNumber !== 2" class="form-group">
          <label for="file">上传文件</label>
          <div class="file-upload-container">
            <input
              type="file"
              id="file"
              ref="fileInput"
              @change="handleFileUpload"
              style="display: none"
            />
            <div class="file-upload-box" @click="triggerFileInput">
              <div v-if="!selectedFile" class="upload-placeholder">
                <span class="upload-icon">📎</span>
                <span>点击上传文件</span>
              </div>
              <div v-else class="selected-file">
                <span class="file-icon">📄</span>
                <span class="file-name">{{ selectedFile.name }}</span>
                <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
                <button class="remove-file-btn" @click.stop="removeFile">×</button>
              </div>
            </div>
            <p class="file-hint">支持PDF、Word、Excel、图片等格式，最大30MB</p>
          </div>
        </div>

        <div class="form-actions">
          <button
            class="submit-btn"
            @click="submitStep"
            :disabled="isSubmitting"
          >
            <span v-if="isSubmitting">提交中...</span>
            <span v-else>{{ existingSubmission ? '重新提交' : '提交' }}</span>
          </button>
        </div>
      </div>

      <!-- 提交成功提示 -->
      <div v-if="showSuccess" class="success-message">
        <div class="success-icon">✓</div>
        <h3>提交成功</h3>
        <p>您的内容已成功提交，请等待企业审核</p>
        <div class="success-actions">
          <button class="action-btn" @click="viewProjectDetails">返回项目详情</button>
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
      project: {},
      existingSubmission: null,
      batchSubmissions: [], // 所有历史提交
      submission: {
        projectId: null,
        studentId: null,
        stepNumber: null,
        content: '',
        uploadedFile: '',
        batchNumber: null,
        briefDescription: '', // 内容简述（五个字以内）
        isFinalSubmission: false // 是否为最终提交
      },
      selectedFile: null,
      loading: true,
      submitted: false,
      isSubmitting: false,
      showSuccess: false,
      showBatchHistory: false, // 是否显示历史提交
      creatingNewBatch: false, // 是否正在创建新提交
      selectedBatchNumber: null // 当前选中的提交批次号
    };
  },
  computed: {
    projectId() {
      return this.$route.params.projectId;
    },
    stepNumber() {
      return parseInt(this.$route.params.stepNumber);
    }
  },
  created() {
    this.submission.projectId = this.projectId;
    this.submission.studentId = localStorage.getItem('userId');
    this.submission.stepNumber = this.stepNumber;

    this.fetchProjectDetails();

    // 如果是第3步，获取所有批次提交
    if (this.stepNumber === 3) {
      this.fetchBatchSubmissions();
    } else {
      this.checkExistingSubmission();
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

        // 检查当前步骤是否可以提交
        if (this.project.currentStep < this.stepNumber) {
          alert(`当前项目还未进入第${this.stepNumber}步，无法提交`);
          this.$router.push({
            name: 'ProjectDetail',
            params: { projectId: this.projectId }
          });
        }
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请稍后重试');
      }
    },
    // 获取所有批次提交（第3步）
    async fetchBatchSubmissions() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submissions/${this.submission.studentId}/batches`
        );
        this.batchSubmissions = response.data;

        // 如果有批次提交，默认显示最新的一个
        if (this.batchSubmissions.length > 0) {
          // 按批次号降序排序
          this.batchSubmissions.sort((a, b) => b.batchNumber - a.batchNumber);
          this.existingSubmission = this.batchSubmissions[0];
          this.selectedBatchNumber = this.existingSubmission.batchNumber;

          // 如果最新提交被拒绝，预填充表单
          if (this.existingSubmission.status === 'REJECTED') {
            this.submission.content = this.existingSubmission.content;
            this.submission.batchNumber = this.existingSubmission.batchNumber;
            this.submission.briefDescription = this.existingSubmission.briefDescription;
            this.submission.isFinalSubmission = this.existingSubmission.isFinalSubmission;
          }
        } else {
          // 如果没有批次提交，创建新的批次
          this.creatingNewBatch = true;
          this.submission.batchNumber = 1;
        }
      } catch (error) {
        console.error('获取批次提交失败:', error);
        // 如果出错，创建新的批次
        this.creatingNewBatch = true;
        this.submission.batchNumber = 1;
      } finally {
        this.loading = false;
      }
    },

    // 获取现有提交（非第3步）
    async checkExistingSubmission() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submissions/${this.submission.studentId}`
        );
        this.existingSubmission = response.data;

        // 如果已有提交且状态为被拒绝，预填充表单
        if (this.existingSubmission && this.existingSubmission.status === 'REJECTED') {
          this.submission.content = this.existingSubmission.content;
          this.submission.isFinalSubmission = this.existingSubmission.isFinalSubmission;
        }
      } catch (error) {
        // 如果没有找到提交记录，不做任何处理
        console.log('没有找到提交记录，创建新提交');
      } finally {
        this.loading = false;
      }
    },
    // 触发文件输入框的点击
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    // 处理文件上传
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        // 检查文件大小，限制为30MB
        if (file.size > 30 * 1024 * 1024) {
          alert('文件大小不能超过30MB');
          this.$refs.fileInput.value = '';
          return;
        }
        this.selectedFile = file;
      }
    },
    // 移除选择的文件
    removeFile() {
      this.selectedFile = null;
      this.$refs.fileInput.value = '';
      this.submission.uploadedFile = '';
    },
    // 上传文件到服务器
    async uploadFile() {
      if (!this.selectedFile) {
        return null;
      }

      try {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        // 发送文件上传请求
        const response = await axios.post('http://localhost:8080/api/files/project-step', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        // 返回服务器生成的文件名
        return response.data.fileName;
      } catch (error) {
        console.error('文件上传失败:', error);
        throw new Error('文件上传失败: ' + (error.response?.data?.error || error.message));
      }
    },
    // 创建新提交
    createNewBatch() {
      // 如果已经在创建新提交，不做任何操作
      if (this.creatingNewBatch) return;

      // 计算新批次号
      let maxBatchNumber = 0;
      if (this.batchSubmissions.length > 0) {
        maxBatchNumber = Math.max(...this.batchSubmissions.map(s => s.batchNumber));
      }

      // 初始化新提交
      this.creatingNewBatch = true;
      this.submission.content = '';
      this.submission.uploadedFile = '';
      this.submission.briefDescription = '';
      this.submission.isFinalSubmission = false;
      this.selectedFile = null;
      this.submission.batchNumber = maxBatchNumber + 1;
      this.existingSubmission = null;
      this.selectedBatchNumber = null;
    },

    // 选择历史提交
    selectBatch(batchNumber) {
      // 如果正在创建新提交，先取消
      this.creatingNewBatch = false;

      // 找到对应批次的提交
      const submission = this.batchSubmissions.find(s => s.batchNumber === batchNumber);
      if (submission) {
        this.existingSubmission = submission;
        this.selectedBatchNumber = batchNumber;

        // 如果状态为被拒绝，预填充表单
        if (submission.status === 'REJECTED') {
          this.submission.content = submission.content;
          this.submission.uploadedFile = submission.uploadedFile;
          this.submission.batchNumber = submission.batchNumber;
          this.submission.briefDescription = submission.briefDescription;
          this.submission.isFinalSubmission = submission.isFinalSubmission;
        }
      }
    },

    // 提交步骤
    async submitStep() {
      this.submitted = true;

      // 表单验证
      if (!this.submission.content) {
        return;
      }

      // 如果是第3步，验证内容简述
      if (this.stepNumber === 3 && !this.submission.briefDescription) {
        return;
      }

      this.isSubmitting = true;

      try {
        // 如果是第2步，不需要上传文件
        if (this.stepNumber !== 2 && this.selectedFile) {
          try {
            const fileName = await this.uploadFile();
            this.submission.uploadedFile = fileName;
          } catch (error) {
            alert('文件上传失败: ' + error.message);
            this.isSubmitting = false;
            return;
          }
        }

        // 准备请求体
        const requestBody = {
          content: this.submission.content,
          uploadedFile: this.stepNumber === 2 ? '' : this.submission.uploadedFile
        };

        // 如果是第3步，添加批次号和内容简述
        if (this.stepNumber === 3) {
          requestBody.batchNumber = this.submission.batchNumber;
          requestBody.briefDescription = this.submission.briefDescription;
        }

        // 提交步骤
        await axios.post(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submit?studentId=${this.submission.studentId}`,
          requestBody
        );

        // 如果是第3步，刷新批次列表
        if (this.stepNumber === 3) {
          await this.fetchBatchSubmissions();
          this.creatingNewBatch = false;
        }

        this.showSuccess = true;
      } catch (error) {
        console.error('提交步骤失败:', error);
        alert('提交步骤失败，请稍后重试: ' + (error.response?.data?.error || error.message));
      } finally {
        this.isSubmitting = false;
      }
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
          return '明确企业技术需求、市场痛点或科研机构的技术供给方向，通过调研、行业分析或技术研讨确定合作目标。请提交您准备完成此项目所使用的技术方向说明。';
        case 2:
          return '制定合作计划，整合资金、设备、人才等资源，明确各方角色分工。请查看企业上传的计划书，并确认是否继续参与项目。';
        case 3:
          return '按计划推进研发、试验或生产。您需要负责技术攻关，企业将提供试验场地和市场反馈。您可以分批次提交内容。';
        case 4:
          return '将技术成果转化为产品或服务，推向市场并验证商业化价值。请提交相关的技术成果资料。';
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
    // 格式化文件大小
    formatFileSize(bytes) {
      if (bytes < 1024) {
        return bytes + ' B';
      } else if (bytes < 1024 * 1024) {
        return (bytes / 1024).toFixed(2) + ' KB';
      } else {
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
      }
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
    },
    // 返回项目详情
    viewProjectDetails() {
      this.$router.push({
        name: 'ProjectDetail',
        params: { projectId: this.projectId }
      });
    }
  }
};
</script>

<style scoped>
.step-submission-container {
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

.toggle-batch-btn, .new-batch-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.toggle-batch-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  margin-right: 10px;
}

.toggle-batch-btn:hover {
  background-color: #d9ecff;
}

.new-batch-btn {
  background-color: #409EFF;
  color: white;
}

.new-batch-btn:hover {
  background-color: #66b1ff;
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

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.batch-title {
  font-size: 18px;
  color: #303133;
  margin: 0;
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

.existing-submission {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.submission-status {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  margin-bottom: 15px;
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

.submission-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
}

.feedback-section, .content-section, .file-section {
  margin-top: 20px;
}

.feedback-section h4, .content-section h4, .file-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.feedback-section p, .content-section p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
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

.submission-form {
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

.required {
  color: #f56c6c;
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

.is-invalid {
  border-color: #f56c6c;
}

.invalid-feedback {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.file-upload-container {
  margin-top: 10px;
}

.file-upload-box {
  border: 2px dashed #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.file-upload-box:hover {
  border-color: #409EFF;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #909399;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

.selected-file {
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-size {
  color: #909399;
  margin-left: 5px;
}

.remove-file-btn {
  background: none;
  border: none;
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  margin-left: 10px;
}

.file-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.submit-btn {
  padding: 10px 30px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.success-message {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
  text-align: center;
}

.success-icon {
  width: 60px;
  height: 60px;
  line-height: 60px;
  background-color: #f0f9eb;
  color: #67c23a;
  font-size: 30px;
  border-radius: 50%;
  margin: 0 auto 20px;
}

.success-message h3 {
  font-size: 22px;
  color: #303133;
  margin: 0 0 15px 0;
}

.success-message p {
  color: #606266;
  margin: 0 0 20px 0;
}

.success-actions {
  margin-top: 20px;
}

.project-plan-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 30px;
}

.project-plan-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-line;
  color: #606266;
  line-height: 1.6;
}

.plan-review-hint {
  margin-top: 15px;
  padding: 10px 15px;
  background-color: #ecf8ff;
  border-left: 4px solid #50bfff;
  color: #5e6d82;
  border-radius: 0 4px 4px 0;
}

@media (max-width: 768px) {
  .project-meta {
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

.final-submission-option {
  margin-top: 15px;
  padding: 15px;
  background-color: #fff8f0;
  border-left: 4px solid #e6a23c;
  border-radius: 4px;
}

.checkbox-container {
  display: flex;
  align-items: center;
}

.checkbox-label {
  margin-left: 10px;
  cursor: pointer;
  font-weight: normal;
}

.final-submission-tag {
  display: inline-block;
  margin-left: 10px;
  padding: 2px 8px;
  background-color: #e6a23c;
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.final-submission-tag-small {
  display: inline-block;
  margin-left: 5px;
  margin-right: 5px;
  padding: 1px 4px;
  background-color: #e6a23c;
  color: white;
  border-radius: 2px;
  font-size: 10px;
  font-weight: bold;
}
</style>
