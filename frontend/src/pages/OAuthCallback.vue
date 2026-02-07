<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { authAPI } from '@/services'

const router = useRouter()
const userStore = useUserStore()

  onMounted(async () => {
    try {
      const response = await authAPI.reissueToken()
      const authorization = response.headers['authorization']
      console.log(response.headers)
      console.log(authorization)
      
      if(!authorization){
        alert('로그인에 실패했습니다')
        router.push('/login')
        return
      }

      const accessToken = authorization.replace('Bearer ', '')
      userStore.setAccessToken(accessToken)
      router.push('/')

    } catch (error) {
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
