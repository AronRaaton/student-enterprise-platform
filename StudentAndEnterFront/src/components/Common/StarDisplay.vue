<template>
  <div class="star-display">
    <div class="stars">
      <span v-for="i in 5" :key="i" class="star" :class="{ 'filled': i <= fullStars, 'half-filled': i === halfStar }">
        <template v-if="i === halfStar">
          <span class="half-star">★</span>
        </template>
        <template v-else>
          ★
        </template>
      </span>
    </div>
    <span v-if="showValue" class="rating-value">{{ rating.toFixed(1) }}</span>
  </div>
</template>

<script>
export default {
  name: 'StarDisplay',
  props: {
    rating: {
      type: Number,
      default: 0
    },
    showValue: {
      type: Boolean,
      default: true
    },
    size: {
      type: String,
      default: 'medium', // small, medium, large
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    }
  },
  computed: {
    fullStars() {
      return Math.floor(this.rating);
    },
    halfStar() {
      const decimal = this.rating - this.fullStars;
      return decimal >= 0.5 ? this.fullStars + 1 : 0;
    }
  }
};
</script>

<style scoped>
.star-display {
  display: flex;
  align-items: center;
}

.stars {
  display: flex;
}

.star {
  position: relative;
  color: #dcdfe6;
}

.star.small {
  font-size: 16px;
}

.star.medium {
  font-size: 20px;
}

.star.large {
  font-size: 24px;
}

.star.filled {
  color: #f7ba2a;
}

.half-star {
  position: absolute;
  top: 0;
  left: 0;
  width: 50%;
  overflow: hidden;
  color: #f7ba2a;
}

.rating-value {
  margin-left: 8px;
  font-weight: bold;
  font-size: 14px;
}
</style>
