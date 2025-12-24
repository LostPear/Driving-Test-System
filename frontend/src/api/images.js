import apiClient from './client'

// 上传图片
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('image', file)
  return apiClient.post('/images/', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取图片URL（直接返回URL，不需要API调用）
export const getImageUrl = (imagePath) => {
  if (!imagePath) return null
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  // 如果是相对路径，添加API前缀
  if (imagePath.startsWith('/api/images/')) {
    return imagePath
  }
  return `/api/images/${imagePath}`
}


