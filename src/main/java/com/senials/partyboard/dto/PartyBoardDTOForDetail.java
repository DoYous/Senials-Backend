package com.senials.partyboard.dto;

import com.senials.partyboardimage.dto.PartyBoardImageDTO;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PartyBoardDTOForDetail {

    private int partyBoardNumber; // auto

    private int userNumber;

    private int hobbyNumber;

    private String partyBoardName;

    private String partyBoardDetail;

    private LocalDate partyBoardOpenDate; // now()

    private int partyBoardStatus; // 0-모집중, 1-모집완료

    private int partyBoardViewCnt;

    private int partyBoardLikeCnt;

    private int partyBoardReportCnt;

    // 이미지 명
    private List<PartyBoardImageDTO> images;

    // 카테고리 명
    private String categoryName;

    // 멤버 수
    private int partyMemberCnt;

    /* AllArgsConstructor */
    @Builder
    public PartyBoardDTOForDetail(int partyBoardNumber, int userNumber, int hobbyNumber, String partyBoardName, String partyBoardDetail, LocalDate partyBoardOpenDate, int partyBoardStatus, int partyBoardViewCnt, int partyBoardLikeCnt, int partyBoardReportCnt, List<PartyBoardImageDTO> images, String categoryName, int partyMemberCnt) {
        this.partyBoardNumber = partyBoardNumber;
        this.userNumber = userNumber;
        this.hobbyNumber = hobbyNumber;
        this.partyBoardName = partyBoardName;
        this.partyBoardDetail = partyBoardDetail;
        this.partyBoardOpenDate = partyBoardOpenDate;
        this.partyBoardStatus = partyBoardStatus;
        this.partyBoardViewCnt = partyBoardViewCnt;
        this.partyBoardLikeCnt = partyBoardLikeCnt;
        this.partyBoardReportCnt = partyBoardReportCnt;
        this.images = images;
        this.categoryName = categoryName;
        this.partyMemberCnt = partyMemberCnt;
    }
}
