<template>
  <div class="register-container">
    <h2>用户注册</h2>

    <!-- 步骤指示器 -->
    <div class="steps-indicator">
      <div class="step" :class="{ 'active': currentStep === 1, 'completed': currentStep > 1 }">
        <div class="step-number">1</div>
        <div class="step-label">基本信息</div>
      </div>
      <div class="step-line" :class="{ 'completed': currentStep > 1 }"></div>
      <div class="step" :class="{ 'active': currentStep === 2, 'completed': currentStep > 2 }">
        <div class="step-number">2</div>
        <div class="step-label">身份验证</div>
      </div>
    </div>

    <!-- 第一步：基本信息 -->
    <form v-if="currentStep === 1" @submit.prevent="goToStep2">
      <div>
        <label for="email">邮箱：</label>
        <input type="email" id="email" v-model="formData.email" required />
      </div>
      <div>
        <label for="name">姓名/企业名称：</label>
        <input type="text" id="name" v-model="formData.name" required />
      </div>
      <div>
        <label for="password">密码：</label>
        <input type="password" id="password" v-model="formData.password" required />
      </div>
      <div>
        <label for="confirmPassword">确认密码：</label>
        <input type="password" id="confirmPassword" v-model="confirmPassword" required />
      </div>
      <div>
        <label for="role">角色：</label>
        <select v-model="formData.role" id="role">
          <option value="student">学生</option>
          <option value="enterprise">企业</option>
        </select>
      </div>
      <button type="submit" :disabled="isSubmitting">下一步</button>
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </form>

    <!-- 第二步：学生身份验证 -->
    <form v-if="currentStep === 2 && formData.role === 'student'" @submit.prevent="handleStudentRegister">
      <div>
        <label for="verificationCode">学信网在线验证码：</label>
        <div class="verification-code-container">
          <input type="text" id="verificationCode" v-model="formData.verificationCode" required placeholder="请输入学信网在线验证码" />
          <a href="https://www.chsi.com.cn" target="_blank" class="verification-link">获取验证码</a>
        </div>
        <div class="verification-tip">请前往学信网获取在线验证码，用于验证学生身份</div>
      </div>
      <div>
        <label for="school">学校：</label>
        <input type="text" id="school" v-model="formData.school" required />
      </div>
      <div>
        <label for="studentNumber">学号：</label>
        <input type="text" id="studentNumber" v-model="formData.studentNumber" required />
      </div>
      <div>
        <label for="major">专业：</label>
        <input type="text" id="major" v-model="formData.major" required />
      </div>
      <div>
        <label for="enrollmentYear">入学年份：</label>
        <select id="enrollmentYear" v-model="formData.enrollmentYear" required>
          <option v-for="year in enrollmentYears" :key="year" :value="year">{{ year }}</option>
        </select>
      </div>
      <div class="button-group">
        <button type="button" class="back-button" @click="currentStep = 1">上一步</button>
        <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '注册中...' : '提交注册' }}</button>
      </div>
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </form>

    <!-- 第二步：企业身份验证 -->
    <form v-if="currentStep === 2 && formData.role === 'enterprise'" @submit.prevent="handleEnterpriseRegister">
      <div class="info-message">
        <p>请提交企业身份验证材料，管理员审核通过后您将收到邮件通知。</p>
      </div>
      <div>
        <label for="legalRepresentative">法定代表人：</label>
        <input type="text" id="legalRepresentative" v-model="formData.legalRepresentative" required />
      </div>
      <div>
        <label for="businessLicense">营业执照：</label>
        <div class="file-upload-container">
          <input type="file" id="businessLicense" @change="handleFileUpload($event, 'businessLicense')" accept="image/*,.pdf" required />
          <div v-if="formData.businessLicense" class="file-preview">
            <span>已选择文件: {{ formData.businessLicense.name }}</span>
            <button type="button" class="remove-file" @click="removeFile('businessLicense')">删除</button>
          </div>
        </div>
      </div>
      <div>
        <label for="description">企业描述（可选）：</label>
        <textarea id="description" v-model="formData.description" rows="4"></textarea>
      </div>
      <div class="button-group">
        <button type="button" class="back-button" @click="currentStep = 1">上一步</button>
        <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '注册中...' : '提交注册' }}</button>
      </div>
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </form>

    <!-- 注册成功提示 -->
    <div v-if="registrationSuccess" class="success-message">
      <h3>注册提交成功！</h3>
      <p v-if="formData.role === 'student'">您的学生账号已注册成功，现在可以登录了。</p>
      <p v-if="formData.role === 'enterprise'">您的企业账号注册申请已提交，请等待管理员审核。审核通过后您将收到邮件通知。</p>
      <button @click="goToLogin" class="primary-button">前往登录</button>
    </div>

    <div class="login-link" v-if="!registrationSuccess">
      <router-link to="/login">已有账号？点击这里登录</router-link>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    // 生成入学年份选项，从当前年份往前20年
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = 0; i < 20; i++) {
      years.push(currentYear - i);
    }

    return {
      currentStep: 1,
      enrollmentYears: years,
      formData: {
        email: '',
        password: '',
        name: '',
        role: 'student',
        // 学生特有字段
        verificationCode: '',
        school: '',
        studentNumber: '',
        major: '',
        enrollmentYear: currentYear,
        // 企业特有字段
        legalRepresentative: '',
        businessLicense: null,
        description: ''
      },
      confirmPassword: '',
      isSubmitting: false,
      errorMessage: '',
      registrationSuccess: false
    };
  },
  methods: {
    // 验证第一步表单并进入第二步
    goToStep2() {
      // 验证必填字段
      if (!this.formData.email || !this.formData.password || !this.formData.name) {
        this.errorMessage = '请填写所有必填字段';
        return;
      }

      // 验证密码一致性
      if (this.formData.password !== this.confirmPassword) {
        this.errorMessage = '两次输入的密码不一致';
        return;
      }

      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.formData.email)) {
        this.errorMessage = '请输入有效的邮箱地址';
        return;
      }

      // 验证密码强度
      if (this.formData.password.length < 6) {
        this.errorMessage = '密码长度不能少于6个字符';
        return;
      }

      // 清除错误信息并进入第二步
      this.errorMessage = '';
      this.currentStep = 2;
    },





    // 学生注册提交
    async handleStudentRegister() {
      if (!this.formData.verificationCode || !this.formData.school || !this.formData.studentNumber || !this.formData.major) {
        this.errorMessage = '请填写所有必填字段';
        return;
      }

      // 验证学号格式
      if (!/^[a-zA-Z0-9]+$/.test(this.formData.studentNumber)) {
        this.errorMessage = '学号只能包含字母和数字';
        return;
      }

      this.isSubmitting = true;
      this.errorMessage = '';

      try {
        // 在实际应用中，这里应该调用后端接口验证学信网验证码
        // 目前我们只需要提供验证码，不需要验证其真实性
        const response = await axios.post('http://localhost:8080/api/auth/register/student', {
          email: this.formData.email,
          password: this.formData.password,
          name: this.formData.name,
          verificationCode: this.formData.verificationCode,
          school: this.formData.school,
          studentNumber: this.formData.studentNumber,
          major: this.formData.major,
          enrollmentYear: this.formData.enrollmentYear
        });

        console.log('学生注册成功:', response.data);
        this.registrationSuccess = true;
      } catch (error) {
        console.error('学生注册失败:', error);

        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else {
          this.errorMessage = '注册失败，请稍后重试';
        }
      } finally {
        this.isSubmitting = false;
      }
    },

    // 处理文件上传
    handleFileUpload(event, fieldName) {
      this.formData[fieldName] = event.target.files[0];
    },

    // 删除文件
    removeFile(fieldName) {
      this.formData[fieldName] = null;
    },

    // 企业注册提交
    async handleEnterpriseRegister() {
      if (!this.formData.legalRepresentative || !this.formData.businessLicense) {
        this.errorMessage = '请填写法定代表人并上传营业执照';
        return;
      }

      this.isSubmitting = true;
      this.errorMessage = '';

      try {
        // 创建FormData对象以便于上传文件
        const formData = new FormData();
        formData.append('email', this.formData.email);
        formData.append('password', this.formData.password);
        formData.append('name', this.formData.name);
        formData.append('legalRepresentative', this.formData.legalRepresentative);
        formData.append('businessLicense', this.formData.businessLicense);
        formData.append('description', this.formData.description || '');

        console.log('准备提交企业注册请求，文件名：', this.formData.businessLicense.name);
        console.log('文件大小：', this.formData.businessLicense.size, '字节');

        // 验证文件大小
        if (this.formData.businessLicense.size > 10 * 1024 * 1024) {
          this.errorMessage = '文件大小不能超过10MB';
          this.isSubmitting = false;
          return;
        }

        console.log('发送企业注册请求...');
        const response = await axios.post('http://localhost:8080/api/auth/register/enterprise', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          timeout: 60000 // 增加超时时间为60秒
        });
        console.log('收到响应:', response);

        console.log('企业注册成功:', response.data);
        this.registrationSuccess = true;
      } catch (error) {
        console.error('企业注册失败:', error);

        if (error.response && error.response.data && error.response.data.message) {
          this.errorMessage = error.response.data.message;
        } else if (error.code === 'ERR_NETWORK') {
          this.errorMessage = '网络错误，请检查服务器是否运行正常';
        } else if (error.code === 'ECONNABORTED') {
          this.errorMessage = '请求超时，请稍后重试';
        } else {
          this.errorMessage = '注册失败，请稍后重试: ' + (error.message || '未知错误');
        }
      } finally {
        this.isSubmitting = false;
      }
    },

    // 跳转到登录页面
    goToLogin() {
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
.register-container {
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

/* 步骤指示器样式 */
.steps-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #e0e0e0;
  color: #757575;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.step-label {
  font-size: 14px;
  color: #757575;
  transition: all 0.3s ease;
}

.step-line {
  flex: 1;
  height: 2px;
  background-color: #e0e0e0;
  margin: 0 15px;
  position: relative;
  top: -15px;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background-color: #1677ff;
  color: white;
}

.step.active .step-label {
  color: #1677ff;
  font-weight: bold;
}

.step.completed .step-number {
  background-color: #4caf50;
  color: white;
}

.step-line.completed {
  background-color: #4caf50;
}

/* 表单样式 */
form div {
  margin-bottom: 15px;
}

label {
  font-size: 1.1em;
  margin-bottom: 5px;
  display: block;
}

input[type="email"],
input[type="password"],
input[type="text"],
select,
textarea {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  font-size: 1em;
  border: 1px solid #ccc;
  border-radius: 5px;
}

textarea {
  resize: vertical;
  min-height: 100px;
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
}

button:hover:not(:disabled) {
  background-color: #45a049;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.button-group button {
  flex: 1;
}

.back-button {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.back-button:hover:not(:disabled) {
  background-color: #e0e0e0;
}

.primary-button {
  background-color: #1677ff;
}

.primary-button:hover:not(:disabled) {
  background-color: #0958d9;
}

/* 验证码输入框样式 */
.verification-code-container {
  display: flex;
  gap: 10px;
}

.verification-code-container input {
  flex: 1;
}

.verification-link {
  display: inline-block;
  padding: 10px 15px;
  background-color: #1677ff;
  color: white;
  text-decoration: none;
  border-radius: 5px;
  font-size: 0.9em;
  white-space: nowrap;
  transition: background-color 0.3s;
}

.verification-link:hover {
  background-color: #0958d9;
}

.verification-tip {
  margin-top: 5px;
  font-size: 0.85em;
  color: #606266;
}

/* 文件上传样式 */
.file-upload-container {
  margin-top: 5px;
}

.file-preview {
  margin-top: 10px;
  padding: 10px;
  background-color: #f0f8ff;
  border-radius: 5px;
  border: 1px solid #d0e3ff;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  padding-bottom: 5px;
  border-bottom: 1px solid #e0e0e0;
}

.file-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.remove-file {
  background-color: #f44336;
  color: white;
  font-size: 0.8em;
  padding: 5px 10px;
  width: auto;
}

.remove-file:hover {
  background-color: #d32f2f;
}

/* 消息样式 */
.error-message {
  color: #f44336;
  margin-top: 15px;
  padding: 10px;
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

.info-message p {
  margin: 8px 0;
}

.info-message ol {
  margin: 8px 0;
  padding-left: 20px;
}

.info-message li {
  margin: 5px 0;
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

.success-message p {
  margin-bottom: 20px;
  color: #333;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}

.login-link a {
  color: #1677ff;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
