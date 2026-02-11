# 언제볼래(When We Meet) - 기능 명세서

## 📋 프로젝트 개요

**프로젝트명**: 언제볼래 (When We Meet)  
**목적**: 그룹 모임의 날짜 및 시간을 효율적으로 조율하는 일정 조율 플랫폼  
**핵심 가치**: 
- 복잡한 메신저 대화 없이 시각적으로 가능한 날짜 선택
- 참여자들의 가능한 시간을 자동으로 분석하여 최적의 날짜 추천
- 링크 공유만으로 간편한 참여

---

## 🔑 핵심 기능 개요

### 1. 사용자 인증
- 소셜 로그인 (카카오, 구글)
- RefreshToken 기반 자동 로그인 유지
- 닉네임 설정 (필수)

### 2. 모임 관리
- 모임 생성 (이름, 시작 시점 설정)
- 모임 목록 조회 (정렬, 무한스크롤)
- 모임 상세 정보 조회
- 초대 링크를 통한 모임 참여

### 3. 일정 조율
- 개인의 불가능한 날짜/시간 입력
- 전체 참여자의 가용성 시각화
- AI 기반 최적 시간 추천
- 모임 시간 확정

### 4. 공유 기능
- 초대 링크 자동 생성
- 클립보드 복사

---

## 📱 페이지별 기능 상세

### 1. 로그인 페이지 (`/login`)

#### 기능
1. **소셜 로그인 제공**
   - 카카오 OAuth 로그인
   - 구글 OAuth 로그인
   
2. **인증 프로세스**
   - OAuth 인증 URL로 리다이렉트
   - 콜백 처리 후 토큰 발급
   - 로그인 성공 시 메인 페이지로 이동

#### API 연동
- `GET /oauth/kakao` - 카카오 로그인 페이지로 이동
- `GET /oauth/google` - 구글 로그인 페이지로 이동

#### 상태 관리
- `isLoading`: 로그인 처리 중 상태

---

### 2. OAuth 콜백 페이지 (`/oauth/callback`)

#### 기능
1. **OAuth 인증 완료 처리**
   - URL에서 authorization code 추출
   - 백엔드로 code 전송하여 토큰 발급
   - 사용자 정보 조회 후 스토어에 저장

2. **자동 리다이렉트**
   - 로그인 성공 시 메인 페이지로 이동
   - 실패 시 로그인 페이지로 복귀

#### API 연동
- OAuth callback 처리 (백엔드에서 자동 처리)
- `GET /api/users/info` - 사용자 정보 조회

---

### 3. 모임 목록 페이지 (`/`)

#### 기능
1. **모임 목록 조회**
   - 사용자가 참여한 모든 모임 표시
   - 페이지네이션 (무한 스크롤)
   - 정렬 옵션: 이름, 참여날짜, D-day
   - 정렬 순서: 오름차순/내림차순

2. **모임 정보 표시**
   - 모임 이름
   - 참여 인원 수
   - D-day 계산 및 표시
   - 확정된 모임 날짜 (있는 경우)

3. **모임 선택**
   - 모임 클릭 시 상세 페이지로 이동
   - shareCode를 URL 파라미터로 전달

4. **새 모임 생성**
   - "새 모임" 버튼 클릭 시 생성 페이지로 이동

#### API 연동
- `GET /api/meetings?page={page}&limit={limit}&type={type}&sort={sort}`
  - Query Parameters:
    - `page`: 현재 페이지 (기본값: 1)
    - `limit`: 페이지당 항목 수 (기본값: 10)
    - `type`: 정렬 기준 (NAME, JOIN_DATE, MEETING_DATE)
    - `sort`: 정렬 순서 (ASC, DESC)
  - Response:
    ```json
    {
      "data": [
        {
          "id": 1,
          "name": "친구들 모임",
          "shareCode": "ABC123",
          "memberNumber": 5,
          "meetingDate": "2026-02-20T19:00:00"
        }
      ],
      "pagination": {
        "currentPage": 1,
        "totalPages": 3,
        "totalItems": 25,
        "hasMore": true
      }
    }
    ```

#### 상태 관리
- `isLoading`: 초기 로딩 상태
- `isLoadingMore`: 추가 데이터 로딩 상태
- `currentPage`: 현재 페이지 번호
- `hasMore`: 더 불러올 데이터 존재 여부
- `sortType`: 현재 정렬 기준
- `sortOrder`: 현재 정렬 순서
- `meetings`: 모임 목록 배열 (Pinia store)

