import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 상태
  const isLoggedIn = ref(false)
  const nickname = ref('')
  const profileImgUrl = ref('') // 프로필 이미지 URL
  const userId = ref(null)
  const showNicknameModal = ref(false)
  const accessToken = ref(null) // accessToken을 메모리에 저장
  const isInitialized = ref(false) // 초기화 완료 여부

  // 액션
  const login = (userInfo) => {
    isLoggedIn.value = true
    userId.value = userInfo.id || null
    nickname.value = userInfo.nickname || ''
    profileImgUrl.value = userInfo.profileImgUrl || ''
  }

  const logout = () => {
    isLoggedIn.value = false
    nickname.value = ''
    profileImgUrl.value = ''
    userId.value = null
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
    userId,
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

