<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- 考试开始前 -->
    <div v-if="!examStarted" class="space-y-6">
      <div class="card">
        <h1 class="text-2xl font-bold text-gray-900 mb-4">模拟考试</h1>
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <h3 class="font-semibold text-blue-900 mb-2">考试说明</h3>
          <ul class="text-sm text-blue-800 space-y-1">
            <li>• 考试时间：45分钟</li>
            <li>• 题目数量：100题（单选题90题，判断题10题）</li>
            <li>• 及格分数：90分</li>
            <li>• 考试期间不可暂停，请合理安排时间</li>
          </ul>
        </div>

        <button
          @click="startExam"
          :disabled="loading"
          class="w-full btn-primary disabled:opacity-50"
        >
          {{ loading ? '正在生成试卷...' : '开始考试' }}
        </button>
      </div>

      <!-- 历史考试记录 -->
      <div class="card">
        <h2 class="text-xl font-bold text-gray-900 mb-4">历史考试记录</h2>
        <div v-if="examHistory.length > 0" class="space-y-3">
          <div
            v-for="exam in examHistory"
            :key="exam.id"
            class="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer"
            @click="viewResult(exam.id)"
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
                <p class="text-sm text-gray-500">{{ exam.date }} · 用时 {{ exam.duration }}</p>
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
          暂无考试记录
        </div>
      </div>
    </div>

    <!-- 考试进行中 -->
    <div v-else class="space-y-6">
      <!-- 考试头部信息 -->
      <div class="card">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-lg font-semibold text-gray-900">模拟考试进行中</h2>
            <p class="text-sm text-gray-600">题目 {{ currentIndex + 1 }} / {{ examQuestions.length }}</p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-600">剩余时间</p>
            <p class="text-2xl font-bold" :class="timeRemaining < 300 ? 'text-red-600' : 'text-gray-900'">
              {{ formattedTime }}
            </p>
          </div>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2 mt-4">
          <div
            class="bg-primary-600 h-2 rounded-full transition-all duration-300"
            :style="{ width: `${examProgress}%` }"
          ></div>
        </div>
      </div>

      <!-- 题目卡片 -->
      <div v-if="currentExamQuestion" class="card">
        <div class="flex items-center justify-between mb-4">
          <span class="px-3 py-1 bg-purple-100 text-purple-700 text-sm font-medium rounded-full">
            {{ getTypeName(currentExamQuestion.type) }}
          </span>
          <span class="text-sm text-gray-600">第 {{ currentIndex + 1 }} 题</span>
        </div>

        <div class="mb-6">
          <h3 class="text-xl font-medium text-gray-900 mb-3">
            {{ currentExamQuestion.question }}
          </h3>
          <div v-if="currentExamQuestion.image" class="mt-4">
            <img :src="getImageUrl(currentExamQuestion.image)" alt="题目图片" class="max-w-full h-auto max-h-96 rounded-lg border border-gray-300 shadow-sm" />
          </div>
        </div>

        <div class="space-y-3">
          <div
            v-for="(option, index) in currentExamQuestion.options"
            :key="index"
            @click="selectExamOption(index)"
            class="p-4 border-2 rounded-lg cursor-pointer transition-all"
            :class="examAnswers[currentIndex] === index 
              ? 'border-primary-500 bg-primary-50' 
              : 'border-gray-200 hover:border-gray-300'"
          >
            <div class="flex items-center">
              <div
                class="h-6 w-6 rounded-full border-2 flex items-center justify-center mr-3"
                :class="examAnswers[currentIndex] === index ? 'border-primary-500' : 'border-gray-300'"
              >
                <div
                  v-if="examAnswers[currentIndex] === index"
                  class="h-3 w-3 rounded-full bg-primary-500"
                ></div>
              </div>
              <span class="font-medium mr-2">{{ String.fromCharCode(65 + index) }}.</span>
              <span>{{ option }}</span>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between mt-6 pt-6 border-t border-gray-200">
          <button
            @click="previousExamQuestion"
            :disabled="currentIndex === 0"
            class="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一题
          </button>
          
          <button
            v-if="currentIndex < examQuestions.length - 1"
            @click="nextExamQuestion"
            class="btn-primary"
          >
            下一题
          </button>
          
          <button
            v-else
            @click="confirmSubmit"
            class="btn-primary bg-green-600 hover:bg-green-700"
          >
            提交试卷
          </button>
        </div>
      </div>

      <!-- 答题卡 -->
      <div class="card">
        <h3 class="font-semibold text-gray-900 mb-4">答题卡</h3>
        <div class="grid grid-cols-10 gap-2">
          <button
            v-for="(question, index) in examQuestions"
            :key="index"
            @click="jumpToQuestion(index)"
            class="h-10 w-10 rounded-lg border-2 font-medium transition-all"
            :class="getAnswerCardClass(index)"
          >
            {{ index + 1 }}
          </button>
        </div>
        <div class="flex items-center justify-center space-x-6 mt-4 text-sm">
          <div class="flex items-center">
            <div class="h-4 w-4 rounded bg-primary-500 mr-2"></div>
            <span class="text-gray-600">已答</span>
          </div>
          <div class="flex items-center">
            <div class="h-4 w-4 rounded border-2 border-gray-300 bg-white mr-2"></div>
            <span class="text-gray-600">未答</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createExam, submitExam, getExamHistory } from '@/api/exam'
