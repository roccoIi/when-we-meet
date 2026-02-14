import { defineStore } from 'pinia'
import { ref } from 'vue'
import { API_BASE_URL } from '../config/constants'

export const useUserStore = defineStore('user', () => {
  // 상태
  const isLoggedIn = ref(false)
  const nickname = ref('')
  const profileImgUrl = ref('') // 프로필 이미지 URL
  const provider = ref(null)
  const showNicknameModal = ref(false)
  const accessToken = ref(null) // accessToken을 메모리에 저장
  const isInitialized = ref(false) // 초기화 완료 여부

  // 액션
  const login = (userInfo) => {
    isLoggedIn.value = true
    provider.value = userInfo.provider || null
    nickname.value = userInfo.nickname || ''
    
    // profileImgUrl 처리
    let imageUrl = userInfo.profileImgUrl || ''
    
    // provider가 없으면 guest 유저 → BASE_URL 붙이기
    if (!userInfo.provider && imageUrl && !imageUrl.startsWith('http')) {
      profileImgUrl.value = `${API_BASE_URL}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`
    } else {
      // OAuth 유저는 그대로 사용
      profileImgUrl.value = imageUrl
    }
  }

  const logout = () => {
    isLoggedIn.value = false
    nickname.value = ''
    profileImgUrl.value = ''
    provider.value = null
    accessToken.value = null // accessToken 초기화
  }

  const setAccessToken = (token) => {
    accessToken.value = token
  }

  const getAccessToken = () => {
    return accessToken.value
  }

  const setNickname = (newNickname) => {
    nickname.value = newNickname
    showNicknameModal.value = false
  }

  const openNicknameModal = () => {
    showNicknameModal.value = true
  }

  const closeNicknameModal = () => {
    showNicknameModal.value = false
  }

  const setInitialized = (value) => {
    isInitialized.value = value
  }

  return {
    isLoggedIn,
    nickname,
    profileImgUrl,
    provider,
    showNicknameModal,
    accessToken,
    isInitialized,
    login,
    logout,
    setAccessToken,
    getAccessToken,
    setNickname,
    openNicknameModal,
    closeNicknameModal,
    setInitialized
  }
})