#### 비즈니스 로직
1. **D-day 계산**
   ```javascript
   const today = new Date();
   const meetingDate = new Date(meeting.meetingDate);
   const diffDays = Math.ceil((meetingDate - today) / (1000 * 60 * 60 * 24));
   // D-0 (당일), D-5 (5일 전), D+3 (3일 지남)
   ```

2. **무한 스크롤**
   - IntersectionObserver를 사용하여 하단 감지
   - 하단 100px 전에 미리 다음 페이지 로드
   - `hasMore`가 false일 때 로딩 중단

---

### 4. 모임 생성 페이지 (`/create`)

#### 기능
1. **모임 정보 입력**
   - 모임 이름 입력 (최대 30자)
   - 실시간 글자 수 표시

2. **시작 시점 선택**
   - 달력에서 날짜 선택
   - 시간 선택 (30분 단위)
   - 과거 날짜 선택 불가
   - 오늘 선택 시 현재 시간 이후만 선택 가능

3. **선택 정보 미리보기**
   - 선택한 날짜/시간을 "YYYY년 MM월 DD일 HH:mm" 형식으로 표시

4. **유효성 검증**
   - 모임 이름 필수 입력
   - 30자 초과 방지
   - 과거 시간 선택 방지

5. **모임 생성 완료**
   - 생성 성공 시 shareCode 반환
   - 해당 모임 상세 페이지로 자동 이동

#### API 연동
- `POST /api/meetings`
  - Request:
    ```json
    {
      "meetingName": "친구들 모임",
      "startDate": "2026-02-20",
      "startTime": "19:00:00"
    }
    ```
  - Response:
    ```json
    {
      "code": "M001",
      "message": "모임 생성 성공",
      "data": {
        "shareCode": "ABC123"
      }
    }
    ```

#### 상태 관리
- `meetingName`: 모임 이름
- `selectedDate`: 선택된 날짜
- `selectedHour`: 선택된 시 (0-23)
- `selectedMinute`: 선택된 분 (0, 30)
- `currentYear`: 달력 표시 년도
- `currentMonth`: 달력 표시 월 (0-11)
- `isLoading`: 생성 요청 중 상태

#### 비즈니스 로직
1. **시간 유효성 검사**
   ```javascript
   const isToday = selectedDate가 오늘인지 확인
   if (isToday) {
     const currentHour = 현재 시간
     const currentMinute = 현재 분
     // 현재 시간 이후만 선택 가능하도록 필터링
   }
   ```

2. **달력 생성**
   ```javascript
   // 해당 월의 첫날과 마지막 날 계산
   // 첫날의 요일만큼 빈 칸 추가
   // 1일부터 마지막날까지 날짜 생성
   ```

---

### 5. 모임 초대 페이지 (`/invite/:shareCode`)

#### 기능
1. **모임 정보 조회**
   - shareCode로 모임 정보 가져오기
   - 모임 이름, 참여 인원 표시

2. **모임 참여**
   - "참여하기" 버튼 클릭 시 모임 참여 API 호출
   - 참여 성공 시 해당 모임 상세 페이지로 이동
   - 이미 참여중인 경우 바로 상세 페이지로 이동

3. **에러 처리**
   - 존재하지 않는 shareCode: 에러 메시지 표시
   - 이미 참여중: 안내 후 상세 페이지로 이동
   - 인증 필요 시: 로그인 페이지로 리다이렉트

4. **참여중인 모임 목록으로 이동**
   - 서브 버튼으로 메인 페이지 이동 제공

#### API 연동
- `GET /api/meetings/share/{shareCode}`
  - Response:
    ```json
    {
      "code": "M001",
      "data": {
        "name": "친구들 모임",
        "participantCount": 5
      }
    }
    ```

- `POST /api/meetings/join/{shareCode}`
  - Response:
    ```json
    {
      "code": "M001",
      "message": "모임 참여 성공"
    }
    ```
  - Error Codes:
    - `M003`: 존재하지 않는 모임
    - `M004`: 이미 참여중인 모임

#### 상태 관리
- `meetingInfo`: 모임 정보 객체
- `isLoading`: 모임 정보 로딩 상태
- `isJoining`: 참여 요청 중 상태
- `errorMessage`: 에러 메시지

---

