<template>
  <view class="page">
    <!-- 未登录提示 -->
    <view v-if="!isLoggedIn" class="login-prompt">
      <view class="icon">🔒</view>
      <view class="tip">请先登录查看购物车</view>
      <button class="login-btn" @click="goToLogin">去登录</button>
    </view>
    
    <template v-else>
      <view v-if="!items.length" class="empty">
        <view class="empty-orb">
          <image class="empty-cart-img" src="/static/icons/shopping-cart.png" mode="aspectFit" />
        </view>
        <text class="empty-text">购物车空空如也</text>
        <button class="go-shop" @click="goShop">去逛逛</button>
      </view>

      <view v-else class="list">
      <view
        v-for="i in items"
        :key="i.id"
        class="row"
        :class="{ 'is-diy': i.isDiy, 'is-off': !i.checked }"
        @click="toggleItem(i)"
      >
        <view class="check" :class="{ on: i.checked }" @click.stop="toggleItem(i)">
          <text v-if="i.checked">✓</text>
        </view>
        <view class="thumb" @click.stop @longpress.stop="saveCartImage(i)">
          <image
            v-if="i.imageUrl"
            class="thumb-img"
            :src="i.imageUrl"
            mode="aspectFill"
          />
          <view v-else class="thumb-placeholder">珠</view>
          <view v-if="i.isDiy" class="diy-badge">DIY</view>
        </view>
        <view class="meta">
          <view class="title">{{ i.title }}</view>
          <view class="price">¥{{ i.price }}</view>
          <view v-if="!i.isDiy" class="stepper" @click.stop>
            <view class="s-btn" :class="{disabled: updating || deleting}" @click="dec(i)">-</view>
            <input class="ipt" type="number" v-model.number="i.quantity" @blur="apply(i)" :disabled="updating || deleting" />
            <view class="s-btn" :class="{disabled: updating || deleting}" @click="inc(i)">+</view>
          </view>
          <view v-else class="diy-info" @click.stop>
            <view class="diy-meta-row">
              <text class="diy-meta">数量 {{ i.quantity }}</text>
              <text v-if="i.diySize" class="diy-dot">·</text>
              <text v-if="i.diySize" class="diy-meta">手围 {{ i.diySize }}cm</text>
            </view>
            <view class="diy-edit-btn" @click.stop="reeditDiy(i)">重新设计</view>
          </view>
        </view>
        <view class="remove" :class="{disabled: updating || deleting}" @click.stop="removeItem(i)">×</view>
      </view>
    </view>

    <view v-if="isLoggedIn && items.length" class="bar">
      <view class="bar-left" @click="toggleSelectAll">
        <view class="check" :class="{ on: allChecked }">
          <text v-if="allChecked">✓</text>
        </view>
        <text class="all-label">全选</text>
      </view>
      <view class="total">合计：<text class="money">¥{{ total }}</text></view>
      <button class="checkout" :disabled="!selectedCount" @click="goCheckout">去结算({{ selectedCount }})</button>
    </view>
    </template>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { cartDelete, cartList, cartUpdate, isLoggedIn as checkLogin } from '../../api/index.js'
import { isAuthError } from '../../api/request.js'
import { updateCartBadge, updateCartBadgeNow } from '../../utils/cartBadge.js'
import { debugCartBadge } from '../../utils/debugCartBadge.js'
import { resolveImageUrl, toDownloadableImageUrl } from '../../utils/imageHelper.js'
import { setTabBarSelected } from '../../utils/tabbar.js'

const isLoggedIn = ref(false)
const items = ref([])
const updating = ref(false) // 正在更新中
const deleting = ref(false) // 正在删除中

const selectedItems = computed(() => items.value.filter(i => i.checked))
const selectedCount = computed(() => selectedItems.value.length)
const allChecked = computed(() => items.value.length > 0 && selectedItems.value.length === items.value.length)
const total = computed(() => {
  return selectedItems.value.reduce((sum, i) => sum + Number(i.price || 0) * Number(i.quantity || 0), 0).toFixed(2)
})

function toggleItem (item) {
  item.checked = !item.checked
}

function toggleSelectAll () {
  const next = !allChecked.value
  items.value.forEach(i => { i.checked = next })
}

// 检查登录状态
function checkLoginStatus() {
  isLoggedIn.value = checkLogin()
  console.log('购物车登录状态:', isLoggedIn.value)
}

