package com.senials.common.mapper;

import com.senials.partyreview.dto.PartyReviewDTO;
import com.senials.partyreview.dto.PartyReviewDTOForDetail;
import com.senials.partyreview.entity.PartyReview;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartyReviewMapper {

    /* PartyReview -> PartyReviewDTO */
    PartyReviewDTO toPartyReviewDTO(PartyReview partyReview);

    /* PartyReview -> PartyReviewDTOForDetail */
    PartyReviewDTOForDetail toPartyReviewDTOForDetail(PartyReview partyReview);

    /* PartyReviewDTO -> PartyReview */
    PartyReview toPartyReview(PartyReviewDTO partyReviewDTO);
}
