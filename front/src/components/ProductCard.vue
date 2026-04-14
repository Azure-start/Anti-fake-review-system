<template>
  <el-card
    class="product-card"
    shadow="hover"
    @click="handleClick"
  >
    <div class="product-image">
      <el-image
        :src="product.image || '/placeholder.png'"
        :alt="product.name"
        fit="cover"
        lazy
      >
        <template #error>
          <div class="image-slot">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
    </div>
    
    <div class="product-info">
      <h3 class="product-name">{{ product.name }}</h3>
      
      <div class="product-price">
        <span class="price">¥{{ product.price }}</span>
      </div>
      
      <div class="product-footer">
        <el-rate
          v-model="product.rating"
          disabled
          show-score
          text-color="#ff9900"
          score-template="{value}"
        />
        <span class="sales">已售 {{ product.sales || 0 }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function handleClick() {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.08);
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.08);
}

.product-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  border-radius: 0;
  margin-bottom: 0;
  background: #f5f7fa;
}

.product-image :deep(.el-image__inner) {
  transition: transform 0.5s;
}

.product-card:hover .product-image :deep(.el-image__inner) {
  transform: scale(1.05);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: rgba(255, 255, 255, 0.8);
  font-size: 40px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 8px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.product-price {
  margin: 4px 0;
}

.price {
  font-size: 26px;
  font-weight: bold;
  background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.sales {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}
</style>

