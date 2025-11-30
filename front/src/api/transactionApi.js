import request from './request'
import { generateMockTransactions } from './mockData'

// 是否使用Mock数据
const USE_MOCK = import.meta.env.MODE === 'development'

/**
 * 创建交易订单
 * @param {Object} data - 订单数据
 */
export function createTransaction(data) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          orderId: `ORD${Date.now()}`,
          txHash: `0x${Math.random().toString(16).slice(2, 66)}`
        })
      }, 800)
    })
  }
  
  return request({
    url: '/transactions/create',
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

