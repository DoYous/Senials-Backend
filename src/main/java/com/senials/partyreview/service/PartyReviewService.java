package com.senials.partyreview.service;

import com.senials.common.mapper.PartyReviewMapper;
import com.senials.common.mapper.PartyReviewMapperImpl;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partyreview.dto.PartyReviewDTO;
import com.senials.partyreview.dto.PartyReviewDTOForDetail;
import com.senials.partyreview.entity.PartyReview;
import com.senials.partyreview.repository.PartyReviewRepository;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PartyReviewService {

    private final PartyReviewMapper partyReviewMapper;

    private final PartyReviewRepository partyReviewRepository;

    private final PartyBoardRepository partyBoardRepository;

    private final UserRepository userRepository;


    public PartyReviewService(
            PartyReviewMapperImpl partyReviewMapperImpl
            , PartyReviewRepository partyReviewRepository
            , PartyBoardRepository partyBoardRepository
            , UserRepository userRepository
    ) {
        this.partyReviewMapper = partyReviewMapperImpl;
        this.partyReviewRepository = partyReviewRepository;
        this.partyBoardRepository = partyBoardRepository;
        this.userRepository = userRepository;
    }


    /* 개인 모임 후기 단일 조회 */
    public PartyReviewDTOForDetail getOnePartyReview(int userNumber, int partyBoardNumber) {

        PartyReview partyReview = partyReviewRepository.findByUser_UserNumberAndPartyBoard_PartyBoardNumber(userNumber, partyBoardNumber);

        if(partyReview != null) {
            return partyReviewMapper.toPartyReviewDTOForDetail(partyReview);
        } else {
            return null;
        }
    }


    /* 모임 후기 전체 수 */
    public int countPartyReviews(int partyBoardNumber) {

        return partyReviewRepository.countAllByPartyBoard_PartyBoardNumber(partyBoardNumber);
    }

    /* 모임 후기 평점 */
    public double getAverageReviewRate(int partyBoardNumber) {

        return partyReviewRepository.findAvgRateByPartyBoard_PartyBoardNumber(partyBoardNumber);
    }


    /* 모임 후기 전체 조회 */
    public List<PartyReviewDTOForDetail> getPartyReviews(int partyBoardNumber, Pageable pageable) {

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요청입니다."));

        /* 모임 번호에 해당하는 모임 후기 리스트 도출 */
        Page<PartyReview> partyReviewList = partyReviewRepository.findAllByPartyBoard(partyBoard, pageable);

        List<PartyReviewDTOForDetail> partyReviewDTOList = partyReviewList.stream()
                .map(partyReviewMapper::toPartyReviewDTOForDetail)
                .toList();

        return partyReviewDTOList;
    }

    /* 모임 후기 작성 */
    @Transactional
    public void registerPartyReview (
            int userNumber
            , int partyBoardNumber
            , PartyReviewDTO partyReviewDTO
    ) {

        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);


        PartyReview partyReview = partyReviewMapper.toPartyReview(partyReviewDTO);
        partyReview.initializeUser(user);
        partyReview.initializePartyBoard(partyBoard);
        partyReview.initializePartyReviewWriteDate(LocalDateTime.now());


        partyReviewRepository.save(partyReview);
    }

    /* 모임 후기 수정 */
    @Transactional
    public void modifyPartyReview(
            int partyReviewNumber
            , PartyReviewDTO partyReviewDTO
    ) {
        PartyReview partyReview = partyReviewRepository.findById(partyReviewNumber)
                .orElseThrow(IllegalArgumentException::new);

        partyReview.updatePartyReviewRate(partyReviewDTO.getPartyReviewRate());
        partyReview.updatePartyReviewDetail(partyReviewDTO.getPartyReviewDetail());
    }

    /* 모임 후기 삭제 */
    @Transactional
    public void removePartyReview (int partyReviewNumber) {

        partyReviewRepository.deleteById(partyReviewNumber);

    }
}
