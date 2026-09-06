<template>
  <view class="page">
    <view v-if="!logged" class="state">
      <image class="state-icon" src="/static/icons/heart.png" mode="aspectFit" />
      <text class="state-text">登录后查看你的心愿收藏</text>
      <button class="primary-btn" @click="goLogin">去登录</button>
    </view>

    <template v-else>
      <view v-if="loading" class="state"><text class="state-text">加载中...</text></view>

      <view v-else-if="!list.length" class="state">
        <image class="state-icon" src="/static/icons/heart.png" mode="aspectFit" />
        <text class="state-text">还没有收藏，去广场种草吧</text>
        <button class="primary-btn" @click="goSquare">逛逛灵感广场</button>
      </view>

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
            <view class="wish-row">
              <text class="wish-label">{{ item.achieved ? '心愿已达成' : '心愿进度' }}</text>
              <text class="wish-num">{{ item.wantCount }}/{{ item.goalCount }}</text>
            </view>
            <view class="progress-track">
              <view
                class="progress-fill"
                :class="{ done: item.achieved }"
                :style="{ width: item.percent + '%' }"
              />
            </view>
            <view class="bottom">
              <text class="price">¥{{ item.price }}</text>
              <view class="btn" @click.stop="goDetail(item)">{{ item.achieved ? '去购买' : '查看' }}</view>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyWishes, isLoggedIn } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'

const logged = ref(false)
const loading = ref(false)
const list = ref([])

async function load() {
  logged.value = isLoggedIn()
  if (!logged.value) return
  loading.value = true
  try {
    const rows = await getMyWishes()
    list.value = (Array.isArray(rows) ? rows : []).map((r) => {
      const goal = r.goalCount || 100
      const want = r.wantCount || 0
      const achieved = !!r.achieved
      return {
        id: r.id,
        title: r.title || '未命名手作',
        imageUrl: resolveImageUrl(r.coverImage || ''),
        price: Number(r.price || 0).toFixed(2),
        wantCount: want,
        goalCount: goal,
        achieved,
        percent: achieved ? 100 : Math.min(99, Math.round((want / goal) * 100))
      }
    })
  } catch (e) {
    console.error('加载收藏失败', e)
    list.value = []
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}
function goSquare() {
  uni.switchTab({ url: '/pages/square/index' })
}
function goLogin() {
  uni.reLaunch({ url: '/pages/index/index?login=1' })
}

onShow(load)
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx 28rpx 60rpx;
  background:
    radial-gradient(ellipse 80% 40% at 15% -5%, rgba(183, 148, 255, 0.26), transparent 55%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 30%, #FBF7FF 100%);
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140rpx 0;
}
.state-icon { width: 96rpx; height: 96rpx; margin-bottom: 24rpx; opacity: 0.85; }
.state-text { font-size: 28rpx; color: $text-sub; margin-bottom: 32rpx; }
.primary-btn {
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  padding: 0 56rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
  border: none;
}
.primary-btn::after { border: none; }

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
  border-radius: 24rpx;
  background: $primary-soft;
  flex-shrink: 0;
}
.meta { flex: 1; min-width: 0; }
.title { font-size: 30rpx; font-weight: 700; color: $text-main; }
.wish-row {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
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
.progress-fill { height: 100%; border-radius: 999rpx; background: $gradient-primary; }
.progress-fill.done { background: linear-gradient(135deg, #34D399, #22C55E); }
.bottom {
  margin-top: 18rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price { color: $primary; font-weight: 700; font-size: 30rpx; }
.btn {
  padding: 8rpx 28rpx;
  border-radius: $radius-pill;
  background: $gradient-primary;
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
</style>
