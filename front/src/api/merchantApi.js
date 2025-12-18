import request from './request'

const USE_MOCK = import.meta.env.MODE === 'development'

// 申请开店
export function applyShop(shopData) {
  
  return request.post('/merchant/shop/apply', shopData)
}

// 获取店铺信息
export function getShopInfo(address) {
  return request.get('/merchant/shop/info', { params: { address } })
}

// 更新店铺信息
export function updateShopInfo(shopData) {
  
  return request.put('/merchant/shop/updateInfo', shopData)
}

// 获取我的商品列表
export function getMyProducts(params = {}) {
  
  return request.get('/merchant/products', { params })
}

// 新增商品
export function createProduct(productData) {
  
  return request.post('/merchant/products', productData)
}

// 更新商品
export function updateProduct(productId, productData) {
  
  return request.put(`/merchant/products/${productId}`, productData)
}

// 下架商品
export function offShelfProduct(productId) {
  
  return request.put(`/merchant/products/off/${productId}`)
}

// 上架商品
export function onSaleProduct(productId) {
  
  return request.put(`/merchant/products/on/${productId}`)
}

// 删除商品
export function deleteProduct(productId) {
  
  return request.delete(`/merchant/products/${productId}`)
}

// 获取店铺订单
export function getShopOrders(params = {}) {
  
  return request.get('/merchant/orders', { params })
}

