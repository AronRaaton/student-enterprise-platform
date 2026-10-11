<template>
  <div class="challenge-publish-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">发布技术挑战</h1>
    </div>

    <!-- 技术挑战发布表单 -->
    <div class="form-container">
      <div class="form-group">
        <label for="title">挑战标题 <span class="required">*</span></label>
        <input
          type="text"
          id="title"
          v-model="challenge.title"
          placeholder="请输入技术挑战标题"
          class="form-control"
          :class="{ 'is-invalid': submitted && !challenge.title }"
        />
        <div v-if="submitted && !challenge.title" class="invalid-feedback">
          请输入挑战标题
        </div>
      </div>

      <div class="form-group">
        <label for="description">挑战描述 <span class="required">*</span></label>
        <textarea
          id="description"
          v-model="challenge.description"
          placeholder="请详细描述技术挑战的内容、目标和评判标准"
          class="form-control"
          rows="6"
          :class="{ 'is-invalid': submitted && !challenge.description }"
        ></textarea>
        <div v-if="submitted && !challenge.description" class="invalid-feedback">
          请输入挑战描述
        </div>
      </div>

      <div class="form-group">
        <label for="difficulty">难度等级 <span class="required">*</span></label>
        <div class="difficulty-selector">
          <div
            v-for="level in 5"
            :key="level"
            class="difficulty-star"
            :class="{ active: level <= challenge.difficulty }"
            @click="challenge.difficulty = level"
          >
            ★
          </div>
          <span class="difficulty-text">{{ getDifficultyText(challenge.difficulty) }}</span>
        </div>
      </div>

      <div class="form-group">
        <label for="requiredSkills">所需技能 <span class="required">*</span></label>
        <textarea
          id="requiredSkills"
          v-model="challenge.requiredSkills"
          placeholder="请列出解决挑战所需的技能和知识"
          class="form-control"
          rows="4"
          :class="{ 'is-invalid': submitted && !challenge.requiredSkills }"
        ></textarea>
        <div v-if="submitted && !challenge.requiredSkills" class="invalid-feedback">
          请输入所需技能
        </div>
      </div>

      <div class="form-group">
        <label for="rewardDescription">奖励描述 <span class="required">*</span></label>
        <textarea
          id="rewardDescription"
          v-model="challenge.rewardDescription"
          placeholder="请描述完成挑战的奖励，如实习机会、推荐信、证书等"
          class="form-control"
          rows="3"
          :class="{ 'is-invalid': submitted && !challenge.rewardDescription }"
        ></textarea>
        <div v-if="submitted && !challenge.rewardDescription" class="invalid-feedback">
          请输入奖励描述
        </div>
      </div>

      <div class="form-group">
        <label for="deadline">截止日期</label>
        <input
          type="date"
          id="deadline"
          v-model="challenge.deadline"
          class="form-control"
        />
      </div>

      <div class="form-actions">
        <button class="cancel-btn" @click="goBack">取消</button>
        <button class="submit-btn" @click="submitChallenge" :disabled="isSubmitting">
          {{ isSubmitting ? '发布中...' : '发布挑战' }}
        </button>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccess" class="success-message">
      <div class="success-content">
        <div class="success-icon">✓</div>
        <h2>技术挑战发布成功！</h2>
        <p>您可以在技术挑战管理页面查看和管理该挑战</p>
        <div class="success-actions">
          <button class="primary-btn" @click="goToChallenges">查看我的挑战</button>
          <button class="secondary-btn" @click="publishAnother">发布另一个挑战</button>
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
      challenge: {
        enterpriseId: null,
        title: '',
        description: '',
        difficulty: 3,
        requiredSkills: '',
        rewardDescription: '',
        deadline: '',
        status: 'OPEN'
      },
      submitted: false,
      isSubmitting: false,
      showSuccess: false
    };
  },
  created() {
    // 获取企业ID
    this.challenge.enterpriseId = localStorage.getItem('userId');
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    getDifficultyText(level) {
      const texts = ['非常简单', '简单', '中等', '困难', '非常困难'];
      return texts[level - 1] || '中等';
    },
    async submitChallenge() {
      this.submitted = true;

      // 表单验证
      if (!this.challenge.title || !this.challenge.description || !this.challenge.requiredSkills || !this.challenge.rewardDescription) {
        return;
      }

      this.isSubmitting = true;

      try {
        const response = await axios.post('http://localhost:8080/api/challenges', this.challenge);
        console.log('技术挑战发布成功:', response.data);
        this.showSuccess = true;
      } catch (error) {
        console.error('技术挑战发布失败:', error);
        alert('技术挑战发布失败，请稍后重试');
      } finally {
        this.isSubmitting = false;
      }
    },
    goToChallenges() {
      this.$router.push({ name: 'EnterpriseChallenges' });
    },
    publishAnother() {
      this.challenge = {
        enterpriseId: localStorage.getItem('userId'),
        title: '',
        description: '',
        difficulty: 3,
        requiredSkills: '',
        rewardDescription: '',
        deadline: '',
        status: 'OPEN'
      };
      this.submitted = false;
      this.showSuccess = false;
    }
  }
};
</script>

<style scoped>
.challenge-publish-container {
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
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  flex-grow: 1;
}

.form-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #606266;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-control:focus {
  border-color: #409EFF;
  outline: none;
}

.form-control.is-invalid {
  border-color: #f56c6c;
}

.invalid-feedback {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 5px;
}

/* 难度选择器样式 */
.difficulty-selector {
  display: flex;
  align-items: center;
}

.difficulty-star {
  font-size: 24px;
  color: #dcdfe6;
  cursor: pointer;
  margin-right: 5px;
  transition: color 0.3s;
}

.difficulty-star:hover,
.difficulty-star.active {
  color: #f7ba2a;
}

.difficulty-text {
  margin-left: 10px;
  color: #606266;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #e9ebef;
}

.submit-btn {
  padding: 12px 25px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 成功提示样式 */
.success-message {
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

.success-content {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.success-icon {
  width: 60px;
  height: 60px;
  background-color: #67c23a;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
  margin: 0 auto 20px;
}

.success-content h2 {
  color: #303133;
  margin-bottom: 10px;
}

.success-content p {
  color: #606266;
  margin-bottom: 25px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.primary-btn {
  padding: 10px 20px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background-color: #e9ebef;
}
</style>
