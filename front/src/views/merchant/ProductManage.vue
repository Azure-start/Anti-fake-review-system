<template>
  <div class="product-manage">
    <div v-if="debug" class="debug-badge">DEBUG: ProductManage 已加载</div>
    <div class="container">
      <el-card header="商品管理" class="product-card">
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增商品
          </el-button>
        </div>

        <!-- 添加/编辑商品弹窗 -->
        <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
          <el-form :model="productForm" :rules="rules" ref="formRef" label-width="120px">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="商品名称" prop="name">
                  <el-input v-model="productForm.name" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="价格(元)" prop="price">
                  <el-input-number v-model="productForm.price" :min="0" :step="0.01" style="width:100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="主图" prop="image">
              <!-- 改为原生input上传 -->
              <input 
                type="file" 
                ref="mainImageInput" 
                accept="image/*"
                @change="handleMainImageUpload"
                style="display: none"
              />
              <el-button 
                size="small" 
                type="primary"
                @click="$refs.mainImageInput.click()"
                :loading="uploadingMain"
              >
                {{ uploadingMain ? '上传中...' : '上传主图' }}
              </el-button>
              <div v-if="productForm.image" class="image-preview">
                <el-image :src="productForm.image" style="width:140px;height:140px; margin-top:8px;" fit="cover" />
                <el-button type="text" @click="removeMainImage">移除</el-button>
              </div>
            </el-form-item>

            <el-form-item label="图片集" prop="images">
              <!-- 改为原生input上传 -->
              <input 
                type="file" 
                ref="galleryInput" 
                accept="image/*"
                multiple
                @change="handleGalleryUpload"
                style="display: none"
              />
              <el-button 
                size="small"
                @click="$refs.galleryInput.click()"
                :loading="uploadingGallery"
              >
                {{ uploadingGallery ? '上传中...' : '上传图片集' }}
              </el-button>
              <div class="gallery" style="margin-top:8px; display:flex; gap:8px; flex-wrap:wrap;">
                <div v-for="(img, idx) in productForm.images" :key="idx" class="thumb">
                  <el-image :src="img" style="width:100px;height:100px;" fit="cover" />
                  <div style="text-align:center; margin-top:4px;">
                    <el-button size="mini" type="text" @click="removeGalleryImage(idx)">移除</el-button>
                  </div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="规格(JSON 或简单文本)" prop="specs">
              <el-input type="textarea" v-model="productForm.specs" rows="2" />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="库存" prop="stock">
                  <el-input-number v-model="productForm.stock" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <template v-if="editing">
                    <el-select
                        v-model="productForm.status"
                        placeholder="请选择"
                        :disabled="productForm.status === 'pending'"
                    >
                      <el-option label="待审核" value="pending" disabled />
                      <el-option label="已批准" value="approved" disabled />
                      <el-option label="已拒绝" value="rejected" disabled />
                      <el-option label="在售" value="onSale" />
                      <el-option label="下架" value="offShelf" />
                    </el-select>
                  </template>
                  <template v-else>
                    <el-tag type="warning">{{ getStatusText('pending') }}</el-tag>
                  </template>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="描述" prop="description">
              <el-input type="textarea" v-model="productForm.description" rows="3" />
            </el-form-item>
          </el-form>

          <template #footer>
            <el-button @click="handleDialogCancel">取消</el-button>
            <el-button type="primary" @click="handleSave">保存</el-button>
          </template>
        </el-dialog>

        <div class="search-form">
          <el-form :inline="true" :model="searchForm" size="small">
            <el-form-item label="商品名称">
              <el-input v-model="searchForm.productName" placeholder="请输入商品名称" style="width: 200px;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 150px;">
                <el-option label="所有" value="" />
                <el-option label="待审核" value="pending" />
                <el-option label="已批准" value="approved" />
                <el-option label="已拒绝" value="rejected" />
                <el-option label="在售" value="onSale" />
                <el-option label="下架" value="offShelf" />
              </el-select>
            </el-form-item>
            <el-form-item label="上架时间">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 280px;"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-loading="loading">
          <el-table :data="products" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商品名称" min-width="200" />
            <el-table-column prop="price" label="价格" width="120">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上架时间" width="180">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" text @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button
                    v-if="row.status === 'onSale'"
                    type="warning"
                    size="small"
                    text
                    @click="handleOffShelf(row)"
                >
                  下架
                </el-button>
                <el-button
                    v-if="['offShelf', 'approved'].includes(row.status)"
                    type="success"
                    size="small"
                    text
                    @click="handleOnShelf(row)"
                >
                  上架
                </el-button>
                <el-button type="danger" size="small" text @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="products.length > 0" class="pagination">
            <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :page-sizes="[5, 10, 20, 50]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="handlePageChange"
                @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, reactive, computed} from 'vue'
