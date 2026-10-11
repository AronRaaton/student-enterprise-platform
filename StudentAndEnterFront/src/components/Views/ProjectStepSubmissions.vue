<template>
  <div class="step-submissions-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">第{{ stepNumber }}步学生提交列表</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 项目信息 -->
    <div v-else class="submissions-content">
      <div class="project-info">
        <h2 class="project-title">{{ project.title }}</h2>
        <div class="project-meta">
          <div class="meta-item">
            <span class="meta-label">项目类型:</span>
            <span class="meta-value">{{ project.collaborationType }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">当前步骤:</span>
            <span class="meta-value">{{ project.currentStep }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">参与学生:</span>
            <span class="meta-value">{{ project.approvedCount }}/{{ project.maxParticipants }}</span>
          </div>
        </div>
      </div>

      <!-- 步骤信息 -->
      <div class="step-info">
        <h3 class="step-title">{{ getStepTitle(stepNumber) }}</h3>
        <p class="step-description">{{ getStepDescription(stepNumber) }}</p>
      </div>

      <!-- 筛选器 -->
      <div class="filter-section">
        <div class="filter-group">
          <label>状态筛选:</label>
          <div class="filter-buttons">
            <button
              class="filter-btn"
              :class="{ active: currentStatusFilter === null }"
              @click="currentStatusFilter = null"
            >
              全部
            </button>
            <button
              class="filter-btn"
              :class="{ active: currentStatusFilter === 'PENDING' }"
              @click="currentStatusFilter = 'PENDING'"
            >
              待审核
            </button>
            <button
              class="filter-btn"
              :class="{ active: currentStatusFilter === 'APPROVED' }"
              @click="currentStatusFilter = 'APPROVED'"
            >
              已通过
            </button>
            <button
              class="filter-btn"
              :class="{ active: currentStatusFilter === 'REJECTED' }"
              @click="currentStatusFilter = 'REJECTED'"
            >
              已拒绝
            </button>
          </div>
        </div>
      </div>

      <!-- 甘特图（第3步自动显示） -->
      <div v-if="stepNumber === 3" class="gantt-chart-container">
        <h3 class="section-title">学生提交时间甘特图</h3>
        <p class="gantt-instruction">点击条形可查看提交详情</p>
        <div class="gantt-chart">
          <div class="gantt-header">
            <div class="gantt-header-left">学生</div>
            <div class="gantt-header-right">
              <div class="gantt-timeline">
                <div v-for="(date, index) in timelineLabels" :key="index" class="gantt-time-label">
                  {{ date }}
                </div>
              </div>
            </div>
          </div>
          <div class="gantt-body">
            <div v-for="student in timeGanttData" :key="student.studentId" class="gantt-row">
              <div class="gantt-row-left">{{ student.studentName }}</div>
              <div class="gantt-row-right">
                <div
                  v-for="submission in student.submissions"
                  :key="submission.submissionId"
                  class="gantt-bar"
                  :class="getStatusClass(submission.status)"
                  :style="getSubmissionBarStyle(submission)"
                  :title="formatDate(submission.submissionTime) + (submission.briefDescription ? ' - ' + submission.briefDescription : '')"
                  @click="viewSubmissionDetails(submission)"
                >
                  {{ submission.briefDescription || '提交' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredSubmissions.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无学生提交</h3>
        <p>当前步骤还没有学生提交内容</p>
      </div>

      <!-- 当不是第3步时显示提交列表 -->
      <div v-else-if="stepNumber !== 3" class="submissions-list">
        <div v-for="submission in filteredSubmissions" :key="submission.submissionId" class="submission-card">
          <div class="submission-header">
            <div class="student-info" @click="viewStudentDetails(submission.studentId)">
              <div class="student-avatar">{{ submission.studentName ? submission.studentName.charAt(0) : 'S' }}</div>
              <div class="student-details">
                <h3 class="student-name">{{ submission.studentName || '未知学生' }}</h3>
                <p class="student-school">{{ submission.studentSchool || '未知学校' }}</p>
              </div>
            </div>
            <div class="submission-meta">
              <div class="submission-time">
                提交时间: {{ formatDate(submission.submissionTime) }}
              </div>
              <div class="submission-status" :class="getStatusClass(submission.status)">
                {{ getStatusText(submission.status) }}
              </div>
            </div>
          </div>
          <div class="submission-content">
            <div class="content-section">
              <h4>提交内容:</h4>
              <p>{{ submission.content }}</p>
            </div>

            <div v-if="submission.uploadedFile" class="file-section">
              <h4>上传的文件:</h4>
              <div class="file-preview">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ getFileName(submission.uploadedFile) }}</span>
                </div>
                <div class="file-actions">
                  <button @click="downloadFile(submission.uploadedFile)" class="action-btn download-btn">
                    <span class="action-icon">⬇️</span> 下载
                  </button>
                  <button v-if="isViewableFile(submission.uploadedFile)" @click="viewFile(submission.uploadedFile)" class="action-btn view-btn">
                    <span class="action-icon">🔍</span> 查看
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="submission-actions">
            <button
              class="action-btn review-btn"
              @click="reviewSubmission(submission.studentId)"
            >
              {{ submission.status === 'PENDING' ? '审核提交' : '查看详情' }}
            </button>
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
      submissions: [],
      loading: true,
      currentStatusFilter: null,
      ganttData: [], // 步骤完成甘特图数据
      timeGanttData: [], // 时间轴甘特图数据
      timelineLabels: [], // 时间轴标签
    };
  },
  computed: {
    projectId() {
      return this.$route.params.projectId;
    },
    stepNumber() {
      return parseInt(this.$route.params.stepNumber);
    },
    filteredSubmissions() {
      if (this.currentStatusFilter === null) {
        return this.submissions;
      }
      return this.submissions.filter(submission => submission.status === this.currentStatusFilter);
    }
  },
  created() {
    this.fetchProjectDetails();
    this.fetchStepSubmissions();
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
    async fetchStepSubmissions() {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/${this.stepNumber}/submissions`
        );
        this.submissions = response.data;

        // 如果是第3步，准备甘特图数据
        if (this.stepNumber === 3) {
          this.prepareGanttData();
          await this.prepareTimeGanttData(); // 等待异步方法完成
        }
      } catch (error) {
        console.error('获取步骤提交列表失败:', error);
        alert('获取步骤提交列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },



    // 准备步骤完成甘特图数据
    prepareGanttData() {
      // 收集所有学生的数据
      const studentMap = new Map();

      // 首先将当前步骤的提交添加到数据中
      this.submissions.forEach(submission => {
        if (!studentMap.has(submission.studentId)) {
          studentMap.set(submission.studentId, {
            studentId: submission.studentId,
            studentName: submission.studentName,
            completedSteps: [],
            currentStep: this.stepNumber
          });
        }

        // 如果已通过，将步骤添加到完成列表
        if (submission.status === 'APPROVED') {
          const studentData = studentMap.get(submission.studentId);
          if (!studentData.completedSteps.includes(this.stepNumber)) {
            studentData.completedSteps.push(this.stepNumber);
          }
        }
      });

      // 获取其他步骤的完成情况
      this.fetchOtherStepsCompletion(studentMap);
    },

    // 准备时间轴甘特图数据
    async prepareTimeGanttData() {
      // 收集所有学生的数据
      const studentMap = new Map();
      let allDates = [];

      // 获取项目的第二阶段结束时间（所有学生共用）
      let projectPhase2EndTime;

      try {
        // 获取第二阶段的所有提交
        const step2Response = await axios.get(
          `http://localhost:8080/api/projects/${this.projectId}/steps/2/submissions`
        );

        // 找出最后一个审核通过的第二阶段提交
        let latestApprovedSubmission = null;

        step2Response.data.forEach(submission => {
          if (submission.status === 'APPROVED' && submission.reviewTime) {
            const reviewTime = new Date(submission.reviewTime);
            if (!latestApprovedSubmission || reviewTime > new Date(latestApprovedSubmission.reviewTime)) {
              latestApprovedSubmission = submission;
            }
          }
        });

        // 如果找到了审核通过的提交，使用其审核时间作为第二阶段结束时间
        if (latestApprovedSubmission) {
          projectPhase2EndTime = new Date(latestApprovedSubmission.reviewTime);
        } else {
          // 如果没有找到，使用项目创建时间或当前时间前15天
          projectPhase2EndTime = this.project.createTime
            ? new Date(this.project.createTime)
            : new Date(new Date().getTime() - 15 * 24 * 60 * 60 * 1000);
        }

        // 将第二阶段结束时间添加到时间轴中
        allDates.push(projectPhase2EndTime);

      } catch (error) {
        console.error('获取第二阶段提交失败:', error);
        // 如果出错，使用默认时间
        projectPhase2EndTime = this.project.createTime
          ? new Date(this.project.createTime)
          : new Date(new Date().getTime() - 15 * 24 * 60 * 60 * 1000);
        allDates.push(projectPhase2EndTime);
      }

      // 处理当前步骤的所有提交
      this.submissions.forEach(submission => {
        if (!studentMap.has(submission.studentId)) {
          studentMap.set(submission.studentId, {
            studentId: submission.studentId,
            studentName: submission.studentName,
            submissions: [],
            step3StartTime: projectPhase2EndTime // 所有学生使用相同的第二阶段结束时间
          });
        }

        // 添加提交到学生数据中
        const studentData = studentMap.get(submission.studentId);
        studentData.submissions.push({
          ...submission,
          startTime: null, // 初始化起始时间
          endTime: null    // 初始化结束时间
        });

        // 记录提交时间用于时间轴
        if (submission.submissionTime) {
          allDates.push(new Date(submission.submissionTime));
        }
      });

      // 生成时间轴标签
      if (allDates.length > 0) {
        // 按时间排序所有日期
        allDates.sort((a, b) => a - b);

        // 获取最早和最晚的日期
        const earliestDate = allDates[0];
        const latestDate = allDates[allDates.length - 1];

        // 确保时间轴有足够的跨度
        const oneDay = 24 * 60 * 60 * 1000; // 一天的毫秒数

        // 开始日期提前半天
        const startDate = new Date(earliestDate.getTime() - oneDay / 2);

        // 结束日期延后一天半
        const endDate = new Date(latestDate.getTime() + oneDay * 1.5);

        // 计算时间跨度（天数）
        const daysDiff = Math.ceil((endDate - startDate) / oneDay);

        // 根据天数生成时间轴标签
        this.timelineLabels = this.generateTimelineLabels(startDate, daysDiff);

        console.log(`时间轴范围: ${this.formatDate(startDate)} 至 ${this.formatDate(endDate)}`);
      }

      // 处理每个学生的提交数据
      studentMap.forEach(student => {
        // 按提交时间排序
        student.submissions.sort((a, b) => {
          return new Date(a.submissionTime) - new Date(b.submissionTime);
        });

        // 过滤出有效的提交（必须有提交时间）
        student.submissions = student.submissions.filter(submission => {
          return submission.submissionTime != null;
        });

        // 计算每个提交的起止时间
        if (student.submissions.length > 0) {
          // 获取项目的第二阶段结束时间（所有学生都使用相同的起始时间）
          const phase2EndTime = this.project.currentStep >= 3 ? student.step3StartTime : new Date();
          const oneHour = 60 * 60 * 1000; // 一小时的毫秒数

          // 打印学生信息
          console.log(`\n\n===== 学生: ${student.studentName} (ID: ${student.studentId}) =====`);
          console.log(`第二阶段结束时间: ${this.formatDate(phase2EndTime)}`);
          console.log(`提交数量: ${student.submissions.length}`);

          // 按提交时间再次排序，确保时间顺序正确
          student.submissions.sort((a, b) => {
            return new Date(a.submissionTime) - new Date(b.submissionTime);
          });

          // 第一个提交的起始时间是第二阶段结束时间，结束时间是提交时间
          student.submissions[0].startTime = phase2EndTime;
          student.submissions[0].endTime = new Date(student.submissions[0].submissionTime);

          // 打印第一个提交的时间信息
          console.log(`\n提交 1 (ID: ${student.submissions[0].submissionId})`);
          console.log(`提交时间: ${this.formatDate(student.submissions[0].submissionTime)}`);
          console.log(`横条起始时间: ${this.formatDate(student.submissions[0].startTime)}`);
          console.log(`横条结束时间: ${this.formatDate(student.submissions[0].endTime)}`);
          console.log(`简述: ${student.submissions[0].briefDescription || '无'}`);

          // 记录所有时间点用于时间轴
          allDates.push(phase2EndTime);
          allDates.push(new Date(student.submissions[0].submissionTime));

          // 后续提交的起始时间是前一个提交的结束时间加一小时
          for (let i = 1; i < student.submissions.length; i++) {
            // 前一个提交的结束时间加一小时，作为当前提交的起始时间
            const prevSubmissionTime = new Date(student.submissions[i-1].submissionTime);
            const currentStartTime = new Date(prevSubmissionTime.getTime() + oneHour);
            const currentEndTime = new Date(student.submissions[i].submissionTime);

            // 确保结束时间晚于起始时间
            if (currentEndTime <= currentStartTime) {
              // 如果结束时间不晚于起始时间，将结束时间设置为起始时间加一小时
              currentEndTime.setTime(currentStartTime.getTime() + oneHour);
            }

            student.submissions[i].startTime = currentStartTime;
            student.submissions[i].endTime = currentEndTime;

            // 打印后续提交的时间信息
            console.log(`\n提交 ${i+1} (ID: ${student.submissions[i].submissionId})`);
            console.log(`提交时间: ${this.formatDate(student.submissions[i].submissionTime)}`);
            console.log(`横条起始时间: ${this.formatDate(student.submissions[i].startTime)}`);
            console.log(`横条结束时间: ${this.formatDate(student.submissions[i].endTime)}`);
            console.log(`简述: ${student.submissions[i].briefDescription || '无'}`);

            // 记录时间点用于时间轴
            allDates.push(currentStartTime);
            allDates.push(currentEndTime);
          }
        }
      });

      // 转换为数组
      this.timeGanttData = Array.from(studentMap.values());
    },

    // 生成时间轴标签
    generateTimelineLabels(startDate, days) {
      const labels = [];
      const labelCount = Math.min(15, days); // 增加标签数量至15个，使时间轴更精细

      // 计算标签间隔
      const interval = Math.max(0.5, days / labelCount); // 允许半天的间隔

      // 计算每个标签的时间间隔（毫秒）
      const intervalMs = interval * 24 * 60 * 60 * 1000;

      // 计算标签数量
      const numLabels = Math.ceil(days / interval) + 1;

      for (let i = 0; i < numLabels; i++) {
        const date = new Date(startDate.getTime() + i * intervalMs);
        labels.push(this.formatShortDateTime(date));
      }

      return labels;
    },

    // 格式化短日期时间（显示月/日 时:分）
    formatShortDateTime(date) {
      return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    },

    // 计算提交条的样式
    getSubmissionBarStyle(submission) {
      if (!submission.startTime || !submission.endTime) return {};

      // 在控制台输出横条的计算信息
      console.log(`\n横条计算 - 学生: ${submission.studentName}, 提交ID: ${submission.submissionId}`);
      console.log(`起始时间: ${this.formatDate(submission.startTime)}`);
      console.log(`结束时间: ${this.formatDate(submission.endTime)}`);

      // 如果没有时间轴标签，返回空样式
      if (this.timelineLabels.length === 0) return {};

      // 解析第一个和最后一个标签的日期
      const firstLabelParts = this.timelineLabels[0].split('/');
      const lastLabelParts = this.timelineLabels[this.timelineLabels.length - 1].split('/');

      // 创建日期对象（使用当前年份）
      const currentYear = new Date().getFullYear();
      const chartStartDate = new Date(currentYear, parseInt(firstLabelParts[0]) - 1, parseInt(firstLabelParts[1]));
      const chartEndDate = new Date(currentYear, parseInt(lastLabelParts[0]) - 1, parseInt(lastLabelParts[1]));

      // 计算总时间跨度（毫秒）
      const timeSpan = chartEndDate - chartStartDate;
      if (timeSpan <= 0) return {};

      // 计算起始时间相对于图表开始时间的位置（百分比）
      const startPosition = submission.startTime - chartStartDate;
      const startPercent = (startPosition / timeSpan) * 100;

      // 计算结束时间相对于图表开始时间的位置（百分比）
      const endPosition = submission.endTime - chartStartDate;
      const endPercent = (endPosition / timeSpan) * 100;

      // 计算实际宽度百分比
      let widthPercent = endPercent - startPercent;

      // 如果宽度小于等于0，或者非常小，设置为最小值
      if (widthPercent <= 0.5) {
        // 如果计算出的宽度不正确或者非常小，使用一个小时作为默认宽度
        const oneHour = 60 * 60 * 1000; // 一小时的毫秒数
        const oneHourPercent = (oneHour / timeSpan) * 100; // 一小时占总时间的百分比
        widthPercent = Math.max(1, oneHourPercent); // 至少为1%
        console.log(`宽度计算过小 (${endPercent - startPercent}%)，使用默认宽度: ${widthPercent.toFixed(2)}%`);
      }

      // 使用实际的起始时间百分比
      let leftPosition = startPercent;

      // 如果左侧位置小于0，设置为0
      leftPosition = Math.max(0, leftPosition);

      // 如果左侧位置大于99，设置为99，确保至少有一部分可见
      leftPosition = Math.min(99 - widthPercent, leftPosition); // 确保横条完全可见

      // 计算实际宽度，确保不超出右边界
      const width = Math.min(widthPercent, 100 - leftPosition);

      // 在控制台输出计算结果
      console.log(`图表开始时间: ${this.formatDate(chartStartDate)}`);
      console.log(`图表结束时间: ${this.formatDate(chartEndDate)}`);
      console.log(`起始百分比: ${startPercent.toFixed(2)}%`);
      console.log(`结束百分比: ${endPercent.toFixed(2)}%`);
      console.log(`宽度百分比: ${widthPercent.toFixed(2)}%`);
      console.log(`最终左侧位置: ${leftPosition.toFixed(2)}%`);
      console.log(`最终宽度: ${width.toFixed(2)}%`);

      // 根据状态设置不同的颜色
      let backgroundColor;
      switch (submission.status) {
        case 'APPROVED':
          backgroundColor = '#67C23A'; // 绿色
          break;
        case 'PENDING':
          backgroundColor = '#E6A23C'; // 橙色
          break;
        case 'REJECTED':
          backgroundColor = '#F56C6C'; // 红色
          break;
        default:
          backgroundColor = '#909399'; // 灰色
      }

      // 不再添加间隔，直接使用计算出的精确值
      const style = {
        left: `${leftPosition}%`,
        width: `${width}%`, // 直接使用计算出的宽度
        backgroundColor: backgroundColor
      };

      console.log(`最终样式: left=${style.left}, width=${style.width}, color=${backgroundColor}`);

      return style;
    },

    // 查看提交详情
    viewSubmissionDetails(submission) {
      // 跳转到提交详情页面
      this.$router.push({
        name: 'ProjectStepReview',
        params: {
          projectId: this.projectId,
          stepNumber: this.stepNumber,
          studentId: submission.studentId
        },
        query: {
          batchNumber: submission.batchNumber // 传递批次号作为查询参数
        }
      });
    },

    // 获取其他步骤的完成情况
    async fetchOtherStepsCompletion(studentMap) {
      try {
        // 获取所有学生的所有步骤提交
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}/submissions`);
        const allSubmissions = response.data;

        // 处理所有提交数据
        allSubmissions.forEach(submission => {
          if (studentMap.has(submission.studentId) && submission.status === 'APPROVED') {
            const studentData = studentMap.get(submission.studentId);
            if (!studentData.completedSteps.includes(submission.stepNumber)) {
              studentData.completedSteps.push(submission.stepNumber);
            }

            // 更新当前步骤
            studentData.currentStep = Math.max(...studentData.completedSteps) + 1;
            if (studentData.currentStep > 4) {
              studentData.currentStep = 4; // 最多4步
            }
          }
        });

        // 转换为数组
        this.ganttData = Array.from(studentMap.values());
      } catch (error) {
        console.error('获取学生步骤完成情况失败:', error);
      }
    },
    reviewSubmission(studentId) {
      this.$router.push({
        name: 'ProjectStepReview',
        params: {
          projectId: this.projectId,
          stepNumber: this.stepNumber,
          studentId: studentId
        }
      });
    },
    viewStudentDetails(studentId) {
      this.$router.push({
        name: 'StudentDetail',
        params: { studentId }
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
.step-submissions-container {
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

.filter-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.gantt-toggle {
  margin-left: auto;
}

.gantt-toggle-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.gantt-toggle-btn:hover {
  background-color: #66b1ff;
}

/* 甘特图样式 */
.gantt-chart-container {
  margin-bottom: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.gantt-instruction {
  color: #606266;
  font-size: 14px;
  margin-top: 5px;
  margin-bottom: 15px;
  font-style: italic;
}

.gantt-chart {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.gantt-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.gantt-header-left {
  width: 150px;
  padding: 12px 15px;
  font-weight: bold;
  border-right: 1px solid #ebeef5;
  position: sticky;
  left: 0;
  background-color: #f5f7fa;
  z-index: 10;
}

.gantt-header-right {
  display: flex;
  flex: 1;
  position: relative;
}

/* 时间轴甘特图样式 */
.gantt-timeline {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 12px 15px;
  height: 30px; /* 减小高度，因为标签不再旋转 */
}

.gantt-time-label {
  font-weight: bold;
  font-size: 10px; /* 减小字体大小，使更多标签能够显示 */
  color: #606266;
  /* 移除旋转，使标签水平显示 */
  white-space: nowrap; /* 防止换行 */
  margin-right: 10px; /* 增加右侧间距 */
}

.gantt-bar {
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  z-index: 1;
  min-width: 10px; /* 减小最小宽度，使小的横条也能准确显示 */
  /* 移除最大宽度限制 */
  padding: 0 5px;
  position: absolute;
  top: 50%; /* 垂直居中 */
  transform: translateY(-50%); /* 垂直居中 */
  cursor: pointer; /* 添加手型光标样式 */
  transition: all 0.2s ease; /* 添加过渡效果 */
  /* 移除固定的水平间距 */
}

.gantt-bar:hover {
  transform: translateY(-50%) scale(1.05); /* 悬停时的放大效果 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); /* 增强悬停时的阴影 */
  z-index: 2; /* 悬停时提高层级 */
}

.gantt-bar.status-approved {
  background-color: #67C23A;
}

.gantt-bar.status-pending {
  background-color: #E6A23C;
}

.gantt-bar.status-rejected {
  background-color: #F56C6C;
}

/* 原来的步骤甘特图样式 */
.gantt-step {
  flex: 1;
  padding: 12px 15px;
  text-align: center;
  font-weight: bold;
  border-right: 1px solid #ebeef5;
}

.gantt-step:last-child {
  border-right: none;
}

.gantt-body {
  max-height: 400px;
  overflow-y: auto;
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  position: relative;
}

.gantt-row:last-child {
  border-bottom: none;
}

.gantt-row-left {
  width: 150px;
  padding: 12px 15px;
  border-right: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  position: sticky;
  left: 0;
  background-color: white;
  z-index: 10;
}

.gantt-row-right {
  display: flex;
  flex: 1;
  position: relative;
  min-height: 70px; /* 增加高度以提供更多空间 */
  overflow: visible; /* 确保横条不会被裁剪 */
}

.gantt-cell {
  flex: 1;
  height: 40px;
  border-right: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gantt-cell:last-child {
  border-right: none;
}

.gantt-cell.completed {
  background-color: #67C23A;
  position: relative;
}

.gantt-cell.completed:after {
  content: '\2713'; /* Checkmark symbol */
  position: absolute;
  color: white;
  font-weight: bold;
}

.gantt-cell.current {
  background-color: #E6A23C;
  position: relative;
}

.gantt-cell.current:after {
  content: '\270E'; /* Pencil symbol */
  position: absolute;
  color: white;
  font-weight: bold;
}

.gantt-cell.pending {
  background-color: #F5F7FA;
}

.filter-group label {
  font-weight: 500;
  color: #303133;
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.filter-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: white;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn:hover {
  border-color: #c6e2ff;
  color: #409EFF;
}

.filter-btn.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 10px 0;
}

.empty-state p {
  color: #909399;
  margin: 0;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.submission-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #eaeaea;
}

.student-info {
  display: flex;
  align-items: center;
  cursor: pointer;
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
  font-size: 18px;
  font-weight: bold;
  margin-right: 15px;
}

.student-details {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-size: 16px;
  color: #303133;
  margin: 0 0 5px 0;
}

.student-school {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.submission-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.submission-time {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.submission-status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
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

.submission-content {
  padding: 20px;
}

.content-section, .file-section {
  margin-bottom: 20px;
}

.content-section h4, .file-section h4 {
  font-size: 16px;
  color: #303133;
  margin: 0 0 10px 0;
}

.content-section p {
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

.submission-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

.review-btn {
  background-color: #409EFF;
  color: white;
  padding: 8px 16px;
}

.review-btn:hover {
  background-color: #66b1ff;
}

@media (max-width: 768px) {
  .submission-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .submission-meta {
    margin-top: 10px;
    align-items: flex-start;
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
