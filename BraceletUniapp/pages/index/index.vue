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
      <view class="mid-item" @click="openWristGuide">
        <view class="mid-icon"><image class="xy-icon" src="/static/icons/info-purple.png" mode="aspectFit" style="width:42rpx;height:42rpx" /></view>
        <text>手围测算</text>
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

    <!-- 手围测算弹层 -->
    <view v-if="showWristGuide" class="wrist-mask" @click="closeWristGuide">
      <view class="wrist-sheet" @click.stop>
        <view class="wrist-handle" />
        <view class="wrist-head">
          <view>
            <text class="wrist-title">手围测算</text>
            <text class="wrist-sub">选对尺寸 · 佩戴更舒适</text>
          </view>
          <view class="wrist-close" @click="closeWristGuide">✕</view>
        </view>

        <view class="wrist-tabs">
          <view
            class="wrist-tab"
            :class="{ active: wristTab === 'estimate' }"
            @click="wristTab = 'estimate'"
          >智能估算</view>
          <view
            class="wrist-tab"
            :class="{ active: wristTab === 'measure' }"
            @click="wristTab = 'measure'"
          >精准测量</view>
        </view>

        <scroll-view scroll-y class="wrist-body">
          <!-- 智能估算 -->
          <view v-if="wristTab === 'estimate'" class="wrist-panel">
            <view class="wrist-tip-banner">
              <text class="tip-strong">量尺不方便？用身高体重快速估算</text>
              <text class="tip-light">仅供参考，建议最终以净手围为准</text>
            </view>

            <view class="est-card">
              <view class="est-field">
                <text class="est-label">身高</text>
                <picker mode="selector" :range="heightLabels" :value="heightIndex" @change="onHeightPick">
                  <view class="est-picker">
                    <text>{{ heightLabels[heightIndex] }}</text>
                    <text class="est-arrow">›</text>
                  </view>
                </picker>
              </view>
              <view class="est-field">
                <text class="est-label">体重</text>
                <picker mode="selector" :range="weightLabels" :value="weightIndex" @change="onWeightPick">
                  <view class="est-picker">
                    <text>{{ weightLabels[weightIndex] }}</text>
                    <text class="est-arrow">›</text>
                  </view>
                </picker>
              </view>
            </view>

            <view class="est-result">
              <text class="est-result-label">建议手围</text>
              <view class="est-result-main">
                <text class="est-size">{{ estimatedSize }}</text>
                <text class="est-unit">cm</text>
              </view>
              <text class="est-result-hint">{{ estimateHint }}</text>
            </view>

            <view class="table-block">
              <text class="block-title">完整估算表</text>
              <text class="table-note">横轴体重 kg · 纵轴身高 cm · 空格表示该组合无参考值</text>
              <scroll-view scroll-x class="table-scroll" :show-scrollbar="false">
                <view class="wrist-table">
                  <view class="t-row t-head">
                    <view class="t-cell t-corner">身高\体重</view>
                    <view v-for="w in weightCols" :key="'h'+w" class="t-cell t-weight">{{ w }}</view>
                  </view>
                  <view
                    v-for="(row, ri) in estimateMatrix"
                    :key="heightRanges[ri].label"
                    class="t-row"
                  >
                    <view class="t-cell t-height">{{ heightRanges[ri].label }}</view>
                    <view
                      v-for="(val, ci) in row"
                      :key="heightRanges[ri].label + '-' + weightCols[ci]"
                      class="t-cell"
                      :class="{ hit: ri === heightIndex && ci === weightIndex && val != null, empty: val == null }"
                    >{{ val == null ? '—' : val }}</view>
                  </view>
                </view>
              </scroll-view>
            </view>
          </view>

          <!-- 精准测量 -->
          <view v-else class="wrist-panel">
            <view class="wrist-alert">
              <text>⚠️ 无需自行加松量，报净手围即可</text>
              <text>⚠️ 测量时请贴紧皮肤，不要刻意勒紧</text>
            </view>

            <view class="measure-card">
              <text class="block-title">什么是净手围？</text>
              <text class="measure-p">手腕最细处、软尺贴紧皮肤一圈的周长。没有软尺可用细线绕一圈，再量线长。</text>
              <view class="measure-rule">
                <text class="rule-em">按净手围下单，手链会做成比净手围大约 1–2cm</text>
                <text class="rule-line">13.5–14.4 → 选 14　　14.5–15.4 → 选 15</text>
                <text class="rule-line">15.5–16.4 → 选 16　　以此类推</text>
              </view>
            </view>

            <view class="measure-card">
              <text class="block-title">纸币估算（无软尺时）</text>
              <view class="money-grid">
                <view v-for="m in moneyRefs" :key="m.note" class="money-item">
                  <text class="money-note">{{ m.note }}</text>
                  <text class="money-cm">{{ m.cm }}cm</text>
                </view>
              </view>
              <text class="measure-p subtle">用对应面额纸币绕腕一圈对照长度</text>
            </view>

            <view class="measure-card soft">
              <text class="measure-p">测量结果仅供参考，特殊体型建议联系客服协助确认尺寸。</text>
            </view>
          </view>
        </scroll-view>

        <view class="wrist-footer">
          <button class="wrist-go-diy" @click="goDiyFromWrist">去设计手串</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getBannerList, getSquareList, userGet, userSet, loginWithWeixinCode } from '../../api/index.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'
