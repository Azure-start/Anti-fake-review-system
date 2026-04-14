# 登录功能测试指南

## 快速开始

### 1. 启动前端应用
```bash
cd front
npm install
npm run dev
```

### 2. 访问登录页面
- 打开浏览器访问 `http://localhost:5173/login`（或相应的开发服务器地址）
- 确保已安装MetaMask浏览器插件
- 连接到Sepolia测试网络

## 测试场景

### 场景1: 完整登录流程测试（后端已准备好）

#### 前置条件
- ✅ 后端服务已启动（http://localhost:8080 或相应地址）
- ✅ `/api/auth/nonce` 接口可用
- ✅ `/api/auth/signin` 接口可用
- ✅ MetaMask已安装并解锁
- ✅ Sepolia测试网络已连接

#### 测试步骤

1. **打开登录页面**
   - 输入 `http://localhost:5173/login`
   - 看到 "请连接钱包" 提示

2. **点击"连接钱包"按钮**
   - MetaMask弹窗应该出现
   - 选择要连接的账户
   - 点击"连接"确认
   - **预期结果**: 钱包地址显示在按钮上

3. **签名验证流程**
   - MetaMask弹窗再次出现（签名请求）
   - 看到签名消息内容
   - 点击"签名"确认
   - **预期结果**: 显示"登录成功"并自动跳转到首页

4. **验证登录状态**
   - 检查首页右上角显示的钱包地址
   - 点击钱包地址，应该看到余额和账户菜单
   - **预期结果**: 正确显示钱包地址和余额

### 场景2: 后端未准备好的测试

#### 使用Mock登录
如果后端未准备好，可以使用浏览器控制台模拟登录：

```javascript
// 在浏览器控制台中执行以下代码
import LoginTestUtils from '/src/utils/loginTestUtils.js'

// 模拟登录到应用
LoginTestUtils.mockLogin(
  '0x1234567890123456789012345678901234567890',
  'mock_jwt_token_' + Date.now(),
  'user'
)

// 查看登录状态
LoginTestUtils.viewUserState()
```

#### 预期结果
- 页面应该导航到首页
- 钱包地址应该显示在导航栏
- 登录信息应该保存到localStorage

### 场景3: 后端接口单独测试

#### 测试Nonce接口
```javascript
// 在浏览器控制台中执行
import LoginTestUtils from '/src/utils/loginTestUtils.js'

// 获取当前连接的账户
const accounts = await LoginTestUtils.getCurrentAccounts()
const address = accounts[0]

// 测试获取nonce
const nonce = await LoginTestUtils.testGetNonce(address)
console.log('Nonce:', nonce)
```

**预期结果**: 
- 返回一个随机字符串
- 无错误信息

#### 测试完整登录流程
```javascript
import LoginTestUtils from '/src/utils/loginTestUtils.js'

// 获取当前账户
const accounts = await LoginTestUtils.getCurrentAccounts()
const address = accounts[0]

// 测试完整登录（会弹出MetaMask签名请求）
const result = await LoginTestUtils.testSignIn(address)
console.log('登录结果:', result)
```

**预期结果**:
- 返回 `{ token: "...", user: {...}, role: "user" }`
- Token应该被自动保存到userStore

### 场景4: 错误场景测试

#### 4.1 MetaMask未安装
```javascript
// 预期行为：页面显示"未检测到Web3钱包"
// 提供"下载MetaMask"链接
```

#### 4.2 用户取消连接
- 点击"连接钱包"
- 在MetaMask弹窗中点击"取消"
- **预期**: 显示"用户取消了连接"

#### 4.3 用户取消签名
- 连接钱包成功
- 在签名请求弹窗中点击"取消"
- **预期**: 显示"签名失败"警告，但钱包连接状态保持

#### 4.4 Nonce获取失败
- 后端关闭或 `/api/auth/nonce` 返回错误
- 点击"连接钱包"
- **预期**: 显示"获取nonce失败"错误信息

