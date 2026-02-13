<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import Calendar from "../components/Calendar.vue";
import ShareModal from "../components/ShareModal.vue";
import NicknameModal from "../components/NicknameModal.vue";
import LeaveConfirmModal from "../components/LeaveConfirmModal.vue";
import { meetingAPI, userAPI, scheduleAPI } from "../services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

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
const monthlyAvailability = ref(null); // 월별 가용성 데이터 { totalMembers, dateAvailability: Map }
const userRole = ref('MEMBER'); // 현재 사용자의 역할 (HOST or MEMBER)

// 공유 모달 상태
const isShareModalOpen = ref(false);
const shareUrl = ref("");

// 닉네임 모달 상태
const showNicknameModal = ref(false);

// 탈퇴 모달 상태
const showLeaveModal = ref(false);
const isLeaving = ref(false);

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
    // meetingsStore의 캐싱 로직 사용
    const data = await meetingsStore.loadMeetingByShareCode(shareCode)
    
    console.log('📦 [MeetingDetail] 미팅 정보:', data);
    console.log('📦 [MeetingDetail] confirmDate 값:', data.confirmDate);
    console.log('📦 [MeetingDetail] confirmDate 타입:', typeof data.confirmDate);
    
    meeting.value = {
      shareCode: data.shareCode,
      name: data.name,
      memberNumber: data.memberNumber,
      participants: data.participants || [],
      startDate: data.startDate,
      meetingDate: data.meetingDate
    }
    
    // 사용자 역할 저장
    if (data.role) {
      userRole.value = data.role;
      console.log('👤 [MeetingDetail] 사용자 역할:', userRole.value);
    }
    
    // 확정된 일정이 있으면 파싱 (confirmDate 사용)
    if (data.confirmDate && data.confirmDate !== null && data.confirmDate !== undefined) {
      console.log('📅 [MeetingDetail] 확정된 일정 발견:', data.confirmDate);
      
      // LocalDateTime "2026-02-15T14:00:00"을 파싱
      const dateTimeString = String(data.confirmDate);
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
      console.log('ℹ️ [MeetingDetail] 확정된 일정 없음 (confirmDate가 없거나 null/undefined)');
      confirmedSchedule.value = null;
    }
  } catch (error) {
    console.error("❌ [MeetingDetail] 모임 정보 조회 실패:", error);
  }
};

