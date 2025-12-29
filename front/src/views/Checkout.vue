<template>
  <div class="checkout">
    <div class="container">
      <el-card header="确认订单" class="checkout-card">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="orderData">
          <el-steps :active="currentStep" finish-status="success">
            <el-step title="确认订单" />
            <el-step title="确认支付" />
            <el-step title="支付成功" />
          </el-steps>

          <el-divider />

          <!-- 订单信息 -->
          <div v-if="currentStep === 0" class="order-section">
            <h3>商品信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品名称">{{ orderData.productName }}</el-descriptions-item>
              <el-descriptions-item label="商品价格">¥{{ orderData.price }}</el-descriptions-item>
              <el-descriptions-item label="商品规格" :span="2">{{ orderData.specText }}</el-descriptions-item>
            </el-descriptions>

            <h3 style="margin-top: 24px;">收货信息</h3>
            <el-form :model="addressForm" label-width="100px">
              <el-form-item label="收货人">
                <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="addressForm.phone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="收货地址">
                <el-input
                  v-model="addressForm.address"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入详细地址"
                />
              </el-form-item>
            </el-form>

            <div class="order-total">
              <div class="total-item">
                <span>合计：</span>
                <span class="total-price">¥{{ orderData.totalAmount }}</span>
              </div>
            </div>

            <div v-if="isSelfPurchase" class="self-purchase-alert" style="margin-top: 12px;">
              <el-alert
                type="warning"
                :closable="false"
                show-icon
                title="商家无法购买自家商品"
                description="该商品属于您的店铺，无法下单。请返回商品页选择其他店铺的商品。"
              />
            </div>

            <div class="order-actions">
              <el-button @click="handleCancel">取消</el-button>
              <el-button type="primary" @click="handleConfirmOrder" :disabled="isSelfPurchase">确认订单</el-button>
            </div>
          </div>

          <!-- 支付状态 -->
          <div v-else-if="currentStep === 1" class="payment-section">
            <el-result
              :icon="paymentStatus === 'pending' ? 'info' : paymentStatus === 'success' ? 'success' : 'error'"
              :title="paymentStatusText"
              :sub-title="paymentSubText"
            >
              <template #extra>
                <div class="payment-info">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="订单号">{{ orderData.orderId }}</el-descriptions-item>
                    <el-descriptions-item label="订单金额">¥{{ orderData.totalAmount }}</el-descriptions-item>
                    <el-descriptions-item label="实际转账金额">{{ (parseFloat(orderData.totalAmount) / 100000).toFixed(6) }} ETH（测试网已缩小100000倍）</el-descriptions-item>
                  <el-descriptions-item label="Gas费用">
                    <span v-if="gasPrice">{{ gasPrice }} GWEI（测试网已优化）</span>
                    <span v-else>自动获取</span>
                  </el-descriptions-item>
                    <el-descriptions-item label="TX哈希（链上交易ID）">{{ txHashDisplay }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                <div class="payment-actions" style="margin-top: 20px;">
                  <el-button 
                    v-if="paymentStatus === 'pending'" 
                    type="primary" 
                    @click="handleTransfer"
                    size="large"
                  >
                    确认支付
                  </el-button>
                  <el-button 
                    v-if="paymentStatus === 'success'" 
                    type="success" 
                    @click="handleViewTransactions"
                    size="large"
                  >
                    查看订单
                  </el-button>
                  <el-button 
                    v-if="paymentStatus === 'failed'" 
                    type="warning" 
                    @click="handleTransfer"
                    size="large"
                  >
                    重新支付
                  </el-button>
                  <el-button 
                    v-if="paymentStatus === 'pending'" 
                    @click="handleCheckPayment"
                    :loading="refreshing"
                    size="large"
                  >
                    刷新状态
                  </el-button>
                </div>
              </template>
            </el-result>
          </div>

          <!-- 支付成功 -->
          <div v-else-if="currentStep === 2" class="success-section">
            <el-result
              icon="success"
              title="支付成功"
              sub-title="您的订单已成功支付，请等待收货后确认收货，然后可以发布评价"
            >
              <template #extra>
                <div class="success-actions">
                  <el-button @click="handleBackHome">返回首页</el-button>
                  <el-button type="primary" @click="handleViewTransactions">查看订单</el-button>
                </div>
              </template>
            </el-result>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTransaction, updateOrderTxHash, updateOrderStatus, getTransactionDetail } from '@/api/transactionApi'
