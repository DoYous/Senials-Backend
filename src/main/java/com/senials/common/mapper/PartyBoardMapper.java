package com.senials.common.mapper;

import com.senials.partyboard.dto.PartyBoardDTOForCard;
import com.senials.partyboard.dto.PartyBoardDTOForDetail;
import com.senials.partyboard.entity.PartyBoard;
import com.senials.partyboardimage.dto.PartyBoardImageDTO;
import com.senials.partyboardimage.entity.PartyBoardImage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartyBoardMapper {

    PartyBoardDTOForDetail toPartyBoardDTOForDetail(PartyBoard partyBoard);

    PartyBoardImageDTO toPartyBoardImageDTO(PartyBoardImage partyBoardImage);

    PartyBoardDTOForCard toPartyBoardDTOForCard(PartyBoard partyBoard);
}
