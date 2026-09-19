<template>
  <div class="main-layout">
    <div class="sidebar">
      <div class="brand">
        <img class="brand-mark" src="/qiyuan_logo.png" alt="祈愿手作" />
        <div class="brand-text">
          <div class="brand-name">祈愿手作</div>
          <div class="brand-sub">后台管理</div>
        </div>
      </div>
      <div class="user-info">
        <el-avatar :size="36" :src="userInfo.avatar"></el-avatar>
        <span class="username">{{ userInfo.displayName }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        background-color="#2A2140"
        text-color="#C4B5FD"
        active-text-color="#FFFFFF"
        router>
        <el-menu-item index="/home/welcome">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/home/banner">
          <i class="el-icon-picture-outline"></i>
          <span>轮播图管理</span>
        </el-menu-item>
        <el-menu-item index="/home/category">
          <i class="el-icon-menu"></i>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/home/product">
          <i class="el-icon-goods"></i>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/home/diy-material">
          <i class="el-icon-bangzhu"></i>
          <span>DIY材料管理</span>
        </el-menu-item>
        <el-menu-item index="/home/order">
          <i class="el-icon-s-order"></i>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/home/customer-service">
          <i class="el-icon-service"></i>
          <span>客服二维码</span>
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          <i class="el-icon-switch-button"></i>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="main-content">
      <router-view></router-view>
      <div class="footer">
        <a href="https://beian.miit.gov.cn/" target="_blank" class="beian-link">
          蜀ICP备2025145558号
        </a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MainLayout',
  data () {
    return {
      userInfo: {
        displayName: localStorage.getItem('username') || '管理员',
        avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      },
      activeMenu: this.$route.path || '/home/welcome'
    }
  },
  watch: {
    '$route.path' (val) {
      this.activeMenu = val
    }
  },
  methods: {
    handleLogout () {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  text-align: left;
}

.sidebar {
  width: 228px;
  background: linear-gradient(180deg, #2A2140 0%, #1F1830 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  box-shadow: 8px 0 32px rgba(42, 33, 64, 0.18);
  z-index: 2;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 18px 18px;
  border-bottom: 1px solid rgba(196, 181, 253, 0.15);
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: #fff;
  object-fit: contain;
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.32);
  flex-shrink: 0;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  letter-spacing: 1px;
}

.brand-sub {
  margin-top: 3px;
  font-size: 12px;
  color: #A78BFA;
}

.user-info {
  margin: 14px 12px;
  padding: 12px 12px;
  display: flex;
  align-items: center;
  border-radius: 12px;
  background: rgba(139, 92, 246, 0.14);
  border: 1px solid rgba(196, 181, 253, 0.18);
}

.username {
  margin-left: 10px;
  font-size: 14px;
  color: #EDE4FF;
  font-weight: 500;
}

.menu {
  border-right: none;
  flex: 1;
  padding: 4px 0 16px;
  background: transparent !important;
}

.menu >>> .el-menu-item {
  margin: 4px 10px;
  border-radius: 10px;
  height: 46px;
  line-height: 46px;
}

.menu >>> .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(139, 92, 246, 0.5), rgba(139, 92, 246, 0.14)) !important;
  border-right: none;
  box-shadow: inset 3px 0 0 #A78BFA;
}

.menu >>> .el-menu-item:hover {
  background: rgba(139, 92, 246, 0.18) !important;
}

.menu >>> .el-menu-item i {
  color: inherit;
  margin-right: 6px;
}

.main-content {
  flex: 1;
  padding: 22px 24px 12px;
  background:
    radial-gradient(ellipse 80% 50% at 10% -10%, rgba(183, 148, 255, 0.22), transparent 55%),
    radial-gradient(ellipse 60% 40% at 90% 0%, rgba(139, 92, 246, 0.12), transparent 50%),
    linear-gradient(180deg, #FBF7FF 0%, #F3EBFF 35%, #F7F4FC 100%);
  overflow: auto;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.footer {
  margin-top: auto;
  padding: 16px 20px 20px;
  text-align: center;
  color: #B5ADC6;
  font-size: 12px;
}

.beian-link {
  color: #909399;
  text-decoration: none;
}

.beian-link:hover {
  color: #8B5CF6;
  text-decoration: underline;
}
</style>
