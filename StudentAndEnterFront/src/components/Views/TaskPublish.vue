<template>
  <div class="task-publish-container module-page module-form">
    <!-- 成功提示 -->
    <div v-if="showSuccess" class="success-message">
      <div class="success-icon">✓</div>
      <h3>任务发布成功！</h3>
      <p>您可以在企业主页查看已发布的任务</p>
      <div class="success-actions">
        <button class="primary-btn" @click="goToHome">返回主页</button>
        <button class="secondary-btn" @click="publishAnother">继续发布</button>
      </div>
    </div>

    <div v-else>
      <div class="header">
        <button type="button" class="back-button" @click="goToHome">← 返回</button>
        <h1>发布任务</h1>
        <p>发布任务让学生参与，提供实际项目经验</p>
      </div>

      <div class="task-form">
        <form @submit.prevent="submitTask">
        <div class="form-section">
          <h2 class="section-title">基本信息</h2>

          <div class="form-group">
            <label for="summary">任务标题 <span class="required">*</span></label>
            <input
              type="text"
              id="summary"
              v-model="task.summary"
              placeholder="请输入任务标题（简洁明了）"
              :class="{ 'error': validationErrors.summary }"
            />
            <span v-if="validationErrors.summary" class="error-message">{{ validationErrors.summary }}</span>
          </div>

          <div class="form-group">
            <label for="techStack">技术栈要求 <span class="required">*</span></label>
            <TechStackSelector
              v-model="task.techStack"
              @update:modelValue="value => task.techStack = value"
              :class="{ 'error': validationErrors.techStack }"
            />
            <span v-if="validationErrors.techStack" class="error-message">{{ validationErrors.techStack }}</span>
            <div class="form-tip">点击"+"添加技术栈，可选择常用技术或自定义输入</div>
          </div>
        </div>

        <div class="form-section">
          <h2 class="section-title">任务详情</h2>

          <div class="form-group">
            <label for="content">任务描述 <span class="required">*</span></label>
            <textarea
              id="content"
              v-model="task.content"
              rows="8"
              placeholder="详细描述任务的背景、目标和具体要求..."
              :class="{ 'error': validationErrors.content }"
            ></textarea>
            <span v-if="validationErrors.content" class="error-message">{{ validationErrors.content }}</span>
          </div>

          <div class="form-group">
            <label for="difficulty">难度级别 <span class="required">*</span></label>
            <div :class="{ 'error-container': validationErrors.difficulty }">
              <StarRating
                v-model="task.difficulty"
                @update:modelValue="value => task.difficulty = value"
                id="difficulty"
              />
            </div>
            <span v-if="validationErrors.difficulty" class="error-message">{{ validationErrors.difficulty }}</span>
            <div class="form-tip">点击星星选择任务难度，1星为最简单，5星为最困难</div>
          </div>
        </div>

        <div class="form-section">
          <h2 class="section-title">截止日期</h2>

          <div class="form-group">
            <label for="deadline">截止日期 <span class="required">*</span></label>
            <input
              type="date"
              id="deadline"
              v-model="task.deadline"
              :min="minDate"
              :class="{ 'error': validationErrors.deadline }"
            />
            <span v-if="validationErrors.deadline" class="error-message">{{ validationErrors.deadline }}</span>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="cancel">取消</button>
          <button type="submit" class="submit-btn" :disabled="isSubmitting">
            {{ isSubmitting ? '发布中...' : '发布任务' }}
          </button>
        </div>
      </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import TechStackSelector from '../Common/TechStackSelector.vue';
import StarRating from '../Common/StarRating.vue';