import { getProductDetail } from '@/api/productApi'
import { useUserStore } from '@/stores/userStore'
import { ethers } from 'ethers'   // ← 新增

/* ---------- 原有状态 ---------- */

// 调试函数：测试以太坊地址验证
function testAddressValidation() {
  const testAddresses = [
    '0x283e75e074cBfB0Da64F0145315De2fDc3d633E7',  // 已知有效的地址
    '0x70997970C51812dc3A010C7d01b50e0d17dc79C8',  // 测试地址
    '',  // 空地址
    'undefined',  // 字符串undefined
    'null',  // 字符串null
    null,  // null值
    undefined  // undefined值
  ]
  
  console.log('=== 以太坊地址验证测试 ===')
  testAddresses.forEach((addr, index) => {
    try {
      const result = ethers.isAddress(addr)
      console.log(`测试地址 ${index}: ${addr} => ${result}`)
    } catch (error) {
      console.log(`测试地址 ${index}: ${addr} => 错误: ${error.message}`)
    }
  })
}
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orderData = ref(null)
const currentStep = ref(0)
const paymentStatus = ref('pending') // pending / success / failed
const gasPrice = ref('') // 当前gas价格
const refreshing = ref(false) // 刷新状态按钮的加载状态

const addressForm = ref({
  name: '',
  phone: '',
  address: ''
})

/* ---------- 计算属性 ---------- */
const paymentStatusText = computed(() =>
    paymentStatus.value === 'pending'
        ? '等待支付'
        : paymentStatus.value === 'success'
            ? '支付成功'
            : '支付失败'
)

const txHashDisplay = computed(() => {
  if (!orderData.value?.txHash) return '处理中...'
  // 显示从链上获取的交易ID
  return orderData.value.txHash
})

const paymentSubText = computed(() =>
    paymentStatus.value === 'pending'
        ? '请在钱包中确认转账（测试网金额已缩小100000倍）'
        : paymentStatus.value === 'success'
            ? '链上已确认，支付完成'
            : '用户取消或交易失败'
)

const isSelfPurchase = computed(() => {
  if (!orderData.value) return false
  const m = orderData.value.merchantAddress?.toLowerCase()
  const u = userStore.walletAddress?.toLowerCase()
  return userStore.isMerchant && m && u && m === u
})

/* ---------- 生命周期 ---------- */
onMounted(() => {
  // 调试：测试地址验证
  testAddressValidation()
  initOrder()
})

