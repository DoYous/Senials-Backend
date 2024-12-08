package com.senials.partyboard.repository;

import com.senials.hobbyboard.entity.Hobby;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyBoardRepository extends JpaRepository<PartyBoard, Integer>, JpaSpecificationExecutor<PartyBoard> {

    // 모임 상세 조회
    PartyBoard findByPartyBoardNumber(int partyBoardNumber);

    Page<PartyBoard> findAllByHobbyIn(List<Hobby> hobby, Pageable pageable);

    /* 페이지네이션 용 */
    Page<PartyBoard> findByUser(User user, Pageable pageable);

    /*사용자별 만든 모임의 수*/
    long countByUser(User user);

    List<PartyBoard> findByHobby(Hobby hobby);

    /* 인기 추천 모임 */
    /* 평점 높은 순, 리뷰 개수 N개 이상, 모집중 >> M개 */
    @Query(value = """
            SELECT pb.* 
            FROM PARTY_BOARD pb 
            JOIN PARTY_REVIEW pr 
                ON pb.party_board_status = 0 
                       AND 
                   pb.party_board_number = pr.party_board_number 
            GROUP BY pb.party_board_number 
            HAVING COUNT(pr.party_review_number) >= :minReviewCount 
            ORDER BY AVG(pr.party_review_rate) DESC 
            """
            , countQuery = """
                SELECT COUNT(*) 
                FROM PARTY_BOARD pb 
                    JOIN PARTY_REVIEW pr 
                        ON pb.party_board_status = 0 
                               AND 
                           pb.party_board_number = pr.party_board_number 
                GROUP BY pb.party_board_number
                HAVING COUNT(pr.party_review_number) >= :minReviewCount
                ORDER BY AVG(pr.party_review_rate) DESC
                """
            , nativeQuery = true)
    Page<PartyBoard> findPopularPartyBoards(int minReviewCount, Pageable pageable);

}
