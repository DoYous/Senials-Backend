package com.senials.partymember.repository;

import com.senials.partyboard.entity.PartyBoard;
import com.senials.partymember.entity.PartyMember;
import org.springframework.data.domain.Page;
import com.senials.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Integer> {

    /*사용자별 참여한 모임 개수*/
    long countByUser_UserNumber(int userNumber);

    // 페이지네이션 용
    Page<PartyMember> findByUser_UserNumber(int userNumber, Pageable pageable);

    List<PartyMember> findAllByPartyBoard(PartyBoard partyBoard);

    PartyMember findByPartyBoardAndUser(PartyBoard partyBoard, User user);

    /* 모임 내 전체 멤버 수 */
    int countAllByPartyBoard(PartyBoard partyBoard);

}
