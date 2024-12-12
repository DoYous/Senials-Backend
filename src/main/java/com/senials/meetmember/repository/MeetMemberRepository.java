package com.senials.meetmember.repository;

import com.senials.meet.entity.Meet;
import com.senials.meetmember.entity.MeetMember;
import com.senials.partymember.entity.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetMemberRepository extends JpaRepository<MeetMember, Integer> {

    /* 모임 일정 참여멤버 조회 */
    List<MeetMember> findAllByMeet(Meet meet);

    /* 모임 멤버 가입상태 확인 */
    MeetMember findByMeetAndPartyMember(Meet meet, PartyMember partyMember);

    /* 모임 일정 참가 여부 확인 */
    boolean existsByMeetAndPartyMember(Meet meet, PartyMember partyMember);
    boolean existsByMeet_MeetNumberAndPartyMember(int meetNumber, PartyMember partyMember);
}
