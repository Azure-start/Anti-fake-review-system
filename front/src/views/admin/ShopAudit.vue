<template>
  <div class="shop-audit">
    <div class="container">
      <el-card header="商家审核" class="audit-card">
        <div v-loading="loading">
          <el-table :data="applications" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantAddress" label="商家地址" width="300">
              <template #default="{ row }">{{ formatAddress(row.merchantAddress) }}</template>
            </el-table-column>
            <el-table-column prop="shopName" label="店铺名称" min-width="150" />
            <el-table-column prop="shopDescription" label="店铺描述" min-width="200" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="申请时间" width="180">
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

          <div v-if="applications.length > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next, jumper, total"
              @current-change="handlePageChange"
            />
          </div>

          <el-empty v-if="applications.length === 0 && !loading" description="暂无待审核申请" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShopApplications, auditShopApplication } from '@/api/adminApi'
import { formatAddress } from '@/utils/chainHelper'

const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载申请列表
async function loadApplications() {
  loading.value = true
  
  try {
    const result = await getShopApplications({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (result.code === 0) {
      applications.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 审核通过
async function handleApprove(row) {
  try {
    await ElMessageBox.confirm('确定要通过该店铺申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await auditShopApplication(row.id, 'approve')
    if (result.code === 0) {
      ElMessage.success('审核通过')
      await loadApplications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

// 审核拒绝
async function handleReject(row) {
  try {
    await ElMessageBox.confirm('确定要拒绝该店铺申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await auditShopApplication(row.id, 'reject')
    if (result.code === 0) {
      ElMessage.success('已拒绝')
      await loadApplications()
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
    approved: '已通过',
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
  loadApplications()
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.shop-audit {
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

