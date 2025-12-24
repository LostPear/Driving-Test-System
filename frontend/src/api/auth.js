import apiClient from './client'

export const login = (credentials) => {
  return apiClient.post('/auth/login/', credentials)
}

export const register = (userData) => {
  return apiClient.post('/auth/register/', userData)
}

export const logout = () => {
  return apiClient.post('/auth/logout/')
}

export const updateProfile = (data) => {
  return apiClient.put('/auth/profile/', data)
}

export const changePassword = (data) => {
  return apiClient.post('/auth/change-password/', data)
}
