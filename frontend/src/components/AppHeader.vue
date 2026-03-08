<script setup>
import { computed, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "../stores/user";
import { useMeetingsStore } from "../stores/meetings";
import { authAPI } from "../services/authAPI";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const meetingsStore = useMeetingsStore();

const showProfileMenu = ref(false);
const isLoggingOut = ref(false);

// 페이지별 헤더 설정
const headerConfig = computed(() => {
  const routeName = route.name;
  
  switch(routeName) {
    case 'MeetingList':
      return {
        type: 'list',
        showBackButton: false,
        showProfile: true,
        title: null
      };
    case 'MeetingDetail':
      return {
        type: 'simple',
        showBackButton: true,
        showProfile: false,
        title: null
      };
    case 'ScheduleInput':
      return {
        type: 'titled',
        showBackButton: true,
        showProfile: false,
        title: '스케줄 추가',
        showRightButton: false
      };
    case 'CreateMeeting':
      return {
        type: 'titled',
        showBackButton: true,
        showProfile: true,
        title: route.query.mode === 'edit' ? '모임 수정하기' : '모임 생성하기',
        showRightButton: false
      };
    case 'MeetingInvite':
      return {
        type: 'none'
      };
    default:
      return {
        type: 'none'
      };
  }
});

const handleHomeClick = () => {
  router.push('/');
};

const handleProfileClick = () => {
  if (!userStore.provider) {
    router.push("/login");
  } else {
    showProfileMenu.value = true;
  }
};

const handleNicknameChange = () => {
  showProfileMenu.value = false;
  userStore.openNicknameModal();
};

const handleLogout = async () => {
  if (isLoggingOut.value) return;
  isLoggingOut.value = true;
  try {
    await authAPI.logout();
  } catch (e) {
    // 서버 오류여도 클라이언트 상태는 초기화
  } finally {
    meetingsStore.clearMeetings();
    userStore.logout();
    showProfileMenu.value = false;
    isLoggingOut.value = false;
    router.push('/');
  }
};

const handleRightButtonClick = () => {
  router.back(); // 닫기 버튼은 뒤로가기
};
</script>

<template>
  <!-- MeetingList 헤더 (타이틀 + 프로필 + 검색창 + 정렬) -->
  <header
    v-if="headerConfig.type === 'list'"
    class="sticky top-0 z-40 px-5 pb-1 pt-5 bg-background-light/95 backdrop-blur-sm transition-all duration-300"
  >
    <!-- 타이틀 + 프로필 -->
    <div class="flex items-center justify-between mb-3">
      <div>
        <h1 class="text-xl font-bold tracking-tight text-slate-700">내 일정</h1>
        <p class="text-sm text-text-sub font-medium">Coordinate your meetups</p>
      </div>

      <div v-if="!userStore.provider" class="flex-shrink-0">
        <button
          @click="handleProfileClick"
          class="px-3 py-1.5 bg-primary hover:bg-primary-dark text-gray-800 font-bold text-sm rounded-xl shadow-soft transition-all flex items-center gap-1.5"
        >
          <span class="material-icons text-base" aria-hidden="true">login</span>
          <span>로그인</span>
        </button>
      </div>

      <button v-else @click="handleProfileClick" class="relative group flex-shrink-0">
        <div class="absolute inset-0 bg-primary rounded-full blur opacity-40 group-hover:opacity-60 transition-opacity"></div>
        <img
          v-if="userStore.profileImgUrl"
          :src="userStore.profileImgUrl"
          :alt="userStore.nickname || 'User'"
          class="relative w-9 h-9 rounded-full object-cover border-2 border-white shadow-sm"
        />
        <div v-else class="relative w-9 h-9 rounded-full bg-primary flex items-center justify-center border-2 border-white shadow-sm">
          <span class="material-icons text-base text-gray-800" aria-hidden="true">person</span>
        </div>
      </button>
    </div>

    <!-- 검색창 + 정렬 (MeetingList에서 slot으로 주입) -->
    <slot></slot>
  </header>

  <!-- Simple 헤더 (홈 버튼) -->
  <header 
    v-else-if="headerConfig.type === 'simple'"
    class="sticky top-0 z-40 flex items-center justify-between px-5 py-4 bg-background-light/95 backdrop-blur-sm"
  >
    <button 
      v-if="headerConfig.showBackButton"
      @click="handleHomeClick"
      class="w-9 h-9 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
      title="홈으로"
    >
      <span class="material-symbols-rounded text-[20px] text-gray-600 group-hover:text-primary-dark transition-colors" aria-hidden="true">home</span>
    </button>
    
    <div class="flex-1"></div>
    
    <!-- Login Button or Profile Picture -->
    <div v-if="!userStore.provider" class="flex-shrink-0">
      <button 
        @click="handleProfileClick"
        class="px-3 py-1.5 bg-primary hover:bg-primary-dark text-gray-800 font-bold text-xs rounded-xl shadow-soft transition-all flex items-center gap-1.5"
      >
        <span class="material-icons text-base" aria-hidden="true">login</span>
        <span>로그인</span>
      </button>
    </div>
    
    <button v-else @click="handleProfileClick" class="relative group flex-shrink-0">
      <div class="absolute inset-0 bg-primary rounded-full blur opacity-40 group-hover:opacity-60 transition-opacity"></div>
      <img 
        v-if="userStore.profileImgUrl"
        :src="userStore.profileImgUrl" 
        :alt="userStore.nickname || 'User'"
        class="relative w-9 h-9 rounded-full object-cover border-2 border-white shadow-sm"
      />
      <div v-else class="relative w-9 h-9 rounded-full bg-primary flex items-center justify-center border-2 border-white shadow-sm">
        <span class="material-icons text-base text-gray-800" aria-hidden="true">person</span>
      </div>
    </button>
  </header>

  <!-- Titled 헤더 (홈 버튼 + 제목 + 오른쪽 버튼) -->
  <header 
    v-else-if="headerConfig.type === 'titled'"
    class="sticky top-0 z-40 flex items-center justify-between px-5 py-4 bg-background-light/95 backdrop-blur-sm"
  >
    <button 
      v-if="headerConfig.showBackButton"
      @click="handleHomeClick"
      class="w-9 h-9 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
      title="홈으로"
    >
      <span class="material-symbols-rounded text-[20px] text-gray-600 group-hover:text-primary-dark transition-colors" aria-hidden="true">home</span>
    </button>
    
    <h1 class="text-base font-bold tracking-tight text-gray-700 flex-1 text-center">{{ headerConfig.title }}</h1>
    
    <!-- Right Button or Profile -->
    <button 
      v-if="headerConfig.showRightButton"
      @click="handleRightButtonClick"
      class="w-9 h-9 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
    >
      <span class="material-symbols-rounded text-[20px] text-gray-600 group-hover:text-primary-dark transition-colors" aria-hidden="true">{{ headerConfig.rightButtonIcon }}</span>
    </button>
    
    <!-- Login Button or Profile Picture -->
    <div v-else-if="!userStore.provider" class="flex-shrink-0">
      <button 
        @click="handleProfileClick"
        class="w-9 h-9 flex items-center justify-center rounded-full bg-primary hover:bg-primary-dark transition-colors shadow-sm"
      >
        <span class="material-icons text-gray-800 text-base" aria-hidden="true">login</span>
      </button>
    </div>
    
    <button v-else @click="handleProfileClick" class="relative group flex-shrink-0">
      <div class="absolute inset-0 bg-primary rounded-full blur opacity-40 group-hover:opacity-60 transition-opacity"></div>
      <img 
        v-if="userStore.profileImgUrl"
        :src="userStore.profileImgUrl" 
        :alt="userStore.nickname || 'User'"
        class="relative w-9 h-9 rounded-full object-cover border-2 border-white shadow-sm"
      />
      <div v-else class="relative w-9 h-9 rounded-full bg-primary flex items-center justify-center border-2 border-white shadow-sm">
        <span class="material-icons text-base text-gray-800" aria-hidden="true">person</span>
      </div>
    </button>
  </header>

  <!-- 프로필 메뉴 모달 -->
  <Teleport to="body">
    <div
      v-if="showProfileMenu"
      class="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-end justify-center"
      @click.self="showProfileMenu = false"
    >
      <div class="bg-white w-full max-w-md rounded-t-3xl shadow-xl pb-safe">
        <!-- 핸들 바 -->
        <div class="flex justify-center pt-3 pb-1">
          <div class="w-10 h-1 rounded-full bg-gray-200"></div>
        </div>

        <!-- 프로필 정보 -->
        <div class="flex items-center gap-3 px-6 py-4 border-b border-gray-100">
          <div class="relative flex-shrink-0">
            <img
              v-if="userStore.profileImgUrl"
              :src="userStore.profileImgUrl"
              :alt="userStore.nickname || 'User'"
              class="w-11 h-11 rounded-full object-cover border-2 border-primary/30"
            />
            <div v-else class="w-11 h-11 rounded-full bg-primary flex items-center justify-center border-2 border-primary/30">
              <span class="material-icons text-gray-800 text-lg" aria-hidden="true">person</span>
            </div>
          </div>
          <div class="min-w-0">
            <p class="font-bold text-gray-800 text-sm truncate">{{ userStore.nickname || '닉네임 없음' }}</p>
            <p class="text-xs text-gray-400 mt-0.5">{{ userStore.provider || '' }} 계정</p>
          </div>
        </div>

        <!-- 메뉴 항목 -->
        <div class="px-4 py-3 space-y-1">
          <button
            @click="handleNicknameChange"
            class="w-full flex items-center gap-3 px-4 py-3.5 rounded-2xl hover:bg-gray-50 transition-colors text-left"
          >
            <span class="material-icons text-gray-500 text-xl" aria-hidden="true">edit</span>
            <span class="text-sm font-medium text-gray-700">닉네임 변경</span>
          </button>

          <button
            @click="handleLogout"
            :disabled="isLoggingOut"
            class="w-full flex items-center gap-3 px-4 py-3.5 rounded-2xl hover:bg-red-50 transition-colors text-left disabled:opacity-50"
          >
            <span class="material-icons text-red-500 text-xl" aria-hidden="true">logout</span>
            <span class="text-sm font-medium text-red-500">{{ isLoggingOut ? '로그아웃 중...' : '로그아웃' }}</span>
          </button>
        </div>

        <div class="pb-6"></div>
      </div>
    </div>
  </Teleport>
</template>
