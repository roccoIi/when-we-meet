<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import { meetingAPI } from "../services";

const router = useRouter();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

const isLoading = ref(false);
const isLoadingMore = ref(false);
const searchQuery = ref("");

// 정렬 기준 (type)
const sortType = ref("JOIN_DATE"); // 기본값: Join At
const sortOrder = ref("DESC"); // 기본값: 내림차순

// 페이지네이션
const currentPage = ref(1);
const pageLimit = ref(10);
const hasMore = ref(true);

// 무한 스크롤을 위한 감지 요소
const loadMoreTrigger = ref(null);

// 정렬 기준 매핑
const sortTypeMapping = {
  "JOIN_DATE": "joinAt",
  "NAME": "name", 
  "MEETING_DATE": "dday"
};

// 정렬 순서 토글
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === "ASC" ? "DESC" : "ASC";
  loadMeetings(true);
};

// D-day 계산
const calculateDday = (meetingDate) => {
  if (!meetingDate) return null;
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const meeting = new Date(meetingDate);
  meeting.setHours(0, 0, 0, 0);
  
  const diffTime = meeting - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) return "D-Day";
  if (diffDays > 0) return `D-${diffDays}`;
  return `D+${Math.abs(diffDays)}`;
};

// D-day 색상
const getDdayColor = (meetingDate) => {
  if (!meetingDate) return 'peach-accent/20';
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const meeting = new Date(meetingDate);
  meeting.setHours(0, 0, 0, 0);
  
  const diffTime = meeting - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays <= 0) return 'soft-pink/20';
  if (diffDays <= 3) return 'mint-primary/80';
  if (diffDays <= 7) return 'mint-light/60';
  return 'mint-light/60';
};

// 날짜 포맷 (한국식)
const formatDate = (dateString) => {
  if (!dateString) return 'TBD';
  
  const date = new Date(dateString);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  
  return `${month}월 ${day}일, ${hour}:${minute}`;
};

onMounted(async () => {
  // App.vue의 초기화가 완료될 때까지 대기
  if (!userStore.isInitialized) {
    // 초기화 완료를 기다림 (최대 5초)
    let attempts = 0
    const maxAttempts = 50 // 5초 (100ms * 50)
    
    while (!userStore.isInitialized && attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 100))
      attempts++
    }
    
    if (userStore.isInitialized) {
    } else {
    }
  }

  // 디버깅: 사용자 정보 확인

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

// 검색 필터링된 모임 목록
const filteredMeetings = computed(() => {
  if (!searchQuery.value.trim()) {
    return meetingsStore.meetings;
  }
  
  const query = searchQuery.value.toLowerCase();
  return meetingsStore.meetings.filter(meeting => 
    meeting.name.toLowerCase().includes(query)
  );
});

// 현재 시간 (상태바용)
const currentTime = ref(new Date().toLocaleTimeString('en-US', { 
  hour: '2-digit', 
  minute: '2-digit',
  hour12: false 
}));

// 1분마다 시간 업데이트
setInterval(() => {
  currentTime.value = new Date().toLocaleTimeString('en-US', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false 
  });
}, 60000);
</script>

