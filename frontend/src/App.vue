<script setup>
import { computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import NicknameModal from "./components/NicknameModal.vue";
import { useUserStore } from "./stores/user";
import { userAPI } from "./services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 헤더를 표시할지 여부 (Login, MeetingList, MeetingInvite는 제외)
const showHeader = computed(() => {
  return route.name !== "Login" && route.name !== "MeetingList" && route.name !== "MeetingInvite";
});

// 인증이 필요 없는 페이지 목록
const noAuthPages = ["Login","OAuthCallback"];

// 앱 초기화: 새로고침 시 토큰 자동 재발급 및 사용자 정보 로드
onMounted(async () => {
  // 라우터가 완전히 준비될 때까지 대기
  await router.isReady()
  
  // 디버깅: 현재 route 정보 출력
  
  // 인증이 필요 없는 페이지에서는 토큰 재발급을 시도하지 않음
  if (noAuthPages.includes(route.name)) {
    userStore.setInitialized(true)
    return
  }

  // 사용자 정보 로드 시도 (accessToken이 없으면 백엔드가 자동 발급)
  try {
    await loadUserInfo()
  } catch (error) {
    // refreshToken이 없거나 만료됨 (로그인 안 한 상태)
  } finally {
    // 초기화 완료 표시 (로그인 여부와 관계없이)
    userStore.setInitialized(true)
  }
})

// 사용자 정보 로드
const loadUserInfo = async () => {
  try {
    const response = await userAPI.getUserInfo()
    
    // 백엔드 CommonResponse 구조: { code, data, message, pagination }
    // 실제 사용자 정보는 data 필드에 있음
    const userInfo = response.data
    
    
    // userInfo가 null이면 에러
    if (!userInfo) {
      throw new Error('사용자 정보가 없습니다')
    }
    
    userStore.login({
      nickname: userInfo.nickname || '',
      profileImgUrl: userInfo.profileImgUrl || '',
      provider: userInfo.provider || ''
    })
    
  } catch (error) {
    console.error('⚠️ [App] 사용자 정보 로드 실패:', error)
    throw error
  }
}
</script>

<template>
  <div id="app" class="font-display antialiased text-gray-800 bg-background-light">
    <div
      class="max-w-app mx-auto min-h-screen bg-background-light relative md:shadow-xl max-md:max-w-full"
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
