import apiClient from './client'

// 获取用户列表
export const getUsers = (params) => {
  return apiClient.get('/users/', { params })
}

// 获取单个用户
export const getUser = (id) => {
  return apiClient.get(`/users/${id}/`)
}

// 更新用户
export const updateUser = (id, data) => {
  return apiClient.put(`/users/${id}/`, data)
}

// 删除用户
export const deleteUser = (id) => {
  return apiClient.delete(`/users/${id}/`)
}

// 获取用户统计信息
export const getUserStats = (id) => {
  return apiClient.get(`/users/${id}/stats/`)
}

// 获取管理员统计数据
export const getAdminStats = () => {
  return apiClient.get('/users/stats/')
}

// 管理员重置用户密码
export const resetUserPassword = (userId) => {
  return apiClient.post(`/users/${userId}/reset-password/`, {})
}

// 管理员创建用户
export const createUser = (data) => {
  return apiClient.post('/users/', data)
}