export default {
  components: {
    TechStackSelector,
    StarRating
  },
  data() {
    return {
      task: {
        summary: '',
        techStack: '',
        content: '',
        difficulty: 0,
        deadline: '',
        enterpriseId: null
      },
      validationErrors: {},
      isSubmitting: false,
      showSuccess: false,
      minDate: this.getFormattedDate(new Date())
    };
  },
  created() {
    // 从localStorage获取企业ID
    this.task.enterpriseId = localStorage.getItem('userId');
  },
  methods: {
    getFormattedDate(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    validateForm() {
      this.validationErrors = {};
      let isValid = true;

      if (!this.task.summary.trim()) {
        this.validationErrors.summary = '请输入任务标题';
        isValid = false;
      }

      // 打印技术栈值以进行调试
      console.log('验证技术栈:', {
        techStack: this.task.techStack,
        type: typeof this.task.techStack,
        isEmpty: !this.task.techStack || !this.task.techStack.trim()
      });

      // 确保技术栈存在且不为空
      if (!this.task.techStack || !this.task.techStack.trim()) {
        this.validationErrors.techStack = '请选择至少一个技术栈';
        isValid = false;
      }

      if (!this.task.content.trim()) {
        this.validationErrors.content = '请输入任务描述';
        isValid = false;
      }

      if (!this.task.difficulty || this.task.difficulty === 0) {
        this.validationErrors.difficulty = '请选择难度级别';
        isValid = false;
      }

      if (!this.task.deadline) {
        this.validationErrors.deadline = '请选择截止日期';
        isValid = false;
      } else {
        const selectedDate = new Date(this.task.deadline);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selectedDate < today) {
          this.validationErrors.deadline = '截止日期不能早于今天';
          isValid = false;
        }
      }



      return isValid;
    },
    async submitTask() {
      // 在提交前打印所有任务数据
      console.log('提交前的任务数据:', {
        summary: this.task.summary,
        techStack: this.task.techStack,
        content: this.task.content,
        difficulty: this.task.difficulty,
        deadline: this.task.deadline
      });

      if (!this.validateForm()) {
        return;
      }

      this.isSubmitting = true;

      // 确保难度是数字类型
      if (typeof this.task.difficulty !== 'number') {
        this.task.difficulty = parseInt(this.task.difficulty) || 3;
      }

      console.log('提交的任务数据:', this.task);

      try {
        // 使用正确的API端点，包含企业ID
        const response = await axios.post(`http://localhost:8080/api/enterprises/tasks/${this.task.enterpriseId}`, {
          summary: this.task.summary,
          content: this.task.content,
          techStack: this.task.techStack,
          requirements: this.task.content, // 如果没有单独的要求字段，使用内容作为要求
          difficulty: this.task.difficulty
        });

        console.log('任务发布成功:', response.data);

        // 显示成功消息
        this.showSuccess = true;
        this.isSubmitting = false;
        console.log('显示成功提示');
      } catch (error) {
        console.error('任务发布失败:', error);
        alert('任务发布失败，请稍后重试');
      } finally {
        this.isSubmitting = false;
      }
    },
    cancel() {
      // 返回上一页
      this.$router.go(-1);
    },
    goToHome() {
      // 返回企业主页
      this.$router.push({ name: 'enterprise-home' });
    },
    publishAnother() {
      // 重置表单并继续发布
      this.task = {
        summary: '',
        techStack: '',
        content: '',
        difficulty: 0,
        deadline: '',
        enterpriseId: localStorage.getItem('userId')
      };
      this.validationErrors = {};
      this.showSuccess = false;
    }
  }
};
</script>

<style scoped>
.success-message {
  background-color: #f0f9eb;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
  text-align: center;
  margin: 50px auto;
  max-width: 500px;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 20px;
  background-color: #f0f9eb;
  width: 80px;
  height: 80px;
  line-height: 80px;
  border-radius: 50%;
  display: inline-block;
  border: 2px solid #67c23a;
}

.success-message h3 {
  font-size: 24px;
  color: #67c23a;
  margin-bottom: 10px;
}

.success-message p {
  color: #606266;
  margin-bottom: 30px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.primary-btn {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background-color: #e6e8eb;
}
.task-publish-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.task-form {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.form-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #f56c6c;
}

input[type="text"],
input[type="number"],
input[type="date"],
select,
textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #409eff;
}

.error {
  border-color: #f56c6c !important;
}

.error-container {
  border: 1px solid #f56c6c;
  border-radius: 4px;
  padding: 10px;
}

.error-message {
  display: block;
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #e6e8eb;
}

.submit-btn {
  padding: 10px 20px;
  background-color: #409eff;
  border: none;
  border-radius: 4px;
  color: white;
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

@media (max-width: 768px) {
  .task-form {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-btn, .submit-btn {
    width: 100%;
  }
}
</style>
