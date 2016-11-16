package uk.gov.moj.cpp.hearing.event.listener.converter;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

public class HearingListedToHearingConverter implements Converter<HearingListed, Hearing> {

	@Override
	public Hearing convert(HearingListed event) {
		 final Hearing hearing = new Hearing();
	        hearing.setHearingId(event.getHearingId());
	        hearing.setCaseId(event.getCaseId());
	        hearing.setHearingType(event.getHearingType());
	        hearing.setStartDate(event.getStartDateOfHearing());
	        hearing.setDuration(event.getDuration());
	        hearing.setCourtCentreName(event.getCourtCentreName());
	        return hearing;
	}

}
