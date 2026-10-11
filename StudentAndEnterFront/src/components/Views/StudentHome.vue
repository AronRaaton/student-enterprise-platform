<template>
  <div class="student-home-container">
    <!-- 顶部导航栏 -->
    <div class="custom-header">
      <div class="header-content">
        <div class="logo-container">
          <h2 class="site-title">学生企业交流平台</h2>
        </div>
        <div class="user-info">
          <div class="message-icon" role="button" tabindex="0" aria-label="查看消息" @keydown.enter="viewMessages" @keydown.space.prevent="viewMessages" @click="viewMessages">
            <span class="icon">💬</span>
            <span v-if="hasUnreadMessages" class="message-badge">{{ unreadMessages }}</span>
          </div>
          <div class="user-dropdown" @click="viewProfile" style="cursor: pointer;">
            <div class="user-avatar">{{ user.name ? user.name.charAt(0) : 'U' }}</div>
            <span class="welcome-text">欢迎，{{ user.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 左侧导航菜单 -->
      <div class="sidebar-menu">
        <div class="menu-item active">
          <i class="icon icon-home"></i>
          <span>主页</span>
        </div>
        <div class="nav-group-label">学习与实践</div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewAllAssignedTasks">
          <i class="icon icon-task"></i>
          <span>我的任务</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewAllNotices">
          <i class="icon icon-announcement"></i>
          <span>公告中心</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewJobs">
          <i class="icon icon-job"></i>
          <span>招聘信息</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewProjects">
          <i class="icon icon-project"></i>
          <span>项目合作</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewChallenges">
          <i class="icon icon-challenge"></i>
          <span>技术挑战</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewCompetitions">
          <i class="icon icon-competition"></i>
          <span>企业竞赛</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewEvents">
          <i class="icon icon-event"></i>
          <span>企业活动</span>
        </div>
        <div class="nav-group-label">交流与资源</div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewForum">
          <i class="icon icon-forum"></i>
          <span>交流论坛</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="viewTrainingMaterials">
          <i class="icon icon-training"></i>
          <span>培训资料</span>
        </div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="main-content">
        <!-- 欢迎卡片 -->
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text-container">
              <h1>欢迎回来，{{ user.name }}</h1>
              <p>今天是 {{ currentDate }}，祝您学习愉快！</p>
              <div class="stats-container">
                <div class="stat-item">
                  <div class="stat-icon">🎯</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ user.completedTasks || 0 }}</div>
                    <div class="stat-label">已完成任务</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">💼</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ participatedProjects.length || 0 }}</div>
                    <div class="stat-label">参与项目</div>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon">🏆</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ participatedChallenges.length || 0 }}</div>
                    <div class="stat-label">技术挑战</div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <!-- 内容区域 - 使用网格布局 -->
        <div class="content-grid">
          <!-- 第一行：任务和公告/技能 -->
          <div class="grid-row main-row">
            <!-- 左侧：已接取的任务 (更宽) -->
            <div class="grid-card task-section wider-task-section">
              <div class="card-header">
                <h3>已接取的任务</h3>
                <button class="view-all-btn" @click="viewAllAssignedTasks">查看全部</button>
              </div>
              <div v-if="assignedTasks.length === 0" class="no-data">
                <p>您还没有接取任务</p>
              </div>
              <div v-else>
                <!-- 已接取任务列表 -->
                <div v-for="task in assignedTasks" :key="task.taskId" class="task-item" @click="goToTaskSubmission(task.taskId)">
                  <div class="task-header">
                    <div class="task-title">{{ task.summary }}</div>
                    <span class="task-status" :class="getStatusClass(task)">{{ getStatusText(task) }}</span>
                  </div>
                  <div class="task-details">
                    <p>
                      <span class="tag info-tag">企业</span>
                      <span
                        class="enterprise-name"
                        @click.stop="viewEnterpriseDetails(task.enterprise && task.enterprise.enterpriseId)"
                      >
                        {{ task.enterprise && task.enterprise.name || '暂无企业信息' }}
                      </span>
                    </p>
                    <p><span class="tag success-tag">技术栈</span> {{ task.techStack }}</p>
                    <p><span class="tag warning-tag">难度</span> <StarRatingDisplay :value="task.difficulty || 3" :showText="false" /></p>
                  </div>
                </div>

                <!-- 分页控制 -->
                <div v-if="totalAssignedTasks.length > assignedTasksPerPage" class="pagination-controls">
                  <button
                    class="pagination-btn"
                    :disabled="assignedTasksPage === 1"
                    @click="prevAssignedTasksPage"
                  >
                    &laquo; 上一页
                  </button>
                  <span class="pagination-info">
                    {{ assignedTasksPage }} / {{ Math.ceil(totalAssignedTasks.length / assignedTasksPerPage) }}
                  </span>
                  <button
                    class="pagination-btn"
                    :disabled="assignedTasksPage >= Math.ceil(totalAssignedTasks.length / assignedTasksPerPage)"
                    @click="nextAssignedTasksPage"
                  >
                    下一页 &raquo;
                  </button>
                </div>
              </div>
            </div>

            <!-- 右侧：公告和技能图谱 (更窄) -->
            <div class="right-column narrower-right-column">
              <!-- 公告栏 -->
              <div class="grid-card notice-section">
                <div class="card-header">
                  <h3>最新公告</h3>
                  <button class="view-all-btn" @click="viewAllNotices">查看全部</button>
                </div>
                <div v-if="notices.length === 0" class="no-data">
                  <p>暂无公告</p>
                </div>
                <div v-else class="notice-list enhanced-notice-list">
                  <div v-for="notice in notices" :key="notice.noticeId" class="notice-item" @click="viewNoticeDetails(notice.noticeId)">
                    <div class="notice-title">{{ notice.title }}</div>
                    <div class="notice-content">
                      <p>{{ truncateText(notice.content, 80) }}</p>
                    </div>
                    <div class="notice-meta">
                      <span class="notice-publisher">发布者: {{ notice.publisher || notice.enterprise?.name || '系统公告' }}</span>
                      <span class="notice-date">{{ formatDate(notice.publishTime || notice.createdTime) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 技能图谱 -->
              <div class="grid-card skills-section">
                <div class="card-header">
                  <h3>技能图谱</h3>
                </div>
                <div v-if="skills.length === 0" class="no-data">
                  <p>暂无技能数据，完成任务后将自动生成</p>
                </div>
                <div v-else class="skills-content">
                  <SkillRadarChart :skills="skills" />
                </div>
              </div>
            </div>
          </div>

          <!-- 第二行：技术挑战和项目合作 -->
          <div class="grid-row content-row">
            <!-- 参与的技术挑战 -->
            <div class="grid-card challenges-section enhanced-challenges-section">
              <div class="card-header">
                <h3>参与的技术挑战</h3>
                <button class="view-all-btn" @click="viewChallenges">查看全部</button>
              </div>
              <div v-if="participatedChallenges.length === 0" class="no-data">
                <p>您还没有参与技术挑战</p>
              </div>
              <div v-else class="compact-list">
                <div v-for="(challenge, index) in displayedChallenges" :key="challenge.challengeId" class="challenge-item" @click="viewChallengeDetails(challenge.challengeId)">
                  <div class="challenge-header">
                    <div class="challenge-title">{{ challenge.title }}</div>
                    <span class="challenge-status" :class="getChallengeStatusClass(challenge.submissionStatus)">
                      {{ getChallengeStatusText(challenge.submissionStatus) }}
                    </span>
                  </div>
                  <div class="challenge-details">
                    <p><span class="tag info-tag">企业</span> {{ challenge.enterprise ? challenge.enterprise.name : '未知企业' }}</p>
                    <p><span class="tag success-tag">技术栈</span> {{ challenge.techStack }}</p>
                  </div>
                </div>
                <div v-if="participatedChallenges.length > challengesPerPage" class="pagination-controls">
                  <button class="pagination-btn" :disabled="challengePage === 1" @click="prevChallengePage">&laquo; 上一页</button>
                  <span class="pagination-info">{{ challengePage }} / {{ Math.ceil(participatedChallenges.length / challengesPerPage) }}</span>
                  <button class="pagination-btn" :disabled="challengePage >= Math.ceil(participatedChallenges.length / challengesPerPage)" @click="nextChallengePage">下一页 &raquo;</button>
                </div>
              </div>
            </div>

            <!-- 参与的项目合作 -->
            <div class="grid-card projects-section enhanced-projects-section">
              <div class="card-header">
                <h3>参与的项目合作</h3>
                <button class="view-all-btn" @click="viewProjects">查看全部</button>
              </div>
              <div v-if="participatedProjects.length === 0" class="no-data">
                <p>您还没有参与项目合作</p>
              </div>
              <div v-else class="compact-list">
                <div v-for="project in displayedProjects" :key="project.projectId" class="project-item" @click="viewProjectDetails(project.projectId)">
                  <div class="project-header">
                    <div class="project-title">{{ project.title }}</div>
                    <span class="project-status" :class="getProjectStatusClass(project.status)">
                      {{ getProjectStatusText(project.status) }}
                    </span>
                  </div>
                  <div class="project-details">
                    <p><span class="tag info-tag">企业</span> {{ project.enterprise && project.enterprise.name ? project.enterprise.name : (project.enterpriseName || '未知企业') }}</p>
                    <p><span class="tag success-tag">类型</span> {{ getProjectTypeText(project.collaborationType) }}</p>
                  </div>
                </div>
                <div v-if="participatedProjects.length > projectsPerPage" class="pagination-controls">
                  <button class="pagination-btn" :disabled="projectPage === 1" @click="prevProjectPage">&laquo; 上一页</button>
                  <span class="pagination-info">{{ projectPage }} / {{ Math.ceil(participatedProjects.length / projectsPerPage) }}</span>
                  <button class="pagination-btn" :disabled="projectPage >= Math.ceil(participatedProjects.length / projectsPerPage)" @click="nextProjectPage">下一页 &raquo;</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 第三行：企业竞赛和企业活动 -->
          <div class="grid-row content-row">
            <!-- 企业竞赛 -->
            <div class="grid-card competitions-section uniform-height-card">
              <div class="card-header">
                <h3>企业竞赛</h3>
                <button class="view-all-btn" @click="viewCompetitions">查看全部</button>
              </div>
              <div v-if="competitions.length === 0" class="no-data">
                <p>暂无企业竞赛</p>
              </div>
              <div v-else class="compact-list">
                <div v-for="competition in displayedCompetitions" :key="competition.competitionId" class="competition-item" @click="viewCompetitionDetails(competition.competitionId)">
                  <div class="competition-header">
                    <div class="competition-title">{{ competition.title }}</div>
                    <span class="competition-status" :class="getCompetitionStatusClass(competition.status)">
                      {{ getCompetitionStatusText(competition.status) }}
                    </span>
                  </div>
                  <div class="competition-details">
                    <p><span class="tag info-tag">企业</span> {{ competition.enterprise ? competition.enterprise.name : '未知企业' }}</p>
                    <p><span class="tag success-tag">奖励</span> {{ competition.prizes || '暂无信息' }}</p>
                  </div>
                </div>
                <div v-if="competitions.length > competitionsPerPage" class="pagination-controls">
                  <button class="pagination-btn" :disabled="competitionPage === 1" @click="prevCompetitionPage">&laquo; 上一页</button>
                  <span class="pagination-info">{{ competitionPage }} / {{ Math.ceil(competitions.length / competitionsPerPage) }}</span>
                  <button class="pagination-btn" :disabled="competitionPage >= Math.ceil(competitions.length / competitionsPerPage)" @click="nextCompetitionPage">下一页 &raquo;</button>
                </div>
              </div>
            </div>

            <!-- 企业活动 (降低高度) -->
            <div class="grid-card events-section uniform-height-card shorter-event-card">
              <div class="card-header">
                <h3>企业活动</h3>
                <button class="view-all-btn" @click="viewEvents">查看全部</button>
              </div>
              <div v-if="events.length === 0" class="no-data">
                <p>暂无企业活动</p>
              </div>
              <div v-else class="compact-list">
                <div v-for="event in displayedEvents" :key="event.eventId" class="event-item" @click="viewEventDetails(event.eventId)">
                  <div class="event-header">
                    <div class="event-title">{{ event.title }}</div>
                    <span class="event-status" :class="getEventStatusClass(event.status)">
                      {{ getEventStatusText(event.status) }}
                    </span>
                  </div>
                  <div class="event-details">
                    <p><span class="tag info-tag">企业</span> {{ event.enterprise ? event.enterprise.name : '未知企业' }}</p>
                    <p><span class="tag warning-tag">时间</span> {{ formatDate(event.startDate || event.eventDate) }}</p>
                  </div>
                </div>
                <div v-if="events.length > eventsPerPage" class="pagination-controls">
                  <button class="pagination-btn" :disabled="eventPage === 1" @click="prevEventPage">&laquo; 上一页</button>
                  <span class="pagination-info">{{ eventPage }} / {{ Math.ceil(events.length / eventsPerPage) }}</span>
                  <button class="pagination-btn" :disabled="eventPage >= Math.ceil(events.length / eventsPerPage)" @click="nextEventPage">下一页 &raquo;</button>
                </div>
              </div>
            </div>
          </div>


        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import StarRatingDisplay from '../Common/StarRatingDisplay.vue';
import SkillRadarChart from '../Common/SkillRadarChart.vue';
import SkillChart from '../Common/SkillChart.vue';

export default {
  name: 'StudentHome',
  components: {
    StarRatingDisplay,
    SkillRadarChart,
    SkillChart
  },
  data() {
    return {
      user: {},
      assignedTasks: [], // 当前页显示的已接取任务
      totalAssignedTasks: [], // 所有已接取任务
      assignedTasksPage: 1, // 当前页码
      assignedTasksPerPage: 2, // 每页显示任务数
      notices: [],
      currentDate: '',
      claimedTaskIds: [], // 存储已领取的任务ID
      skills: [], // 学生技能数据
      studentId: null,
      unreadMessages: 0, // 初始化为0，会在fetchUnreadMessages中更新
      hasUnreadMessages: false, // 是否有未读消息，用于控制红点显示
      messageCheckInterval: null,
      lastMessageCheck: 0, // 上次检查消息的时间戳

      // 参与的技术挑战
      participatedChallenges: [],
      // 参与的项目合作
      participatedProjects: [],
      // 企业竞赛
      competitions: [],
      // 企业活动
      events: [],
      // 技术挑战分页
      challengePage: 1,
      challengesPerPage: 3,
      // 项目分页
      projectPage: 1,
      projectsPerPage: 2, // 每页显示2个项目
      // 竞赛分页
      competitionPage: 1,
      competitionsPerPage: 2,
      // 活动分页
      eventPage: 1,
      eventsPerPage: 2,

      // 编辑模式
      editMode: false,
      isAdmin: false, // 在created中检查是否为管理员
      // 组件样式
      componentStyles: {
        taskSection: {},
        rightColumn: {},
        noticeSection: {},
        skillsSection: {},
        challengesSection: {},
        projectsSection: {},
        competitionsSection: {},
        eventsSection: {},
        jobsSection: {}
      },
      // 存储布局的本地存储键
      layoutStorageKey: 'studentHomeLayout'
    };
  },
  computed: {
    // 当前页的技术挑战
    displayedChallenges() {
      const startIndex = (this.challengePage - 1) * this.challengesPerPage;
      const endIndex = startIndex + this.challengesPerPage;
      return this.participatedChallenges.slice(startIndex, endIndex);
    },
    // 当前页的项目合作
    displayedProjects() {
      const startIndex = (this.projectPage - 1) * this.projectsPerPage;
      const endIndex = startIndex + this.projectsPerPage;
      return this.participatedProjects.slice(startIndex, endIndex);
    },
    // 当前页的竞赛
    displayedCompetitions() {
      const startIndex = (this.competitionPage - 1) * this.competitionsPerPage;
      const endIndex = startIndex + this.competitionsPerPage;
      return this.competitions.slice(startIndex, endIndex);
    },
    // 当前页的活动
    displayedEvents() {
      const startIndex = (this.eventPage - 1) * this.eventsPerPage;
      const endIndex = startIndex + this.eventsPerPage;
      return this.events.slice(startIndex, endIndex);
    },

  },
  created() {
    // 获取当前日期
    const now = new Date();
    this.currentDate = now.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    });

    // 从本地存储获取学生ID
    this.studentId = localStorage.getItem('userId');
    if (!this.studentId) {
      console.error('未找到学生ID');
      // 尝试从其他地方获取
      const student = JSON.parse(localStorage.getItem('student'));
      if (student && student.studentId) {
        this.studentId = student.studentId;
      } else {
        // 如果仍然找不到，重定向到登录页面
        this.$router.push('/login');
        return;
      }
    }

    // 检查是否为管理员
    this.checkAdminStatus();

    // The portfolio workspace uses a fixed layout; legacy editor layouts are not restored.

    // 获取用户信息
    this.fetchUserInfo(this.studentId);

    // 获取已接取任务
    this.fetchAssignedTasks(this.studentId);

    // 获取已完成任务，用于生成技能数据
    this.fetchCompletedTasks();

    // 获取所有公告
    this.fetchAllNotices();

    // 获取未读消息数量
    this.fetchUnreadMessages();

    // 获取参与的技术挑战
    this.fetchParticipatedChallenges();

    // 获取参与的项目合作
    this.fetchParticipatedProjects();

    // 获取企业竞赛
    this.fetchCompetitions();

    // 获取企业活动
    this.fetchEvents();

    // 设置定时检查未读消息
    this.lastMessageCheck = Date.now(); // 初始化最后检查时间
    // 使用try-catch包裹定时器创建，避免可能的错误
    try {
      // 设置定时器，定期获取未读消息数量
      this.messageCheckInterval = setInterval(() => {
        // 如果组件已经被销毁，不执行检查
        if (!this.$el || !this.$el.isConnected) {
          console.log('组件已被销毁，停止检查消息');
          clearInterval(this.messageCheckInterval);
          return;
        }

        // 每10秒获取一次未读消息数量，确保及时显示最新的未读消息
        console.log('执行定期未读消息检查');
        this.fetchUnreadMessages();

        // 更新最后检查时间
        this.lastMessageCheck = Date.now();
      }, 10000); // 每10秒检查一次，确保及时显示最新的未读消息
    } catch (error) {
      console.error('创建消息检查定时器失败:', error);
    }
  },
  beforeUnmount() {
    // 清除定时器
    if (this.messageCheckInterval) {
      clearInterval(this.messageCheckInterval);
      this.messageCheckInterval = null;
    }
  },
  // 添加自定义指令
  directives: {
    // 可拖拽指令
    draggable: {
      bind(el, binding) {
        if (!binding.value) return;

        // 初始化样式
        el.style.position = 'relative';
        el.style.cursor = binding.value ? 'move' : 'default';
        el.style.userSelect = 'none';
        el.classList.add('draggable-element');

        // 创建拖拽手柄
        const dragHandle = document.createElement('div');
        dragHandle.className = 'drag-handle';
        dragHandle.innerHTML = '<span>拖拽</span>';
        dragHandle.style.display = binding.value ? 'block' : 'none';
        el.appendChild(dragHandle);

        // 拖拽事件处理
        const handleDragStart = function(e) {
          if (!binding.value) return;
          e.preventDefault();

          // 记录初始位置
          const startX = e.clientX;
          const startY = e.clientY;
          const startLeft = parseInt(el.style.left || '0');
          const startTop = parseInt(el.style.top || '0');

          // 添加活动样式
          el.classList.add('dragging');

          // 移动事件
          const handleDragMove = function(e) {
            const newLeft = startLeft + e.clientX - startX;
            const newTop = startTop + e.clientY - startY;

            el.style.left = newLeft + 'px';
            el.style.top = newTop + 'px';
          };

          // 拖拽结束事件
          const handleDragEnd = function() {
            document.removeEventListener('mousemove', handleDragMove);
            document.removeEventListener('mouseup', handleDragEnd);

            // 移除活动样式
            el.classList.remove('dragging');

            // 触发自定义事件
            el.dispatchEvent(new CustomEvent('dragend', {
              detail: { left: el.style.left, top: el.style.top }
            }));
          };

          // 添加事件监听
          document.addEventListener('mousemove', handleDragMove);
          document.addEventListener('mouseup', handleDragEnd);
        };

        // 绑定拖拽事件
        dragHandle.addEventListener('mousedown', handleDragStart);

        // 存储事件处理函数，便于后续移除
        el._dragHandleEvents = {
          handle: dragHandle,
          start: handleDragStart
        };
      },

      update(el, binding) {
        if (el._dragHandleEvents && el._dragHandleEvents.handle) {
          el.style.cursor = binding.value ? 'move' : 'default';
          el._dragHandleEvents.handle.style.display = binding.value ? 'block' : 'none';
        }
      },

      unbind(el) {
        // 清理事件和元素
        if (el._dragHandleEvents) {
          el._dragHandleEvents.handle.removeEventListener('mousedown', el._dragHandleEvents.start);
          if (el._dragHandleEvents.handle.parentNode === el) {
            el.removeChild(el._dragHandleEvents.handle);
          }
          delete el._dragHandleEvents;
        }
      }
    },

    // 可调整大小指令
    resizable: {
      bind(el, binding) {
        // 初始化样式
        el.style.position = 'relative';
        el.classList.add('resizable-element');

        // 创建调整大小的手柄
        const resizeHandle = document.createElement('div');
        resizeHandle.className = 'resize-handle';
        resizeHandle.innerHTML = '<span>调整大小</span>';
        resizeHandle.style.display = binding.value ? 'block' : 'none';
        el.appendChild(resizeHandle);

        // 添加调试信息
        console.log('创建调整手柄:', resizeHandle);

        // 调整大小事件处理
        const handleResizeStart = function(e) {
          if (!binding.value) return;
          e.preventDefault();
          e.stopPropagation();

          // 记录初始尺寸
          const startX = e.clientX;
          const startY = e.clientY;
          const startWidth = el.offsetWidth;
          const startHeight = el.offsetHeight;

          // 添加活动样式
          el.classList.add('resizing');

          // 调整大小事件
          const handleResizeMove = function(e) {
            const newWidth = startWidth + e.clientX - startX;
            const newHeight = startHeight + e.clientY - startY;

            // 设置最小尺寸
            el.style.width = Math.max(100, newWidth) + 'px';
            el.style.height = Math.max(50, newHeight) + 'px';
          };

          // 调整结束事件
          const handleResizeEnd = function() {
            document.removeEventListener('mousemove', handleResizeMove);
            document.removeEventListener('mouseup', handleResizeEnd);

            // 移除活动样式
            el.classList.remove('resizing');

            // 触发自定义事件
            el.dispatchEvent(new CustomEvent('resizeend', {
              detail: { width: el.style.width, height: el.style.height }
            }));
          };

          // 添加事件监听
          document.addEventListener('mousemove', handleResizeMove);
          document.addEventListener('mouseup', handleResizeEnd);
        };

        // 绑定调整大小事件
        resizeHandle.addEventListener('mousedown', handleResizeStart);

        // 存储事件处理函数，便于后续移除
        el._resizeHandleEvents = {
          handle: resizeHandle,
          start: handleResizeStart
        };
      },

      update(el, binding) {
        if (el._resizeHandleEvents && el._resizeHandleEvents.handle) {
          el._resizeHandleEvents.handle.style.display = binding.value ? 'block' : 'none';
          console.log('更新调整手柄显示:', binding.value, el._resizeHandleEvents.handle);
        } else {
          console.warn('调整手柄不存在:', el);
          // 如果手柄不存在但应该显示，重新创建
          if (binding.value) {
            const resizeHandle = document.createElement('div');
            resizeHandle.className = 'resize-handle';
            resizeHandle.innerHTML = '<span>调整大小</span>';
            resizeHandle.style.display = 'block';
            el.appendChild(resizeHandle);

            // 添加调整大小事件
            const handleResizeStart = function(e) {
              if (!binding.value) return;
              e.preventDefault();
              e.stopPropagation();

              const startX = e.clientX;
              const startY = e.clientY;
              const startWidth = el.offsetWidth;
              const startHeight = el.offsetHeight;

              el.classList.add('resizing');

              const handleResizeMove = function(e) {
                const newWidth = startWidth + e.clientX - startX;
                const newHeight = startHeight + e.clientY - startY;

                el.style.width = Math.max(100, newWidth) + 'px';
                el.style.height = Math.max(50, newHeight) + 'px';
              };

              const handleResizeEnd = function() {
                document.removeEventListener('mousemove', handleResizeMove);
                document.removeEventListener('mouseup', handleResizeEnd);

                el.classList.remove('resizing');

                el.dispatchEvent(new CustomEvent('resizeend', {
                  detail: { width: el.style.width, height: el.style.height }
                }));
              };

              document.addEventListener('mousemove', handleResizeMove);
              document.addEventListener('mouseup', handleResizeEnd);
            };

            resizeHandle.addEventListener('mousedown', handleResizeStart);

            el._resizeHandleEvents = {
              handle: resizeHandle,
              start: handleResizeStart
            };

            console.log('重新创建调整手柄:', resizeHandle);
          }
        }
      },

      unbind(el) {
        // 清理事件和元素
        if (el._resizeHandleEvents) {
          el._resizeHandleEvents.handle.removeEventListener('mousedown', el._resizeHandleEvents.start);
          if (el._resizeHandleEvents.handle.parentNode === el) {
            el.removeChild(el._resizeHandleEvents.handle);
          }
          delete el._resizeHandleEvents;
        }
      }
    }
  },
  methods: {
    fetchUserInfo(studentId) {
      axios
        .get(`http://localhost:8080/api/login/student/${studentId}`)
        .then((response) => {
          this.user = response.data;
        })
        .catch((error) => {
          console.error("无法获取用户信息:", error);
        });
    },
    fetchAssignedTasks(studentId) {
      return axios
        .get(`http://localhost:8080/api/students/tasks/${studentId}`)
        .then(async (response) => {
          const tasks = response.data;

          // 提取已领取任务的ID
          this.claimedTaskIds = tasks.map(task => task.taskId);
          console.log('已领取的任务IDs:', this.claimedTaskIds);

          // 获取每个任务的提交状态
          const tasksWithStatus = await Promise.all(tasks.map(async (task) => {
            try {
              // 获取学生对该任务的提交记录
              const submissionResponse = await axios.get(
                `http://localhost:8080/api/students/tasks/submissions/${studentId}/tasks/${task.taskId}`
              );

              // 直接使用数据库中的状态，不再使用本地存储或其他逻辑覆盖
              const currentStatus = submissionResponse.data.status;
              console.log(`任务 ${task.taskId} 的当前提交状态:`, currentStatus);

              return {
                ...task,
                status: currentStatus,
                submission: submissionResponse.data
              };
            } catch (error) {
              console.error(`无法获取任务 ${task.taskId} 的提交状态:`, error);
              return task;
            }
          }));

          // 打印所有任务的状态信息，便于调试
          tasksWithStatus.forEach(task => {
            console.log(`任务 ${task.taskId} 的最终状态:`, task.status);
          });

          // 按状态排序：未提交 > 需要修改 > 待审核 > 已完成/已通过
          tasksWithStatus.sort((a, b) => {
            const statusA = this.getTaskSubmissionStatus(a);
            const statusB = this.getTaskSubmissionStatus(b);

            // 定义状态优先级
            const priority = {
              'NOT_SUBMITTED': 1,
              'NEEDS_REVISION': 2,
              'SUBMITTED': 3,
              'COMPLETED': 4,
              'COMPLETED_BEFORE': 5  // 已通过的任务排在最后
            };

            return (priority[statusA] || 99) - (priority[statusB] || 99);
          });

          console.log('排序后的任务状态:', tasksWithStatus.map(t => ({
            id: t.taskId,
            status: this.getTaskSubmissionStatus(t),
            hasCompletedBefore: t.hasCompletedBefore
          })));

          console.log('排序后的任务:', tasksWithStatus.map(t => ({ id: t.taskId, status: this.getTaskSubmissionStatus(t) })));

          // 设置分页相关数据
          this.totalAssignedTasks = tasksWithStatus;
          this.updateAssignedTasksPage();

        })
        .catch((error) => {
          console.error("无法获取已接取任务:", error);
          this.claimedTaskIds = [];
        });
    },

    fetchAllNotices() {
      axios
        .get("http://localhost:8080/api/login/notice/all")
        .then((response) => {
          this.notices = response.data.slice(0, 5);  // 获取最新5个公告
        })
        .catch((error) => {
          console.error("无法获取公告:", error);
        });
    },

    goToTaskSubmission(taskId) {
      this.$router.push({ name: "TaskSubmission", params: { taskId } });
    },

    viewAllAssignedTasks() {
      this.$router.push({ name: "AllAssignedTasks" });
    },

    viewAllNotices() {
      this.$router.push({ name: "all-notices" });
    },

    viewNoticeDetails(noticeId) {
      this.$router.push({ name: "notice-detail", params: { noticeId } });
    },

    viewChallenges() {
      this.$router.push({ name: "StudentChallenges" });
    },

    viewChallengeDetails(challengeId) {
      this.$router.push({ name: "TechnicalChallengeDetail", params: { challengeId } });
    },



    viewJobs() {
      this.$router.push({ name: "student-jobs" });
    },

    viewProjects() {
      this.$router.push({ name: "StudentProjects" });
    },

    viewCompetitions() {
      this.$router.push({ name: "StudentCompetitions" });
    },

    viewEvents() {
      this.$router.push({ name: "StudentEvents" });
    },

    viewForum() {
      this.$router.push({
        name: 'ForumHome',
        query: {
          userId: this.studentId,
          userType: 'STUDENT'
        }
      });
    },

    // 查看竞赛详情
    viewCompetitionDetails(competitionId) {
      this.$router.push({ name: 'CompetitionDetail', params: { competitionId } });
    },

    // 查看培训资料
    viewTrainingMaterials() {
      this.$router.push({ name: 'StudentTrainingMaterials' });
    },

    // 查看消息
    viewMessages() {
      this.$router.push({ name: 'student-messages' });
    },

    // 查看个人资料
    viewProfile() {
      this.$router.push({ name: 'StudentProfileSelf', params: { studentId: this.studentId } });
    },

    // 查看项目详情
    viewProjectDetails(projectId) {
      this.$router.push({ name: 'ProjectDetail', params: { projectId } });
    },

    // 查看岗位详情
    viewJobDetails(jobId) {
      this.$router.push({ name: 'JobDetail', params: { jobId } });
    },

    // 获取技术挑战状态的CSS类名
    getChallengeStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'APPROVED':
          return 'status-completed';
        case 'PENDING':
          return 'status-in-progress';
        case 'REJECTED':
          return 'status-rejected';
        default:
          return 'status-pending';
      }
    },

    // 获取技术挑战状态的文本
    getChallengeStatusText(status) {
      if (!status) return '待审核';

      switch(status) {
        case 'APPROVED':
          return '已通过';
        case 'PENDING':
          return '待审核';
        case 'REJECTED':
          return '未通过';
        default:
          return '待审核';
      }
    },

    // 获取项目状态的CSS类名
    getProjectStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'COMPLETED':
          return 'status-completed';
        case 'OPEN':
          return 'status-in-progress';
        case 'CLOSED':
          return 'status-rejected';
        default:
          return 'status-pending';
      }
    },

    // 获取项目状态的文本
    getProjectStatusText(status) {
      if (!status) return '进行中';

      switch(status) {
        case 'COMPLETED':
          return '已完成';
        case 'OPEN':
          return '进行中';
        case 'CLOSED':
          return '已关闭';
        default:
          return '进行中';
      }
    },

    // 获取竞赛状态的CSS类名
    getCompetitionStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'OPEN':
          return 'status-pending';
        case 'ONGOING':
          return 'status-in-progress';
        case 'CLOSED':
          return 'status-completed';
        default:
          return 'status-pending';
      }
    },

    // 获取竞赛状态的文本
    getCompetitionStatusText(status) {
      if (!status) return '未知状态';

      switch(status) {
        case 'OPEN':
          return '报名中';
        case 'ONGOING':
          return '进行中';
        case 'CLOSED':
          return '已结束';
        default:
          return '未知状态';
      }
    },

    // 获取项目类型的文本
    getProjectTypeText(type) {
      if (!type) return '未知类型';

      // 处理后端返回的collaborationType字段
      if (type === '订单培养' || type.includes('订单')) {
        return '订单培养';
      } else if (type === '共建实训基地' || type.includes('实训')) {
        return '实训基地';
      } else if (type === '产学研合作' || type.includes('产学研')) {
        return '产学研';
      }

      // 兼容旧的枚举值
      switch(type) {
        case 'ORDER_TRAINING':
          return '订单培养';
        case 'TRAINING_BASE':
          return '实训基地';
        case 'INDUSTRY_ACADEMIA_RESEARCH':
          return '产学研';
        default:
          return type; // 直接返回原始类型文本
      }
    },

    // 获取活动状态的CSS类名
    getEventStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'UPCOMING':
          return 'status-pending';
        case 'ONGOING':
          return 'status-in-progress';
        case 'COMPLETED':
          return 'status-completed';
        default:
          return 'status-pending';
      }
    },

    // 获取活动状态的文本
    getEventStatusText(status) {
      if (!status) return '未知状态';

      switch(status) {
        case 'UPCOMING':
          return '即将开始';
        case 'ONGOING':
          return '进行中';
        case 'COMPLETED':
          return '已结束';
        default:
          return '未知状态';
      }
    },

    // 查看活动详情
    viewEventDetails(eventId) {
      this.$router.push({ name: 'EventDetail', params: { eventId } });
    },



    // 更新已接取任务的分页显示
    updateAssignedTasksPage() {
      const startIndex = (this.assignedTasksPage - 1) * this.assignedTasksPerPage;
      const endIndex = startIndex + this.assignedTasksPerPage;

      // 从总任务中获取当前页的任务
      this.assignedTasks = this.totalAssignedTasks.slice(startIndex, endIndex);

      console.log(`当前页码: ${this.assignedTasksPage}, 显示任务: ${startIndex}-${endIndex-1}`);
    },

    // 上一页
    prevAssignedTasksPage() {
      if (this.assignedTasksPage > 1) {
        this.assignedTasksPage--;
        this.updateAssignedTasksPage();
      }
    },

    // 下一页
    nextAssignedTasksPage() {
      const totalPages = Math.ceil(this.totalAssignedTasks.length / this.assignedTasksPerPage);
      if (this.assignedTasksPage < totalPages) {
        this.assignedTasksPage++;
        this.updateAssignedTasksPage();
      }
    },

    // 技术挑战分页控制
    prevChallengePage() {
      if (this.challengePage > 1) {
        this.challengePage--;
      }
    },
    nextChallengePage() {
      const totalPages = Math.ceil(this.participatedChallenges.length / this.challengesPerPage);
      if (this.challengePage < totalPages) {
        this.challengePage++;
      }
    },

    // 项目分页控制
    prevProjectPage() {
      if (this.projectPage > 1) {
        this.projectPage--;
      }
    },
    nextProjectPage() {
      const totalPages = Math.ceil(this.participatedProjects.length / this.projectsPerPage);
      if (this.projectPage < totalPages) {
        this.projectPage++;
      }
    },

    // 竞赛分页控制
    prevCompetitionPage() {
      if (this.competitionPage > 1) {
        this.competitionPage--;
      }
    },
    nextCompetitionPage() {
      const totalPages = Math.ceil(this.competitions.length / this.competitionsPerPage);
      if (this.competitionPage < totalPages) {
        this.competitionPage++;
      }
    },

    // 活动分页控制
    prevEventPage() {
      if (this.eventPage > 1) {
        this.eventPage--;
      }
    },
    nextEventPage() {
      const totalPages = Math.ceil(this.events.length / this.eventsPerPage);
      if (this.eventPage < totalPages) {
        this.eventPage++;
      }
    },



    // 获取已完成的任务
    fetchCompletedTasks() {
      // 如果没有学生ID，则不执行
      if (!this.studentId) {
        console.warn('没有学生ID，无法获取已完成任务');
        return Promise.resolve();
      }

      return axios.get(`http://localhost:8080/api/students/${this.studentId}/completed-tasks`)
        .then(response => {
          const completedTasks = response.data || [];
          console.log('已完成的任务:', completedTasks);

          // 生成技能数�?
          this.generateSkillsData(completedTasks);
          return completedTasks;
        })
        .catch(error => {
          console.error('获取已完成任务失�?', error);
          // 出错时设置空技能数�?
          this.skills = [];
          return [];
        });
    },



    // 获取参与的技术挑战
    fetchParticipatedChallenges() {
      if (!this.studentId) {
        console.warn('没有学生ID，无法获取参与的技术挑战');
        return;
      }

      // 先获取所有技术挑战
      axios.get('http://localhost:8080/api/challenges', {
        params: { page: 0, size: 100 }
      })
        .then(allChallengesResponse => {
          const allChallenges = allChallengesResponse.data.content || [];
          console.log('所有技术挑战:', allChallenges);

          // 然后获取学生的提交记录
          return axios.get(`http://localhost:8080/api/challenges/submissions/student/${this.studentId}`)
            .then(submissionsResponse => {
              const submissions = submissionsResponse.data || [];
              console.log('学生提交的技术挑战:', submissions);

              if (submissions.length === 0) {
                // 如果没有提交记录，显示空数组
                this.participatedChallenges = [];
                return;
              }

              // 将提交记录与挑战详情合并
              const participatedChallenges = [];

              for (const submission of submissions) {
                const challenge = allChallenges.find(c => c.challengeId === submission.challengeId);
                if (challenge) {
                  participatedChallenges.push({
                    ...challenge,
                    submissionStatus: submission.status,
                    submissionTime: submission.submitTime,
                    rating: submission.rating
                  });
                }
              }

              this.participatedChallenges = participatedChallenges.slice(0, 3); // 只显示前3个挑战
              console.log('参与的技术挑战:', this.participatedChallenges);
            });
        })
        .catch(error => {
          console.error('获取参与的技术挑战失败:', error);
          this.participatedChallenges = [];
        });
    },

    // 获取参与的项目合作
    fetchParticipatedProjects() {
      if (!this.studentId) {
        console.warn('没有学生ID，无法获取参与的项目合作');
        return;
      }

      axios.get(`http://localhost:8080/api/projects/student/${this.studentId}/participated`)
        .then(response => {
          this.participatedProjects = (response.data || []).slice(0, 3); // 只显示前3个项目
          console.log('参与的项目合作', this.participatedProjects);
        })
        .catch(error => {
          console.error('获取参与的项目合作失败', error);
          this.participatedProjects = [];
        });
    },

    // 获取企业竞赛
    fetchCompetitions() {
      axios.get('http://localhost:8080/api/competitions', {
        params: { page: 0, size: 10 }
      })
        .then(response => {
          this.competitions = response.data.content || [];
          console.log('企业竞赛:', this.competitions);
        })
        .catch(error => {
          console.error('获取企业竞赛失败:', error);
          this.competitions = [];
        });
    },

    // 获取企业活动
    fetchEvents() {
      axios.get('http://localhost:8080/api/events', {
        params: { page: 0, size: 10 }
      })
        .then(response => {
          this.events = response.data.content || [];
          console.log('企业活动:', this.events);
        })
        .catch(error => {
          console.error('获取企业活动失败:', error);
          this.events = [];
        });
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '';

      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString; // 如果日期无效，返回原始字符串

      // 格式化为“2023年10月15日”的形式
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    },

    // 截断文本
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    // 获取任务提交状态
    getTaskSubmissionStatus(task) {
      // 如果没有状态信息，默认为未提交
      if (!task || !task.status) return 'NOT_SUBMITTED';

      // 直接返回数据库中的状态
      return task.status;
    },

    // 获取任务状态的CSS类名
    getStatusClass(task) {
      const status = this.getTaskSubmissionStatus(task);

      switch(status) {
        case 'COMPLETED':
          return 'status-completed';
        case 'SUBMITTED':
          return 'status-in-progress';
        case 'NEEDS_REVISION':
          return 'status-rejected';
        case 'NOT_SUBMITTED':
        default:
          return 'status-pending';
      }
    },

    // 获取任务状态的文本
    getStatusText(task) {
      const status = this.getTaskSubmissionStatus(task);

      switch(status) {
        case 'COMPLETED':
          return '已完成';
        case 'SUBMITTED':
          return '待审核';
        case 'NEEDS_REVISION':
          return '需要修改';
        case 'NOT_SUBMITTED':
          return '未提交';
        default:
          return '未知状态';
      }
    },

    // 检查是否为管理员
    checkAdminStatus() {
      // 从本地存储中获取用户角色
      const userRole = localStorage.getItem('userRole');
      this.isAdmin = userRole === 'ADMIN';

      // 测试时可以强制设置为管理员
      this.isAdmin = true; // 测试时打开此行
    },

    // 切换编辑模式
    toggleEditMode() {
      this.editMode = !this.editMode;
      console.log('切换编辑模式:', this.editMode);

      // 先清除所有现有的手柄
      document.querySelectorAll('.drag-handle, .resize-handle, .floating-handle').forEach(handle => {
        if (handle.parentNode) {
          handle.parentNode.removeChild(handle);
        }
      });

      // 强制更新所有可编辑组件
      this.$nextTick(() => {
        if (this.editMode) {
          // 添加可拖拽类
          const components = document.querySelectorAll('.grid-card');
          components.forEach(component => {
            component.classList.add('draggable');
          });

          // 使用浮动手柄方法
          this.createFloatingHandles();

          // 创建一个浮动提示框
          const tipBox = document.createElement('div');
          tipBox.className = 'edit-mode-tip';
          tipBox.innerHTML = `
            <h3>编辑模式已启用</h3>
            <p>现在您可以：</p>
            <ul>
              <li>完成后点击右上角的"退出编辑模式"按钮保存更改</li>
            </ul>
            <button class="close-tip">我知道了</button>
          `;
          document.body.appendChild(tipBox);

          // 添加关闭按钮事件
          tipBox.querySelector('.close-tip').addEventListener('click', () => {
            document.body.removeChild(tipBox);
          });
        } else {
          // 退出编辑模式时保存布局
          this.saveLayout();

          // 移除所有手柄和手柄容器
          document.querySelectorAll('.resize-handle, .drag-handle, .floating-handle').forEach(handle => {
            if (handle.parentNode) {
              handle.parentNode.removeChild(handle);
            }
          });

          // 移除手柄容器
          const handlesContainer = document.getElementById('floating-handles-container');
          if (handlesContainer) {
            document.body.removeChild(handlesContainer);
          }

          // 移除可拖拽类
          document.querySelectorAll('.draggable').forEach(component => {
            component.classList.remove('draggable');
          });

          // 移除滚动和调整事件监听器
          if (this._handlePositionEvents) {
            window.removeEventListener('scroll', this._handlePositionEvents.scroll);
            window.removeEventListener('resize', this._handlePositionEvents.resize);
            this._handlePositionEvents = null;
          }

          // 移除提示框
          const tipBox = document.querySelector('.edit-mode-tip');
          if (tipBox && tipBox.parentNode) {
            tipBox.parentNode.removeChild(tipBox);
          }

          // 显示保存成功提示
          const savedTip = document.createElement('div');
          savedTip.className = 'saved-tip';
          savedTip.textContent = '布局已保存';
          document.body.appendChild(savedTip);

          // 3秒后自动移除提示
          setTimeout(() => {
            if (savedTip.parentNode) {
              savedTip.parentNode.removeChild(savedTip);
            }
          }, 3000);
        }
      });
    },

    // 保存布局
    saveLayout() {
      const layout = {
        taskSection: this.componentStyles.taskSection,
        rightColumn: this.componentStyles.rightColumn,
        noticeSection: this.componentStyles.noticeSection,
        skillsSection: this.componentStyles.skillsSection,
        challengesSection: this.componentStyles.challengesSection,
        projectsSection: this.componentStyles.projectsSection,
        competitionsSection: this.componentStyles.competitionsSection,
        eventsSection: this.componentStyles.eventsSection,
        jobsSection: this.componentStyles.jobsSection
      };

      localStorage.setItem(this.layoutStorageKey, JSON.stringify(layout));
      alert('布局已保存');
    },

    // 创建独立的手柄
    createFloatingHandles() {
      // 先清除所有现有的浮动手柄
      document.querySelectorAll('.floating-handle').forEach(handle => {
        if (handle.parentNode) {
          handle.parentNode.removeChild(handle);
        }
      });

      // 移除旧的手柄容器
      const oldContainer = document.getElementById('floating-handles-container');
      if (oldContainer) {
        document.body.removeChild(oldContainer);
      }

      if (!this.editMode) return;

      // 创建一个新的手柄容器，确保它始终在最高层级
      const handlesContainer = document.createElement('div');
      handlesContainer.id = 'floating-handles-container';
      handlesContainer.style.position = 'fixed';
      handlesContainer.style.top = '0';
      handlesContainer.style.left = '0';
      handlesContainer.style.width = '100%';
      handlesContainer.style.height = '100%';
      handlesContainer.style.pointerEvents = 'none'; // 不拦截鼠标事件
      handlesContainer.style.zIndex = '999999'; // 超高层级
      document.body.appendChild(handlesContainer);

      // 获取所有可编辑组件
      const components = document.querySelectorAll('.grid-card');

      // 创建一个更新手柄位置的函数
      const updateHandlePositions = () => {
        components.forEach((component, index) => {
          // 获取组件当前位置
          const rect = component.getBoundingClientRect();

          // 更新拖拽手柄位置
          const dragHandle = document.querySelector(`.drag-handle[data-component-index="${index}"]`);
          if (dragHandle) {
            dragHandle.style.left = rect.left + 'px';
            dragHandle.style.top = rect.top + 'px';
            dragHandle.style.zIndex = '99999'; // 确保始终在最高层级
            dragHandle.style.pointerEvents = 'auto'; // 确保可点击
          }

          // 更新调整大小手柄位置
          const resizeHandle = document.querySelector(`.resize-handle[data-component-index="${index}"]`);
          if (resizeHandle) {
            resizeHandle.style.left = (rect.right - 100) + 'px';
            resizeHandle.style.top = (rect.bottom - 30) + 'px';
            resizeHandle.style.zIndex = '99999'; // 确保始终在最高层级
            resizeHandle.style.pointerEvents = 'auto'; // 确保可点击
          }
        });
      };

      // 添加滚动事件监听器
      window.addEventListener('scroll', updateHandlePositions);
      window.addEventListener('resize', updateHandlePositions);

      // 存储事件监听器引用，便于后续移除
      this._handlePositionEvents = {
        scroll: updateHandlePositions,
        resize: updateHandlePositions
      };

      components.forEach((component, index) => {
        // 获取组件位置
        const rect = component.getBoundingClientRect();

        // 创建拖拽手柄
        const dragHandle = document.createElement('button');
        dragHandle.className = 'floating-handle drag-handle';
        dragHandle.textContent = '拖拽';
        dragHandle.style.position = 'fixed';
        dragHandle.style.left = rect.left + 'px';
        dragHandle.style.top = rect.top + 'px';
        dragHandle.style.zIndex = '99999'; // 确保最高层级
        dragHandle.dataset.componentIndex = index;
        // 添加到手柄容器中
        const handleContainer = document.getElementById('floating-handles-container');
        handleContainer.appendChild(dragHandle);
        // 手柄本身需要响应鼠标事件
        dragHandle.style.pointerEvents = 'auto';

        // 创建调整大小手柄
        const resizeHandle = document.createElement('button');
        resizeHandle.className = 'floating-handle resize-handle';
        resizeHandle.textContent = '调整大小';
        resizeHandle.style.position = 'fixed';
        resizeHandle.style.left = (rect.right - 100) + 'px';
        resizeHandle.style.top = (rect.bottom - 30) + 'px';
        resizeHandle.style.zIndex = '99999'; // 确保最高层级
        resizeHandle.dataset.componentIndex = index;
        // 添加到手柄容器中
        const resizeContainer = document.getElementById('floating-handles-container');
        resizeContainer.appendChild(resizeHandle);
        // 手柄本身需要响应鼠标事件
        resizeHandle.style.pointerEvents = 'auto';

        // 添加拖拽事件
        dragHandle.addEventListener('mousedown', (e) => {
          e.preventDefault();

          const startX = e.clientX;
          const startY = e.clientY;
          const startLeft = parseInt(component.style.left || '0');
          const startTop = parseInt(component.style.top || '0');

          const handleDragMove = function(e) {
            const newLeft = startLeft + e.clientX - startX;
            const newTop = startTop + e.clientY - startY;

            component.style.left = newLeft + 'px';
            component.style.top = newTop + 'px';

            // 同时移动手柄
            dragHandle.style.left = (rect.left + e.clientX - startX) + 'px';
            dragHandle.style.top = (rect.top + e.clientY - startY) + 'px';
            resizeHandle.style.left = (rect.right - 100 + e.clientX - startX) + 'px';
            resizeHandle.style.top = (rect.bottom - 30 + e.clientY - startY) + 'px';
          };

          const handleDragEnd = function() {
            document.removeEventListener('mousemove', handleDragMove);
            document.removeEventListener('mouseup', handleDragEnd);

            component.dispatchEvent(new CustomEvent('dragend', {
              detail: { left: component.style.left, top: component.style.top }
            }));
          };

          document.addEventListener('mousemove', handleDragMove);
          document.addEventListener('mouseup', handleDragEnd);
        });

        // 添加调整大小事件
        resizeHandle.addEventListener('mousedown', (e) => {
          e.preventDefault();

          const startX = e.clientX;
          const startY = e.clientY;
          const startWidth = component.offsetWidth;
          const startHeight = component.offsetHeight;

          // 获取组件的原始样式
          const originalStyles = window.getComputedStyle(component);
          const originalFlex = originalStyles.getPropertyValue('flex');
          const originalFlexGrow = originalStyles.getPropertyValue('flex-grow');
          const originalFlexShrink = originalStyles.getPropertyValue('flex-shrink');
          const originalFlexBasis = originalStyles.getPropertyValue('flex-basis');

          // 暂时移除flex属性，使其可以自由调整大小
          component.style.flex = 'none';
          component.style.flexGrow = '0';
          component.style.flexShrink = '0';
          component.style.flexBasis = 'auto';

          const handleResizeMove = function(e) {
            const newWidth = startWidth + e.clientX - startX;
            const newHeight = startHeight + e.clientY - startY;

            // 设置绝对宽高
            component.style.width = Math.max(100, newWidth) + 'px';
            component.style.height = Math.max(50, newHeight) + 'px';

            // 移动调整手柄
            resizeHandle.style.left = (rect.right - 100 + e.clientX - startX) + 'px';
            resizeHandle.style.top = (rect.bottom - 30 + e.clientY - startY) + 'px';

            // 更新所有手柄位置
            if (this._handlePositionEvents && this._handlePositionEvents.scroll) {
              this._handlePositionEvents.scroll();
            }
          }.bind(this);

          const handleResizeEnd = function() {
            document.removeEventListener('mousemove', handleResizeMove);
            document.removeEventListener('mouseup', handleResizeEnd);

            // 触发自定义事件
            component.dispatchEvent(new CustomEvent('resizeend', {
              detail: {
                width: component.style.width,
                height: component.style.height,
                originalFlex,
                originalFlexGrow,
                originalFlexShrink,
                originalFlexBasis
              }
            }));

            // 更新所有手柄位置
            if (this._handlePositionEvents && this._handlePositionEvents.scroll) {
              setTimeout(() => {
                this._handlePositionEvents.scroll();
              }, 100);
            }
          }.bind(this);

          document.addEventListener('mousemove', handleResizeMove);
          document.addEventListener('mouseup', handleResizeEnd);
        });
      });
    },

    // 加载已保存的布局
    loadSavedLayout() {
      const savedLayout = localStorage.getItem(this.layoutStorageKey);
      if (savedLayout) {
        try {
          const layout = JSON.parse(savedLayout);
          this.componentStyles = { ...this.componentStyles, ...layout };
        } catch (error) {
          console.error('加载布局失败:', error);
        }
      }
    },

    // 更新组件位置
    updateComponentPosition(componentName, event) {
      if (!this.componentStyles[componentName]) {
        this.componentStyles[componentName] = {};
      }

      // 获取新的位置
      const { left, top } = event.detail || event.target.style;

      this.componentStyles[componentName] = {
        ...this.componentStyles[componentName],
        position: 'relative',
        left,
        top
      };

      console.log(`更新组件 ${componentName} 位置:`, left, top);
    },

    // 更新组件大小
    updateComponentSize(componentName, event) {
      if (!this.componentStyles[componentName]) {
        this.componentStyles[componentName] = {};
      }

      // 获取新的大小和原始样式
      const {
        width,
        height,
        originalFlex,
        originalFlexGrow,
        originalFlexShrink,
        originalFlexBasis
      } = event.detail || event.target.style;

      this.componentStyles[componentName] = {
        ...this.componentStyles[componentName],
        width,
        height,
        flex: 'none', // 设置为非flex布局
        flexGrow: '0',
        flexShrink: '0',
        flexBasis: 'auto',
        position: 'relative', // 确保定位正确
        overflow: 'visible' // 确保内容不被裁剪
      };

      console.log(`更新组件 ${componentName} 大小:`, width, height);

      // 强制更新所有组件的布局
      this.$nextTick(() => {
        // 更新浮动手柄位置
        if (this._handlePositionEvents && this._handlePositionEvents.scroll) {
          this._handlePositionEvents.scroll();
        }
      });
    },

    // 获取未读消息数量
    fetchUnreadMessages() {
      const studentId = this.studentId;
      if (!studentId) {
        console.warn('没有学生ID，无法获取未读消息');
        return;
      }

      console.log('获取学生未读消息数量:', studentId);

      // 检查本地存储中是否有未读消息数量
      const storedUnreadMessages = localStorage.getItem(`unreadMessages_${studentId}`);
      if (storedUnreadMessages) {
        try {
          const { count, timestamp } = JSON.parse(storedUnreadMessages);
          // 如果存储的时间不超过30秒，直接使用存储的值
          if (Date.now() - timestamp < 30 * 1000) {
            console.log('使用本地存储的未读消息数量:', count);
            this.unreadMessages = count;
            this.hasUnreadMessages = count > 0;
            return;
          }
        } catch (e) {
          console.error('解析本地存储的未读消息数量失败:', e);
        }
      }

      // 获取学生已完成的任务，找出相关企业
      axios.get(`http://localhost:8080/api/students/${studentId}/completed-tasks`)
        .then(response => {
          const completedTasks = response.data || [];

          // 提取企业ID
          const enterpriseIds = [...new Set(
            completedTasks
              .filter(task => task.task && task.task.enterprise)
              .map(task => task.task.enterprise.enterpriseId)
          )];

          // 如果没有完成任何企业任务，则没有未读消息
          if (enterpriseIds.length === 0) {
            this.unreadMessages = 0;
            this.hasUnreadMessages = false;

            // 保存到本地存储
            localStorage.setItem(`unreadMessages_${studentId}`, JSON.stringify({
              count: 0,
              timestamp: Date.now()
            }));

            return;
          }

          // 获取每个企业的聊天记录，统计未读消息
          let totalUnread = 0;
          const promises = enterpriseIds.map(enterpriseId => {
            return axios.get(`http://localhost:8080/api/chat/history/${studentId}/${enterpriseId}`)
              .then(response => {
                const messages = response.data || [];

                // 计算未读消息数量 - 只计算企业发送给学生的未读消息
                const unreadCount = messages.filter(
                  msg => msg.receiverId === studentId && msg.senderType === 'ENTERPRISE' && !msg.read
                ).length;

                return unreadCount;
              })
              .catch(error => {
                console.error(`获取与企业 ${enterpriseId} 的聊天记录失败:`, error);
                return 0;
              });
          });

          // 等待所有请求完成
          Promise.all(promises)
            .then(results => {
              // 计算总未读消息数量
              totalUnread = results.reduce((sum, count) => sum + count, 0);

              console.log('未读消息总数:', totalUnread);
              this.unreadMessages = totalUnread;
              this.hasUnreadMessages = totalUnread > 0;

              // 保存到本地存储
              localStorage.setItem(`unreadMessages_${studentId}`, JSON.stringify({
                count: totalUnread,
                timestamp: Date.now()
              }));
            })
            .catch(error => {
              console.error('计算未读消息总数失败:', error);
            });
        })
        .catch(error => {
          console.error('获取已完成任务失败:', error);
          this.unreadMessages = 0;
          this.hasUnreadMessages = false;
        });
    },

    // 根据已完成任务生成技能数据
    generateSkillsData(completedTasks) {
      // 安全检查
      if (!completedTasks || !Array.isArray(completedTasks) || completedTasks.length === 0) {
        console.log('没有已完成的任务，无法生成技能数据');
        this.skills = [];
        return;
      }

      // 技能映射表，用于累计每种技术的评分和次数
      const skillMap = {};

      // 遍历所有已完成的任务
      completedTasks.forEach(submission => {
        try {
          // 确保submission和task对象存在且有效
          if (!submission || !submission.task) {
            console.warn('跳过无效的提交记录', submission);
            return;
          }

          // 确保技术栈存在
          if (!submission.task.techStack) {
            console.warn('任务没有技术栈信息:', submission.task.taskId);
            return;
          }

          // 获取任务难度和评分，使用默认值防止空值
          const difficulty = (submission.task.difficulty !== undefined && submission.task.difficulty !== null) ?
                            submission.task.difficulty : 3;
          const rating = (submission.rating !== undefined && submission.rating !== null) ?
                        submission.rating : 3;

          // 分割技术栈字符串为数组，并过滤空值
          const techStackString = String(submission.task.techStack || '');
          const techStacks = techStackString.split(',').map(tech => tech.trim()).filter(tech => tech);

          if (techStacks.length === 0) {
            console.warn('任务技术栈解析后为空', techStackString);
            return;
          }

          // 为每个技术栈增加评分
          techStacks.forEach(tech => {
            if (!skillMap[tech]) {
              skillMap[tech] = { total: 0, count: 0 };
            }

            // 根据任务难度和评分计算技能值
            // 难度越高，评分越高，技能值越高
            const skillValue = (difficulty * 0.6 + rating * 0.4);

            skillMap[tech].total += skillValue;
            skillMap[tech].count += 1;
          });
        } catch (error) {
          console.error('处理任务时出错', error, submission);
          // 继续处理下一个任务
        }
      });

      // 检查是否有收集到技能数据
      if (Object.keys(skillMap).length === 0) {
        console.log('没有收集到有效的技能数据');
        this.skills = [];
        return;
      }

      try {
        // 转换为技能数组格式
        const skillsArray = Object.keys(skillMap).map(tech => {
          // 计算平均评分，并限制在1-5之间
          const avgRating = skillMap[tech].total / skillMap[tech].count;
          const level = Math.min(5, Math.max(1, Math.round(avgRating)));

          return {
            name: tech,
            level: level
          };
        });

        // 按评分降序排序，并限制最多显示8个技能
        this.skills = skillsArray
          .sort((a, b) => b.level - a.level)
          .slice(0, 8);

        console.log('生成的技能数据', this.skills);
      } catch (error) {
        console.error('生成技能数组时出错:', error);
        this.skills = [];
      }
    }
  },
};
</script>

