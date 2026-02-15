import { defineStore } from 'pinia'
import { ref } from 'vue'
import { meetingAPI } from '../services'
import { API_BASE_URL } from '../config/constants'

export const useMeetingsStore = defineStore('meetings', () => {
  // 상태
  const meetings = ref([])
  const currentMeeting = ref(null)
  const sortBy = ref('dday') // 'name', 'dday', 'participants'
  const isLoading = ref(false)
  
  /**
   * 프로필 이미지 URL 처리 (guest 유저는 BASE_URL 붙이기)
   */
  const processProfileImageUrl = (imageUrl, hasProvider = true) => {
    if (!imageUrl) return ''
    
    // OAuth 유저 또는 이미 전체 URL인 경우 그대로 반환
    if (hasProvider || imageUrl.startsWith('http')) {
      return imageUrl
    }
    
    // Guest 유저: BASE_URL 붙이기
    return `${API_BASE_URL}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`
  }

  // 액션
  const setMeetings = (meetingList) => {
    meetings.value = meetingList
  }

  const addMeeting = (meeting) => {
    meetings.value.push(meeting)
  }

  const setCurrentMeeting = (meeting) => {
    currentMeeting.value = meeting
  }

  const setSortBy = (sortType) => {
    sortBy.value = sortType
  }

  const getSortedMeetings = () => {
    const sorted = [...meetings.value]
    switch (sortBy.value) {
      case 'name':
        return sorted.sort((a, b) => a.name.localeCompare(b.name))
      case 'dday':
        return sorted.sort((a, b) => {
          if (!a.meetingDate) return 1
          if (!b.meetingDate) return -1
          return new Date(a.meetingDate) - new Date(b.meetingDate)
        })
      case 'participants':
        return sorted.sort((a, b) => b.participantCount - a.participantCount)
      default:
        return sorted
    }
  }

  /**
   * shareCode로 미팅 정보 로드 (버전 체크 캐싱)
   * - 캐시가 있으면 버전 체크 API로 유효성 확인
   * - 버전이 같으면 캐시 사용, 다르면 전체 데이터 로드
   * 
   * @param {string} shareCode - 공유 코드
   */
  const loadMeetingByShareCode = async (shareCode) => {
    // === 1. 캐시 없음 or 다른 미팅 ===
    if (!currentMeeting.value || currentMeeting.value.shareCode !== shareCode) {
      return await fetchAndCache(shareCode)
    }
    
    // 캐시에 version 정보가 없으면 무효한 캐시로 간주
    if (currentMeeting.value.version === undefined || currentMeeting.value.version === null) {
      return await fetchAndCache(shareCode)
    }
    
    // === 2. 버전 체크 ===
    try {
      // 버전 체크 API 호출 (경량)
      const versionResponse = await meetingAPI.getMeetingVersion(shareCode)
      const serverVersionData = versionResponse.data || versionResponse
      const serverVersion = serverVersionData.version
      
      
      // 버전 비교
      if (currentMeeting.value.version === serverVersion) {
        return currentMeeting.value
      }
      
      // 버전 다름 → 전체 데이터 로드
      return await fetchAndCache(shareCode)
      
    } catch (error) {
      console.error('⚠️ [Cache] 버전 체크 실패 - 전체 로드')
      return await fetchAndCache(shareCode)
    }
  }
  
  /**
   * API 호출 및 캐싱 (내부 헬퍼 함수)
   */
  const fetchAndCache = async (shareCode) => {
    try {
      const response = await meetingAPI.getMeetingDetailByShareCode(shareCode)
      const data = response.data || response
      
      // meetingDate 파싱
      // 백엔드 응답: meetingDate는 날짜만 ("2026-02-18"), startTime/endTime은 별도
      let parsedStartDate = data.meetingDate || null
      
      // participants의 프로필 이미지 URL 처리
      const processedParticipants = (data.info || []).map(participant => ({
        ...participant,
        profileImgUrl: processProfileImageUrl(
          participant.profileImgUrl,
          !!participant.provider // provider 존재 여부
        )
      }))
      
      const meetingInfo = {
        id: data.id,
        shareCode: shareCode,
        name: data.name,
        memberNumber: data.memberNumber,
        participants: processedParticipants,
        startDate: parsedStartDate,
        meetingDate: data.meetingDate,
        startTime: data.startTime,
        endTime: data.endTime,
        role: data.role,
        confirmDate: data.confirmDate,
        version: data.version
      }
      // 스토어에 저장
      setCurrentMeeting(meetingInfo)
      
      return meetingInfo
    } catch (error) {
      console.error('❌ [Cache] API 호출 실패:', error)
      throw error
    }
  }

  /**
   * 현재 미팅 정보 초기화
   */
  const clearCurrentMeeting = () => {
    currentMeeting.value = null
  }

  return {
    meetings,
    currentMeeting,
    sortBy,
    isLoading,
    setMeetings,
    addMeeting,
    setCurrentMeeting,
    setSortBy,
    getSortedMeetings,
    loadMeetingByShareCode,
    clearCurrentMeeting
  }
})

