package com.senials.partyboardimage.repository;

import com.senials.partyboardimage.entity.PartyBoardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyBoardImageRepository extends JpaRepository<PartyBoardImage, Integer> {

    /* 모임 번호로 이미지명 문자열 1개 가져오기 */
    @Query(value =
            "SELECT party_board_img " +
            "FROM PARTY_BOARD_IMAGE " +
            "where party_board_number = :partyBoardNumber " +
            "LIMIT 1"
            , nativeQuery = true)
    String findPartyBoardImgByPartyBoardNumber(int partyBoardNumber);
}
