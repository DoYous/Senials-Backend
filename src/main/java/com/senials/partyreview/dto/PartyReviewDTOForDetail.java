package com.senials.partyreview.dto;

import com.senials.user.dto.UserDTOForPublic;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PartyReviewDTOForDetail {

    private int partyReviewNumber;

    private UserDTOForPublic user;

    private int partyBoardNumber;

    private int partyReviewRate;

    private String partyReviewDetail;

    private LocalDateTime partyReviewWriteDate;

    /* AllArgsConstructor */
    @Builder
    public PartyReviewDTOForDetail(int partyReviewNumber, UserDTOForPublic user, int partyBoardNumber, int partyReviewRate, String partyReviewDetail, LocalDateTime partyReviewWriteDate) {
        this.partyReviewNumber = partyReviewNumber;
        this.user = user;
        this.partyBoardNumber = partyBoardNumber;
        this.partyReviewRate = partyReviewRate;
        this.partyReviewDetail = partyReviewDetail;
        this.partyReviewWriteDate = partyReviewWriteDate;
    }
}
