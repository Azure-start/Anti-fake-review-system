import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const walletAddress = ref('')
  const token = ref('')
  const balance = ref('0')
  const rewardBalance = ref('0')
  const isConnected = ref(false)
  const userRole = ref('user') // 'user' | 'merchant' | 'admin'
  const shopInfo = ref(null) // 商家店铺信息

  // 计算属性
  const formattedAddress = computed(() => {
    if (!walletAddress.value) return ''
    return `${walletAddress.value.slice(0, 6)}...${walletAddress.value.slice(-4)}`
  })

  const isUser = computed(() => userRole.value === 'user')
  const isMerchant = computed(() => userRole.value === 'merchant')
  const isAdmin = computed(() => userRole.value === 'admin')
  const hasShop = computed(() => shopInfo.value !== null)

  // 检查token是否过期（通过解析JWT中的exp字段）
  function isTokenExpired(token) {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return true
      
      // 解码payload（第二部分）
      const payload = JSON.parse(atob(parts[1]))
      const expirationTime = payload.exp * 1000 // 转换为毫秒
      const now = Date.now()
      
      if (now >= expirationTime) {
        console.log('Token已过期，过期时间:', new Date(expirationTime).toLocaleString())
        return true
      }
      return false
    } catch (error) {
      console.error('检查Token过期时出错:', error)
      return true // 如果无法解析，视为过期
    }
  }

  // 初始化时从localStorage恢复状态
  function initStore() {
    const savedAddress = localStorage.getItem('walletAddress')
    const savedToken = localStorage.getItem('token')
    const savedRole = localStorage.getItem('userRole')
    const savedShopInfo = localStorage.getItem('shopInfo')
    
    if (savedAddress && savedToken) {
      // 检查token是否过期
      if (isTokenExpired(savedToken)) {
        console.log('恢复状态时发现Token已过期，清除状态')
        localStorage.removeItem('walletAddress')
        localStorage.removeItem('token')
        localStorage.removeItem('userRole')
        localStorage.removeItem('shopInfo')
        return
      }
      
      walletAddress.value = savedAddress
      token.value = savedToken
      isConnected.value = true
      if (savedRole) {
        userRole.value = savedRole
      }
      if (savedShopInfo) {
        try {
          shopInfo.value = JSON.parse(savedShopInfo)
        } catch (e) {
          console.error('Failed to parse shopInfo:', e)
        }
      }
    }
  }

  // 设置钱包地址
  function setWalletAddress(address) {
    walletAddress.value = address
    localStorage.setItem('walletAddress', address)
  }

  // 设置token
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置余额
  function setBalance(newBalance) {
    balance.value = newBalance
  }

  // 设置奖励余额
  function setRewardBalance(newBalance) {
    rewardBalance.value = newBalance
  }

  // 设置连接状态
  function setConnected(connected) {
    isConnected.value = connected
  }

  // 设置用户角色
  function setUserRole(role) {
    userRole.value = role
    localStorage.setItem('userRole', role)
  }

  // 设置店铺信息
  function setShopInfo(shop) {
    shopInfo.value = shop
    if (shop) {
      localStorage.setItem('shopInfo', JSON.stringify(shop))
    } else {
      localStorage.removeItem('shopInfo')
    }
  }

  // 登出
  function logout() {
    walletAddress.value = ''
    token.value = ''
    balance.value = '0'
    rewardBalance.value = '0'
    isConnected.value = false
    userRole.value = 'user'
    shopInfo.value = null
    localStorage.removeItem('walletAddress')
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('shopInfo')
  }

  return {
    walletAddress,
    token,
    balance,
    rewardBalance,
    isConnected,
    userRole,
    shopInfo,
    formattedAddress,
    isUser,
    isMerchant,
    isAdmin,
    hasShop,
    initStore,
    isTokenExpired,
    setWalletAddress,
    setToken,
    setBalance,
    setRewardBalance,
    setConnected,
    setUserRole,
    setShopInfo,
    logout
  }
})