### 6. 모임 상세 페이지 (`/meeting/:shareCode`)

#### 기능
1. **모임 정보 표시**
   - 모임 이름
   - 참여 인원 수
   - 참여자 프로필 이미지 및 닉네임

2. **월별 가용성 달력**
   - 각 날짜별 참여 가능 인원 수 시각화
   - 색상 코딩으로 가용성 표시:
     - 전체 인원의 80% 이상 가능: 높은 가용성
     - 50-80% 가능: 보통 가용성
     - 50% 미만 가능: 낮은 가용성
   - 확정된 날짜는 특별 표시
   - 월 변경 시 해당 월 데이터 다시 로드

3. **일정 입력 기능**
   - "내 일정 추가하기" 버튼
   - 일정 입력 페이지로 이동

4. **확정된 일정 관리**
   - 확정된 날짜/시간 표시
   - 일정 초기화 기능 (null로 설정)

5. **추천 일정**
   - 가장 많은 사람이 가능한 시간대 추천
   - 필터링: 전체, 주중, 주말
   - 순위별로 최대 5개 표시
   - 각 추천 일정에서 "일정 선택" 버튼
   - 선택 시 모임 일정 확정

6. **공유 기능**
   - 공유 버튼 클릭 시 초대 링크 생성
   - 자동으로 클립보드에 복사
   - 공유 모달 표시

7. **닉네임 변경**
   - 닉네임 변경 버튼 클릭 시 모달 표시
   - 새 닉네임 입력 후 저장

#### API 연동
- `GET /api/meetings/share/{shareCode}/detail`
  - Response:
    ```json
    {
      "code": "M001",
      "data": {
        "name": "친구들 모임",
        "memberNumber": 5,
        "meetingDate": "2026-02-20T19:00:00",
        "info": [
          {
            "nickname": "홍길동",
            "profileImgUrl": "https://..."
          }
        ]
      }
    }
    ```

- `GET /api/schedules/share/{shareCode}/monthly?year={year}&month={month}`
  - Response:
    ```json
    {
      "code": "S001",
      "data": {
        "totalMembers": 5,
        "MembersScheduleByDate": [
          {
            "date": "2026-02-20",
            "availableCount": 4,
            "unAvailableMembers": ["홍길동"]
          }
        ]
      }
    }
    ```

- `GET /api/schedules/share/{shareCode}/recommend?type={type}`
  - Query Parameters:
    - `type`: ALL, WEEKDAY, WEEKEND
  - Response:
    ```json
    {
      "code": "S001",
      "data": [
        {
          "day": "2026-02-20",
          "startTime": "19:00:00",
          "endTime": "21:00:00"
        }
      ]
    }
    ```

- `PUT /api/meetings/share/{shareCode}`
  - Request:
    ```json
    {
      "name": null,
      "meetingDate": "2026-02-20T19:00:00"
    }
    ```
  - 일정 초기화 시:
    ```json
    {
      "name": null,
      "meetingDate": null
    }
    ```

#### 상태 관리
- `meeting`: 모임 정보 객체
- `currentYear`: 달력 표시 년도
- `currentMonth`: 달력 표시 월
- `monthlyAvailability`: 월별 가용성 데이터
- `recommendedSchedules`: 추천 일정 배열
- `recommendType`: 추천 필터 타입 (ALL/WEEKDAY/WEEKEND)
- `confirmedSchedule`: 확정된 일정 객체
- `isUpdatingSchedule`: 일정 업데이트 중 상태
- `isShareModalOpen`: 공유 모달 표시 여부
- `shareUrl`: 공유 링크

#### 비즈니스 로직
1. **가용성 계산**
   ```javascript
   const totalMembers = 5;
   const availableCount = 4;
   const availability = availableCount / totalMembers; // 0.8 (80%)
   
   // 색상 결정
   if (availability >= 0.8) return 'high'; // 초록
   if (availability >= 0.5) return 'medium'; // 노랑
   return 'low'; // 빨강
   ```

2. **날짜 형식 변환**
   ```javascript
   // LocalDateTime "2026-02-15T14:00:00" -> 표시용
   const [datePart, timePart] = dateTimeString.split('T');
   // datePart: "2026-02-15"
   // timePart: "14:00:00"
   ```

3. **추천 순위 결정**
   - 백엔드에서 정렬된 순서대로 받음
   - 순서대로 1위, 2위, 3위... 할당

