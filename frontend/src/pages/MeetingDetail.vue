<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import Calendar from "../components/Calendar.vue";
import ShareModal from "../components/ShareModal.vue";
import NicknameModal from "../components/NicknameModal.vue";
import { meetingAPI, userAPI } from "../services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const shareCode = route.params.shareCode;
const meeting = ref(null);
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const unavailableDates = ref([]);
const recommendedDates = ref([]);
const isLoading = ref(false);

// 공유 모달 상태
const isShareModalOpen = ref(false);
const shareUrl = ref("");

// 닉네임 모달 상태
const showNicknameModal = ref(false);

onMounted(async () => {
  // 1️⃣ App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    console.log('⏳ [MeetingDetail] 초기화 대기 중...')
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
      console.log('✅ [MeetingDetail] 초기화 완료')
    } else {
      console.log('⚠️ [MeetingDetail] 초기화 타임아웃')
    }
  }

  // 2️⃣ 사용자 정보가 없으면 로드 (invite에서 왔을 경우)
  if (!userStore.isLoggedIn || !userStore.nickname) {
    console.log('🔄 [MeetingDetail] 사용자 정보 로드 시도...')
    try {
      const userInfoResponse = await userAPI.getUserInfo()
      const userInfo = userInfoResponse.data || userInfoResponse
      
      console.log('📦 [MeetingDetail] 받은 사용자 정보:', userInfo)
      
      if (userInfo && (userInfo.nickname || userInfo.profileImgUrl)) {
        userStore.login({
          nickname: userInfo.nickname || '',
          profileImgUrl: userInfo.profileImgUrl || ''
        })
        console.log('✅ [MeetingDetail] 사용자 정보 로드 완료:', userInfo.nickname)
      } else {
        console.log('⚠️ [MeetingDetail] 사용자 정보 없음')
      }
    } catch (error) {
      console.error('⚠️ [MeetingDetail] 사용자 정보 로드 실패:', error)
      // 로그인 실패 시 아무것도 하지 않음 (로그인 안 한 상태 유지)
    }
  }

  // 3️⃣ 닉네임 체크 (사용자 정보 로드 후)
  if (!userStore.nickname) {
    console.log('⚠️ [MeetingDetail] 닉네임 없음 - 모달 표시');
    showNicknameModal.value = true;
  } else {
    console.log('✅ [MeetingDetail] 닉네임 존재:', userStore.nickname);
  }

  // 4️⃣ 모임 데이터 로드
  await loadMeetingDetail();
  await loadCalendarData();
  await loadRecommendedDates();
});

const loadMeetingDetail = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    const response = await meetingAPI.getMeetingDetailByShareCode(shareCode)
    const data = response.data || response
    meeting.value = {
      shareCode: shareCode,
      name: data.name,
      memberNumber: data.memberNumber,
      participants: data.info || []
    }
  } catch (error) {
    console.error("모임 정보 조회 실패:", error);
  }
};

const loadCalendarData = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const data = await meetingAPI.getMeetingCalendarByShareCode(shareCode, currentYear.value, currentMonth.value)
    // unavailableDates.value = data.unavailableDates

    // 임시 데이터
    unavailableDates.value = ["2026-01-26", "2026-01-27", "2026-01-30"];
  } catch (error) {
    console.error("달력 데이터 조회 실패:", error);
  }
};

const loadRecommendedDates = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const data = await meetingAPI.getRecommendedDatesByShareCode(shareCode)
    // recommendedDates.value = data.dates

    // 임시 데이터
    recommendedDates.value = [
      { date: "2026-02-01", availableCount: 5, rank: 1 },
      { date: "2026-02-05", availableCount: 4, rank: 2 },
      { date: "2026-02-08", availableCount: 4, rank: 3 },
      { date: "2026-02-12", availableCount: 3, rank: 4 },
      { date: "2026-02-15", availableCount: 3, rank: 5 },
    ];
  } catch (error) {
    console.error("추천 날짜 조회 실패:", error);
  }
};

const handleMonthChange = async () => {
  await loadCalendarData();
};

const handleShareClick = async () => {
  try {
    // 1. 공유 링크 생성
    shareUrl.value = `${window.location.origin}/invite/${shareCode}`;

    // 2. 즉시 클립보드에 복사
    await navigator.clipboard.writeText(shareUrl.value);
    
    // 3. 모달 열기
    isShareModalOpen.value = true;
  } catch (error) {
    console.error("공유 실패:", error);
    alert("공유 링크 생성에 실패했습니다.");
  }
};

const closeShareModal = () => {
  isShareModalOpen.value = false;
};

const closeNicknameModal = () => {
  // 닉네임이 설정되었는지 확인
  if (userStore.nickname) {
    showNicknameModal.value = false;
    console.log('✅ [MeetingDetail] 닉네임 설정 완료:', userStore.nickname);
  } else {
    // 닉네임이 없으면 모달을 닫지 않음
    alert('닉네임을 설정해야 모임에 참여할 수 있습니다.');
  }
};