import request from '@/api/request'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/userStore'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {
  getMyProducts,
  deleteProduct,
  offShelfProduct,
  createProduct,
  updateProduct,
  onSaleProduct
} from '@/api/merchantApi'

const router = useRouter()

const loading = ref(false)
const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  productName: '',
  status: '',
  dateRange: null
})

// 弹窗与表单状态
const dialogVisible = ref(false)
const editing = ref(false)
const currentId = ref(null)
const formRef = ref(null)

// 添加上传状态变量
const uploadingMain = ref(false)
const uploadingGallery = ref(false)

const productForm = reactive({
  name: '',
  description: '',
  price: 0,
  image: '',
  // images 存为数组，保存时会序列化为 JSON 字符串发送给后端
  images: [],
  specs: '',
  stock: 0,
  // 默认不直接上架，新增时应为待审核
  status: 'pending'
})

const rules = reactive({
  name: [{required: true, message: '请输入商品名称', trigger: 'blur'}],
  price: [{required: true, message: '请输入价格', trigger: 'change'}],
  stock: [{required: true, message: '请输入库存', trigger: 'change'}]
})

const dialogTitle = computed(() => (editing.value ? '编辑商品' : '添加商品'))

// 加载商品列表
async function loadProducts() {
  loading.value = true

  try {
    // 构建查询参数
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      merchantAddress: userStore.walletAddress,
      productName: searchForm.productName,
      status: searchForm.status
    }

    // 处理时间范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startTime = new Date(searchForm.dateRange[0])
      // 结束时间设置为当天的23:59:59
      const endDate = new Date(searchForm.dateRange[1])
      endDate.setHours(23, 59, 59, 999)
      params.endTime = endDate
    }

    const result = await getMyProducts(params)

    if (result.code === 0) {
      products.value = result.list || []
      total.value = result.total || 0
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理查询
function handleSearch() {
  currentPage.value = 1
  loadProducts()
}

// 处理重置
function handleReset() {
  Object.assign(searchForm, {
    productName: '',
    status: '',
    dateRange: null
  })
  currentPage.value = 1
  loadProducts()
}

// 添加商品
function handleAdd() {
  editing.value = false
  currentId.value = null
  console.log('ProductManage: handleAdd clicked')
  // 重置表单
  Object.assign(productForm, {
    name: '',
    description: '',
    price: 0,
    image: '',
    images: [],
    specs: '',
    stock: 0,
    status: 'pending'
  })
  dialogVisible.value = true
}

// 编辑商品：在弹窗中编辑
function handleEdit(row) {
  editing.value = true
  currentId.value = row.id
  console.log('ProductManage: handleEdit clicked', row && row.id)
  // 将后端字段映射到表单（images/specs 转为字符串）
  Object.assign(productForm, {
    name: row.name || '',
    description: row.description || '',
    price: row.price || 0,
    image: row.image || '',
    images: (() => {
      try {
        if (!row.images) return []
        if (typeof row.images === 'string') {
          const parsed = JSON.parse(row.images)
          return Array.isArray(parsed) ? parsed : [parsed]
        }
        return Array.isArray(row.images) ? row.images : []
      } catch (e) {
        return []
      }
    })(),
    specs: typeof row.specs === 'string' ? row.specs : JSON.stringify(row.specs || {}),
    stock: row.stock || 0,
    status: row.status || 'onSale'
  })
  dialogVisible.value = true
}

function handleDialogCancel() {
  dialogVisible.value = false
  // 清理表单校验
  if (formRef.value && formRef.value.clearValidate) formRef.value.clearValidate()
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch (err) {
    return
  }

  const payload = {
    name: productForm.name,
    description: productForm.description,
    price: Number(productForm.price),
    image: productForm.image,
    // images 字段发送为 JSON 字符串
    images: Array.isArray(productForm.images) ? JSON.stringify(productForm.images) : (function () {
      const v = productForm.images || ''
      if (!v) return '[]'
      try {
        return JSON.stringify(JSON.parse(v))
      } catch (e) {
        return JSON.stringify(v.split(',').map(s => s.trim()).filter(Boolean))
      }
    })(),
    specs: (() => {
      const v = productForm.specs
      if (!v) return ''
      try {
        const parsed = JSON.parse(v)
        return typeof parsed === 'string' ? parsed : JSON.stringify(parsed)
      } catch (e) {
        return v
      }
    })(),
    stock: Number(productForm.stock),
    status: productForm.status
  }

  // 如果是新增商品，强制设置为待审核，并带上商家信息
  if (!editing.value) {
    payload.status = 'pending'
    payload.merchantId = userStore.shopInfo?.id || null
    payload.merchantAddress = userStore.shopInfo?.address || userStore.walletAddress || null
  }

  // 审核中商品不允许改状态，强制保持 pending
  if (editing.value && productForm.status === 'pending') {
    payload.status = 'pending'
  }

  try {
    let result
    if (editing.value && currentId.value) {
      result = await updateProduct(currentId.value, payload)
    } else {
      result = await createProduct(payload)
    }

    if (result && result.code === 0) {
      ElMessage.success(editing.value ? result.message || '更新成功' : result.message || '创建成功')
      dialogVisible.value = false
      await loadProducts()
    }
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('保存失败')
  }
}

// 下架商品
async function handleOffShelf(row) {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await offShelfProduct(row.id)
    if (result.code === 0) {
      ElMessage.success(result.message || '下架成功')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架商品失败:', error)
    }
  }
}