/* ---------- 方法 ---------- */
async function initOrder() {
  loading.value = true
  try {
    const pid = route.query.productId
    const orderId = route.query.orderId
    const from = route.query.from

    if (!pid) return ElMessage.error('缺少商品信息'), router.replace('/')

    // 如果是从交易记录页面过来的待支付订单，获取订单详情
    if (from === 'transactions' && orderId) {
      try {
        console.log('从交易记录页面加载待支付订单，订单ID:', orderId)
        const orderDetail = await getTransactionDetail(orderId)
        console.log('订单详情:', orderDetail)
        
        if (orderDetail && orderDetail.status === 'pending') {
          // 获取商品信息
          const prod = await getProductDetail(pid)
          
          // 验证商户地址，如果订单中的地址无效则使用商品中的地址
          let finalMerchantAddress = orderDetail.merchantAddress
          let finalMerchantName = orderDetail.merchantName
          
          console.log('订单详情中的商户地址:', orderDetail.merchantAddress)
          console.log('商品详情中的商户地址:', prod.merchantAddress)
          console.log('订单详情中的商户名称:', orderDetail.merchantName)
          console.log('商品详情中的商户名称:', prod.merchantName)
          
          // 如果订单中的商户地址无效，使用商品中的商户地址
          if (!orderDetail.merchantAddress || !ethers.isAddress(orderDetail.merchantAddress)) {
            console.log('订单中的商户地址无效，使用商品中的商户地址')
            finalMerchantAddress = prod.merchantAddress
            finalMerchantName = prod.merchantName || orderDetail.merchantName
          }
          
          orderData.value = {
            orderId: orderDetail.orderId,
            productId: prod.id,
            productName: prod.name,
            price: orderDetail.amount || prod.price,
            spec: orderDetail.spec || {},
            specText: orderDetail.specText || '默认规格',
            totalAmount: orderDetail.amount || prod.price,
            merchantName: finalMerchantName,
            merchantAddress: finalMerchantAddress,
            fromTransactions: true  // 标记来自交易记录的待支付订单
          }
          
          // 直接进入支付确认步骤
          currentStep.value = 1
          ElMessage.info('正在加载待支付订单，请完成支付')
          
          // 如果订单有地址信息，填充表单
          if (orderDetail.address) {
            addressForm.value = { ...orderDetail.address }
          }
          
          console.log('待支付订单加载成功:', orderData.value)
          console.log('最终使用的商户名称:', finalMerchantName)
          console.log('最终使用的商户地址:', finalMerchantAddress)
          console.log('最终商户地址格式验证:', ethers.isAddress(finalMerchantAddress))
          loading.value = false
          return
        }
      } catch (orderError) {
        console.warn('获取订单详情失败，将创建新订单:', orderError)
      }
    }

    // 正常流程：创建新订单
    const prod = await getProductDetail(pid)
    const spec = JSON.parse(route.query.spec || '{}')
    const specText = Object.entries(spec)
        .map(([k, v]) => `${k}:${v}`)
        .join(', ') || '默认规格'

    orderData.value = {
      productId: prod.id,
      productName: prod.name,
      price: prod.price,
      spec,
      specText,
      totalAmount: prod.price,
      merchantName: prod.merchantName,
      merchantAddress: prod.merchantAddress
    }

    // 调试：检查商品详情数据
    console.log('=== 正常商品加载调试信息 ===')
    console.log('商品详情数据:', prod)
    console.log('商户地址:', prod.merchantAddress)
    console.log('商户地址长度:', prod.merchantAddress?.length)
    console.log('商户地址格式验证:', prod.merchantAddress ? ethers.isAddress(prod.merchantAddress) : '地址为空')
    console.log('商户名称:', prod.merchantName)

    // 商家自检
    if (
        userStore.isMerchant &&
        prod.merchantAddress &&
        userStore.walletAddress &&
        prod.merchantAddress.toLowerCase() ===
        userStore.walletAddress.toLowerCase()
    ) {
      ElMessage.warning('商家不能购买自家商品')
      return router.replace(`/product/${prod.id}`)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载订单失败')
    router.replace('/')
  } finally {
    loading.value = false
  }
}

async function handleConfirmOrder() {
  if (!addressForm.value.name || !addressForm.value.phone || !addressForm.value.address)
    return ElMessage.warning('请填写完整收货信息')

  if (isSelfPurchase.value) {
    ElMessage.warning('商家不能购买自家商品')
    return router.push(`/product/${orderData.value.productId}`)
  }

  // 如果是从交易记录页面过来的待支付订单，直接跳到支付步骤
  if (orderData.value.fromTransactions && orderData.value.orderId) {
    currentStep.value = 1
    ElMessage.info('正在跳转到支付页面，请完成支付')
    return
  }

  try {
    ElMessage.info('正在创建订单...')
    const res = await createTransaction({
      productId: orderData.value.productId,
      spec: orderData.value.spec,
      address: addressForm.value,
      userAddress: userStore.walletAddress
    })

    orderData.value.orderId = res.orderId
    orderData.value.txHash = res.txHash // 后端返回的订单哈希（非链上）
    currentStep.value = 1
    ElMessage.success('订单创建成功，请完成链上支付')
  } catch (e) {
    console.error(e)
    ElMessage.error('创建订单失败')
  }
}

/* ===== 核心：弹出 MetaMask 发起转账 ===== */
async function handleTransfer() {
  try {
    // 检查是否安装了MetaMask
    if (!window.ethereum) {
      ElMessage.error('请先安装MetaMask钱包')
      return
    }

    // 检查订单数据完整性
    if (!orderData.value || !orderData.value.merchantAddress || !orderData.value.totalAmount) {
      ElMessage.error('订单数据不完整，请重新下单')
      return
    }

    // 请求账户访问
    const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' })
    const from = accounts[0]
    let to = orderData.value.merchantAddress?.trim()
    
    // 调试：检查商户地址
    console.log('=== 支付确认调试信息 ===')
    console.log('订单数据中的原始商户地址:', orderData.value?.merchantAddress)
    console.log('原始商户地址类型:', typeof orderData.value?.merchantAddress)
    console.log('原始商户地址长度:', orderData.value?.merchantAddress?.length)
    console.log('清理后的商户地址:', to)
    console.log('清理后的商户地址类型:', typeof to)
    console.log('清理后的商户地址长度:', to?.length)
    
    // 验证商户地址
    if (!to || to === 'undefined' || to === 'null') {
      ElMessage.warning('商户地址无效，使用测试地址进行转账')
      console.log('商户地址验证失败: 地址为空或未定义')
      // 使用Goerli测试网的一个常用测试地址
      to = '0x70997970C51812dc3A010C7d01b50e0d17dc79C8'
    }
    
    // 验证以太坊地址格式
    console.log('开始验证以太坊地址格式...')
    console.log('待验证地址:', to)
    console.log('地址类型:', typeof to)
    
    try {
      const isValidAddress = ethers.isAddress(to)
      console.log('ethers.isAddress 验证结果:', isValidAddress)
      
      if (!isValidAddress) {
        ElMessage.warning('商户地址格式不正确，使用测试地址进行转账')
        console.log('商户地址格式验证失败，使用测试地址')
        to = '0x70997970C51812dc3A010C7d01b50e0d17dc79C8'
      } else {
        console.log('商户地址格式验证通过')
      }
    } catch (error) {
      console.log('ethers.isAddress 验证出错:', error)
      ElMessage.warning('商户地址验证出错，使用测试地址进行转账')
      to = '0x70997970C51812dc3A010C7d01b50e0d17dc79C8'
    }
    
    console.log('最终使用的商户地址:', to)
    console.log('最终地址格式验证:', ethers.isAddress(to))
    
    // 将金额缩小100000倍用于测试网
    const originalAmount = parseFloat(orderData.value.totalAmount.toString())
    const testAmount = (originalAmount / 100000).toFixed(6) // 保留6位小数
    const amountEth = testAmount.toString()

    ElMessage.info(`正在发起转账请求，请在MetaMask中确认...（测试网金额：${amountEth} ETH）`)

    // 获取当前网络的gas价格
    let transactionGasPrice
    try {
      const gasPriceHex = await window.ethereum.request({
        method: 'eth_gasPrice',
      })
      transactionGasPrice = gasPriceHex
      
      // 在测试网环境下，使用更低的gas价格
      const currentChainId = await window.ethereum.request({ method: 'eth_chainId' })
      console.log('当前链ID:', currentChainId)
      
      // Sepolia测试网 (0xaa36a7) 和 Goerli测试网 (0x5) 使用更低的gas价格
      if (currentChainId === '0xaa36a7' || currentChainId === '0x5') {
        const networkGasPrice = BigInt(transactionGasPrice)
        // 使用网络价格的10%，但最低1 GWEI
        const lowGasPrice = networkGasPrice / 10n
        const minGasPrice = BigInt('0x3b9aca00') // 1 GWEI
        transactionGasPrice = lowGasPrice < minGasPrice ? minGasPrice.toString(16) : lowGasPrice.toString(16)
        console.log('测试网环境，使用降低后的gas价格:', ethers.formatUnits(transactionGasPrice, 'gwei'), 'GWEI')
        // 更新显示的gas价格
        gasPrice.value = ethers.formatUnits(transactionGasPrice, 'gwei')
      } else {
        console.log('当前网络gas价格:', ethers.formatUnits(transactionGasPrice, 'gwei'), 'GWEI')
        // 更新显示的gas价格
        gasPrice.value = ethers.formatUnits(transactionGasPrice, 'gwei')
      }
    } catch (error) {
      console.warn('获取网络gas价格失败，使用默认值:', error)
      transactionGasPrice = '0x3b9aca00' // 1 GWEI 默认值
      gasPrice.value = '1.0' // 显示默认值
    }

    // 构建交易参数
    const transactionParameters = {
      to: to, // 接收方地址
      from: from, // 发送方地址
      value: ethers.parseEther(amountEth).toString(16), // 金额转换为十六进制
      gasPrice: transactionGasPrice, // 使用网络推荐的gas价格
      gas: '0x5208', // 21000 gas limit
    }

    // 发起转账请求
    const txHash = await window.ethereum.request({
      method: 'eth_sendTransaction',
      params: [transactionParameters],
    })

    console.log('交易已发送，交易哈希：', txHash)
    console.log('从钱包获取的交易ID，准备获取链上交易详情...')
    
    // 保存钱包返回的交易哈希（初步）
    const walletTxHash = txHash
    console.log('钱包返回的交易ID:', walletTxHash)
    
    // 监听交易状态，从链上获取准确的交易哈希
    await waitForTransaction(walletTxHash)
    
  } catch (err) {
    console.error('转账失败：', err)
    
    // 判断是否是用户取消
    if (err.code === 4001) {
      ElMessage.warning('您取消了转账')
      paymentStatus.value = 'failed'
    } else {
      // 更详细的错误信息
      let errorMsg = '转账失败：'
      if (err.message) {
        errorMsg += err.message
      } else if (err.error && err.error.message) {
        errorMsg += err.error.message
      } else {
        errorMsg += '未知错误，请检查控制台日志'
      }
      ElMessage.error(errorMsg)
      paymentStatus.value = 'failed'
    }
  }
}

/* ===== 等待交易确认 ===== */
async function waitForTransaction(txHash) {
  try {
    ElMessage.info('交易已发送，等待链上确认...')
    
    // 创建provider来监听交易
    const provider = new ethers.BrowserProvider(window.ethereum)
    
    // 等待交易确认（1个区块确认）
    const receipt = await provider.waitForTransaction(txHash, 1)
    
    if (receipt && receipt.status === 1) {
      console.log('交易确认成功：', receipt)
      console.log('交易哈希（从收据）:', receipt.hash)
      console.log('区块号:', receipt.blockNumber)
      console.log('Gas使用量:', receipt.gasUsed.toString())
      console.log('有效Gas价格:', receipt.gasPrice ? ethers.formatUnits(receipt.gasPrice, 'gwei') + ' GWEI' : '未知')
      
      // 从交易收据中获取交易哈希（确保使用链上的准确交易ID）
      if (receipt.hash) {
        orderData.value.txHash = receipt.hash
        console.log('✅ 更新交易哈希为链上准确交易ID:', receipt.hash)
        console.log('📋 交易详情:')
        console.log('   - 区块号:', receipt.blockNumber)
        console.log('   - Gas使用:', receipt.gasUsed.toString())
        console.log('   - 状态:', receipt.status === 1 ? '成功' : '失败')
      } else {
        console.warn('⚠️ 无法从交易收据获取交易哈希，使用钱包返回的哈希')
        orderData.value.txHash = txHash
      }
      
      // 获取完整的交易详情
      try {
        const provider = new ethers.BrowserProvider(window.ethereum)
        const fullTransaction = await provider.getTransaction(receipt.hash)
        if (fullTransaction) {
          console.log('完整交易详情:')
          console.log('- 交易ID:', fullTransaction.hash)
          console.log('- 发送方:', fullTransaction.from)
          console.log('- 接收方:', fullTransaction.to)
          console.log('- 交易金额:', ethers.formatEther(fullTransaction.value), 'ETH')
          console.log('- Gas限制:', fullTransaction.gasLimit.toString())
          console.log('- Gas价格:', ethers.formatUnits(fullTransaction.gasPrice, 'gwei'), 'GWEI')
          console.log('- 数据:', fullTransaction.data)
          
          // 验证交易哈希一致性
          console.log('🔍 交易哈希验证:')
          console.log('   - 钱包返回的交易ID:', txHash)
          console.log('   - 链上交易收据哈希:', receipt.hash)
          console.log('   - 完整交易详情哈希:', fullTransaction.hash)
          console.log('   - 哈希一致性:', txHash === receipt.hash && receipt.hash === fullTransaction.hash ? '✅ 一致' : '❌ 不一致')
        }
      } catch (txError) {
        console.warn('获取完整交易详情失败:', txError)
      }
      
      // 立即更新支付状态为成功（确保界面立即响应）
      paymentStatus.value = 'success'
      console.log('✅ 支付状态已更新为: success')
      console.log('当前 paymentStatus.value:', paymentStatus.value)
      
      ElMessage.success('支付成功！交易已在链上确认')
      
      // 异步更新后端数据，不阻塞前端状态更新
      const updateBackend = async () => {
        if (orderData.value.orderId && orderData.value.txHash) {
          try {
            console.log('🔄 正在更新后端订单交易哈希...')
            await updateOrderTxHash(orderData.value.orderId, orderData.value.txHash)
            console.log('✅ 后端订单交易哈希更新成功')
            
            // 更新订单状态为已完成
            console.log('🔄 正在更新订单状态为已完成...')
            await updateOrderStatus(orderData.value.orderId, 'completed')
            console.log('✅ 订单状态更新成功')
          } catch (updateError) {
            console.warn('⚠️ 更新后端数据失败:', updateError)
            // 后端更新失败不影响前端状态
          }
        }
      }
      
      // 后台异步更新，不阻塞
      updateBackend()
      
      // 立即进入下一步（不等待后端更新）
      setTimeout(() => {
        currentStep.value = 2
        console.log('✅ 页面已切换到支付成功步骤')
        console.log('当前 paymentStatus.value:', paymentStatus.value)
      }, 1500)
      
    } else {
      console.error('交易失败：', receipt)
      ElMessage.error('交易执行失败')
      paymentStatus.value = 'failed'
    }
    
  } catch (error) {
    console.error('等待交易确认失败：', error)
    
    // 尝试手动检查交易状态
    try {
      const provider = new ethers.BrowserProvider(window.ethereum)
      const receipt = await provider.getTransactionReceipt(txHash)
      
      if (receipt && receipt.status === 1) {
        // 交易实际上已成功，更新状态
        console.log('⚠️ 等待超时但交易已成功:', receipt)
        orderData.value.txHash = receipt.hash
        paymentStatus.value = 'success'
        ElMessage.success('交易已确认成功')
        
        // 更新后端
        if (orderData.value.orderId && orderData.value.txHash) {
          try {
            await updateOrderTxHash(orderData.value.orderId, orderData.value.txHash)
            await updateOrderStatus(orderData.value.orderId, 'completed')
          } catch (e) {
            console.warn('更新后端失败:', e)
          }
        }
        
        setTimeout(() => {
          currentStep.value = 2
        }, 1500)
        return
      }
    } catch (checkError) {
      console.error('手动检查交易状态也失败:', checkError)
    }
    
    ElMessage.error('交易确认超时，请点击"刷新状态"按钮检查订单状态')
    paymentStatus.value = 'pending' // 保持待确认状态，让用户可以刷新
  }
}

async function handleCheckPayment() {
  if (!orderData.value?.orderId) {
    ElMessage.warning('订单信息不完整')
    return
  }
  
  if (refreshing.value) {
    return // 防止重复点击
  }
  
  refreshing.value = true
  
  try {
    console.log('🔄 开始检查订单状态...')
    
    // 1. 先查询后端订单状态
    const orderDetail = await getTransactionDetail(orderData.value.orderId)
    console.log('📋 查询到的订单状态:', orderDetail)
    console.log('   - 订单ID:', orderDetail.orderId)
    console.log('   - 交易哈希:', orderDetail.txHash)
    console.log('   - 订单状态:', orderDetail.status)
    
    if (orderDetail.txHash && orderDetail.txHash !== 'pending' && orderDetail.txHash !== '') {
      // 订单已有交易哈希，更新到前端
      orderData.value.txHash = orderDetail.txHash
      console.log('✅ 订单已有交易哈希:', orderDetail.txHash)
      
      // 尝试验证链上状态（如果MetaMask可用）
      if (window.ethereum) {
        try {
          const provider = new ethers.BrowserProvider(window.ethereum)
          const receipt = await provider.getTransactionReceipt(orderDetail.txHash)
          console.log('📋 链上交易收据:', receipt)
          
          if (receipt) {
            if (receipt.status === 1) {
              // 交易已在链上确认成功
              console.log('✅ 链上确认交易成功')
              paymentStatus.value = 'success'
              ElMessage.success('支付已确认！交易已在链上完成')
              
              // 确保后端状态是最新的（异步，不阻塞）
              updateOrderStatus(orderData.value.orderId, 'completed').catch(e => {
                console.warn('更新订单状态失败:', e)
              })
              
              // 自动进入下一步
              setTimeout(() => {
                currentStep.value = 2
              }, 1000)
            } else if (receipt.status === 0) {
              // 交易失败
              console.log('❌ 链上交易失败')
              ElMessage.error('交易已失败，请重新支付')
              paymentStatus.value = 'failed'
            }
          } else {
            // 收据为null，交易还在处理中
            console.log('⏳ 交易还在处理中')
            ElMessage.info('交易正在链上处理，请稍后再试')
          }
        } catch (chainError) {
          console.warn('⚠️ 查询链上状态失败:', chainError)
          // 无法查询链上状态，但订单有交易哈希，根据后端状态判断
          if (orderDetail.status === 'completed') {
            console.log('✅ 后端显示订单已完成')
            paymentStatus.value = 'success'
            ElMessage.success('订单已完成（后端确认）')
            setTimeout(() => {
              currentStep.value = 2
            }, 1000)
          } else {
            ElMessage.warning('订单有交易哈希但无法验证链上状态，交易ID: ' + orderDetail.txHash)
          }
        }
      } else {
        // 没有MetaMask，根据后端状态判断
        console.log('⚠️ MetaMask不可用，使用后端状态')
        if (orderDetail.status === 'completed') {
          paymentStatus.value = 'success'
          ElMessage.success('订单已完成')
          setTimeout(() => {
            currentStep.value = 2
          }, 1000)
        } else {
          ElMessage.info('订单有交易哈希: ' + orderDetail.txHash)
        }
      }
    } else if (orderDetail.status === 'completed') {
      // 订单状态已完成，但可能没有交易哈希
      console.log('✅ 订单状态为已完成')
      paymentStatus.value = 'success'
      ElMessage.success('订单已完成')
      setTimeout(() => {
        currentStep.value = 2
      }, 1000)
    } else {
      // 还未支付或交易未确认
      console.log('⏳ 订单尚未支付')
      ElMessage.info('订单尚未支付，请点击"确认支付"按钮完成付款')
    }
  } catch (error) {
    console.error('❌ 检查订单状态失败:', error)
    ElMessage.error('检查订单状态失败: ' + (error.message || '请稍后再试'))
  } finally {
    refreshing.value = false
  }
}

function handleCancel() {
  ElMessageBox.confirm('确定要取消订单吗？', '提示')
      .then(() => router.back())
      .catch(() => {})
}

function handleBackHome() {
  router.push('/')
}
function handleViewTransactions() {
  router.push('/transactions')
}
</script>
<style scoped>
.checkout {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.checkout-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.checkout-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.loading-container {
  padding: 40px 0;
}

.order-section,
.payment-section,
.success-section {
  padding: 30px;
}

.order-section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #303133;
  font-weight: 600;
}

.order-total {
  margin-top: 30px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f2f5 0%, #eef1f8 100%);
  border-radius: 16px;
  text-align: right;
}

.total-item {
  font-size: 18px;
  font-weight: 500;
  color: #606266;
}

.total-price {
  font-size: 36px;
  font-weight: bold;
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-left: 8px;
}

.order-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.order-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 32px;
}

.payment-info {
  margin: 30px 0;
}

.payment-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

.payment-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 16px 40px;
  font-size: 16px;
}

.success-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.success-actions :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  padding: 12px 32px;
}
</style>

