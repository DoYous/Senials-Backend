package com.senials.user.controller;

import com.senials.common.ResponseMessage;
import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.partyboardimage.dto.FileDTO;
import com.senials.user.dto.UserCommonDTO;
import com.senials.user.dto.UserDTO;
import com.senials.user.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping
public class UserController {
    private UserService userService;
    private final ResourceLoader resourceLoader;

    public UserController(UserService userService, ResourceLoader resourceLoader) {

        this.userService = userService;
        this.resourceLoader = resourceLoader;
    }
    // 모든 사용자 조회
    @GetMapping("/users")
    public ResponseEntity<ResponseMessage> getAllUsers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        List<UserDTO> users = userService.getAllUsers();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", users);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "전체 사용자 조회 성공", responseMap));
    }

    // 특정 사용자 조회
    @GetMapping("users/{userNumber}")
    public ResponseEntity<ResponseMessage> getUserByNumber(@PathVariable int userNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        // userNumber로 데이터 조회
        UserCommonDTO user = userService.getUserByNumber(userNumber);

        if (user == null) {
            return ResponseEntity.status(404)
                    .headers(headers)
                    .body(new ResponseMessage(404, "사용자를 찾을 수 없습니다.", null));
        }

        // 응답 생성 (userNumber는 제외됨)
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("user", user);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자 조회 성공", responseMap));
    }

    //특정 사용자 탈퇴
    @DeleteMapping("users/{userNumber}")
    public ResponseEntity<ResponseMessage> deleteUser(@PathVariable int userNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        boolean isDeleted = userService.deleteUser(userNumber);

        if (!isDeleted) {
            return ResponseEntity.status(404)
                    .headers(headers)
                    .body(new ResponseMessage(404, "회원 탈퇴 실패: 사용자를 찾을 수 없습니다.", null));
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "회원 탈퇴 성공", null));
    }

    // 특정 사용자 수정 put
    @PutMapping("users/{userNumber}/modify")
    public ResponseEntity<ResponseMessage> updateUserProfile(
            @PathVariable int userNumber,
            @RequestBody Map<String, String> updatedFields) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        boolean isUpdated = userService.updateUserProfile(
                userNumber,
                updatedFields.get("userNickname"),
                updatedFields.get("userDetail")
                /*, updatedFields.get("userProfileImg")*/
        );

        if (!isUpdated) {
            return ResponseEntity.status(404)
                    .headers(headers)
                    .body(new ResponseMessage(404, "사용자를 찾을 수 없습니다.", null));
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자 프로필 수정 성공", null));
    }

    // 사용자 프로필 출력
    @GetMapping("/img/userProfile/{userNumber}")
    public ResponseEntity<Resource> getUserImage(@PathVariable String userNumber){
        try{
            //확장자 동적 확인
            String[] extensions = {".png", ".jpg", ".jpeg"};
            Resource resource = null;
            for (String ext : extensions){
                resource = resourceLoader.getResource("classpath:static/img/user_profile/" + userNumber + ext);
                if (resource.exists()){
                    break;
                }
            }

            if (resource != null && resource.exists() && resource.isReadable()){
                String contentType = "image/png";//기본 MIME 타입 설정
                if(resource.getFilename().endsWith(".jpg") || resource.getFilename().endsWith("jpeg")){
                    contentType = "image/jpeg";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            }else {
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    //사용자 프로필 원래꺼는 삭제, 새로 등록된거 DB 에 추가
    // 사용자 프로필 이미지 업로드
    @PostMapping("users/{userNumber}/profile/upload")
    public ResponseEntity<ResponseMessage> uploadProfileImage(
            @PathVariable int userNumber,
            @RequestParam("profileImage") MultipartFile profileImage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        try {
            // 기존 이미지 삭제
            String[] extensions = {".png", ".jpg", ".jpeg"};
            for (String ext : extensions) {
                File existingFile = new File("src/main/resources/static/img/user_profile/" + userNumber + ext);
                if (existingFile.exists()) {
                    existingFile.delete();
                    break;
                }
            }

            // 새 이미지 저장
            String newFileName = userNumber + "." + getFileExtension(profileImage.getOriginalFilename());
            File newFile = new File("src/main/resources/static/img/user_profile/" + newFileName);
            profileImage.transferTo(newFile);

            // 데이터베이스에 새로운 이미지 경로 저장
            userService.updateUserProfileImage(userNumber, newFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new ResponseMessage(200, "프로필 사진 업로드 성공", null));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .headers(headers)
                    .body(new ResponseMessage(500, "파일 저장 중 오류가 발생했습니다.", null));
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }



    //사용자 별 참여한 모임 출력
    // 사용자별 참여 모임 목록 조회
    @GetMapping("users/{userNumber}/parties")
    public ResponseEntity<ResponseMessage> getUserJoinedPartyBoards(
            @PathVariable int userNumber,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        // Service 호출
        List<PartyBoardDTOForCard> joinedParties = userService.getJoinedPartyBoardsByUserNumber(userNumber, page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (joinedParties.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ResponseMessage(404, "사용자가 참여한 모임이 없습니다.", null));
        }

        // 응답 데이터 생성
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("joinedParties", joinedParties);
        responseMap.put("currentPage", page);
        responseMap.put("pageSize", size);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 참여한 모임 조회 성공", responseMap));
    }

    //사용자 별 자신이 만든 모임 조회
    @GetMapping("users/{userNumber}/made")
    public ResponseEntity<ResponseMessage> getUserMadeParties(
            @PathVariable int userNumber,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {

        // Service 호출
        List<PartyBoardDTOForCard> madeParties = userService.getMadePartyBoardsByUserNumber(userNumber, page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (madeParties.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ResponseMessage(404, "사용자가 만든 모임이 없습니다.", null));
        }

        // 응답 데이터 생성
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("madeParties", madeParties);
        responseMap.put("currentPage", page);
        responseMap.put("pageSize", size);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 만든 모임 조회 성공", responseMap));
    }


    /*모임 개수 api*/

    /*사용자 별 참여한 모임 개수*/
    @GetMapping("users/{userNumber}/parties/count")
    public ResponseEntity<ResponseMessage> countUserJoinedParties(@PathVariable int userNumber) {
        long count = userService.countPartiesPartyBoardsByUserNumber(userNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("partiesPartyCount", count);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 참여한 모임 개수 조회 성공", responseMap));
    }
  
    /*사용자 별 만든 모임 개수*/
    @GetMapping("users/{userNumber}/made/count")
    public ResponseEntity<ResponseMessage> countUserMadeParties(@PathVariable int userNumber) {
        long count = userService.countMadePartyBoardsByUserNumber(userNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("madePartyCount", count);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ResponseMessage(200, "사용자가 만든 모임 개수 조회 성공", responseMap));

    }

}
