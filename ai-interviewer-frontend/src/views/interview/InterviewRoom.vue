<template>
  <div class="interview-room-container">
    <h1>面试房间</h1>
    <p>面试房间号: {{ roomCode }}</p>
    <p>这是面试官视角的面试房间，功能正在开发中...</p>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const roomCode = route.params.roomCode
</script>

<style scoped>
.interview-room-container {
  padding: 20px;
}
</style> 