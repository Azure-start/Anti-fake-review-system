<template>
  <el-dialog
    v-model="visible"
    title="切换角色（仅用于测试）"
    width="400px"
    @close="handleClose"
  >
    <div class="role-select">
      <el-radio-group v-model="selectedRole" @change="handleChange">
        <el-radio label="user" class="role-option">用户</el-radio>
        <el-radio label="merchant" class="role-option">商家</el-radio>
        <el-radio label="admin" class="role-option">管理员</el-radio>
      </el-radio-group>
    </div>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const userStore = useUserStore()

const visible = ref(props.modelValue)
const selectedRole = ref(userStore.userRole)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    selectedRole.value = userStore.userRole
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function getRoleText(role) {
  const roleMap = {
    user: '用户',
    merchant: '商家',
    admin: '管理员'
  }
  return roleMap[role] || '未知'
}

function handleChange(role) {
  userStore.setUserRole(role)
  ElMessage.success(`已切换到${getRoleText(role)}角色`)
  
  // 根据角色跳转到对应页面
  setTimeout(() => {
    if (role === 'merchant') {
      router.push('/merchant')
    } else if (role === 'admin') {
      router.push('/admin')
    } else {
      router.push('/')
    }
    handleClose()
  }, 100)
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.role-select {
  padding: 20px 0;
}

.role-option {
  display: block;
  margin-bottom: 16px;
  font-size: 16px;
}
</style>

