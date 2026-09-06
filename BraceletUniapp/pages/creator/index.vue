<template>
  <view class="page">
    <!-- 创作者头部卡 -->
    <view class="hero">
      <view class="hero-deco" />
      <view class="hero-content">
        <text class="hero-en">CREATOR CENTER</text>
        <text class="hero-title">创作者中心</text>
        <text class="hero-sub">{{ nickname }}，把灵感变成手作</text>
      </view>
      <image class="hero-icon" src="/static/icons/sparkles.png" mode="aspectFit" />
    </view>

    <!-- 数据统计 -->
    <view class="stat-row">
      <view class="stat">
        <text class="stat-num">{{ stats.collect }}</text>
        <text class="stat-label">我的收藏</text>
      </view>
      <view class="stat">
        <text class="stat-num">{{ stats.orders }}</text>
        <text class="stat-label">有效订单</text>
      </view>
      <view class="stat">
        <text class="stat-num">{{ stats.points }}</text>
        <text class="stat-label">成长值</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="panel">
      <view class="panel-title">开始创作</view>
      <view class="entry" @click="goDesign">
        <view class="entry-icon"><image src="/static/icons/palette.png" mode="aspectFit" /></view>
        <view class="entry-text">
          <text class="entry-name">DIY 设计手串</text>
          <text class="entry-desc">一颗一颗，拼出专属灵感</text>
        </view>
        <text class="entry-arrow">›</text>
      </view>
      <view class="entry" @click="goSquare">
        <view class="entry-icon"><image src="/static/icons/layout-grid.png" mode="aspectFit" /></view>
        <view class="entry-text">
          <text class="entry-name">灵感广场</text>
          <text class="entry-desc">发现心动，为心愿助力</text>
        </view>
        <text class="entry-arrow">›</text>
      </view>
      <view class="entry" @click="goCollect">
        <view class="entry-icon"><image src="/static/icons/heart.png" mode="aspectFit" /></view>
        <view class="entry-text">
          <text class="entry-name">我的收藏</text>
          <text class="entry-desc">查看你许下的心愿</text>
        </view>
        <text class="entry-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMyWishes, getMemberInfo, userGet, isLoggedIn } from '../../api/index.js'

const nickname = ref('创作者')
const stats = ref({ collect: 0, orders: 0, points: 0 })

async function load() {
  const u = userGet()
  nickname.value = (u && u.nickName) || '创作者'
  if (!isLoggedIn()) {
    stats.value = { collect: 0, orders: 0, points: 0 }
    return
  }
  try {
    const [wishes, member] = await Promise.all([
      getMyWishes().catch(() => []),
      getMemberInfo().catch(() => null)
    ])
    stats.value = {
      collect: Array.isArray(wishes) ? wishes.length : 0,
      orders: member ? (member.orderCount || 0) : 0,
      points: member ? (member.points || 0) : 0
    }
  } catch (e) {
    console.error('创作者中心加载失败', e)
  }
}

function goDesign() {
  uni.switchTab({ url: '/pages/design/index' })
}
function goSquare() {
  uni.switchTab({ url: '/pages/square/index' })
}
function goCollect() {
  uni.navigateTo({ url: '/pages/collect/index' })
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
    radial-gradient(ellipse 85% 42% at 0% 0%, rgba(183, 148, 255, 0.26), transparent 55%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 32%, #FBF7FF 100%);
}

.hero {
  position: relative;
  overflow: hidden;
  border-radius: 32rpx;
  padding: 44rpx 40rpx;
  background: $gradient-banner;
  box-shadow: $shadow-card;
  margin-bottom: 20rpx;
}
.hero-deco {
  position: absolute;
  right: -40rpx;
  top: -50rpx;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(183, 148, 255, 0.5) 0%, transparent 72%);
}
.hero-content { position: relative; z-index: 1; }
.hero-en { display: block; font-size: 20rpx; letter-spacing: 2rpx; color: $primary-light; font-weight: 600; margin-bottom: 8rpx; }
.hero-title { display: block; font-size: 44rpx; font-weight: 700; color: $text-on-soft; letter-spacing: 2rpx; }
.hero-sub { display: block; margin-top: 12rpx; font-size: 24rpx; color: rgba(74, 58, 120, 0.68); }
.hero-icon { position: absolute; right: 44rpx; top: 48rpx; width: 52rpx; height: 52rpx; opacity: 0.85; z-index: 1; }

.stat-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.stat {
  flex: 1;
  background: $card-bg;
  border-radius: 24rpx;
  padding: 26rpx 0;
  text-align: center;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-soft;
}
.stat-num { display: block; font-size: 40rpx; font-weight: 700; color: $primary; }
.stat-label { display: block; margin-top: 6rpx; font-size: 22rpx; color: $text-sub; }

.panel {
  background: $card-bg;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-soft;
  padding: 24rpx;
}
.panel-title { font-size: 30rpx; font-weight: 700; color: $text-main; margin-bottom: 16rpx; }
.entry {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 22rpx 8rpx;
  border-bottom: 1rpx solid rgba(139, 92, 246, 0.08);
}
.entry:last-child { border-bottom: none; }
.entry-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 35%, #F8F3FF 0%, #EDE4FF 55%, #E4D6FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.entry-icon image { width: 40rpx; height: 40rpx; }
.entry-text { flex: 1; min-width: 0; }
.entry-name { display: block; font-size: 28rpx; font-weight: 600; color: $text-main; }
.entry-desc { display: block; margin-top: 6rpx; font-size: 22rpx; color: $text-sub; }
.entry-arrow { font-size: 40rpx; color: $text-hint; }
</style>
