import request from './request'

const USE_MOCK = import.meta.env.MODE === 'development'

// 申请开店
export function applyShop(shopData) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            applicationId: 1,
            status: 'pending',
            message: '申请已提交，等待管理员审核'
          }
        })
      }, 500)
    })
  }
  return request.post('/api/merchant/shop/apply', shopData)
}

// 获取店铺信息
export function getShopInfo() {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            id: 1,
            name: '我的店铺',
            description: '专注于高品质商品的电商店铺',
            logo: 'https://via.placeholder.com/100x100?text=Shop',
            address: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb',
            status: 'approved',
            createdAt: '2024-01-01T00:00:00Z'
          }
        })
      }, 500)
    })
  }
  return request.get('/api/merchant/shop/info')
}

// 更新店铺信息
export function updateShopInfo(shopData) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: { ...shopData, updated: true }
        })
      }, 500)
    })
  }
  return request.put('/api/merchant/shop/info', shopData)
}

// 获取我的商品列表
export function getMyProducts(params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const { page = 1, pageSize = 10 } = params
        const products = [
          {
            id: 1,
            name: '我的商品1',
            price: 99,
            image: 'https://via.placeholder.com/300x300?text=Product+1',
            status: 'onSale', // onSale, offShelf, deleted
            stock: 100,
            sales: 50,
            createdAt: '2024-01-01T00:00:00Z'
          },
          {
            id: 2,
            name: '我的商品2',
            price: 199,
            image: 'https://via.placeholder.com/300x300?text=Product+2',
            status: 'onSale',
            stock: 50,
            sales: 30,
            createdAt: '2024-01-02T00:00:00Z'
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
  return request.get('/api/merchant/products', { params })
}

// 上架商品
export function createProduct(productData) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            id: Date.now(),
            ...productData,
            status: 'onSale',
            createdAt: new Date().toISOString()
          }
        })
      }, 500)
    })
  }
  return request.post('/api/merchant/products', productData)
}

// 更新商品
export function updateProduct(productId, productData) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: {
            id: productId,
            ...productData,
            updated: true
          }
        })
      }, 500)
    })
  }
  return request.put(`/api/merchant/products/${productId}`, productData)
}

// 下架商品
export function offShelfProduct(productId) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: { id: productId, status: 'offShelf' }
        })
      }, 500)
    })
  }
  return request.put(`/api/merchant/products/${productId}/offshelf`)
}

// 删除商品
export function deleteProduct(productId) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 0,
          data: { id: productId, deleted: true }
        })
      }, 500)
    })
  }
  return request.delete(`/api/merchant/products/${productId}`)
}

// 获取店铺订单
export function getShopOrders(params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const { page = 1, pageSize = 10 } = params
        const orders = [
          {
            id: 1,
            orderId: 'ORD001',
            productId: 1,
            productName: '我的商品1',
            customerAddress: '0x1234567890123456789012345678901234567890',
            amount: 99,
            status: 'completed',
            txHash: '0xabc123',
            createdAt: '2024-01-10T00:00:00Z'
          }
        ]
        resolve({
          code: 0,
          data: {
            list: orders,
            total: orders.length,
            page,
            pageSize
          }
        })
      }, 500)
    })
  }
  return request.get('/api/merchant/orders', { params })
}