<style scoped>
.student-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  font-family: 'Segoe UI', Arial, sans-serif;
  color: #333;
  line-height: 1.6;
  overflow-x: hidden;
  scroll-behavior: smooth;
}

/* Header styles */
.custom-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: top 0.3s;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.logo-container {
  display: flex;
  align-items: center;
}

.site-title {
  font-size: 1.5rem;
  color: #409EFF;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-icon {
  position: relative;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-icon:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.icon {
  font-size: 24px;
  color: #409EFF;
}

.message-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 14px;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  z-index: 100; /* 确保消息标记显示在最上层 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); /* 添加阴影使其更明显 */
  border: 1px solid white; /* 添加白色边框使其更明显 */
}

.enterprise-name {
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.enterprise-name:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.enterprise-tag {
  display: inline-block;
  background-color: #ecf5ff;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.enterprise-tag:hover {
  background-color: #d9ecff;
}

.notice-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  font-weight: bold;
}

.welcome-text {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
}

/* Main container styles */
.main-container {
  display: flex;
  flex: 1;
}

/* Sidebar styles */
.sidebar-menu {
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  width: 200px;
  height: calc(100vh - 64px);
  position: fixed;
  top: 64px;
  left: 0;
  padding: 20px 0;
  z-index: 90;
  transition: top 0.3s ease;
  overflow-y: auto;
}

