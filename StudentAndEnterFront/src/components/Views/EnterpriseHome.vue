<template>
  <div class="enterprise-home-container">
    <!-- 顶部导航栏 -->
    <div class="top-header">
      <div class="logo-container">
        <h2 class="logo">学生企业交流平台</h2>
      </div>
      <div class="user-info">
        <div class="message-icon" role="button" tabindex="0" aria-label="查看消息" @keydown.enter="viewMessages" @keydown.space.prevent="viewMessages" @click="viewMessages">
          <span class="icon">💬</span>
          <span v-show="unreadMessages > 0" class="message-badge">{{ unreadMessages }}</span>
        </div>
        <div class="user-profile" @click="viewProfile" style="cursor: pointer; display: flex; align-items: center;">
          <div class="user-avatar">
            {{ enterprise.name ? enterprise.name.charAt(0) : 'E' }}
          </div>
          <div class="user-details">
            <span class="user-name">{{ enterprise.name }}</span>
            <span class="user-role">企业用户</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 左侧导航菜单 -->
      <div class="sidebar">
        <div class="menu-item active">
          <span>主页</span>
        </div>
        <div class="nav-group-label">管理与发布</div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToAnnouncementPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>发布公告</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToTaskPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>发布任务</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToStudentsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>学生管理</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToJobsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>岗位管理</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToJobApplicationsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>岗位申请</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToProjectsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>项目合作</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToChallengesPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>技术挑战</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToCompetitionsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>企业竞赛</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToEventsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>企业活动</span>
        </div>
        <div class="nav-group-label">交流与资源</div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToForumPage">
          <span>交流论坛</span>
        </div>
        <div class="menu-item" role="button" tabindex="0" @keydown.enter="$event.currentTarget.click()" @keydown.space.prevent="$event.currentTarget.click()" @click="goToTrainingMaterialsPage" :class="{ 'disabled': verificationStatus !== 'APPROVED' }">
          <span>培训资料</span>
        </div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="content">
        <!-- 验证状态卡片 -->
        <div v-if="verificationStatus !== 'APPROVED'" class="verification-card" :class="{'warning': verificationStatus === 'PENDING', 'error': verificationStatus === 'REJECTED' || !verificationStatus}">
          <div class="verification-icon">⚠️</div>
          <div class="verification-content">
            <h3>企业身份验证</h3>
            <p v-if="verificationStatus === 'PENDING'">您的企业身份验证正在审核中，请耐心等待。审核期间无法使用企业功能。</p>
            <p v-else-if="verificationStatus === 'REJECTED'">您的企业身份验证未通过，请重新提交验证材料。验证通过前无法使用企业功能。</p>
            <p v-else>请提交企业身份验证材料，完成验证后才能使用全部功能。在验证通过前，您无法发布公告、发布任务或管理学生。</p>
          </div>
          <button class="verification-btn" @click="goToVerificationPage">立即验证</button>
        </div>

        <!-- 欢迎卡片 -->
        <div class="welcome-card">
          <div class="welcome-text">
            <h1>欢迎回来，{{ enterprise.name }}</h1>
            <p>今天是 {{ currentDate }}，继续管理您的企业任务和公告吧！</p>
          </div>
          <div class="overview-section compact-overview">
            <div class="overview-header">
              <h2>总览</h2>
            </div>
            <div class="overview-content-wrapper">
              <div class="overview-content">
              <div class="overview-item" @click="setActiveFeature('default')" :class="{ 'active': activeFeature === 'default' }">
                <div class="overview-icon">🏛️</div>
                <div class="overview-label">默认界面</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('students')" :class="{ 'active': activeFeature === 'students' }">
                <div class="overview-icon">👤</div>
                <div class="overview-label">关注学生</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('tasks')" :class="{ 'active': activeFeature === 'tasks' }">
                <div class="overview-icon">📝</div>
                <div class="overview-label">发布任务</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('announcements')" :class="{ 'active': activeFeature === 'announcements' }">
                <div class="overview-icon">📢</div>
                <div class="overview-label">公告信息</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('projects')" :class="{ 'active': activeFeature === 'projects' }">
                <div class="overview-icon">🤝</div>
                <div class="overview-label">项目合作</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('challenges')" :class="{ 'active': activeFeature === 'challenges' }">
                <div class="overview-icon">💡</div>
                <div class="overview-label">技术挑战</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('competitions')" :class="{ 'active': activeFeature === 'competitions' }">
                <div class="overview-icon">🏆</div>
                <div class="overview-label">企业竞赛</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('events')" :class="{ 'active': activeFeature === 'events' }">
                <div class="overview-icon">🎉</div>
                <div class="overview-label">企业活动</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('training')" :class="{ 'active': activeFeature === 'training' }">
                <div class="overview-icon">📚</div>
                <div class="overview-label">培训资料</div>
              </div>
              <div class="overview-item" @click="setActiveFeature('jobs')" :class="{ 'active': activeFeature === 'jobs' }">
                <div class="overview-icon">💼</div>
                <div class="overview-label">发布岗位</div>
              </div>
              </div>
            </div>
          </div>

        </div>

        <!-- 内容区域 -->
        <div class="content-grid">
          <!-- 默认界面 -->
          <div v-if="activeFeature === 'default'" class="default-view">
            <div class="default-content">
              <div class="welcome-message">
                <h2>欢迎使用学生企业合作平台</h2>
                <p>请从左侧导航或上方总览选择您需要的功能</p>
              </div>
              <div class="stats-overview">
                <div class="stat-card">
                  <div class="stat-icon">👤</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ students.length }}</div>
                    <div class="stat-label">关注学生</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">📝</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ tasks.length }}</div>
                    <div class="stat-label">发布任务</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">📢</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ announcements.length }}</div>
                    <div class="stat-label">公告信息</div>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon">🤝</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ projects.length }}</div>
                    <div class="stat-label">项目合作</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- 关注的学生 -->
          <div v-if="activeFeature === 'students' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'students'}">
            <div class="card-header">
              <h2>关注的学生</h2>
              <button class="view-all-btn" @click="goToFavoritedStudentsPage">查看全部</button>
            </div>
            <div v-if="students.length === 0" class="empty-state">
              <div class="empty-icon">👤</div>
              <p>您还没有关注学生</p>
              <button class="action-btn" @click="goToFavoritedStudentsPage">浏览学生</button>
            </div>
            <div v-else class="student-list">
              <div v-for="student in displayedStudents" :key="student.studentId" class="student-card" @click="viewStudentDetails(student.studentId)">
                <div class="student-avatar">{{ student.name ? student.name.charAt(0) : 'S' }}</div>
                <div class="student-info">
                  <h3 class="student-name">{{ student.name }}</h3>
                  <p class="student-id">学号: {{ student.studentId }}</p>
                </div>
                <div class="student-actions">
                  <button class="action-btn small" @click.stop="viewStudentDetails(student.studentId)">查看</button>
                </div>
              </div>
              <div v-if="students.length > studentsPerPage" class="pagination">
                <button class="page-btn" @click="prevStudentPage" :disabled="studentPage === 0">&lt;</button>
                <span>{{ studentPage + 1 }} / {{ Math.ceil(students.length / studentsPerPage) }}</span>
                <button class="page-btn" @click="nextStudentPage" :disabled="studentPage >= Math.ceil(students.length / studentsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 我发布的任务 -->
          <div v-if="activeFeature === 'tasks' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'tasks'}">
            <div class="card-header">
              <h2>我发布的任务</h2>
              <button class="view-all-btn" @click="goToEnterpriseAllTasksPage">查看全部</button>
            </div>
            <div v-if="tasks.length === 0" class="empty-state">
              <div class="empty-icon">📃</div>
              <p>您还没有发布任务</p>
              <button class="action-btn" @click="goToTaskPublishPage">发布任务</button>
            </div>
            <div v-else class="task-list">
              <div v-for="task in displayedTasks" :key="task.taskId" class="task-card">
                <div class="task-header">
                  <h3 class="task-title">{{ task.summary || task.title }}</h3>
                  <span class="task-date">{{ formatDate(task.createTime) }}</span>
                </div>
                <div class="task-content">
                  <p>{{ truncateText(task.content || task.requirements, 100) }}</p>
                </div>
                <div class="task-stats">
                  <div class="stat-item">
                    <span class="stat-label">接取人数:</span>
                    <span class="stat-value">{{ task.assignmentCount || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">待审核:</span>
                    <span class="stat-value">{{ task.submittedCount || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">已通过:</span>
                    <span class="stat-value">{{ task.completedCount || 0 }}</span>
                  </div>
                </div>
                <div class="task-footer">
                  <span class="task-tech">{{ task.techStack || '技术栈未指定' }}</span>
                  <div class="task-actions">
                    <button class="action-btn small" @click.stop="viewTaskDetails(task.taskId)">详情</button>
                    <button class="action-btn small" @click.stop="viewCompletionDetails(task.taskId)">提交情况</button>
                  </div>
                </div>
              </div>
              <div v-if="tasks.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevTaskPage" :disabled="taskPage === 0">&lt;</button>
                <span>{{ taskPage + 1 }} / {{ Math.ceil(tasks.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextTaskPage" :disabled="taskPage >= Math.ceil(tasks.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 已发布公告 -->
          <div v-if="activeFeature === 'announcements' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'announcements'}">
            <div class="card-header">
              <h2>已发布公告</h2>
              <button class="view-all-btn" @click="goToEnterpriseAllAnnouncementsPage">查看全部</button>
            </div>
            <div v-if="announcements.length === 0" class="empty-state">
              <div class="empty-icon">📢</div>
              <p>您还没有发布公告</p>
              <button class="action-btn" @click="goToAnnouncementPage">发布公告</button>
            </div>
            <div v-else class="announcement-list">
              <div v-for="notice in displayedAnnouncements" :key="notice.noticeId" class="announcement-card">
                <div class="announcement-header">
                  <h3 class="announcement-title">{{ notice.title }}</h3>
                  <span class="announcement-date">{{ formatDate(notice.createdTime || notice.createTime) }}</span>
                </div>
                <div class="announcement-content">
                  <p>{{ truncateText(notice.content, 150) }}</p>
                </div>
                <div class="announcement-footer">
                  <span class="announcement-status" :class="getStatusClass(notice.status)">
                    {{ getStatusText(notice.status) }}
                  </span>
                  <button class="action-btn small" @click.stop="viewNoticeDetails(notice.noticeId)">查看详情</button>
                </div>
              </div>
              <div v-if="announcements.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevAnnouncementPage" :disabled="announcementPage === 0">&lt;</button>
                <span>{{ announcementPage + 1 }} / {{ Math.ceil(announcements.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextAnnouncementPage" :disabled="announcementPage >= Math.ceil(announcements.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 发布的岗位 -->
          <div v-if="activeFeature === 'jobs' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'jobs'}">
            <div class="card-header">
              <h2>发布的岗位</h2>
              <button class="view-all-btn" @click="goToJobsPage">查看全部</button>
            </div>
            <div v-if="jobs.length === 0" class="empty-state">
              <div class="empty-icon">💼</div>
              <p>您还没有发布岗位</p>
              <button class="action-btn" @click="goToJobsPage">发布岗位</button>
            </div>
            <div v-else class="job-list">
              <div v-for="job in displayedJobs" :key="job.jobId" class="job-card">
                <div class="job-header">
                  <h3 class="job-title">{{ job.title }}</h3>
                  <span class="job-salary">{{ job.salaryRange || '薪资面议' }}</span>
                </div>
                <div class="job-content">
                  <p class="job-tech"><span class="label">技术栈:</span> {{ job.technologyStack || '未指定' }}</p>
                  <p class="job-education"><span class="label">学历要求:</span> {{ job.educationRequirement || '不限' }}</p>
                  <p class="job-location"><span class="label">工作地点:</span> {{ job.city || '未指定' }}</p>
                </div>
                <div class="job-footer">
                  <span class="job-date">{{ formatDate(job.createTime) }}</span>
                  <div class="job-actions">
                    <button class="action-btn small" @click.stop="viewJobDetails(job.jobId)">详情</button>
                    <button class="action-btn small" @click.stop="viewJobApplications(job.jobId)">申请情况</button>
                  </div>
                </div>
              </div>
              <div v-if="jobs.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevJobPage" :disabled="jobPage === 0">&lt;</button>
                <span>{{ jobPage + 1 }} / {{ Math.ceil(jobs.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextJobPage" :disabled="jobPage >= Math.ceil(jobs.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 项目合作 -->
          <div v-if="activeFeature === 'projects' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'projects'}">
            <div class="card-header">
              <h2>项目合作</h2>
              <button class="view-all-btn" @click="goToProjectsPage">查看全部</button>
            </div>
            <div v-if="projects.length === 0" class="empty-state">
              <div class="empty-icon">🤝</div>
              <p>您还没有发布项目合作</p>
              <button class="action-btn" @click="goToProjectsPage">发布项目</button>
            </div>
            <div v-else class="project-list">
              <div v-for="project in displayedProjects" :key="project.projectId" class="project-card">
                <div class="project-header">
                  <h3 class="project-title">{{ project.title }}</h3>
                  <span class="project-status" :class="getProjectStatusClass(project.status)">{{ getProjectStatusText(project.status) }}</span>
                </div>
                <div class="project-content">
                  <p>{{ truncateText(project.description, 80) }}</p>
                </div>
                <div class="project-details">
                  <p class="project-type"><span class="label">类型:</span> {{ getProjectTypeText(project.projectType || project.type) }}</p>
                  <p class="project-deadline"><span class="label">截止日期:</span> {{ formatDate(project.deadline || project.endDate) }}</p>
                </div>
                <div class="project-footer">
                  <div class="project-actions">
                    <button class="action-btn small" @click.stop="viewProjectDetails(project.projectId)">详情</button>
                    <button class="action-btn small" @click.stop="viewProjectApplications(project.projectId)">申请情况</button>
                  </div>
                </div>
              </div>
              <div v-if="projects.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevProjectPage" :disabled="projectPage === 0">&lt;</button>
                <span>{{ projectPage + 1 }} / {{ Math.ceil(projects.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextProjectPage" :disabled="projectPage >= Math.ceil(projects.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 技术挑战 -->
          <div v-if="activeFeature === 'challenges' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'challenges'}">
            <div class="card-header">
              <h2>技术挑战</h2>
              <button class="view-all-btn" @click="goToChallengesPage">查看全部</button>
            </div>
            <div v-if="challenges.length === 0" class="empty-state">
              <div class="empty-icon">💡</div>
              <p>您还没有发布技术挑战</p>
              <button class="action-btn" @click="goToChallengesPage">发布挑战</button>
            </div>
            <div v-else class="challenge-list">
              <div v-for="challenge in displayedChallenges" :key="challenge.challengeId" class="challenge-card">
                <div class="challenge-header">
                  <h3 class="challenge-title">{{ challenge.title }}</h3>
                  <span class="challenge-status" :class="getChallengeStatusClass(challenge.status)">{{ getChallengeStatusText(challenge.status) }}</span>
                </div>
                <div class="challenge-content">
                  <p>{{ truncateText(challenge.description, 80) }}</p>
                </div>
                <div class="challenge-stats">
                  <div class="stat-item">
                    <span class="stat-label">难度:</span>
                    <span class="stat-value">{{ getDifficultyText(challenge.difficulty) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">截止日期:</span>
                    <span class="stat-value">{{ formatDate(challenge.deadline) }}</span>
                  </div>
                </div>
                <div class="challenge-footer">
                  <div class="challenge-actions">
                    <button class="action-btn small" @click.stop="viewChallengeDetails(challenge.challengeId)">详情</button>
                    <button class="action-btn small" @click.stop="viewChallengeSubmissions(challenge.challengeId)">提交情况</button>
                  </div>
                </div>
              </div>
              <div v-if="challenges.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevChallengePage" :disabled="challengePage === 0">&lt;</button>
                <span>{{ challengePage + 1 }} / {{ Math.ceil(challenges.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextChallengePage" :disabled="challengePage >= Math.ceil(challenges.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 企业竞赛 -->
          <div v-if="activeFeature === 'competitions' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'competitions'}">
            <div class="card-header">
              <h2>企业竞赛</h2>
              <button class="view-all-btn" @click="goToCompetitionsPage">查看全部</button>
            </div>
            <div v-if="competitions.length === 0" class="empty-state">
              <div class="empty-icon">🏆</div>
              <p>您还没有发布企业竞赛</p>
              <button class="action-btn" @click="goToCompetitionsPage">发布竞赛</button>
            </div>
            <div v-else class="competition-list">
              <div v-for="competition in displayedCompetitions" :key="competition.competitionId" class="competition-card">
                <div class="competition-header">
                  <h3 class="competition-title">{{ competition.title }}</h3>
                  <span class="competition-status" :class="getCompetitionStatusClass(competition.status)">{{ getCompetitionStatusText(competition.status) }}</span>
                </div>
                <div class="competition-content">
                  <p>{{ truncateText(competition.description, 80) }}</p>
                </div>
                <div class="competition-stats">
                  <div class="stat-item">
                    <span class="stat-label">开始日期:</span>
                    <span class="stat-value">{{ formatDate(competition.startDate) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">截止日期:</span>
                    <span class="stat-value">{{ formatDate(competition.endDate) }}</span>
                  </div>
                </div>
                <div class="competition-footer">
                  <div class="competition-actions">
                    <button class="action-btn small" @click.stop="viewCompetitionDetails(competition.competitionId)">详情</button>
                    <button class="action-btn small" @click.stop="viewCompetitionSubmissions(competition.competitionId)">提交情况</button>
                  </div>
                </div>
              </div>
              <div v-if="competitions.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevCompetitionPage" :disabled="competitionPage === 0">&lt;</button>
                <span>{{ competitionPage + 1 }} / {{ Math.ceil(competitions.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextCompetitionPage" :disabled="competitionPage >= Math.ceil(competitions.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 企业活动 -->
          <div v-if="activeFeature === 'events' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'events'}">
            <div class="card-header">
              <h2>企业活动</h2>
              <button class="view-all-btn" @click="goToEventsPage">查看全部</button>
            </div>
            <div v-if="events.length === 0" class="empty-state">
              <div class="empty-icon">🎉</div>
              <p>您还没有发布企业活动</p>
              <button class="action-btn" @click="goToEventsPage">发布活动</button>
            </div>
            <div v-else class="event-list">
              <div v-for="event in displayedEvents" :key="event.eventId" class="event-card">
                <div class="event-header">
                  <h3 class="event-title">{{ event.title }}</h3>
                  <span class="event-status" :class="getEventStatusClass(event.status)">{{ getEventStatusText(event.status) }}</span>
                </div>
                <div class="event-content">
                  <p>{{ truncateText(event.description, 80) }}</p>
                </div>
                <div class="event-stats">
                  <div class="stat-item">
                    <span class="stat-label">活动时间:</span>
                    <span class="stat-value">{{ formatDate(event.eventDate) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">活动地点:</span>
                    <span class="stat-value">{{ event.location || '未指定' }}</span>
                  </div>
                </div>
                <div class="event-footer">
                  <div class="event-actions">
                    <button class="action-btn small" @click.stop="viewEventDetails(event.eventId)">详情</button>
                    <button class="action-btn small" @click.stop="viewEventRegistrations(event.eventId)">报名情况</button>
                  </div>
                </div>
              </div>
              <div v-if="events.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevEventPage" :disabled="eventPage === 0">&lt;</button>
                <span>{{ eventPage + 1 }} / {{ Math.ceil(events.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextEventPage" :disabled="eventPage >= Math.ceil(events.length / itemsPerPage) - 1">&gt;</button>
              </div>
            </div>
          </div>

          <!-- 培训资料 -->
          <div v-if="activeFeature === 'training' || activeFeature === 'default'" class="content-card" :class="{'full-width': activeFeature === 'training'}">
            <div class="card-header">
              <h2>培训资料</h2>
              <button class="view-all-btn" @click="goToTrainingMaterialsPage">查看全部</button>
            </div>
            <div v-if="trainingMaterials.length === 0" class="empty-state">
              <div class="empty-icon">📚</div>
              <p>您还没有上传培训资料</p>
              <button class="action-btn" @click="goToTrainingMaterialsPage">上传资料</button>
            </div>
            <div v-else class="training-list">
              <div v-for="material in displayedTrainingMaterials" :key="material.materialId" class="training-card">
                <div class="training-header">
                  <h3 class="training-title">{{ material.title }}</h3>
                  <span class="training-type">{{ material.materialType || material.category || '未分类' }}</span>
                </div>
                <div class="training-content">
                  <p>{{ truncateText(material.description, 80) }}</p>
                </div>
                <div class="training-stats">
                  <div class="stat-item">
                    <span class="stat-label">章节数:</span>
                    <span class="stat-value">{{ material.chapters ? material.chapters.length : (material.chapterCount || 0) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">上传时间:</span>
                    <span class="stat-value">{{ formatDate(material.createTime) }}</span>
                  </div>
                </div>
                <div class="training-footer">
                  <div class="training-actions">
                    <button class="action-btn small" @click.stop="viewTrainingDetails(material.materialId)">详情</button>
                    <button class="action-btn small" @click.stop="editTrainingMaterial(material.materialId)">编辑</button>
                  </div>
                </div>
              </div>
              <div v-if="trainingMaterials.length > itemsPerPage" class="pagination">
                <button class="page-btn" @click="prevTrainingPage" :disabled="trainingPage === 0">&lt;</button>
                <span>{{ trainingPage + 1 }} / {{ Math.ceil(trainingMaterials.length / itemsPerPage) }}</span>
                <button class="page-btn" @click="nextTrainingPage" :disabled="trainingPage >= Math.ceil(trainingMaterials.length / itemsPerPage) - 1">&gt;</button>
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

export default {
  data() {
    return {
      enterprise: {},
      students: [],
      tasks: [],
      announcements: [],
      projects: [],
      challenges: [],
      jobs: [],
      competitions: [],
      events: [],
      trainingMaterials: [],
      currentDate: '',
      // 当前激活的功能
      activeFeature: 'default',
      // 分页相关
      studentPage: 0,
      taskPage: 0,
      announcementPage: 0,
      projectPage: 0,
      jobPage: 0,
      challengePage: 0,
      competitionPage: 0,
      eventPage: 0,
      trainingPage: 0,
      studentsPerPage: 5,
      itemsPerPage: 2,
      // 消息相关
      unreadMessages: 0,
      messageCheckInterval: null,
      // 验证相关
      verificationStatus: null
    };
  },
  computed: {
    // 当前页的学生
    displayedStudents() {
      const start = this.studentPage * this.studentsPerPage;
      const end = start + this.studentsPerPage;
      return this.students.slice(start, end);
    },
    // 当前页的任务
    displayedTasks() {
      const start = this.taskPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.tasks.slice(start, end);
    },
    // 当前页的公告
    displayedAnnouncements() {
      const start = this.announcementPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.announcements.slice(start, end);
    },
    // 当前页的岗位
    displayedJobs() {
      const start = this.jobPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.jobs.slice(start, end);
    },
    // 当前页的项目
    displayedProjects() {
      const start = this.projectPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.projects.slice(start, end);
    },
    // 当前页的技术挑战
    displayedChallenges() {
      const start = this.challengePage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.challenges.slice(start, end);
    },
    // 当前页的企业竞赛
    displayedCompetitions() {
      const start = this.competitionPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.competitions.slice(start, end);
    },
    // 当前页的企业活动
    displayedEvents() {
      const start = this.eventPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.events.slice(start, end);
    },
    // 当前页的培训资料
    displayedTrainingMaterials() {
      const start = this.trainingPage * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.trainingMaterials.slice(start, end);
    }
  },
  created() {
    this.currentDate = this.formatCurrentDate();
    this.fetchEnterpriseInfo();
    this.fetchStudents();
    this.fetchTasks();
    this.fetchAnnouncements();
    this.fetchProjects();
    this.fetchChallenges();
    this.fetchJobs();
    this.fetchCompetitions();
    this.fetchEvents();
    this.fetchTrainingMaterials();
    this.checkVerificationStatus();

    // 获取未读消息数量
    this.fetchUnreadMessages();

    // 设置定时检查未读消息
    try {
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
      }, 10000); // 每10秒检查一次，确保及时显示最新的未读消息
    } catch (error) {
      console.error('创建消息检查定时器失败:', error);
    }
  },

  beforeUnmount() {
    // 清除定时器
    if (this.messageCheckInterval) {
      clearInterval(this.messageCheckInterval);
    }
  },
  methods: {
    fetchEnterpriseInfo() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}`)
        .then(response => {
          this.enterprise = response.data;
        })
        .catch(error => {
          console.error('无法获取企业信息:', error);
        });
    },
    fetchStudents() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}/favorites`)
        .then(response => {
          this.students = response.data;
        })
        .catch(error => {
          console.error('无法获取关注的学生:', error);
        });
    },
    fetchTasks() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/tasks/enterprise/${enterpriseId}`)
        .then(response => {
          this.tasks = response.data;
          // 获取每个任务的提交统计信息
          this.tasks.forEach(task => {
            this.fetchTaskStats(task.taskId);
          });
        })
        .catch(error => {
          console.error('无法获取任务列表:', error);
        });
    },
    fetchAnnouncements() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}/announcements`)
        .then(response => {
          this.announcements = response.data;
        })
        .catch(error => {
          console.error('无法获取公告:', error);
        });
    },
    // 获取企业的项目合作
    fetchProjects() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/projects/enterprise/${enterpriseId}`)
        .then(response => {
          this.projects = response.data;
        })
        .catch(error => {
          console.error('无法获取项目合作:', error);
        });
    },
    // 获取企业的技术挑战
    fetchChallenges() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/challenges/enterprise/${enterpriseId}`)
        .then(response => {
          this.challenges = response.data;
        })
        .catch(error => {
          console.error('无法获取技术挑战:', error);
          this.challenges = [];
        });
    },

    // 获取岗位数据
    fetchJobs() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/jobs/enterprise/${enterpriseId}`)
        .then(response => {
          this.jobs = response.data;
          // 处理岗位数据中的薪资和技术栈
          this.jobs.forEach(job => {
            // 如果没有salaryRange字段，但有minSalary和maxSalary，则生成salaryRange
            if (!job.salaryRange && (job.minSalary || job.maxSalary)) {
              job.salaryRange = this.formatSalaryRange(job.minSalary, job.maxSalary);
            }
            // 如果没有technologyStack字段，但有techStack，则使用techStack
            if (!job.technologyStack && job.techStack) {
              job.technologyStack = job.techStack;
            }
            // 如果没有educationRequirement字段，但有education，则使用education
            if (!job.educationRequirement && job.education) {
              job.educationRequirement = job.education;
            }
            // 如果没有city字段，但有location，则使用location
            if (!job.city && job.location) {
              job.city = job.location;
            }
          });
        })
        .catch(error => {
          console.error('无法获取岗位数据:', error);
          this.jobs = [];
        });
    },

    // 获取竞赛数据
    fetchCompetitions() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/competitions/enterprise/${enterpriseId}`)
        .then(response => {
          this.competitions = response.data || [];
        })
        .catch(error => {
          console.error('无法获取竞赛数据:', error);
          this.competitions = [];
        });
    },

    // 获取活动数据
    fetchEvents() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/events/enterprise/${enterpriseId}`)
        .then(response => {
          this.events = response.data || [];
        })
        .catch(error => {
          console.error('无法获取活动数据:', error);
          this.events = [];
        });
    },

    // 获取培训资料数据
    fetchTrainingMaterials() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/training/enterprise/${enterpriseId}`)
        .then(response => {
          this.trainingMaterials = response.data || [];
          // 处理培训资料数据
          this.trainingMaterials.forEach(material => {
            // 处理分类信息
            if (!material.materialType && !material.category) {
              material.category = '未分类';
            }

            // 处理章节数信息
            if (!material.chapters && !material.chapterCount && material.chapterList) {
              material.chapters = material.chapterList;
            }

            // 确保创建时间字段存在
            if (!material.createTime && material.createdTime) {
              material.createTime = material.createdTime;
            }
          });
        })
        .catch(error => {
          console.error('无法获取培训资料数据:', error);
          this.trainingMaterials = [];
        });
    },

    // 设置当前激活的功能
    setActiveFeature(feature) {
      // 如果点击的是当前激活的功能，则切换回默认界面
      if (this.activeFeature === feature && feature !== 'default') {
        this.activeFeature = 'default';
      } else {
        this.activeFeature = feature;
      }

      // 根据选择的功能显示相应的内容
      switch(feature) {
        case 'students':
          this.fetchStudents();
          break;
        case 'tasks':
          this.fetchTasks();
          break;
        case 'announcements':
          this.fetchAnnouncements();
          break;
        case 'projects':
          this.fetchProjects();
          break;
        case 'challenges':
          this.fetchChallenges();
          break;
        case 'jobs':
          this.fetchJobs();
          break;
        case 'competitions':
          this.fetchCompetitions();
          break;
        case 'events':
          this.fetchEvents();
          break;
        case 'training':
          this.fetchTrainingMaterials();
          break;
      }

      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },



    // 查看岗位详情
    viewJobDetails(jobId) {
      this.$router.push({ name: 'JobDetail', params: { jobId } });
    },

    // 查看岗位申请情况
    viewJobApplications(jobId) {
      this.$router.push({
        name: 'enterprise-job-applications',
        query: { jobId }
      });
    },

    // 查看项目详情
    viewProjectDetails(projectId) {
      this.$router.push({ name: 'ProjectDetail', params: { projectId } });
    },

    // 查看项目申请情况
    viewProjectApplications(projectId) {
      this.$router.push({ name: 'ProjectApplications', params: { projectId } });
    },

    // 查看技术挑战详情
    viewChallengeDetails(challengeId) {
      this.$router.push({ name: 'ChallengeDetail', params: { challengeId } });
    },

    // 查看技术挑战提交情况
    viewChallengeSubmissions(challengeId) {
      this.$router.push({
        name: 'ChallengeDetail',
        params: { challengeId },
        query: { tab: 'submissions' }
      });
    },

    // 查看竞赛详情
    viewCompetitionDetails(competitionId) {
      this.$router.push({ name: 'CompetitionDetail', params: { competitionId } });
    },

    // 查看竞赛提交情况
    viewCompetitionSubmissions(competitionId) {
      this.$router.push({
        name: 'CompetitionDetail',
        params: { competitionId },
        query: { tab: 'submissions' }
      });
    },

    // 查看活动详情
    viewEventDetails(eventId) {
      this.$router.push({ name: 'EventDetail', params: { eventId } });
    },

    // 查看活动报名情况
    viewEventRegistrations(eventId) {
      this.$router.push({
        name: 'EventDetail',
        params: { eventId },
        query: { tab: 'registrations' }
      });
    },


    // 查看学生详情
    viewStudentDetails(studentId) {
      // 企业查看学生详情，直接使用StudentDetail组件
      this.$router.push({ name: 'StudentDetail', params: { studentId } });
    },
    // 查看任务详情
    viewTaskDetails(taskId) {
      this.$router.push({ name: 'TaskDetailForEnter', params: { taskId } });
    },
    // 查看任务完成情况
    viewCompletionDetails(taskId) {
      this.$router.push({ name: 'TaskSubmissions', params: { taskId } });
    },
    // 获取任务的提交统计信息
    fetchTaskStats(taskId) {
      axios.get(`http://localhost:8080/api/tasks/${taskId}/stats`)
        .then(response => {
          const stats = response.data;
          const taskIndex = this.tasks.findIndex(t => t.taskId === taskId);
          if (taskIndex !== -1) {
            this.tasks[taskIndex] = {
              ...this.tasks[taskIndex],
              assignmentCount: stats.totalCount || 0,
              submittedCount: stats.submittedCount || 0,
              completedCount: stats.completedCount || 0
            };
          }
        })
        .catch(error => {
          console.error(`无法获取任务 ${taskId} 的统计信息:`, error);
        });
    },
    // 查看公告详情
    viewNoticeDetails(noticeId) {
      // 如果有公告详情页面，跳转到该页面
      // this.$router.push({ name: 'NoticeDetail', params: { noticeId } });
      alert('公告详情功能开发中');
    },
    // 跳转到公告发布页面
    goToAnnouncementPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法发布公告。');
        return;
      }
      this.$router.push({ name: 'enterprise-notice-pub' });
    },
    // 跳转到任务发布页面
    goToTaskPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法发布任务。');
        return;
      }
      this.$router.push({ name: 'TaskPublish' });
    },
    // 跳转到任务发布页面
    goToTaskPublishPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法发布任务。');
        return;
      }
      this.$router.push({ name: 'TaskPublish' });
    },
    // 跳转到岗位管理页面
    goToJobsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法管理岗位。');
        return;
      }
      this.$router.push({ name: 'enterprise-jobs' });
    },

    // 跳转到岗位申请页面
    goToJobApplicationsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法查看岗位申请。');
        return;
      }
      this.$router.push({ name: 'enterprise-job-applications' });
    },
    // 跳转到项目合作页面
    goToProjectsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法管理项目合作。');
        return;
      }
      this.$router.push({ name: 'EnterpriseProjects' });
    },
    // 跳转到技术挑战页面
    goToChallengesPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法管理技术挑战。');
        return;
      }
      this.$router.push({ name: 'EnterpriseChallenges' });
    },
    // 跳转到竞赛页面
    goToCompetitionsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法发布竞赛。');
        return;
      }
      this.$router.push({ name: 'EnterpriseCompetitions' });
    },
    // 跳转到企业活动页面
    goToEventsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法发布活动。');
        return;
      }
      this.$router.push({ name: 'EnterpriseEvents' });
    },
    // 跳转到论坛页面
    goToForumPage() {
      const enterpriseId = localStorage.getItem('userId');
      this.$router.push({
        name: 'ForumHome',
        query: {
          userId: enterpriseId,
          userType: 'ENTERPRISE'
        }
      });
    },
    // 跳转到培训资料页面
    goToTrainingMaterialsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法管理培训资料。');
        return;
      }
      this.$router.push({ name: 'EnterpriseTrainingMaterials' });
    },

    // 查看培训资料详情
    viewTrainingDetails(materialId) {
      this.$router.push({ name: 'TrainingMaterialDetail', params: { materialId } });
    },

    // 编辑培训资料
    editTrainingMaterial(materialId) {
      this.$router.push({
        name: 'TrainingMaterialEdit',
        params: { materialId }
      });
    },
    // 跳转到所有任务页面
    goToEnterpriseAllTasksPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法查看任务。');
        return;
      }
      this.$router.push({ name: 'EnterpriseAllTasks' });
    },
    // 跳转到所有公告页面
    goToEnterpriseAllAnnouncementsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法查看公告。');
        return;
      }
      this.$router.push({ name: 'EnterpriseAllAnnouncements' });
    },
    // 跳转到收藏学生页面
    goToStudentsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法管理学生。');
        return;
      }
      this.$router.push({ name: 'FavoritedStudents' });
    },
    // 跳转到收藏学生页面
    goToFavoritedStudentsPage() {
      if (this.verificationStatus !== 'APPROVED') {
        alert('您的企业身份验证尚未通过，无法查看收藏学生。');
        return;
      }
      this.$router.push({ name: 'FavoritedStudents' });
    },
    // 检查企业验证状态
    checkVerificationStatus() {
      const enterpriseId = localStorage.getItem('userId');
      axios.get(`http://localhost:8080/api/auth/enterprise/${enterpriseId}/verification-status`)
        .then(response => {
          if (response.data) {
            this.verificationStatus = response.data.verificationStatus;
          }
        })
        .catch(error => {
          console.error('获取验证状态失败:', error);
          this.verificationStatus = null;
        });
    },
    // 跳转到企业验证页面
    goToVerificationPage() {
      this.$router.push({ name: 'enterprise-verification' });
    },
    // 分页相关方法
    prevStudentPage() {
      if (this.studentPage > 0) {
        this.studentPage--;
      }
    },
    nextStudentPage() {
      if (this.studentPage < Math.ceil(this.students.length / this.studentsPerPage) - 1) {
        this.studentPage++;
      }
    },
    prevTaskPage() {
      if (this.taskPage > 0) {
        this.taskPage--;
      }
    },
    nextTaskPage() {
      if (this.taskPage < Math.ceil(this.tasks.length / this.itemsPerPage) - 1) {
        this.taskPage++;
      }
    },
    prevAnnouncementPage() {
      if (this.announcementPage > 0) {
        this.announcementPage--;
      }
    },
    nextAnnouncementPage() {
      if (this.announcementPage < Math.ceil(this.announcements.length / this.itemsPerPage) - 1) {
        this.announcementPage++;
      }
    },

    // 岗位分页控制
    prevJobPage() {
      if (this.jobPage > 0) {
        this.jobPage--;
      }
    },
    nextJobPage() {
      if (this.jobPage < Math.ceil(this.jobs.length / this.itemsPerPage) - 1) {
        this.jobPage++;
      }
    },

    // 项目分页控制
    prevProjectPage() {
      if (this.projectPage > 0) {
        this.projectPage--;
      }
    },
    nextProjectPage() {
      if (this.projectPage < Math.ceil(this.projects.length / this.itemsPerPage) - 1) {
        this.projectPage++;
      }
    },

    // 技术挑战分页控制
    prevChallengePage() {
      if (this.challengePage > 0) {
        this.challengePage--;
      }
    },
    nextChallengePage() {
      if (this.challengePage < Math.ceil(this.challenges.length / this.itemsPerPage) - 1) {
        this.challengePage++;
      }
    },

    // 企业竞赛分页控制
    prevCompetitionPage() {
      if (this.competitionPage > 0) {
        this.competitionPage--;
      }
    },
    nextCompetitionPage() {
      if (this.competitionPage < Math.ceil(this.competitions.length / this.itemsPerPage) - 1) {
        this.competitionPage++;
      }
    },

    // 企业活动分页控制
    prevEventPage() {
      if (this.eventPage > 0) {
        this.eventPage--;
      }
    },
    nextEventPage() {
      if (this.eventPage < Math.ceil(this.events.length / this.itemsPerPage) - 1) {
        this.eventPage++;
      }
    },

    // 培训资料分页控制
    prevTrainingPage() {
      if (this.trainingPage > 0) {
        this.trainingPage--;
      }
    },
    nextTrainingPage() {
      if (this.trainingPage < Math.ceil(this.trainingMaterials.length / this.itemsPerPage) - 1) {
        this.trainingPage++;
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '未知时间';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },

    // 格式化薪资范围
    formatSalaryRange(minSalary, maxSalary) {
      if (!minSalary && !maxSalary) return '面议';

      minSalary = minSalary || 0;
      maxSalary = maxSalary || 0;

      if (minSalary === maxSalary) {
        return this.formatSalary(minSalary);
      }

      return `${this.formatSalary(minSalary)} - ${this.formatSalary(maxSalary)}`;
    },

    // 格式化单个薪资值
    formatSalary(salary) {
      if (!salary) return '0';

      // 如果薪资大于等于10000，显示为XXk
      if (salary >= 10000) {
        return `${(salary / 1000).toFixed(0)}k`;
      }

      // 否则显示为XXXX元
      return `${salary}元`;
    },
    // 获取当前日期
    formatCurrentDate() {
      const now = new Date();
      const year = now.getFullYear();
      const month = now.getMonth() + 1;
      const day = now.getDate();
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      const weekday = weekdays[now.getDay()];
      return `${year}年${month}月${day}日 ${weekday}`;
    },
    // 截断文本
    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },
    // 获取公告状态类名
    getStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'APPROVED':
          return 'status-approved';
        case 'REJECTED':
          return 'status-rejected';
        case 'PENDING':
        default:
          return 'status-pending';
      }
    },
    // 获取公告状态文本
    getStatusText(status) {
      if (!status) return '待审核';

      switch(status) {
        case 'APPROVED':
          return '已发布';
        case 'REJECTED':
          return '已拒绝';
        case 'PENDING':
        default:
          return '待审核';
      }
    },

    // 获取项目状态的CSS类名
    getProjectStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'COMPLETED':
          return 'status-approved';
        case 'OPEN':
          return 'status-pending';
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

    // 获取项目类型的文本
    getProjectTypeText(type) {
      if (!type) return '未知类型';

      switch(type) {
        case 'ORDER_TRAINING':
          return '订单培养';
        case 'TRAINING_BASE':
          return '实训基地';
        case 'INDUSTRY_ACADEMIA_RESEARCH':
          return '产学研';
        default:
          return '未知类型';
      }
    },

    // 获取技术挑战状态的CSS类名
    getChallengeStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'OPEN':
          return 'status-pending';
        case 'CLOSED':
          return 'status-rejected';
        case 'COMPLETED':
          return 'status-approved';
        default:
          return 'status-pending';
      }
    },

    // 获取技术挑战状态的文本
    getChallengeStatusText(status) {
      if (!status) return '未知状态';

      switch(status) {
        case 'OPEN':
          return '开放中';
        case 'CLOSED':
          return '已关闭';
        case 'COMPLETED':
          return '已完成';
        default:
          return '未知状态';
      }
    },

    // 获取难度文本
    getDifficultyText(difficulty) {
      if (!difficulty) return '中等';

      switch(difficulty) {
        case 1:
          return '非常简单';
        case 2:
          return '简单';
        case 3:
          return '中等';
        case 4:
          return '困难';
        case 5:
          return '非常困难';
        default:
          return '中等';
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
          return 'status-approved';
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

    // 获取活动状态的CSS类名
    getEventStatusClass(status) {
      if (!status) return 'status-pending';

      switch(status) {
        case 'UPCOMING':
          return 'status-pending';
        case 'ONGOING':
          return 'status-in-progress';
        case 'COMPLETED':
          return 'status-approved';
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

    // 获取未读消息数量
    fetchUnreadMessages() {
      const enterpriseId = localStorage.getItem('userId');
      console.log('获取企业未读消息数量:', enterpriseId);

      // 检查本地存储中是否有未读消息数量
      const storedUnreadMessages = localStorage.getItem(`unreadMessages_${enterpriseId}`);
      if (storedUnreadMessages) {
        try {
          const { count, timestamp } = JSON.parse(storedUnreadMessages);
          // 如果存储的时间不超过30秒，直接使用存储的值
          // 缩短时间以确保更频繁地从数据库获取最新的未读消息数量
          if (Date.now() - timestamp < 30 * 1000) {
            console.log('使用本地存储的未读消息数量:', count);
            this.unreadMessages = count;
            return;
          }
        } catch (e) {
          console.error('解析本地存储的未读消息数量失败:', e);
        }
      }

      // 直接使用后端的API获取未读消息数量
      axios.get(`http://localhost:8080/api/chat/unread/${enterpriseId}`)
        .then(response => {
          console.log('未读消息响应:', response.data);
          const count = response.data.unreadCount || 0;
          this.unreadMessages = count;

          // 保存到本地存储
          localStorage.setItem(`unreadMessages_${enterpriseId}`, JSON.stringify({
            count: count,
            timestamp: Date.now()
          }));
        })
        .catch(error => {
          console.error('获取未读消息数量失败:', error);

          // 如果获取失败，尝试使用备用方法
          this.fetchUnreadMessagesFromHistory();
        });
    },

    // 从聊天历史记录中获取未读消息数量
    fetchUnreadMessagesFromHistory() {
      const enterpriseId = localStorage.getItem('userId');

      // 获取企业收藏的学生
      axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}/favorites`)
        .then(response => {
          const students = response.data || [];

          if (students.length === 0) {
            this.unreadMessages = 0;

            // 保存到本地存储
            localStorage.setItem(`unreadMessages_${enterpriseId}`, JSON.stringify({
              count: 0,
              timestamp: Date.now()
            }));

            return;
          }

          // 寿每个学生获取聊天记录，统计未读消息
          let totalUnread = 0;
          const promises = students.map(student => {
            return axios.get(`http://localhost:8080/api/chat/history/${enterpriseId}/${student.studentId}`)
              .then(response => {
                const messages = response.data || [];

                // 计算未读消息数量 - 只计算学生发送给企业的未读消息
                const unreadCount = messages.filter(
                  msg => msg.receiverId === enterpriseId && msg.senderType === 'STUDENT' && !msg.read
                ).length;

                return unreadCount;
              })
              .catch(error => {
                console.error(`获取与学生 ${student.studentId} 的聊天记录失败:`, error);
                return 0;
              });
          });

          // 等待所有请求完成
          Promise.all(promises)
            .then(results => {
              // 累加所有未读消息数量
              const count = results.reduce((sum, count) => sum + count, 0);
              this.unreadMessages = count;
              console.log('未读消息数量:', this.unreadMessages);

              // 保存到本地存储
              localStorage.setItem(`unreadMessages_${enterpriseId}`, JSON.stringify({
                count: count,
                timestamp: Date.now()
              }));
            })
            .catch(error => {
              console.error('计算未读消息数量失败:', error);
            });
        })
        .catch(error => {
          console.error('获取收藏学生列表失败:', error);
        });
    },

    // 查看企业资料
    viewProfile() {
      // 获取企业ID
      const enterpriseId = localStorage.getItem('userId');
      if (!enterpriseId) {
        console.error('未找到企业ID');
        return;
      }
      // 企业查看自己的资料，使用EnterpriseProfileSelf组件
      this.$router.push({ name: 'EnterpriseProfileSelf', params: { enterpriseId } });
    },

    // 查看消息
    viewMessages() {
      // 跳转到消息列表页面
      this.$router.push('/enterprise/messages');

      // 获取企业ID
      const enterpriseId = localStorage.getItem('userId');

      // 获取企业收藏的学生
      axios.get(`http://localhost:8080/api/enterprises/${enterpriseId}/favorites`)
        .then(response => {
          const students = response.data || [];

          // 寿每个学生的消息标记为已读
          students.forEach(student => {
            axios.post(`http://localhost:8080/api/chat/mark-read/${enterpriseId}/${student.studentId}`)
              .catch(error => {
                console.error(`标记学生 ${student.studentId} 的消息为已读失败:`, error);
              });
          });

          // 点击消息图标后，重置未读消息数量
          this.unreadMessages = 0;
        })
        .catch(error => {
          console.error('获取收藏学生列表失败:', error);
          // 即使出错也重置未读消息数量
          this.unreadMessages = 0;
        });
    }
  },
};
</script>

