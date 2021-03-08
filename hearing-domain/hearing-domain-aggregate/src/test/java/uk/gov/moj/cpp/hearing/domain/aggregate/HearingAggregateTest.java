package uk.gov.moj.cpp.hearing.domain.aggregate;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithAllLevelJudicialResults;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateDefendantCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.junit.Ignore;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.result.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

public class HearingAggregateTest {

    private static final String GUILTY = "GUILTY";
    private static final HearingAggregate HEARING_AGGREGATE = new HearingAggregate();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(HEARING_AGGREGATE);
        } catch (final SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Test
    public void shouldInitiateHearing() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithAllLevelJudicialResults();

        final HearingInitiated result = (HearingInitiated) new HearingAggregate().initiate(initiateHearingCommand.getHearing()).collect(Collectors.toList()).get(0);

        assertThat(result.getHearing().getId(), is(initiateHearingCommand.getHearing().getId()));
    }

    @Test
    public void shouldInitiateHearingWithAllResultsCleanedUp() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithAllLevelJudicialResults();

        final HearingInitiated result = (HearingInitiated) new HearingAggregate().initiate(initiateHearingCommand.getHearing()).collect(Collectors.toList()).get(0);

        final Hearing targetHearing = result.getHearing();
        assertThat(targetHearing.getId(), is(initiateHearingCommand.getHearing().getId()));
        assertThat(targetHearing.getDefendantJudicialResults(), nullValue());
        assertThat(targetHearing.getProsecutionCases(), hasSize(greaterThanOrEqualTo(1)));
        targetHearing.getProsecutionCases().forEach(pc -> {
            assertThat(pc.getDefendants(), hasSize(greaterThanOrEqualTo(1)));
            pc.getDefendants().forEach(d -> {
                assertThat(d.getDefendantCaseJudicialResults(), nullValue());
                assertThat(d.getOffences(), hasSize(greaterThanOrEqualTo(1)));
                d.getOffences().forEach(o -> {
                    assertThat(o.getJudicialResults(), nullValue());
                });
            });
        });
    }

    @Test
    public void shouldInitiateHearingOffencePlea() {

        final UpdateHearingWithInheritedPleaCommand command = new UpdateHearingWithInheritedPleaCommand(
                randomUUID(),
                Plea.plea()
                        .withPleaValue(GUILTY)
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
    public void shouldIgnoreLogHearingEventGivenNoPreviousHearing() {

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
    public void shouldIgnoreLogHearingEventGivenAPreviousEventId() {

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
    public void shouldLoglogHearingEvent() {

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
    public void shouldLogHearingEvent() {

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
    public void shouldIgnoreCorrectHearingEventGivenInvalidPreviousEventId() {

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
    public void shouldIgnorelogHearingEventGivenEventHasBeenDeleted() {

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
    public void shouldHearingEventNotIgnoredGivenEventHasPreivouslyBeenDeleted() {

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
    public void shouldUpdateDefendantDetailsNotIgnoreWhenResultShared() {

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
    public void shouldUpdateDefendantDetailsWhenResultNotShared() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final Hearing hearing = initiateHearingCommand.getHearing();

        final CaseDefendantDetailsWithHearingCommand command = with(
                initiateDefendantCommandTemplate(hearing.getId()),
                template -> {
                    template.getDefendant().setId(hearing.getProsecutionCases().get(0).getDefendants().get(0).getId());
                    template.getDefendant().setMasterDefendantId(hearing.getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId());
                    template.getDefendant().setProsecutionCaseId(hearing.getProsecutionCases().get(0).getDefendants().get(0).getProsecutionCaseId());
                });

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(hearing));

        final DefendantDetailsUpdated result = (DefendantDetailsUpdated) hearingAggregate.updateDefendantDetails(command.getHearingId(), command.getDefendant()).collect(Collectors.toList()).get(0);

        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getFirstName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getFirstName()));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), Matchers.is(result.getDefendant().getMasterDefendantId()));
        assertThat(hearing.getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getLastName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getLastName()));

    }


    @Test
    public void shouldUpdateHearingEvents() {

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
    public void shouldNotUpdateHearingEventsNoHearing() {

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
    public void shouldNotLogPauseHearingEventIfNoActiveHearingsReturned() {

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
    public void shouldNotRaiseEventWhenHearingResultHasAlreadyShared() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.TRUE);
        hearingAggregate.apply(new HearingInitiated(hearing));
        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing)
                hearingAggregate.updateCaseDefendantsForHearing(hearing.getId(), ProsecutionCase.prosecutionCase().build())
                        .findFirst()
                        .orElse(null);
        assertThat(caseDefendantsUpdatedForHearing, nullValue());
    }

    @Test
    public void shouldRaiseEventWhenHearingResultHasNotAlreadyShared() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
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
    public void shouldAddDefenceCounselBeforeHearingEnded(){
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
    public void shouldAddProsecutionCounselBeforeHearingEnded(){
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

        final List<Object> events = hearingAggregate.addProsecutionCounsel(prosecutionCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselChangeIgnored prosecutionCounselChangeIgnored = (ProsecutionCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(prosecutionCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
    }

    @Test
    public void shouldBookProvisionalHearingSlots() {
        final UUID courtScheduleId1 = randomUUID();
        final UUID courtScheduleId2 = randomUUID();
        final List<ProvisionalHearingSlotInfo> provisionalHearingSlotInfos = asList(
                new ProvisionalHearingSlotInfo(courtScheduleId1),
                new ProvisionalHearingSlotInfo(courtScheduleId2));

        final UUID hearingId = randomUUID();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Stream<Object> stream = hearingAggregate.bookProvisionalHearingSlots(hearingId, provisionalHearingSlotInfos);

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));

        final BookProvisionalHearingSlots bookProvisionalHearingSlots = (BookProvisionalHearingSlots) objectList.get(0);
        assertThat(bookProvisionalHearingSlots.getHearingId(), is(hearingId));

        final List<ProvisionalHearingSlotInfo> slots = bookProvisionalHearingSlots.getSlots();
        assertThat(slots.size(), is(provisionalHearingSlotInfos.size()));
        assertThat(slots.get(0).getCourtScheduleId(), is(courtScheduleId1));
        assertThat(slots.get(1).getCourtScheduleId(), is(courtScheduleId2));
    }



    @Test
    public void shouldRaiseMultipleDraftResultsSavedWhenAllTargetsAreValid(){

        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
               // .withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResulstSaved = eventStream.filter(x  -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved)x).findFirst();
        assertThat("MultipleDraftResulstSaved not present", multipleDraftResulstSaved.orElse(null), notNullValue() );

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenAllTargetsAreNotValid(){
        final HearingAggregate hearingAggregate = new HearingAggregate();

        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                //.withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final Target dupTarget = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                //.withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResulstSaved = eventStream.filter(x  -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved)x).findFirst();
        assertThat("MultipleDraftResulstSaved present", multipleDraftResulstSaved.orElse(null), nullValue());

    }

    @Test
    public void shouldRaiseSaveDraftResultErrorWhenAllTargetsAreNotValid(){
        final HearingAggregate hearingAggregate = new HearingAggregate();

        Map<UUID, Target> existingTargets =  new HashMap<>();

        final Target previousTarget = target().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                //     .withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();

        existingTargets.put(randomUUID(),previousTarget);

        HearingAggregateMomento hearingAggregateMomento = mock(HearingAggregateMomento.class);
        when(hearingAggregateMomento.getTargets()).thenReturn(existingTargets);

        setField(hearingAggregate, "momento", hearingAggregateMomento);
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
           //     .withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final Target dupTarget = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(randomUUID())
          //      .withHearingStates(Arrays.asList(SHARED_AMEND_LOCKED_USER_ERROR.name()))
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x  -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed)x).findFirst();
        assertThat("SaveDraftResultFailed not present", saveDraftResultFailed.orElse(null), nullValue());

    }

    @Test
    public void shouldCorrectHearingDaysWithoutCourtCentreIfNotAlreadySet() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        initiateHearingCommand
                .getHearing()
                .getHearingDays()
                .forEach(hearingDay -> {
                    hearingDay.setCourtRoomId(null);
                    hearingDay.setCourtCentreId(null);
                });

        HearingAggregate hearingAggregate = new HearingAggregate();

        final HearingInitiated result = (HearingInitiated) hearingAggregate.initiate(initiateHearingCommand.getHearing()).collect(Collectors.toList()).get(0);

        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));

        final HearingDaysWithoutCourtCentreCorrected event = new HearingDaysWithoutCourtCentreCorrected();
        event.setId(randomUUID());
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();

        HearingDay hearingDay = new HearingDay(courtCentreId, courtRoomId, false,0, 0, ZonedDateTime.now());
        event.setHearingDays(asList(hearingDay));
        hearingAggregate.apply(event);

        final List<HearingDay> actualHearingDays = result.getHearing().getHearingDays();
        assertThat(actualHearingDays.stream().map(HearingDay::getCourtCentreId).collect(toSet()), is(of(courtCentreId)));
        assertThat(actualHearingDays.stream().map(HearingDay::getCourtRoomId).collect(toSet()), is(of(courtRoomId)));
    }

    @Test
    public void shouldRaiseEventOnRequestApprovalRejectedCommand() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();

        final Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.TRUE);
        hearingAggregate.apply(new HearingInitiated(hearing));

        final UUID userId = randomUUID();
        Target target = Target.target()
                .withHearingId(hearing.getId())

                .build();

        final ApprovalRequestRejected approvalRequestRejected = (ApprovalRequestRejected)
                hearingAggregate.approvalRequest(hearing.getId(), userId)
                        .findFirst()
                        .orElse(null);
        assertThat(approvalRequestRejected, notNullValue());
        assertThat(approvalRequestRejected.getHearingId(), is(hearing.getId()));
        assertThat(approvalRequestRejected.getUserId(), is(userId));
    }

    @Test
    public void shouldRaiseEventOnValidateResultAmendmentsCommand() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.TRUE);
        hearingAggregate.apply(new HearingInitiated(hearing));
        final UUID userId = randomUUID();
        final ResultAmendmentsValidationFailed validationFailed = (ResultAmendmentsValidationFailed)
                hearingAggregate.validateResultsAmendments(hearing.getId(), userId, "APPROVE")
                        .findFirst()
                        .orElse(null);
        assertThat(validationFailed, notNullValue());
        assertThat(validationFailed.getHearingId(), is(hearing.getId()));

    }
}
