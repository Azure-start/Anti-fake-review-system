<template>
  <div class="dashboard">
    <div class="container">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-label">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSystemStats } from '@/api/adminApi'

const stats = ref([
  { title: '总用户数', value: '0' },
  { title: '总商家数', value: '0' },
  { title: '总商品数', value: '0' },
  { title: '总订单数', value: '0' }
])

async function loadStats() {
  try {
    const result = await getSystemStats()
    if (result.code === 0) {
      const data = result.data
      stats.value = [
        { title: '总用户数', value: data.totalUsers || 0 },
        { title: '总商家数', value: data.totalMerchants || 0 },
        { title: '总商品数', value: data.totalProducts || 0 },
        { title: '总订单数', value: data.totalOrders || 0 }
      ]
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.stat-card {
  margin-top: 20px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
}

.stat-content {
  text-align: center;
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
</style>

