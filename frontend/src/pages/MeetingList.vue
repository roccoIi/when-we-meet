<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import MeetingCard from "../components/MeetingCard.vue";
import { meetingAPI } from "../services/api";

const router = useRouter();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

const isLoading = ref(false);
const showSortMenu = ref(false);

// 정렬 옵션
const sortOptions = [
  { value: "dday", label: "D-day 빠른순" },
  { value: "name", label: "이름순" },
  { value: "participants", label: "참여자순" },
];

const currentSortLabel = computed(() => {
  return (
    sortOptions.find((opt) => opt.value === meetingsStore.sortBy)?.label ||
    "D-day 빠른순"
  );
});

onMounted(async () => {
  // 임시 로그인 상태 (실제로는 토큰 체크 등으로 확인)
  if (!userStore.isLoggedIn) {
    userStore.login({ id: 1, nickname: "" }); // 닉네임 없으면 모달 자동 표시
  }

  await loadMeetings();
});

const loadMeetings = async () => {
  isLoading.value = true;
  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const data = await meetingAPI.getMeetings()
    // meetingsStore.setMeetings(data.meetings)

    // 임시 데이터
    meetingsStore.setMeetings([
      {
        id: 1,
        name: "친구들 모임",
        participantCount: 5,
        meetingDate: "2026-02-01",
      },
      {
        id: 2,
        name: "스터디 그룹",
        participantCount: 8,
        meetingDate: null,
      },
      {
        id: 3,
        name: "회사 동료 회식",
        participantCount: 12,
        meetingDate: "2026-01-28",
      },
    ]);
  } catch (error) {
    console.error("모임 목록 조회 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

const handleMeetingClick = (meeting) => {
  meetingsStore.setCurrentMeeting(meeting);
  router.push(`/meeting/${meeting.id}`);
};

const handleCreateMeeting = () => {
  router.push("/create");
};

const handleSortChange = (sortValue) => {
  meetingsStore.setSortBy(sortValue);
  showSortMenu.value = false;
};

const sortedMeetings = computed(() => meetingsStore.getSortedMeetings());
</script>

<template>
  <div class="min-h-screen bg-gray-100 pb-20">
    <div class="bg-gray-100 px-6 pt-6 border-b border-gray-100">
      <h2 class="text-xl font-semibold text-gray-800">
        <span class="text-primary">{{ userStore.nickname }}</span> 님의 모임 목록입니다
      </h2>
    </div>

    <div class="px-5 py-4">
      <div v-if="isLoading" class="text-center py-10 text-gray-600">
        로딩 중...
      </div>

      <div
        v-else-if="sortedMeetings.length === 0"
        class="text-center py-15 text-gray-400"
      >
        <p>아직 참여한 모임이 없습니다</p>
        <p class="mt-2 text-sm">새로운 모임을 만들어보세요!</p>
      </div>

      <div v-else class="flex flex-col">
        <MeetingCard
          v-for="meeting in sortedMeetings"
          :key="meeting.id"
          :meeting="meeting"
          @click="handleMeetingClick(meeting)"
        />
      </div>
    </div>

    <div
      class="fixed bottom-0 left-0 right-0 max-w-app mx-auto px-5 py-4 bg-white border-t border-gray-300 flex justify-between items-center gap-3"
    >
      <div class="relative">
        <button
          class="px-4 py-3 bg-white border border-gray-300 rounded-lg flex items-center gap-2 cursor-pointer text-sm text-gray-600 transition-all hover:border-primary hover:text-primary"
          @click="showSortMenu = !showSortMenu"
        >
          <span>{{ currentSortLabel }}</span>
          <span class="text-xs">▼</span>
        </button>

        <div
          v-if="showSortMenu"
          class="absolute bottom-full left-0 mb-2 bg-white border border-gray-300 rounded-lg shadow-lg overflow-hidden min-w-[140px]"
        >
          <button
            v-for="option in sortOptions"
            :key="option.value"
            class="block w-full px-4 py-3 border-none bg-white text-left cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-100"
            :class="{
              'text-primary font-semibold':
                meetingsStore.sortBy === option.value,
            }"
            @click="handleSortChange(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <button
        class="px-6 py-3 bg-primary text-white border-none rounded-lg text-base font-semibold cursor-pointer flex items-center gap-1.5 transition-colors hover:bg-primary-dark active:scale-95"
        @click="handleCreateMeeting"
      >
        <span class="text-xl font-bold">+</span>
        <span>새 모임</span>
      </button>
    </div>
  </div>
</template>
