package com.senials.partyreview.repository;

import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyreview.entity.PartyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartyReviewRepository extends JpaRepository<PartyReview, Integer> {

    /* 유저 모임 후기 단일 조회*/
    PartyReview findByUser_UserNumberAndPartyBoard_PartyBoardNumber(int userNumber, int partyBoardNumber);

    /* 상세 페이지 용 */
    Page<PartyReview> findAllByPartyBoard(PartyBoard partyBoard, Pageable pageable);

    /* 모임 후기 평점 */
    @Query(value = "SELECT IFNULL(ROUND(AVG(partyReviewRate), 1), 0) from PartyReview WHERE partyBoard = :partyBoard")
    double findAvgRateByPartyBoard(PartyBoard partyBoard);
    @Query(value = "SELECT IFNULL(ROUND(AVG(party_review_rate), 1), 0) from PARTY_REVIEW WHERE party_board_number = :partyBoardNumber", nativeQuery = true)
    double findAvgRateByPartyBoard_PartyBoardNumber(int partyBoardNumber);

    /* 모임 후기 개수 */
    int countAllByPartyBoard(PartyBoard partyBoard);
    int countAllByPartyBoard_PartyBoardNumber(int partyBoardNumber);
}
