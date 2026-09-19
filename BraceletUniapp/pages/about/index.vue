<template>
  <view class="page">
    <view class="hero">
      <text class="hero-title">祈愿手作定制手串退换政策</text>
      <text class="hero-sub">购买前请仔细阅读 · AFTER-SALES</text>
    </view>

    <view class="section">
      <view class="section-head warn">
        <text class="section-no">一</text>
        <text class="section-title">无法退换 · 天然水晶默认微瑕</text>
      </view>
      <text class="p">
        本店材质包含天然水晶及玉石，默认微瑕。矿坑、划痕、棉絮、扣手、黑点、冰片等自然形成的表皮及内里产物，不属于质量问题，无法退换。完美主义者慎拍哦！
      </text>
    </view>

    <view class="section">
      <view class="section-head warn">
        <text class="section-no">二</text>
        <text class="section-title">无法退换 · 定制商品不支持无理由退换</text>
      </view>
      <view class="bullets">
        <text class="li">商品为定制商品，一经发货后，不支持无理由退换货。</text>
        <text class="li">下单后想看实物图请联系客服查看实物图片。</text>
        <text class="li">如不确定手围请联系客服！付款前请务必确认自己的手围与手串手围一致，尺寸不符需自行改款或付费维修。</text>
        <text class="li">实物图均为自然光下一物一拍，有需要可以联系小程序客服在未发货前索要任意角度视频/图片。一经实物图确认后，无法因色差原因退换，建议勿拍哦！</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head ok">
        <text class="section-no">三</text>
        <text class="section-title">可退范围 · 请保证内外包装完整</text>
      </view>
      <view class="bullets">
        <text class="li">未发货可随心退换，24小时内自动退回到原账户，不会存在退款中已发货的问题。</text>
        <text class="li">珠子存在打开碎开，或配饰存在质量问题，凭开箱视频48小时内随心退换。</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-no">四</text>
        <text class="section-title">维修服务 · 订单一年内有效期</text>
      </view>
      <text class="p">水晶手串内置赠送备用线及消磁石，可用于自行改款，也可以联系客服寄回帮忙改款。</text>
      <text class="p">保修期内，如出现绳子断开、配饰氧化等老化问题，请联系客服进行维修，邮费需自付。</text>
      <view class="tip-box">
        <text class="tip-line">客服时间：12:00—23:00</text>
        <text class="tip-line">如对本售后须知有疑问，请联系客服咨询。</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-no">·</text>
        <text class="section-title">关于材质</text>
      </view>
      <text class="note">（下文代指水晶、玉、石及各类珠宝）</text>
      <view class="bullets">
        <text class="li">本店在售材质均为天然，部分材质可能会因其结构特殊性存在国检允许的优化工艺，介意勿拍。</text>
        <text class="li">天然水晶默认微瑕，棉絮/冰裂/平头/矿坑等属自然印记，非质量问题，详情请看售后政策。</text>
        <text class="li">实物因拍摄光线、设备显色差异可能存在色差，请以包容心迎接天然独特之美。</text>
        <text class="li">配件默认为高保色合金，长期遇水/汗可能氧化变色，非质量问题，如有需求可联系客服自费更换。</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-no">·</text>
        <text class="section-title">发货与快递</text>
      </view>
      <view class="bullets">
        <text class="li">默认快递：中通快递</text>
        <text class="li">发货时效：由于多达数百种珠子需人工挑选，有较长的配货时长，按照订单提交顺序进行配货。具体时间咨询客服。</text>
        <text class="li">顺丰加急：可联系客服补快递差价改顺丰。</text>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <text class="section-no">·</text>
        <text class="section-title">其他</text>
      </view>
      <text class="p">
        DIY珠子尺寸默认误差 0.4mm，如10mm代表尺寸在9.6mm至10.4mm之间。配货中会尽可能寻找尺寸相近的珠子搭配。
      </text>
    </view>

    <view class="footer">
      <text class="copyright">© 祈愿手作 · 天然水晶定制</text>
    </view>
  </view>
</template>

<script setup>
</script>

<style lang="scss" scoped>
@import '../../styles/theme.scss';

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx 28rpx 48rpx;
  background:
    $page-mist,
    linear-gradient(180deg, $page-bg 0%, $page-bg-deep 40%, $page-bg 100%);
}

.hero {
  background: $gradient-banner;
  border-radius: $radius-card;
  padding: 36rpx 28rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid $border-glass;
  box-shadow: $shadow-card-soft;
}
.hero-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: $text-on-soft;
  line-height: 1.4;
}
.hero-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: rgba(74, 58, 120, 0.55);
  letter-spacing: 1rpx;
}

.section {
  background: rgba(255, 255, 255, 0.94);
  border-radius: $radius-card;
  padding: 28rpx 24rpx;
  margin-bottom: 16rpx;
  box-shadow: $shadow-card-soft;
  border: 1rpx solid $border-glass;
}
.section-head {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 16rpx;
  padding-bottom: 14rpx;
  border-bottom: 1rpx solid rgba(139, 92, 246, 0.08);
}
.section-no {
  flex-shrink: 0;
  min-width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  border-radius: 12rpx;
  background: $icon-orb;
  color: $primary;
  font-size: 22rpx;
  font-weight: 700;
}
.section-head.warn .section-no {
  background: rgba(255, 237, 213, 0.9);
  color: #C2410C;
}
.section-head.ok .section-no {
  background: $success-soft;
  color: #15803D;
}
.section-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 700;
  color: $text-main;
  line-height: 1.45;
}
.p {
  display: block;
  font-size: 26rpx;
  color: $text-sub;
  line-height: 1.75;
  margin-bottom: 12rpx;
}
.p:last-child { margin-bottom: 0; }
.note {
  display: block;
  font-size: 22rpx;
  color: $text-hint;
  margin-bottom: 12rpx;
}
.bullets {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}
.li {
  position: relative;
  display: block;
  padding-left: 24rpx;
  font-size: 26rpx;
  color: $text-sub;
  line-height: 1.7;
}
.li::before {
  content: '';
  position: absolute;
  left: 4rpx;
  top: 16rpx;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: $primary-muted;
}
.tip-box {
  margin-top: 8rpx;
  padding: 18rpx 20rpx;
  border-radius: 16rpx;
  background: $primary-soft;
  border: 1rpx solid rgba(139, 92, 246, 0.12);
}
.tip-line {
  display: block;
  font-size: 24rpx;
  color: $text-on-soft;
  line-height: 1.7;
}
.footer {
  text-align: center;
  padding: 28rpx 0 8rpx;
}
.copyright {
  font-size: 22rpx;
  color: $text-hint;
}
</style>
