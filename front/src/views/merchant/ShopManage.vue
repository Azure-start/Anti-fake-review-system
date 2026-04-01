<template>
  <div class="shop-manage">
    <div class="container">
      <el-card header="店铺管理" class="shop-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="shopInfo">
          <el-alert
            v-if="shopInfo.status === 'pending'"
            title="审核中"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          >
            您的店铺申请正在审核中，请耐心等待
          </el-alert>

          <el-alert
            v-if="shopInfo.status === 'rejected'"
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
              <el-input v-model="shopInfo.name" :disabled="shopInfo.status !== 'approved'" />
            </el-form-item>

            <el-form-item label="店铺描述">
              <el-input
                v-model="shopInfo.description"
                type="textarea"
                :rows="4"
                :disabled="shopInfo.status !== 'approved'"
              />
            </el-form-item>

            <el-form-item label="店铺Logo">
              <el-image
                v-if="shopInfo.logo"
                :src="shopInfo.logo"
                style="width: 120px; height: 120px; border-radius: 8px;"
              />
            </el-form-item>

            <el-form-item label="店铺状态">
              <el-tag :type="getStatusType(shopInfo.status)">
                {{ getStatusText(shopInfo.status) }}
              </el-tag>
            </el-form-item>

            <el-form-item v-if="shopInfo.status === 'approved'">
              <el-button type="primary" @click="handleUpdate" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>

            <el-form-item v-if="shopInfo.status === 'rejected' || !shopInfo">
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
import { getShopInfo, updateShopInfo } from '@/api/merchantApi'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const shopInfo = ref(null)

// 获取店铺信息
async function loadShopInfo() {
  loading.value = true
  
  try {
    const result = await getShopInfo()
    if (result.code === 0) {
      shopInfo.value = result.data
      if (result.data) {
        userStore.setShopInfo(result.data)
      }
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
    const result = await updateShopInfo(shopInfo.value)
    if (result.code === 0) {
      ElMessage.success('更新成功')
      await loadShopInfo()
    } else {
      ElMessage.error(result.message || '更新失败')
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
    pending: '审核中',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return statusMap[status] || status
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
</style>

