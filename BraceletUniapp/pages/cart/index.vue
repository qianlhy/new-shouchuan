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
      <view v-for="i in items" :key="i.id" class="row" :class="{updating: updating, deleting: deleting, 'diy-item': i.isDiy}">
        <view class="thumb">
          <image v-if="i.imageUrl" :src="i.imageUrl" mode="aspectFill" class="thumb-img" />
          <view v-if="i.isDiy" class="diy-badge">DIY</view>
        </view>
        <view class="meta">
          <view class="title">{{ i.title }}</view>
          <view class="price">¥{{ i.price }}</view>
          <!-- DIY商品不显示数量调整 -->
          <view v-if="!i.isDiy" class="stepper">
            <view class="s-btn" :class="{disabled: updating || deleting}" @click="dec(i)">-</view>
            <input class="ipt" type="number" v-model.number="i.quantity" @blur="apply(i)" :disabled="updating || deleting" />
            <view class="s-btn" :class="{disabled: updating || deleting}" @click="inc(i)">+</view>
          </view>
          <view v-else class="diy-info">
            <text class="diy-quantity">数量: {{ i.quantity }}</text>
            <text v-if="i.diySize" class="diy-size">手围: {{ i.diySize }}cm</text>
            <view class="diy-edit-btn" @click.stop="reeditDiy(i)">重新设计</view>
          </view>
        </view>
        <view class="remove" :class="{disabled: updating || deleting}" @click="removeItem(i)">×</view>
      </view>
    </view>

    <view v-if="isLoggedIn" class="bar">
      <view class="total">合计：<text class="money">¥{{ total }}</text></view>
      <button class="checkout" :disabled="!items.length" @click="goCheckout">去结算</button>
    </view>
    </template>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { cartDelete, cartList, cartUpdate, isLoggedIn as checkLogin } from '../../api/index.js'
import { updateCartBadge, updateCartBadgeNow } from '../../utils/cartBadge.js'
import { debugCartBadge } from '../../utils/debugCartBadge.js'
import { resolveImageUrl } from '../../utils/imageHelper.js'

const isLoggedIn = ref(false)
const items = ref([])
const updating = ref(false) // 正在更新中
const deleting = ref(false) // 正在删除中

const total = computed(() => {
  return items.value.reduce((sum, i) => sum + Number(i.price || 0) * Number(i.quantity || 0), 0).toFixed(2)
})

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
    // 处理图片链接，过滤掉无效商品（没有标题或ID的）
    list = list.map(item => {
      let imageUrl = item.coverImage || item.imageUrl || item.image || ''
      imageUrl = resolveImageUrl(imageUrl)
      
      // 处理DIY商品数据
      if (item.isDiy && item.diyData) {
        try {
          const diyInfo = JSON.parse(item.diyData)
          return {
            ...item,
            imageUrl: diyInfo.imageUrl || imageUrl,
            diySize: diyInfo.size,
            diyBeads: diyInfo.beads || []
          }
        } catch (e) {
          console.error('解析DIY数据失败:', e)
        }
      }
      
      return { ...item, imageUrl }
    })
    // .filter(item => {
    //   // 过滤掉无效商品：必须有标题
    //   const hasTitle = item.title && String(item.title).trim() !== ''
    //   // DIY商品(productId为负数)也是有效的，普通商品需要有有效的productId
    //   const isDiy = item.isDiy || (item.productId !== undefined && item.productId !== null && item.productId < 0)
    //   const hasValidId = isDiy || (item.productId !== undefined && item.productId !== null && item.productId > 0)
    //   return hasTitle && hasValidId
    // })
    
    items.value = list
    // 立即更新购物车角标
    updateCartBadgeNow()
  } catch (e) {
    console.error('加载购物车失败:', e)
    // 如果是401错误，说明token失效
    if (e.code === 401) {
      isLoggedIn.value = false
      uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
    }
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
    uni.showToast({ title: '删除失败', icon: 'none' })
  } finally {
    deleting.value = false
  }
}

// 去结算 - 跳转到订单确认页
function goCheckout() {
  if (!items.value || items.value.length === 0) {
    uni.showToast({ title: '购物车为空', icon: 'none' })
    return
  }
  
  uni.navigateTo({ url: '/pages/order/confirm' })
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
  load()
})

// 开发环境暴露调试函数
if (typeof window !== 'undefined') {
  window.debugCartBadge = debugCartBadge
}
</script>

