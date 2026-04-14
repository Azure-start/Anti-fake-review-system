// Mock测试数据

// 角色类型：user, merchant, admin

// 商家店铺信息
export const mockShopInfo = {
  id: 1,
  name: '我的店铺',
  description: '专注于高品质商品的电商店铺',
  logo: 'https://via.placeholder.com/100x100?text=Shop',
  address: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb',
  status: 'approved', // pending, approved, rejected
  createdAt: '2024-01-01T00:00:00Z'
}

// 商家申请列表（管理员审核用）
export const mockShopApplications = [
  {
    id: 1,
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb',
    shopName: '新商家店铺',
    shopDescription: '专注于高品质商品的电商店铺',
    status: 'pending', // pending, approved, rejected
    createdAt: '2024-01-15T00:00:00Z'
  },
  {
    id: 2,
    merchantAddress: '0x842d35Cc6634C0532925a3b844Bc9e7595f0bEc',
    shopName: '测试店铺',
    shopDescription: '这是一个测试店铺',
    status: 'pending',
    createdAt: '2024-01-16T00:00:00Z'
  }
]

// 用户列表（管理员用）
export const mockUsers = [
  {
    id: 1,
    address: '0x1234567890123456789012345678901234567890',
    role: 'user',
    balance: '1.5',
    createdAt: '2024-01-01T00:00:00Z'
  },
  {
    id: 2,
    address: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb',
    role: 'merchant',
    balance: '5.2',
    shopName: '我的店铺',
    createdAt: '2024-01-15T00:00:00Z'
  }
]

