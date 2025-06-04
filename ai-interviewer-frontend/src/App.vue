<script setup>
// 不需要任何导入，只需要渲染router-view
</script>

<template>
  <!-- 应用主容器 -->
  <div id="app">
    <!-- 路由视图，显示当前路由匹配的组件 -->
    <router-view />
  </div>
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: Arial, Helvetica, sans-serif;
}

#app {
  height: 100vh;
  width: 100%;
}
</style>
