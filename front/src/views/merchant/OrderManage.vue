<template>
  <div class="order-manage">
    <div class="container">
      <el-card header="订单管理" class="order-card">
        <div class="search-form">
          <el-form :inline="true" :model="searchParams" class="demo-form-inline">
            <el-form-item label="订单号">
              <el-input v-model="searchParams.orderId" placeholder="输入订单号" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="商品名称">
              <el-input v-model="searchParams.productName" placeholder="输入商品名称" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="买家地址">
              <el-input v-model="searchParams.customerAddress" placeholder="输入买家地址" clearable style="width: 200px;"></el-input>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchParams.status" placeholder="选择状态" clearable style="width: 120px;">
                <el-option label="待支付" value="pending"></el-option>
                <el-option label="已完成" value="completed"></el-option>
                <el-option label="已取消" value="cancelled"></el-option>
                <el-option label="支付失败" value="failed"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              ></el-date-picker>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-loading="loading">
          <el-table :data="orders" stripe>
            <el-table-column prop="orderId" label="订单号" width="200" />
            <el-table-column prop="productName" label="商品名称" min-width="200" />
            <el-table-column prop="userAddress" label="买家地址" width="200">
              <template #default="{ row }">{{ formatAddress(row.userAddress) }}</template>
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
              :current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              :page-sizes="[5, 10, 20, 50]"
              layout="prev, pager, next, sizes, jumper, total"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
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
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchParams = ref({
  orderId: '',
  productName: '',
  customerAddress: '',
  status: ''
})
const dateRange = ref([])

// 加载订单列表
async function loadOrders() {
  loading.value = true
  
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      merchantAddress: userStore.walletAddress,
      ...searchParams.value
    }
    
    // 添加时间范围条件
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const result = await getShopOrders(params)
    
    if (result.code === 0) {
      orders.value = result.list || []
      total.value = result.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadOrders()
}

// 重置
function handleReset() {
  searchParams.value = {
    orderId: '',
    productName: '',
    customerAddress: '',
    status: ''
  }
  dateRange.value = []
  currentPage.value = 1
  loadOrders()
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

function handlePageChange(page) {
  currentPage.value = page
  loadOrders()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
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

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 15px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
}
</style>