---

### 7. 일정 입력 페이지 (`/meeting/:shareCode/schedule`)

#### 기능
1. **입력 모드 전환**
   - 날짜 모드: 하루 전체를 불가능한 날짜로 선택
   - 시간 모드: 30분 단위로 세밀하게 불가능한 시간 선택
   - 버튼 클릭으로 모드 전환

2. **날짜 모드**
   - 달력에서 날짜 클릭으로 선택/해제
   - 다중 선택 가능
   - 선택된 날짜는 배열로 관리

3. **시간 모드**
   - 날짜별로 시간 슬롯 표시
   - 09:00 ~ 23:30 범위
   - 30분 단위 슬롯
   - 클릭으로 선택/해제
   - 연속된 시간은 자동으로 그룹핑하여 표시

4. **선택 현황 표시**
   - 선택된 날짜/시간 개수 표시
   - 칩 형태로 선택 항목 나열
   - 각 항목에서 X 버튼으로 개별 제거
   - 시간 모드는 범위로 그룹핑 (예: "11일 20:00 ~ 21:30")

5. **기존 일정 불러오기**
   - 페이지 진입 시 저장된 일정 자동 로드
   - 백엔드 형식을 UI 선택 상태로 변환

6. **일정 저장**
   - UI 선택 상태를 백엔드 형식으로 변환
   - 날짜 모드: 09:00:00 ~ 23:59:59로 저장
   - 시간 모드: 연속된 시간을 범위로 그룹핑하여 저장
   - 저장 성공 시 모임 상세 페이지로 복귀

#### API 연동
- `GET /api/schedules/share/{shareCode}/user`
  - Response:
    ```json
    {
      "code": "S001",
      "data": [
        {
          "unavailableDate": "2026-02-25",
          "unavailableStartTime": "09:00:00",
          "unavailableEndTime": "23:59:59"
        },
        {
          "unavailableDate": "2026-02-26",
          "unavailableStartTime": "14:00:00",
          "unavailableEndTime": "16:30:00"
        }
      ]
    }
    ```

- `POST /api/schedules/share/{shareCode}`
  - Request:
    ```json
    {
      "schedules": [
        {
          "unavailableDate": "2026-02-25",
          "unavailableStartTime": "09:00:00",
          "unavailableEndTime": "23:59:59"
        }
      ]
    }
    ```

#### 상태 관리
- `viewMode`: 현재 모드 ('date' 또는 'time')
- `selectedDates`: 선택된 날짜 배열 (날짜 모드)
- `selectedTimes`: 선택된 시간 배열 (시간 모드)
- `currentYear`: 달력 표시 년도
- `currentMonth`: 달력 표시 월
- `isLoading`: 기존 일정 로딩 상태
- `isSaving`: 저장 요청 중 상태

#### 비즈니스 로직
1. **백엔드 형식 -> UI 선택 변환**
   ```javascript
   // 날짜 모드 판별: 09:00:00 ~ 23:59:59인 경우
   if (startTime === "09:00:00" && endTime === "23:59:59") {
     selectedDates.push(date);
   }
   
   // 시간 모드: 30분 단위로 분할
   const current = new Date(startDateTime);
   while (current < endDateTime) {
     selectedTimes.push(formatToISO(current)); // "2026-02-15T14:00"
     current.setMinutes(current.getMinutes() + 30);
   }
   ```

2. **UI 선택 -> 백엔드 형식 변환**
   ```javascript
   // 날짜 모드: 하루 전체 범위로
   dates.forEach(date => {
     schedules.push({
       unavailableDate: date,
       unavailableStartTime: "09:00:00",
       unavailableEndTime: "23:59:59"
     });
   });
   
   // 시간 모드: 연속된 시간 그룹핑
   const sortedTimes = selectedTimes.sort();
   let rangeStart = sortedTimes[0];
   let rangeEnd = sortedTimes[0];
   
   for (let i = 1; i < sortedTimes.length; i++) {
     const timeDiff = (current - previous) / (1000 * 60); // 분
     if (timeDiff === 30) {
       rangeEnd = sortedTimes[i]; // 연속이면 확장
     } else {
       schedules.push(convertToRange(rangeStart, rangeEnd)); // 저장
       rangeStart = sortedTimes[i]; // 새 범위 시작
     }
   }
   ```

