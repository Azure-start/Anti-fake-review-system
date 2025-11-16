import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChainStore = defineStore('chain', () => {
  const networkId = ref(null)
  const networkName = ref('')
  const isCorrectNetwork = ref(false)

  // 设置网络信息
  function setNetwork(id, name, isCorrect = false) {
    networkId.value = id
    networkName.value = name
    isCorrectNetwork.value = isCorrect
  }

  // 重置网络信息
  function resetNetwork() {
    networkId.value = null
    networkName.value = ''
    isCorrectNetwork.value = false
  }

  return {
    networkId,
    networkName,
    isCorrectNetwork,
    setNetwork,
    resetNetwork
  }
})

