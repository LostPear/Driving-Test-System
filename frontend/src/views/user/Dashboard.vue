<template>
  <div class="space-y-6">
    <!-- 欢迎横幅 -->
    <div class="bg-gradient-to-r from-primary-600 to-primary-700 rounded-lg shadow-lg p-6 sm:p-8 text-white">
      <h1 class="text-3xl font-bold mb-2">欢迎回来，{{ username }}！</h1>
      <p class="text-primary-100">继续您的驾照理论学习之旅</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">练习题目</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.practiceCount }}</p>
          </div>
          <div class="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">已完成题目数量</p>
      </div>

      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">模拟考试</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.examCount }}</p>
          </div>
          <div class="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">已参加考试次数</p>
      </div>

      <div class="card hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-600">正确率</p>
            <p class="text-3xl font-bold text-gray-900 mt-2">{{ stats.accuracy }}%</p>
          </div>
          <div class="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
        </div>
        <p class="text-sm text-gray-500 mt-2">总体答题准确率</p>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">快速开始</h2>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <button
          @click="$router.push('/user/practice/sequential')"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-200 transition-colors">
            <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">顺序练习</h3>
            <p class="text-sm text-gray-500">按顺序练习题目</p>
          </div>
        </button>

        <button
          @click="$router.push('/user/practice/random')"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center group-hover:bg-green-200 transition-colors">
            <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">随机练习</h3>
            <p class="text-sm text-gray-500">随机抽取题目练习</p>
          </div>
        </button>

        <button
          @click="startExam"
          class="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-primary-500 hover:bg-primary-50 transition-all group"
        >
          <div class="h-12 w-12 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-200 transition-colors">
            <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
          </div>
          <div class="ml-4 text-left">
            <h3 class="font-semibold text-gray-900">模拟考试</h3>
            <p class="text-sm text-gray-500">参加模拟考试</p>
          </div>
        </button>
      </div>
    </div>

    <!-- 最近考试记录 -->
    <div class="card">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-bold text-gray-900">最近考试记录</h2>
        <router-link to="/user/exam" class="text-sm text-primary-600 hover:text-primary-700 font-medium">
          查看全部
        </router-link>
      </div>
      
      <div v-if="recentExams.length > 0" class="space-y-3">
        <div
          v-for="exam in recentExams"
          :key="exam.id"
          class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center space-x-4">
            <div
              class="h-12 w-12 rounded-lg flex items-center justify-center"
              :class="exam.passed ? 'bg-green-100' : 'bg-red-100'"
            >
              <svg v-if="exam.passed" class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <svg v-else class="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <div>
              <h3 class="font-medium text-gray-900">{{ exam.title }}</h3>
              <p class="text-sm text-gray-500">{{ exam.date }}</p>
            </div>
          </div>
          <div class="text-right">
            <p class="text-lg font-semibold" :class="exam.passed ? 'text-green-600' : 'text-red-600'">
              {{ exam.score }}分
            </p>
            <p class="text-sm text-gray-500">{{ exam.correct }}/{{ exam.total }}</p>
          </div>
        </div>
      </div>
      
      <div v-else class="text-center py-8 text-gray-500">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p class="mt-2">暂无考试记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getExamHistory } from '@/api/exam'
import { getUserStats } from '@/api/users'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = computed(() => authStore.username)

const stats = ref({
  practiceCount: 0,
  examCount: 0,
  accuracy: 0
})

const recentExams = ref([])

// 加载数据的函数
const loadData = async () => {
  await loadStats()
  await loadRecentExams()
}

onMounted(async () => {
  await loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, async (newPath) => {
  if (newPath === '/user/dashboard') {
    await loadData()
  }
}, { immediate: false })

const loadStats = async () => {
  try {
    const data = await getUserStats(authStore.user?.id)
    stats.value = data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    stats.value = {
      practiceCount: 0,
      examCount: 0,
      accuracy: 0
    }
  }
}

const loadRecentExams = async () => {
  try {
    const data = await getExamHistory({ limit: 5 })
    recentExams.value = data.results || []
  } catch (error) {
    console.error('加载考试记录失败:', error)
    recentExams.value = []
  }
}

const startExam = () => {
  router.push('/user/exam')
}
</script>
