package com.whenwemeet.backend.global.security.service;

import com.whenwemeet.backend.domain.meetingRoom.entity.UserMeetingRoom;
import com.whenwemeet.backend.domain.meetingRoom.repository.UserMeetingRoomRepository;
import com.whenwemeet.backend.domain.schedule.entity.UnavailableTime;
import com.whenwemeet.backend.domain.schedule.repository.UnavailableRepository;
import com.whenwemeet.backend.global.exception.ErrorCode;
import com.whenwemeet.backend.global.exception.type.NotFoundException;
import com.whenwemeet.backend.global.exception.type.UnAuthorizedException;
import com.whenwemeet.backend.global.jwt.util.JwtUtil;
import com.whenwemeet.backend.global.security.dto.OAuth2Response;
import com.whenwemeet.backend.domain.user.entity.User;
import com.whenwemeet.backend.domain.user.repository.UserRepository;
import com.whenwemeet.backend.global.security.dto.CustomOAuth2User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
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
    public User getUser(HttpServletRequest request, OAuth2Response oAuth2Response){

        // 1) RefreshTokendl 들어있을 쿠키를 확인합니다.
        log.info("Oauth로그인 중 중복계정 확인을 위해 쿠키 확인시작");
        String cookie = jwtUtil.tokenByCookie(request, REFRESH_TOKEN_NAME);
        if(cookie == null){
            log.info("[쿠키 미존재] 즉시 로그인 유저로 판단 Oauth로그인 및 회원가입 진행");
            return userRepository.findUserByProviderAndProviderID(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                .orElseGet(() -> {
                    User newUser = oAuth2Response.toEntity();
                    return userRepository.save(newUser);
                });
        }

        // 2) 쿠키에서 UserId 추출
        log.info("2) 쿠키에서 UserId 추출");
        Long userId = jwtUtil.getUserId(cookie);

        log.info("3) 해당 UserId를 사용중이었던 User객체 추출 (게스트)");
        // 3) 해당 UserId를 사용중이었던 User객체 추출 (게스트)
        User guestUser = userRepository.findUserById(userId)
                .orElse(null);
        Long tempId = guestUser == null ? null : guestUser.getId();
        log.info("tempId = {}", tempId);
        log.info("4) Oauth로그인한 정보가 이미 회원에 등록되어있는지 확인 (만약 없다면 Null)");

        // 4) Oauth로그인한 정보가 이미 회원에 등록되어있는지 확인 (만약 없다면 Null)
        User user = userRepository.findUserByProviderAndProviderID(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                .orElse(null);

        tempId = user == null ? null : user.getId();
        log.info("tempId = {}", tempId);
        // 5-1) 만일 Oauth로 로그인한 기록이 없는 유저라면 기존 게스트유저 정보에 Oauth로그인 정보 추가입력
        if(user == null){
            log.info("5-1) 만일 Oauth로 로그인한 기록이 없는 유저라면 기존 게스트유저 정보에 Oauth로그인 정보 추가입력");
            guestUser.updateNewUser(oAuth2Response.toEntity());
            userRepository.save(guestUser);

            // 지금까지 사용했던 guest유저 반환
            log.info("5-1) [완료] 기존 guestUser 반환");
            return guestUser;

        // 5-2) 만일 기존에 Oauth로그인 기록이 있다면, 지금까지 게스트유저로 입력한 모든 정보를 기존 Oauth계정으로 수정
        } else {
            log.info("5-2) 만일 기존에 Oauth로그인 기록이 있다면, 지금까지 게스트유저로 입력한 모든 정보를 기존 Oauth계정으로 수정");
            // 참여중인 미팅룸의 ID 조회 (GuestUser, User)
            HashSet<UserMeetingRoom> guestUserMeetingRoomIdSet = userMeetingRoomRepository.findAllByUser(guestUser);
            HashSet<Long> userMeetingRoomIdSet = userMeetingRoomRepository.findMeetingRoomIdsByUser(user);

            // 두 Set의 차집합 필터링 이후 해당 값에 대한 유저 변경
            List<Long> removeIdList = new ArrayList<>();
            guestUserMeetingRoomIdSet.forEach(umr -> {
                if(userMeetingRoomIdSet.contains(umr.getMeetingRoom().getId())){
                    // 여기선 겹치는 존재이기 때문에 guest데이터를 삭제해야한다. (한번에 삭제해야하니깐 그 리스트를 담아두는게 나을듯)
                    removeIdList.add(umr.getId());
                } else {
                    //  여기선 겹치지 않기 때문에 userMeetingRoom 의 User를 변경해줘야 한다.
                    umr.changeUser(user);
                }
            });
            userMeetingRoomRepository.saveAll(guestUserMeetingRoomIdSet);
            userMeetingRoomRepository.deleteAllById(removeIdList);

            // 방별로 등록한 불가능일정 모두 수정 (Unavailable)
            List<UnavailableTime> unavailableList = unavailableRepository.findAllByUser(guestUser);
            unavailableList.forEach(ut -> ut.changeUser(user));
            unavailableRepository.saveAll(unavailableList);

            // 기존 Oauth 유저 반환
            log.info("5-2) [완료] 기존 OAuthUser 반환");
            return user;
        }



//        return userRepository.findUserByProviderAndProviderID(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
//                .orElseGet(() -> {
//                    User newUser = oAuth2Response.toEntity();
//                    return userRepository.save(newUser);
//                });
    }
}
