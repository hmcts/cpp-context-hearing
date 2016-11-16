package uk.gov.moj.cpp.hearing.event.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacated;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingListedToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

	@Mock
	private JsonObjectToObjectConverter jsonObjectToObjectConverter;

	@Mock
	private HearingListedToHearingConverter hearingListedToHearingConverter;

	@Mock
	private HearingRepository hearingRepository;

	@Mock
	private JsonEnvelope envelope;

	@Mock
	private HearingListed hearingListed;
	
	@Mock
	private HearingVacated hearingVacated;

	@Mock
	private Hearing hearing;

	@Mock
	private JsonObject payload;


	@InjectMocks
	private HearingListedEventListener peopleEventListener;

	@Test
	public void shouldHandleHearingListedEvent() throws Exception {

		when(envelope.payloadAsJsonObject()).thenReturn(payload);
		when(jsonObjectToObjectConverter.convert(payload, HearingListed.class)).thenReturn(hearingListed);
		when(hearingListed.getStartDateOfHearing()).thenReturn(LocalDate.now());
		when(hearingListed.getDuration()).thenReturn(1);
		when(hearingListed.getCourtCentreName()).thenReturn("courtCentreName");
		when(hearingListed.getHearingType()).thenReturn(HearingTypeEnum.PTP);
		when(hearingListedToHearingConverter.convert(hearingListed)).thenReturn(hearing);
		peopleEventListener.hearingListed(envelope);
		verify(hearingRepository).save(hearing);

	}
	
	@Test
	public void shouldHandleHearingVacatedEvent() throws Exception {

		when(envelope.payloadAsJsonObject()).thenReturn(payload);
		when(payload.getString("hearingId")).thenReturn("6daefec6-5f78-4109-82d9-1e60544a6c01");
		when(hearingRepository.findByHearingId(UUID.fromString("6daefec6-5f78-4109-82d9-1e60544a6c01"))).thenReturn(hearing);
		peopleEventListener.hearingVacated(envelope);
		verify(hearingRepository).save(hearing);

	}
}