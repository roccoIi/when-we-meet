const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8000";

// 공통 fetch 함수
const fetchAPI = async (endpoint, options = {}) => {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.statusText}`);
  }

  return response.json();
};

// 인증 관련 API
export const authAPI = {
  // 카카오 로그인
  loginWithKakao: async (code) => {
    return fetchAPI("/api/auth/kakao", {
      method: "POST",
      body: JSON.stringify({ code }),
    });
  },

  // 구글 로그인
  loginWithGoogle: async (token) => {
    return fetchAPI("/api/auth/google", {
      method: "POST",
      body: JSON.stringify({ token }),
    });
  },

  // 로그아웃
  logout: async () => {
    return fetchAPI("/api/auth/logout", {
      method: "POST",
    });
  },

  // 사용자 정보 조회
  getUserInfo: async () => {
    return fetchAPI("/api/auth/me");
  },
};

// 사용자 관련 API
export const userAPI = {
  // 닉네임 설정
  setNickname: async (nickname) => {
    return fetchAPI("/api/user/nickname", {
      method: "PUT",
      body: JSON.stringify({ nickname }),
    });
  },
};

// 모임 관련 API
export const meetingAPI = {
  // 모임 목록 조회
  getMeetings: async (page = 1, limit = 10) => {
    return fetchAPI(`/api/meetings?page=${page}&limit=${limit}`);
  },

  // 모임 생성
  createMeeting: async (meetingData) => {
    return fetchAPI("/api/meetings", {
      method: "POST",
      body: JSON.stringify(meetingData),
    });
  },

  // 모임 상세 조회
  getMeetingDetail: async (meetingId) => {
    return fetchAPI(`/api/meetings/${meetingId}`);
  },

  // 모임 공유 링크 생성
  getShareLink: async (meetingId) => {
    return fetchAPI(`/api/meetings/${meetingId}/share`);
  },

  // 모임 달력 데이터 조회 (불가능한 날짜 포함)
  getMeetingCalendar: async (meetingId, year, month) => {
    return fetchAPI(
      `/api/meetings/${meetingId}/calendar?year=${year}&month=${month}`
    );
  },

  // 추천 모임 날짜 조회 (1~5위)
  getRecommendedDates: async (meetingId) => {
    return fetchAPI(`/api/meetings/${meetingId}/recommend`);
  },
};

// 일정 관련 API
export const scheduleAPI = {
  // 사용자 일정 조회
  getUserSchedule: async (meetingId) => {
    return fetchAPI(`/api/meetings/${meetingId}/schedule`);
  },

  // 사용자 일정 등록/수정
  saveSchedule: async (meetingId, scheduleData) => {
    return fetchAPI(`/api/meetings/${meetingId}/schedule`, {
      method: "POST",
      body: JSON.stringify(scheduleData),
    });
  },
};
