<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { KAKAO_AUTH_URL, GOOGLE_AUTH_URL } from "../config/constants";

const router = useRouter();
const userStore = useUserStore();

const isLoading = ref(false);

const handleKakaoLogin = async () => {
  isLoading.value = true;
  try {
    // 카카오 로그인 페이지로 리다이렉트
    window.location.href = KAKAO_AUTH_URL;
  } catch (error) {
    console.error("카카오 로그인 실패:", error);
    alert("로그인에 실패했습니다");
  } finally {
    isLoading.value = false;
  }
};

const handleGoogleLogin = async () => {
  isLoading.value = true;
  try {
    // 실제 구글 로그인 구현 시 주석 해제
    // window.location.href = GOOGLE_AUTH_URL;

    // 임시: 로그인 처리
    userStore.login({ id: 1, nickname: "구글유저" });
    router.push("/");
  } catch (error) {
    console.error("구글 로그인 실패:", error);
    alert("로그인에 실패했습니다");
  } finally {
    isLoading.value = false;
  }
};

</script>

<template>
  <div class="bg-background-light font-display text-text-main flex flex-col justify-between h-full min-h-screen antialiased overflow-hidden relative">
    <!-- Decorative Background Blurs -->
    <div class="fixed inset-0 pointer-events-none z-0 overflow-hidden">
      <div class="absolute top-[-5%] right-[-10%] w-[400px] h-[400px] bg-secondary/20 rounded-full blur-[80px]"></div>
      <div class="absolute bottom-[-5%] left-[-10%] w-[350px] h-[350px] bg-primary/15 rounded-full blur-[60px]"></div>
      <div class="absolute top-[40%] left-[20%] w-[200px] h-[200px] bg-tertiary/15 rounded-full blur-[50px]"></div>
    </div>

    <!-- Main Content -->
    <main class="relative z-10 flex flex-col h-full w-full max-w-md mx-auto px-8 py-8">
      <!-- Logo and Title Section -->
      <div class="flex-1 flex flex-col justify-center items-center text-center mt-8 mb-6 space-y-6">
        <!-- Logo -->
        <div class="relative group">
          <div class="absolute -inset-2 bg-gradient-to-tr from-primary to-secondary rounded-[2rem] blur-md opacity-30"></div>
          <div class="relative w-28 h-28 bg-white rounded-[2rem] flex items-center justify-center shadow-soft transform rotate-3 transition-transform hover:rotate-0 duration-300">
            <span class="material-icons text-primary text-5xl">calendar_month</span>
            <div class="absolute top-4 right-5 w-3 h-3 bg-accent-pink rounded-full"></div>
          </div>
        </div>

        <!-- Title -->
        <div class="space-y-1">
          <h1 class="text-3xl font-bold tracking-tight text-gray-800">
            <span class="text-accent-pink">언제</span>볼래
          </h1>
          <p class="text-sm font-semibold text-text-sub uppercase tracking-wider bg-white/50 px-3 py-1 rounded-full inline-block">When We Meet</p>
        </div>

        <!-- Description -->
        <div class="max-w-[280px]">
          <p class="text-base text-gray-500 font-medium leading-relaxed">
            Pick a date, find a time,<br/>make memories together.
          </p>
        </div>
      </div>

      <!-- Login Buttons Section -->
      <div class="w-full space-y-6 mb-10">
        <div class="relative flex py-1 items-center">
          <div class="flex-grow border-t border-gray-200"></div>
          <span class="flex-shrink-0 mx-4 text-gray-400 text-xs font-medium bg-background-light px-2">Simple Social Login</span>
          <div class="flex-grow border-t border-gray-200"></div>
        </div>

        <div class="space-y-4">
          <!-- Kakao Login -->
          <button 
            @click="handleKakaoLogin"
            :disabled="isLoading"
            class="relative w-full flex items-center justify-center bg-[#FEE500] hover:bg-[#ffe924] text-[#391b1b] font-bold py-4 px-6 rounded-2xl transition-all duration-200 transform hover:scale-[1.01] shadow-btn focus:outline-none focus:ring-4 focus:ring-yellow-100 disabled:opacity-50 disabled:cursor-not-allowed" 
            type="button"
          >
            <div class="absolute left-6 flex items-center">
              <svg class="w-5 h-5 fill-current" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 3C5.373 3 0 6.666 0 11.188C0 14.104 2.115 16.685 5.346 18.067C5.176 18.673 4.819 19.957 4.708 20.467C4.552 21.185 5.291 21.657 5.86 21.229C7.472 20.021 9.47 18.528 10.457 17.809C10.963 17.844 11.478 17.863 12 17.863C18.627 17.863 24 14.197 24 9.675C24 5.153 18.627 3 12 3Z"></path>
              </svg>
            </div>
            <span class="text-sm">Login with Kakao</span>
          </button>

          <!-- Google Login -->
          <button 
            @click="handleGoogleLogin"
            :disabled="isLoading"
            class="relative w-full flex items-center justify-center bg-white hover:bg-gray-50 text-gray-600 font-bold py-4 px-6 rounded-2xl border border-gray-100 transition-all duration-200 transform hover:scale-[1.01] shadow-btn focus:outline-none focus:ring-4 focus:ring-gray-100 disabled:opacity-50 disabled:cursor-not-allowed" 
            type="button"
          >
            <div class="absolute left-6 flex items-center">
              <svg class="w-5 h-5" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"></path>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"></path>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"></path>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"></path>
              </svg>
            </div>
            <span class="text-sm">Login with Google</span>
          </button>
          <!-- Apple Login
          <button 
            @click="handleAppleLogin"
            :disabled="isLoading"
            class="relative w-full flex items-center justify-center bg-gray-900 hover:bg-gray-800 text-white font-bold py-4 px-6 rounded-2xl transition-all duration-200 transform hover:scale-[1.01] shadow-lg shadow-gray-200 focus:outline-none focus:ring-4 focus:ring-gray-200 disabled:opacity-50 disabled:cursor-not-allowed" 
            type="button"
          >
            <div class="absolute left-6 flex items-center">
              <svg class="w-5 h-5 fill-current" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M17.05 20.28c-.98.95-2.05.88-3.08.4-1.09-.5-2.08-.48-3.24 0-1.44.62-2.2.44-3.06-.4C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.74 1.18 0 2.45-1.02 3.67-.86 1.54.12 2.7.75 3.44 1.88-2.9 1.75-2.43 5.48.56 6.74-.63 1.63-1.46 3.23-2.75 4.47zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"></path>
              </svg>
            </div>
            <span class="text-sm">Continue with Apple</span>
          </button> -->
        </div>
      </div>

      <!-- Terms and Conditions -->
      <div class="text-center pb-4">
        <p class="text-xs text-gray-400">
          By continuing, you agree to our 
          <a class="text-gray-600 hover:text-primary-dark font-semibold transition-colors" href="#">Terms of Service</a>
          and
          <a class="text-gray-600 hover:text-primary-dark font-semibold transition-colors" href="#">Privacy Policy</a>.
        </p>
      </div>
    </main>
  </div>
</template>
