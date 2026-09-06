/**
 * API接口统一导出文件
 * - 真实后端API从 ./api.js 导入
 * - 设计台mock功能保留在此文件
 * - 地址管理本地存储功能保留在此文件
 */

// ==================== 导入真实后端API ====================
import { STORAGE_TOKEN_KEY, STORAGE_USER_KEY } from '../config.js'
import { addAddress, deleteAddress, getAddressList, getDefaultAddress, setDefaultAddress, wechatLogin } from './api.js'

export {
addAddress,
    // 购物车
    addToCart, bannerList, cancelOrder, // Refund API
    cancelRefundOrder, cartAdd, cartDelete, cartList, cartUpdate, categoryList, checkPaymentStatus, clearCart, completeOrder, createDiyOrder, createOrderFromCart, deleteAddress, deleteCartItem, // Cancel Refund API
    // 管理员
    deliveryOrder, designCategoryList, designOrderCreate, designProductList, getAddressList,
    // 轮播图
    getBannerList, getCartList,
    // 分类
    getCategoryList, getCurrentUserInfo, getDefaultAddress, getHistoryOrders, getOrderDetail, getOrderDetailById, getOrderList, getProductDetail,
    // 商品
    getProductList,
    // 店铺
    getShopStatus, loginWithWeixinCode, orderCreate, orderDetail, orderList, orderPay, payOrder, productDetail, productList, refundOrder, reminderOrder, repetitionOrder, setDefaultAddress, subCartItem,
    // 订单
    submitOrder, updateOrderAddress, uploadFile,
    // 登录
    wechatLogin
} from './api.js'


// ==================== 本地存储工具 ====================
const promise = (data) => Promise.resolve(data)

// ==================== 用户相关工具函数 ====================
export const userGet = () => {
  try {
    return uni.getStorageSync(STORAGE_USER_KEY) || null
  } catch (e) {
    return null
  }
}

export const userSet = (u) => {
  try {
    uni.setStorageSync(STORAGE_USER_KEY, u || null)
  } catch (e) {
    console.error('保存用户信息失败:', e)
  }
}

export const tokenGet = () => {
  try {
    return uni.getStorageSync(STORAGE_TOKEN_KEY) || ''
  } catch (e) {
    return ''
  }
}

// 检查是否已登录
export const isLoggedIn = () => {
  const user = userGet()
  const token = tokenGet()
  return !!(user && token)
}

// 要求登录（如果未登录则跳转到首页）
export const requireLogin = () => {
  if (!isLoggedIn()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index?login=1' })
    }, 1500)
    return false
  }
  return true
}

// 退出登录
export const logout = async () => {
  try {
    uni.removeStorageSync(STORAGE_USER_KEY)
    uni.removeStorageSync(STORAGE_TOKEN_KEY)
    return promise({ ok: true, message: '退出登录成功' })
  } catch (e) {
    console.error('退出登录失败', e)
    return promise({ ok: false, message: '退出登录失败' })
  }
}


// ==================== 地址管理适配层 ====================

// 后端 -> 前端 字段映射
const fromApiAddress = (addr) => {
  if (!addr) return null
  return {
    id: addr.id,
    name: addr.consignee || '',
    phone: addr.phone || '',
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detail: addr.detailAddress || '',
    isDefault: addr.isDefault // 0 or 1
  }
}

// 前端 -> 后端 字段映射
const toApiAddress = (data) => {
  return {
    id: data.id, // 如果有id则是修改
    consignee: data.name,
    phone: data.phone,
    province: data.province,
    city: data.city,
    district: data.district,
    detailAddress: data.detail,
    isDefault: data.isDefault ? 1 : 0
  }
}

export const addressList = () => {
  return getAddressList().then(list => {
    return list.map(fromApiAddress)
  })
}

export const addressDetail = (id) => {
  // 由于没有详情接口，先获取列表再查找
  return getAddressList().then(list => {
    const addr = list.find(a => a.id === Number(id))
    return fromApiAddress(addr) || null
  })
}

export const addressAdd = (data) => {
  const apiData = toApiAddress(data)
  // 如果是添加，id可能不需要，或者由后端生成，但如果data里没有id则没问题
  return addAddress(apiData).then(res => ({ ok: true, data: res }))
}

export const addressUpdate = (id, data) => {
  // 复用 addAddress，确保 id 存在
  const apiData = toApiAddress({ ...data, id })
  return addAddress(apiData).then(res => ({ ok: true, data: res }))
}

export const addressDelete = (id) => {
  return deleteAddress(id).then(() => ({ ok: true }))
}

export const addressSetDefault = (id) => {
  return setDefaultAddress(id).then(() => ({ ok: true }))
}

export const addressGetDefault = () => {
  return getDefaultAddress().then(res => fromApiAddress(res))
}

// ==================== 自动登录功能 ====================

/**
 * 静默自动登录
 * 流程：
 * 1. 检查本地是否有token，有则直接返回
 * 2. 没有token则调用微信登录获取code
 * 3. 用code调用后端登录接口，后端会查询数据库是否有该用户
 * 4. 如果有用户，返回用户信息；如果没有，返回需要授权
 * @returns {Promise<boolean>} 是否登录成功
 */
export const silentAutoLogin = () => {
  return new Promise((resolve) => {
    console.log('[SilentLogin] 开始静默自动登录')
    
    // 1. 检查本地token
    const token = tokenGet()
    console.log('[SilentLogin] 本地token:', token ? '存在' : '不存在')
    if (token) {
      console.log('[SilentLogin] 本地已有token，无需登录')
      resolve(true)
      return
    }
    
    // 2. 调用微信登录获取code
    console.log('[SilentLogin] 调用uni.login获取微信code')
    uni.login({
      provider: 'weixin',
      success: async (loginRes) => {
        console.log('[SilentLogin] uni.login成功:', loginRes)
        if (loginRes.code) {
          try {
            // 3. 调用后端登录接口（不传用户信息，让后端查询数据库）
            console.log('[SilentLogin] 调用wechatLogin，code:', loginRes.code.substring(0, 10) + '...')
            const res = await wechatLogin(loginRes.code, {})
            console.log('[SilentLogin] wechatLogin返回:', res)
            if (res && res.token) {
              console.log('[SilentLogin] 登录成功，用户信息:', res)
              resolve(true)
            } else {
              console.log('[SilentLogin] 登录失败：未获取到token，返回数据:', res)
              resolve(false)
            }
          } catch (e) {
            console.error('[SilentLogin] 登录失败，异常:', e)
            resolve(false)
          }
        } else {
          console.log('[SilentLogin] 获取微信code失败，loginRes:', loginRes)
          resolve(false)
        }
      },
      fail: (err) => {
        console.error('[SilentLogin] 微信登录调用失败:', err)
        resolve(false)
      }
    })
  })
}

/**
 * 检查并执行自动登录
 * 用于App.vue中应用启动时调用
 * @returns {Promise<boolean>}
 */
export const checkAndAutoLogin = async () => {
  console.log('[CheckAndAutoLogin] 开始检查登录状态')
  // 如果已经登录，不执行
  if (isLoggedIn()) {
    console.log('[CheckAndAutoLogin] 已经登录，跳过')
    return true
  }
  
  console.log('[CheckAndAutoLogin] 未登录，尝试静默登录')
  // 尝试静默登录
  const result = await silentAutoLogin()
  console.log('[CheckAndAutoLogin] 静默登录结果:', result)
  return result
}

