package uk.gov.moj.cpp.hearing.command.handler;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Target2.target2;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObjectBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustodyTimeLimitClockHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CustodyTimeLimitClockStopped.class);

    @Mock
    private EventSource eventSource;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Requester requester;

    @InjectMocks
    private CustodyTimeLimitClockHandler handler;

    @Test
    public void shouldCreateCustodyTimeLimitClockStoppedEventWhenPleaIsGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final ZonedDateTime sittingDay = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2) ;

        final CourtCentre courtCentre = CoreTestTemplates.courtCentre().build();


        final UUID defendantId1 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID caseId = randomUUID();


        final Defendant defendant1 = Defendant.defendant()
                .withId(defendantId1)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId1)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId1)
                                .withPleaValue("GUILTY")
                                .build())
                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                .withTimeLimit(LocalDate.now())
                                .withDaysSpent(10)
                                .build())
                        .build()))
                .build();

        final Defendant defendant2 = Defendant.defendant()
                .withId(defendantId2)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId2).build()))
                .build();
        final Hearing hearing =  Hearing.hearing()
                .withHasSharedResults(true)
                .withId(initiateHearingCommand.getHearing().getId())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(TestUtilities.asList(defendant1,defendant2)

                        ).build()))
                .withHearingDays(asList(CoreTestTemplates.hearingDay(sittingDay, courtCentre).build()))
                .build();

        hearing.setHasSharedResults(false);


        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);


        final Target2 target1 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId1)
                .withOffenceId(offenceId1)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId1)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId1)
                        .withNonStandaloneAncillaryResult(false)
                        .withCategory("I")
                        .build()))
                .withHearingDay(hearingDay)
                .build();
        final Target2 target2 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId2)
                .withOffenceId(offenceId2)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withHearingDay(hearingDay)
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId2)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId2)
                        .withNonStandaloneAncillaryResult(false)
                        .withCategory("I")
                        .build()))
                .build();


            aggregate.apply(ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withTargets(TestUtilities.asList(target1, target2))
                .withHearingDay(hearingDay)
                .withHearing(hearing)
                .build());

        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearing.getId().toString());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.stop-custody-time-limit-clock"), payloadBuilder.build());

        final JsonEnvelope resultsEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add("resultDefinitions", createArrayBuilder().add(createObjectBuilder().add("id", randomUUID().toString()))));

        when(this.eventSource.getStreamById(hearing.getId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);
        when(requester.request(any(JsonEnvelope.class))).thenReturn(resultsEnvelope);

        handler.stopCustodyTimeLimitClock(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.custody-time-limit-clock-stopped"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString()))
                        ))
                )));
    }

    @Test
    public void shouldCreateCustodyTimeLimitClockStoppedEventWhenVerdictIsGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final ZonedDateTime sittingDay = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2) ;

        final CourtCentre courtCentre = CoreTestTemplates.courtCentre().build();

        final UUID defendantId1 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID caseId = randomUUID();

        final Defendant defendant1 = Defendant.defendant()
                .withId(defendantId1)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId1)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId1)
                                .withPleaValue("NOT GUILTY")
                                .build())
                        .withVerdict(Verdict.verdict()
                                .withVerdictType(VerdictType.verdictType()
                                        .withCategoryType("GUILTY")
                                        .withId(randomUUID())
                                        .build())
                                .build())
                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                .withTimeLimit(LocalDate.now())
                                .withDaysSpent(10)
                                .build())
                        .build()))
                .build();

        final Defendant defendant2 = Defendant.defendant()
                .withId(defendantId2)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId2).build()))
                .build();
        final Hearing hearing =  Hearing.hearing()
                .withHasSharedResults(true)
                .withId(initiateHearingCommand.getHearing().getId())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(TestUtilities.asList(defendant1,defendant2)

                        ).build()))
                .withHearingDays(asList(CoreTestTemplates.hearingDay(sittingDay, courtCentre).build()))
                .build();

        hearing.setHasSharedResults(false);


        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);


        final Target2 target1 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId1)
                .withOffenceId(offenceId1)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId1)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId1)
                        .build()))
                .withHearingDay(hearingDay)
                .build();
        final Target2 target2 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId2)
                .withOffenceId(offenceId2)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withHearingDay(hearingDay)
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId2)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId2)
                        .build()))
                .build();


        aggregate.apply(ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withTargets(TestUtilities.asList(target1, target2))
                .withHearingDay(hearingDay)
                .withHearing(hearing)
                .build());

        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearing.getId().toString());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.stop-custody-time-limit-clock"), payloadBuilder.build());

        final JsonEnvelope resultsEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add("resultDefinitions", createArrayBuilder().add(createObjectBuilder().add("id", randomUUID().toString()))));

        when(this.eventSource.getStreamById(hearing.getId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);
        when(requester.request(any(JsonEnvelope.class))).thenReturn(resultsEnvelope);

        handler.stopCustodyTimeLimitClock(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.custody-time-limit-clock-stopped"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString()))
                        ))
                )));
    }

    @Test
    public void shouldCreateCustodyTimeLimitClockStoppedEventWhenResultDefinitionIdIsOfFinalType() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final ZonedDateTime sittingDay = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2) ;

        final CourtCentre courtCentre = CoreTestTemplates.courtCentre().build();

        final UUID defendantId1 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID caseId = randomUUID();

        final Defendant defendant1 = Defendant.defendant()
                .withId(defendantId1)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId1)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId1)
                                .withPleaValue("NOT GUILTY")
                                .build())
                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                .withTimeLimit(LocalDate.now())
                                .withDaysSpent(10)
                                .build())
                        .build()))
                .build();

        final Defendant defendant2 = Defendant.defendant()
                .withId(defendantId2)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId2).build()))
                .build();
        final Hearing hearing =  Hearing.hearing()
                .withHasSharedResults(true)
                .withId(initiateHearingCommand.getHearing().getId())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(TestUtilities.asList(defendant1,defendant2)

                        ).build()))
                .withHearingDays(asList(CoreTestTemplates.hearingDay(sittingDay, courtCentre).build()))
                .build();

        hearing.setHasSharedResults(false);


        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);

        final Target2 target1 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId1)
                .withOffenceId(offenceId1)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withResultDefinitionId(UUID.fromString("bccc0055-a214-4576-9cad-092e95713893"))
                        .withDefendantId(defendantId1)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId1)
                        .build()))
                .withHearingDay(hearingDay)
                .build();
        final Target2 target2 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId2)
                .withOffenceId(offenceId2)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withHearingDay(hearingDay)
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId2)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId2)
                        .build()))
                .build();


        aggregate.apply(ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withTargets(TestUtilities.asList(target1, target2))
                .withHearingDay(hearingDay)
                .withHearing(hearing)
                .build());

        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearing.getId().toString());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.stop-custody-time-limit-clock"), payloadBuilder.build());

        final JsonEnvelope resultsEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add("resultDefinitions", createArrayBuilder().add(createObjectBuilder().add("id", "bccc0055-a214-4576-9cad-092e95713893"))));

        when(this.eventSource.getStreamById(hearing.getId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);
        when(requester.request(any(JsonEnvelope.class))).thenReturn(resultsEnvelope);

        handler.stopCustodyTimeLimitClock(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.custody-time-limit-clock-stopped"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString()))
                        ))
                )));

    }

    @Test
    public void shouldNotCreateCustodyTimeLimitClockStoppedEvent() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final ZonedDateTime sittingDay = ZonedDateTime.now(ZoneOffset.UTC).minusDays(2) ;

        final CourtCentre courtCentre = CoreTestTemplates.courtCentre().build();

        final UUID defendantId1 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID caseId = randomUUID();

        final Defendant defendant1 = Defendant.defendant()
                .withId(defendantId1)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId1)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId1)
                                .withPleaValue("NOT GUILTY")
                                .build())
                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                .withTimeLimit(LocalDate.now())
                                .withDaysSpent(10)
                                .build())
                        .build()))
                .build();

        final Defendant defendant2 = Defendant.defendant()
                .withId(defendantId2)
                .withOffences(asList(uk.gov.justice.core.courts.Offence.offence()
                        .withId(offenceId2).build()))
                .build();
        final Hearing hearing =  Hearing.hearing()
                .withHasSharedResults(true)
                .withId(initiateHearingCommand.getHearing().getId())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(TestUtilities.asList(defendant1,defendant2)

                        ).build()))
                .withHearingDays(asList(CoreTestTemplates.hearingDay(sittingDay, courtCentre).build()))
                .build();

        hearing.setHasSharedResults(false);


        final HearingAggregate aggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing));
        }};

        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);

        final Target2 target1 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId1)
                .withOffenceId(offenceId1)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId1)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId1)
                        .build()))
                .withHearingDay(hearingDay)
                .build();
        final Target2 target2 = target2().withTargetId(randomUUID())
                .withDefendantId(defendantId2)
                .withOffenceId(offenceId2)
                .withCaseId(caseId)
                .withHearingId(hearing.getId())
                .withHearingDay(hearingDay)
                .withResultLines(asList(ResultLine2.resultLine2()
                        .withResultLineId(randomUUID())
                        .withDefendantId(defendantId2)
                        .withCaseId(caseId)
                        .withLevel(Level.OFFENCE)
                        .withOffenceId(offenceId2)
                        .build()))
                .build();


        aggregate.apply(ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withTargets(TestUtilities.asList(target1, target2))
                .withHearingDay(hearingDay)
                .withHearing(hearing)
                .build());

        final JsonObjectBuilder payloadBuilder = createObjectBuilder()
                .add("hearingId", hearing.getId().toString());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.stop-custody-time-limit-clock"), payloadBuilder.build());

        final JsonEnvelope resultsEnvelope = envelopeFrom(metadataWithRandomUUIDAndName(),
                createObjectBuilder().add("resultDefinitions", createArrayBuilder().add(createObjectBuilder().add("id", randomUUID().toString()))));

        when(this.eventSource.getStreamById(hearing.getId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);
        when(requester.request(any(JsonEnvelope.class))).thenReturn(resultsEnvelope);

        handler.stopCustodyTimeLimitClock(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream)
                .filter(s -> s.metadata().name().equals("hearing.event.custody-time-limit-clock-stopped")).count(), is(0l));

    }
}
