<template>
  <div class="project-publish-container module-page module-form">
    <!-- 顶部导航栏 -->
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <span class="back-icon">←</span> 返回
      </button>
      <h1 class="page-title">{{ isEditing ? '编辑项目合作' : '发布项目合作' }}</h1>
    </div>

    <!-- 项目发布表单 -->
    <div class="form-container">
      <div class="form-group">
        <label for="title">项目标题 <span class="required">*</span></label>
        <input
          type="text"
          id="title"
          v-model="project.title"
          placeholder="请输入项目标题"
          class="form-control"
          :class="{ 'is-invalid': submitted && !project.title }"
        />
        <div v-if="submitted && !project.title" class="invalid-feedback">
          请输入项目标题
        </div>
      </div>

      <div class="form-group">
        <label for="description">项目描述 <span class="required">*</span></label>
        <textarea
          id="description"
          v-model="project.description"
          placeholder="请详细描述项目内容、目标和预期成果"
          class="form-control"
          rows="6"
          :class="{ 'is-invalid': submitted && !project.description }"
        ></textarea>
        <div v-if="submitted && !project.description" class="invalid-feedback">
          请输入项目描述
        </div>
      </div>

      <div class="form-group">
        <label for="projectDirection">项目方向 <span class="required">*</span></label>
        <input
          type="text"
          id="projectDirection"
          v-model="project.projectDirection"
          placeholder="例如：Web开发、移动应用、数据分析等"
          class="form-control"
          :class="{ 'is-invalid': submitted && !project.projectDirection }"
        />
        <div v-if="submitted && !project.projectDirection" class="invalid-feedback">
          请输入项目方向
        </div>
      </div>

      <div class="form-group">
        <label for="requiredSkills">所需技能 <span class="required">*</span></label>
        <textarea
          id="requiredSkills"
          v-model="project.requiredSkills"
          placeholder="请列出参与项目所需的技能和经验要求"
          class="form-control"
          rows="4"
          :class="{ 'is-invalid': submitted && !project.requiredSkills }"
        ></textarea>
        <div v-if="submitted && !project.requiredSkills" class="invalid-feedback">
          请输入所需技能
        </div>
      </div>

      <div class="form-row">
        <div class="form-group half-width">
          <label for="maxParticipants">最大参与人数</label>
          <input
            type="number"
            id="maxParticipants"
            v-model="project.maxParticipants"
            min="1"
            max="50"
            class="form-control"
          />
        </div>

        <div class="form-group half-width">
          <label for="startDate">开始日期</label>
          <input
            type="date"
            id="startDate"
            v-model="project.startDate"
            class="form-control"
          />
        </div>
      </div>

      <div class="form-group">
        <label for="endDate">结束日期</label>
        <input
          type="date"
          id="endDate"
          v-model="project.endDate"
          class="form-control"
        />
      </div>

      <div class="form-group">
        <label for="collaborationType">合作意向类型 <span class="required">*</span></label>
        <select
          id="collaborationType"
          v-model="project.collaborationType"
          class="form-control"
          :class="{ 'is-invalid': submitted && !project.collaborationType }"
        >
          <option value="">请选择合作意向类型</option>
          <option value="订单培养">订单培养</option>
          <option value="共建实训基地">实训基地</option>
          <option value="产学研合作">产学研合作</option>
        </select>
        <div v-if="submitted && !project.collaborationType" class="invalid-feedback">
          请选择合作意向类型
        </div>
      </div>

      <!-- 实训基地类型的字段 -->
      <div v-if="project.collaborationType === '共建实训基地'">
        <div class="form-group">
          <label for="location">地点安排 <span class="required">*</span></label>
          <input
            type="text"
            id="location"
            v-model="project.location"
            placeholder="请输入项目地点安排"
            class="form-control"
            :class="{ 'is-invalid': submitted && project.collaborationType === '共建实训基地' && !project.location }"
          />
          <div v-if="submitted && project.collaborationType === '共建实训基地' && !project.location" class="invalid-feedback">
            请输入地点安排
          </div>
        </div>

        <div class="form-group">
          <label for="timeCycle">时间周期 <span class="required">*</span></label>
          <input
            type="text"
            id="timeCycle"
            v-model="project.timeCycle"
            placeholder="例如：3个月、半年、一学期等"
            class="form-control"
            :class="{ 'is-invalid': submitted && project.collaborationType === '共建实训基地' && !project.timeCycle }"
          />
          <div v-if="submitted && project.collaborationType === '共建实训基地' && !project.timeCycle" class="invalid-feedback">
            请输入时间周期
          </div>
        </div>

        <div class="form-group">
          <label for="trainingPlan">培训计划 <span class="required">*</span></label>
          <textarea
            id="trainingPlan"
            v-model="project.trainingPlan"
            placeholder="请详细描述项目的培训计划和学习路径"
            class="form-control"
            rows="4"
            :class="{ 'is-invalid': submitted && project.collaborationType === '共建实训基地' && !project.trainingPlan }"
          ></textarea>
          <div v-if="submitted && project.collaborationType === '共建实训基地' && !project.trainingPlan" class="invalid-feedback">
            请输入培训计划
          </div>
        </div>

        <div class="form-group">
          <label for="resources">企业提供的资源 <span class="required">*</span></label>
          <textarea
            id="resources"
            v-model="project.resources"
            placeholder="请详细描述企业能提供的资源，如设备、场地、指导人员等"
            class="form-control"
            rows="4"
            :class="{ 'is-invalid': submitted && project.collaborationType === '共建实训基地' && !project.resources }"
          ></textarea>
          <div v-if="submitted && project.collaborationType === '共建实训基地' && !project.resources" class="invalid-feedback">
            请输入企业提供的资源
          </div>
        </div>
      </div>

      <!-- 产学研合作类型的字段 -->
      <div v-if="project.collaborationType === '产学研合作'">
        <div class="form-group">
          <label for="techRequirement">企业技术需求 <span class="required">*</span></label>
          <textarea
            id="techRequirement"
            v-model="project.techRequirement"
            placeholder="请详细描述企业的技术需求、市场痛点或技术难题"
            class="form-control"
            rows="6"
            :class="{ 'is-invalid': submitted && project.collaborationType === '产学研合作' && !project.techRequirement }"
          ></textarea>
          <div v-if="submitted && project.collaborationType === '产学研合作' && !project.techRequirement" class="invalid-feedback">
            请输入企业技术需求
          </div>
        </div>

        <div class="project-steps-info">
          <h3 class="steps-title">产学研合作项目流程</h3>
          <div class="step-item">
            <div class="step-number">1</div>
            <div class="step-content">
              <h4>需求分析与问题识别</h4>
              <p>学生提交可转化的科研成果，企业审核确定是否符合需求</p>
            </div>
          </div>
          <div class="step-item">
            <div class="step-number">2</div>
            <div class="step-content">
              <h4>项目立项与资源整合</h4>
              <p>企业上传计划书，学生确认是否继续参与</p>
            </div>
          </div>
          <div class="step-item">
            <div class="step-number">3</div>
            <div class="step-content">
              <h4>项目实施与动态监控</h4>
              <p>学生负责技术攻关，企业提供试验场地和市场反馈</p>
            </div>
          </div>
          <div class="step-item">
            <div class="step-number">4</div>
            <div class="step-content">
              <h4>成果转化与市场推广</h4>
              <p>企业提供技术成果相关资料和市场表现信息</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 所有合作类型都需要的资金或技术支持字段 -->
      <div class="form-group">
        <label for="support">资金或技术支持 <span class="required">*</span></label>
        <textarea
          id="support"
          v-model="project.support"
          placeholder="请详细描述企业能提供的资金或技术支持"
          class="form-control"
          rows="4"
          :class="{ 'is-invalid': submitted && !project.support }"
        ></textarea>
        <div v-if="submitted && !project.support" class="invalid-feedback">
          请输入资金或技术支持
        </div>
      </div>

      <div class="form-actions">
        <button class="cancel-btn" @click="goBack">取消</button>
        <button class="submit-btn" @click="submitProject" :disabled="isSubmitting">
          {{ isSubmitting ? '处理中...' : (isEditing ? '保存修改' : '发布项目') }}
        </button>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccess" class="success-message">
      <div class="success-content">
        <div class="success-icon">✓</div>
        <h2>{{ isEditing ? '项目更新成功！' : '项目发布成功！' }}</h2>
        <p>{{ isEditing ? '您的项目已成功更新，可以在项目管理页面查看和管理该项目' : '您可以在项目管理页面查看和管理该项目' }}</p>
        <div class="success-actions">
          <button class="primary-btn" @click="goToProjects">查看我的项目</button>
          <button v-if="!isEditing" class="secondary-btn" @click="publishAnother">发布另一个项目</button>
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
      project: {
        enterpriseId: null,
        title: '',
        description: '',
        projectDirection: '',
        requiredSkills: '',
        maxParticipants: 10,
        startDate: '',
        endDate: '',
        status: 'OPEN',
        collaborationType: '',
        location: '',
        trainingPlan: '',
        timeCycle: '',
        resources: '',
        support: '',
        techRequirement: '',
        projectPlan: '',
        currentStep: 1
      },
      submitted: false,
      isSubmitting: false,
      showSuccess: false,
      isEditing: false,
      projectId: null
    };
  },
  created() {
    // 获取企业ID
    this.project.enterpriseId = localStorage.getItem('userId');

    // 检查是否是编辑模式
    if (this.$route.query.edit && this.$route.query.projectId) {
      this.isEditing = true;
      this.projectId = this.$route.query.projectId;
      this.fetchProjectDetails();
    }
  },
  methods: {
    goBack() {
      this.$router.go(-1);
    },
    async fetchProjectDetails() {
      try {
        const response = await axios.get(`http://localhost:8080/api/projects/${this.projectId}`);
        const projectData = response.data;

        // 填充表单数据
        this.project = {
          enterpriseId: projectData.enterpriseId,
          title: projectData.title,
          description: projectData.description,
          projectDirection: projectData.projectDirection,
          requiredSkills: projectData.requiredSkills,
          maxParticipants: projectData.maxParticipants,
          startDate: projectData.startDate,
          endDate: projectData.endDate,
          status: projectData.status,
          collaborationType: projectData.collaborationType,
          location: projectData.location || '',
          trainingPlan: projectData.trainingPlan || '',
          timeCycle: projectData.timeCycle || '',
          resources: projectData.resources || '',
          support: projectData.support || '',
          techRequirement: projectData.techRequirement || '',
          projectPlan: projectData.projectPlan || '',
          currentStep: projectData.currentStep || 1
        };
      } catch (error) {
        console.error('获取项目详情失败:', error);
        alert('获取项目详情失败，请重试');
        this.$router.go(-1);
      }
    },

    async submitProject() {
      this.submitted = true;

      // 基本字段验证
      if (!this.project.title || !this.project.description || !this.project.projectDirection ||
          !this.project.requiredSkills || !this.project.collaborationType || !this.project.support) {
        return;
      }

      // 根据合作类型验证不同字段
      if (this.project.collaborationType === '共建实训基地') {
        if (!this.project.location || !this.project.timeCycle || !this.project.trainingPlan || !this.project.resources) {
          return;
        }
      } else if (this.project.collaborationType === '产学研合作') {
        if (!this.project.techRequirement) {
          return;
        }
      }

      this.isSubmitting = true;

      try {
        let response;
        if (this.isEditing) {
          // 编辑模式，使用PUT请求更新项目
          response = await axios.put(`http://localhost:8080/api/projects/${this.projectId}`, this.project);
          console.log('项目更新成功:', response.data);
        } else {
          // 创建模式，使用POST请求创建项目
          response = await axios.post('http://localhost:8080/api/projects', this.project);
          console.log('项目发布成功:', response.data);
        }
        this.showSuccess = true;
      } catch (error) {
        console.error(this.isEditing ? '项目更新失败:' : '项目发布失败:', error);
        alert(this.isEditing ? '项目更新失败，请稍后重试' : '项目发布失败，请稍后重试');
      } finally {
        this.isSubmitting = false;
      }
    },
    goToProjects() {
      this.$router.push({ name: 'EnterpriseProjects' });
    },
    publishAnother() {
      this.project = {
        enterpriseId: localStorage.getItem('userId'),
        title: '',
        description: '',
        projectDirection: '',
        requiredSkills: '',
        maxParticipants: 10,
        startDate: '',
        endDate: '',
        status: 'OPEN',
        collaborationType: '',
        location: '',
        trainingPlan: '',
        timeCycle: '',
        resources: '',
        support: '',
        techRequirement: '',
        projectPlan: '',
        currentStep: 1
      };
      this.submitted = false;
      this.showSuccess = false;
    }
  }
};
</script>

