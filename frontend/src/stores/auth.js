import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister, logout as apiLogout } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  
  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role || null)
  const username = computed(() => user.value?.username || '')
  
  function setAuth(userData, authToken) {
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }
  
  function clearAuth() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  async function login(credentials) {
    try {
      const response = await apiLogin(credentials)
      setAuth(response.user, response.token)
      return { success: true, user: response.user }
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || '登录失败，请检查用户名和密码' 
      }
    }
  }
  
  async function register(userData) {
    try {
      const response = await apiRegister(userData)
      return { success: true, message: '注册成功，请登录' }
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || '注册失败，请重试' 
      }
    }
  }
  
  async function logout() {
    try {
      await apiLogout()
    } catch (error) {
      // 静默处理登出错误
    } finally {
      clearAuth()
    }
  }
  
  function checkAuth() {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')
    
    if (storedUser && storedToken) {
      try {
        user.value = JSON.parse(storedUser)
        token.value = storedToken
      } catch (error) {
        clearAuth()
      }
    }
  }
  
  return {
    user,
    token,
    isAuthenticated,
    userRole,
    username,
    login,
    register,
    logout,
    checkAuth,
    setAuth,
    clearAuth
  }
})
