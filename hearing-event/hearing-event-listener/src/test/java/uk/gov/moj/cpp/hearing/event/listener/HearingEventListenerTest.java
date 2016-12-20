package uk.gov.moj.cpp.hearing.event.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventsToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

import java.time.ZonedDateTime;
import java.util.Optional;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

	@Mock
	private JsonObjectToObjectConverter jsonObjectToObjectConverter;

	@Mock
	private HearingEventsToHearingConverter hearingEventsToHearingConverter;

	@Mock
	private HearingRepository hearingRepository;

	@Mock
	private JsonEnvelope envelope;

	@Mock
	private HearingInitiated hearingInitiated;


	@Mock
	private Hearing hearing;

	@Mock
	private JsonObject payload;


	@InjectMocks
	private HearingEventListener peopleEventListener;

	@Test
	public void shouldHandleHearingCreatedEvent() throws Exception {

		when(envelope.payloadAsJsonObject()).thenReturn(payload);
		when(jsonObjectToObjectConverter.convert(payload, HearingInitiated.class)).thenReturn(hearingInitiated);
		when(hearingInitiated.getStartDateTime()).thenReturn(ZonedDateTime.now());
		when(hearingInitiated.getDuration()).thenReturn(1);
		when(hearingRepository.getByHearingId(hearing.geHearingId())).thenReturn(Optional.empty());
		when(hearingEventsToHearingConverter.convert(hearingInitiated)).thenReturn(hearing);
		peopleEventListener.hearingInitiated(envelope);
		verify(hearingRepository).save(hearing);

	}

}