import request from './request'
import { mockProducts, generateMockReviews } from './mockData'

// 是否使用Mock数据（开发模式且后端未启动时自动启用）
const USE_MOCK = import.meta.env.MODE === 'development'

/**
 * 获取商品列表
 * @param {Object} params - 查询参数
 */
export function getProductList(params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const { page = 1, pageSize = 12, keyword } = params
        let filteredProducts = [...mockProducts]
        
        // 搜索过滤
        if (keyword) {
          filteredProducts = filteredProducts.filter(p => 
            p.name.toLowerCase().includes(keyword.toLowerCase())
          )
        }
        
        // 分页
        const total = filteredProducts.length
        const start = (page - 1) * pageSize
        const end = start + pageSize
        const list = filteredProducts.slice(start, end)
        
        resolve({ list, total })
      }, 300)
    })
  }
  
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 * @param {string|number} productId - 商品ID
 */
export function getProductDetail(productId) {
  if (USE_MOCK) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const product = mockProducts.find(p => p.id === Number(productId))
        if (product) {
          resolve(product)
        } else {
          reject(new Error('商品不存在'))
        }
      }, 200)
    })
  }
  
  return request({
    url: `/products/${productId}`,
    method: 'get'
  })
}

/**
 * 获取商品评价列表
 * @param {string|number} productId - 商品ID
 * @param {Object} params - 查询参数
 */
export function getProductReviews(productId, params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const reviews = generateMockReviews(productId)
        resolve({ list: reviews, total: reviews.length })
      }, 200)
    })
  }
  
  return request({
    url: `/products/${productId}/reviews`,
    method: 'get',
    params
  })
}

/**
 * 发布评价
 * @param {Object} data - 评价数据
 */
export function submitReview(data) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          nftId: `NFT_${data.productId}_${Date.now()}`,
          txHash: `0x${Math.random().toString(16).slice(2, 66)}`
        })
      }, 1000)
    })
  }
  
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

