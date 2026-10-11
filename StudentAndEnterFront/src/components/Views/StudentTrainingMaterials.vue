<template>
  <div class="training-materials-container">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">培训资料</h1>
      <div class="search-container">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索培训资料..."
          class="search-input"
          @keyup.enter="searchMaterials"
        />
        <button class="search-btn" @click="searchMaterials">搜索</button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-area">
      <!-- 行业类型筛选 -->
      <div class="industry-filter">
        <div class="filter-header">
          <h3>行业类型</h3>
        </div>
        <div class="filter-options">
          <div
            class="filter-option"
            :class="{ active: selectedIndustry === 'all' }"
            @click="selectIndustry('all')"
          >
            全部
          </div>
          <div
            v-for="industry in allIndustryTypes"
            :key="industry"
            class="filter-option"
            :class="{ active: selectedIndustry === industry }"
            @click="selectIndustry(industry)"
          >
            {{ industry }}
          </div>
        </div>
      </div>

      <!-- 培训资料列表 -->
      <div class="materials-section">
        <div v-if="loading" class="loading-container">
          <div class="spinner"></div>
          <p>加载中...</p>
        </div>

        <div v-else-if="filteredMaterials.length === 0" class="empty-state">
          <div class="empty-icon">📚</div>
          <h3>暂无培训资料</h3>
          <p>当前没有符合条件的培训资料</p>
        </div>

        <div v-else class="materials-grid">
          <div
            v-for="material in filteredMaterials"
            :key="material.materialId"
            class="material-card"
            @click="viewMaterialDetail(material.materialId)"
          >
            <div class="material-header">
              <h3 class="material-title">{{ material.title }}</h3>
              <span class="material-type">{{ material.industryType }}</span>
            </div>
            <div class="material-content">
              <p class="material-description">{{ material.description }}</p>
              <div class="material-meta">
                <span class="material-enterprise">{{ material.enterpriseName }}</span>
                <span class="material-date">{{ material.formattedCreateTime }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 培训资料详情对话框 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content detail-modal">
        <div class="modal-header">
          <h2>{{ currentMaterial.title }}</h2>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body">
          <div class="material-info">
            <div class="info-item">
              <span class="info-label">行业类型:</span>
              <span class="info-value">{{ currentMaterial.industryType }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布企业:</span>
              <span class="info-value">{{ currentMaterial.enterpriseName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间:</span>
              <span class="info-value">{{ currentMaterial.formattedCreateTime }}</span>
            </div>
            <div class="info-item description">
              <span class="info-label">描述:</span>
              <p class="info-value">{{ currentMaterial.description }}</p>
            </div>
          </div>

          <!-- 章节列表 -->
          <div class="chapters-list">
            <h3>培训内容</h3>
            <div v-if="currentMaterial.chapters && currentMaterial.chapters.length > 0">
              <div v-for="(chapter, index) in currentMaterial.chapters" :key="chapter.chapterId" class="chapter-item">
                <div class="chapter-header" @click="toggleChapter(chapter.chapterId)">
                  <h4 class="chapter-title">{{ index + 1 }}. {{ chapter.title }}</h4>
                  <span class="toggle-icon">{{ expandedChapters.includes(chapter.chapterId) ? '▼' : '▶' }}</span>
                </div>
                <div v-if="expandedChapters.includes(chapter.chapterId)" class="chapter-content">
                  <p v-if="chapter.description" class="chapter-description">{{ chapter.description }}</p>
                  <div v-if="chapter.content" class="chapter-text">{{ chapter.content }}</div>

                  <!-- 文件列表 -->
                  <div v-if="chapter.files && chapter.files.length > 0" class="files-list">
                    <h5>文件资料</h5>
                    <div v-for="file in chapter.files" :key="file.fileId" class="file-item">
                      <span class="file-icon">{{ getFileIcon(file.fileType) }}</span>
                      <span class="file-name">{{ file.fileName }}</span>
                      <div class="file-actions">
                        <a :href="`http://localhost:8080/api/files/download/${file.filePath}`" class="download-btn">
                          <span class="action-icon">⬇️</span> 下载
                        </a>
                        <a v-if="canPreview(file.fileType)"
                           :href="`http://localhost:8080/api/files/download/${file.filePath}?preview=true`"
                           target="_blank"
                           class="preview-btn">
                          <span class="action-icon">🔍</span> 预览
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-chapters">
              <p>暂无培训内容</p>
            </div>
          </div>
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
      loading: true,
      trainingMaterials: [],
      industryTypes: [],
      selectedIndustry: 'all',
      searchQuery: '',
      showDetailModal: false,
      currentMaterial: {},
      expandedChapters: []
    };
  },
  computed: {
    filteredMaterials() {
      if (this.selectedIndustry === 'all') {
        return this.trainingMaterials;
      }
      return this.trainingMaterials.filter(material => material.industryType === this.selectedIndustry);
    },
    // 确保始终有一个完整的行业类型列表
    allIndustryTypes() {
      // 如果API返回的行业类型为空，则使用默认列表
      if (this.industryTypes.length === 0) {
        return [
          '计算机/网络/技术类', '电子/电器/通信技术类', '行政/后勤类', '翻译类',
          '销售类', '客户服务类', '市场/公关/媒介类', '咨询/顾问类', '技工类',
          '财务/审计/统计类', '人力资源类', '教育/培训类', '质量管理类',
          '美术/设计/创意类', '金融保险类', '贸易/物流/采购/运输类', '经营管理类',
          '商业零售类', '建筑/房地产/装饰装修/物业管理类', '法律类',
          '酒店/餐饮/旅游/服务类', '生物/制药/化工/环保类', '文体/影视/写作/媒体类',
          '机械/仪器仪表类', '科研类', '工厂生产类', '医疗卫生/美容保健类',
          '电气/能源/动力类', '其他类'
        ];
      }
      return this.industryTypes;
    }
  },
  created() {
    this.fetchIndustryTypes();
    this.fetchTrainingMaterials();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'student-home' });
    },
    async fetchIndustryTypes() {
      try {
        const response = await axios.get('http://localhost:8080/api/training/industry-types');
        this.industryTypes = response.data;
      } catch (error) {
        console.error('获取行业类型失败:', error);
        this.industryTypes = [];
      }
    },
    async fetchTrainingMaterials() {
      this.loading = true;
      try {
        const response = await axios.get('http://localhost:8080/api/training');
        this.trainingMaterials = response.data;
      } catch (error) {
        console.error('获取培训资料失败:', error);
        this.trainingMaterials = [];
      } finally {
        this.loading = false;
      }
    },
    selectIndustry(industry) {
      this.selectedIndustry = industry;
    },
    async searchMaterials() {
      if (!this.searchQuery.trim()) {
        this.fetchTrainingMaterials();
        return;
      }

      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/training/search?keyword=${encodeURIComponent(this.searchQuery)}`);
        this.trainingMaterials = response.data;
      } catch (error) {
        console.error('搜索培训资料失败:', error);
      } finally {
        this.loading = false;
      }
    },
    async viewMaterialDetail(materialId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/training/${materialId}`);
        this.currentMaterial = response.data;
        this.expandedChapters = []; // 重置展开的章节
        this.showDetailModal = true;
      } catch (error) {
        console.error('获取培训资料详情失败:', error);
        alert('获取详情失败，请重试');
      }
    },
    closeDetailModal() {
      this.showDetailModal = false;
      this.currentMaterial = {};
    },
    toggleChapter(chapterId) {
      if (this.expandedChapters.includes(chapterId)) {
        this.expandedChapters = this.expandedChapters.filter(id => id !== chapterId);
      } else {
        this.expandedChapters.push(chapterId);
      }
    },
    getFileIcon(fileType) {
      if (!fileType) return '📄';

      if (fileType.includes('pdf')) {
        return '📕';
      } else if (fileType.includes('word') || fileType.includes('doc')) {
        return '📝';
      } else if (fileType.includes('powerpoint') || fileType.includes('presentation') || fileType.includes('ppt')) {
        return '📊';
      } else if (fileType.includes('excel') || fileType.includes('spreadsheet') || fileType.includes('xls')) {
        return '📈';
      } else if (fileType.includes('video') || fileType.includes('mp4')) {
        return '🎬';
      } else if (fileType.includes('audio') || fileType.includes('mp3')) {
        return '🎵';
      } else if (fileType.includes('zip') || fileType.includes('rar') || fileType.includes('compressed')) {
        return '📦';
      } else if (fileType.includes('image')) {
        return '🖼️';
      } else {
        return '📄';
      }
    },
    canPreview(fileType) {
      if (!fileType) return false;

      return fileType.includes('pdf') ||
             fileType.includes('image') ||
             fileType.includes('text');
    }
  }
};
</script>

