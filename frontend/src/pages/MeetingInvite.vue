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

// 닉네임 설정 모달 관련
const showNicknameModal = ref(false);
const nicknameInput = ref("");
const isSettingNickname = ref(false);
const nicknameError = ref("");

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
        participantCount: data.memberNumber || 0
      };
      console.log('✅ [Invite] 모임 정보 로드 완료:', meetingInfo.value);
    } else {
      throw new Error('Invalid response data');
    }
  } catch (error) {
    console.error("❌ [Invite] 모임 정보 조회 실패:", error);
    
    // 에러 구조 상세 로깅
    console.log("🔍 [Invite] 에러 객체 전체:", error);
    console.log("🔍 [Invite] error.response:", error.response);
    console.log("🔍 [Invite] error.response?.data:", error.response?.data);
    console.log("🔍 [Invite] error.response?.status:", error.response?.status);
    console.log("🔍 [Invite] error.message:", error.message);
    
    // 백엔드에서 전달한 에러 메시지 추출
    let backendMessage = null;
    if (error.response?.data) {
      const errorData = error.response.data;
      // CommonResponse 구조: { code, message, data, pagination }
      backendMessage = errorData.message || errorData.error || null;
      console.log("🔍 [Invite] 백엔드 에러 메시지:", backendMessage);
      console.log("🔍 [Invite] 백엔드 에러 코드:", errorData.code);
    }
    
    // 백엔드 에러 코드 추출
    const errorCode = error.response?.data?.code;
    
    // 에러 코드별 처리
    if (errorCode) {
      console.log(`⚠️ [Invite] 에러 코드 ${errorCode} 감지`);
      
      switch(errorCode) {
        case 'M003':
          console.log("⚠️ [Invite] M003 - 더이상 존재하지 않는 모임");
          errorMessage.value = backendMessage || "더이상 존재하지 않는 모임입니다.";
          break;
        case 'M005':
          console.log("⚠️ [Invite] M005 - 만료된 초대링크");
          errorMessage.value = backendMessage || "이미 만료된 초대링크입니다. 새로 발급된 초대링크를 확인해주세요.";
          break;
        case 'A001':
          console.log("⚠️ [Invite] A001 - 만료된 JWT");
          errorMessage.value = backendMessage || "로그인이 만료되었습니다. 다시 로그인해주세요.";
          break;
        default:
          console.log(`⚠️ [Invite] 기타 에러 코드: ${errorCode}`);
          errorMessage.value = backendMessage || "모임 정보를 불러올 수 없습니다.";
      }
    } else {
      // HTTP 상태 코드로 판단
      console.log("⚠️ [Invite] 에러 코드 없음 - HTTP 상태로 판단");
      if (error.response?.status === 401) {
        console.log("⚠️ [Invite] 401 인증 오류");
        errorMessage.value = backendMessage || "로그인이 필요합니다.";
      } else if (error.response?.status === 404) {
        console.log("⚠️ [Invite] 404 모임을 찾을 수 없음");
        errorMessage.value = backendMessage || "존재하지 않는 모임입니다.";
      } else if (error.response?.status === 403) {
        console.log("⚠️ [Invite] 403 접근 권한 없음");
        errorMessage.value = backendMessage || "접근 권한이 없습니다.";
      } else {
        // 기타 에러
        errorMessage.value = backendMessage || "모임 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.";
      }
    }
    
    console.log("📢 [Invite] 최종 표시될 에러 메시지:", errorMessage.value);
  } finally {
    isLoading.value = false;
  }
};

const handleJoinMeeting = async () => {
  // 닉네임이 없으면 모달 표시
  if (!userStore.nickname) {
    console.log('⚠️ [Invite] 닉네임 없음 - 모달 표시');
    nicknameError.value = "";
    nicknameInput.value = "";
    showNicknameModal.value = true;
    return;
  }
  
  // 닉네임이 있으면 바로 참여 처리
  await joinMeeting();
};

