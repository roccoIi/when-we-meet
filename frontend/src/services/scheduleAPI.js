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
   * 내 일정 등록/수정 (ID 기반 - 내부용)
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {number} meetingId - 모임 ID
   * @param {Array<Object>} scheduleRanges - 일정 범위 배열
   * @param {string} scheduleRanges[].startTime - 시작 시간 (LocalDateTime 형식: YYYY-MM-DDTHH:mm:ss)
   * @param {string} scheduleRanges[].endTime - 종료 시간 (LocalDateTime 형식: YYYY-MM-DDTHH:mm:ss)
   * 
   * @example
   * // 여러 시간 범위를 선택한 경우
   * saveSchedule(123, [
   *   { startTime: '2026-01-15T14:00:00', endTime: '2026-01-15T16:00:00' },
   *   { startTime: '2026-01-15T19:00:00', endTime: '2026-01-15T20:00:00' },
   *   { startTime: '2026-01-16T12:00:00', endTime: '2026-01-16T17:00:00' }
   * ])
   */
  saveSchedule: async (meetingId, scheduleRanges) => {
    const response = await apiClient.post(`/api/meetings/${meetingId}/schedule`, scheduleRanges)
    return response.data
  },

  /**
   * 내 일정 등록/수정 (ShareCode 기반 - 권장)
   * 
   * @param {string} shareCode - 공유 코드
   * @param {Array<Object>} scheduleRanges - 일정 범위 배열
   */
  saveScheduleByShareCode: async (shareCode, scheduleRanges) => {
    const response = await apiClient.post(`/api/meetings/code/${shareCode}/schedule`, scheduleRanges)
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
