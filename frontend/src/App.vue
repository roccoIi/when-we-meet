<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import NicknameModal from "./components/NicknameModal.vue";
import { useUserStore } from "./stores/user";
import { authAPI } from "./services";

const route = useRoute();
const userStore = useUserStore();

// 로그인 페이지에서는 헤더를 숨김
const showHeader = computed(() => route.name !== "Login");

// 앱 초기화: 새로고침 시 토큰 자동 재발급
onMounted(async () => {
  // accessToken이 없으면 재발급 시도
  if (!userStore.getAccessToken()) {
    try {
      console.log('🔄 [App] 토큰 재발급 시도...')
      const response = await authAPI.reissueToken()
      const token = response.headers['authorization']?.replace('Bearer ', '')
      
      if (token) {
        userStore.setAccessToken(token)
        console.log('✅ [App] 토큰 재발급 성공')
        
        // 사용자 정보도 가져오기
        try {
          const userInfo = await authAPI.getUserInfo()
          userStore.login(userInfo)
          console.log('✅ [App] 사용자 정보 로드 완료')
        } catch (error) {
          console.log('⚠️ [App] 사용자 정보 로드 실패')
        }
      }
    } catch (error) {
      // refreshToken이 없거나 만료됨 (로그인 안 한 상태)
      console.log('ℹ️ [App] 로그인 필요')
    }
  } else {
    console.log('✅ [App] accessToken 이미 있음')
  }
})
</script>

<template>
  <div id="app" class="font-sans antialiased text-gray-800">
    <div
      class="max-w-app mx-auto min-h-screen bg-gray-100 relative md:shadow-xl max-md:max-w-full"
    >
      <AppHeader v-if="showHeader" />

      <main class="w-full">
        <router-view />
      </main>
    </div>

    <NicknameModal
      v-if="userStore.showNicknameModal"
      @close="userStore.closeNicknameModal"
    />
  </div>
</template>
