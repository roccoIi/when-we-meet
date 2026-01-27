<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useMeetingsStore } from "../stores/meetings";
import { meetingAPI } from "../services/api";

const router = useRouter();
const meetingsStore = useMeetingsStore();

const meetingName = ref("");
const isLoading = ref(false);
const error = ref("");

const handleSubmit = async () => {
  if (!meetingName.value.trim()) {
    error.value = "모임 이름을 입력해주세요";
    return;
  }

  if (meetingName.value.length > 30) {
    error.value = "모임 이름은 30자 이하로 입력해주세요";
    return;
  }

  isLoading.value = true;
  error.value = "";

  try {
    // API 호출 (실제 백엔드 연동 시 주석 해제)
    // const newMeeting = await meetingAPI.createMeeting({ name: meetingName.value })

    // 임시: 새 모임 생성
    const newMeeting = {
      id: Date.now(),
      name: meetingName.value,
      participantCount: 1,
      meetingDate: null,
    };

    meetingsStore.addMeeting(newMeeting);
    router.push(`/meeting/${newMeeting.id}`);
  } catch (err) {
    error.value = "모임 생성에 실패했습니다";
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.back();
};
</script>

<template>
  <div class="min-h-[calc(100vh-60px)] bg-gray-100 p-5">
    <div class="w-full">
      <h2 class="text-2xl font-bold text-gray-800 mb-6">새 모임 만들기</h2>

      <div class="bg-white rounded-2xl p-6 mb-5 shadow-sm">
        <div class="mb-5">
          <label
            for="meeting-name"
            class="block text-sm font-semibold text-gray-800 mb-2"
          >
            모임 이름
          </label>
          <input
            id="meeting-name"
            v-model="meetingName"
            type="text"
            class="w-full px-3 py-3 border border-gray-300 rounded-lg text-base transition-colors focus:outline-none focus:border-primary"
            placeholder="예: 친구들 모임, 스터디 그룹"
            maxlength="30"
            @keyup.enter="handleSubmit"
            autofocus
          />
          <p class="text-right text-xs text-gray-400 mt-1">
            {{ meetingName.length }}/30
          </p>
        </div>

        <p v-if="error" class="text-red-500 text-sm mb-4">{{ error }}</p>

        <div class="flex gap-3">
          <button
            class="flex-1 px-3 py-3.5 border-none rounded-lg text-base font-semibold cursor-pointer transition-all bg-gray-100 text-gray-600 hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
            @click="handleCancel"
            :disabled="isLoading"
          >
            취소
          </button>
          <button
            class="flex-1 px-3 py-3.5 border-none rounded-lg text-base font-semibold cursor-pointer transition-all bg-primary text-white hover:bg-primary-dark disabled:opacity-50 disabled:cursor-not-allowed"
            @click="handleSubmit"
            :disabled="isLoading || !meetingName.trim()"
          >
            {{ isLoading ? "생성 중..." : "모임 만들기" }}
          </button>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-5 shadow-sm">
        <h3 class="text-base font-semibold text-gray-800 mb-3">📌 안내</h3>
        <ul class="list-none p-0">
          <li
            class="text-sm text-gray-600 mb-2 pl-4 relative before:content-['•'] before:absolute before:left-0 before:text-primary"
          >
            모임을 만들면 고유한 공유 링크가 생성됩니다
          </li>
          <li
            class="text-sm text-gray-600 mb-2 pl-4 relative before:content-['•'] before:absolute before:left-0 before:text-primary"
          >
            링크를 통해 다른 사람들을 초대할 수 있습니다
          </li>
          <li
            class="text-sm text-gray-600 pl-4 relative before:content-['•'] before:absolute before:left-0 before:text-primary"
          >
            각 참여자는 자신의 가능한 날짜를 입력할 수 있습니다
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
