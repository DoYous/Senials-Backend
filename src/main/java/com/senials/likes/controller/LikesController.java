package com.senials.likes.controller;

import com.senials.common.ResponseMessage;
import com.senials.config.HttpHeadersFactory;
import com.senials.likes.service.LikesService;
import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LikesController {
    private final LikesService likesService;
    private final UserRepository userRepository;
    private final HttpHeadersFactory httpHeadersFactory;

    public LikesController(LikesService likesService, UserRepository userRepository, HttpHeadersFactory httpHeadersFactory) {
        this.likesService = likesService;
        this.userRepository = userRepository;
        this.httpHeadersFactory = httpHeadersFactory;
    }

    // 사용자가 좋아한 모임 목록
    @GetMapping("/users/{userNumber}/likes")
    public ResponseEntity<ResponseMessage> getLikedPartyBoards(@PathVariable int userNumber,
                                                                          @RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "9") int size) {
        List<PartyBoardDTOForCard> likedBoards = likesService.getLikedPartyBoardsByUserNumber(userNumber, page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        // 응답 데이터 생성
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("likesParties", likedBoards);
        responseMap.put("currentPage", page);
        responseMap.put("pageSize", size);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 만든 모임 조회 성공", responseMap));
    }


    /*사용자 별 좋아요 한 모임 개수*/
    @GetMapping("/users/{userNumber}/like/count")
    public ResponseEntity<ResponseMessage> countUserLikeParties(@PathVariable int userNumber) {
        long count = likesService.countLikesPartyBoardsByUserNumber(userNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("likesPartyCount", count);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 좋아요한 개수 조회 성공", responseMap));
    }

    //사용자가 좋아한 상태별 모임 목록
/*    @GetMapping("/{userNumber}/likes/{partyBoardStatus}")
    public ResponseEntity<List<PartyBoardDTOForCard>> getLikedPartyBoardsStatus(@PathVariable int userNumber) {
        List<PartyBoardDTOForCard> likedBoards = likesService.getLikedPartyBoardsByUserNumber(userNumber);
        return ResponseEntity.ok(likedBoards);
    }*/

    @PutMapping("/likes/partyBoards/{partyBoardNumber}")
    public ResponseEntity<ResponseMessage> toggleLike(
            @PathVariable int partyBoardNumber
    )
    {
        /* 유저 임의 지정 */
        Integer userNumber = 3;
        // userNumber = null;
        
        
        Integer code = 2;
        if(userNumber != null) {
            code = likesService.toggleLike(userNumber, partyBoardNumber);
        }

        
        String message = null;
        if(code == 1) {
            message = "좋아요";
        } else if(code == 0) {
            message = "좋아요 취소";
        } else {
            message = "로그인 필요";
        }
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", code);
        

        HttpHeaders headers = httpHeadersFactory.createJsonHeaders();
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, message, responseMap));
    }
}
