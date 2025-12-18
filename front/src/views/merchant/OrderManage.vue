<template>
  <div class="order-manage">
    <div class="container">
      <el-card header="订单管理" class="order-card">
        <div v-loading="loading">
          <el-table :data="orders" stripe>
            <el-table-column prop="orderId" label="订单号" width="200" />
            <el-table-column prop="productName" label="商品名称" min-width="200" />
            <el-table-column prop="customerAddress" label="买家地址" width="200">
              <template #default="{ row }">{{ formatAddress(row.customerAddress) }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">¥{{ row.amount }}</template>
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
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>

          <div v-if="orders.length > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next, jumper, total"
              @current-change="handlePageChange"
            />
          </div>

          <el-empty v-if="orders.length === 0 && !loading" description="暂无订单" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getShopOrders } from '@/api/merchantApi'
import { formatAddress } from '@/utils/chainHelper'

const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载订单列表
async function loadOrders() {
  loading.value = true
  
  try {
    const result = await getShopOrders({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (result.code === 0) {
      orders.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

function getStatusType(status) {
  const statusMap = {
    pending: 'warning',
    completed: 'success',
    cancelled: 'info',
    failed: 'danger'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    pending: '待支付',
    completed: '已完成',
    cancelled: '已取消',
    failed: '支付失败'
  }
  return statusMap[status] || status
}

function formatHash(hash) {
  if (!hash) return '-'
  return `${hash.slice(0, 6)}...${hash.slice(-4)}`
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

function handlePageChange() {
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-manage {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.order-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.order-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
}
</style>

