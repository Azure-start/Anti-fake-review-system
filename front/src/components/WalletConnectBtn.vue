<template>
  <div class="wallet-connect">
    <el-button
      v-if="!userStore.isConnected"
      type="primary"
      :loading="connecting"
      @click="handleConnect"
    >
      <el-icon><Wallet /></el-icon>
      连接钱包
    </el-button>
    
    <el-dropdown v-else trigger="click" @command="handleCommand">
      <el-button type="primary">
        <el-icon><Wallet /></el-icon>
        {{ userStore.formattedAddress }}
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item disabled>
            <span style="color: #909399">余额: {{ userStore.balance }} ETH</span>
          </el-dropdown-item>
          <el-dropdown-item disabled>
            <span style="color: #909399">奖励: {{ userStore.rewardBalance }} ETH</span>
          </el-dropdown-item>
          <el-dropdown-item divided command="account">
            <el-icon><User /></el-icon>
            我的账户
          </el-dropdown-item>
          <el-dropdown-item command="transactions">
            <el-icon><Document /></el-icon>
            交易记录
          </el-dropdown-item>
          <el-dropdown-item command="rewards">
            <el-icon><Trophy /></el-icon>
            我的奖励
          </el-dropdown-item>
          <el-dropdown-item divided>
            <span style="color: #909399">角色：{{ roleText }}</span>
          </el-dropdown-item>
          <el-dropdown-item divided command="switchRole">
            <el-icon><Setting /></el-icon>
            切换角色（测试）
          </el-dropdown-item>
          <el-dropdown-item divided command="disconnect">
            <el-icon><SwitchButton /></el-icon>
            断开连接
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    
    <!-- 角色切换对话框 -->
    <RoleSwitchDialog v-model="showRoleDialog" />
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Wallet,
  User,
  Document,
  Trophy,
  SwitchButton,
  Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { useChainStore } from '@/stores/chainStore'
import { getProvider, checkNetwork, switchNetwork, formatAddress, getBalance } from '@/utils/chainHelper'
import { getNonce, signIn } from '@/api/authApi'
import RoleSwitchDialog from './RoleSwitchDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const chainStore = useChainStore()
const connecting = ref(false)
const showRoleDialog = ref(false)

// 获取角色文本
function getRoleText(role) {
  const roleMap = {
    user: '用户',
    merchant: '商家',
    admin: '管理员'
  }
  return roleMap[role] || '未知'
}

// 当前角色文本
const roleText = computed(() => getRoleText(userStore.userRole))

