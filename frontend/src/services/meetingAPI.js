/**
 * 모임 관련 API
 */

import apiClient from './api'

export const meetingAPI = {
  /**
   * 모임 목록 조회 (내가 속한 모임)
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {Object} params - 쿼리 파라미터
   * @param {number} params.page - 페이지 번호 (default: 1)
   * @param {number} params.limit - 페이지당 항목 수 (default: 10)
   * @param {string} params.type - 정렬 기준: 'NAME' | 'JOIN_DATE' | 'MEETING_DATE' (default: 'JOIN_DATE')
   * @param {string} params.sort - 정렬 방법: 'ASC' | 'DESC' (default: 'DESC')
   * 
   * @returns {Promise<{data: Array, pagination: Object}>}
   * @example
   * // 응답 형식
   * {
   *   "code": "200",
   *   "message": "SUCCESS",
   *   "data": [
   *     {
   *       "shareCode": "8f66fdb722264",
   *       "name": "테스트미팅",
   *       "memberNumber": 2,
   *       "meetingDate": null,
   *       "role": "MEMBER"
   *     }
   *   ],
   *   "pagination": {
   *     "currentPage": 1,
   *     "totalPages": 2,
   *     "totalItems": 20,
   *     "hasMore": true
   *   }
   * }
   */
  getMeetings: async ({ page = 1, limit = 10, type = 'JOIN_DATE', sort = 'DESC' } = {}) => {
    const url = `/api/meetings?page=${page}&limit=${limit}&type=${type}&sort=${sort}`
    
    const response = await apiClient.get(url)
    // response.data는 백엔드의 CommonResponse 전체
    // { code, message, data: [...], pagination: {...} }
    return response.data
  },

  /**
   * 모임 생성
   * userId는 accessToken에서 백엔드가 추출 (자동으로 생성자로 설정)
   * 
   * @param {Object} meetingData - 모임 정보
   * @param {string} meetingData.name - 모임 이름 (필수, 최대 30자)
   * @param {string} meetingData.description - 모임 설명 (선택)
   * @param {string} meetingData.targetDate - 목표 날짜 (선택, ISO 8601 형식)
   * 
   * @example
   * createMeeting({ 
   *   name: '팀 회식',
   *   description: '12월 회식 일정 조율',
   *   targetDate: '2024-12-25'
   * })
   */
  createMeeting: async (meetingData) => {
    const response = await apiClient.post('/api/meetings', meetingData)
    return response.data
  },

  /**
   * 모임 상세 조회 (ID 기반 - 내부용)
   * 
   * @param {number} meetingId - 모임 ID
   */
  getMeetingDetail: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}`)
    return response.data
  },

  /**
   * 모임 상세 조회 (ShareCode 기반 - 권장)
   * 
   * @param {string} shareCode - 공유 코드
   * @returns {Promise<{name: string, memberNumber: number, info: Array<{nickname: string, profileImgUrl: string}>}>}
   */
  getMeetingDetailByShareCode: async (shareCode) => {
    const response = await apiClient.get(`/api/meetings/${shareCode}`)
    return response.data
  },

  /**
   * 모임 수정
   * 
   * @param {number} meetingId - 모임 ID
   * @param {Object} meetingData - 수정할 모임 정보
   * @param {string} meetingData.name - 모임 이름 (선택)
   * @param {string} meetingData.description - 모임 설명 (선택)
   * @param {string} meetingData.confirmedDate - 확정된 날짜 (선택, ISO 8601 형식)
   */
  updateMeeting: async (meetingId, meetingData) => {
    const response = await apiClient.put(`/api/meetings/${meetingId}`, meetingData)
    return response.data
  },

  /**
   * 모임 삭제
   * 생성자만 삭제 가능 (백엔드에서 권한 체크)
   * 
   * @param {number} meetingId - 모임 ID
   */
  deleteMeeting: async (meetingId) => {
    const response = await apiClient.delete(`/api/meetings/${meetingId}`)
    return response.data
  },

  /**
   * 모임 삭제 (ShareCode 기반)
   * 생성자만 삭제 가능 (백엔드에서 권한 체크)
   * 
   * @param {string} shareCode - 공유 코드
   */
  deleteMeetingByShareCode: async (shareCode) => {
    const response = await apiClient.delete(`/api/meetings/share/${shareCode}`)
    return response.data
  },

  /**
   * 모임 탈퇴
   * 현재 사용자가 모임에서 나가기
   * 
   * @param {string} shareCode - 공유 코드
   */
  leaveMeeting: async (shareCode) => {
    const response = await apiClient.delete(`/api/meetings/leave/${shareCode}`)
    return response.data
  },

  /**
   * 모임 공유 링크 생성/조회
   * 
   * @param {number} meetingId - 모임 ID
   * @returns {Promise<{shareCode: string, shareUrl: string, expiresAt: string}>}
   */
  getShareLink: async (meetingId) => {
    const response = await apiClient.post(`/api/meetings/share/${meetingId}`)
    return response.data
  },

  /**
   * 공유 코드로 모임 정보 조회
   * 
   * @param {string} shareCode - 공유 코드
   * @returns {Promise<{name: string, participantCount: number}>}
   */
  getMeetingByShareCode: async (shareCode) => {
    const response = await apiClient.get(`/api/meetings/share/${shareCode}`)
    return response.data
  },

  /**
   * 공유 코드로 모임 참여
   * 
   * @param {string} shareCode - 공유 코드
   * @returns {Promise<{meetingId: number, message: string}>}
   */
  joinMeetingByShareCode: async (shareCode) => {
    const response = await apiClient.post(`/api/meetings/enter/${shareCode}`)
    return response.data
  },

  /**
   * 모임 달력 데이터 조회 (ID 기반 - 내부용)
   * 모든 참여자의 일정을 취합하여 불가능한 날짜 포함
   * 
   * @param {number} meetingId - 모임 ID
   * @param {number} year - 년도 (예: 2024)
   * @param {number} month - 월 (1-12)
   */
  getMeetingCalendar: async (meetingId, year, month) => {
    const response = await apiClient.get(
      `/api/meetings/${meetingId}/calendar?year=${year}&month=${month}`
    )
    return response.data
  },

  /**
   * 모임 달력 데이터 조회 (ShareCode 기반 - 권장)
   * 
   * @param {string} shareCode - 공유 코드
   * @param {number} year - 년도 (예: 2024)
   * @param {number} month - 월 (1-12)
   */
  getMeetingCalendarByShareCode: async (shareCode, year, month) => {
    const response = await apiClient.get(
      `/api/meetings/code/${shareCode}/calendar?year=${year}&month=${month}`
    )
    return response.data
  },

  /**
   * 추천 모임 날짜 조회 (1~5위) - ID 기반
   * 가장 많은 사람이 참여 가능한 날짜 순으로 정렬
   * 
   * @param {number} meetingId - 모임 ID
   * @param {number} limit - 추천 개수 (default: 5)
   */
  getRecommendedDates: async (meetingId, limit = 5) => {
    const response = await apiClient.get(
      `/api/meetings/${meetingId}/recommend?limit=${limit}`
    )
    return response.data
  },

  /**
   * 추천 모임 날짜 조회 (ShareCode 기반 - 권장)
   * 
   * @param {string} shareCode - 공유 코드
   * @param {number} limit - 추천 개수 (default: 5)
   */
  getRecommendedDatesByShareCode: async (shareCode, limit = 5) => {
    const response = await apiClient.get(
      `/api/meetings/code/${shareCode}/recommend?limit=${limit}`
    )
    return response.data
  },

  /**
   * 모임 참여자 목록 조회
   * 
   * @param {number} meetingId - 모임 ID
   */
  getParticipants: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}/participants`)
    return response.data
  },

  /**
   * 미팅 일정 확정/초기화 (ShareCode 기반)
   * 
   * @param {string} shareCode - 공유 코드
   * @param {Object} updateData - 업데이트 데이터
   * @param {string|null} updateData.name - 미팅룸 이름 (선택사항, null 가능)
   * @param {string|null} updateData.meetingDate - 확정 일정 (LocalDateTime 형식: YYYY-MM-DDTHH:mm:ss, null이면 초기화)
   * 
   * @example
   * // 일정 확정
   * updateMeetingSchedule('abc123', { name: null, meetingDate: '2026-02-15T14:00:00' })
   * 
   * // 일정 초기화
   * updateMeetingSchedule('abc123', { name: null, meetingDate: null })
   */
  updateMeetingSchedule: async (shareCode, updateData) => {
    const response = await apiClient.put(`/api/meetings/${shareCode}`, updateData)
    return response.data
  },
}