import { getImageUrl } from '@/api/images'

const router = useRouter()
const route = useRoute()

const examStarted = ref(false)
const loading = ref(false)
const examQuestionIds = ref([]) // 存储题目ID列表
const examQuestions = ref([])
const examAnswers = ref({})
const currentIndex = ref(0)
const timeRemaining = ref(45 * 60) // 45分钟
const examHistory = ref([])

let timer = null

const currentExamQuestion = computed(() => examQuestions.value[currentIndex.value])
const examProgress = computed(() => {
  const answered = Object.keys(examAnswers.value).length
  return (answered / examQuestions.value.length) * 100
})

const formattedTime = computed(() => {
  const minutes = Math.floor(timeRemaining.value / 60)
  const seconds = timeRemaining.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 加载数据的函数
const loadData = async () => {
  await loadExamHistory()
}

onMounted(async () => {
  await loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, async (newPath) => {
  if (newPath === '/user/exam') {
    await loadData()
  }
}, { immediate: false })

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const loadExamHistory = async () => {
  try {
    const data = await getExamHistory()
    examHistory.value = data.results || []
  } catch (error) {
    console.error('加载考试历史失败:', error)
    examHistory.value = []
  }
}

const startExam = async () => {
  loading.value = true
  
  try {
    // 获取题目，但不创建数据库记录
    const data = await createExam({ type: 'exam' })
    examQuestionIds.value = (data.questions || []).map(q => q.id)
    examQuestions.value = data.questions || []
    examStarted.value = true
    
    // 开始计时
    timer = setInterval(() => {
      timeRemaining.value--
      if (timeRemaining.value <= 0) {
        autoSubmitExam()
      }
    }, 1000)
  } catch (error) {
    console.error('创建考试失败:', error)
    alert('创建考试失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const selectExamOption = (index) => {
  examAnswers.value[currentIndex.value] = index
}

const nextExamQuestion = () => {
  if (currentIndex.value < examQuestions.value.length - 1) {
    currentIndex.value++
  }
}

const previousExamQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const jumpToQuestion = (index) => {
  currentIndex.value = index
}

const getAnswerCardClass = (index) => {
  if (examAnswers.value[index] !== undefined) {
    return index === currentIndex.value
      ? 'border-primary-600 bg-primary-600 text-white'
      : 'border-primary-500 bg-primary-500 text-white'
  }
  return index === currentIndex.value
    ? 'border-gray-400 bg-gray-100'
    : 'border-gray-300 bg-white hover:border-gray-400'
}

const confirmSubmit = () => {
  const unanswered = examQuestions.value.length - Object.keys(examAnswers.value).length
  if (unanswered > 0) {
    if (!confirm(`还有 ${unanswered} 道题未作答，确定要提交吗？`)) {
      return
    }
  }
  submitExamNow()
}

const autoSubmitExam = () => {
  alert('考试时间已到，系统将自动提交试卷')
  submitExamNow()
}

const submitExamNow = async () => {
  if (timer) {
    clearInterval(timer)
  }
  
  try {
    const result = await submitExam(examQuestionIds.value, examAnswers.value, 'exam')
    router.push(`/user/exam/result/${result.id}`)
  } catch (error) {
    console.error('提交失败:', error)
    alert('提交失败，请稍后重试')
    router.push('/user/dashboard')
  }
}

const viewResult = (id) => {
  router.push(`/user/exam/result/${id}`)
}

const getTypeName = (type) => {
  if (type === 'single') return '单选题'
  if (type === 'multiple') return '多选题'
  if (type === 'judge') return '判断题'
  return type
}
</script>