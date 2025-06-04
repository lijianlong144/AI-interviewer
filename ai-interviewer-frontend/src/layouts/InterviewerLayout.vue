<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="220px">
        <div class="logo">AI面试系统</div>
        <el-menu
          router
          :default-active="activeMenu"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/interviewer/dashboard">
            <el-icon><Menu /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/interviewer/question">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/interviewer/interview">
            <el-icon><Calendar /></el-icon>
            <span>面试管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="user-info">
                <el-avatar size="small" :src="userInfo?.avatar || ''">{{ userInfo?.realName?.charAt(0) || 'U' }}</el-avatar>
                <span class="username">{{ userInfo?.realName || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <el-breadcrumb class="breadcrumb" separator="/">
            <el-breadcrumb-item :to="{ path: '/interviewer/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
          
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取当前路由信息
const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

// 用户信息
const userInfo = computed(() => userStore.userInfo)

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
}

.el-aside {
  background-color: #001529;
  color: #fff;
  height: 100vh;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #002140;
}

.el-header {
  background-color: #fff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 8px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.el-main {
  padding: 20px;
  background-color: #f0f2f5;
}
</style> 