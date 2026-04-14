<template>
  <div class="product-detail">
    <div class="container">
      <div class="page-controls" style="margin-bottom: 16px;">
        <el-button type="text" :icon="ArrowLeft" @click="goBack">返回</el-button>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 商品详情 -->
      <div v-else-if="product" class="detail-content">
        <el-row :gutter="40">
          <!-- 左侧：商品图片 -->
          <el-col :xs="24" :sm="12">
            <div class="product-images">
              <el-carousel height="500px" indicator-position="outside">
                <el-carousel-item v-for="(image, index) in productImages" :key="index">
                  <el-image
                    :src="image"
                    fit="cover"
                    :preview-src-list="productImages"
                    :initial-index="index"
                  />
                </el-carousel-item>
              </el-carousel>
            </div>
          </el-col>

          <!-- 右侧：商品信息 -->
          <el-col :xs="24" :sm="12">
            <div class="product-info">
              <h1 class="product-title">{{ product.name }}</h1>
              
              <div class="product-rating">
                <el-rate v-model="product.rating" disabled show-score text-color="#ff9900" />
                <span class="sales">已售 {{ product.sales || 0 }}</span>
              </div>

              <div class="product-price">
                <span class="price">¥{{ product.price }}</span>
              </div>

              <el-divider />

              <!-- 规格选择 -->
              <div class="product-spec" v-if="product.specs && product.specs.length > 0">
                <div v-for="spec in product.specs" :key="spec.name" class="spec-item">
                  <div class="spec-label">{{ spec.name }}:</div>
                  <el-radio-group v-model="selectedSpec[spec.name]">
                    <el-radio-button
                      v-for="option in spec.options"
                      :key="option"
                      :label="option"
                    />
                  </el-radio-group>
                </div>
              </div>

              <!-- 购买按钮 -->
              <div class="product-actions">
                <el-alert
                  v-if="isSelfPurchase"
                  type="warning"
                  :closable="false"
                  show-icon
                  title="商家无法购买自家商品"
                  description="您当前为商家身份，且该商品由您店铺发布。请购买其他店铺的商品。"
                  style="margin-bottom: 12px;"
                />
                <el-button
                  type="primary"
                  size="large"
                  :icon="ShoppingCart"
                  @click="handleBuy"
                  :disabled="!canBuy"
                >
                  立即购买
                </el-button>
              </div>

              <el-divider />

              <!-- 商家信息 -->
              <div class="merchant-info">
                <h3>商家信息</h3>
                <el-descriptions :column="1">
                  <el-descriptions-item label="商家名称">{{ product.shopName || '官方店铺' }}</el-descriptions-item>
                  <el-descriptions-item label="商家地址">{{ formatAddress(product.merchantAddress) }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 商品描述 -->
        <el-card class="description-card" header="商品描述">
          <div class="description-content">{{ product.description }}</div>
        </el-card>

        <!-- 评价列表 -->
        <el-card class="reviews-card" header="用户评价">
          <div v-if="reviews.length > 0" class="reviews-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="review-user">
                  <el-avatar :size="40">{{ review.userAddress.slice(2, 4).toUpperCase() }}</el-avatar>
                  <div class="user-info">
                    <div class="user-address">{{ formatAddress(review.userAddress) }}</div>
                    <div class="review-time">{{ formatTime(review.createdAt) }}</div>
                  </div>
                </div>
                <el-rate v-model="review.rating" disabled />
              </div>
              <div class="review-content">{{ review.content }}</div>
              <div v-if="getReviewImages(review).length > 0" class="review-images">
                <el-image
                  v-for="(img, idx) in getReviewImages(review)"
                  :key="idx"
                  :src="img"
                  fit="cover"
                  :preview-src-list="getReviewImages(review)"
                  :initial-index="idx"
                  class="review-image"
                />
              </div>
              <div v-if="review.nftId" class="review-nft">
                <el-tag type="success" size="small">
                  <el-icon><Star /></el-icon>
                  已上链·NFT ID: {{ review.nftId }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无评价" />
        </el-card>
      </div>

      <!-- 空状态 -->
      <el-result v-else icon="error" title="商品不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star, ArrowLeft } from '@element-plus/icons-vue'
import { useProductStore } from '@/stores/productStore'
import { useUserStore } from '@/stores/userStore'
import { getProductDetail, getProductReviews } from '@/api/productApi'
import { formatAddress } from '@/utils/chainHelper'

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const userStore = useUserStore()

const loading = ref(false)
const product = ref(null)
const reviews = ref([])
const selectedSpec = ref({})

const productImages = computed(() => {
  if (!product.value) return []
  
  // 统一处理商品图片数据
  let images = product.value.images
  
  if (!images) {
    // 如果没有 images 字段，使用 image 字段
    return product.value.image ? [product.value.image] : []
  }
  
  // 如果是字符串，尝试解析
  if (typeof images === 'string') {
    try {
      const parsed = parseJSONString(images)
      if (Array.isArray(parsed)) {
        return parsed.filter(img => img && typeof img === 'string')
      } else if (typeof parsed === 'string') {
        return parsed ? [parsed] : []
      }
      return []
    } catch (e) {
      // 解析失败，作为单个图片处理
      return images.trim() ? [images.trim()] : []
    }
  }
  
  // 如果是数组，直接返回
  if (Array.isArray(images)) {
    return images.filter(img => img && typeof img === 'string')
  }
  
  return []
})

const isSelfPurchase = computed(() => {
  if (!product.value) return false
  const merchantAddr = product.value.merchantAddress?.toLowerCase()
  const userAddr = userStore.walletAddress?.toLowerCase()
  return userStore.isMerchant && merchantAddr && userAddr && merchantAddr === userAddr
})

const canBuy = computed(() => {
  // 如果是商家且商品属于自己，禁止购买
  if (isSelfPurchase.value) return false

  // 无规格或全部选择完成
  if (!product.value || !product.value.specs) return true
  return product.value.specs.every(spec => selectedSpec.value[spec.name])
})

// 辅助函数：解析 JSON 字符串
function parseJSONString(str) {
  if (!str || typeof str !== 'string') return null
  
  const trimmed = str.trim()
  if (!trimmed) return null
  
  try {
    return JSON.parse(trimmed)
  } catch (e) {
    // 如果是类似 ["url"] 但没有正确转义的格式
    if (trimmed.startsWith('["') && trimmed.endsWith('"]')) {
      const content = trimmed.slice(2, -2)
      return content ? [content] : []
    }
    // 如果是类似 [url] 的格式
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      const content = trimmed.slice(1, -1)
      return content ? [content] : []
    }
    throw e
  }
}

// 获取评价图片数组
function getReviewImages(review) {
  if (!review.images) return []
  
  // 如果已经处理过，直接返回
  if (review._processedImages && Array.isArray(review._processedImages)) {
    return review._processedImages
  }
  
  let images = review.images
  
  // 如果是字符串，尝试解析
  if (typeof images === 'string') {
    try {
      const parsed = parseJSONString(images)
      if (Array.isArray(parsed)) {
        review._processedImages = parsed.filter(img => img && typeof img === 'string')
      } else if (typeof parsed === 'string') {
        review._processedImages = parsed ? [parsed] : []
      } else {
        review._processedImages = []
      }
    } catch (e) {
      console.warn('评价图片解析失败:', e, images)
      review._processedImages = images.trim() ? [images.trim()] : []
    }
    return review._processedImages || []
  }
  
  // 如果是数组，直接返回
  if (Array.isArray(images)) {
    review._processedImages = images.filter(img => img && typeof img === 'string')
    return review._processedImages
  }
  
  return []
}

// 加载商品详情
async function loadProductDetail() {
  loading.value = true
  
  try {
    const data = await getProductDetail(route.params.id)
    
    // 统一处理商品数据
    if (typeof data.specs === 'string') {
      try {
        data.specs = parseJSONString(data.specs)
      } catch (e) {
        console.warn('规格数据解析失败:', e)
        data.specs = []
      }
    }
    
    // 确保 specs 是数组
    if (!Array.isArray(data.specs)) {
      data.specs = []
    }
    
    product.value = data
    productStore.setCurrentProduct(data)
    
    // 初始化规格选择
    if (data.specs && data.specs.length > 0) {
      data.specs.forEach(spec => {
        if (spec.options && spec.options.length > 0) {
          selectedSpec.value[spec.name] = spec.options[0]
        }
      })
    }
    
    // 加载评价
    await loadReviews()
  } catch (error) {
    console.error('加载商品详情失败:', error)
    if (!error.request) {
      ElMessage.error('加载商品详情失败')
    }
  } finally {
    loading.value = false
  }
}

// 加载评价列表
async function loadReviews() {
  try {
    const data = await getProductReviews(route.params.id, { limit: 10 })
    const reviewsData = data.list || []
    
    // 预处理评价数据
    reviewsData.forEach(review => {
      // 确保图片数据被正确解析
      if (review.images) {
        // 调用 getReviewImages 会设置 _processedImages 缓存
        getReviewImages(review)
      }
      
      // 处理评分
      if (typeof review.rating === 'string') {
        review.rating = parseFloat(review.rating) || 0
      }
    })
    
    reviews.value = reviewsData
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

// 购买
function handleBuy() {
  if (!userStore.isConnected) {
    ElMessage.warning('请先连接钱包')
    router.push('/login')
    return
  }

  // 商家不可购买自家商品
  if (isSelfPurchase.value) {
    ElMessage.warning('商家不能购买自家商品')
    return
  }
  
  // 构建规格信息
  const specInfo = Object.keys(selectedSpec.value).length > 0 
    ? JSON.stringify(selectedSpec.value) 
    : ''
  
  router.push({
    path: '/checkout',
    query: {
      productId: product.value.id,
      spec: specInfo
    }
  })
}

// 返回上一级（有历史则后退，否则回首页）
function goBack() {
  try {
    if (window.history && window.history.length > 1) {
      router.back()
    } else {
      router.push('/')
    }
  } catch (e) {
    router.push('/')
  }
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  
  try {
    const date = new Date(time)
    if (isNaN(date.getTime())) {
      // 如果日期无效，尝试解析为时间戳
      const timestamp = parseInt(time)
      if (!isNaN(timestamp)) {
        return new Date(timestamp).toLocaleString('zh-CN')
      }
      return time
    }
    return date.toLocaleString('zh-CN')
  } catch (e) {
    console.warn('时间格式化失败:', e, time)
    return time
  }
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.loading-container {
  padding: 40px 0;
}

.detail-content {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.product-images {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.product-images :deep(.el-carousel__indicators) {
  margin-top: 20px;
}

.product-info {
  padding: 0 20px;
}

.product-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 20px 0;
  color: #303133;
  line-height: 1.3;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.sales {
  color: #909399;
  font-size: 15px;
  font-weight: 500;
}

.product-price {
  margin-bottom: 24px;
}

.price {
  font-size: 42px;
  font-weight: bold;
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.product-spec {
  margin-bottom: 30px;
}

.spec-item {
  margin-bottom: 20px;
}

.spec-label {
  font-size: 15px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}

.product-actions {
  margin-bottom: 24px;
}

.product-actions .el-button {
  width: 100%;
  height: 56px;
  font-size: 18px;
  border-radius: 12px;
  font-weight: 600;
}

.product-actions .el-button:hover {
  background: #409eff;
}

.merchant-info h3 {
  font-size: 18px;
  margin: 0 0 20px 0;
  color: #303133;
  font-weight: 600;
}

.description-card,
.reviews-card {
  margin-bottom: 30px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.description-card :deep(.el-card__header),
.reviews-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.description-content {
  line-height: 2;
  color: #606266;
  padding: 20px;
}

.review-item {
  padding: 24px;
  border-bottom: 1px solid #ebeef5;
  transition: background 0.3s;
}

.review-item:hover {
  background: #fafafa;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-user :deep(.el-avatar) {
  border: 2px solid #ebeef5;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-address {
  font-weight: 600;
  color: #303133;
  font-family: monospace;
}

.review-time {
  font-size: 13px;
  color: #909399;
}

.review-content {
  margin-bottom: 16px;
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

.review-images {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  transition: transform 0.3s;
}

.review-image:hover {
  transform: scale(1.05);
}

.review-nft {
  margin-top: 12px;
}
</style>