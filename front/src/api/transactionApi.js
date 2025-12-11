import request from './request'

/**
 * 创建交易订单
 * @param {Object} data - 订单数据
 */
export function createTransaction(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

/**
 * 获取用户交易记录
 * @param {Object} params - 查询参数
 */
export function getUserTransactions(params = {}) {
  return request({
    url: '/orders/user',
    method: 'get',
    params
  })
}

/**
 * 获取交易详情
 * @param {string|number} transactionId - 交易ID
 */
export function getTransactionDetail(transactionId) {

  return request({
    url: `/orders/${transactionId}`,
    method: 'get'
  })
}

/**
 * 更新订单交易哈希
 * @param {string|number} orderId - 订单ID
 * @param {string} txHash - 链上交易哈希
 */
export function updateOrderTxHash(orderId, txHash) {
  return request({
    url: `/orders/${orderId}/tx-hash`,
    method: 'put',
    data: { txHash }
  })
}

/**
 * 更新订单状态
 * @param {string|number} orderId - 订单ID
 * @param {string} status - 订单状态
 */
export function updateOrderStatus(orderId, status) {
  return request({
    url: `/orders/${orderId}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 确认收货
 * @param {string|number} orderId - 订单ID
 * @param {string} userAddress - 用户地址
 */
export function confirmReceipt(orderId, userAddress) {
  return request({
    url: `/orders/${orderId}/confirm`,
    method: 'put',
    params: { userAddress }
  })
}

