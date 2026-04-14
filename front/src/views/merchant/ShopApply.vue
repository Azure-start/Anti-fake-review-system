<template>
  <div class="shop-apply">
    <div class="container">
      <el-card header="申请开店" class="apply-card">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="店铺名称" prop="shopName">
            <el-input v-model="form.shopName" placeholder="请输入店铺名称" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item label="店铺描述" prop="shopDescription">
            <el-input
              v-model="form.shopDescription"
              type="textarea"
              :rows="4"
              placeholder="请描述您的店铺特色、主营商品等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="店铺Logo" prop="shopLogo">
            <el-upload
              class="logo-uploader"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleLogoChange"
            >
              <img v-if="logoUrl" :src="logoUrl" class="logo-preview" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸：200x200px，支持JPG、PNG格式</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              提交申请
            </el-button>
            <el-button @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { applyShop } from '@/api/merchantApi'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const logoUrl = ref('')
const uploadingLogo = ref(false)

const form = ref({
  shopName: '',
  shopDescription: ''
})

const rules = {
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度为2-50个字符', trigger: 'blur' }
  ],
  shopDescription: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { min: 10, max: 200, message: '店铺描述长度为10-200个字符', trigger: 'blur' }
  ]
}

// 验证文件（复用于商品上传）
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
    console.error('上传 Logo 失败', e)
    ElMessage.error('Logo 上传失败')
    throw e
  }
}

// el-upload 的 on-change 回调（file 为 el-upload 传入的文件对象，真实文件在 file.raw）
async function handleLogoChange(file) {
  console.log('[shopLogo] handleLogoChange called', file)
  const realFile = file && file.raw ? file.raw : file
  if (!realFile) return

  if (!validateFile(realFile)) return

  uploadingLogo.value = true
  try {
    const localUrl = URL.createObjectURL(realFile)
    logoUrl.value = localUrl

    const serverUrl = await uploadFileToServer(realFile)
    console.log('[shopLogo] uploadFileToServer returned:', serverUrl)

    URL.revokeObjectURL(localUrl)
    logoUrl.value = serverUrl
    ElMessage.success('Logo 上传成功')
  } catch (err) {
    console.error('Logo 上传错误:', err)
    ElMessage.error('Logo 上传失败')
    logoUrl.value = ''
  } finally {
    uploadingLogo.value = false
    // 如果是 el-upload，会自动管理 fileList；不需要手动清空input
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  
  try {
    const shopData = {
      address: userStore.walletAddress,
      shopName: form.value.shopName,
      shopDescription: form.value.shopDescription,
      shopLogo: logoUrl.value
    }
    
    const result = await applyShop(shopData)
    
    if (result.code === 0) {
      ElMessage.success('申请已提交，等待管理员审核')
      router.push('/merchant/shop')
    } else {
      ElMessage.error(result.message || '申请失败')
    }
  } catch (error) {
    console.error('申请开店失败:', error)
    if (!error.request) {
      ElMessage.error('申请开店失败')
    }
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.back()
}
</script>

<style scoped>
.shop-apply {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.apply-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.apply-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
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

