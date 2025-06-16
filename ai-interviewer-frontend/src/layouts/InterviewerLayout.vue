<template>
  <div class="layout-container">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
        <div class="logo-container">
          <div class="logo" :class="{ 'collapsed': isCollapse }">
            <el-icon class="logo-icon"><Monitor /></el-icon>
            <span class="logo-text" v-show="!isCollapse">AI面试系统</span>
          </div>
        </div>
        <el-menu
          router
          :default-active="activeMenu"
          :collapse="isCollapse"
          background-color="#0f2350"
          text-color="#ffffff"
          active-text-color="#ffffff"
          :collapse-transition="false"
        >
          <el-menu-item index="/interviewer/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>工作台</template>
          </el-menu-item>
          <el-menu-item index="/interviewer/schedule">
            <el-icon><Tickets /></el-icon>
            <template #title>面试安排</template>
          </el-menu-item>
          <el-menu-item index="/interviewer/question">
            <el-icon><Document /></el-icon>
            <template #title>题库管理</template>
          </el-menu-item>
          <el-menu-item index="/interviewer/interview">
            <el-icon><Calendar /></el-icon>
            <template #title>面试管理</template>
          </el-menu-item>
        </el-menu>
        <div class="collapse-trigger" @click="toggleCollapse">
          <el-icon v-if="isCollapse"><Expand /></el-icon>
          <el-icon v-else><Fold /></el-icon>
        </div>
      </el-aside>
      
      <el-container class="main-container">
        <el-header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/interviewer/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatar || ''">{{ userInfo?.realName?.charAt(0) || 'U' }}</el-avatar>
                <span class="username">{{ userInfo?.realName || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
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
        </el-header>
        
        <el-main>
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { 
  DataBoard, 
  Calendar, 
  Document, 
  Tickets, 
  ArrowDown,
  Fold,
  Expand,
  Monitor,
  UserFilled,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 获取当前路由信息
const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    // 跳转到个人中心
    // router.push('/interviewer/profile')
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
  overflow: hidden;
}

.sidebar-container {
  position: relative;
  background-color: #0f2350;
  color: #fff;
  height: 100vh;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.1);
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  padding: 0 10px;
  background: linear-gradient(135deg, #0f2350 0%, #1a3a7a 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  transition: all 0.3s;
}

.logo.collapsed {
  justify-content: center;
}

.logo-icon {
  font-size: 24px;
  color: #fff;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  white-space: nowrap;
  transition: opacity 0.3s;
}

.collapse-trigger {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  background-color: #2563eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
  z-index: 10;
}

.collapse-trigger:hover {
  background-color: #1d4ed8;
  transform: translateX(-50%) scale(1.05);
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.main-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.username {
  margin: 0 8px;
  font-weight: 500;
}

.el-main {
  padding: 24px;
  background-color: #f8fafc;
  overflow-y: auto;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
}

:deep(.el-menu-item.is-active) {
  background-color: #2563eb;
  color: #ffffff !important;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(37, 99, 235, 0.1);
  color: #ffffff !important;
}

:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-menu-item) {
  text-align: center;
  padding: 0 !important;
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
</style> 