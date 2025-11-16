<template>
  <div class="transactions">
    <div class="container">
      <el-card header="交易记录" class="transactions-card">
        <div v-loading="loading">
          <el-table :data="transactions" stripe>
            <el-table-column prop="orderId" label="订单号" width="200" />
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
                  v-if="row.status === 'completed' && row.receiveStatus === 'confirmed'"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserTransactions } from '@/api/transactionApi'

const router = useRouter()

const loading = ref(false)
const transactions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载交易记录
async function loadTransactions() {
  loading.value = true
  
  try {
    const data = await getUserTransactions({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    transactions.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载交易记录失败:', error)
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
  if (!hash) return '-'
  return `${hash.slice(0, 6)}...${hash.slice(-4)}`
}

// 格式化时间
function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 去评价
function handleReview(row) {
  router.push(`/review/${row.productId}`)
}

// 确认收货
function handleConfirmReceive(row) {
  ElMessage.success('确认收货成功，现在可以评价了')
  // 这里应该调用API更新收货状态，Mock模式下直接更新本地数据
  if (row.receiveStatus === 'pending') {
    row.receiveStatus = 'confirmed'
  }
}

// 查看详情
function handleViewDetail(row) {
  // 可以打开详情对话框
  console.log('查看详情:', row)
}

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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
}
</style>

