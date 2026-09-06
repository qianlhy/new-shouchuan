<template>
  <view class="page">
    <view class="banner">
      <view class="banner-deco d1" />
      <view class="banner-deco d2" />
      <view class="banner-deco d3" />
      <view class="banner-deco d4" />
      <view class="banner-content">
        <text class="banner-en">INSPIRATION SQUARE</text>
        <text class="banner-title">灵感广场</text>
        <text class="banner-sub">发现心动，遇见灵感的你。</text>
      </view>
      <image class="banner-star" src="/static/icons/sparkles.png" mode="aspectFit" />
    </view>

    <view class="tabs">
      <view
        v-for="t in tabs"
        :key="t.key"
        class="tab"
        :class="{ active: activeTab === t.key }"
        @click="switchTab(t.key)"
      >
        {{ t.label }}
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="!displayList.length" class="state">暂无灵感作品</view>

    <view v-else class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="card"
        @click="goDetail(item)"
      >
        <image class="thumb" :src="item.imageUrl" mode="aspectFill" />
        <view class="meta">
          <text class="title">{{ item.title }}</text>
          <view class="author">
            <text class="at">@</text>
            <text class="name">{{ item.author }}</text>
          </view>

          <view class="wish-row">
            <text class="wish-label">{{ item.achieved ? '心愿已达成' : '心愿进度' }}</text>
            <text class="wish-num">{{ item.progressText }}</text>
          </view>
          <view class="progress-track">
            <view
              class="progress-fill"
              :class="{ done: item.achieved }"
              :style="{ width: item.progressPercent + '%' }"
            />
          </view>

          <view class="actions">
            <template v-if="item.achieved">
              <view class="price-pill">¥{{ item.price }}</view>
              <view class="buy-btn" @click.stop="goBuy(item)">去购买</view>
            </template>
            <view v-else class="want-btn" @click.stop="toggleWant(item)">
              {{ wantedIds.includes(item.id) ? '已想要' : '想要' }}
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getProductList } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'

const WANT_KEY = 'xy_square_wanted'

const tabs = [
  { key: 'latest', label: '最新' },
  { key: 'hot', label: '热门' },
  { key: 'designer', label: '设计师款' }
]

const activeTab = ref('latest')
const loading = ref(false)
const products = ref([])
const wantedIds = ref([])

const displayList = computed(() => {
  let list = [...products.value]
  if (activeTab.value === 'hot') {
    list.sort((a, b) => (b.sales || b.id || 0) - (a.sales || a.id || 0))
  } else if (activeTab.value === 'designer') {
    const filtered = list.filter((p) => {
      const name = `${p.title || ''}${p.categoryName || ''}${p.name || ''}`
      return /设计|严选|设计师|定制/.test(name)
    })
    if (filtered.length) list = filtered
  } else {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  }
  return list.map(mapCard)
})

function mapCard(p) {
  const status = Number(p.status ?? 1)
  const achieved = status === 1
  const price = Number(p.price || 0).toFixed(2)
  const wantCount = (wantedIds.value.includes(p.id) ? 1 : 0) + (p.id % 37)
  const goal = 100
  const progress = achieved ? 100 : Math.min(99, wantCount)
  return {
    id: p.id,
    title: p.name || p.title || '未命名手作',
    author: '许愿手作',
    imageUrl: resolveImageUrl(p.image || p.imageUrl || p.coverImage || ''),
    price,
    achieved,
    progressPercent: progress,
    progressText: achieved ? `${goal}/${goal}` : `${progress}/${goal}`,
    raw: p
  }
}

function loadWanted() {
  try {
    wantedIds.value = uni.getStorageSync(WANT_KEY) || []
  } catch (e) {
    wantedIds.value = []
  }
}

function saveWanted() {
  uni.setStorageSync(WANT_KEY, wantedIds.value)
}

