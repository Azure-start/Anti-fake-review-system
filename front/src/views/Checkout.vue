<template>
  <div class="checkout">
    <div class="container">
      <el-card header="确认订单" class="checkout-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="orderData">
          <el-steps :active="currentStep" finish-status="success">
            <el-step title="确认订单" />
            <el-step title="确认支付" />
            <el-step title="支付成功" />
          </el-steps>

          <el-divider />

          <!-- 订单信息 -->
          <div v-if="currentStep === 0" class="order-section">
            <h3>商品信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品名称">{{ orderData.productName }}</el-descriptions-item>
              <el-descriptions-item label="商品价格">¥{{ orderData.price }}</el-descriptions-item>
              <el-descriptions-item label="商品规格" :span="2">{{ orderData.specText }}</el-descriptions-item>
            </el-descriptions>

            <h3 style="margin-top: 24px;">收货信息</h3>
            <el-form :model="addressForm" label-width="100px">
              <el-form-item label="收货人">
                <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="addressForm.phone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="收货地址">
                <el-input
                  v-model="addressForm.address"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入详细地址"
                />
              </el-form-item>
            </el-form>

            <div class="order-total">
              <div class="total-item">
                <span>合计：</span>
                <span class="total-price">¥{{ orderData.totalAmount }}</span>
              </div>
            </div>

            <div v-if="isSelfPurchase" class="self-purchase-alert" style="margin-top: 12px;">
              <el-alert
                type="warning"
                :closable="false"
                show-icon
                title="商家无法购买自家商品"
                description="该商品属于您的店铺，无法下单。请返回商品页选择其他店铺的商品。"
              />
            </div>

            <div class="order-actions">
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="handleConfirmOrder" :disabled="isSelfPurchase">确认订单</el-button>
            </div>
          </div>

          <!-- 支付状态 -->
          <div v-else-if="currentStep === 1" class="payment-section">
            <el-result
              :icon="paymentStatus === 'pending' ? 'info' : paymentStatus === 'success' ? 'success' : 'error'"
              :title="paymentStatusText"
              :sub-title="paymentSubText"
            >
              <template #extra v-if="paymentStatus === 'pending'">
                <div class="payment-info">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="订单号">{{ orderData.orderId }}</el-descriptions-item>
                    <el-descriptions-item label="支付金额">¥{{ orderData.totalAmount }}</el-descriptions-item>
                    <el-descriptions-item label="TX哈希">{{ orderData.txHash || '处理中...' }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                <el-button type="primary" @click="handleCheckPayment">刷新状态</el-button>
              </template>
            </el-result>
          </div>

          <!-- 支付成功 -->
          <div v-else-if="currentStep === 2" class="success-section">
            <el-result
              icon="success"
              title="支付成功"
              sub-title="您的订单已成功支付，请等待收货后确认收货，然后可以发布评价"
            >
              <template #extra>
                <div class="success-actions">
                  <el-button @click="handleBackHome">返回首页</el-button>
                  <el-button type="primary" @click="handleViewTransactions">查看订单</el-button>
                </div>
              </template>
            </el-result>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTransaction } from '@/api/transactionApi'
import { getProductDetail } from '@/api/productApi'
import { useUserStore } from '@/stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orderData = ref(null)
const currentStep = ref(0)
const paymentStatus = ref('pending') // pending, success, failed

const addressForm = ref({
  name: '',
  phone: '',
  address: ''
})

const paymentStatusText = computed(() => {
  switch (paymentStatus.value) {
    case 'pending':
      return '等待支付'
    case 'success':
      return '支付成功'
    case 'failed':
      return '支付失败'
    default:
      return '处理中'
  }
})

const paymentSubText = computed(() => {
  switch (paymentStatus.value) {
    case 'pending':
      return '请在MetaMask中确认交易'
    case 'success':
      return '恭喜您，订单已支付成功'
    case 'failed':
      return '支付失败，请重试'
    default:
      return ''
  }
})

const isSelfPurchase = computed(() => {
  if (!orderData.value) return false
  const merchantAddr = orderData.value.merchantAddress?.toLowerCase()
  const userAddr = userStore.walletAddress?.toLowerCase()
  return userStore.isMerchant && merchantAddr && userAddr && merchantAddr === userAddr
})

