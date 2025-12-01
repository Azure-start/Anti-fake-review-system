import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/Checkout.vue'),
    meta: { title: '确认订单', requiresAuth: true }
  },
  {
    path: '/reviews',
    name: 'UserReviews',
    component: () => import('@/views/UserReviews.vue')
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component: () => import('@/views/Transactions.vue'),
    meta: { title: '交易记录', requiresAuth: true }
  },
  {
    path: '/review/:productId',
    name: 'Review',
    component: () => import('@/views/Review.vue'),
    meta: { title: '发布评价', requiresAuth: true }
  },
  {
    path: '/rewards',
    name: 'Rewards',
    component: () => import('@/views/Rewards.vue'),
    meta: { title: '我的奖励', requiresAuth: true }
  },
  {
    path: '/account',
    name: 'Account',
    component: () => import('@/views/Account.vue'),
    meta: { title: '我的账户', requiresAuth: true }
  },
  // 商家端路由
  {
    path: '/merchant',
    name: 'Merchant',
    redirect: '/merchant/shop',
    meta: { title: '商家中心', requiresAuth: true, requiresRole: 'merchant' }
  },
  {
    path: '/merchant/shop',
    name: 'ShopManage',
    component: () => import('@/views/merchant/ShopManage.vue'),
    meta: { title: '店铺管理', requiresAuth: true, requiresRole: 'merchant' }
  },
  {
    path: '/merchant/shop/apply',
    name: 'ShopApply',
    component: () => import('@/views/merchant/ShopApply.vue'),
    meta: { title: '申请开店', requiresAuth: true, requiresRole: 'merchant' }
  },
  {
    path: '/merchant/products',
    name: 'ProductManage',
    component: () => import('@/views/merchant/ProductManage.vue'),
    meta: { title: '商品管理', requiresAuth: true, requiresRole: 'merchant' }
  },
  {
    path: '/merchant/orders',
    name: 'MerchantOrders',
    component: () => import('@/views/merchant/OrderManage.vue'),
    meta: { title: '订单管理', requiresAuth: true, requiresRole: 'merchant' }
  },
  // 管理员端路由
  {
    path: '/admin',
    name: 'Admin',
    redirect: '/admin/dashboard',
    meta: { title: '管理员', requiresAuth: true, requiresRole: 'admin' }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '系统概览', requiresAuth: true, requiresRole: 'admin' }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresRole: 'admin' }
  },
  {
    path: '/admin/shop-audit',
    name: 'ShopAudit',
    component: () => import('@/views/admin/ShopAudit.vue'),
    meta: { title: '商家审核', requiresAuth: true, requiresRole: 'admin' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果路由需要认证且用户未登录，跳转到登录页
  if (to.meta.requiresAuth && !userStore.isConnected) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 检查角色权限
  if (to.meta.requiresRole) {
    const requiredRole = to.meta.requiresRole
    const userRole = userStore.userRole
    
    if (userRole !== requiredRole) {
      ElMessage.warning('您没有权限访问该页面')
      // 根据用户角色跳转到对应的首页
      if (userRole === 'merchant') {
        next('/merchant')
      } else if (userRole === 'admin') {
        next('/admin')
      } else {
        next('/')
      }
      return
    }
  }
  
  next()
})

export default router

