<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import Calendar from "../components/Calendar.vue";
import TimeCalendar from "../components/TimeCalendar.vue";
import { scheduleAPI } from "../services/api";

const route = useRoute();
const router = useRouter();

const meetingId = route.params.id;
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const selectedDates = ref([]);
const selectedTimes = ref([]);
const isLoading = ref(false);
const isSaving = ref(false);
const viewMode = ref("date");

onMounted(async () => {
  await loadUserSchedule();
});

const loadUserSchedule = async () => {
  isLoading.value = true;
  try {
    selectedDates.value = [];
  } catch (error) {
    console.error("일정 조회 실패:", error);
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

const toggleViewMode = () => {
  viewMode.value = viewMode.value === "date" ? "time" : "date";
};

const selectedCount = computed(() => {
  return viewMode.value === "date"
    ? selectedDates.value.length
    : selectedTimes.value.length;
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
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-10">
    <div class="w-full">
      <div class="mb-5 flex justify-between items-start gap-4">
        <div class="flex-1">
          <h2 class="text-2xl font-bold text-gray-800 mb-2">
            내 일정 추가하기
          </h2>
          <p class="text-sm text-gray-600">
            모임이
            <strong class="text-primary font-semibold">{{
              viewMode === "date" ? "가능한 날짜" : "가능한 시간"
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
            v-for="date in selectedDates.sort()"
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
            v-for="time in selectedTimes.sort()"
            :key="time"
            class="flex items-center gap-2 px-3 py-2 bg-blue-50 border border-primary rounded-full text-sm text-gray-800"
          >
            <span>{{ formatTime(time) }}</span>
            <button
              class="bg-none border-none text-gray-400 text-base cursor-pointer p-0 w-5 h-5 flex items-center justify-center transition-colors hover:text-red-500"
              @click="handleTimeClick(time)"
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
  </div>
</template>
