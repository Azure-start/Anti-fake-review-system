# 登录功能 - 快速参考卡

## 🎯 核心流程 (必读!)

```
点击连接钱包
  ↓
MetaMask授权连接
  ↓
自动进行签名验证 ⭐⭐⭐
  ├─ 获取Nonce
  ├─ 用户签名
  └─ 发送到后端验证
  ↓
后端验证成功 ✅
  ↓
显示"登录成功"并跳转首页

❌ 任何一步失败 → 显示错误 → 可重试
```

## 📁 关键文件位置

### 核心代码
- **登录API**: `src/api/authApi.js`
- **钱包组件**: `src/components/WalletConnectBtn.vue`
- **登录页面**: `src/views/Login.vue`
- **状态管理**: `src/stores/userStore.js`

### 测试工具
- **调试工具**: `src/utils/loginTestUtils.js`

### 文档
| 文档 | 用途 | 读者 |
|------|------|------|
| LOGIN_INTEGRATION_GUIDE.md | 完整流程说明 | 全员 |
| LOGIN_TEST_GUIDE.md | 测试步骤 | 测试/开发 |
| LOGIN_CHECKLIST.md | 验收清单 | PM/QA |
| LOGIN_IMPLEMENTATION_SUMMARY.md | 实现总结 | 开发/交接 |
| LOGIN_COMPLETION_REPORT.md | 完成报告 | 全员 |

## 🔧 后端接口速查

### 接口1: 获取Nonce
```
POST /api/auth/nonce
请求: { "address": "0x..." }
响应: {
  "code": 0,
  "data": "nonce_string",
  "message": "success"
}
```

### 接口2: 签名登录 ⭐ 最关键
```
POST /api/auth/signin
请求: {
  "address": "0x...",
  "signature": "0x...",
  "nonce": "nonce_string"
}
响应: {
  "code": 0,
  "data": {
    "token": "jwt_token",
    "user": { id, address, displayName },
    "role": "user"
  },
  "message": "success"
}
```

**签名消息格式**:
```
请签名以验证您的身份

Nonce: {nonce}
```

## 🧪 快速测试

### 在浏览器控制台
```javascript
// 导入测试工具
import LoginTestUtils from '/src/utils/loginTestUtils.js'

// 查看完整信息
LoginTestUtils.fullDebugInfo()

// 测试Nonce接口
const nonce = await LoginTestUtils.testGetNonce('0x...')

// 测试完整登录（需要MetaMask确认）
const result = await LoginTestUtils.testSignIn('0x...')
```

### 网络监控
打开F12 → Network标签 → 执行登录
- 查看 POST /api/auth/nonce 的返回值
- 查看 POST /api/auth/signin 的请求和响应

## ❌ 常见问题快速解决

| 问题 | 可能原因 | 解决方案 |
|------|---------|--------|
| 连接后立即显示成功 | 跳过了签名验证 | 检查handleConnect()中是否调用signInWallet() |
| 签名请求没出现 | MetaMask设置或后端问题 | 检查Nonce是否获取成功 |
| 显示"登录失败"无详情 | 错误信息未传递 | 查看Network标签中的响应 |
| 登录成功但无法跳转 | token未保存或路由问题 | 检查store.isConnected是否为true |
| MetaMask弹窗不出现 | 浏览器阻止或未解锁 | 检查浏览器弹窗设置和MetaMask状态 |

## 📊 状态检查

```javascript
// 在浏览器控制台查看完整状态
import { useUserStore } from '@/stores/userStore'
const store = useUserStore()
console.log(store.$state)

// 关键字段:
// walletAddress  - 钱包地址
// token         - JWT token (应该有值)
// balance       - ETH余额
// isConnected   - 登录状态 (应该为true)
// userRole      - 用户角色 (user/merchant/admin)
```

## 🔐 安全检查清单

- [ ] 签名消息包含Nonce (防止重放)
- [ ] 后端验证Nonce一次性使用
- [ ] Nonce有过期时间设置 (5-10分钟)
- [ ] Token使用JWT格式
- [ ] 请求包含Authorization header
- [ ] 签名算法正确

## 📈 性能指标

| 操作 | 预期耗时 |
|------|--------|
| MetaMask连接 | < 3秒 |
| 获取Nonce | < 1秒 |
| 用户签名 | 1-10秒 |
| 后端验证 | < 2秒 |
| 页面跳转 | < 1秒 |
| **总计** | < 20秒 |

## 🚀 部署前检查

```
前端代码检查:
  [ ] 无console错误
  [ ] 无Network 4xx/5xx错误
  [ ] 登录流程完整

后端接口检查:
  [ ] /api/auth/nonce 正常
  [ ] /api/auth/signin 正常
  [ ] 错误信息清晰

集成测试:
  [ ] 完整登录流程可用
  [ ] 错误处理正确
  [ ] 页面跳转正常

安全检查:
  [ ] 签名验证无误
  [ ] Token格式正确
  [ ] CORS配置正确
```

## 💡 开发提示

### 调试签名问题
```javascript
// 在signInWallet中添加日志
console.log('签名消息:', message)
console.log('签名结果:', signature)
console.log('发送数据:', { address, signature, nonce })
```

### 调试Nonce问题
```javascript
// 在handleConnect中添加日志
const nonceResponse = await getNonce(address)
console.log('Nonce响应:', nonceResponse)
console.log('提取的Nonce:', nonce)
```

### 模拟登录（后端未就绪）
```javascript
// 在浏览器控制台执行
LoginTestUtils.mockLogin(
  '0x1234567890123456789012345678901234567890',
  'test_token_' + Date.now(),
  'user'
)
// 这样可以测试前端流程，无需后端
```

## 📞 需要帮助?

1. **查看完整文档**
   - LOGIN_INTEGRATION_GUIDE.md (推荐首先查看)

2. **查看测试指南**
   - LOGIN_TEST_GUIDE.md

3. **查看验收清单**
   - LOGIN_CHECKLIST.md

4. **在控制台运行工具**
   ```javascript
   LoginTestUtils.fullDebugInfo()
   ```

5. **查看浏览器工具**
   - F12 → Network标签 → 监控API调用
   - F12 → Console标签 → 查看错误日志

## ⭐ 最重要的三点

1. **流程必须完整** 
   - 不能跳过签名验证步骤
   - 后端必须验证成功

2. **签名消息必须一致**
   - 前端: `请签名以验证您的身份\n\nNonce: {nonce}`
   - 后端: 验证时必须使用相同格式

3. **错误必须显示**
   - 所有失败都要显示错误信息
   - 允许用户重新尝试

---

**快速开始**: 
1. 启动前端: `npm run dev`
2. 访问: `http://localhost:5173/login`
3. 点击"连接钱包"
4. 在MetaMask中确认
5. **验证**: 是否要求签名? (必须要求)
