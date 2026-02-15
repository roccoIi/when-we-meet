<script setup>
const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  isLeaving: {
    type: Boolean,
    default: false
  },
  meetingName: {
    type: String,
    default: '모임'
  }
});

const emit = defineEmits(['confirm', 'cancel']);

const handleConfirm = () => {
  emit('confirm');
};

const handleCancel = () => {
  emit('cancel');
};
</script>

<template>
  <div 
    v-if="isOpen"
    class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50"
    @click.self="handleCancel"
  >
    <div class="bg-white rounded-3xl shadow-soft max-w-sm w-full p-8 relative">
      <!-- Icon -->
      <div class="text-center mb-6">
        <div class="relative inline-block mb-4">
          <div class="absolute inset-0 bg-red-500 rounded-full blur-md opacity-20"></div>
          <div class="relative w-16 h-16 bg-red-50 rounded-full flex items-center justify-center shadow-soft">
            <span class="material-symbols-rounded text-red-500 text-3xl">logout</span>
          </div>
        </div>
        <h3 class="text-xl font-bold text-gray-800 mb-3">모임 탈퇴</h3>
        <p class="text-sm text-gray-600 leading-relaxed">
          방을 나가면 입력한 정보는<br/>
          저장되지 않습니다.<br/>
          <span class="font-bold text-gray-800 mt-2 block">정말로 방을 나가시겠습니까?</span>
        </p>
      </div>

      <!-- Buttons -->
      <div class="space-y-3">
        <button
          @click="handleConfirm"
          :disabled="isLeaving"
          class="w-full px-6 py-3 bg-red-500 hover:bg-red-600 text-white rounded-2xl font-bold shadow-lg transition-all transform active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          <span v-if="!isLeaving">예, 나가겠습니다</span>
          <span v-else class="flex items-center justify-center gap-2">
            <div class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            탈퇴 중...
          </span>
        </button>
        <button
          @click="handleCancel"
          :disabled="isLeaving"
          class="w-full px-6 py-3 bg-white text-gray-700 border border-gray-200 rounded-2xl font-semibold hover:bg-gray-50 transition-all disabled:opacity-50"
        >
          아니오
        </button>
      </div>
    </div>
  </div>
</template>
