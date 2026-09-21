<template>
  <view class="page" v-if="detail">
    <swiper
      class="gallery"
      circular
      :indicator-dots="bannerList.length > 1"
      :autoplay="true"
      :interval="4000"
      :duration="500"
    >
      <swiper-item v-for="(img, index) in bannerList" :key="index">
        <image class="img" :src="img" mode="aspectFill" @click="handlePreview(index)" />
      </swiper-item>
    </swiper>

    <view class="info-card">
      <view class="title-row">
        <text class="title">{{ detail.title }}</text>
      </view>
      <view class="price-row">
        <text class="price">¥ {{ detail.price }}</text>
      </view>

      <view class="creator-row" v-if="creatorName">
        <view class="creator-left">
          <view class="avatar">{{ creatorInitial }}</view>
          <view class="creator-meta">
            <text class="creator-name">{{ creatorName }}</text>
            <text class="creator-tag">官方设计</text>
          </view>
        </view>
      </view>

      <view class="desc" v-if="detail.description">{{ detail.description }}</view>

      <!-- DIY 原设计入口 -->
      <view class="diy-entry" v-if="hasDiyTemplate" @click="useDesign">
        <image class="diy-thumb" :src="designPreview" mode="aspectFill" />
        <view class="diy-entry-mid">
          <text class="diy-entry-title">原设计图</text>
          <text class="diy-entry-sub">点击将这套搭配带入DIY</text>
        </view>
        <text class="diy-entry-go">去设计 ›</text>
      </view>
    </view>

    <!-- 搭配清单 -->
    <view class="material-card" v-if="hasDiyTemplate && materialList.length">
      <view class="material-head">
        <text class="material-title">搭配清单</text>
        <text class="material-count">共 {{ totalBeads }} 颗</text>
      </view>
      <view class="material-item" v-for="(m, idx) in materialList" :key="idx">
        <image class="m-img" :src="m.imageUrl" mode="aspectFill" />
        <view class="m-mid">
          <text class="m-name">{{ m.title }}</text>
          <text class="m-spec">{{ m.size || '-' }}mm · ¥{{ m.price }}/颗</text>
        </view>
        <text class="m-qty">x{{ m.qty }}</text>
      </view>
      <text class="material-tip">材料单价仅供参考，成品价含配件、设计与制作成本。</text>
    </view>

    <!-- 普通商品数量（无 DIY 模板时） -->
    <view class="qty-box" v-if="!hasDiyTemplate">
      <view class="label">数量</view>
      <view class="stepper">
        <view class="s-btn" @click="dec">-</view>
        <input class="ipt" type="number" v-model.number="count" />
        <view class="s-btn" @click="inc">+</view>
      </view>
    </view>

    <view class="space"></view>

    <view class="action-bar">
      <view class="icon-btn" @click="goCart">
        <image class="ibar-icon" src="/static/icons/shopping-cart.png" mode="aspectFit" />
        <text>购物车</text>
      </view>
      <button class="share-btn" open-type="share">
        <image class="ibar-icon" src="/static/icons/sparkles.png" mode="aspectFit" />
        <text>分享</text>
      </button>
      <template v-if="hasDiyTemplate">
        <button class="btn ghost" @click="useDesign">使用该设计</button>
        <button class="btn primary" @click="buyNow">立即购买</button>
      </template>
      <template v-else>
        <button class="btn ghost" @click="handleAdd">加入购物车</button>
        <button class="btn primary" @click="buyNow">立即购买</button>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { productDetail, cartAdd } from '../../api/index.js'
import { isAuthError } from '../../api/request.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'
import { onLoad, onShareAppMessage } from '@dcloudio/uni-app'
import { updateCartBadge } from '../../utils/cartBadge.js'

const DIY_EDIT_STORAGE_KEY = 'diy_edit_cart'

const detail = ref(null)
const product = ref(null)
const bannerList = ref([])
const diyInfo = ref(null)
let pid = ''
const count = ref(1)

