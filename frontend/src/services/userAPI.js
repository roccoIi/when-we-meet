/**
 * 사용자 관련 API
 */

import apiClient from './api'

export const userAPI = {
  /**
   * 내 닉네임 설정/수정
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {string} nickname - 새로운 닉네임 (최대 10자)
   * 
   * @example
   * setNickname('홍길동')
   */
  setNickname: async (nickname) => {
    const response = await apiClient.put('/api/user/nickname', { nickname })
    return response.data
  },

  /**
   * 내 프로필 조회
   * userId는 accessToken에서 백엔드가 추출
   */
  getMyProfile: async () => {
    const response = await apiClient.get('/api/user/me')
    return response.data
  },

  /**
   * 다른 사용자 프로필 조회 (공개 정보만)
   * 
   * @param {number} userId - 사용자 ID
   */
  getUserProfile: async (userId) => {
    const response = await apiClient.get(`/api/user/${userId}`)
    return response.data
  },

  /**
   * 내 프로필 수정
   * userId는 accessToken에서 백엔드가 추출
   * 
   * @param {Object} profileData - 수정할 프로필 정보
   * @param {string} profileData.nickname - 닉네임 (선택)
   * @param {string} profileData.profileImage - 프로필 이미지 URL (선택)
   * @param {string} profileData.bio - 소개 (선택)
   */
  updateProfile: async (profileData) => {
    const response = await apiClient.put('/api/user/profile', profileData)
    return response.data
  },

  /**
   * 회원 탈퇴
   * userId는 accessToken에서 백엔드가 추출
   */
  deleteAccount: async () => {
    const response = await apiClient.delete('/api/user/me')
    return response.data
  },
}
