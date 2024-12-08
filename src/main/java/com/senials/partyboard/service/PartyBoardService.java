package com.senials.partyboard.service;

import com.senials.common.mapper.PartyBoardMapper;
import com.senials.common.mapper.PartyBoardMapperImpl;
import com.senials.favorites.entity.Favorites;
import com.senials.favorites.repository.FavoritesRepository;
import com.senials.hobbyboard.entity.Hobby;
import com.senials.hobbyboard.repository.HobbyRepository;
import com.senials.likes.repository.LikeRepository;
import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.partyboard.dto.PartyBoardDTOForDetail;
import com.senials.partyboard.dto.PartyBoardDTOForModify;
import com.senials.partyboard.dto.PartyBoardDTOForWrite;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partyboard.repository.PartyBoardSpecification;
import com.senials.partyboardimage.entity.PartyBoardImage;
import com.senials.partyboardimage.repository.PartyBoardImageRepository;
import com.senials.partymember.repository.PartyMemberRepository;
import com.senials.partyreview.repository.PartyReviewRepository;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartyBoardService {

    private final String imageRootPath = "src/main/resources/static/img/party_board";

    private final PartyBoardMapper partyBoardMapper;

    private final PartyBoardRepository partyBoardRepository;

    private final UserRepository userRepository;

    private final HobbyRepository hobbyRepository;

    private final FavoritesRepository favoritesRepository;

    private final PartyMemberRepository partyMemberRepository;

    private final PartyReviewRepository partyReviewRepository;

    private final LikeRepository likeRepository;


    @Autowired
    public PartyBoardService(
            PartyBoardMapperImpl partyBoardMapperImpl
            , PartyBoardRepository partyBoardRepository
            , UserRepository userRepository
            , HobbyRepository hobbyRepository
            , FavoritesRepository favoritesRepository
            , PartyMemberRepository partyMemberRepository
            , PartyReviewRepository partyReviewRepository
            , LikeRepository likeRepository
    )
    {
        this.partyBoardMapper = partyBoardMapperImpl;
        this.partyBoardRepository = partyBoardRepository;
        this.userRepository = userRepository;
        this.hobbyRepository = hobbyRepository;
        this.favoritesRepository = favoritesRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.partyReviewRepository = partyReviewRepository;
        this.likeRepository = likeRepository;
    }

    /* 인기 추천 모임 (평점 높은 순, 리뷰 개수 N개 이상, 모집중 >> M개 제한)*/
    public List<PartyBoardDTOForCard> getPopularPartyBoards(int minReviewCount, int size, int pageNumber) {

        Page<PartyBoard> partyBoardList = partyBoardRepository.findPopularPartyBoards(minReviewCount, PageRequest.of(pageNumber, size));

        List<PartyBoardDTOForCard> partyBoardDTOForCardList = partyBoardList.stream().map(partyBoard -> {

            int partyMemberCnt = partyMemberRepository.countAllByPartyBoard(partyBoard);
            int partyReviewCnt = partyReviewRepository.countAllByPartyBoard(partyBoard);
            double partyAvgRate = partyReviewRepository.findAvgRateByPartyBoard(partyBoard);

            String partyImageThumbnail = null;
            List<PartyBoardImage> partyBoardImageList = partyBoard.getImages();
            if (partyBoardImageList != null && !partyBoardImageList.isEmpty()) {
                partyImageThumbnail = partyBoardImageList.get(0).getPartyBoardImg();
            }

            PartyBoardDTOForCard partyBoardCard = partyBoardMapper.toPartyBoardDTOForCard(partyBoard);
            partyBoardCard.setMemberCount(partyMemberCnt);
            partyBoardCard.setReviewCount(partyReviewCnt);
            partyBoardCard.setAverageRating(partyAvgRate);
            partyBoardCard.setFirstImage(partyImageThumbnail);

            return partyBoardCard;

        }).toList();

        return partyBoardDTOForCardList;
    }


    /* 모임 검색 및 정렬 */
    public List<PartyBoardDTOForCard> searchPartyBoard(String sortMethod, String keyword, Integer cursor, int size, boolean isLikedOnly, Integer userNumber) {

        Sort.Order numberAsc = Sort.Order.asc("partyBoardNumber");
        Sort.Order numberDesc = Sort.Order.desc("partyBoardNumber");


        String sortColumn = null;
        Pageable pageable = null;
        boolean isAscending = false;
        boolean isIntegerSort = true;


        switch (sortMethod) {
            /* 최신순 */
            case "lastest":
                sortColumn = "partyBoardOpenDate";
                pageable = PageRequest.of(0, size
                        , Sort.by(Sort.Order.desc(sortColumn), numberDesc));
                isIntegerSort = false;
                break;

            /* 오래된 순*/
            case "oldest":
                sortColumn = "partyBoardOpenDate";
                pageable = PageRequest.of(0, size
                        , Sort.by(Sort.Order.asc(sortColumn), numberAsc));
                isAscending = true;
                isIntegerSort = false;
                break;

            /* 좋아요순 */
            case "mostLiked":
                sortColumn = "partyBoardLikeCnt";
                pageable = PageRequest.of(0, size
                        , Sort.by(Sort.Order.desc(sortColumn), numberDesc));
                break;

            /* 조회수순 */
            case "mostViewed":
                sortColumn = "partyBoardViewCnt";
                pageable = PageRequest.of(0, size
                        , Sort.by(Sort.Order.desc(sortColumn), numberDesc));
                break;
            default:
        }

        /* 유저 number 필요 */
        User user;
        if(userNumber != null) {
            user = userRepository.findById(userNumber).orElseThrow(IllegalArgumentException::new);
        } else {
            user = null;
        }

        /* 관심사 기반 추천 확인 */
        // 관심사 기반 추천 시 최소 빈 리스트 / 미추천 시 null
        List<Hobby> hobbyList = null;
        if(isLikedOnly && user != null) {
            List<Favorites> favoritesList = favoritesRepository.findAllByUser(user);

            /* 관심사 존재하는지 체크 */
            if(!favoritesList.isEmpty()) {
                hobbyList = favoritesList.stream().map(Favorites::getHobby).toList();
            } else {
                hobbyList = new ArrayList<>();
            }
        }


        Page<PartyBoard> partyBoardList = null;
        /* Specification 쿼리문 실행 */
        Specification<PartyBoard> spec = null;
        if(isIntegerSort) {
            spec = PartyBoardSpecification.searchLoadInteger(sortColumn, keyword, cursor, isAscending, hobbyList);
        } else {
            spec = PartyBoardSpecification.searchLoadLocalDate(sortColumn, keyword, cursor, isAscending, hobbyList);
        }
        partyBoardList = partyBoardRepository.findAll(spec, pageable);


        // if(!sortMethod.equals("mostRated")) {
        //
        // } else {
        //     partyBoardList = partyBoardRepository.findPopularPartyBoards();
        // }


        List<PartyBoardDTOForCard> partyBoardDTOForCardList = partyBoardList.stream()
                .map(partyBoard -> {
                    PartyBoardDTOForCard partyBoardCard = partyBoardMapper.toPartyBoardDTOForCard(partyBoard);

                    int partyMemberCnt = partyMemberRepository.countAllByPartyBoard(partyBoard);
                    int partyReviewCnt = partyReviewRepository.countAllByPartyBoard(partyBoard);
                    double partyAvgRate = partyReviewRepository.findAvgRateByPartyBoard(partyBoard);
                    boolean isLiked = user != null && likeRepository.existsByUserAndPartyBoard(user, partyBoard);

                    String partyImageThumbnail = null;
                    List<PartyBoardImage> partyBoardImageList = partyBoard.getImages();
                    if (partyBoardImageList != null && !partyBoardImageList.isEmpty()) {
                        partyImageThumbnail = partyBoardImageList.get(0).getPartyBoardImg();
                    }

                    partyBoardCard.setMemberCount(partyMemberCnt);
                    partyBoardCard.setReviewCount(partyReviewCnt);
                    partyBoardCard.setAverageRating(partyAvgRate);
                    partyBoardCard.setFirstImage(partyImageThumbnail);
                    partyBoardCard.setLiked(isLiked);

                    return partyBoardCard;
                }).collect(Collectors.toList());

        return partyBoardDTOForCardList;
    }


    // 모임 상세 조회
    public PartyBoardDTOForDetail getPartyBoardByNumber(int partyBoardNumber) {

        PartyBoard partyBoard = partyBoardRepository.findByPartyBoardNumber(partyBoardNumber);

        PartyBoardDTOForDetail partyBoardDTO = partyBoardMapper.toPartyBoardDTOForDetail(partyBoard);
        partyBoardDTO.setUserNumber(partyBoard.getUser().getUserNumber());
        partyBoardDTO.setImages(partyBoard.getImages().stream().map(partyBoardMapper::toPartyBoardImageDTO).toList());

        return partyBoardDTO;
    }


    /* 모임 글 작성 */
    @Transactional
    public int registerPartyBoard(int userNumber, PartyBoardDTOForWrite newPartyBoardDTO) {

        // 1. userNumber로 User 엔티티 조회
        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        // 2. hobbyNumber로 Hobby 엔티티 조회
        Hobby hobby = hobbyRepository.findById(newPartyBoardDTO.getHobbyNumber())
                .orElseThrow(IllegalArgumentException::new);

        // 3. PartyBoardDTOForWrite -> PartyBoard 엔티티 생성
        PartyBoard newPartyBoard = PartyBoard.builder()
                .partyBoardNumber(0)
                .user(user)
                .hobby(hobby)
                .partyBoardName(newPartyBoardDTO.getPartyBoardName())
                .partyBoardDetail(newPartyBoardDTO.getPartyBoardDetail())
                .partyBoardOpenDate(LocalDate.now())
                .build();

        // 4. 이미지 저장
        List<PartyBoardImage> partyBoardImages = new ArrayList<>();
        for (String savedFile : newPartyBoardDTO.getSavedFiles()) {
            // PartyBoardImage 엔티티 생성
            PartyBoardImage partyBoardImage = PartyBoardImage.builder()
                    .partyBoard(newPartyBoard)
                    .partyBoardImg(savedFile)
                    .build();

            partyBoardImages.add(partyBoardImage);
        }

        newPartyBoard.initializeImages(partyBoardImages);

        // 4. 엔티티 저장
        PartyBoard registeredPartyBoard = partyBoardRepository.save(newPartyBoard);
        return registeredPartyBoard.getPartyBoardNumber();
    }


    /* 모임 글 수정 */
    @Transactional
    public void modifyPartyBoard(PartyBoardDTOForModify partyBoardDTO) {

        int partyBoardNumber = partyBoardDTO.getPartyBoardNumber();

        /* 기존 엔티티 로드 */
        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invalid partyBoardNumber: " + partyBoardNumber));


        /* 수정 DTO 데이터 */
        int newHobbyNumber = partyBoardDTO.getHobbyNumber();
        String newPartyBoardName = partyBoardDTO.getPartyBoardName();
        String newPartyBoardDetail = partyBoardDTO.getPartyBoardDetail();
        int newPartyBoardStatus = partyBoardDTO.getPartyBoardStatus();
        List<Integer> removedFileNumbers = partyBoardDTO.getRemovedFileNumbers();
        List<String> addedFiles = partyBoardDTO.getAddedFiles();


        /* 1. hobbyNumber 수정 됐을 때 */
        if(partyBoard.getHobby().getHobbyNumber() != partyBoardDTO.getHobbyNumber()) {
            Hobby hobby = hobbyRepository.findById(partyBoardDTO.getHobbyNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid hobbyNumber: " + partyBoardDTO.getHobbyNumber()));

            partyBoard.updateHobby(hobby);
        }

        /* 나머지는 비교X */
        partyBoard.updatePartyBoardName(newPartyBoardName);
        partyBoard.updatePartyBoardDetail(newPartyBoardDetail);
        partyBoard.updatePartyBoardStatus(newPartyBoardStatus);


        List<PartyBoardImage> partyBoardImages = partyBoard.getImages();

        String imgBoardPath = imageRootPath + "/" + partyBoardNumber + "/thumbnail";
        /* 2. 이미지 삭제*/
        if (removedFileNumbers != null && !removedFileNumbers.isEmpty()) {
            // 역순으로 순회 (리스트 순회 중 삭제해도 문제 없도록) > Iterator로 순회하는 방법도 있음
            for (int i = partyBoardImages.size() - 1; i >= 0; i--) {
                PartyBoardImage partyboardImage = partyBoardImages.get(i);

                // 역순으로 순회 (리스트 순회 중 삭제해도 문제 없도록)
                for (int j = removedFileNumbers.size() - 1; j >= 0; j--) {
                    Integer removedFileNumber = removedFileNumbers.get(j);

                    // 이미지 번호와 제거한 이미지 번호가 일치할 때
                    if (partyboardImage.getPartyBoardImageNumber() == removedFileNumber) {

                        // PartyBoard 엔티티 이미지 리스트에서 제거
                        partyBoardImages.remove(partyboardImage);
                        // 엔티티에서 제거 완료한 이미지 번호는 더 이상 비교하지 않음
                        removedFileNumbers.remove(removedFileNumber);

                        /* 실제 파일 삭제 */
                        File removedFile = new File(imgBoardPath + "/" + partyboardImage.getPartyBoardImg());
                        removedFile.delete();
                        break;
                    }
                }
            }
        }

        /* 3. 이미지 추가 */
        if (addedFiles != null && !addedFiles.isEmpty()) {
            for (String addedFile : addedFiles) {
                PartyBoardImage partyBoardImage = PartyBoardImage.builder()
                        .partyBoard(partyBoard)
                        .partyBoardImg(addedFile)
                        .build();

                partyBoardImages.add(partyBoardImage);
            }
        }
    }

    /* 모임 글 삭제 */
    @Transactional
    public void removePartyBoard(int partyBoardNumber) {

        partyBoardRepository.deleteById(partyBoardNumber);
    }

}
