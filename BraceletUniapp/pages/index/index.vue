<template>
  <view class="page">
    <!-- 购买须知：撞色紫底（对标灵感广场，内容直接落在紫底上） -->
    <view class="guide-wrap">
      <view class="guide-deco d1" />
      <view class="guide-deco d2" />
      <view class="guide-deco d3" />
      <view class="guide-deco d4" />
      <view class="guide-inner">
        <view class="guide-head">
          <view>
            <text class="guide-title">水晶购买须知</text>
            <text class="guide-sub">买前必看 · CRYSTAL GUIDE</text>
          </view>
          <text class="guide-more" @click="goAbout">全部 ›</text>
        </view>
        <view class="guide-grid">
          <view class="guide-item" @click="goAbout">
            <view class="guide-icon"><image class="xy-icon" src="/static/icons/shield-check-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
            <text>售后范围</text>
          </view>
          <view class="guide-item" @click="goAbout">
            <view class="guide-icon"><image class="xy-icon" src="/static/icons/truck-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
            <text>物流运输</text>
          </view>
          <view class="guide-item" @click="goAbout">
            <view class="guide-icon"><image class="xy-icon" src="/static/icons/gem-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
            <text>天然材质</text>
          </view>
          <view class="guide-item" @click="goAbout">
            <view class="guide-icon"><image class="xy-icon" src="/static/icons/circle-help-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
            <text>常见问题</text>
          </view>
        </view>
        <text class="guide-note">注：购买前请先阅读相关须知内容</text>
      </view>
    </view>

    <!-- 中部入口 -->
    <view class="mid-row">
      <view class="mid-item" @click="onLuckyDraw">
        <view class="mid-icon"><image class="xy-icon" src="/static/icons/gift-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
        <text>幸运抽奖</text>
      </view>
      <view class="mid-item" @click="goService">
        <view class="mid-icon"><image class="xy-icon" src="/static/icons/message-circle-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
        <text>联系客服</text>
      </view>
      <view class="mid-item" @click="goDesign">
        <view class="mid-icon"><image class="xy-icon" src="/static/icons/palette-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
        <text>我的设计</text>
      </view>
    </view>

    <!-- DIY / 商城 -->
    <view class="action-section">
      <view class="action-card diy-card" @click="goDesign">
        <view class="card-badge">DIY-CUSTOM</view>
        <text class="card-title">设计手串</text>
        <text class="card-subtitle">一颗一颗，拼出专属灵感</text>
        <view class="card-orb" />
      </view>
      <view class="action-card select-card" @click="goProductList">
        <view class="card-badge">MUST-HAVE</view>
        <text class="card-title">优选商城</text>
        <text class="card-subtitle">祈愿严选成品手串</text>
        <view class="cart-glow">
          <image class="cart-glow-icon" src="/static/icons/shopping-cart-white.png" mode="aspectFit" />
        </view>
      </view>
    </view>

    <!-- 轮播（有数据时展示） -->
    <view v-if="banners.length" class="banner-section">
      <swiper
        class="banner-swiper"
        circular
        :indicator-dots="true"
        :autoplay="true"
        :interval="4000"
        :duration="500"
        indicator-active-color="#8B5CF6"
        indicator-color="rgba(139,92,246,0.25)"
      >
        <swiper-item v-for="(item, index) in banners" :key="item.id || index">
          <image
            class="banner-image"
            :src="item.imageUrl"
            mode="aspectFill"
            @click="onBannerClick(item)"
          />
        </swiper-item>
      </swiper>
    </view>

    <!-- 推荐设计 -->
    <view class="recommend">
      <view class="rec-head">
        <text class="rec-title">推荐设计</text>
        <view class="rec-refresh" @click="loadRecommend">
          <image class="xy-icon" src="/static/icons/refresh-cw-purple.png" mode="aspectFit" style="width:40rpx;height:40rpx" />
          <text>刷新</text>
        </view>
      </view>

      <view v-if="!recommends.length" class="rec-empty">暂无推荐，先去广场逛逛吧</view>

      <view
        v-for="item in recommends"
        :key="item.id"
        class="rec-card"
        @click="goDetail(item)"
      >
        <view class="rec-info">
          <text class="rec-name">{{ item.name || item.title }}</text>
          <view class="rec-author">
            <text class="at">@</text>
            <text>祈愿手作</text>
          </view>
          <view class="rec-btn" @click.stop="goDetail(item)">查看实物</view>
        </view>
        <image class="rec-img" :src="item.imageUrl" mode="aspectFill" />
      </view>
    </view>

    <view class="footer">
      <text class="footer-text">祈愿手作 · 匠心定制</text>
    </view>

    <!-- 登录弹窗 -->
    <view v-if="showEditProfile" class="overlay">
      <view class="edit-popup">
        <text class="popup-title">完善个人资料</text>
        <view class="form-item">
          <text class="form-label">头像</text>
          <button class="avatar-wrapper" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
            <image class="avatar-preview" :src="tempAvatarUrl || '/static/tabbar/mine.png'" mode="aspectFill" />
            <text class="avatar-tip">点击更换</text>
          </button>
        </view>
        <view class="form-item">
          <text class="form-label">昵称</text>
          <input type="nickname" class="nickname-input" v-model="tempNickname" placeholder="请输入昵称" @blur="onNicknameBlur" />
        </view>
        <button class="confirm-btn" @click="confirmLogin">确认登录</button>
        <text class="skip-btn" @click="skipLogin">跳过，使用默认</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBannerList, getProductList, userGet, userSet, loginWithWeixinCode } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'
