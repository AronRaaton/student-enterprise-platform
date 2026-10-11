<template>
  <div class="project-detail-container module-page module-detail">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">项目详情</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 项目详情 -->
    <div v-else class="project-content">
      <!-- 学生参与者视图 - 仅对产学研项目显示完整的参与者视图 -->
      <div v-if="isStudent && isParticipant && showParticipantView && project.collaborationType === '产学研合作'" class="participant-view">
        <div class="participant-header">
          <h2 class="project-title">{{ project.title }}</h2>
          <button class="toggle-view-btn" @click="showParticipantView = false">切换到详情视图</button>

          <!-- 添加与企业交流的按钮 -->
          <button class="action-btn chat-btn" @click="chatWithEnterprise" v-if="project.enterpriseId">
            <span class="chat-icon">💬</span> 与企业交流
          </button>
        </div>

        <div class="step-navigation">
          <div
            v-for="step in 4"
            :key="step"
            class="step-nav-item"
            :class="{
              'active': project.currentStep === step,
              'completed': project.currentStep > step,
              'pending': project.currentStep < step
            }"
          >
            <div class="step-number">{{ step }}</div>
            <div class="step-info">
              <h4>{{ getStepTitle(step) }}</h4>
              <p>{{ getStepDescription(step) }}</p>

              <div v-if="project.currentStep >= step" class="step-status">
                <span v-if="studentStepSubmissions[step]" :class="getStatusClass(studentStepSubmissions[step].status)">
                  {{ getStatusText(studentStepSubmissions[step].status) }}
                </span>
                <span v-else class="status-not-submitted">未提交</span>
              </div>

              <div class="step-actions">
                <button
                  v-if="project.currentStep >= step"
                  class="action-btn submit-step-btn highlight-btn"
                  @click="submitStep(step)"
                >
                  {{ getStepSubmissionText(step, studentStepSubmissions[step]) }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 如果是第2步，显示项目计划书 -->
        <div v-if="project.currentStep === 2 && project.projectPlan" class="project-plan-section">
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
                <a @click="downloadFile(project.projectPlanFile)" class="action-btn download-btn" style="cursor: pointer">
                  <span class="action-icon">⬇️</span> 下载
                </a>
                <a v-if="isViewableFile(project.projectPlanFile)" @click="viewFile(project.projectPlanFile)" class="action-btn view-btn" style="cursor: pointer">
                  <span class="action-icon">🔍</span> 查看
                </a>
              </div>
            </div>
          </div>
        </div>

        <div class="back-to-details">
          <button class="action-btn" @click="showParticipantView = false">返回项目详情</button>
        </div>
      </div>

      <!-- 正常项目详情视图 -->
      <div v-else>
        <div class="project-header">
          <h2 class="project-title">{{ project.title }}</h2>
          <span class="project-status" :class="getStatusClass(project.status)">
            {{ getStatusText(project.status) }}
          </span>
        </div>

        <!-- 如果是已参与的学生，显示参与者提示和操作按钮 -->
        <div v-if="isStudent && isParticipant" class="participant-notification">
          <div class="notification-message">
            <span class="notification-icon">✅</span>
            <span>您已经成功参与该项目</span>
          </div>

          <div class="view-toggle">
            <!-- 产学研项目显示切换视图按钮 -->
            <div v-if="project.collaborationType === '产学研合作'" class="toggle-buttons">
              <button class="toggle-view-btn" @click="showParticipantView = true">
                <span class="view-icon">📈</span> 进入项目工作区
              </button>
            </div>

            <!-- 添加与企业交流的按钮 -->
            <button class="action-btn chat-btn" @click="chatWithEnterprise" v-if="project.enterpriseId">
              <span class="chat-icon">💬</span> 与企业交流
            </button>
          </div>
        </div>

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
          <span class="meta-label">发布时间:</span>
          <span class="meta-value">{{ formatDate(project.createTime) }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">开始时间:</span>
          <span class="meta-value">{{ project.startDate ? formatDate(project.startDate) : '未设置' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">结束时间:</span>
          <span class="meta-value">{{ project.endDate ? formatDate(project.endDate) : '未设置' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">最大参与人数:</span>
          <span class="meta-value">{{ project.maxParticipants }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-label">已批准人数:</span>
          <span class="meta-value">{{ project.approvedCount || 0 }}</span>
        </div>
        <div v-if="project.collaborationType === '产学研合作'" class="meta-item">
          <span class="meta-label">当前步骤:</span>
          <span class="meta-value">第 {{ project.currentStep }} 步: {{ getStepTitle(project.currentStep) }}</span>
        </div>
      </div>

      <div class="project-section">
        <h3 class="section-title">项目描述</h3>
        <div class="section-content">
          <p>{{ project.description }}</p>
        </div>
      </div>

      <div class="project-section">
        <h3 class="section-title">所需技能</h3>
        <div class="section-content">
          <p>{{ project.requiredSkills }}</p>
        </div>
      </div>

      <!-- 产学研项目步骤显示 -->
      <div v-if="project.collaborationType === '产学研合作'" class="project-steps">
        <h3 class="section-title">项目步骤</h3>

        <div class="steps-container">
          <div
            v-for="step in 4"
            :key="step"
            class="step-item"
            :class="{
              'active': project.currentStep === step,
              'completed': project.currentStep > step,
              'pending': project.currentStep < step
            }"
          >
            <div class="step-number">{{ step }}</div>
            <div class="step-content">
              <h4>{{ getStepTitle(step) }}</h4>
              <p>{{ getStepDescription(step) }}</p>

              <!-- 学生视图 - 已参与项目的学生可以提交步骤 -->
              <div v-if="isStudent && isParticipant && project.currentStep >= step" class="step-actions">
                <button
                  class="action-btn submit-step-btn highlight-btn"
                  @click="submitStep(step)"
                >
                  {{ getStepSubmissionText(step, studentStepSubmissions[step]) }}
                </button>
              </div>

              <!-- 企业视图 - 第2步上传计划书 -->
              <div v-if="isEnterprise && project.currentStep === 2 && step === 2" class="step-actions">
                <button
                  class="action-btn upload-plan-btn"
                  @click="uploadProjectPlan"
                >
                  {{ project.projectPlan ? '修改计划书' : '上传计划书' }}
                </button>
              </div>

              <!-- 企业视图 - 查看学生提交 -->
              <div v-if="isEnterprise && project.currentStep >= step && stepSubmissions[step] && stepSubmissions[step].length > 0" class="step-actions">
                <button
                  class="action-btn view-submissions-btn"
                  @click="viewStepSubmissions(step)"
                >
                  查看学生提交 ({{ stepSubmissions[step].length }})
                </button>
              </div>
            </div>
          </div>
        </div>


      </div>

      <!-- 学生视图 - 申请按钮或项目操作按钮 -->
      <div v-if="isStudent" class="project-actions">
        <!-- 未参与项目的申请按钮 -->
        <button
          v-if="project.status === 'OPEN' && !isParticipant"
          class="action-btn apply-btn"
          @click="applyForProject"
          :disabled="hasApplied"
        >
          <span v-if="hasApplied">已申请</span>
          <span v-else-if="isRejected">重新申请</span>
          <span v-else>申请参与项目</span>
        </button>

        <!-- 已参与项目的操作按钮 -->
        <div v-if="isParticipant" class="participant-actions">
          <!-- 如果是产学研项目，显示切换视图按钮 -->
          <button
            v-if="project.collaborationType === '产学研合作'"
            class="action-btn toggle-view-btn-large"
            @click="showParticipantView = !showParticipantView"
          >
            {{ showParticipantView ? '返回项目详情' : '进入项目工作区' }}
          </button>

          <!-- 实训基地项目的操作 -->
          <div v-if="project.collaborationType && (project.collaborationType.includes('实训基地') || project.collaborationType === '实训基地')" class="training-base-actions">
            <!-- 进入实训基地详情页面按钮 -->
            <button
              class="action-btn training-base-btn"
              @click="enterTrainingBaseDetail"
            >
              <span class="training-base-icon">🏫</span> 进入实训基地
            </button>

            <!-- 如果项目已结束，显示上传实训报告按钮 -->
            <button
              v-if="isProjectEnded"
              class="action-btn upload-report-btn"
              @click="uploadTrainingReport"
            >
              <span class="upload-icon">📁</span> 上传实训报告
            </button>
            <!-- 如果项目未结束，显示提示信息 -->
            <div v-else class="training-not-ended-message">
              <span class="info-icon">ℹ️</span> 实训还未结束，请结束后再来
            </div>
          </div>

          <!-- 订单培养项目的操作 -->
          <div v-if="project.collaborationType === '订单培养'" class="order-training-actions">
            <button
              class="action-btn order-training-btn"
              @click="enterOrderTraining"
            >
              <span class="order-training-icon">📚</span> 进入订单培养项目
            </button>
          </div>

          <!-- 与企业交流按钮 -->
          <button
            class="action-btn chat-btn-large"
            @click="chatWithEnterprise"
            v-if="project.enterpriseId"
          >
            <span class="chat-icon">💬</span> 与企业交流
          </button>
        </div>
      </div>

      <!-- 企业视图 - 申请列表 -->
      <div v-if="isEnterprise && activeTab === 'applications'" class="project-applications">
        <h3 class="section-title">申请列表</h3>

        <div v-if="loadingApplications" class="loading-container">
          <div class="loading-spinner"></div>
          <p>加载申请列表中...</p>
        </div>

        <div v-else-if="applications.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p>暂无学生申请</p>
        </div>

        <div v-else class="applications-list">
          <div v-for="application in applications" :key="application.applicationId" class="application-card">
            <div class="application-header">
              <div class="student-info">
                <div class="student-avatar" @click="viewStudentDetails(application.studentId)" title="点击查看学生详情">
                  {{ application.studentName ? application.studentName.charAt(0) : 'S' }}
                </div>
                <div class="student-details">
                  <h4 class="student-name" @click="viewStudentDetails(application.studentId)" title="点击查看学生详情" style="cursor: pointer">
                    {{ application.studentName }}
                  </h4>
                  <p class="student-school">{{ application.studentSchool }}</p>
                </div>
              </div>
              <span class="application-status" :class="getApplicationStatusClass(application.status)">
                {{ getApplicationStatusText(application.status) }}
              </span>
            </div>

            <div class="application-content">
              <h4>申请理由:</h4>
              <p>{{ application.applicationReason }}</p>

              <!-- 显示上传的文件 -->
              <div v-if="application.uploadedFile" class="uploaded-file-section">
                <h4>上传的文件:</h4>
                <div class="file-preview">
                  <div class="file-info">
                    <span class="file-icon">📄</span>
                    <span class="file-name">{{ getFileName(application.uploadedFile) }}</span>
                  </div>
                  <div class="file-actions">
                    <a @click="downloadFile(application.uploadedFile)" class="action-btn download-btn" style="cursor: pointer">
                      <span class="action-icon">⬇️</span> 下载
                    </a>
                    <a v-if="isViewableFile(application.uploadedFile)" @click="viewFile(application.uploadedFile)" class="action-btn view-btn" style="cursor: pointer">
                      <span class="action-icon">🔍</span> 查看
                    </a>
                  </div>
                </div>
              </div>

              <!-- 显示备注信息 -->
              <div v-if="application.notes" class="notes-section">
                <h4>备注信息:</h4>
                <p>{{ application.notes }}</p>
              </div>

              <!-- 显示相关经验 -->
              <div v-if="application.relevantExperience" class="experience-section">
                <h4>相关经验:</h4>
                <p>{{ application.relevantExperience }}</p>
              </div>

              <!-- 显示联系方式 -->
              <div v-if="application.contactInfo" class="contact-section">
                <h4>联系方式:</h4>
                <p>{{ application.contactInfo }}</p>
              </div>
            </div>

            <div class="application-meta">
              <span class="application-date">申请时间: {{ formatDate(application.applyTime) }}</span>
            </div>

            <div v-if="application.status === 'PENDING'" class="application-actions">
              <button class="action-btn approve-btn" @click="approveApplication(application.applicationId)">批准</button>
              <button class="action-btn reject-btn" @click="rejectApplication(application.applicationId)">拒绝</button>
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

export default {
  data() {
    return {
      project: {},
      loading: true,
      loadingApplications: false,
      applications: [],
      userRole: localStorage.getItem('userRole') || '',
      userId: localStorage.getItem('userId'),
      hasApplied: false,
      isRejected: false,
      isParticipant: false,
      showParticipantView: false, // 是否显示参与者视图
      activeTab: this.$route.query.tab || 'details',
      stepSubmissions: {
        1: [],
        2: [],
        3: [],
        4: []
      },
      studentStepSubmissions: {
        1: null,
        2: null,
        3: null,
        4: null
      }
    };
  },
  computed: {
    isStudent() {
      return this.userRole === 'student';
    },
    isEnterprise() {
      return this.userRole === 'enterprise';
    },
    projectId() {
      return this.$route.params.projectId;
    },
    isProjectEnded() {
      // 检查项目是否已结束（当前日期大于结束日期）
      if (!this.project.endDate) return false;
      const today = new Date();
      const endDate = new Date(this.project.endDate);
      return today > endDate;
    }
  },
  created() {
    this.fetchProjectDetails();

    if (this.isStudent) {
      this.checkIfApplied();

      // 如果是从学生项目列表页面跳转过来的已参与项目
      if (this.$route.query.participating === 'true') {
        this.showParticipantView = true;
      }
    } else if (this.isEnterprise && this.activeTab === 'applications') {
      this.fetchApplications();
    }
  },

  // 添加watch以确保在isParticipant变化时更新UI
  watch: {
    isParticipant(newValue) {
      // 如果学生成为参与者
      if (newValue) {
        // 如果是从项目列表的“进入项目”按钮跳转来的，自动显示参与者视图
        if (this.$route.query.participating === 'true') {
          this.showParticipantView = true;
        }

        // 如果是产学研项目，获取学生的步骤提交
        if (this.project.collaborationType === '产学研合作') {
          this.fetchStudentStepSubmissions();
        }
      }
    },
    // 监听项目数据变化，确保在项目数据加载后正确设置参与者状态
    project: {
      handler(newProject) {
        if (newProject && Object.keys(newProject).length > 0 && this.isStudent) {
          this.checkIfParticipant();
        }
      },
      deep: true
    }
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

        // 调试信息，显示项目类型
        console.log('项目类型:', this.project.collaborationType);

        // 如果是产学研项目且是企业用户，获取所有步骤提交
        if (this.project.collaborationType === '产学研合作' && this.isEnterprise) {
          this.fetchStepSubmissions();
        }

        // 注意：对于学生用户，我们不在这里调用checkIfParticipant
        // 而是通过project watcher来触发，以确保正确的执行顺序
      } catch (error) {
        console.error('获取项目详情失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async checkIfApplied() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/applications/student/${this.userId}`);
        // 找到当前项目的申请
        const application = response.data.find(app => app.projectId === parseInt(this.projectId));

        if (application) {
          // 如果申请存在且状态为待审核或已批准，则不能再次申请
          this.hasApplied = application.status === 'PENDING' || application.status === 'APPROVED';

          // 如果申请被拒绝，显示重新申请按钮
          this.isRejected = application.status === 'REJECTED';
        } else {
          this.hasApplied = false;
          this.isRejected = false;
        }
      } catch (error) {
        console.error('检查申请状态失败:', error);
      }
    },
    async fetchApplications() {
      this.loadingApplications = true;

      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/applications`);
        this.applications = response.data;
      } catch (error) {
        console.error('获取申请列表失败:', error);
      } finally {
        this.loadingApplications = false;
      }
    },
    applyForProject() {
      if (this.hasApplied) {
        return;
      }

      this.$router.push({
        name: 'ProjectApplication',
        params: { projectId: this.projectId }
      });
    },
    async approveApplication(applicationId) {
      try {
        await axios.put(`http://localhost:8080/api/projects/applications/${applicationId}/review`, {
          feedback: '申请已通过，欢迎加入项目团队！'
        }, {
          params: { status: 'APPROVED' }
        });

        // 更新申请列表
        this.fetchApplications();
        // 更新项目详情
        this.fetchProjectDetails();
      } catch (error) {
        console.error('批准申请失败:', error);
        alert('批准申请失败，请重试');
      }
    },
    async rejectApplication(applicationId) {
      const feedback = prompt('请输入拒绝理由:');
      if (feedback === null) return;

      try {
        await axios.put(`http://localhost:8080/api/projects/applications/${applicationId}/review`, {
          feedback
        }, {
          params: { status: 'REJECTED' }
        });

        // 更新申请列表
        this.fetchApplications();
      } catch (error) {
        console.error('拒绝申请失败:', error);
        alert('拒绝申请失败，请重试');
      }
    },

    // 查看学生详情
    viewStudentDetails(studentId) {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId }
      });
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

    // 查看文件（预览）
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
    },
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    getStatusClass(status) {
      switch (status) {
        case 'OPEN':
          return 'status-open';
        case 'CLOSED':
          return 'status-closed';
        case 'COMPLETED':
          return 'status-completed';
        default:
          return '';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'OPEN':
          return '开放中';
        case 'CLOSED':
          return '已关闭';
        case 'COMPLETED':
          return '已完成';
        default:
          return '未知状态';
      }
    },
    getApplicationStatusClass(status) {
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
    getApplicationStatusText(status) {
      switch (status) {
        case 'PENDING':
          return '待审核';
        case 'APPROVED':
          return '已批准';
        case 'REJECTED':
          return '已拒绝';
        default:
          return '未知状态';
      }
    },

    // 检查学生是否参与了项目
    async checkIfParticipant() {
      try {
        console.log('正在检查学生参与状态...');
        const response = await axios.get(`http://localhost:8080/api/projects/applications/student/${this.userId}`);
        // 找到当前项目的申请
        const application = response.data.find(app => app.projectId === parseInt(this.projectId));

        // 如果申请存在且状态为已批准，则是参与者
        const wasParticipant = this.isParticipant; // 保存之前的状态
        this.isParticipant = application && application.status === 'APPROVED';

        console.log('学生参与状态:', this.isParticipant);
        console.log('项目类型:', this.project.collaborationType);

        // 如果参与状态没有变化，手动触发watcher
        if (wasParticipant === this.isParticipant && this.isParticipant) {
          // 如果是产学研项目，获取学生的步骤提交
          if (this.project.collaborationType === '产学研合作') {
            this.fetchStudentStepSubmissions();
          }

          // 如果是从项目列表的“进入项目”按钮跳转来的，自动显示参与者视图
          if (this.$route.query.participating === 'true') {
            this.showParticipantView = true;
          }
        }
      } catch (error) {
        console.error('检查参与状态失败:', error);
      }
    },

    // 获取所有学生的步骤提交
    async fetchStepSubmissions() {
      try {
        for (let step = 1; step <= 4; step++) {
          if (this.project.currentStep >= step) {
            const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/steps/${step}/submissions`);
            this.stepSubmissions[step] = response.data;
          }
        }
      } catch (error) {
        console.error('获取步骤提交失败:', error);
      }
    },

    // 获取当前学生的步骤提交
    async fetchStudentStepSubmissions() {
      try {
        for (let step = 1; step <= 4; step++) {
          if (this.project.currentStep >= step) {
            try {
              const response = await axios.get(
                `http://localhost:8080/api/projects/${this.projectId}/steps/${step}/submissions/${this.userId}`
              );
              this.studentStepSubmissions[step] = response.data;
            } catch (error) {
              // 如果没有找到提交记录，不做任何处理
              console.log(`没有找到第${step}步的提交记录`);
            }
          }
        }
      } catch (error) {
        console.error('获取学生步骤提交失败:', error);
      }
    },

    // 提交步骤
    submitStep(stepNumber) {
      // 如果是第2步，则导航到计划书审阅页面
      if (stepNumber === 2) {
        this.$router.push({
          name: 'ProjectPlanReview',
          params: {
            projectId: this.projectId
          }
        });
      } else {
        this.$router.push({
          name: 'ProjectStepSubmission',
          params: {
            projectId: this.projectId,
            stepNumber: stepNumber
          }
        });
      }
    },

    // 上传项目计划书
    uploadProjectPlan() {
      this.$router.push({
        name: 'ProjectPlanUpload',
        params: { projectId: this.projectId }
      });
    },

    // 查看学生提交
    viewStepSubmissions(stepNumber) {
      this.$router.push({
        name: 'ProjectStepSubmissions',
        params: {
          projectId: this.projectId,
          stepNumber: stepNumber
        }
      });
    },

    // 审核学生提交
    reviewStepSubmission(stepNumber, studentId) {
      this.$router.push({
        name: 'ProjectStepReview',
        params: {
          projectId: this.projectId,
          stepNumber: stepNumber,
          studentId: studentId
        }
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
          return '明确企业技术需求、市场痛点或科研机构的技术供给方向，通过调研、行业分析或技术研讨确定合作目标。';
        case 2:
          return '制定合作计划，整合资金、设备、人才等资源，明确各方角色分工。';
        case 3:
          return '按计划推进研发、试验或生产，企业提供试验场地和市场反馈，学生负责技术攻关。';
        case 4:
          return '将技术成果转化为产品或服务，推向市场并验证商业化价值。';
        default:
          return '未知步骤描述';
      }
    },

    // 获取步骤提交按钮文本
    getStepSubmissionText(stepNumber, submission) {
      // 如果是第2步，显示审阅计划书
      if (stepNumber === 2) {
        if (!submission) {
          return '审阅计划书';
        }

        switch (submission.status) {
          case 'PENDING':
            return '审阅中';
          case 'APPROVED':
            return '已同意计划';
          case 'REJECTED':
            return '已拒绝计划';
          default:
            return '审阅计划书';
        }
      } else {
        // 其他步骤的文本
        if (!submission) {
          return '提交内容';
        }

        switch (submission.status) {
          case 'PENDING':
            return '已提交，待审核';
          case 'APPROVED':
            return '已通过';
          case 'REJECTED':
            return '重新提交';
          default:
            return '提交内容';
        }
      }
    },

    // 获取进度条宽度
    getProgressWidth(status) {
      switch (status) {
        case 'PENDING':
          return '50%';
        case 'APPROVED':
          return '100%';
        case 'REJECTED':
          return '30%';
        default:
          return '0%';
      }
    },
    chatWithEnterprise() {
      if (!this.project.enterpriseId) {
        alert('无法获取企业信息，请稍后再试');
        return;
      }

      // 跳转到聊天页面
      this.$router.push({
        name: 'ChatS2E',
        params: { enterpriseId: this.project.enterpriseId }
      });
    },

    // 上传实训报告
    uploadTrainingReport() {
      // 跳转到实训报告上传页面
      this.$router.push({
        name: 'TrainingReportUpload',
        params: { projectId: this.projectId }
      });
    },

    // 进入实训基地详情页面
    enterTrainingBaseDetail() {
      this.$router.push({
        name: 'TrainingBaseDetail',
        params: { projectId: this.projectId }
      });
    },

    // 进入订单培养项目
    enterOrderTraining() {
      // 跳转到订单培养项目详情页面
      this.$router.push({
        name: 'OrderTrainingDetail',
        params: { projectId: this.projectId }
      });
    }
  }
};
</script>

