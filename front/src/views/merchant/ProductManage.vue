<template>
  <div class="product-manage">
    <div class="container">
      <el-card header="商品管理" class="product-card">
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            上架商品
          </el-button>
        </div>

        <div v-loading="loading">
          <el-table :data="products" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商品名称" min-width="200" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上架时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button
                  v-if="row.status === 'onSale'"
                  type="warning"
                  size="small"
                  text
                  @click="handleOffShelf(row)"
                >
                  下架
                </el-button>
                <el-button
                  v-if="row.status === 'offShelf'"
                  type="success"
                  size="small"
                  text
                  @click="handleOnShelf(row)"
                >
                  上架
                </el-button>
                <el-button type="danger" size="small" text @click="handleDelete(row)">
                  删除
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
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMyProducts,
  deleteProduct,
  offShelfProduct,
  createProduct,
  updateProduct
} from '@/api/merchantApi'

const router = useRouter()

const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载商品列表
async function loadProducts() {
  loading.value = true
  
  try {
    const result = await getMyProducts({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    
    if (result.code === 0) {
      products.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加商品
function handleAdd() {
  router.push('/merchant/product/add')
}

// 编辑商品
function handleEdit(row) {
  router.push(`/merchant/product/edit/${row.id}`)
}

// 下架商品
async function handleOffShelf(row) {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await offShelfProduct(row.id)
    if (result.code === 0) {
      ElMessage.success('下架成功')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架商品失败:', error)
    }
  }
}

// 上架商品
async function handleOnShelf(row) {
  try {
    const result = await updateProduct(row.id, { status: 'onSale' })
    if (result.code === 0) {
      ElMessage.success('上架成功')
      await loadProducts()
    }
  } catch (error) {
    console.error('上架商品失败:', error)
  }
}

// 删除商品
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await deleteProduct(row.id)
    if (result.code === 0) {
      ElMessage.success('删除成功')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

function getStatusType(status) {
  const statusMap = {
    onSale: 'success',
    offShelf: 'warning',
    deleted: 'danger'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    onSale: '在售',
    offShelf: '已下架',
    deleted: '已删除'
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
.product-manage {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.product-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.toolbar {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
}
</style>

