/**
 * 일정 관련 API
 */

import apiClient from './api'

export const scheduleAPI = {
  /**
   * 내 일정 조회 (특정 모임에서)
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {number} meetingId - 모임 ID
   */
  getUserSchedule: async (meetingId) => {
    const response = await apiClient.get(`/api/meetings/${meetingId}/schedule`)
    return response.data
  },

  /**
   * 내 일정 등록/수정
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {number} meetingId - 모임 ID
   * @param {Object} scheduleData - 일정 데이터
   * @param {Array<string>} scheduleData.availableDates - 가능한 날짜 배열 (날짜만 선택한 경우)
   * @param {Array<Object>} scheduleData.availableTimes - 가능한 시간 배열 (시간까지 선택한 경우)
   * @param {string} scheduleData.availableTimes[].date - 날짜 (ISO 8601 형식: YYYY-MM-DD)
   * @param {number} scheduleData.availableTimes[].hour - 시간 (0-23)
   * 
   * @example
   * // 날짜만 선택한 경우
   * saveSchedule(123, {
   *   availableDates: ['2024-12-25', '2024-12-26', '2024-12-27']
   * })
   * 
   * @example
   * // 시간까지 선택한 경우
   * saveSchedule(123, {
   *   availableTimes: [
   *     { date: '2024-12-25', hour: 14 },  // 12월 25일 14시
   *     { date: '2024-12-25', hour: 15 },  // 12월 25일 15시
   *     { date: '2024-12-26', hour: 18 },  // 12월 26일 18시
   *   ]
   * })
   */
  saveSchedule: async (meetingId, scheduleData) => {
    const response = await apiClient.post(`/api/meetings/${meetingId}/schedule`, scheduleData)
    return response.data
  },

  /**
   * 내 일정 삭제
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {number} meetingId - 모임 ID
   */
  deleteSchedule: async (meetingId) => {
    const response = await apiClient.delete(`/api/meetings/${meetingId}/schedule`)
    return response.data
  },

  /**
   * 특정 참여자의 일정 조회 (다른 사람 일정 확인)
   * 
   * @param {number} meetingId - 모임 ID
   * @param {number} participantId - 참여자 ID
   */
  getParticipantSchedule: async (meetingId, participantId) => {
    const response = await apiClient.get(
      `/api/meetings/${meetingId}/participants/${participantId}/schedule`
    )
    return response.data
  },
}