const handleNicknameSubmit = async () => {
  const nickname = nicknameInput.value.trim();
  
  // 유효성 검사
  if (!nickname) {
    nicknameError.value = "닉네임을 입력해주세요.";
    return;
  }
  
  if (nickname.length > 10) {
    nicknameError.value = "닉네임은 10자 이하로 입력해주세요.";
    return;
  }
  
  isSettingNickname.value = true;
  nicknameError.value = "";
  
  try {
    console.log('🔄 [Invite] 게스트 유저 생성 요청:', nickname);
    
    // 1단계: 게스트 유저 생성
    await userAPI.createFirstUser(nickname);
    console.log('✅ [Invite] 게스트 유저 생성 성공');
    
    // 2단계: 사용자 정보 업데이트
    userStore.login({
      nickname: nickname,
      profileImgUrl: '',
      provider: ''
    });
    
    // 3단계: 모달 닫기
    showNicknameModal.value = false;
    
    // 4단계: 미팅룸 입장
    await joinMeeting();
    
  } catch (error) {
    console.error('❌ [Invite] 게스트 유저 생성 실패:', error);
    
    const errorData = error.response?.data;
    const backendErrorMessage = errorData?.message;
    
    nicknameError.value = backendErrorMessage || "닉네임 설정에 실패했습니다. 다시 시도해주세요.";
  } finally {
    isSettingNickname.value = false;
  }
};

const joinMeeting = async () => {
  isJoining.value = true;
  
  try {
    console.log('🔄 [Invite] 모임 참여 요청 시작:', shareCode);
    await meetingAPI.joinMeetingByShareCode(shareCode);
    console.log('✅ [Invite] 모임 참여 성공');
    alert("모임에 참여했습니다! 🎉");
    // 참여 후 해당 모임 상세 페이지로 이동 (shareCode 사용)
    router.push(`/meeting/${shareCode}`);
  } catch (error) {
    console.error("❌ [Invite] 모임 참여 실패:", error);
    
    // 에러 구조 상세 로깅
    console.log("🔍 [Invite] 참여 에러 - error.response:", error.response);
    console.log("🔍 [Invite] 참여 에러 - error.response?.data:", error.response?.data);
    console.log("🔍 [Invite] 참여 에러 - error.response?.status:", error.response?.status);
    
    // 백엔드 에러 정보 추출
    const errorData = error.response?.data;
    const errorCode = errorData?.code;
    const backendErrorMessage = errorData?.message;
    
    console.log("🔍 [Invite] 참여 에러 코드:", errorCode);
    console.log("🔍 [Invite] 참여 에러 메시지:", backendErrorMessage);
    
    // 에러 코드별 처리
    if (errorCode) {
      console.log(`⚠️ [Invite] 에러 코드 ${errorCode} 감지`);
      
      switch(errorCode) {
        case "M003":
          // 더이상 존재하지 않는 모임
          console.log("⚠️ [Invite] M003 - 더이상 존재하지 않는 모임");
          alert(backendErrorMessage || "더이상 존재하지 않는 모임입니다.");
          router.push(`/`);
          break;
          
        case "M004":
          // 이미 참여중인 모임
          console.log("ℹ️ [Invite] M004 - 이미 참여중인 모임");
          alert(backendErrorMessage || "이미 참여중인 모임입니다.");
          // 이미 참여중이면 바로 모임 페이지로 이동
          router.push(`/meeting/${shareCode}`);
          break;
          
        case "M005":
          // 만료된 초대링크
          console.log("⚠️ [Invite] M005 - 만료된 초대링크");
          alert(backendErrorMessage || "이미 만료된 초대링크입니다. 새로 발급된 초대링크를 확인해주세요.");
          router.push(`/`);
          break;
          
        case "A001":
          // 만료된 JWT
          console.log("⚠️ [Invite] A001 - 만료된 JWT");
          alert(backendErrorMessage || "로그인이 만료되었습니다. 다시 로그인해주세요.");
          router.push("/login");
          break;
          
        case "U001":
          // 등록되지 않은 사용자
          console.log("⚠️ [Invite] U001 - 등록되지 않은 사용자");
          alert(backendErrorMessage || "등록되지 않은 사용자입니다.");
          router.push("/login");
          break;
          
        default:
          console.log(`⚠️ [Invite] 기타 에러 코드: ${errorCode}`);
          const displayMessage = backendErrorMessage || "모임 참여에 실패했습니다. 다시 시도해주세요.";
          console.log("📢 [Invite] 표시할 에러 메시지:", displayMessage);
          alert(displayMessage);
      }
    } else {
      // 에러 코드가 없는 경우
      console.log("⚠️ [Invite] 에러 코드 없음");
      const displayMessage = backendErrorMessage || "모임 참여에 실패했습니다. 다시 시도해주세요.";
      console.log("📢 [Invite] 표시할 에러 메시지:", displayMessage);
      alert(displayMessage);
    }
  } finally {
    isJoining.value = false;
  }
};