const hasDiyTemplate = computed(() => !!(diyInfo.value && Array.isArray(diyInfo.value.beads) && diyInfo.value.beads.length))
const creatorName = computed(() => (detail.value && detail.value.creatorName) || '')
const creatorInitial = computed(() => (creatorName.value || '祈').slice(0, 1))
const designPreview = computed(() => {
  if (diyInfo.value && diyInfo.value.imageUrl) return resolveImageUrl(diyInfo.value.imageUrl)
  return (detail.value && detail.value.coverImage) || ''
})
const materialList = computed(() => {
  const beads = (diyInfo.value && diyInfo.value.beads) || []
  const map = {}
  beads.forEach(b => {
    const key = String(b.productId != null ? b.productId : `${b.title}_${b.size}`)
    if (!map[key]) {
      map[key] = {
        title: b.title || b.name || '材料',
        size: b.size,
        price: b.price,
        imageUrl: resolveImageUrl(b.imageUrl || ''),
        qty: 0
      }
    }
    map[key].qty += 1
  })
  return Object.values(map)
})
const totalBeads = computed(() => materialList.value.reduce((s, m) => s + m.qty, 0))

function inc () { count.value = Number(count.value || 1) + 1 }
function dec () { count.value = Math.max(1, Number(count.value || 1) - 1) }

function handlePreview (current) {
  if (!bannerList.value || bannerList.value.length === 0) return
  uni.previewImage({
    urls: bannerList.value,
    current: bannerList.value[current],
    indicator: 'number',
    loop: true
  })
}

function goCart () {
  uni.switchTab({ url: '/pages/cart/index' })
}

function useDesign () {
  if (!hasDiyTemplate.value) {
    uni.showToast({ title: '暂无设计模板', icon: 'none' })
    return
  }
  try {
    uni.setStorageSync(DIY_EDIT_STORAGE_KEY, JSON.stringify({
      diyData: diyInfo.value,
      fromProductId: Number(pid) || null
    }))
    uni.switchTab({ url: '/pages/design/index' })
  } catch (e) {
    console.error('带入制作台失败', e)
    uni.showToast({ title: '打开制作台失败', icon: 'none' })
  }
}

async function handleAdd () {
  try {
    await cartAdd(Number(pid), Number(count.value))
    uni.showToast({ title: '已加入购物车', icon: 'success' })
    updateCartBadge()
  } catch (e) {
    console.error('添加购物车失败:', e)
    if (e.authExpired || isAuthError(e)) return
    uni.showToast({
      title: e.msg || e.message || '添加失败，请重试',
      icon: 'none'
    })
  }
}

async function buyNow () {
  try {
    const item = {
      productId: product.value.id,
      title: product.value.title,
      price: product.value.price,
      image: product.value.coverImage,
      imageUrl: product.value.coverImage,
      quantity: Number(count.value)
    }
    uni.setStorageSync('direct_buy_item', item)
    uni.navigateTo({ url: '/pages/order/confirm?mode=direct' })
  } catch (e) {
    console.error('立即购买失败:', e)
    uni.showToast({ title: '操作失败，请重试', icon: 'none' })
  }
}

function parseDiyData (raw) {
  if (!raw) return null
  try {
    const diy = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!diy || !Array.isArray(diy.beads)) return null
    return diy
  } catch (e) {
    console.error('解析 diyData 失败', e)
    return null
  }
}

onShareAppMessage(() => ({
  title: (detail.value && detail.value.title) || '祈愿手作',
  path: `/pages/product/detail?id=${pid}`,
  imageUrl: (detail.value && detail.value.coverImage) || '/static/logo/qiyuan_logo.png'
}))

onLoad(async (options) => {
  pid = options && options.id ? options.id : ''
  if (!pid) return
  try {
    const res = await productDetail(Number(pid))
    product.value = res && res.product ? res.product : res

    if (product.value) {
      if (product.value.coverImage) {
        product.value.coverImage = resolveImageUrl(product.value.coverImage)
      }

      let details = []
      if (product.value.detailImages) {
        details = Array.isArray(product.value.detailImages)
          ? product.value.detailImages
          : (typeof product.value.detailImages === 'string' ? JSON.parse(product.value.detailImages) : [])
      }

      const processedDetails = details.map(img => resolveImageUrl(img))
      const images = new Set()
      if (product.value.coverImage) images.add(product.value.coverImage)
      processedDetails.forEach(img => images.add(img))
      bannerList.value = Array.from(images).filter(Boolean)

      diyInfo.value = parseDiyData(product.value.diyData)
    }

    detail.value = product.value || null
  } catch (e) {
    console.error('获取详情失败', e)
  }
})
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  background: $page-bg;
  min-height: 100vh;
  padding-bottom: 160rpx;
}
.gallery {
  height: 520rpx;
  background: $primary-soft-2;
}
.img { width: 100%; height: 100%; }