<style>
.page {
  padding-bottom: 160rpx;
  min-height: 100vh;
  box-sizing: border-box;
  background:
    radial-gradient(ellipse 80% 40% at 20% 0%, rgba(183, 148, 255, 0.26), transparent 55%),
    radial-gradient(ellipse 60% 30% at 90% 10%, rgba(221, 200, 255, 0.35), transparent 50%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 40%, #FBF7FF 100%);
}

/* 登录提示样式 */
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
  color: #666;
  margin-bottom: 48rpx;
}
.login-prompt .login-btn {
  background: linear-gradient(90deg, #B794FF, #8B5CF6 55%, #7C3AED);
  color: #fff;
  border-radius: 999rpx;
  padding: 0 64rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 10rpx 24rpx rgba(139, 92, 246, 0.32);
}

.empty {
  padding: 120rpx 24rpx;
  text-align: center;
  color: #999;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-orb {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 35%, #F8F3FF 0%, #EDE4FF 55%, #E4D6FF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
  box-shadow: 0 12rpx 32rpx rgba(90, 50, 160, 0.08);
}
.empty-cart-img {
  width: 88rpx;
  height: 88rpx;
}
.empty-text {
  font-size: 28rpx;
  color: #8B849C;
  margin-bottom: 28rpx;
}
.go-shop {
  background: linear-gradient(90deg, #B794FF, #8B5CF6 55%, #7C3AED);
  color: #fff;
  border-radius: 999rpx;
  padding: 0 56rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: 0 10rpx 24rpx rgba(139, 92, 246, 0.32);
  border: none;
}
.go-shop::after { border: none; }

.list { padding: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.row { background: #ffffff; border-radius: 24rpx; padding: 16rpx; display: flex; gap: 16rpx; align-items: center; box-shadow: 0 10rpx 28rpx rgba(75,45,160,0.08); transition: all 0.3s ease; border: 1rpx solid rgba(107,78,255,0.08); }
.row.updating { opacity: 0.6; }
.row.deleting { opacity: 0.3; transform: translateX(-20rpx); }
.thumb { width: 140rpx; height: 140rpx; background: #EDE7FF; border-radius: 16rpx; overflow: hidden; position: relative; }
.thumb-img { width: 100%; height: 100%; border-radius: 16rpx; }
.meta { flex: 1; }
.title { font-size: 28rpx; color: #333; }
.price { color: #e54d42; font-weight: 700; margin-top: 4rpx; }
.remove { width: 48rpx; height: 48rpx; line-height: 48rpx; text-align: center; border-radius: 50%; background: #f5f5f5; color: #999; cursor: pointer; transition: all 0.2s ease; }
.remove:active { background: #e54d42; color: #fff; }
.remove.disabled { opacity: 0.4; pointer-events: none; }
.stepper { margin-top: 10rpx; display: flex; align-items: center; }
.s-btn { width: 54rpx; height: 54rpx; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 10rpx; font-size: 32rpx; color: #333; cursor: pointer; transition: all 0.2s ease; }
.s-btn:active { background: #ddd; }
.s-btn.disabled { opacity: 0.4; pointer-events: none; }
.ipt { width: 100rpx; margin: 0 10rpx; text-align: center; height: 54rpx; border: 2rpx solid #eee; border-radius: 10rpx; }
.ipt:disabled { background: #f9f9f9; color: #999; }

/* DIY商品样式 */
.diy-item { background: linear-gradient(135deg, #FBF9FF, #ffffff); border: 2rpx solid #E0D4FF; }
.diy-badge { position: absolute; top: 8rpx; left: 8rpx; background: linear-gradient(135deg, #6B4EFF, #8B6CFF); color: #fff; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx; font-weight: 600; }
.diy-info { margin-top: 10rpx; display: flex; flex-direction: column; gap: 4rpx; align-items: flex-start; }
.diy-quantity { font-size: 24rpx; color: #666; }
.diy-size { font-size: 22rpx; color: #999; }
.diy-edit-btn {
  margin-top: 8rpx;
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #B794FF, #8B5CF6 55%, #7C3AED);
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
}

.bar { position: fixed; left: 0; right: 0; bottom: 0; background: #ffffff; padding: 12rpx 24rpx calc(12rpx + env(safe-area-inset-bottom)); display: flex; justify-content: space-between; align-items: center; box-shadow: 0 -6rpx 12rpx rgba(0,0,0,0.04); }
.total { color: #333; font-size: 28rpx; }
.money { color: #e54d42; font-weight: 700; }
.checkout { background: linear-gradient(90deg, #B794FF, #8B5CF6 55%, #7C3AED); color: #fff; border-radius: 999rpx; padding: 0 28rpx; height: 72rpx; line-height: 72rpx; font-weight: 600; box-shadow: 0 10rpx 24rpx rgba(139,92,246,.32); }
</style>
