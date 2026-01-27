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
          class="aspect-square flex justify-center items-start p-1 rounded-lg cursor-pointer transition-all relative"
          :class="{
            'cursor-default': !date,
            'opacity-30 cursor-not-allowed': date && isPast(date),
            'bg-gray-300': date && isUnavailable(date),
            'border-2 border-primary': date && isToday(date),
            'hover:bg-gray-100': date && !isPast(date),
          }"
          @click="handleDateClick(date)"
        >
          <span
            v-if="date"
            class="text-sm font-medium"
            :class="{
              'text-sunday': getDayClass(index, date) === 'sunday',
              'text-saturday': getDayClass(index, date) === 'saturday',
              'text-gray-800': getDayClass(index, date) === '',
              'text-gray-400': isPast(date),
            }"
          >
            {{ date }}
          </span>
          <div
            v-if="date && isUnavailable(date)"
            class="absolute inset-0 bg-gray-400/20 rounded-lg"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>
