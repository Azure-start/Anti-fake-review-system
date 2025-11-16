import request from './request'
import { generateMockRewards } from './mockData'

// 是否使用Mock数据
const USE_MOCK = import.meta.env.MODE === 'development'

/**
 * 获取奖励余额
 */
export function getRewardBalance() {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ balance: '0.008' })
      }, 300)
    })
  }
  
  return request({
    url: '/rewards/balance',
    method: 'get'
  })
}

/**
 * 获取奖励记录
 * @param {Object} params - 查询参数
 */
export function getRewardHistory(params = {}) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        const rewards = generateMockRewards()
        resolve({ list: rewards, total: rewards.length })
      }, 300)
    })
  }
  
  return request({
    url: '/rewards/history',
    method: 'get',
    params
  })
}

/**
 * 兑换优惠券
 * @param {Object} data - 兑换数据
 */
export function exchangeCoupon(data) {
  if (USE_MOCK) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ success: true, message: '兑换成功' })
      }, 500)
    })
  }
  
  return request({
    url: '/rewards/exchange',
    method: 'post',
    data
  })
}