// 跳转登录
function goToLogin() {
  uni.reLaunch({ url: '/pages/index/index' })
}

function goShop() {
  uni.switchTab({ url: '/pages/square/index' })
}

/** 长按购物车缩略图 → 保存设计图到相册 */
function saveCartImage(item) {
  const url = item && item.imageUrl
  if (!url) {
    uni.showToast({ title: '暂无图片可保存', icon: 'none' })
    return
  }
  uni.showModal({
    title: '保存图片',
    content: '将这张设计图保存到手机相册？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '保存中...', mask: true })
        let filePath = url
        if (/^https?:\/\//i.test(url) || String(url).includes('/admin/common/image/')) {
          const downloadUrl = toDownloadableImageUrl(url)
          filePath = await new Promise((resolve, reject) => {
            uni.downloadFile({
              url: downloadUrl,
              success: (r) => {
                if (r.statusCode === 200 && r.tempFilePath) resolve(r.tempFilePath)
                else reject(new Error('图片下载失败'))
              },
              fail: (e) => reject(new Error((e && e.errMsg) || '图片下载失败'))
            })
          })
        }
        await new Promise((resolve, reject) => {
          uni.saveImageToPhotosAlbum({
            filePath,
            success: resolve,
            fail: reject
          })
        })
        uni.hideLoading()
        uni.showToast({ title: '已保存到相册', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        const msg = (e && (e.errMsg || e.message)) || ''
        if (/auth|authorize|permission|隐私|deny|拒绝/i.test(msg)) {
          uni.showModal({
            title: '需要相册权限',
            content: '请在设置中允许保存到相册后重试',
            confirmText: '去设置',
            success: (r) => {
              if (r.confirm) uni.openSetting({})
            }
          })
        } else {
          uni.showToast({ title: msg || '保存失败', icon: 'none' })
        }
      }
    }
  })
}

/** 从购物车重新打开 DIY 制作台并回填设计 */
function reeditDiy(item) {
  if (!item || !item.isDiy) return
  if (!item.diyData) {
    uni.showToast({ title: '无法读取设计数据', icon: 'none' })
    return
  }
  try {
    uni.setStorageSync('diy_edit_cart', {
      cartItemId: item.id,
      productId: item.productId,
      diyData: item.diyData
    })
    uni.switchTab({ url: '/pages/design/index' })
  } catch (e) {
    console.error('跳转重新设计失败', e)
    uni.showToast({ title: '打开失败', icon: 'none' })
  }
}

async function load() {
  // 先检查登录状态
  checkLoginStatus()
  
  // 如果未登录，不加载购物车数据
  if (!isLoggedIn.value) {
    console.log('未登录，跳过加载购物车数据')
    return
  }
  
  try {
    const res = await cartList()
    console.log('购物车数据:', res)
    
    let list = res.items || []
    list = list.map(item => {
      const isDiy = !!(item.isDiy || item.diy || (item.productId != null && Number(item.productId) < 0) || item.diyData)
      let imageUrl = item.coverImage || item.imageUrl || item.image || ''
      imageUrl = resolveImageUrl(imageUrl)
      let diySize = item.diySize
      let diyBeads = []
      let title = item.title
      let price = item.price

      if (isDiy && item.diyData) {
        try {
          const diyInfo = typeof item.diyData === 'string' ? JSON.parse(item.diyData) : item.diyData
          diySize = diyInfo.size != null ? diyInfo.size : diySize
          diyBeads = diyInfo.beads || []
          if (!title) title = diyInfo.title || 'DIY设计'
          if (price == null || price === '') price = diyInfo.price

          // 优先用整串设计图；失败再退回珠子实拍
          const designUrl = diyInfo.imageUrl || ''
          const beadImg = diyBeads.find(b => b && (b.imageUrl || b.image))
          const beadUrl = beadImg ? (beadImg.imageUrl || beadImg.image) : ''
          const isLogoFallback = designUrl && (
            designUrl.includes('qiyuan_logo') ||
            designUrl.includes('/static/logo/')
          )
          imageUrl = resolveImageUrl((!isLogoFallback && designUrl) ? designUrl : (beadUrl || designUrl || imageUrl))
        } catch (e) {
          console.error('解析DIY数据失败:', e)
        }
      }

      return {
        ...item,
        isDiy,
        checked: true,
        title: title || (isDiy ? 'DIY设计' : '商品'),
        price,
        imageUrl,
        diySize,
        diyBeads
      }
    }).filter(item => item && (item.id != null || item.productId != null || item.diyData))

    items.value = list
    updateCartBadgeNow()
  } catch (e) {
    console.error('加载购物车失败:', e)
    items.value = []
    if (e.authExpired || isAuthError(e)) {
      isLoggedIn.value = false
      return
    }
    uni.showToast({ title: e.msg || '购物车加载失败', icon: 'none' })
  }
}