3. **시간 범위 변환**
   ```javascript
   // "2026-02-15T14:00" -> 백엔드 형식
   {
     unavailableDate: "2026-02-15",
     unavailableStartTime: "14:00:00",
     unavailableEndTime: "15:30:00" // 마지막 선택 + 30분
   }
   
   // 23:30 예외 처리: 23:59:59로 변환
   ```

4. **연속 시간 그룹핑 (표시용)**
   ```javascript
   // ["2026-02-15T14:00", "2026-02-15T14:30", "2026-02-15T15:00"]
   // -> "15일 14:00 ~ 15:30"
   ```

---

## 🔄 공통 컴포넌트

### 1. AppHeader

#### 기능
- 뒤로가기 버튼 (필요시 표시)
- 앱 로고/타이틀 표시
- 사용자 프로필 메뉴
  - 프로필 이미지
  - 닉네임
  - 드롭다운 메뉴
  - 로그아웃 기능

#### 상태 관리
- `userStore.nickname`: 사용자 닉네임
- `userStore.profileImgUrl`: 프로필 이미지 URL

---

### 2. Calendar

#### Props
- `year`: 표시할 년도
- `month`: 표시할 월 (1-12)
- `unavailableDates`: 선택 불가능한 날짜 배열 (과거 날짜 등)
- `selectedDates`: 선택된 날짜 배열 (일정 입력용)
- `monthlyAvailability`: 월별 가용성 데이터 (모임 상세용)
- `confirmedDate`: 확정된 날짜 (모임 상세용)

#### Events
- `@update:year`: 년도 변경 이벤트
- `@update:month`: 월 변경 이벤트
- `@dateClick`: 날짜 클릭 이벤트

#### 기능
1. **달력 렌더링**
   - 해당 년/월의 달력 생성
   - 이전 달 빈 칸 처리
   - 날짜 클릭 이벤트 발생

2. **날짜 상태 표시**
   - 선택 불가능 (과거 날짜)
   - 선택됨 (일정 입력)
   - 가용성 수준 (모임 상세)
   - 확정됨 (모임 상세)

3. **월 변경**
   - 이전/다음 달 버튼
   - 월 변경 시 이벤트 발생

---

### 3. TimeCalendar

#### Props
- `selectedTimes`: 선택된 시간 배열 (ISO 8601 형식)

#### Events
- `@timeClick`: 시간 슬롯 클릭 이벤트

#### 기능
1. **시간 슬롯 표시**
   - 날짜별 그룹핑
   - 09:00 ~ 23:30 범위
   - 30분 단위 슬롯

2. **선택 상태 표시**
   - 선택된 시간 하이라이트
   - 클릭으로 토글

---

### 4. NicknameModal

#### Events
- `@close`: 모달 닫기 이벤트

#### 기능
1. **닉네임 입력**
   - 텍스트 입력 필드
   - 유효성 검증 (필수, 길이 제한)

2. **닉네임 저장**
   - API 호출하여 닉네임 업데이트
   - 성공 시 스토어 업데이트
   - 모달 닫기

#### API 연동
- `PUT /api/users/nickname`
  - Request:
    ```json
    {
      "nickname": "새닉네임"
    }
    ```

---

### 5. ShareModal

#### Props
- `isOpen`: 모달 표시 여부
- `shareUrl`: 공유할 URL
- `meetingName`: 모임 이름

#### Events
- `@close`: 모달 닫기 이벤트

#### 기능
1. **URL 표시**
   - 생성된 초대 링크 표시
   - 이미 클립보드에 복사됨

2. **복사 상태 피드백**
   - 복사 완료 메시지 표시

---

### 6. MeetingCard

#### Props
- `meeting`: 모임 객체
  ```javascript
  {
    id: 1,
    name: "친구들 모임",
    shareCode: "ABC123",
    memberNumber: 5,
    meetingDate: "2026-02-20T19:00:00"
  }
  ```

#### 기능
1. **모임 정보 표시**
   - 모임 이름
   - 참여 인원
   - D-day 계산 및 표시
   - 확정 날짜 (있는 경우)

2. **클릭 이벤트**
   - 카드 클릭 시 모임 상세로 이동

---

## 🔐 인증 및 권한

### 인증 방식
- **OAuth 2.0** 소셜 로그인
- **JWT Access Token**: API 요청에 사용 (짧은 만료 시간)
- **Refresh Token**: Access Token 자동 갱신 (긴 만료 시간, HttpOnly Cookie)

