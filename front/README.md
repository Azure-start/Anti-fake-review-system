# 区块链电商防刷评系统 - 前端项目

基于Vue 3 + Element Plus + Ethers.js构建的区块链电商防刷评系统前端应用。

## 项目简介

本项目是一个基于区块链技术的电商防刷评系统前端，实现了以下核心功能：

- 🔐 MetaMask钱包连接与签名登录
- 🛍️ 商品浏览、详情查看与购买
- 💳 区块链支付与交易记录查询
- ⭐ 评价发布与链上存证（NFT生成）
- 🎁 奖励机制与优惠券兑换
- 📊 个人账户与统计信息

## 技术栈

- **前端框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **区块链交互**: Ethers.js 6.x
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **区块链网络**: Sepolia测试网络

## 项目结构

```
src/
├── api/                    # API接口封装
│   ├── authApi.js         # 认证接口
│   ├── productApi.js      # 商品接口
│   ├── transactionApi.js  # 交易接口
│   ├── rewardApi.js       # 奖励接口
│   └── request.js         # Axios配置
├── assets/                # 静态资源
├── components/            # 公共组件
│   ├── WalletConnectBtn.vue   # 钱包连接按钮
│   ├── NavBar.vue             # 导航栏
│   ├── ProductCard.vue        # 商品卡片
│   └── NftTip.vue             # NFT提示
├── router/                # 路由配置
│   └── index.js
├── stores/                # Pinia状态管理
│   ├── userStore.js       # 用户状态
│   ├── productStore.js    # 商品状态
│   └── chainStore.js      # 区块链状态
├── utils/                 # 工具函数
│   └── chainHelper.js     # 区块链工具
├── views/                 # 页面组件
│   ├── Home.vue           # 首页
│   ├── Login.vue          # 登录页
│   ├── ProductDetail.vue  # 商品详情
│   ├── Checkout.vue       # 确认订单
│   ├── Transactions.vue   # 交易记录
│   ├── Review.vue         # 发布评价
│   ├── Rewards.vue        # 我的奖励
│   └── Account.vue        # 我的账户
├── App.vue                # 根组件
└── main.js                # 入口文件
```

## 快速开始

### 环境要求

- Node.js >= 20.19.0 或 >= 22.12.0
- npm 或 yarn
- MetaMask浏览器插件

### 安装依赖

```bash
npm install
```

### 环境配置

创建 `.env` 文件（或使用已提供的示例）：

```env
# 后端API地址
VITE_API_BASE_URL=http://localhost:3000/api

# 区块链网络配置
VITE_CHAIN_ID=11155111
VITE_CHAIN_NAME=Sepolia
```

### 开发运行

```bash
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

## 功能说明

### 1. 钱包连接

- 自动检测MetaMask安装状态
- 连接Sepolia测试网络
- 签名验证登录
- 获取账户余额

### 2. 商品浏览

- 商品列表分页展示
- 商品搜索功能
- 商品详情查看（图片、价格、评分、评价）
- 规格选择

### 3. 交易流程

- 确认订单信息
- 填写收货地址
- MetaMask支付确认
- 交易状态实时更新
- 交易记录查询

### 4. 评价功能

- 评分与文字评价
- 图片上传（最多3张）
- 内容长度校验
- 链上存证生成NFT
- NFT ID展示

### 5. 奖励系统

- 奖励余额查询
- 奖励记录查看
- 优惠券兑换
- 个人统计

## 浏览器兼容

推荐使用以下浏览器并安装MetaMask插件：

- ✅ Chrome / Edge（Chromium）
- ✅ Firefox
- ✅ Brave
- ✅ Opera

## 开发规范

### 代码风格

- 使用 ESLint 进行代码检查
- 使用 Prettier 进行代码格式化
- 遵循 Vue 3 官方风格指南

### Git提交规范

- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建/工具相关

## 注意事项

1. **测试网络**: 本项目使用Sepolia测试网络，需要测试币（Sepolia ETH）
2. **后端接口**: 确保后端API服务正常运行
3. **跨域问题**: 开发环境使用Vite代理，生产环境需要配置CORS
4. **MetaMask**: 首次使用需要连接钱包并切换网络

## 后续优化

- [ ] 添加国际化支持（i18n）
- [ ] 实现响应式布局优化
- [ ] 添加单元测试
- [ ] 性能优化（代码分割、懒加载）
- [ ] 错误监控与日志收集
- [ ] PWA支持

## 相关文档

- [Vue 3 官方文档](https://cn.vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/zh-CN/)
- [Ethers.js 文档](https://docs.ethers.io/)
- [MetaMask 开发者文档](https://docs.metamask.io/)

## 许可证

MIT License

## 贡献指南

欢迎提交Issue和Pull Request！
