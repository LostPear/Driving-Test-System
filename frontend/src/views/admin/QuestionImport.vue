<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">题库导入</h1>
      <p class="text-gray-600 mt-1">批量导入题库文件，支持 Excel、CSV、JSON 格式</p>
    </div>

    <!-- 导入说明 -->
    <div class="card bg-blue-50 border border-blue-200">
      <h3 class="font-semibold text-blue-900 mb-3 flex items-center">
        <svg class="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        导入说明
      </h3>
      <ul class="text-sm text-blue-800 space-y-1 ml-7">
        <li>• 支持的文件格式：Excel (.xlsx, .xls)、CSV (.csv)、JSON (.json)</li>
        <li>• 文件大小不超过 10MB</li>
        <li>• Excel/CSV 文件应包含以下列：题目类型、题目内容、选项A、选项B、选项C、选项D、正确答案、答案解析</li>
        <li>• JSON 文件格式请参考下方示例</li>
      </ul>
    </div>

    <!-- 文件上传区域 -->
    <div class="card">
      <div
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @drop.prevent="handleDrop"
        class="border-2 border-dashed rounded-lg p-12 text-center transition-colors"
        :class="isDragging ? 'border-primary-500 bg-primary-50' : 'border-gray-300'"
      >
        <svg class="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>
        <p class="text-lg font-medium text-gray-900 mb-2">
          拖拽文件到此处或
          <label class="text-primary-600 hover:text-primary-700 cursor-pointer">
            浏览文件
            <input
              ref="fileInput"
              type="file"
              class="hidden"
              accept=".xlsx,.xls,.csv,.json"
              @change="handleFileSelect"
            />
          </label>
        </p>
        <p class="text-sm text-gray-500">
          支持 Excel、CSV、JSON 格式，最大 10MB
        </p>
      </div>

      <!-- 选中的文件 -->
      <div v-if="selectedFile" class="mt-4 p-4 bg-gray-50 rounded-lg flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <svg class="h-10 w-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <div>
            <p class="font-medium text-gray-900">{{ selectedFile.name }}</p>
            <p class="text-sm text-gray-500">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
        </div>
        <button @click="clearFile" class="text-red-600 hover:text-red-800">
          <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 导入按钮 -->
      <div v-if="selectedFile" class="mt-4">
        <button
          @click="importFile"
          :disabled="importing"
          class="w-full btn-primary disabled:opacity-50"
        >
          {{ importing ? '导入中...' : '开始导入' }}
        </button>
      </div>

      <!-- 导入进度 -->
      <div v-if="importing" class="mt-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-medium text-gray-700">导入进度</span>
          <span class="text-sm font-medium text-gray-700">{{ importProgress }}%</span>
        </div>
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-primary-600 h-2 rounded-full transition-all duration-300"
            :style="{ width: `${importProgress}%` }"
          ></div>
        </div>
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult" class="mt-4 p-4 rounded-lg" :class="importResult.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
        <div class="flex items-center mb-2">
          <svg
            v-if="importResult.success"
            class="h-5 w-5 text-green-600 mr-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          <svg
            v-else
            class="h-5 w-5 text-red-600 mr-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          <span class="font-semibold" :class="importResult.success ? 'text-green-900' : 'text-red-900'">
            {{ importResult.message }}
          </span>
        </div>
        <div v-if="importResult.stats" class="text-sm" :class="importResult.success ? 'text-green-800' : 'text-red-800'">
          <p>成功导入：{{ importResult.stats.success }} 条</p>
          <p v-if="importResult.stats.failed > 0">失败：{{ importResult.stats.failed }} 条</p>
        </div>
      </div>
    </div>

    <!-- JSON 格式示例 -->
    <div class="card">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">JSON 格式示例</h3>
      <div class="bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto">
        <pre class="text-sm"><code>{{jsonExample}}</code></pre>
      </div>
      <button @click="downloadTemplate" class="mt-4 btn-secondary">
        <svg class="h-5 w-5 inline-block mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
        </svg>
        下载模板文件
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { importQuestions } from '@/api/questions'

const isDragging = ref(false)
const selectedFile = ref(null)
const fileInput = ref(null)
const importing = ref(false)
const importProgress = ref(0)
const importResult = ref(null)

const jsonExample = `[
  {
    "type": "single",
    "difficulty": "easy",
    "question": "机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？",
    "options": [
      "50米至100米",
      "150米以外",
      "100米至150米",
      "50米以内"
    ],
    "correctAnswer": 0,
    "explanation": "根据《道路交通安全法实施条例》规定..."
  },
  {
    "type": "judge",
    "difficulty": "medium",
    "question": "驾驶机动车在道路上行驶时，应当悬挂机动车号牌。",
    "options": ["正确", "错误"],
    "correctAnswer": 0,
    "explanation": "这是法律规定的要求。"
  }
]`

const handleDrop = (e) => {
  isDragging.value = false
  const files = e.dataTransfer.files
  if (files.length > 0) {
    handleFile(files[0])
  }
}

const handleFileSelect = (e) => {
  const files = e.target.files
  if (files.length > 0) {
    handleFile(files[0])
  }
}

const handleFile = (file) => {
  // 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    alert('文件大小不能超过 10MB')
    return
  }

  // 验证文件类型
  const validTypes = [
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/csv',
    'application/json'
  ]
  
  if (!validTypes.includes(file.type) && !file.name.match(/\.(xlsx|xls|csv|json)$/)) {
    alert('不支持的文件格式')
    return
  }

  selectedFile.value = file
  importResult.value = null
}

const clearFile = () => {
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const importFile = async () => {
  if (!selectedFile.value) return

  importing.value = true
  importProgress.value = 0
  importResult.value = null

  // 模拟进度
  const progressInterval = setInterval(() => {
    if (importProgress.value < 90) {
      importProgress.value += 10
    }
  }, 200)

  try {
    const result = await importQuestions(selectedFile.value)
    
    clearInterval(progressInterval)
    importProgress.value = 100

    setTimeout(() => {
      importResult.value = {
        success: true,
        message: '导入成功！',
        stats: result.stats || { success: 100, failed: 0 }
      }

      importing.value = false
    }, 500)
  } catch (error) {
    clearInterval(progressInterval)
    importProgress.value = 100

    setTimeout(() => {
      importResult.value = {
        success: false,
        message: error.response?.data?.message || '导入失败，请检查文件格式',
        stats: null
      }
      importing.value = false
    }, 500)
  }
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const downloadTemplate = () => {
  const template = {
    questions: JSON.parse(jsonExample)
  }
  
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'question_template.json'
  a.click()
  URL.revokeObjectURL(url)
}
</script>