// 上架商品
async function handleOnShelf(row) {
  try {
    const result = await onSaleProduct(row.id)
    if (result.code === 0) {
      ElMessage.success(result.message || '上架成功')
      await loadProducts()
    }
  } catch (error) {
    console.error('上架商品失败:', error)
  }
}

// 删除商品
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const result = await deleteProduct(row.id)
    if (result.code === 0) {
      ElMessage.success(result.message || '删除成功')
      await loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

function getStatusType(status) {
  const statusMap = {
    // 把后端状态映射为 Element Plus 的 tag type（颜色）
    pending: 'warning',   // 待处理 - 橙色
    approved: 'primary',  // 已批准 - 蓝色
    rejected: 'danger',   // 已拒绝 - 红色
    onSale: 'success',    // 出售中 - 绿色
    offShelf: 'info',     // 下架 - 灰/蓝
    deleted: 'danger'
  }
  return statusMap[status] || 'info'
}

function getStatusText(status) {
  const statusMap = {
    // 将后端状态值转换为前端展示的中文
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝',
    onSale: '在售',
    offShelf: '下架',
    deleted: '已删除'
  }
  return statusMap[status] || status
}

function formatTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

function handlePageChange(page) {
  currentPage.value = page
  loadProducts()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

onMounted(() => {
  loadProducts()
})

const userStore = useUserStore()

// ============ 修改图片上传相关代码 ============

// 处理主图上传
async function handleMainImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件
  if (!validateFile(file)) {
    event.target.value = ''
    return
  }

  uploadingMain.value = true
  try {
    // 显示本地预览
    const localUrl = URL.createObjectURL(file)
    productForm.image = localUrl

    // 上传到服务器
    const serverUrl = await uploadFileToServer(file)
    
    // 替换为服务器URL
    URL.revokeObjectURL(localUrl)
    productForm.image = serverUrl
    
    ElMessage.success('主图上传成功')
  } catch (error) {
    console.error('主图上传失败:', error)
    productForm.image = ''
    // 错误信息已在 uploadFileToServer 中显示
  } finally {
    uploadingMain.value = false
    event.target.value = ''
  }
}

// 处理图片集上传
async function handleGalleryUpload(event) {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  // 验证所有文件
  const validFiles = files.filter(file => {
    if (!validateFile(file)) {
      ElMessage.error(`文件 ${file.name} 格式或大小不正确，已跳过`)
      return false
    }
    return true
  })

  if (validFiles.length === 0) {
    event.target.value = ''
    return
  }

  uploadingGallery.value = true
  const successUrls = []
  const errors = []

  for (const file of validFiles) {
    try {
      // 显示本地预览
      const localUrl = URL.createObjectURL(file)
      const previewIndex = productForm.images.length
      productForm.images.push(localUrl)

      // 上传到服务器
      const serverUrl = await uploadFileToServer(file)
      
      // 替换为服务器URL
      URL.revokeObjectURL(localUrl)
      productForm.images[previewIndex] = serverUrl
      successUrls.push(serverUrl)
      
    } catch (error) {
      console.error(`图片 ${file.name} 上传失败:`, error)
      errors.push(file.name)
      
      // 移除失败的预览
      const index = productForm.images.findIndex(img => img.startsWith('blob:'))
      if (index !== -1) {
        URL.revokeObjectURL(productForm.images[index])
        productForm.images.splice(index, 1)
      }
    }
  }

  if (successUrls.length > 0) {
    ElMessage.success(`成功上传 ${successUrls.length} 张图片`)
  }
  if (errors.length > 0) {
    ElMessage.error(`${errors.length} 张图片上传失败`)
  }

  uploadingGallery.value = false
  event.target.value = ''
}

// 验证文件
function validateFile(file) {
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WEBP 格式的图片')
    return false
  }

  // 验证文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }

  return true
}

