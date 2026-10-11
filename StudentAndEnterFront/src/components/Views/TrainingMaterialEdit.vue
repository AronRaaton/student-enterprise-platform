<template>
  <div class="training-material-edit-container module-page module-form">
    <div class="page-header">
      <h1>{{ isEditing ? '编辑培训资料' : '创建培训资料' }}</h1>
      <button class="back-btn" @click="goBack">返回</button>
    </div>

    <div v-if="loading" class="loading-spinner">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <div v-else class="edit-form">
      <div class="form-section">
        <h2>基本信息</h2>
        <div class="form-group">
          <label for="title">标题</label>
          <input type="text" id="title" v-model="material.title" placeholder="请输入培训资料标题" />
        </div>

        <div class="form-group">
          <label for="category">分类</label>
          <select id="category" v-model="selectedCategory">
            <option value="">请选择分类</option>
            <option value="技术培训">技术培训</option>
            <option value="管理培训">管理培训</option>
            <option value="职业发展">职业发展</option>
            <option value="软技能">软技能</option>
            <option value="其他">其他</option>
          </select>
        </div>

        <div class="form-group">
          <label for="description">描述</label>
          <textarea id="description" v-model="material.description" placeholder="请输入培训资料描述" rows="4"></textarea>
        </div>
      </div>

      <div class="form-section">
        <h2>章节管理</h2>
        <div v-if="getChapters().length > 0" class="chapters-list">
          <div v-for="(chapter, index) in getChapters()" :key="index" class="chapter-item">
            <div class="chapter-header">
              <h3>章节 {{ index + 1 }}</h3>
              <button class="action-btn danger small" @click="removeChapter(index)">删除</button>
            </div>

            <div class="chapter-form">
              <div class="form-group">
                <label :for="'chapter-title-' + index">标题</label>
                <input type="text" :id="'chapter-title-' + index" v-model="chapter.title" placeholder="请输入章节标题" />
              </div>

              <div class="form-group">
                <label :for="'chapter-description-' + index">描述</label>
                <textarea :id="'chapter-description-' + index" v-model="chapter.description" placeholder="请输入章节描述" rows="2"></textarea>
              </div>

              <div class="form-group">
                <label>章节内容</label>
                <div class="file-upload-container">
                  <div v-if="chapter.fileUrl" class="file-preview">
                    <span>{{ chapter.fileName || '已上传文件' }}</span>
                    <button class="action-btn danger small" @click="removeChapterFile(chapter)">删除</button>
                  </div>
                  <div v-else class="file-upload">
                    <input type="file" :id="'chapter-file-' + index" @change="handleChapterFileUpload($event, chapter)" />
                    <label :for="'chapter-file-' + index" class="file-upload-label">选择文件</label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="add-chapter">
          <button class="action-btn" @click="addChapter">添加章节</button>
        </div>
      </div>

      <div class="form-actions">
        <button class="action-btn primary" @click="saveMaterial">保存</button>
        <button class="action-btn" @click="goBack">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'TrainingMaterialEdit',
  data() {
    return {
      materialId: null,
      isEditing: false,
      loading: false,
      material: {
        title: '',
        category: '',
        description: '',
        chapters: []
      }
    };
  },
  created() {
    this.materialId = this.$route.params.materialId;
    this.isEditing = !!this.materialId;

    if (this.isEditing) {
      this.fetchMaterialDetails();
    }
  },
  computed: {
    // Portfolio build fix: v-model requires an assignable binding.
    selectedCategory: {
      get() { return this.material.materialType || this.material.category || ''; },
      set(value) {
        if ('materialType' in this.material) this.material.materialType = value;
        else this.material.category = value;
      }
    }
  },
  methods: {
    fetchMaterialDetails() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/training/${this.materialId}`)
        .then(response => {
          this.material = response.data;
          if (!this.material.chapters) {
            this.material.chapters = [];
          }
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
        this.material.chapters = this.material.chapterList;
        return this.material.chapters;
      } else {
        return this.material.chapters || [];
      }
    },
    addChapter() {
      if (!this.material.chapters) {
        this.material.chapters = [];
      }

      this.getChapters().push({
        title: '',
        description: '',
        fileUrl: null,
        fileName: null,
        fileType: null,
        file: null // 用于存储文件对象
      });
    },
    removeChapter(index) {
      if (confirm('确定要删除此章节吗？')) {
        this.getChapters().splice(index, 1);
      }
    },
    handleChapterFileUpload(event, chapter) {
      const file = event.target.files[0];
      if (!file) return;

      // 检查文件大小
      if (file.size > 30 * 1024 * 1024) { // 30MB
        alert('文件大小不能超过30MB');
        event.target.value = '';
        return;
      }

      chapter.file = file;
      chapter.fileName = file.name;
      chapter.fileType = this.getFileType(file.name);

      // 创建临时URL用于预览
      if (chapter.fileType === 'PDF' || chapter.fileType === 'IMAGE') {
        chapter.fileUrl = URL.createObjectURL(file);
      } else {
        chapter.fileUrl = '#'; // 占位符
      }
    },
    removeChapterFile(chapter) {
      chapter.file = null;
      chapter.fileName = null;
      chapter.fileType = null;
      chapter.fileUrl = null;
    },
    getFileType(fileName) {
      const extension = fileName.split('.').pop().toLowerCase();
      if (['pdf'].includes(extension)) return 'PDF';
      if (['jpg', 'jpeg', 'png', 'gif'].includes(extension)) return 'IMAGE';
      if (['mp4', 'webm', 'ogg'].includes(extension)) return 'VIDEO';
      if (['mp3', 'wav'].includes(extension)) return 'AUDIO';
      return 'OTHER';
    },
    async saveMaterial() {
      if (!this.material.title) {
        alert('请输入培训资料标题');
        return;
      }

      this.loading = true;

      try {
        // 处理文件上传
        for (const chapter of this.material.chapters) {
          if (chapter.file) {
            const formData = new FormData();
            formData.append('file', chapter.file);

            const response = await axios.post('http://localhost:8080/api/upload', formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            });

            chapter.fileUrl = response.data.url;
            delete chapter.file; // 删除文件对象，避免序列化问题
          }
        }

        // 保存培训资料
        if (this.isEditing) {
          await axios.put(`http://localhost:8080/api/training/${this.materialId}`, this.material);
          alert('培训资料更新成功');
        } else {
          const enterpriseId = localStorage.getItem('userId');
          this.material.enterpriseId = enterpriseId;
          await axios.post('http://localhost:8080/api/training', this.material);
          alert('培训资料创建成功');
        }

        this.$router.push({ name: 'EnterpriseTrainingMaterials' });
      } catch (error) {
        console.error('保存培训资料失败:', error);
        alert('保存培训资料失败');
      } finally {
        this.loading = false;
      }
    },
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
.training-material-edit-container {
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

.edit-form {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.form-section {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.form-section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #606266;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.chapters-list {
  margin-bottom: 20px;
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

.chapter-header h3 {
  margin: 0;
}

.chapter-form {
  padding: 15px;
}

.file-upload-container {
  margin-top: 10px;
}

.file-upload input[type="file"] {
  display: none;
}

.file-upload-label {
  display: inline-block;
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.file-upload-label:hover {
  background-color: #66b1ff;
}

.file-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.add-chapter {
  margin-top: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding: 20px;
}

.action-btn {
  background-color: #909399;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #a6a9ad;
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

.action-btn.small {
  padding: 5px 10px;
  font-size: 12px;
}
</style>
