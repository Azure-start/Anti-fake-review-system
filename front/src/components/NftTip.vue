<template>
  <el-dialog
    v-model="visible"
    title="评价已上链"
    width="500px"
    align-center
  >
    <div class="nft-tip-content">
      <el-result
        icon="success"
        title="评价已生成NFT"
        sub-title="您的评价已成功上链"
      >
        <template #extra>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="NFT ID">
              <div class="nft-id">
                {{ nftId }}
                <el-button
                  type="primary"
                  :icon="CopyDocument"
                  size="small"
                  circle
                  @click="copyNftId"
                />
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="交易哈希">
              <div class="tx-hash">
                {{ txHash }}
                <el-button
                  type="primary"
                  :icon="CopyDocument"
                  size="small"
                  circle
                  @click="copyTxHash"
                />
              </div>
            </el-descriptions-item>
          </el-descriptions>
          
          <div class="actions">
            <el-button @click="handleClose">关闭</el-button>
            <el-button type="primary" @click="handleViewInWallet">
              在钱包中查看
            </el-button>
          </div>
        </template>
      </el-result>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  nftId: String,
  txHash: String
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 复制NFT ID
function copyNftId() {
  navigator.clipboard.writeText(props.nftId).then(() => {
    ElMessage.success('NFT ID已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 复制交易哈希
function copyTxHash() {
  navigator.clipboard.writeText(props.txHash).then(() => {
    ElMessage.success('交易哈希已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 关闭弹窗
function handleClose() {
  visible.value = false
}

// 在钱包中查看
function handleViewInWallet() {
  if (props.txHash) {
    const explorerUrl = `https://sepolia.etherscan.io/tx/${props.txHash}`
    window.open(explorerUrl, '_blank')
  }
}
</script>

<style scoped>
.nft-tip-content {
  padding: 20px 0;
}

.nft-id,
.tx-hash {
  display: flex;
  align-items: center;
  gap: 8px;
  word-break: break-all;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}
</style>

