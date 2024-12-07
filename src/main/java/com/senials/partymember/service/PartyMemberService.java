package com.senials.partymember.service;

import com.senials.common.mapper.UserMapper;
import com.senials.common.mapper.UserMapperImpl;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboard.repository.PartyBoardRepository;
import com.senials.partymember.entity.PartyMember;
import com.senials.partymember.repository.PartyMemberRepository;
import com.senials.user.dto.UserDTOForPublic;
import com.senials.user.entity.User;
import com.senials.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartyMemberService {

    private final UserMapper userMapper;
    private final PartyBoardRepository partyBoardRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final UserRepository userRepository;

    public PartyMemberService(
            UserMapperImpl userMapperImpl
            , PartyBoardRepository partyBoardRepository
            , PartyMemberRepository partyMemberRepository,
            UserRepository userRepository) {
        this.userMapper = userMapperImpl;
        this.partyBoardRepository = partyBoardRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.userRepository = userRepository;
    }

    /* 모임 멤버 전체 조회 */
    public List<UserDTOForPublic> getPartyMembers (int partyBoardNumber) {

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        /* 모임 번호에 해당하는 멤버 리스트 도출 */
        List<PartyMember> partyMemberList = partyMemberRepository.findAllByPartyBoard(partyBoard);

        /* 멤버 리스트를 통해 유저 정보 도출 */
        List<UserDTOForPublic> userDTOForPublicList = partyMemberList.stream()
                .map(partyMember -> userMapper.toUserDTOForPublic(partyMember.getUser()))
                .toList();

        return userDTOForPublicList;
    }


    /* 모임 참가 */
    @Transactional
    public void registerPartyMember (int userNumber, int partyBoardNumber) {
        User user = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);

        PartyBoard partyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        PartyMember newMember = new PartyMember(0, partyBoard, user);

        partyBoard.registerPartyMember(newMember);

    }


    /* 모임 탈퇴 */
    @Transactional
    public void unregisterPartyMember (int userNumber, int partyBoardNumber) {

        PartyBoard targetPartyBoard = partyBoardRepository.findById(partyBoardNumber)
                .orElseThrow(IllegalArgumentException::new);

        User targetUser = userRepository.findById(userNumber)
                .orElseThrow(IllegalArgumentException::new);


        PartyMember targetPartyMember = partyMemberRepository.findByPartyBoardAndUser(targetPartyBoard, targetUser);


        partyMemberRepository.delete(targetPartyMember);

    }
}
