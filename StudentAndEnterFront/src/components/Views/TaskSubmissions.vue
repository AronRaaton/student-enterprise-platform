<template>
  <div class="task-submissions-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">任务提交情况</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 任务详情 -->
    <div v-else-if="task" class="content-container">
      <div class="task-info-card">
        <div class="task-header">
          <h2 class="task-title">{{ task.summary || task.title }}</h2>
          <span class="task-date">发布时间: {{ formatDate(task.createTime) }}</span>
        </div>
        <div class="task-content">
          <p class="task-description">{{ task.content || task.requirements }}</p>
          <div class="task-meta">
            <span class="task-tech">技术栈: {{ task.techStack || '未指定' }}</span>
            <span class="task-count">总接取数量: {{ submissions.length }}</span>
          </div>
        </div>
      </div>

      <!-- 提交状态统计 -->
      <div class="status-statistics">
        <div class="stat-card" @click="filterByStatus(null)" :class="{ active: currentStatusFilter === null }">
          <div class="stat-value">{{ submissions.length }}</div>
          <div class="stat-label">全部</div>
        </div>
        <div class="stat-card" @click="filterByStatus('NOT_SUBMITTED')" :class="{ active: currentStatusFilter === 'NOT_SUBMITTED' }">
          <div class="stat-value">{{ getStatusCount('NOT_SUBMITTED') }}</div>
          <div class="stat-label">未提交</div>
        </div>
        <div class="stat-card" @click="filterByStatus('SUBMITTED')" :class="{ active: currentStatusFilter === 'SUBMITTED' }">
          <div class="stat-value">{{ getStatusCount('SUBMITTED') }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card" @click="filterByStatus('NEEDS_REVISION')" :class="{ active: currentStatusFilter === 'NEEDS_REVISION' }">
          <div class="stat-value">{{ getStatusCount('NEEDS_REVISION') }}</div>
          <div class="stat-label">需要修改</div>
        </div>
        <div class="stat-card" @click="filterByStatus('COMPLETED')" :class="{ active: currentStatusFilter === 'COMPLETED' }">
          <div class="stat-value">{{ getStatusCount('COMPLETED') }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>

      <!-- 提交列表 -->
      <div class="submissions-section">
        <h2 class="section-title">学生提交列表</h2>

        <div v-if="submissions.length === 0" class="empty-state">
          <div class="empty-icon">📝</div>
          <p>暂无学生提交此任务</p>
        </div>

        <div v-else class="submissions-list">
          <div v-for="submission in filteredSubmissions" :key="submission.submissionId" class="submission-card">
            <div class="submission-header" @click="toggleSubmission(submission.submissionId)">
              <div class="student-info" @click.stop="viewStudentDetails(submission.student.studentId)">
                <div class="student-avatar">{{ submission.student && submission.student.name ? submission.student.name.charAt(0) : 'S' }}</div>
                <div class="student-details">
                  <h3 class="student-name">{{ submission.student ? submission.student.name : '未知学生' }}</h3>
                  <p class="student-id">学号: {{ submission.student ? submission.student.studentId : '未知' }}</p>
                </div>
              </div>
              <div class="submission-meta">
                <span class="submission-date">提交时间: {{ formatDate(submission.submissionTime) }}</span>
                <span class="submission-status" :class="getStatusClass(submission.status)">
                  {{ getStatusText(submission.status) }}
                </span>
                <span class="toggle-icon">{{ expandedSubmissions.includes(submission.submissionId) ? '▼' : '▶' }}</span>
              </div>
            </div>

            <div v-if="expandedSubmissions.includes(submission.submissionId)" class="submission-details">
              <div class="submission-content">
                <div class="submission-tabs">
                  <div class="tab" :class="{ active: activeTab[submission.submissionId] === 'content' }"
                       @click="setActiveTab(submission.submissionId, 'content')">提交内容</div>
                  <div class="tab" :class="{ active: activeTab[submission.submissionId] === 'history' }"
                       @click="loadSubmissionHistory(submission.submissionId)">提交历史</div>
                  <div class="tab" :class="{ active: activeTab[submission.submissionId] === 'gantt' }"
                       @click="setActiveTab(submission.submissionId, 'gantt')">进度甘特图</div>
                </div>

                <div v-if="activeTab[submission.submissionId] === 'content'" class="tab-content">
                  <h4>提交内容:</h4>
                  <p>{{ submission.submissionContent || '无内容' }}</p>

                  <div v-if="submission.fileUrl" class="file-link">
                    <h4>文件链接:</h4>
                    <a :href="submission.fileUrl" target="_blank" class="file-url">{{ submission.fileUrl }}</a>
                  </div>
                </div>

                <div v-if="activeTab[submission.submissionId] === 'history'" class="tab-content">
                  <div v-if="loadingHistories[submission.submissionId]" class="loading-histories">
                    <div class="loading-spinner small"></div>
                    <p>加载历史记录中...</p>
                  </div>
                  <div v-else-if="submissionHistories[submission.submissionId] && submissionHistories[submission.submissionId].length > 0" class="history-list">
                    <div v-for="history in submissionHistories[submission.submissionId]" :key="history.historyId" class="history-item"
                         :class="{'history-approved': history.status === 'APPROVED', 'history-needs-revision': history.status === 'NEEDS_REVISION'}">
                      <div class="history-header">
                        <div class="history-meta">
                          <span class="history-type">{{ history.submissionTypeDisplayName }}</span>
                          <span class="history-date">{{ history.formattedCreatedTime }}</span>
                          <span class="history-status">{{ history.statusDisplayName }}</span>
                        </div>
                        <div class="history-description">{{ history.description }}</div>
                      </div>

                      <div class="history-content">
                        <p>{{ history.content }}</p>

                        <div v-if="history.fileUrl" class="history-file-link">
                          <a :href="history.fileUrl" target="_blank" class="file-link">外部链接: {{ history.fileUrl }}</a>
                        </div>

                        <div v-if="history.uploadedFile" class="history-uploaded-file">
                          <a :href="`http://localhost:8080/api/files/download/${history.uploadedFile}`" class="file-link">下载文件: {{ history.uploadedFile }}</a>
                        </div>
                      </div>

                      <div v-if="history.status === 'SUBMITTED'" class="history-review-actions">
                        <div class="form-group">
                          <label for="historyFeedback">反馈意见:</label>
                          <textarea
                            :id="'historyFeedback-' + history.historyId"
                            v-model="historyFeedbackMap[history.historyId]"
                            rows="2"
                            placeholder="请输入对该次提交的反馈意见..."
                            class="history-feedback-textarea"
                          ></textarea>
                        </div>
                        <div class="history-review-buttons">
                          <button
                            class="history-approve-btn"
                            @click="approveHistorySubmission(history.historyId)"
                            :disabled="historyReviewLoading"
                          >
                            <span v-if="historyReviewLoading && currentHistoryReviewId === history.historyId" class="loading-icon small"></span>
                            <span v-else>通过</span>
                          </button>
                          <button
                            class="history-reject-btn"
                            @click="rejectHistorySubmission(history.historyId)"
                            :disabled="historyReviewLoading"
                          >
                            <span v-if="historyReviewLoading && currentHistoryReviewId === history.historyId" class="loading-icon small"></span>
                            <span v-else>需要修改</span>
                          </button>
                        </div>
                      </div>

                      <div v-else-if="history.feedback" class="history-feedback">
                        <h4>企业反馈:</h4>
                        <p>{{ history.feedback }}</p>
                        <div class="feedback-time" v-if="history.formattedReviewTime">
                          反馈时间: {{ history.formattedReviewTime }}
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-history">
                    <p>暂无提交历史记录</p>
                  </div>
                </div>

                <div v-if="activeTab[submission.submissionId] === 'gantt'" class="tab-content gantt-chart-container">
                  <h4>学生进度甘特图:</h4>
                  <div v-if="!submissionHistories[submission.submissionId] || submissionHistories[submission.submissionId].length === 0" class="loading-histories">
                    <div class="loading-spinner small"></div>
                    <p>正在加载历史记录...</p>
                  </div>
                  <div v-else-if="submissionHistories[submission.submissionId].length > 0" class="gantt-chart">
                    <div class="gantt-header">
                      <div class="gantt-task-label">提交内容</div>
                      <div class="gantt-timeline">
                        <div v-for="(date, index) in getGanttDates(submission.submissionId)" :key="index" class="gantt-date">
                          {{ formatGanttDate(date) }}
                        </div>
                      </div>
                    </div>
                    <div class="gantt-body">
                      <div v-for="(history, index) in submissionHistories[submission.submissionId]" :key="history.historyId" class="gantt-row">
                        <div class="gantt-task-info">
                          <div class="gantt-task-name">{{ getShortDescription(history.description) }}</div>
                          <div class="gantt-task-type">{{ history.submissionTypeDisplayName }}</div>
                        </div>
                        <div class="gantt-task-timeline">
                          <div class="gantt-task-bar"
                               :style="getGanttBarStyle(history, index, submission.submissionId)"
                               :class="getGanttBarClass(history)">
                            {{ formatGanttDate(new Date(history.createdTime)) }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-history">
                    <p>暂无提交历史记录，无法生成甘特图</p>
                  </div>
                </div>



                <div v-if="submission.feedback" class="feedback-section">
                  <h4>反馈意见:</h4>
                  <p>{{ submission.feedback }}</p>
                </div>
              </div>


            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 任务不存在 -->
    <div v-else class="error-container">
      <div class="error-icon">❌</div>
      <h2>任务不存在</h2>
      <p>无法找到该任务信息，请返回重试</p>
      <button class="back-btn" @click="goBack">返回</button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      task: null,
      submissions: [],
      loading: true,
      expandedSubmissions: [],
      feedbackMap: {},
      ratingMap: {},
      reviewLoading: false,
      currentReviewId: null,
      currentStatusFilter: null,
      submissionFiles: {},
      activeTab: {}, // 存储每个提交的活动标签
      submissionHistories: {}, // 存储每个提交的历史记录
      loadingHistories: {}, // 存储每个提交的历史记录加载状态
      historyFeedbackMap: {}, // 存储每个历史记录的反馈
      historyReviewLoading: false, // 历史记录审核加载状态
      currentHistoryReviewId: null // 当前正在审核的历史记录ID
    };
  },
  computed: {
    filteredSubmissions() {
      if (this.currentStatusFilter === null) {
        return this.submissions;
      }
      return this.submissions.filter(submission => submission.status === this.currentStatusFilter);
    }
  },
  created() {
    const taskId = this.$route.params.taskId;
    if (taskId) {
      this.fetchTaskDetails(taskId);
      this.fetchTaskSubmissions(taskId).then(() => {
        // 检查是否有需要展开的提交ID
        const expandSubmissionId = this.$route.query.expandSubmission;
        if (expandSubmissionId && !this.expandedSubmissions.includes(parseInt(expandSubmissionId))) {
          this.expandedSubmissions.push(parseInt(expandSubmissionId));
        }
      });
    } else {
      this.loading = false;
    }
  },
  methods: {
    // 检查是否是PDF文件
    isPdfFile(fileName) {
      if (!fileName) return false;
      return fileName.toLowerCase().endsWith('.pdf');
    },

    // 根据文件名获取文件类型
    // 下载文件
    async downloadFile(url, fileName) {
      console.log('开始下载文件:', { url, fileName });

      try {
        // 使用fetch API获取文件
        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Cache-Control': 'no-cache',
            'Pragma': 'no-cache'
          }
        });

        if (!response.ok) {
          // 如果响应不成功，尝试解析错误信息
          const errorText = await response.text();
          console.error('下载文件失败:', response.status, errorText);
          alert('下载文件失败: ' + response.status + ' ' + response.statusText);
          return;
        }

        // 获取blob数据
        const blob = await response.blob();

        // 创建一个URL对象 - 使用不同的变量名避免冲突
        const blobUrl = window.URL.createObjectURL(blob);

        // 创建一个隐藏的a标签来触发下载
        const link = document.createElement('a');
        link.href = blobUrl;
        link.setAttribute('download', fileName);
        document.body.appendChild(link);

        // 模拟点击
        link.click();

        // 清理
        setTimeout(() => {
          window.URL.revokeObjectURL(blobUrl);
          document.body.removeChild(link);
        }, 100);
      } catch (error) {
        console.error('下载文件时发生错误:', error);
        alert('下载文件时发生错误: ' + error.message);
      }
    },

    getFileTypeFromName(fileName) {
      if (!fileName) return 'unknown';

      const lowerCaseFileName = fileName.toLowerCase();
      if (lowerCaseFileName.endsWith('.pdf')) {
        return 'pdf';
      } else if (lowerCaseFileName.endsWith('.doc') || lowerCaseFileName.endsWith('.docx')) {
        return 'word';
      } else if (lowerCaseFileName.endsWith('.xls') || lowerCaseFileName.endsWith('.xlsx')) {
        return 'excel';
      } else if (lowerCaseFileName.endsWith('.ppt') || lowerCaseFileName.endsWith('.pptx')) {
        return 'powerpoint';
      } else if (lowerCaseFileName.endsWith('.jpg') || lowerCaseFileName.endsWith('.jpeg') ||
                lowerCaseFileName.endsWith('.png') || lowerCaseFileName.endsWith('.gif')) {
        return 'image';
      } else if (lowerCaseFileName.endsWith('.zip') || lowerCaseFileName.endsWith('.rar') ||
                lowerCaseFileName.endsWith('.7z')) {
        return 'archive';
      } else if (lowerCaseFileName.endsWith('.txt')) {
        return 'text';
      } else {
        return 'other';
      }
    },
    fetchTaskDetails(taskId) {
      axios.get(`http://localhost:8080/api/tasks/${taskId}`)
        .then(response => {
          this.task = response.data;
        })
        .catch(error => {
          console.error('无法获取任务详情:', error);
        });
    },
    fetchTaskSubmissions(taskId) {
      return axios.get(`http://localhost:8080/api/tasks/${taskId}/submissions`)
        .then(response => {
          this.submissions = response.data;
          // 初始化反馈映射和评分映射
          this.submissions.forEach(submission => {
            this.feedbackMap[submission.submissionId] = submission.feedback || '';
            this.ratingMap[submission.submissionId] = submission.rating || 0;
          });
          this.loading = false;
          return this.submissions; // 返回提交列表，以便于链式调用
        })
        .catch(error => {
          console.error('无法获取任务提交列表:', error);
          this.loading = false;
          return []; // 出错时返回空数组
        });
    },
    toggleSubmission(submissionId) {
      if (this.expandedSubmissions.includes(submissionId)) {
        this.expandedSubmissions = this.expandedSubmissions.filter(id => id !== submissionId);
      } else {
        this.expandedSubmissions.push(submissionId);
        // 当展开提交详情时，检查是否有附件
        this.checkSubmissionFile(submissionId);
        // 设置默认标签为内容
        this.activeTab[submissionId] = 'content';
        // 自动加载历史记录，以便甘特图选项卡可以显示
        this.loadSubmissionHistoryInBackground(submissionId);
      }
    },

    // 在后台加载提交历史，不切换标签
    loadSubmissionHistoryInBackground(submissionId) {
      // 如果已经加载过，不重复加载
      if (this.submissionHistories[submissionId] && this.submissionHistories[submissionId].length > 0) {
        return;
      }

      // 请求历史记录
      axios.get(`http://localhost:8080/api/submission-history/submission/${submissionId}`)
        .then(response => {
          // 存储历史记录
          this.submissionHistories[submissionId] = response.data;
          console.log(`后台加载提交 ${submissionId} 的历史记录:`, response.data.length);
        })
        .catch(error => {
          console.error(`后台加载提交 ${submissionId} 的历史记录失败:`, error);
          // 设置空数组
          this.submissionHistories[submissionId] = [];
        });
    },

    // 设置活动标签
    setActiveTab(submissionId, tabName) {
      // 直接设置响应式对象的属性 (Vue 3 方式)
      this.activeTab[submissionId] = tabName;

      // 如果是历史标签，加载历史记录
      if (tabName === 'history') {
        this.loadSubmissionHistory(submissionId);
      }
    },

    // 加载提交历史
    loadSubmissionHistory(submissionId) {
      // 设置活动标签为历史
      this.activeTab[submissionId] = 'history';

      // 如果已经加载过，不重复加载
      if (this.submissionHistories[submissionId] && this.submissionHistories[submissionId].length > 0) {
        return;
      }

      // 设置加载状态
      this.loadingHistories[submissionId] = true;

      // 请求历史记录
      axios.get(`http://localhost:8080/api/submission-history/submission/${submissionId}`)
        .then(response => {
          // 存储历史记录
          this.submissionHistories[submissionId] = response.data;
          console.log(`获取到提交 ${submissionId} 的历史记录:`, response.data.length);
        })
        .catch(error => {
          console.error(`获取提交 ${submissionId} 的历史记录失败:`, error);
          // 设置空数组
          this.submissionHistories[submissionId] = [];
        })
        .finally(() => {
          // 关闭加载状态
          this.loadingHistories[submissionId] = false;
        });
    },

    // 审核历史提交记录
    async approveHistorySubmission(historyId) {
      await this.reviewHistorySubmission(historyId, 'APPROVED');
    },

    async rejectHistorySubmission(historyId) {
      await this.reviewHistorySubmission(historyId, 'NEEDS_REVISION');
    },

    async reviewHistorySubmission(historyId, status) {
      // 检查是否有反馈
      const feedback = this.historyFeedbackMap[historyId];
      if (!feedback || feedback.trim() === '') {
        alert('请输入反馈意见');
        return;
      }

      this.historyReviewLoading = true;
      this.currentHistoryReviewId = historyId;

      try {
        const response = await axios.post(`http://localhost:8080/api/submission-history/${historyId}/review`, {
          status: status,
          feedback: feedback
        });

        console.log(`历史记录 ${historyId} 审核成功:`, response.data);

        // 更新历史记录
        const updatedHistory = response.data;

        // 找到该历史记录所属的提交ID
        const submissionId = updatedHistory.submissionId;

        // 更新历史记录数组中的对应项
        if (this.submissionHistories[submissionId]) {
          const index = this.submissionHistories[submissionId].findIndex(h => h.historyId === historyId);
          if (index !== -1) {
            this.submissionHistories[submissionId][index] = updatedHistory;
          }
        }

        // 清空反馈输入
        delete this.historyFeedbackMap[historyId];

        // 如果是完成全部的提交，刷新提交列表
        if (updatedHistory.submissionType === 'COMPLETE') {
          await this.fetchTaskSubmissions(this.task.taskId);
        }

        alert(status === 'APPROVED' ? '审核通过成功' : '已要求学生修改');
      } catch (error) {
        console.error(`历史记录 ${historyId} 审核失败:`, error);
        alert(`审核失败: ${error.response?.data?.message || error.message}`);
      } finally {
        this.historyReviewLoading = false;
        this.currentHistoryReviewId = null;
      }
    },

    checkSubmissionFile(submissionId) {
      // 如果已经获取过文件信息，不再重复获取
      if (this.submissionFiles[submissionId]) return;

      // 首先获取提交记录，检查fileUrl和uploadedFile字段
      const submission = this.submissions.find(s => s.submissionId === submissionId);
      if (!submission) {
        console.log(`提交 ${submissionId} 不存在`);
        return;
      }

      // 如果既没有上传文件也没有外部链接，则返回
      if (!submission.uploadedFile && !submission.fileUrl) {
        console.log(`提交 ${submissionId} 没有文件或链接`);
        return;
      }

      console.log(`检查提交 ${submissionId} 的文件:`, {
        fileUrl: submission.fileUrl,
        uploadedFile: submission.uploadedFile
      });

      axios.get(`http://localhost:8080/api/files/submission/${submissionId}`)
        .then(response => {
          console.log(`提交 ${submissionId} 的文件响应:`, response.data);

          // 检查是否有下载链接（上传的文件）
          if (response.data.fileDownloadUri) {
            this.submissionFiles[submissionId] = {
              fileName: response.data.fileName,
              downloadUrl: `http://localhost:8080${response.data.fileDownloadUri}`,
              fileType: response.data.fileType || this.getFileTypeFromName(response.data.fileName)
            };
            console.log(`提交 ${submissionId} 有上传的文件:`, this.submissionFiles[submissionId]);
          }
          // 检查是否有外部链接
          else if (response.data.externalUrl) {
            this.submissionFiles[submissionId] = {
              fileName: response.data.fileName || '外部链接',
              externalUrl: response.data.externalUrl
            };
            console.log(`提交 ${submissionId} 有外部链接:`, this.submissionFiles[submissionId]);
          }
          // 其他情况（普通文件URL）
          else if (response.data.fileUrl) {
            this.submissionFiles[submissionId] = {
              fileName: response.data.fileName || '文件链接',
              fileUrl: response.data.fileUrl
            };
            console.log(`提交 ${submissionId} 有普通文件链接:`, this.submissionFiles[submissionId]);
          }
        })
        .catch(error => {
          // 如果没有文件，尝试直接使用submission中的fileUrl
          console.log(`提交 ${submissionId} 获取文件信息失败:`, error.response?.data?.error || error.message);

          // 如果fileUrl是一个外部链接，直接使用
          if (submission.fileUrl && (submission.fileUrl.startsWith('http') || submission.fileUrl.startsWith('https'))) {
            this.submissionFiles[submissionId] = {
              fileName: '外部链接',
              externalUrl: submission.fileUrl
            };
            console.log(`直接使用外部链接:`, this.submissionFiles[submissionId]);
          } else if (submission.fileUrl) {
            // 其他情况，尝试作为普通文件链接
            this.submissionFiles[submissionId] = {
              fileName: submission.fileUrl,
              fileUrl: submission.fileUrl
            };
            console.log(`直接使用文件链接:`, this.submissionFiles[submissionId]);
          }
        });
    },
    approveSubmission(submissionId) {
      this.reviewSubmission(submissionId, 'COMPLETED');
    },
    rejectSubmission(submissionId) {
      this.reviewSubmission(submissionId, 'NEEDS_REVISION');
    },
    setRating(submissionId, rating) {
      this.ratingMap[submissionId] = rating;
    },
    filterByStatus(status) {
      this.currentStatusFilter = status;
    },
    getStatusCount(status) {
      return this.submissions.filter(submission => submission.status === status).length;
    },
    reviewSubmission(submissionId, status) {
      this.reviewLoading = true;
      this.currentReviewId = submissionId;

      axios.post(`http://localhost:8080/api/enterprises/tasks/submissions/${submissionId}/review`, {
        status: status,
        feedback: this.feedbackMap[submissionId],
        rating: this.ratingMap[submissionId] || 0
      })
        .then(response => {
          // 更新本地提交状态
          const index = this.submissions.findIndex(s => s.submissionId === submissionId);
          if (index !== -1) {
            this.submissions[index] = response.data;
          }

          // 显示成功消息
          alert(status === 'COMPLETED' ? '已通过该提交' : '已要求学生修改');
        })
        .catch(error => {
          console.error('审核提交失败:', error);
          alert('审核失败，请重试');
        })
        .finally(() => {
          this.reviewLoading = false;
          this.currentReviewId = null;
        });
    },
    goBack() {
      this.$router.go(-1);
    },
    viewStudentDetails(studentId) {
      if (studentId) {
        this.$router.push({ name: 'StudentDetail', params: { studentId } });
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },

    // 格式化甘特图日期
    formatGanttDate(date) {
      if (!date) return '';
      return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },

    // 获取甘特图日期范围
    getGanttDates(submissionId) {
      if (!this.submissionHistories[submissionId] || this.submissionHistories[submissionId].length === 0) {
        return [];
      }

      // 获取所有历史记录的日期
      const histories = this.submissionHistories[submissionId];

      // 按创建时间排序历史记录（从早到晚）
      const sortedHistories = [...histories].sort((a, b) =>
        new Date(a.createdTime) - new Date(b.createdTime)
      );

      // 找出最早和最晚的日期
      let minDate, maxDate;

      // 第一次提交的起始时间是任务创建时间（近似值）
      minDate = new Date(this.task.createTime);

      // 最后一次提交的时间
      maxDate = new Date(sortedHistories[sortedHistories.length - 1].createdTime);

      // 添加一天的缓冲
      maxDate.setDate(maxDate.getDate() + 1);

      // 计算总天数
      const totalDays = Math.ceil((maxDate - minDate) / (1000 * 60 * 60 * 24));

      // 根据总天数决定日期间隔
      // 如果天数太多，我们增加间隔以减少日期标签数量
      let interval = 1; // 默认每天显示一个标签

      if (totalDays > 30) {
        interval = 7; // 每周显示一个标签
      } else if (totalDays > 14) {
        interval = 2; // 每两天显示一个标签
      }

      // 生成日期范围
      const dates = [];
      const currentDate = new Date(minDate);
      currentDate.setHours(0, 0, 0, 0);

      while (currentDate <= maxDate) {
        dates.push(new Date(currentDate));
        // 根据计算的间隔增加日期
        currentDate.setDate(currentDate.getDate() + interval);
      }

      return dates;
    },

    // 获取甘特图条样式
    getGanttBarStyle(history, index, submissionId) {
      const dates = this.getGanttDates(submissionId);
      if (dates.length === 0) return {};

      const histories = this.submissionHistories[submissionId];
      if (!histories || histories.length === 0) return {};

      // 按创建时间排序历史记录（从早到晚）
      const sortedHistories = [...histories].sort((a, b) =>
        new Date(a.createdTime) - new Date(b.createdTime)
      );

      // 找到当前历史记录在排序后的数组中的索引
      const sortedIndex = sortedHistories.findIndex(h => h.historyId === history.historyId);
      if (sortedIndex === -1) return {};

      let startTime, endTime;
      const createdTime = new Date(history.createdTime);

      // 第一次提交的起始时间是学生通过任务审核的时间，终止时间是提交的时间
      if (sortedIndex === 0) {
        // 对于第一次提交，起始时间设为任务创建时间（近似值）
        // 理想情况下应该是学生通过任务审核的时间，但这里用任务创建时间代替
        startTime = new Date(this.task.createTime);
        endTime = createdTime;
      } else {
        // 后续提交的起始时间是前一次提交的时间，终止时间是当前提交的时间
        startTime = new Date(sortedHistories[sortedIndex - 1].createdTime);
        endTime = createdTime;
      }

      // 使用日期数组的第一个和最后一个日期作为时间轴的起点和终点
      const minDate = dates[0];
      const maxDate = dates[dates.length - 1];

      // 计算左侧位置和宽度百分比
      const totalTime = maxDate - minDate;
      const startPosition = Math.max(0, startTime - minDate); // 确保不会出现负值
      const endPosition = Math.min(totalTime, endTime - minDate); // 确保不会超出总时间

      const leftPercent = (startPosition / totalTime) * 100;
      const widthPercent = ((endPosition - startPosition) / totalTime) * 100;

      return {
        left: `${Math.max(0, leftPercent)}%`, // 确保不会出现负值
        width: `${Math.max(widthPercent, 2)}%` // 确保宽度至少有2%以便可见
      };
    },

    // 获取甘特图条的类名
    getGanttBarClass(history) {
      if (history.status === 'APPROVED') {
        return 'gantt-bar-approved';
      } else if (history.status === 'NEEDS_REVISION') {
        return 'gantt-bar-needs-revision';
      } else {
        return 'gantt-bar-submitted';
      }
    },

    // 获取简短描述
    getShortDescription(description) {
      if (!description) return '无描述';
      return description.length > 15 ? description.substring(0, 15) + '...' : description;
    },
    getStatusClass(status) {
      if (!status) return 'status-not-submitted';

      switch(status) {
        case 'COMPLETED':
          return 'status-completed';
        case 'SUBMITTED':
          return 'status-submitted';
        case 'NEEDS_REVISION':
          return 'status-needs-revision';
        case 'NOT_SUBMITTED':
        default:
          return 'status-not-submitted';
      }
    },
    getStatusText(status) {
      if (!status) return '未提交';

      switch(status) {
        case 'COMPLETED':
          return '已通过';
        case 'SUBMITTED':
          return '待审核';
        case 'NEEDS_REVISION':
          return '需要修改';
        case 'NOT_SUBMITTED':
        default:
          return '未提交';
      }
    }
  }
};
</script>

