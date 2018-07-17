package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.MagistratesCourtHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MagistratesCourtInitiateHearingCommandHandlerTest {
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            SendingSheetCompletedRecorded.class,
            MagsCourtHearingRecorded.class
    );

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private MagistratesCourtInitiateHearingCommandHandler magistratesCourtInitiateHearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testRecordSendingSheetComplete() throws Exception {

        SendingSheetCompleted sendingSheetCompleted = SendingSheetCompleted.builder()
                .withHearing(Hearing.builder()
                        .withCaseId(randomUUID())
                        .withCourtCentreId(randomUUID().toString())
                        .withCaseUrn(STRING.next())
                        .withCourtCentreName(STRING.next())
                        .withSendingCommittalDate(PAST_LOCAL_DATE.next())
                        .withType(STRING.next())
                        .build())
                .withCrownCourtHearing(CrownCourtHearing.builder()
                        .withCourtCentreId(randomUUID())
                        .withCourtCentreName(STRING.next())
                        .withCcHearingDate(PAST_LOCAL_DATE.next().toString())
                        .build())
                .build();

        setupMockedEventStream(sendingSheetCompleted.getHearing().getCaseId(), this.eventStream, new CaseAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.record-sending-sheet-complete"),
                objectToJsonObjectConverter.convert(sendingSheetCompleted));

        magistratesCourtInitiateHearingCommandHandler.recordSendingSheetComplete(command);

        //noinspection unchecked
        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.sending-sheet-recorded"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.caseId", is(sendingSheetCompleted.getHearing().getCaseId().toString())),
                                withJsonPath("$.hearing.courtCentreId", is(sendingSheetCompleted.getHearing().getCourtCentreId())),
                                withJsonPath("$.hearing.caseUrn", is(sendingSheetCompleted.getHearing().getCaseUrn())),
                                withJsonPath("$.hearing.sendingCommittalDate", is(sendingSheetCompleted.getHearing().getSendingCommittalDate().toString())),
                                withJsonPath("$.hearing.type", is(sendingSheetCompleted.getHearing().getType())),
                                withJsonPath("$.crownCourtHearing.courtCentreId", is(sendingSheetCompleted.getCrownCourtHearing().getCourtCentreId().toString())),
                                withJsonPath("$.crownCourtHearing.courtCentreName", is(sendingSheetCompleted.getCrownCourtHearing().getCourtCentreName())),
                                withJsonPath("$.crownCourtHearing.ccHearingDate", is(sendingSheetCompleted.getCrownCourtHearing().getCcHearingDate()))

                        )))

        ));

    }

    @Test
    public void recordMagsCourtHearing() throws EventStreamException {

        RecordMagsCourtHearingCommand recordMagsCourtHearingCommand = new RecordMagsCourtHearingCommand(Hearing.builder()
                .withCaseId(randomUUID())
                .withCourtCentreId(randomUUID().toString())
                .withCaseUrn(STRING.next())
                .withCourtCentreName(STRING.next())
                .withSendingCommittalDate(PAST_LOCAL_DATE.next())
                .withType(STRING.next())
                .withDefendants(Collections.singletonList(Defendant.builder()
                        .withId(randomUUID())
                        .withOffences(Collections.singletonList(Offence.builder()
                                .withId(randomUUID())
                                .withPlea(Plea.plea()
                                        .withPleaDate(PAST_LOCAL_DATE.next())
                                        .withPleaValue("GUILTY")
                                        .build())
                                .build()))
                        .build()))
                .build());

        when(this.eventSource.getStreamById(any(UUID.class))).thenReturn(eventStream);

        when(this.aggregateService.get(eventStream, MagistratesCourtHearingAggregate.class)).thenReturn(new MagistratesCourtHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.record-mags-court-hearing"),
                objectToJsonObjectConverter.convert(recordMagsCourtHearingCommand));

        magistratesCourtInitiateHearingCommandHandler.recordMagsCourtHearing(command);

        //noinspection unchecked
        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.mags-court-hearing-recorded"),
                        payloadIsJson(allOf(
                                withJsonPath("$.originatingHearing.caseId", is(recordMagsCourtHearingCommand.getHearing().getCaseId().toString())),
                                withJsonPath("$.originatingHearing.courtCentreId", is(recordMagsCourtHearingCommand.getHearing().getCourtCentreId())),
                                withJsonPath("$.originatingHearing.caseUrn", is(recordMagsCourtHearingCommand.getHearing().getCaseUrn())),
                                withJsonPath("$.originatingHearing.sendingCommittalDate", is(recordMagsCourtHearingCommand.getHearing().getSendingCommittalDate().toString())),
                                withJsonPath("$.originatingHearing.type", is(recordMagsCourtHearingCommand.getHearing().getType())),
                                withJsonPath("$.convictionDate", is(recordMagsCourtHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getPlea().getPleaDate().toString())),
                                withJsonPath("$.hearingId", is(not(nullValue())))
                        )))
        ));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}
