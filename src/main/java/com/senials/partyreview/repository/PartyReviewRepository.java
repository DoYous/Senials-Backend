package com.senials.partyreview.repository;

import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyreview.entity.PartyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PartyReviewRepository extends JpaRepository<PartyReview, Integer> {

    /* 상세 페이지 정렬 용 */
    List<PartyReview> findAllByPartyBoardOrderByPartyReviewWriteDateDesc(PartyBoard partyBoard);

    /* 모임 후기 평점 */
    @Query(value = "SELECT IFNULL(ROUND(AVG(partyReviewRate), 1), 0) from PartyReview WHERE partyBoard = :partyBoard")
    double findAvgRateByPartyBoard(PartyBoard partyBoard);

    /* 모임 후기 개수 */
    int countAllByPartyBoard(PartyBoard partyBoard);
}
