package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantWelshInfo;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantsWithWelshTranslationsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.DefendantsWelshInformationRecorded;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefendantsWelshTranslationsCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DefendantsWelshInformationRecorded.class);

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento;

    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private DefendantsWelshTranslationsCommandHandler defendantsWelshTranslationsCommandHandler;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Spy
    private ListToJsonArrayConverter listToJsonArrayConverter;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Test
    public void shouldTestHearingCommandSaveDefendantsWelshTranslations() throws EventStreamException {
        final UUID hearingId = randomUUID();

        final List<DefendantWelshInfo> defendantsWelshList = Arrays.asList(createDefendantWelshInfo(true), createDefendantWelshInfo(true));
        final DefendantsWithWelshTranslationsCommand defendantsWithWelshTranslationsCommand = new DefendantsWithWelshTranslationsCommand(hearingId, defendantsWelshList);
        final JsonEnvelope envelope = createHearingCommandSaveDefendantsWelshTranslationsEnvelopeForHearing(hearingId, defendantsWithWelshTranslationsCommand);
        System.out.println("envelope " + envelope);

        // set up the hearing
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(Hearing.hearing().build()));

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);

        defendantsWelshTranslationsCommandHandler.saveDefendantsForWelshTranslations(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        org.hamcrest.MatcherAssert.assertThat("hearing.event.defendants-welsh-information-recorded", is(actualEventProduced.metadata().name()));
    }

    private DefendantWelshInfo createDefendantWelshInfo(final boolean welshTranslation) {
        return new DefendantWelshInfo(randomUUID(), welshTranslation);
    }

    private JsonEnvelope createHearingCommandSaveDefendantsWelshTranslationsEnvelopeForHearing(final UUID hearingId,
                                                                                               final DefendantsWithWelshTranslationsCommand defendantsWithWelshTranslationsCommand) {
        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("defendantsWelshList", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(defendantsWithWelshTranslationsCommand)).build());

        return JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.save-defendants-welsh-translations"), payloadBuilder.build());
    }

}
