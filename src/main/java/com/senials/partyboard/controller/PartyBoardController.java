package com.senials.partyboard.controller;

import com.senials.common.ResponseMessage;
import com.senials.config.HttpHeadersFactory;
import com.senials.hobbyboard.service.HobbyService;
import com.senials.likes.service.LikesService;
import com.senials.partyboard.dto.*;
import com.senials.partyboard.service.PartyBoardService;
import com.senials.partymember.service.PartyMemberService;
import com.senials.partyreview.dto.PartyReviewDTOForDetail;
import com.senials.partyreview.service.PartyReviewService;
import com.senials.user.dto.UserDTOForPublic;
import com.senials.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PartyBoardController {


    private final LikesService likesService;
    private final PartyMemberService partyMemberService;
    private final PartyReviewService partyReviewService;
    private Integer loggedInUserNumber = 3;
    private final PartyBoardService partyBoardService;
    private final HttpHeadersFactory httpHeadersFactory;
    private final UserService userService;
    private final HobbyService hobbyService;


    @Autowired
    public PartyBoardController(
            PartyBoardService partyBoardService
            , HttpHeadersFactory httpHeadersFactory
            , UserService userService,
            HobbyService hobbyService, LikesService likesService, PartyMemberService partyMemberService, PartyReviewService partyReviewService)
    {
        this.partyBoardService = partyBoardService;
        this.httpHeadersFactory = httpHeadersFactory;
        this.userService = userService;
        this.hobbyService = hobbyService;
        this.likesService = likesService;
        this.partyMemberService = partyMemberService;
        this.partyReviewService = partyReviewService;
    }

    /* 같은 취미 추천 모임 (상세 페이지 최하단) */
    @GetMapping("/partyboards/recommended-parties")
    public ResponseEntity<ResponseMessage>  getRecommendPartyBoards(
            @RequestParam int partyBoardNumber
    ) {
        List<PartyBoardDTOForCard> recommendedPartyBoards = partyBoardService.getRecommendedPartyBoards(loggedInUserNumber, partyBoardNumber);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("recommendedPartyBoards", recommendedPartyBoards);

        HttpHeaders headers = httpHeadersFactory.createJsonHeaders();
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "취미 기반 추천 모임 조회 성공", responseMap));
    }


    /* 인기 추천 모임 (평점 높은 순, 리뷰 개수 minReviewCount개 이상, 모집중 >> size개 제한)*/
    @GetMapping("/partyboards/popular-parties")
    public ResponseEntity<ResponseMessage> getPopularPartyBoards(
            @RequestParam(required = false, defaultValue = "1") Integer minReviewCount
            , @RequestParam(required = false, defaultValue = "4") Integer size
            , @RequestParam(required = false, defaultValue = "0") Integer pageNumber
    ) {

        List<PartyBoardDTOForCard> partyBoardDTOForCardList = partyBoardService.getPopularPartyBoards(minReviewCount, size, pageNumber);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("popularPartyBoards", partyBoardDTOForCardList);

        HttpHeaders headers = httpHeadersFactory.createJsonHeaders();
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "인기 추천 모임 조회 성공", responseMap));

    }


    // 모임 검색
    // 쿼리스트링
    // 1. sortMethod : 검색결과 정렬기준 (기본 - 최신순; lastest)
    // 2. keyword : 검색어
    // 3. cursor : 클라이언트가 마지막으로 받은 검색결과 partyBoardNumber (마지막 검색결과 다음 행부터 N개 게시글 Response)
    // 4. size : 한 번에 요청할 데이터 개수 (기본 - 8)
    // 5. likedOnly : 관심사로 등록한 취미와 일치하는 모임만 필터링
    @GetMapping("/partyboards/search")
    public ResponseEntity<ResponseMessage> searchPartyBoard(
            @RequestParam(required = false, defaultValue = "lastest") String sortMethod
            , @RequestParam(required = false) String keyword
            , @RequestParam(required = false) Integer cursor
            , @RequestParam(required = false, defaultValue = "8") Integer size
            , @RequestParam(required = false, defaultValue = "false") boolean isLikedOnly
    )
    {
        /* isLikedOnly 유저 세션 검사 필요 */
        Integer userNumber = 3;

        /* 더보기 버튼 출력 여부 확인 용 데이터 + 1 */
        List<PartyBoardDTOForCard> partyBoardDTOList = partyBoardService.searchPartyBoard(sortMethod, keyword, cursor, size + 1, isLikedOnly, userNumber);


        Map<String, Object> responseMap = new HashMap<>();


        /* 남은 정보 존재 여부 설정 - 가져온 데이터가 페이지 별 최대 정보 수(size)보다 같거나 작을 경우 false */
        boolean isRemain = true;
        if(partyBoardDTOList.size() <= size) {
            isRemain = false;
        } else {
            // 남은 정보 존재 시 마지막 PartyBoard 제거
            partyBoardDTOList.remove(partyBoardDTOList.size() - 1);
        }
        responseMap.put("isRemain", isRemain);
        responseMap.put("partyBoards", partyBoardDTOList);


        /* 마지막 PartyBoardNumber */
        Integer nextCursor = null;
        if (!partyBoardDTOList.isEmpty()) {
            nextCursor = partyBoardDTOList.get(partyBoardDTOList.size() - 1).getPartyBoardNumber();
        }
        responseMap.put("cursor", nextCursor);


        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "조회 성공", responseMap));
    }


    // 모임 상세 조회
    @GetMapping("/partyboards/{partyBoardNumber}")
    public ResponseEntity<ResponseMessage> getPartyBoardByNumber(@PathVariable Integer partyBoardNumber) {

        Integer tempUserNumber = 3;


        PartyBoardDTOForDetail partyBoardDTO = partyBoardService.getPartyBoardByNumber(partyBoardNumber);

        // 신고 수 마스킹
        partyBoardDTO.setPartyBoardReportCnt(0);

        // 모임장 정보 불러오기
        UserDTOForPublic masterUserDTO = userService.getUserPublicByNumber(partyBoardDTO.getUserNumber());


        // 로그인 유저 정보 불러오기 + 좋아요 여부
        UserDTOForPublic loggedInUserDTO = null;
        boolean isLiked = false;
        boolean isMember = false;
        boolean isMaster = false;
        if(tempUserNumber != null) {
            loggedInUserDTO = userService.getUserPublicByNumber(tempUserNumber);
            isLiked = likesService.isLikedByPartyBoardNumber(tempUserNumber, partyBoardNumber);
            isMember = partyMemberService.checkIsMember(tempUserNumber, partyBoardNumber);
            isMaster = masterUserDTO.getUserNumber() == loggedInUserDTO.getUserNumber();
        }


        // 내가 작성한 후기 불러오기
        PartyReviewDTOForDetail myReview = partyReviewService.getOnePartyReview(loggedInUserNumber, partyBoardNumber);


        // 초기 로딩용 랜덤 모임 멤버 4명 불러오기
        List<UserDTOForPublic> randMembers = partyMemberService.getRandomPartyMembers(partyBoardNumber);


        // ResponseBody 삽입
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("partyBoard", partyBoardDTO);
        responseMap.put("partyMaster", masterUserDTO);
        responseMap.put("isLoggedIn", tempUserNumber != null);
        responseMap.put("loggedInUser", loggedInUserDTO);
        responseMap.put("isLiked", isLiked);
        responseMap.put("isMember", isMember);
        responseMap.put("isMaster", isMaster);
        responseMap.put("myReview", myReview);
        responseMap.put("randMembers", randMembers);


        // ResponseHeader 설정
        HttpHeaders headers = httpHeadersFactory.createJsonHeaders();
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "조회 성공", responseMap));
    }


    /* 모임 글 작성 */
    @PostMapping("/partyboards")
    public ResponseEntity<ResponseMessage> registerPartyBoard(
            @RequestPart("imageFiles") List<MultipartFile> imageFiles
            , @ModelAttribute PartyBoardDTOForWrite newPartyBoardDTO
    ) {

        // 유저 번호 임의 지정
        int userNumber = 3;

        int newPartyBoardNumber = partyBoardService.registerPartyBoard(userNumber, imageFiles, newPartyBoardDTO);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("partyBoardNumber", newPartyBoardNumber);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "글 작성 성공", responseMap));

    }


    /* 모임 글 수정 */
    @PutMapping("/partyboards/{partyBoardNumber}")
    public ResponseEntity<ResponseMessage> modifyPartyBoard(
            @PathVariable int partyBoardNumber,
            @ModelAttribute PartyBoardDTOForModify partyBoardDTO
    ) {
        // PathVariable의 partyBoardNumber를 DTO에 삽입
        partyBoardDTO.setPartyBoardNumber(partyBoardNumber);

        // form 태그로 테스트할 때 공백이 리스트에 삽입되는 것 방지
        // partyBoardDTO.getRemovedFileNumbers().removeAll(partyBoardDTO.getRemovedFileNumbers());
        // partyBoardDTO.getAddedFiles().removeAll(partyBoardDTO.getAddedFiles());

        partyBoardService.modifyPartyBoard(partyBoardDTO);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "글 수정 성공", null));
    }


    /* 모임 글 삭제 */
    @DeleteMapping("/partyboards/{partyBoardNumber}")
    public ResponseEntity<ResponseMessage> removePartyBoard(
            @PathVariable int partyBoardNumber
    ) {

        partyBoardService.removePartyBoard(partyBoardNumber);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "글 삭제 성공", null));
    }

}