### 토큰 관리
1. **자동 로그인**
   - 앱 마운트 시 refreshToken으로 accessToken 재발급 시도
   - 성공 시 사용자 정보 로드
   - 실패 시 로그인 필요 상태 유지

2. **토큰 갱신**
   - API 요청 시 401 에러 발생 시
   - Axios interceptor에서 자동으로 토큰 재발급 시도
   - 재발급 성공 시 원래 요청 재시도
   - 재발급 실패 시 로그인 페이지로 이동

### API 인증
- 모든 API 요청 시 Authorization 헤더에 JWT 포함
- `Authorization: Bearer {accessToken}`

---

## 📊 데이터 모델

### User
```javascript
{
  nickname: String,        // 사용자 닉네임
  profileImgUrl: String,   // 프로필 이미지 URL
  provider: String         // OAuth 제공자 (KAKAO, GOOGLE)
}
```

### Meeting
```javascript
{
  id: Number,              // 모임 ID
  name: String,            // 모임 이름
  shareCode: String,       // 공유 코드 (고유)
  memberNumber: Number,    // 참여 인원 수
  meetingDate: String,     // 확정된 날짜/시간 (ISO 8601, nullable)
  info: Array              // 참여자 정보 배열
}
```

### Schedule (백엔드 형식)
```javascript
{
  unavailableDate: String,      // "2026-02-25" (LocalDate)
  unavailableStartTime: String, // "14:00:00" (LocalTime)
  unavailableEndTime: String    // "16:30:00" (LocalTime)
}
```

### MonthlyAvailability
```javascript
{
  totalMembers: Number,        // 전체 참여자 수
  MembersScheduleByDate: [     // 날짜별 가용성
    {
      date: String,            // "2026-02-20"
      availableCount: Number,  // 가능한 인원 수
      unAvailableMembers: [String] // 불가능한 멤버 닉네임 배열
    }
  ]
}
```

### RecommendedSchedule
```javascript
{
  day: String,        // "2026-02-20" (LocalDate)
  startTime: String,  // "19:00:00" (LocalTime)
  endTime: String     // "21:00:00" (LocalTime)
}
```

---

## 🔄 상태 관리 (Pinia Store)

### User Store (`stores/user.js`)

#### State
```javascript
{
  nickname: '',
  profileImgUrl: '',
  provider: '',
  isInitialized: false,      // 앱 초기화 완료 여부
  showNicknameModal: false
}
```

#### Getters
```javascript
{
  isLoggedIn: state => !!state.nickname // 로그인 여부
}
```

#### Actions
```javascript
{
  login(userInfo),           // 로그인 처리
  logout(),                  // 로그아웃 처리
  updateNickname(nickname),  // 닉네임 업데이트
  setInitialized(value),     // 초기화 상태 설정
  openNicknameModal(),       // 닉네임 모달 열기
  closeNicknameModal()       // 닉네임 모달 닫기
}
```

---

### Meetings Store (`stores/meetings.js`)

#### State
```javascript
{
  meetings: [],              // 모임 목록
  currentMeeting: null       // 현재 선택된 모임
}
```

#### Actions
```javascript
{
  setMeetings(meetings),            // 모임 목록 설정
  addMeetings(meetings),            // 모임 목록 추가 (무한스크롤)
  setCurrentMeeting(meeting),       // 현재 모임 설정
  clearMeetings()                   // 모임 목록 초기화
}
```

---

## 🌐 API 엔드포인트 요약

### 인증
- `GET /oauth/kakao` - 카카오 로그인
- `GET /oauth/google` - 구글 로그인
- `GET /api/users/info` - 사용자 정보 조회
- `PUT /api/users/nickname` - 닉네임 변경
- `POST /api/auth/logout` - 로그아웃

### 모임
- `GET /api/meetings` - 모임 목록 조회
- `POST /api/meetings` - 모임 생성
- `GET /api/meetings/share/{shareCode}` - 공유 코드로 모임 조회
- `GET /api/meetings/share/{shareCode}/detail` - 모임 상세 조회
- `POST /api/meetings/join/{shareCode}` - 모임 참여
- `PUT /api/meetings/share/{shareCode}` - 모임 정보 수정 (일정 확정/초기화)