// 连接钱包
async function handleConnect() {
    console.log('开始连接钱包...')
    console.log('window.ethereum状态:', window.ethereum)
    console.log('window.ethereum.isMetaMask:', window.ethereum?.isMetaMask)
    
    // 更详细的MetaMask检测
    if (typeof window === 'undefined') {
      ElMessage.error('请在浏览器环境中使用此功能')
      return
    }
    
    if (!window.ethereum) {
      ElMessage.warning('未检测到Web3钱包，请先安装MetaMask或其他兼容钱包')
      return
    }
    
    if (!window.ethereum.isMetaMask) {
      console.warn('检测到的钱包不是MetaMask:', window.ethereum)
      ElMessage.warning('检测到其他钱包，建议使用MetaMask以获得最佳体验')
    }

    // 检查浏览器是否可能阻止弹窗
    console.log('检查浏览器弹窗阻止设置...')
    if (window.ethereum._state && window.ethereum._state.accounts && window.ethereum._state.accounts.length > 0) {
      console.log('MetaMask已有连接账户，无需弹窗:', window.ethereum._state.accounts)
    }

    connecting.value = true
  console.log('MetaMask已检测到，准备获取账户...')
  ElMessage.info('正在连接MetaMask钱包，请查看钱包弹窗...')
  
  // 先检查是否已经连接
  try {
    const accounts = await window.ethereum.request({ method: 'eth_accounts' })
    console.log('当前已连接的账户:', accounts)
    if (accounts.length > 0) {
      console.log('钱包已连接，直接使用现有连接:', accounts[0])
      // 直接使用已连接的账户
      const address = accounts[0]
      
      // 获取余额
      const balance = await getBalance(address)
      
      // 更新状态
      userStore.setWalletAddress(address)
      userStore.setBalance(balance)
      userStore.setConnected(true)
      
      // 强制立即同步状态到localStorage
      localStorage.setItem('walletAddress', address)
      localStorage.setItem('isConnected', 'true')
      
      console.log('使用现有连接成功，状态已更新:', {
        address: userStore.walletAddress,
        balance: userStore.balance,
        isConnected: userStore.isConnected
      })
      
      // 强制刷新组件状态
      await nextTick()
      
      ElMessage.success('钱包连接成功')
      connecting.value = false
      return
    } else {
      console.log('没有已连接的账户，需要请求新连接')
    }
  } catch (checkError) {
    console.error('检查已连接账户失败:', checkError)
  }

  try {
    // 1. 获取钱包地址 - 使用最简化的方法
    console.log('开始获取钱包地址...')
    
    let accounts
    let address
    
    // 方法1: 直接使用window.ethereum.request（最可靠的方法）
    try {
      console.log('尝试使用window.ethereum.request...')
       
       // 确保用户交互触发，防止浏览器阻止弹窗
       console.log('触发用户交互，准备调用eth_requestAccounts...')
       
       // 重要：确保这是用户交互的直接结果
       // 在调用前添加一个小的延迟，确保浏览器识别为用户交互
       await new Promise(resolve => setTimeout(resolve, 100))
       
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('MetaMask连接超时，请检查钱包是否已解锁')), 30000)
      })
      
       // 添加用户提示，确保用户知道要查看弹窗
       ElMessage.info('正在连接MetaMask钱包，请查看浏览器弹窗...')
       
       // 确保调用是用户交互的直接结果 - 使用同步方式确保调用链
       console.log('立即调用eth_requestAccounts...')
       const requestPromise = window.ethereum.request({ method: 'eth_requestAccounts' })
       
      accounts = await Promise.race([
        requestPromise,
        timeoutPromise
      ])
      
      console.log('使用request方法成功，获取到账户:', accounts)
      address = accounts[0]
      
    } catch (requestError) {
      console.error('request方法失败:', requestError)
      
      // 方法2: 尝试使用provider
      try {
        console.log('尝试使用provider方法...')
        const provider = getProvider()
        const timeoutPromise = new Promise((_, reject) => {
          setTimeout(() => reject(new Error('MetaMask连接超时，请检查钱包是否已解锁')), 30000)
        })
        
        accounts = await Promise.race([
          provider.send('eth_requestAccounts', []),
          timeoutPromise
        ])
        
        console.log('使用provider方法成功，获取到账户:', accounts)
        address = accounts[0]
        
      } catch (providerError) {
        console.error('provider方法也失败:', providerError)
        throw new Error(`无法获取钱包账户: ${requestError.message} | ${providerError.message}`)
      }
    }
    
    console.log('最终获取到的地址:', address)
    
    // 2. 检测并切换网络（暂时禁用自动网络切换）
    // const networkInfo = await checkNetwork()
    // chainStore.setNetwork(networkInfo.currentChainId, networkInfo.chainName, networkInfo.isCorrectNetwork)
    
    // if (!networkInfo.isCorrectNetwork) {
    //   const result = await ElMessageBox.confirm(
    //     `当前网络不正确，需要切换到${networkInfo.chainName}测试网络`,
    //     '网络切换',
    //     {
    //       confirmButtonText: '切换网络',
    //       cancelButtonText: '取消',
    //       type: 'warning'
    //     }
    //   )
      
    //   if (result === 'confirm') {
    //     const switched = await switchNetwork()
    //     if (switched) {
    //       ElMessage.success('网络切换成功')
    //       chainStore.setNetwork(networkInfo.targetChainId, networkInfo.chainName, true)
    //     } else {
    //       ElMessage.error('网络切换失败')
    //       connecting.value = false
    //       return
    //     }
    //   } else {
    //     connecting.value = false
    //     return
    //   }
    // }

    // 3. 获取余额
    const balance = await getBalance(address)
    
    // 4. 签名登录（暂时跳过，等后端就绪）
    try {
      await signInWallet(address)
    } catch (signError) {
      console.warn('签名登录失败（后端可能未启动）:', signError)
      // 后端未启动时跳过签名，仍然认为连接成功
    }

    // 5. 更新状态
    console.log('更新用户状态...')
    userStore.setWalletAddress(address)
    userStore.setBalance(balance)
    userStore.setConnected(true)
    
    // 强制立即同步状态到localStorage
    localStorage.setItem('walletAddress', address)
    localStorage.setItem('isConnected', 'true')
    
    console.log('连接状态已更新:', {
      address: userStore.walletAddress,
      balance: userStore.balance,
      isConnected: userStore.isConnected
    })
    
    // 强制刷新组件状态
    await nextTick()
    console.log('UI状态已强制刷新')

    ElMessage.success('钱包连接成功')

    // 登录成功后询问是否注册为商家（默认为普通用户）
    try {
      // 仅普通用户时弹窗；商家/管理员不弹
      if (userStore.userRole === 'user') {
        await ElMessageBox.confirm(
          '是否注册为商家？注册后可管理店铺与商品（仍可购买他人商品）。',
          '注册为商家',
          {
            confirmButtonText: '注册为商家',
            cancelButtonText: '暂不注册',
            type: 'info'
          }
        )
        // 用户选择“注册为商家”
        userStore.setUserRole('merchant')
        ElMessage.success('已切换为商家角色')
        router.push('/merchant/shop/apply')
      }
    } catch (e) {
      // 用户选择“暂不注册”或关闭弹窗，保持普通用户
    }
  } catch (error) {
    console.error('连接钱包失败:', error)
    console.error('错误详情:', {
      code: error.code,
      message: error.message,
      stack: error.stack
    })
    if (error.code === 4001) {
      ElMessage.info('用户取消了连接')
    } else {
      ElMessage.error(`连接钱包失败: ${error.message || '请重试'}`)
    }
  } finally {
    connecting.value = false
  }
}

