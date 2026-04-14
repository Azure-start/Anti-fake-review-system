import request from './request'

/**
 * 获取商品列表
 * @param {Object} params - 查询参数
 */
export function getProductList(params = {}) {
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
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