.menu-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  color: #606266;
}

.menu-item .icon {
  margin-right: 10px;
  font-size: 18px;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
  border-left: 3px solid #409EFF;
}

/* Main content styles */
.main-content {
  padding: 20px;
  flex: 1;
  margin-left: 200px; /* 与侧边栏宽度相同 */
  margin-top: 64px; /* 与导航栏高度相同 */
}

/* Welcome card styles */
.welcome-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-bottom: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  position: relative;
}

.welcome-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.welcome-text-container {
  flex: 1.2;
}

.welcome-text-container h1 {
  font-size: 2rem;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 600;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.welcome-text-container p {
  color: #606266;
  margin-bottom: 16px;
  font-size: 1.1rem;
}

.stats-container {
  display: flex;
  gap: 16px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 12px 16px;
  min-width: 120px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #ecf5ff;
}

.stat-icon {
  font-size: 24px;
  margin-right: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.welcome-image {
  flex: 0.8;
  display: flex;
  justify-content: flex-end;
}

.welcome-image img {
  max-width: 320px;
  max-height: 220px;
  object-fit: contain;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.welcome-image img:hover {
  transform: scale(1.05);
}

/* 网格布局样式 */
.content-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
  padding: 0 8px;
  /* Keep the workspace stationary when returning to this route. */
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.grid-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 主行样式 */
.main-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.main-row {
  position: relative;
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.main-row .left-column {
  flex: 1;
}

.main-row .right-column {
  display: flex;
  flex-direction: column;
  gap: 15px; /* 按要求设置公告和技能之间的间距为15px */
  width: 100%;
  position: relative;
}

.main-row .task-section {
  height: 100%;
  min-height: 500px;
}

.main-row .notice-section {
  flex: 1.5; /* 增加公告区域的比例 */
  min-height: 230px;
}

.main-row .skills-section {
  flex: 1; /* 减小技能区域的比例 */
  min-height: 200px;
  position: absolute;
  bottom: 0;
  width: 100%;
}

/* 增强的公告区域 */
.enhanced-notice-section {
  display: flex;
  flex-direction: column;
  position: relative;
}

.enhanced-notice-list {
  flex: 1;
  max-height: none;
  overflow-y: auto;
  padding-right: 5px;
}

.enhanced-notice-list::-webkit-scrollbar {
  width: 6px;
}

.enhanced-notice-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.enhanced-notice-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.enhanced-notice-list::-webkit-scrollbar-thumb:hover {
  background: #409EFF;
}

/* 紧凑的技能区域 */
.compact-skills-section {
  display: flex;
  flex-direction: column;
  position: relative;
}

.compact-skills-section .skills-content {
  transform: scale(1);
  margin: -10px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.compact-skills-section .skills-content canvas {
  transition: transform 0.3s ease;
}

.compact-skills-section:hover .skills-content canvas {
  transform: scale(1.05);
}

/* 内容行样式 */
.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-top: 10px; /* 技术挑战和任务之间的间距为10px */
  margin-bottom: 10px; /* 保持一致的间距 */
}

.fixed-height-card {
  height: 300px;
  overflow-y: auto;
}

/* 增强的项目合作区域 */
.enhanced-projects-section {
  height: 450px; /* 进一步增加项目合作区域的高度，确保内容显示完全 */
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #ccc #f1f1f1;
}

/* 增强的技术挑战区域 */
.enhanced-challenges-section {
  height: 450px; /* 与项目合作区域保持一致的高度 */
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #ccc #f1f1f1;
}

/* 统一高度的卡片 */
.uniform-height-card {
  min-height: 350px; /* 最小高度，可以通过调整大小增加 */
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #ccc #f1f1f1;
  margin-bottom: 24px; /* 增加卡片之间的间距，避免贴在一起 */
}

/* 自定义滚动条样式 */
.enhanced-projects-section::-webkit-scrollbar,
.enhanced-challenges-section::-webkit-scrollbar,
.uniform-height-card::-webkit-scrollbar {
  width: 6px;
}

.enhanced-projects-section::-webkit-scrollbar-track,
.enhanced-challenges-section::-webkit-scrollbar-track,
.uniform-height-card::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.enhanced-projects-section::-webkit-scrollbar-thumb,
.enhanced-challenges-section::-webkit-scrollbar-thumb,
.uniform-height-card::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.enhanced-projects-section::-webkit-scrollbar-thumb:hover,
.enhanced-challenges-section::-webkit-scrollbar-thumb:hover,
.uniform-height-card::-webkit-scrollbar-thumb:hover {
  background: #409EFF;
}

.compact-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
}

.grid-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
  padding: 20px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.grid-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.grid-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.grid-card:hover::after {
  opacity: 1;
}

/* 技能图谱样式 */
.skills-section {
  display: flex;
  flex-direction: column;
}

.skills-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  transform: scale(1.1);
}

/* 公告样式 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  overflow-y: auto;
}

.notice-item {
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #ebeef5;
  position: relative;
  overflow: hidden;
}

.notice-item:hover {
  background-color: #f0f9ff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #a0cfff;
}

.notice-item::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: linear-gradient(to bottom, #409EFF, #67C23A);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.notice-item:hover::after {
  opacity: 1;
}

.notice-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 8px;
  color: #303133;
}

.notice-content {
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
  line-height: 1.5;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}

/* 企业活动样式 */
.event-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #fafafa;
}

/* 学生主页上的企业活动样式调整 */
.events-section .event-item {
  margin-bottom: 6px;
  font-size: 0.9em;
}

.event-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.15);
  border-color: #a0cfff;
  background-color: #f0f9ff;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.event-title {
  font-weight: bold;
  color: #303133;
  flex: 1;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: bold;
  margin-left: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.event-details {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 卡片样式 */
.challenges-section, .projects-section, .competitions-section, .recommended-jobs-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.challenge-item, .project-item, .competition-item, .job-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #fafafa;
  height: 150px; /* 统一高度为150px */
  display: flex;
  flex-direction: column;
}

