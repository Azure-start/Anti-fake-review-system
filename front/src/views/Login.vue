<template>
  <div class="login-page">
    <div class="login-container">
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <el-icon :size="40"><Shop /></el-icon>
            <h2>区块链电商防刷评系统</h2>
          </div>
        </template>

        <div class="login-content">
          <el-result
            icon="info"
            title="请连接钱包"
            sub-title="使用MetaMask钱包登录系统"
          >
            <template #extra>
              <div class="wallet-tips">
                <el-alert
                  title="温馨提示"
                  type="info"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <ul class="tip-list">
                      <li>请确保已安装MetaMask浏览器插件</li>
                      <li>登录前请切换到Sepolia测试网络</li>
                      <li>首次连接需要签名验证身份</li>
                    </ul>
                  </template>
                </el-alert>
              </div>

              <div class="wallet-check">
                <WalletConnectBtn />
              </div>

              <div class="install-link">
                <el-link
                  href="https://metamask.io/"
                  target="_blank"
                  type="primary"
                  :icon="Download"
                >
                  下载MetaMask
                </el-link>
              </div>
            </template>
          </el-result>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import WalletConnectBtn from '@/components/WalletConnectBtn.vue'

const router = useRouter()
const userStore = useUserStore()

// 监听登录状态
watch(() => userStore.isConnected, (isConnected) => {
  if (isConnected) {
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.push(redirect)
  }
})

onMounted(() => {
  // 如果已登录，直接跳转
  if (userStore.isConnected) {
    router.push('/')
  }
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}


.login-container {
  width: 100%;
  max-width: 600px;
  position: relative;
  z-index: 1;
}

.login-card {
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border-radius: 24px;
  overflow: hidden;
  border: none;
}

.login-card :deep(.el-card__header) {
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  padding: 30px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-direction: column;
}

.card-header :deep(.el-icon) {
  color: #606266;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.login-content {
  padding: 30px;
  background: #fff;
}

.wallet-tips {
  margin-bottom: 24px;
}

.tip-list {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.tip-list li {
  margin-bottom: 8px;
  line-height: 1.8;
  color: #606266;
}

.wallet-check {
  display: flex;
  justify-content: center;
  margin: 30px 0;
}

.install-link {
  text-align: center;
  margin-top: 24px;
}
</style>

