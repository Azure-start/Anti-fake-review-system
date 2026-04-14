<template>
  <div class="transactions">
    <div class="container">
      <el-card header="交易记录" class="transactions-card">
        <!-- 后端状态提示 -->
        <el-alert
          v-if="backendStatus === 'error'"
          title="后端服务未连接，无法获取交易记录"
          type="error"
          :closable="false"
          class="backend-status-alert"
        />
        <div v-loading="loading">
          <el-table :data="transactions" stripe>
            <el-table-column prop="orderId" label="订单号" width="220" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="txHash" label="交易哈希" width="150">
              <template #default="{ row }">
                <el-link v-if="row.txHash" :href="`https://sepolia.etherscan.io/tx/${row.txHash}`" target="_blank" type="primary">
                  {{ formatHash(row.txHash) }}
                </el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'pending'"
                  type="warning"
                  size="small"
                  @click="handlePay(row)"
                >
                  去支付
                </el-button>
                <el-button
                  v-if="row.status === 'completed' && row.receiveStatus === 'confirmed' && row.reviewStatus !== 1"
                  type="primary"
                  size="small"
                  @click="handleReview(row)"
                >
                  去评价
                </el-button>
                <el-button
                  v-if="row.status === 'completed' && row.receiveStatus === 'pending'"
                  type="success"
                  size="small"
                  @click="handleConfirmReceive(row)"
                >
                  确认收货
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click="handleViewDetail(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="transactions.length === 0" description="暂无交易记录" />

          <!-- 分页 -->
          <div v-if="transactions.length > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next, jumper, total"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-card>
    </div>
    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="900px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderId }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ currentOrder.productName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易哈希">{{ currentOrder.txHash }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentOrder.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserTransactions, confirmReceipt } from '@/api/transactionApi'
import { useUserStore } from '@/stores/userStore'   // ← 引入
const userStore = useUserStore()                    // ← 实例化

const detailVisible = ref(false)
const currentOrder = ref({})
const backendStatus = ref('checking') // checking, connected, error


const router = useRouter()

const loading = ref(false)
const transactions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

function handleViewDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

