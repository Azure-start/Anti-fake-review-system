import request from './request'

/**
 * 获取签名nonce
 * @param {string} address - 钱包地址
 * @returns {Promise<string>} nonce值
 */
export async function getNonce(address) {
  try {
    const response = await request({
      url: '/auth/nonce',
      method: 'post',
      data: { address }
    })
    
    // 处理响应数据 - 可能是直接返回nonce字符串，也可能是对象
    if (typeof response === 'string') {
      return response
    }
    
    // 如果是对象，尝试从各个可能的字段提取nonce
    if (response && typeof response === 'object') {
      return response.nonce || response.data || response
    }
    
    throw new Error('无法从响应中提取nonce')
  } catch (error) {
    console.error('获取nonce失败:', error)
    throw error
  }
}

/**
 * 签名登录
 * @param {Object} data - 登录数据 { address, signature, nonce }
 * @returns {Promise<Object>} 登录结果 { token, user, role等 }
 */
export async function signIn(data) {
  try {
    const response = await request({
      url: '/auth/signin',
      method: 'post',
      data
    })
    
    // 统一解析后端返回结构，确保拿到 token / user / role
    // 典型结构：{ code, user, token } 或 { code, data: { user, token } }
    if (!response) {
      throw new Error('登录失败：空响应')
    }
    
    // 有些情况下拦截器已经返回的是 data.data，这里统一兼容
    const raw = response
    const wrapped = raw.data && typeof raw.data === 'object' ? raw.data : raw

    const user =
      wrapped.user ||
      raw.user ||
      wrapped.userData ||
      raw.userData ||
      null

    const token =
      wrapped.token ||
      raw.token ||
      wrapped.jwtToken ||
      raw.jwtToken ||
      wrapped.accessToken ||
      raw.accessToken ||
      ''

    const role =
      wrapped.role ||
      raw.role ||
      (user && user.role) ||
      wrapped.userRole ||
      raw.userRole ||
      'user'

    if (!token) {
      throw new Error('登录失败：未获取到token')
    }

      return {
      token,
      user,
      role
    }
  } catch (error) {
    console.error('登录失败:', error)
    throw error
  }
}

