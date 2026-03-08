<script setup>
import { ref } from "vue";
import { useUserStore } from "../stores/user";
import { userAPI } from "../services";

const emit = defineEmits(["close"]);
const userStore = useUserStore();

const nickname = ref(userStore.nickname || "");
const isLoading = ref(false);
const error = ref("");

const handleSubmit = async () => {
  if (!nickname.value.trim()) {
    error.value = "닉네임을 입력해주세요";
    return;
  }

  if (nickname.value.length > 10) {
    error.value = "닉네임은 10자 이하로 입력해주세요";
    return;
  }

  isLoading.value = true;
  error.value = "";

  try {
    
    // API 호출
    const response = await userAPI.setNickname(nickname.value);

    // store 업데이트
    userStore.setNickname(nickname.value);
    
    emit("close");
  } catch (err) {
    console.error('❌ [Nickname] 닉네임 설정 실패:', err);
    error.value = "닉네임 설정에 실패했습니다";
  } finally {
    isLoading.value = false;
  }
};

const handleBackdropClick = (e) => {
  if (e.target === e.currentTarget && userStore.nickname) {
    // 이미 닉네임이 있는 경우에만 닫기 허용
    emit("close");
  }
};
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 bg-black/40 backdrop-blur-sm z-[1000] flex items-end justify-center"
      @click="handleBackdropClick"
    >
      <div class="bg-white w-full max-w-md rounded-t-3xl shadow-xl">
        <!-- 핸들 바 -->
        <div class="flex justify-center pt-3 pb-1">
          <div class="w-10 h-1 rounded-full bg-gray-200"></div>
        </div>

        <div class="px-6 pt-3 pb-8">
          <h2 class="text-lg font-bold mb-1 text-gray-800">닉네임 설정</h2>
          <p class="text-xs text-gray-500 mb-5">사용하실 닉네임을 입력해주세요</p>

          <input
            v-model="nickname"
            type="text"
            class="w-full px-4 py-3 border border-gray-200 rounded-2xl text-sm transition-colors focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-gray-50"
            placeholder="닉네임 (최대 10자)"
            maxlength="10"
            @keyup.enter="handleSubmit"
          />

          <p v-if="error" class="text-red-500 text-xs mt-2">{{ error }}</p>

          <div class="flex gap-2 mt-5">
            <button
              v-if="userStore.nickname"
              class="flex-1 px-3 py-3 rounded-2xl text-sm font-medium transition-all bg-gray-100 text-gray-600 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="emit('close')"
              :disabled="isLoading"
            >
              취소
            </button>
            <button
              class="flex-1 px-3 py-3 rounded-2xl text-sm font-semibold transition-all bg-primary text-gray-800 hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed"
              @click="handleSubmit"
              :disabled="isLoading"
            >
              {{ isLoading ? "저장 중..." : "확인" }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
