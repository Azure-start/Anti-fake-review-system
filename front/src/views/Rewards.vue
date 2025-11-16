<template>
  <div class="rewards">
    <div class="container">
      <el-row :gutter="20">
        <!-- 左侧：余额信息 -->
        <el-col :xs="24" :sm="8">
          <el-card header="余额信息" class="balance-card">
            <div class="balance-content">
              <div class="balance-item">
                <div class="balance-label">测试币余额</div>
                <div class="balance-value">{{ userStore.balance }} ETH</div>
              </div>
              <el-divider />
              <div class="balance-item">
                <div class="balance-label">奖励余额</div>
                <div class="balance-value reward">{{ userStore.rewardBalance }} ETH</div>
              </div>
              <el-button type="primary" @click="handleRefreshBalance">刷新余额</el-button>
            </div>
          </el-card>

          <el-card header="兑换优惠券" class="exchange-card" style="margin-top: 20px;">
            <el-form :model="exchangeForm" @submit.prevent="handleExchange">
              <el-form-item>
                <el-input v-model="exchangeForm.code" placeholder="请输入兑换码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleExchange" style="width: 100%;">
                  兑换
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧：奖励记录 -->
        <el-col :xs="24" :sm="16">
          <el-card header="奖励记录" class="history-card">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="全部" name="all">
                <RewardList :list="allRewards" />
              </el-tab-pane>
              <el-tab-pane label="评价奖励" name="review">
                <RewardList :list="reviewRewards" />
              </el-tab-pane>
              <el-tab-pane label="邀请奖励" name="invite">
                <RewardList :list="inviteRewards" />
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { getRewardBalance, getRewardHistory, exchangeCoupon } from '@/api/rewardApi'
import RewardList from './components/RewardList.vue'

const userStore = useUserStore()
const activeTab = ref('all')
const rewards = ref([])

const exchangeForm = ref({
  code: ''
})

const allRewards = computed(() => rewards.value)
const reviewRewards = computed(() => rewards.value.filter(r => r.type === 'review'))
const inviteRewards = computed(() => rewards.value.filter(r => r.type === 'invite'))

// 加载奖励记录
async function loadRewards() {
  try {
    const data = await getRewardHistory()
    rewards.value = data.list || []
  } catch (error) {
    console.error('加载奖励记录失败:', error)
  }
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

// 兑换优惠券
async function handleExchange() {
  if (!exchangeForm.value.code) {
    ElMessage.warning('请输入兑换码')
    return
  }

  try {
    await exchangeCoupon({ code: exchangeForm.value.code })
    ElMessage.success('兑换成功')
    exchangeForm.value.code = ''
    handleRefreshBalance()
  } catch (error) {
    console.error('兑换失败:', error)
    if (!error.request) {
      ElMessage.error('兑换失败，请检查兑换码是否正确')
    }
  }
}

onMounted(() => {
  loadRewards()
  handleRefreshBalance()
})
</script>

<style scoped>
.rewards {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.balance-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.balance-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.balance-content {
  text-align: center;
  padding: 20px;
}

.balance-item {
  padding: 24px 0;
}

.balance-label {
  font-size: 15px;
  color: #909399;
  margin-bottom: 12px;
  font-weight: 500;
}

.balance-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}

.balance-value.reward {
  color: #f56c6c;
}

.balance-content :deep(.el-button) {
  width: 100%;
  margin-top: 16px;
  border-radius: 12px;
  font-weight: 600;
}

.exchange-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.exchange-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.exchange-card :deep(.el-button) {
  width: 100%;
  border-radius: 12px;
  font-weight: 600;
}

.history-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.history-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.history-card :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: #fafafa;
}

.history-card :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.history-card :deep(.el-tabs__active-bar) {
  background: #409eff;
}
</style>

