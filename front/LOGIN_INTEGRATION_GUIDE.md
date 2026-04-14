# 登录功能集成指南

## 项目概述
本项目已实现完整的Web3/区块链钱包登录系统，集成MetaMask钱包进行身份验证。

## 登录流程说明

### 1. 用户界面流程（Login.vue）
- **入口页面**: `src/views/Login.vue`
- **功能**:
  - 显示MetaMask连接提示
  - 用户连接钱包后显示成功动画
  - 2秒后自动跳转到首页或指定的重定向页面
  - 错误提示和重试机制

### 2. 钱包连接流程（WalletConnectBtn.vue）

#### 第一步：连接钱包账户
```javascript
const accounts = await window.ethereum.request({ 
  method: 'eth_requestAccounts' 
})
```
- 检测MetaMask是否已安装
- 请求用户授权连接
- 获取用户钱包地址

#### 第二步：获取账户余额
```javascript
const balance = await getBalance(address)
```
- 使用ethers.js获取ETH余额
- 格式化显示

#### 第三步：签名登录流程（signInWallet函数）- **关键步骤**

此步骤**必须成功**才能完成登录。如果任何一个子步骤失败，整个登录过程将失败。

##### 3.1 获取Nonce（随机数）
```
POST /api/auth/nonce
{
  "address": "0x..."
}
Response: "nonce_string"
```
**失败处理**: 如果获取失败，显示错误信息，登录过程终止

##### 3.2 签名消息
```javascript
const message = `请签名以验证您的身份\n\nNonce: ${nonce}`
const signature = await signer.signMessage(message)
```
**用户操作**: 用户在MetaMask中确认签名
**失败处理**: 如果用户取消签名，登录过程终止

##### 3.3 提交登录请求并验证
```
POST /api/auth/signin
{
  "address": "0x...",
  "signature": "0x...",
  "nonce": "nonce_string"
}
Response: {
  "code": 0,
  "data": {
    "token": "jwt_token",
    "user": { ... },
    "role": "user|merchant|admin"
  },
  "message": "success"
}
```
**关键**: 只有后端**返回成功**（无错误），才认为登录成功
**失败处理**: 如果后端返回错误，显示错误信息，登录过程终止

#### 第四步：登录成功后的操作
仅当上述第三步完全成功时，才执行：
- 保存Token到userStore
- 更新钱包地址和余额到userStore
- 设置 `isConnected = true`
- 显示"登录成功"消息
- 可能询问是否注册为商家

### 3. 状态管理（userStore.js）

存储的用户信息：
```javascript
{
  walletAddress: string      // 钱包地址
  token: string              // JWT token
  balance: string            // ETH余额
  rewardBalance: string      // 奖励余额
  isConnected: boolean       // 连接状态
  userRole: string           // 'user'|'merchant'|'admin'
  shopInfo: object|null      // 商家店铺信息
}
```

所有信息持久化到localStorage，页面刷新后自动恢复。

## API集成文件

### 1. authApi.js (`src/api/authApi.js`)
**已完善的功能**:
- ✅ `getNonce(address)` - 获取签名随机数
  - 处理多种响应格式
  - 返回nonce字符串
  
- ✅ `signIn(data)` - 提交签名登录
  - 接收 { address, signature, nonce }
  - 返回 { token, user, role }
  - 完善的错误处理

### 2. request.js (`src/api/request.js`)
**已有功能**:
- ✅ 自动添加Authorization header
- ✅ 响应拦截和错误处理
- ✅ Token验证失败时自动登出
- ✅ 开发模式错误提示

### 3. chainHelper.js (`src/utils/chainHelper.js`)
**已有功能**:
- ✅ MetaMask检测
- ✅ Provider初始化
- ✅ 网络检测和切换
- ✅ 余额查询
- ✅ 地址格式化

## 后端接口要求

### 获取Nonce接口
```
POST /api/auth/nonce
请求体: { "address": "0x..." }
响应: {
  "code": 0,
  "data": "random_nonce_string",
  "message": "success"
}
```

**说明**: 
- Nonce应该是一个随机字符串
- 需要服务端记录该nonce及其过期时间
- 建议5-10分钟过期
- **重要**: 前端依赖此接口，失败会导致登录中止

### 签名登录接口 - **必须成功才算登录完成**
```
POST /api/auth/signin
请求体: {
  "address": "0x...",
  "signature": "0x...",
  "nonce": "nonce_string"
}
响应: {
  "code": 0,
  "data": {
    "token": "jwt_token_here",
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

**说明**:
- 服务端需要验证签名的合法性
- 验证nonce是否存在且未过期
- 验证nonce是否已使用过（一次性使用）
- 返回的token在后续请求中通过Authorization header传递
- **重要**: 任何验证失败都会导致登录失败，前端会显示错误信息
- **重要**: 只有此接口返回成功，前端才会认为登录成功并跳转

## 完整登录流程图

```
用户访问登录页
    ↓
调用 WalletConnectBtn 组件的 handleConnect()
    ↓
检测 MetaMask 是否安装
    ↓
请求用户授权连接账户
    ↓
获取钱包地址并获取余额
    ↓
