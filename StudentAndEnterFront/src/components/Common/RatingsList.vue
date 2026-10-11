<template>
  <div class="ratings-list">
    <h3 class="ratings-title">{{ title }}</h3>

    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else-if="ratings.length === 0" class="no-ratings">
      暂无评价
    </div>

    <div v-else class="ratings-container">
      <div v-for="rating in ratings" :key="rating.id" class="rating-item">
        <div class="rating-header">
          <div class="student-info">
            <span class="student-name">{{ rating.studentName || '匿名用户' }}</span>
            <span class="student-school" v-if="rating.studentSchool">{{ rating.studentSchool }}</span>
          </div>
          <div class="rating-stars">
            <span v-for="star in 5" :key="star" class="star" :class="{ 'filled': star <= rating.rating }">★</span>
          </div>
        </div>

        <div class="rating-comment" v-if="rating.comment">
          {{ rating.comment }}
        </div>

        <div class="rating-date">
          {{ formatDate(rating.createTime) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'RatingsList',
  props: {
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
      default: '评价列表'
    }
  },
  data() {
    return {
      ratings: [],
      loading: true
    };
  },
  created() {
    this.fetchRatings();
  },
  watch: {
    targetId() {
      this.fetchRatings();
    }
  },
  methods: {
    async fetchRatings() {
      this.loading = true;
      try {
        const endpoint = this.targetType === 'task'
          ? `/api/ratings/tasks/${this.targetId}`
          : `/api/ratings/enterprises/${this.targetId}`;

        const response = await axios.get(endpoint);
        // 确保ratings是一个数组，并过滤掉无效的评分（评分为0或null的记录）
        this.ratings = Array.isArray(response.data)
          ? response.data.filter(rating =>
              rating &&
              rating.rating &&
              rating.rating > 0
            )
          : [];
      } catch (error) {
        console.error(`获取${this.targetType === 'task' ? '任务' : '企业'}评价失败:`, error);
        this.ratings = [];
      } finally {
        this.loading = false;
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.ratings-list {
  margin-top: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.ratings-title {
  font-size: 18px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.no-ratings {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-style: italic;
}

.ratings-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rating-item {
  padding: 15px;
  border-radius: 4px;
  background-color: #f9f9f9;
  border: 1px solid #ebeef5;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.student-info {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

.student-school {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}

.rating-stars {
  display: flex;
}

.star {
  font-size: 16px;
  color: #dcdfe6;
}

.star.filled {
  color: #f7ba2a;
}

.rating-comment {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-line;
}

.rating-date {
  font-size: 12px;
  color: #909399;
  text-align: right;
}
</style>
