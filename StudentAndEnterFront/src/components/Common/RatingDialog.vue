<template>
  <div class="rating-dialog-overlay" v-if="show" @click.self="closeDialog">
    <div class="rating-dialog">
      <div class="dialog-header">
        <h3>{{ title }}</h3>
        <button class="close-btn" @click="closeDialog">×</button>
      </div>
      <div class="dialog-content">
        <div class="rating-section">
          <label>您的评分:</label>
          <star-rating v-model="rating" :type="'rating'" />
        </div>
        <div class="comment-section">
          <label for="comment">评价 (可选):</label>
          <textarea
            id="comment"
            v-model="comment"
            placeholder="请输入您的评价..."
            rows="4"
          ></textarea>
        </div>
      </div>
      <div class="dialog-footer">
        <button class="cancel-btn" @click="closeDialog">取消</button>
        <button class="submit-btn" @click="submitRating" :disabled="!rating">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
import StarRating from './StarRating.vue';
import axios from 'axios';

export default {
  name: 'RatingDialog',
  components: {
    StarRating
  },
  props: {
    show: {
      type: Boolean,
      default: false
    },
    targetId: {
      type: [Number, String],
      required: true
    },
    targetType: {
      type: String,
      required: true,
      validator: (value) => ['task', 'enterprise'].includes(value)
    },
    title: {
      type: String,
      default: '评分'
    }
  },
  data() {
    return {
      rating: 0,
      comment: ''
    };
  },
  methods: {
    closeDialog() {
      this.rating = 0;
      this.comment = '';
      this.$emit('close');
    },
    async submitRating() {
      // 验证评分
      if (!this.rating) {
        alert('请选择评分');
        return;
      }

      try {
        const studentId = localStorage.getItem('userId');
        const endpoint = this.targetType === 'task'
          ? `/api/ratings/tasks/${this.targetId}`
          : `/api/ratings/enterprises/${this.targetId}`;

        const response = await axios.post(endpoint, {
          studentId,
          rating: this.rating,
          comment: this.comment.trim()
        });

        this.$emit('rated', response.data);
        this.closeDialog();
      } catch (error) {
        console.error('评分提交失败:', error);
        alert('评分提交失败，请重试');
      }
    }
  }
};
</script>

<style scoped>
.rating-dialog-overlay {
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

.rating-dialog {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eaeaea;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
}

.dialog-content {
  padding: 20px;
}

.rating-section {
  margin-bottom: 20px;
}

.rating-section label,
.comment-section label {
  display: block;
  margin-bottom: 10px;
  font-weight: 500;
  color: #606266;
}

.comment-section textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
}



.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
  border-top: 1px solid #eaeaea;
}

.cancel-btn,
.submit-btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn {
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  margin-right: 10px;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: 1px solid #409EFF;
}

.submit-btn:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  border-color: #a0cfff;
  cursor: not-allowed;
}
</style>
