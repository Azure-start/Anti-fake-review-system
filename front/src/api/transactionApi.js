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
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const transactions = generateMockTransactions()
        const { page = 1, pageSize = 10 } = params
        const total = transactions.length
        const start = (page - 1) * pageSize
        const end = start + pageSize
        const list = transactions.slice(start, end)
        resolve({ list, total })
      }, 300)
    })
  }
  
  return request({
    url: '/transactions/user',
    method: 'get',
    params
  })
}

/**
 * 获取交易详情
 * @param {string|number} transactionId - 交易ID
 */
export function getTransactionDetail(transactionId) {
  if (USE_MOCK) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const transactions = generateMockTransactions()
        const transaction = transactions.find(t => t.id === Number(transactionId))
        if (transaction) {
          resolve(transaction)
        } else {
          reject(new Error('交易不存在'))
        }
      }, 200)
    })
  }
  
  return request({
    url: `/transactions/${transactionId}`,
    method: 'get'
  })
}

