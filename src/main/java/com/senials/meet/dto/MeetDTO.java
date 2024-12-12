package com.senials.meet.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MeetDTO {

    private int meetNumber;

    private int partyBoardNumber;

    private LocalDate meetStartDate;

    private LocalDate meetEndDate;

    private LocalTime meetStartTime;

    private LocalTime meetFinishTime;

    private int meetEntryFee;

    private String meetLocation;

    private int meetMaxMember;

    // 모임 참여 여부
    private boolean isJoined;

    // 모임 멤버 수
    private int meetMemberCnt;

    /* AllArgsConstructor */
    @Builder
    public MeetDTO(int meetNumber, int partyBoardNumber, LocalDate meetStartDate, LocalDate meetEndDate, LocalTime meetStartTime, LocalTime meetFinishTime, int meetEntryFee, String meetLocation, int meetMaxMember) {
        this.meetNumber = meetNumber;
        this.partyBoardNumber = partyBoardNumber;
        this.meetStartDate = meetStartDate;
        this.meetEndDate = meetEndDate;
        this.meetStartTime = meetStartTime;
        this.meetFinishTime = meetFinishTime;
        this.meetEntryFee = meetEntryFee;
        this.meetLocation = meetLocation;
        this.meetMaxMember = meetMaxMember;
    }
}
