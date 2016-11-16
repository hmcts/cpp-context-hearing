package uk.gov.moj.cpp.hearing.event.listener.converter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

public class HearingListedToHearingConverterTest {

	private HearingListed event;
	private HearingListedToHearingConverter convertor;
	private final UUID HEARING_ID = UUID.randomUUID();
	private final UUID CASE_ID = UUID.randomUUID();
	private static final LocalDate startDateOfHearing = LocalDate.now();

	@Before
	public void setup() {
		event = new HearingListed(HEARING_ID, CASE_ID, HearingTypeEnum.PTP, "courtCentreName", startDateOfHearing, 1);
		convertor = new HearingListedToHearingConverter();
	}

	@Test
	public void shouldConvertToHearing() {
		Hearing hearing = convertor.convert(event);
		assertThat(hearing.geHearingtId(), is(HEARING_ID));
		assertThat(hearing.getCaseId(), is(CASE_ID));
		assertThat(hearing.getHearingType(), is(HearingTypeEnum.PTP));
		assertThat(hearing.getStartDate(), is(startDateOfHearing));
		assertThat(hearing.getDuration(), is(1));
	}

}