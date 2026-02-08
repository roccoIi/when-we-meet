/**
 * 인증 관련 API
 */

import apiClient from './api'

export const authAPI = {
  /**
   * 토큰 재발급
   * refreshToken은 쿠키로 자동 전송됨
   */
  reissueToken: async () => {
    const response = await apiClient.post('/api/auth/reissue')
    return response  // 전체 response 반환 (헤더 접근 가능)
  },

  /**
   * 로그아웃
   */
  logout: async () => {
    const response = await apiClient.post('/api/auth/logout')
    return response.data
  },

  /**
   * 사용자 정보 조회
   */
  getUserInfo: async () => {
    const response = await apiClient.get('/api/auth/me')
    return response.data
  },
}