async function removeItem(item) {
  if (deleting.value || updating.value) {
    console.log('操作进行中，请稍后')
    return
  }
  
  deleting.value = true
  try {
    // 调用API删除商品（支持普通商品和DIY商品）
    // 如果有id字段优先使用id（DIY商品通过id删除），否则使用productId
    const productId = item.productId
    const id = item.id
    await cartDelete(productId, id)
    
    await load()
    // 立即更新购物车角标
    updateCartBadgeNow()
    uni.showToast({ title: '已删除', icon: 'success', duration: 1000 })
  } catch (e) {
    console.error('删除失败:', e)
    if (e.authExpired || isAuthError(e)) return
    uni.showToast({ title: '删除失败', icon: 'none' })
  } finally {
    deleting.value = false
  }
}

// 去结算 - 只结算勾选商品
function goCheckout() {
  const selected = selectedItems.value
  if (!selected.length) {
    uni.showToast({ title: '请先勾选要结算的商品', icon: 'none' })
    return
  }
  const cartItemIds = selected.map(i => i.id).filter(id => id != null)
  if (!cartItemIds.length) {
    uni.showToast({ title: '商品数据异常，请刷新购物车', icon: 'none' })
    return
  }
  try {
    uni.setStorageSync('checkout_selected_ids', cartItemIds)
    uni.setStorageSync('checkout_selected_items', selected)
  } catch (e) {
    console.error('保存勾选结算数据失败', e)
  }
  uni.navigateTo({ url: '/pages/order/confirm?mode=selected' })
}

async function apply(i) {
  if (updating.value) return
  
  updating.value = true
  const pid = i.productId || i.id
  const qty = Math.max(1, Number(i.quantity || 1))
  i.quantity = qty
  
  try {
    await cartUpdate(pid, qty)
    // 更新购物车角标
    updateCartBadge()
  } catch (e) {
    console.error('更新数量失败:', e)
    if (!(e.authExpired || isAuthError(e))) {
      uni.showToast({ title: '更新失败', icon: 'none' })
    }
  } finally {
    updating.value = false
  }
}

async function inc(i) {
  if (updating.value) {
    console.log('更新中，请稍后')
    return
  }
  
  updating.value = true
  const pid = i.productId || i.id
  const qty = Number(i.quantity || 1) + 1
  i.quantity = qty
  
  try {
    await cartUpdate(pid, qty)
    // 更新购物车角标
    updateCartBadge()
  } catch (e) {
    console.error('增加数量失败:', e)
    // 恢复原数量
    i.quantity = qty - 1
  } finally {
    updating.value = false
  }
}

async function dec(i) {
  if (updating.value) {
    console.log('更新中，请稍后')
    return
  }
  
  updating.value = true
  const pid = i.productId || i.id
  const oldQty = Number(i.quantity || 1)
  const qty = Math.max(1, oldQty - 1)
  i.quantity = qty
  
  try {
    await cartUpdate(pid, qty)
    // 更新购物车角标
    updateCartBadge()
  } catch (e) {
    console.error('减少数量失败:', e)
    // 恢复原数量
    i.quantity = oldQty
  } finally {
    updating.value = false
  }
}

onShow(() => {
  setTabBarSelected(3)
  load()
})

// 开发环境暴露调试函数
if (typeof window !== 'undefined') {
  window.debugCartBadge = debugCartBadge
}
</script>

<style lang="scss">
@import '../../styles/theme.scss';

.page {
  padding-bottom: 160rpx;
  min-height: 100vh;
  box-sizing: border-box;
  background:
    $page-mist,
    linear-gradient(180deg, $page-bg 0%, $page-bg-deep 40%, $page-bg 100%);
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx 48rpx;
  min-height: 60vh;
}
.login-prompt .icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
  opacity: 0.5;
}
.login-prompt .tip {
  font-size: 32rpx;
  color: $text-sub;
  margin-bottom: 48rpx;
}
.login-prompt .login-btn {
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  padding: 0 64rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: $shadow-btn;
}

