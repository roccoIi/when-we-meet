import { defineStore } from 'pinia'
import { ref } from 'vue'

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

  return {
    meetings,
    currentMeeting,
    sortBy,
    isLoading,
    setMeetings,
    addMeeting,
    setCurrentMeeting,
    setSortBy,
    getSortedMeetings
  }
})

