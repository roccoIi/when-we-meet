<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userAPI } from '@/services'

const router = useRouter()
const userStore = useUserStore()

onMounted(async () => {
  try {
    console.log('🔄 [OAuth] 로그인 처리 시작...')
    
    // 사용자 정보 가져오기 (백엔드가 자동으로 토큰 발급)
    try {
      console.log('🔄 [OAuth] 사용자 정보 조회 중...')
      const userInfoResponse = await userAPI.getUserInfo()
      
      // 🔍 응답 구조 상세 로그
      console.log('📦 [OAuth] 전체 응답:', userInfoResponse)
      console.log('📦 [OAuth] response.data:', userInfoResponse.data)
      
      const userInfo = userInfoResponse.data || userInfoResponse
      
      console.log('📦 [OAuth] 추출된 사용자 정보:', userInfo)
      console.log('  - nickname:', userInfo.nickname)
      console.log('  - profileImgUrl:', userInfo.profileImgUrl)
      console.log('  - provider:', userInfo.provider)
      
      userStore.login({
        nickname: userInfo.nickname,
        profileImgUrl: userInfo.profileImgUrl,
        provider: userInfo.provider
      })
      
      console.log('✅ [OAuth] 로그인 완료:', userInfo.nickname, '(', userInfo.provider, ')')
    } catch (error) {
      console.error('⚠️ [OAuth] 사용자 정보 로드 실패:', error)
    }

    // 초기화 완료 표시
    userStore.setInitialized(true)
    console.log('✅ [OAuth] 초기화 완료')

    // 메인 페이지로 이동
    router.push('/')

  } catch (error) {
    console.error('❌ [OAuth] 로그인 처리 실패:', error)
    alert('로그인 처리 중 오류가 발생했습니다')
    router.push('/login')
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-[#667eea] to-[#764ba2] flex justify-center items-center">
    <div class="text-center">
      <div class="text-7xl mb-4 animate-bounce">⏳</div>
      <h2 class="text-2xl font-bold text-white mb-2">로그인 처리 중...</h2>
      <p class="text-white/80">잠시만 기다려주세요</p>
    </div>
  </div>
</template>
