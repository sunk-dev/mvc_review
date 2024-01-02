package com.spring.mvc.chap05.service;

import com.spring.mvc.chap05.dto.request.AutoLoginDTO;
import com.spring.mvc.chap05.dto.request.LoginRequestDTO;
import com.spring.mvc.chap05.dto.request.SignUpRequestDTO;
import com.spring.mvc.chap05.dto.response.LoginUserResponseDTO;
import com.spring.mvc.chap05.entity.Member;
import com.spring.mvc.chap05.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;

import static com.spring.mvc.chap05.service.LoginResult.*;
import static com.spring.mvc.util.LoginUtils.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder encoder;

    // 회원가입 처리 서비스
    public boolean join(SignUpRequestDTO dto, String savePath) {

        // 클라이언트가 보낸 회원가입 데이터를
        // 패스워드 인코딩하여 엔터티로 변환해서 전달

        Member member = dto.toEntity(encoder);
        member.setProfileImage(savePath);
        return memberMapper.save(member);
    }

    // 로그인 검증 처리
    public LoginResult authenticate(
            LoginRequestDTO dto
    , HttpSession session
    , HttpServletResponse response) {

        Member foundMember = getMember(dto.getAccount());

        if (foundMember == null) { // 회원가입 안한 상태
            log.info("{} - 회원가입이 필요합니다.", dto.getAccount());
            return NO_ACC;
        }

        // 비밀번호 일치 검사
        String inputPassword = dto.getPassword(); // 사용자 입력 패스워드
        String realPassword = foundMember.getPassword(); // 실제 패스워드

        if (!encoder.matches(inputPassword, realPassword)) {
            log.info("비밀번호가 일치하지 않습니다!");
            return NO_PW;
        }

        //자동로그인 처리
        if(dto.isAutoLogin()){
            //1. 자동 로그인 쿠키 생성- 쿠키 안에 절대 중복되지 않는 값(브라우저 세션아이디)을 저장
            Cookie autoLoaginCookie = new Cookie(AUTO_LOGIN_COOKIE,session.getId());
            //2.쿠키 설정- 사용경로, 수명...
            int limitTime=60*60*24*90; //자동로그인시간 90일 설정
            autoLoaginCookie.setPath("/");
            autoLoaginCookie.setMaxAge(limitTime);

            //3.쿠키를 클라이언트에 전송
            response.addCookie(autoLoaginCookie);

            //4. DB에도 쿠키에 관련된 값들을 저장(랜덤 세션키 만료시간 저장)저장 갱신 update
            memberMapper.saveAutoLogin(
                    AutoLoginDTO.builder()
                            .sessionId(session.getId())
                            .limitTime(LocalDateTime.now().plusDays(90))
                            .account(dto.getAccount())
                            .build()
            );




        }

        log.info("{}님 로그인 성공!", foundMember.getAccount());
        return SUCCESS;

    }

    private Member getMember(String account) {
        return memberMapper.findMember(account);
    }

    // 아이디, 이메일 중복검사 서비스
    public boolean checkDuplicateValue(String type, String keyword) {
        return memberMapper.isDuplicate(type, keyword);
    }

    //세션을 이용해서 일반 로그인 유지하기
    public void maintainLoginState(HttpSession session, String account){
        //세션은 서버에서만 유일하게 보관되는 데이터로서
        //로그인 유지든 상태유지가 필요할때 사용되는 개념입니다.
        // 세션은 쿠키와 달리 모든 데이터를 저장할수 있음.

        //세션의 수명은 설정한 수명의 영향을 받고 브라우저의 수명과 함께한다.
        //브라우저가 닫히면 세션도 닫힌다!

        //현재 로그인한 사람의 모든 정보조회
        Member member = getMember(account);
        LoginUserResponseDTO dto= LoginUserResponseDTO.builder()
                .account(member.getAccount())
                .email(member.getEmail())
                .auth(member.getAuth().name())
                .nickName(member.getName()).build();

        //세션에 로그인한 회원의 정보저장
        session.setAttribute(LOGIN_KEY,dto);



        //세션도 수명을 설정해야함
        session.setMaxInactiveInterval(60*60); //1시간 지나면 로그인이 풀리도록 설정



    }


    public void autoLoginClear(HttpServletRequest request, HttpServletResponse response) {
        ///1. 자동로그인 쿠키를 가벼온다

        Cookie c = WebUtils.getCookie(request, AUTO_LOGIN_COOKIE);
        //2. 쿠키를 삭제한다
        //-> 쿠키의 수명을 0으로 설절
        if(c!=null){
            c.setMaxAge(0);
            c.setPath("/");
            response.addCookie(c);
            
            //3. 데이터베이스도 세션아이디와 만료시간을 제거한다.
            memberMapper.saveAutoLogin(AutoLoginDTO.builder()
                            .sessionId("none")
                            .limitTime(LocalDateTime.now())
                            .account(getCurrentLoginMemberAccount(request.getSession()))
                            .build());
            
        }

    }
}