### 일정
- `GET /api/schedules/share/{shareCode}/user` - 내 일정 조회
- `POST /api/schedules/share/{shareCode}` - 일정 저장
- `GET /api/schedules/share/{shareCode}/monthly` - 월별 가용성 조회
- `GET /api/schedules/share/{shareCode}/recommend` - 추천 일정 조회

---

## 🔄 사용자 플로우

### 플로우 1: 모임 생성 및 공유
```
1. 로그인 페이지에서 소셜 로그인
   ↓
2. 모임 목록 페이지 → "새 모임" 클릭
   ↓
3. 모임 생성 페이지
   - 모임 이름 입력: "친구들 모임"
   - 시작 날짜 선택: 2026-02-20
   - 시작 시간 선택: 19:00
   - "모임 만들기" 클릭
   ↓
4. API: POST /api/meetings
   - Response: { shareCode: "ABC123" }
   ↓
5. 모임 상세 페이지로 이동 (/meeting/ABC123)
   ↓
6. "공유" 버튼 클릭
   - 초대 링크 생성: https://example.com/invite/ABC123
   - 클립보드에 자동 복사
   - 공유 모달 표시
   ↓
7. 링크를 메신저 등으로 공유
```

### 플로우 2: 초대 링크로 참여 및 일정 입력
```
1. 초대 링크 클릭 (https://example.com/invite/ABC123)
   ↓
2. 모임 초대 페이지
   - API: GET /api/meetings/share/ABC123
   - 모임 이름, 참여 인원 표시
   ↓
3. 로그인 필요 시 로그인 페이지로 이동
   - 소셜 로그인 완료 후 초대 페이지로 복귀
   ↓
4. "모임 참여하기" 클릭
   - API: POST /api/meetings/join/ABC123
   ↓
5. 모임 상세 페이지로 이동
   ↓
6. "내 일정 추가하기" 클릭
   ↓
7. 일정 입력 페이지
   - 날짜 모드: 2월 25일, 26일 선택 (불가능한 날짜)
   - "시간변경" 클릭 → 시간 모드
   - 2월 27일 14:00~16:00 선택 (세밀한 시간)
   - "저장" 클릭
   ↓
8. API: POST /api/schedules/share/ABC123
   - Request: [
       { date: "2026-02-25", start: "09:00:00", end: "23:59:59" },
       { date: "2026-02-26", start: "09:00:00", end: "23:59:59" },
       { date: "2026-02-27", start: "14:00:00", end: "16:30:00" }
     ]
   ↓
9. 모임 상세 페이지로 복귀
   - 달력에서 가용성 업데이트 확인
```

### 플로우 3: 추천 일정 확인 및 확정
```
1. 모임 상세 페이지
   - 모든 참여자가 일정 입력 완료
   ↓
2. 추천 일정 섹션 확인
   - API: GET /api/schedules/share/ABC123/recommend?type=ALL
   - 1위: 2월 20일 19:00~21:00 (5명 가능)
   - 2위: 2월 22일 18:00~20:00 (4명 가능)
   ↓
3. 필터 변경 (주중/주말)
   - API: GET /api/schedules/share/ABC123/recommend?type=WEEKDAY
   - 추천 목록 업데이트
   ↓
4. 1위 추천 "일정 선택" 클릭
   - 확인 다이얼로그: "2월 20일 19:00~21:00로 확정하시겠습니까?"
   ↓
5. 확인 클릭
   - API: PUT /api/meetings/share/ABC123
   - Request: { meetingDate: "2026-02-20T19:00:00" }
   ↓
6. 확정된 일정 표시
   - "✅ 확정된 모임 일정"
   - "2월 20일 (수) ⏰ 19:00"
   - 달력에서 해당 날짜 특별 표시
```

### 플로우 4: 일정 수정 및 초기화
```
1. 모임 상세 페이지
   ↓
2. "내 일정 추가하기" 클릭
   ↓
3. 일정 입력 페이지
   - API: GET /api/schedules/share/ABC123/user
   - 기존 선택 항목 표시
   ↓
4. 기존 선택 수정
   - 날짜 추가/제거
   - 시간 추가/제거
   - "저장" 클릭
   ↓
5. API: POST /api/schedules/share/ABC123
   - 전체 일정을 새로 저장 (덮어쓰기)
   ↓
6. 모임 상세로 복귀
   - 추천 일정 자동으로 재계산
```

