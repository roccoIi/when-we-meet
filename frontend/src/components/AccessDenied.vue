<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

defineProps({
  title: {
    type: String,
    default: '접근 권한이 없습니다'
  },
  message: {
    type: String,
    default: '이 페이지에 접근하려면 로그인이 필요합니다.\n로그인 후 다시 시도해주세요.'
  }
});
</script>

<template>
  <div class="min-h-screen relative flex flex-col bg-background-light overflow-hidden text-gray-800 antialiased selection:bg-primary selection:text-neutral-dark font-display">
    <main class="flex-1 flex items-center justify-center px-6">
      <div class="max-w-md w-full text-center">
        <!-- 아이콘 -->
        <div class="mb-6">
          <div class="mx-auto w-24 h-24 bg-red-50 rounded-full flex items-center justify-center">
            <span class="material-symbols-rounded text-red-500 text-6xl">lock</span>
          </div>
        </div>
        
        <!-- 메시지 -->
        <h2 class="text-2xl font-bold text-gray-800 mb-3">{{ title }}</h2>
        <p class="text-gray-600 mb-8 whitespace-pre-line">{{ message }}</p>
        
        <!-- 버튼 -->
        <div class="space-y-3">
          <button 
            @click="router.push('/login')"
            class="w-full bg-primary hover:bg-primary-dark text-gray-800 font-bold py-4 px-6 rounded-2xl transition-all transform hover:scale-[1.02] shadow-btn"
          >
            로그인하기
          </button>
          <button 
            @click="router.push('/')"
            class="w-full bg-white hover:bg-gray-50 text-gray-700 font-semibold py-4 px-6 rounded-2xl border-2 border-gray-200 transition-all"
          >
            홈으로
          </button>
        </div>
      </div>
    </main>
  </div>
</template>
