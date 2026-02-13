<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import NicknameModal from "../components/NicknameModal.vue";
import { meetingAPI } from "../services";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

// 수정 모드 체크
const isEditMode = ref(false);
const editShareCode = ref(null);

const meetingName = ref("");
const isLoading = ref(false);
const error = ref("");

// 다음 가능한 30분 단위 시간 계산
const getNextAvailableTime = () => {
  const now = new Date();
  const currentHour = now.getHours();
  const currentMinute = now.getMinutes();
  
  // 현재 시간보다 미래의 가장 가까운 30분 단위로 올림
  let nextHour, nextMinute;
  
  if (currentMinute < 30) {
    nextHour = currentHour;
    nextMinute = 30;
  } else {
    // 30분 이후면 다음 시간의 0분
    nextHour = currentHour + 1;
    nextMinute = 0;
  }
  
  // 6시 이전이면 6:00으로 설정
  if (nextHour < 6) {
    return { hour: 6, minute: 0 };
  }
  
  // 23시를 넘으면 23:30으로 설정
  if (nextHour > 23) {
    return { hour: 23, minute: 30 };
  }
  
  // 23:30을 넘으면 23:30으로 설정
  if (nextHour === 23 && nextMinute > 30) {
    return { hour: 23, minute: 30 };
  }
  
  return { hour: nextHour, minute: nextMinute };
};

const nextTime = getNextAvailableTime();

// 날짜 및 시간 선택
const selectedDate = ref(new Date());
const selectedHour = ref(nextTime.hour);
const selectedMinute = ref(nextTime.minute);

// 종료 시간 (기본값 23:30)
const endHour = ref(23);
const endMinute = ref(30);

// 시간 옵션 생성 (6시부터 23시까지)
const allHours = Array.from({ length: 18 }, (_, i) => i + 6); // 6시 ~ 23시
const allMinutes = [0, 30];

// 오늘 날짜 체크
const isToday = computed(() => {
  const today = new Date();
  return selectedDate.value.getFullYear() === today.getFullYear() &&
         selectedDate.value.getMonth() === today.getMonth() &&
         selectedDate.value.getDate() === today.getDate();
});

// 선택 가능한 시간 목록 (오늘이면 현재 시간 이후만)
const availableHours = computed(() => {
  if (!isToday.value) return allHours;
  
  const currentHour = new Date().getHours();
  return allHours.filter(hour => hour >= currentHour);
});

// 선택 가능한 분 목록 (오늘이고 같은 시간이면 현재 분 이후만)
const availableMinutes = computed(() => {
  if (!isToday.value) return allMinutes;
  
  const currentHour = new Date().getHours();
  const currentMinute = new Date().getMinutes();
  
  if (selectedHour.value > currentHour) {
    return allMinutes;
  } else if (selectedHour.value === currentHour) {
    return allMinutes.filter(minute => minute > currentMinute);
  }
  
  return allMinutes;
});

// 종료 시간 선택 가능한 시간 목록 (시작 시간 + 30분 이후부터)
const availableEndHours = computed(() => {
  const startTotalMinutes = selectedHour.value * 60 + selectedMinute.value;
  const minEndTotalMinutes = startTotalMinutes + 30;
  
  return allHours.filter(hour => {
    const hourInMinutes = hour * 60;
    // 해당 시간의 마지막 분(30분)이 최소 종료 시간보다 크거나 같으면 선택 가능
    return hourInMinutes + 30 >= minEndTotalMinutes;
  });
});

// 종료 분 선택 가능한 분 목록
const availableEndMinutes = computed(() => {
  const startTotalMinutes = selectedHour.value * 60 + selectedMinute.value;
  const minEndTotalMinutes = startTotalMinutes + 30;
  
  return allMinutes.filter(minute => {
    const endTotalMinutes = endHour.value * 60 + minute;
    return endTotalMinutes >= minEndTotalMinutes;
  });
});

// 선택된 날짜/시간 포맷팅
const formattedDateTime = computed(() => {
  const year = selectedDate.value.getFullYear();
  const month = String(selectedDate.value.getMonth() + 1).padStart(2, '0');
  const day = String(selectedDate.value.getDate()).padStart(2, '0');
  const hour = String(selectedHour.value).padStart(2, '0');
  const minute = String(selectedMinute.value).padStart(2, '0');
  
  return `${year}년 ${month}월 ${day}일 ${hour}:${minute}`;
});

