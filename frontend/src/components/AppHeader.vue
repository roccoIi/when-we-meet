<script setup>
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";

const router = useRouter();
const userStore = useUserStore();

const handleLoginClick = () => {
  if (userStore.isLoggedIn) {
    // 로그인 되어있으면 닉네임 클릭 시 모달 열기
    userStore.openNicknameModal();
  } else {
    // 로그인 안되어있으면 로그인 페이지로 이동
    router.push("/login");
  }
};
</script>

<template>
  <header class="bg-white border-b border-gray-300 sticky top-0 z-50 w-full">
    <div class="px-5 py-4 flex justify-between items-center">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="size-6 text-primary">
        <path d="M11.47 3.841a.75.75 0 0 1 1.06 0l8.69 8.69a.75.75 0 1 0 1.06-1.061l-8.689-8.69a2.25 2.25 0 0 0-3.182 0l-8.69 8.69a.75.75 0 1 0 1.061 1.06l8.69-8.689Z" />
        <path d="m12 5.432 8.159 8.159c.03.03.06.058.091.086v6.198c0 1.035-.84 1.875-1.875 1.875H15a.75.75 0 0 1-.75-.75v-4.5a.75.75 0 0 0-.75-.75h-3a.75.75 0 0 0-.75.75V21a.75.75 0 0 1-.75.75H5.625a1.875 1.875 0 0 1-1.875-1.875v-6.198a2.29 2.29 0 0 0 .091-.086L12 5.432Z" />
      </svg>

      <h1
        class="text-xl font-bold text-primary cursor-pointer select-none"
        @click="router.push('/')"
      >
        언제볼래
      </h1>

      <button
        class="px-4 py-2 bg-primary text-white border-none rounded-full text-sm font-medium cursor-pointer transition-all hover:bg-primary-dark active:scale-95"
        @click="handleLoginClick"
      >
        <span v-if="userStore.isLoggedIn">
          {{ userStore.nickname }}
        </span>
        <span v-else>로그인</span>
      </button>
    </div>
  </header>
</template>
