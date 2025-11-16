<template>
  <div class="user-manage">
    <div class="container">
      <el-card header="用户管理" class="user-card">
        <div v-loading="loading">
          <el-table :data="users" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="address" label="钱包地址" width="300">
              <template #default="{ row }">{{ formatAddress(row.address) }}</template>
            </el-table-column>
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="120">
              <template #default="{ row }">{{ row.balance }} ETH</template>
            </el-table-column>
            <el-table-column prop="shopName" label="店铺名称" min-width="150">
              <template #default="{ row }">{{ row.shopName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.role !== 'admin'"
                  type="danger"
                  size="small"
                  text
                  @click="handleBan(row)"
                >
                  封禁
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="users.length > 0" class="pagination">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList } from '@/api/adminApi'
import { formatAddress } from '@/utils/chainHelper'

const loading = ref(false)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载用户列表
async function loadUsers() {
  loading.value = true
  
  try {
    const result = await getUserList({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (result.code === 0) {
      users.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function getRoleType(role) {
  const roleMap = {
    user: 'info',
    merchant: 'success',
    admin: 'danger'
  }
  return roleMap[role] || 'info'
}

function getRoleText(role) {
  const roleMap = {
    user: '用户',
    merchant: '商家',
    admin: '管理员'
  }
  return roleMap[role] || role
}

async function handleBan(row) {
  try {
    await ElMessageBox.confirm('确定要封禁该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    ElMessage.success('封禁成功')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('封禁用户失败:', error)
    }
  }
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

function handlePageChange() {
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.user-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.user-card :deep(.el-card__header) {
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

