<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import NicknameModal from "../components/NicknameModal.vue";
import { meetingAPI } from "../services";

const router = useRouter();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

const meetingName = ref("");
const isLoading = ref(false);
const error = ref("");

// 날짜 및 시간 선택
const selectedDate = ref(new Date());
const selectedHour = ref(new Date().getHours());
const selectedMinute = ref(Math.floor(new Date().getMinutes() / 30) * 30); // 30분 단위로 반올림

// 시간 옵션 생성
const allHours = Array.from({ length: 24 }, (_, i) => i);
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

// 선택된 날짜/시간 포맷팅
const formattedDateTime = computed(() => {
  const year = selectedDate.value.getFullYear();
  const month = String(selectedDate.value.getMonth() + 1).padStart(2, '0');
  const day = String(selectedDate.value.getDate()).padStart(2, '0');
  const hour = String(selectedHour.value).padStart(2, '0');
  const minute = String(selectedMinute.value).padStart(2, '0');
  
  return `${year}년 ${month}월 ${day}일 ${hour}:${minute}`;
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

  // 초기화 완료 후 닉네임 체크
  // if (!userStore.isLoggedIn) {
  //   console.log('⚠️ [CreateMeeting] 닉네임 없음 - 모달 표시');
  //   showNicknameModal.value = true;
  // } else {
  //   console.log('✅ [CreateMeeting] 닉네임 존재:', userStore.nickname);
  // }
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
    // 날짜/시간 포맷팅
    const startDate = `${selectedDate.value.getFullYear()}-${String(selectedDate.value.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.value.getDate()).padStart(2, '0')}`;
    const startTime = `${String(selectedHour.value).padStart(2, '0')}:${String(selectedMinute.value).padStart(2, '0')}:00`;
    
    console.log('모임 생성 데이터:', {
      meetingName: meetingName.value,
      startDate,
      startTime
    });

    // API 호출
    const response = await meetingAPI.createMeeting({
      meetingName: meetingName.value,
      startDate: startDate,
      startTime: startTime
    })
    
    console.log('✅ [CreateMeeting] 전체 응답:', response);
    console.log('✅ [CreateMeeting] response.data:', response.data);
    
    // CommonResponse 형식에서 데이터 추출
    // response.data = { code, message, data: { shareCode } }
    const responseData = response.data || response;
    const actualData = responseData.data || responseData;
    const shareCode = actualData.shareCode;
    
    console.log('✅ [CreateMeeting] 추출된 데이터:', actualData);
    console.log('✅ [CreateMeeting] shareCode:', shareCode);
    
    if (!shareCode) {
      throw new Error('shareCode를 받지 못했습니다');
    }
    
    alert(`모임이 생성되었습니다! 🎉`);
    router.push(`/meeting/${shareCode}`)
  } catch (err) {
    error.value = "모임 생성에 실패했습니다";
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.back();
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
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-20">
    <div class="w-full max-w-2xl mx-auto">
      <h2 class="text-xl font-bold text-gray-800 mb-3">새 모임 만들기</h2>

      <!-- 모임 이름 입력 -->
      <div class="bg-white rounded-2xl p-4 mb-5 shadow-sm">
        <label
          for="meeting-name"
          class="block text-sm font-semibold text-gray-800 mb-2"
        >
          모임 이름
        </label>
        <input
          id="meeting-name"
          v-model="meetingName"
          type="text"
          class="w-full px-3 py-3 border border-gray-300 rounded-lg text-base transition-colors focus:outline-none focus:border-primary"
          placeholder="예: 친구들 모임, 스터디 그룹"
          maxlength="30"
          autofocus
        />
        <p class="text-right text-xs text-gray-400 mt-1">
          {{ meetingName.length }}/30
        </p>
      </div>

      <!-- 날짜 및 시간 선택 -->
      <div class="bg-white rounded-2xl p-5 mb-5 shadow-sm">
        <h3 class="text-base font-semibold text-gray-800 mb-4">
          모임 시작 날짜 및 시간
        </h3>

        <!-- 선택된 날짜/시간 표시 -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 mb-5">
          <p class="text-sm text-gray-600 mb-1">선택한 시작 시점</p>
          <p class="text-lg font-bold text-primary">{{ formattedDateTime }} 부터</p>
        </div>

        <!-- 달력 -->
        <div class="mb-5">
          <!-- 달력 헤더 -->
          <div class="flex justify-between items-center mb-4">
            <button
              @click="prevMonth"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
            >
              ←
            </button>
            <h4 class="text-base font-semibold text-gray-800">
              {{ currentYear }}년 {{ currentMonth + 1 }}월
            </h4>
            <button
              @click="nextMonth"
              class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100 transition-colors"
            >
              →
            </button>
          </div>

          <!-- 요일 헤더 -->
          <div class="grid grid-cols-7 gap-1 mb-2">
            <div
              v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
              :key="day"
              class="text-center text-xs font-medium text-gray-500 py-2"
            >
              {{ day }}
            </div>
          </div>

          <!-- 날짜 그리드 -->
          <div class="grid grid-cols-7 gap-1">
            <button
              v-for="(day, index) in calendarDays"
              :key="index"
              @click="day && !isPastDate(day) && handleDateSelect(day)"
              :disabled="!day || isPastDate(day)"
              :class="[
                'aspect-square flex items-center justify-center rounded-lg text-sm transition-all',
                !day ? 'invisible' : '',
                isPastDate(day)
                  ? 'text-gray-300 cursor-not-allowed'
                  : isSameDay(day, selectedDate)
                  ? 'bg-primary text-white font-bold shadow-lg'
                  : 'hover:bg-gray-100 text-gray-700 cursor-pointer'
              ]"
            >
              {{ day?.getDate() }}
            </button>
          </div>
        </div>

        <!-- 시간 선택 -->
        <div class="grid grid-cols-2 gap-4">
          <!-- 시 선택 -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">
              시
            </label>
            <div class="h-40 overflow-y-auto border border-gray-300 rounded-lg">
              <button
                v-for="hour in availableHours"
                :key="hour"
                @click="selectedHour = hour"
                :class="[
                  'w-full py-3 text-center transition-colors',
                  selectedHour === hour
                    ? 'bg-primary text-white font-bold'
                    : 'hover:bg-gray-100'
                ]"
              >
                {{ String(hour).padStart(2, '0') }}시
              </button>
              <!-- 선택 불가능한 시간 표시 (과거) -->
              <div
                v-for="hour in allHours.filter(h => !availableHours.includes(h))"
                :key="`disabled-${hour}`"
                class="w-full py-3 text-center text-gray-300 cursor-not-allowed"
              >
                {{ String(hour).padStart(2, '0') }}시
              </div>
            </div>
          </div>

          <!-- 분 선택 -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-2">
              분
            </label>
            <div class="h-40 overflow-y-auto border border-gray-300 rounded-lg">
              <button
                v-for="minute in availableMinutes"
                :key="minute"
                @click="selectedMinute = minute"
                :class="[
                  'w-full py-3 text-center transition-colors',
                  selectedMinute === minute
                    ? 'bg-primary text-white font-bold'
                    : 'hover:bg-gray-100'
                ]"
              >
                {{ String(minute).padStart(2, '0') }}분
              </button>
              <!-- 선택 불가능한 분 표시 (과거) -->
              <div
                v-for="minute in allMinutes.filter(m => !availableMinutes.includes(m))"
                :key="`disabled-${minute}`"
                class="w-full py-3 text-center text-gray-300 cursor-not-allowed"
              >
                {{ String(minute).padStart(2, '0') }}분
              </div>
            </div>
          </div>
        </div>

        <p class="text-xs text-gray-500 mt-4 text-center">
          💡 이 시점부터 가능한 날짜와 시간을 참여자들이 선택할 수 있습니다
        </p>
      </div>

      <p v-if="error" class="text-red-500 text-sm mb-4 text-center">{{ error }}</p>

      <!-- 버튼 -->
      <div class="flex gap-3">
        <button
          class="flex-1 px-4 py-3.5 border-none rounded-xl text-base font-semibold cursor-pointer transition-all bg-gray-100 text-gray-600 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
          @click="handleCancel"
          :disabled="isLoading"
        >
          취소
        </button>
        <button
          class="flex-1 px-4 py-3.5 border-none rounded-xl text-base font-semibold cursor-pointer transition-all bg-gradient-to-r from-primary to-purple-600 text-white shadow-lg hover:shadow-xl hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100"
          @click="handleSubmit"
          :disabled="isLoading || !meetingName.trim()"
        >
          {{ isLoading ? "생성 중..." : "모임 만들기" }}
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
