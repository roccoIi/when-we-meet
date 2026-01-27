<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import NicknameModal from "./components/NicknameModal.vue";
import { useUserStore } from "./stores/user";

const route = useRoute();
const userStore = useUserStore();

// 로그인 페이지에서는 헤더를 숨김
const showHeader = computed(() => route.name !== "Login");
</script>

<template>
  <div id="app" class="font-sans antialiased text-gray-800">
    <div
      class="max-w-app mx-auto min-h-screen bg-gray-100 relative md:shadow-xl max-md:max-w-full"
    >
      <AppHeader v-if="showHeader" />

      <main class="w-full">
        <router-view />
      </main>
    </div>

    <NicknameModal
      v-if="userStore.showNicknameModal"
      @close="userStore.closeNicknameModal"
    />
  </div>
</template>
