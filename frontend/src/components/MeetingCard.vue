<script setup>
import { computed } from "vue";

const props = defineProps({
  meeting: {
    type: Object,
    required: true,
  },
});

// D-day 계산
const dday = computed(() => {
  if (!props.meeting.meetingDate) {
    return "미정";
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const meetingDate = new Date(props.meeting.meetingDate);
  meetingDate.setHours(0, 0, 0, 0);

  const diffTime = meetingDate - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return "D-Day";
  if (diffDays > 0) return `D-${diffDays}`;
  return `D+${Math.abs(diffDays)}`;
});

// D-day 색상
const ddayColor = computed(() => {
  if (props.meeting.meetingDate) {
    const today = new Date();
    const meetingDate = new Date(props.meeting.meetingDate);
    const diffTime = meetingDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays <= 0) return "#e74c3c"; // 지난 날짜 - 빨강
    if (diffDays <= 3) return "#e67e22"; // 3일 이내 - 주황
    if (diffDays <= 7) return "#f39c12"; // 7일 이내 - 노랑
    return "#27ae60"; // 그 외 - 초록
  }
  return "#95a5a6"; // 미정 - 회색
});

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};
</script>

<template>
  <div
    class="bg-white rounded-2xl p-5 mb-3 shadow-sm cursor-pointer transition-all hover:-translate-y-0.5 hover:shadow-md active:translate-y-0"
  >
    <div class="flex justify-between items-center mb-3">
      <h3
        class="text-lg font-semibold text-gray-800 flex-1 overflow-hidden text-ellipsis whitespace-nowrap mr-3"
      >
        {{ meeting.name }}
      </h3>
      <span
        class="px-3 py-1.5 rounded-xl text-white text-sm font-bold whitespace-nowrap"
        :style="{ backgroundColor: ddayColor }"
      >
        {{ dday }}
      </span>
    </div>

    <div class="flex gap-4">
      <div class="flex items-center gap-1.5">
        <span class="text-base">👥</span>
        <span class="text-sm text-gray-600"
          >{{ meeting.participantCount }}명</span
        >
      </div>

      <div v-if="meeting.meetingDate" class="flex items-center gap-1.5">
        <span class="text-base">📅</span>
        <span class="text-sm text-gray-600">{{
          formatDate(meeting.meetingDate)
        }}</span>
      </div>
    </div>
  </div>
</template>
