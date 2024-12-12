package com.senials.meet.service;

import com.senials.common.mapper.MeetMapper;
import com.senials.common.mapper.MeetMapperImpl;
import com.senials.meet.dto.MeetDTO;
import com.senials.meet.entity.Meet;
import com.senials.meet.repository.MeetRepository;
import com.senials.meetmember.repository.MeetMemberRepository;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partymember.entity.PartyMember;
import com.senials.partymember.repository.PartyMemberRepository;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetService {

    private final MeetMapper meetMapper;

    private final PartyBoardRepository partyBoardRepository;

    private final MeetRepository meetRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;
    private final MeetMemberRepository meetMemberRepository;


    @Autowired
    public MeetService(
            MeetMapperImpl meetMapperImpl
            , PartyBoardRepository partyBoardRepository
            , MeetRepository meetRepository,
            PartyMemberRepository partyMemberRepository, UserRepository userRepository, MeetMemberRepository meetMemberRepository) {
        this.meetMapper = meetMapperImpl;
        this.partyBoardRepository = partyBoardRepository;
        this.meetRepository = meetRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.userRepository = userRepository;
        this.meetMemberRepository = meetMemberRepository;
    }

    /* 모임 내 일정 개수 확인 */
    public int countMeets(int partyBoardNumber) {
        return meetRepository.countAllByPartyBoard_PartyBoardNumber(partyBoardNumber);
    }


    /* 모임 내 일정 전체 조회 */
    public List<MeetDTO> getMeetsByPartyBoardNumber(Integer userNumber, int partyBoardNumber, int pageNumber, int pageSize) {

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        Page<Meet> meetList = meetRepository.findAllByPartyBoard(partyBoard, PageRequest.of(pageNumber, pageSize, Sort.by("meetNumber").descending()));

        List<MeetDTO> meetDTOList = meetList.stream().map(meet -> {
            MeetDTO meetDTO = meetMapper.toMeetDTO(meet);
            meetDTO.setMeetMemberCnt(meet.getMeetMembers().size());

            return meetDTO;
        }).collect(Collectors.toList());

        // 로그인 여부 확인
        if(userNumber != null) {
            User user = userRepository.findById(userNumber).orElseThrow(RuntimeException::new);
            PartyMember partyMember = partyMemberRepository.findByPartyBoardAndUser(partyBoard, user);

            // 모임 멤버일 시 일정 별 참여 여부 설정
            if(partyMember != null) {
                for (MeetDTO meetDTO : meetDTOList) {
                    meetDTO.setJoined(meetMemberRepository.existsByMeet_MeetNumberAndPartyMember(meetDTO.getMeetNumber(), partyMember));
                }
            }
        }

        return meetDTOList;
    }


    /* 모임 일정 추가 */
    @Transactional
    public void registerMeet(int partyBoardNumber, MeetDTO meetDTO) {

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        Meet meet = meetMapper.toMeet(meetDTO);

        /* PartyBoard 엔티티 연결 */
        meet.initializePartyBoard(partyBoard);

        meetRepository.save(meet);
    }

  
    public List<MeetDTO> getMeetsByUserNumber(int userNumber) {
        List<Meet> meets = meetRepository.findAllByUserNumber(userNumber);
        return meets.stream()
                .map(meet -> new MeetDTO(
                        meet.getMeetNumber(),
                        meet.getPartyBoard().getPartyBoardNumber(),
                        meet.getMeetStartDate(),
                        meet.getMeetEndDate(),
                        meet.getMeetStartTime(),
                        meet.getMeetFinishTime(),
                        meet.getMeetEntryFee(),
                        meet.getMeetLocation(),
                        meet.getMeetMaxMember()
                ))
                .collect(Collectors.toList());
    }


    /* 모임 일정 수정 */
    @Transactional
    public void modifyMeet(int meetNumber, MeetDTO meetDTO) {
        /* 기존 일정 엔티티 로드 */
        Meet meet = meetRepository.findById(meetNumber)
                .orElseThrow(IllegalArgumentException::new);


        /* 컬럼 변경 별도 검증 없이 모두 갱신*/
        meet.updateAll(meetDTO);
    }


    /* 모임 일정 삭제 */
    @Transactional
    public void removeMeet(int meetNumber) {

        meetRepository.deleteById(meetNumber);

    }

}
