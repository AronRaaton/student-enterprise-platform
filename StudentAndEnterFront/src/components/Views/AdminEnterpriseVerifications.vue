<template>
  <div class="verifications-container">
    <h2>企业验证申请管理</h2>
    
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    
    <div v-else-if="pendingVerifications.length === 0" class="empty-state">
      <p>当前没有待审核的企业验证申请</p>
    </div>
    
    <div v-else class="verifications-list">
      <div v-for="verification in pendingVerifications" :key="verification.verificationId" class="verification-card">
        <div class="verification-header">
          <h3>{{ verification.enterprise.name }}</h3>
          <span class="status-badge pending">待审核</span>
        </div>
        
        <div class="verification-details">
          <div class="detail-item">
            <span class="label">企业邮箱：</span>
            <span class="value">{{ verification.enterprise.email }}</span>
          </div>
          <div class="detail-item">
            <span class="label">法定代表人：</span>
            <span class="value">{{ verification.legalRepresentative }}</span>
          </div>
          <div class="detail-item">
            <span class="label">提交时间：</span>
            <span class="value">{{ formatDate(verification.submitTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">营业执照：</span>
            <a :href="getBusinessLicenseUrl(verification.businessLicense)" target="_blank" class="license-link">查看营业执照</a>
          </div>
        </div>
        
        <div class="verification-actions">
          <button class="approve-btn" @click="approveVerification(verification.verificationId)">批准</button>
          <button class="reject-btn" @click="rejectVerification(verification.verificationId)">拒绝</button>
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
      pendingVerifications: [],
      loading: true
    };
  },
  created() {
    this.fetchPendingVerifications();
  },
  methods: {
    fetchPendingVerifications() {
      this.loading = true;
      axios.get('http://localhost:8080/api/admin/enterprise-verifications/pending')
        .then(response => {
          this.pendingVerifications = response.data;
          this.loading = false;
        })
        .catch(error => {
          console.error('获取待审核企业验证申请失败:', error);
          this.loading = false;
        });
    },
    
    formatDate(dateString) {
      if (!dateString) return '未知';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    
    getBusinessLicenseUrl(filename) {
      if (!filename) return '#';
      return `http://localhost:8080/uploads/${filename}`;
    },
    
    approveVerification(verificationId) {
      if (!confirm('确定要批准此企业验证申请吗？')) return;
      
      const adminId = localStorage.getItem('userId'); // 假设管理员ID存储在localStorage中
      
      axios.post(`http://localhost:8080/api/admin/enterprise-verifications/${verificationId}/approve?adminId=${adminId}`)
        .then(response => {
          alert('企业验证申请已批准');
          this.fetchPendingVerifications();
        })
        .catch(error => {
          console.error('批准企业验证申请失败:', error);
          alert('操作失败: ' + (error.response?.data?.message || '未知错误'));
        });
    },
    
    rejectVerification(verificationId) {
      if (!confirm('确定要拒绝此企业验证申请吗？')) return;
      
      const adminId = localStorage.getItem('userId'); // 假设管理员ID存储在localStorage中
      
      axios.post(`http://localhost:8080/api/admin/enterprise-verifications/${verificationId}/reject?adminId=${adminId}`)
        .then(response => {
          alert('企业验证申请已拒绝');
          this.fetchPendingVerifications();
        })
        .catch(error => {
          console.error('拒绝企业验证申请失败:', error);
          alert('操作失败: ' + (error.response?.data?.message || '未知错误'));
        });
    }
  }
};
</script>

<style scoped>
.verifications-container {
  max-width: 900px;
  margin: 30px auto;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  background-color: #f9f9f9;
  border-radius: 8px;
  color: #666;
}

.verifications-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.verification-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.verification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.verification-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.pending {
  background-color: #e6a23c;
  color: white;
}

.verification-details {
  padding: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
}

.detail-item .label {
  width: 100px;
  color: #606266;
  font-weight: bold;
}

.detail-item .value {
  flex: 1;
  color: #303133;
}

.license-link {
  color: #409EFF;
  text-decoration: none;
}

.license-link:hover {
  text-decoration: underline;
}

.verification-actions {
  display: flex;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  gap: 10px;
}

.verification-actions button {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
}

.approve-btn {
  background-color: #67c23a;
  color: white;
}

.approve-btn:hover {
  background-color: #5daf34;
}

.reject-btn {
  background-color: #f56c6c;
  color: white;
}

.reject-btn:hover {
  background-color: #e64242;
}
</style>
