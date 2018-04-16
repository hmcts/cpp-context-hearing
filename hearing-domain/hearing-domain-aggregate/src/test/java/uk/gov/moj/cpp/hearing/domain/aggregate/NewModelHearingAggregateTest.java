package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;

public class NewModelHearingAggregateTest {
    
    private static final NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();

    @Test
    public void initiate() {
        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Initiated result = (Initiated) new NewModelHearingAggregate().initiate(initiateHearingCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getCases(), is(initiateHearingCommand.getCases()));
        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));
    }

    @Test
    public void initiateHearingOffencePlea() {
       final  InitiateHearingOffencePleaCommand command = new InitiateHearingOffencePleaCommand(
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                randomUUID(),
                PAST_LOCAL_DATE.next(),
                "GUILTY"
        );
        newModelHearingAggregate.initiateHearingOffencePlea(command)
            .map(InitiateHearingOffencePlead.class::cast)
            .forEach(event -> {
                assertThat(event.getCaseId(), is(command.getCaseId()));
                assertThat(event.getDefendantId(), is(command.getDefendantId()));
                assertThat(event.getHearingId(), is(command.getHearingId()));
                assertThat(event.getOffenceId(), is(command.getOffenceId()));
                assertThat(event.getPleaDate(), is(command.getPleaDate()));
                assertThat(event.getValue(), is(command.getValue()));
        });
    }

    @Test
    public void logHearingEvent_shouldIgnore_givenNoPreviousHearing() {

        LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) new NewModelHearingAggregate()
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

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        newModelHearingAggregate.logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

        HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) newModelHearingAggregate
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

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        HearingEventLogged result = (HearingEventLogged) newModelHearingAggregate
                .logHearingEvent(logEventCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(result.getLastHearingEventId(), is(nullValue()));
        assertThat(result.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(result.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(result.getLastModifiedTime(), is(logEventCommand.getLastModifiedTime()));
        assertThat(result.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(result.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(result.isAlterable(), is(false));

        assertThat(result.getCourtCentreId(), is(initiateHearingCommand.getHearing().getCourtCentreId()));
        assertThat(result.getCourtCentreName(), is(initiateHearingCommand.getHearing().getCourtCentreName()));
        assertThat(result.getCourtRoomId(), is(initiateHearingCommand.getHearing().getCourtRoomId()));
        assertThat(result.getCourtRoomName(), is(initiateHearingCommand.getHearing().getCourtRoomName()));
        assertThat(result.getCaseUrn(), is(initiateHearingCommand.getCases().get(0).getUrn()));
        assertThat(result.getCaseId(), is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(result.getHearingType(), is(initiateHearingCommand.getHearing().getType()));
    }

    @Test
    public void correctHearingEvent_shouldLog() {

        UUID previousHearingEventId = randomUUID();

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
        newModelHearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());


        List<Object> events = newModelHearingAggregate.correctHearingEvent(correctLogEventCommand).collect(Collectors.toList());

        HearingEventDeleted hearingEventDeleted = (HearingEventDeleted) events.get(0);
        assertThat(hearingEventDeleted.getHearingEventId(), is(previousHearingEventId));


        HearingEventLogged hearingEventLogged = (HearingEventLogged) events.get(1);

        assertThat(hearingEventLogged.getHearingEventId(), is(correctLogEventCommand.getLatestHearingEventId()));
        assertThat(hearingEventLogged.getLastHearingEventId(), is(previousHearingEventId));
        assertThat(hearingEventLogged.getHearingId(), is(correctLogEventCommand.getHearingId()));
        assertThat(hearingEventLogged.getEventTime(), is(correctLogEventCommand.getEventTime()));
        assertThat(hearingEventLogged.getLastModifiedTime(), is(correctLogEventCommand.getLastModifiedTime()));
        assertThat(hearingEventLogged.getRecordedLabel(), is(correctLogEventCommand.getRecordedLabel()));
        assertThat(hearingEventLogged.getHearingEventDefinitionId(), is(correctLogEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventLogged.isAlterable(), is(false));

        assertThat(hearingEventLogged.getCourtCentreId(), is(initiateHearingCommand.getHearing().getCourtCentreId()));
        assertThat(hearingEventLogged.getCourtCentreName(), is(initiateHearingCommand.getHearing().getCourtCentreName()));
        assertThat(hearingEventLogged.getCourtRoomId(), is(initiateHearingCommand.getHearing().getCourtRoomId()));
        assertThat(hearingEventLogged.getCourtRoomName(), is(initiateHearingCommand.getHearing().getCourtRoomName()));
        assertThat(hearingEventLogged.getCaseUrn(), is(initiateHearingCommand.getCases().get(0).getUrn()));
        assertThat(hearingEventLogged.getCaseId(), is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(hearingEventLogged.getHearingType(), is(initiateHearingCommand.getHearing().getType()));
    }

    @Test
    public void correctHearingEvent_shouldIgnore_givenInvalidPreviousEventId() {

        UUID previousHearingEventId = randomUUID();

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) newModelHearingAggregate
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

        UUID previousHearingEventId = randomUUID();

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
        newModelHearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());
        newModelHearingAggregate.correctHearingEvent(CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

        newModelHearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

        LogEventCommand logEventCommand = LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) newModelHearingAggregate
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

        UUID previousHearingEventId = randomUUID();

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
        newModelHearingAggregate.logHearingEvent(LogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build());

        CorrectLogEventCommand correctLogEventCommand = CorrectLogEventCommand.builder()
                .withHearingEventId(previousHearingEventId)
                .withLastestHearingEventId(randomUUID())
                .withHearingId(randomUUID())
                .withEventTime(PAST_ZONED_DATE_TIME.next())
                .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                .withRecordedLabel(STRING.next())
                .withHearingEventDefinitionId(randomUUID())
                .withAlterable(false)
                .build();

        newModelHearingAggregate.correctHearingEvent(correctLogEventCommand);

        HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) newModelHearingAggregate
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
    public void updateVerdict_shouldUpdateVerdict() {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Verdict verdict = Verdict.builder()
                .withId(randomUUID())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withValue(VerdictValue.builder()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .withCode(STRING.next())
                        .withCategory(STRING.next())
                )
                .withNumberOfJurors(integer(9,12).next())
                .withUnanimous(BOOLEAN.next())
                .build();


        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        OffenceVerdictUpdated offenceVerdictUpdated = (OffenceVerdictUpdated) hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList()).get(0);

        assertThat(offenceVerdictUpdated.getCaseId(), Matchers.is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(offenceVerdictUpdated.getOffenceId(), Matchers.is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId()));
        assertThat(offenceVerdictUpdated.getHearingId(), Matchers.is(initiateHearingCommand.getHearing().getId()));

        assertThat(offenceVerdictUpdated.getVerdictId(), Matchers.is(verdict.getId()));

        assertThat(offenceVerdictUpdated.getVerdictValueId(), Matchers.is(verdict.getValue().getId()));
        assertThat(offenceVerdictUpdated.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(offenceVerdictUpdated.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(offenceVerdictUpdated.getCategory(), Matchers.is(verdict.getValue().getCategory()));
    }


}