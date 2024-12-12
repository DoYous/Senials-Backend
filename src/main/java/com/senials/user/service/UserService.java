package com.senials.user.service;

import com.senials.common.mapper.UserMapper;
import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partymember.entity.PartyMember;
import com.senials.partymember.repository.PartyMemberRepository;
import com.senials.partyreview.entity.PartyReview;
import com.senials.user.dto.UserCommonDTO;
import com.senials.user.dto.UserDTO;
import com.senials.user.dto.UserDTOForPublic;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private UserMapper userMapper;

    private com.senials.user.repository.UserRepository userRepository;

    private final PartyMemberRepository partyMemberRepository;
    private final PartyBoardRepository partyBoardRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository, PartyMemberRepository partyMemberRepository,PartyBoardRepository partyBoardRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.partyBoardRepository = partyBoardRepository;
    }

    //모든 사용자 조회
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserDTO)
                .toList();

    }

    // 특정 사용자 조회
    public UserCommonDTO getUserByNumber(int userNumber) {
        return userRepository.findById(userNumber)
                .map(user -> new UserCommonDTO(
                        user.getUserName(),
                        user.getUserNickname(),
                        user.getUserDetail()
                        /*, user.getUserProfileImg()*/
                ))
                .orElseThrow(IllegalArgumentException::new);
    }


    // 특정 사용자 조회 (UserDTOForPublic)
    public UserDTOForPublic getUserPublicByNumber(int userNumber) {
        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        return userMapper.toUserDTOForPublic(user);
    }


    //특정 사용자 탈퇴
    public boolean deleteUser(int userNumber) {
        if (userRepository.existsById(userNumber)) {
            userRepository.deleteById(userNumber);
            return true;
        }
        return false; // 사용자 존재하지 않음
    }

    // 특정 사용자 수정
    public boolean updateUserProfile(int userNumber, String userNickname, String userDetail
                                     /*String userProfileImg*/
    ) {
        return userRepository.findById(userNumber).map(existingUser -> {
            if (userNickname != null) {
                existingUser.updateUserNickname(userNickname);
            }
            if (userDetail != null) {
                existingUser.updateUserDetail(userDetail);
            }
    /*        if (userProfileImg != null) {
                existingUser.updateUserProfileImg(userProfileImg);
            }*/
            userRepository.save(existingUser);
            return true;
        }).orElseThrow(IllegalArgumentException::new);
    }

    //사용자 프로필 수정
    public boolean updateUserProfileImage(int userNumber, String userProfileImg) {
        return userRepository.findById(userNumber).map(user -> {
            user.updateUserProfileImg(userProfileImg);
            userRepository.save(user);
            return true;
        }).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }




    //사용자별 참여한 모임 출력
    // 사용자별 참여한 모임 목록을 PartyBoardDTOForCard로 반환
    public List<PartyBoardDTOForCard> getJoinedPartyBoardsByUserNumber(int userNumber, int page, int size) {
        // Page 요청 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size);

        // 사용자가 참여한 PartyMember 목록 페이징 조회
        Page<PartyMember> partyMembersPage = partyMemberRepository.findByUser_UserNumber(userNumber, pageable);

        // PartyBoardDTOForCard 리스트로 변환
        return partyMembersPage.stream()
                .map(pm -> {
                    PartyBoard partyBoard = pm.getPartyBoard();
                    // 별점 평균 계산
                    double averageRating = 0.0;
                    List<PartyReview> reviews = partyBoard.getReviews();
                    if (!reviews.isEmpty()) {
                        int totalRating = reviews.stream().mapToInt(PartyReview::getPartyReviewRate).sum();
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


    // 사용자가 만든 모임 목록 조회
    public List<PartyBoardDTOForCard> getMadePartyBoardsByUserNumber(int userNumber, int page, int size) {
        // User가 존재하는지 확인
        User user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Page 요청 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size);

        // User가 만든 PartyBoard 페이징 조회
        Page<PartyBoard> partyBoardPage = partyBoardRepository.findByUser(user, pageable);

        // PartyBoardDTOForCard 리스트로 변환
        return partyBoardPage.stream().map(partyBoard -> {
            // 별점 평균 계산
            double averageRating = 0.0;
            List<PartyReview> reviews = partyBoard.getReviews();

            if (!reviews.isEmpty()) {
                int totalRating = reviews.stream().mapToInt(PartyReview::getPartyReviewRate).sum();
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
    /*사용자 별 참여한 모임 개수*/
    public long countPartiesPartyBoardsByUserNumber(int userNumber) {
        return partyMemberRepository.countByUser_UserNumber(userNumber);
    }

    /*사용자 별 만든 모임 개수*/
    public long countMadePartyBoardsByUserNumber(int userNumber) {
        User user = userRepository.findById(userNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return partyBoardRepository.countByUser(user);
    }

}
