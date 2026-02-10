<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import Calendar from "../components/Calendar.vue";
import TimeCalendar from "../components/TimeCalendar.vue";
import NicknameModal from "../components/NicknameModal.vue";
import { scheduleAPI, userAPI } from "../services";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const shareCode = route.params.shareCode;
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const selectedDates = ref([]);
const selectedTimes = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);
const viewMode = ref("date");

// 닉네임 모달 상태
const showNicknameModal = ref(false);

onMounted(async () => {
  // 1️⃣ App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    console.log('⏳ [ScheduleInput] 초기화 대기 중...')
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
      console.log('✅ [ScheduleInput] 초기화 완료')
    } else {
      console.log('⚠️ [ScheduleInput] 초기화 타임아웃')
    }
  }

  // 2️⃣ 사용자 정보가 없으면 로드
  if (!userStore.isLoggedIn || !userStore.nickname) {
    console.log('🔄 [ScheduleInput] 사용자 정보 로드 시도...')
    try {
      const userInfoResponse = await userAPI.getUserInfo()
      const userInfo = userInfoResponse.data || userInfoResponse
      
      console.log('📦 [ScheduleInput] 받은 사용자 정보:', userInfo)
      
      if (userInfo && (userInfo.nickname || userInfo.profileImgUrl || userInfo.provider)) {
        userStore.login({
          nickname: userInfo.nickname || '',
          profileImgUrl: userInfo.profileImgUrl || '',
          provider: userInfo.provider || ''
        })
        console.log('✅ [ScheduleInput] 사용자 정보 로드 완료:', userInfo.nickname, '(', userInfo.provider, ')')
      } else {
        console.log('⚠️ [ScheduleInput] 사용자 정보 없음')
      }
    } catch (error) {
      console.error('⚠️ [ScheduleInput] 사용자 정보 로드 실패:', error)
      // 로그인 실패 시 아무것도 하지 않음 (로그인 안 한 상태 유지)
    }
  }

  // 3️⃣ 닉네임 체크 (사용자 정보 로드 후)
  if (!userStore.nickname) {
    console.log('⚠️ [ScheduleInput] 닉네임 없음 - 모달 표시');
    showNicknameModal.value = true;
  } else {
    console.log('✅ [ScheduleInput] 닉네임 존재:', userStore.nickname);
  }

  // 4️⃣ 사용자 일정 로드
  await loadUserSchedule();
});

/**
 * 백엔드에서 받은 일정 데이터를 날짜/시간 선택으로 변환
 * @param {Array<Object>} schedules - [{ unavailableDate, unavailableStartTime, unavailableEndTime }, ...]
 */
const convertSchedulesToSelections = (schedules) => {
  const dates = [];
  const times = [];

  console.log('🔄 [convertSchedulesToSelections] 변환 시작, 개수:', schedules.length);

  schedules.forEach((schedule, index) => {
    console.log(`🔄 [Schedule ${index}] unavailableDate:`, schedule.unavailableDate);
    console.log(`🔄 [Schedule ${index}] unavailableStartTime:`, schedule.unavailableStartTime);
    console.log(`🔄 [Schedule ${index}] unavailableEndTime:`, schedule.unavailableEndTime);

    // LocalDate와 LocalTime을 결합하여 Date 객체 생성
    const startDateTime = `${schedule.unavailableDate}T${schedule.unavailableStartTime}`;
    const endDateTime = `${schedule.unavailableDate}T${schedule.unavailableEndTime}`;
    
    const start = new Date(startDateTime);
    const end = new Date(endDateTime);

    console.log(`🔄 [Schedule ${index}] start Date 객체:`, start);
    console.log(`🔄 [Schedule ${index}] end Date 객체:`, end);
    console.log(`🔄 [Schedule ${index}] start 시간: ${start.getHours()}:${start.getMinutes()}:${start.getSeconds()}`);
    console.log(`🔄 [Schedule ${index}] end 시간: ${end.getHours()}:${end.getMinutes()}:${end.getSeconds()}`);

    // 날짜 달력용: 09:00:00 ~ 23:59:59인 경우 날짜로 추가
    if (
      start.getHours() === 9 && start.getMinutes() === 0 && start.getSeconds() === 0 &&
      end.getHours() === 23 && end.getMinutes() === 59 && end.getSeconds() === 59
    ) {
      console.log(`📅 [Schedule ${index}] 날짜로 추가:`, schedule.unavailableDate);
      dates.push(schedule.unavailableDate);
    }
    
    // 시간 달력용: 30분 단위로 시간 추가 (09:00:00 ~ 23:59:59도 포함)
    const current = new Date(start);
    console.log(`⏰ [Schedule ${index}] 시간으로 처리 시작`);
    
    let count = 0;
    while (current < end) {
      const year = current.getFullYear();
      const month = String(current.getMonth() + 1).padStart(2, '0');
      const day = String(current.getDate()).padStart(2, '0');
      const hour = String(current.getHours()).padStart(2, '0');
      const minute = String(current.getMinutes()).padStart(2, '0');
      const timeString = `${year}-${month}-${day}T${hour}:${minute}`;
      
      console.log(`⏰ [Schedule ${index}] 시간 추가:`, timeString);
      times.push(timeString);
      count++;
      
      // 30분 증가
      current.setMinutes(current.getMinutes() + 30);
    }
    console.log(`⏰ [Schedule ${index}] 총 ${count}개 시간 추가됨`);
  });

  console.log('✅ [convertSchedulesToSelections] 최종 dates:', dates);
  console.log('✅ [convertSchedulesToSelections] 최종 times:', times);

  return { dates, times };
};

