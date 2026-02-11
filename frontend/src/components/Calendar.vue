<script setup>
import { ref, computed, watch } from "vue";

const props = defineProps({
  year: {
    type: Number,
    required: true,
  },
  month: {
    type: Number,
    required: true,
  },
  unavailableDates: {
    type: Array,
    default: () => [],
  },
  selectedDates: {
    type: Array,
    default: () => [],
  },
  monthlyAvailability: {
    type: Object,
    default: null,
  },
  confirmedDate: {
    type: String,
    default: null,
  },
  minDate: {
    type: String,
    default: null, // "2026-02-15" 형식
  },
});

const emit = defineEmits(["update:year", "update:month", "dateClick"]);

const currentYear = ref(props.year);
const currentMonth = ref(props.month);

watch(
  () => props.year,
  (newVal) => {
    currentYear.value = newVal;
  }
);

watch(
  () => props.month,
  (newVal) => {
    currentMonth.value = newVal;
  }
);

// 요일 헤더 (일요일부터)
const weekdays = ["일", "월", "화", "수", "목", "금", "토"];

// 해당 월의 날짜 배열 생성
const calendarDates = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1);
  const lastDay = new Date(currentYear.value, currentMonth.value, 0);
  const firstDayOfWeek = firstDay.getDay();
  const daysInMonth = lastDay.getDate();

  const dates = [];

  for (let i = 0; i < firstDayOfWeek; i++) {
    dates.push(null);
  }

  for (let i = 1; i <= daysInMonth; i++) {
    dates.push(i);
  }

  return dates;
});

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12;
    currentYear.value--;
  } else {
    currentMonth.value--;
  }
  emit("update:year", currentYear.value);
  emit("update:month", currentMonth.value);
};

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else {
    currentMonth.value++;
  }
  emit("update:year", currentYear.value);
  emit("update:month", currentMonth.value);
};

const isUnavailable = (date) => {
  if (!date) return false;
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  return props.unavailableDates.includes(dateString);
};

const isSelected = (date) => {
  if (!date) return false;
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  return props.selectedDates.includes(dateString);
};

const isToday = (date) => {
  if (!date) return false;
  const today = new Date();
  return (
    date === today.getDate() &&
    currentMonth.value === today.getMonth() + 1 &&
    currentYear.value === today.getFullYear()
  );
};

const isPast = (date) => {
  if (!date) return false;
  
  // minDate가 설정되어 있으면 그 날짜 이전을 회색 처리
  if (props.minDate) {
    const minDateTime = new Date(props.minDate);
    minDateTime.setHours(0, 0, 0, 0);
    const checkDate = new Date(currentYear.value, currentMonth.value - 1, date);
    checkDate.setHours(0, 0, 0, 0);
    return checkDate < minDateTime;
  }
  
  // minDate가 없으면 오늘 이전을 회색 처리
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const checkDate = new Date(currentYear.value, currentMonth.value - 1, date);
  return checkDate < today;
};

const handleDateClick = (date) => {
  if (!date || isPast(date)) return;
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  emit("dateClick", dateString);
};

const getDayClass = (index, date) => {
  if (!date) return "";
  const dayOfWeek = index % 7;
  if (dayOfWeek === 0) return "sunday";
  if (dayOfWeek === 6) return "saturday";
  return "";
};

/**
 * 날짜가 확정된 날짜인지 확인
 */
const isConfirmedDate = (date) => {
  if (!date || !props.confirmedDate) return false;
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  return dateString === props.confirmedDate;
};

/**
 * 날짜의 가용성 정보 가져오기
 */
const getAvailability = (date) => {
  if (!date || !props.monthlyAvailability) return null;
  
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  
  const availability = props.monthlyAvailability.dateAvailability.get(dateString);
  
  // 데이터에 없으면 모두 가능
  if (!availability) {
    return {
      availableCount: props.monthlyAvailability.totalMembers,
      percentage: 100
    };
  }
  
  const percentage = (availability.availableCount / props.monthlyAvailability.totalMembers) * 100;
  return {
    availableCount: availability.availableCount,
    percentage: percentage
  };
};

/**
 * 가용성에 따른 배경 색상 클래스 반환
 */
