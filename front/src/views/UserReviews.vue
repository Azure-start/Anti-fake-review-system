<template>
  <div class="user-reviews">
    <div class="container">
      <el-card header="我的评价" class="reviews-card">
        <div v-loading="loading">
          <el-table :data="reviews" stripe>
            <el-table-column prop="productName" label="商品" />
            <el-table-column prop="rating" label="评分" width="150">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" />
            <el-table-column label="区块链状态" width="120">
              <template #default="{ row }">
                <el-tag 
                  :type="row.txHash ? 'success' : 'warning'"
                  size="small"
                >
                  {{ row.txHash ? '已上链' : '未上链' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="评价时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  text
                  @click="handleViewDetail(row)"
                >
                  详情
                </el-button>
                <el-button
                  v-if="!row.txHash"
                  type="warning"
                  size="small"
                  text
                  @click="handleUploadToBlockchain(row)"
                  :loading="uploadLoading[row.id]"
                >
                  上链
                </el-button>
                <el-button
                  v-else
                  type="success"
                  size="small"
                  text
                  @click="handleViewBlockchain(row)"
                >
                  链上详情
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
        <el-descriptions-item label="区块链状态" :span="2">
          <el-tag :type="currentReview.txHash ? 'success' : 'warning'">
            {{ currentReview.txHash ? '已上链' : '未上链' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentReview.txHash" label="交易哈希" :span="2">
          <el-link type="primary" :href="`https://testnet.bscscan.com/tx/${currentReview.txHash}`" target="_blank">
            {{ currentReview.txHash.substring(0, 10) }}...{{ currentReview.txHash.substring(currentReview.txHash.length - 8) }}
          </el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 区块链详情弹窗 -->
    <el-dialog v-model="blockchainDetailVisible" title="区块链详情" width="700px">
      <div v-if="blockchainData.blockchainStatus === '已上链'">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="评论ID">{{ blockchainData.reviewId }}</el-descriptions-item>
          <el-descriptions-item label="商品ID">{{ blockchainData.productId }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ blockchainData.rating }}</el-descriptions-item>
          <el-descriptions-item label="评价内容">{{ blockchainData.content }}</el-descriptions-item>
          <el-descriptions-item label="用户地址">{{ blockchainData.userAddress }}</el-descriptions-item>
          <el-descriptions-item label="交易哈希">
            <el-link type="primary" :href="`https://testnet.bscscan.com/tx/${blockchainData.txHash}`" target="_blank">
              {{ blockchainData.txHash }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(blockchainData.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h4>区块链数据</h4>
        <el-card>
          <pre>{{ JSON.stringify(blockchainData.blockchainData, null, 2) }}</pre>
        </el-card>
      </div>
      <div v-else-if="blockchainData.blockchainStatus === '调试信息'">
        <el-result
          icon="info"
          title="调试信息"
          :sub-title="blockchainData.message"
        >
          <template #extra>
            <el-button type="primary" @click="blockchainDetailVisible = false">关闭</el-button>
          </template>
        </el-result>
        
        <el-divider />
        
        <h4>详细调试数据</h4>
        <el-card>
          <pre>{{ JSON.stringify(blockchainData.debugInfo, null, 2) }}</pre>
        </el-card>
      </div>
      <div v-else>
        <el-result
          icon="info"
          title="未上链"
          :sub-title="blockchainData.message || '该评论尚未上传到区块链'"
        >
          <template #extra>
            <el-button type="primary" @click="blockchainDetailVisible = false">关闭</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { getUserReviews } from '@/api/rewardApi'
import { uploadReviewToBlockchain, debugBlockchainStatus } from '@/api/blockchainApi'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const reviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentReview = ref({})
const blockchainDetailVisible = ref(false)
const blockchainData = ref({})
const uploadLoading = ref({})

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

// 上传到区块链
async function handleUploadToBlockchain(row) {
  try {
    await ElMessageBox.confirm(
      `确定要将评价 "${row.content.substring(0, 20)}..." 上传到区块链吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    uploadLoading.value[row.id] = true
    
    const result = await uploadReviewToBlockchain(row.id)
    
    if (result.code === 0) {
      ElMessage.success('上传成功')
      // 更新本地数据
      row.txHash = result.txHash
      row.blockchainStatus = '已上链'
    } else {
      ElMessage.warning(result.message || '上传失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('上传区块链失败:', error)
      ElMessage.error('上传区块链失败')
    }
  } finally {
    uploadLoading.value[row.id] = false
  }
}

// 查看区块链详情（整合调试功能）
async function handleViewBlockchain(row) {
  console.log('🐛 查看区块链详情，评价ID:', row.id)
  
  try {
    loading.value = true
    
    console.log('🚀 调用调试接口...')
    const debugResult = await debugBlockchainStatus(row.id)
    console.log('✅ 调试接口返回:', debugResult)
    
    // 显示调试结果
    ElMessage.success('区块链详情已获取')
    
    // 在弹窗中显示调试信息
    blockchainData.value = {
      blockchainStatus: '调试信息',
      debugInfo: debugResult,
      message: '区块链查询完成'
    }
    blockchainDetailVisible.value = true
    
  } catch (error) {
    console.error('❌ 区块链详情获取失败:', error)
    
    let errorDetails = []
    if (error) {
      if (typeof error === 'string') {
        errorDetails.push(error)
      } else {
        if (error.message) errorDetails.push(error.message)
        if (error.code) errorDetails.push(`Code: ${error.code}`)
        if (error.response) errorDetails.push(`Response: ${JSON.stringify(error.response)}`)
      }
    }
    
    const errorMsg = `区块链详情获取失败: ${errorDetails.join(', ') || '未知错误'}`
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
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