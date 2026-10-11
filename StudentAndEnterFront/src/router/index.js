import { createRouter, createWebHistory } from 'vue-router';
import Login from '../components/Views/Login.vue';
import Register from '../components/Views/Register.vue';  // 注册页面
import StudentHome from '../components/Views/StudentHome.vue';
import EnterpriseHome from '../components/Views/EnterpriseHome.vue';
import AllTask from "@/components/Views/AllTask.vue";
import AllNotices from "@/components/Views/AllNotices.vue";
import TaskDetail from "@/components/Views/TaskDetail.vue";
import Chate2s from "@/components/Views/Chate2s.vue";
import EnterpriseNoticePub from "@/components/Views/EnterpriseNoticePub.vue";
import Admin from "@/components/Views/Admin.vue";
import NoticeDetail from "@/components/Views/NoticeDetail.vue";
import StudentDetail from "@/components/Views/StudentDetail.vue";
import TaskDetailForEnter from "@/components/Views/TaskDetailForEnter.vue";
import AllAssignedTasks from "@/components/Views/AllAssignedTasks.vue";
import TaskSubmission from "@/components/Views/TaskSubmission.vue";
import TaskSubmissions from "@/components/Views/TaskSubmissions.vue";
import FavoritedStudents from "@/components/Views/FavoritedStudents.vue";
import TaskPublish from "@/components/Views/TaskPublish.vue";
import EnterpriseAllTasks from "@/components/Views/EnterpriseAllTasks.vue";
import EnterpriseAllAnnouncements from "@/components/Views/EnterpriseAllAnnouncements.vue";
import ChatE2S from "@/components/Views/Chate2s.vue";
import ChatS2E from "@/components/Views/Chats2e.vue";
import EnterpriseDetailForStudent from "@/components/Views/EnterpriseDetailForStudent.vue";
import EnterpriseDetail from "@/components/Views/EnterpriseDetail.vue";
import StudentMessages from "@/components/Views/StudentMessages.vue";
import EnterpriseMessages from "@/components/Views/EnterpriseMessages.vue";
import EnterpriseVerification from "@/components/Views/EnterpriseVerification.vue";
import AdminEnterpriseVerifications from "@/components/Views/AdminEnterpriseVerifications.vue";
import EnterpriseJobManagement from "@/components/Views/EnterpriseJobManagement.vue";
import EnterpriseJobApplications from "@/components/Views/EnterpriseJobApplications.vue";
import StudentJobBrowse from "@/components/Views/StudentJobBrowse.vue";
import JobDetail from "@/components/Views/JobDetail.vue";
import StudentProfileSelf from "@/components/Views/StudentProfileSelf.vue";
import EnterpriseProfileSelf from "@/components/Views/EnterpriseProfileSelf.vue";

// 项目合作相关组件
import ProjectCollaborationPublish from "@/components/Views/ProjectCollaborationPublish.vue";
import EnterpriseProjects from "@/components/Views/EnterpriseProjects.vue";
import StudentProjects from "@/components/Views/StudentProjects.vue";
import ProjectDetail from "@/components/Views/ProjectDetail.vue";
import ProjectApplication from "@/components/Views/ProjectApplication.vue";
import ProjectApplicationReview from "@/components/Views/ProjectApplicationReview.vue";

// 技术挑战相关组件
import TechnicalChallengePublish from "@/components/Views/TechnicalChallengePublish.vue";
import EnterpriseChallenges from "@/components/Views/EnterpriseChallenges.vue";
import StudentChallenges from "@/components/Views/StudentChallenges.vue";
import TechnicalChallengeDetail from "@/components/Views/TechnicalChallengeDetail.vue";
import TechnicalChallengeSubmission from "@/components/Views/TechnicalChallengeSubmission.vue";

// 竞赛相关组件
import StudentCompetitions from "@/components/Views/StudentCompetitions.vue";
import CompetitionDetail from "@/components/Views/CompetitionDetail.vue";
import EnterpriseCompetitions from "@/components/Views/EnterpriseCompetitions.vue";
import CompetitionPublish from "@/components/Views/CompetitionPublish.vue";

// 企业活动相关组件
import StudentEvents from "@/components/Views/StudentEvents.vue";
import EventDetail from "@/components/Views/EventDetail.vue";
import EnterpriseEvents from "@/components/Views/EnterpriseEvents.vue";
import EventPublish from "@/components/Views/EventPublish.vue";

