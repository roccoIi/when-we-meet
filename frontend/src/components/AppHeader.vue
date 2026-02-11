<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useUserStore } from "../stores/user";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

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
    userStore.openNicknameModal();
  }
};

const handleRightButtonClick = () => {
  router.back(); // 닫기 버튼은 뒤로가기
};
</script>

<template>
  <!-- MeetingList 헤더 (검색창 + 정렬 포함) -->
  <header 
    v-if="headerConfig.type === 'list'"
    class="sticky top-0 z-40 px-6 pb-4 pt-6 bg-background-light/95 backdrop-blur-sm transition-all duration-300"
  >
    <slot></slot>
  </header>

  <!-- Simple 헤더 (홈 버튼) -->
  <header 
    v-else-if="headerConfig.type === 'simple'"
    class="sticky top-0 z-40 flex items-center justify-between px-6 py-5 bg-background-light/95 backdrop-blur-sm"
  >
    <button 
      v-if="headerConfig.showBackButton"
      @click="handleHomeClick"
      class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
      title="홈으로"
    >
      <span class="material-symbols-rounded text-gray-600 group-hover:text-primary-dark transition-colors">home</span>
    </button>
    
    <div class="flex-1"></div>
    
    <!-- Login Button or Profile Picture -->
    <div v-if="!userStore.provider" class="flex-shrink-0">
      <button 
        @click="handleProfileClick"
        class="px-4 py-2 bg-primary hover:bg-primary-dark text-gray-800 font-bold text-sm rounded-xl shadow-soft transition-all flex items-center gap-2"
      >
        <span class="material-icons text-lg">login</span>
        <span>로그인</span>
      </button>
    </div>
    
    <button v-else @click="handleProfileClick" class="relative group flex-shrink-0">
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
  </header>

  <!-- Titled 헤더 (홈 버튼 + 제목 + 오른쪽 버튼) -->
  <header 
    v-else-if="headerConfig.type === 'titled'"
    class="sticky top-0 z-40 flex items-center justify-between px-6 py-5 bg-background-light/95 backdrop-blur-sm"
  >
    <button 
      v-if="headerConfig.showBackButton"
      @click="handleHomeClick"
      class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
      title="홈으로"
    >
      <span class="material-symbols-rounded text-gray-600 group-hover:text-primary-dark transition-colors">home</span>
    </button>
    
    <h1 class="text-lg font-bold tracking-tight text-gray-700 flex-1 text-center">{{ headerConfig.title }}</h1>
    
    <!-- Right Button or Profile -->
    <button 
      v-if="headerConfig.showRightButton"
      @click="handleRightButtonClick"
      class="w-10 h-10 flex items-center justify-center rounded-full bg-white hover:bg-neutral-light transition-colors group shadow-sm border border-gray-100"
    >
      <span class="material-symbols-rounded text-gray-600 group-hover:text-primary-dark transition-colors">{{ headerConfig.rightButtonIcon }}</span>
    </button>
    
    <!-- Login Button or Profile Picture -->
    <div v-else-if="!userStore.provider" class="flex-shrink-0">
      <button 
        @click="handleProfileClick"
        class="w-11 h-11 flex items-center justify-center rounded-full bg-primary hover:bg-primary-dark transition-colors shadow-sm"
      >
        <span class="material-icons text-gray-800 text-lg">login</span>
      </button>
    </div>
    
    <button v-else @click="handleProfileClick" class="relative group flex-shrink-0">
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
  </header>
</template>
