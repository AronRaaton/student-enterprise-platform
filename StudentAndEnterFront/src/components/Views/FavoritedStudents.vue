<template>
  <div class="favorited-students-container module-page module-list">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">收藏的学生</h1>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-container">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索学生姓名或学校..." 
          @input="filterStudents"
        />
        <span class="search-icon">🔍</span>
      </div>
      <div class="filter-options">
        <select v-model="schoolFilter" @change="filterStudents">
          <option value="">所有学校</option>
          <option v-for="school in uniqueSchools" :key="school" :value="school">{{ school }}</option>
        </select>
        <select v-model="sortOption" @change="filterStudents">
          <option value="name">按姓名排序</option>
          <option value="completedTasks">按完成任务数排序</option>
          <option value="enrollmentYear">按入学年份排序</option>
        </select>
      </div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="filteredStudents.length === 0" class="empty-state">
      <div class="empty-icon">👤</div>
      <p v-if="students.length === 0">您还没有收藏任何学生</p>
      <p v-else>没有找到匹配的学生</p>
      <button class="action-btn" @click="goToAllTasks">浏览任务</button>
    </div>

    <!-- 学生列表 -->
    <div v-else class="students-grid">
      <div 
        v-for="student in filteredStudents" 
        :key="student.studentId" 
        class="student-card"
        @click="viewStudentDetails(student.studentId)"
      >
        <div class="student-avatar">
          {{ student.name ? student.name.charAt(0) : 'S' }}
        </div>
        <div class="student-info">
          <h3 class="student-name">{{ student.name }}</h3>
          <p class="student-school">{{ student.school || '未知学校' }}</p>
          <div class="student-meta">
            <span class="meta-item">
              <i class="meta-icon">📚</i>
              完成任务: {{ student.completedTasks || 0 }}
            </span>
            <span class="meta-item">
              <i class="meta-icon">🎓</i>
              入学年份: {{ student.enrollmentYear || '未知' }}
            </span>
          </div>
        </div>
        <button class="unfavorite-btn" @click.stop="unfavoriteStudent(student.studentId)">
          <span class="unfavorite-icon">★</span>
          取消收藏
        </button>
      </div>
    </div>

    <!-- 分页控制 -->
    <div v-if="filteredStudents.length > 0" class="pagination-controls">
      <button 
        class="pagination-btn" 
        :disabled="currentPage === 1" 
        @click="changePage(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
      <button 
        class="pagination-btn" 
        :disabled="currentPage === totalPages" 
        @click="changePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      students: [],
      filteredStudents: [],
      loading: true,
      searchQuery: '',
      schoolFilter: '',
      sortOption: 'name',
      currentPage: 1,
      itemsPerPage: 12,
      enterpriseId: null
    };
  },
  computed: {
    uniqueSchools() {
      const schools = this.students
        .map(student => student.school)
        .filter(school => school); // Filter out null/undefined
      return [...new Set(schools)]; // Remove duplicates
    },
    totalPages() {
      return Math.ceil(this.filteredStudents.length / this.itemsPerPage);
    },
    paginatedStudents() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredStudents.slice(start, end);
    }
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.fetchFavoritedStudents();
  },
  methods: {
    fetchFavoritedStudents() {
      this.loading = true;
      axios.get(`http://localhost:8080/api/enterprises/${this.enterpriseId}/favorites`)
        .then(response => {
          this.students = response.data;
          this.filterStudents();
        })
        .catch(error => {
          console.error('无法获取收藏的学生:', error);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    filterStudents() {
      let result = [...this.students];
      
      // Apply search filter
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(student => 
          (student.name && student.name.toLowerCase().includes(query)) ||
          (student.school && student.school.toLowerCase().includes(query))
        );
      }
      
      // Apply school filter
      if (this.schoolFilter) {
        result = result.filter(student => student.school === this.schoolFilter);
      }
      
      // Apply sorting
      result.sort((a, b) => {
        if (this.sortOption === 'name') {
          return (a.name || '').localeCompare(b.name || '');
        } else if (this.sortOption === 'completedTasks') {
          return (b.completedTasks || 0) - (a.completedTasks || 0);
        } else if (this.sortOption === 'enrollmentYear') {
          return (b.enrollmentYear || 0) - (a.enrollmentYear || 0);
        }
        return 0;
      });
      
      this.filteredStudents = result;
      this.currentPage = 1; // Reset to first page when filtering
    },
    unfavoriteStudent(studentId) {
      if (!confirm('确定要取消收藏该学生吗？')) return;
      
      axios.delete(`http://localhost:8080/api/enterprises/${this.enterpriseId}/favorites/${studentId}`)
        .then(() => {
          this.students = this.students.filter(student => student.studentId !== studentId);
          this.filterStudents();
        })
        .catch(error => {
          console.error('取消收藏失败:', error);
          alert('取消收藏失败，请重试');
        });
    },
    viewStudentDetails(studentId) {
      this.$router.push({ name: 'StudentDetail', params: { studentId } });
    },
    goBack() {
      this.$router.go(-1);
    },
    goToAllTasks() {
      this.$router.push({ name: 'all-task' });
    },
    changePage(page) {
      this.currentPage = page;
    }
  }
};
</script>

<style scoped>
.favorited-students-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  background-color: transparent;
  border: none;
  display: flex;
  align-items: center;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  flex: 1;
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin: 0;
}

/* 搜索和筛选区域 */
.search-filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
  background-color: #fff;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-box {
  flex: 1;
  min-width: 250px;
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 10px 15px 10px 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.search-box input:focus {
  outline: none;
  border-color: #409EFF;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
}

.filter-options {
  display: flex;
  gap: 10px;
}

.filter-options select {
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.3s;
}

.filter-options select:focus {
  outline: none;
  border-color: #409EFF;
}

/* 加载中状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-radius: 50%;
  border-top-color: #409EFF;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #909399;
}

.empty-state p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #66b1ff;
}

/* 学生列表 */
.students-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.student-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  position: relative;
  display: flex;
  padding: 20px;
}

.student-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.student-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 15px;
  flex-shrink: 0;
}

.student-info {
  flex: 1;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #303133;
}

.student-school {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.student-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

.meta-icon {
  margin-right: 5px;
  font-size: 16px;
}

.unfavorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: transparent;
  border: none;
  color: #f56c6c;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.unfavorite-btn:hover {
  background-color: rgba(245, 108, 108, 0.1);
}

.unfavorite-icon {
  margin-right: 5px;
  color: #e6a23c;
}

/* 分页控制 */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
}

.pagination-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #66b1ff;
}

.pagination-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.page-info {
  margin: 0 15px;
  font-size: 14px;
  color: #606266;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .search-filter-container {
    flex-direction: column;
  }
  
  .filter-options {
    width: 100%;
  }
  
  .students-grid {
    grid-template-columns: 1fr;
  }
  
  .student-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .student-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .student-meta {
    justify-content: center;
  }
  
  .unfavorite-btn {
    position: static;
    margin-top: 15px;
  }
}
</style>
