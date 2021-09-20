package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddDefendantCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefendantAdded.class,
            HearingChangeIgnored.class);

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @InjectMocks
    private AddDefendantCommandHandler addDefendantCommandHandler;

    @SuppressWarnings("unchecked")
    @Test
    public void testCaseDefendantAddedIgnored() throws EventStreamException {
        //Given
        final UUID arbitraryHearingId = UUID.randomUUID();
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();
        final CaseAggregate caseAggregate = new CaseAggregate() {{
            apply(RegisteredHearingAgainstCase.builder().withCaseId(arbitraryDefendant.getProsecutionCaseId()).withHearingId(arbitraryHearingId).build());
        }};
        setupMockedEventStream(arbitraryHearingId, this.hearingEventStream, new HearingAggregate());
        setupMockedEventStream(arbitraryDefendant.getProsecutionCaseId(), this.caseEventStream, caseAggregate);
        when(this.eventSource.getStreamById(arbitraryDefendant.getProsecutionCaseId())).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(caseAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("defendants", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-defendant"), payload);

        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.hearing-change-ignored"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingId.toString()))))
                )));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCaseDefendantAdded_When_Hearing_Date_Already_Passed() throws EventStreamException {
        //Given
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();
        CommandHelpers.InitiateHearingCommandHelper arbitraryHearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(arbitraryHearingObject.getHearing()));
        }};
        final CaseAggregate caseAggregate = new CaseAggregate() {{
            apply(RegisteredHearingAgainstCase.builder().withCaseId(arbitraryDefendant.getProsecutionCaseId()).withHearingId(arbitraryHearingObject.getHearingId()).build());
        }};
        setupMockedEventStream(arbitraryHearingObject.getHearingId(), this.hearingEventStream, hearingAggregate);
        setupMockedEventStream(arbitraryDefendant.getProsecutionCaseId(), this.caseEventStream, caseAggregate);
        when(this.eventSource.getStreamById(arbitraryDefendant.getProsecutionCaseId())).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(caseAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("defendants", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-defendant"), payload);
        when(this.eventSource.getStreamById(arbitraryHearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);


        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.hearing-change-ignored"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingObject.getHearingId().toString()))))
                )));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCaseDefendantShouldAdded() throws EventStreamException {
        //Given
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();
        CommandHelpers.InitiateHearingCommandHelper arbitraryHearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            Hearing hearing = arbitraryHearingObject.getHearing();
            HearingDay hearingDay = hearing.getHearingDays().get(0);
            hearingDay.setSittingDay(ZonedDateTime.now());
            apply(new HearingInitiated(hearing));
        }};
        final CaseAggregate caseAggregate = new CaseAggregate() {{
            apply(RegisteredHearingAgainstCase.builder().withCaseId(arbitraryDefendant.getProsecutionCaseId()).withHearingId(arbitraryHearingObject.getHearingId()).build());
        }};
        setupMockedEventStream(arbitraryHearingObject.getHearingId(), this.hearingEventStream, hearingAggregate);
        setupMockedEventStream(arbitraryDefendant.getProsecutionCaseId(), this.caseEventStream, caseAggregate);
        when(this.eventSource.getStreamById(arbitraryDefendant.getProsecutionCaseId())).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(caseAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("defendants", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-defendant"), payload);
        when(this.eventSource.getStreamById(arbitraryHearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-added"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingObject.getHearingId().toString())),
                                withJsonPath("$.defendant.id", is(arbitraryDefendant.getId().toString())))))
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCaseDefendantShouldNotBeAddedWhenDuplicate() throws EventStreamException {
        //Given
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();
        CommandHelpers.InitiateHearingCommandHelper arbitraryHearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            Hearing hearing = arbitraryHearingObject.getHearing();
            HearingDay hearingDay = hearing.getHearingDays().get(0);
            hearingDay.setSittingDay(ZonedDateTime.now());
            apply(new HearingInitiated(hearing));
        }};
        final CaseAggregate caseAggregate = new CaseAggregate() {{
            apply(RegisteredHearingAgainstCase.builder().withCaseId(arbitraryDefendant.getProsecutionCaseId()).withHearingId(arbitraryHearingObject.getHearingId()).build());
        }};
        setupMockedEventStream(arbitraryHearingObject.getHearingId(), this.hearingEventStream, hearingAggregate);
        setupMockedEventStream(arbitraryDefendant.getProsecutionCaseId(), this.caseEventStream, caseAggregate);
        when(this.eventSource.getStreamById(arbitraryDefendant.getProsecutionCaseId())).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(caseAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("defendants", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-defendant"), payload);
        when(this.eventSource.getStreamById(arbitraryHearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-added"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingObject.getHearingId().toString())),
                                withJsonPath("$.defendant.id", is(arbitraryDefendant.getId().toString())))))
        ));

        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().size(), is(2));

        // Add the same defendant again, it should be ignored
        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().size(), is(2));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCaseDefendantShouldAdded_When_Hearing_Date_In_Future() throws EventStreamException {
        //Given
        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();
        CommandHelpers.InitiateHearingCommandHelper arbitraryHearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            Hearing hearing = arbitraryHearingObject.getHearing();
            HearingDay hearingDay = hearing.getHearingDays().get(0);
            hearingDay.setSittingDay(ZonedDateTime.now().plusDays(1));
            apply(new HearingInitiated(hearing));
        }};
        final CaseAggregate caseAggregate = new CaseAggregate() {{
            apply(RegisteredHearingAgainstCase.builder().withCaseId(arbitraryDefendant.getProsecutionCaseId()).withHearingId(arbitraryHearingObject.getHearingId()).build());
        }};
        setupMockedEventStream(arbitraryHearingObject.getHearingId(), this.hearingEventStream, hearingAggregate);
        setupMockedEventStream(arbitraryDefendant.getProsecutionCaseId(), this.caseEventStream, caseAggregate);
        when(this.eventSource.getStreamById(arbitraryDefendant.getProsecutionCaseId())).thenReturn(this.caseEventStream);
        when(this.aggregateService.get(this.caseEventStream, CaseAggregate.class)).thenReturn(caseAggregate);

        JsonObject payload = Json.createObjectBuilder()
                .add("defendants", Json.createArrayBuilder().add(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                .build();
        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.add-defendant"), payload);
        when(this.eventSource.getStreamById(arbitraryHearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);


        addDefendantCommandHandler.addDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-added"),
                        payloadIsJson(allOf(withJsonPath("$.hearingId", is(arbitraryHearingObject.getHearingId().toString())),
                                withJsonPath("$.defendant.id", is(arbitraryDefendant.getId().toString())))))
        ));
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}