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
});

const emit = defineEmits(["timeClick"]);

const startDate = ref(new Date());
startDate.value.setHours(0, 0, 0, 0);

const hours = Array.from({ length: 16 }, (_, i) => i + 9);

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

const formatHour = (hour) => {
  return `${String(hour).padStart(2, "0")}:00`;
};

const getTimeString = (date, hour) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hourStr = String(hour).padStart(2, "0");
  return `${year}-${month}-${day}T${hourStr}:00`;
};

const isSelected = (date, hour) => {
  const timeString = getTimeString(date, hour);
  return props.selectedTimes.includes(timeString);
};

const isPast = (date, hour) => {
  const now = new Date();
  const checkTime = new Date(date);
  checkTime.setHours(hour, 0, 0, 0);
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

const handleTimeClick = (date, hour) => {
  if (isPast(date, hour)) return;
  const timeString = getTimeString(date, hour);
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
  <div class="bg-white rounded-xl p-4">
    <div class="flex justify-between items-center mb-4">
      <button
        class="w-9 h-9 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
        @click="prevWeek"
      >
        ◀
      </button>
      <h3 class="text-lg font-semibold text-pink-800">시간별 일정</h3>
      <button
        class="w-9 h-9 border-none bg-gray-100 rounded-lg cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-200"
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
                class="sticky left-0 bg-white z-10 min-w-[60px] px-2 py-3 font-semibold text-gray-600 border-b-2 border-gray-300"
              >
                시간
              </th>
              <th
                v-for="date in dates"
                :key="date.toISOString()"
                class="min-w-[80px] px-2 py-3 font-semibold border-b-2 border-gray-300 whitespace-nowrap text-center"
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
            <tr v-for="hour in hours" :key="hour">
              <td
                class="sticky left-0 bg-white z-[5] px-2 py-2 font-medium text-gray-600 border-r border-gray-300 text-center"
              >
                {{ formatHour(hour) }}
              </td>
              <td
                v-for="date in dates"
                :key="`${date.toISOString()}-${hour}`"
                class="p-1 border border-gray-300 cursor-pointer transition-all text-center"
                :class="{
                  'bg-gray-100 cursor-not-allowed opacity-50': isPast(
                    date,
                    hour
                  ),
                  'hover:bg-blue-50': !isPast(date, hour),
                }"
                @click="handleTimeClick(date, hour)"
              >
                <div
                  class="w-full h-8 rounded transition-all"
                  :class="{
                    'bg-primary': isSelected(date, hour),
                    'bg-gray-300': isPast(date, hour),
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
  height: 8px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  @apply bg-gray-100 rounded;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  @apply bg-gray-400 rounded hover:bg-gray-600;
}
</style>
