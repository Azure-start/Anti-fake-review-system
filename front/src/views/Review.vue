<template>
  <div class="review">
    <div class="container">
      <el-card header="发布评价" class="review-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else class="review-form">
          <!-- 商品信息 -->
          <div class="product-preview" v-if="product">
            <el-image :src="product.image || '/placeholder.png'" class="product-img" />
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <div class="product-price">¥{{ product.price }}</div>
            </div>
          </div>

          <!-- 评价表单 -->
          <el-form :model="reviewForm" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="评分" prop="rating">
              <el-rate v-model="reviewForm.rating" :colors="colors" />
            </el-form-item>

            <el-form-item label="评价内容" prop="content">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="6"
                placeholder="请至少输入10个字，分享您的购物体验..."
                show-word-limit
                maxlength="500"
              />
            </el-form-item>

            <el-form-item label="上传图片">
              <!-- 修改后的上传组件 -->
              <el-upload
                v-model:file-list="fileList"
                action="#"
                :auto-upload="false"
                :limit="3"
                :on-change="handleImageChange"
                :on-preview="handlePreview"
                :on-remove="handleRemove"
                list-type="picture-card"
                multiple
                accept=".jpg,.jpeg,.png,.gif,.webp"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="upload-tip">最多上传3张图片，支持JPG、PNG、GIF、WEBP格式，每张不超过5MB</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-alert
              v-if="!canReview"
              title="提示"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 20px;"
            >
              {{ warningText }}
            </el-alert>

            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="!canReview || uploadingImages">
                提交评价
              </el-button>
              <el-button @click="handleCancel">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <!-- NFT提示弹窗 -->
    <NftTip
      v-model="showNftTip"
      :nft-id="nftId"
      :tx-hash="txHash"
    />

    <!-- 图片预览 -->
    <el-dialog v-model="previewVisible" title="预览图片">
      <el-image :src="previewImage" fit="contain" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { getProductDetail, submitReview } from '@/api/productApi'
import { getUserTransactions } from '@/api/transactionApi'
import NftTip from '@/components/NftTip.vue'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const uploadingImages = ref(false)
const product = ref(null)
const hasConfirmedTransaction = ref(false)
const fileList = ref([])
const imageUrls = ref([]) // 存储已上传的图片URL
const previewVisible = ref(false)
const previewImage = ref('')
const showNftTip = ref(false)
const nftId = ref('')
const txHash = ref('')
const currentOrderId = ref('')

const formRef = ref(null)

const reviewForm = ref({
  rating: 5,
  content: ''
})

const rules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, message: '评价内容至少需要10个字', trigger: 'blur' }
  ]
}

const colors = ['#99A9BF', '#F7BA2A', '#FF9900']

const canReview = computed(() => {
  return hasConfirmedTransaction.value
})

const warningText = computed(() => {
  return '请先完成购买并确认收货后才能评价'
})

// 验证文件（复用开店申请页面的逻辑）
function validateFile(file) {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式的图片')
    return false
  }
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function uploadFileToServer(file) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // request 拦截器会返回解包后的数据
    return res
  } catch (e) {
    console.error('上传图片失败', e)
    ElMessage.error('图片上传失败')
    throw e
  }
}

