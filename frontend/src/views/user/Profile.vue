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

    <!-- 学习统计 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-6">学习统计</h2>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="text-center p-6 bg-blue-50 rounded-lg">
          <svg class="h-12 w-12 text-blue-600 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
          <p class="text-3xl font-bold text-blue-600">{{ stats.practiceCount }}</p>
          <p class="text-sm text-gray-600 mt-2">练习题目</p>
        </div>

        <div class="text-center p-6 bg-green-50 rounded-lg">
          <svg class="h-12 w-12 text-green-600 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p class="text-3xl font-bold text-green-600">{{ stats.examCount }}</p>
          <p class="text-sm text-gray-600 mt-2">模拟考试</p>
        </div>

        <div class="text-center p-6 bg-purple-50 rounded-lg">
          <svg class="h-12 w-12 text-purple-600 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
          </svg>
          <p class="text-3xl font-bold text-purple-600">{{ stats.accuracy }}%</p>
          <p class="text-sm text-gray-600 mt-2">正确率</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { updateProfile as apiUpdateProfile, changePassword as apiChangePassword } from '@/api/auth'
import { getUserStats } from '@/api/users'

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
  username: '',
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

const stats = ref({
  practiceCount: 0,
  examCount: 0,
  accuracy: 0
})

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
const loadData = async () => {
  loadUserData()
  await loadStats()
}

onMounted(async () => {
  await loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, async (newPath) => {
  if (newPath === '/user/profile') {
    await loadData()
  }
}, { immediate: false })

const loadUserData = () => {
  const user = authStore.user
  if (user) {
    formData.username = user.username || ''
    formData.email = user.email || ''
  }
}

const loadStats = async () => {
  try {
    const data = await getUserStats(authStore.user?.id)
    stats.value = data
  } catch (error) {
    stats.value = {
      // practiceCount: 156,
      // examCount: 8,
      // accuracy: 85
    }
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
