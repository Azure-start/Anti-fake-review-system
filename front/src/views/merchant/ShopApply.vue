<template>
  <div class="shop-apply">
    <div class="container">
      <el-card header="申请开店" class="apply-card">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
          <el-form-item label="店铺名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入店铺名称" maxlength="50" show-word-limit />
          </el-form-item>

          <el-form-item label="店铺描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请描述您的店铺特色、主营商品等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="店铺Logo" prop="logo">
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

const form = ref({
  name: '',
  description: ''
})

const rules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { min: 10, max: 200, message: '店铺描述长度为10-200个字符', trigger: 'blur' }
  ]
}

function handleLogoChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    logoUrl.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  
  try {
    const shopData = {
      name: form.value.name,
      description: form.value.description,
      logo: logoUrl.value
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

