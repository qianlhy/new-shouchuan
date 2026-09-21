// API配置文件 - 根据后端接口文档配置
// 基于 api-documentation.json

// 开发环境 - 根据接口文档
// 统一指向云托管后端地址，方便开发与真机调试
const DEV_API_BASE_URL = 'https://xuyuan.baibaiyeye.com.cn'
// const DEV_API_BASE_URL = "http://localhost:8080"

// 生产环境
const PROD_API_BASE_URL = 'https://xuyuan.baibaiyeye.com.cn'

// 判断当前环境
const isDevelopment = process.env.NODE_ENV === 'development'

// 导出当前使用的API地址
export const API_BASE_URL = isDevelopment ? DEV_API_BASE_URL : PROD_API_BASE_URL

// JWT Token认证配置
export const TOKEN_HEADER = 'authentication'  // 请求头中token的key
export const STORAGE_TOKEN_KEY = 'token'       // 本地存储token的key
export const STORAGE_USER_KEY = 'user'         // 本地存储用户信息的key

// API接口路径 - 根据接口文档
export const API_PATHS = {
  // ==================== 用户登录模块 ====================
  // 微信登录
  WECHAT_LOGIN: '/user/user/login',
  // 查询当前用户信息（用于token失效时恢复登录）
  USER_INFO: '/user/user/info',

  // ==================== 分类模块 ====================
  // 查询分类列表
  CATEGORY_LIST: '/user/category/list',
  
  // ==================== 商品模块 ====================
  // 根据分类查询商品列表
  PRODUCT_LIST: '/user/product/list',
  // 查询商品详情
  PRODUCT_DETAIL: '/user/product/detail',
  
  // ==================== 购物车模块 ====================
  // 添加商品到购物车
  CART_ADD: '/user/cart/add',
  // 查看购物车
  CART_LIST: '/user/cart/list',
  // 删除购物车商品
  CART_DELETE: '/user/cart/delete',
  // 清空购物车
  CART_CLEAN: '/user/cart/clean',
  // 减少商品数量
  CART_SUB: '/user/cart/sub',
  // 更新 DIY 设计（重新设计保存）
  CART_UPDATE_DIY: '/user/cart/updateDiy',
  
  // ==================== 订单模块 ====================
  // 提交订单
  // 注意：原接口 /user/order/submit 返回404，暂时使用 /user/order/create 替代
  // 前端需确保先将商品加入购物车
  ORDER_SUBMIT: '/user/order/create',
  // 从购物车创建订单
  ORDER_CREATE: '/user/order/create',
  // 订单支付
  ORDER_PAYMENT: '/user/order/payment',
  // 查询订单支付状态
  ORDER_PAYMENT_STATUS: '/user/order/paymentStatus',
  // 分页查询订单列表
  ORDER_LIST: '/user/order/list',
  // 查询订单详情
  ORDER_DETAIL: '/user/order/detail',
  // 历史订单查询
  ORDER_HISTORY: '/user/order/historyOrders',
  // 查询订单详情（旧接口）
  ORDER_DETAIL_BY_ID: '/user/order/orderDetail',
  // 取消订单
  ORDER_CANCEL: '/user/order/cancel',
  // 确认收货
  ORDER_COMPLETE: '/user/order/complete',
  // 再来一单
  ORDER_REPETITION: '/user/order/repetition',
  // 催单
  ORDER_REMINDER: '/user/order/reminder',
    // 修改订单地址
  ORDER_UPDATE_ADDRESS: '/user/order/updateAddress',
  
  // ==================== 管理员模块 ====================
  // 订单发货
  ADMIN_ORDER_DELIVERY: '/admin/order/deliveryWithTrackingNumber',

  // ==================== 轮播图模块 ====================
  // 查询轮播图列表
  BANNER_LIST: '/user/banner/list',
  
  // ==================== 店铺模块 ====================
  // 获取店铺营业状态
  SHOP_STATUS: '/user/shop/status',
  
  // ==================== DIY设计模块 ====================
  // 查询DIY分类列表
  DIY_CATEGORY_LIST: '/user/design/category/list',
  // 查询色系列表
  DIY_COLOR_SERIES_LIST: '/user/design/colorSeries/list',
  // 查询DIY材料列表（支持分类和色系筛选）
  DIY_MATERIAL_LIST: '/user/design/material/list',
  // 从DIY设计创建订单
  DIY_ORDER_CREATE: '/user/design/order/create',

  // ==================== 地址管理模块 ====================
  // 添加地址
  ADDRESS_ADD: '/user/address/add',
  // 修改地址
  ADDRESS_UPDATE: '/user/address/update',
  // 查询地址列表
  ADDRESS_LIST: '/user/address/list',
  // 获取默认地址
  ADDRESS_DEFAULT: '/user/address/default',
  // 删除地址
  ADDRESS_DELETE: '/user/address/delete',
  // 设置默认地址
  ADDRESS_SET_DEFAULT: '/user/address/setDefault',

  // ==================== 心愿众筹模块（广场） ====================
  // 切换想要状态
  WISH_TOGGLE: '/user/wish/toggle',
  // 批量查询众筹进度（公开）
  WISH_COUNTS: '/user/wish/counts',
  // 我想要的商品列表（我的收藏）
  WISH_MINE: '/user/wish/mine',

  // ==================== 灵感广场作品 ====================
  SQUARE_SUBMIT: '/user/square/submit',
  SQUARE_LIST: '/user/square/list',
  SQUARE_DETAIL: '/user/square/detail',

  // ==================== 会员中心模块 ====================
  // 获取当前用户会员信息
  MEMBER_INFO: '/user/member/info'
}

// 请求超时时间（毫秒）
export const REQUEST_TIMEOUT = 60000

// 请求头配置
export const REQUEST_HEADERS = {
  'Content-Type': 'application/json'
}

// 订单状态枚举
export const ORDER_STATUS = {
  PENDING: 0,      // 待支付
  PAID: 1,         // 已支付
  SHIPPED: 2,      // 已发货
  COMPLETED: 3     // 已完成
}

// 订单状态文本
export const ORDER_STATUS_TEXT = {
  0: '待支付',
  1: '已支付',
  2: '已发货',
  3: '已完成'
}

// 店铺状态枚举
export const SHOP_STATUS = {
  CLOSED: 0,       // 已打样
  OPEN: 1          // 营业中
}

// 商品状态枚举
export const PRODUCT_STATUS = {
  OFFLINE: 0,      // 下架
  ONLINE: 1        // 上架
}

// 响应码
export const RESPONSE_CODE = {
  SUCCESS: 1,      // 成功 (旧接口)
  FAIL: 0,         // 失败 (旧接口)
  SUCCESS_NEW: 0   // 成功 (新接口 /design/)
}