<style scoped>
.task-submissions-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  background-color: transparent;
  border: none;
  display: flex;
  align-items: center;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  flex: 1;
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin: 0;
}

/* 加载中状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

.loading-icon.small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 内容区域样式 */
.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 状态统计样式 */
.status-statistics {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border: 2px solid #409EFF;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 任务信息卡片样式 */
.task-info-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.task-header {
  padding: 20px;
  background-color: #ecf5ff;
  border-bottom: 1px solid #d9ecff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.task-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
  flex: 1;
}

.task-date {
  font-size: 14px;
  color: #909399;
}

.task-content {
  padding: 20px;
}

.task-description {
  margin-top: 0;
  margin-bottom: 15px;
  line-height: 1.6;
  color: #606266;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.task-tech, .task-count {
  font-size: 14px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 5px 10px;
  border-radius: 4px;
}

/* 提交列表样式 */
.submissions-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.section-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 18px;
  color: #303133;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
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
}

.empty-state p {
  color: #909399;
  margin-bottom: 15px;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.submission-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
}

.submission-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.3s;
  flex-wrap: wrap;
  gap: 10px;
}

.submission-header:hover {
  background-color: #f5f7fa;
}

.student-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.student-info:hover {
  background-color: rgba(64, 158, 255, 0.1);
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

.student-id {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.submission-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.submission-date {
  font-size: 14px;
  color: #909399;
}

.submission-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-submitted {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-needs-revision {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-not-submitted {
  background-color: #f4f4f5;
  color: #909399;
}

.toggle-icon {
  font-size: 12px;
  color: #909399;
}

.submission-details {
  padding: 15px;
  border-top: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

.submission-content h4, .file-link h4, .feedback-section h4, .review-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.submission-content p, .feedback-section p {
  margin-top: 0;
  margin-bottom: 15px;
  line-height: 1.6;
  color: #606266;
}

.file-link {
  margin-bottom: 15px;
}

.file-url {
  color: #409EFF;
  text-decoration: none;
  word-break: break-all;
}

.file-url:hover {
  text-decoration: underline;
}

.file-attachment {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
}

.file-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-name {
  font-size: 14px;
  color: #606266;
  word-break: break-all;
}

.download-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  background-color: #409EFF;
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.3s;
}

.download-btn:hover {
  background-color: #66b1ff;
}

.download-icon, .external-icon {
  margin-right: 5px;
}

.external-link-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  background-color: #67c23a;
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.3s;
}

.external-link-btn:hover {
  background-color: #85ce61;
}

.file-link-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  background-color: #909399;
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.3s;
}

.file-link-btn:hover {
  background-color: #a6a9ad;
}

.file-icon {
  margin-right: 5px;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.preview-btn {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  background-color: #E6A23C;
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
  transition: background-color 0.3s;
}

.preview-btn:hover {
  background-color: #EBB563;
}

.preview-icon {
  margin-right: 5px;
}

.feedback-section {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.review-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  font-size: 14px;
  color: #606266;
}

.form-group textarea {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
}

.form-group textarea:focus {
  outline: none;
  border-color: #409EFF;
}

/* 评分样式 */
.rating-input {
  display: flex;
  gap: 5px;
}

.star {
  font-size: 24px;
  color: #dcdfe6;
  cursor: pointer;
  transition: color 0.3s;
}

.star:hover, .star.active {
  color: #e6a23c;
}

.review-actions {
  display: flex;
  gap: 10px;
}

.approve-btn, .reject-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
}

.approve-btn {
  background-color: #67c23a;
  color: white;
}

.approve-btn:hover:not(:disabled) {
  background-color: #85ce61;
}

.reject-btn {
  background-color: #e6a23c;
  color: white;
}

.reject-btn:hover:not(:disabled) {
  background-color: #ebb563;
}

.approve-btn:disabled, .reject-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 错误状态样式 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.error-container h2 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #f56c6c;
}

.error-container p {
  margin-bottom: 20px;
  color: #909399;
}

.back-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #66b1ff;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .back-button {
    position: static;
    margin-bottom: 15px;
  }

  .page-title {
    text-align: left;
  }

  .task-header, .submission-header, .submission-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .submission-meta {
    margin-top: 10px;
  }

  .review-actions {
    flex-direction: column;
    width: 100%;
  }

  .approve-btn, .reject-btn {
    width: 100%;
  }
}
/* 提交历史样式 */
.submission-tabs {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
  margin-bottom: 15px;
}

