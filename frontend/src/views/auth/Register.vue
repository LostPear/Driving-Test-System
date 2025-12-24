<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-primary-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Logo 和标题 -->
      <div class="text-center">
        <div class="mx-auto h-16 w-16 bg-primary-600 rounded-full flex items-center justify-center">
          <svg class="h-10 w-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">
          创建新账户
        </h2>
        <p class="mt-2 text-sm text-gray-600">
          注册账户开始您的学习之旅
        </p>
      </div>

      <!-- 注册表单 -->
      <div class="card">
        <form class="space-y-6" @submit.prevent="handleRegister">
          <!-- 成功提示 -->
          <div v-if="successMessage" class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
              <span>{{ successMessage }}</span>
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="errorMessage" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            <div class="flex items-center">
              <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <span>{{ errorMessage }}</span>
            </div>
          </div>

          <!-- 邮箱 -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
              邮箱
            </label>
            <input
              id="email"
              v-model="formData.email"
              type="email"
              required
              class="input-field"
              :class="{ 'border-red-500': errors.email }"
              placeholder="请输入邮箱"
              @blur="validateEmail"
            />
            <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
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
              :class="{ 'border-red-500': errors.username }"
              placeholder="请输入用户名（3-20个字符）"
              @blur="validateUsername"
            />
            <p v-if="errors.username" class="mt-1 text-sm text-red-600">{{ errors.username }}</p>
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
              :class="{ 'border-red-500': errors.password }"
              placeholder="请输入密码（至少6个字符）"
              @blur="validatePassword"
            />
            <p v-if="errors.password" class="mt-1 text-sm text-red-600">{{ errors.password }}</p>
          </div>

          <!-- 确认密码 -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 mb-2">
              确认密码
            </label>
            <input
              id="confirmPassword"
              v-model="formData.confirmPassword"
              type="password"
              required
              class="input-field"
              :class="{ 'border-red-500': errors.confirmPassword }"
              placeholder="请再次输入密码"
              @blur="validateConfirmPassword"
            />
            <p v-if="errors.confirmPassword" class="mt-1 text-sm text-red-600">{{ errors.confirmPassword }}</p>
          </div>

          <!-- 用户协议 -->
          <div class="flex items-center">
            <input
              id="agree"
              v-model="formData.agree"
              type="checkbox"
              required
              class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
            />
            <label for="agree" class="ml-2 block text-sm text-gray-900">
              我同意
              <a href="#" class="text-primary-600 hover:text-primary-500">用户协议</a>
              和
              <a href="#" class="text-primary-600 hover:text-primary-500">隐私政策</a>
            </label>
          </div>

          <!-- 提交按钮 -->
          <div>
            <button
              type="submit"
              :disabled="loading || !isFormValid"
              class="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading">注册中...</span>
              <span v-else>注册</span>
            </button>
          </div>

          <!-- 登录链接 -->
          <div class="text-center text-sm">
            <span class="text-gray-600">已有账户？</span>
            <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-500 ml-1">
              立即登录
            </router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formData = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const errors = reactive({
  email: '',
  username: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const isFormValid = computed(() => {
  return (
    formData.email &&
    formData.username &&
    formData.password &&
    formData.confirmPassword &&
    formData.agree &&
    !errors.email &&
    !errors.username &&
    !errors.password &&
    !errors.confirmPassword
  )
})

const validateEmail = () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!formData.email) {
    errors.email = '请输入邮箱'
  } else if (!emailRegex.test(formData.email)) {
    errors.email = '请输入有效的邮箱地址'
  } else {
    errors.email = ''
  }
}

const validateUsername = () => {
  if (!formData.username) {
    errors.username = '请输入用户名'
  } else if (formData.username.length < 3 || formData.username.length > 20) {
    errors.username = '用户名长度应为3-20个字符'
  } else {
    errors.username = ''
  }
}

const validatePassword = () => {
  if (!formData.password) {
    errors.password = '请输入密码'
  } else if (formData.password.length < 6) {
    errors.password = '密码长度至少为6个字符'
  } else {
    errors.password = ''
  }
  // 同时验证确认密码
  if (formData.confirmPassword) {
    validateConfirmPassword()
  }
}

const validateConfirmPassword = () => {
  if (!formData.confirmPassword) {
    errors.confirmPassword = '请再次输入密码'
  } else if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
  } else {
    errors.confirmPassword = ''
  }
}

const handleRegister = async () => {
  // 验证所有字段
  validateEmail()
  validateUsername()
  validatePassword()
  validateConfirmPassword()

  if (!isFormValid.value) {
    return
  }

  errorMessage.value = ''
  successMessage.value = ''
  loading.value = true

  try {
    const result = await authStore.register({
      email: formData.email,
      username: formData.username,
      password: formData.password,
      password2: formData.confirmPassword
    })

    if (result.success) {
      successMessage.value = result.message
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      errorMessage.value = result.message
    }
  } catch (error) {
    errorMessage.value = '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