import { setTabBarSelected } from '../../utils/tabbar.js'

const banners = ref([])
const recommends = ref([])
const user = ref(null)
const showEditProfile = ref(false)
const tempAvatarUrl = ref('')
const tempNickname = ref('')

const loadBanners = async () => {
  try {
    const list = await getBannerList()
    if (Array.isArray(list)) {
      banners.value = list.map((item) => ({
        ...item,
        imageUrl: resolveImageUrl(item.imageUrl || '')
      }))
    }
  } catch (e) {
    console.error('获取轮播图失败', e)
  }
}

const loadRecommend = async () => {
  try {
    const list = await getProductList(0, true)
    const arr = Array.isArray(list) ? list : []
    recommends.value = arr.slice(0, 4).map((p) => ({
      ...p,
      imageUrl: resolveImageUrl(p.image || p.imageUrl || p.coverImage || '')
    }))
  } catch (e) {
    console.error('推荐加载失败', e)
    recommends.value = []
  }
}

const checkLogin = () => {
  user.value = userGet()
}

const handleUserClick = () => {
  if (!user.value) {
    showEditProfile.value = true
    tempNickname.value = '微信用户'
    tempAvatarUrl.value = ''
  }
}

const onChooseAvatar = (e) => {
  tempAvatarUrl.value = e.detail.avatarUrl
}

const onNicknameBlur = (e) => {
  tempNickname.value = e.detail.value
}

const confirmLogin = () => {
  performLogin({
    nickName: tempNickname.value || '微信用户',
    avatarUrl: tempAvatarUrl.value || ''
  })
}

const skipLogin = () => {
  performLogin({ nickName: '微信用户', avatarUrl: '' })
}

const performLogin = (userInfo) => {
  showEditProfile.value = false
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      if (loginRes.code) {
        try {
          const res = await loginWithWeixinCode(loginRes.code, userInfo)
          user.value = userGet() || {
            nickName: (res && res.nickname) || userInfo.nickName,
            avatarUrl: (res && res.avatar) || userInfo.avatarUrl
          }
          uni.showToast({ title: '登录成功', icon: 'success' })
        } catch (e) {
          console.error('后端登录失败', e)
          userSet(userInfo)
          user.value = userInfo
          uni.showToast({ title: '登录异常，请重试', icon: 'none' })
        }
      }
    }
  })
}