.tab {
  padding: 10px 15px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  font-weight: 500;
  color: #606266;
}

.tab:hover {
  color: #409EFF;
}

.tab.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
}

.tab-content {
  padding: 10px 0;
}

.loading-histories {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 15px;
  background-color: #fafafa;
  transition: all 0.3s;
}

.history-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.history-approved {
  border-left: 4px solid #67c23a;
}

.history-needs-revision {
  border-left: 4px solid #e6a23c;
}

.history-header {
  margin-bottom: 10px;
}

.history-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 5px;
}

.history-type {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.history-date {
  color: #909399;
  font-size: 12px;
}

.history-status {
  font-weight: bold;
  font-size: 12px;
}

.history-description {
  font-size: 14px;
  color: #303133;
  margin-top: 5px;
  font-weight: 500;
}

.history-content {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

.history-file-link, .history-uploaded-file {
  margin-top: 8px;
}

.file-link {
  color: #409EFF;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.file-link:hover {
  text-decoration: underline;
}

.history-feedback {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.history-feedback h4 {
  color: #303133;
  margin-top: 0;
  margin-bottom: 5px;
  font-size: 14px;
}

.feedback-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.history-review-actions {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.history-feedback-textarea {
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  width: 100%;
  margin-bottom: 10px;
}

.history-feedback-textarea:focus {
  outline: none;
  border-color: #409EFF;
}

.history-review-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.history-approve-btn, .history-reject-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  transition: all 0.3s;
}

.history-approve-btn {
  background-color: #409EFF;
  color: white;
}

.history-approve-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.history-reject-btn {
  background-color: #F56C6C;
  color: white;
}

.history-reject-btn:hover:not(:disabled) {
  background-color: #f78989;
}

.history-approve-btn:disabled, .history-reject-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.empty-history {
  text-align: center;
  padding: 20px;
  color: #909399;
}

/* 甘特图样式 */
.gantt-chart-container {
  margin-top: 20px;
  overflow-x: auto;
}

.gantt-chart {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.gantt-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
}

.gantt-task-label {
  width: 200px;
  padding: 0 15px;
  font-weight: bold;
  color: #303133;
  border-right: 1px solid #ebeef5;
}

.gantt-timeline {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.gantt-date {
  flex: 1;
  text-align: center;
  font-size: 12px;
  color: #606266;
}

.gantt-body {
  max-height: 300px;
  overflow-y: auto;
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  height: 40px;
}

.gantt-task-info {
  width: 200px;
  padding: 5px 15px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.gantt-task-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
}

.gantt-task-type {
  font-size: 11px;
  color: #909399;
}

.gantt-task-timeline {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.gantt-task-bar {
  position: absolute;
  top: 10px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
}

.gantt-bar-submitted {
  background-color: #409EFF;
}

.gantt-bar-approved {
  background-color: #67c23a;
}

.gantt-bar-needs-revision {
  background-color: #e6a23c;
}
</style>
