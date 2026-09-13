<template>
  <view class="page">
    <view class="user-card">
      <view class="user-info" @click="user ? openEditProfile() : handleLogin()">
        <image v-if="user && user.avatarUrl" class="avatar" :src="user.avatarUrl" mode="aspectFill" />
        <image v-else class="avatar" src="/static/icons/user.png" mode="aspectFit" />
        <view class="text-info">
          <view class="nickname">{{ user ? (user.nickName || '微信用户') : '点击登录' }}</view>
          <view class="welcome">{{ user ? '欢迎回来，继续你的祈愿' : '登录同步设计与订单' }}</view>
        </view>
      </view>
      <button v-if="user" class="edit-btn" @click="openEditProfile">编辑资料</button>
      <button v-else class="edit-btn" @click="handleLogin">去登录</button>
    </view>

    <view class="vip-card" @click="onVip">
      <view class="vip-left">
        <text class="vip-title">{{ member ? member.levelName : '会员中心' }}</text>
        <text v-if="member" class="vip-desc">累计消费 ¥{{ member.totalSpent }} · 成长值 {{ member.points }}</text>
        <view v-if="member && member.nextLevelName" class="vip-progress">
          <view class="vip-progress-track">
            <view class="vip-progress-fill" :style="{ width: member.progressPercent + '%' }" />
          </view>
          <text class="vip-progress-text">距 {{ member.nextLevelName }} 还差 ¥{{ member.nextThreshold }}</text>
        </view>
        <view class="vip-btn">{{ user ? '查看会员权益 ›' : '登录查看会员权益 ›' }}</view>
      </view>
      <image class="xy-icon vip-crown" src="/static/icons/crown.png" mode="aspectFit" />
    </view>

    <view class="panel order-panel">
      <view class="panel-header" @click="goAllOrders">
        <text class="panel-title">我的订单</text>
        <view class="panel-more">
          <text>查看全部 ›</text>
        </view>
      </view>
      <view class="order-grid">
        <view class="order-item" @click.stop="goOrders(0)">
          <view class="icon-box">
            <image class="xy-icon ui-icon" src="/static/icons/wallet.png" mode="aspectFit" />
            <view class="badge" v-if="count.s0">{{ count.s0 }}</view>
          </view>
          <text class="order-text">待付款</text>
        </view>
        <view class="order-item" @click.stop="goOrders(1)">
          <view class="icon-box">
            <image class="xy-icon ui-icon" src="/static/icons/package.png" mode="aspectFit" />
            <view class="badge" v-if="count.s1">{{ count.s1 }}</view>
          </view>
          <text class="order-text">待发货</text>
        </view>
        <view class="order-item" @click.stop="goOrders(2)">
          <view class="icon-box">
            <image class="xy-icon ui-icon" src="/static/icons/truck.png" mode="aspectFit" />
            <view class="badge" v-if="count.s2">{{ count.s2 }}</view>
          </view>
          <text class="order-text">待收货</text>
        </view>
        <view class="order-item" @click.stop="goOrders(3)">
          <view class="icon-box">
            <image class="xy-icon ui-icon" src="/static/icons/circle-check.png" mode="aspectFit" />
            <view class="badge" v-if="count.s3">{{ count.s3 }}</view>
          </view>
          <text class="order-text">已完成</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <view class="panel-header">
        <text class="panel-title">创作专区</text>
      </view>
      <view class="order-grid">
        <view class="order-item" @click="goCreator">
          <view class="icon-box soft"><image class="xy-icon ui-icon" src="/static/icons/sparkles.png" mode="aspectFit" /></view>
          <text class="order-text">创作者中心</text>
        </view>
        <view class="order-item" @click="goDesign">
          <view class="icon-box soft"><image class="xy-icon ui-icon" src="/static/icons/palette.png" mode="aspectFit" /></view>
          <text class="order-text">我的设计</text>
        </view>
        <view class="order-item" @click="goCollect">
          <view class="icon-box soft"><image class="xy-icon ui-icon" src="/static/icons/heart.png" mode="aspectFit" /></view>
          <text class="order-text">我的收藏</text>
        </view>
        <view class="order-item" @click="goSquare">
          <view class="icon-box soft"><image class="xy-icon ui-icon" src="/static/icons/layout-grid.png" mode="aspectFit" /></view>
          <text class="order-text">灵感广场</text>
        </view>
      </view>
    </view>

    <view class="panel">
      <view class="panel-header">
        <text class="panel-title">常用功能</text>
      </view>
      <view class="func-grid">
        <view class="func-item" @click="goAddress">
          <view class="func-icon"><image class="xy-icon ui-icon" src="/static/icons/map-pin.png" mode="aspectFit" /></view>
          <text>收货地址</text>
        </view>
        <view class="func-item" @click="contact">
          <view class="func-icon"><image class="xy-icon ui-icon" src="/static/icons/headphones.png" mode="aspectFit" /></view>
          <text>联系客服</text>
        </view>
        <view class="func-item" @click="goSetting">
          <view class="func-icon"><image class="xy-icon ui-icon" src="/static/icons/settings.png" mode="aspectFit" /></view>
          <text>设置</text>
        </view>
        <view class="func-item" @click="about">
          <view class="func-icon"><image class="xy-icon ui-icon" src="/static/icons/info.png" mode="aspectFit" /></view>
          <text>关于我们</text>
        </view>
      </view>
    </view>

    <view class="logout-section" v-if="user">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <view v-if="showQR" class="mask" @click="showQR=false">
      <view class="qr-modal" @click.stop>
        <view class="qr-title">客服微信</view>
        <image class="qr" :src="qrUrl" mode="widthFix" show-menu-by-longpress="true" @click="previewQR" />
        <view class="qr-tips">长按识别二维码添加</view>
        <button class="qr-close" @click="showQR=false">关闭</button>
      </view>
    </view>

    <view v-if="showEditProfile" class="mask" @click="showEditProfile=false">
      <view class="edit-modal" @click.stop>
        <view class="modal-title">编辑资料</view>
        <view class="form-item">
          <view class="label">头像</view>
          <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
            <image class="avatar-preview" :src="tempAvatarUrl || user.avatarUrl || '/static/icons/user.png'" mode="aspectFit" />
            <view class="avatar-tip">点击选择头像</view>
          </button>
        </view>
        <view class="form-item">
          <view class="label">昵称</view>
          <input class="nickname-input" type="nickname" v-model="tempNickName" placeholder="请输入昵称" :maxlength="20" />
        </view>
        <view class="modal-btns">
          <button class="cancel-btn" @click="cancelEdit">取消</button>
          <button class="save-btn" @click="saveProfile">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { orderList, userGet, logout, loginWithWeixinCode, getMemberInfo } from '../../api/index.js'

