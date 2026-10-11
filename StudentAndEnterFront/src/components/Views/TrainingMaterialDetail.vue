<template>
  <div class="training-material-detail-container module-page module-detail">
    <div class="page-header">
      <h1>培训资料详情</h1>
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <div v-if="loading" class="loading-spinner">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else class="material-content">
      <div class="material-header">
        <h2>{{ material.title }}</h2>
        <div class="material-meta">
          <span class="material-category">分类: {{ material.materialType || material.category || '未分类' }}</span>
          <span class="material-date">上传时间: {{ formatDate(material.createTime) }}</span>
        </div>
      </div>

      <div class="material-description">
        <h3>资料描述</h3>
        <p>{{ material.description || '无描述' }}</p>
      </div>

      <div class="material-chapters">
        <h3>章节列表</h3>
        <div v-if="getChapters().length > 0" class="chapters-list">
          <div v-for="(chapter, index) in getChapters()" :key="chapter.id || index" class="chapter-item">
            <div class="chapter-header">
              <h4>{{ index + 1 }}. {{ chapter.title }}</h4>
              <div class="chapter-actions">
                <button class="action-btn" @click="viewChapter(chapter)">查看</button>
                <button class="action-btn" @click="downloadChapter(chapter)">下载</button>
              </div>
            </div>
            <p class="chapter-description">{{ chapter.description || '无描述' }}</p>
          </div>
        </div>
        <div v-else class="empty-chapters">
          <p>暂无章节内容</p>
        </div>
      </div>

      <div class="material-actions">
        <button class="action-btn primary" @click="editMaterial" v-if="isEnterprise">编辑资料</button>
        <button class="action-btn danger" @click="deleteMaterial" v-if="isEnterprise">删除资料</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'TrainingMaterialDetail',
  data() {
    return {
      materialId: null,
      material: {},
      loading: true,
      isEnterprise: false
    };
  },
  created() {
    this.materialId = this.$route.params.materialId;
    this.isEnterprise = localStorage.getItem('userType') === 'enterprise';
    this.fetchMaterialDetails();
  },
  methods: {
    fetchMaterialDetails() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/training/${this.materialId}`)
        .then(response => {
          this.material = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取培训资料详情失败:', error);
          alert('获取培训资料详情失败');
          this.loading = false;
        });
    },
    getChapters() {
      if (this.material.chapters && this.material.chapters.length > 0) {
        return this.material.chapters;
      } else if (this.material.chapterList && this.material.chapterList.length > 0) {
        return this.material.chapterList;
      } else {
        return [];
      }
    },
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    viewChapter(chapter) {
      if (chapter.fileType === 'PDF' && chapter.fileUrl) {
        window.open(chapter.fileUrl, '_blank');
      } else if (chapter.videoUrl) {
        // 处理视频播放
        alert('视频播放功能待实现');
      } else {
        alert('无法查看此章节内容');
      }
    },
    downloadChapter(chapter) {
      if (chapter.fileUrl) {
        window.open(chapter.fileUrl, '_blank');
      } else {
        alert('无法下载此章节内容');
      }
    },
    editMaterial() {
      this.$router.push({ name: 'TrainingMaterialEdit', params: { materialId: this.materialId } });
    },
    deleteMaterial() {
      if (confirm('确定要删除此培训资料吗？此操作不可撤销。')) {
        axios.delete(`http://localhost:8080/api/training/${this.materialId}`)
          .then(() => {
            alert('培训资料已成功删除');
            this.$router.push({ name: 'EnterpriseTrainingMaterials' });
          })
          .catch(error => {
            console.error('删除培训资料失败:', error);
            alert('删除培训资料失败');
          });
      }
    },
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.training-material-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #66b1ff;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #409eff;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.material-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.material-header {
  padding: 20px;
  background-color: #ecf5ff;
  border-bottom: 1px solid #d9ecff;
}

.material-meta {
  display: flex;
  gap: 20px;
  margin-top: 10px;
  color: #606266;
  font-size: 14px;
}

.material-description {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.material-chapters {
  padding: 20px;
}

.chapters-list {
  margin-top: 15px;
}

.chapter-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 15px;
  overflow: hidden;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.chapter-header h4 {
  margin: 0;
}

.chapter-actions {
  display: flex;
  gap: 10px;
}

.chapter-description {
  padding: 15px;
  margin: 0;
  color: #606266;
}

.empty-chapters {
  display: flex;
  justify-content: center;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.material-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.action-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.action-btn.primary {
  background-color: #409eff;
}

.action-btn.primary:hover {
  background-color: #66b1ff;
}

.action-btn.danger {
  background-color: #f56c6c;
}

.action-btn.danger:hover {
  background-color: #f78989;
}
</style>
