/**
 * 환경변수 및 전역 상수 관리
 */

// API 기본 URL
export const API_BASE_URL = import.meta.env.VITE_BASE_URL

// OAuth 관련 URL
export const KAKAO_AUTH_URL = `${API_BASE_URL}/oauth2/authorization/kakao`
export const GOOGLE_AUTH_URL = `${API_BASE_URL}/oauth2/authorization/google`

// API 엔드포인트
export const REISSUE_TOKEN_URL = '/api/auth/reissue'

// 기타 설정
export const APP_NAME = '언제볼래'
export const MAX_NICKNAME_LENGTH = 10
export const MAX_MEETING_NAME_LENGTH = 30
