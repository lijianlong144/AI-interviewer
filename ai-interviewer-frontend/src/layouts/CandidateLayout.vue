<template>
  <div class="layout-container">
    <div class="navbar">
      <div class="navbar-brand">
        <el-icon class="brand-icon"><Monitor /></el-icon>
        <span class="brand-name">AI面试系统</span>
      </div>

      <div class="navbar-menu">
        <div 
          v-for="(item, index) in navItems" 
          :key="index" 
          class="nav-item"
          :class="{ active: activeMenu === item.path }"
          @click="navigateTo(item.path)"
        >
          <el-icon class="nav-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="nav-text">{{ item.text }}</span>
        </div>
      </div>

      <div class="navbar-user">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-profile">
            <el-avatar class="user-avatar" :size="36" :src="userInfo?.avatar || ''">
              {{ userInfo?.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-info">
              <span class="user-name">{{ userInfo?.realName || '用户' }}</span>
              <span class="user-role">求职者</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-content">
      <div class="content-header">
        <div class="page-path">
          <h1 class="page-title">{{ currentRoute.meta.title }}</h1>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/candidate/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </div>
      
      <div class="content-body">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { 
  HomeFilled, 
  Search, 
  Calendar, 
  Document, 
  User, 
  Monitor, 
  UserFilled, 
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 导航项目
const navItems = [
  { path: '/candidate/dashboard', text: '我的主页', icon: HomeFilled },
  { path: '/candidate/positions', text: '职位搜索', icon: Search },
  { path: '/candidate/interviews', text: '面试安排', icon: Calendar },
  { path: '/candidate/resume', text: '我的简历', icon: Document },
  { path: '/candidate/profile', text: '个人中心', icon: User }
]

// 获取当前路由信息
const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 导航处理
const navigateTo = (path) => {
  router.push(path)
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/candidate/profile')
  }
}

// 初始化获取用户信息
onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfoAction()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f9fafb;
}

/* 导航栏样式 */
.navbar {
  height: 70px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  position: relative;
  z-index: 10;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 40px;
  color: var(--primary-color);
}

.brand-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  white-space: nowrap;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-grow: 1;
  height: 100%;
}

.nav-item {
  height: 70px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  color: var(--text-secondary);
}

.nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: transparent;
  transition: background-color 0.3s ease;
}

.nav-item:hover {
  color: var(--primary-color);
  background-color: rgba(37, 99, 235, 0.04);
}

.nav-item.active {
  color: var(--primary-color);
  font-weight: 500;
}

.nav-item.active::after {
  background-color: var(--primary-color);
}

.nav-icon {
  font-size: 18px;
}

.nav-text {
  font-size: 15px;
}

.navbar-user {
  margin-left: 20px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.user-profile:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.user-avatar {
  background-color: var(--primary-color);
  color: white;
  font-weight: 600;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 30px 30px;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.content-header {
  padding: 20px 0;
  margin-bottom: 10px;
}

.page-path {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.breadcrumb {
  font-size: 13px;
  color: var(--text-secondary);
}

.content-body {
  flex: 1;
  border-radius: 12px;
  background-color: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  padding: 24px;
}

/* 页面过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .navbar {
    padding: 0 20px;
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-item {
    padding: 0 12px;
    justify-content: center;
  }
  
  .user-info {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 15px;
  }
  
  .brand-name {
    display: none;
  }
  
  .navbar-menu {
    gap: 0;
  }
  
  .main-content {
    padding: 0 15px 15px;
  }
  
  .content-body {
    padding: 15px;
  }
}
</style> 