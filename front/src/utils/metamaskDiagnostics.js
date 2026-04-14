/**
 * MetaMask 签名诊断工具
 * 在浏览器控制台中复制并执行此代码，诊断为什么签名弹窗没有出现
 */

console.log('=== MetaMask 诊断工具 ===\n')

// 1. 检查MetaMask是否安装
console.log('1️⃣ 检查MetaMask是否安装:')
if (typeof window.ethereum !== 'undefined') {
  console.log('✅ MetaMask已安装')
  console.log('   isMetaMask:', window.ethereum.isMetaMask)
} else {
  console.error('❌ MetaMask未安装或不可用')
}

// 2. 检查是否有已连接的账户
console.log('\n2️⃣ 检查是否有已连接的账户:')
window.ethereum.request({ method: 'eth_accounts' })
  .then(accounts => {
    if (accounts.length > 0) {
      console.log('✅ 已连接账户:', accounts[0])
    } else {
      console.warn('⚠️ 没有已连接的账户，需要先连接')
    }
  })
  .catch(err => {
    console.error('❌ 检查账户出错:', err.message)
  })

// 3. 检查当前网络
console.log('\n3️⃣ 检查当前网络:')
window.ethereum.request({ method: 'eth_chainId' })
  .then(chainId => {
    console.log('当前Chain ID:', chainId)
    console.log('Sepolia Chain ID: 0xaa36a7 (11155111)')
    if (chainId === '0xaa36a7') {
      console.log('✅ 正在使用Sepolia测试网')
    } else {
      console.warn('⚠️ 不在Sepolia网络上，请切换网络')
    }
  })
  .catch(err => {
    console.error('❌ 检查网络出错:', err.message)
  })

// 4. 测试签名功能
console.log('\n4️⃣ 测试签名功能:')
console.log('准备测试签名，请等待...')

async function testSign() {
  try {
    // 获取账户
    const accounts = await window.ethereum.request({ method: 'eth_accounts' })
    if (accounts.length === 0) {
      console.error('❌ 没有账户，无法测试签名')
      return
    }

    // 测试消息
    const testMessage = '这是一条测试消息\nNonce: test123'
    console.log('要签名的消息:', testMessage)
    console.log('请在MetaMask中点击"签名"按钮...')

    // 请求签名
    const signature = await window.ethereum.request({
      method: 'personal_sign',
      params: [testMessage, accounts[0]]
    })

    console.log('✅ 签名成功!')
    console.log('签名结果:', signature)
  } catch (error) {
    console.error('❌ 签名失败:', error.message)
    console.error('错误代码:', error.code)
    
    if (error.code === 4001) {
      console.warn('用户取消了签名')
    } else if (error.message?.includes('locked')) {
      console.error('MetaMask已锁定，请解锁')
    } else {
      console.error('其他错误，详见上方信息')
    }
  }
}

testSign()

console.log('\n=== 诊断完成 ===')
console.log('如果看到签名成功的提示，说明MetaMask工作正常')
console.log('如果没有弹窗，请检查:')
console.log('  1. MetaMask是否已安装和解锁')
console.log('  2. 浏览器是否阻止了弹窗')
console.log('  3. MetaMask是否连接到此网站')
console.log('  4. 是否在Sepolia测试网')