// 论坛相关组件
import ForumHome from "@/components/Views/ForumHome.vue";
import ForumPost from "@/components/Views/ForumPost.vue";
import CreateForumPost from "@/components/Views/CreateForumPost.vue";
import LikedPosts from "@/components/Views/LikedPosts.vue";

// 培训资料相关组件
import EnterpriseTrainingMaterials from "@/components/Views/EnterpriseTrainingMaterials.vue";
import StudentTrainingMaterials from "@/components/Views/StudentTrainingMaterials.vue";


const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: Login },
    { path: '/register', name: 'register', component: Register },  // 注册页面路由
    { path: '/student-home', name: 'student-home', component: StudentHome },
    { path: '/enterprise-home', name: 'enterprise-home', component: EnterpriseHome },
    { path: '/all-task', name: 'all-task', component: AllTask },
    { path: '/all-notices', name: 'all-notices', component: AllNotices },
    { path: '/task-detail/:taskId', name: 'TaskDetail', component: TaskDetail },
    { path: '/chate2s/:studentId', name: 'Chate2s', component: Chate2s },
    { path: '/enterprise-notice-pub', name: 'enterprise-notice-pub', component: EnterpriseNoticePub },
    { path: '/admin', name: 'admin', component: Admin },
    { path: '/admin/enterprise-verifications', name: 'AdminEnterpriseVerifications', component: AdminEnterpriseVerifications },
    { path: '/notice-detail/:noticeId', name: 'notice-detail', component: NoticeDetail },
    { path: '/student-detail/:studentId', name: 'StudentDetail', component: StudentDetail },
    { path: '/task-detail-for-enter/:taskId', name: 'TaskDetailForEnter', component: TaskDetail, props: (route) => ({ ...route.params, role: 'enterprise' }) },
    { path: '/assigned-tasks', name: 'AllAssignedTasks', component: AllAssignedTasks },
    { path: '/task-submission/:taskId', name: 'TaskSubmission', component: TaskSubmission },
    { path: '/task-submissions/:taskId', name: 'TaskSubmissions', component: TaskSubmissions },
    { path: '/favorited-students', name: 'FavoritedStudents', component: FavoritedStudents },
    { path: '/task-publish', name: 'TaskPublish', component: TaskPublish },
    { path: '/enterprise-all-tasks', name: 'EnterpriseAllTasks', component: EnterpriseAllTasks },
    { path: '/enterprise-all-announcements', name: 'EnterpriseAllAnnouncements', component: EnterpriseAllAnnouncements },
    { path: '/chat/:studentId', name: 'ChatE2S', component: ChatE2S },
    { path: '/student/chat/:enterpriseId', name: 'ChatS2E', component: ChatS2E },
    { path: '/student/enterprise/:enterpriseId', name: 'EnterpriseDetailForStudent', component: EnterpriseDetailForStudent },
    { path: '/enterprise/profile/:enterpriseId', name: 'EnterpriseDetail', component: EnterpriseDetail },
    { path: '/student/messages', name: 'student-messages', component: StudentMessages },
    { path: '/enterprise/messages', name: 'enterprise-messages', component: EnterpriseMessages },
    { path: '/enterprise/verification', name: 'enterprise-verification', component: EnterpriseVerification },
    { path: '/enterprise/jobs', name: 'enterprise-jobs', component: EnterpriseJobManagement },
    { path: '/enterprise/job-applications', name: 'enterprise-job-applications', component: EnterpriseJobApplications },
    { path: '/student/jobs', name: 'student-jobs', component: StudentJobBrowse },
    { path: '/jobs/:jobId', name: 'JobDetail', component: JobDetail },

    // 项目合作相关路由
    { path: '/enterprise/projects/publish', name: 'ProjectCollaborationPublish', component: ProjectCollaborationPublish },
    { path: '/enterprise/projects', name: 'EnterpriseProjects', component: EnterpriseProjects },
    { path: '/student/projects', name: 'StudentProjects', component: StudentProjects },
    { path: '/projects/:projectId', name: 'ProjectDetail', component: ProjectDetail },
    { path: '/projects/:projectId/apply', name: 'ProjectApplication', component: ProjectApplication },
    { path: '/enterprise/projects/applications/:applicationId', name: 'ProjectApplicationReview', component: ProjectApplicationReview },

    // 产学研项目步骤相关路由
    { path: '/projects/:projectId/steps/:stepNumber/submit', name: 'ProjectStepSubmission', component: () => import('@/components/Views/ProjectStepSubmission.vue') },
    { path: '/projects/:projectId/steps/:stepNumber/submissions', name: 'ProjectStepSubmissions', component: () => import('@/components/Views/ProjectStepSubmissions.vue') },
    { path: '/projects/:projectId/steps/:stepNumber/review/:studentId', name: 'ProjectStepReview', component: () => import('@/components/Views/ProjectStepReview.vue') },
    { path: '/projects/:projectId/plan', name: 'ProjectPlanUpload', component: () => import('@/components/Views/ProjectPlanUpload.vue') },
    { path: '/projects/:projectId/plan-review', name: 'ProjectPlanReview', component: () => import('@/components/Views/ProjectPlanReview.vue') },

    // 实训基地项目相关路由
    { path: '/projects/:projectId/training-base', name: 'TrainingBaseDetail', component: () => import('@/components/Views/TrainingBaseDetail.vue') },
    { path: '/projects/:projectId/training-report', name: 'TrainingReportUpload', component: () => import('@/components/Views/TrainingReportUpload.vue') },

    // 订单培养项目相关路由
    { path: '/projects/:projectId/order-training', name: 'OrderTrainingDetail', component: () => import('@/components/Views/OrderTrainingDetail.vue') },
    { path: '/projects/:projectId/order-training/step/:stepNumber/upload', name: 'OrderTrainingUpload', component: () => import('@/components/Views/OrderTrainingUpload.vue') },

    // 技术挑战相关路由
    { path: '/enterprise/challenges/publish', name: 'TechnicalChallengePublish', component: TechnicalChallengePublish },
    { path: '/enterprise/challenges', name: 'EnterpriseChallenges', component: EnterpriseChallenges },
    { path: '/student/challenges', name: 'StudentChallenges', component: StudentChallenges },
    { path: '/challenges/:challengeId', name: 'ChallengeDetail', component: TechnicalChallengeDetail },
    { path: '/challenges/:challengeId/submit', name: 'TechnicalChallengeSubmission', component: TechnicalChallengeSubmission },

    // 竞赛相关路由
    { path: '/student/competitions', name: 'StudentCompetitions', component: StudentCompetitions },
    { path: '/competitions/:competitionId', name: 'CompetitionDetail', component: CompetitionDetail },
    { path: '/enterprise/competitions', name: 'EnterpriseCompetitions', component: EnterpriseCompetitions },
    { path: '/enterprise/competitions/publish', name: 'CompetitionPublish', component: CompetitionPublish },

    // 企业活动相关路由
    { path: '/student/events', name: 'StudentEvents', component: StudentEvents },
    { path: '/events/:eventId', name: 'EventDetail', component: EventDetail },
    { path: '/enterprise/events', name: 'EnterpriseEvents', component: EnterpriseEvents },
    { path: '/enterprise/events/publish', name: 'EventPublish', component: EventPublish },

    // 学生和企业的个人资料页面
    { path: '/student/profile/:studentId', name: 'StudentProfileSelf', component: StudentProfileSelf },
    { path: '/enterprise/profile-self/:enterpriseId', name: 'EnterpriseProfileSelf', component: EnterpriseProfileSelf },

    // 学生和企业查看对方的详情页面
    { path: '/student/enterprise/:enterpriseId', name: 'EnterpriseDetailForStudent', component: EnterpriseDetailForStudent },
    { path: '/enterprise/student/:studentId', name: 'StudentDetail', component: StudentDetail },

    // 论坛相关路由
    { path: '/forum', name: 'ForumHome', component: ForumHome },
    { path: '/forum/posts/:postId', name: 'ForumPost', component: ForumPost },
    { path: '/forum/create', name: 'CreateForumPost', component: CreateForumPost },
    { path: '/forum/edit/:postId', name: 'EditForumPost', component: CreateForumPost },
    { path: '/forum/liked-posts', name: 'LikedPosts', component: LikedPosts },

    // 培训资料相关路由
    { path: '/enterprise/training-materials', name: 'EnterpriseTrainingMaterials', component: EnterpriseTrainingMaterials },
    { path: '/student/training-materials', name: 'StudentTrainingMaterials', component: StudentTrainingMaterials },
    { path: '/training-materials/:materialId', name: 'TrainingMaterialDetail', component: () => import('@/components/Views/TrainingMaterialDetail.vue') },
    { path: '/training-materials/:materialId/edit', name: 'TrainingMaterialEdit', component: () => import('@/components/Views/TrainingMaterialEdit.vue') },
];

const router = createRouter({
    history: createWebHistory(),
    routes
});


export default router;
