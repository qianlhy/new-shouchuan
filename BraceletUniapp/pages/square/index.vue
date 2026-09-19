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
import { computed, ref, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getProductList, getWishCounts, getMyWishes, toggleWish, isLoggedIn } from '../../api/index.js'
import { isAuthError } from '../../api/request.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'
import { setTabBarSelected } from '../../utils/tabbar.js'

const tabs = [
  { key: 'latest', label: '最新' },
  { key: 'hot', label: '热门' },
  { key: 'designer', label: '设计师款' }
]

const activeTab = ref('latest')
const loading = ref(false)
const products = ref([])
const wantedIds = ref([])          // 当前用户已想要的商品ID
// 众筹进度：{ [productId]: { wantCount, goalCount, achieved } }
const wishMap = reactive({})

const displayList = computed(() => {
  let list = [...products.value]
  if (activeTab.value === 'hot') {
    // 热门：按想要人数排序
    list.sort((a, b) => (wishMap[b.id]?.wantCount || 0) - (wishMap[a.id]?.wantCount || 0))
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
  const wish = wishMap[p.id] || {}
  const goal = wish.goalCount || 100
  const wantCount = wish.wantCount || 0
  const achieved = !!wish.achieved
  const progress = achieved ? 100 : Math.min(99, Math.round((wantCount / goal) * 100))
  return {
    id: p.id,
    title: p.name || p.title || '未命名手作',
    author: '祈愿手作',
    imageUrl: resolveImageUrl(p.image || p.imageUrl || p.coverImage || ''),
    price: Number(p.price || 0).toFixed(2),
    achieved,
    progressPercent: progress,
    progressText: achieved ? `${goal}/${goal}` : `${wantCount}/${goal}`,
    raw: p
  }
}

async function loadWishData(ids) {
  // 公开：批量众筹进度
  try {
    const counts = await getWishCounts(ids)
    counts.forEach((c) => {
      wishMap[c.productId] = {
        wantCount: c.wantCount || 0,
        goalCount: c.goalCount || 100,
        achieved: !!c.achieved
      }
    })
  } catch (e) {
    console.error('众筹进度加载失败', e)
  }
  // 登录用户：我想要的列表，用于标记按钮态
  if (isLoggedIn()) {
    try {
      const mine = await getMyWishes()
      wantedIds.value = mine.map((m) => m.id)
    } catch (e) {
      console.error('我的想要加载失败', e)
    }
  } else {
    wantedIds.value = []
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const list = await getProductList(0, true)
    products.value = Array.isArray(list) ? list : []
    const ids = products.value.map((p) => p.id).filter(Boolean)
    await loadWishData(ids)
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

async function toggleWant(item) {
  if (!isLoggedIn()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index?login=1' }), 1200)
    return
  }
  try {
    const res = await toggleWish(item.id)
    // 用后端返回的真实进度更新
    wishMap[item.id] = {
      wantCount: res.wantCount || 0,
      goalCount: res.goalCount || 100,
      achieved: !!res.achieved
    }
    const idx = wantedIds.value.indexOf(item.id)
    if (res.wanted) {
      if (idx < 0) wantedIds.value.push(item.id)
      uni.showToast({ title: '已加入心愿', icon: 'none' })
    } else {
      if (idx >= 0) wantedIds.value.splice(idx, 1)
      uni.showToast({ title: '已取消心愿', icon: 'none' })
    }
  } catch (e) {
    console.error('操作失败', e)
    if (e.authExpired || isAuthError(e)) return
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

onShow(() => {
  setTabBarSelected(1)
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