const count = ref({ s0: 0, s1: 0, s2: 0, s3: 0 })
const showQR = ref(false)
const qrUrl = '/static/CustomerService/714966e4f87775b79a26b9002c0606d1.jpg'
const user = ref(null)
const member = ref(null)
const showEditProfile = ref(false)
const tempAvatarUrl = ref('')
const tempNickName = ref('')

async function loadMember() {
  if (!user.value) {
    member.value = null
    return
  }
  try {
    const info = await getMemberInfo()
    if (info) {
      member.value = {
        ...info,
        totalSpent: Number(info.totalSpent || 0).toFixed(2),
        nextThreshold: info.nextThreshold != null ? Number(info.nextThreshold).toFixed(0) : null
      }
    }
  } catch (e) {
    console.error('加载会员信息失败:', e)
    member.value = null
  }
}

async function loadOrders() {
  if (!user.value) {
    count.value = { s0: 0, s1: 0, s2: 0, s3: 0 }
    return
  }
  try {
    const res = await orderList({ page: 1, size: 100 })
    let orders = []
    if (res && res.orders) orders = res.orders
    else if (res && res.records) orders = res.records
    else if (Array.isArray(res)) orders = res
    else if (res && res.data) orders = res.data.orders || (Array.isArray(res.data) ? res.data : (res.data.records || []))

    count.value = {
      s0: orders.filter(o => o.status === 0).length,
      s1: orders.filter(o => o.status === 1 || o.status === 4).length,
      s2: orders.filter(o => o.status === 2).length,
      s3: orders.filter(o => o.status === 3).length
    }
  } catch (e) {
    console.error('加载订单失败:', e)
  }
}

onMounted(() => {
  user.value = userGet()
  loadOrders()
  loadMember()
})

onShow(() => {
  user.value = userGet()
  setTimeout(() => {
    loadOrders()
    loadMember()
  }, 300)
})

