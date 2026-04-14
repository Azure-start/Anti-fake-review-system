<template>
  <div class="account">
    <div class="container">
      <el-card header="我的账户" class="account-card">
        <el-row :gutter="20">
          <!-- 账户信息 -->
          <el-col :xs="24" :sm="12">
            <div class="account-info">
              <h3>账户信息</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="钱包地址">
                  <div class="address-row">
                    {{ userStore.walletAddress }}
                    <el-button
                      type="primary"
                      :icon="CopyDocument"
                      size="small"
                      text
                      @click="copyAddress"
                    >
                      复制
                    </el-button>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="测试币余额">
                  {{ userStore.balance }} ETH
                </el-descriptions-item>
                <el-descriptions-item label="奖励余额">
                  {{ userStore.rewardBalance }} ETH
                </el-descriptions-item>
                <el-descriptions-item label="账户状态">
                  <el-tag type="success">已激活</el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <div class="account-actions">
                <el-button type="primary" @click="handleRefreshBalance">
                  <el-icon><Refresh /></el-icon>
                  刷新余额
                </el-button>
                <el-link :href="'https://sepolia.etherscan.io/address/'+ userStore.walletAddress"  target="_blank" type="primary">
                  <el-icon><View /></el-icon>
                  查看链上地址
                </el-link>
              </div>
            </div>
          </el-col>

          <!-- 统计信息 -->
          <el-col :xs="24" :sm="12">
            <div class="account-stats">
              <h3>我的统计</h3>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-statistic title="累计交易" :value="stats.totalTransactions">
                    <template #suffix>
                      <span style="font-size: 14px;">笔</span>
                    </template>
                  </el-statistic>
                </el-col>
                <el-col :span="12">
                  <el-statistic title="累计评价" :value="stats.totalReviews">
                    <template #suffix>
                      <span style="font-size: 14px;">条</span>
                    </template>
                  </el-statistic>
                </el-col>
                <el-col :span="12">
                  <el-statistic title="累计奖励" :value="stats.totalRewards">
                    <template #suffix>
                      <span style="font-size: 14px;">ETH</span>
                    </template>
                  </el-statistic>
                </el-col>
                <el-col :span="12">
                  <el-statistic title="优惠券" :value="stats.totalCoupons">
                    <template #suffix>
                      <span style="font-size: 14px;">张</span>
                    </template>
                  </el-statistic>
                </el-col>
              </el-row>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <!-- 优惠券列表 -->
        <div class="coupons-section">
          <h3>我的优惠券</h3>
          <el-empty v-if="coupons.length === 0" description="暂无优惠券" />
          <div v-else class="coupons-list">
            <el-card v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
              <div class="coupon-content">
                <div class="coupon-info">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-desc">{{ coupon.description }}</div>
                </div>
                <div class="coupon-action">
                  <el-tag :type="getCouponType(coupon.status)">
                    {{ getCouponStatusText(coupon.status) }}
                  </el-tag>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CopyDocument, Refresh, View } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { getRewardBalance } from '@/api/rewardApi'

const userStore = useUserStore()

const stats = ref({
  totalTransactions: 0,
  totalReviews: 0,
  totalRewards: '0',
  totalCoupons: 0
})

const coupons = ref([])

// 加载真实数据
async function loadData() {
  try {
    // 这里应该调用真实的API获取统计数据
    // 暂时使用默认值
    stats.value = {
      totalTransactions: 0,
      totalReviews: 0,
      totalRewards: '0',
      totalCoupons: 0
    }
    
    // 这里应该调用真实的API获取优惠券数据
    // 暂时使用空数组
    coupons.value = []
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 复制地址
function copyAddress() {
  navigator.clipboard.writeText(userStore.walletAddress).then(() => {
    ElMessage.success('地址已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 刷新余额
async function handleRefreshBalance() {
  try {
    const data = await getRewardBalance()
    userStore.setRewardBalance(data.balance || '0')
    ElMessage.success('余额已更新')
  } catch (error) {
    console.error('刷新余额失败:', error)
    if (!error.request) {
      ElMessage.error('刷新余额失败')
    }
  }
}

// 获取优惠券类型
function getCouponType(status) {
  const statusMap = {
    unused: 'success',
    used: 'info',
    expired: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取优惠券状态文本
function getCouponStatusText(status) {
  const statusMap = {
    unused: '未使用',
    used: '已使用',
    expired: '已过期'
  }
  return statusMap[status] || status
}

onMounted(() => {
  // 加载真实数据
  loadData()
  handleRefreshBalance()
})
</script>

<style scoped>
.account {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.account-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.account-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.account-info h3,
.account-stats h3,
.coupons-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #303133;
  font-weight: 600;
}

.address-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: monospace;
  font-weight: 500;
}

.account-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.account-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
}

.coupons-section {
  margin-top: 32px;
}

.coupons-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.coupon-card {
  margin-bottom: 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.coupon-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
}

.coupon-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.coupon-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.coupon-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
</style>

