<script setup>
// 不需要任何导入，只需要渲染router-view
</script>

<template>
  <!-- 应用主容器 -->
  <div id="app">
    <!-- 路由视图，显示当前路由匹配的组件，添加过渡效果 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

#app {
  height: 100vh;
  width: 100%;
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
