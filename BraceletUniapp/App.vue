<template>
  <view />
</template>

<script>
import { checkAndAutoLogin } from './api/index.js'
import { updateCartBadgeNow } from './utils/cartBadge.js'

export default {
  onLaunch() {
    setTimeout(() => {
      updateCartBadgeNow()
    }, 500)

    checkAndAutoLogin().then((success) => {
      if (success) {
        console.log('[App] 自动登录成功')
      }
    })
  },
  onShow() {
    updateCartBadgeNow()

    checkAndAutoLogin().then((success) => {
      if (success) {
        console.log('[App] 自动登录成功')
      }
    })
  },
  onHide() {}
}
</script>

<style lang="scss">
@import './styles/theme.scss';
@import './static/iconfont/xyicon.css';

page {
  background: linear-gradient(180deg, #FBF7FF 0%, #F5EEFF 42%, #FBF7FF 100%);
  background-color: $page-bg;
  color: $text-main;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
}

.xy-btn-primary {
  background: $gradient-primary;
  color: #fff;
  border-radius: $radius-pill;
  box-shadow: $shadow-btn;
  border: none;
  font-weight: 600;
}

.xy-btn-primary::after {
  border: none;
}

.xy-card {
  background: $card-bg;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  border: 1rpx solid $border-soft;
}
</style>