// 处理图片上传（修复参数问题）
async function handleImageChange(uploadFile, uploadFiles) {
  console.log('[review] handleImageChange called', uploadFile, uploadFiles)
  const realFile = uploadFile && uploadFile.raw ? uploadFile.raw : uploadFile
  if (!realFile) return

  if (!validateFile(realFile)) {
    // 从fileList中移除无效文件
    const index = fileList.value.findIndex(f => f.uid === uploadFile.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
    return false
  }

  try {
    uploadingImages.value = true
    
    // 生成本地预览URL
    const localUrl = URL.createObjectURL(realFile)
    
    // 更新当前文件的预览URL
    const fileIndex = fileList.value.findIndex(f => f.uid === uploadFile.uid)
    if (fileIndex > -1) {
      fileList.value[fileIndex].url = localUrl
    }
    
    // 上传到服务器
    const serverUrl = await uploadFileToServer(realFile)
    console.log('[review] uploadFileToServer returned:', serverUrl)
    
    // 释放本地URL
    URL.revokeObjectURL(localUrl)
    
    // 更新为服务器URL
    if (fileIndex > -1) {
      fileList.value[fileIndex].url = serverUrl
    }
    
    // 保存到imageUrls数组
    if (!imageUrls.value.includes(serverUrl)) {
      imageUrls.value.push(serverUrl)
    }
    
    ElMessage.success('图片上传成功')
  } catch (err) {
    console.error('图片上传错误:', err)
    ElMessage.error('图片上传失败')
    
    // 上传失败时从fileList中移除
    const index = fileList.value.findIndex(f => f.uid === uploadFile.uid)
    if (index > -1) {
      fileList.value.splice(index, 1)
    }
  } finally {
    uploadingImages.value = false
  }
}

// 处理图片删除
function handleRemove(file, fileList) {
  const urlToRemove = file.url
  const index = imageUrls.value.findIndex(url => url === urlToRemove)
  if (index > -1) {
    imageUrls.value.splice(index, 1)
  }
}

// 检查是否已确认收货
async function checkTransactionStatus() {
  try {
    console.log('🔍 开始检查交易状态...')
    console.log('📍 用户钱包地址:', userStore.walletAddress)
    console.log('📦 商品ID:', route.params.productId)
    
    if (!userStore.walletAddress) {
      console.warn('⚠️ 用户钱包地址为空，请先连接钱包')
      hasConfirmedTransaction.value = false
      return
    }
    
    const data = await getUserTransactions({ userAddress: userStore.walletAddress })
    console.log('📋 获取到的交易数据:', data)
    
    const transactions = data.list || []
    const productId = Number(route.params.productId)
    
    console.log(`📊 共找到 ${transactions.length} 条交易记录`)
    console.log('🔎 搜索条件: productId =', productId, ', status = completed, receiveStatus = confirmed')
    
    // 打印所有交易记录供调试
    transactions.forEach((t, index) => {
      console.log(`  交易[${index}]:`, {
        productId: t.productId,
        status: t.status,
        receiveStatus: t.receiveStatus,
        orderId: t.orderId
      })
    })
    
    // 查找该商品是否有已确认收货的交易
    const confirmedTransaction = transactions.find(
      t => t.productId === productId && 
           t.status === 'completed' && 
           t.receiveStatus === 'confirmed'
    )
    
    console.log('✅ 找到符合条件的交易:', !!confirmedTransaction)
    if (confirmedTransaction) {
      console.log('📄 匹配的交易详情:', confirmedTransaction)
      // 保存订单ID，用于提交评价时更新订单状态
      currentOrderId.value = confirmedTransaction.orderId
    }
    
    hasConfirmedTransaction.value = !!confirmedTransaction
    console.log('🎯 最终评价权限状态:', hasConfirmedTransaction.value)
    
  } catch (error) {
    console.error('❌ 检查交易状态失败:', error)
    hasConfirmedTransaction.value = false
  }
}

// 加载商品信息
async function loadProduct() {
  loading.value = true
  
  try {
    const data = await getProductDetail(route.params.productId)
    product.value = data
    
    // 检查交易状态
    await checkTransactionStatus()
  } catch (error) {
    console.error('加载商品失败:', error)
    if (!error.request) {
      ElMessage.error('加载商品信息失败')
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}

// 提交评价
async function handleSubmit() {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 检查用户是否已连接钱包
  if (!userStore.isConnected || !userStore.walletAddress) {
    ElMessage.error('请先连接钱包')
    return
  }

  // 等待所有图片上传完成
  if (uploadingImages.value) {
    ElMessage.warning('请等待图片上传完成')
    return
  }

  submitting.value = true
  
  try {
    // 使用从fileList中获取的已上传图片URL
    const uploadedImages = fileList.value
      .map(f => f.url)
      .filter(url => url && url.startsWith('http')) // 确保是服务器URL
    
    const payload = {
      productId: Number(route.params.productId),
      rating: reviewForm.value.rating,
      content: reviewForm.value.content.trim(),
      images: JSON.stringify(uploadedImages),
      userAddress: userStore.walletAddress,
      orderId: currentOrderId.value
    }

    console.log('提交评价的数据:', payload)
    
    const result = await submitReview(payload)
    
    ElMessage.success('评价提交成功')
    
    // 显示NFT提示
    if (result.nftId && result.txHash) {
      nftId.value = result.nftId
      txHash.value = result.txHash
      showNftTip.value = true
    }
    
    // 评价成功后返回到交易记录页面
    setTimeout(() => {
      if (route.query.from === 'transactions') {
        router.push('/transactions')
      } else {
        router.push(`/product/${route.params.productId}`)
      }
    }, 2000)
  } catch (error) {
    console.error('提交评价失败:', error)
    if (!error.request) {
      ElMessage.error('提交评价失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消
function handleCancel() {
  router.back()
}

// 预览图片
function handlePreview(file) {
  previewImage.value = file.url
  previewVisible.value = true
}

onMounted(() => {
  // 从URL参数获取订单ID
  if (route.query.orderId) {
    currentOrderId.value = route.query.orderId
  }
  loadProduct()
})
</script>

<style scoped>
.review {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.review-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.review-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.loading-container {
  padding: 40px 0;
}

.product-preview {
  display: flex;
  gap: 20px;
  padding: 30px;
  background: #f5f7fa;
  border-radius: 16px;
  margin-bottom: 40px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.product-img {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

.product-info h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.product-price {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.review-form {
  padding: 30px;
}

.review-form :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
}

/* 新增上传提示样式 */
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>