package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;

@RunWith(MockitoJUnitRunner.class)
public class CourtListRestrictionsCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CourtListRestricted.class,
            HearingChangeIgnored.class
    );

    @InjectMocks
    private RestrictCourtListCommandHandler restrictCourtListCommandHandler;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento;

    @Mock
    private AggregateService aggregateService;


    @Test
    public void shouldTestRestrictCourtList() throws EventStreamException {
        final Metadata metadata = metadataFor("hearing.command.restrict-court-list", UUID.randomUUID());
        final UUID hearingId = randomUUID();
        final List<UUID> caseIds = Arrays.asList(randomUUID(), randomUUID());
        final uk.gov.justice.hearing.courts.CourtListRestricted courtListRestricted = uk.gov.justice.hearing.courts.CourtListRestricted.courtListRestricted()
                .withHearingId(hearingId)
                .withCaseIds(caseIds).build();
        final Envelope<uk.gov.justice.hearing.courts.CourtListRestricted> envelope = envelopeFrom(metadata, courtListRestricted);

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        HearingAggregate hearingAggregate =  new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);
        when(hearingAggregateMomento.getHearing()).thenReturn(hearing);
        setField(hearingAggregate, "momento", hearingAggregateMomento);

        restrictCourtListCommandHandler.restrictCourtList(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);

        org.hamcrest.MatcherAssert.assertThat("hearing.event.court-list-restricted", is(actualEventProduced.metadata().name()));
    }

    @Test
    public void shouldTestRestrictCourtListWhenHearingNotExist() throws EventStreamException {
        final Metadata metadata = metadataFor("hearing.command.restrict-court-list", UUID.randomUUID());
        final UUID hearingId = randomUUID();
        final List<UUID> caseIds = Arrays.asList(randomUUID(), randomUUID());
        final uk.gov.justice.hearing.courts.CourtListRestricted courtListRestricted = uk.gov.justice.hearing.courts.CourtListRestricted.courtListRestricted()
                .withHearingId(hearingId)
                .withCaseIds(caseIds).build();
        final Envelope<uk.gov.justice.hearing.courts.CourtListRestricted> envelope = envelopeFrom(metadata, courtListRestricted);

        when(eventSource.getStreamById(hearingId)).thenReturn(hearingEventStream);
        HearingAggregate hearingAggregate =  new HearingAggregate();
        when(aggregateService.get(eq(hearingEventStream), any())).thenReturn(hearingAggregate);

        restrictCourtListCommandHandler.restrictCourtList(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(hearingEventStream).collect(Collectors.toList()).get(0);
        org.hamcrest.MatcherAssert.assertThat("hearing.hearing-change-ignored", is(actualEventProduced.metadata().name()));
    }
}