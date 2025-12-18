import request from './request'

const USE_MOCK = import.meta.env.MODE === 'development'

// 获取用户列表
export function getUserList(params = {}) {
  return request.get('/admin/users', { params })
}

// 获取商家申请列表
export function getShopApplications(params = {}) {
  return request.get('/admin/shop-applications', { params })
}

// 审核商家申请
export function auditShopApplication(applicationId, action) {
  return request.put(`/admin/shop-applications/${applicationId}/audit`, { action })
}

// 获取待审核商品列表
export function getPendingProducts(params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const { page = 1, pageSize = 10 } = params
        const products = [
          {
            id: 100,
            name: '待审核商品1',
            price: 99,
            image: 'https://via.placeholder.com/300x300?text=Pending+1',
            merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb',
            status: 'pending',
            createdAt: '2024-01-17T00:00:00Z'
          }
        ]
        resolve({
          code: 0,
          data: {
            list: products,
            total: products.length,
            page,
            pageSize
          }
        })
      }, 500)
    })
  }
  return request.get('/admin/products/pending', { params })
}

// 审核商品
export function auditProduct(productId, action) {
  // action: 'approve' | 'reject'
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            id: productId,
            status: action === 'approve' ? 'approved' : 'rejected',
            message: action === 'approve' ? '商品已上架' : '商品已拒绝'
          }
        })
      }, 500)
    })
  }
  return request.put(`/admin/products/${productId}/audit`, { action })
}

// 获取系统统计
export function getSystemStats() {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            totalUsers: 1250,
            totalMerchants: 85,
            totalProducts: 3200,
            totalOrders: 15600,
            totalTransactions: 45.5 // ETH
          }
        })
      }, 500)
    })
  }
  return request.get('/admin/stats')
}

