<template>
  <div class="competition-detail-container module-page module-detail">
    <div class="back-button-container">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
    </div>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="!competition" class="error-container">
      <p>竞赛信息不存在或已被删除</p>
    </div>

    <div v-else class="competition-content">
      <div class="competition-header">
        <h1 class="competition-title">{{ competition.title }}</h1>
        <span class="competition-status" :class="getStatusClass(competition.status)">
          {{ getStatusText(competition.status) }}
        </span>
      </div>

      <div class="competition-info-card">
        <div class="info-section">
          <div class="info-item">
            <span class="info-label">主办方:</span>
            <span class="info-value">{{ competition.enterprise.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">竞赛时间:</span>
            <span class="info-value">{{ formatDate(competition.startDate) }} - {{ formatDate(competition.endDate) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">报名截止:</span>
            <span class="info-value">{{ formatDate(competition.registrationDeadline) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">参与人数上限:</span>
            <span class="info-value">{{ competition.maxParticipants }} 人</span>
          </div>
        </div>

        <!-- 学生视图的操作按钮 -->
        <div v-if="userRole === 'student'" class="action-section">
          <button
            v-if="competition.status === 'OPEN'"
            class="register-btn"
            @click="registerForCompetition"
            :disabled="isRegistered"
          >
            {{ isRegistered ? '已报名' : '立即报名' }}
          </button>
          <button
            v-if="isRegistered && (!competition.winnersSelected || competition.winnersSelected === false)"
            class="submit-btn"
            @click="submitWork"
          >
            提交作品
          </button>
        </div>

        <!-- 企业视图的操作按钮 -->
        <div v-if="userRole === 'enterprise' && competition.status === 'CLOSED' && (!competition.winnersSelected || competition.winnersSelected === false)" class="action-section">
          <button
            class="award-btn"
            @click="openAwardDialog"
          >
            评选获奖作品
          </button>
        </div>
      </div>

      <div class="competition-details">
        <div class="detail-section">
          <h2 class="section-title">竞赛介绍</h2>
          <div class="section-content" v-html="formatDescription(competition.description)"></div>
        </div>

        <div class="detail-section">
          <h2 class="section-title">竞赛奖励</h2>
          <div class="section-content" v-html="formatDescription(competition.rewards)"></div>
        </div>

        <div class="detail-section">
          <h2 class="section-title">参赛要求</h2>
          <div class="section-content" v-html="formatDescription(competition.requirements)"></div>
        </div>
      </div>

      <!-- 竞赛获奖结果展示 -->
      <div v-if="competition.winnersSelected" class="winners-section">
        <h2 class="section-title">竞赛获奖结果</h2>
        <div class="winners-list">
          <div v-if="winners.firstPrize" class="winner-item first-prize">
            <div class="prize-badge">一等奖</div>
            <div class="winner-info">
              <h3 class="winner-name">{{ winners.firstPrize.studentName }}</h3>
              <p class="submission-time">提交时间: {{ formatDateTime(winners.firstPrize.submissionTime) }}</p>
              <p v-if="winners.firstPrize.content" class="submission-content">{{ winners.firstPrize.content }}</p>
              <div v-if="winners.firstPrize.uploadedFile" class="submission-file">
                <span class="file-icon">📎</span>
                <a href="#" @click.prevent="downloadFile(winners.firstPrize.submissionId, winners.firstPrize.uploadedFile)" class="file-name">{{ winners.firstPrize.uploadedFile }}</a>
              </div>
              <div v-if="winners.firstPrize.fileUrl" class="submission-link">
                <a :href="winners.firstPrize.fileUrl" target="_blank">{{ winners.firstPrize.fileUrl }}</a>
              </div>
            </div>
          </div>

          <div v-if="winners.secondPrize" class="winner-item second-prize">
            <div class="prize-badge">二等奖</div>
            <div class="winner-info">
              <h3 class="winner-name">{{ winners.secondPrize.studentName }}</h3>
              <p class="submission-time">提交时间: {{ formatDateTime(winners.secondPrize.submissionTime) }}</p>
              <p v-if="winners.secondPrize.content" class="submission-content">{{ winners.secondPrize.content }}</p>
              <div v-if="winners.secondPrize.uploadedFile" class="submission-file">
                <span class="file-icon">📎</span>
                <a href="#" @click.prevent="downloadFile(winners.secondPrize.submissionId, winners.secondPrize.uploadedFile)" class="file-name">{{ winners.secondPrize.uploadedFile }}</a>
              </div>
              <div v-if="winners.secondPrize.fileUrl" class="submission-link">
                <a :href="winners.secondPrize.fileUrl" target="_blank">{{ winners.secondPrize.fileUrl }}</a>
              </div>
            </div>
          </div>

          <div v-if="winners.thirdPrize" class="winner-item third-prize">
            <div class="prize-badge">三等奖</div>
            <div class="winner-info">
              <h3 class="winner-name">{{ winners.thirdPrize.studentName }}</h3>
              <p class="submission-time">提交时间: {{ formatDateTime(winners.thirdPrize.submissionTime) }}</p>
              <p v-if="winners.thirdPrize.content" class="submission-content">{{ winners.thirdPrize.content }}</p>
              <div v-if="winners.thirdPrize.uploadedFile" class="submission-file">
                <span class="file-icon">📎</span>
                <a href="#" @click.prevent="downloadFile(winners.thirdPrize.submissionId, winners.thirdPrize.uploadedFile)" class="file-name">{{ winners.thirdPrize.uploadedFile }}</a>
              </div>
              <div v-if="winners.thirdPrize.fileUrl" class="submission-link">
                <a :href="winners.thirdPrize.fileUrl" target="_blank">{{ winners.thirdPrize.fileUrl }}</a>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 学生视图：已提交作品 (仅当已报名且有提交且未评奖时显示) -->
      <div v-if="userRole === 'student' && isRegistered && submissions.length > 0 && (!competition.winnersSelected || competition.winnersSelected === false)" class="submissions-section">
        <h2 class="section-title">我的提交</h2>
        <div class="submissions-list">
          <div v-for="(submission, index) in submissions" :key="submission.submissionId" class="submission-item">
            <div class="submission-header">
              <h3 class="submission-title">提交 #{{ index + 1 }}</h3>
              <span class="submission-date">{{ formatDateTime(submission.submissionTime) }}</span>
            </div>
            <div class="submission-content">
              <p v-if="submission.content">{{ submission.content }}</p>
              <div v-if="submission.uploadedFile" class="submission-file">
                <span class="file-icon">📎</span>
                <a href="#" @click.prevent="downloadFile(submission.submissionId, submission.uploadedFile)" class="file-name">{{ submission.uploadedFile }}</a>
              </div>
              <div v-if="submission.fileUrl" class="submission-link">
                <a :href="submission.fileUrl" target="_blank">{{ submission.fileUrl }}</a>
              </div>
            </div>
            <div v-if="submission.status === 'GRADED'" class="submission-feedback">
              <div class="feedback-score">
                <span class="score-label">得分:</span>
                <span class="score-value">{{ submission.score }}</span>
              </div>
              <div v-if="submission.feedback" class="feedback-content">
                <span class="feedback-label">反馈:</span>
                <p class="feedback-text">{{ submission.feedback }}</p>
              </div>
            </div>
            <div class="submission-actions">
              <button class="edit-btn" @click="editSubmission(submission)">修改提交</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 企业视图：所有提交作品 (仅企业可见) -->
      <div v-if="userRole === 'enterprise' && allSubmissions.length > 0" class="all-submissions-section">
        <h2 class="section-title">参赛作品</h2>
        <div class="submissions-list">
          <div v-for="(submission, index) in allSubmissions" :key="submission.submissionId" class="submission-item">
            <div class="submission-header">
              <h3 class="submission-title">{{ submission.registration && submission.registration.student ? submission.registration.student.name : '学生' }} 的提交</h3>
              <span class="submission-date">{{ formatDateTime(submission.submissionTime) }}</span>
            </div>
            <div class="submission-content">
              <p v-if="submission.content">{{ submission.content }}</p>
              <div v-if="submission.uploadedFile" class="submission-file">
                <span class="file-icon">📎</span>
                <a href="#" @click.prevent="downloadFile(submission.submissionId, submission.uploadedFile)" class="file-name">{{ submission.uploadedFile }}</a>
              </div>
              <div v-if="submission.fileUrl" class="submission-link">
                <a :href="submission.fileUrl" target="_blank">{{ submission.fileUrl }}</a>
              </div>
            </div>
            <div v-if="showAwardSelection && !competition.winnersSelected" class="award-selection">
              <button
                class="award-btn first-prize-btn"
                @click="selectWinner('first', submission.submissionId)"
                :class="{ 'selected': selectedWinners.first === submission.submissionId }"
              >
                一等奖
              </button>
              <button
                class="award-btn second-prize-btn"
                @click="selectWinner('second', submission.submissionId)"
                :class="{ 'selected': selectedWinners.second === submission.submissionId }"
              >
                二等奖
              </button>
              <button
                class="award-btn third-prize-btn"
                @click="selectWinner('third', submission.submissionId)"
                :class="{ 'selected': selectedWinners.third === submission.submissionId }"
              >
                三等奖
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提交作品对话框 -->
    <div v-if="showSubmitDialog" class="dialog-overlay">
      <div class="dialog-container">
        <div class="dialog-header">
          <h2>{{ editMode ? '修改提交' : '提交竞赛作品' }}</h2>
          <button class="close-btn" @click="closeSubmitDialog">×</button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label for="content">作品说明</label>
            <textarea
              id="content"
              v-model="submissionForm.content"
              rows="5"
              placeholder="请描述您的作品..."
            ></textarea>
          </div>
          <div class="form-group">
            <label for="fileUrl">外部链接 (可选)</label>
            <input
              type="text"
              id="fileUrl"
              v-model="submissionForm.fileUrl"
              placeholder="例如: GitHub 仓库链接"
            />
          </div>
          <div class="form-group">
            <label for="file">上传文件 (可选)</label>
            <input
              type="file"
              id="file"
              @change="handleFileChange"
            />
            <p v-if="submissionForm.currentFile" class="current-file">
              当前文件: {{ submissionForm.currentFile }}
            </p>
            <p class="file-hint">支持 PDF, ZIP, DOC, DOCX, PPT, PPTX 等格式，最大 30MB</p>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="closeSubmitDialog">取消</button>
          <button class="submit-btn" @click="submitWorkForm" :disabled="submitting">
            {{ submitting ? '提交中...' : (editMode ? '保存修改' : '提交') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 评奖对话框 -->
    <div v-if="showAwardDialog" class="dialog-overlay">
      <div class="dialog-container">
        <div class="dialog-header">
          <h2>评选获奖作品</h2>
          <button class="close-btn" @click="closeAwardDialog">×</button>
        </div>
        <div class="dialog-content">
          <p>请在参赛作品列表中选择一等奖、二等奖和三等奖获奖作品。</p>
          <p>注意：评奖后将无法更改，请谨慎操作。</p>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="closeAwardDialog">取消</button>
          <button class="award-confirm-btn" @click="confirmAwards" :disabled="!canConfirmAwards || submitting">
            {{ submitting ? '提交中...' : '确认评奖' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CompetitionDetail',
  data() {
    return {
      competition: null,
      loading: true,
      userRole: null,
      userId: null,
      registration: null,
      submissions: [],
      allSubmissions: [],
      isRegistered: false,
      showSubmitDialog: false,
      showAwardDialog: false,
      showAwardSelection: false,
      editMode: false,
      currentSubmissionId: null,
      submissionForm: {
        content: '',
        fileUrl: '',
        file: null,
        currentFile: null
      },
      submitting: false,
      winners: {},
      selectedWinners: {
        first: null,
        second: null,
        third: null
      }
    };
  },
  computed: {
    canConfirmAwards() {
      // 至少需要选择一等奖
      return this.selectedWinners.first !== null;
    }
  },
  created() {
    this.userRole = localStorage.getItem('role');
    this.userId = localStorage.getItem('userId');
    this.fetchCompetitionDetails();
  },
  methods: {
    fetchCompetitionDetails() {
      const competitionId = this.$route.params.competitionId;

      // 获取竞赛详情
      axios.get(`http://localhost:8080/api/competitions/${competitionId}`)
        .then(response => {
          this.competition = response.data;

          // 如果竞赛已评奖，获取获奖信息
          if (this.competition.winnersSelected) {
            this.fetchWinners(competitionId);
          }

          // 如果是学生视图，检查是否已报名
          if (this.userRole === 'student') {
            this.checkRegistrationStatus();
          }
          // 如果是企业视图，获取所有提交
          else if (this.userRole === 'enterprise') {
            this.fetchAllSubmissions(competitionId);
          } else {
            this.loading = false;
          }
        })
        .catch(error => {
          console.error('获取竞赛详情失败:', error);
          this.loading = false;
        });
    },
    fetchWinners(competitionId) {
      axios.get(`http://localhost:8080/api/competitions/${competitionId}/winners`)
        .then(response => {
          this.winners = response.data;
        })
        .catch(error => {
          console.error('获取获奖信息失败:', error);
        });
    },
    checkRegistrationStatus() {
      const competitionId = this.$route.params.competitionId;

      // 获取竞赛的所有报名记录
      axios.get(`http://localhost:8080/api/competitions/${competitionId}/registrations`)
        .then(response => {
          const registrations = response.data;

          // 查找当前学生的报名记录
          const myRegistration = registrations.find(reg =>
            reg.student && reg.student.studentId === parseInt(this.userId)
          );

          if (myRegistration) {
            this.isRegistered = true;
            this.registration = myRegistration;
            this.fetchSubmissions(myRegistration.registrationId);
          } else {
            this.isRegistered = false;
            this.loading = false;
          }
        })
        .catch(error => {
          console.error('检查报名状态失败:', error);
          this.loading = false;
        });
    },
    fetchSubmissions(registrationId) {
      // 获取报名记录的所有提交
      axios.get(`http://localhost:8080/api/competitions/submissions/${registrationId}`)
        .then(response => {
          this.submissions = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取提交记录失败:', error);
          this.loading = false;
        });
    },
    fetchAllSubmissions(competitionId) {
      // 获取竞赛的所有提交作品
      axios.get(`http://localhost:8080/api/competitions/${competitionId}/submissions`)
        .then(response => {
          this.allSubmissions = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取所有提交失败:', error);
          this.loading = false;
        });
    },
    registerForCompetition() {
      if (!this.userId) {
        alert('请先登录');
        return;
      }

      const competitionId = this.$route.params.competitionId;

      axios.post(`http://localhost:8080/api/competitions/${competitionId}/register?studentId=${this.userId}`)
        .then(response => {
          alert('报名成功');
          this.checkRegistrationStatus();
        })
        .catch(error => {
          alert('报名失败: ' + (error.response?.data || '未知错误'));
        });
    },
    submitWork() {
      this.editMode = false;
      this.submissionForm = {
        content: '',
        fileUrl: '',
        file: null,
        currentFile: null
      };
      this.showSubmitDialog = true;
    },
    editSubmission(submission) {
      this.editMode = true;
      this.currentSubmissionId = submission.submissionId;
      this.submissionForm = {
        content: submission.content || '',
        fileUrl: submission.fileUrl || '',
        file: null,
        currentFile: submission.uploadedFile
      };
      this.showSubmitDialog = true;
    },
    closeSubmitDialog() {
      this.showSubmitDialog = false;
      this.editMode = false;
      this.currentSubmissionId = null;
      this.submissionForm = {
        content: '',
        fileUrl: '',
        file: null,
        currentFile: null
      };
    },
    handleFileChange(event) {
      this.submissionForm.file = event.target.files[0];
    },
    submitWorkForm() {
      if (!this.registration) {
        alert('请先报名竞赛');
        return;
      }

      this.submitting = true;
      const formData = new FormData();

      if (this.submissionForm.content) {
        formData.append('content', this.submissionForm.content);
      }

      if (this.submissionForm.fileUrl) {
        formData.append('fileUrl', this.submissionForm.fileUrl);
      }

      if (this.submissionForm.file) {
        formData.append('file', this.submissionForm.file);
      }

      // 如果是编辑模式，使用PUT请求更新提交
      if (this.editMode && this.currentSubmissionId) {
        axios.put(`http://localhost:8080/api/competitions/submissions/${this.currentSubmissionId}`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
          .then(response => {
            alert('作品更新成功');
            this.closeSubmitDialog();
            this.fetchSubmissions(this.registration.registrationId);
          })
          .catch(error => {
            alert('更新失败: ' + (error.response?.data || '未知错误'));
          })
          .finally(() => {
            this.submitting = false;
          });
      } else {
        // 新提交作品
        formData.append('registrationId', this.registration.registrationId);

        axios.post('http://localhost:8080/api/competitions/submissions', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
          .then(response => {
            alert('作品提交成功');
            this.closeSubmitDialog();
            this.fetchSubmissions(this.registration.registrationId);
          })
          .catch(error => {
            alert('提交失败: ' + (error.response?.data || '未知错误'));
          })
          .finally(() => {
            this.submitting = false;
          });
      }
    },
    downloadFile(submissionId, fileName) {
      // 实现文件下载功能
      const competitionId = this.$route.params.competitionId;
      const downloadUrl = `http://localhost:8080/uploads/competitions/${competitionId}/${submissionId}/${fileName}`;

      // 创建一个临时链接并模拟点击
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    openAwardDialog() {
      this.showAwardDialog = true;
      this.showAwardSelection = true;
    },
    closeAwardDialog() {
      this.showAwardDialog = false;
      this.showAwardSelection = false;
    },
    selectWinner(prize, submissionId) {
      // 如果已经选了这个作品为其他奖项，先取消选择
      if (this.selectedWinners.first === submissionId && prize !== 'first') {
        this.selectedWinners.first = null;
      }
      if (this.selectedWinners.second === submissionId && prize !== 'second') {
        this.selectedWinners.second = null;
      }
      if (this.selectedWinners.third === submissionId && prize !== 'third') {
        this.selectedWinners.third = null;
      }

      // 设置新的选择
      if (this.selectedWinners[prize] === submissionId) {
        // 如果点击的是已选中的，取消选择
        this.selectedWinners[prize] = null;
      } else {
        // 如果这个奖项已经选了其他作品，先取消原来的选择
        this.selectedWinners[prize] = submissionId;
      }
    },
    confirmAwards() {
      if (!this.canConfirmAwards) {
        alert('请至少选择一等奖获奖作品');
        return;
      }

      this.submitting = true;
      const competitionId = this.$route.params.competitionId;

      // 构建请求参数
      let url = `http://localhost:8080/api/competitions/${competitionId}/winners?firstPrizeSubmissionId=${this.selectedWinners.first}`;

      if (this.selectedWinners.second) {
        url += `&secondPrizeSubmissionId=${this.selectedWinners.second}`;
      }

      if (this.selectedWinners.third) {
        url += `&thirdPrizeSubmissionId=${this.selectedWinners.third}`;
      }

      axios.post(url)
        .then(response => {
          alert('竞赛获奖作品设置成功');
          this.closeAwardDialog();
          // 重新加载竞赛详情
          this.fetchCompetitionDetails();
        })
        .catch(error => {
          alert('设置获奖作品失败: ' + (error.response?.data || '未知错误'));
        })
        .finally(() => {
          this.submitting = false;
        });
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return '';
      const date = new Date(dateTimeString);
      return date.toLocaleString('zh-CN');
    },
    formatDescription(text) {
      if (!text) return '';
      return text.replace(/\n/g, '<br>');
    },
    getStatusText(status) {
      switch (status) {
        case 'OPEN': return '报名中';
        case 'ONGOING': return '进行中';
        case 'CLOSED': return '已结束';
        case 'CANCELLED': return '已取消';
        default: return '未知状态';
      }
    },
    getStatusClass(status) {
      switch (status) {
        case 'OPEN': return 'status-open';
        case 'ONGOING': return 'status-ongoing';
        case 'CLOSED': return 'status-closed';
        case 'CANCELLED': return 'status-cancelled';
        default: return '';
      }
    },
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.competition-detail-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.back-button-container {
  margin-bottom: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
}

.back-icon {
  margin-right: 5px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 40px;
  color: #f56c6c;
}

.competition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.competition-title {
  margin: 0;
  font-size: 24px;
}

.competition-status {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
}

.status-open {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-ongoing {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-closed {
  background-color: #f4f4f5;
  color: #909399;
}

.status-cancelled {
  background-color: #fef0f0;
  color: #f56c6c;
}

.competition-info-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
}

.info-section {
  flex: 1;
}

.info-item {
  margin-bottom: 10px;
}

.info-label {
  font-weight: bold;
  color: #606266;
  margin-right: 8px;
}

.action-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}

.register-btn, .submit-btn, .award-btn, .edit-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
}

.register-btn {
  background-color: #409eff;
  color: white;
}

.register-btn:hover {
  background-color: #66b1ff;
}

.register-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.submit-btn {
  background-color: #67c23a;
  color: white;
}

.submit-btn:hover {
  background-color: #85ce61;
}

.award-btn {
  background-color: #e6a23c;
  color: white;
}

.award-btn:hover {
  background-color: #ebb563;
}

.edit-btn {
  background-color: #909399;
  color: white;
  font-size: 14px;
  padding: 6px 12px;
}

.edit-btn:hover {
  background-color: #a6a9ad;
}

.competition-details {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-content {
  line-height: 1.6;
  color: #606266;
}

/* 获奖结果区域 */
.winners-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.winners-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.winner-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.first-prize {
  background-color: #fff9f0;
  border-color: #ffd591;
}

.second-prize {
  background-color: #f9f9f9;
  border-color: #d9d9d9;
}

.third-prize {
  background-color: #fff8f6;
  border-color: #ffbb96;
}

.prize-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 8px 16px;
  font-weight: bold;
  color: white;
  font-size: 14px;
}

.first-prize .prize-badge {
  background-color: #e6a23c;
}

.second-prize .prize-badge {
  background-color: #909399;
}

.third-prize .prize-badge {
  background-color: #f56c6c;
}

.winner-info {
  margin-top: 10px;
}

.winner-name {
  font-size: 18px;
  margin: 0 0 10px 0;
}

.submission-time {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.submission-content {
  margin-bottom: 10px;
  line-height: 1.5;
}

/* 提交作品区域 */
.submissions-section, .all-submissions-section {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.submissions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.submission-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.submission-title {
  margin: 0;
  font-size: 16px;
}

.submission-date {
  font-size: 14px;
  color: #909399;
}

.submission-content {
  margin-bottom: 10px;
}

.submission-file, .submission-link {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.file-icon {
  margin-right: 5px;
}

.file-name {
  color: #409eff;
  text-decoration: none;
}

.file-name:hover {
  text-decoration: underline;
}

.submission-feedback {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.feedback-score {
  margin-bottom: 5px;
}

.score-label, .feedback-label {
  font-weight: bold;
  margin-right: 5px;
}

.score-value {
  font-size: 16px;
  color: #f56c6c;
}

.feedback-text {
  margin: 5px 0 0;
  color: #606266;
}

.submission-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

/* 评奖选择区域 */
.award-selection {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.first-prize-btn, .second-prize-btn, .third-prize-btn {
  padding: 6px 12px;
  border: 1px solid;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.first-prize-btn {
  background-color: #fff9f0;
  border-color: #e6a23c;
  color: #e6a23c;
}

.second-prize-btn {
  background-color: #f9f9f9;
  border-color: #909399;
  color: #909399;
}

.third-prize-btn {
  background-color: #fff8f6;
  border-color: #f56c6c;
  color: #f56c6c;
}

.first-prize-btn:hover {
  background-color: #e6a23c;
  color: white;
}

.second-prize-btn:hover {
  background-color: #909399;
  color: white;
}

.third-prize-btn:hover {
  background-color: #f56c6c;
  color: white;
}

.first-prize-btn.selected {
  background-color: #e6a23c;
  color: white;
}

.second-prize-btn.selected {
  background-color: #909399;
  color: white;
}

.third-prize-btn.selected {
  background-color: #f56c6c;
  color: white;
}

/* 对话框样式 */
.dialog-overlay {
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

.dialog-container {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-header h2 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}

.dialog-content {
  padding: 20px;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input, .form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.current-file {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.file-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  padding: 10px 20px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn, .submit-btn, .award-confirm-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.cancel-btn {
  background-color: #f4f4f5;
  color: #909399;
}

.cancel-btn:hover {
  background-color: #e9e9eb;
}

.submit-btn {
  background-color: #67c23a;
  color: white;
}

.submit-btn:hover {
  background-color: #85ce61;
}

.award-confirm-btn {
  background-color: #e6a23c;
  color: white;
}

.award-confirm-btn:hover {
  background-color: #ebb563;
}

.submit-btn:disabled, .award-confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