// 날짜 포맷 (한국어)
const formattedDateShort = computed(() => {
  const month = selectedDate.value.getMonth() + 1;
  const day = selectedDate.value.getDate();
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  const dayName = days[selectedDate.value.getDay()];
  
  return `${month}월 ${day}일 (${dayName})`;
});

// 시간 범위 포맷
const formattedTimeRange = computed(() => {
  const startH = String(selectedHour.value).padStart(2, '0');
  const startM = String(selectedMinute.value).padStart(2, '0');
  const endH = String(endHour.value).padStart(2, '0');
  const endM = String(endMinute.value).padStart(2, '0');
  
  return `${startH}:${startM} - ${endH}:${endM}`;
});

// 현재 월 이름 (한국어)
const currentMonthName = computed(() => {
  return `${currentYear.value}년 ${currentMonth.value + 1}월`;
});

// 닉네임 모달 상태
const showNicknameModal = ref(false);

onMounted(async () => {
  // App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    console.log('⏳ [CreateMeeting] 초기화 대기 중...')
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
      console.log('✅ [CreateMeeting] 초기화 완료')
    } else {
      console.log('⚠️ [CreateMeeting] 초기화 타임아웃')
    }
  }

  // 수정 모드 확인
  if (route.query.mode === 'edit' && route.query.shareCode) {
    isEditMode.value = true;
    editShareCode.value = route.query.shareCode;
    console.log('✏️ [CreateMeeting] 수정 모드:', editShareCode.value);
    
    // 기존 미팅 정보 로드
    await loadExistingMeeting(editShareCode.value);
  }
});

// 날짜가 과거인지 확인
const isPastDate = (date) => {
  if (!date) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const checkDate = new Date(date);
  checkDate.setHours(0, 0, 0, 0);
  return checkDate < today;
};

// 기존 미팅 정보 로드 (캐싱 사용)
const loadExistingMeeting = async (shareCode) => {
  try {
    console.log('🔄 [CreateMeeting] 기존 미팅 정보 로드 중...');
    
    // meetingsStore의 캐싱 로직 사용
    const data = await meetingsStore.loadMeetingByShareCode(shareCode);
    
    console.log('📦 [CreateMeeting] 기존 미팅 데이터:', data);
    
    // 모임 이름 설정
    if (data.name) {
      meetingName.value = data.name;
      console.log('📝 [CreateMeeting] 모임 이름:', data.name);
    }
    
    // 희망 날짜 설정 (meetingDate)
    if (data.meetingDate) {
      // meetingDate는 "2026-02-15T19:00:00" 형식
      const dateString = String(data.meetingDate).split('T')[0]; // "2026-02-15"
      const [year, month, day] = dateString.split('-').map(Number);
      selectedDate.value = new Date(year, month - 1, day);
      console.log('📅 [CreateMeeting] 희망 날짜:', dateString);
      
      // 시간도 meetingDate에서 추출
      const timeString = String(data.meetingDate).split('T')[1]; // "19:00:00"
      if (timeString) {
        const [hour, minute] = timeString.split(':').map(Number);
        selectedHour.value = hour;
        selectedMinute.value = minute;
        console.log('⏰ [CreateMeeting] 희망 시작 시간:', timeString);
      }
    }
    
    // 희망 종료 시간 설정 (백엔드 API 응답에 endTime이 있다면)
    // 참고: meetingsStore에는 endTime이 저장되지 않으므로
    // 필요하다면 별도로 API 호출이 필요할 수 있음
    // 현재는 기본값(23:30) 사용
    
    console.log('✅ [CreateMeeting] 기존 미팅 정보 로드 완료');
  } catch (error) {
    console.error('❌ [CreateMeeting] 기존 미팅 정보 로드 실패:', error);
    alert('미팅 정보를 불러오는데 실패했습니다.');
    router.back();
  }
};

// 날짜 선택 핸들러
const handleDateSelect = (date) => {
  selectedDate.value = date;
  
  // 날짜 변경 시 시간 유효성 체크
  if (isToday.value) {
    const currentHour = new Date().getHours();
    const currentMinute = new Date().getMinutes();
    
    // 선택된 시간이 현재보다 이전이면 현재 시간으로 설정
    if (selectedHour.value < currentHour) {
      selectedHour.value = currentHour;
      selectedMinute.value = Math.ceil(currentMinute / 30) * 30;
      if (selectedMinute.value >= 60) {
        selectedMinute.value = 0;
        selectedHour.value++;
      }
    } else if (selectedHour.value === currentHour && selectedMinute.value <= currentMinute) {
      selectedMinute.value = Math.ceil(currentMinute / 30) * 30;
      if (selectedMinute.value >= 60) {
        selectedMinute.value = 0;
        selectedHour.value++;
      }
    }
  }
};

