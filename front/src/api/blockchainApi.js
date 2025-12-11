import request from './request'

/**
 * 获取评论的区块链详情
 * @param {string|number} reviewId - 评论ID
 */
export function getBlockchainReview(reviewId) {
  return request({
    url: `/reviews/blockchain/${reviewId}`,
    method: 'get'
  })
}

/**
 * 获取区块链上的评论列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页条数
 */
export function getBlockchainReviews(params = {}) {
  return request({
    url: '/reviews/blockchain-list',
    method: 'get',
    params
  })
}

/**
 * 上传评论到区块链
 * @param {string|number} reviewId - 评论ID
 */
export function uploadReviewToBlockchain(reviewId) {
  return request({
    url: `/reviews/upload-to-blockchain/${reviewId}`,
    method: 'post'
  })
}

/**
 * 批量上传所有未上链的评论
 */
export function uploadAllReviewsToBlockchain() {
  return request({
    url: '/reviews/upload-all-to-blockchain',
    method: 'post'
  })
}

/**
 * 调试接口：检查评论的区块链状态
 * @param {string|number} reviewId - 评论ID
 */
export function debugBlockchainStatus(reviewId) {
  return request({
    url: `/reviews/debug/blockchain-status/${reviewId}`,
    method: 'get'
  })
}