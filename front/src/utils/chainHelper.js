import { ethers } from 'ethers'

// 获取环境变量配置
const CHAIN_ID = import.meta.env.VITE_CHAIN_ID || 11155111
const CHAIN_NAME = import.meta.env.VITE_CHAIN_NAME || 'Sepolia'

/**
 * 检测MetaMask是否已安装
 */
export function isMetaMaskInstalled() {
  return typeof window.ethereum !== 'undefined' && window.ethereum.isMetaMask
}

/**
 * 获取ethers provider
 */
export function getProvider() {
  if (!isMetaMaskInstalled()) {
    throw new Error('MetaMask未安装')
  }
  return new ethers.BrowserProvider(window.ethereum)
}

/**
 * 检测当前网络是否为指定测试网络
 */
export async function checkNetwork() {
  try {
    const provider = getProvider()
    const network = await provider.getNetwork()
    const currentChainId = Number(network.chainId)
    
    return {
      isCorrectNetwork: currentChainId === Number(CHAIN_ID),
      currentChainId,
      targetChainId: Number(CHAIN_ID),
      chainName: CHAIN_NAME
    }
  } catch (error) {
    console.error('检测网络失败:', error)
    return {
      isCorrectNetwork: false,
      currentChainId: null,
      targetChainId: Number(CHAIN_ID),
      chainName: CHAIN_NAME
    }
  }
}

/**
 * 切换到指定网络
 */
export async function switchNetwork() {
  if (!isMetaMaskInstalled()) {
    throw new Error('MetaMask未安装')
  }

  const chainId = `0x${Number(CHAIN_ID).toString(16)}`
  
  try {
    await window.ethereum.request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId }]
    })
    return true
  } catch (switchError) {
    // 如果网络不存在，尝试添加网络
    if (switchError.code === 4902) {
      try {
        await window.ethereum.request({
          method: 'wallet_addEthereumChain',
          params: [{
            chainId,
            chainName: CHAIN_NAME,
            nativeCurrency: {
              name: 'ETH',
              symbol: 'ETH',
              decimals: 18
            },
            rpcUrls: ['https://sepolia.infura.io/v3/'],
            blockExplorerUrls: ['https://sepolia.etherscan.io/']
          }]
        })
        return true
      } catch (addError) {
        console.error('添加网络失败:', addError)
        return false
      }
    }
    console.error('切换网络失败:', switchError)
    return false
  }
}

/**
 * 格式化以太坊地址（显示前6位和后4位）
 */
export function formatAddress(address) {
  if (!address) return ''
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

/**
 * 获取账户余额
 */
export async function getBalance(address) {
  try {
    const provider = getProvider()
    const balance = await provider.getBalance(address)
    return ethers.formatEther(balance)
  } catch (error) {
    console.error('获取余额失败:', error)
    return '0'
  }
}

