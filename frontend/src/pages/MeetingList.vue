<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import MeetingCard from "../components/MeetingCard.vue";
import { meetingAPI } from "../services";

const router = useRouter();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

const isLoading = ref(false);
const isLoadingMore = ref(false); // 추가 로딩 상태
const showSortTypeMenu = ref(false);

// 정렬 기준 (type)
const sortType = ref("JOIN_DATE"); // 기본값: 참여날짜
const sortOrder = ref("DESC"); // 기본값: 내림차순

// 페이지네이션
const currentPage = ref(1);
const pageLimit = ref(10);
const hasMore = ref(true); // 더 불러올 데이터가 있는지

// 무한 스크롤을 위한 감지 요소
const loadMoreTrigger = ref(null);

// 정렬 기준 옵션
const sortTypeOptions = [
  { value: "NAME", label: "이름" },
  { value: "JOIN_DATE", label: "참여날짜" },
  { value: "MEETING_DATE", label: "D-day" },
];

const currentSortTypeLabel = computed(() => {
  return (
    sortTypeOptions.find((opt) => opt.value === sortType.value)?.label ||
    "참여날짜"
  );
});

// 정렬 순서 토글
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === "ASC" ? "DESC" : "ASC";
  loadMeetings(true); // 정렬 변경 시 첫 페이지부터 다시 로드
};

onMounted(async () => {
  // App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    console.log('⏳ [MeetingList] 초기화 대기 중...')
    // 초기화 완료를 기다림 (최대 5초)
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
      console.log('✅ [MeetingList] 초기화 완료, 모임 로드 시작')
    } else {
      console.log('⚠️ [MeetingList] 초기화 타임아웃')
    }
  }

  // 디버깅: 사용자 정보 확인
  console.log('👤 [MeetingList] 현재 사용자 정보:');
  console.log('  - isLoggedIn:', userStore.isLoggedIn);
  console.log('  - nickname:', userStore.nickname);
  console.log('  - profileImgUrl:', userStore.profileImgUrl);

  await loadMeetings();
  setupInfiniteScroll();
});

// 무한 스크롤 설정
const setupInfiniteScroll = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      const target = entries[0];
      if (
        target.isIntersecting &&
        !isLoading.value &&
        !isLoadingMore.value &&
        hasMore.value
      ) {
        loadMoreMeetings();
      }
    },
    {
      root: null,
      rootMargin: "100px", // 하단 100px 전에 미리 로드
      threshold: 0.1,
    },
  );

  if (loadMoreTrigger.value) {
    observer.observe(loadMoreTrigger.value);
  }
};

const loadMeetings = async (reset = true) => {
  if (reset) {
    isLoading.value = true;
    currentPage.value = 1;
    hasMore.value = true;
  }

  try {
    // API 호출
    const response = await meetingAPI.getMeetings({
      page: currentPage.value,
      limit: pageLimit.value,
      type: sortType.value,
      sort: sortOrder.value,
    });

    console.log("API 응답:", response);

    // 백엔드 응답 구조에 맞게 데이터 추출
    // response.data = 모임 배열
    // response.pagination = { currentPage, totalPages, totalItems, hasMore }
    const meetings = response.data || [];
    const pagination = response.pagination || {};

    if (reset) {
      // 첫 페이지 로드 시 기존 데이터 교체
      meetingsStore.setMeetings(meetings);
    } else {
      // 추가 로드 시 기존 데이터에 추가
      meetingsStore.meetings.push(...meetings);
    }

    // pagination.hasMore를 사용하여 더 불러올 데이터 확인
    hasMore.value = pagination.hasMore ?? false;

    console.log("로드된 모임 수:", meetings.length);
    console.log("더 불러올 데이터:", hasMore.value);
  } catch (error) {
    console.error("모임 목록 조회 실패:", error);
    // 에러 시 빈 배열로 설정
    if (reset) {
      meetingsStore.setMeetings([]);
    }
    hasMore.value = false;
  } finally {
    if (reset) {
      isLoading.value = false;
    } else {
      isLoadingMore.value = false;
    }
  }
};

// 추가 데이터 로드
const loadMoreMeetings = async () => {
  if (isLoadingMore.value || !hasMore.value) return;

  isLoadingMore.value = true;
  currentPage.value++;
  await loadMeetings(false); // reset = false
};

