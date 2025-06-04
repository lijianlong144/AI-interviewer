<template>
  <div class="not-found">
    <el-result
      icon="error"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const goHome = () => {
  const role = userStore.role
  
  // 根据角色跳转到对应的首页
  switch (role) {
    case 'INTERVIEWER':
      router.push('/interviewer/dashboard')
      break
    case 'HR':
      router.push('/hr/dashboard')
      break
    case 'CANDIDATE':
      router.push('/candidate/dashboard')
      break
    default:
      router.push('/login')
  }
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style> 