const handleScheduleInput = () => {
  router.push(`/meeting/${shareCode}/schedule`);
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
  const weekday = weekdays[date.getDay()];
  return `${month}월 ${day}일 (${weekday})`;
};

const getRankEmoji = (rank) => {
  const emojis = ["🥇", "🥈", "🥉", "4️⃣", "5️⃣"];
  return emojis[rank - 1] || "";
};
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-10">
    <div v-if="meeting" class="w-full">
      <!-- 모임 헤더 -->
      <div class="flex justify-between items-center mb-3">
        <h2 class="text-2xl font-bold text-gray-800">{{ meeting.name }}</h2>
        <button
          class="flex items-center gap-1.5 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-semibold text-primary cursor-pointer transition-all hover:bg-blue-50 hover:border-primary"
          @click="handleShareClick"
        >
          <span>📤</span>
          <span>공유</span>
        </button>
      </div>

      <!-- 참여 현황 -->
      <div class="bg-white rounded-xl p-4 mb-5 shadow-sm">
        <div class="flex items-center justify-between">
          <!-- 참여자 수 -->
          <div class="flex items-center gap-2">
            <span class="text-2xl">👥</span>
            <span class="text-base text-gray-700">
              총 <strong class="text-primary font-semibold">{{ meeting.memberNumber }}명</strong> 참여중
            </span>
          </div>

          <!-- 참여자 프로필 이미지 (겹침 효과) -->
          <div class="flex items-center">
            <div 
              v-for="(participant, index) in meeting.participants" 
              :key="index"
              class="relative group"
              :style="{ 
                zIndex: meeting.participants.length - index,
                marginLeft: index > 0 ? '-12px' : '0'
              }"
            >
              <!-- 프로필 이미지 -->
              <img 
                :src="participant.profileImgUrl" 
                :alt="participant.nickname"
                class="w-10 h-10 rounded-full border-2 border-white object-cover cursor-pointer transition-transform hover:scale-110 hover:z-50"
              />
              
              <!-- 호버 시 닉네임 툴팁 -->
              <div class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-1.5 bg-gray-800 text-white text-xs font-medium rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap">
                {{ participant.nickname }}
                <!-- 화살표 -->
                <div class="absolute top-full left-1/2 transform -translate-x-1/2 -mt-0.5">
                  <div class="border-4 border-transparent border-t-gray-800"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 달력 -->
      <div class="mb-5">
        <Calendar
          :year="currentYear"
          :month="currentMonth"
          :unavailableDates="unavailableDates"
          @update:year="
            (val) => {
              currentYear = val;
              handleMonthChange();
            }
          "
          @update:month="
            (val) => {
              currentMonth = val;
              handleMonthChange();
            }
          "
        />

        <div class="flex justify-center mt-3">
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <div class="w-5 h-5 bg-gray-300 rounded"></div>
            <span>불가능한 날짜</span>
          </div>
        </div>
      </div>

      <!-- 일정 입력 버튼 -->
      <button
        class="w-full px-4 py-4 bg-primary text-white border-none rounded-xl text-base font-semibold cursor-pointer transition-colors mb-6 hover:bg-primary-dark active:scale-[0.98]"
        @click="handleScheduleInput"
      >
        내 일정 추가하기
      </button>

      <!-- 추천 날짜 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">추천 모임 날짜</h3>

        <div
          v-if="recommendedDates.length === 0"
          class="text-center py-10 text-gray-400 text-sm"
        >
          아직 입력된 일정이 없습니다
        </div>

        <div v-else class="flex flex-col gap-3">
          <div
            v-for="item in recommendedDates"
            :key="item.date"
            class="flex items-center gap-4 p-4 rounded-xl transition-all hover:translate-x-1"
            :class="
              item.rank === 1
                ? 'bg-gradient-to-r from-yellow-100 to-yellow-200'
                : 'bg-gray-50'
            "
          >
            <div class="text-3xl">
              {{ getRankEmoji(item.rank) }}
            </div>
            <div class="flex-1">
              <p class="text-base font-semibold text-gray-800 mb-1">
                {{ formatDate(item.date) }}
              </p>
              <p class="text-sm text-gray-600">
                {{ item.availableCount }}명 가능
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-10 text-gray-600">로딩 중...</div>

    <!-- 공유 모달 -->
    <ShareModal
      :isOpen="isShareModalOpen"
      :shareUrl="shareUrl"
      :meetingName="meeting?.name || '모임'"
      @close="closeShareModal"
    />

    <!-- 닉네임 모달 -->
    <NicknameModal
      v-if="showNicknameModal"
      @close="closeNicknameModal"
    />
  </div>
</template>