const loadUserSchedule = async () => {
  isLoading.value = true;
  try {
    // 기존 선택 초기화
    selectedDates.value = [];
    selectedTimes.value = [];

    // 백엔드에서 내 일정 조회
    const response = await scheduleAPI.getMyScheduleByShareCode(shareCode);
    
    console.log('📥 [ScheduleInput] API 응답 전체:', response);
    console.log('📥 [ScheduleInput] 응답 타입:', typeof response);
    console.log('📥 [ScheduleInput] 배열인가?:', Array.isArray(response));

    // 응답 데이터 추출 (response.data 또는 response 자체가 배열일 수 있음)
    let schedules = response;
    if (response && response.data && Array.isArray(response.data)) {
      schedules = response.data;
    } else if (!Array.isArray(response)) {
      schedules = [];
    }

    console.log('📥 [ScheduleInput] 처리할 일정 배열:', schedules);

    // 응답 데이터가 있으면 변환
    if (schedules && schedules.length > 0) {
      const { dates, times } = convertSchedulesToSelections(schedules);
      selectedDates.value = [...dates];
      selectedTimes.value = [...times];
      
      console.log('✅ [ScheduleInput] 변환된 날짜:', selectedDates.value);
      console.log('✅ [ScheduleInput] 변환된 시간:', selectedTimes.value);
      console.log('✅ [ScheduleInput] selectedDates.value.length:', selectedDates.value.length);
      console.log('✅ [ScheduleInput] selectedTimes.value.length:', selectedTimes.value.length);
    } else {
      console.log('ℹ️ [ScheduleInput] 저장된 일정이 없습니다.');
    }
  } catch (error) {
    console.error("❌ [ScheduleInput] 일정 조회 실패:", error);
    console.error("❌ [ScheduleInput] 에러 상세:", error.response || error.message);
    // 에러가 발생해도 빈 배열로 계속 진행 (처음 입력하는 경우일 수 있음)
    selectedDates.value = [];
    selectedTimes.value = [];
  } finally {
    isLoading.value = false;
  }
};

const handleDateClick = (dateString) => {
  const index = selectedDates.value.indexOf(dateString);
  if (index > -1) {
    selectedDates.value.splice(index, 1);
  } else {
    selectedDates.value.push(dateString);
  }
};

const handleTimeClick = (timeString) => {
  const index = selectedTimes.value.indexOf(timeString);
  if (index > -1) {
    selectedTimes.value.splice(index, 1);
  } else {
    selectedTimes.value.push(timeString);
  }
};

/**
 * 날짜를 하루 전체 범위로 변환
 * @param {Array<string>} selectedDates - ['2026-02-25', '2026-02-26', ...]
 * @returns {Array<Object>} - [{ unavailableDate: '2026-02-25', unavailableStartTime: '09:00:00', unavailableEndTime: '23:59:59' }, ...]
 */
const convertDatesToRanges = (selectedDates) => {
  if (!selectedDates || selectedDates.length === 0) return [];
  
  return selectedDates.map(dateString => {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return {
      unavailableDate: `${year}-${month}-${day}`,
      unavailableStartTime: '09:00:00',
      unavailableEndTime: '23:59:59'
    };
  });
};

