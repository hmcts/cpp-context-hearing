package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateDefendantCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.matchers.NotNull;

public class HearingAggregateTest {

    private static final HearingAggregate HEARING_AGGREGATE = new HearingAggregate();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(HEARING_AGGREGATE);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void initiate() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingInitiated result = (HearingInitiated) new HearingAggregate().initiate(initiateHearingCommand.getHearing()).collect(Collectors.toList()).get(0);

        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));
    }

    @Test
    public void initiateHearingOffencePlea() {

        final UpdateHearingWithInheritedPleaCommand command = new UpdateHearingWithInheritedPleaCommand(
                randomUUID(),
                Plea.plea()
                        .withPleaValue(PleaValue.GUILTY)
                        .withPleaDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(randomUUID())
                        .withOriginatingHearingId(randomUUID())
                        .withDelegatedPowers(DelegatedPowers.delegatedPowers()
                                .withUserId(UUID.randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .build());

        final InheritedPlea event = (InheritedPlea) HEARING_AGGREGATE.inheritPlea(command.getHearingId(), command.getPlea()).collect(Collectors.toList()).get(0);

        assertThat(event.getHearingId(), is(command.getHearingId()));
        assertThat(event.getPlea().getOffenceId(), is(command.getPlea().getOffenceId()));
        assertThat(event.getPlea().getPleaDate(), is(command.getPlea().getPleaDate()));
        assertThat(event.getPlea().getPleaValue(), is(command.getPlea().getPleaValue()));
        assertThat(event.getPlea().getDelegatedPowers().getUserId(), is(command.getPlea().getDelegatedPowers().getUserId()));
        assertThat(event.getPlea().getDelegatedPowers().getFirstName(), is(command.getPlea().getDelegatedPowers().getFirstName()));
        assertThat(event.getPlea().getDelegatedPowers().getLastName(), is(command.getPlea().getDelegatedPowers().getLastName()));
    }

    @Test
    public void logHearingEvent_shouldIgnore_givenNoPreviousHearing() {

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) new HearingAggregate()
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Hearing not found"));
        assertThat(hearingEventIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));

    }

    @Test
    public void logHearingEvent_shouldIgnore_givenAPreviousEventId() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Already logged"));
        assertThat(hearingEventIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));

    }

    @Test
    public void logHearingEvent_shouldLog() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final HearingEventLogged hearingEventLogged = (HearingEventLogged) hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        assertHearingEventLogged(hearingEventLogged, logEventCommand, initiateHearingCommand);
    }

    @Test
    public void correctHearingEvent_shouldLog() {

        final UUID previousHearingEventId = randomUUID();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .withDefenceCounselId(randomUUID())
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventCorrection = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(correctLogEventCommand.getHearingEventId())
                .withEventTime(correctLogEventCommand.getEventTime())
                .withLastModifiedTime(correctLogEventCommand.getLastModifiedTime())
                .withRecordedLabel(correctLogEventCommand.getRecordedLabel()).build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent);


        final List<Object> events = hearingAggregate.correctHearingEvent(correctLogEventCommand.getLatestHearingEventId(),
                correctLogEventCommand.getHearingId(),
                correctLogEventCommand.getHearingEventDefinitionId(),
                correctLogEventCommand.getAlterable(),
                correctLogEventCommand.getDefenceCounselId(),
                hearingEventCorrection).collect(Collectors.toList());

        final HearingEventDeleted hearingEventDeleted = (HearingEventDeleted) events.get(0);
        assertThat(hearingEventDeleted.getHearingEventId(), is(previousHearingEventId));


        final HearingEventLogged hearingEventLogged = (HearingEventLogged) events.get(1);

        assertThat(hearingEventLogged.getHearingEventId(), is(correctLogEventCommand.getLatestHearingEventId()));
        assertThat(hearingEventLogged.getLastHearingEventId(), is(previousHearingEventId));
        assertThat(hearingEventLogged.getHearingId(), is(correctLogEventCommand.getHearingId()));
        assertThat(hearingEventLogged.getEventTime(), is(correctLogEventCommand.getEventTime()));
        assertThat(hearingEventLogged.getLastModifiedTime(), is(correctLogEventCommand.getLastModifiedTime()));
        assertThat(hearingEventLogged.getRecordedLabel(), is(correctLogEventCommand.getRecordedLabel()));
        assertThat(hearingEventLogged.getHearingEventDefinitionId(), is(correctLogEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventLogged.isAlterable(), is(false));
        assertThat(hearingEventLogged.getCourtCentre().getId(), is(initiateHearingCommand.getHearing().getCourtCentre().getId()));
        assertThat(hearingEventLogged.getCourtCentre().getName(), is(initiateHearingCommand.getHearing().getCourtCentre().getName()));
        assertThat(hearingEventLogged.getCourtCentre().getRoomId(), is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()));
        assertThat(hearingEventLogged.getCourtCentre().getRoomName(), is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()));
        assertThat(hearingEventLogged.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
        assertThat(hearingEventLogged.getHearingType().getId(), is(initiateHearingCommand.getHearing().getType().getId()));
        assertThat(hearingEventLogged.getHearingType().getDescription(), is(initiateHearingCommand.getHearing().getType().getDescription()));
        assertThat(hearingEventLogged.getDefenceCounselId(), is(correctLogEventCommand.getDefenceCounselId()));
    }

    @Test
    public void correctHearingEvent_shouldIgnore_givenInvalidPreviousEventId() {

        final UUID previousHearingEventId = randomUUID();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventCorrection = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(correctLogEventCommand.getHearingEventId())
                .withEventTime(correctLogEventCommand.getEventTime())
                .withLastModifiedTime(correctLogEventCommand.getLastModifiedTime())
                .withRecordedLabel(correctLogEventCommand.getRecordedLabel()).build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .correctHearingEvent(correctLogEventCommand.getLatestHearingEventId(),
                        correctLogEventCommand.getHearingId(),
                        correctLogEventCommand.getHearingEventDefinitionId(),
                        correctLogEventCommand.getAlterable(),
                        correctLogEventCommand.getDefenceCounselId(),
                        hearingEventCorrection).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Hearing event not found"));
        assertThat(hearingEventIgnored.getHearingId(), is(correctLogEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(correctLogEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(correctLogEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(correctLogEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(correctLogEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));
    }

    @Test
    public void logHearingEvent_shouldIgnore_givenEventHasBeenDeleted() {

        final UUID previousHearingEventId = randomUUID();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final LogEventCommand logEventCommandArbitrary = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventArbitrary = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommandArbitrary.getHearingEventId())
                .withEventTime(logEventCommandArbitrary.getEventTime())
                .withLastModifiedTime(logEventCommandArbitrary.getLastModifiedTime())
                .withRecordedLabel(logEventCommandArbitrary.getRecordedLabel()).build();

        hearingAggregate.logHearingEvent(logEventCommandArbitrary.getHearingId(), logEventCommandArbitrary.getHearingEventDefinitionId(), logEventCommandArbitrary.getAlterable(), logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary);

        final CorrectLogEventCommand logEventCommandCorrectionArbitrary = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventCorrectionArbitrary = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommandCorrectionArbitrary.getHearingEventId())
                .withEventTime(logEventCommandCorrectionArbitrary.getEventTime())
                .withLastModifiedTime(logEventCommandCorrectionArbitrary.getLastModifiedTime())
                .withRecordedLabel(logEventCommandCorrectionArbitrary.getRecordedLabel()).build();

        hearingAggregate.correctHearingEvent(logEventCommandCorrectionArbitrary.getLatestHearingEventId(), logEventCommandCorrectionArbitrary.getHearingId(), logEventCommandCorrectionArbitrary.getHearingEventDefinitionId(), logEventCommandCorrectionArbitrary.getAlterable(), logEventCommandCorrectionArbitrary.getDefenceCounselId(), hearingEventCorrectionArbitrary);

        hearingAggregate.logHearingEvent(logEventCommandArbitrary.getHearingId(), logEventCommandArbitrary.getHearingEventDefinitionId(), logEventCommandArbitrary.getAlterable(), logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary);

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();


        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Already deleted"));
        assertThat(hearingEventIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));
    }

    @Test
    public void correctHearingEvent_shouldIgnore_givenEventHasPreivouslyBeenDeleted() {

        final UUID previousHearingEventId = randomUUID();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final LogEventCommand logEventCommandArbitrary = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventArbitrary = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommandArbitrary.getHearingEventId())
                .withEventTime(logEventCommandArbitrary.getEventTime())
                .withLastModifiedTime(logEventCommandArbitrary.getLastModifiedTime())
                .withRecordedLabel(logEventCommandArbitrary.getRecordedLabel()).build();


        hearingAggregate.logHearingEvent(logEventCommandArbitrary.getHearingId(),
                logEventCommandArbitrary.getHearingEventDefinitionId(),
                logEventCommandArbitrary.getAlterable(),
                logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary);

        final CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEventCorrection = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(correctLogEventCommand.getHearingEventId())
                .withEventTime(correctLogEventCommand.getEventTime())
                .withLastModifiedTime(correctLogEventCommand.getLastModifiedTime())
                .withRecordedLabel(correctLogEventCommand.getRecordedLabel()).build();

        hearingAggregate.correctHearingEvent(correctLogEventCommand.getLatestHearingEventId(),
                correctLogEventCommand.getHearingId(),
                correctLogEventCommand.getHearingEventDefinitionId(),
                correctLogEventCommand.getAlterable(),
                correctLogEventCommand.getDefenceCounselId(),
                hearingEventCorrection);

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .correctHearingEvent(correctLogEventCommand.getLatestHearingEventId(),
                        correctLogEventCommand.getHearingId(),
                        correctLogEventCommand.getHearingEventDefinitionId(),
                        correctLogEventCommand.getAlterable(),
                        correctLogEventCommand.getDefenceCounselId(),
                        hearingEventCorrection).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Already deleted"));
        assertThat(hearingEventIgnored.getHearingId(), is(correctLogEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(correctLogEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(correctLogEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(correctLogEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(correctLogEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));
    }

    @Test
    public void updateDefendantDetails_shouldIgnore_when_resultShared() {

        final int expected = 0;

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final CaseDefendantDetailsWithHearingCommand command = with(
                initiateDefendantCommandTemplate(initiateHearingCommand.getHearing().getId()),
                template -> template.getDefendant().setId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.apply(ResultsShared.builder().build());

        final Stream<Object> stream = hearingAggregate.updateDefendantDetails(command.getHearingId(), command.getDefendant());

        assertEquals(expected, stream.count());
    }

    @Test
    public void updateDefendantDetails_shouldUpdate_when_resultNotShared() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final Hearing hearing = initiateHearingCommand.getHearing();

        final CaseDefendantDetailsWithHearingCommand command = with(
                initiateDefendantCommandTemplate(hearing.getId()),
                template -> {
                    template.getDefendant().setId(hearing.getProsecutionCases().get(0).getDefendants().get(0).getId());
                    template.getDefendant().setProsecutionCaseId(hearing.getProsecutionCases().get(0).getDefendants().get(0).getProsecutionCaseId());
                });

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(hearing));

        final DefendantDetailsUpdated result = (DefendantDetailsUpdated) hearingAggregate.updateDefendantDetails(command.getHearingId(), command.getDefendant()).collect(Collectors.toList()).get(0);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getFirstName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getFirstName()));

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getLastName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getLastName()));

    }


    @Test
    public void updateHearingEvents_shouldUpdate() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final UpdateHearingEventsCommand updateHearingEventsCommand = UpdateHearingEventsCommand.builder()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withHearingEvents(singletonList(HearingEvent.builder()
                        .withHearingEventId(randomUUID())
                        .withRecordedLabel(STRING.next())
                        .build()))
                .build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final HearingEventsUpdated result = (HearingEventsUpdated) hearingAggregate.updateHearingEvents(updateHearingEventsCommand.getHearingId(), updateHearingEventsCommand.getHearingEvents()).findFirst().orElse(null);

        assertNotNull(result);
        assertThat(result.getHearingEvents().get(0).getHearingEventId(), is(updateHearingEventsCommand.getHearingEvents().get(0).getHearingEventId()));
        assertThat(result.getHearingEvents().get(0).getRecordedLabel(), is(updateHearingEventsCommand.getHearingEvents().get(0).getRecordedLabel()));
        assertThat(result.getHearingId(), is(updateHearingEventsCommand.getHearingId()));
    }

    @Test
    public void updateHearingEvents_shouldNotUpdateNoHearing() {

        final UpdateHearingEventsCommand updateHearingEventsCommand = UpdateHearingEventsCommand.builder()
                .withHearingId(randomUUID())
                .withHearingEvents(singletonList(
                        HearingEvent.builder()
                                .withHearingEventId(randomUUID())
                                .withRecordedLabel(STRING.next())
                                .build()))
                .build();

        final HearingEventIgnored result = (HearingEventIgnored) HEARING_AGGREGATE
                .updateHearingEvents(updateHearingEventsCommand.getHearingId(), updateHearingEventsCommand.getHearingEvents()).findFirst().orElse(null);

        assertNotNull(result);
        assertThat(result.getHearingId(), is(updateHearingEventsCommand.getHearingId()));
    }

    @Test
    public void logHearingEvent_shouldNotLogPauseHearingEvent_IfNoActiveHearingsReturned() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final List<Object> events = hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList());

        final HearingEventLogged startHearingEventLogged = (HearingEventLogged) events.get(0);
        assertHearingEventLogged(startHearingEventLogged, logEventCommand, initiateHearingCommand);
    }

    @Test
    public void shouldNotRaiseEvent_whenHearingResult_hasAlreadyShared() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.TRUE);
        hearingAggregate.apply(new HearingInitiated(hearing));
        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing)
                hearingAggregate.updateCaseDefendantsForHearing(hearing.getId(), ProsecutionCase.prosecutionCase().build())
                        .findFirst()
                        .orElse(null);
        assertThat(caseDefendantsUpdatedForHearing, nullValue());
    }

    @Test
    public void shouldRaiseEvent_whenHearingResult_hasNotAlreadyShared() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.FALSE);

        hearingAggregate.apply(new HearingInitiated(hearing));

        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing)
                hearingAggregate.updateCaseDefendantsForHearing(hearing.getId(), ProsecutionCase.prosecutionCase().build())
                        .findFirst()
                        .orElse(null);
        assertThat(caseDefendantsUpdatedForHearing.getHearingId(), is(hearing.getId()));
    }

    private void assertHearingEventLogged(final HearingEventLogged hearingEventLogged, final LogEventCommand logEventCommand, final InitiateHearingCommand initiateHearingCommand) {
        assertThat(hearingEventLogged.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventLogged.getLastHearingEventId(), is(nullValue()));
        assertThat(hearingEventLogged.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventLogged.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventLogged.getLastModifiedTime(), is(logEventCommand.getLastModifiedTime()));
        assertThat(hearingEventLogged.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventLogged.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventLogged.isAlterable(), is(false));
        assertThat(hearingEventLogged.getCourtCentre().getId(), is(initiateHearingCommand.getHearing().getCourtCentre().getId()));
        assertThat(hearingEventLogged.getCourtCentre().getName(), is(initiateHearingCommand.getHearing().getCourtCentre().getName()));
        assertThat(hearingEventLogged.getCourtCentre().getRoomId(), is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()));
        assertThat(hearingEventLogged.getCourtCentre().getRoomName(), is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()));
        assertThat(hearingEventLogged.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
        assertThat(hearingEventLogged.getHearingType().getId(), is(initiateHearingCommand.getHearing().getType().getId()));
        assertThat(hearingEventLogged.getHearingType().getDescription(), is(initiateHearingCommand.getHearing().getType().getDescription()));
    }

    @Test
    public void addDefenceCounsel_beforeHearingEnded(){
        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing Started")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(),new ArrayList<>(),
                "Margaret",randomUUID(),"Brown","H","Y","Ms", randomUUID());

        final List<Object> events = HEARING_AGGREGATE.addDefenceCounsel(defenceCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselAdded defenceCounselAdded = (DefenceCounselAdded) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselAdded.getHearingId(), is(logEventCommand.getHearingId()));
    }

    @Test
    public void shouldNotAllowedToAddDefenceCounsel_afterHearingEnded_forSPICases(){
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing ended")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(),new ArrayList<>(),
                "Leigh",randomUUID(),"Ann","H","Y","Ms", randomUUID());

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addDefenceCounsel(defenceCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselChangeIgnored defenceCounselChangeIgnored = (DefenceCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(defenceCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
    }

    @Test
    public void shouldNotAllowedToAddDefenceCounsel_afterHearingEnded_forSJPCases(){
        final InitiateHearingCommand initiateHearingCommand = initiateHearingTemplateForMagistrates();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing ended")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(),new ArrayList<>(),
                "Leigh",randomUUID(),"Ann","H","Y","Ms", randomUUID());

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addDefenceCounsel(defenceCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselChangeIgnored defenceCounselChangeIgnored = (DefenceCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(defenceCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
    }

    @Test
    public void addProsecutionCounsel_beforeHearingEnded(){
        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing Started")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final List<Object> events = HEARING_AGGREGATE.addProsecutionCounsel(prosecutionCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselAdded prosecutionCounselAdded = (ProsecutionCounselAdded) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselAdded.getHearingId(), is(logEventCommand.getHearingId()));
    }

    @Test
    public void shouldNotAllowedToAddProsecutionCounsel_afterHearingEnded_forSPICases(){
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing ended")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addProsecutionCounsel(prosecutionCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselChangeIgnored prosecutionCounselChangeIgnored = (ProsecutionCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(prosecutionCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
    }

    @Test
    public void shouldNotAllowedToAddProsecutionCounsel_afterHearingEnded_forSJPCases(){
        final InitiateHearingCommand initiateHearingCommand = initiateHearingTemplateForMagistrates();

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel("Hearing ended")
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();
        final uk.gov.moj.cpp.hearing.eventlog.HearingEvent hearingEvent = uk.gov.moj.cpp.hearing.eventlog.HearingEvent.builder()
                .withHearingEventId(logEventCommand.getHearingEventId())
                .withEventTime(logEventCommand.getEventTime())
                .withLastModifiedTime(logEventCommand.getLastModifiedTime())
                .withRecordedLabel(logEventCommand.getRecordedLabel()).build();

        final ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                Arrays.asList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                Arrays.asList(UUID.randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addProsecutionCounsel(prosecutionCounsel,logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselChangeIgnored prosecutionCounselChangeIgnored = (ProsecutionCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(prosecutionCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
    }
}