// 加载交易记录
async function loadTransactions() {
  loading.value = true
  backendStatus.value = 'checking'
  
  try {
    console.log('📋 开始加载交易记录，用户地址:', userStore.walletAddress)
    
    if (!userStore.walletAddress) {
      console.warn('⚠️ 用户钱包地址为空，请先连接钱包')
      backendStatus.value = 'error'
      transactions.value = []
      total.value = 0
      ElMessage.warning('请先连接钱包')
      return
    }
    
    const data = await getUserTransactions({
      userAddress: userStore.walletAddress,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    // 成功获取数据
    backendStatus.value = 'connected'
    console.log('✅ 成功连接到后端服务')
    console.log('📊 后端返回数据:', data)
    
    transactions.value = data.list || []
    total.value = data.total || 0
    
    console.log(`📊 成功加载 ${transactions.value.length} 条交易记录`)
    
    // 打印交易记录详情供调试
    transactions.value.forEach((tx, index) => {
      console.log(`  交易[${index}]:`, {
        orderId: tx.orderId,
        productId: tx.productId,
        status: tx.status,
        receiveStatus: tx.receiveStatus,
        txHash: tx.txHash,
        productName: tx.productName
      })
    })
    
    // 验证交易哈希的一致性，显示警告信息
    let invalidHashCount = 0
    transactions.value.forEach((tx, index) => {
      if (tx.txHash) {
        // 检查格式
        if (!tx.txHash.startsWith('0x')) {
          console.error(`❌ 交易 ${tx.orderId} 的交易哈希格式错误 - 缺少0x前缀:`, tx.txHash)
          invalidHashCount++
        }
        // 检查长度
        else if (tx.txHash.length !== 66) {
          console.error(`❌ 交易 ${tx.orderId} 的交易哈希长度错误 - 期望66字符，实际${tx.txHash.length}字符:`, tx.txHash)
          invalidHashCount++
        }
        // 检查十六进制格式
        else if (!/^0x[0-9a-fA-F]{64}$/.test(tx.txHash)) {
          console.error(`❌ 交易 ${tx.orderId} 的交易哈希包含非法字符:`, tx.txHash)
          invalidHashCount++
        } else {
          console.log(`✅ 交易 ${tx.orderId} 的交易哈希验证通过:`, tx.txHash)
        }
      } else {
        console.log(`ℹ️ 交易 ${tx.orderId} 暂无交易哈希`)
      }
    })
    
    if (invalidHashCount > 0) {
      console.warn(`⚠️ 发现 ${invalidHashCount} 条交易记录的交易哈希格式无效`)
      ElMessage.warning(`发现 ${invalidHashCount} 条交易记录的交易哈希格式无效，请检查后端数据`)
    }
    
  } catch (error) {
    console.error('加载交易记录失败:', error)
    backendStatus.value = 'error'
    transactions.value = [] // 清空交易记录，不显示模拟数据
    total.value = 0
    ElMessage.error('加载交易记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
function getStatusType(status) {
  const statusMap = {
    pending: 'warning',
    completed: 'success',
    cancelled: 'info',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
function getStatusText(status) {
  const statusMap = {
    pending: '待支付',
    completed: '已完成',
    cancelled: '已取消',
    failed: '支付失败'
  }
  return statusMap[status] || status
}

// 格式化哈希
function formatHash(hash) {
  if (!hash) return '暂无'
  
  // 验证交易哈希格式
  if (!hash.startsWith('0x')) {
    console.warn('⚠️ 无效的交易哈希格式 - 缺少0x前缀:', hash)
    return '无效哈希'
  }
  
  if (hash.length !== 66) {
    console.warn(`⚠️ 交易哈希长度异常 - 期望66字符，实际${hash.length}字符:`, hash)
    return '无效哈希'
  }
  
  // 验证是否为有效的十六进制字符串
  const hexPattern = /^0x[0-9a-fA-F]{64}$/
  if (!hexPattern.test(hash)) {
    console.warn('⚠️ 交易哈希包含非法字符:', hash)
    return '无效哈希'
  }
  
  return `${hash.slice(0, 6)}...${hash.slice(-4)}`
}

// 格式化时间
function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 去支付
function handlePay(row) {
  // 跳转到支付页面，带上订单信息
  router.push({
    path: '/checkout',
    query: {
      productId: row.productId,
      orderId: row.orderId,
      from: 'transactions'
    }
  })
}

// 去评价
function handleReview(row) {
  router.push({
    path: `/review/${row.productId}`,
    query: { 
      from: 'transactions',
      orderId: row.orderId 
    }
  })
}

// 确认收货
async function handleConfirmReceive(row) {
  try {
    console.log('🔄 开始确认收货，订单ID:', row.orderId, '用户地址:', userStore.walletAddress)
    
    // 调用后端API确认收货
    const result = await confirmReceipt(row.orderId, userStore.walletAddress)
    
    // 后端返回的是 code 和 message，需要检查 code === 0 表示成功
    if (result.code === 0) {
      ElMessage.success('确认收货成功，现在可以评价了')
      // 更新本地数据状态
      row.receiveStatus = 'confirmed'
      console.log('✅ 确认收货成功，订单ID:', row.orderId)
    } else {
      ElMessage.error(result.message || '确认收货失败')
      console.error('❌ 确认收货失败，订单ID:', row.orderId, '错误信息:', result.message)
    }
  } catch (error) {
    console.error('❌ 确认收货API调用失败:', error)
    ElMessage.error('确认收货失败，请稍后重试')
  }
}

// 查看详情
// function handleViewDetail(row) {
//   // 可以打开详情对话框
//   console.log('查看详情:', row)
// }

// 分页改变
function handlePageChange() {
  loadTransactions()
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.transactions {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.transactions-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.transactions-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.transactions-card :deep(.el-table) {
  border-radius: 0;
}

.transactions-card :deep(.el-table__header th) {
  background: #fafafa;
  color: #303133;
  font-weight: 600;
  border-bottom: 2px solid #ebeef5;
}

.transactions-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

.transactions-card :deep(.el-table--striped .el-table__body tr:hover > td) {
  background: #ecf5ff;
}

.backend-status-alert {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
}
</style>