// 上传文件到服务器
async function uploadFileToServer(file) {
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await request.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    console.log('上传响应:', response)

    // 根据不同的响应格式提取URL
    let imageUrl = null

    // 情况1: 直接返回URL字符串
    if (typeof response === 'string' && (response.startsWith('http') || response.startsWith('/'))) {
      imageUrl = response
    }
    // 情况2: 返回对象包含url字段
    else if (response && response.url) {
      imageUrl = response.url
    }
    // 情况3: 标准格式 {code: 0, data: 'url'}
    else if (response && response.code === 0 && response.data) {
      imageUrl = response.data
    }
    // 情况4: 返回对象包含data字段
    else if (response && response.data) {
      if (typeof response.data === 'string') {
        imageUrl = response.data
      } else if (response.data.url) {
        imageUrl = response.data.url
      }
    }

    if (!imageUrl) {
      console.error('无法解析上传响应:', response)
      throw new Error('服务器返回格式错误')
    }

    return imageUrl

  } catch (error) {
    console.error('上传请求失败:', error)
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      throw new Error(`服务器错误 ${status}: ${JSON.stringify(data)}`)
    } else if (error.request) {
      throw new Error('网络错误：无法连接到服务器')
    } else {
      throw error
    }
  }
}

// 移除主图
function removeMainImage() {
  // 如果是本地预览URL，需要释放
  if (productForm.image && productForm.image.startsWith('blob:')) {
    URL.revokeObjectURL(productForm.image)
  }
  productForm.image = ''
}

// 移除图片集中的图片
function removeGalleryImage(idx) {
  // 如果是本地预览URL，需要释放
  const imgUrl = productForm.images[idx]
  if (imgUrl && imgUrl.startsWith('blob:')) {
    URL.revokeObjectURL(imgUrl)
  }
  productForm.images.splice(idx, 1)
}
</script>

<style scoped>
.product-manage {
  padding: 40px 0;
  min-height: calc(100vh - 64px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.product-card {
  margin-top: 20px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.06);
}

.product-card :deep(.el-card__header) {
  background: #ffffff;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 600;
  padding: 20px 24px;
}

.toolbar {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px;
}
</style>