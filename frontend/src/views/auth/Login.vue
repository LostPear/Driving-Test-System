<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo 和标题 -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-primary-600 rounded-full flex items-center justify-center">
          <svg class="h-10 w-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          驾照理论模拟考试系统
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          登录您的账户开始练习
        </p>
      </div>

      <!-- 登录表单 -->
      <div class="card">
        <form class="space-y-6" @submit.prevent="handleLogin">
          <!-- 错误提示 -->
          <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <span>{{ errorMessage }}</span>
            </div>
          </div>

          <!-- 忘记密码提示 -->
          <div v-if="showForgotPasswordMessage" class="bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
              <span>如果忘记密码，请联系管理员重置密码。</span>
            </div>
          </div>

          <!-- 用户名 -->
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-2">
              用户名
            </label>
            <input
              id="username"
              v-model="formData.username"
              type="text"
              required
              class="input-field"
              placeholder="请输入用户名"
            />
          </div>

          <!-- 密码 -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <input
              id="password"
              v-model="formData.password"
              type="password"
              required
              class="input-field"
              placeholder="请输入密码"
            />
          </div>

          <!-- 记住我 -->
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <input
                id="remember"
                v-model="formData.remember"
                type="checkbox"
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label for="remember" class="ml-2 block text-sm text-gray-900">
                记住我
              </label>
            </div>
            <div class="text-sm">
              <a 
                href="#" 
                @click.prevent="showForgotPasswordTip"
                class="font-medium text-primary-600 hover:text-primary-500"
              >
                忘记密码？
              </a>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div>
            <button
              type="submit"
              :disabled="loading"
              class="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading">登录中...</span>
              <span v-else>登录</span>
            </button>
          </div>

          <!-- 注册链接 -->
          <div class="text-center text-sm">
            <span class="text-gray-600">还没有账户？</span>
            <router-link to="/register" class="font-medium text-primary-600 hover:text-primary-500 ml-1">
              立即注册
            </router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formData = reactive({
  username: '',
  password: '',
  remember: false
})

const loading = ref(false)
const errorMessage = ref('')
const showForgotPasswordMessage = ref(false)

const showForgotPasswordTip = () => {
  showForgotPasswordMessage.value = true
  // 3秒后自动隐藏提示
  setTimeout(() => {
    showForgotPasswordMessage.value = false
  }, 5000)
}

const handleLogin = async () => {
  errorMessage.value = ''
  showForgotPasswordMessage.value = false
  loading.value = true

  try {
    const result = await authStore.login({
      username: formData.username,
      password: formData.password
    })

    if (result.success) {
      // 根据用户角色跳转
      if (result.user.role === 'admin') {
        router.push('/admin')
      } else {
        router.push('/user')
      }
    } else {
      errorMessage.value = result.message
    }
  } catch (error) {
    errorMessage.value = '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