const handleMeetingClick = (meeting) => {
  meetingsStore.setCurrentMeeting(meeting);
  router.push(`/meeting/${meeting.shareCode}`);
};

const handleCreateMeeting = () => {
  router.push("/create");
};

const handleSortTypeChange = (typeValue) => {
  sortType.value = typeValue;
  showSortTypeMenu.value = false;
  loadMeetings(true); // 정렬 변경 시 첫 페이지부터 다시 로드
};

// Store의 정렬 기능 대신 API 정렬 사용
const sortedMeetings = computed(() => meetingsStore.meetings);
</script>

<template>
  <div class="min-h-screen bg-gray-100 pb-20">
    <!-- 헤더: 정렬 옵션 + 제목 -->
    <div class="bg-gray-100 px-6 pt-6 pb-4 border-b border-gray-200">
      <div class="flex justify-between items-center gap-4">
        <!-- 정렬 옵션 (좌측) -->
        <div class="flex items-center gap-2">
          <!-- 정렬 기준 선택 -->
          <div class="relative">
            <button
              class="px-3 py-2 bg-white border border-gray-300 rounded-lg flex items-center gap-2 cursor-pointer text-sm text-gray-700 transition-all hover:border-primary hover:text-primary"
              @click="showSortTypeMenu = !showSortTypeMenu"
            >
              <span>{{ currentSortTypeLabel }}</span>
              <span class="text-xs">▼</span>
            </button>

            <!-- 정렬 기준 드롭다운 -->
            <div
              v-if="showSortTypeMenu"
              class="absolute top-full left-0 mt-2 bg-white border border-gray-300 rounded-lg shadow-lg overflow-hidden min-w-[120px] z-10"
            >
              <button
                v-for="option in sortTypeOptions"
                :key="option.value"
                class="block w-full px-4 py-3 border-none bg-white text-left cursor-pointer text-sm text-gray-600 transition-colors hover:bg-gray-100"
                :class="{
                  'text-primary font-semibold bg-blue-50':
                    sortType === option.value,
                }"
                @click="handleSortTypeChange(option.value)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>

          <!-- 정렬 순서 토글 버튼 -->
          <button
            class="w-10 h-10 bg-white border border-gray-300 rounded-lg flex items-center justify-center cursor-pointer transition-all hover:border-primary hover:bg-blue-50"
            @click="toggleSortOrder"
            :title="sortOrder === 'ASC' ? '오름차순' : '내림차순'"
          >
            <span v-if="sortOrder === 'ASC'" class="text-lg">↑</span>
            <span v-else class="text-lg">↓</span>
          </button>
        </div>

        <!-- 제목 (우측) -->
        <h2 class="text-lg font-semibold text-gray-800 whitespace-nowrap">
          <span v-if="userStore.nickname">
            <span class="text-primary">{{ userStore.nickname }}</span> 님의 모임 목록
          </span>
          <span v-else>
            모임 목록
          </span>
        </h2>
      </div>
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

        <!-- 무한 스크롤 트리거 -->
        <div ref="loadMoreTrigger" class="h-10"></div>

        <!-- 추가 로딩 인디케이터 -->
        <div v-if="isLoadingMore" class="text-center py-6">
          <div
            class="inline-block w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"
          ></div>
          <p class="mt-2 text-sm text-gray-600">추가 로딩 중...</p>
        </div>

        <!-- 더 이상 데이터 없음 -->
        <div
          v-if="!hasMore && sortedMeetings.length > 0"
          class="text-center py-6 text-gray-400 text-sm"
        >
          모든 모임을 불러왔습니다
        </div>
      </div>
    </div>
    <!-- 새 모임 버튼 (하단 고정) -->
    <div
      class="fixed bottom-0 left-0 right-0 max-w-app mx-auto px-5 py-4 bg-white border-t border-gray-300"
    >
      <button
        class="w-full px-6 py-3 bg-primary text-white border-none rounded-lg text-base font-semibold cursor-pointer flex items-center justify-center gap-1.5 transition-colors hover:bg-primary-dark active:scale-95"
        @click="handleCreateMeeting"
      >
        <span class="text-xl font-bold">+</span>
        <span>새 모임</span>
      </button>
    </div>
  </div>
</template>
