<template>
  <div class="login-container">
    <div class="login-bg-orb o1"></div>
    <div class="login-bg-orb o2"></div>
    <div class="login-bg-orb o3"></div>
    <div class="login-box">
      <div class="login-brand">
        <img class="login-mark" src="/qiyuan_logo.png" alt="祈愿手作" />
        <h2>祈愿手作</h2>
        <p class="login-sub">后台管理系统</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" @keyup.enter.native="handleLogin"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <a href="https://beian.miit.gov.cn/" target="_blank" class="beian-link">
          蜀ICP备2025145558号
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import { adminLogin } from '@/api/admin'

export default {
  name: 'Login',
  data () {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async handleLogin () {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const res = await adminLogin(this.loginForm)
            localStorage.setItem('token', res.data.token)
            localStorage.setItem('username', this.loginForm.username)
            this.$message.success('登录成功')
            this.$router.push('/home')
          } catch (error) {
            console.error('登录失败:', error)
            this.$message.error('登录失败：' + (error.response?.data?.msg || '未知错误'))
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #FBF7FF 0%, #EDE4FF 45%, #D8C4FF 100%);
}

.login-bg-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.o1 {
  width: 420px;
  height: 420px;
  right: -80px;
  top: -100px;
  background: radial-gradient(circle, rgba(183, 148, 255, 0.55) 0%, transparent 70%);
}
.o2 {
  width: 280px;
  height: 280px;
  left: -60px;
  bottom: 40px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.28) 0%, transparent 70%);
}
.o3 {
  width: 160px;
  height: 160px;
  left: 30%;
  top: 18%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.7) 0%, transparent 70%);
}

.login-box {
  width: 420px;
  padding: 36px 34px 28px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  box-shadow: 0 18px 48px rgba(90, 50, 160, 0.14);
  border: 1px solid rgba(139, 92, 246, 0.12);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(8px);
  text-align: left;
}

.login-brand {
  text-align: center;
  margin-bottom: 28px;
}
.login-mark {
  width: 96px;
  height: 96px;
  margin: 0 auto 14px;
  border-radius: 20px;
  background: #fff;
  object-fit: contain;
  box-shadow: 0 10px 24px rgba(139, 92, 246, 0.28);
  display: block;
}
.login-box h2 {
  margin: 0;
  font-size: 26px;
  color: #2A2140;
  letter-spacing: 2px;
}
.login-sub {
  margin: 8px 0 0;
  font-size: 13px;
  color: #8B849C;
}

.login-btn {
  width: 100%;
  background: linear-gradient(90deg, #B794FF, #8B5CF6 55%, #7C3AED) !important;
  border: none !important;
  font-weight: 600;
}

.footer {
  margin-top: auto;
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding-top: 12px;
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
