<template>
  <div class="reward-list">
    <div v-if="list.length === 0" class="empty-state">
      <el-empty description="暂无奖励记录" />
    </div>
    
    <div v-else class="reward-items">
      <div v-for="reward in list" :key="reward.id" class="reward-item">
        <div class="reward-icon">
          <el-icon :size="32"><Trophy /></el-icon>
        </div>
        <div class="reward-content">
          <div class="reward-title">{{ reward.title }}</div>
          <div class="reward-desc">{{ reward.description }}</div>
          <div class="reward-time">{{ formatTime(reward.createdAt) }}</div>
        </div>
        <div class="reward-amount">
          <span class="amount">+{{ reward.amount }}</span>
          <span class="unit">ETH</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Trophy } from '@element-plus/icons-vue'

const props = defineProps({
  list: {
    type: Array,
    default: () => []
  }
})

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.reward-list {
  padding: 20px 0;
}

.empty-state {
  padding: 40px 0;
}

.reward-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  transition: background 0.3s;
}

.reward-item:hover {
  background: #f5f7fa;
}

.reward-item:last-child {
  border-bottom: none;
}

.reward-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fef0e6;
  border-radius: 50%;
  color: #e6a23c;
}

.reward-content {
  flex: 1;
  margin-left: 16px;
}

.reward-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.reward-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.reward-time {
  font-size: 12px;
  color: #909399;
}

.reward-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-weight: bold;
  color: #f56c6c;
}

.amount {
  font-size: 24px;
}

.unit {
  font-size: 14px;
}
</style>

