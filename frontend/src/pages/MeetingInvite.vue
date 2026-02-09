<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { meetingAPI, userAPI } from "../services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const shareCode = route.params.shareCode;
const meetingInfo = ref(null);
const isLoading = ref(true);
const isJoining = ref(false);
const errorMessage = ref("");

onMounted(async () => {
  // 1️⃣ App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    console.log('⏳ [Invite] 초기화 대기 중...')
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
      console.log('✅ [Invite] 초기화 완료')
    } else {
      console.log('⚠️ [Invite] 초기화 타임아웃')
    }
  }

  // 2️⃣ 사용자 정보가 없으면 로드
  if (!userStore.isLoggedIn || !userStore.nickname) {
    console.log('🔄 [Invite] 사용자 정보 로드 시도...')
    try {
      const userInfoResponse = await userAPI.getUserInfo()
      const userInfo = userInfoResponse.data || userInfoResponse
      
      console.log('📦 [Invite] 받은 사용자 정보:', userInfo)
      
      if (userInfo && (userInfo.nickname || userInfo.profileImgUrl || userInfo.provider)) {
        userStore.login({
          nickname: userInfo.nickname || '',
          profileImgUrl: userInfo.profileImgUrl || '',
          provider: userInfo.provider || ''
        })
        console.log('✅ [Invite] 사용자 정보 로드 완료:', userInfo.nickname, '(', userInfo.provider, ')')
      } else {
        console.log('⚠️ [Invite] 사용자 정보 없음')
      }
    } catch (error) {
      console.error('⚠️ [Invite] 사용자 정보 로드 실패:', error)
      // 로그인 실패 시 아무것도 하지 않음 (로그인 안 한 상태로도 초대 페이지는 볼 수 있어야 함)
    }
  }

  // 3️⃣ 모임 정보 로드
  await loadMeetingInfo();
});

const loadMeetingInfo = async () => {
  isLoading.value = true;
  errorMessage.value = "";
  
  try {
    console.log('🔄 [Invite] 모임 정보 조회 중...', shareCode);
    const response = await meetingAPI.getMeetingByShareCode(shareCode);
    
    console.log('📦 [Invite] 응답:', response);
    
    // 백엔드 응답 구조에 맞게 데이터 추출
    const data = response.data || response;
    
    if (data && data.name) {
      meetingInfo.value = {
        name: data.name,
        participantCount: data.participantCount || data.memberNumber || 0
      };
      console.log('✅ [Invite] 모임 정보 로드 완료:', meetingInfo.value);
    } else {
      throw new Error('Invalid response data');
    }
  } catch (error) {
    console.error("❌ [Invite] 모임 정보 조회 실패:", error);
    
    // 401 에러인 경우 (인증 필요)
    if (error.response?.status === 401) {
      console.log("⚠️ [Invite] 인증이 필요한 API - 백엔드를 공개 API로 설정하세요");
    }
    
    // 개발 모드: 임시 데이터 표시
    if (import.meta.env.DEV) {
      console.log("⚠️ [개발 모드] 임시 데이터로 화면 표시");
      meetingInfo.value = {
        name: "테스트 모임",
        participantCount: 3
      };
    } else {
      // 프로덕션 모드: 에러 메시지 표시
      errorMessage.value = "유효하지 않거나 만료된 초대 링크입니다.";
    }
  } finally {
    isLoading.value = false;
  }
};

const handleJoinMeeting = async () => {
  isJoining.value = true;
  
  try {
    await meetingAPI.joinMeetingByShareCode(shareCode);
    alert("모임에 참여했습니다!");
    // 참여 후 해당 모임 상세 페이지로 이동 (shareCode 사용)
    router.push(`/meeting/${shareCode}`);
  } catch (error) {
    console.error("모임 참여 실패:", error);
    if (error.response?.data?.code === "M003") { // 존재하지 않는 모임
      alert(error.response?.data?.message);
      router.push(`/`);
    } else if (error.response?.data?.code === "M004") { // 이미 참여중인 모임
      alert(error.response?.data?.message);
      // 이미 참여중이면 바로 모임 페이지로 이동
      router.push(`/meeting/${shareCode}`);
    } else {
      alert("모임 참여에 실패했습니다. 다시 시도해주세요.");
    }
  } finally {
    isJoining.value = false;
  }
};

const goToMeetingList = () => {
  router.push("/");
};
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center p-5">
    <!-- 로딩 중 -->
    <div v-if="isLoading" class="text-center">
      <div class="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-600">모임 정보를 불러오는 중...</p>
    </div>

    <!-- 에러 메시지 -->
    <div v-else-if="errorMessage" class="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center">
      <div class="text-6xl mb-4">😕</div>
      <h2 class="text-xl font-bold text-gray-800 mb-3">모임을 찾을 수 없습니다</h2>
      <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
      <button
        class="w-full px-6 py-3 bg-primary text-white rounded-lg font-semibold hover:bg-primary-dark transition-colors"
        @click="goToMeetingList"
      >
        메인으로 돌아가기
      </button>
    </div>

    <!-- 모임 정보 -->
    <div v-else-if="meetingInfo" class="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full">
      <!-- 아이콘 -->
      <div class="text-center mb-6">
        <div class="inline-flex items-center justify-center w-20 h-20 bg-gradient-to-br from-primary to-purple-600 rounded-full mb-4">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-10 h-10 text-white">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
          </svg>
        </div>
        <h2 class="text-2xl font-bold text-gray-800 mb-2">모임 초대</h2>
        <p class="text-gray-600">새로운 모임에 초대되었습니다!</p>
      </div>

      <!-- 모임 정보 -->
      <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-5 mb-6">
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-gray-600">모임 이름</span>
          <span class="text-lg font-bold text-gray-800">{{ meetingInfo.name }}</span>
        </div>
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-600">참여 인원</span>
          <span class="flex items-center gap-1.5">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-5 h-5 text-primary">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z" />
            </svg>
            <span class="text-lg font-bold text-primary">{{ meetingInfo.participantCount }}명</span>
          </span>
        </div>
      </div>

      <!-- 버튼 -->
      <div class="space-y-3">
        <!-- 모임 참여하기 (메인 버튼) -->
        <button
          class="w-full px-6 py-4 bg-gradient-to-r from-primary to-purple-600 text-white rounded-xl font-bold text-lg shadow-lg hover:shadow-xl hover:scale-105 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          @click="handleJoinMeeting"
          :disabled="isJoining"
        >
          <span v-if="!isJoining">🎉 모임 참여하기</span>
          <span v-else class="flex items-center justify-center gap-2">
            <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            참여 중...
          </span>
        </button>

        <!-- 참여중인 모임 목록 확인하기 -->
        <button
          class="w-full px-6 py-3 bg-white text-gray-700 border-2 border-gray-300 rounded-xl font-semibold hover:bg-gray-50 transition-colors"
          @click="goToMeetingList"
          :disabled="isJoining"
        >
          참여중인 모임 목록 확인하기
        </button>
      </div>

      <!-- 안내 문구 -->
      <p class="text-xs text-gray-500 text-center mt-6">
        💡 모임에 참여하면 일정을 입력하고<br />다른 참여자들과 만날 날짜를 조율할 수 있습니다
      </p>
    </div>
  </div>
</template>
