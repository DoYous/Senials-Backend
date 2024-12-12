package com.senials.meet.controller;

import com.senials.common.ResponseMessage;
import com.senials.meet.dto.MeetDTO;
import com.senials.meet.repository.MeetRepository;
import com.senials.meet.service.MeetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MeetController {

    private Integer loggedInUserNumber = 3;
    private final MeetService meetService;
    private final MeetRepository meetRepository;


    @Autowired
    public MeetController(MeetService meetService, MeetRepository meetRepository) {
        this.meetService = meetService;
        this.meetRepository = meetRepository;
    }


    /* 모임 내 일정 전체 조회 (로그인 중일시 참여여부 까지) */
    @GetMapping("/partyboards/{partyBoardNumber}/meets")
    public ResponseEntity<ResponseMessage> getMeetsByPartyBoardNumber(
            @PathVariable Integer partyBoardNumber
            , @RequestParam(required = false, defaultValue = "4") Integer pageSize
            , @RequestParam(required = false, defaultValue = "0") Integer pageNumber
    ) {

        List<MeetDTO> meetDTOList = meetService.getMeetsByPartyBoardNumber(loggedInUserNumber, partyBoardNumber, pageNumber, pageSize);
        int totalCnt = meetService.countMeets(partyBoardNumber);

        Map<String, Object> responseMap = new HashMap<>();
        boolean hasMore = (pageNumber + 1) * pageSize < totalCnt;

        responseMap.put("meets", meetDTOList);
        responseMap.put("hasMore", hasMore);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "일정 전체 조회 완료", responseMap));
    }

    /* 모임 일정 추가 */
    @PostMapping("/partyboards/{partyBoardNumber}/meets")
    public ResponseEntity<ResponseMessage> registerMeet(
            @PathVariable Integer partyBoardNumber,
            @RequestBody MeetDTO meetDTO
    ) {
        meetService.registerMeet(partyBoardNumber, meetDTO);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "일정 추가 완료", null));
    }


    /* 모임 일정 수정 */
    /* meetNumber 만으로 고유하기 때문에 partyBoardNumber는 필요없음 */
    @PutMapping("/partyboards/{partyBoardNumber}/meets/{meetNumber}")
    public ResponseEntity<ResponseMessage> modifyMeet (
            @PathVariable Integer meetNumber
            , @RequestBody MeetDTO meetDTO
    ) {
        meetService.modifyMeet(meetNumber, meetDTO);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "일정 수정 완료", null));
    }


    /* 모임 일정 삭제 */
    @DeleteMapping("/partyboards/{partyBoardNumber}/meets/{meetNumber}")
    public ResponseEntity<ResponseMessage> removeMeet (
            @PathVariable Integer meetNumber
    ) {
        meetService.removeMeet(meetNumber);

        // ResponseHeader 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        return ResponseEntity.ok().headers(headers).body(new ResponseMessage(200, "일정 삭제 완료", null));
    }
        
    @GetMapping("/users/{userNumber}/meets")
    public ResponseEntity<List<MeetDTO>> getUserMeets(@PathVariable int userNumber) {
        List<MeetDTO> meets = meetService.getMeetsByUserNumber(userNumber);
        return ResponseEntity.ok(meets);
        
    }
}
