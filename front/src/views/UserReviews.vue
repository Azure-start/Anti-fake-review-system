<template>
  <div class="user-reviews">
    <div class="container">
      <el-card header="我的评价" class="reviews-card">
        <div v-loading="loading">
          <el-table :data="reviews" stripe>
            <el-table-column prop="productName" label="商品" />
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" />
            <el-table-column prop="createdAt" label="评价时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
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
          <el-empty v-if="reviews.length === 0" description="暂无评价" />
          <div v-if="reviews.length > 0" class="pagination">
            <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next, jumper, total"
                @current-change="loadReviews"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 评价详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品">{{ currentReview.productName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentReview.content }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ formatTime(currentReview.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { getUserReviews } from '@/api/rewardApi'

const userStore = useUserStore()

const loading = ref(false)
const reviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentReview = ref({})

// 格式化时间
function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 加载评价
async function loadReviews() {
  loading.value = true
  
  try {
    const data = await getUserReviews({
      userAddress: userStore.walletAddress,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    reviews.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载评价失败:', error)
  } finally {
    loading.value = false
  }
}

function handleViewDetail(row) {
  currentReview.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.user-reviews {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.reviews-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}
.reviews-card :deep(.el-card__header) {
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
  background: rgba(255, 255, 255, 0.8);
}
</style>