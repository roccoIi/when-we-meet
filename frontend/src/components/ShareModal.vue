<script setup>
import { ref } from "vue";

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
  shareUrl: {
    type: String,
    required: true,
  },
  meetingName: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["close"]);

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(props.shareUrl);
    alert("링크가 클립보드에 복사되었습니다!");
  } catch (error) {
    console.error("복사 실패:", error);
    alert("링크 복사에 실패했습니다.");
  }
};

const handleShare = async () => {
  try {
    if (navigator.share) {
      await navigator.share({
        title: props.meetingName,
        text: `"${props.meetingName}" 모임에 참여해주세요!`,
        url: props.shareUrl,
      });
    } else {
      // navigator.share가 지원되지 않으면 클립보드 복사
      await copyToClipboard();
    }
  } catch (error) {
    // 사용자가 공유를 취소한 경우 (에러 아님)
    if (error.name !== "AbortError") {
      console.error("공유 실패:", error);
    }
  }
};

const handleBackdropClick = () => {
  emit("close");
};
</script>

<template>
  <!-- 백드롭 (배경 어둡게) -->
  <Transition name="fade">
    <div
      v-if="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity"
      @click="handleBackdropClick"
    ></div>
  </Transition>

  <!-- 하단 모달 -->
  <Transition name="slide-up">
    <div
      v-if="isOpen"
      class="fixed bottom-0 left-0 right-0 bg-white rounded-t-3xl shadow-2xl z-50 max-w-app mx-auto"
      style="height: 40vh; min-height: 300px;"
    >
      <!-- 상단 핸들 바 -->
      <div class="flex justify-center pt-3 pb-2">
        <div class="w-12 h-1.5 bg-gray-300 rounded-full"></div>
      </div>

      <!-- 모달 내용 -->
      <div class="px-6 py-4 flex flex-col h-full">
        <!-- 제목 -->
        <h3 class="text-xl font-bold text-gray-800 mb-6 text-center">
          초대 링크 공유하기
        </h3>

        <!-- 링크 표시 영역 -->
        <div class="bg-gray-50 border border-gray-300 rounded-xl p-4 mb-6 flex items-center gap-3">
          <!-- 링크 텍스트 -->
          <div class="flex-1 overflow-hidden">
            <p class="text-sm text-gray-600 mb-1">공유 링크</p>
            <p class="text-sm text-gray-800 truncate font-medium">
              {{ shareUrl }}
            </p>
          </div>

          <!-- 복사 버튼 -->
          <button
            class="flex-shrink-0 px-4 py-2.5 bg-white border-2 border-primary text-primary rounded-lg font-semibold text-sm transition-all hover:bg-primary hover:text-white active:scale-95"
            @click="copyToClipboard"
          >
            복사
          </button>
        </div>

        <!-- 안내 문구 -->
        <p class="text-xs text-gray-500 text-center mb-6">
          이 링크를 받은 사람은 모임에 참여할 수 있습니다
        </p>

        <!-- 버튼 영역 -->
        <div class="mt-auto flex flex-col gap-3 pb-6">
          <!-- 공유하기 버튼 (메인) -->
          <button
            class="w-full px-6 py-4 bg-gradient-to-r from-primary to-purple-600 text-white border-none rounded-xl text-base font-bold cursor-pointer transition-all shadow-lg hover:shadow-xl hover:scale-105 active:scale-95"
            @click="handleShare"
          >
            <span class="flex items-center justify-center gap-2">
              <span>📤</span>
              <span>다른 앱으로 공유하기</span>
            </span>
          </button>

          <!-- 닫기 버튼 -->
          <button
            class="w-full px-6 py-3 bg-white text-gray-600 border-2 border-gray-300 rounded-xl text-base font-semibold cursor-pointer transition-all hover:bg-gray-50 active:scale-95"
            @click="handleBackdropClick"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
/* 페이드 인/아웃 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 슬라이드 업 애니메이션 */
.slide-up-enter-active {
  transition: transform 0.3s ease-out;
}

.slide-up-leave-active {
  transition: transform 0.3s ease-in;
}

.slide-up-enter-from {
  transform: translateY(100%);
}

.slide-up-leave-to {
  transform: translateY(100%);
}
</style>
