package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final HearingInitiated result = (HearingInitiated) new HearingAggregate().initiate(initiateHearingCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));
    }

    @Test
    public void initiateHearingOffencePlea() {
        final UpdateHearingWithInheritedPleaCommand command = new UpdateHearingWithInheritedPleaCommand(
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                PAST_LOCAL_DATE.next(),
                PleaValue.GUILTY,
                uk.gov.justice.json.schemas.core.DelegatedPowers.delegatedPowers()
                        .withUserId(UUID.randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next()).build()
        );
        HEARING_AGGREGATE.inheritPlea(command)
                .map(InheritedPlea.class::cast)
                .forEach(event -> {
                    assertThat(event.getCaseId(), is(command.getCaseId()));
                    assertThat(event.getDefendantId(), is(command.getDefendantId()));
                    assertThat(event.getHearingId(), is(command.getHearingId()));
                    assertThat(event.getOffenceId(), is(command.getOffenceId()));
                    assertThat(event.getPleaDate(), is(command.getPleaDate()));
                    assertThat(event.getValue(), is(command.getValue()));
                    assertThat(event.getDelegatedPowers().getUserId(), is(command.getDelegatedPowers().getUserId()));
                    assertThat(event.getDelegatedPowers().getFirstName(), is(command.getDelegatedPowers().getFirstName()));
                    assertThat(event.getDelegatedPowers().getLastName(), is(command.getDelegatedPowers().getLastName()));
                });
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

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) new HearingAggregate()
                .logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

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

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

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

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final HearingEventLogged hearingEventLogged = (HearingEventLogged) hearingAggregate
                .logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

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

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        hearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());


        final List<Object> events = hearingAggregate.correctHearingEvent(correctLogEventCommand).collect(Collectors.toList());

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

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .correctHearingEvent(correctLogEventCommand).collect(Collectors.toList()).get(0);

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
        hearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());
        hearingAggregate.correctHearingEvent(CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

        hearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

        final LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

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
        hearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

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

        hearingAggregate.correctHearingEvent(correctLogEventCommand);

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .correctHearingEvent(correctLogEventCommand).collect(Collectors.toList()).get(0);

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

        final Stream<Object> stream = hearingAggregate.updateDefendantDetails(command);

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

        final DefendantDetailsUpdated result = (DefendantDetailsUpdated) hearingAggregate.updateDefendantDetails(command).collect(Collectors.toList()).get(0);

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

        final HearingEventsUpdated result = (HearingEventsUpdated) hearingAggregate.updateHearingEvents(updateHearingEventsCommand).findFirst().orElse(null);

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
                .updateHearingEvents(updateHearingEventsCommand).findFirst().orElse(null);

        assertNotNull(result);
        assertThat(result.getHearingId(), is(updateHearingEventsCommand.getHearingId()));
    }

    @Test
    public void should_raise_a_private_event_nows_generated() {

        final NowsMaterialStatusUpdated expected = new NowsMaterialStatusUpdated(UUID.randomUUID(), UUID.randomUUID(), "generated");
        final Stream<Object> stream = new HearingAggregate().nowsMaterialStatusUpdated(expected);

        assertThat(stream.findFirst().orElse(null), is(expected));

    }

    private CaseDefendantDetailsWithHearingCommand initiateDefendantCommandTemplate(final UUID hearingId) {

        return CaseDefendantDetailsWithHearingCommand.caseDefendantDetailsWithHearingCommand()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());
    }
}
