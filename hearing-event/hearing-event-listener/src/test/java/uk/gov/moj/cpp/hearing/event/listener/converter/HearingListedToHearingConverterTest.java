package uk.gov.moj.cpp.hearing.event.listener.converter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class HearingListedToHearingConverterTest {

	private static final UUID HEARING_ID = UUID.randomUUID();
	private static final ZonedDateTime START_DATE_OF_HEARING = new UtcClock().now();

	private HearingInitiated event;
	private HearingEventsToHearingConverter converter;

	@Before
	public void setup() {
		event = new HearingInitiated(HEARING_ID, START_DATE_OF_HEARING, 1,"TRAIL");
		converter = new HearingEventsToHearingConverter();
	}

	@Test
	public void shouldConvertToHearing() {
		Hearing hearing = converter.convert(event);
		assertThat(hearing.getHearingId(), is(HEARING_ID));
		assertThat(hearing.getStartdate(), is(START_DATE_OF_HEARING.toLocalDate()));
		assertThat(hearing.getDuration(), is(1));
	}

}