function handleLogin() {
  uni.showLoading({ title: '正在登录...' })
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      uni.getUserProfile({
        desc: '用于完善会员资料',
        success: async (profileRes) => {
          try {
            const res = await loginWithWeixinCode(loginRes.code, profileRes.userInfo)
            if (res && res.token) {
              user.value = {
                id: res.id,
                nickName: res.nickname,
                avatarUrl: res.avatar,
                openid: res.openid
              }
              uni.showToast({ title: '登录成功', icon: 'success' })
              loadOrders()
              loadMember()
            } else {
              throw new Error(res.msg || '登录失败')
            }
          } catch (e) {
            uni.showToast({ title: '登录失败: ' + (e.message || '未知错误'), icon: 'none' })
          } finally {
            uni.hideLoading()
          }
        },
        fail: () => {
          uni.hideLoading()
          uni.showToast({ title: '用户取消授权', icon: 'none' })
        }
      })
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({ title: '启动登录失败', icon: 'none' })
    }
  })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    confirmColor: '#6B4EFF',
    success: (res) => {
      if (res.confirm) {
        logout()
        user.value = null
        count.value = { s0: 0, s1: 0, s2: 0, s3: 0 }
        uni.showToast({ title: '已退出', icon: 'none' })
      }
    }
  })
}

function openEditProfile() {
  if (!user.value) return handleLogin()
  tempAvatarUrl.value = user.value.avatarUrl || ''
  tempNickName.value = user.value.nickName || ''
  showEditProfile.value = true
}

function cancelEdit() {
  showEditProfile.value = false
}

function onChooseAvatar(e) {
  tempAvatarUrl.value = e.detail.avatarUrl
}

function saveProfile() {
  const updatedUser = {
    ...user.value,
    nickName: tempNickName.value || user.value.nickName,
    avatarUrl: tempAvatarUrl.value || user.value.avatarUrl
  }
  try {
    uni.setStorageSync('user', updatedUser)
    user.value = updatedUser
    uni.showToast({ title: '保存成功', icon: 'success' })
    showEditProfile.value = false
  } catch (e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}

function goOrders(status) {
  uni.navigateTo({ url: `/pages/order/list?status=${status}` })
}
function goAllOrders() {
  uni.navigateTo({ url: '/pages/order/list' })
}
function goAddress() {
  uni.navigateTo({ url: '/pages/address/list' })
}
function goSetting() {
  uni.navigateTo({ url: '/pages/setting/index' })
}
function about() {
  uni.navigateTo({ url: '/pages/about/index' })
}
function contact() {
  showQR.value = true
}
function previewQR() {
  uni.previewImage({ urls: [qrUrl] })
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
function goCreator() {
  uni.navigateTo({ url: '/pages/creator/index' })
}
function onVip() {
  if (!user.value) return handleLogin()
  const m = member.value
  if (!m) {
    uni.showToast({ title: '会员信息加载中', icon: 'none' })
    return
  }
  const benefits = [
    '普通会员：基础购物权益',
    '银卡会员：满 ¥500 · 专属客服',
    '金卡会员：满 ¥2000 · 优先发货',
    '黑钻会员：满 ¥5000 · 生日礼遇 + 新品优先'
  ]
  const tip = m.nextLevelName
    ? `当前：${m.levelName}（成长值 ${m.points}）\n距 ${m.nextLevelName} 还需累计消费 ¥${m.nextThreshold}`
    : `当前：${m.levelName}（已是最高等级，成长值 ${m.points}）`
  uni.showModal({
    title: '会员权益',
    content: `${tip}\n\n${benefits.join('\n')}`,
    showCancel: false,
    confirmColor: '#8B5CF6',
    confirmText: '知道了'
  })
}
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  padding: 24rpx 28rpx 160rpx;
  background:
    radial-gradient(ellipse 80% 40% at 10% -10%, rgba(183, 148, 255, 0.28), transparent 55%),
    radial-gradient(ellipse 70% 35% at 95% 8%, rgba(221, 200, 255, 0.45), transparent 50%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 38%, #FBF7FF 100%);
  min-height: 100vh;
  box-sizing: border-box;
}

.user-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: transparent;
  border-radius: 32rpx;
  padding: 20rpx 8rpx 28rpx;
  margin-bottom: 8rpx;
  box-shadow: none;
}
.user-info { display: flex; align-items: center; gap: 22rpx; }
.avatar {
  width: 110rpx;
  height: 110rpx;
  background: #fff;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.55);
}
.nickname { font-size: 34rpx; font-weight: 700; color: $text-main; margin-bottom: 8rpx; }
.welcome { font-size: 24rpx; color: $text-sub; }
.edit-btn {
  margin: 0;
  padding: 0 24rpx;
  height: 56rpx;
  line-height: 56rpx;
  background: #fff;
  color: $primary;
  font-size: 24rpx;
  font-weight: 600;
  border-radius: 28rpx;
  border: 1rpx solid rgba(139, 92, 246, 0.18);
  box-shadow: 0 6rpx 16rpx rgba(139, 92, 246, 0.08);
}
.edit-btn::after { border: none; }

.vip-card {
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $gradient-vip;
  border-radius: 28rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 20rpx;
  box-shadow: $shadow-card;
  border: 1rpx solid rgba(139, 92, 246, 0.12);
}
.vip-card::after {
  content: '';
  position: absolute;
  right: -20rpx;
  top: -30rpx;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(183, 148, 255, 0.45) 0%, transparent 70%);
  pointer-events: none;
}
.vip-left { position: relative; z-index: 1; }
.vip-title { display: block; font-size: 30rpx; font-weight: 700; color: $text-on-soft; margin-bottom: 10rpx; }
.vip-desc { display: block; font-size: 22rpx; color: rgba(74, 58, 120, 0.7); margin-bottom: 12rpx; }
.vip-progress { margin-bottom: 14rpx; }
.vip-progress-track {
  width: 320rpx;
  max-width: 60vw;
  height: 10rpx;
  border-radius: 999rpx;
  background: rgba(139, 92, 246, 0.15);
  overflow: hidden;
}
.vip-progress-fill {
  height: 100%;
  border-radius: 999rpx;
  background: $gradient-primary;
}
.vip-progress-text { display: block; margin-top: 6rpx; font-size: 20rpx; color: rgba(74, 58, 120, 0.6); }
.vip-btn {
  display: inline-flex;
  padding: 10rpx 22rpx;
  border-radius: $radius-pill;
  background: $gradient-primary;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
.vip-card .xy-icon,
.vip-crown {
  position: relative;
  z-index: 1;
  opacity: 0.9;
  width: 56rpx;
  height: 56rpx;
}
.ui-icon {
  width: 46rpx;
  height: 46rpx;
  display: block;
}
.icon-box {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: $icon-orb;
  color: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: inset 0 0 0 1rpx rgba(139, 92, 246, 0.06);
}
.icon-box.soft {
  background: radial-gradient(circle at 40% 35%, #FBF7FF 0%, #F0E6FF 50%, #E6D8FF 100%);
}
.order-text { font-size: 22rpx; color: $text-sub; }

.panel {
  background: $card-bg;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  margin-bottom: 20rpx;
  padding: 24rpx;
  border: 1rpx solid $border-soft;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}
.panel-title { font-size: 30rpx; font-weight: 700; color: $text-main; }
.panel-more { display: flex; align-items: center; color: $text-sub; font-size: 24rpx; gap: 4rpx; }

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}
.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  border-radius: 999rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 18rpx;
  line-height: 32rpx;
  text-align: center;
  border: 2rpx solid #fff;
}

