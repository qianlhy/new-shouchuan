<template>
  <view class="page" v-if="detail">
    <image class="cover" :src="detail.imageUrl" mode="aspectFill" @click="preview" />

    <view class="info-card">
      <text class="title">{{ detail.title }}</text>
      <view class="price-row">
        <text class="price">¥ {{ detail.price }}</text>
        <text class="meta-text">{{ detail.beadCount || 0 }} 颗</text>
        <text v-if="detail.handSize" class="meta-text">· 手围 {{ detail.handSize }}cm</text>
      </view>
    </view>

    <view class="material-card" v-if="materialList.length">
      <view class="material-head">
        <text class="material-title">搭配清单（按顺序）</text>
        <text class="material-count">共 {{ totalBeads }} 颗</text>
      </view>
      <view class="material-item" v-for="(m, idx) in materialList" :key="idx">
        <text class="m-pos">{{ idx + 1 }}</text>
        <image class="m-img" :src="m.imageUrl" mode="aspectFill" />
        <view class="m-mid">
          <text class="m-name">{{ m.title }}</text>
          <text class="m-spec">{{ m.size || '-' }}mm · ¥{{ m.price }}/颗</text>
        </view>
        <text class="m-qty">x{{ m.qty }}</text>
      </view>
    </view>

    <view class="space" />
    <view class="action-bar">
      <button class="btn primary" @click="useDesign">带入 DIY 制作台</button>
    </view>
  </view>
  <view v-else-if="loading" class="state">加载中...</view>
  <view v-else class="state">作品不存在或未上架</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSquareDetail } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'

const DIY_EDIT_STORAGE_KEY = 'diy_edit_cart'
const detail = ref(null)
const diyInfo = ref(null)
const loading = ref(true)
let sid = ''

const materialList = computed(() => {
  const beads = (diyInfo.value && diyInfo.value.beads) || []
  // 按 position 展示顺序，清单按珠子逐颗列出更利于核对顺序
  const sorted = [...beads].sort((a, b) => Number(a.position || 0) - Number(b.position || 0))
  return sorted.map((b, i) => ({
    title: b.title || b.name || `珠子${i + 1}`,
    size: b.size,
    price: b.price,
    imageUrl: resolveImageUrl(b.imageUrl || ''),
    qty: 1
  }))
})
const totalBeads = computed(() => materialList.value.length)

function preview() {
  if (!detail.value || !detail.value.imageUrl) return
  uni.previewImage({ urls: [detail.value.imageUrl] })
}

function useDesign() {
  if (!diyInfo.value || !Array.isArray(diyInfo.value.beads) || !diyInfo.value.beads.length) {
    uni.showToast({ title: '暂无设计数据', icon: 'none' })
    return
  }
  try {
    uni.setStorageSync(DIY_EDIT_STORAGE_KEY, JSON.stringify({
      diyData: diyInfo.value,
      fromSquareId: Number(sid) || null
    }))
    uni.switchTab({ url: '/pages/design/index' })
  } catch (e) {
    uni.showToast({ title: '打开制作台失败', icon: 'none' })
  }
}

onLoad(async (options) => {
  sid = options && options.id ? options.id : ''
  const channel = (options && options.channel) || 'square'
  if (!sid) {
    loading.value = false
    return
  }
  try {
    const res = await getSquareDetail(sid, channel)
    if (!res) {
      detail.value = null
      return
    }
    let diy = res.diyData
    if (typeof diy === 'string') {
      try { diy = JSON.parse(diy) } catch (e) { diy = null }
    }
    if (diy && Array.isArray(diy.beads)) {
      diy.beads = [...diy.beads].sort((a, b) => Number(a.position || 0) - Number(b.position || 0))
    }
    diyInfo.value = diy
    detail.value = {
      id: res.id,
      title: res.title || 'DIY设计',
      imageUrl: resolveImageUrl(res.imageUrl || (diy && diy.imageUrl) || ''),
      price: Number(res.price || 0).toFixed(2),
      beadCount: res.beadCount || (diy && diy.beads && diy.beads.length) || 0,
      handSize: res.handSize || (diy && diy.size)
    }
  } catch (e) {
    console.error('广场详情加载失败', e)
    detail.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss">
$page-bg: #F7F5FF;
$primary: #8B5CF6;
$text-main: #2A2140;
$text-sub: #8B849C;

.page {
  min-height: 100vh;
  background: $page-bg;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}
.cover {
  width: 100%;
  height: 720rpx;
  background: #eee;
}
.info-card {
  margin: -40rpx 24rpx 20rpx;
  position: relative;
  z-index: 1;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(139, 92, 246, 0.08);
}
.title {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: $text-main;
}
.price-row {
  margin-top: 16rpx;
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 12rpx;
}
.price {
  font-size: 40rpx;
  font-weight: 800;
  color: $primary;
}
.meta-text {
  font-size: 24rpx;
  color: $text-sub;
}
.material-card {
  margin: 0 24rpx 20rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
}
.material-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.material-title {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-main;
}
.material-count {
  font-size: 24rpx;
  color: $text-sub;
}
.material-item {
  display: flex;
  align-items: center;
  padding: 14rpx 0;
  border-bottom: 1rpx solid rgba(139, 92, 246, 0.08);
}
.m-pos {
  width: 36rpx;
  font-size: 22rpx;
  color: $primary;
  font-weight: 700;
}
.m-img {
  width: 72rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #f5f3ff;
  margin-right: 16rpx;
}
.m-mid { flex: 1; min-width: 0; }
.m-name {
  display: block;
  font-size: 26rpx;
  color: $text-main;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.m-spec {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $text-sub;
}
.m-qty {
  font-size: 26rpx;
  color: $text-main;
  font-weight: 600;
}
.space { height: 40rpx; }
.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -8rpx 24rpx rgba(42, 33, 64, 0.06);
}
.btn {
  margin: 0;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 700;
}
.btn.primary {
  background: linear-gradient(135deg, #8B5CF6, #A78BFA);
  color: #fff;
}
.btn::after { border: none; }
.state {
  text-align: center;
  padding: 120rpx 0;
  color: $text-sub;
}
</style>