/**
 * 선택된 시간들을 연속된 시간 범위로 그룹핑
 * @param {Array<string>} selectedTimes - ['2026-01-15T14:00', '2026-01-15T15:00', ...]
 * @returns {Array<Object>} - [{ unavailableDate: '2026-01-15', unavailableStartTime: '14:00:00', unavailableEndTime: '16:00:00' }, ...]
 */
const convertTimesToRanges = (selectedTimes) => {
  if (!selectedTimes || selectedTimes.length === 0) return [];
  
  // 시간순으로 정렬
  const sortedTimes = [...selectedTimes].sort();
  const ranges = [];
  let rangeStart = sortedTimes[0];
  let rangeEnd = sortedTimes[0];
  
  for (let i = 1; i < sortedTimes.length; i++) {
    const currentTime = new Date(sortedTimes[i]);
    const previousTime = new Date(sortedTimes[i - 1]);
    
    // 이전 시간과 정확히 30분 차이만 연속으로 간주
    const timeDiff = (currentTime - previousTime) / (1000 * 60); // 분 단위
    
    if (timeDiff === 30) {
      // 연속된 시간이면 범위 확장
      rangeEnd = sortedTimes[i];
    } else {
      // 연속되지 않으면 이전 범위 저장하고 새 범위 시작
      ranges.push(convertToScheduleRequest(rangeStart, rangeEnd, true));
      rangeStart = sortedTimes[i];
      rangeEnd = sortedTimes[i];
    }
  }
  
  // 마지막 범위 추가
  ranges.push(convertToScheduleRequest(rangeStart, rangeEnd, true));
  
  return ranges;
};

/**
 * ISO 8601 형식을 ScheduleRequest 형식으로 변환
 * @param {string} startTimeString - '2026-01-15T14:00'
 * @param {string} endTimeString - '2026-01-15T15:00'
 * @param {boolean} addEndTime - 종료 시간에 30분 추가 여부
 * @returns {Object} - { unavailableDate: '2026-01-15', unavailableStartTime: '14:00:00', unavailableEndTime: '15:30:00' }
 */
const convertToScheduleRequest = (startTimeString, endTimeString, addEndTime = false) => {
  const startDate = new Date(startTimeString);
  const endDate = new Date(endTimeString);
  
  const year = startDate.getFullYear();
  const month = String(startDate.getMonth() + 1).padStart(2, '0');
  const day = String(startDate.getDate()).padStart(2, '0');
  
  const startHour = String(startDate.getHours()).padStart(2, '0');
  const startMinute = String(startDate.getMinutes()).padStart(2, '0');
  
  // 종료 시간 처리
  let endHour, endMinute, endSecond;
  
  // 23:30인 경우 23:59:59로 처리
  if (endDate.getHours() === 23 && endDate.getMinutes() === 30 && addEndTime) {
    endHour = '23';
    endMinute = '59';
    endSecond = '59';
  } else {
    // 종료 시간에 30분 추가
    if (addEndTime) {
      endDate.setMinutes(endDate.getMinutes() + 30);
    }
    
    endHour = String(endDate.getHours()).padStart(2, '0');
    endMinute = String(endDate.getMinutes()).padStart(2, '0');
    endSecond = '00';
  }
  
  return {
    unavailableDate: `${year}-${month}-${day}`,
    unavailableStartTime: `${startHour}:${startMinute}:00`,
    unavailableEndTime: `${endHour}:${endMinute}:${endSecond}`
  };
};

const toggleViewMode = () => {
  viewMode.value = viewMode.value === "date" ? "time" : "date";
};

const selectedCount = computed(() => {
  return viewMode.value === "date"
    ? selectedDates.value.length
    : selectedTimes.value.length;
});

/**
 * 선택된 시간들을 연속된 그룹으로 변환 (화면 표시용)
 * @returns {Array<Object>} - [{ start: '2026-01-15T14:00', end: '2026-01-15T16:00', times: [...] }, ...]
 */
