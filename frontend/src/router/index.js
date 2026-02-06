import { createRouter, createWebHistory } from "vue-router";
import MeetingList from "../pages/MeetingList.vue";
import CreateMeeting from "../pages/CreateMeeting.vue";
import MeetingDetail from "../pages/MeetingDetail.vue";
import ScheduleInput from "../pages/ScheduleInput.vue";
import Login from "../pages/Login.vue";
import OAuthCallback from "../pages/OAuthCallback.vue";

const routes = [
  {
    path: "/",
    name: "MeetingList",
    component: MeetingList,
  },
  {
    path: "/create",
    name: "CreateMeeting",
    component: CreateMeeting,
  },
  {
    path: "/meeting/:id",
    name: "MeetingDetail",
    component: MeetingDetail,
    props: true,
  },
  {
    path: "/meeting/:id/schedule",
    name: "ScheduleInput",
    component: ScheduleInput,
    props: true,
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },
  {
    path: "/oauth/callback",
    name: "OAuthCallback",
    component: OAuthCallback,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
