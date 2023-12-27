package com.spring.mvc.interceptor;

import com.spring.mvc.chap05.repository.BoardMapper;
import com.spring.mvc.util.LoginUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.spring.mvc.util.LoginUtils.*;

/**
 * - 인터셉터: 컨트롤러의 요청이 들어가기 전 후에
 *   공통적으로 처리할 코드나 검사할 일들을 정의해 놓는 클래스
 *
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class BoardInterceptor implements HandlerInterceptor {

    private  final BoardMapper boardMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 로그인을 안했으면 글쓰기, 글수정, 글삭제 요청을 튕겨낼것
        HttpSession session = request.getSession();
        if(!isLogin(session)){
            log.info("this request ({}) id denied!",request.getRequestURI());
            response.sendRedirect("/members/sign-in");
            return false;

        }
        //삭제요청이 들어올때 서버에서 다시한번 관리자인지 내가 쓴글인지 체크
        //현재 요청이 삭제 요청인지 확인
        String uri = request.getRequestURI();
        if(uri.contains("delete")){
            //로그인한 계정명과 게시물의 계정명이 일치하는 지 체크
            // 로그인한 계정명은 -> 세션에서 가져옴
            // 게시물 계정명은 -> DB에서 가져옴
            //글번호는 어떻게 구함 - 쿼리 스트링에서 구함 ? 뒤에 붙어있음
            String bno = request.getParameter("bno");
            String targetAccount = boardMapper.findOne(Integer.parseInt(bno)).getAccount();

            //만약에 관리자라면 삭제 통과
            if(isAdmin(session)) return true;
            //만약에 내가 쓴글이 아니면
            if(!isMine(session,targetAccount)){
                //접근 권한이 없다는 안내페이지로 이동시킴
                response.sendRedirect("/access-deny");
                return false;
            }

        }


        return true;
    }
}
