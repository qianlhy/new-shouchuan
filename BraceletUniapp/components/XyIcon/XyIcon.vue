<template>
  <image
    class="xy-icon"
    :src="src"
    :style="iconStyle"
    mode="aspectFit"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 40 },
  // 保留 color 参数兼容旧调用；PNG 已是品牌紫色
  color: { type: String, default: '#6B4EFF' }
})

const src = computed(() => `/static/icons/${props.name}.png`)

const iconStyle = computed(() => {
  const s = String(props.size)
  const size = /rpx|px|em|%/.test(s) ? s : `${s}rpx`
  return {
    width: size,
    height: size
  }
})
</script>

<style scoped>
.xy-icon {
  display: block;
  flex-shrink: 0;
}
</style>
