<template>
  <div class="shop-manage">
    <div class="container">
      <el-card header="店铺管理" class="shop-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="shopInfo">
          <el-alert
            v-if="shopInfo.shopStatus === 'pending'"
            title="审核中"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          >
            您的店铺申请正在审核中，请耐心等待
          </el-alert>

          <el-alert
            v-if="shopInfo.shopStatus === 'rejected'"
            title="审核未通过"
            type="error"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          >
            您的店铺申请未通过审核，请重新申请
          </el-alert>

          <el-form :model="shopInfo" label-width="120px">
            <el-form-item label="店铺名称">
              <el-input v-model="shopInfo.shopName" :disabled="shopInfo.shopStatus !== 'approved'" />
            </el-form-item>

            <el-form-item label="店铺描述">
              <el-input
                v-model="shopInfo.shopDescription"
                type="textarea"
                :rows="4"
                :disabled="shopInfo.shopStatus !== 'approved'"
              />
            </el-form-item>

            <el-form-item label="店铺Logo">
              <el-upload
                v-if="shopInfo.shopStatus === 'approved'"
                class="logo-uploader"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleLogoChange"
              >
                <img v-if="logoUrl || shopInfo.shopLogo" :src="logoUrl || shopInfo.shopLogo" class="logo-preview" />
                <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <el-image
                v-else-if="shopInfo.shopLogo"
                :src="shopInfo.shopLogo"
                style="width: 120px; height: 120px; border-radius: 8px;"
              />
              <div v-if="shopInfo.shopStatus === 'approved'" class="upload-tip">建议尺寸：200x200px，支持JPG、PNG格式</div>
            </el-form-item>

            <el-form-item label="店铺状态">
              <el-tag :type="getStatusType(shopInfo.shopStatus)">
                {{ getStatusText(shopInfo.shopStatus) }}
              </el-tag>
            </el-form-item>

            <el-form-item v-if="shopInfo.shopStatus === 'approved'">
              <el-button type="primary" @click="handleUpdate" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>

            <el-form-item v-if="shopInfo.shopStatus === 'rejected' || !shopInfo">
              <el-button type="primary" @click="handleApply">
                重新申请
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-else class="empty-state">
          <el-empty description="您还没有店铺，请先申请开店">
            <el-button type="primary" @click="handleApply">立即申请</el-button>
          </el-empty>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getShopInfo, updateShopInfo } from '@/api/merchantApi'
import { useUserStore } from '@/stores/userStore'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const shopInfo = ref(null)
const logoUrl = ref('')
const uploadingLogo = ref(false)

// 验证文件
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

// 上传文件到服务器
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
    console.error('上传 Logo 失败', e)
    ElMessage.error('Logo 上传失败')
    throw e
  }
}

// 处理Logo上传
async function handleLogoChange(file) {
  const realFile = file && file.raw ? file.raw : file
  if (!realFile) return

  if (!validateFile(realFile)) return

  uploadingLogo.value = true
  try {
    const localUrl = URL.createObjectURL(realFile)
    logoUrl.value = localUrl

    const serverUrl = await uploadFileToServer(realFile)
    console.log('Logo 上传成功，服务器URL:', serverUrl)

    URL.revokeObjectURL(localUrl)
    logoUrl.value = serverUrl
    // 更新 shopInfo 中的 shopLogo，以便保存时使用
    shopInfo.value.shopLogo = serverUrl
    ElMessage.success('Logo 上传成功')
  } catch (err) {
    console.error('Logo 上传错误:', err)
    ElMessage.error('Logo 上传失败')
    logoUrl.value = ''
  } finally {
    uploadingLogo.value = false
  }
}

// 获取店铺信息
async function loadShopInfo() {
  loading.value = true
  
  try {
    const address = userStore.walletAddress
    if (!address) {
      console.warn('未获取到钱包地址，无法拉取店铺信息')
      loading.value = false
      return
    }

    const result = await getShopInfo(address)
    console.log('后端返回数据:', result)
    // request 拦截器已经解包了数据，result 直接就是店铺数据对象
    if (result) {
      shopInfo.value = result
      // 重置logoUrl，使用服务器返回的logo
      logoUrl.value = ''
      console.log('处理后的店铺信息:', shopInfo.value)
      userStore.setShopInfo(result)
    }
  } catch (error) {
    console.error('获取店铺信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新店铺信息
async function handleUpdate() {
  updating.value = true
  
  try {
    // 使用新上传的logo（如果有），否则使用原有的logo
    const finalLogo = logoUrl.value || shopInfo.value.shopLogo
    
    // 只向后端提交当前使用的字段
    const payload = {
      address: userStore.walletAddress,
      shopName: shopInfo.value.shopName,
      shopDescription: shopInfo.value.shopDescription,
      shopLogo: finalLogo
    }
    const result = await updateShopInfo(payload)
    // request 拦截器已经解包了数据
    if (result) {
      ElMessage.success('更新成功')
      // 清空logoUrl，重新加载店铺信息
      logoUrl.value = ''
      await loadShopInfo()
    } else {
      ElMessage.error('更新失败')
    }
  } catch (error) {
    console.error('更新店铺信息失败:', error)
    if (!error.request) {
      ElMessage.error('更新失败')
    }
  } finally {
    updating.value = false
  }
}

// 去申请
function handleApply() {
  router.push('/merchant/shop/apply')
}

function getStatusType(shopStatus) {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[shopStatus] || 'info'
}

function getStatusText(shopStatus) {
  const statusMap = {
    pending: '审核中',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return statusMap[shopStatus] || shopStatus
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
.shop-manage {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.shop-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.shop-card :deep(.el-card__header) {
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

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.logo-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.logo-uploader:hover {
  border-color: #409eff;
}

.logo-uploader-icon {
  font-size: 40px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.logo-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
</style>

