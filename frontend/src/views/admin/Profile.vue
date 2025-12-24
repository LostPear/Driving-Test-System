<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <div class="card">
      <h1 class="text-2xl font-bold text-gray-900 mb-6">个人资料</h1>

      <form @submit.prevent="updateProfile" class="space-y-6">
        <!-- 用户名 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            用户名
          </label>
          <input
            v-model="formData.username"
            type="text"
            class="input-field bg-gray-100 cursor-not-allowed"
            readonly
            disabled
          />
          <p class="mt-1 text-sm text-gray-500">用户名不可更改</p>
        </div>

        <!-- 邮箱 -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            邮箱
          </label>
          <input
            v-model="formData.email"
            type="email"
            class="input-field"
            :class="{ 'border-red-500': errors.email }"
            @blur="validateEmail"
          />
          <p v-if="errors.email" class="mt-1 text-sm text-red-600">{{ errors.email }}</p>
        </div>

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

        <div class="flex items-center space-x-4">
          <button
            type="submit"
            :disabled="loading || !isFormValid"
            class="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '保存中...' : '保存更改' }}
          </button>
          <button
            type="button"
            @click="resetForm"
            class="btn-secondary"
          >
            取消
          </button>
        </div>
      </form>
    </div>

    <!-- 修改密码 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-6">修改密码</h2>

      <form @submit.prevent="changePassword" class="space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            当前密码
          </label>
          <input
            v-model="passwordForm.currentPassword"
            type="password"
            class="input-field"
            placeholder="请输入当前密码"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            新密码
          </label>
          <input
            v-model="passwordForm.newPassword"
            type="password"
            class="input-field"
            :class="{ 'border-red-500': passwordErrors.newPassword }"
            placeholder="请输入新密码（至少6个字符）"
            @blur="validateNewPassword"
          />
          <p v-if="passwordErrors.newPassword" class="mt-1 text-sm text-red-600">
            {{ passwordErrors.newPassword }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            确认新密码
          </label>
          <input
            v-model="passwordForm.confirmPassword"
            type="password"
            class="input-field"
            :class="{ 'border-red-500': passwordErrors.confirmPassword }"
            placeholder="请再次输入新密码"
            @blur="validateConfirmPassword"
          />
          <p v-if="passwordErrors.confirmPassword" class="mt-1 text-sm text-red-600">
            {{ passwordErrors.confirmPassword }}
          </p>
        </div>

        <!-- 密码修改成功提示 -->
        <div v-if="passwordSuccessMessage" class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
          <div class="flex items-center">
            <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
            </svg>
            <span>{{ passwordSuccessMessage }}</span>
          </div>
        </div>

        <!-- 密码修改错误提示 -->
        <div v-if="passwordErrorMessage" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          <div class="flex items-center">
            <svg class="h-5 w-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            <span>{{ passwordErrorMessage }}</span>
          </div>
        </div>

        <button
          type="submit"
          :disabled="passwordLoading || !isPasswordFormValid"
          class="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ passwordLoading ? '修改中...' : '修改密码' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { updateProfile as apiUpdateProfile, changePassword as apiChangePassword } from '@/api/auth'

const route = useRoute()

const authStore = useAuthStore()

const formData = reactive({
  username: '',
  email: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  email: ''
})

const passwordErrors = reactive({
  newPassword: '',
  confirmPassword: ''
})

const loading = ref(false)
const passwordLoading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const passwordSuccessMessage = ref('')
const passwordErrorMessage = ref('')

const isFormValid = computed(() => {
  return formData.email && !errors.email
})

const isPasswordFormValid = computed(() => {
  return (
    passwordForm.currentPassword &&
    passwordForm.newPassword &&
    passwordForm.confirmPassword &&
    !passwordErrors.newPassword &&
    !passwordErrors.confirmPassword
  )
})

// 加载数据的函数
const loadData = () => {
  loadUserData()
}

onMounted(() => {
  loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/admin/profile') {
    loadData()
  }
}, { immediate: false })

const loadUserData = () => {
  const user = authStore.user
  if (user) {
    formData.username = user.username || ''
    formData.email = user.email || ''
  }
}

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

const validateNewPassword = () => {
  if (!passwordForm.newPassword) {
    passwordErrors.newPassword = '请输入新密码'
  } else if (passwordForm.newPassword.length < 6) {
    passwordErrors.newPassword = '密码长度至少为6个字符'
  } else {
    passwordErrors.newPassword = ''
  }
  if (passwordForm.confirmPassword) {
    validateConfirmPassword()
  }
}

const validateConfirmPassword = () => {
  if (!passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = '请再次输入新密码'
  } else if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordErrors.confirmPassword = '两次输入的密码不一致'
  } else {
    passwordErrors.confirmPassword = ''
  }
}

const updateProfile = async () => {
  validateEmail()

  if (!isFormValid.value) return

  loading.value = true
  successMessage.value = ''
  errorMessage.value = ''

  try {
    await apiUpdateProfile(formData)
    successMessage.value = '个人资料更新成功'
    
    // 更新本地存储的用户信息
    const updatedUser = { ...authStore.user, ...formData }
    authStore.setAuth(updatedUser, authStore.token)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '更新失败，请重试'
  } finally {
    loading.value = false
  }
}

const changePassword = async () => {
  validateNewPassword()
  validateConfirmPassword()

  if (!isPasswordFormValid.value) return

  passwordLoading.value = true
  passwordSuccessMessage.value = ''
  passwordErrorMessage.value = ''

  try {
    await apiChangePassword({
      oldPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    
    passwordSuccessMessage.value = '密码修改成功'
    
    // 重置密码表单
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    passwordErrorMessage.value = error.response?.data?.message || '密码修改失败，请检查当前密码是否正确'
  } finally {
    passwordLoading.value = false
  }
}

const resetForm = () => {
  loadUserData()
  errors.email = ''
  successMessage.value = ''
  errorMessage.value = ''
}
</script>