const goToMeetingList = () => {
  router.push("/");
};

const closeNicknameModal = () => {
  showNicknameModal.value = false;
  nicknameInput.value = "";
  nicknameError.value = "";
};
</script>

<template>
  <div class="bg-background-light text-gray-800 min-h-screen antialiased selection:bg-primary selection:text-neutral-dark flex items-center justify-center p-5">
    <div class="w-full max-w-md">
      <!-- Loading State -->
      <div v-if="isLoading" class="text-center">
        <div class="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-600 font-medium">모임 정보를 불러오는 중...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMessage" class="bg-white rounded-3xl shadow-soft p-8 text-center border border-gray-100">
        <div class="w-20 h-20 mx-auto mb-4 bg-red-50 rounded-full flex items-center justify-center">
          <span class="material-icons text-red-400 text-4xl">error_outline</span>
        </div>
        <h2 class="text-2xl font-bold text-gray-800 mb-3">모임을 찾을 수 없습니다</h2>
        <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
        <button
          @click="goToMeetingList"
          class="w-full px-6 py-3 bg-primary hover:bg-primary-dark text-gray-800 rounded-2xl font-bold transition-all shadow-soft"
        >
          메인으로 돌아가기
        </button>
      </div>

      <!-- Meeting Invitation -->
      <div v-else-if="meetingInfo" class="bg-white rounded-3xl shadow-soft p-8 border border-gray-100 relative overflow-hidden">
        <!-- Decorative Background -->
        <div class="absolute -top-10 -right-10 w-32 h-32 bg-pastel-mint/20 rounded-full blur-3xl"></div>
        <div class="absolute -bottom-10 -left-10 w-32 h-32 bg-pastel-pink/20 rounded-full blur-3xl"></div>
        
        <div class="relative z-10">
          <!-- Icon -->
          <div class="text-center mb-6">
            <div class="relative inline-block mb-4">
              <div class="absolute inset-0 bg-gradient-to-br from-primary to-pastel-blue rounded-full blur-md opacity-40"></div>
              <div class="relative w-20 h-20 bg-gradient-to-br from-primary to-pastel-blue rounded-full flex items-center justify-center shadow-soft">
                <span class="material-icons text-white text-4xl">group_add</span>
              </div>
            </div>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">모임 초대</h2>
            <p class="text-gray-600 font-medium">새로운 모임에 초대되었습니다!</p>
          </div>

          <!-- Meeting Info -->
          <div class="bg-gradient-to-r from-primary/10 to-pastel-blue/10 rounded-2xl p-5 mb-6 border border-primary/20">
            <div class="flex items-center justify-between mb-3">
              <span class="text-sm text-gray-600 font-medium">모임 이름</span>
              <span class="text-lg font-bold text-gray-800">{{ meetingInfo.name }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-600 font-medium">참여 인원</span>
              <span class="flex items-center gap-2">
                <span class="material-icons text-primary-dark text-lg">group</span>
                <span class="text-lg font-bold text-primary-dark">{{ meetingInfo.participantCount }}명</span>
              </span>
            </div>
          </div>

          <!-- Buttons -->
          <div class="space-y-3">
            <!-- Join Button -->
            <button
              @click="handleJoinMeeting"
              :disabled="isJoining"
              class="w-full px-6 py-4 bg-primary hover:bg-primary-dark text-gray-800 rounded-2xl font-bold text-lg shadow-glow transition-all transform active:scale-[0.98] flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!isJoining" class="material-icons">celebration</span>
              <span v-if="!isJoining">모임 참여하기</span>
              <span v-else class="flex items-center justify-center gap-2">
                <div class="w-5 h-5 border-2 border-gray-800 border-t-transparent rounded-full animate-spin"></div>
                참여 중...
              </span>
            </button>

            <!-- Go to Meeting List -->
            <button
              @click="goToMeetingList"
              :disabled="isJoining"
              class="w-full px-6 py-3 bg-white text-gray-700 border border-gray-200 rounded-2xl font-semibold hover:bg-gray-50 transition-all shadow-sm disabled:opacity-50"
            >
              참여중인 모임 목록 확인하기
            </button>
          </div>

          <!-- Info Text -->
          <div class="mt-6 pt-6 border-t border-gray-100">
            <div class="flex items-start gap-2 text-xs text-gray-500">
              <span class="material-icons text-amber-500 text-sm">info</span>
              <p class="leading-relaxed">
                모임에 참여하면 일정을 입력하고 다른 참여자들과 만날 날짜를 조율할 수 있습니다
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Nickname Setting Modal -->
    <div 
      v-if="showNicknameModal"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50"
      @click.self="closeNicknameModal"
    >
      <div class="bg-white rounded-3xl shadow-soft max-w-md w-full p-8 relative">
        <!-- Close Button -->
        <button
          @click="closeNicknameModal"
          class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <span class="material-icons">close</span>
        </button>

        <!-- Icon -->
        <div class="text-center mb-6">
          <div class="relative inline-block mb-4">
            <div class="absolute inset-0 bg-gradient-to-br from-primary to-secondary rounded-full blur-md opacity-40"></div>
            <div class="relative w-16 h-16 bg-gradient-to-br from-primary to-secondary rounded-full flex items-center justify-center shadow-soft">
              <span class="material-icons text-white text-3xl">person_add</span>
            </div>
          </div>
          <h3 class="text-xl font-bold text-gray-800 mb-2">닉네임 설정</h3>
          <p class="text-sm text-gray-600">
            입장을 위해선 닉네임 설정이 필요합니다.
          </p>
        </div>

        <!-- Input -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-2">
            닉네임
          </label>
          <input
            v-model="nicknameInput"
            type="text"
            placeholder="닉네임을 입력하세요 (최대 10자)"
            maxlength="10"
            class="w-full px-4 py-3 bg-neutral-light border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            @keyup.enter="handleNicknameSubmit"
            :disabled="isSettingNickname"
          />
          <p v-if="nicknameError" class="mt-2 text-sm text-red-500">
            {{ nicknameError }}
          </p>
          <p class="mt-2 text-xs text-gray-500">
            {{ nicknameInput.length }}/10자
          </p>
        </div>

        <!-- Buttons -->
        <div class="space-y-3">
          <button
            @click="handleNicknameSubmit"
            :disabled="isSettingNickname || !nicknameInput.trim()"
            class="w-full px-6 py-3 bg-primary hover:bg-primary-dark text-gray-800 rounded-2xl font-bold shadow-glow transition-all transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="!isSettingNickname">확인</span>
            <span v-else class="flex items-center justify-center gap-2">
              <div class="w-5 h-5 border-2 border-gray-800 border-t-transparent rounded-full animate-spin"></div>
              설정 중...
            </span>
          </button>
          <button
            @click="closeNicknameModal"
            :disabled="isSettingNickname"
            class="w-full px-6 py-3 bg-white text-gray-700 border border-gray-200 rounded-2xl font-semibold hover:bg-gray-50 transition-all disabled:opacity-50"
          >
            취소
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
