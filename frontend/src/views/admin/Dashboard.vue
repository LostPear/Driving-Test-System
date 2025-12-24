<template>
  <div class="space-y-6">
    <!-- 欢迎横幅 -->
    <div class="bg-gradient-to-r from-purple-600 to-purple-700 rounded-lg shadow-lg p-6 sm:p-8 text-white">
      <h1 class="text-3xl font-bold mb-2">管理后台</h1>
      <p class="text-purple-100">欢迎回来，{{ username }}</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">总用户数</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.totalUsers }}</p>
          </div>
          <div class="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">系统注册用户总数</p>
      </div>

      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">题库总数</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.totalQuestions }}</p>
          </div>
          <div class="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">系统题库题目总数</p>
      </div>

      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">今日考试</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.todayExams }}</p>
          </div>
          <div class="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">今日进行的考试次数</p>
      </div>

      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">平均通过率</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.passRate }}%</p>
          </div>
          <div class="h-12 w-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">所有考试的平均通过率</p>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">快速操作</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <button
          @click="$router.push('/admin/questions')"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition-colors">
            <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">添加题目</h3>
            <p class="text-sm text-gray-500">新增题库题目</p>
          </div>
        </button>

        <button
          @click="$router.push('/admin/import')"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center group-hover:bg-green-200 transition-colors">
            <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">批量导入</h3>
            <p class="text-sm text-gray-500">导入题库文件</p>
          </div>
        </button>

        <button
          @click="$router.push('/admin/users')"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition-colors">
            <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">用户管理</h3>
            <p class="text-sm text-gray-500">管理系统用户</p>
          </div>
        </button>

        <button
          @click="refreshStats"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-orange-100 rounded-lg flex items-center justify-center group-hover:bg-orange-200 transition-colors">
            <svg class="h-6 w-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">刷新数据</h3>
            <p class="text-sm text-gray-500">更新统计信息</p>
          </div>
        </button>
      </div>
    </div>

    <!-- 最近注册用户 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">最近注册用户</h2>
      <div class="space-y-3">
        <div
          v-for="user in recentUsers"
          :key="user.id"
          class="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
        >
          <div class="flex items-center space-x-3">
            <div class="h-10 w-10 rounded-full bg-primary-600 flex items-center justify-center text-white font-medium">
              {{ user.username.charAt(0).toUpperCase() }}
            </div>
            <div>
              <h3 class="font-medium text-gray-900">{{ user.username }}</h3>
              <p class="text-sm text-gray-500">{{ user.email }}</p>
            </div>
          </div>
          <span class="text-sm text-gray-500">{{ user.createdAt }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getAdminStats } from '@/api/users'

const route = useRoute()
const authStore = useAuthStore()

const username = computed(() => authStore.username)

const stats = ref({
  totalUsers: 0,
  totalQuestions: 0,
  todayExams: 0,
  passRate: 0
})

const recentUsers = ref([])

// 加载数据的函数
const loadData = async () => {
  await loadStats()
}

onMounted(async () => {
  await loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, async (newPath) => {
  if (newPath === '/admin/dashboard') {
    await loadData()
  }
}, { immediate: false })

const loadStats = async () => {
  try {
    const data = await getAdminStats()
    if (data.stats) {
      stats.value = data.stats
    }
    if (data.recentUsers) {
      recentUsers.value = data.recentUsers
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    stats.value = {
      totalUsers: 0,
      totalQuestions: 0,
      todayExams: 0,
      passRate: 0
    }
    recentUsers.value = []
  }
}

const refreshStats = () => {
  loadStats()
}
</script>
