<template>
  <view class="page">
    <!-- 搜索区域 - 仅在没有传入单号时显示 -->
    <view class="search-box" v-if="!isFromOrder">
      <view class="search-input-wrapper">
        <input
          class="search-input"
          v-model="trackingNumber"
          placeholder="请输入快递单号"
          confirm-type="search"
          @confirm="queryKuaidiInfo"
        />
        <view class="clear-btn" v-if="trackingNumber" @click="clearInput">
          <text class="clear-icon">×</text>
        </view>
      </view>
      <button class="search-btn" @click="queryKuaidiInfo" :disabled="loading">
        <text v-if="!loading">查询</text>
        <text v-else>查询中...</text>
      </button>
    </view>

    <!-- 快递公司选择 - 仅在没有传入单号时显示 -->
    <view class="company-section" v-if="!isFromOrder && companiesList.length > 0">
      <view class="section-title">选择快递公司（可选）</view>
      <scroll-view class="company-list" scroll-x>
        <view
          class="company-item"
          v-for="item in companiesList"
          :key="item.code"
          :class="{ active: selectedCompany === item.code }"
          @click="selectCompany(item.code)"
        >
          <text class="company-name">{{ item.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 查询结果 -->
    <view class="result-section" v-if="kuaidiInfo">
      <!-- 快递状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="statusClass">
            <text class="icon-text">{{ statusIcon }}</text>
          </view>
          <view class="status-info">
            <text class="status-text">{{ kuaidiInfo.stateDesc || '查询中' }}</text>
            <text class="company-text">{{ companyName }} {{ trackingNumber }}</text>
          </view>
        </view>
      </view>

      <!-- 物流轨迹 -->
      <view class="timeline-section">
        <view class="section-title">物流轨迹</view>
        <view class="timeline">
          <view
            class="timeline-item"
            v-for="(item, index) in kuaidiInfo.data"
            :key="index"
            :class="{ first: index === 0 }"
          >
            <view class="timeline-left">
              <text class="time-date">{{ formatDate(item.time) }}</text>
              <text class="time-hour">{{ formatHour(item.time) }}</text>
            </view>
            <view class="timeline-line">
              <view class="timeline-dot" :class="{ active: index === 0 }"></view>
              <view class="timeline-connector" v-if="index !== kuaidiInfo.data.length - 1"></view>
            </view>
            <view class="timeline-content">
              <text class="timeline-text">{{ item.context }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="!kuaidiInfo && !loading && !errorMsg">
      <image class="empty-icon" src="/static/images/empty.png" mode="aspectFit"></image>
      <text class="empty-text">输入快递单号查询物流信息</text>
    </view>

    <!-- 错误提示 -->
    <view class="error-state" v-if="errorMsg">
      <text class="error-text">{{ errorMsg }}</text>
    </view>
  </view>
</template>

<script setup>
import { getKuaidiCompanies, queryKuaidi } from '@/api/api.js'
import { onLoad } from '@dcloudio/uni-app'
import { computed, onMounted, ref } from 'vue'

// 响应式数据
const trackingNumber = ref('')
const phone = ref('')  // 收件人手机号
const loading = ref(false)
const kuaidiInfo = ref(null)
const errorMsg = ref('')
const companies = ref({})
const selectedCompany = ref('')
const isFromOrder = ref(false)  // 是否从订单页面跳转过来

// 页面加载时接收参数
onLoad((options) => {
  if (options && options.num) {
    trackingNumber.value = options.num
    phone.value = options.phone || ''  // 接收手机号
    isFromOrder.value = true  // 标记为从订单页面跳转
    // 自动查询
    queryKuaidiInfo()
  }
})

// 快递公司列表
const companiesList = computed(() => {
  return Object.entries(companies.value).map(([code, name]) => ({
    code,
    name
  }))
})

// 快递公司名称
const companyName = computed(() => {
  if (!kuaidiInfo.value) return ''
  const com = kuaidiInfo.value.com
  return companies.value[com] || com || '未知快递'
})

// 状态样式
const statusClass = computed(() => {
  if (!kuaidiInfo.value) return ''
  const state = parseInt(kuaidiInfo.value.state)
  switch (state) {
    case 0: return 'transit'
    case 1: return 'pickup'
    case 3: return 'signed'
    case 5: return 'delivering'
    case 4:
    case 6: return 'returned'
    default: return 'transit'
  }
})

// 状态图标
const statusIcon = computed(() => {
  if (!kuaidiInfo.value) return '?'
  const state = parseInt(kuaidiInfo.value.state)
  switch (state) {
    case 0: return '运'
    case 1: return '揽'
    case 3: return '签'
    case 5: return '派'
    case 4:
    case 6: return '退'
    default: return '?'
  }
})

// 查询快递信息
async function queryKuaidiInfo() {
  if (!trackingNumber.value.trim()) {
    uni.showToast({ title: '请输入快递单号', icon: 'none' })
    return
  }

  loading.value = true
  errorMsg.value = ''
  kuaidiInfo.value = null

  try {
    const result = await queryKuaidi(trackingNumber.value.trim(), selectedCompany.value, phone.value)
    console.log('快递查询结果:', result)
    
    if (result) {
      // 检查返回的数据是否有效
      if (result.message && result.message.includes('数据不完整')) {
        // 单号不存在或无效
        errorMsg.value = '未查询到该快递单号的物流信息，请检查单号是否正确'
      } else if (result.data && Array.isArray(result.data) && result.data.length > 0) {
        // 有物流轨迹数据，正常显示
        kuaidiInfo.value = result
      } else if (result.message === 'ok') {
        // 查询成功但暂无物流轨迹
        errorMsg.value = '暂无物流信息，请稍后再试'
      } else {
        errorMsg.value = '未查询到物流信息'
      }
    } else {
      errorMsg.value = '未查询到物流信息'
    }
  } catch (e) {
    console.error('查询快递失败:', e)
    errorMsg.value = e.message || '查询失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 清空输入
function clearInput() {
  trackingNumber.value = ''
  kuaidiInfo.value = null
  errorMsg.value = ''
  selectedCompany.value = ''
}

// 选择快递公司
function selectCompany(code) {
  if (selectedCompany.value === code) {
    selectedCompany.value = ''
  } else {
    selectedCompany.value = code
  }
}

// 格式化日期
function formatDate(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 格式化时间
function formatHour(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 加载快递公司列表
async function loadCompanies() {
  try {
    const res = await getKuaidiCompanies()
    if (res) {
      companies.value = res
    }
  } catch (e) {
    console.error('加载快递公司列表失败:', e)
  }
}

// 页面加载
onMounted(() => {
  loadCompanies()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

// 搜索区域
.search-box {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;

  .search-input-wrapper {
    flex: 1;
    position: relative;
    background: #fff;
    border-radius: 12rpx;
    display: flex;
    align-items: center;

    .search-input {
      flex: 1;
      height: 80rpx;
      padding: 0 80rpx 0 30rpx;
      font-size: 28rpx;
    }

    .clear-btn {
      position: absolute;
      right: 20rpx;
      top: 50%;
      transform: translateY(-50%);
      width: 40rpx;
      height: 40rpx;
      background: #ddd;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      .clear-icon {
        color: #fff;
        font-size: 28rpx;
        line-height: 1;
      }
    }
  }

  .search-btn {
    width: 140rpx;
    height: 80rpx;
    line-height: 80rpx;
    background: #6B4EFF;
    color: #333;
    font-size: 28rpx;
    border-radius: 12rpx;
    margin: 0;
    padding: 0;

    &:disabled {
      opacity: 0.6;
    }

    &:active {
      opacity: 0.8;
    }
  }
}

// 快递公司选择
.company-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;

  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .company-list {
    white-space: nowrap;

    .company-item {
      display: inline-block;
      padding: 16rpx 30rpx;
      margin-right: 16rpx;
      background: #f5f5f5;
      border-radius: 30rpx;
      border: 2rpx solid transparent;

      &.active {
        background: #fff5d6;
        border-color: #6B4EFF;
      }

      .company-name {
        font-size: 26rpx;
        color: #666;
      }

      &.active .company-name {
        color: #333;
        font-weight: bold;
      }
    }
  }
}

// 状态卡片
.status-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .status-header {
    display: flex;
    align-items: center;

    .status-icon {
      width: 100rpx;
      height: 100rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 30rpx;

      &.transit { background: #e6f7ff; }
      &.pickup { background: #fff7e6; }
      &.signed { background: #f6ffed; }
      &.delivering { background: #fff2e8; }
      &.returned { background: #fff1f0; }

      .icon-text {
        font-size: 40rpx;
        font-weight: bold;

        .transit & { color: #1890ff; }
        .pickup & { color: #faad14; }
        .signed & { color: #52c41a; }
        .delivering & { color: #fa8c16; }
        .returned & { color: #ff4d4f; }
      }
    }

    .status-info {
      flex: 1;

      .status-text {
        display: block;
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 10rpx;
      }

      .company-text {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}

// 物流轨迹
.timeline-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;

  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
  }

  .timeline {
    .timeline-item {
      display: flex;
      padding-bottom: 40rpx;
      position: relative;

      &.first {
        .timeline-text {
          color: #333;
          font-weight: 500;
        }
      }

      .timeline-left {
        width: 120rpx;
        text-align: center;
        flex-shrink: 0;

        .time-date {
          display: block;
          font-size: 24rpx;
          color: #999;
          margin-bottom: 6rpx;
        }

        .time-hour {
          display: block;
          font-size: 22rpx;
          color: #bbb;
        }
      }

      .timeline-line {
        width: 40rpx;
        display: flex;
        flex-direction: column;
        align-items: center;
        flex-shrink: 0;

        .timeline-dot {
          width: 20rpx;
          height: 20rpx;
          border-radius: 50%;
          background: #ddd;

          &.active {
            background: #6B4EFF;
            box-shadow: 0 0 0 8rpx rgba(245, 201, 58, 0.2);
          }
        }

        .timeline-connector {
          flex: 1;
          width: 2rpx;
          background: #eee;
          margin-top: 10rpx;
        }
      }

      .timeline-content {
        flex: 1;
        padding-left: 20rpx;

        .timeline-text {
          font-size: 28rpx;
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;

  .empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 30rpx;
    opacity: 0.5;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

// 错误状态
.error-state {
  display: flex;
  justify-content: center;
  padding: 100rpx 40rpx;

  .error-text {
    font-size: 28rpx;
    color: #ff4d4f;
  }
}
</style>
