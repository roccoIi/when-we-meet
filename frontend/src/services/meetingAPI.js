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
   * @param {string} params.sortBy - 정렬 기준: 'name' | 'dday' | 'participants' (optional)
   */
  getMeetings: async ({ page = 1, limit = 10, sortBy } = {}) => {
    let url = `/api/meetings?page=${page}&limit=${limit}`
    if (sortBy) url += `&sortBy=${sortBy}`
    
    const response = await apiClient.get(url)
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
   * 모임 상세 조회
   * 
   * @param {number} meetingId - 모임 ID
   */
  getMeetingDetail: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}`)
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
   * 모임 공유 링크 생성/조회
   * 
   * @param {number} meetingId - 모임 ID
   * @returns {Promise<{shareUrl: string, expiresAt: string}>}
   */
  getShareLink: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}/share`)
    return response.data
  },

  /**
   * 모임 달력 데이터 조회
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
   * 추천 모임 날짜 조회 (1~5위)
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
   * 모임 참여자 목록 조회
   * 
   * @param {number} meetingId - 모임 ID
   */
  getParticipants: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}/participants`)
    return response.data
  },
}
