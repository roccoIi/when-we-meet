/**
 * API 통합 Export
 * 모든 API를 한 곳에서 import 가능
 * 
 * 사용 예시:
 * import { authAPI, meetingAPI } from '@/services'
 * 또는
 * import { authAPI } from '@/services/authAPI'
 */

export { authAPI } from './authAPI'
export { userAPI } from './userAPI'
export { meetingAPI } from './meetingAPI'
export { scheduleAPI } from './scheduleAPI'

// axios 인스턴스도 export (필요시 직접 사용)
export { default as apiClient } from './api'