async function loadProducts() {
  loading.value = true
  try {
    const list = await getProductList()
    products.value = Array.isArray(list) ? list : []
  } catch (e) {
    console.error('广场加载失败', e)
    products.value = []
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(key) {
  activeTab.value = key
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}

function goBuy(item) {
  uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}

function toggleWant(item) {
  const idx = wantedIds.value.indexOf(item.id)
  if (idx >= 0) {
    wantedIds.value.splice(idx, 1)
    uni.showToast({ title: '已取消想要', icon: 'none' })
  } else {
    wantedIds.value.push(item.id)
    uni.showToast({ title: '已加入想要', icon: 'none' })
  }
  saveWanted()
}

onShow(() => {
  loadWanted()
  loadProducts()
})
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  min-height: 100vh;
  background:
    radial-gradient(ellipse 80% 40% at 15% -5%, rgba(183, 148, 255, 0.28), transparent 55%),
    radial-gradient(ellipse 60% 30% at 90% 0%, rgba(221, 200, 255, 0.4), transparent 50%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 30%, #FBF7FF 100%);
  padding: 24rpx 28rpx 160rpx;
  box-sizing: border-box;
}

.banner {
  position: relative;
  overflow: hidden;
  border-radius: 32rpx;
  padding: 44rpx 40rpx 48rpx;
  background: $gradient-banner;
  box-shadow: $shadow-card;
  margin-bottom: 28rpx;
}

.banner-deco {
  position: absolute;
  border-radius: 50%;
}
.d1 {
  width: 200rpx; height: 200rpx; right: -50rpx; top: -60rpx;
  background: radial-gradient(circle, rgba(183, 148, 255, 0.55) 0%, rgba(221, 200, 255, 0.15) 60%, transparent 75%);
}
.d2 {
  width: 120rpx; height: 120rpx; right: 90rpx; bottom: -40rpx;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.22) 0%, transparent 70%);
}
.d3 {
  width: 56rpx; height: 56rpx; left: 36rpx; bottom: 28rpx;
  background: rgba(255, 255, 255, 0.55);
}
.d4 {
  width: 90rpx; height: 90rpx; left: -20rpx; top: 20rpx;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.7) 0%, transparent 70%);
}

.banner-content { position: relative; z-index: 1; }
.banner-en {
  display: block;
  font-size: 20rpx;
  letter-spacing: 2rpx;
  color: $primary-light;
  margin-bottom: 8rpx;
  font-weight: 600;
}
.banner-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: $text-on-soft;
  letter-spacing: 2rpx;
}
.banner-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: rgba(74, 58, 120, 0.65);
}
.banner-star {
  position: absolute;
  right: 40rpx;
  top: 44rpx;
  width: 48rpx;
  height: 48rpx;
  opacity: 0.85;
  z-index: 1;
}

.tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.tab {
  padding: 14rpx 32rpx;
  border-radius: $radius-pill;
  font-size: 26rpx;
  color: $text-sub;
  background: transparent;
}
.tab.active {
  background: $gradient-primary;
  color: #fff;
  font-weight: 600;
  box-shadow: $shadow-btn;
}

.state {
  text-align: center;
  color: $text-sub;
  padding: 80rpx 0;
  font-size: 28rpx;
}

.list { display: flex; flex-direction: column; gap: 20rpx; }

.card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  background: $card-bg;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-soft;
}

.thumb {
  width: 148rpx;
  height: 148rpx;
  border-radius: 50%;
  background: $primary-soft;
  flex-shrink: 0;
}

.meta { flex: 1; min-width: 0; }
.title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-main;
}
.author {
  display: flex;
  align-items: center;
  margin-top: 8rpx;
  font-size: 24rpx;
}
.at { color: $primary; font-weight: 700; margin-right: 4rpx; }
.name { color: $primary; }

.wish-row {
  display: flex;
  justify-content: space-between;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: $text-sub;
}
.progress-track {
  margin-top: 10rpx;
  height: 12rpx;
  border-radius: 999rpx;
  background: #EEEAF8;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 999rpx;
  background: $gradient-primary;
}
.progress-fill.done { background: linear-gradient(135deg, #34D399, #22C55E); }

.actions {
  margin-top: 18rpx;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16rpx;
}
.want-btn {
  min-width: 140rpx;
  text-align: center;
  padding: 10rpx 28rpx;
  border-radius: $radius-pill;
  border: 2rpx solid $primary;
  color: $primary;
  font-size: 24rpx;
  font-weight: 600;
  background: #fff;
}
.price-pill {
  padding: 10rpx 22rpx;
  border-radius: $radius-pill;
  background: #2A2438;
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
}
.buy-btn {
  padding: 10rpx 28rpx;
  border-radius: $radius-pill;
  background: $gradient-primary;
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
</style>