<style scoped>
/* 总览区域样式 */
.overview-section {
  margin-top: 20px;
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 缩小的总览区域 */
.compact-overview {
  max-width: 80%;
  margin-left: auto;
  margin-right: 0;
}

.overview-header {
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.overview-header h2 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

/* 添加水平滚动容器 */
.overview-content-wrapper {
  overflow-x: auto;
  padding-bottom: 5px;
}

.overview-content {
  display: flex;
  flex-wrap: nowrap; /* 防止换行 */
  gap: 10px;
  min-width: min-content; /* 确保内容不会被压缩 */
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 15px;
  border-radius: 8px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  min-width: 80px; /* 缩小最小宽度 */
  margin-bottom: 5px;
  flex-shrink: 0; /* 防止项目被压缩 */
}

.overview-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #a0cfff;
}

.overview-item.active {
  background-color: #ecf5ff;
  border-color: #409EFF;
}

.overview-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap; /* 防止文本换行 */
}

/* 默认界面样式 */
.default-view {
  width: 100%;
  margin-bottom: 20px;
}

.default-content {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-message {
  text-align: center;
  margin-bottom: 30px;
}

.welcome-message h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 10px;
}

.welcome-message p {
  color: #606266;
  font-size: 16px;
}

.stats-overview {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 15px 20px;
  min-width: 180px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
  margin-right: 15px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 全宽卡片样式 */
.full-width {
  width: 100%;
  grid-column: span 2;
}

/* 基础样式 */
.enterprise-home-container {
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 顶部导航栏样式 */
.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  color: #409EFF;
  font-size: 24px;
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
  font-size: 12px;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  text-align: center;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  border: 1px solid white;
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
  font-size: 18px;
  font-weight: bold;
  margin-right: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: bold;
  color: #333;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

/* 主容器样式 */
.main-container {
  display: flex;
  height: calc(100vh - 70px);
}

/* 侧边栏样式 */
.sidebar {
  width: 200px;
  background-color: #fff;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  padding: 20px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 5px;
}

.menu-item:hover {
  background-color: #ecf5ff;
  color: #409EFF;
}

.menu-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
  border-left: 3px solid #409EFF;
}

.icon {
  margin-right: 10px;
  font-size: 18px;
}

.icon-home::before { content: '🏠'; }
.icon-announcement::before { content: '📢'; }
.icon-task::before { content: '📋'; }
.icon-student::before { content: '👥'; }
.icon-job::before { content: '💼'; }
.icon-project::before { content: '🤝'; }
.icon-challenge::before { content: '🏅'; }
.icon-competition::before { content: '🏆'; }
.icon-event::before { content: '🎉'; }
.icon-forum::before { content: '💬'; }
.icon-training::before { content: '📘'; }

/* 内容区域样式 */
.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 欢迎卡片样式 */
.welcome-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.welcome-text {
  flex: 1;
  max-width: 300px;
}

.welcome-text h1 {
  font-size: 24px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 10px;
}

.welcome-text p {
  color: #606266;
  margin: 0;
}





/* 内容网格样式 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

.content-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.view-all-btn {
  background: none;
  border: none;
  color: #409EFF;
  cursor: pointer;
  font-size: 14px;
}

.view-all-btn:hover {
  text-decoration: underline;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.empty-state p {
  color: #909399;
  margin-bottom: 15px;
}

.action-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: #66b1ff;
}

.action-btn.small {
  font-size: 12px;
  padding: 5px 10px;
}

/* 学生卡片样式 */
.student-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.student-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #f5f7fa;
  transition: all 0.3s;
  cursor: pointer;
}

