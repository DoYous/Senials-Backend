package com.senials.likes.repository;

import com.senials.likes.entity.Likes;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Integer> {

    // 특정 사용자가 좋아요한 모든 데이터를 상세하게 조회 (PartyReview와 PartyBoardImage 포함)
    @Query("SELECT l FROM Likes l " +
            "JOIN FETCH l.partyBoard pb " +
            "LEFT JOIN FETCH pb.reviews pr " +
            "LEFT JOIN FETCH pb.images img " +
            "WHERE l.user = :user")
    List<Likes> findWithDetailsByUser(@Param("user") User user);

    // 페이지네이션 용
    Page<Likes> findAllByUser(User user, Pageable pageable);

    /*사용자별 좋아요 한 모임 개수*/
    long countByUser(User user);

    /* 특정 user가 partyBoard를 좋아요 했는지 */
    boolean existsByUserAndPartyBoard(User user, PartyBoard partyBoard);

    /* 특정 user가 partyBoard를 좋아요 했는지 */
    boolean existsByUser_UserNumberAndPartyBoard_PartyBoardNumber(int userNumber, int partyBoardNumber);

    /* user가 partyBoard 좋아요 한 정보 : 좋아요 취소 용 */
    Likes findByUserAndPartyBoard(User user, PartyBoard partyBoard);

    /* user가 partyBoard 좋아요 한 정보 : 좋아요 취소 용 */
    Likes findByUser_UserNumberAndPartyBoard_PartyBoardNumber(int userNumber, int partyBoardNumber);

}
