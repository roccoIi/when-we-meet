<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "../stores/user";
import { authAPI } from "../services/api";

const router = useRouter();
const userStore = useUserStore();

const isLoading = ref(false);

const handleKakaoLogin = async () => {
  isLoading.value = true;
  try {
    // 실제 카카오 로그인 구현 시 주석 해제
    // const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`
    // window.location.href = kakaoAuthUrl

    // 임시: 로그인 처리
    userStore.login({ id: 1, nickname: "테스트유저" });
    router.push("/");
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
    // const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=email profile`
    // window.location.href = googleAuthUrl

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
  <div
    class="min-h-screen bg-gradient-to-br from-[#667eea] to-[#764ba2] flex justify-center items-center p-5 w-full"
  >
    <div class="max-w-[400px] w-full">
      <div class="text-center mb-12">
        <div class="text-7xl mb-4 animate-bounce">📅</div>
        <h1 class="text-4xl font-bold text-white mb-2 drop-shadow-md">
          언제만나
        </h1>
        <p class="text-base text-white/90">모임 날짜를 쉽게 정해보세요</p>
      </div>

      <div class="flex flex-col gap-3 mb-6">
        <button
          class="w-full px-4 py-4 border-none rounded-xl text-base font-semibold cursor-pointer flex items-center justify-center gap-3 transition-all shadow-lg bg-[#FEE500] text-black hover:enabled:-translate-y-0.5 hover:enabled:shadow-xl active:enabled:translate-y-0 disabled:opacity-60 disabled:cursor-not-allowed"
          @click="handleKakaoLogin"
          :disabled="isLoading"
        >
          <span class="text-xl font-bold">💬</span>
          <span>카카오로 시작하기</span>
        </button>

        <button
          class="w-full px-4 py-4 border-none rounded-xl text-base font-semibold cursor-pointer flex items-center justify-center gap-3 transition-all shadow-lg bg-white text-gray-800 hover:enabled:-translate-y-0.5 hover:enabled:shadow-xl active:enabled:translate-y-0 disabled:opacity-60 disabled:cursor-not-allowed"
          @click="handleGoogleLogin"
          :disabled="isLoading"
        >
          <span class="text-xl font-bold">G</span>
          <span>Google로 시작하기</span>
        </button>
      </div>

      <p class="text-center text-xs text-white/80 leading-relaxed">
        로그인하면 <a href="#" class="text-white underline">이용약관</a> 및
        <a href="#" class="text-white underline">개인정보처리방침</a>에<br />
        동의하는 것으로 간주됩니다
      </p>
    </div>
  </div>
</template>
