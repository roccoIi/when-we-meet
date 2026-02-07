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

// 토큰 재발급 진행 중 여부
let isRefreshing = false
// 재발급 대기 중인 요청들
let refreshSubscribers = []

// 재발급 완료 후 대기 중인 요청 재실행
const onRefreshed = (accessToken) => {
  refreshSubscribers.forEach((callback) => callback(accessToken))
  refreshSubscribers = []
}

// 재발급 실패 시 대기 중인 요청 모두 실패 처리
const onRefreshFailed = () => {
  refreshSubscribers = []
}

// Request Interceptor: 모든 요청에 accessToken 추가
apiClient.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const accessToken = userStore.getAccessToken()

    // accessToken이 있으면 헤더에 추가
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response Interceptor: 401 에러 시 자동으로 토큰 재발급
apiClient.interceptors.response.use(
  (response) => {
    // 정상 응답 처리
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // 401 에러이고, 재발급 요청이 아니며, 재시도하지 않은 요청인 경우
    if (
      error.response?.status === 401 &&
      originalRequest.url !== REISSUE_TOKEN_URL &&
      !originalRequest._retry
    ) {
      if (isRefreshing) {
        // 이미 재발급 진행 중이면 대기열에 추가
        return new Promise((resolve, reject) => {
          refreshSubscribers.push((accessToken) => {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`
            resolve(apiClient(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // 토큰 재발급 요청 (refreshToken은 쿠키로 자동 전송됨)
        const response = await apiClient.post(REISSUE_TOKEN_URL)
        
        // 응답 헤더에서 새로운 accessToken 추출
        const newAccessToken = response.headers['authorization']?.replace('Bearer ', '')
        
        if (newAccessToken) {
          // store에 새 토큰 저장
          const userStore = useUserStore()
          userStore.setAccessToken(newAccessToken)

          // 대기 중인 요청들에 새 토큰 전달
          onRefreshed(newAccessToken)

          // 원래 요청 재시도
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return apiClient(originalRequest)
        } else {
          throw new Error('No access token in response')
        }
      } catch (refreshError) {
        // 재발급 실패 시
        console.error('Token refresh failed:', refreshError)
        onRefreshFailed()

        // 로그아웃 처리
        const userStore = useUserStore()
        userStore.logout()

        // 로그인 페이지로 리다이렉트
        router.push('/login')

        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
