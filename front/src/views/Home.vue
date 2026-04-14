<template>
  <div class="home">
    <div class="container">
      <!-- 搜索栏 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 商品列表 -->
      <div v-else class="product-list">
        <el-row :gutter="20">
          <el-col
            v-for="product in products"
            :key="product.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
          >
            <ProductCard :product="product" />
          </el-col>
        </el-row>

        <!-- 空状态 -->
        <el-empty v-if="products.length === 0" description="暂无商品" />

        <!-- 分页 -->
        <div v-if="products.length > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, jumper, total"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useProductStore } from '@/stores/productStore'
import { getProductList } from '@/api/productApi'
import ProductCard from '@/components/ProductCard.vue'

const productStore = useProductStore()
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)

const products = computed(() => productStore.products)
const total = computed(() => productStore.total)
const loading = computed(() => productStore.loading)

// 加载商品列表
async function loadProducts() {
  productStore.setLoading(true)
  
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    
    const data = await getProductList(params)
    productStore.setProducts(data.list || [], data.total || 0)
  } catch (error) {
    console.error('加载商品失败:', error)
    // 后端未启动时，不显示错误，只记录日志
    if (!error.request) {
      ElMessage.error('加载商品失败')
    }
  } finally {
    productStore.setLoading(false)
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadProducts()
}

// 监听搜索关键词变化，当清空时自动重置
watch(searchKeyword, (newVal) => {
  if (newVal === '') {
    currentPage.value = 1
    loadProducts()
  }
})

// 分页改变
function handlePageChange() {
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.home {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-section {
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.search-section :deep(.el-input__wrapper) {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}


.loading-container {
  padding: 40px 0;
}

.product-list {
  min-height: 400px;
}

.product-list :deep(.el-row) {
  margin: -10px;
}

.product-list :deep(.el-col) {
  padding: 10px;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 50px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
}
</style>

