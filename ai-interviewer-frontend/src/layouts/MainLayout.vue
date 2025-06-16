<template>
  <el-container class="layout-container">
    <el-aside width="240px">
      <div class="sidebar">
        <div class="logo">
          <span class="logo-icon"><el-icon><Monitor /></el-icon></span>
          <span class="logo-text">AI面试系统</span>
        </div>
        <el-menu 
          :default-active="activeMenu" 
          router
          class="el-menu-vertical"
          background-color="transparent"
          text-color="var(--text-secondary)"
          active-text-color="var(--primary-color)"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/question">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </el-menu-item>
          <el-menu-item index="/interview">
            <el-icon><VideoCamera /></el-icon>
            <span>面试管理</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>
    
    <el-container class="main-content">
      <el-header>
        <div class="header-content">
          <div class="header-left">
            <h2 class="page-title">{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="32" class="user-avatar">
                  {{ userInfo.realName?.[0] || userInfo.username?.[0] || 'U' }}
                </el-avatar>
                <span class="user-name">{{ userInfo.realName || userInfo.username }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
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
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '工作台')

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  ElMessage.success('退出成功')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: var(--background-light);
}

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  margin-bottom: 1rem;
}

.logo-icon {
  font-size: 1.5rem;
  margin-right: 8px;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 600;
}

.el-aside {
  background-color: white;
  border-right: 1px solid var(--border-color);
  transition: width var(--transition-normal);
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 0 24px 24px 0;
  margin-right: 12px;
}

.el-menu-vertical .el-menu-item.is-active {
  background: rgba(37, 99, 235, 0.1);
  color: var(--primary-color);
  font-weight: 500;
}

.el-header {
  height: 64px;
  background-color: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.main-content {
  position: relative;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.user-avatar {
  background-color: var(--primary-color);
  color: white;
  font-weight: 600;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-icon {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.el-main {
  padding: 24px;
  background-color: var(--background-light);
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