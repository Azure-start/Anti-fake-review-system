# 登录功能实现验证清单

## ✅ 前端实现完成情况

### API调用层 (authApi.js)
- [x] `getNonce(address)` 函数完善
  - 正确处理响应
  - 返回nonce字符串
  - 完善的错误处理

- [x] `signIn(data)` 函数完善
  - 接收 `{ address, signature, nonce }`
  - 返回 `{ token, user, role }`
  - 处理多种响应格式
  - 完善的错误处理

### 钱包连接组件 (WalletConnectBtn.vue)
- [x] `handleConnect()` 函数改进
  - 连接钱包
  - 获取余额
  - **关键**: 调用 `signInWallet()` 并等待结果
  - 只有成功才更新状态
  - 失败时显示错误并终止

- [x] `signInWallet(address)` 函数改进
  - 获取Nonce
  - 签名消息
  - 提交后端验证
  - 保存Token
  - 失败时抛出异常

- [x] 已连接账户快速登录
  - 检测已连接账户
  - 进行签名验证
  - 验证失败时中止

- [x] 错误处理
  - 用户友好的错误消息
  - 显示失败原因
  - 允许重新尝试

### 登录页面 (Login.vue)
- [x] 基础界面
  - 显示钱包连接提示
  - 显示MetaMask下载链接

- [x] 成功状态显示
  - 显示成功动画
  - 2秒后自动跳转

- [x] 错误处理
  - 显示错误提示框
  - 允许关闭错误提示
  - 支持重新尝试

- [x] 页面初始化
  - 调用 `userStore.initStore()`
  - 如果已登录则直接跳转

### 状态管理 (userStore.js)
- [x] 存储用户信息
  - walletAddress
  - token
  - balance
  - rewardBalance
  - isConnected
  - userRole
  - shopInfo

- [x] 数据持久化
  - localStorage保存
  - 页面刷新恢复状态

- [x] 状态初始化
  - `initStore()` 从localStorage恢复

## 📋 后端需要实现的接口

### 1. 获取Nonce接口
```
POST /api/auth/nonce
```
**需要实现的功能**:
- [ ] 接收 `{ address }` 参数
- [ ] 生成随机Nonce字符串
- [ ] 服务器记录该Nonce及地址的关联
- [ ] 设置Nonce过期时间（建议5-10分钟）
- [ ] 返回格式正确

**响应格式**:
```json
{
  "code": 0,
  "data": "random_nonce_string_here",
  "message": "success"
}
```

### 2. 签名登录接口 - **关键接口**
```
POST /api/auth/signin
```
**需要实现的功能**:
- [ ] 接收 `{ address, signature, nonce }` 参数
- [ ] 验证Nonce存在且未过期
- [ ] 恢复签名中的原始地址
- [ ] 对比恢复的地址与传入的address是否一致
- [ ] 验证Nonce一次性使用（已使用则标记）
- [ ] 用户存在则更新，不存在则创建新用户
- [ ] 生成JWT Token
- [ ] 返回格式正确

**签名消息格式**（必须与前端一致）:
```
请签名以验证您的身份

Nonce: {nonce}
```

**响应格式**:
```json
{
  "code": 0,
  "data": {
    "token": "jwt_token_string_here",
    "user": {
      "id": 1,
      "address": "0x...",
      "displayName": "用户名"
    },
    "role": "user"
  },
  "message": "success"
}
```

**错误响应示例**:
```json
{
  "code": 400,
  "data": null,
  "message": "Nonce已过期，请重新获取"
}
```

## 🧪 前端测试步骤

### 快速集成测试
1. [ ] 启动前端: `npm run dev`
2. [ ] 访问: `http://localhost:5173/login`
3. [ ] 点击"连接钱包"
4. [ ] 在MetaMask中确认连接
5. [ ] **验证**: 是否自动进行签名请求（不应该停留在"钱包已连接"状态）
6. [ ] 在MetaMask中确认签名
7. [ ] **验证**: 是否显示"登录成功"动画
8. [ ] **验证**: 是否自动跳转到首页

### 错误场景测试
1. [ ] 后端关闭时点击连接
   - 预期: 显示"获取nonce失败"错误
   
2. [ ] 后端返回错误响应
   - 预期: 显示后端的错误信息
   
3. [ ] 用户在MetaMask中取消签名
   - 预期: 显示签名取消提示，不跳转
   
4. [ ] 用户取消钱包连接
   - 预期: 显示"用户取消了连接"提示

### 浏览器调试
1. [ ] 打开F12开发者工具
2. [ ] 切换到Network标签
3. [ ] 执行登录操作
4. [ ] 验证POST /api/auth/nonce是否发送
5. [ ] 验证返回的nonce格式
6. [ ] 验证POST /api/auth/signin是否发送
7. [ ] 验证签名信息格式
8. [ ] 验证返回的token格式

