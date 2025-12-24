<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- 成绩卡片 -->
    <div class="card text-center">
      <div
        class="inline-flex h-24 w-24 rounded-full items-center justify-center mx-auto mb-4"
        :class="result.passed ? 'bg-green-100' : 'bg-red-100'"
      >
        <svg
          v-if="result.passed"
          class="h-12 w-12 text-green-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <svg
          v-else
          class="h-12 w-12 text-red-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </div>

      <h1
        class="text-3xl font-bold mb-2"
        :class="result.passed ? 'text-green-600' : 'text-red-600'"
      >
        {{ result.passed ? '恭喜通过！' : '未通过考试' }}
      </h1>
      
      <p class="text-5xl font-bold text-gray-900 my-6">{{ result.score }}</p>
      <p class="text-gray-600 mb-6">
        答对 {{ result.correct }} 题，答错 {{ result.wrong }} 题，未答 {{ result.unanswered }} 题
      </p>

      <div class="flex items-center justify-center space-x-4">
        <button @click="$router.push('/user/exam')" class="btn-primary">
          再考一次
        </button>
        <button @click="$router.push('/user/dashboard')" class="btn-secondary">
          返回首页
        </button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="card text-center">
        <p class="text-gray-600 text-sm mb-1">用时</p>
        <p class="text-2xl font-bold text-gray-900">{{ result.duration }}</p>
      </div>
      <div class="card text-center">
        <p class="text-gray-600 text-sm mb-1">正确率</p>
        <p class="text-2xl font-bold text-green-600">{{ result.accuracy }}%</p>
      </div>
      <div class="card text-center">
        <p class="text-gray-600 text-sm mb-1">答对</p>
        <p class="text-2xl font-bold text-green-600">{{ result.correct }}</p>
      </div>
      <div class="card text-center">
        <p class="text-gray-600 text-sm mb-1">答错</p>
        <p class="text-2xl font-bold text-red-600">{{ result.wrong }}</p>
      </div>
    </div>

    <!-- 错题列表 -->
    <div v-if="wrongQuestions.length > 0" class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">错题解析</h2>
      
      <div class="space-y-6">
        <div
          v-for="(question, index) in wrongQuestions"
          :key="question.id"
          class="border-b border-gray-200 pb-6 last:border-b-0"
        >
          <div class="flex items-start justify-between mb-3">
            <span class="px-3 py-1 bg-red-100 text-red-700 text-sm font-medium rounded-full">
              第 {{ question.number }} 题
            </span>
            <span class="text-sm text-gray-500">{{ getTypeName(question.type) }}</span>
          </div>

          <h3 class="text-lg font-medium text-gray-900 mb-4">
            {{ question.question }}
          </h3>

          <div class="space-y-2 mb-4">
            <div
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="p-3 rounded-lg border-2"
              :class="getOptionResultClass(optIndex, question)"
            >
              <div class="flex items-center">
                <span class="font-medium mr-2">{{ String.fromCharCode(65 + optIndex) }}.</span>
                <span>{{ option }}</span>
                <span
                  v-if="optIndex === question.correctAnswer"
                  class="ml-auto text-green-600 font-medium"
                >
                  正确答案
                </span>
                <span
                  v-else-if="optIndex === question.userAnswer"
                  class="ml-auto text-red-600 font-medium"
                >
                  您的答案
                </span>
              </div>
            </div>
          </div>

          <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p class="text-sm text-blue-900">
              <span class="font-semibold">解析：</span>
              {{ question.explanation }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="card text-center py-8">
      <svg class="mx-auto h-16 w-16 text-green-500 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <h3 class="text-lg font-medium text-gray-900 mb-2">全部答对！</h3>
      <p class="text-gray-600">您已经掌握了所有题目，继续保持！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getExamResult } from '@/api/exam'

const route = useRoute()

const result = ref({
  score: 0,
  correct: 0,
  wrong: 0,
  unanswered: 0,
  passed: false,
  duration: '0分钟',
  accuracy: 0
})

const wrongQuestions = ref([])

onMounted(async () => {
  await loadResult()
})

const loadResult = async () => {
  try {
    const data = await getExamResult(route.params.id)
    result.value = data
    wrongQuestions.value = data.wrongQuestions || []
  } catch (error) {
    console.error('加载考试结果失败:', error)
    result.value = {
      score: 0,
      correct: 0,
      wrong: 0,
      unanswered: 0,
      passed: false,
      duration: '0分钟',
      accuracy: 0
    }
    wrongQuestions.value = []
    alert('加载考试结果失败，请稍后重试')
  }
}

const getOptionResultClass = (index, question) => {
  if (index === question.correctAnswer) {
    return 'border-green-500 bg-green-50'
  }
  if (index === question.userAnswer) {
    return 'border-red-500 bg-red-50'
  }
  return 'border-gray-200'
}

const getTypeName = (type) => {
  if (type === 'single') return '单选题'
  if (type === 'multiple') return '多选题'
  if (type === 'judge') return '判断题'
  return type
}
</script>