import { setTabBarSelected } from '../../utils/tabbar.js'

const banners = ref([])
const recommends = ref([])
const user = ref(null)
const showEditProfile = ref(false)
const tempAvatarUrl = ref('')
const tempNickname = ref('')

const showWristGuide = ref(false)
const wristTab = ref('estimate')
const heightIndex = ref(2) // 默认 160-165
const weightIndex = ref(2) // 默认 50kg → 14.5

const heightRanges = [
  { label: '150-155', min: 150, max: 155 },
  { label: '155-160', min: 155, max: 160 },
  { label: '160-165', min: 160, max: 165 },
  { label: '165-170', min: 165, max: 170 },
  { label: '170-175', min: 170, max: 175 },
  { label: '175-180', min: 175, max: 180 },
  { label: '180-185', min: 180, max: 185 },
  { label: '185-190', min: 185, max: 190 }
]
const weightCols = [40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100]
const heightLabels = heightRanges.map((h) => `${h.label} cm`)
const weightLabels = weightCols.map((w) => `${w} kg`)

/**
 * 客户提供的身高×体重手围参考表（空位 null = 无参考值）
 * 行：150-155 … 185-190；列：40 … 100 kg
 */
const estimateMatrix = [
  [13.5, 14, 15, 15.5, 16, 16, null, null, null, null, null, null, null],
  [14, 14, 14, 15, 15.5, 16, 17, 17.5, 18, null, null, null, null],
  [null, 14, 14.5, 15, 15.5, 17, 17.5, 18, 18, 18.5, 19, 19, 19],
  [null, null, 15, 15, 15.5, 16, 16.5, 17, 17.5, 18, 19, 19, 19],
  [null, null, null, 15.5, 16, 16.5, 17, 17.5, 18, 18, 19, 19.5, 19.5],
  [null, null, null, 16, 17, 17.5, 17.5, 18, 18, 18.5, 18.5, 19, 19.5],
  [null, null, null, null, null, 17, 17.5, 18, 18, 18.5, 18.5, 19, 19.5],
  [null, null, null, null, null, null, 17, 17.5, 18, 18.5, 19, 20, 21]
]

/** 在同一身高行内，取最近有值的体重格 */
function lookupWristSize(ri, ci) {
  const row = estimateMatrix[ri]
  if (!row) return null
  if (row[ci] != null) return row[ci]
  for (let d = 1; d < row.length; d++) {
    if (ci - d >= 0 && row[ci - d] != null) return row[ci - d]
    if (ci + d < row.length && row[ci + d] != null) return row[ci + d]
  }
  return null
}

const moneyRefs = [
  { note: '1元', cm: '13' },
  { note: '5元', cm: '13.5' },
  { note: '10元', cm: '14' },
  { note: '20元', cm: '14.5' },
  { note: '50元', cm: '15' },
  { note: '100元', cm: '15.5' }
]