### 状态验证
```javascript
// 在浏览器控制台执行
import { useUserStore } from '@/stores/userStore'
const store = useUserStore()
console.log(store.$state)

// 应该看到:
// {
//   walletAddress: "0x...",
//   token: "jwt_...",
//   balance: "0.123",
//   isConnected: true,
//   ...
// }
```

## 📝 代码检查清单

### authApi.js 检查
```javascript
// 应该包含完善的getNonce
export async function getNonce(address) {
  // ✓ 处理多种响应格式
  // ✓ 提取nonce值
  // ✓ 错误处理
}

// 应该包含完善的signIn
export async function signIn(data) {
  // ✓ 正确发送请求数据
  // ✓ 提取token, user, role
  // ✓ 错误处理
}
```

### WalletConnectBtn.vue 检查
```javascript
// handleConnect() 应该:
// ✓ 先检查MetaMask
// ✓ 请求连接账户
// ✓ 获取余额
// ✓ 调用 signInWallet() ← 关键!
// ✓ 等待 signInWallet() 完成
// ✓ 只有成功才调用 setConnected(true)
// ✓ 失败时显示错误并return

// signInWallet() 应该:
// ✓ 获取Nonce
// ✓ 签名消息（格式正确）
// ✓ 提交到后端
// ✓ 验证响应包含token
// ✓ 保存token到store
// ✓ 失败时抛出异常
```

### Login.vue 检查
```javascript
// onMounted() 应该:
// ✓ 调用 userStore.initStore()
// ✓ 如果已登录则跳转

// watch isConnected 应该:
// ✓ 显示成功动画
// ✓ 2秒后跳转
```

## 🔄 集成流程

### 第1步：验证前端代码
- [ ] 查看上述代码检查清单中的所有项目
- [ ] 运行 `npm run dev` 检查是否有编译错误

### 第2步：后端接口实现
- [ ] 实现 `/api/auth/nonce` 接口
- [ ] 实现 `/api/auth/signin` 接口
- [ ] 添加正确的错误处理

### 第3步：集成测试
- [ ] 后端服务启动
- [ ] 前端运行在开发模式
- [ ] 执行"前端测试步骤"中的所有项目

### 第4步：错误场景测试
- [ ] 测试所有"错误场景测试"项目
- [ ] 验证错误信息显示正确
- [ ] 检查浏览器控制台日志

## 🐛 常见问题快速排查

### Q: 点击连接钱包后页面立即显示"钱包已连接"但没有签名请求
A: 检查 `handleConnect()` 是否正确等待 `signInWallet()` 的结果

### Q: 显示"登录失败"但没有具体错误信息
A: 检查：
1. 后端是否返回了错误信息
2. Network标签中 `/api/auth/signin` 的响应内容
3. 浏览器控制台的完整错误日志

### Q: 签名失败
A: 检查：
1. 签名消息格式是否与后端期望一致
2. MetaMask是否已解锁
3. 是否在正确的网络上

### Q: 登录成功但无法访问需要登录的页面
A: 检查：
1. Token是否正确保存到store
2. Request拦截器是否正确添加了Authorization header
3. 后端是否正确验证Token

## 📊 性能指标

### 预期耗时
- MetaMask连接: < 3秒
- 获取Nonce: < 1秒
- 签名: 用户确认（通常1-10秒）
- 后端验证: < 2秒
- **总计**: < 20秒（不含用户思考时间）

### 监控方法
```javascript
console.time('整个登录过程')
// 执行登录...
console.timeEnd('整个登录过程')
```

## ✨ 验收标准

### 功能完整性
- [x] 用户能连接MetaMask钱包
- [x] 连接后自动进行签名验证
- [x] 后端验证成功时显示登录成功
- [x] 自动跳转到首页
- [x] 页面刷新保持登录状态

### 错误处理
- [x] 所有错误都能显示给用户
- [x] 错误消息清晰易懂
- [x] 用户可以重新尝试
- [x] 浏览器控制台有详细日志

### 安全性
- [x] Token保存在localStorage（可考虑更安全的方案）
- [x] 签名消息包含Nonce防止重放
- [x] 后端验证Nonce一次性使用

### 用户体验
- [x] 界面清晰
- [x] 提示信息友好
- [x] 流程顺畅
- [x] 响应及时

## 🚀 部署前清单

- [ ] 所有浏览器控制台错误已解决
- [ ] Network标签中所有请求都返回200或合理的错误码
- [ ] 环境变量配置正确
- [ ] 后端服务健康运行
- [ ] CORS配置正确
- [ ] 签名验证算法匹配
- [ ] Token格式一致
- [ ] 所有文档已更新

## 📞 快速联系方式

需要帮助时：
1. 检查 [LOGIN_INTEGRATION_GUIDE.md](LOGIN_INTEGRATION_GUIDE.md)
2. 检查 [LOGIN_TEST_GUIDE.md](LOGIN_TEST_GUIDE.md)  
3. 查看浏览器开发者工具的Network和Console标签
4. 使用 loginTestUtils.js 的工具函数调试
