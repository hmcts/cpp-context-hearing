package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;

import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationFinalisedOnTargetUpdated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import uk.gov.justice.services.messaging.JsonObjects;
import javax.json.JsonObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTargetCommandHandlerTest {


    @InjectMocks
    private UpdateTargetCommandHandler updateTargetCommandHandler;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            ApplicationFinalisedOnTargetUpdated.class);

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private HearingAggregate hearingAggregate;

    @Test
    void shouldHandlePatchApplicationFinalisedOnTarget() throws EventStreamException {
        final UUID hearingId = UUID.randomUUID();
        final UUID targetId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("targetId", targetId.toString())
                .add("hearingDay", hearingDay.toString())
                .build();
        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
        when(this.aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(hearingAggregate.updateApplicationFinalisedOnTarget(targetId, hearingId, hearingDay, true))
                .thenReturn(Stream.of(new ApplicationFinalisedOnTargetUpdated(targetId, hearingId, hearingDay, true)));

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.command.patch-application-finalised-on-target"), payload);
        updateTargetCommandHandler.patchApplicationFinalisedOnTarget(jsonEnvelop);

        final List<?> events = verifyAppendAndGetArgumentFrom(eventStream).toList();

        final ApplicationFinalisedOnTargetUpdated event = asPojo((JsonEnvelope) events.get(0), ApplicationFinalisedOnTargetUpdated.class);
        assertThat(event.getHearingId(), is(hearingId));
        assertThat(event.getId(), is(targetId));
        assertThat(event.getHearingDay(), is(hearingDay));
        assertThat(event.isApplicationFinalised(), is(true));
    }

}
