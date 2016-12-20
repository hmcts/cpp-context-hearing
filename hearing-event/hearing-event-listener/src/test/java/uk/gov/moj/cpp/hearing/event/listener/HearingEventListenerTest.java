package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingEventsToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.ProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

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
    private ProsecutionCounselRepository prosecutionCounselRepository;

	@Mock
	private JsonEnvelope envelope;

	@Mock
	private HearingInitiated hearingInitiated;

    @Mock
    private ProsecutionCounselAdded prosecutionCounselAdded;

	@Mock
	private Hearing hearing;

    @Mock
    private ProsecutionCounsel prosecutionCounsel;


    @Mock
	private JsonObject payload;


	@InjectMocks
	private HearingEventListener hearingEventListener;

	@Test
	public void shouldHandleHearingCreatedEvent() throws Exception {

		when(envelope.payloadAsJsonObject()).thenReturn(payload);
		when(jsonObjectToObjectConverter.convert(payload, HearingInitiated.class)).thenReturn(hearingInitiated);
		when(hearingInitiated.getStartDateTime()).thenReturn(ZonedDateTime.now());
		when(hearingInitiated.getDuration()).thenReturn(1);
		when(hearingRepository.getByHearingId(hearing.geHearingId())).thenReturn(Optional.empty());
		when(hearingEventsToHearingConverter.convert(hearingInitiated)).thenReturn(hearing);
		hearingEventListener.hearingInitiated(envelope);
		verify(hearingRepository).save(hearing);

	}

	@Test
	public void shouldHandleProsecutionCounselAddedEvent() throws Exception {
		when(envelope.payloadAsJsonObject()).thenReturn(payload);

		final ProsecutionCounselAdded prosecutionCounselAdded =
                new ProsecutionCounselAdded(randomUUID(), randomUUID(), randomUUID(), STRING.next());

		when(jsonObjectToObjectConverter.convert(payload, ProsecutionCounselAdded.class)).thenReturn(prosecutionCounselAdded);
		when(hearingEventsToHearingConverter.convert(prosecutionCounselAdded)).thenReturn(prosecutionCounsel);
		hearingEventListener.prosecutionCounselAdded(envelope);
		verify(prosecutionCounselRepository).save(prosecutionCounsel);
	}
}