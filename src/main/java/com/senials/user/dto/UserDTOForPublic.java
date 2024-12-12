package com.senials.user.dto;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserDTOForPublic {

    private int userNumber;

    private String userName;

    private LocalDate userBirth;

    // private int userGender;

    private int userStatus;

    private String userNickname;

    private String userDetail;

    private String userProfileImg;

    private LocalDate userSignupDate;

    /* AllArgsConstructor */
    @Builder
    public UserDTOForPublic(int userNumber, String userName, LocalDate userBirth, int userStatus, String userNickname, String userDetail, String userProfileImg, LocalDate userSignupDate) {
        this.userNumber = userNumber;
        this.userName = userName;
        this.userBirth = userBirth;
        this.userStatus = userStatus;
        this.userNickname = userNickname;
        this.userDetail = userDetail;
        this.userProfileImg = userProfileImg;
        this.userSignupDate = userSignupDate;
    }
}

