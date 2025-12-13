<template>
  <div class="product-audit">
    <div class="container">
      <el-card header="商品审核" class="audit-card">
        <div v-loading="loading">
          <el-table :data="products" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商品名称" min-width="180" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="merchantAddress" label="商家地址" min-width="280">
              <template #default="{ row }">{{ formatAddress(row.merchantAddress) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'pending'"
                  type="success"
                  size="small"
                  @click="handleApprove(row)"
                >
                  通过
                </el-button>
                <el-button
                  v-if="row.status === 'pending'"
                  type="danger"
                  size="small"
                  @click="handleReject(row)"
                >
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="products.length > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next, jumper, total"
              @current-change="handlePageChange"
            />
          </div>

          <el-empty v-if="products.length === 0 && !loading" description="暂无待审核商品" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingProducts, auditProduct } from '@/api/adminApi'
import { formatAddress } from '@/utils/chainHelper'

const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadProducts() {
  loading.value = true
  try {
    const result = await getPendingProducts({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    products.value = result.list || []
    total.value = result.total || 0
    currentPage.value = result.page || 1
  } catch (error) {
    console.error('加载待审核商品失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确定要通过该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const result = await auditProduct(row.id, 'approve')
    if (result.code === 0 || result.id) {
      ElMessage.success('审核通过')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定要拒绝该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const result = await auditProduct(row.id, 'reject')
    if (result.code === 0 || result.id) {
      ElMessage.success('已拒绝')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

function getStatusType(status) {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

function handlePageChange() {
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-audit {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.audit-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.audit-card :deep(.el-card__header) {
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