const onBannerClick = (item) => {
  if (!item.link) return
  const tabbarPages = [
    '/pages/index/index',
    '/pages/square/index',
    '/pages/design/index',
    '/pages/cart/index',
    '/pages/mine/index'
  ]
  if (tabbarPages.includes(item.link)) {
    uni.switchTab({ url: item.link })
  } else {
    uni.navigateTo({ url: item.link })
  }
}

const goDesign = () => uni.switchTab({ url: '/pages/design/index' })
const goProductList = () => uni.navigateTo({ url: '/pages/product/list' })
const goAbout = () => uni.navigateTo({ url: '/pages/about/index' })
const goService = () => uni.navigateTo({ url: '/pages/customer-service/index' })
const goDetail = (item) => uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })

const onLuckyDraw = () => {
  uni.showToast({ title: '幸运抽奖即将开放', icon: 'none' })
}

onShow(() => {
  setTabBarSelected(0)
  checkLogin()
  setTimeout(checkLogin, 1000)
})

onMounted(() => {
  loadBanners()
  loadRecommend()
  checkLogin()
  // 兼容旧入口 ?login=1
  const pages = getCurrentPages()
  const cur = pages[pages.length - 1]
  if (cur && cur.options && cur.options.login === '1') {
    handleUserClick()
  }
})
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  min-height: 100vh;
  background:
    $page-mist,
    linear-gradient(180deg, $page-bg 0%, $page-bg-deep 36%, $page-bg 100%);
  padding: 24rpx 28rpx 160rpx;
  box-sizing: border-box;
}

.guide-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 32rpx;
  padding: 36rpx 28rpx 28rpx;
  margin-bottom: 20rpx;
  background: $gradient-banner;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-glass;
}
.guide-deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}
.guide-wrap .d1 {
  width: 240rpx;
  height: 240rpx;
  right: -50rpx;
  top: -60rpx;
  background: radial-gradient(circle, rgba(255, 230, 250, 0.45) 0%, rgba(183, 148, 255, 0.35) 40%, transparent 72%);
}
.guide-wrap .d2 {
  width: 140rpx;
  height: 140rpx;
  right: 80rpx;
  bottom: -40rpx;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.16) 0%, transparent 70%);
}
.guide-wrap .d3 {
  width: 56rpx;
  height: 56rpx;
  left: 36rpx;
  bottom: 36rpx;
  background: rgba(255, 255, 255, 0.55);
}
.guide-wrap .d4 {
  width: 90rpx;
  height: 90rpx;
  left: -20rpx;
  top: 20rpx;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.7) 0%, transparent 70%);
}

.guide-inner {
  position: relative;
  z-index: 1;
}
.guide-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 22rpx;
}
.guide-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: $text-on-soft;
  letter-spacing: 1rpx;
}
.guide-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: rgba(74, 58, 120, 0.55);
  letter-spacing: 1rpx;
}
.guide-more {
  font-size: 24rpx;
  color: $primary;
  font-weight: 600;
  padding-top: 6rpx;
}
.guide-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}
.guide-item {
  background: rgba(255, 255, 255, 0.88);
  border-radius: 20rpx;
  padding: 22rpx;
  display: flex;
  align-items: center;
  gap: 14rpx;
  font-size: 26rpx;
  color: $text-main;
  border: 1rpx solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 6rpx 16rpx rgba(90, 50, 160, 0.06);
}
.guide-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  background: $icon-orb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.7);
}
.ui-icon {
  width: 42rpx;
  height: 42rpx;
}
.guide-note {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: rgba(74, 58, 120, 0.55);
  text-align: center;
}

.mid-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.mid-item {
  flex: 1;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24rpx;
  padding: 22rpx 10rpx;
  text-align: center;
  box-shadow: $shadow-card-soft;
  border: 1rpx solid $border-glass;
  font-size: 24rpx;
  color: $text-main;
}
.mid-icon {
  width: 60rpx;
  height: 60rpx;
  margin: 0 auto 10rpx;
  border-radius: 50%;
  background: $icon-orb;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 1rpx rgba(255, 255, 255, 0.7);
}
.mid-icon .ui-icon {
  width: 42rpx;
  height: 42rpx;
}