.func-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18rpx;
}
.func-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  font-size: 22rpx;
  color: $text-sub;
}
.func-icon {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: $icon-orb;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 1rpx rgba(139, 92, 246, 0.06);
}

.logout-section { margin-top: 12rpx; }
.logout-btn {
  background: #fff;
  color: #FF5722;
  border-radius: $radius-pill;
  border: 1rpx solid rgba(255, 87, 34, 0.2);
}
.logout-btn::after { border: none; }

.mask {
  position: fixed;
  inset: 0;
  background: rgba(20, 12, 40, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.qr-modal, .edit-modal {
  width: 80%;
  background: #fff;
  border-radius: 28rpx;
  padding: 28rpx;
}
.qr-title, .modal-title {
  text-align: center;
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 20rpx;
}
.qr { width: 100%; border-radius: 16rpx; }
.qr-tips { text-align: center; color: $text-sub; font-size: 24rpx; margin: 16rpx 0; }
.qr-close, .save-btn {
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  font-weight: 600;
}
.qr-close::after, .save-btn::after, .cancel-btn::after, .avatar-btn::after { border: none; }
.form-item { margin-bottom: 22rpx; }
.label { font-size: 26rpx; color: $text-sub; margin-bottom: 10rpx; }
.avatar-btn {
  margin: 0;
  padding: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-preview { width: 120rpx; height: 120rpx; border-radius: 50%; background: $primary-soft; }
.avatar-tip { margin-top: 8rpx; font-size: 22rpx; color: $text-hint; }
.nickname-input {
  background: #F7F4FC;
  border-radius: 16rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
}
.modal-btns { display: flex; gap: 16rpx; }
.cancel-btn, .save-btn { flex: 1; }
.cancel-btn { background: #F3EEFF; color: $primary; border-radius: $radius-pill; }
</style>