.student-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.student-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 15px;
}

.student-info {
  flex: 1;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.student-id {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.student-actions {
  margin-left: 10px;
}

/* 任务卡片样式 */
.job-card, .project-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.job-card:hover, .project-card:hover, .training-card:hover, .challenge-card:hover, .competition-card:hover, .event-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border-color: #a0cfff;
  background-color: #f0f9ff;
}

.job-header, .project-header, .training-header, .challenge-header, .competition-header, .event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background-color: #ecf5ff;
  border-bottom: 1px solid #d9ecff;
}

.job-title, .project-title, .training-title, .challenge-title, .competition-title, .event-title {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
  margin: 0;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.job-salary, .project-status, .training-type, .challenge-status, .competition-status, .event-status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: bold;
  background-color: #f0f9eb;
  color: #67c23a;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.job-content, .project-content, .project-details, .training-content, .challenge-content, .competition-content, .event-content {
  padding: 12px 15px;
}

.job-tech, .job-education, .job-location, .project-type, .project-deadline {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.label {
  font-weight: 500;
  color: #303133;
  margin-right: 5px;
}

.job-footer, .project-footer, .training-footer, .challenge-footer, .competition-footer, .event-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.job-date {
  font-size: 12px;
  color: #909399;
}

.job-actions, .project-actions, .training-actions, .challenge-actions, .competition-actions, .event-actions {
  display: flex;
  gap: 8px;
}

.training-list, .challenge-list, .competition-list, .event-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.training-card, .challenge-card, .competition-card, .event-card {
  border-radius: 8px;
  background-color: #f5f7fa;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid #ebeef5;
}

.training-stats, .challenge-stats, .competition-stats, .event-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 15px;
  background-color: #ecf5ff;
  border-top: 1px solid #d9ecff;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-card {
  border-radius: 8px;
  background-color: #f5f7fa;
  overflow: hidden;
  transition: all 0.3s;
}

