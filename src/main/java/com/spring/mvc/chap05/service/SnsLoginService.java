package com.spring.mvc.chap05.service;

import com.spring.mvc.chap05.dto.request.SignUpRequestDTO;
import com.spring.mvc.chap05.dto.response.KakaoUserResponseDTO;
import com.spring.mvc.chap05.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SnsLoginService {
    private final MemberService memberService;

    // 카카오 로그인 처리
    public void kakaoLogin(Map<String,String> requestParam, HttpSession session) {

        // 인가 코드를 가지고 토큰을 발급받는 요청보내기
        String kakaoAccessToken = getKakaoAccessToken(requestParam);
        log.debug("access_token:{}",kakaoAccessToken);

        //토큰을 통해서 사용자 정보 가져오기
        KakaoUserResponseDTO dto = getKakaoUserInfo(kakaoAccessToken);
        //카카오에서 받은 회원정보로 우리 사이트 회원가입

        String nickname = dto.getProperties().getNickname();
        //회원 중복확인
        if(!memberService.checkDuplicateValue("account",nickname)){

        memberService.join(
                SignUpRequestDTO.builder()
                                .account(nickname)
                        .password("0000")
                        .name(nickname)
                        .email(nickname+"@abc.com")
                        .loginMethod(Member.LoginMethod.KAKAO)
                        .build(),
                dto.getProperties().getProfileImage()
        );
        }

        ///우리사이트 로그인 처리
        memberService.maintainLoginState(session,nickname);

    }

    private KakaoUserResponseDTO getKakaoUserInfo(String kakaoAccessToken) {
        //토큰으로 사용자 정보 모집
        String requestUri="https://kapi.kakao.com/v2/user/me";

        //요청헤더 설정
        HttpHeaders headers=new HttpHeaders();
        headers.add("Authorization","Bearer "+kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //요청보내기
        RestTemplate template=new RestTemplate();
        ResponseEntity<KakaoUserResponseDTO> responseEntity = template.exchange(
                requestUri,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                KakaoUserResponseDTO.class
        );

        //응답정보 꺼내기

        KakaoUserResponseDTO reponseJSON = responseEntity.getBody();
        log.info("user_profile:{}",reponseJSON);

        return reponseJSON;



    }

    // 토큰 발급 요청
    private String getKakaoAccessToken(Map<String,String> requestParam) {

        // 요청 URI
        String requestUri = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 요청 바디에 파라미터 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", requestParam.get("appkey"));
        params.add("redirect_uri", requestParam.get("redirect"));
        params.add("code", requestParam.get("code"));

        //카카오 인증서버로 POST 요청 날리기



        RestTemplate template= new RestTemplate();
        HttpEntity<Object> requestEntity = new HttpEntity<>(params, headers);
        /*
        -RestTemplate 객체가 REST API 통신을 위한 API인데 (자바스크립트 fetch 역할)
        - 서버에 통신을 보내면서 응답을 받을 수 있는 메서드가 exchange
        param1 요청 URL
        param2: 요청방식(get,post,put..)
        param3: 요청 헤더와 요청 바디 정보- HttpEntity로 포장해서 줘야함.
        param4:응답 결과를 어떤 타입으로 받아낼 것인디(ex:DTO로 받을 건지,,Map으로 받을 건지)

         */
        ResponseEntity<Map> responseEntity = template.exchange(requestUri, HttpMethod.POST, requestEntity, Map.class);
        //응답데이터에서 JSON 추출
        Map<String,Object> reponseJSON = (Map<String, Object>) responseEntity.getBody();
        log.info("응답데이터 :{}",reponseJSON);

        //access token 추출
        String accessToken = (String)reponseJSON.get("access_token");

        return accessToken;
    }


}
