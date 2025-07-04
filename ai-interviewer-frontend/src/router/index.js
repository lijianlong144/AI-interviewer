import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserRole, getUserInfo } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login',
    meta: { requiresAuth: false }
  },
  // 面试官路由
  {
    path: '/interviewer',
    component: () => import('@/layouts/InterviewerLayout.vue'),
    meta: { roles: ['INTERVIEWER'] },
    children: [
      {
        path: 'dashboard',
        name: 'InterviewerDashboard',
        component: () => import('@/views/interviewer/Dashboard.vue'),
        meta: { title: '工作台', roles: ['INTERVIEWER'] }
      },
      {
        path: 'question',
        name: 'InterviewerQuestionList',
        component: () => import('@/views/question/QuestionList.vue'),
        meta: { title: '题库管理', roles: ['INTERVIEWER'] }
      },
      {
        path: 'interview',
        name: 'InterviewerInterviewList',
        component: () => import('@/views/interview/InterviewList.vue'),
        meta: { title: '面试管理', roles: ['INTERVIEWER'] }
      },
      {
        path: 'interview-room/:roomCode',
        name: 'InterviewerRoom',
        component: () => import('@/views/interview/InterviewRoom.vue'),
        meta: { title: '面试房间', roles: ['INTERVIEWER'] }
      }
    ]
  },
  // HR路由
  {
    path: '/hr',
    component: () => import('@/layouts/HRLayout.vue'),
    meta: { roles: ['HR'] },
    children: [
      {
        path: 'dashboard',
        name: 'HRDashboard',
        component: () => import('@/views/hr/Dashboard.vue'),
        meta: { title: '工作台', roles: ['HR'] }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/hr/UserManagement.vue'),
        meta: { title: '用户管理', roles: ['HR'] }
      },
      {
        path: 'applications',
        name: 'ApplicationManagement',
        component: () => import('@/views/hr/ApplicationManagement.vue'),
        meta: { title: '申请管理', roles: ['HR'] }
      },
      {
        path: 'application/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/hr/ApplicationDetail.vue'),
        meta: { title: '申请详情', roles: ['HR'] }
      },
      {
        path: 'interviews',
        name: 'InterviewManagement',
        component: () => import('@/views/hr/InterviewManagement.vue'),
        meta: { title: '面试管理', roles: ['HR'] }
      },
      {
        path: 'questions',
        name: 'QuestionManagement',
        component: () => import('@/views/hr/QuestionManagement.vue'),
        meta: { title: '题库管理', roles: ['HR'] }
      },
      {
        path: 'positions',
        name: 'PositionManagement',
        component: () => import('@/views/hr/PositionManagement.vue'),
        meta: { title: '职位管理', roles: ['HR'] }
      }
    ]
  },
  // 面试者路由
  {
    path: '/candidate',
    component: () => import('@/layouts/CandidateLayout.vue'),
    meta: { roles: ['CANDIDATE'] },
    children: [
      {
        path: 'dashboard',
        name: 'CandidateDashboard',
        component: () => import('@/views/candidate/Dashboard.vue'),
        meta: { title: '我的面试', roles: ['CANDIDATE'] }
      },
      {
        path: 'positions',
        name: 'PositionList',
        component: () => import('@/views/candidate/PositionList.vue'),
        meta: { title: '职位列表', roles: ['CANDIDATE'] }
      },
      {
        path: 'position/:id',
        name: 'PositionDetail',
        component: () => import('@/views/candidate/PositionDetail.vue'),
        meta: { title: '职位详情', roles: ['CANDIDATE'] }
      },
      {
        path: 'resume',
        name: 'ResumeList',
        component: () => import('@/views/candidate/ResumeList.vue'),
        meta: { title: '我的简历', roles: ['CANDIDATE'] }
      },
      {
        path: 'resume/create',
        name: 'ResumeCreate',
        component: () => import('@/views/candidate/ResumeEdit.vue'),
        meta: { title: '创建简历', roles: ['CANDIDATE'] }
      },
      {
        path: 'resume/edit/:id',
        name: 'ResumeEdit',
        component: () => import('@/views/candidate/ResumeEdit.vue'),
        meta: { title: '编辑简历', roles: ['CANDIDATE'] }
      },
      {
        path: 'resume/preview/:id',
        name: 'ResumePreview',
        component: () => import('@/views/candidate/ResumePreview.vue'),
        meta: { title: '预览简历', roles: ['CANDIDATE'] }
      },
      {
        path: 'interviews',
        name: 'MyInterviews',
        component: () => import('@/views/candidate/InterviewList.vue'),
        meta: { title: '我的面试', roles: ['CANDIDATE'] }
      },
      {
        path: 'interview/:id',
        name: 'InterviewDetail',
        component: () => import('@/views/candidate/InterviewDetail.vue'),
        meta: { title: '面试详情', roles: ['CANDIDATE'] }
      },
      {
        path: 'profile',
        name: 'CandidateProfile',
        component: () => import('@/views/candidate/Profile.vue'),
        meta: { title: '个人信息', roles: ['CANDIDATE'] }
      },
      {
        path: 'interview-result/:id',
        name: 'CandidateInterviewResult',
        component: () => import('@/views/candidate/InterviewResult.vue'),
        meta: { title: '面试结果', roles: ['CANDIDATE'] }
      },
      {
        path: 'interview-detail/:id',
        name: 'CandidateInterviewDetail',
        component: () => import('@/views/candidate/InterviewDetail.vue'),
        meta: { title: '面试详情', roles: ['CANDIDATE'] }
      },
      {
        path: 'interview-room/:roomCode',
        name: 'CandidateInterviewRoom',
        component: () => import('@/views/interview/InterviewRoom.vue'),
        meta: { title: 'AI面试房间', roles: ['CANDIDATE'] }
      }
    ]
  },
  // 公共面试房间路由（不需要布局）
  {
    path: '/interview-room/:roomCode',
    name: 'InterviewRoom',
    component: () => import('@/views/interview/InterviewRoom.vue'),
    meta: { title: 'AI面试房间', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = getToken()
  const userRole = getUserRole()
  const userInfo = getUserInfo()

  // 不需要认证的路由直接放行
  if (to.meta.requiresAuth === false) {
    return next()
  }
  
  // 未登录，跳转到登录页
  if (!token) {
    return next('/login')
  }
  
  // 检查是否有用户信息
  if (!userInfo) {
    ElMessage.warning('未获取到用户信息，请重新登录')
    const userStore = useUserStore()
    userStore.logout()
    return next('/login')
  }
  
  // 检查路由是否有角色限制
  if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    // 根据角色跳转到对应的默认页面
    switch (userRole) {
      case 'INTERVIEWER':
        return next('/interviewer/dashboard')
      case 'HR':
        return next('/hr/dashboard')
      case 'CANDIDATE':
        return next('/candidate/dashboard')
      default:
        return next('/login')
    }
  }
  
  next()
})

export default router