.challenge-item:hover, .project-item:hover, .competition-item:hover, .job-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.15);
  border-color: #a0cfff;
  background-color: #f0f9ff;
  animation: pulse 1s ease;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4); }
  70% { box-shadow: 0 0 0 5px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

.challenge-header, .project-header, .competition-header, .job-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.challenge-title, .project-title, .competition-title, .job-title {
  font-weight: bold;
  color: #303133;
  flex: 1;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.challenge-status, .project-status, .competition-status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: bold;
  margin-left: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.challenge-details, .project-details, .competition-details, .job-details {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  justify-content: space-between;
}

.challenge-details p, .project-details p, .competition-details p, .job-details p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 1.5;
  flex-wrap: wrap; /* 允许内容换行 */
  word-break: break-word; /* 允许长单词断行 */
}

.primary-tag {
  background-color: #f0f9eb;
  color: #67c23a;
}

.task-section, .notice-section, .skill-chart-section, .skills-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
  position: relative;
}

.card-header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.grid-card:hover .card-header::after {
  width: 80px;
}

.card-header h3, .card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 10px;
}

.card-header h3::before, .card-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background-color: #409EFF;
  border-radius: 2px;
}

.view-all-btn {
  background-color: transparent;
  color: #409EFF;
  border: none;
  cursor: pointer;
  font-size: 14px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.view-all-btn:hover {
  color: #66b1ff;
  background-color: rgba(64, 158, 255, 0.1);
}

.view-all-btn::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: #409EFF;
  transform: scaleX(0);
  transform-origin: right;
  transition: transform 0.3s ease;
}

