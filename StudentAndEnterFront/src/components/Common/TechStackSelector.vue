<template>
  <div class="tech-stack-selector">
    <div class="tech-tags-container">
      <div
        v-for="(tag, index) in selectedTags"
        :key="index"
        class="tech-tag"
      >
        {{ tag }}
        <span class="remove-tag" @click="removeTag(index)">×</span>
      </div>

      <div class="add-tag-container" v-if="!showTagSelector">
        <button class="add-tag-btn" @click="toggleTagSelector">
          <span class="plus-icon">+</span> 添加技术栈
        </button>
      </div>
    </div>

    <div v-if="showTagSelector" class="tag-selector-panel">
      <div class="tag-selector-header">
        <h3>选择技术栈</h3>
        <span class="close-selector" @click="toggleTagSelector">×</span>
      </div>

      <div class="common-tags">
        <h4>常用技术栈</h4>
        <div class="common-tags-grid">
          <div
            v-for="tag in commonTags"
            :key="tag"
            class="common-tag"
            :class="{ 'selected': selectedTags.includes(tag) }"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </div>
        </div>
      </div>

      <div class="custom-tag">
        <h4>添加自定义技术栈</h4>
        <div class="custom-tag-input">
          <input
            type="text"
            v-model="customTag"
            placeholder="输入技术栈名称"
            @keyup.enter="addCustomTag"
          />
          <button
            class="add-custom-tag-btn"
            @click="addCustomTag"
            :disabled="!customTag.trim()"
          >
            添加
          </button>
        </div>
      </div>

      <div class="tag-selector-footer">
        <button class="confirm-btn" @click="confirmSelection">确认</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TechStackSelector',
  props: {
    // 兼容Vue 2的v-model
    value: {
      type: String,
      default: ''
    },
    // Vue 3的v-model
    modelValue: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      selectedTags: [],
      showTagSelector: false,
      customTag: '',
      commonTags: [
        'JavaScript', 'TypeScript', 'Vue.js', 'React', 'Angular',
        'Node.js', 'Express', 'Java', 'Spring Boot', 'Python',
        'Django', 'Flask', 'PHP', 'Laravel', 'C#', '.NET',
        'Go', 'Rust', 'Ruby', 'Ruby on Rails', 'HTML', 'CSS',
        'SASS/SCSS', 'MySQL', 'PostgreSQL', 'MongoDB', 'Redis',
        'Docker', 'Kubernetes', 'AWS', 'Azure', 'GCP',
        'Git', 'CI/CD', 'RESTful API', 'GraphQL', 'WebSockets',
        'React Native', 'Flutter', 'Android', 'iOS', 'Swift',
        'Kotlin', 'TensorFlow', 'PyTorch', 'Machine Learning',
        'Data Science', 'Blockchain', 'Microservices', 'DevOps'
      ]
    };
  },
  created() {
    // 初始化已选标签
    const initialValue = this.modelValue || this.value;
    if (initialValue) {
      this.selectedTags = initialValue.split(',').map(tag => tag.trim());
    }

    // 调试信息
    console.log('初始化TechStackSelector:', {
      value: this.value,
      modelValue: this.modelValue,
      selectedTags: this.selectedTags
    });
  },
  watch: {
    // 监听外部值的变化
    modelValue: {
      handler(newValue) {
        console.log('modelValue变化:', newValue);
        if (newValue !== undefined) {
          this.updateSelectedTags(newValue);
        }
      },
      immediate: true
    },
    value: {
      handler(newValue) {
        console.log('value变化:', newValue);
        // 只在modelValue不存在时才使用value
        if (newValue !== undefined && this.modelValue === undefined) {
          this.updateSelectedTags(newValue);
        }
      },
      immediate: true
    }
  },
  methods: {
    toggleTagSelector() {
      this.showTagSelector = !this.showTagSelector;
      if (!this.showTagSelector) {
        this.customTag = '';
      }
    },
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag);
      if (index === -1) {
        this.selectedTags.push(tag);
      } else {
        this.selectedTags.splice(index, 1);
      }
      this.emitChange();
    },
    removeTag(index) {
      this.selectedTags.splice(index, 1);
      this.emitChange();
    },
    addCustomTag() {
      const tag = this.customTag.trim();
      if (tag && !this.selectedTags.includes(tag)) {
        this.selectedTags.push(tag);
        this.customTag = '';
        this.emitChange();
      }
    },
    confirmSelection() {
      this.toggleTagSelector();
    },
    updateSelectedTags(value) {
      if (value && typeof value === 'string') {
        this.selectedTags = value.split(',').map(tag => tag.trim()).filter(tag => tag);
      } else {
        this.selectedTags = [];
      }
    },
    emitChange() {
      const tagsString = this.selectedTags.join(', ');
      // 发送Vue 3的v-model事件
      this.$emit('update:modelValue', tagsString);
      // 兼容Vue 2的v-model
      this.$emit('input', tagsString);

      console.log('发送技术栈变更:', {
        tags: this.selectedTags,
        tagsString: tagsString
      });
    }
  }
};
</script>

<style scoped>
.tech-stack-selector {
  position: relative;
  width: 100%;
}

.tech-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
  padding: 5px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
}

.tech-tag {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 14px;
}

.remove-tag {
  margin-left: 5px;
  cursor: pointer;
  font-weight: bold;
}

.remove-tag:hover {
  color: #f56c6c;
}

.add-tag-container {
  display: flex;
  align-items: center;
}

.add-tag-btn {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
}

.plus-icon {
  font-weight: bold;
  margin-right: 4px;
  font-size: 16px;
}

.tag-selector-panel {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
  margin-top: 5px;
  padding: 15px;
}

.tag-selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.tag-selector-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.close-selector {
  cursor: pointer;
  font-size: 20px;
  color: #909399;
}

.close-selector:hover {
  color: #f56c6c;
}

.common-tags h4, .custom-tag h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.common-tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
  margin-bottom: 15px;
}

.common-tag {
  padding: 6px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
}

.common-tag:hover {
  background-color: #e6f1fc;
  color: #409eff;
}

.common-tag.selected {
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.custom-tag-input {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.custom-tag-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.add-custom-tag-btn {
  padding: 8px 15px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-custom-tag-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.tag-selector-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.confirm-btn {
  padding: 8px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.confirm-btn:hover {
  background-color: #66b1ff;
}
</style>
