/**
 * 登录功能测试工具
 * 用于前端开发调试和后端接口集成测试
 * 
 * 使用方法：
 * 1. 在浏览器控制台中导入或复制此文件的代码
 * 2. 调用相应的测试函数
 */

import { useUserStore } from '@/stores/userStore'
import { getNonce, signIn } from '@/api/authApi'
import { getProvider, getBalance } from '@/utils/chainHelper'

/**
 * 登录测试工具
 */
export const LoginTestUtils = {
  
  /**
   * 检查MetaMask是否已安装
   */
  checkMetaMask() {
    const installed = typeof window.ethereum !== 'undefined' && window.ethereum.isMetaMask
    console.log(`MetaMask ${installed ? '已安装' : '未安装'}`)
    return installed
  },

  /**
   * 获取当前连接的账户
   */
  async getCurrentAccounts() {
    try {
      const accounts = await window.ethereum.request({ method: 'eth_accounts' })
      console.log('当前账户:', accounts)
      return accounts
    } catch (error) {
      console.error('获取账户失败:', error)
      return []
    }
  },

  /**
   * 获取指定地址的nonce
   */
  async testGetNonce(address) {
    try {
      console.log(`正在获取 ${address} 的 nonce...`)
      const nonce = await getNonce(address)
      console.log('获取成功，nonce:', nonce)
      return nonce
    } catch (error) {
      console.error('获取nonce失败:', error)
      throw error
    }
  },

  /**
   * 测试完整的签名登录流程（需要手动在MetaMask中确认）
   */
  async testSignIn(address) {
    try {
      console.log('开始测试签名登录流程...')
      
      // 1. 获取nonce
      console.log('第1步：获取nonce')
      const nonce = await this.testGetNonce(address)
      
      // 2. 签名消息
      console.log('第2步：签名消息，请在MetaMask中确认')
      const provider = getProvider()
      const signer = await provider.getSigner()
      const message = `请签名以验证您的身份\n\nNonce: ${nonce}`
      const signature = await signer.signMessage(message)
      console.log('签名成功:', signature)
      
      // 3. 提交登录
      console.log('第3步：提交登录请求到后端')
      const result = await signIn({
        address,
        signature,
        nonce
      })
      console.log('登录成功，返回数据:', result)
      
      return result
    } catch (error) {
      console.error('签名登录失败:', error)
      throw error
    }
  },

  /**
   * 保存token到userStore（用于测试）
   */
  saveToken(token, role = 'user') {
    const userStore = useUserStore()
    userStore.setToken(token)
    if (role) {
      userStore.setUserRole(role)
    }
    console.log('Token已保存，用户角色:', role)
  },

  /**
   * 查看当前用户状态
   */
  viewUserState() {
    const userStore = useUserStore()
    console.log('=== 当前用户状态 ===')
    console.log('钱包地址:', userStore.walletAddress)
    console.log('Token:', userStore.token ? userStore.token.substring(0, 30) + '...' : '无')
    console.log('余额:', userStore.balance)
    console.log('奖励余额:', userStore.rewardBalance)
    console.log('连接状态:', userStore.isConnected)
    console.log('用户角色:', userStore.userRole)
    console.log('店铺信息:', userStore.shopInfo)
    console.log('==================')
    return {
      walletAddress: userStore.walletAddress,
      token: userStore.token,
      balance: userStore.balance,
      rewardBalance: userStore.rewardBalance,
      isConnected: userStore.isConnected,
      userRole: userStore.userRole,
      shopInfo: userStore.shopInfo
    }
  },

  /**
   * 查看localStorage中的持久化数据
   */
  viewPersistentData() {
    console.log('=== localStorage 中的数据 ===')
    console.log('walletAddress:', localStorage.getItem('walletAddress'))
    console.log('token:', localStorage.getItem('token') ? localStorage.getItem('token').substring(0, 30) + '...' : '无')
    console.log('userRole:', localStorage.getItem('userRole'))
    console.log('shopInfo:', localStorage.getItem('shopInfo'))
    console.log('isConnected:', localStorage.getItem('isConnected'))
    console.log('=============================')
  },

  /**
   * 清除所有登录信息（登出）
   */
  clearAll() {
    const userStore = useUserStore()
    userStore.logout()
    console.log('已清除所有登录信息')
    this.viewUserState()
  },

  /**
   * 模拟登录（用于后端未就绪时测试）
   */
  mockLogin(address, token = null, role = 'user') {
    const userStore = useUserStore()
    userStore.setWalletAddress(address)
    userStore.setToken(token || 'mock_token_' + Date.now())
    userStore.setConnected(true)
    userStore.setUserRole(role)
    console.log('模拟登录成功')
    this.viewUserState()
  },

  /**
   * 获取账户余额
   */
  async getAccountBalance(address) {
    try {
      console.log(`正在获取 ${address} 的余额...`)
      const balance = await getBalance(address)
      console.log('余额:', balance, 'ETH')
      return balance
    } catch (error) {
      console.error('获取余额失败:', error)
      throw error
    }
  },

  /**
   * 完整的调试信息
   */
  fullDebugInfo() {
    console.log('===== 完整调试信息 =====')
    console.log('1. MetaMask状态:')
    this.checkMetaMask()
    
    console.log('\n2. 用户状态:')
    this.viewUserState()
    
    console.log('\n3. 持久化数据:')
    this.viewPersistentData()
  }
}

// 导出供浏览器控制台使用
window.LoginTestUtils = LoginTestUtils

export default LoginTestUtils