.view-all-btn:hover::after {
  transform: scaleX(1);
  transform-origin: left;
}

/* Task styles */
.task-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
}

/* 分页控制样式 */
.pagination-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 15px;
  margin-bottom: 5px;
}

.pagination-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-btn:hover {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.pagination-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.pagination-info {
  margin: 0 10px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.task-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #d0e1fd;
  background-color: #f0f9ff;
}

.task-action-hint {
  color: #409EFF;
  font-size: 0.85rem;
  margin-top: 10px;
  text-align: center;
  padding: 5px;
  background-color: #ecf5ff;
  border-radius: 4px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-bottom: 1px solid #ebeef5;
}

.task-title {
  font-weight: bold;
  color: #303133;
  flex: 1;
}

.task-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.status-completed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-in-progress {
  background-color: #ecf5ff;
  color: #409EFF;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.task-details {
  padding: 15px;
}

.task-details p {
  display: flex;
  align-items: center;
  gap: 8px;
}

.enterprise-name {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.2s;
}

.enterprise-name:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.tag {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-width: 60px;
  text-align: center;
  transition: all 0.3s ease;
  margin-right: 8px;
}

.tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

.info-tag {
  background-color: #f0f9eb;
  color: #67c23a;
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.success-tag {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid rgba(64, 158, 255, 0.2);
}

.warning-tag {
  background-color: #fdf6ec;
  color: #e6a23c;
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.skills-section, .notice-section, .recommended-jobs-section {
  margin-bottom: 20px;
}

.primary-btn, .action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  padding: 8px 15px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.primary-btn:hover, .action-btn:hover {
  background-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.primary-btn:active, .action-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.primary-btn::before, .action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.7s;
}

.primary-btn:hover::before, .action-btn:hover::before {
  left: 100%;
}

.small-btn {
  font-size: 12px;
  padding: 6px 12px;
}

/* Notice styles */
.notice-item {
  padding: 10px 15px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-list {
  max-height: 200px;
  overflow-y: auto;
}

.skill-chart-container {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
}

.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notice-title {
  margin: 0;
  font-size: 1rem;
  color: #303133;
}

.date-tag {
  font-size: 12px;
  color: #909399;
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
}

.notice-content {
  color: #606266;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.5;
}

.no-data {
  text-align: center;
  padding: 30px 0;
  color: #909399;
}

/* Icon styles */
.icon-home::before { content: '🏠'; }
.icon-announcement::before { content: '📢'; }
.icon-task::before { content: '📋'; }
.icon-student::before { content: '👥'; }
.icon-job::before { content: '💼'; }
.icon-project::before { content: '🤝'; }
.icon-challenge::before { content: '🏆'; }
.icon-competition::before { content: '🏆'; }
.icon-event::before { content: '🎉'; }
.icon-forum::before { content: '💬'; }
.icon-training::before { content: '📚'; }

/* 评分提示卡片样式 */
.rating-tip-card {
  background-color: #fff8e1;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  display: flex;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #ffc107;
}

.tip-icon {
  font-size: 36px;
  margin-right: 20px;
  color: #ffc107;
  display: flex;
  align-items: center;
}

.tip-content {
  flex: 1;
}

.tip-content h3 {
  margin-top: 0;
  color: #303133;
  font-size: 18px;
}

.tip-content p {
  color: #606266;
  margin: 10px 0;
}

.tip-content ul {
  margin: 10px 0;
  padding-left: 20px;
}

.tip-content li {
  margin-bottom: 8px;
  color: #606266;
}

.tip-content strong {
  color: #303133;
}

/* 加载动画 */
@keyframes shimmer {
  0% {
    background-position: -468px 0;
  }
  100% {
    background-position: 468px 0;
  }
}

.loading-shimmer {
  animation-duration: 1.5s;
  animation-fill-mode: forwards;
  animation-iteration-count: infinite;
  animation-name: shimmer;
  animation-timing-function: linear;
  background: linear-gradient(to right, #f6f7f8 8%, #edeef1 18%, #f6f7f8 33%);
  background-size: 800px 104px;
  position: relative;
}

/* 页面过渡效果 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

/* 卡片悬停效果 */
.hover-lift {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.hover-lift:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Responsive styles */
@media (max-width: 768px) {
  .content-sections {
    grid-template-columns: 1fr;
  }

  .welcome-content {
    flex-direction: column;
  }

  .welcome-image {
    justify-content: center;
    margin-top: 20px;
  }

  .welcome-image img {
    max-width: 100%;
  }

  .sidebar-menu {
    width: 60px;
  }

  .menu-item span {
    display: none;
  }

  .stats-container {
    flex-direction: column;
    gap: 10px;
  }

  .content-row {
    grid-template-columns: 1fr;
  }

  .main-row {
    flex-direction: column;
  }

  .right-column {
    margin-top: 20px;
  }
}

/* 优化布局样式 */
.wider-task-section {
  flex: 1.6 !important; /* 更宽的任务区域 */
}

.narrower-right-column {
  flex: 0.8 !important; /* 更窄的右侧栏 */
}

/* 企业活动卡片样式 */
.shorter-event-card {
  min-height: 200px; /* 降低最小高度 */
}

.shorter-event-card .event-item {
  min-height: 60px; /* 降低最小高度 */
  overflow: hidden;
  margin-bottom: 8px; /* 减小间距 */
}

.shorter-event-card .event-header {
  padding: 8px 12px; /* 减小内边距 */
}

.shorter-event-card .event-details {
  padding: 8px 12px; /* 减小内边距 */
}

/* 编辑模式样式 */
.edit-mode-toggle {
  position: fixed;
  top: 70px;
  right: 20px;
  z-index: 1000;
  display: flex;
  gap: 10px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.edit-mode-toggle button {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 14px;
  font-weight: bold;
}

.edit-mode-toggle button:hover {
  background-color: #66b1ff;
}

.edit-mode-toggle button.active {
  background-color: #F56C6C;
}

.edit-mode-toggle .save-btn {
  background-color: #67C23A;
}

.edit-mode-toggle .save-btn:hover {
  background-color: #85ce61;
}

/* 可拖拽元素样式 */
.draggable {
  border: 2px dashed #409EFF !important;
  transition: all 0.3s ease;
  position: relative !important;
  overflow: visible !important; /* 确保手柄不被裁剪 */
  padding-top: 10px !important; /* 为拖拽手柄留出空间 */
  padding-bottom: 10px !important; /* 为调整手柄留出空间 */
  z-index: 1 !important; /* 确保元素层级正确 */
}

.draggable:hover {
  border-color: #66b1ff !important;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.5) !important;
}

/* 确保手柄始终在最上层 */
.draggable * {
  pointer-events: auto;
}

/* 手柄容器样式 */
.draggable .drag-handle,
.draggable .resize-handle {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 防止组件重叠 */
.grid-card {
  box-sizing: border-box !important;
}

/* 右侧栏组件样式 */
.right-column > .grid-card {
  margin-bottom: 24px !important;
}

/* 当组件被调整大小后的样式 */
.grid-card[style*="width"] {
  flex: none !important;
  box-sizing: border-box !important;
  margin: 10px !important;
  clear: both !important;
}

.draggable-element {
  transition: all 0.3s ease;
}

.draggable-element.dragging {
  opacity: 0.8;
  z-index: 100;
}

/* 拖拽手柄样式 */
.drag-handle {
  position: fixed !important; /* 改为fixed定位，确保始终在可视区域 */
  top: 0 !important;
  left: 0 !important;
  background-color: #409EFF !important;
  color: white !important;
  padding: 8px 12px !important;
  font-size: 14px !important;
  cursor: move !important;
  z-index: 99999 !important; /* 增加z-index值 */
  border-radius: 0 0 8px 0 !important;
  user-select: none !important;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3) !important;
  font-weight: bold !important;
  transition: all 0.2s ease !important;
  display: block !important;
  width: auto !important;
  height: auto !important;
  min-width: 40px !important;
  min-height: 20px !important;
  pointer-events: auto !important;
  text-align: center !important;
}

.drag-handle:hover {
  background-color: #66b1ff !important;
  transform: scale(1.1) !important;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.4) !important;
}

.drag-handle:active {
  background-color: #3a8ee6 !important;
  transform: scale(0.95) !important;
}

.drag-handle::before {
  content: '\2630' !important; /* 汉堡图标 */
  margin-right: 5px !important;
  font-size: 16px !important;
  display: inline-block !important;
}

/* 调整大小手柄样式 */
.resize-handle {
  position: fixed !important; /* 改为fixed定位，确保始终在可视区域 */
  bottom: 0 !important;
  right: 0 !important;
  background-color: #F56C6C !important;
  color: white !important;
  padding: 8px 12px !important;
  font-size: 14px !important;
  cursor: se-resize !important;
  z-index: 99999 !important; /* 增加z-index值 */
  border-radius: 8px 0 0 0 !important;
  user-select: none !important;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.3) !important;
  font-weight: bold !important;
  transition: all 0.2s ease !important;
  display: block !important;
  width: auto !important;
  height: auto !important;
  min-width: 40px !important;
  min-height: 20px !important;
  pointer-events: auto !important;
  text-align: center !important;
}

.resize-handle:hover {
  background-color: #f78989 !important;
  transform: scale(1.1) !important;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.4) !important;
}

.resize-handle:active {
  background-color: #dd6161 !important;
  transform: scale(0.95) !important;
}

.resize-handle::before {
  content: '\2922' !important; /* 调整大小图标 */
  margin-right: 5px !important;
  font-size: 16px !important;
  display: inline-block !important;
}

.resizable-element {
  transition: all 0.3s ease;
}

.resizable-element.resizing {
  opacity: 0.8;
  z-index: 100;
}

/* 编辑模式提示框样式 */
.edit-mode-tip {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.3);
  z-index: 2000;
  max-width: 400px;
  width: 90%;
  text-align: left;
  animation: editTipFadeIn 0.5s ease;
}

.edit-mode-tip h3 {
  color: #409EFF;
  margin-top: 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.edit-mode-tip ul {
  padding-left: 20px;
}

.edit-mode-tip li {
  margin-bottom: 10px;
  line-height: 1.5;
}

.edit-mode-tip strong {
  font-weight: bold;
}

.edit-mode-tip .close-tip {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: block;
  margin: 15px auto 0;
  font-weight: bold;
  transition: background-color 0.3s;
}

.edit-mode-tip .close-tip:hover {
  background-color: #66b1ff;
}

@keyframes editTipFadeIn {
  from { opacity: 0; transform: translate(-50%, -60%); }
  to { opacity: 1; transform: translate(-50%, -50%); }
}

/* 保存成功提示样式 */
.saved-tip {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #67C23A;
  color: white;
  padding: 10px 20px;
  border-radius: 4px;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  z-index: 2000;
  animation: fadeInDown 0.5s ease, fadeOut 0.5s ease 2.5s;
}

@keyframes fadeInDown {
  from { opacity: 0; transform: translate(-50%, -20px); }
  to { opacity: 1; transform: translateX(-50%); }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

/* 手柄容器样式 */
#floating-handles-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 999999;
}

/* 确保所有浮动手柄始终在最上层 */
.floating-handle {
  position: fixed !important;
  z-index: 999999 !important;
  pointer-events: auto !important;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5) !important;
}
</style>

