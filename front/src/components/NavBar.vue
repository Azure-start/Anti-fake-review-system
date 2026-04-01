<template>
  <div class="navbar">
    <div class="container">
      <div class="navbar-left">
        <router-link :to="logoLink" class="logo">
          <el-icon :size="24"><Shop /></el-icon>
          <span>{{ logoText }}</span>
        </router-link>
      </div>
      
      <div class="navbar-center">
        <el-menu
          mode="horizontal"
          :default-active="activeMenu"
          router
          class="navbar-menu"
        >
          <el-menu-item
            v-for="menu in menus"
            :key="menu.index"
            :index="menu.index"
          >
            {{ menu.label }}
          </el-menu-item>
        </el-menu>
      </div>
      
      <div class="navbar-right">
        <WalletConnectBtn />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Shop } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import WalletConnectBtn from './WalletConnectBtn.vue'

const route = useRoute()
const userStore = useUserStore()

// 用户端菜单
const userMenus = [
  { index: '/', label: '首页' },
  { index: '/transactions', label: '交易记录' },
  // { index: '/rewards', label: '我的奖励' },
  { index: '/reviews',label: '我的评论' },
  // { index: '/account', label: '我的账户' }

]

// 商家端菜单
const merchantMenus = [
  { index: '/merchant/shop', label: '店铺管理' },
  { index: '/merchant/products', label: '商品管理' },
  { index: '/merchant/orders', label: '订单管理' }
]

// 管理员端菜单
const adminMenus = [
  { index: '/admin/dashboard', label: '系统概览' },
  { index: '/admin/users', label: '用户管理' },
  { index: '/admin/shop-audit', label: '商家审核' }
]

const menus = computed(() => {
  if (userStore.isAdmin) return adminMenus
  if (userStore.isMerchant) return merchantMenus
  return userMenus
})

const logoLink = computed(() => {
  if (userStore.isAdmin) return '/admin'
  if (userStore.isMerchant) return '/merchant'
  return '/'
})

const logoText = computed(() => {
  if (userStore.isAdmin) return '管理员中心'
  if (userStore.isMerchant) return '商家中心'
  return '区块链电商'
})

const activeMenu = computed(() => {
  const path = route.path
  if (userStore.isAdmin) {
    if (path.startsWith('/admin')) return path
    return '/admin/dashboard'
  }
  if (userStore.isMerchant) {
    if (path.startsWith('/merchant')) return path
    return '/merchant/shop'
  }
  if (path.startsWith('/product')) return '/'
  if (path.startsWith('/checkout')) return '/'
  if (path.startsWith('/review')) return '/'
  if (path.startsWith('/transactions')) return '/transactions'
  if (path.startsWith('/rewards')) return '/rewards'
  if (path.startsWith('/account')) return '/account'
  return path
})
</script>

<style scoped>
.navbar {
  background: #ffffff;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.navbar-left .logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  text-decoration: none;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 8px;
}

.navbar-left .logo:hover {
  background: #f5f7fa;
  transform: translateY(-2px);
}

.navbar-left .logo :deep(.el-icon) {
  color: #606266;
}

.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.navbar-menu {
  border-bottom: none;
  background: transparent;
}

.navbar-menu :deep(.el-menu-item) {
  color: #606266;
  border-bottom: none;
  transition: all 0.3s;
}

.navbar-menu :deep(.el-menu-item:hover) {
  background: #f5f7fa;
  color: #303133;
  border-radius: 8px;
}

.navbar-menu :deep(.el-menu-item.is-active) {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  background: transparent;
  border-radius: 8px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>

