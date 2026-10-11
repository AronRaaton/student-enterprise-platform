<template>
  <div class="admin-container">
    <!-- 左侧导航菜单 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h1>管理后台</h1>
        <div class="admin-info">
          <div class="admin-avatar">A</div>
          <span class="admin-id">管理员: {{ adminId }}</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div
          class="nav-item"
          :class="{ active: currentView === 'users' }"
          @click="currentView='users'"
        >
          <i class="nav-icon users-icon">👥</i>
          <span>用户管理</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentView === 'tasks' }"
          @click="currentView='tasks'"
        >
          <i class="nav-icon tasks-icon">📋</i>
          <span>任务监控</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentView === 'announcements' }"
          @click="currentView='announcements'"
        >
          <i class="nav-icon announcements-icon">📢</i>
          <span>公告监控</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentView === 'verifications' }"
          @click="currentView='verifications'"
        >
          <i class="nav-icon verifications-icon">✅</i>
          <span>企业验证</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <button @click="goToLoginPage" class="btn-back-login">
          <i class="logout-icon">🚪</i>
          返回登录
        </button>
      </div>
    </aside>

    <!-- 右侧内容区域 -->
    <main class="content-area">

    <!-- 用户管理 -->
    <div v-show="currentView === 'users'" class="user-management">
      <!-- 学生列表 -->
      <div class="user-section">
        <h2>学生用户列表</h2>
        <div class="search-bar">
          <input v-model="studentSearch" placeholder="搜索学生..." @input="searchStudents">
        </div>
        <table class="wide-table">
          <thead>
          <tr>
            <th>学号</th>
            <th>姓名</th>
            <th>学校</th>
            <th>专业</th>
            <th>入学年份</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th>完成任务</th>
            <th>关注企业</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
            <tr v-for="student in filteredStudents" :key="student.id">
              <td>{{ student.studentNumber }}</td>
              <td>{{ student.name }}</td>
              <td>{{ student.school || '未设置' }}</td>
              <td>{{ student.major || '未设置' }}</td>
              <td>{{ student.enrollmentYear || '未设置' }}</td>
              <td>{{ student.email }}</td>
              <td>{{ formatDate(student.registerDate || student.createdTime || new Date()) }}</td>
              <td>{{ student.completedTasks || 0 }}</td>
              <td>{{ student.followedEnterprises || 0 }}</td>
              <td class="action-buttons">
                <button class="btn-view" @click="viewStudentDetails(student)">查看详情</button>
                <button class="btn-modify" @click="editStudent(student)">修改</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="pagination">
          <button :disabled="studentPage === 1" @click="changePage('student', studentPage - 1)">上一页</button>
          <span>{{ studentPage }} / {{ studentTotalPages }}</span>
          <button :disabled="studentPage === studentTotalPages" @click="changePage('student', studentPage + 1)">下一页</button>
        </div>
      </div>

      <!-- 企业列表 -->
      <div class="user-section">
        <h2>企业用户列表</h2>
        <table class="wide-table">
          <thead>
          <tr>
            <th>企业ID</th>
            <th>企业名称</th>
            <th>所在地</th>
            <th>评分</th>
            <th>类型</th>
            <th>成立年份</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="e in enterprises" :key="e.id">
            <td>{{ e.enterpriseId }}</td>
            <td>{{ e.name }}</td>
            <td>{{ e.location || '未设置' }}</td>
            <td>{{ typeof e.avgRating === 'number' ? e.avgRating.toFixed(1) : '0.0' }}</td>
            <td>{{ e.type || '未设置' }}</td>
            <td>{{ e.establishmentYear || e.foundingYear || '未设置' }}</td>
            <td class="action-buttons">
              <button class="btn-view" @click="viewEnterpriseDetails(e)">查看详情</button>
              <button class="btn-modify" @click="editEnterprise(e)">修改</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 任务监控 -->
    <div v-show="currentView === 'tasks'" class="task-monitor">
      <h2>任务监控</h2>
      <div class="search-bar">
        <input v-model="taskSearch" placeholder="搜索任务..." @input="searchTasks">
      </div>
      <div v-if="filteredTasks.length === 0" class="no-data">
        <p>暂无任务数据</p>
      </div>
      <div v-else class="task-list">
        <div v-for="task in filteredTasks" :key="task.taskId" class="task-card">
          <h3>{{ task.summary }}</h3>
          <p><strong>发布企业：</strong>{{ task.enterprise ? task.enterprise.name : '未知企业' }}</p>
          <p>{{ task.content }}</p>
          <p><strong>技术栈：</strong>{{ task.techStack }}</p>
          <p><strong>创建时间：</strong>{{ formatDate(task.createTime) }}</p>
          <button class="btn-primary" @click="viewTaskDetails(task.taskId)">查看任务详情</button>
        </div>
        <div class="pagination">
          <button :disabled="taskPage === 1" @click="changePage('task', taskPage - 1)">上一页</button>
          <span>{{ taskPage }} / {{ taskTotalPages }}</span>
          <button :disabled="taskPage === taskTotalPages" @click="changePage('task', taskPage + 1)">下一页</button>
        </div>
      </div>
    </div>

    <!-- 公告监控 -->
    <div v-show="currentView === 'announcements'" class="announcement-monitor">
      <div class="announcement-header">
        <h2>公告监控</h2>
        <div class="announcement-tabs">
          <button
            :class="{ 'active-tab': announcementTab === 'published' }"
            @click="announcementTab = 'published'"
          >
            已发布公告
          </button>
          <button
            :class="{ 'active-tab': announcementTab === 'pending' }"
            @click="announcementTab = 'pending'"
          >
            待审核公告
            <span v-if="pendingNotices.length > 0" class="badge">{{ pendingNotices.length }}</span>
          </button>
        </div>

        <div class="search-bar" v-if="announcementTab === 'published'">
          <input v-model="noticeSearch" placeholder="搜索公告..." @input="searchNotices">
        </div>
      </div>

      <!-- 已发布公告 -->
      <div v-if="announcementTab === 'published'" class="published-announcements">
        <div v-if="filteredNotices.length === 0" class="no-data">
          <div class="empty-state">
            <i class="empty-icon">📢</i>
            <p>暂无已发布公告</p>
          </div>
        </div>
        <div v-else class="notice-list">
          <div v-for="notice in filteredNotices" :key="notice.noticeId" class="notice-card">
            <div class="notice-header">
              <h3>{{ notice.title }}</h3>
              <span class="notice-status published">已发布</span>
            </div>
            <p class="notice-content">{{ notice.content.substring(0, 100) }}{{ notice.content.length > 100 ? '...' : '' }}</p>
            <p class="notice-info">
              <span><i class="time-icon">📅</i> {{ formatDate(notice.createdTime) }}</span>
              <span><i class="enterprise-icon">🏢</i> {{ notice.enterprise ? notice.enterprise.name : '未知企业' }}</span>
            </p>
            <button class="btn-primary" @click="viewNoticeDetails(notice.noticeId)">
              <i class="view-icon">🔍</i> 查看详情
            </button>
          </div>
          <div class="pagination">
            <button :disabled="publishedPage === 1" @click="changePage('published', publishedPage - 1)">上一页</button>
            <span>{{ publishedPage }} / {{ publishedTotalPages }}</span>
            <button :disabled="publishedPage === publishedTotalPages" @click="changePage('published', publishedPage + 1)">下一页</button>
          </div>
        </div>
      </div>

      <!-- 待审核公告 -->
      <div v-if="announcementTab === 'pending'" class="pending-notices">
        <div v-if="pendingNotices.length === 0" class="no-data">
          <div class="empty-state">
            <i class="empty-icon">✅</i>
            <p>没有待审核的公告</p>
          </div>
        </div>
        <div v-else class="notice-list">
          <div v-for="notice in pendingNotices" :key="notice.noticeId || notice.id" class="notice-card pending">
            <div class="notice-header">
              <h3>{{ notice.title }}</h3>
              <span class="notice-status pending">待审核</span>
            </div>
            <p class="notice-content">{{ notice.content ? notice.content.substring(0, 100) + (notice.content.length > 100 ? '...' : '') : '无内容' }}</p>
            <p class="notice-info">
              <span><i class="time-icon">📅</i> {{ formatDate(notice.createdTime) }}</span>
              <span><i class="enterprise-icon">🏢</i> {{ notice.enterprise ? notice.enterprise.name : '未知企业' }}</span>
            </p>
            <button class="btn-review" @click="reviewNotice(notice)">
              <i class="review-icon">⚙️</i> 审核公告
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 审核公告弹窗 -->
    <div v-if="showReviewModal" class="modal-overlay">
      <div class="modal-content">
        <h2>审核公告</h2>
        <div class="notice-preview">
          <h3>{{ currentNotice.title }}</h3>
          <p>{{ currentNotice.content }}</p>
        </div>
        <div class="review-form">
          <div class="form-group review-options">
            <label class="radio-container">
              <input type="radio" v-model="reviewDecision" value="approve">
              <span class="radio-label">通过</span>
            </label>
            <label class="radio-container">
              <input type="radio" v-model="reviewDecision" value="reject">
              <span class="radio-label">拒绝</span>
            </label>
          </div>
          <div class="form-group" v-if="reviewDecision === 'reject'">
            <label>审核意见</label>
            <textarea v-model="reviewFeedback" placeholder="请输入拒绝原因..."></textarea>
          </div>
          <div class="button-group">
            <button class="btn-primary" @click="submitReview">提交审核</button>
            <button class="btn-secondary" @click="closeReviewModal">取消</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 企业验证管理 -->
    <div v-show="currentView === 'verifications'" class="enterprise-verifications">
      <h2>企业验证申请管理</h2>

      <div v-if="loadingVerifications" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="pendingVerifications.length === 0" class="empty-state">
        <i class="empty-icon">✅</i>
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

    <!-- 修改学生或企业信息的模态框 -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h2>{{ modalType === 'student' ? '修改学生信息' : '修改企业信息' }}</h2>
        <form @submit.prevent="submitForm">
          <div class="form-group" v-if="modalType === 'student'">
            <label for="studentNumber">学号</label>
            <input v-model="currentItem.studentNumber" type="text" id="studentNumber" required />
          </div>
          <div class="form-group">
            <label for="name">{{ modalType === 'student' ? '姓名' : '企业名称' }}</label>
            <input v-model="currentItem.name" type="text" id="name" required />
          </div>
          <div class="form-group">
            <label for="email">{{ modalType === 'student' ? '学生邮箱' : '企业邮箱' }}</label>
            <input v-model="currentItem.email" type="email" id="email" required />
          </div>

          <!-- 学生特有字段 -->
          <template v-if="modalType === 'student'">
            <div class="form-group">
              <label for="school">学校</label>
              <input v-model="currentItem.school" type="text" id="school" />
            </div>
            <div class="form-group">
              <label for="major">专业</label>
              <input v-model="currentItem.major" type="text" id="major" />
            </div>
            <div class="form-group">
              <label for="enrollmentYear">入学年份</label>
              <input v-model="currentItem.enrollmentYear" type="text" id="enrollmentYear" placeholder="例如：2020" />
            </div>
          </template>

          <!-- 企业特有字段 -->
          <template v-if="modalType === 'enterprise'">
            <div class="form-group">
              <label for="type">企业类型</label>
              <input v-model="currentItem.type" type="text" id="type" />
            </div>
            <div class="form-group">
              <label for="location">所在地</label>
              <input v-model="currentItem.location" type="text" id="location" />
            </div>
            <div class="form-group">
              <label for="address">详细地址</label>
              <input v-model="currentItem.address" type="text" id="address" />
            </div>
            <div class="form-group">
              <label for="foundingYear">成立年份</label>
              <input v-model="currentItem.foundingYear" type="number" id="foundingYear" />
            </div>
            <div class="form-group">
              <label for="employeeCount">员工数量</label>
              <input v-model="currentItem.employeeCount" type="number" id="employeeCount" />
            </div>
            <div class="form-group description-group">
              <label for="description">企业描述</label>
              <textarea v-model="currentItem.description" id="description" rows="8" class="large-textarea" placeholder="请输入企业描述..."></textarea>
            </div>
          </template>

          <!-- 学生特有字段 -->
          <template v-if="modalType === 'student'">
            <div class="form-group">
              <label for="school">学校</label>
              <input v-model="currentItem.school" type="text" id="school" />
            </div>
            <div class="form-group">
              <label for="major">专业</label>
              <input v-model="currentItem.major" type="text" id="major" />
            </div>
            <div class="form-group">
              <label for="enrollmentYear">入学年份</label>
              <input v-model="currentItem.enrollmentYear" type="number" id="enrollmentYear" />
            </div>
          </template>
          <div class="form-group">
            <button class="btn-primary" type="submit">提交</button>
            <button class="btn-secondary" type="button" @click="closeModal">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 企业详情模态框 -->
    <div v-if="showEnterpriseDetailsModal" class="modal-overlay">
      <div class="modal-content enterprise-details-modal">
        <h2>企业详细信息</h2>
        <div class="enterprise-details" v-if="currentEnterpriseDetails">
          <div class="detail-row">
            <span class="detail-label">企业ID:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.enterpriseId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">企业名称:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.name }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">邮箱:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.email }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">企业类型:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.type || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">所在地:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.location || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">详细地址:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.address || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">成立年份:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.establishmentYear || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">员工数量:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.employeeCount || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">评分:</span>
            <span class="detail-value">{{ currentEnterpriseDetails.avgRating ? currentEnterpriseDetails.avgRating.toFixed(1) : '0.0' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">注册时间:</span>
            <span class="detail-value">{{ formatDate(currentEnterpriseDetails.registerDate) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">发布任务数:</span>
            <span class="detail-value" v-if="currentEnterpriseDetails.publishedTasks !== undefined">
              {{ currentEnterpriseDetails.publishedTasks }}
            </span>
            <span class="detail-value" v-else>
              <span class="loading-text">加载中...</span>
            </span>
          </div>
          <div class="detail-row full-width">
            <span class="detail-label">企业描述:</span>
            <span class="detail-value description">{{ currentEnterpriseDetails.description || '暂无描述' }}</span>
          </div>
        </div>
        <div class="form-group">
          <button class="btn-primary" @click="editEnterprise(currentEnterpriseDetails)">编辑信息</button>
          <button class="btn-secondary" @click="closeEnterpriseDetailsModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 学生详情模态框 -->
    <div v-if="showStudentDetailsModal" class="modal-overlay">
      <div class="modal-content student-details-modal">
        <h2>学生详细信息</h2>
        <div class="student-details" v-if="currentStudentDetails">
          <div class="detail-row">
            <span class="detail-label">学生ID:</span>
            <span class="detail-value">{{ currentStudentDetails.studentId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">学号:</span>
            <span class="detail-value">{{ currentStudentDetails.studentNumber }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">姓名:</span>
            <span class="detail-value">{{ currentStudentDetails.name }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">邮箱:</span>
            <span class="detail-value">{{ currentStudentDetails.email }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">学校:</span>
            <span class="detail-value">{{ currentStudentDetails.school || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">专业:</span>
            <span class="detail-value">{{ currentStudentDetails.major || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">入学年份:</span>
            <span class="detail-value">{{ currentStudentDetails.enrollmentYear || '未设置' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">注册时间:</span>
            <span class="detail-value">{{ formatDate(currentStudentDetails.registerDate) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">完成任务数:</span>
            <span class="detail-value" v-if="currentStudentDetails.completedTasks !== undefined">
              {{ currentStudentDetails.completedTasks }}
            </span>
            <span class="detail-value" v-else>
              <span class="loading-text">加载中...</span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">关注企业数:</span>
            <span class="detail-value" v-if="currentStudentDetails.followedEnterprises !== undefined">
              {{ currentStudentDetails.followedEnterprises }}
            </span>
            <span class="detail-value" v-else>
              <span class="loading-text">加载中...</span>
            </span>
          </div>
        </div>
        <div class="form-group">
          <button class="btn-primary" @click="editStudent(currentStudentDetails)">编辑信息</button>
          <button class="btn-secondary" @click="closeStudentDetailsModal">关闭</button>
        </div>
      </div>
    </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      currentView: 'users',
      adminId: null, // 将初始值设为 null

      // 学生相关
      students: [],
      studentSearch: '',
      studentPage: 1,
      studentTotalPages: 1,

      // 企业相关
      enterprises: [],
      enterprisePage: 1,
      enterpriseTotalPages: 1,

      // 任务相关
      tasks: [],
      taskSearch: '',
      taskPage: 1,
      taskTotalPages: 1,

      // 公告相关
      notices: [],
      pendingNotices: [],
      noticeSearch: '',
      publishedPage: 1,
      publishedTotalPages: 1,
      announcementTab: 'published', // 公告页面的当前标签，默认显示已发布公告

      // 审核相关
      showReviewModal: false,
      currentNotice: null,
      reviewDecision: 'approve', // 默认选择通过
      reviewFeedback: '',

      // 分页大小
      pageSize: 10,
      showModal: false,
      modalType: '',
      currentItem: {},

      // 企业详情模态框
      showEnterpriseDetailsModal: false,
      currentEnterpriseDetails: null,

      // 学生详情模态框
      showStudentDetailsModal: false,
      currentStudentDetails: null,

      // 企业验证相关
      pendingVerifications: [],
      loadingVerifications: true,
    };
  },

  computed: {
    filteredStudents() {
      if (!this.studentSearch) return this.students;
      const search = this.studentSearch.toLowerCase();
      return this.students.filter(student =>
        student.name.toLowerCase().includes(search) ||
        student.studentNumber.toLowerCase().includes(search)
      );
    },

    filteredTasks() {
      if (!this.taskSearch) return this.tasks;
      const search = this.taskSearch.toLowerCase();
      return this.tasks.filter(task =>
        task.summary.toLowerCase().includes(search) ||
        task.content.toLowerCase().includes(search) ||
        (task.enterprise && task.enterprise.name.toLowerCase().includes(search))
      );
    },

    filteredNotices() {
      if (!this.noticeSearch) return this.notices;
      const search = this.noticeSearch.toLowerCase();
      return this.notices.filter(notice =>
        notice.title.toLowerCase().includes(search) ||
        notice.content.toLowerCase().includes(search) ||
        (notice.enterprise && notice.enterprise.name.toLowerCase().includes(search))
      );
    }
  },

  created() {
    // 在组件创建时获取管理员信息
    this.getAdminInfo();
    this.fetchAllData();
  },

  methods: {
    async getAdminInfo() {
      try {
        const adminId = localStorage.getItem('adminId');
        if (adminId) {
          // 直接使用本地存储的adminId
          this.adminId = adminId;
        }
      } catch (error) {
        console.error('获取管理员信息失败:', error);
      }
    },

    async fetchAllData() {
      this.initializeData(); // 添加初始化
      try {
        await Promise.all([
          this.fetchStudents(),
          this.fetchEnterprises(),
          this.fetchTasks(),
          this.fetchNotices(),
          this.fetchPendingNotices(),
          this.fetchPendingVerifications()
        ]);
      } catch (error) {
        console.error('获取数据失败:', error);
      }
    },

    async fetchStudents() {
      try {
        const response = await axios.get(`http://localhost:8080/api/admin/students`, {
          params: {
            page: this.studentPage - 1,
            size: this.pageSize
          }
        });

        // 获取学生列表
        const students = response.data.content.map(student => ({
          ...student,
          id: student.id || student.studentId, // 确保ID字段存在
          registerDate: student.registerDate || new Date().toISOString(), // 确保注册时间字段存在
          completedTasks: student.completedTasks || 0, // 确保完成任务字段存在
          followedEnterprises: 0, // 初始化关注企业数量
          school: student.school || '', // 学校
          major: student.major || '', // 专业
          enrollmentYear: student.enrollmentYear || '' // 入学年份
        }));

        // 对每个学生获取关注企业数量
        for (const student of students) {
          try {
            const countResponse = await axios.get(`http://localhost:8080/api/admin/students/${student.studentId}/followed-enterprises-count`);
            student.followedEnterprises = countResponse.data || 0;
          } catch (countError) {
            console.error(`获取学生 ${student.studentId} 关注企业数量失败:`, countError);
          }
        }

        this.students = students;
        this.studentTotalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取学生列表失败:', error);
        this.students = [];
      }
    },

    async fetchEnterprises() {
      try {
        const response = await axios.get(`http://localhost:8080/api/admin/enterprises`, {
          params: {
            page: this.enterprisePage - 1,
            size: this.pageSize
          }
        });
        // 确保每个企业对象都有正确的字段
        this.enterprises = response.data.content.map(enterprise => {
          // 获取企业任务数量
          axios.get(`http://localhost:8080/api/tasks/enterprise/${enterprise.enterpriseId}/count`)
            .then(countResponse => {
              enterprise.publishedTasks = countResponse.data || 0;
            })
            .catch(error => {
              console.error(`获取企业 ${enterprise.enterpriseId} 任务数量失败:`, error);
              enterprise.publishedTasks = 0;
            });

          return {
            ...enterprise,
            id: enterprise.id || enterprise.enterpriseId, // 确保ID字段存在
            publishedTasks: enterprise.publishedTasks || 0, // 确保发布任务数量字段存在
            avgRating: enterprise.rating || enterprise.avgRating || 0, // 尝试多个可能的评分字段名称
            registerDate: enterprise.registerDate || enterprise.createdTime || new Date().toISOString(), // 确保注册时间字段存在
            location: enterprise.location || '', // 所在地
            type: enterprise.type || '', // 企业类型
            establishmentYear: enterprise.establishmentYear || enterprise.foundingYear || '' // 尝试多个可能的成立年份字段名称
          };
        });
        this.enterpriseTotalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取企业列表失败:', error);
        this.enterprises = [];
      }
    },

    async fetchTasks() {
      try {
        const response = await axios.get(`http://localhost:8080/api/admin/tasks`, {
          params: {
            page: this.taskPage - 1,
            size: this.pageSize
          }
        });
        // 直接使用返回的任务数据
        console.log('Tasks data:', response.data.content); // 调试日志
        this.tasks = response.data.content;
        this.taskTotalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取任务列表失败:', error);
        this.tasks = [];
      }
    },

    async fetchNotices() {
      try {
        const response = await axios.get(`http://localhost:8080/api/admin/notices`, {
          params: {
            page: this.publishedPage - 1,
            size: this.pageSize
          }
        });
        // 过滤出已发布的公告（状态为APPROVED）
        console.log('Notices data:', response.data.content); // 调试日志
        this.notices = response.data.content.filter(notice => notice.status === 'APPROVED');
        this.publishedTotalPages = response.data.totalPages;
      } catch (error) {
        console.error('获取公告列表失败:', error);
      }
    },

    async fetchPendingNotices() {
      try {
        const response = await axios.get(`http://localhost:8080/api/admin/notices/pending`);
        console.log('Pending notices data:', response.data); // 调试日志

        // 如果数据是数组，直接使用，否则将其包装为数组
        const notices = Array.isArray(response.data) ? response.data : [response.data];

        // 处理可能的空值
        if (notices && notices.length > 0) {
          // 确保只显示状态为PENDING的公告
          this.pendingNotices = notices;
          console.log('Filtered pending notices:', this.pendingNotices);
        } else {
          this.pendingNotices = [];
        }

        console.log('Filtered pending notices:', this.pendingNotices); // 调试日志
      } catch (error) {
        console.error('获取待审核公告失败:', error);
        this.pendingNotices = [];
      }
    },

    async submitReview() {
      try {
        // 调试日志
        console.log('Current notice:', this.currentNotice);
        console.log('Review decision:', this.reviewDecision);

        // 确保使用正确的ID，可能是noticeId而不是id
        const noticeId = this.currentNotice.noticeId || this.currentNotice.id;

        if (!noticeId) {
          console.error('公告 ID 不存在');
          alert('公告 ID 不存在，无法提交审核');
          return;
        }

        console.log(`提交审核请求到: http://localhost:8080/api/admin/notices/${noticeId}/review`);

        const response = await axios.post(`http://localhost:8080/api/admin/notices/${noticeId}/review`, {
          approved: this.reviewDecision === 'approve',
          feedback: this.reviewDecision === 'reject' ? this.reviewFeedback : ''
        });

        console.log('审核响应:', response.data);

        alert(this.reviewDecision === 'approve' ? '公告审核通过成功!' : '公告已拒绝');
        this.closeReviewModal();
        await this.fetchAllData();
      } catch (error) {
        console.error('提交审核失败:', error);
        alert('提交审核失败: ' + (error.response?.data?.message || error.message));
      }
    },

    reviewNotice(notice) {
      console.log('Reviewing notice:', notice); // 调试日志
      this.currentNotice = notice;
      this.reviewDecision = 'approve'; // 默认选中通过
      this.reviewFeedback = '';
      this.showReviewModal = true;
    },

    closeReviewModal() {
      this.showReviewModal = false;
      this.currentNotice = null;
      this.reviewDecision = 'approve';
      this.reviewFeedback = '';
    },

    changePage(type, page) {
      switch(type) {
        case 'student':
          this.studentPage = page;
          this.fetchStudents();
          break;
        case 'task':
          this.taskPage = page;
          this.fetchTasks();
          break;
        case 'published':
          this.publishedPage = page;
          this.fetchNotices();
          break;
      }
    },

    formatDate(date) {
      return new Date(date).toLocaleString();
    },

    viewTaskDetails(taskId) {
      this.$router.push({
        name: 'TaskDetail',
        params: { taskId: taskId.toString() }
      });
    },

    viewNoticeDetails(noticeId) {
      this.$router.push({
        name: 'notice-detail',
        params: { noticeId: noticeId.toString() }
      });
    },
    editStudent(student) {
      this.modalType = 'student';
      // 确保复制所有需要的字段，包括id
      this.currentItem = {
        id: student.id,
        studentId: student.studentId, // 如果后端使用studentId
        studentNumber: student.studentNumber,
        name: student.name,
        email: student.email,
        school: student.school || '',
        major: student.major || '',
        enrollmentYear: student.enrollmentYear || ''
      };
      this.showModal = true;
      console.log('Current student item:', this.currentItem); // 调试用
    },
    editEnterprise(enterprise) {
      this.modalType = 'enterprise';
      // 确保复制所有需要的字段，包括id
      this.currentItem = {
        id: enterprise.id,
        enterpriseId: enterprise.enterpriseId, // 如果后端使用enterpriseId
        name: enterprise.name,
        email: enterprise.email,
        type: enterprise.type,
        location: enterprise.location,
        address: enterprise.address,
        foundingYear: enterprise.establishmentYear,
        employeeCount: enterprise.employeeCount,
        description: enterprise.description
      };
      this.showModal = true;
      console.log('Current enterprise item:', this.currentItem); // 调试用
    },

    // 查看企业详情
    viewEnterpriseDetails(enterprise) {
      this.currentEnterpriseDetails = { ...enterprise };
      this.showEnterpriseDetailsModal = true;

      // 获取企业发布的任务数量
      axios.get(`http://localhost:8080/api/tasks/enterprise/${enterprise.enterpriseId}/count`)
        .then(response => {
          // 使用Vue.set确保响应式更新
          this.$set(this.currentEnterpriseDetails, 'publishedTasks', response.data || 0);
        })
        .catch(error => {
          console.error(`获取企业 ${enterprise.enterpriseId} 任务数量失败:`, error);
          this.$set(this.currentEnterpriseDetails, 'publishedTasks', 0);
        });
    },

    // 查看学生详情
    viewStudentDetails(student) {
      this.currentStudentDetails = { ...student };
      this.showStudentDetailsModal = true;

      // 获取学生完成的任务数量
      axios.get(`http://localhost:8080/api/students/${student.studentId}/completed-tasks/count`)
        .then(response => {
          // 使用Vue.set确保响应式更新
          this.$set(this.currentStudentDetails, 'completedTasks', response.data || 0);
        })
        .catch(error => {
          console.error(`获取学生 ${student.studentId} 完成任务数量失败:`, error);
          // 使用已有的数据或设置为0
          this.$set(this.currentStudentDetails, 'completedTasks', student.completedTasks || 0);
        });

      // 获取学生关注的企业数量
      axios.get(`http://localhost:8080/api/students/${student.studentId}/followed-enterprises/count`)
        .then(response => {
          this.$set(this.currentStudentDetails, 'followedEnterprises', response.data || 0);
        })
        .catch(error => {
          console.error(`获取学生 ${student.studentId} 关注企业数量失败:`, error);
          this.$set(this.currentStudentDetails, 'followedEnterprises', student.followedEnterprises || 0);
        });
    },

    // 关闭企业详情模态框
    closeEnterpriseDetailsModal() {
      this.showEnterpriseDetailsModal = false;
      this.currentEnterpriseDetails = null;
    },

    // 关闭学生详情模态框
    closeStudentDetailsModal() {
      this.showStudentDetailsModal = false;
      this.currentStudentDetails = null;
    },
    async submitForm() {
      try {
        let endpoint;
        let updateData;

        if (this.modalType === 'student') {
          const studentId = this.currentItem.id || this.currentItem.studentId;
          if (!studentId) {
            throw new Error('Student ID is missing');
          }
          endpoint = `http://localhost:8080/api/admin/students/${studentId}`;
          updateData = {
            studentNumber: this.currentItem.studentNumber,
            name: this.currentItem.name,
            email: this.currentItem.email,
            school: this.currentItem.school,
            major: this.currentItem.major,
            enrollmentYear: this.currentItem.enrollmentYear
          };
        } else {
          const enterpriseId = this.currentItem.id || this.currentItem.enterpriseId;
          if (!enterpriseId) {
            throw new Error('Enterprise ID is missing');
          }
          endpoint = `http://localhost:8080/api/admin/enterprises/${enterpriseId}`;
          updateData = {
            name: this.currentItem.name,
            email: this.currentItem.email
          };
        }

        console.log('Submitting to endpoint:', endpoint); // 调试用
        console.log('Update data:', updateData); // 调试用

        const response = await axios.put(endpoint, updateData);

        if (response.data) {
          // 提示成功
          alert('修改成功！');

          // 刷新数据
          if (this.modalType === 'student') {
            await this.fetchStudents();
          } else {
            await this.fetchEnterprises();
          }

          // 关闭模态框
          this.closeModal();
        }
      } catch (error) {
        console.error('提交表单失败:', error);
        alert(error.message || '修改失败，请稍后重试');
      }
    },
    closeModal() {
      this.showModal = false;
      this.currentItem = {}; // 清空当前编辑项
      this.modalType = '';
    },
    goToLoginPage() {
      // 返回到登录页面的逻辑
      this.$router.push('/login'); // 假设你使用的是Vue Router
    },

    // 获取待审核的企业验证申请
    async fetchPendingVerifications() {
      this.loadingVerifications = true;
      try {
        const response = await axios.get('http://localhost:8080/api/admin/enterprise-verifications/pending');
        this.pendingVerifications = response.data;
        console.log('待审核企业验证申请:', this.pendingVerifications);
      } catch (error) {
        console.error('获取待审核企业验证申请失败:', error);
        this.pendingVerifications = [];
      } finally {
        this.loadingVerifications = false;
      }
    },

    // 获取营业执照URL
    getBusinessLicenseUrl(filename) {
      if (!filename) return '#';
      return `http://localhost:8080/uploads/${filename}`;
    },

    // 格式化日期
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

    // 批准企业验证申请
    async approveVerification(verificationId) {
      if (!confirm('确定要批准此企业验证申请吗？')) return;

      try {
        const adminId = localStorage.getItem('userId') || this.adminId; // 使用存储的管理员ID

        const response = await axios.post(`http://localhost:8080/api/admin/enterprise-verifications/${verificationId}/approve?adminId=${adminId}`);
        alert('企业验证申请已批准');
        await this.fetchPendingVerifications();
      } catch (error) {
        console.error('批准企业验证申请失败:', error);
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'));
      }
    },

    // 拒绝企业验证申请
    async rejectVerification(verificationId) {
      if (!confirm('确定要拒绝此企业验证申请吗？这将删除企业信息和验证信息。')) return;

      try {
        const adminId = localStorage.getItem('userId') || this.adminId; // 使用存储的管理员ID

        const response = await axios.post(`http://localhost:8080/api/admin/enterprise-verifications/${verificationId}/reject?adminId=${adminId}`);
        alert('企业验证申请已拒绝');
        await this.fetchPendingVerifications();
      } catch (error) {
        console.error('拒绝企业验证申请失败:', error);
        alert('操作失败: ' + (error.response?.data?.message || '未知错误'));
      }
    },
    // 添加数据初始化方法
    initializeData() {
      this.students = [];
      this.enterprises = [];
      this.tasks = [];
      this.notices = [];
      this.pendingNotices = [];
    }
  }
};
</script>

<style scoped>
.admin-container {
  display: flex;
  height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  margin: 0;
  padding: 0;
}

/* 左侧导航栏样式 */
.sidebar {
  width: 220px;
  background-color: #5d7fa3;
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h1 {
  font-size: 1.5rem;
  margin: 0 0 15px 0;
  color: #ffffff;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  background-color: #3498db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.admin-id {
  font-size: 14px;
  color: #e0e6ed;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
}

.nav-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.nav-item.active {
  background-color: rgba(41, 128, 185, 0.4);
  border-left-color: #2980b9;
}

.nav-icon {
  font-size: 1.2rem;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-back-login {
  width: 100%;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background-color 0.3s;
}

.btn-back-login:hover {
  background-color: rgba(255, 255, 255, 0.25);
}

/* 右侧内容区域样式 */
.content-area {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.wide-table {
  width: 100%;
  border-collapse: collapse;
  margin: 1rem 0;
  table-layout: auto;
}

.wide-table th,
.wide-table td {
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
  text-align: left;
  white-space: nowrap;
}

.wide-table th {
  background-color: #fafafa;
  font-weight: 600;
}

.user-section {
  margin-bottom: 3rem;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-section h2 {
  color: #333;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #eee;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-primary {
  background-color: #4CAF50;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.btn-secondary {
  background-color: #f44336;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #45a049;
}

.btn-secondary:hover {
  background-color: #da190b;
}

.search-bar {
  margin-bottom: 1rem;
}

.search-bar input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.notice-card {
  background: white;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.notice-info {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 0.9rem;
  margin: 0.5rem 0;
}

.btn-review {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 80%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.enterprise-details-modal, .student-details-modal {
  max-width: 700px;
}

.student-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.enterprise-details {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.detail-row {
  flex: 0 0 calc(50% - 15px);
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.detail-row.full-width {
  flex: 0 0 100%;
}

.detail-label {
  font-weight: bold;
  color: #666;
  margin-bottom: 5px;
  font-size: 0.9rem;
}

.detail-value {
  color: #333;
}

.detail-value.description {
  white-space: pre-line;
  line-height: 1.5;
  margin-top: 5px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-view, .btn-modify {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
  min-width: 80px;
  text-align: center;
}

.btn-view:hover, .btn-modify:hover {
  background-color: #40a9ff;
}

.large-textarea {
  min-height: 150px;
  width: 100%;
  font-family: inherit;
  padding: 10px;
  line-height: 1.5;
  resize: vertical;
}

.description-group {
  margin-bottom: 20px;
}

.loading-text {
  font-style: italic;
  color: #999;
}

.review-form {
  margin-top: 1rem;
}

.button-group {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.no-data {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1rem;
}

.task-card {
  background: white;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.review-options {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.radio-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 0;
}

.radio-container input[type="radio"] {
  margin-right: 8px;
}

.radio-label {
  font-size: 16px;
}

/* 公告监控样式 */
.announcement-monitor {
  margin-top: 1rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.announcement-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.announcement-header h2 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 1.5rem;
}

.announcement-tabs {
  display: flex;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.announcement-tabs button {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  color: #7f8c8d;
  position: relative;
  transition: all 0.3s;
}

.announcement-tabs button:hover {
  color: #3498db;
}

.announcement-tabs button.active-tab {
  color: #3498db;
  border-bottom-color: #3498db;
  font-weight: 500;
}

.badge {
  position: absolute;
  top: 5px;
  right: 5px;
  background-color: #e74c3c;
  color: white;
  border-radius: 50%;
  min-width: 18px;
  height: 18px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notice-status {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.notice-status.published {
  background-color: #e1f5fe;
  color: #0288d1;
}

.notice-status.pending {
  background-color: #fff8e1;
  color: #ffa000;
}

/* 企业验证管理样式 */
.enterprise-verifications {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.enterprise-verifications h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 1.5rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #5d7fa3;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 40px;
  background-color: #f9f9f9;
  border-radius: 8px;
  color: #666;
}

.empty-icon {
  font-size: 2rem;
  margin-bottom: 10px;
  display: block;
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

.notice-content {
  margin-bottom: 15px;
  color: #555;
  line-height: 1.5;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #95a5a6;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.btn-approve {
  background-color: #2ecc71;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background-color 0.3s;
}

.btn-approve:hover {
  background-color: #27ae60;
}

.btn-reject {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background-color 0.3s;
}

.btn-reject:hover {
  background-color: #c0392b;
}

.notice-actions {
  display: flex;
  gap: 10px;
}
</style>
