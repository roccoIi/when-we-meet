<script setup>
import { ref, computed } from "vue";

const props = defineProps({
  selectedTimes: {
    type: Array,
    default: () => [],
  },
  daysToShow: {
    type: Number,
    default: 7,
  },
  minDate: {
    type: String,
    default: null, // "2026-02-15" 형식
  },
  startTime: {
    type: String,
    default: null, // "18:00:00" 형식
  },
});

const emit = defineEmits(["timeClick"]);

// minDate가 있으면 그 날짜부터 시작, 없으면 오늘부터
const startDate = ref(new Date());
if (props.minDate) {
  startDate.value = new Date(props.minDate);
} else {
  startDate.value.setHours(0, 0, 0, 0);
}

// 30분 단위 시간 슬롯 생성
// startTime이 있으면 그 시간부터, 없으면 9:00부터 23:30까지
const timeSlots = computed(() => {
  const slots = [];
  let startHour = 9;
  
  // startTime이 있으면 파싱
  if (props.startTime) {
    const [hourStr] = props.startTime.split(':');
    startHour = parseInt(hourStr, 10);
  }
  
  // startHour부터 23:30까지 생성
  for (let hour = startHour; hour <= 23; hour++) {
    slots.push({ hour, minute: 0 });
    slots.push({ hour, minute: 30 });
  }
  return slots;
});

const dates = computed(() => {
  const result = [];
  for (let i = 0; i < props.daysToShow; i++) {
    const date = new Date(startDate.value);
    date.setDate(date.getDate() + i);
    result.push(date);
  }
  return result;
});

const formatDate = (date) => {
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
  const weekday = weekdays[date.getDay()];
  return `${month}/${day} (${weekday})`;
};

const formatTime = (hour, minute) => {
  return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
};

const getTimeString = (date, hour, minute) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hourStr = String(hour).padStart(2, "0");
  const minuteStr = String(minute).padStart(2, "0");
  return `${year}-${month}-${day}T${hourStr}:${minuteStr}`;
};

const isSelected = (date, hour, minute) => {
  const timeString = getTimeString(date, hour, minute);
  return props.selectedTimes.includes(timeString);
};

const isPast = (date, hour, minute) => {
  const checkTime = new Date(date);
  checkTime.setHours(hour, minute, 0, 0);
  
  // minDate가 설정되어 있으면 그 날짜 이전을 회색 처리
  if (props.minDate) {
    const minDateTime = new Date(props.minDate);
    minDateTime.setHours(0, 0, 0, 0);
    
    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);
    
    // minDate보다 이전 날짜는 모두 회색
    if (checkDate < minDateTime) {
      return true;
    }
    
    // minDate와 같은 날이면 시간도 체크
    if (checkDate.getTime() === minDateTime.getTime()) {
      // minDate의 시작 시간을 0시로 간주하므로, 해당 날짜의 모든 시간은 사용 가능
      return false;
    }
    
    // minDate보다 이후 날짜면 현재 시간과 비교
    const now = new Date();
    return checkTime < now;
  }
  
  // minDate가 없으면 오늘 이전을 회색 처리
  const now = new Date();
  return checkTime < now;
};

const isToday = (date) => {
  const today = new Date();
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  );
};

const handleTimeClick = (date, hour, minute) => {
  if (isPast(date, hour, minute)) return;
  const timeString = getTimeString(date, hour, minute);
  emit("timeClick", timeString);
};

const prevWeek = () => {
  startDate.value.setDate(startDate.value.getDate() - 7);
  startDate.value = new Date(startDate.value);
};

const nextWeek = () => {
  startDate.value.setDate(startDate.value.getDate() + 7);
  startDate.value = new Date(startDate.value);
};

const getDayClass = (date) => {
  const day = date.getDay();
  if (day === 0) return "sunday";
  if (day === 6) return "saturday";
  return "";
};
</script>

<template>
  <div class="bg-white rounded-xl p-3">
    <div class="flex justify-between items-center mb-3">
      <button
        class="w-7 h-7 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
        @click="prevWeek"
      >
        ◀
      </button>
      <h3 class="text-base font-semibold text-pink-800">시간별 일정</h3>
      <button
        class="w-7 h-7 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
        @click="nextWeek"
      >
        ▶
      </button>
    </div>

    <div class="overflow-x-auto">
      <div class="min-w-full">
        <table class="w-full border-collapse text-sm">
          <thead>
            <tr>
              <th
                class="sticky left-0 bg-white z-10 min-w-[50px] px-1.5 py-2 font-semibold text-gray-600 border-b-2 border-gray-300"
              >
                시간
              </th>
              <th
                v-for="date in dates"
                :key="date.toISOString()"
                class="min-w-[65px] px-1.5 py-2 font-semibold border-b-2 border-gray-300 whitespace-nowrap text-center"
                :class="{
                  'text-sunday': getDayClass(date) === 'sunday',
                  'text-saturday': getDayClass(date) === 'saturday',
                  'text-gray-800': getDayClass(date) === '',
                  'bg-blue-50 text-primary': isToday(date),
                }"
              >
                {{ formatDate(date) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="`${slot.hour}-${slot.minute}`">
              <td
                class="sticky left-0 bg-white z-[5] px-1.5 py-0 font-medium text-gray-600 border-r border-gray-300 text-center align-top"
              >
                {{ formatTime(slot.hour, slot.minute) }}
              </td>
              <td
                v-for="date in dates"
                :key="`${date.toISOString()}-${slot.hour}-${slot.minute}`"
                class="p-0.5 border border-gray-300 cursor-pointer transition-all text-center"
                :class="{
                  'bg-gray-100 cursor-not-allowed opacity-50': isPast(
                    date,
                    slot.hour,
                    slot.minute
                  ),
                  'hover:bg-blue-50': !isPast(date, slot.hour, slot.minute),
                }"
                @click="handleTimeClick(date, slot.hour, slot.minute)"
              >
                <div
                  class="w-full h-6 rounded transition-all"
                  :class="{
                    'bg-primary': isSelected(date, slot.hour, slot.minute),
                    'bg-gray-300': isPast(date, slot.hour, slot.minute),
                  }"
                ></div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  @apply bg-gray-400 rounded hover:bg-gray-600;
}
</style>
