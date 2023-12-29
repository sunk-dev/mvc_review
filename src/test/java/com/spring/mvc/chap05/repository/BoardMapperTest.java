package com.spring.mvc.chap05.repository;

import com.spring.mvc.chap05.common.Search;
import com.spring.mvc.chap05.entity.Board;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BoardMapperTest {


    @Autowired
    BoardMapper mapper;

    // 게시물을 300개 저장

//    @Test
//    @DisplayName("게시물 300개를 저장해야 한다")
//    void bulkInsertTest() {
//        //given
//        for (int i = 1; i <= 300; i++) {
//
//            Board b = Board.builder()
//                    .content("테스트용 내용 " + i)
//                    .title("테스트용 제목 " + i)
//                    .build();
//
//            mapper.save(b);
//        }
//        //when
//
//        //then
//    }
    
    
    
    @Test
    @DisplayName("게시물을 전체조회하면 303개의 게시물이 조회된다.")
    void findAllTest() {
        //given
        
        //when
        List<Board> boardList = mapper.findAll(new Search());
        //then
        assertEquals(303, boardList.size());
    }
    
    
    @Test
    @DisplayName("30번 게시물을 단일조회하면 제목에 27이 포함되어 있어야 한다.")
    void findOneTest() {
        //given
        int boardNo = 30;
        //when
        Board b = mapper.findOne(boardNo);
        //then
        assertTrue(b.getTitle().contains("27"));
    }
    

    @Test
    @DisplayName("29번 게시물을 삭제하고 다시 조회하면 조회되지 않아야 한다.")
    @Transactional
    @Rollback
    void deleteTest() {
        // 테스트는 몇번을 돌려도 같은 결과가 나와야 함
        //given
        int boardNo = 29;
        //when
        boolean flag = mapper.deleteByNo(boardNo);
        Board board = mapper.findOne(boardNo);
        //then
        assertTrue(flag);
        assertNull(board);
    }

}