// 이전/다음 달 이동
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth());

const prevMonth = () => {
  if (currentMonth.value === 0) {
    currentMonth.value = 11;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
};

const nextMonth = () => {
  if (currentMonth.value === 11) {
    currentMonth.value = 0;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
};

// 달력 날짜 생성
const calendarDays = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value + 1, 0);
  const startDay = firstDay.getDay();
  const days = [];
  
  // 이전 달의 빈 칸
  for (let i = 0; i < startDay; i++) {
    days.push(null);
  }
  
  // 현재 달의 날짜
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push(new Date(currentYear.value, currentMonth.value, i));
  }
  
  return days;
});

const isSameDay = (date1, date2) => {
  if (!date1 || !date2) return false;
  return date1.getFullYear() === date2.getFullYear() &&
         date1.getMonth() === date2.getMonth() &&
         date1.getDate() === date2.getDate();
};

const handleSubmit = async () => {
  if (!meetingName.value.trim()) {
    error.value = "모임 이름을 입력해주세요";
    return;
  }

  if (meetingName.value.length > 30) {
    error.value = "모임 이름은 30자 이하로 입력해주세요";
    return;
  }

  isLoading.value = true;
  error.value = "";

  try {
    if (isEditMode.value) {
      // 수정 모드: UPDATE API 호출
      const startDate = `${selectedDate.value.getFullYear()}-${String(selectedDate.value.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.value.getDate()).padStart(2, '0')}`;
      const startTime = `${String(selectedHour.value).padStart(2, '0')}:${String(selectedMinute.value).padStart(2, '0')}:00`;
      const endTime = `${String(endHour.value).padStart(2, '0')}:${String(endMinute.value).padStart(2, '0')}:00`;
      
      console.log('✏️ [CreateMeeting] 모임 수정 데이터:', {
        name: meetingName.value,
        meetingDate: null, // 확정일자는 수정하지 않음
        startDate,
        startTime,
        endTime
      });

      await meetingAPI.updateMeetingSchedule(editShareCode.value, {
        name: meetingName.value,
        meetingDate: null,
        startDate: startDate,
        startTime: startTime,
        endTime: endTime
      });
      
      alert('모임이 수정되었습니다! ✅');
      router.push(`/meeting/${editShareCode.value}`);
    } else {
      // 생성 모드: CREATE API 호출
      const startDate = `${selectedDate.value.getFullYear()}-${String(selectedDate.value.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.value.getDate()).padStart(2, '0')}`;
      const startTime = `${String(selectedHour.value).padStart(2, '0')}:${String(selectedMinute.value).padStart(2, '0')}:00`;
      const endTime = `${String(endHour.value).padStart(2, '0')}:${String(endMinute.value).padStart(2, '0')}:00`;
      
      console.log('➕ [CreateMeeting] 모임 생성 데이터:', {
        meetingName: meetingName.value,
        startDate,
        startTime,
        endTime
      });

      const response = await meetingAPI.createMeeting({
        meetingName: meetingName.value,
        startDate: startDate,
        startTime: startTime,
        endTime: endTime
      });
      
      console.log('✅ [CreateMeeting] 전체 응답:', response);
      
      const responseData = response.data || response;
      const actualData = responseData.data || responseData;
      const shareCode = actualData.shareCode;
      
      if (!shareCode) {
        throw new Error('shareCode를 받지 못했습니다');
      }
      
      alert('모임이 생성되었습니다! 🎉');
      router.push(`/meeting/${shareCode}`);
    }
  } catch (err) {
    error.value = isEditMode.value ? "모임 수정에 실패했습니다" : "모임 생성에 실패했습니다";
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.back();
};

const handleDelete = async () => {
  if (!isEditMode.value || !editShareCode.value) {
    return;
  }
  
  const confirmDelete = confirm(`"${meetingName.value}" 모임을 정말 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`);
  
  if (!confirmDelete) {
    return;
  }
  
  isLoading.value = true;
  error.value = "";
  
  try {
    console.log('🗑️ [CreateMeeting] 모임 삭제 요청:', editShareCode.value);
    
    await meetingAPI.deleteMeetingByShareCode(editShareCode.value);
    
    console.log('✅ [CreateMeeting] 모임 삭제 성공');
    
    // meetingsStore에서 캐시 제거
    meetingsStore.clearCurrentMeeting();
    
    alert('모임이 삭제되었습니다.');
    router.push('/');
  } catch (err) {
    console.error('❌ [CreateMeeting] 모임 삭제 실패:', err);
    
    const errorData = err.response?.data;
    const errorMessage = errorData?.message || '모임 삭제에 실패했습니다.';
    
    error.value = errorMessage;
    alert(errorMessage);
  } finally {
    isLoading.value = false;
  }
};

const closeNicknameModal = () => {
  // 닉네임이 설정되었는지 확인
  if (userStore.nickname) {
    showNicknameModal.value = false;
    console.log('✅ [CreateMeeting] 닉네임 설정 완료:', userStore.nickname);
  } else {
    // 닉네임이 없으면 메인으로 돌아감
    alert('닉네임을 설정해야 모임을 만들 수 있습니다.');
    router.push('/');
  }
};
</script>

<template>
  <div>
    <div class="min-h-screen relative flex flex-col bg-background-light overflow-hidden text-gray-800 antialiased selection:bg-primary selection:text-neutral-dark">
    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto no-scrollbar pb-32 px-6 pt-2">
        <!-- Meeting Name Input -->
        <div class="mb-6">
          <label class="block text-sm font-bold text-gray-600 mb-2 ml-1" for="meeting-name">모임 이름</label>
          <div class="relative">
            <input 
              id="meeting-name"
              v-model="meetingName"
              class="w-full bg-white border border-pastel-border rounded-xl px-4 py-4 text-lg font-semibold text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all shadow-soft" 
              placeholder="예) 여우들의 송년회 🦊" 
              type="text"
              maxlength="30"
              autofocus
            />
          </div>
          <p class="text-right text-xs text-gray-400 mt-1 mr-1">
            {{ meetingName.length }}/30
          </p>
        </div>

        <!-- Date Selection -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-3 ml-1">
            <label class="block text-sm font-bold text-gray-600">날짜 선택</label>
            <div class="flex items-center gap-2">
              <button 
                @click="prevMonth"
                class="p-1 hover:bg-neutral-light rounded-full transition-colors"
              >
                <span class="material-symbols-rounded text-gray-400 text-lg">chevron_left</span>
              </button>
              <span class="text-sm font-bold text-gray-800">{{ currentMonthName }}</span>
              <button 
                @click="nextMonth"
                class="p-1 hover:bg-neutral-light rounded-full transition-colors"
              >
                <span class="material-symbols-rounded text-gray-400 text-lg">chevron_right</span>
              </button>
            </div>
          </div>
          
          <div class="bg-white rounded-2xl p-4 shadow-soft border border-gray-100">
            <div class="grid grid-cols-7 gap-y-2 gap-x-1 mb-2 text-center">
              <div 
                v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
                :key="day"
                class="text-[10px] font-bold text-gray-400 uppercase tracking-wide"
              >
                {{ day }}
              </div>
              
              <!-- Empty cells for offset -->
              <div 
                v-for="n in calendarDays.findIndex(d => d !== null)"
                :key="`empty-${n}`"
                class="h-8"
              ></div>
              
              <!-- Date cells -->
              <div
                v-for="(day, index) in calendarDays.filter(d => d !== null)"
                :key="`day-${index}`"
                class="relative group cursor-pointer"
                @click="!isPastDate(day) && handleDateSelect(day)"
              >
                <div 
                  :class="[
                    'w-8 h-8 mx-auto flex items-center justify-center rounded-full text-sm font-medium transition-all',
                    isPastDate(day) 
                      ? 'text-gray-400 cursor-not-allowed' 
                      : isSameDay(day, selectedDate)
                      ? 'bg-primary text-gray-800 font-bold shadow-md shadow-primary/30'
                      : 'text-gray-600 hover:bg-neutral-light'
                  ]"
                >
                  {{ day.getDate() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Time Selection -->
        <div class="mb-44">
          <label class="block text-sm font-bold text-gray-600 mb-3 ml-1">모임 시간 선택</label>
          <div class="grid grid-cols-2 gap-4">
            <!-- Start Time -->
            <div class="bg-white rounded-2xl p-4 shadow-soft border border-gray-100">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wide block mb-3 text-center">
                {{ String(selectedHour).padStart(2, '0') }}:{{ String(selectedMinute).padStart(2, '0') }}부터
              </span>
              <div class="flex flex-col gap-3">
                <div>
                  <label class="text-xs text-gray-500 mb-1 block">Hour</label>
                  <select 
                    v-model="selectedHour"
                    class="w-full bg-neutral-light border-none rounded-lg px-3 py-2 text-center text-lg font-bold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  >
                    <option 
                      v-for="hour in availableHours" 
                      :key="hour" 
                      :value="hour"
                    >
                      {{ String(hour).padStart(2, '0') }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-gray-500 mb-1 block">Minute</label>
                  <select 
                    v-model="selectedMinute"
                    class="w-full bg-neutral-light border-none rounded-lg px-3 py-2 text-center text-lg font-bold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  >
                    <option 
                      v-for="minute in availableMinutes" 
                      :key="minute" 
                      :value="minute"
                    >
                      {{ String(minute).padStart(2, '0') }}
                    </option>
                  </select>
                </div>
              </div>
            </div>

            <!-- End Time -->
            <div class="bg-white rounded-2xl p-4 shadow-soft border border-gray-100">
              <span class="text-xs font-bold text-gray-400 uppercase tracking-wide block mb-3 text-center">
                {{ String(endHour).padStart(2, '0') }}:{{ String(endMinute).padStart(2, '0') }}까지
              </span>
              <div class="flex flex-col gap-3">
                <div>
                  <label class="text-xs text-gray-500 mb-1 block">Hour</label>
                  <select 
                    v-model="endHour"
                    class="w-full bg-neutral-light border-none rounded-lg px-3 py-2 text-center text-lg font-bold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  >
                    <option 
                      v-for="hour in availableEndHours" 
                      :key="hour" 
                      :value="hour"
                    >
                      {{ String(hour).padStart(2, '0') }}
                    </option>
                  </select>
                </div>
                <div>
                  <label class="text-xs text-gray-500 mb-1 block">Minute</label>
                  <select 
                    v-model="endMinute"
                    class="w-full bg-neutral-light border-none rounded-lg px-3 py-2 text-center text-lg font-bold text-gray-800 focus:outline-none focus:ring-2 focus:ring-primary/50"
                  >
                    <option 
                      v-for="minute in availableEndMinutes" 
                      :key="minute" 
                      :value="minute"
                    >
                      {{ String(minute).padStart(2, '0') }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

    <!-- Bottom Fixed Area -->
    <div class="fixed bottom-0 left-0 right-0 z-30 bg-white border-t border-gray-100 rounded-t-3xl shadow-[0_-8px_30px_rgba(0,0,0,0.04)] px-6 py-6 pb-8 max-w-app mx-auto">
      <div class="flex items-center justify-between mb-5">
        <div class="flex flex-col">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wide mb-1">최종 선택</span>
          <div class="flex items-center gap-2">
            <span class="material-symbols-rounded text-primary-dark text-lg">event</span>
            <span class="text-base font-bold text-gray-800">{{ formattedDateShort }}</span>
            <span class="w-1 h-1 bg-gray-300 rounded-full"></span>
            <span class="text-base font-medium text-gray-600">{{ formattedTimeRange }}</span>
          </div>
        </div>
      </div>
      
      <p v-if="error" class="text-red-500 text-sm mb-3 text-center">{{ error }}</p>
      
      <button 
        @click="handleSubmit"
        :disabled="isLoading || !meetingName.trim()"
          class="w-full bg-primary hover:bg-primary-dark text-gray-800 font-extrabold text-lg py-4 rounded-2xl shadow-glow transition-all transform active:scale-[0.98] flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span class="material-symbols-rounded">{{ isEditMode ? 'edit' : 'check_circle' }}</span>
          {{ isLoading ? (isEditMode ? "수정 중..." : "생성 중...") : (isEditMode ? "모임 수정하기" : "모임 생성하기") }}
        </button>
      
      <!-- 삭제 버튼 (수정 모드일 때만 표시) -->
      <button
        v-if="isEditMode"
        @click="handleDelete"
        :disabled="isLoading"
        class="w-full mt-3 bg-white hover:bg-red-50 text-red-500 border-2 border-red-200 hover:border-red-300 font-bold text-base py-3 rounded-2xl transition-all transform active:scale-[0.98] flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span class="material-symbols-rounded">delete</span>
        {{ isLoading ? "삭제 중..." : "모임 삭제하기" }}
      </button>
    </div>
    </div>

    <!-- Nickname Modal -->
    <NicknameModal
      v-if="showNicknameModal"
      @close="closeNicknameModal"
    />
  </div>
</template>