// 签名登录
async function signInWallet(address) {
  try {
    // 获取nonce
    const nonceData = await getNonce(address)
    const nonce = nonceData.nonce

    // 签名消息
    const provider = getProvider()
    const signer = await provider.getSigner()
    const message = `请签名以验证您的身份\n\nNonce: ${nonce}`
    const signature = await signer.signMessage(message)

    // 提交签名
    const authData = await signIn({
      address,
      signature,
      nonce: nonce
    })

    // 保存token
    userStore.setToken(authData.token)
  } catch (error) {
    console.error('签名登录失败:', error)
    throw error
  }
}

// 处理下拉菜单命令
function handleCommand(command) {
  switch (command) {
    case 'account':
      router.push('/account')
      break
    case 'transactions':
      router.push('/transactions')
      break
    case 'rewards':
      router.push('/rewards')
      break
    case 'switchRole':
      handleSwitchRole()
      break
    case 'disconnect':
      handleDisconnect()
      break
  }
}

// 切换角色（测试用）
function handleSwitchRole() {
  showRoleDialog.value = true
}

// 断开连接
function handleDisconnect() {
  ElMessageBox.confirm(
    '确定要断开钱包连接吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.logout()
    chainStore.resetNetwork()
    ElMessage.success('已断开连接')
    router.push('/')
  }).catch(() => {})
}
</script>

<style scoped>
.wallet-connect {
  display: inline-block;
}

.wallet-connect :deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

.wallet-connect :deep(.el-icon) {
  font-size: 18px;
}
</style>

