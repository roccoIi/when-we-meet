import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 상태
  const isLoggedIn = ref(false)
  const nickname = ref('')
  const userId = ref(null)
  const showNicknameModal = ref(false)

  // 액션
  const login = (userInfo) => {
    isLoggedIn.value = true
    userId.value = userInfo.id
    nickname.value = userInfo.nickname || ''
    
    // 닉네임이 없으면 모달 표시
    if (!nickname.value) {
      showNicknameModal.value = true
    }
  }

  const logout = () => {
    isLoggedIn.value = false
    nickname.value = ''
    userId.value = null
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

  return {
    isLoggedIn,
    nickname,
    userId,
    showNicknameModal,
    login,
    logout,
    setNickname,
    openNicknameModal,
    closeNicknameModal
  }
})