.action-section {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.action-card {
  flex: 1;
  position: relative;
  overflow: hidden;
  min-height: 220rpx;
  border-radius: 28rpx;
  padding: 28rpx 24rpx;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-glass;
}
/* 撞色：左浅紫实心块 / 右白底 */
.diy-card { background: $gradient-diy; }
.select-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid $border-glass;
}
.card-badge {
  font-size: 18rpx;
  color: rgba(74, 58, 120, 0.5);
  letter-spacing: 1rpx;
  margin-bottom: 12rpx;
}
.card-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: $text-on-soft;
}
.card-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: rgba(74, 58, 120, 0.65);
  max-width: 78%;
}
.card-orb {
  position: absolute;
  right: -20rpx;
  bottom: -30rpx;
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 230, 250, 0.4) 0%, rgba(183, 148, 255, 0.35) 40%, transparent 72%);
}
.cart-glow {
  position: absolute;
  right: 18rpx;
  bottom: 18rpx;
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  background: $gradient-primary;
  box-shadow: 0 12rpx 28rpx rgba(139, 92, 246, 0.38);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cart-glow::before {
  content: '';
  position: absolute;
  inset: -18rpx;
  border-radius: 36rpx;
  background: radial-gradient(circle, rgba(183, 148, 255, 0.45) 0%, transparent 70%);
  z-index: 0;
}
.cart-glow-icon {
  width: 44rpx;
  height: 44rpx;
  position: relative;
  z-index: 1;
}

.banner-section { margin-bottom: 24rpx; }
.banner-swiper {
  height: 280rpx;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: $shadow-card;
}
.banner-image { width: 100%; height: 100%; }

.recommend { margin-top: 8rpx; }
.rec-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.rec-title { font-size: 32rpx; font-weight: 700; color: $text-main; }
.rec-refresh {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: $primary;
}
.refresh-icon {
  width: 40rpx;
  height: 40rpx;
}
.rec-empty {
  text-align: center;
  color: $text-sub;
  padding: 40rpx 0;
  font-size: 26rpx;
}
.rec-card {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.94);
  border-radius: $radius-card;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: $shadow-card-soft;
  border: 1rpx solid $border-glass;
}
.rec-name {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: $text-main;
}
.rec-author {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: $text-sub;
}
.at {
  color: $primary;
  font-weight: 700;
  margin-right: 4rpx;
}
.rec-btn {
  display: inline-flex;
  margin-top: 24rpx;
  padding: 10rpx 26rpx;
  border-radius: $radius-pill;
  background: $gradient-primary;
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
.rec-img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 24rpx;
  background: $primary-soft;
  flex-shrink: 0;
}

.footer {
  margin-top: 40rpx;
  text-align: center;
}
.footer-text { font-size: 22rpx; color: $text-hint; }

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(20, 12, 40, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.edit-popup {
  width: 80%;
  background: #fff;
  border-radius: 28rpx;
  padding: 36rpx;
}
.popup-title {
  display: block;
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 24rpx;
}
.form-item { margin-bottom: 24rpx; }
.form-label { display: block; font-size: 26rpx; color: $text-sub; margin-bottom: 12rpx; }
.avatar-wrapper {
  margin: 0;
  padding: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-wrapper::after { border: none; }
.avatar-preview {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: $primary-soft;
}
.avatar-tip { margin-top: 8rpx; font-size: 22rpx; color: $text-hint; }
.nickname-input {
  background: #F7F4FC;
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
}
.confirm-btn {
  margin-top: 8rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
.confirm-btn::after { border: none; }
.skip-btn {
  display: block;
  text-align: center;
  margin-top: 18rpx;
  color: $text-sub;
  font-size: 24rpx;
}
</style>