const groupedTimeRanges = computed(() => {
  if (selectedTimes.value.length === 0) return [];
  
  // 시간순으로 정렬
  const sortedTimes = [...selectedTimes.value].sort();
  const groups = [];
  let groupStart = sortedTimes[0];
  let groupEnd = sortedTimes[0];
  let groupTimes = [sortedTimes[0]];
  
  for (let i = 1; i < sortedTimes.length; i++) {
    const currentTime = new Date(sortedTimes[i]);
    const previousTime = new Date(sortedTimes[i - 1]);
    
    // 이전 시간과 정확히 30분 차이만 연속으로 간주
    const timeDiff = (currentTime - previousTime) / (1000 * 60); // 분 단위
    
    if (timeDiff === 30) {
      // 연속된 시간이면 그룹 확장
      groupEnd = sortedTimes[i];
      groupTimes.push(sortedTimes[i]);
    } else {
      // 연속되지 않으면 이전 그룹 저장하고 새 그룹 시작
      groups.push({
        start: groupStart,
        end: groupEnd,
        times: [...groupTimes]
      });
      groupStart = sortedTimes[i];
      groupEnd = sortedTimes[i];
      groupTimes = [sortedTimes[i]];
    }
  }
  
  // 마지막 그룹 추가
  groups.push({
    start: groupStart,
    end: groupEnd,
    times: [...groupTimes]
  });
  
  return groups;
});

const handleSave = async () => {
  const hasSelection =
    viewMode.value === "date"
      ? selectedDates.value.length > 0
      : selectedTimes.value.length > 0;

  if (!hasSelection) {
    const message =
      viewMode.value === "date"
        ? "최소 1개 이상의 날짜를 선택해주세요"
        : "최소 1개 이상의 시간을 선택해주세요";
    alert(message);
    return;
  }

  isSaving.value = true;
  try {
    // viewMode에 따라 날짜 또는 시간 범위로 변환
    let scheduleRanges;
    if (viewMode.value === "date") {
      scheduleRanges = convertDatesToRanges(selectedDates.value);
    } else {
      scheduleRanges = convertTimesToRanges(selectedTimes.value);
    }
    
    console.log("전송할 데이터:", scheduleRanges);
    
    // 백엔드로 전송
    await scheduleAPI.saveSchedule(shareCode, scheduleRanges);
    
    alert("일정이 저장되었습니다!");
    router.back();
  } catch (error) {
    console.error("일정 저장 실패:", error);
    alert("일정 저장에 실패했습니다");
  } finally {
    isSaving.value = false;
  }
};

const handleCancel = () => {
  router.back();
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
  const weekday = weekdays[date.getDay()];
  return `${month}/${day} (${weekday})`;
};

const formatTime = (timeString) => {
  const date = new Date(timeString);
  const day = date.getDate();
  const hour = date.getHours();
  return `${day}일 ${hour}시`;
};

/**
 * 시간 범위를 "11일 20:00 ~ 21:30" 형식으로 포맷
 * @param {string} startTime - 시작 시간
 * @param {string} endTime - 종료 시간
 * @returns {string}
 */
const formatTimeRange = (startTime, endTime) => {
  const startDate = new Date(startTime);
  const endDate = new Date(endTime);
  
  const day = startDate.getDate();
  const startHour = String(startDate.getHours()).padStart(2, '0');
  const startMinute = String(startDate.getMinutes()).padStart(2, '0');
  
  // 종료 시간이 23:30인 경우 23:59로 표시
  if (endDate.getHours() === 23 && endDate.getMinutes() === 30) {
    return `${day}일 ${startHour}:${startMinute} ~ 23:59`;
  }
  
  // 종료 시간에 30분 추가 (실제 종료 시간 표시)
  endDate.setMinutes(endDate.getMinutes() + 30);
  
  const endHour = String(endDate.getHours()).padStart(2, '0');
  const endMinute = String(endDate.getMinutes()).padStart(2, '0');
  
  return `${day}일 ${startHour}:${startMinute} ~ ${endHour}:${endMinute}`;
};

/**
 * 시간 범위 그룹 전체를 제거
 * @param {Array<string>} times - 제거할 시간들의 배열
 */
const handleTimeRangeRemove = (times) => {
  // times 배열에 포함된 모든 시간을 selectedTimes에서 제거
  selectedTimes.value = selectedTimes.value.filter(time => !times.includes(time));
};