#### 4.5 签名验证失败
- 网络请求到 `/api/auth/signin` 但服务端验证失败
- **预期**: 显示后端返回的错误信息

## 浏览器控制台调试工具

已提供 `LoginTestUtils` 对象，包含以下方法：

### 基本检查
```javascript
// 检查MetaMask
LoginTestUtils.checkMetaMask()

// 获取当前账户
await LoginTestUtils.getCurrentAccounts()

// 查看用户状态
LoginTestUtils.viewUserState()

// 查看localStorage数据
LoginTestUtils.viewPersistentData()
```

### 测试接口
```javascript
// 测试获取nonce
await LoginTestUtils.testGetNonce('0x...')

// 测试完整登录流程（需要用户在MetaMask中确认）
await LoginTestUtils.testSignIn('0x...')

// 获取账户余额
await LoginTestUtils.getAccountBalance('0x...')
```

### 状态管理
```javascript
// 模拟登录（不需要MetaMask）
LoginTestUtils.mockLogin('0x...', 'token', 'user')

// 保存token
LoginTestUtils.saveToken('token_string', 'user')

// 清除登录信息
LoginTestUtils.clearAll()
```

### 完整调试
```javascript
// 显示所有调试信息
LoginTestUtils.fullDebugInfo()
```

## 常见问题排查

### Q: 点击"连接钱包"没有反应
**A**: 
- 检查MetaMask是否已安装和解锁
- 检查浏览器控制台是否有错误
- 尝试刷新页面重新连接

### Q: 签名请求没有弹出
**A**: 
- 检查MetaMask钱包窗口是否在后台
- 检查浏览器弹窗是否被阻止
- 检查MetaMask是否已解锁

### Q: 登录成功但没有跳转
**A**: 
- 检查浏览器控制台是否有JavaScript错误
- 检查 `userStore.isConnected` 是否为 true
- 检查路由配置是否正确

### Q: 页面刷新后登录信息消失
**A**: 
- 这是正常现象，刷新时需要重新初始化状态
- 可以检查localStorage中的数据是否存在
- 调用 `userStore.initStore()` 恢复状态

### Q: Token保存但无法使用
**A**: 
- 检查 `Authorization` header是否被正确添加
- 检查后端Token验证逻辑
- 检查Token格式（应该是 `Bearer {token}`）

## 网络监控

### 使用浏览器开发者工具
1. 打开 F12 开发者工具
2. 切换到 "Network" 标签
3. 刷新页面并执行登录操作
4. 查看请求和响应

### 关键请求URL
- `POST /api/auth/nonce` - 获取随机数
- `POST /api/auth/signin` - 提交签名登录

### 预期响应格式
```json
{
  "code": 0,
  "data": "nonce值或登录结果",
  "message": "success"
}
```

## 性能测试

### 登录耗时统计
```javascript
// 在浏览器控制台中测试
console.time('登录')
// 执行登录操作
console.timeEnd('登录')
```

### 预期耗时
- MetaMask弹窗: < 1秒
- 获取nonce: < 1秒
- 签名: 用户确认
- 登录请求: < 2秒
- 总计: < 5秒（不含用户操作时间）

## 持续集成测试建议

### 后端API Mock
对于CI/CD流程，建议：
1. 使用Mock Service Worker (MSW)
2. 为登录API创建Mock响应
3. 测试各种成功和失败场景

### E2E测试
考虑使用 Cypress 或 Playwright 进行E2E测试：
```javascript
// Cypress 示例
describe('登录流程', () => {
  it('应该成功连接钱包并登录', () => {
    cy.visit('/login')
    cy.contains('连接钱包').click()
    // 模拟MetaMask交互
    // 验证重定向
  })
})
```

## 反馈和改进

如果在测试过程中发现问题，请：
1. 查看浏览器控制台错误信息
2. 检查Network标签中的请求/响应
3. 运行 `LoginTestUtils.fullDebugInfo()` 收集调试信息
4. 记录复现步骤和错误信息，反馈给开发团队