```
1. 모임 상세 페이지 (일정 확정된 상태)
   ↓
2. "일정 초기화" 버튼 클릭
   - 확인 다이얼로그: "확정된 일정을 초기화하시겠습니까?"
   ↓
3. 확인 클릭
   - API: PUT /api/meetings/share/ABC123
   - Request: { meetingDate: null }
   ↓
4. 확정 일정 제거
   - "확정된 날짜가 존재하지 않습니다" 표시
   - 다시 추천 일정으로 선택 가능
```

---

## ⚡ 성능 최적화

### 1. 무한 스크롤
- IntersectionObserver 사용
- 하단 100px 전에 미리 로드
- 중복 요청 방지 (isLoadingMore 플래그)

### 2. API 캐싱
- 월별 가용성 데이터는 currentYear, currentMonth 변경 시에만 재로드
- 추천 일정은 recommendType 변경 시에만 재로드

### 3. 달력 렌더링 최적화
- Computed property 사용하여 필요 시에만 재계산
- v-for key 사용으로 불필요한 재렌더링 방지

---

## 🐛 에러 처리

### API 에러 코드
- `M001`: 성공
- `M003`: 존재하지 않는 모임
- `M004`: 이미 참여중인 모임
- `S001`: 일정 관련 성공
- `401`: 인증 필요
- `403`: 권한 없음
- `500`: 서버 에러

### 에러 처리 전략
1. **네트워크 에러**: "네트워크 연결을 확인해주세요"
2. **인증 에러**: 로그인 페이지로 리다이렉트
3. **권한 에러**: "접근 권한이 없습니다"
4. **비즈니스 로직 에러**: 백엔드 메시지 그대로 표시
5. **알 수 없는 에러**: "오류가 발생했습니다. 다시 시도해주세요"

---

## 🔄 앱 초기화 프로세스

```javascript
// App.vue onMounted
1. 라우터 준비 대기
   ↓
2. 현재 페이지가 인증 필요 없는 페이지인지 확인
   - Login, OAuthCallback → 토큰 재발급 스킵
   ↓
3. 인증 필요한 페이지:
   - API: GET /api/users/info
   - 성공: userStore.login() 호출
   - 실패: 로그인 안 한 상태 유지
   ↓
4. userStore.setInitialized(true)
   ↓
5. 각 페이지 onMounted:
   - userStore.isInitialized가 true가 될 때까지 대기
   - 최대 5초 타임아웃
   - 초기화 완료 후 페이지별 데이터 로드
```

---

## 🎯 핵심 비즈니스 규칙

### 모임 생성
- 모임 이름: 필수, 1~30자
- 시작 시점: 현재 시간 이후만 가능

### 일정 입력
- 날짜 모드: 하루 전체 (09:00:00 ~ 23:59:59)
- 시간 모드: 30분 단위, 09:00 ~ 23:30 범위
- 저장 시 전체 일정을 새로 저장 (덮어쓰기)

### 가용성 계산
- 전체 참여자 대비 불가능 표시 안 한 인원의 비율
- 80% 이상: 높은 가용성
- 50-80%: 보통 가용성
- 50% 미만: 낮은 가용성

### 추천 일정
- 가장 많은 사람이 가능한 시간대 우선
- 필터: 전체, 주중(월~금), 주말(토~일)
- 최대 5개까지 표시

### 일정 확정
- 추천 일정 중 하나 선택
- 또는 직접 날짜/시간 입력
- 확정 후 초기화 가능 (null 설정)

---

## 📝 추가 구현 필요 사항

이 명세서에 포함되지 않았지만 실제 구현 시 고려해야 할 사항:

1. **알림 기능**
   - 모임 일정 확정 시 참여자들에게 알림
   - 새 참여자 입장 알림

2. **모임 관리**
   - 모임 삭제
   - 모임에서 나가기
   - 모임 이름 수정

3. **참여자 관리**
   - 특정 참여자 강제 퇴장 (방장 권한)
   - 방장 위임

4. **일정 분석**
   - 참여자별 입력 현황 표시
   - 아직 입력 안 한 사람 목록

5. **오프라인 지원**
   - 네트워크 끊김 시 로컬 저장
   - 재연결 시 자동 동기화

6. **다크 모드**
   - 테마 전환 기능

---

이 명세서는 프로젝트의 현재 구현된 기능을 기반으로 작성되었습니다. 새로운 기능 추가나 변경 시 이 문서를 업데이트하세요.