<template>
  <div class="bg-background-light font-display text-gray-800 min-h-screen flex flex-col overflow-hidden relative">
    <!-- Search and Sort Section (sticky) -->
    <div class="sticky top-0 z-30 px-6 pb-1 pt-6 bg-background-light/95 backdrop-blur-sm transition-all duration-300">
      <!-- Title and Profile -->
      <div class="flex items-center justify-between mb-4">
        <div>
          <h1 class="text-2xl font-bold tracking-tight text-slate-700">내 일정</h1>
          <p class="text-sm text-text-sub font-medium">Coordinate your sweet meetups</p>
        </div>
        
        <!-- Login Button or Profile Picture -->
        <div v-if="!userStore.provider" class="flex-shrink-0">
          <button 
            @click="$router.push('/login')"
            class="px-4 py-2 bg-primary hover:bg-primary-dark text-gray-800 font-bold text-sm rounded-xl shadow-soft transition-all flex items-center gap-2"
          >
            <span class="material-icons text-lg">login</span>
            <span>로그인</span>
          </button>
        </div>
        
        <button v-else @click="userStore.openNicknameModal()" class="relative group flex-shrink-0">
          <div class="absolute inset-0 bg-primary rounded-full blur opacity-40 group-hover:opacity-60 transition-opacity"></div>
          <img 
            v-if="userStore.profileImgUrl"
            :src="userStore.profileImgUrl" 
            :alt="userStore.nickname || 'User'"
            class="relative w-11 h-11 rounded-full object-cover border-2 border-white shadow-sm"
          />
          <div v-else class="relative w-11 h-11 rounded-full bg-primary flex items-center justify-center border-2 border-white shadow-sm">
            <span class="material-icons text-gray-800">person</span>
          </div>
        </button>
      </div>

      <!-- Search Bar -->
      <div class="relative mb-3 group">
        <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
          <span class="material-icons text-mint-dark/80">search</span>
        </div>
        <input 
          v-model="searchQuery"
          class="block w-full pl-11 pr-4 py-3.5 border-none rounded-2xl leading-5 bg-white text-slate-700 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary/30 shadow-soft transition-all" 
          placeholder="모임 이름 검색..." 
          type="text"
        />
      </div>

      <!-- Sort Controls -->
      <div class="flex items-center justify-between pb-2 px-1">
        <div class="flex items-center gap-2">
          <div class="relative group">
            <select 
              v-model="sortType"
              @change="loadMeetings(true)"
              class="appearance-none bg-white text-slate-600 font-bold text-sm py-2.5 pl-4 pr-10 rounded-xl shadow-sm border border-transparent focus:ring-2 focus:ring-primary/30 focus:outline-none cursor-pointer hover:bg-gray-50 transition-colors w-32"
            >
              <option value="JOIN_DATE">참여일자</option>
              <option value="NAME">이름</option>
              <option value="MEETING_DATE">D-Day</option>
            </select>
            <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-mint-dark">
              <span class="material-icons text-sm">expand_more</span>
            </div>
          </div>
          <button 
            @click="toggleSortOrder"
            class="flex items-center justify-center w-10 h-10 bg-white text-primary-dark rounded-xl shadow-sm hover:bg-gray-50 active:scale-95 transition-all group border border-transparent hover:border-primary/20"
          >
            <span class="material-icons text-xl group-hover:scale-110 transition-transform" :class="sortOrder === 'ASC' ? '' : 'transform rotate-180'">sort</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <main class="flex-1 overflow-y-auto px-6 pt-2 pb-28 no-scrollbar space-y-5">
      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-10">
        <div class="w-12 h-12 border-4 border-mint-primary/20 border-t-mint-primary rounded-full animate-spin mx-auto"></div>
        <p class="mt-4 text-text-sub">로딩 중...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredMeetings.length === 0" class="text-center py-20 text-text-sub">
        <span class="material-icons text-6xl mb-4 text-mint-primary/30">event_busy</span>
        <p class="text-lg font-semibold mb-2">아직 참여한 모임이 없습니다</p>
        <p class="text-sm">새로운 모임을 만들어보세요!</p>
      </div>

      <!-- Meeting Cards -->
      <div v-else>
        <div
          v-for="meeting in filteredMeetings"
          :key="meeting.id"
          @click="handleMeetingClick(meeting)"
          class="bg-white rounded-3xl p-5 border border-gray-100 shadow-soft hover:shadow-soft-hover transition-all duration-300 relative group cursor-pointer mb-5"
          :class="meeting.meetingDate ? 'bg-gradient-to-br from-white to-gray-50' : ''"
        >
          <!-- Decorative Blobs (only for confirmed meetings) -->
          <div v-if="meeting.meetingDate" class="absolute -top-10 -right-10 w-32 h-32 bg-primary/20 rounded-full blur-3xl"></div>
          <div v-if="meeting.meetingDate" class="absolute bottom-0 left-0 w-24 h-24 bg-tertiary/30 rounded-full blur-2xl"></div>

          <div class="relative z-10">
            <div class="flex justify-between items-start mb-3">
              <div class="flex flex-col flex-1">
                <h3 class="text-lg font-bold text-slate-700 mb-1">{{ meeting.name }}</h3>
                <div class="flex items-center gap-1.5 text-xs font-semibold mt-1">
                  <div v-if="meeting.meetingDate" class="flex items-center gap-1.5 text-slate-500">
                    <div class="w-2 h-2 rounded-full bg-primary"></div>
                    <span>확정됨</span>
                  </div>
                  <div v-else class="flex items-center gap-1.5 text-text-sub">
                    <span class="material-icons text-sm text-mint-dark">how_to_vote</span>
                    <span>투표 진행 중</span>
                  </div>
                </div>
              </div>
              <div 
                v-if="calculateDday(meeting.meetingDate)"
                class="font-bold px-3 py-1.5 rounded-xl text-xs shadow-sm border backdrop-blur-sm"
                :class="meeting.meetingDate 
                  ? 'bg-white/80 text-primary-dark border-primary/20' 
                  : 'bg-tertiary/20 text-slate-600 border-transparent'"
              >
                {{ calculateDday(meeting.meetingDate) }}
              </div>
            </div>

            <!-- Meeting Date -->
            <div class="flex items-center gap-1.5 mb-4 text-sm font-medium">
              <span class="material-icons text-base text-slate-400">event</span>
              <span class="text-slate-600">{{ formatDate(meeting.meetingDate) }}</span>
            </div>

            <!-- Participants and Member Count -->
            <div class="flex items-center justify-between">
              <!-- Participants (placeholder - actual data from API if available) -->
              <div v-if="meeting.memberNumber > 0" class="flex -space-x-3 overflow-hidden">
                <div 
                  v-for="n in Math.min(3, meeting.memberNumber)" 
                  :key="n"
                  class="h-9 w-9 rounded-full ring-2 ring-white bg-gradient-to-br from-primary/30 to-tertiary/30 flex items-center justify-center text-xs font-bold text-slate-600 shadow-sm"
                >
                  {{ n }}
                </div>
                <div 
                  v-if="meeting.memberNumber > 3"
                  class="h-9 w-9 rounded-full ring-2 ring-white bg-tertiary/30 flex items-center justify-center text-xs font-bold text-slate-600 shadow-sm"
                >
                  +{{ meeting.memberNumber - 3 }}
                </div>
              </div>

              <!-- Total Member Count -->
              <div class="flex items-center gap-1.5 bg-white/60 px-3 py-1.5 rounded-lg">
                <span class="material-icons text-base text-slate-400">group</span>
                <span class="text-slate-600 font-bold">{{ meeting.memberNumber || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Infinite Scroll Trigger -->
        <div ref="loadMoreTrigger" class="h-10"></div>

        <!-- Loading More -->
        <div v-if="isLoadingMore" class="py-4 flex justify-center">
          <div class="w-8 h-8 border-4 border-mint-primary/20 border-t-mint-primary rounded-full animate-spin"></div>
        </div>

        <!-- No More Data -->
        <div v-if="!hasMore && filteredMeetings.length > 0" class="text-center py-6 text-text-sub text-sm">
          모든 모임을 불러왔습니다
        </div>
      </div>
    </main>

    <!-- FAB (Floating Action Button) -->
    <div class="fixed bottom-8 right-0 left-0 z-50 max-w-app mx-auto px-6 pointer-events-none">
      <div class="flex justify-end pointer-events-auto">
        <button 
          @click="handleCreateMeeting"
          class="w-16 h-16 bg-primary rounded-full shadow-lg shadow-primary/40 flex items-center justify-center text-gray-800 hover:scale-105 active:scale-95 transition-all duration-300 group border-2 border-white"
        >
          <span class="material-icons text-3xl group-hover:rotate-90 transition-transform duration-300 text-slate-700">add</span>
        </button>
      </div>
    </div>
  </div>
</template>
