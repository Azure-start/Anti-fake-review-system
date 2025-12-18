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
  
  return request.get('/admin/products/pending', { params })
}

// 审核商品
export function auditProduct(productId, action) {
  
  return request.put(`/admin/products/${productId}/audit`, { action })
}

// 获取系统统计
export function getSystemStats() {
  
  return request.get('/admin/stats')
}