const loadCalendarData = async () => {
  try {
    console.log(`🔄 [MeetingDetail] 월별 가용성 조회 중... (${currentYear.value}년 ${currentMonth.value}월)`);
    
    const response = await scheduleAPI.getMonthlyAvailability(shareCode, currentYear.value, currentMonth.value);
    console.log('📦 [MeetingDetail] 월별 가용성 응답:', response);
    
    const data = response.data || response;
    
    if (data && data.MembersScheduleByDate) {
      // Map으로 변환하여 빠른 검색 가능하도록
      const dateMap = new Map();
      
      data.MembersScheduleByDate.forEach(item => {
        dateMap.set(item.date, {
          availableCount: item.availableCount,
          unAvailableMembers: item.unAvailableMembers || []
        });
      });
      
      monthlyAvailability.value = {
        totalMembers: data.totalMembers,
        dateAvailability: dateMap
      };
      
      console.log('✅ [MeetingDetail] 월별 가용성 로드 완료');
      console.log('   - 전체 참여자:', data.totalMembers);
      console.log('   - 데이터 개수:', dateMap.size);
    } else {
      monthlyAvailability.value = null;
    }
  } catch (error) {
    console.error("❌ [MeetingDetail] 달력 데이터 조회 실패:", error);
    monthlyAvailability.value = null;
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

const handleEditMeeting = () => {
  // 수정 모드로 CreateMeeting 페이지로 이동
  router.push({
    path: '/create',
    query: {
      shareCode: shareCode,
      mode: 'edit'
    }
  });
};

const handleLeaveMeeting = () => {
  // 탈퇴 확인 모달 표시
  showLeaveModal.value = true;
};

const confirmLeave = async () => {
  isLeaving.value = true;
  
  try {
    console.log('🚪 [MeetingDetail] 모임 탈퇴 요청:', shareCode);
    
    await meetingAPI.leaveMeeting(shareCode);
    
    console.log('✅ [MeetingDetail] 모임 탈퇴 성공');
    
    // meetingsStore에서 캐시 제거
    meetingsStore.clearCurrentMeeting();
    
    alert('모임에서 나갔습니다.');
    router.push('/');
  } catch (error) {
    console.error('❌ [MeetingDetail] 모임 탈퇴 실패:', error);
    
    const errorData = error.response?.data;
    const errorMessage = errorData?.message || '모임 탈퇴에 실패했습니다.';
    
    alert(errorMessage);
  } finally {
    isLeaving.value = false;
    showLeaveModal.value = false;
  }
};

const cancelLeave = () => {
  showLeaveModal.value = false;
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
  return emojis[rank - 1] || "5️⃣";
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
    
    await meetingAPI.updateMeetingSchedule(shareCode, {
      name: null,
      meetingDate: meetingDate,
      startDate: null,
      startTime: null,
      endTime: null
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
    
    // 모임 정보 및 달력 데이터 다시 로드
    await loadMeetingDetail();
    await loadCalendarData();
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
    
    await meetingAPI.updateMeetingSchedule(shareCode, {
      name: null,
      meetingDate: null,
      startDate: null,
      startTime: null,
      endTime: null
    });
    
    // 확정된 일정 제거
    confirmedSchedule.value = null;
    
    console.log('✅ [MeetingDetail] 일정 초기화 완료');
    alert('모임 일정이 초기화되었습니다.');
    
    // 모임 정보 및 달력 데이터 다시 로드
    await loadMeetingDetail();
    await loadCalendarData();
  } catch (error) {
    console.error('❌ [MeetingDetail] 일정 초기화 실패:', error);
    alert('일정 초기화에 실패했습니다. 다시 시도해주세요.');
  } finally {
    isUpdatingSchedule.value = false;
  }
};
</script>

<template>
  <div>
    <div class="min-h-screen relative flex flex-col bg-background-light overflow-hidden text-gray-800 antialiased selection:bg-primary selection:text-neutral-dark font-display">
    <!-- 메인 컨텐츠 -->
    <main v-if="meeting" class="flex-1 overflow-y-auto pb-32 px-6 pt-2">
      <!-- 모임 정보 -->
      <div class="mb-8">
        <div class="flex items-start justify-between mb-4">
          <div class="flex-1">
            <h2 class="text-3xl font-extrabold text-gray-800 leading-tight mb-2">{{ meeting.name }}</h2>
            <p class="text-sm text-gray-500">
              Created by <span class="font-medium text-gray-700">{{ meeting.participants[0]?.nickname || '호스트' }}</span>
            </p>
          </div>
          <div class="flex gap-2 flex-shrink-0 ml-3">
            <!-- HOST: 수정 버튼 -->
            <button 
              v-if="userRole === 'HOST'"
              @click="handleEditMeeting"
              class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
              title="모임 정보 수정"
            >
              <span class="material-symbols-rounded text-gray-600 group-hover:text-primary-dark transition-colors">edit</span>
            </button>
            
            <!-- MEMBER: 탈퇴 버튼 -->
            <button 
              v-if="userRole === 'MEMBER'"
              @click="handleLeaveMeeting"
              class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-red-50 transition-colors group shadow-sm border border-gray-100"
              title="모임 탈퇴"
            >
              <span class="material-symbols-rounded text-gray-600 group-hover:text-red-500 transition-colors">logout</span>
            </button>
            
            <!-- 공유 버튼 (공통) -->
            <button 
              @click="handleShareClick"
              class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
            >
              <span class="material-symbols-rounded text-gray-600 group-hover:text-primary-dark transition-colors">ios_share</span>
            </button>
          </div>
        </div>

          <!-- 참여자 정보 -->
          <div class="flex items-center justify-between bg-white p-4 rounded-xl border border-gray-100 shadow-soft">
            <div class="flex -space-x-3 rtl:space-x-reverse overflow-hidden">
              <div 
                v-for="(participant, index) in meeting.participants.slice(0, 4)" 
                :key="index"
                class="relative group"
              >
                <img 
                  :src="participant.profileImgUrl" 
                  :alt="participant.nickname"
                  class="w-10 h-10 border-2 border-pastel-border rounded-full object-cover cursor-pointer transition-transform hover:scale-110"
                />
                <!-- 툴팁 -->
                <div class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-1.5 bg-gray-800 text-white text-xs font-medium rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50">
                  {{ participant.nickname }}
                </div>
              </div>
              <div 
                v-if="meeting.memberNumber > 4"
                class="flex items-center justify-center w-10 h-10 text-xs font-bold text-gray-600 bg-neutral-light border-2 border-pastel-border rounded-full hover:bg-gray-200"
              >
                +{{ meeting.memberNumber - 4 }}
              </div>
            </div>
            <button 
              @click="showNicknameModal = true"
              class="text-sm font-semibold text-primary-dark hover:text-primary transition-colors"
            >
              내 닉네임
            </button>
          </div>
        </div>

        <!-- 달력 -->
        <div class="bg-white rounded-2xl p-5 shadow-soft border border-gray-100 mb-6">
          <Calendar
            :year="currentYear"
            :month="currentMonth"
            :unavailableDates="unavailableDates"
            :monthlyAvailability="monthlyAvailability"
            :confirmedDate="confirmedSchedule?.day || null"
            :minDate="meeting?.startDate || null"
            @update:year="(val) => { currentYear = val; handleMonthChange(); }"
            @update:month="(val) => { currentMonth = val; handleMonthChange(); }"
          />

          <!-- 범례 -->
          <div class="flex items-center justify-center gap-4 mt-6 text-xs font-medium">
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded-full bg-primary"></span>
              <span class="text-gray-500">높음 (80%+)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded-full bg-tertiary border border-gray-100"></span>
              <span class="text-gray-500">중간 (50%+)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded-full bg-secondary"></span>
              <span class="text-gray-500">낮음</span>
            </div>
          </div>
        </div>

        <!-- 확정된 일정 표시 (있는 경우) -->
        <div 
          v-if="confirmedSchedule"
          class="bg-gradient-to-r from-primary/30 to-primary/10 border border-primary rounded-2xl p-4 mb-6 relative overflow-hidden"
        >
          <div class="absolute left-0 top-0 bottom-0 w-1 bg-primary"></div>
          <div class="flex items-center justify-between">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="text-xs font-bold text-gray-700 bg-primary px-2 py-0.5 rounded-full">확정됨</span>
              </div>
              <h4 class="text-lg font-bold text-gray-800">{{ confirmedSchedule.displayDate }} {{ confirmedSchedule.displayTime }}</h4>
            </div>
            <button 
              v-if="userStore.nickname"
              @click="handleResetSchedule"
              :disabled="isUpdatingSchedule"
              class="w-8 h-8 rounded-full bg-white flex items-center justify-center text-red-500 hover:bg-red-50 transition-all shadow-sm disabled:opacity-50"
            >
              <span class="material-symbols-rounded text-xl">close</span>
            </button>
          </div>
        </div>

        <!-- 추천 일정 -->
        <div class="bg-white border-t border-gray-100 rounded-t-3xl px-5 pt-6 pb-20 relative shadow-[0_-5px_20px_-5px_rgba(0,0,0,0.03)]">
          <div class="absolute top-2 left-1/2 -translate-x-1/2 w-10 h-1 bg-gray-200 rounded-full"></div>
          
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <span class="material-symbols-rounded text-primary-dark">auto_awesome</span>
              <h3 class="text-base font-bold text-gray-800">추천 일정</h3>
            </div>
            
            <!-- 필터 버튼 -->
            <div class="flex gap-1">
              <button
                @click="handleRecommendTypeChange('ALL')"
                class="px-2 py-1 text-xs font-medium rounded-lg transition-all"
                :class="recommendType === 'ALL' ? 'bg-primary text-gray-800' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              >
                전체
              </button>
              <button
                @click="handleRecommendTypeChange('WEEKDAY')"
                class="px-2 py-1 text-xs font-medium rounded-lg transition-all"
                :class="recommendType === 'WEEKDAY' ? 'bg-primary text-gray-800' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              >
                주중
              </button>
              <button
                @click="handleRecommendTypeChange('WEEKEND')"
                class="px-2 py-1 text-xs font-medium rounded-lg transition-all"
                :class="recommendType === 'WEEKEND' ? 'bg-primary text-gray-800' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              >
                주말
              </button>
            </div>
          </div>

          <div v-if="recommendedSchedules.length === 0" class="text-center py-10 text-gray-400 text-sm">
            아직 입력된 일정이 없습니다
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="item in recommendedSchedules"
              :key="`${item.day}-${item.startTime}`"
              class="flex items-center justify-between p-4 rounded-xl relative overflow-hidden group"
              :class="item.rank === 1 
                ? 'bg-gradient-to-r from-primary/30 to-primary/10 border border-primary' 
                : 'bg-white border border-gray-100 shadow-sm'"
            >
              <div v-if="item.rank === 1" class="absolute left-0 top-0 bottom-0 w-1 bg-primary"></div>
              
              <div class="relative z-10 flex items-center gap-3">
                <!-- 순위 메달 이모지 -->
                <div class="text-3xl flex-shrink-0">
                  <span v-if="item.rank === 1">🥇</span>
                  <span v-else-if="item.rank === 2">🥈</span>
                  <span v-else-if="item.rank === 3">🥉</span>
                  <span v-else-if="item.rank === 4">4️⃣</span>
                  <span v-else>5️⃣</span>
                </div>
                
                <div class="flex flex-col flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span v-if="item.rank === 1" class="text-xs font-bold text-gray-700 bg-primary px-2 py-0.5 rounded-full">
                      추천
                    </span>
                  </div>
                  <h4 class="text-lg font-bold text-gray-800">{{ item.displayDate }} {{ item.displayTime }}</h4>
                </div>
              </div>
              
              <div class="relative z-10">
                <button
                  v-if="userStore.nickname && !confirmedSchedule"
                  @click="handleConfirmSchedule(item)"
                  :disabled="isUpdatingSchedule"
                  class="w-8 h-8 rounded-full flex items-center justify-center transition-all shadow-sm disabled:opacity-50"
                  :class="item.rank === 1 
                    ? 'bg-white text-primary hover:bg-primary hover:text-white' 
                    : 'bg-gray-50 text-gray-400 hover:text-white hover:bg-gray-400'"
                >
                  <span class="material-symbols-rounded">{{ item.rank === 1 ? 'check' : 'add' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 로딩 상태 -->
      <div v-else class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <div class="w-12 h-12 border-4 border-primary border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p class="text-gray-600">로딩 중...</p>
        </div>
      </div>

    <!-- 그라데이션 오버레이 -->
    <div class="fixed bottom-0 left-0 right-0 h-24 bg-gradient-to-t from-background-light to-transparent pointer-events-none z-20 max-w-app mx-auto"></div>

    <!-- 하단 고정 버튼 -->
    <div class="fixed bottom-0 left-0 right-0 z-30 max-w-app mx-auto px-6 pb-6">
      <button
        @click="handleScheduleInput"
        class="w-full bg-primary hover:bg-primary-dark text-gray-800 font-extrabold text-lg py-4 rounded-2xl shadow-glow transition-all transform active:scale-[0.98] flex items-center justify-center gap-2"
      >
          <span class="material-symbols-rounded">edit_calendar</span>
          내 일정 추가하기
        </button>
    </div>
    </div>

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

    <!-- 탈퇴 확인 모달 -->
    <LeaveConfirmModal
      :isOpen="showLeaveModal"
      :isLeaving="isLeaving"
      :meetingName="meeting?.name"
      @confirm="confirmLeave"
      @cancel="cancelLeave"
    />
  </div>
</template>
