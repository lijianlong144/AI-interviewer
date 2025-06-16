<template>
  <div class="table-actions">
    <el-tooltip
      v-for="(button, index) in visibleButtons"
      :key="index"
      :content="button.text"
      placement="top"
      :hide-after="1000"
    >
      <button 
        class="action-button" 
        :class="[button.type || 'primary']"
        @click="() => button.onClick && button.onClick()"
        :disabled="button.disabled"
      >
        <el-icon v-if="button.icon" class="button-icon">
          <component :is="button.icon" />
        </el-icon>
        <span class="button-text">{{ button.text }}</span>
      </button>
    </el-tooltip>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  buttons: {
    type: Array,
    required: true
  }
})

const visibleButtons = computed(() => {
  return props.buttons.filter(button => button.visible !== false)
})
</script>

<style scoped>
.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s ease;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 32px;
  min-width: 68px;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15);
}

.action-button:active {
  transform: translateY(0);
}

.action-button.primary {
  background-color: #2563eb;
}

.action-button.primary:hover {
  background-color: #1d4ed8;
}

.action-button.success {
  background-color: #10b981;
}

.action-button.success:hover {
  background-color: #059669;
}

.action-button.warning {
  background-color: #f59e0b;
}

.action-button.warning:hover {
  background-color: #d97706;
}

.action-button.danger {
  background-color: #ef4444;
}

.action-button.danger:hover {
  background-color: #dc2626;
}

.action-button.info {
  background-color: #6366f1;
}

.action-button.info:hover {
  background-color: #4f46e5;
}

.action-button:disabled {
  background-color: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.button-icon {
  margin-right: 4px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .table-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .action-button {
    width: 100%;
    justify-content: center;
  }
}
</style> 