const closeNicknameModal = () => {
  // 닉네임이 설정되었는지 확인
  if (userStore.nickname) {
    showNicknameModal.value = false;
    console.log('✅ [ScheduleInput] 닉네임 설정 완료:', userStore.nickname);
  } else {
    // 닉네임이 없으면 모달을 닫지 않고 미팅 상세로 돌아감
    alert('닉네임을 설정해야 일정을 추가할 수 있습니다.');
    router.back();
  }
};
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-10">
    <div class="w-full">
      <div class="mb-5 flex justify-between items-start gap-4">
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-gray-800 mb-2">
            내 일정 제외하기
          </h2>
          <p class="text-sm text-gray-600">
            모임이
            <strong class="text-primary font-semibold">{{
              viewMode === "date" ? "불가능한 날짜" : "불가능한 시간"
            }}</strong
            >을 선택해주세요
          </p>
        </div>
        <button
          class="px-5 py-2.5 bg-white border-2 border-primary rounded-lg text-primary text-sm font-semibold cursor-pointer transition-all whitespace-nowrap min-w-[100px] hover:bg-primary hover:text-white active:scale-95"
          @click="toggleViewMode"
        >
          {{ viewMode === "date" ? "시간변경" : "날짜변경" }}
        </button>
      </div>

      <div class="mb-5">
        <Calendar
          v-if="viewMode === 'date'"
          :year="currentYear"
          :month="currentMonth"
          :unavailableDates="[]"
          :selectedDates="selectedDates"
          @update:year="(val) => (currentYear = val)"
          @update:month="(val) => (currentMonth = val)"
          @dateClick="handleDateClick"
        />

        <TimeCalendar
          v-else
          :selectedTimes="selectedTimes"
          @timeClick="handleTimeClick"
        />

        <div class="bg-white px-3 py-3 text-center rounded-b-xl -mt-3">
          <p class="text-sm text-gray-600">
            <span class="text-xl font-bold text-primary">{{
              selectedCount
            }}</span
            >개의 {{ viewMode === "date" ? "날짜" : "시간" }} 선택됨
          </p>
        </div>
      </div>

      <div
        v-if="selectedCount > 0"
        class="bg-white rounded-2xl p-5 mb-4 shadow-sm"
      >
        <h3 class="text-base font-semibold text-gray-800 mb-3">
          선택한 {{ viewMode === "date" ? "날짜" : "시간" }}
        </h3>

        <div v-if="viewMode === 'date'" class="flex flex-wrap gap-2">
          <div
            v-for="date in [...selectedDates].sort()"
            :key="date"
            class="flex items-center gap-2 px-3 py-2 bg-blue-50 border border-primary rounded-full text-sm text-gray-800"
          >
            <span>{{ formatDate(date) }}</span>
            <button
              class="bg-none border-none text-gray-400 text-base cursor-pointer p-0 w-5 h-5 flex items-center justify-center transition-colors hover:text-red-500"
              @click="handleDateClick(date)"
            >
              ✕
            </button>
          </div>
        </div>

        <div v-else class="flex flex-wrap gap-2">
          <div
            v-for="(range, index) in groupedTimeRanges"
            :key="`range-${index}`"
            class="flex items-center gap-2 px-3 py-2 bg-blue-50 border border-primary rounded-full text-sm text-gray-800"
          >
            <span>{{ formatTimeRange(range.start, range.end) }}</span>
            <button
              class="bg-none border-none text-gray-400 text-base cursor-pointer p-0 w-5 h-5 flex items-center justify-center transition-colors hover:text-red-500"
              @click="handleTimeRangeRemove(range.times)"
            >
              ✕
            </button>
          </div>
        </div>
      </div>

      <div class="bg-yellow-50 rounded-xl p-4 mb-5">
        <p class="text-sm text-gray-600 leading-relaxed text-center">
          💡 선택한 날짜들은 다른 참여자들과 비교하여<br />
          가장 많은 사람이 가능한 날짜를 추천해드립니다
        </p>
      </div>

      <div class="flex gap-3">
        <button
          class="flex-1 px-3 py-4 border-none rounded-xl text-base font-semibold cursor-pointer transition-all bg-white border border-gray-300 text-gray-600 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
          @click="handleCancel"
          :disabled="isSaving"
        >
          취소
        </button>
        <button
          class="flex-1 px-3 py-4 border-none rounded-xl text-base font-semibold cursor-pointer transition-all bg-primary text-white hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98]"
          @click="handleSave"
          :disabled="isSaving || selectedCount === 0"
        >
          {{ isSaving ? "저장 중..." : "저장" }}
        </button>
      </div>
    </div>

    <!-- 닉네임 모달 -->
    <NicknameModal
      v-if="showNicknameModal"
      @close="closeNicknameModal"
    />
  </div>
</template>
