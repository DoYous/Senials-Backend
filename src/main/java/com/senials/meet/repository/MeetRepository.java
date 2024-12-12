package com.senials.meet.repository;

import com.senials.meet.entity.Meet;
import com.senials.partyboard.entity.PartyBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetRepository extends JpaRepository<Meet, Integer> {

    /* 모임 내 일정 전체 조회 (내림차순) */
    Page<Meet> findAllByPartyBoard(PartyBoard partyBoard, Pageable pageable);

    /* 모임 내 일정 총 개수 */
    int countAllByPartyBoard(PartyBoard partyBoard);
    int countAllByPartyBoard_PartyBoardNumber(int partyBoardNumber);
  
    //사용자 별 참여한 모임 확인
    @Query("SELECT m FROM Meet m JOIN m.partyBoard pb WHERE pb.user.userNumber = :userNumber")
    List<Meet> findAllByUserNumber(int userNumber);

    Meet findByPartyBoard(PartyBoard partyBoard);
}
