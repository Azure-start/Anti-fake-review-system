import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProductStore = defineStore('product', () => {
  const products = ref([])
  const currentProduct = ref(null)
  const total = ref(0)
  const loading = ref(false)

  // 设置商品列表
  function setProducts(list, totalCount = 0) {
    products.value = list
    total.value = totalCount
  }

  // 添加商品
  function addProduct(product) {
    products.value.push(product)
  }

  // 更新商品
  function updateProduct(productId, updates) {
    const index = products.value.findIndex(p => p.id === productId)
    if (index !== -1) {
      products.value[index] = { ...products.value[index], ...updates }
    }
  }

  // 设置当前商品
  function setCurrentProduct(product) {
    currentProduct.value = product
  }

  // 设置加载状态
  function setLoading(value) {
    loading.value = value
  }

  return {
    products,
    currentProduct,
    total,
    loading,
    setProducts,
    addProduct,
    updateProduct,
    setCurrentProduct,
    setLoading
  }
})

