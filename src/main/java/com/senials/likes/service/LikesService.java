package com.senials.likes.service;

import com.senials.common.mapper.PartyBoardMapper;
import com.senials.common.mapper.PartyBoardMapperImpl;
import com.senials.likes.entity.Likes;
import com.senials.likes.repository.LikeRepository;
import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partyreview.entity.PartyReview;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikesService {
    private final LikeRepository likesRepository;
    private final UserRepository userRepository;
    private final PartyBoardRepository partyBoardRepository;

    public LikesService(LikeRepository likesRepository, UserRepository userRepository, PartyBoardRepository partyBoardRepository) {
        this.likesRepository = likesRepository;
        this.userRepository = userRepository;
        this.partyBoardRepository = partyBoardRepository;
    }

    // 좋아요 한 모임 리스트 가져오기
    public List<PartyBoardDTOForCard> getLikedPartyBoardsByUserNumber(int userNumber, int page, int size) {

        //User 가 존재하는지 확인
        User user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Page 요청 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size);

        // User가 좋아한 목록 페이징 조회
        Page<Likes> likesList = likesRepository.findAllByUser(user, pageable);

        return likesList.stream().map(likes -> {
            PartyBoard partyBoard = likes.getPartyBoard();

            // 별점 평균 계산
            double averageRating = 0.0;
            List<PartyReview> reviews = partyBoard.getReviews();
            if (!reviews.isEmpty()) {
                int totalRating = 0;
                for (PartyReview review : reviews) {
                    totalRating += review.getPartyReviewRate();
                }
                averageRating = (double) totalRating / reviews.size();
            }
            // 첫 번째 이미지 가져오기
            String firstImage = partyBoard.getImages().isEmpty() ? null : partyBoard.getImages().get(0).getPartyBoardImg();
            // DTO 생성
            return new PartyBoardDTOForCard(
                    partyBoard.getPartyBoardNumber(),
                    partyBoard.getPartyBoardName(),
                    partyBoard.getPartyBoardStatus(),
                    partyBoard.getPartyMembers().size(), // 참여 인원 수
                    averageRating,
                    firstImage
            );
        }).collect(Collectors.toList());
    }


    /*모임 개수 api*/
    /*사용자 별 좋아요 한 모임 개수*/
    public long countLikesPartyBoardsByUserNumber(int userNumber) {
        User user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return likesRepository.countByUser(user);
    }


    /* 좋아요 / 좋아요 취소 */
    public int toggleLike(int userNumber, int partyBoardNumber) {

        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        Likes foundLikes = likesRepository.findByUserAndPartyBoard(user, partyBoard);

        if(foundLikes != null) {
            likesRepository.delete(foundLikes);
            return 0;
        } else {
            Likes newLikes = new Likes(0, user, partyBoard);
            likesRepository.save(newLikes);
            return 1;
        }
    }

    // 특정 모임 좋아요 체크
    public boolean isLikedByPartyBoardNumber(int userNumber, int partyBoardNumber) {

        return likesRepository.existsByUser_UserNumberAndPartyBoard_PartyBoardNumber(userNumber, partyBoardNumber);
    }
}
