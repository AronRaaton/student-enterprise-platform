<template>
  <div class="training-materials-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">培训资料管理</h1>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-area">
      <!-- 操作按钮 -->
      <div class="action-bar">
        <button class="create-btn" @click="showCreateForm">
          <span class="btn-icon">+</span> 发布新培训资料
        </button>
      </div>

      <!-- 培训资料列表 -->
      <div v-if="loading" class="loading-container">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="trainingMaterials.length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <h3>暂无培训资料</h3>
        <p>点击"发布新培训资料"按钮开始创建</p>
      </div>

      <div v-else class="materials-list">
        <div v-for="material in trainingMaterials" :key="material.materialId" class="material-card">
          <div class="material-header">
            <h3 class="material-title">{{ material.title }}</h3>
            <span class="material-type">{{ material.industryType }}</span>
          </div>
          <div class="material-content">
            <p class="material-description">{{ material.description }}</p>
            <div class="material-meta">
              <span class="material-date">发布时间: {{ material.formattedCreateTime }}</span>
              <span class="material-chapters">章节数: {{ material.chapters.length }}</span>
            </div>
          </div>
          <div class="material-actions">
            <button class="edit-btn" @click="editMaterial(material)">编辑</button>
            <button class="view-btn" @click="viewMaterial(material.materialId)">查看</button>
            <button class="delete-btn" @click="confirmDeleteMaterial(material)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建培训资料表单对话框 -->
    <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? '编辑培训资料' : '发布新培训资料' }}</h2>
          <button class="close-btn" @click="closeForm">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitForm">
            <div class="form-group">
              <label for="title">标题</label>
              <input
                id="title"
                v-model="formData.title"
                type="text"
                placeholder="请输入培训资料标题"
                required
              />
            </div>

            <div class="form-group">
              <label for="industryType">行业类型</label>
              <select id="industryType" v-model="formData.industryType" required>
                <option value="">请选择行业类型</option>
                <option value="计算机/网络/技术类">计算机/网络/技术类</option>
                <option value="电子/电器/通信技术类">电子/电器/通信技术类</option>
                <option value="行政/后勤类">行政/后勤类</option>
                <option value="翻译类">翻译类</option>
                <option value="销售类">销售类</option>
                <option value="客户服务类">客户服务类</option>
                <option value="市场/公关/媒介类">市场/公关/媒介类</option>
                <option value="咨询/顾问类">咨询/顾问类</option>
                <option value="技工类">技工类</option>
                <option value="财务/审计/统计类">财务/审计/统计类</option>
                <option value="人力资源类">人力资源类</option>
                <option value="教育/培训类">教育/培训类</option>
                <option value="质量管理类">质量管理类</option>
                <option value="美术/设计/创意类">美术/设计/创意类</option>
                <option value="金融保险类">金融保险类</option>
                <option value="贸易/物流/采购/运输类">贸易/物流/采购/运输类</option>
                <option value="经营管理类">经营管理类</option>
                <option value="商业零售类">商业零售类</option>
                <option value="建筑/房地产/装饰装修/物业管理类">建筑/房地产/装饰装修/物业管理类</option>
                <option value="法律类">法律类</option>
                <option value="酒店/餐饮/旅游/服务类">酒店/餐饮/旅游/服务类</option>
                <option value="生物/制药/化工/环保类">生物/制药/化工/环保类</option>
                <option value="文体/影视/写作/媒体类">文体/影视/写作/媒体类</option>
                <option value="机械/仪器仪表类">机械/仪器仪表类</option>
                <option value="科研类">科研类</option>
                <option value="工厂生产类">工厂生产类</option>
                <option value="医疗卫生/美容保健类">医疗卫生/美容保健类</option>
                <option value="电气/能源/动力类">电气/能源/动力类</option>
                <option value="其他类">其他类</option>
              </select>
            </div>

            <div class="form-group">
              <label for="description">描述</label>
              <textarea
                id="description"
                v-model="formData.description"
                placeholder="请输入培训资料描述（可选）"
                rows="4"
                class="compact-textarea"
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeForm">取消</button>
              <button type="submit" class="submit-btn">{{ isEditing ? '保存修改' : '创建' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 章节管理对话框 -->
    <div v-if="showChaptersModal" class="modal-overlay" @click.self="closeChaptersModal">
      <div class="modal-content chapters-modal">
        <div class="modal-header">
          <h2>{{ currentMaterial.title }} - 章节管理</h2>
          <button class="close-btn" @click="closeChaptersModal">×</button>
        </div>
        <div class="modal-body">
          <!-- 章节列表 -->
          <div class="chapters-list">
            <div v-if="currentMaterial.chapters && currentMaterial.chapters.length > 0">
              <div v-for="(chapter, index) in currentMaterial.chapters" :key="chapter.chapterId" class="chapter-item">
                <div class="chapter-header">
                  <h3 class="chapter-title">{{ index + 1 }}. {{ chapter.title }}</h3>
                  <div class="chapter-actions">
                    <button class="edit-chapter-btn" @click="editChapter(chapter)">编辑</button>
                    <button class="upload-file-btn" @click="showFileUpload(chapter)">上传文件</button>
                    <button class="delete-chapter-btn" @click="confirmDeleteChapter(chapter)">删除</button>
                  </div>
                </div>
                <div class="chapter-content">
                  <p v-if="chapter.description">{{ chapter.description }}</p>
                  <div v-if="chapter.content" class="chapter-text">{{ chapter.content }}</div>

                  <!-- 文件列表 -->
                  <div v-if="chapter.files && chapter.files.length > 0" class="files-list">
                    <h4>文件列表</h4>
                    <div v-for="file in chapter.files" :key="file.fileId" class="file-item">
                      <span class="file-icon">📄</span>
                      <span class="file-name">{{ file.fileName }}</span>
                      <div class="file-actions">
                        <a :href="`http://localhost:8080/api/files/download/${file.filePath}`" class="download-btn">下载</a>
                        <button class="delete-file-btn" @click="confirmDeleteFile(file)">删除</button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-chapters">
              <p>暂无章节，请添加章节</p>
            </div>
          </div>

          <!-- 添加章节按钮 -->
          <div class="add-chapter-container">
            <button class="add-chapter-btn" @click="showAddChapter">
              <span class="btn-icon">+</span> 添加章节
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑章节对话框 -->
    <div v-if="showChapterForm" class="modal-overlay" @click.self="closeChapterForm">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditingChapter ? '编辑章节' : '添加章节' }}</h2>
          <button class="close-btn" @click="closeChapterForm">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitChapterForm">
            <div class="form-group">
              <label for="chapterTitle">标题</label>
              <input
                id="chapterTitle"
                v-model="chapterForm.title"
                type="text"
                placeholder="请输入章节标题"
                required
              />
            </div>

            <div class="form-group">
              <label for="chapterDescription">描述</label>
              <textarea
                id="chapterDescription"
                v-model="chapterForm.description"
                placeholder="请输入章节描述（可选）"
                rows="3"
                class="compact-textarea"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="chapterContent">内容</label>
              <textarea
                id="chapterContent"
                v-model="chapterForm.content"
                placeholder="请输入章节内容（可选）"
                rows="6"
                class="compact-textarea"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="chapterFile">上传文件（可选）</label>
              <input
                id="chapterFile"
                type="file"
                @change="handleChapterFileChange"
              />
              <p class="file-hint">支持的文件类型：PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, MP4, MP3, ZIP</p>
            </div>

            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeChapterForm">取消</button>
              <button type="submit" class="submit-btn">{{ isEditingChapter ? '保存修改' : '添加' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 文件上传对话框 -->
    <div v-if="showFileUploadModal" class="modal-overlay" @click.self="closeFileUploadModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>上传文件</h2>
          <button class="close-btn" @click="closeFileUploadModal">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="uploadFile">
            <div class="form-group">
              <label for="file">选择文件</label>
              <input
                id="file"
                type="file"
                @change="handleFileChange"
                required
              />
              <p class="file-hint">支持的文件类型：PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, MP4, MP3, ZIP</p>
            </div>

            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeFileUploadModal">取消</button>
              <button type="submit" class="submit-btn" :disabled="!selectedFile">上传</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认对话框 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal-content delete-confirm-modal">
        <div class="modal-header">
          <h2>确认删除</h2>
          <button class="close-btn" @click="cancelDelete">×</button>
        </div>
        <div class="modal-body">
          <p class="delete-message">{{ deleteMessage }}</p>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="cancelDelete">取消</button>
            <button type="button" class="delete-confirm-btn" @click="executeDelete">确认删除</button>
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
      showForm: false,
      isEditing: false,
      formData: {
        title: '',
        industryType: '',
        description: ''
      },
      showChaptersModal: false,
      currentMaterial: {},
      showChapterForm: false,
      isEditingChapter: false,
      chapterForm: {
        title: '',
        description: '',
        content: '',
        materialId: null,
        sequence: 0
      },
      currentChapter: null,
      showFileUploadModal: false,
      selectedFile: null,
      chapterFile: null,
      // 删除确认对话框
      showDeleteConfirm: false,
      deleteType: '', // 'material', 'chapter', 'file'
      itemToDelete: null,
      deleteMessage: ''
    };
  },
  created() {
    this.fetchTrainingMaterials();
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'enterprise-home' });
    },
    async fetchTrainingMaterials() {
      this.loading = true;
      const enterpriseId = localStorage.getItem('userId');

      try {
        const response = await axios.get(`http://localhost:8080/api/training/enterprise/${enterpriseId}`);
        this.trainingMaterials = response.data;
      } catch (error) {
        console.error('获取培训资料失败:', error);
      } finally {
        this.loading = false;
      }
    },
    showCreateForm() {
      this.isEditing = false;
      this.formData = {
        title: '',
        industryType: '',
        description: ''
      };
      this.showForm = true;
    },
    editMaterial(material) {
      this.isEditing = true;
      this.formData = {
        materialId: material.materialId,
        title: material.title,
        industryType: material.industryType,
        description: material.description
      };
      this.showForm = true;
    },
    closeForm() {
      this.showForm = false;
    },
    async submitForm() {
      const enterpriseId = localStorage.getItem('userId');
      const data = {
        ...this.formData,
        enterpriseId
      };

      try {
        if (this.isEditing) {
          // 更新培训资料
          await axios.put(`http://localhost:8080/api/training/${data.materialId}`, data);
        } else {
          // 创建新培训资料
          await axios.post('http://localhost:8080/api/training', data);
        }

        // 刷新列表
        this.fetchTrainingMaterials();
        this.closeForm();
      } catch (error) {
        console.error('保存培训资料失败:', error);
        alert('操作失败，请重试');
      }
    },
    async viewMaterial(materialId) {
      try {
        const response = await axios.get(`http://localhost:8080/api/training/${materialId}`);
        this.currentMaterial = response.data;
        this.showChaptersModal = true;
      } catch (error) {
        console.error('获取培训资料详情失败:', error);
        alert('获取详情失败，请重试');
      }
    },
    closeChaptersModal() {
      this.showChaptersModal = false;
      this.currentMaterial = {};
    },
    showAddChapter() {
      this.isEditingChapter = false;
      this.chapterForm = {
        title: '',
        description: '',
        content: '',
        materialId: this.currentMaterial.materialId,
        sequence: this.currentMaterial.chapters ? this.currentMaterial.chapters.length + 1 : 1
      };
      this.showChapterForm = true;
    },
    editChapter(chapter) {
      this.isEditingChapter = true;
      this.currentChapter = chapter;
      this.chapterForm = {
        chapterId: chapter.chapterId,
        title: chapter.title,
        description: chapter.description,
        content: chapter.content,
        materialId: chapter.materialId,
        sequence: chapter.sequence
      };
      this.showChapterForm = true;
    },
    closeChapterForm() {
      this.showChapterForm = false;
      this.chapterFile = null;
    },
    handleChapterFileChange(event) {
      this.chapterFile = event.target.files[0];
    },

    async submitChapterForm() {
      try {
        let chapterId;

        if (this.isEditingChapter) {
          // 更新章节
          await axios.put(`http://localhost:8080/api/training/chapter/${this.chapterForm.chapterId}`, this.chapterForm);
          chapterId = this.chapterForm.chapterId;
        } else {
          // 添加新章节
          const response = await axios.post('http://localhost:8080/api/training/chapter', this.chapterForm);
          chapterId = response.data.chapterId;
        }

        // 如果有文件要上传，则上传文件
        if (this.chapterFile) {
          const formData = new FormData();
          formData.append('file', this.chapterFile);

          await axios.post(`http://localhost:8080/api/training/file/${chapterId}`, formData, {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          });

          // 重置文件选择
          this.chapterFile = null;
        }

        // 刷新当前培训资料
        this.viewMaterial(this.currentMaterial.materialId);
        this.closeChapterForm();
      } catch (error) {
        console.error('保存章节失败:', error);
        alert('操作失败，请重试');
      }
    },
    showFileUpload(chapter) {
      this.currentChapter = chapter;
      this.selectedFile = null;
      this.showFileUploadModal = true;
    },
    closeFileUploadModal() {
      this.showFileUploadModal = false;
      this.selectedFile = null;
    },
    handleFileChange(event) {
      this.selectedFile = event.target.files[0];
    },
    async uploadFile() {
      if (!this.selectedFile) {
        alert('请选择文件');
        return;
      }

      const formData = new FormData();
      formData.append('file', this.selectedFile);

      try {
        await axios.post(`http://localhost:8080/api/training/file/${this.currentChapter.chapterId}`, formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        // 刷新当前培训资料
        this.viewMaterial(this.currentMaterial.materialId);
        this.closeFileUploadModal();
      } catch (error) {
        console.error('上传文件失败:', error);
        alert('上传失败，请重试');
      }
    },

    // 确认删除培训资料
    confirmDeleteMaterial(material) {
      this.deleteType = 'material';
      this.itemToDelete = material;
      this.deleteMessage = `确定要删除培训资料 "${material.title}" 吗？删除后将无法恢复。`;
      this.showDeleteConfirm = true;
    },

    // 确认删除章节
    confirmDeleteChapter(chapter) {
      this.deleteType = 'chapter';
      this.itemToDelete = chapter;
      this.deleteMessage = `确定要删除章节 "${chapter.title}" 吗？删除后将无法恢复。`;
      this.showDeleteConfirm = true;
    },

    // 确认删除文件
    confirmDeleteFile(file) {
      this.deleteType = 'file';
      this.itemToDelete = file;
      this.deleteMessage = `确定要删除文件 "${file.fileName}" 吗？删除后将无法恢复。`;
      this.showDeleteConfirm = true;
    },

    // 执行删除操作
    async executeDelete() {
      try {
        if (this.deleteType === 'material') {
          await axios.delete(`http://localhost:8080/api/training/${this.itemToDelete.materialId}`);
          this.fetchTrainingMaterials();
          this.closeChaptersModal();
        } else if (this.deleteType === 'chapter') {
          await axios.delete(`http://localhost:8080/api/training/chapter/${this.itemToDelete.chapterId}`);
          this.viewMaterial(this.currentMaterial.materialId);
        } else if (this.deleteType === 'file') {
          await axios.delete(`http://localhost:8080/api/training/file/${this.itemToDelete.fileId}`);
          this.viewMaterial(this.currentMaterial.materialId);
        }

        this.showDeleteConfirm = false;
        this.itemToDelete = null;
      } catch (error) {
        console.error('删除失败:', error);
        alert('删除失败，请重试');
      }
    },

    // 取消删除
    cancelDelete() {
      this.showDeleteConfirm = false;
      this.itemToDelete = null;
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
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.create-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 15px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.btn-icon {
  margin-right: 5px;
  font-size: 16px;
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
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.materials-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.material-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.material-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.material-title {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.material-type {
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
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
}

.material-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
  margin-bottom: 10px;
}

.material-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.edit-btn, .view-btn {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.edit-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
}

.view-btn {
  background-color: #409EFF;
  color: white;
  border: none;
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
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.chapters-modal {
  width: 700px;
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

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #606266;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn, .submit-btn {
  padding: 8px 15px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  background-color: white;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
}

/* 章节列表样式 */
.chapters-list {
  margin-bottom: 20px;
}

.chapter-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 15px;
}

.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.chapter-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chapter-actions {
  display: flex;
  gap: 10px;
}

.edit-chapter-btn, .upload-file-btn {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.edit-chapter-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
}

.upload-file-btn {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid #e1f3d8;
}

.chapter-content {
  padding: 15px;
}

.chapter-text {
  white-space: pre-line;
  margin-top: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.files-list {
  margin-top: 15px;
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
}

.files-list h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
  font-size: 14px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 5px 0;
}

.file-icon {
  margin-right: 5px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.download-btn {
  color: #409EFF;
  text-decoration: none;
  font-size: 12px;
}

.empty-chapters {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.add-chapter-container {
  text-align: center;
}

.add-chapter-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}

.file-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 删除按钮样式 */
.delete-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.delete-chapter-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.delete-file-btn {
  color: #f56c6c;
  background: none;
  border: none;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
  margin-left: 10px;
}

.file-actions {
  display: flex;
  align-items: center;
}

/* 删除确认对话框样式 */
.delete-confirm-modal {
  width: 400px;
}

.delete-message {
  margin-bottom: 20px;
  color: #606266;
}

.delete-confirm-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

/* 缩小文本框宽度 */
.compact-textarea {
  width: 90%;
  max-width: 450px;
  resize: vertical;
}

/* 文本框宽度已调整 */
</style>