// 初始化订单数据
async function initOrder() {
  loading.value = true
  
  try {
    const productId = route.query.productId
    if (!productId) {
      ElMessage.error('缺少商品信息')
      router.push('/')
      return
    }

    const product = await getProductDetail(productId)
    const spec = JSON.parse(route.query.spec || '{}')
    const specText = Object.entries(spec).map(([key, value]) => `${key}:${value}`).join(', ') || '默认规格'

    orderData.value = {
      productId: product.id,
      productName: product.name,
      price: product.price,
      spec,
      specText,
      totalAmount: product.price,
      merchantName: product.merchantName,
      merchantAddress: product.merchantAddress
    }

    // 商家不可购买自家商品 - 直接拦截并返回商品详情页
    if (userStore.isMerchant && product.merchantAddress && userStore.walletAddress && product.merchantAddress.toLowerCase() === userStore.walletAddress.toLowerCase()) {
      ElMessage.warning('商家不能购买自家商品')
      router.replace(`/product/${product.id}`)
      return
    }
  } catch (error) {
    console.error('初始化订单失败:', error)
    if (!error.request) {
      ElMessage.error('加载订单信息失败')
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 确认订单
async function handleConfirmOrder() {
  if (!addressForm.value.name || !addressForm.value.phone || !addressForm.value.address) {
    ElMessage.warning('请填写完整收货信息')
    return
  }

  // 商家不可购买自家商品（兜底校验）
  if (isSelfPurchase.value) {
    ElMessage.warning('商家不能购买自家商品')
    router.push(`/product/${orderData.value.productId}`)
    return
  }

  try {
    ElMessage.info('正在创建订单...')
    
    const result = await createTransaction({
      productId: orderData.value.productId,
      spec: orderData.value.spec,
      address: addressForm.value
    })

    orderData.value.orderId = result.orderId
    orderData.value.txHash = result.txHash
    
    currentStep.value = 1
    ElMessage.success('订单创建成功，请在MetaMask中确认支付')
    
    // 监听支付状态
    pollPaymentStatus()
  } catch (error) {
    console.error('创建订单失败:', error)
    if (!error.request) {
      ElMessage.error('创建订单失败')
    }
  }
}

// 轮询支付状态
async function pollPaymentStatus() {
  let attempts = 0
  const maxAttempts = 30 // 最多尝试30次

  const interval = setInterval(async () => {
    attempts++
    
    try {
      // 这里应该调用后端接口检查交易状态
      // 暂时模拟
      if (attempts >= 5) {
        paymentStatus.value = 'success'
        currentStep.value = 2
        clearInterval(interval)
        ElMessage.success('支付成功')
      }
      
      if (attempts >= maxAttempts) {
        clearInterval(interval)
        paymentStatus.value = 'failed'
        ElMessage.error('支付超时，请检查交易状态')
      }
    } catch (error) {
      console.error('检查支付状态失败:', error)
    }
  }, 2000)
}

// 检查支付状态
async function handleCheckPayment() {
  // 重新轮询
  pollPaymentStatus()
}

// 取消订单
function handleCancel() {
  ElMessageBox.confirm('确定要取消订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.back()
  }).catch(() => {})
}

// 返回首页
function handleBackHome() {
  router.push('/')
}

// 去评价
function handleGoReview() {
  router.push(`/review/${orderData.value.productId}`)
}

// 查看订单
function handleViewTransactions() {
  router.push('/transactions')
}

onMounted(() => {
  initOrder()
})
</script>

<style scoped>
.checkout {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.checkout-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.checkout-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.loading-container {
  padding: 40px 0;
}

.order-section,
.payment-section,
.success-section {
  padding: 30px;
}

.order-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #303133;
  font-weight: 600;
}

.order-total {
  margin-top: 30px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f2f5 0%, #eef1f8 100%);
  border-radius: 16px;
  text-align: right;
}

.total-item {
  font-size: 18px;
  font-weight: 500;
  color: #606266;
}

.total-price {
  font-size: 36px;
  font-weight: bold;
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-left: 8px;
}

.order-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.order-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 32px;
}

.payment-info {
  margin: 30px 0;
}

.success-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.success-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 32px;
}
</style>