<style scoped>
.project-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

/* 产学研项目步骤样式 */
.project-steps {
  margin-bottom: 30px;
}

/* 参与者视图样式 */
.participant-view {
  margin-bottom: 30px;
}

.participant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.view-toggle {
  margin: 10px 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toggle-view-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.toggle-view-btn:hover {
  background-color: #66b1ff;
}

.step-navigation {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.step-nav-item {
  display: flex;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s;
}

.step-nav-item.active {
  border-left: 4px solid #409EFF;
}

.step-nav-item.completed {
  border-left: 4px solid #67c23a;
}

.step-nav-item.pending {
  opacity: 0.7;
}

.step-status {
  margin-top: 10px;
  font-weight: 500;
}

.status-not-submitted {
  color: #909399;
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

.file-section {
  margin-top: 20px;
}

.file-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.back-to-details {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.steps-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

.step-item {
  display: flex;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s;
}

.step-item.active {
  border-left: 4px solid #409EFF;
}

.step-item.completed {
  border-left: 4px solid #67c23a;
}

.step-item.pending {
  opacity: 0.7;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-right: 20px;
  flex-shrink: 0;
}

.active .step-number {
  background-color: #409EFF;
  color: white;
}

.completed .step-number {
  background-color: #67c23a;
  color: white;
}

.pending .step-number {
  background-color: #f5f7fa;
  color: #909399;
}

.step-content {
  flex: 1;
}

.step-content h4 {
  font-size: 18px;
  color: #303133;
  margin: 0 0 10px 0;
}

.step-content p {
  color: #606266;
  margin: 0 0 15px 0;
  line-height: 1.6;
}

.step-actions {
  margin-top: 15px;
}

.submit-step-btn {
  background-color: #409EFF;
  color: white;
}

.submit-step-btn:hover {
  background-color: #66b1ff;
}

.upload-plan-btn {
  background-color: #67c23a;
  color: white;
}

.upload-plan-btn:hover {
  background-color: #85ce61;
}

.view-submissions-btn {
  background-color: #909399;
  color: white;
}

.view-submissions-btn:hover {
  background-color: #a6a9ad;
}

/* 甲特图样式 */
.gantt-chart-section {
  margin-top: 30px;
}

.gantt-chart {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.gantt-header {
  display: grid;
  grid-template-columns: 150px 1fr 100px 100px;
  background-color: #f5f7fa;
  padding: 12px 15px;
  font-weight: bold;
  color: #606266;
}

.gantt-row {
  display: grid;
  grid-template-columns: 150px 1fr 100px 100px;
  padding: 12px 15px;
  border-top: 1px solid #ebeef5;
  align-items: center;
}

.gantt-student {
  font-weight: 500;
  color: #303133;
}

.progress-bar {
  height: 10px;
  background-color: #f5f7fa;
  border-radius: 5px;
  overflow: hidden;
  width: 100%;
}

.progress-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s;
}

.progress-fill.status-pending {
  background-color: #e6a23c;
}

.progress-fill.status-approved {
  background-color: #67c23a;
}

.progress-fill.status-rejected {
  background-color: #f56c6c;
}

.gantt-status {
  font-weight: 500;
  text-align: center;
}

.gantt-status.status-pending {
  color: #e6a23c;
}

.gantt-status.status-approved {
  color: #67c23a;
}

.gantt-status.status-rejected {
  color: #f56c6c;
}

.gantt-actions {
  text-align: center;
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

/* 项目内容 */
.project-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.project-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.project-status {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.status-open {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-completed {
  background-color: #ecf5ff;
  color: #409EFF;
}

.project-meta {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
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

.project-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
}

.section-content {
  color: #606266;
  line-height: 1.6;
}

.project-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.action-btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.apply-btn {
  background-color: #409EFF;
  color: white;
  border: none;
}

.apply-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.apply-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 申请列表 */
.project-applications {
  margin-top: 30px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #909399;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.application-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.student-info {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.student-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.student-details {
  display: flex;
  flex-direction: column;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.student-school {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.application-status {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 14px;
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

.application-content {
  margin-bottom: 15px;
}

.application-content h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.application-content p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

/* 文件预览样式 */
.uploaded-file-section,
.notes-section,
.experience-section,
.contact-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eaeaea;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f2f5;
  padding: 10px 15px;
  border-radius: 6px;
  margin-top: 8px;
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

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  text-decoration: none;
  transition: background-color 0.3s;
}

.download-btn {
  background-color: #ecf5ff;
  color: #409EFF;
}

.download-btn:hover {
  background-color: #d9ecff;
}

.view-btn {
  background-color: #f0f9eb;
  color: #67c23a;
}

.view-btn:hover {
  background-color: #e1f3d8;
}

.action-icon {
  margin-right: 5px;
}

.application-meta {
  margin-bottom: 15px;
  font-size: 14px;
  color: #909399;
}

.application-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.approve-btn {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.approve-btn:hover {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.reject-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
}

.reject-btn:hover {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.highlight-btn {
  background-color: #4caf50;
  color: white;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: all 0.3s ease;
}

.highlight-btn:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.chat-btn {
  background-color: #2196F3;
  color: white;
  margin-left: 10px;
}

.chat-btn-large {
  background-color: #2196F3;
  color: white;
  font-size: 16px;
  padding: 10px 20px;
  margin: 0 10px;
}

.chat-icon {
  margin-right: 5px;
}

.toggle-view-btn-large {
  background-color: #409EFF;
  color: white;
  font-size: 16px;
  padding: 10px 20px;
  margin: 0 10px;
}

.participant-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.toggle-buttons {
  display: flex;
  margin-right: 15px;
}

.participant-notification {
  background-color: #f0f9eb;
  border-radius: 8px;
  padding: 15px;
  margin: 15px 0;
  border-left: 4px solid #67c23a;
}

.notification-message {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  color: #67c23a;
  font-weight: 500;
}

.notification-icon {
  font-size: 20px;
  margin-right: 10px;
}

.view-icon {
  margin-right: 5px;
}

/* 实训基地项目相关样式 */
.training-base-actions {
  display: flex;
  align-items: center;
  margin: 0 15px;
}

.upload-report-btn {
  display: flex;
  align-items: center;
  background-color: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-report-btn:hover {
  background-color: #85ce61;
}

.upload-icon {
  margin-right: 5px;
}

.training-base-btn {
  display: flex;
  align-items: center;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 15px;
  margin-right: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.training-base-btn:hover {
  background-color: #66b1ff;
}

.training-base-icon {
  margin-right: 5px;
}

.training-not-ended-message {
  display: flex;
  align-items: center;
  background-color: #f4f4f5;
  border-radius: 4px;
  padding: 8px 15px;
  color: #909399;
}

.info-icon {
  margin-right: 5px;
  font-size: 16px;
}

/* 订单培养项目相关样式 */
.order-training-actions {
  display: flex;
  align-items: center;
  margin: 0 15px;
}

.order-training-btn {
  display: flex;
  align-items: center;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-training-btn:hover {
  background-color: #66b1ff;
}

.order-training-icon {
  margin-right: 5px;
}
</style>
