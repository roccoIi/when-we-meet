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
  
  if (availability.percentage >= 70) {
    return 'bg-green-100 text-green-800';
  } else if (availability.percentage >= 40) {
    return 'bg-yellow-100 text-yellow-800';
  } else {
    return 'bg-red-100 text-red-800';
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
  <div class="bg-white rounded-xl p-4">
    <div class="flex justify-between items-center mb-4">
      <button
        class="w-9 h-9 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
        @click="prevMonth"
      >
        ◀
      </button>
      <h3 class="text-lg font-semibold text-gray-800">
        {{ currentYear }}년 {{ currentMonth }}월
      </h3>
      <button
        class="w-9 h-9 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
        @click="nextMonth"
      >
        ▶
      </button>
    </div>

    <div class="w-full">
      <div class="grid grid-cols-7 gap-1 mb-2">
        <div
          v-for="(day, index) in weekdays"
          :key="day"
          class="text-center text-sm font-semibold py-2"
          :class="{
            'text-sunday': index === 0,
            'text-saturday': index === 6,
            'text-gray-600': index !== 0 && index !== 6,
          }"
        >
          {{ day }}
        </div>
      </div>

      <div class="grid grid-cols-7 gap-1">
        <div
          v-for="(date, index) in calendarDates"
          :key="index"
          class="aspect-square flex flex-col justify-start items-center p-1 rounded-lg cursor-pointer transition-all relative group"
          :class="[
            {
              'cursor-default': !date,
              'opacity-30 cursor-not-allowed': date && isPast(date),
              'bg-gray-300': date && isUnavailable(date),
              'bg-primary text-white font-bold shadow-md': date && isSelected(date),
              'border-2 border-primary': date && isToday(date) && !isSelected(date) && !isConfirmedDate(date),
              'border-4 border-yellow-400 shadow-lg': date && isConfirmedDate(date),
              'hover:bg-gray-100': date && !isPast(date) && !isSelected(date),
            },
            date && !isSelected(date) && !isUnavailable(date) ? getAvailabilityClass(date) : ''
          ]"
          @click="handleDateClick(date)"
        >
          <!-- 확정 날짜 뱃지 -->
          <div v-if="date && isConfirmedDate(date)" class="absolute -top-1 -right-1 bg-yellow-500 text-white text-xs px-1.5 py-0.5 rounded-full font-bold shadow z-10">
            확정
          </div>
          
          <!-- 날짜 숫자 -->
          <span
            v-if="date"
            class="text-sm font-medium"
            :class="{
              'text-white': isSelected(date),
              'text-sunday': !isSelected(date) && getDayClass(index, date) === 'sunday',
              'text-saturday': !isSelected(date) && getDayClass(index, date) === 'saturday',
              'text-gray-400': !isSelected(date) && isPast(date),
            }"
          >
            {{ date }}
          </span>
          
          <!-- 가용 인원 표시 -->
          <span
            v-if="date && monthlyAvailability && !isSelected(date) && !isPast(date)"
            class="text-xs font-semibold mt-0.5"
          >
            {{ getAvailability(date)?.availableCount }}/{{ monthlyAvailability.totalMembers }}
          </span>
          
          <!-- 불가능한 멤버 툴팁 -->
          <div 
            v-if="date && monthlyAvailability && getUnavailableMembers(date).length > 0"
            class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 px-3 py-2 bg-gray-800 text-white text-xs rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none whitespace-nowrap z-20 shadow-lg"
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
          
          <div
            v-if="date && isUnavailable(date)"
            class="absolute inset-0 bg-gray-400/20 rounded-lg"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>
