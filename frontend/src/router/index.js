import { createRouter, createWebHistory } from "vue-router";
import MeetingList from "../pages/MeetingList.vue";
import CreateMeeting from "../pages/CreateMeeting.vue";
import MeetingDetail from "../pages/MeetingDetail.vue";
import ScheduleInput from "../pages/ScheduleInput.vue";
import MeetingInvite from "../pages/MeetingInvite.vue";
import Login from "../pages/Login.vue";
import OAuthCallback from "../pages/OAuthCallback.vue";
import AccessDenied from "../components/AccessDenied.vue";
import { useUserStore } from "../stores/user";

const routes = [
  {
    path: "/",
    name: "MeetingList",
    component: MeetingList,
    meta: { requiresAuth: false }
  },
  {
    path: "/create",
    name: "CreateMeeting",
    component: CreateMeeting,
    meta: { requiresAuth: false }  // 생성 모드는 로그인 불필요
  },
  {
    path: "/meeting/:shareCode",
    name: "MeetingDetail",
    component: MeetingDetail,
    props: true,
    meta: { requiresAuth: true }  // 인증 필요
  },
  {
    path: "/meeting/:shareCode/schedule",
    name: "ScheduleInput",
    component: ScheduleInput,
    props: true,
    meta: { requiresAuth: true }  // 인증 필요
  },
  {
    path: "/invite/:shareCode",
    name: "MeetingInvite",
    component: MeetingInvite,
    props: true,
    meta: { requiresAuth: false }  // 초대 링크는 로그인 불필요
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: "/oauth/callback",
    name: "OAuthCallback",
    component: OAuthCallback,
    meta: { requiresAuth: false }
  },
  {
    path: "/access-denied",
    name: "AccessDenied",
    component: AccessDenied,
    meta: { requiresAuth: false }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 전역 Navigation Guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  
  // 인증이 필요한 페이지인지 확인
  let requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  
  // CreateMeeting의 수정 모드는 인증 필요
  if (to.name === 'CreateMeeting' && to.query.mode === 'edit') {
    requiresAuth = true;
  }
  
  if (requiresAuth) {
    // AccessToken 체크
    const accessToken = userStore.getAccessToken();
    
    if (!accessToken) {
      // AccessDenied 페이지로 리다이렉트
      next({ name: 'AccessDenied' });
    } else {
      next();
    }
  } else {
    // 인증 불필요한 페이지
    next();
  }
});

export default router;