.empty {
  padding: 120rpx 24rpx;
  text-align: center;
  color: $text-hint;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-orb {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: $icon-orb;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
  box-shadow: $shadow-card-soft;
}
.empty-cart-img {
  width: 88rpx;
  height: 88rpx;
}
.empty-text {
  font-size: 28rpx;
  color: $text-sub;
  margin-bottom: 28rpx;
}
.go-shop {
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
.go-shop::after { border: none; }

.list { padding: 24rpx 24rpx 40rpx; display: flex; flex-direction: column; gap: 20rpx; }
.row {
  background: $card-bg;
  border-radius: 24rpx;
  padding: 24rpx 20rpx;
  display: flex;
  gap: 20rpx;
  align-items: center;
  box-shadow: $shadow-card-soft;
  border: 1rpx solid $border-soft;
}
.row.is-off {
  opacity: 0.92;
}
.check {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 2rpx solid $primary-light;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
  background: #FFFFFF;
}
.check.on {
  background: $primary;
  border-color: $primary;
}
.bar-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-right: 12rpx;
}
.all-label { font-size: 26rpx; color: $text-main; font-weight: 600; }
.thumb {
  width: 160rpx;
  height: 160rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  border: 1rpx solid rgba(139, 92, 246, 0.12);
}
.thumb-img {
  width: 100%;
  height: 100%;
  display: block;
  background: #FFFFFF;
}
.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-soft;
  color: $primary;
  font-size: 40rpx;
  font-weight: 700;
}
.meta { flex: 1; min-width: 0; }
.title {
  font-size: 30rpx;
  color: $text-main;
  font-weight: 700;
  line-height: 1.35;
}
.price {
  color: #E11D48;
  font-weight: 800;
  margin-top: 8rpx;
  font-size: 34rpx;
}
.remove {
  width: 52rpx;
  height: 52rpx;
  line-height: 52rpx;
  text-align: center;
  border-radius: 50%;
  background: $primary-soft;
  color: $text-sub;
  flex-shrink: 0;
  font-size: 32rpx;
}
.remove:active { background: #E11D48; color: #fff; }
.remove.disabled { opacity: 0.4; pointer-events: none; }
.stepper { margin-top: 12rpx; display: flex; align-items: center; }
.s-btn {
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-soft;
  border-radius: 10rpx;
  font-size: 32rpx;
  color: $text-main;
}
.s-btn:active { background: $primary-soft-2; }
.s-btn.disabled { opacity: 0.4; pointer-events: none; }
.ipt {
  width: 100rpx;
  margin: 0 10rpx;
  text-align: center;
  height: 54rpx;
  border: 1rpx solid rgba(139, 92, 246, 0.18);
  border-radius: 10rpx;
  color: $text-main;
}
.ipt:disabled { background: #f9f9f9; color: #999; }

.row.is-diy {
  background: $card-bg;
  border-color: rgba(139, 92, 246, 0.22);
}
.diy-badge {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background: $primary;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 0 0 12rpx 0;
  font-weight: 600;
}
.diy-info {
  margin-top: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: flex-start;
}
.diy-meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6rpx;
}
.diy-meta {
  font-size: 22rpx;
  color: $text-sub;
  font-weight: 500;
  line-height: 1.3;
}
.diy-dot {
  font-size: 22rpx;
  color: $text-hint;
}
.diy-edit-btn {
  padding: 8rpx 22rpx;
  border-radius: $radius-pill;
  background: $btn-ghost-bg;
  color: $btn-ghost-text;
  font-size: 22rpx;
  font-weight: 500;
  border: 1rpx solid $btn-ghost-border;
  line-height: 1.3;
}

.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.96);
  padding: 12rpx 24rpx calc(12rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  box-shadow: 0 -6rpx 16rpx rgba(90, 50, 160, 0.05);
  gap: 12rpx;
}
.total { color: $text-main; font-size: 26rpx; flex: 1; text-align: right; }
.money { color: #e54d42; font-weight: 700; }
.checkout {
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  padding: 0 28rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-weight: 600;
  box-shadow: $shadow-btn;
  flex-shrink: 0;
}
.checkout[disabled] { opacity: 0.45; }
</style>
