package com.senials.partymember.controller;

import com.senials.common.ResponseMessage;
import com.senials.config.HttpHeadersFactory;
import com.senials.partymember.service.PartyMemberService;
import com.senials.user.dto.UserDTOForPublic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PartyMemberController {

    private Integer loggedInUserNumber = 3;
    private final HttpHeadersFactory httpHeadersFactory;
    private final PartyMemberService partyMemberService;


    public PartyMemberController(
            PartyMemberService partyMemberService,
            HttpHeadersFactory httpHeadersFactory) {
        this.partyMemberService = partyMemberService;
        this.httpHeadersFactory = httpHeadersFactory;
    }


    /* 모임 멤버 전체 조회 */
    @GetMapping("/partyboards/{partyBoardNumber}/partymembers")
    public ResponseEntity<ResponseMessage> getPartyMembers (
            @PathVariable Integer partyBoardNumber
    ) {
        List<UserDTOForPublic> userDTOForPublicList = partyMemberService.getPartyMembers(partyBoardNumber);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("partyMembers", userDTOForPublicList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "모임 멤버 전체 조회 성공", responseMap));
    }

    /* 모임 참가 */
    @PostMapping("/partyboards/{partyBoardNumber}/partymembers")
    public ResponseEntity<ResponseMessage> registerPartyMember (
            @PathVariable Integer partyBoardNumber
    ) {
        // 유저 번호 임의 지정

        int code = 2;
        if(loggedInUserNumber != null) {
            code = partyMemberService.registerPartyMember(loggedInUserNumber, partyBoardNumber);
        }

        String message = null;
        if(code == 1) {
            message = "참가 성공";
        } else if(code == 0) {
            message = "요청 실패 (관리자에게 문의)";
        } else {
            message = "로그인 필요";
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, message, responseMap));
    }

    /* 모임 탈퇴 */
    @DeleteMapping("/partyboards/{partyBoardNumber}/partymembers")
    public ResponseEntity<ResponseMessage> unregisterPartyMember (
            @PathVariable Integer partyBoardNumber
    ) {


        int code = 0;
        if(loggedInUserNumber != null) {
            partyMemberService.unregisterPartyMember(loggedInUserNumber, partyBoardNumber);
            code = 1;
        }

        String message = null;
        if(code == 1) {
            message = "나가기 성공";
        } else {
            message = "요청 실패 (관리자에게 문의)";
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, message, responseMap));
    }
}
