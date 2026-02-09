<script setup>
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();

const handleLoginClick = () => {
  console.log('🔘 [Header] 버튼 클릭');
  console.log('  - isLoggedIn:', userStore.isLoggedIn);
  console.log('  - nickname:', userStore.nickname);
  console.log('  - provider:', userStore.provider);
  if (userStore.provider === null) {
    // 로그아웃 상태 또는 닉네임 없음: 로그인 페이지로 이동
    console.log('  → 로그인 페이지로 이동');
    router.push("/login");
  } else {
    // 로그인 상태 + 닉네임 있음: 닉네임 모달 열기 (닉네임 변경)
    console.log('  → 닉네임 모달 열기');
    userStore.openNicknameModal();
  }
};
</script>

<template>
  <header class="bg-white border-b border-gray-300 sticky top-0 z-50 w-full">
    <div class="px-5 py-4 flex justify-between items-center">
      <svg 
        xmlns="http://www.w3.org/2000/svg" 
        viewBox="0 0 24 24" 
        fill="currentColor" 
        class="size-6 text-primary cursor-pointer transition-transform hover:scale-110 active:scale-95"
        @click="router.push('/')"
      >
        <path d="M11.47 3.841a.75.75 0 0 1 1.06 0l8.69 8.69a.75.75 0 1 0 1.06-1.061l-8.689-8.69a2.25 2.25 0 0 0-3.182 0l-8.69 8.69a.75.75 0 1 0 1.061 1.06l8.69-8.689Z" />
        <path d="m12 5.432 8.159 8.159c.03.03.06.058.091.086v6.198c0 1.035-.84 1.875-1.875 1.875H15a.75.75 0 0 1-.75-.75v-4.5a.75.75 0 0 0-.75-.75h-3a.75.75 0 0 0-.75.75V21a.75.75 0 0 1-.75.75H5.625a1.875 1.875 0 0 1-1.875-1.875v-6.198a2.29 2.29 0 0 0 .091-.086L12 5.432Z" />
      </svg>

      <h1
        class="text-xl font-bold text-primary cursor-pointer select-none"
        @click="router.push('/')"
      >
        언제볼래
      </h1>

      <!-- 로그인 전: 로그인 버튼 -->
      <button
        v-if="!userStore.provider"
        class="px-4 py-2 bg-primary text-white border-none rounded-full text-sm font-medium cursor-pointer transition-all hover:bg-primary-dark active:scale-95"
        @click="handleLoginClick"
      >
        로그인
      </button>

      <!-- 로그인 후: 프로필 이미지 -->
      <button
        v-else
        class="w-10 h-10 rounded-full overflow-hidden border-2 border-primary cursor-pointer transition-all hover:scale-110 active:scale-95"
        @click="handleLoginClick"
        :title="userStore.nickname"
      >
        <img 
          v-if="userStore.profileImgUrl"
          :src="userStore.profileImgUrl" 
          :alt="userStore.nickname"
          class="w-full h-full object-cover"
        />
        <!-- 프로필 이미지가 없으면 닉네임 첫 글자 -->
        <div 
          v-else
          class="w-full h-full bg-primary text-white flex items-center justify-center text-sm font-bold"
        >
          {{ userStore.nickname?.charAt(0) || '?' }}
        </div>
      </button>
    </div>
  </header>
</template>