// 商品列表数据
export const mockProducts = [
  {
    id: 1,
    name: 'iPhone 17 Pro Max',
    price: 9999,
    image: '/src/assets/photo/iphone17.png',
    rating: 4.8,
    sales: 2580,
    description: '全新iPhone 17 Pro Max，搭载A17 Pro芯片，配备钛金属边框和USB-C接口。',
    specs: [
      {
        name: '容量',
        options: ['128GB', '256GB', '512GB', '1TB']
      },
      {
        name: '颜色',
        options: ['深空黑色', '蓝色钛金属', '白色钛金属', '原色钛金属']
      }
    ],
    images: [
      '/src/assets/photo/iphone17-1.png',
      '/src/assets/photo/iphone17-2.png',
      '/src/assets/photo/iphone17-3.png'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 2,
    name: 'MacBook Pro 16英寸',
    price: 19999,
    image: '/src/assets/photo/mac.png',
    rating: 4.9,
    sales: 1230,
    description: 'MacBook Pro配备M3 Max芯片，16英寸Liquid Retina XDR显示屏，专业级性能。',
    specs: [
      {
        name: '芯片',
        options: ['M3', 'M3 Pro', 'M3 Max']
      },
      {
        name: '内存',
        options: ['16GB', '32GB', '64GB', '96GB', '128GB']
      },
      {
        name: '存储',
        options: ['512GB', '1TB', '2TB', '4TB', '8TB']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=MacBook+Pro+1',
      'https://via.placeholder.com/800x800?text=MacBook+Pro+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 3,
    name: 'AirPods Pro (第3代)',
    price: 1999,
    image: '/src/assets/photo/airpods.png',
    rating: 4.7,
    sales: 5670,
    description: 'AirPods Pro具有主动降噪功能和空间音频，带来沉浸式聆听体验。',
    specs: [
      {
        name: '类型',
        options: ['标准版', 'USB-C充电盒', 'MagSafe充电盒']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=AirPods+Pro+1',
      'https://via.placeholder.com/800x800?text=AirPods+Pro+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 4,
    name: 'iPad Pro 12.9英寸',
    price: 7999,
    image: '/src/assets/photo/ipad.png',
    rating: 4.8,
    sales: 1890,
    description: 'iPad Pro配备M2芯片和12.9英寸Liquid Retina XDR显示屏。',
    specs: [
      {
        name: '容量',
        options: ['128GB', '256GB', '512GB', '1TB', '2TB']
      },
      {
        name: '连接方式',
        options: ['Wi-Fi', 'Wi-Fi + 蜂窝网络']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=iPad+Pro+1',
      'https://via.placeholder.com/800x800?text=iPad+Pro+2',
      'https://via.placeholder.com/800x800?text=iPad+Pro+3'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 5,
    name: 'Apple Watch Ultra 2',
    price: 6499,
    image: '/src/assets/photo/watch.png',
    rating: 4.6,
    sales: 3400,
    description: 'Apple Watch Ultra 2，钛金属表壳，49毫米显示屏，专业的户外运动手表。',
    specs: [
      {
        name: '表带',
        options: ['高山回环式表带', '野径回环式表带', '海洋表带']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=Apple+Watch+1',
      'https://via.placeholder.com/800x800?text=Apple+Watch+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 6,
    name: 'HomePod mini',
    price: 749,
    image: '/src/assets/photo/homPodMini.png',
    rating: 4.5,
    sales: 8900,
    description: 'HomePod mini，小巧身材，震撼音效，支持Siri语音控制。',
    specs: [
      {
        name: '颜色',
        options: ['白色', '深空灰色', '蓝色', '黄色', '橙色']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=HomePod+1',
      'https://via.placeholder.com/800x800?text=HomePod+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 7,
    name: 'Magic Keyboard',
    price: 899,
    image: '/src/assets/photo/magicKeyboard.png',
    rating: 4.4,
    sales: 5600,
    description: 'Magic Keyboard，剪刀式结构，舒适按键，支持Touch ID。',
    specs: [
      {
        name: '语言',
        options: ['中文（拼音）', '中文（五笔）', '英文', '日文']
      },
      {
        name: '连接',
        options: ['有线', '无线']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=Keyboard+1',
      'https://via.placeholder.com/800x800?text=Keyboard+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 8,
    name: 'Studio Display',
    price: 11499,
    image: '/src/assets/photo/studio.png',
    rating: 4.7,
    sales: 1200,
    description: 'Studio Display，27英寸5K视网膜显示屏，专为专业人士设计。',
    specs: [
      {
        name: '支架',
        options: ['可调角度支架', '可调高度支架', '倾斜和高度可调支架', 'nano-texture纳米纹理玻璃']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=Display+1',
      'https://via.placeholder.com/800x800?text=Display+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 9,
    name: 'AirTag',
    price: 229,
    image: '/src/assets/photo/airTag.png',
    rating: 4.3,
    sales: 15600,
    description: 'AirTag，小巧精致，帮你找到丢失的物品。',
    specs: [
      {
        name: '数量',
        options: ['1个装', '4个装']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=AirTag+1',
      'https://via.placeholder.com/800x800?text=AirTag+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 10,
    name: 'MagSafe充电器',
    price: 329,
    image: '/src/assets/photo/magsafe.png',
    rating: 4.2,
    sales: 23800,
    description: 'MagSafe充电器，15W无线充电，磁吸设计，安全便捷。',
    specs: [
      {
        name: '类型',
        options: ['标准版', '带USB-C数据线']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=MagSafe+1',
      'https://via.placeholder.com/800x800?text=MagSafe+2'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 11,
    name: '27英寸iMac',
    price: 13999,
    image: '/src/assets/photo/iMac.png',
    rating: 4.8,
    sales: 890,
    description: '27英寸iMac，配备M3芯片，Liquid Retina 5K显示屏，一体式设计。',
    specs: [
      {
        name: '芯片',
        options: ['M3', 'M3 Pro']
      },
      {
        name: '内存',
        options: ['8GB', '16GB', '24GB', '32GB']
      },
      {
        name: '存储',
        options: ['256GB', '512GB', '1TB', '2TB']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=iMac+1',
      'https://via.placeholder.com/800x800?text=iMac+2',
      'https://via.placeholder.com/800x800?text=iMac+3'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  },
  {
    id: 12,
    name: 'Logic Pro X',
    price: 1298,
    image: '/src/assets/photo/logic.png',
    rating: 4.9,
    sales: 5400,
    description: 'Logic Pro X，专业音乐制作软件，丰富的音效和循环。',
    specs: [
      {
        name: '类型',
        options: ['单机版', '教育版', '学生版']
      }
    ],
    images: [
      'https://via.placeholder.com/800x800?text=Logic+Pro+1'
    ],
    merchantName: 'Apple官方旗舰店',
    merchantAddress: '0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb'
  }
]

// 生成评价数据
export function generateMockReviews(productId) {
  const reviewTemplates = [
    {
      rating: 5,
      content: '非常满意！产品质量很好，发货速度快，包装也很用心。第一次购买就爱上了，会继续回购的。推荐给大家！',
      userAddress: '0x1234567890abcdef1234567890abcdef12345678'
    },
    {
      rating: 4,
      content: '整体不错，性价比很高。功能齐全，使用体验良好。就是价格稍微贵了点，如果能降价就更好了。',
      userAddress: '0x9876543210fedcba9876543210fedcba98765432'
    },
    {
      rating: 5,
      content: '超出预期！这是我在这个平台买的最满意的一件商品。质量没话说，卖家服务态度也很好，五星好评！',
      userAddress: '0xabcdef1234567890abcdef1234567890abcdef12'
    },
    {
      rating: 4,
      content: '商品收到，与描述一致。物流很快，包装完好。使用了一段时间，效果不错，值得推荐。',
      userAddress: '0xfedcba0987654321fedcba0987654321fedcba09'
    },
    {
      rating: 5,
      content: '超级棒！这是我用过最好的产品之一。设计和做工都很精良，用户体验非常出色。强烈推荐！',
      userAddress: '0x5555666677778888999900001111222233334444'
    }
  ]

  return reviewTemplates.map((template, index) => ({
    id: `review_${productId}_${index + 1}`,
    ...template,
    createdAt: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString(),
    images: [],
    nftId: `NFT_${productId}_${index + 1}`
  }))
}

// 生成交易记录数据
export function generateMockTransactions() {
  return [
    {
      id: 1,
      orderId: 'ORD20241201001',
      productId: 1,
      productName: 'iPhone 15 Pro Max',
      amount: 9999,
      status: 'completed',
      receiveStatus: 'confirmed', // confirmed: 已确认收货, pending: 待确认
      txHash: '0x8f3b3b1a7c4d2e9f6a8c5d4e3f2b1a9c8d7e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b9c8d',
      createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 2,
      orderId: 'ORD20241202002',
      productId: 3,
      productName: 'AirPods Pro (第3代)',
      amount: 1999,
      status: 'completed',
      receiveStatus: 'pending', // 已支付但未确认收货
      txHash: '0x7e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b9c8d7e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b9c8d',
      createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      orderId: 'ORD20241203003',
      productId: 2,
      productName: 'MacBook Pro 16英寸',
      amount: 19999,
      status: 'pending',
      receiveStatus: 'pending',
      txHash: null,
      createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 4,
      orderId: 'ORD20241204004',
      productId: 6,
      productName: 'HomePod mini',
      amount: 749,
      status: 'completed',
      receiveStatus: 'confirmed',
      txHash: '0x6d4e3f2a1b9c8d7e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b9c8d7e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b9c8d',
      createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 5,
      orderId: 'ORD20241205005',
      productId: 9,
      productName: 'AirTag',
      amount: 229,
      status: 'cancelled',
      receiveStatus: 'pending',
      txHash: null,
      createdAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString()
    }
  ]
}

// 生成奖励记录数据
export function generateMockRewards() {
  return [
    {
      id: 1,
      type: 'review',
      title: '评价奖励',
      description: '发布了高质量评价，获得奖励',
      amount: '0.001',
      status: 'received',
      createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 2,
      type: 'review',
      title: '评价奖励',
      description: '发布了高质量评价，获得奖励',
      amount: '0.001',
      status: 'received',
      createdAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      type: 'invite',
      title: '邀请奖励',
      description: '成功邀请好友注册',
      amount: '0.005',
      status: 'received',
      createdAt: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 4,
      type: 'review',
      title: '评价奖励',
      description: '发布了高质量评价，获得奖励',
      amount: '0.001',
      status: 'pending',
      createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString()
    }
  ]
}

// 生成优惠券数据
export function generateMockCoupons() {
  return [
    {
      id: 1,
      name: '新用户专享优惠券',
      description: '首次购买立减100元',
      discount: 100,
      status: 'unused',
      validUntil: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 2,
      name: '双十一优惠券',
      description: '满1000减200',
      discount: 200,
      status: 'used',
      validUntil: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: 3,
      name: '会员专享券',
      description: '任意商品9折优惠',
      discount: 0.1,
      status: 'unused',
      validUntil: new Date(Date.now() + 60 * 24 * 60 * 60 * 1000).toISOString()
    }
  ]
}

// 统计信息
export const mockStats = {
  totalTransactions: 15,
  totalReviews: 8,
  totalRewards: '0.008',
  totalCoupons: 3
}


