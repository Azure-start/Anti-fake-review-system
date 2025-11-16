import request from './request'

/**
 * 获取签名nonce
 * @param {string} address - 钱包地址
 */
export function getNonce(address) {
  return request({
    url: '/auth/nonce',
    method: 'post',
    data: { address }
  })
}

/**
 * 签名登录
 * @param {Object} data - 登录数据
 */
export function signIn(data) {
  return request({
    url: '/auth/signin',
    method: 'post',
    data
  })
}