执行 signInWallet(address) - 【关键步骤】
    ├─ 调用 getNonce(address) → 获取随机数
    │   └─ 失败 → ❌ 显示错误，登录终止
    ├─ 签名消息: "请签名以验证您的身份\n\nNonce: ${nonce}"
    │   └─ 用户取消 → ❌ 显示错误，登录终止
    ├─ 调用 signIn({ address, signature, nonce }) → 请求后端验证
    │   └─ 后端返回错误 → ❌ 显示错误信息，登录终止
    │   └─ 后端返回成功 → ✅ 保存 token 到 userStore
    └─ 返回 ✅ 
    ↓ (只有上述全部成功才会到这里)
更新状态：
  - setWalletAddress(address)
  - setBalance(balance)
  - setConnected(true)
    ↓
显示"登录成功"消息
    ↓
Login.vue 监听到 isConnected 变化
    ↓
显示成功动画（2秒）
    ↓
自动重定向到首页或 query.redirect 页面
    ↓ (可选)
弹窗询问是否注册为商家
```

## 环境配置

### .env 文件需要的配置
```
VITE_API_BASE_URL=/api           # 后端API基础URL
VITE_CHAIN_ID=11155111            # Sepolia测试网链ID
VITE_CHAIN_NAME=Sepolia           # 链名称
```

## 常见问题排查

### 1. "未检测到Web3钱包"
- **原因**: 未安装MetaMask插件
- **解决**: 下载MetaMask浏览器插件

### 2. "MetaMask连接超时"
- **原因**: 钱包未解锁或网络问题
- **解决**: 检查钱包是否已解锁，重新尝试连接

### 3. 获取nonce失败
- **原因**: 后端服务未启动或 /api/auth/nonce 接口未实现
- **解决**: 启动后端服务，确认接口实现

### 4. 签名请求后没有反应
- **原因**: MetaMask钱包窗口在后台或浏览器阻止了弹窗
- **解决**: 
  - 确保MetaMask钱包已解锁
  - 检查浏览器是否阻止了弹窗
  - 查看浏览器控制台是否有错误日志

### 5. "登录失败：签名验证失败"
- **原因**: 后端验证签名时出错，可能是：
  - 签名消息格式与后端期望不符
  - Nonce已过期
  - Nonce已被使用过
  - 签名内容被篡改
- **解决**: 检查后端签名验证逻辑，确保：
  - 签名消息格式完全一致：`请签名以验证您的身份\n\nNonce: {nonce}`
  - Nonce在有效期内
  - Nonce只能使用一次
  - 签名验证算法正确

### 6. 显示"登录失败"但不知道具体原因
- **原因**: 后端返回了错误但错误信息未正确传递
- **解决**: 
  - 打开浏览器开发者工具 (F12)
  - 切换到 Network 标签
  - 重新尝试登录
  - 查看 `/api/auth/nonce` 和 `/api/auth/signin` 的响应内容
  - 查看浏览器控制台的错误日志

### 7. 钱包已连接，但页面未跳转
- **原因**: userStore 状态未正确更新或路由配置问题
- **解决**: 
  - 检查浏览器控制台是否有JavaScript错误
  - 确认 `userStore.isConnected` 为 true
  - 检查路由配置是否正确
  - 尝试手动调用 `router.push('/')`

## 开发建议

### 1. 本地调试
```javascript
// 在浏览器控制台查看登录过程
// 1. 查看钱包地址
console.log(userStore.walletAddress)

// 2. 查看token
console.log(userStore.token)

// 3. 查看连接状态
console.log(userStore.isConnected)

// 4. 查看localStorage
localStorage.getItem('token')
localStorage.getItem('walletAddress')
```

### 2. 使用Network标签调试
监控这两个关键接口：
- `POST /api/auth/nonce` - 查看是否返回nonce
- `POST /api/auth/signin` - 查看响应状态和错误信息

### 3. 前端测试Mock
如果后端未准备好，可以在 authApi.js 中添加Mock返回：
```javascript
export async function getNonce(address) {
  // Mock response
  return 'mock_nonce_' + Date.now()
}

export async function signIn(data) {
  // Mock response
  return {
    token: 'mock_jwt_token_' + Date.now(),
    user: { address: data.address },
    role: 'user'
  }
}
```

### 3. 签名信息格式
确保签名消息格式与后端验证逻辑一致，当前格式为：
```
请签名以验证您的身份

Nonce: {nonce}
```

如需修改，同时更新 `src/components/WalletConnectBtn.vue` 中 signInWallet 函数的 message 变量。

## 主要改进点总结

1. **完善API处理**: authApi.js 现在能正确处理多种响应格式
2. **改进错误信息**: WalletConnectBtn 现在提供更详细的错误日志和用户提示
3. **增强登录UI**: Login.vue 现在包含成功动画和错误提示
4. **完整流程验证**: 整个登录流程已完整集成并可正常运行

## 文件清单

已修改的文件：
- ✅ `src/api/authApi.js` - API调用完善
- ✅ `src/components/WalletConnectBtn.vue` - 签名登录逻辑改进
- ✅ `src/views/Login.vue` - UI和流程增强

可供参考的文件：
- 📄 `src/stores/userStore.js` - 状态管理（无需修改）
- 📄 `src/api/request.js` - HTTP请求配置（无需修改）
- 📄 `src/utils/chainHelper.js` - 链操作工具（无需修改）
- 📄 `src/main.js` - 应用入口
- 📄 `src/router/index.js` - 路由配置

## 后端开发指南

详见 Java 后端代码：
- `AuthController.java` - 认证控制器
- `IUsersService.java` - 用户服务接口
- `UsersServiceImpl.java` - 用户服务实现
- `LoginDTO.java` - 登录数据传输对象
