<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import Calendar from "../components/Calendar.vue";
import ShareModal from "../components/ShareModal.vue";
import NicknameModal from "../components/NicknameModal.vue";
import { meetingAPI, userAPI, scheduleAPI } from "../services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const shareCode = route.params.shareCode;
const meeting = ref(null);
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const unavailableDates = ref([]);
const recommendedSchedules = ref([]);
const isLoading = ref(false);
const recommendType = ref('ALL'); // 'ALL', 'WEEKDAY', 'WEEKEND'
const confirmedSchedule = ref(null); // 확정된 일정 { day, startTime }
const isUpdatingSchedule = ref(false); // 일정 업데이트 중

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
      
      if (userInfo && (userInfo.nickname || userInfo.profileImgUrl || userInfo.provider)) {
        userStore.login({
          nickname: userInfo.nickname || '',
          profileImgUrl: userInfo.profileImgUrl || '',
          provider: userInfo.provider || ''
        })
        console.log('✅ [MeetingDetail] 사용자 정보 로드 완료:', userInfo.nickname, '(', userInfo.provider, ')')
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
  await loadRecommendedSchedules(recommendType.value);
});

const loadMeetingDetail = async () => {
  try {
    // API 호출
    const response = await meetingAPI.getMeetingDetailByShareCode(shareCode)
    console.log('📦 [MeetingDetail] API 응답 전체:', response);
    
    const data = response.data || response
    console.log('📦 [MeetingDetail] 파싱된 데이터:', data);
    console.log('📦 [MeetingDetail] data의 모든 키:', Object.keys(data));
    console.log('📦 [MeetingDetail] meetingDate 값:', data.meetingDate);
    console.log('📦 [MeetingDetail] meetingDate 타입:', typeof data.meetingDate);
    console.log('📦 [MeetingDetail] meetingDate가 undefined인가?:', data.meetingDate === undefined);
    console.log('📦 [MeetingDetail] meetingDate가 null인가?:', data.meetingDate === null);
    
    meeting.value = {
      shareCode: shareCode,
      name: data.name,
      memberNumber: data.memberNumber,
      participants: data.info || []
    }
    
    // 확정된 일정이 있으면 파싱
    if (data.meetingDate && data.meetingDate !== null && data.meetingDate !== undefined) {
      console.log('📅 [MeetingDetail] 확정된 일정 발견:', data.meetingDate);
      
      // LocalDateTime "2026-02-15T14:00:00"을 파싱
      const dateTimeString = String(data.meetingDate);
      const [datePart, timePart] = dateTimeString.split('T');
      
      console.log('📅 [MeetingDetail] datePart:', datePart);
      console.log('📅 [MeetingDetail] timePart:', timePart);
      
      confirmedSchedule.value = {
        day: datePart,
        startTime: timePart,
        displayDate: formatDate(datePart),
        displayTime: timePart ? timePart.substring(0, 5) : '00:00' // "14:00"
      };
      
      console.log('✅ [MeetingDetail] confirmedSchedule 설정됨:', confirmedSchedule.value);
    } else {
      console.log('ℹ️ [MeetingDetail] 확정된 일정 없음 (meetingDate가 없거나 null/undefined)');
      confirmedSchedule.value = null;
    }
  } catch (error) {
    console.error("❌ [MeetingDetail] 모임 정보 조회 실패:", error);
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

const loadRecommendedSchedules = async (type = 'ALL') => {
  try {
    console.log(`🔄 [MeetingDetail] 추천 스케줄 조회 중... (타입: ${type})`);
    
    const response = await scheduleAPI.getRecommendSchedule(shareCode, type);
    console.log('📦 [MeetingDetail] 추천 스케줄 응답:', response);
    
    // 응답 데이터 추출
    const data = response.data || response;
    
    if (Array.isArray(data) && data.length > 0) {
      // 데이터 파싱 및 변환
      recommendedSchedules.value = data.map((item, index) => ({
        rank: index + 1,
        day: item.day, // LocalDate 형식: "2026-02-15"
        startTime: item.startTime, // LocalTime 형식: "14:00:00" 또는 "14:00"
        endTime: item.endTime, // LocalTime 형식: "16:00:00" 또는 "16:00"
        displayDate: formatDate(item.day),
        displayTime: formatTimeRange(item.startTime, item.endTime)
      }));
      
      console.log('✅ [MeetingDetail] 추천 스케줄 로드 완료:', recommendedSchedules.value);
    } else {
      recommendedSchedules.value = [];
      console.log('ℹ️ [MeetingDetail] 추천 스케줄이 없습니다.');
    }
  } catch (error) {
    console.error("❌ [MeetingDetail] 추천 스케줄 조회 실패:", error);
    recommendedSchedules.value = [];
  }
};

// 추천 타입 변경 핸들러
const handleRecommendTypeChange = async (type) => {
  recommendType.value = type;
  await loadRecommendedSchedules(type);
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

const formatTimeRange = (startTime, endTime) => {
  // LocalTime 형식: "14:00:00" 또는 "14:00"
  const formatTime = (time) => {
    if (!time) return '';
    const parts = time.split(':');
    return `${parts[0]}:${parts[1]}`; // "14:00" 형식으로 반환
  };
  
  return `${formatTime(startTime)} ~ ${formatTime(endTime)}`;
};

const getRankEmoji = (rank) => {
  const emojis = ["🥇", "🥈", "🥉", "4️⃣", "5️⃣"];
  return emojis[rank - 1] || "";
};

/**
 * LocalDate와 LocalTime을 LocalDateTime으로 변환
 * @param {string} day - "2026-02-15"
 * @param {string} time - "14:00:00" 또는 "14:00"
 * @returns {string} - "2026-02-15T14:00:00"
 */
const convertToLocalDateTime = (day, time) => {
  // time이 "14:00" 형식이면 "14:00:00"으로 변환
  const timeParts = time.split(':');
  const formattedTime = timeParts.length === 2 
    ? `${timeParts[0]}:${timeParts[1]}:00` 
    : time;
  
  return `${day}T${formattedTime}`;
};

/**
 * 일정 확정하기
 */
const handleConfirmSchedule = async (schedule) => {
  if (isUpdatingSchedule.value) return;
  
  const confirmMessage = `${schedule.displayDate} ${schedule.displayTime}\n이 시간으로 모임을 확정하시겠습니까?`;
  if (!confirm(confirmMessage)) return;
  
  isUpdatingSchedule.value = true;
  
  try {
    // LocalDate + LocalTime -> LocalDateTime 변환
    const meetingDate = convertToLocalDateTime(schedule.day, schedule.startTime);
    
    console.log('🔄 [MeetingDetail] 일정 확정 요청:', { meetingDate });
    
    await scheduleAPI.updateMeetingSchedule(shareCode, {
      name: null,
      meetingDate: meetingDate
    });
    
    // 확정된 일정 저장
    confirmedSchedule.value = {
      day: schedule.day,
      startTime: schedule.startTime,
      displayDate: schedule.displayDate,
      displayTime: schedule.displayTime
    };
    
    console.log('✅ [MeetingDetail] 일정 확정 완료');
    alert('모임 일정이 확정되었습니다! 🎉');
    
    // 모임 정보 다시 로드
    await loadMeetingDetail();
  } catch (error) {
    console.error('❌ [MeetingDetail] 일정 확정 실패:', error);
    alert('일정 확정에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isUpdatingSchedule.value = false;
  }
};

/**
 * 일정 초기화하기
 */
const handleResetSchedule = async () => {
  if (isUpdatingSchedule.value) return;
  
  if (!confirm('확정된 일정을 초기화하시겠습니까?')) return;
  
  isUpdatingSchedule.value = true;
  
  try {
    console.log('🔄 [MeetingDetail] 일정 초기화 요청');
    
    await scheduleAPI.updateMeetingSchedule(shareCode, {
      name: null,
      meetingDate: null
    });
    
    // 확정된 일정 제거
    confirmedSchedule.value = null;
    
    console.log('✅ [MeetingDetail] 일정 초기화 완료');
    alert('모임 일정이 초기화되었습니다.');
    
    // 모임 정보 다시 로드
    await loadMeetingDetail();
  } catch (error) {
    console.error('❌ [MeetingDetail] 일정 초기화 실패:', error);
    alert('일정 초기화에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isUpdatingSchedule.value = false;
  }
};
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-10">
        <div v-if="meeting" class="w-full">
          <!-- 모임 헤더 -->
          <div class="flex justify-between items-center mb-3">
            <h2 class="text-2xl font-bold text-gray-800">{{ meeting.name }}</h2>
            
            <div class="flex items-center gap-2">
              <!-- 닉네임 변경 버튼 -->
              <button
                class="flex items-center gap-1.5 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-semibold text-gray-700 cursor-pointer transition-all hover:bg-gray-50 hover:border-gray-400"
                @click="showNicknameModal = true"
              >
                <span>✏️</span>
                <span>닉네임 변경</span>
              </button>
              
              <!-- 공유 버튼 -->
              <button
                class="flex items-center gap-1.5 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-semibold text-primary cursor-pointer transition-all hover:bg-blue-50 hover:border-primary"
                @click="handleShareClick"
              >
                <span>📤</span>
                <span>공유</span>
              </button>
            </div>
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

      <!-- 확정된 일정 -->
      <div 
        class="rounded-2xl p-5 mb-5 shadow-sm border-2"
        :class="confirmedSchedule 
          ? 'bg-gradient-to-r from-green-50 to-emerald-50 border-green-300' 
          : 'bg-gray-50 border-gray-300'"
      >
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-lg font-semibold flex items-center gap-2"
            :class="confirmedSchedule ? 'text-green-800' : 'text-gray-700'"
          >
            <span>{{ confirmedSchedule ? '✅' : '📅' }}</span>
            <span>확정된 모임 일정</span>
          </h3>
          <button
            v-if="userStore.nickname && confirmedSchedule"
            class="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            @click="handleResetSchedule"
            :disabled="isUpdatingSchedule"
          >
            일정 초기화
          </button>
        </div>
        
        <!-- 확정된 일정이 있는 경우 -->
        <div v-if="confirmedSchedule" class="bg-white rounded-xl p-4">
          <p class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <span>{{ confirmedSchedule.displayDate }}</span>
            <span class="text-primary">⏰ {{ confirmedSchedule.displayTime }}</span>
          </p>
        </div>
        
        <!-- 확정된 일정이 없는 경우 -->
        <div v-else class="bg-white rounded-xl p-4 text-center">
          <p class="text-gray-500 py-4">
            확정된 날짜가 존재하지 않습니다
          </p>
        </div>
      </div>

      <!-- 추천 일정 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-gray-800">추천 모임 일정</h3>
          
          <!-- 필터 버튼 -->
          <div class="flex gap-2">
            <button
              class="px-3 py-1.5 text-sm font-medium rounded-lg transition-all"
              :class="recommendType === 'ALL' 
                ? 'bg-primary text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="handleRecommendTypeChange('ALL')"
            >
              전체
            </button>
            <button
              class="px-3 py-1.5 text-sm font-medium rounded-lg transition-all"
              :class="recommendType === 'WEEKDAY' 
                ? 'bg-primary text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="handleRecommendTypeChange('WEEKDAY')"
            >
              주중
            </button>
            <button
              class="px-3 py-1.5 text-sm font-medium rounded-lg transition-all"
              :class="recommendType === 'WEEKEND' 
                ? 'bg-primary text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="handleRecommendTypeChange('WEEKEND')"
            >
              주말
            </button>
          </div>
        </div>

        <div
          v-if="recommendedSchedules.length === 0"
          class="text-center py-10 text-gray-400 text-sm"
        >
          아직 입력된 일정이 없습니다
        </div>

        <div v-else class="flex flex-col gap-3">
          <div
            v-for="item in recommendedSchedules"
            :key="`${item.day}-${item.startTime}`"
            class="flex items-center gap-3 p-4 rounded-xl transition-all"
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
                {{ item.displayDate }}
              </p>
              <p class="text-sm text-primary font-medium">
                ⏰ {{ item.displayTime }}
              </p>
            </div>
            <button
              v-if="userStore.nickname"
              class="px-4 py-2 bg-primary text-white text-sm font-semibold rounded-lg hover:bg-primary-dark transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap"
              @click="handleConfirmSchedule(item)"
              :disabled="isUpdatingSchedule"
            >
              일정 선택
            </button>
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
