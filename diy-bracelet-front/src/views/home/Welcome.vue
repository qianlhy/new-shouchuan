<template>
  <div class="xy-page welcome-page">
    <div class="welcome-hero">
      <p class="welcome-en">QIYUAN ADMIN</p>
      <h2 class="welcome-title">欢迎使用祈愿手作后台管理</h2>
      <p class="welcome-subtitle">{{ greeting }}，{{ userInfo.displayName || '管理员' }}</p>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stats-card">
          <div class="xy-icon-orb"><i class="el-icon-picture-outline"></i></div>
          <div class="stats-info">
            <div class="stats-title">轮播图</div>
            <div class="stats-value">{{ stats.banners || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card">
          <div class="xy-icon-orb"><i class="el-icon-menu"></i></div>
          <div class="stats-info">
            <div class="stats-title">商品分类</div>
            <div class="stats-value">{{ stats.categories || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card">
          <div class="xy-icon-orb"><i class="el-icon-goods"></i></div>
          <div class="stats-info">
            <div class="stats-title">商品数量</div>
            <div class="stats-value">{{ stats.products || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stats-card">
          <div class="xy-icon-orb"><i class="el-icon-s-order"></i></div>
          <div class="stats-info">
            <div class="stats-title">待处理订单</div>
            <div class="stats-value">{{ stats.pendingOrders || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="section-block">
      <h3 class="section-title">快捷操作</h3>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-button type="primary" class="xy-btn-gradient quick-btn" icon="el-icon-picture-outline" @click="$router.push('/home/banner')">轮播图管理</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" class="xy-btn-gradient quick-btn" icon="el-icon-menu" @click="$router.push('/home/category')">分类管理</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" class="xy-btn-gradient quick-btn" icon="el-icon-goods" @click="$router.push('/home/product')">商品管理</el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" class="xy-btn-gradient quick-btn" icon="el-icon-s-order" @click="$router.push('/home/order')">订单管理</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="section-block">
      <h3 class="section-title">使用提示</h3>
      <div class="xy-panel tips-panel">
        <ul>
          <li>点击左侧菜单可以快速切换不同功能模块</li>
          <li>可以通过轮播图管理首页展示图片</li>
          <li>商品分类管理帮助您更好地组织商品</li>
          <li>订单管理可以查看和处理用户订单</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import { getBannerList, getCategoryList, getProductList, getOrderList } from '@/api/admin'

export default {
  name: 'Welcome',
  data () {
    return {
      userInfo: {
        username: localStorage.getItem('username') || '',
        displayName: localStorage.getItem('username') || '管理员'
      },
      stats: {
        banners: 0,
        categories: 0,
        products: 0,
        pendingOrders: 0
      },
      greeting: ''
    }
  },
  created () {
    this.updateGreeting()
    this.fetchStats()
  },
  methods: {
    updateGreeting () {
      const hour = new Date().getHours()
      if (hour < 6) {
        this.greeting = '凌晨好'
      } else if (hour < 9) {
        this.greeting = '早上好'
      } else if (hour < 12) {
        this.greeting = '上午好'
      } else if (hour < 14) {
        this.greeting = '中午好'
      } else if (hour < 18) {
        this.greeting = '下午好'
      } else if (hour < 22) {
        this.greeting = '晚上好'
      } else {
        this.greeting = '深夜好'
      }
    },
    async fetchStats () {
      try {
        const bannerResponse = await getBannerList()
        this.stats.banners = bannerResponse.data.banners ? bannerResponse.data.banners.length : 0

        const categoryResponse = await getCategoryList()
        this.stats.categories = categoryResponse.data.categories ? categoryResponse.data.categories.length : 0

        const productResponse = await getProductList()
        this.stats.products = productResponse.data.products ? productResponse.data.products.length : 0

        const orderResponse = await getOrderList({ status: 0, page: 1, size: 50 })
        this.stats.pendingOrders = orderResponse.data.orders ? orderResponse.data.orders.length : 0
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.welcome-page {
  max-width: 1100px;
}

.welcome-hero {
  padding: 28px 32px;
  margin-bottom: 20px;
  border-radius: 18px;
  background: linear-gradient(145deg, #FBF7FF 0%, #F0E6FF 45%, #E4D4FF 100%);
  border: 1px solid rgba(139, 92, 246, 0.12);
  box-shadow: 0 12px 32px rgba(90, 50, 160, 0.07);
}

.welcome-en {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 3px;
  color: #A78BFA;
  font-weight: 600;
}

.welcome-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #2A2140;
  letter-spacing: 1px;
}

.welcome-subtitle {
  margin: 10px 0 0;
  font-size: 15px;
  color: #8B849C;
}

.stats-row {
  margin-bottom: 8px;
}

.stats-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 18px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(139, 92, 246, 0.1);
  box-shadow: 0 8px 24px rgba(90, 50, 160, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(90, 50, 160, 0.1);
}

.stats-title {
  font-size: 13px;
  color: #8B849C;
  margin-bottom: 6px;
}

.stats-value {
  font-size: 26px;
  color: #2A2140;
  font-weight: 700;
  line-height: 1;
}

.section-block {
  margin-top: 12px;
  margin-bottom: 8px;
}

.section-title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: #2A2140;
}

.quick-btn {
  width: 100%;
  margin-bottom: 12px;
  border-radius: 999px !important;
  height: 40px;
}

.tips-panel {
  background: linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 100%) !important;
  margin-bottom: 0;
}

.tips-panel ul {
  padding-left: 20px;
  margin: 0;
}

.tips-panel li {
  line-height: 2;
  color: #4A3A78;
  font-size: 14px;
}
</style>