const estimatedSize = computed(() => {
  const v = lookupWristSize(heightIndex.value, weightIndex.value)
  return v == null ? '--' : v
})

const estimateHint = computed(() => {
  const row = estimateMatrix[heightIndex.value]
  const exact = row && row[weightIndex.value] != null
  if (!exact && estimatedSize.value !== '--') {
    return '该身高体重无精确格，已就近取值；建议以净手围为准'
  }
  return '下单选此尺寸即可，成品会再放大 1–2cm 松量'
})

const openWristGuide = () => {
  wristTab.value = 'estimate'
  showWristGuide.value = true
}
const closeWristGuide = () => {
  showWristGuide.value = false
}
const onHeightPick = (e) => {
  heightIndex.value = Number(e.detail.value) || 0
}
const onWeightPick = (e) => {
  weightIndex.value = Number(e.detail.value) || 0
}
const goDiyFromWrist = () => {
  showWristGuide.value = false
  uni.switchTab({ url: '/pages/design/index' })
}
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
    const list = await getSquareList('recommend')
    const arr = Array.isArray(list) ? list : []
    recommends.value = arr.slice(0, 4).map((p) => ({
      ...p,
      name: p.title || p.name,
      imageUrl: resolveImageUrl(p.imageUrl || p.image || p.coverImage || ''),
      _fromSquare: true
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
  const urls = banners.value
    .map((b) => b.imageUrl)
    .filter(Boolean)
  if (!urls.length) return
  const current = item?.imageUrl || urls[0]
  uni.previewImage({
    urls,
    current
  })
}

const goDesign = () => uni.switchTab({ url: '/pages/design/index' })
const goProductList = () => uni.navigateTo({ url: '/pages/product/list' })
const goAbout = () => uni.navigateTo({ url: '/pages/about/index' })
const goService = () => uni.navigateTo({ url: '/pages/customer-service/index' })
const goDetail = (item) => {
  if (item && item._fromSquare) {
    uni.navigateTo({ url: `/pages/square/detail?id=${item.id}&channel=recommend` })
    return
  }
  uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}

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
  height: 340rpx;
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

/* —— 手围测算弹层 —— */
.wrist-mask {
  position: fixed;
  inset: 0;
  background: rgba(20, 12, 40, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}
.wrist-sheet {
  width: 100%;
  max-height: 86vh;
  background: linear-gradient(180deg, #FBF8FF 0%, #FFFFFF 28%);
  border-radius: 32rpx 32rpx 0 0;
  display: flex;
  flex-direction: column;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -12rpx 40rpx rgba(90, 50, 160, 0.12);
}
.wrist-handle {
  width: 72rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: rgba(139, 92, 246, 0.22);
  margin: 14rpx auto 8rpx;
}
.wrist-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8rpx 32rpx 16rpx;
}
.wrist-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: $text-main;
  letter-spacing: 1rpx;
}
.wrist-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $text-sub;
}
.wrist-close {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(139, 92, 246, 0.08);
  color: $primary;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wrist-tabs {
  display: flex;
  margin: 0 32rpx 12rpx;
  padding: 6rpx;
  background: rgba(139, 92, 246, 0.08);
  border-radius: 999rpx;
}
.wrist-tab {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  color: $text-sub;
  border-radius: 999rpx;
  font-weight: 500;
}
.wrist-tab.active {
  background: #fff;
  color: $primary;
  font-weight: 700;
  box-shadow: 0 4rpx 14rpx rgba(90, 50, 160, 0.1);
}
.wrist-body {
  flex: 1;
  height: 0;
  min-height: 520rpx;
  max-height: 58vh;
  padding: 0 32rpx;
  box-sizing: border-box;
}
.wrist-panel { padding-bottom: 24rpx; }

.wrist-tip-banner {
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
  border-radius: 20rpx;
  padding: 22rpx 24rpx;
  margin-bottom: 20rpx;
}
.tip-strong {
  display: block;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
}
.tip-light {
  display: block;
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 22rpx;
}

.est-card {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}
.est-field {
  flex: 1;
  background: #fff;
  border-radius: 20rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid rgba(139, 92, 246, 0.12);
  box-shadow: $shadow-card-soft;
}
.est-label {
  display: block;
  font-size: 22rpx;
  color: $text-sub;
  margin-bottom: 10rpx;
}
.est-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
  font-weight: 600;
  color: $text-main;
}
.est-arrow { color: $primary; font-size: 32rpx; }

.est-result {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  text-align: center;
  margin-bottom: 24rpx;
  border: 2rpx solid rgba(139, 92, 246, 0.18);
  box-shadow: 0 10rpx 28rpx rgba(90, 50, 160, 0.08);
}
.est-result-label {
  display: block;
  font-size: 24rpx;
  color: $text-sub;
}
.est-result-main {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6rpx;
  margin: 10rpx 0 12rpx;
}
.est-size {
  font-size: 72rpx;
  font-weight: 800;
  color: $primary;
  line-height: 1;
}
.est-unit {
  font-size: 28rpx;
  color: $primary;
  font-weight: 600;
}
.est-result-hint {
  display: block;
  font-size: 22rpx;
  color: $text-sub;
  line-height: 1.5;
}

.block-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: $text-main;
  margin-bottom: 14rpx;
}
.table-block { margin-bottom: 12rpx; }
.table-note {
  display: block;
  font-size: 20rpx;
  color: $text-hint;
  margin: -6rpx 0 12rpx;
}
.table-scroll { width: 100%; }
.wrist-table {
  display: inline-flex;
  flex-direction: column;
  min-width: 100%;
  background: #1a1228;
  border-radius: 16rpx;
  overflow: hidden;
  padding: 8rpx;
}
.t-row { display: flex; }
.t-cell {
  width: 72rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.88);
  border: 1rpx solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}
