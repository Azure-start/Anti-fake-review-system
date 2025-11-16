<template>
  <div id="app">
    <NavBar />
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import NavBar from '@/components/NavBar.vue'

const userStore = useUserStore()

onMounted(() => {
  // 初始化用户状态
  userStore.initStore()
})
</script>

<style scoped>
#app {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f0f2f5 0%, #fafafa 100%);
}
</style>

