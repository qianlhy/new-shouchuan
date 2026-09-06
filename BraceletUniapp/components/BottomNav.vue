<template>
  <view class="tabbar">
    <view class="item" :class="{ active: active === 'home' }" @click="go('home')">
      <view class="icon" />
      <text>首页</text>
    </view>
    <view class="item" :class="{ active: active === 'square' }" @click="go('square')">
      <view class="icon" />
      <text>广场</text>
    </view>
    <view class="item" :class="{ active: active === 'design' }" @click="go('design')">
      <view class="icon" />
      <text>DIY</text>
    </view>
    <view class="item" :class="{ active: active === 'cart' }" @click="go('cart')">
      <view class="icon" />
      <text>购物车</text>
    </view>
    <view class="item" :class="{ active: active === 'mine' }" @click="go('mine')">
      <view class="icon" />
      <text>我的</text>
    </view>
  </view>
</template>

<script setup>
defineProps({ active: { type: String, default: '' } })

function go(key) {
  const map = {
    home: '/pages/index/index',
    square: '/pages/square/index',
    design: '/pages/design/index',
    cart: '/pages/cart/index',
    mine: '/pages/mine/index'
  }
  if (map[key]) uni.switchTab({ url: map[key] })
}
</script>

<style lang="scss">
@import '../styles/theme.scss';

.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 110rpx;
  background: #ffffff;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 999;
}
.item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-sub;
  font-size: 22rpx;
}
.item .icon {
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  background: #d8d8d8;
  margin-bottom: 6rpx;
}
.item.active { color: $primary; }
.item.active .icon { background: $primary; }
</style>
