<template>
  <div class="star-rating">
    <div class="stars-container">
      <span
        v-for="n in 5"
        :key="n"
        class="star"
        :class="{ 'filled': n <= currentValue, 'hovered': !readonly && n <= hoverRating }"
        @click="!readonly && updateRating(n)"
        @mouseover="!readonly && (hoverRating = n)"
        @mouseleave="!readonly && (hoverRating = 0)"
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
  name: 'StarRating',
  props: {
    // 兼容Vue 2的v-model
    value: {
      type: Number,
      default: 0
    },
    // Vue 3的v-model
    modelValue: {
      type: Number,
      default: 0
    },
    // 评分类型：'difficulty' 或 'rating'
    type: {
      type: String,
      default: 'rating'
    },
    // 是否只读
    readonly: {
      type: Boolean,
      default: false
    },
    // 是否显示文本
    showText: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      hoverRating: 0
    };
  },
  computed: {
    // 使用优先级获取实际值
    currentValue() {
      // 如果提供了modelValue，优先使用它
      return this.modelValue !== undefined ? this.modelValue : this.value;
    },
    ratingText() {
      // 使用currentValue而不是value
      const rating = this.hoverRating || this.currentValue;

      // 根据传入的类型显示不同的文本
      if (this.type === 'difficulty') {
        switch(rating) {
          case 0: return '请选择难度';
          case 1: return '非常简单';
          case 2: return '简单';
          case 3: return '中等';
          case 4: return '困难';
          case 5: return '非常困难';
          default: return '';
        }
      } else { // 评分类型
        switch(rating) {
          case 0: return '请评分';
          case 1: return '很差';
          case 2: return '较差';
          case 3: return '一般';
          case 4: return '较好';
          case 5: return '非常好';
          default: return '';
        }
      }
    }
  },
  methods: {
    updateRating(rating) {
      console.log('点击星级:', rating);
      // 在Vue 3中，对于v-model应使用update:前缀
      this.$emit('update:modelValue', rating);
      // 为了兼容性，保留input事件
      this.$emit('input', rating);
    }
  }
};
</script>

<style scoped>
.star-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stars-container {
  display: flex;
  align-items: center;
}

.star {
  font-size: 24px;
  color: #dcdfe6;
  cursor: pointer;
  transition: color 0.2s;
  user-select: none;
}

.star.filled {
  color: #f7ba2a;
}

.star.hovered {
  color: #f7ba2a;
}

.rating-text {
  margin-top: 5px;
  font-size: 14px;
  color: #606266;
}
</style>
