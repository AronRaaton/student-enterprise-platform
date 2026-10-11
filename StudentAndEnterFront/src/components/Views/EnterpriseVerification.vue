<template>
  <div class="verification-container module-page module-detail">
    <div class="page-header"><button type="button" class="back-button" @click="$router.push('/enterprise-home')">← 返回</button><h2 class="page-title">企业身份验证</h2></div>
    
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    
    <div v-else-if="verificationStatus === 'APPROVED'" class="success-message">
      <h3>您的企业身份已验证通过！</h3>
      <p>您现在可以使用所有企业功能。</p>
    </div>
    
    <div v-else-if="verificationStatus === 'REJECTED'" class="error-message">
      <h3>您的企业身份验证未通过</h3>
      <p>请重新提交验证材料。</p>
    </div>
    
    <div v-else-if="verificationStatus === 'PENDING'" class="info-message">
      <h3>您的企业身份验证正在审核中</h3>
      <p>请耐心等待管理员审核，审核通过后您将可以使用所有企业功能。</p>
    </div>
    
    <form v-else @submit.prevent="submitVerification" class="verification-form">
      <div class="info-message">
        <p>请提交企业身份验证材料，管理员审核通过后您将可以使用所有企业功能。</p>
      </div>
      
      <div>
        <label for="legalRepresentative">法定代表人：</label>
        <input type="text" id="legalRepresentative" v-model="formData.legalRepresentative" required />
      </div>
      
      <div>
        <label for="businessLicense">营业执照：</label>
        <div class="file-upload-container">
          <input type="file" id="businessLicense" @change="handleFileUpload" accept="image/*,.pdf" required />
          <div v-if="formData.businessLicense" class="file-preview">
            <span>已选择文件: {{ formData.businessLicense.name }}</span>
            <button type="button" class="remove-file" @click="removeFile">删除</button>
          </div>
        </div>
      </div>
      
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '提交中...' : '提交验证' }}</button>
      
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </form>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      loading: true,
      verificationStatus: null,
      formData: {
        legalRepresentative: '',
        businessLicense: null
      },
      isSubmitting: false,
      errorMessage: '',
      enterpriseId: null
    };
  },
  created() {
    this.enterpriseId = localStorage.getItem('userId');
    this.checkVerificationStatus();
  },
  methods: {
    // 检查企业验证状态
    async checkVerificationStatus() {
      this.loading = true;
      try {
        const response = await axios.get(`http://localhost:8080/api/auth/enterprise/${this.enterpriseId}/verification-status`);
        if (response.data) {
          this.verificationStatus = response.data.verificationStatus;
        }
      } catch (error) {
        console.error('获取验证状态失败:', error);
        this.verificationStatus = null;
      } finally {
        this.loading = false;
      }
    },
    
    // 处理文件上传
    handleFileUpload(event) {
      this.formData.businessLicense = event.target.files[0];
    },
    
    // 删除文件
    removeFile() {
      this.formData.businessLicense = null;
    },
    
    // 提交验证
    async submitVerification() {
      if (!this.formData.legalRepresentative || !this.formData.businessLicense) {
        this.errorMessage = '请填写所有必填字段并上传营业执照';
        return;
      }
      
      this.isSubmitting = true;
      this.errorMessage = '';
      
      try {
        // 创建FormData对象以便于上传文件
        const formData = new FormData();
        formData.append('enterpriseId', this.enterpriseId);
        formData.append('legalRepresentative', this.formData.legalRepresentative);
        formData.append('businessLicense', this.formData.businessLicense);
        
        const response = await axios.post('http://localhost:8080/api/auth/enterprise/verify', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        console.log('验证提交成功:', response.data);
        this.verificationStatus = 'PENDING';
      } catch (error) {
        console.error('验证提交失败:', error);
        
        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = '提交失败，请稍后重试';
        }
      } finally {
        this.isSubmitting = false;
      }
    }
  }
};
</script>

<style scoped>
.verification-container {
  max-width: 600px;
  margin: 50px auto;
  padding: 30px;
  background-color: #f4f4f4;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 1.8em;
  color: #333;
}

.verification-form div {
  margin-bottom: 15px;
}

label {
  font-size: 1.1em;
  margin-bottom: 5px;
  display: block;
}

input[type="text"],
input[type="file"] {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  font-size: 1em;
  border: 1px solid #ccc;
  border-radius: 5px;
}

button {
  width: 100%;
  padding: 12px;
  background-color: #4caf50;
  color: white;
  font-size: 1.2em;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 20px;
}

button:hover:not(:disabled) {
  background-color: #45a049;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 20px;
}

.file-upload-container {
  margin-top: 5px;
}

.file-preview {
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f8ff;
  border-radius: 5px;
  border: 1px solid #d0e3ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remove-file {
  background-color: #f44336;
  color: white;
  font-size: 0.8em;
  padding: 5px 10px;
  width: auto;
  margin-top: 0;
}

.remove-file:hover {
  background-color: #d32f2f;
}

.error-message {
  color: #f44336;
  margin-top: 15px;
  padding: 15px;
  background-color: #ffebee;
  border-radius: 4px;
  text-align: center;
}

.info-message {
  color: #1677ff;
  margin: 15px 0;
  padding: 15px;
  background-color: #e6f7ff;
  border-radius: 4px;
  text-align: left;
  border-left: 4px solid #1677ff;
}

.success-message {
  text-align: center;
  padding: 20px;
  background-color: #e8f5e9;
  border-radius: 8px;
  margin: 20px 0;
}

.success-message h3 {
  color: #2e7d32;
  margin-bottom: 15px;
}
</style>
