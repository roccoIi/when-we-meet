import { defineStore } from 'pinia'
import { ref } from 'vue'
import { meetingAPI } from '../services'

export const useMeetingsStore = defineStore('meetings', () => {
  // 상태
  const meetings = ref([])
  const currentMeeting = ref(null)
  const sortBy = ref('dday') // 'name', 'dday', 'participants'
  const isLoading = ref(false)

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
   * shareCode로 미팅 정보 로드 (캐싱 지원)
   * - 이미 로드된 미팅이고 shareCode가 같으면 캐시된 데이터 반환
   * - 다른 미팅이거나 캐시가 없으면 API 호출
   */
  const loadMeetingByShareCode = async (shareCode) => {
    console.log('🔄 [MeetingsStore] loadMeetingByShareCode 호출:', shareCode)
    
    // 이미 같은 미팅이 로드되어 있으면 캐시 사용
    if (currentMeeting.value && currentMeeting.value.shareCode === shareCode) {
      console.log('✅ [MeetingsStore] 캐시된 미팅 정보 사용:', currentMeeting.value.name)
      return currentMeeting.value
    }
    
    // 새로운 미팅이면 API 호출
    console.log('🌐 [MeetingsStore] API 호출:', shareCode)
    try {
      const response = await meetingAPI.getMeetingDetailByShareCode(shareCode)
      const data = response.data || response
      
      console.log('📦 [MeetingsStore] API 응답:', data)
      
      // meetingDate 파싱
      let parsedStartDate = null
      if (data.meetingDate) {
        const [datePart] = String(data.meetingDate).split('T')
        parsedStartDate = datePart
      }
      
      const meetingInfo = {
        shareCode: shareCode,
        name: data.name,
        memberNumber: data.memberNumber,
        participants: data.info || [],
        startDate: parsedStartDate,
        meetingDate: data.meetingDate,
        role: data.role,
        confirmDate: data.confirmDate
      }
      
      // 스토어에 저장
      setCurrentMeeting(meetingInfo)
      console.log('✅ [MeetingsStore] 미팅 정보 저장 완료:', meetingInfo.name)
      
      return meetingInfo
    } catch (error) {
      console.error('❌ [MeetingsStore] API 호출 실패:', error)
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