<style scoped>
.training-materials-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  margin-right: 15px;
}

.back-icon {
  font-size: 20px;
  margin-right: 5px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  flex: 1;
}

.search-container {
  display: flex;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px 0 0 4px;
  font-size: 14px;
  width: 200px;
}

.search-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
}

.content-area {
  display: flex;
  gap: 20px;
}

.industry-filter {
  width: 250px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  align-self: flex-start;
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

.filter-header {
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  flex-shrink: 0;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 5px;
}

.filter-option {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
  color: #606266;
}

.filter-option:hover {
  background-color: #f5f7fa;
}

.filter-option.active {
  background-color: #ecf5ff;
  color: #409EFF;
}

.materials-section {
  flex: 1;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #409EFF;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.material-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.material-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.15);
}

.material-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.material-title {
  font-size: 16px;
  color: #303133;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.material-type {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  margin-left: 10px;
}

.material-content {
  flex: 1;
}

.material-description {
  color: #606266;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 14px;
}

.material-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}

/* 模态框样式 */
.modal-overlay {
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

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 800px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.material-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.info-label {
  font-weight: bold;
  color: #606266;
  width: 100px;
}

.info-value {
  color: #303133;
  flex: 1;
}

.info-item.description {
  display: block;
}

.info-item.description .info-label {
  display: block;
  margin-bottom: 5px;
}

.info-item.description .info-value {
  padding-left: 0;
  white-space: pre-line;
}

.chapters-list h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
}

.chapter-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  cursor: pointer;
}

.chapter-title {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.toggle-icon {
  color: #909399;
}

.chapter-content {
  padding: 15px;
}

.chapter-description {
  color: #606266;
  margin-top: 0;
  margin-bottom: 10px;
}

.chapter-text {
  white-space: pre-line;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  color: #303133;
}

.files-list {
  margin-top: 15px;
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
}

.files-list h5 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.file-item:last-child {
  border-bottom: none;
}

.file-icon {
  margin-right: 10px;
  font-size: 18px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-actions {
  display: flex;
  gap: 10px;
}

.download-btn, .preview-btn {
  color: #409EFF;
  text-decoration: none;
  font-size: 12px;
  display: flex;
  align-items: center;
}

.action-icon {
  margin-right: 3px;
}

.empty-chapters {
  text-align: center;
  padding: 20px;
  color: #909399;
}
</style>
