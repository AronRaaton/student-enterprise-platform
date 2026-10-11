<template>
  <div class="star-rating-display">
    <div class="stars-container">
      <span 
        v-for="n in 5" 
        :key="n" 
        class="star" 
        :class="{ 'filled': n <= value }"
      >
        ★
      </span>
    </div>
    <div v-if="showText" class="rating-text">
      {{ ratingText }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'StarRatingDisplay',
  props: {
    value: {
      type: Number,
      default: 0
    },
    showText: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    ratingText() {
      switch(this.value) {
        case 0: return '未设置难度';
        case 1: return '非常简单';
        case 2: return '简单';
        case 3: return '中等';
        case 4: return '困难';
        case 5: return '非常困难';
        default: return '';
      }
    }
  }
};
</script>

<style scoped>
.star-rating-display {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stars-container {
  display: flex;
  align-items: center;
}

.star {
  font-size: 20px;
  color: #dcdfe6;
  user-select: none;
}

.star.filled {
  color: #f7ba2a;
}

.rating-text {
  margin-top: 5px;
  font-size: 14px;
  color: #606266;
}
</style>