const getAvailabilityClass = (date) => {
  const availability = getAvailability(date);
  if (!availability) return '';
  
  if (availability.percentage >= 80) {
    return 'bg-primary';
  } else if (availability.percentage >= 50) {
    return 'bg-tertiary';
  } else {
    return 'bg-secondary';
  }
};

/**
 * 날짜의 불가능한 멤버 리스트 가져오기
 */
const getUnavailableMembers = (date) => {
  if (!date || !props.monthlyAvailability) return [];
  
  const dateString = `${currentYear.value}-${String(
    currentMonth.value
  ).padStart(2, "0")}-${String(date).padStart(2, "0")}`;
  
  const availability = props.monthlyAvailability.dateAvailability.get(dateString);
  
  // 데이터에 없으면 = 모두 가능 (불가능한 멤버 없음)
  if (!availability) {
    return [];
  }
  
  return availability.unAvailableMembers || [];
};
</script>

<template>
  <div>
    <!-- 달력 헤더 -->
    <div class="flex items-center justify-between mb-6">
      <button
        @click="prevMonth"
        class="p-1 hover:bg-neutral-light rounded-full transition-colors"
      >
        <span class="material-symbols-rounded text-gray-400">chevron_left</span>
      </button>
      <h3 class="text-lg font-bold text-gray-800">
        {{ currentYear }}년 {{ currentMonth }}월
      </h3>
      <button
        @click="nextMonth"
        class="p-1 hover:bg-neutral-light rounded-full transition-colors"
      >
        <span class="material-symbols-rounded text-gray-400">chevron_right</span>
      </button>
    </div>

    <div class="w-full">
      <!-- 요일 헤더 -->
      <div class="grid grid-cols-7 gap-y-4 gap-x-2 mb-2 text-center">
        <div
          v-for="(day, index) in ['일', '월', '화', '수', '목', '금', '토']"
          :key="day"
          class="text-xs font-bold text-gray-400 uppercase tracking-wide"
        >
          {{ day }}
        </div>
      </div>

      <!-- 날짜 그리드 -->
      <div class="grid grid-cols-7 gap-y-4 gap-x-2 text-center">
        <div
          v-for="(date, index) in calendarDates"
          :key="index"
          class="relative group cursor-pointer"
          @click="handleDateClick(date)"
        >
          <div
            v-if="date"
            :class="[
              'w-8 h-8 mx-auto flex items-center justify-center rounded-full text-sm font-medium transition-colors',
              {
                'cursor-default': !date,
                'bg-gray-200 text-gray-400 cursor-not-allowed': date && isPast(date),
                'bg-primary text-gray-800 font-bold border border-white shadow-sm': date && isSelected(date) && !isPast(date),
                'bg-primary text-gray-800 font-extrabold ring-2 ring-primary ring-offset-2': date && isConfirmedDate(date) && !isPast(date),
                'text-gray-600 hover:bg-neutral-light': date && !isPast(date) && !isSelected(date) && !isConfirmedDate(date) && !monthlyAvailability,
              },
              date && !isPast(date) && !isSelected(date) && !isConfirmedDate(date) && monthlyAvailability ? getAvailabilityClass(date) + ' text-gray-700 font-bold border border-white shadow-sm' : ''
            ]"
          >
            <!-- 확정된 날짜 표시 -->
            <div 
              v-if="date && isConfirmedDate(date)"
              class="absolute -top-1 -right-1 w-2.5 h-2.5 bg-[#FF8B94] rounded-full animate-pulse border-2 border-white"
            ></div>
            
            {{ date }}
          </div>
          
          <!-- 불가능한 멤버 툴팁 -->
          <div 
            v-if="date && monthlyAvailability && getUnavailableMembers(date).length > 0"
            class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-800 text-white text-xs rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-50 shadow-lg"
          >
            <div class="font-semibold mb-1">불가능한 멤버</div>
            <div class="flex flex-col gap-0.5">
              <span v-for="member in getUnavailableMembers(date)" :key="member">
                • {{ member }}
              </span>
            </div>
            <!-- 화살표 -->
            <div class="absolute top-full left-1/2 transform -translate-x-1/2 -mt-0.5">
              <div class="border-4 border-transparent border-t-gray-800"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
