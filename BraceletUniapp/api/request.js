/**
 * HTTP请求封装 - 统一处理JWT认证和错误
 */

import {
  API_BASE_URL,
  TOKEN_HEADER,
  STORAGE_TOKEN_KEY,
  STORAGE_USER_KEY,
  REQUEST_TIMEOUT,
  REQUEST_HEADERS,
  RESPONSE_CODE
} from '../config.js'

const AUTH_EXPIRED_MSG = '登录已过期，请重新登录'
const LOGIN_REQUIRED_MSG = '请先登录'

/** 防止短时间内多次弹「登录过期」 */
let authHandling = false

/**
 * 获取本地存储的Token
 */
function getToken() {
  try {
    return uni.getStorageSync(STORAGE_TOKEN_KEY) || ''
  } catch (e) {
    console.error('获取Token失败:', e)
    return ''
  }
}

/**
 * 是否鉴权失败（HTTP 401 / 业务 401 / 文案含 token|未登录|登录）
 */
export function isAuthError(errorOrCode, msg) {
  const code = typeof errorOrCode === 'object' && errorOrCode !== null
    ? errorOrCode.code
    : errorOrCode
  const text = typeof errorOrCode === 'object' && errorOrCode !== null
    ? (errorOrCode.msg || errorOrCode.message || '')
    : (msg || '')
  if (code === 401) return true
  const lower = String(text).toLowerCase()
  return lower.includes('token') || text.includes('未登录') || text.includes('登录过期') || text.includes('登录已过期')
}

/**
 * 构造统一的鉴权错误对象（页面 catch 可用 e.msg / e.message）
 */
export function createAuthError(message = AUTH_EXPIRED_MSG) {
  const err = new Error(message)
  err.code = 401
  err.msg = message
  err.authExpired = true
  return err
}

/**
 * 处理登录失效：清本地凭证、提示、跳转首页登录
 * 可被 request / uploadFile 共用
 */
export function handleTokenExpired(message = AUTH_EXPIRED_MSG) {
  if (authHandling) return
  authHandling = true

  try {
    uni.removeStorageSync(STORAGE_TOKEN_KEY)
    uni.removeStorageSync(STORAGE_USER_KEY)
  } catch (e) {
    console.error('清除登录信息失败:', e)
  }

  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })

  setTimeout(() => {
    authHandling = false
    uni.reLaunch({
      url: '/pages/index/index?login=1'
    })
  }, 2000)
}

/**
 * 统一请求方法
 * @param {Object} options 请求配置
 * @param {String} options.url 接口路径（相对路径）
 * @param {String} options.method 请求方法 GET/POST/PUT/DELETE
 * @param {Object} options.data 请求参数
 * @param {Boolean} options.needAuth 是否需要认证，默认true
 * @param {Object} options.header 自定义请求头
 */
export function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    needAuth = true,
    header = {}
  } = options

  const fullUrl = `${API_BASE_URL}${url}`

  console.log(`📡 ${method}请求:`, fullUrl)
  if (method !== 'GET' && data && Object.keys(data).length > 0) {
    console.log('📦 请求数据:', data)
  }

  const requestHeader = {
    ...REQUEST_HEADERS,
    ...header
  }

  if (needAuth) {
    const token = getToken()
    if (token) {
      requestHeader[TOKEN_HEADER] = token
    } else {
      console.warn('请求需要认证但Token不存在:', url)
      // 本地无 token：直接提示登录，避免业务层报出莫名其妙的失败
      handleTokenExpired(LOGIN_REQUIRED_MSG)
      return Promise.reject(createAuthError(LOGIN_REQUIRED_MSG))
    }
  }

  return new Promise((resolve, reject) => {
    const requestConfig = {
      url: fullUrl,
      method,
      header: requestHeader,
      timeout: REQUEST_TIMEOUT
    }

    if (method !== 'GET') {
      requestConfig.data = data
    }

    uni.request({
      ...requestConfig,
      success: (res) => {
        // HTTP 401：后端拦截器常见返回（无业务 JSON）
        if (res.statusCode === 401) {
          console.error('HTTP 401 登录失效:', url)
          handleTokenExpired(AUTH_EXPIRED_MSG)
          reject(createAuthError(AUTH_EXPIRED_MSG))
          return
        }

        if (res.statusCode !== 200) {
          const error = {
            code: res.statusCode,
            msg: `请求失败(${res.statusCode})`,
            data: null
          }
          console.error('HTTP请求失败:', error)
          reject(error)
          return
        }

        const responseData = res.data || {}

        // 兼容：code=0 或 1 都视为成功
        const isSuccess =
          responseData.code === RESPONSE_CODE.SUCCESS ||
          responseData.code === RESPONSE_CODE.SUCCESS_NEW

        if (isSuccess) {
          resolve(responseData.data !== undefined ? responseData.data : responseData)
          return
        }

        const error = {
          code: responseData.code,
          msg: responseData.msg || '请求失败',
          data: responseData.data
        }
        console.error('业务请求失败:', error)

        if (isAuthError(error)) {
          handleTokenExpired(AUTH_EXPIRED_MSG)
          reject(createAuthError(AUTH_EXPIRED_MSG))
          return
        }

        reject(error)
      },
      fail: (err) => {
        const error = {
          code: 0,
          msg: err.errMsg || '网络请求失败',
          data: null
        }
        console.error('网络请求失败:', error)
        reject(error)
      }
    })
  })
}

/**
 * GET请求
 */
export function get(url, params = {}, needAuth = true) {
  const queryString = Object.keys(params)
    .filter(key => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')

  const fullUrl = queryString ? `${url}?${queryString}` : url

  console.log('📤 GET请求:', fullUrl, '参数:', params)

  return request({
    url: fullUrl,
    method: 'GET',
    needAuth
  })
}

/**
 * POST请求
 */
export function post(url, data = {}, needAuth = true) {
  return request({
    url,
    method: 'POST',
    data,
    needAuth
  })
}

/**
 * PUT请求
 */
export function put(url, data = {}, needAuth = true) {
  return request({
    url,
    method: 'PUT',
    data,
    needAuth
  })
}

/**
 * DELETE请求
 */
export function del(url, data = {}, needAuth = true) {
  return request({
    url,
    method: 'DELETE',
    data,
    needAuth
  })
}