.info-card {
  margin: 20rpx 24rpx 0;
  position: relative;
  z-index: 2;
  background: $card-bg;
  border-radius: $radius-card;
  padding: 28rpx 28rpx 20rpx;
  box-shadow: $shadow-card;
}
.title { font-size: 36rpx; font-weight: 700; color: $text-main; line-height: 1.35; }
.price-row { margin-top: 12rpx; }
.price { color: $primary; font-size: 40rpx; font-weight: 700; }

.creator-row {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.creator-left { display: flex; align-items: center; gap: 16rpx; }
.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $gradient-primary;
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.creator-meta { display: flex; flex-direction: column; gap: 4rpx; }
.creator-name { font-size: 26rpx; color: $text-main; font-weight: 600; }
.creator-tag {
  align-self: flex-start;
  font-size: 20rpx;
  color: $primary-deep;
  background: $primary-soft;
  padding: 2rpx 12rpx;
  border-radius: 8rpx;
}
.desc {
  margin-top: 16rpx;
  color: $text-sub;
  font-size: 26rpx;
  line-height: 1.6;
}

.diy-entry {
  margin-top: 24rpx;
  padding: 18rpx;
  border-radius: 20rpx;
  border: 2rpx solid rgba(139, 92, 246, 0.22);
  background: linear-gradient(120deg, #FBF8FF 0%, #F3EBFF 100%);
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.diy-thumb {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: #fff;
  flex-shrink: 0;
}
.diy-entry-mid { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.diy-entry-title { font-size: 28rpx; font-weight: 700; color: $text-main; }
.diy-entry-sub { font-size: 22rpx; color: $text-sub; }
.diy-entry-go { font-size: 26rpx; color: $primary; font-weight: 600; flex-shrink: 0; }

.material-card {
  margin: 20rpx 24rpx 0;
  background: $card-bg;
  border-radius: $radius-card;
  padding: 24rpx 28rpx 20rpx;
  box-shadow: $shadow-card;
}
.material-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.material-title { font-size: 30rpx; font-weight: 700; color: $text-main; }
.material-count { font-size: 22rpx; color: $text-hint; }
.material-item {
  display: flex;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid $border-soft;
}
.material-item:last-of-type { border-bottom: none; }
.m-img {
  width: 72rpx;
  height: 72rpx;
  border-radius: 16rpx;
  background: $primary-soft;
  flex-shrink: 0;
}
.m-mid { flex: 1; margin: 0 18rpx; display: flex; flex-direction: column; gap: 6rpx; }
.m-name { font-size: 28rpx; color: $text-main; font-weight: 600; }
.m-spec { font-size: 22rpx; color: $text-sub; }
.m-qty { font-size: 26rpx; color: $text-main; font-weight: 600; }
.material-tip {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: $text-hint;
  line-height: 1.5;
}

.qty-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $card-bg;
  margin: 20rpx 24rpx 0;
  padding: 16rpx 24rpx;
  border-radius: $radius-card;
}
.label { color: $text-main; font-size: 28rpx; }
.stepper { display: flex; align-items: center; }
.s-btn {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-soft;
  border-radius: 10rpx;
  font-size: 36rpx;
  color: $text-main;
}
.ipt {
  width: 120rpx;
  margin: 0 12rpx;
  text-align: center;
  height: 64rpx;
  border: 2rpx solid $border-soft;
  border-radius: 10rpx;
}
.space { height: 20rpx; }

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 12rpx 20rpx calc(12rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 10rpx;
  box-shadow: 0 -6rpx 16rpx rgba(90, 50, 160, 0.06);
}
.icon-btn, .share-btn {
  width: 88rpx;
  padding: 0;
  margin: 0;
  background: transparent;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: $text-sub;
  line-height: 1.2;
}
.share-btn::after { border: none; }
.ibar-icon { width: 40rpx; height: 40rpx; margin-bottom: 4rpx; }
.btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-weight: 600;
  font-size: 28rpx;
  padding: 0;
  margin: 0;
}
.btn.ghost {
  background: #fff;
  color: $primary;
  border: 2rpx solid $primary;
}
.btn.primary {
  background: $gradient-primary;
  color: #fff;
  border: none;
  box-shadow: $shadow-btn;
}
</style>
