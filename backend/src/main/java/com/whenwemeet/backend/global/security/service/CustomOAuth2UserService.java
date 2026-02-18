package com.whenwemeet.backend.global.security.service;

import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.entity.UserType;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import com.whenwemeet.backend.global.security.dto.OAuth2Response;
import com.whenwemeet.backend.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Value("${spring.jwt.name.refresh-token}")
    private String REFRESH_TOKEN_NAME;
    private final UserRepository userRepository;
    private final UserMeetingRoomRepository userMeetingRoomRepository;
    private final UnavailableRepository unavailableRepository;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser (OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 로그인한 유저 정보
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Oauth 로그인 서비스 제공한 기업명 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 유저정보에 대한 DTO를 생성한다.
        OAuth2Response oAuth2Response = OAuth2Response.of(registrationId, oAuth2User.getAttributes());

        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        // 4. 회원가입 및 로그인
        User user = getUser(request, oAuth2Response);

        // 5. Security context에 저장할 객체 생성
        return new CustomOAuth2User(user);
    }


    /**
     * 서비스기업에서 제공한 유저정보의 provider, provider_id 기반으로 조회를 진행합니다.
     * 1) 만일 DB에 존재하는 사용자라면 해당 사용자의 정보 반환
     * 2) 만일 DB에 존재하지 않는다면 회원가입(DB Insert) 진행 후 반환
     * @param oAuth2Response OAuth 업체 측에서 제공한 사용자 정보
     * @return 현재 사용자 객체
     */
    @Transactional
    public User getUser(HttpServletRequest request, OAuth2Response oAuth2Response) {

        /**
         * 1) 쿠키 존재?
         *    ├─ i) NO → 일반 OAuth 로그인
         *    │          ├─ a) 계정 없음 → 신규 생성
         *    │          └─ b) 계정 있음 → 로그인
         *    │
         *    └─ ii) YES → guest 존재?
         *             ├─ a) NO → 일반 로그인 처리
         *             │
         *             └─ b) YES → OAuth 존재?
         *                        ├─ (1) NO → guest 업그레이드
         *                        └─ (2) YES → 병합
         */
        // 1) 기본정보 유무 확인 (쿠키와 Oauth유저 정보)
        String cookie = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);
        User oauthUser = userRepository.findUserByProviderAndProviderID(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                .orElse(null);

        // i) 쿠키가 없다
        if(cookie == null){
            return handleUserLogin(oauthUser, oAuth2Response);
        }

        // ii) 쿠키가 있다. -> 그럼 해당 쿠키의 정보를 가진 게스트 유저가 있는가?
        Long userId = jwtUtil.getUserId(cookie);
        User guestUser = userRepository.findById(userId) // 동일한 아이디가 있어도 그게 진짜 guest일때만 guestUser, 아니면 null
                .filter(user -> user.getRole() == UserType.GUEST)
                .orElse(null);

        // a) 게스트 정보를 가진 유저가 존재하지 않는다.
        if(guestUser == null){
            return handleUserLogin(oauthUser, oAuth2Response);
        }

        // b) 게스트 로그인 기록이 존재한다. 그럼 기존에 oauth로그인 기록이 존재하는가?
        // (1) 존재하지 않는다. 그럼 기존 guest유저 정보를 oauth유저 정보로 승격한다.
        if(oauthUser == null){
            guestUser.updateNewUser(oAuth2Response.toEntity());
            return userRepository.save(guestUser);
        }

        // (2) 존재한다. 기존 게스트 유저의 사용기록을 oauth유저로 이관한다.

        // 참여중인 미팅룸의 ID 조회 (GuestUser, User)
        HashSet<UserMeetingRoom> guestUserMeetingRoomIdSet = userMeetingRoomRepository.findAllByUser(guestUser);
        HashSet<Long> userMeetingRoomIdSet = userMeetingRoomRepository.findMeetingRoomIdsByUser(oauthUser);

        // 두 Set의 차집합 필터링 이후 해당 값에 대한 유저 변경
        List<Long> removeIdList = new ArrayList<>();
        guestUserMeetingRoomIdSet.forEach(umr -> {
            if (userMeetingRoomIdSet.contains(umr.getMeetingRoom().getId())) {
                // 여기선 겹치는 존재이기 때문에 guest데이터를 삭제해야한다. (한번에 삭제해야하니깐 그 리스트를 담아두는게 나을듯)
                removeIdList.add(umr.getId());
            } else {
                //  여기선 겹치지 않기 때문에 userMeetingRoom 의 User를 변경해줘야 한다.
                umr.changeUser(oauthUser);
            }
        });
        userMeetingRoomRepository.saveAll(guestUserMeetingRoomIdSet);
        userMeetingRoomRepository.deleteAllById(removeIdList);

        // 방별로 등록한 불가능일정 모두 수정 (Unavailable)
        List<UnavailableTime> unavailableList = unavailableRepository.findAllByUser(guestUser);
        unavailableList.forEach(ut -> ut.changeUser(oauthUser));
        unavailableRepository.saveAll(unavailableList);

        // 기존 Oauth 유저 반환
        return oauthUser;
    }

    private User handleUserLogin(User oauthUser, OAuth2Response oAuth2Response){
        // Oauth로그인 이력은 있는가?
        // 로그인 기록이 있다면 해당 정보를 반환한다.
        if(oauthUser != null){
            return oauthUser;
        }

        // 만약 없다면 새롭게 유저 정보를 생성하여 저장하고 반환한다.
        User newUser = oAuth2Response.toEntity();
        return userRepository.save(newUser);
    }
}