.t-corner, .t-height {
  width: 110rpx;
  font-size: 16rpx;
  color: rgba(255, 255, 255, 0.65);
  background: rgba(255, 255, 255, 0.04);
}
.t-head .t-cell {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16rpx;
  background: rgba(139, 92, 246, 0.2);
}
.t-cell.hit {
  background: $primary;
  color: #fff;
  font-weight: 700;
  border-color: $primary;
}
.t-cell.empty {
  color: rgba(255, 255, 255, 0.28);
}

.wrist-alert {
  background: #FFF5F5;
  border: 1rpx solid rgba(229, 77, 66, 0.2);
  border-radius: 18rpx;
  padding: 18rpx 20rpx;
  margin-bottom: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 24rpx;
  color: #C0392B;
  font-weight: 600;
  line-height: 1.4;
}
.measure-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 22rpx;
  margin-bottom: 16rpx;
  border: 1rpx solid rgba(139, 92, 246, 0.1);
  box-shadow: $shadow-card-soft;
}
.measure-card.soft {
  background: rgba(139, 92, 246, 0.05);
  box-shadow: none;
}
.measure-p {
  display: block;
  font-size: 24rpx;
  color: $text-main;
  line-height: 1.6;
}
.measure-p.subtle {
  margin-top: 12rpx;
  color: $text-sub;
  font-size: 22rpx;
}
.measure-rule {
  margin-top: 16rpx;
  background: linear-gradient(135deg, rgba(255, 214, 102, 0.35), rgba(255, 236, 179, 0.45));
  border-radius: 14rpx;
  padding: 16rpx;
}
.rule-em {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: #8A5A00;
  margin-bottom: 10rpx;
  line-height: 1.45;
}
.rule-line {
  display: block;
  font-size: 22rpx;
  color: #6B4E16;
  line-height: 1.55;
}
.money-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}
.money-item {
  background: rgba(139, 92, 246, 0.06);
  border-radius: 14rpx;
  padding: 16rpx 8rpx;
  text-align: center;
}
.money-note {
  display: block;
  font-size: 22rpx;
  color: $text-sub;
}
.money-cm {
  display: block;
  margin-top: 6rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $primary;
}

.wrist-footer {
  padding: 12rpx 32rpx 0;
}
.wrist-go-diy {
  margin: 0;
  height: 84rpx;
  line-height: 84rpx;
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
}
.wrist-go-diy::after { border: none; }
</style>