.task-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.task-header {
  padding: 15px;
  background-color: #ecf5ff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.task-date {
  font-size: 12px;
  color: #909399;
}

.task-content {
  padding: 15px;
  color: #606266;
  font-size: 14px;
}

.task-stats {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background-color: #f0f9eb;
  border-top: 1px solid #e1f3d8;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #67c23a;
}

.task-footer {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-tech {
  font-size: 12px;
  color: #909399;
  background-color: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.task-actions {
  display: flex;
  gap: 10px;
}

/* 公告卡片样式 */
.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.announcement-card {
  border-radius: 8px;
  background-color: #f5f7fa;
  overflow: hidden;
  transition: all 0.3s;
}

.announcement-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.announcement-header {
  padding: 15px;
  background-color: #f0f9eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  flex: 1;
}

.announcement-date {
  font-size: 12px;
  color: #909399;
}

.announcement-content {
  padding: 15px;
  color: #606266;
  font-size: 14px;
}

.announcement-footer {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-approved {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-rejected {
  background-color: #fef0f0;
  color: #f56c6c;
}

.status-pending {
  background-color: #f4f4f5;
  color: #909399;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 15px;
  gap: 10px;
}

.page-btn {
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  width: 30px;
  height: 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  color: #409EFF;
  border-color: #c6e2ff;
}

.page-btn:disabled {
  cursor: not-allowed;
  color: #c0c4cc;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .main-container {
    flex-direction: column;
    height: auto;
  }

  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .welcome-card {
    flex-direction: column;
    text-align: center;
  }

  .stats-container {
    margin-top: 20px;
  }

  .top-header {
    padding: 10px 15px;
  }
}

/* 验证卡片样式 */
.verification-card {
  display: flex;
  align-items: center;
  background-color: #fff7e6;
  border-radius: 8px;
  padding: 15px 20px;
  margin-bottom: 20px;
  border-left: 4px solid #faad14;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.verification-icon {
  font-size: 24px;
  margin-right: 15px;
  color: #faad14;
}

.verification-content {
  flex: 1;
}

.verification-content h3 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 16px;
}

.verification-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.verification-btn {
  background-color: #faad14;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
  white-space: nowrap;
  margin-left: 15px;
}

.verification-btn:hover {
  background-color: #e8971a;
}

/* 验证状态卡片样式 */
.verification-card.warning {
  background-color: #fff8e6;
  border-left: 4px solid #faad14;
}

.verification-card.error {
  background-color: #fff1f0;
  border-left: 4px solid #f5222d;
}

/* 禁用菜单项样式 */
.menu-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.menu-item.disabled:hover {
  background-color: transparent;
}
</style>
