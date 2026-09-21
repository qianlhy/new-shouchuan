<template>
  <view class="page">
    <view class="banner">
      <view class="banner-content">
        <text class="banner-en">RECOMMENDED</text>
        <text class="banner-title">推荐设计</text>
        <text class="banner-sub">精选设计，一键带入 DIY</text>
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!list.length" class="state">暂无设计作品</view>

    <view v-else class="list">
      <view
        v-for="item in list"
        :key="item.id"
        class="card"
        @click="goDetail(item)"
      >
        <image class="thumb" :src="item.imageUrl" mode="aspectFill" />
        <view class="meta">
          <text class="title">{{ item.title }}</text>
          <view class="sub-row">
            <text class="sub">{{ item.beadCount || 0 }} 颗珠子</text>
            <text v-if="item.handSize" class="sub">· 手围 {{ item.handSize }}cm</text>
          </view>
          <view class="actions">
            <view class="price-pill">¥{{ item.price }}</view>
            <view class="diy-btn" @click.stop="bringDiy(item)">带入DIY</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSquareList, getSquareDetail } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'

const DIY_EDIT_STORAGE_KEY = 'diy_edit_cart'
const loading = ref(false)
const list = ref([])

function mapItem(p) {
  return {
    id: p.id,
    title: p.title || 'DIY设计',
    imageUrl: resolveImageUrl(p.imageUrl || ''),
    price: Number(p.price || 0).toFixed(2),
    beadCount: p.beadCount || 0,
    handSize: p.handSize,
    diyData: p.diyData
  }
}

async function loadList() {
  loading.value = true
  try {
    const rows = await getSquareList('recommend')
    list.value = (Array.isArray(rows) ? rows : []).map(mapItem)
  } catch (e) {
    console.error('推荐设计加载失败', e)
    list.value = []
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/square/detail?id=${item.id}&channel=recommend` })
}

async function bringDiy(item) {
  try {
    let diyData = item.diyData
    if (!diyData) {
      const detail = await getSquareDetail(item.id, 'recommend')
      diyData = detail && detail.diyData
    }
    if (!diyData) {
      uni.showToast({ title: '暂无设计数据', icon: 'none' })
      return
    }
    uni.setStorageSync(DIY_EDIT_STORAGE_KEY, JSON.stringify({
      diyData,
      fromSquareId: item.id
    }))
    uni.switchTab({ url: '/pages/design/index' })
  } catch (e) {
    console.error('带入DIY失败', e)
    uni.showToast({ title: '打开制作台失败', icon: 'none' })
  }
}

onShow(() => {
  loadList()
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
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.banner {
  margin: 24rpx;
  padding: 36rpx 32rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
}
.banner-en {
  display: block;
  font-size: 20rpx;
  letter-spacing: 3rpx;
  color: rgba(255, 255, 255, 0.7);
}
.banner-title {
  display: block;
  margin-top: 8rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #fff;
}
.banner-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
}
.state {
  text-align: center;
  color: $text-sub;
  font-size: 26rpx;
  padding: 80rpx 0;
}
.list {
  padding: 0 24rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.card {
  display: flex;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(139, 92, 246, 0.08);
}
.thumb {
  width: 220rpx;
  height: 220rpx;
  flex-shrink: 0;
  background: #f0ecff;
}
.meta {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.title {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-main;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sub-row {
  margin-top: 8rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 4rpx;
}
.sub {
  font-size: 22rpx;
  color: $text-sub;
}
.actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.price-pill {
  font-size: 30rpx;
  font-weight: 700;
  color: $primary;
}
.diy-btn {
  padding: 10rpx 22rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #8B5CF6, #A78BFA);
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
}
</style>
