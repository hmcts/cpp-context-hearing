package uk.gov.moj.cpp.hearing.command.handler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
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

import javax.json.JsonObjectBuilder;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

@RunWith(MockitoJUnitRunner.class)
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

    @InjectMocks
    private CustodyTimeLimitClockHandler handler;

    @Test
    public void shouldCreateCustodyTimeLimitClockStoppedEvent() throws EventStreamException {

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

        when(this.eventSource.getStreamById(hearing.getId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(aggregate);

        handler.stopCustodyTimeLimitClock(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.event.custody-time-limit-clock-stopped"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearing.getId().toString())),
                                withJsonPath("$.offenceIds[0]", is(offenceId1.toString()))
                        ))
                )));
    }


}
