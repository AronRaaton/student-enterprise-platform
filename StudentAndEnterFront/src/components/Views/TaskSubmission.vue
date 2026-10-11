<template>
  <div class="task-submission-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">任务提交</h1>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 任务详情和提交表单 -->
    <div v-else class="content-container">
      <!-- 任务详情 -->
      <div class="task-details-section">
        <h2 class="section-title">任务详情</h2>
        <div class="task-info-card">
          <h3 class="task-title">{{ task.summary }}</h3>
          <div class="task-meta">
            <div class="meta-item">
              <span class="meta-label">发布企业:</span>
              <span class="meta-value">{{ task.enterprise ? task.enterprise.name : '未知企业' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">技术栈:</span>
              <span class="meta-value">{{ task.techStack }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">发布时间:</span>
              <span class="meta-value">{{ formatDate(task.createTime) }}</span>
            </div>
          </div>
          <div class="task-description">
            <p>{{ task.content }}</p>
          </div>
          <div class="task-requirements" v-if="task.requirements">
            <h4>任务要求:</h4>
            <p>{{ task.requirements }}</p>
          </div>
        </div>
      </div>

      <!-- 任务状态 -->
      <div class="submission-status-section">
        <h2 class="section-title">任务状态</h2>
        <div class="status-card">
          <div class="status-header">
            <div class="status-badge" :class="getStatusClass(submission.status)">
              {{ getStatusText(submission.status) }}
            </div>
            <div class="submission-date" v-if="submission.submissionTime">
              提交时间: {{ formatDate(submission.submissionTime) }}
            </div>
          </div>

          <div class="feedback-section" v-if="submission.feedback">
            <h4>反馈意见:</h4>
            <p>{{ submission.feedback }}</p>
          </div>
        </div>
      </div>

      <!-- 已完成任务的评分展示 -->
      <div class="completed-task-section" v-if="submission && submission.status === 'COMPLETED'">
        <h2 class="section-title">任务已完成</h2>

        <div class="rating-display" v-if="submission.rating">
          <h3>企业评分</h3>
          <div class="stars-container">
            <span v-for="star in 5" :key="star" class="star" :class="{ active: submission.rating >= star }">★</span>
            <span class="rating-value">{{ submission.rating }}/5</span>
          </div>
        </div>

        <div class="feedback-display" v-if="submission.feedback">
          <h3>企业反馈</h3>
          <div class="feedback-content">
            {{ submission.feedback }}
          </div>
        </div>

        <div class="action-buttons">
          <button class="rate-task-button" @click="openRatingDialog">给任务评分</button>
          <button class="retake-button" @click="retakeTask">重新接取任务</button>
        </div>
      </div>

      <!-- 评分对话框 -->
      <rating-dialog
        :show="showRatingDialog"
        :target-id="taskId"
        target-type="task"
        title="给任务评分"
        @close="showRatingDialog = false"
        @rated="onTaskRated"
      />

      <!-- 提交表单 -->
      <div class="submission-form-section" v-if="canSubmit">
        <h2 class="section-title">提交任务</h2>
        <div class="submission-form">
          <div class="form-group submission-type-selector">
            <label>提交类型:</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="submissionType" value="PARTIAL" name="submissionType">
                部分完成
              </label>
              <label class="radio-label">
                <input type="radio" v-model="submissionType" value="COMPLETE" name="submissionType">
                全部完成
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="description">本次提交描述:</label>
            <textarea
              id="description"
              v-model="submissionDescription"
              rows="2"
              placeholder="请简要描述本次提交完成了哪些内容..."
            ></textarea>
          </div>

          <div class="form-group">
            <label for="content">提交内容:</label>
            <textarea
              id="content"
              v-model="submissionContent"
              rows="6"
              placeholder="请输入任务完成情况描述..."
            ></textarea>
          </div>

          <div class="form-group">
            <label for="fileUrl">文件链接:</label>
            <input
              type="text"
              id="fileUrl"
              v-model="fileUrl"
              placeholder="请输入文件链接（如GitHub仓库地址）"
            />
          </div>

          <div class="form-group">
            <label for="fileUpload">上传文件:</label>
            <div class="file-upload-container">
              <input
                type="file"
                id="fileUpload"
                ref="fileInput"
                @change="handleFileChange"
              />
              <div v-if="uploadedFile" class="uploaded-file-info">
                <span>已选择文件: {{ uploadedFile.name }}</span>
                <button class="remove-file-btn" @click="removeFile">移除</button>
              </div>
              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
                <div class="progress-bar" :style="{width: uploadProgress + '%'}"></div>
                <span class="progress-text">{{ uploadProgress }}%</span>
              </div>
              <div v-if="submissionFile" class="current-file">
                <span>当前文件: {{ submissionFile }}</span>
                <div class="file-actions">
                  <button @click="downloadFile(fileDownloadUrl, submissionFile)" class="download-link">下载</button>

                  <!-- PDF预览按钮，只对PDF文件显示 -->
                  <a v-if="isPdfFile(submissionFile)"
                     :href="fileDownloadUrl + '?preview=true'"
                     target="_blank"
                     class="preview-link">
                    预览PDF
                  </a>
                </div>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button
              class="submit-button"
              @click="submitTask"
              :disabled="submitting"
            >
              <span v-if="submitting" class="loading-icon"></span>
              <span v-else>{{ submitButtonText }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 提交历史记录 -->
      <div class="submission-history-section" v-if="submissionHistories.length > 0">
        <h2 class="section-title">提交历史</h2>
        <div class="history-list">
          <div v-for="history in submissionHistories" :key="history.historyId" class="history-item"
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

            <div v-if="history.feedback" class="history-feedback">
              <h4>企业反馈:</h4>
              <p>{{ history.feedback }}</p>
              <div class="feedback-time" v-if="history.formattedReviewTime">
                反馈时间: {{ history.formattedReviewTime }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import RatingDialog from '../Common/RatingDialog.vue';

export default {
  components: {
    RatingDialog
  },
  data() {
    return {
      task: {},
      submission: {},
      loading: true,
      submitting: false,
      submissionContent: "",
      submissionDescription: "",
      submissionType: "PARTIAL", // 默认为部分完成
      fileUrl: "",
      studentId: null,
      taskId: null,
      uploadedFile: null,
      uploadProgress: 0,
      submissionFile: null,
      fileDownloadUrl: null,
      submissionHistories: [], // 提交历史记录
      showRatingDialog: false
    };
  },
  computed: {
    canSubmit() {
      // 在未提交、已提交待审核或需要重新提交状态下都能提交
      return this.submission &&
        (this.submission.status === 'NOT_SUBMITTED' ||
         this.submission.status === 'SUBMITTED' ||
         this.submission.status === 'NEEDS_REVISION');
    },
    submitButtonText() {
      // 根据状态显示不同的按钮文本
      if (this.submission && this.submission.status === 'SUBMITTED') {
        return '修改提交';
      }
      return '提交任务';
    },
    // 显示提交次数
    submissionCountText() {
      if (!this.submission || !this.submission.submissionCount) return '首次提交';
      return `第 ${this.submission.submissionCount} 次提交`;
    }
  },
  created() {
    this.studentId = localStorage.getItem("userId");
    this.taskId = this.$route.params.taskId;

    // 检查任务是否在重新领取列表中
    const retakenTasks = JSON.parse(localStorage.getItem('retakenTasks') || '[]');
    const isRetaken = retakenTasks.includes(String(this.taskId));
    console.log(`任务 ${this.taskId} 是否被重新领取:`, isRetaken);

    if (!this.studentId || !this.taskId) {
      this.loading = false;
      return;
    }

    Promise.all([
      this.fetchTaskDetails(),
      this.fetchSubmissionDetails()
    ]).then(() => {
      // 如果有提交ID，获取提交历史
      if (this.submission && this.submission.submissionId) {
        return this.fetchSubmissionHistories(this.submission.submissionId);
      }
    }).finally(() => {
      this.loading = false;

      // 如果是重新领取的任务，将状态显示为未提交
      if (isRetaken && this.submission) {
        console.log('重新领取的任务，将状态显示为未提交');
        this.submission.status = 'NOT_SUBMITTED';
        this.submissionContent = '';
        this.fileUrl = '';
      }
    });
  },
  methods: {
    // 检查是否是PDF文件
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

    isPdfFile(fileName) {
      if (!fileName) return false;
      return fileName.toLowerCase().endsWith('.pdf');
    },
    fetchTaskDetails() {
      return axios
        .get(`http://localhost:8080/api/tasks/${this.taskId}`)
        .then((response) => {
          this.task = response.data;
        })
        .catch((error) => {
          console.error("无法获取任务详情:", error);
        });
    },
    fetchSubmissionDetails() {
      // 检查任务是否在重新领取列表中
      const retakenTasks = JSON.parse(localStorage.getItem('retakenTasks') || '[]');
      const isRetaken = retakenTasks.includes(String(this.taskId));

      return axios
        .get(`http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`)
        .then((response) => {
          this.submission = response.data;

          // 如果是重新领取的任务，将状态设置为未提交，不预填充表单
          if (isRetaken) {
            console.log('重新领取的任务，将状态设置为未提交');
            this.submission.status = 'NOT_SUBMITTED';
            this.submissionContent = '';
            this.fileUrl = '';
          } else {
            // 如果不是重新领取的任务，正常预填充表单
            if (this.submission.submissionContent) {
              this.submissionContent = this.submission.submissionContent;
            }
            if (this.submission.fileUrl) {
              this.fileUrl = this.submission.fileUrl;

              // 检查是否有上传的文件
              if (this.submission.submissionId) {
                axios.get(`http://localhost:8080/api/files/submission/${this.submission.submissionId}`)
                  .then(response => {
                    // 检查是否有下载链接（上传的文件）
                    if (response.data.fileDownloadUri) {
                      this.submissionFile = response.data.fileName;
                      this.fileDownloadUrl = `http://localhost:8080${response.data.fileDownloadUri}`;
                      console.log('找到上传的文件:', this.submissionFile);
                    }
                    // 检查是否有外部链接
                    else if (response.data.externalUrl) {
                      // 外部链接已经在fileUrl中，不需要额外处理
                      console.log('找到外部链接:', response.data.externalUrl);
                    }
                  })
                  .catch(error => {
                    // 如果没有文件，忽略错误
                    console.log('没有找到上传的文件:', error.response?.data?.error || error.message);
                  });
              }
            }
          }
        })
        .catch((error) => {
          console.error("无法获取提交记录:", error);
          // 如果没有提交记录，创建一个默认的提交对象
          this.submission = {
            status: 'NOT_SUBMITTED'
          };
        });
    },
    handleFileChange(event) {
      this.uploadedFile = event.target.files[0];
      this.uploadProgress = 0;
    },

    removeFile() {
      this.uploadedFile = null;
      this.$refs.fileInput.value = '';
      this.uploadProgress = 0;
    },

    uploadFile() {
      return new Promise((resolve, reject) => {
        if (!this.uploadedFile) {
          console.error('文件对象为null或未定义');
          alert('文件对象为null或未定义，请重新选择文件');
          reject(new Error('文件对象为null或未定义'));
          return;
        }

        console.log('文件信息:', {
          name: this.uploadedFile.name,
          size: this.uploadedFile.size,
          type: this.uploadedFile.type,
          lastModified: this.uploadedFile.lastModified
        });

        // 检查是否有提交ID
        if (!this.submission || !this.submission.submissionId) {
          console.error('没有提交ID，无法上传文件');
          alert('请先提交任务内容，然后再上传文件');
          reject(new Error('没有提交ID'));
          return;
        }

        // 创建FormData对象
        const formData = new FormData();

        // 添加文件到FormData
        formData.append('file', this.uploadedFile);

        // 添加提交ID
        formData.append('submissionId', this.submission.submissionId);

        // 检查FormData是否包含文件
        let hasFile = false;
        for (let pair of formData.entries()) {
          console.log('表单数据:', pair[0], pair[1]);
          if (pair[0] === 'file' && pair[1] instanceof File) {
            hasFile = true;
            console.log('表单包含文件:', pair[1].name, pair[1].size, pair[1].type);
          }
        }

        if (!hasFile) {
          console.error('FormData中没有文件');
          alert('FormData中没有文件，请重新选择文件');
          reject(new Error('FormData中没有文件'));
          return;
        }

        // 使用XMLHttpRequest代替fetch或axios
        const xhr = new XMLHttpRequest();

        // 监听上传进度
        xhr.upload.onprogress = (event) => {
          if (event.lengthComputable) {
            this.uploadProgress = Math.round((event.loaded * 100) / event.total);
            console.log('上传进度:', this.uploadProgress + '%');
          }
        };

        // 监听请求完成
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const data = JSON.parse(xhr.responseText);
              this.submissionFile = data.fileName;
              this.fileDownloadUrl = `http://localhost:8080${data.fileDownloadUri}`;
              console.log('文件上传成功:', this.submissionFile);
              resolve(data.fileName);
            } catch (e) {
              console.error('解析响应失败:', e);
              reject(new Error('解析响应失败: ' + e.message));
            }
          } else {
            console.error('文件上传失败:', xhr.status, xhr.responseText);
            reject(new Error(`服务器响应错误: ${xhr.status}`));
          }
        };

        // 监听请求错误
        xhr.onerror = () => {
          console.error('网络错误');
          reject(new Error('网络错误'));
        };

        // 打开连接并发送请求
        xhr.open('POST', 'http://localhost:8080/api/files/upload', true);

        // 发送请求
        console.log('开始上传文件，提交ID:', this.submission.submissionId);
        xhr.send(formData);
      });
    },

    // 获取提交历史记录
    fetchSubmissionHistories(submissionId) {
      return axios.get(`http://localhost:8080/api/submission-history/submission/${submissionId}`)
        .then(response => {
          this.submissionHistories = response.data;
          console.log('获取到提交历史记录:', this.submissionHistories.length);
        })
        .catch(error => {
          console.error('获取提交历史记录失败:', error);
        });
    },

    // 创建提交历史记录
    async createSubmissionHistory() {
      if (!this.submission || !this.submission.submissionId) {
        console.error('没有提交ID，无法创建历史记录');
        return null;
      }

      try {
        const response = await axios.post('http://localhost:8080/api/submission-history', {
          submissionId: this.submission.submissionId,
          content: this.submissionContent,
          description: this.submissionDescription,
          submissionType: this.submissionType,
          fileUrl: this.fileUrl,
          uploadedFile: this.submissionFile
        });

        console.log('创建提交历史记录成功:', response.data);
        return response.data;
      } catch (error) {
        console.error('创建提交历史记录失败:', error);
        alert('创建提交历史记录失败: ' + (error.response?.data?.error || error.message));
        return null;
      }
    },

    async submitTask() {
      if (!this.submissionContent.trim()) {
        alert("请输入提交内容");
        return;
      }

      if (!this.submissionDescription.trim()) {
        alert("请输入本次提交描述");
        return;
      }

      this.submitting = true;

      try {
        // 检查是否有文件要上传但没有提交ID
        if (this.uploadedFile && (!this.submission || !this.submission.submissionId)) {
          console.log('有文件要上传，但没有提交ID，先创建提交记录');

          // 先提交任务内容，获取提交ID
          const response = await axios.post(
            `http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`,
            {
              content: this.submissionContent,
              fileUrl: this.fileUrl
            }
          );

          this.submission = response.data;
          console.log('创建提交记录成功，提交ID:', this.submission.submissionId);

          // 现在有了提交ID，可以上传文件
          if (!this.uploadedFile) {
            console.error('文件对象为null或未定义');
            alert('文件对象为null或未定义，请重新选择文件');
            return;
          }

          console.log('文件信息:', {
            name: this.uploadedFile.name,
            size: this.uploadedFile.size,
            type: this.uploadedFile.type,
            lastModified: this.uploadedFile.lastModified
          });

          const formData = new FormData();

          // 添加文件到FormData
          formData.append('file', this.uploadedFile);

          // 添加提交ID
          formData.append('submissionId', this.submission.submissionId);

          // 检查FormData是否包含文件
          let hasFile = false;
          for (let pair of formData.entries()) {
            console.log('表单数据:', pair[0], pair[1]);
            if (pair[0] === 'file' && pair[1] instanceof File) {
              hasFile = true;
              console.log('表单包含文件:', pair[1].name, pair[1].size, pair[1].type);
            }
          }

          if (!hasFile) {
            console.error('FormData中没有文件');
            alert('FormData中没有文件，请重新选择文件');
            return;
          }

          try {
            console.log('开始上传文件，提交ID:', this.submission.submissionId);

            // 使用我们的uploadFile方法上传文件
            const fileName = await this.uploadFile();

            if (fileName) {
              console.log('文件上传成功:', fileName);

              // 文件上传成功，清空上传控件
              this.uploadedFile = null;
              this.$refs.fileInput.value = '';

              // 创建提交历史记录
              await this.createSubmissionHistory();

              // 获取最新的提交记录和历史
              const refreshResponse = await axios.get(
                `http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`
              );
              this.submission = refreshResponse.data;
              await this.fetchSubmissionHistories(this.submission.submissionId);

              alert("任务提交成功！文件已上传。");

              // 清空表单
              if (this.submissionType === "COMPLETE") {
                this.submissionContent = "";
                this.submissionDescription = "";
                this.fileUrl = "";
              }
            } else {
              throw new Error('文件上传失败，没有返回文件名');
            }
          } catch (uploadError) {
            console.error('文件上传失败:', uploadError);
            alert('任务内容已提交，但文件上传失败: ' + (uploadError.response?.data?.error || uploadError.message));
          }
        } else if (!this.uploadedFile) {
          // 没有文件要上传
          if (!this.submission || !this.submission.submissionId) {
            // 如果没有提交ID，先创建提交记录
            const response = await axios.post(
              `http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`,
              {
                content: this.submissionContent,
                fileUrl: this.fileUrl
              }
            );

            this.submission = response.data;
            console.log('创建提交记录成功，提交ID:', this.submission.submissionId);
          }

          // 创建提交历史记录
          await this.createSubmissionHistory();

          // 获取最新的提交记录和历史
          const refreshResponse = await axios.get(
            `http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`
          );
          this.submission = refreshResponse.data;
          await this.fetchSubmissionHistories(this.submission.submissionId);

          alert("任务提交成功！");

          // 清空表单
          if (this.submissionType === "COMPLETE") {
            this.submissionContent = "";
            this.submissionDescription = "";
            this.fileUrl = "";
          }
        } else {
          // 已有提交ID，直接上传文件
          const fileName = await this.uploadFile();
          if (fileName) {
            // 文件上传成功
            this.uploadedFile = null;
            this.$refs.fileInput.value = '';

            // 创建提交历史记录
            await this.createSubmissionHistory();

            // 更新提交记录和历史
            const refreshResponse = await axios.get(
              `http://localhost:8080/api/students/tasks/submissions/${this.studentId}/tasks/${this.taskId}`
            );
            this.submission = refreshResponse.data;
            await this.fetchSubmissionHistories(this.submission.submissionId);

            alert("文件上传成功！");

            // 清空表单
            if (this.submissionType === "COMPLETE") {
              this.submissionContent = "";
              this.submissionDescription = "";
              this.fileUrl = "";
            }
          } else {
            alert("文件上传失败，请重试。");
          }
        }
      } catch (error) {
        console.error("提交任务失败:", error);
        alert("提交任务失败，请重试: " + (error.response?.data?.error || error.message));
      } finally {
        this.submitting = false;
      }
    },
    goBack() {
      this.$router.go(-1);
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    // 打开评分对话框
    openRatingDialog() {
      this.showRatingDialog = true;
    },
    // 处理任务评分完成事件
    onTaskRated(ratingData) {
      alert('评分成功！感谢您的反馈。');
    },
    // 重新接取任务
    retakeTask() {
      if (!confirm('确定要重新接取该任务吗？您需要重新提交任务内容。')) {
        return;
      }

      console.log('开始重新接取任务:', {
        taskId: this.taskId,
        studentId: this.studentId
      });

      // 使用后端的重新接取API
      axios.post(`http://localhost:8080/api/students/tasks/${this.taskId}/retake/${this.studentId}`)
        .then(response => {
          console.log('重新接取任务成功:', response.data);

          // 更新本地提交对象
          this.submission = response.data;

          // 清空表单内容
          this.submissionContent = '';
          this.fileUrl = '';
          this.uploadedFile = null;
          if (this.$refs.fileInput) {
            this.$refs.fileInput.value = '';
          }

          alert('重新接取任务成功！请重新提交任务内容。');

          // 刷新页面显示最新状态
          this.fetchSubmissionDetails();
          return response.data;
        })
        .catch(error => {
          console.error('重新接取任务失败:', error);
          console.error('错误详情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status,
            statusText: error.response?.statusText
          });
          alert(`重新接取任务失败，错误信息: ${error.response?.data?.message || error.message}`);
        });
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
          return '任务通过';
        case 'SUBMITTED':
          return '已提交待审核';
        case 'NEEDS_REVISION':
          return '需要重新提交';
        case 'NOT_SUBMITTED':
        default:
          return '未提交';
      }
    }
  }
};
</script>

<style scoped>
/* 已完成任务样式 */
.completed-task-section {
  background-color: #f0f9eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e1f3d8;
}

.rating-display, .feedback-display {
  margin-bottom: 20px;
}

.rating-display h3, .feedback-display h3 {
  font-size: 16px;
  color: #67c23a;
  margin-bottom: 10px;
}

.stars-container {
  display: flex;
  align-items: center;
}

.star {
  font-size: 24px;
  color: #dcdfe6;
  margin-right: 5px;
}

.star.active {
  color: #e6a23c;
}

.rating-value {
  margin-left: 10px;
  font-weight: bold;
  color: #303133;
}

.feedback-content {
  background-color: white;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e1f3d8;
  color: #606266;
  line-height: 1.6;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.rate-task-button,
.retake-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.rate-task-button {
  background-color: #67c23a;
  border-color: #67c23a;
}

.rate-task-button:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.retake-button:hover {
  background-color: #66b1ff;
}
.task-submission-container {
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 内容区域样式 */
.content-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
}

/* 任务详情样式 */
.task-details-section, .submission-status-section, .submission-form-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.task-info-card {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.task-title {
  font-size: 18px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #909399;
  margin-right: 5px;
}

.meta-value {
  color: #606266;
  font-weight: 500;
}

.task-description, .task-requirements {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
}

.task-requirements h4 {
  color: #303133;
  margin-bottom: 10px;
}

/* 任务状态样式 */
.status-card {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
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

.submission-date {
  font-size: 14px;
  color: #909399;
}

.feedback-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.feedback-section h4 {
  color: #303133;
  margin-top: 0;
  margin-bottom: 10px;
}

/* 提交表单样式 */
.submission-form {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fafafa;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-weight: 500;
}

.form-group textarea, .form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  transition: border-color 0.3s;
}

.form-group textarea:focus, .form-group input:focus {
  outline: none;
  border-color: #409EFF;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.submit-button {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
}

.submit-button:hover {
  background-color: #66b1ff;
}

.submit-button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.loading-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .task-meta {
    flex-direction: column;
    gap: 10px;
  }

  .status-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

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
}
.file-upload-container {
  margin-top: 10px;
}

.uploaded-file-info {
  display: flex;
  align-items: center;
  margin-top: 10px;
  padding: 8px;
  background-color: #f0f9ff;
  border-radius: 4px;
}

.remove-file-btn {
  margin-left: 10px;
  padding: 2px 8px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.upload-progress {
  margin-top: 10px;
  height: 20px;
  background-color: #eee;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background-color: #409EFF;
  transition: width 0.3s;
}

.progress-text {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  text-align: center;
  line-height: 20px;
  color: white;
  font-size: 12px;
  text-shadow: 0 0 2px rgba(0, 0, 0, 0.5);
}

.current-file {
  margin-top: 10px;
  padding: 8px;
  background-color: #f0f9ff;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.download-link {
  color: #409EFF;
  text-decoration: none;
}

.download-link:hover {
  text-decoration: underline;
}

.file-actions {
  display: flex;
  gap: 10px;
  margin-top: 5px;
}

.preview-link {
  color: #E6A23C;
  text-decoration: none;
  font-weight: 500;
}

.preview-link:hover {
  text-decoration: underline;
}

/* 提交历史记录样式 */
.submission-history-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-top: 20px;
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

/* 提交类型选择器样式 */
.submission-type-selector {
  margin-bottom: 20px;
}

.radio-group {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}

.radio-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.radio-label input[type="radio"] {
  margin-right: 5px;
}
</style>
