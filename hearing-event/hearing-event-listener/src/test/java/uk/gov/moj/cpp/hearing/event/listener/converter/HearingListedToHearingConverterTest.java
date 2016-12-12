package uk.gov.moj.cpp.hearing.event.listener.converter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class HearingListedToHearingConverterTest {

	private HearingInitiated event;
	private HearingEventsToHearingConverter convertor;
	private final UUID HEARING_ID = UUID.randomUUID();
	private final UUID CASE_ID = UUID.randomUUID();
	private static final ZonedDateTime START_DATE_OF_HEARING = ZonedDateTime.now();
	private static final LocalTime NOT_BEFORE = LocalTime.now();

	@Before
	public void setup() {
		event = new HearingInitiated(HEARING_ID, START_DATE_OF_HEARING, 1,"TRAIL");
		convertor = new HearingEventsToHearingConverter();
	}

	@Test
	public void shouldConvertToHearing() {
		Hearing hearing = convertor.convert(event);
		assertThat(hearing.geHearingId(), is(HEARING_ID));
		assertThat(hearing.getStartdate(), is(START_DATE_OF_HEARING.toLocalDate()));
		assertThat(hearing.getDuration(), is(1));
	}

}