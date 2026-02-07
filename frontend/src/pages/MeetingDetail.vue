<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import Calendar from "../components/Calendar.vue";
import { meetingAPI } from "../services";

const route = useRoute();
const router = useRouter();

const meetingId = route.params.id;
const meeting = ref(null);
const currentYear = ref(new Date().getFullYear());
const currentMonth = ref(new Date().getMonth() + 1);
const unavailableDates = ref([]);
const recommendedDates = ref([]);
const isLoading = ref(false);

onMounted(async () => {
  await loadMeetingDetail();
  await loadCalendarData();
  await loadRecommendedDates();
});

const loadMeetingDetail = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // meeting.value = await meetingAPI.getMeetingDetail(meetingId)

    // 임시 데이터
    meeting.value = {
      id: meetingId,
      name: "친구들 모임",
      participantCount: 5,
    };
  } catch (error) {
    console.error("모임 정보 조회 실패:", error);
  }
};

const loadCalendarData = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const data = await meetingAPI.getMeetingCalendar(meetingId, currentYear.value, currentMonth.value)
    // unavailableDates.value = data.unavailableDates

    // 임시 데이터
    unavailableDates.value = ["2026-01-26", "2026-01-27", "2026-01-30"];
  } catch (error) {
    console.error("달력 데이터 조회 실패:", error);
  }
};

const loadRecommendedDates = async () => {
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const data = await meetingAPI.getRecommendedDates(meetingId)
    // recommendedDates.value = data.dates

    // 임시 데이터
    recommendedDates.value = [
      { date: "2026-02-01", availableCount: 5, rank: 1 },
      { date: "2026-02-05", availableCount: 4, rank: 2 },
      { date: "2026-02-08", availableCount: 4, rank: 3 },
      { date: "2026-02-12", availableCount: 3, rank: 4 },
      { date: "2026-02-15", availableCount: 3, rank: 5 },
    ];
  } catch (error) {
    console.error("추천 날짜 조회 실패:", error);
  }
};

const handleMonthChange = async () => {
  await loadCalendarData();
};

const handleShareClick = async () => {
  try {
    // 실제로는 백엔드에서 공유 링크 생성
    const shareUrl = `${window.location.origin}/meeting/${meetingId}`;

    if (navigator.share) {
      await navigator.share({
        title: meeting.value.name,
        text: `"${meeting.value.name}" 모임에 참여해주세요!`,
        url: shareUrl,
      });
    } else {
      // 모바일 공유가 지원되지 않으면 클립보드에 복사
      await navigator.clipboard.writeText(shareUrl);
      alert("링크가 클립보드에 복사되었습니다!");
    }
  } catch (error) {
    console.error("공유 실패:", error);
  }
};

const handleScheduleInput = () => {
  router.push(`/meeting/${meetingId}/schedule`);
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekdays = ["일", "월", "화", "수", "목", "금", "토"];
  const weekday = weekdays[date.getDay()];
  return `${month}월 ${day}일 (${weekday})`;
};

const getRankEmoji = (rank) => {
  const emojis = ["🥇", "🥈", "🥉", "4️⃣", "5️⃣"];
  return emojis[rank - 1] || "";
};
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5 pb-10">
    <div v-if="meeting" class="w-full">
      <!-- 모임 헤더 -->
      <div class="flex justify-between items-center mb-5">
        <h2 class="text-2xl font-bold text-gray-800">{{ meeting.name }}</h2>
        <button
          class="flex items-center gap-1.5 px-4 py-2.5 bg-white border border-gray-300 rounded-lg text-sm font-semibold text-primary cursor-pointer transition-all hover:bg-blue-50 hover:border-primary"
          @click="handleShareClick"
        >
          <span>📤</span>
          <span>공유</span>
        </button>
      </div>

      <!-- 달력 -->
      <div class="mb-5">
        <Calendar
          :year="currentYear"
          :month="currentMonth"
          :unavailableDates="unavailableDates"
          @update:year="
            (val) => {
              currentYear = val;
              handleMonthChange();
            }
          "
          @update:month="
            (val) => {
              currentMonth = val;
              handleMonthChange();
            }
          "
        />

        <div class="flex justify-center mt-3">
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <div class="w-5 h-5 bg-gray-300 rounded"></div>
            <span>불가능한 날짜</span>
          </div>
        </div>
      </div>

      <!-- 일정 입력 버튼 -->
      <button
        class="w-full px-4 py-4 bg-primary text-white border-none rounded-xl text-base font-semibold cursor-pointer transition-colors mb-6 hover:bg-primary-dark active:scale-[0.98]"
        @click="handleScheduleInput"
      >
        내 일정 추가하기
      </button>

      <!-- 추천 날짜 -->
      <div class="bg-white rounded-2xl p-5 shadow-sm">
        <h3 class="text-lg font-semibold text-gray-800 mb-4">추천 모임 날짜</h3>

        <div
          v-if="recommendedDates.length === 0"
          class="text-center py-10 text-gray-400 text-sm"
        >
          아직 입력된 일정이 없습니다
        </div>

        <div v-else class="flex flex-col gap-3">
          <div
            v-for="item in recommendedDates"
            :key="item.date"
            class="flex items-center gap-4 p-4 rounded-xl transition-all hover:translate-x-1"
            :class="
              item.rank === 1
                ? 'bg-gradient-to-r from-yellow-100 to-yellow-200'
                : 'bg-gray-50'
            "
          >
            <div class="text-3xl">
              {{ getRankEmoji(item.rank) }}
            </div>
            <div class="flex-1">
              <p class="text-base font-semibold text-gray-800 mb-1">
                {{ formatDate(item.date) }}
              </p>
              <p class="text-sm text-gray-600">
                {{ item.availableCount }}명 가능
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-10 text-gray-600">로딩 중...</div>
  </div>
</template>
