package com.senials.partyboard.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class PartyBoardDTOForCard {
    private int partyBoardNumber;
    private String partyBoardName;
    private int partyBoardStatus;
    private long memberCount; // 참여 인원 수
    private double averageRating; // 별점 평균
    private String firstImage; // 첫 번째 이미지 경로
    private int reviewCount;    // 모임 후기 수
    private boolean isLiked;    // 좋아요 여부

    /* AllArgsConstructor */
    public PartyBoardDTOForCard(
            int partyBoardNumber
            , String partyBoardName
            , int partyBoardStatus
            , long memberCount
            , double averageRating
            , String firstImage
    )
    {
        this.partyBoardNumber = partyBoardNumber;
        this.partyBoardName = partyBoardName;
        this.partyBoardStatus = partyBoardStatus;
        this.memberCount = memberCount;
        this.averageRating = averageRating;
        this.firstImage = firstImage;
    }
}

