/**
 * Axios 인스턴스 및 인터셉터 설정
 * 모든 API 요청에 대한 공통 설정 관리
 */

import axios from 'axios'
import { useUserStore } from '../stores/user'
import { API_BASE_URL, REISSUE_TOKEN_URL } from '../config/constants'
import router from '../router'

// axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키를 포함하여 요청 (refreshToken용)
})


// Request Interceptor: 모든 요청에 accessToken 추가
apiClient.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const accessToken = userStore.getAccessToken()



    // accessToken이 있으면 헤더에 추가
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    } else {
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response Interceptor: 응답 헤더에서 토큰 자동 추출 및 401 에러 처리
apiClient.interceptors.response.use(
  (response) => {
    // 정상 응답 처리: Authorization 헤더에 토큰이 있으면 자동 저장
    const authorization = response.headers['authorization']
    if (authorization) {
      const accessToken = authorization.replace('Bearer ', '')
      const userStore = useUserStore()
      userStore.setAccessToken(accessToken)
    }
    
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // 공개 API 목록 (토큰 없이 접근 가능)
    const publicAPIs = [
      '/api/meetings/share/',  // 공유 코드로 모임 정보 조회
    ]

    // 공개 API는 401 처리 안 함
    const isPublicAPI = publicAPIs.some(api => originalRequest.url?.includes(api))

    // 401 에러 발생 시 토큰 재발급 시도
    if (
      error.response?.status === 401 && 
      !isPublicAPI && 
      originalRequest.url !== REISSUE_TOKEN_URL &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true
      
      
      try {
        // 토큰 재발급 요청 (refreshToken은 쿠키로 자동 전송됨)
        const response = await apiClient.post(REISSUE_TOKEN_URL)
        
        // 응답 헤더에서 새로운 accessToken 추출
        const newAccessToken = response.headers['authorization']?.replace('Bearer ', '')
        
        if (newAccessToken) {
          // store에 새 토큰 저장
          const userStore = useUserStore()
          userStore.setAccessToken(newAccessToken)

          // 원래 요청에 새 토큰 추가하고 재시도
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return apiClient(originalRequest)
        } else {
          throw new Error('No access token in response')
        }
      } catch (refreshError) {
        // 재발급 실패 시 로그인 페이지로 이동
        console.error('❌ [Interceptor] 토큰 재발급 실패 - 로그인 필요')
        
        // 로그아웃 처리
        const userStore = useUserStore()
        userStore.logout()

        // 로그인 페이지로 리다이렉트
        router.push('/login')

        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