<style scoped>
.project-publish-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eaeaea;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  color: #409EFF;
  font-size: 16px;
  cursor: pointer;
  padding: 5px 10px;
  margin-right: 15px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #ecf5ff;
}

.back-icon {
  margin-right: 5px;
  font-size: 18px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
  flex-grow: 1;
}

.form-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.half-width {
  flex: 1;
  margin-bottom: 0;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #606266;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-control:focus {
  border-color: #409EFF;
  outline: none;
}

.form-control.is-invalid {
  border-color: #f56c6c;
}

.invalid-feedback {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 12px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background-color: #e9ebef;
}

.submit-btn {
  padding: 12px 25px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 成功提示样式 */
.success-message {
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

.success-content {
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  text-align: center;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.success-icon {
  width: 60px;
  height: 60px;
  background-color: #67c23a;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
  margin: 0 auto 20px;
}

.success-content h2 {
  color: #303133;
  margin-bottom: 10px;
}

.success-content p {
  color: #606266;
  margin-bottom: 25px;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.primary-btn {
  padding: 10px 20px;
  background-color: #409EFF;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background-color: #e9ebef;
}

/* 项目步骤样式 */
.project-steps-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 5px;
  border-left: 4px solid #409EFF;
}

.steps-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.step-item {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #ddd;
}

.step-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.step-number {
  width: 30px;
  height: 30px;
  background-color: #409EFF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-content h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.step-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
