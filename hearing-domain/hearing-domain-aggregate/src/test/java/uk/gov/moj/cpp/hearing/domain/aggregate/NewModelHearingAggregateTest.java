package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
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

        HearingInitiated result = (HearingInitiated) new NewModelHearingAggregate().initiate(initiateHearingCommand).collect(Collectors.toList()).get(0);

        assertThat(result.getCases(), is(initiateHearingCommand.getCases()));
        assertThat(result.getHearing(), is(initiateHearingCommand.getHearing()));
    }

    @Test
    public void initiateHearingOffencePlea() {
        final InitiateHearingOffencePleaCommand command = new InitiateHearingOffencePleaCommand(
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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
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
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
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
                        .withCategoryType(STRING.next())
                        .withVerdictTypeId(randomUUID())
                )
                .withNumberOfJurors(integer(9, 12).next())
                .withUnanimous(BOOLEAN.next())
                .build();


        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        VerdictUpsert verdictUpsert = (VerdictUpsert) hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList()).get(0);

        assertThat(verdictUpsert.getCaseId(), Matchers.is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(verdictUpsert.getOffenceId(), Matchers.is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId()));
        assertThat(verdictUpsert.getHearingId(), Matchers.is(initiateHearingCommand.getHearing().getId()));

        assertThat(verdictUpsert.getVerdictId(), Matchers.is(verdict.getId()));

        assertThat(verdictUpsert.getVerdictValueId(), Matchers.is(verdict.getValue().getId()));
        assertThat(verdictUpsert.getVerdictTypeId(), Matchers.is(verdict.getValue().getVerdictTypeId()));
        assertThat(verdictUpsert.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(verdictUpsert.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(verdictUpsert.getCategory(), Matchers.is(verdict.getValue().getCategory()));
    }

    @Test
    public void updateDefendantDetails_shouldIgnore_when_resultshared() {

        final int expected = 0;

        CaseDefendantDetailsWithHearingCommand command = initiateDefendantCommandTemplate();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();

        hearingAggregate.apply(ResultsShared.builder().build());

        Stream<Object> stream = hearingAggregate.updateDefendantDetails(command);

        assertEquals(expected, stream.count());
    }


    @Test
    public void updateDefendantDetails_shouldUpdate_when_resultnotshared() {

        CaseDefendantDetailsWithHearingCommand command = initiateDefendantCommandTemplate();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();

        DefendantDetailsUpdated result = (DefendantDetailsUpdated) hearingAggregate.updateDefendantDetails(command).collect(Collectors.toList()).get(0);

        assertThat(result.getCaseId(), Matchers.is(command.getCaseId()));
    }

    @Test
    public void updateVerdict_whenCategoryTypeIsGuiltyShouldUpdateConvictionDate() {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Verdict verdict = Verdict.builder()
                .withId(randomUUID())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withValue(VerdictValue.builder()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .withCode(STRING.next())
                        .withCategory(STRING.next())
                        .withCategoryType("GUILTY")
                        .withVerdictTypeId(randomUUID())
                )
                .withNumberOfJurors(integer(9, 12).next())
                .withUnanimous(BOOLEAN.next())
                .build();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        List<Object> objects = hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList());

        assertThat(objects.size(), Matchers.is(2));

        VerdictUpsert verdictUpsert = (VerdictUpsert) objects.get(0);
        ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) objects.get(1);

        assertThat(verdictUpsert.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(verdictUpsert.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(verdictUpsert.getVerdictTypeId(), Matchers.is(verdict.getValue().getVerdictTypeId()));
        assertThat(verdictUpsert.getCategory(), Matchers.is(verdict.getValue().getCategory()));
        assertThat(verdictUpsert.getCategoryType(), Matchers.is(verdict.getValue().getCategoryType()));
        
        assertThat(convictionDateAdded.getCaseId(), Matchers.is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(convictionDateAdded.getOffenceId(), Matchers.is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId()));
        assertThat(convictionDateAdded.getHearingId(), Matchers.is(initiateHearingCommand.getHearing().getId()));
        assertThat(convictionDateAdded.getConvictionDate(), Matchers.is(verdict.getVerdictDate()));
    }

    @Test
    public void updateVerdict_whenCategoryTypeIsNotGuiltyShouldClearConvictionDate() {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Verdict verdict = Verdict.builder()
                .withId(randomUUID())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withValue(VerdictValue.builder()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .withCode(STRING.next())
                        .withCategory(STRING.next())
                        .withCategoryType("NOT_GUILTY")
                        .withVerdictTypeId(randomUUID())
                )
                .withNumberOfJurors(integer(9, 12).next())
                .withUnanimous(BOOLEAN.next())
                .build();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        List<Object> objects = hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList());

        assertThat(objects.size(), Matchers.is(2));

        VerdictUpsert verdictUpsert = (VerdictUpsert) objects.get(0);
        ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) objects.get(1);

        assertThat(verdictUpsert.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(verdictUpsert.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(verdictUpsert.getVerdictTypeId(), Matchers.is(verdict.getValue().getVerdictTypeId()));
        assertThat(verdictUpsert.getCategory(), Matchers.is(verdict.getValue().getCategory()));
        assertThat(verdictUpsert.getCategoryType(), Matchers.is(verdict.getValue().getCategoryType()));

        assertThat(convictionDateRemoved.getCaseId(), Matchers.is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(convictionDateRemoved.getOffenceId(), Matchers.is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId()));
        assertThat(convictionDateRemoved.getHearingId(), Matchers.is(initiateHearingCommand.getHearing().getId()));
    }
    
    @Test
    public void updateVerdict_whenCategoryTypeStartsWithGuiltyShouldUpdateConvictionDate() {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Verdict verdict = Verdict.builder()
                .withId(randomUUID())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withValue(VerdictValue.builder()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .withCode(STRING.next())
                        .withCategory(STRING.next())
                        .withCategoryType("GUILTY_BUT_OF_LESSER_OFFENCE")
                        .withLesserOffence("Obstructing a Police Officer")
                        .withVerdictTypeId(randomUUID())
                )
                .withNumberOfJurors(integer(9, 12).next())
                .withUnanimous(BOOLEAN.next())
                .build();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        List<Object> objects = hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList());
        
        assertThat(objects.size(), Matchers.is(2));
        
        VerdictUpsert verdictUpsert = (VerdictUpsert) objects.get(0);
        ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) objects.get(1);

        assertThat(verdictUpsert.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(verdictUpsert.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(verdictUpsert.getVerdictTypeId(), Matchers.is(verdict.getValue().getVerdictTypeId()));
        assertThat(verdictUpsert.getCategory(), Matchers.is(verdict.getValue().getCategory()));
        assertThat(verdictUpsert.getCategoryType(), Matchers.is(verdict.getValue().getCategoryType()));
        assertThat(verdictUpsert.getLesserOffence(), Matchers.is(verdict.getValue().getLesserOffence()));
        
        assertThat(convictionDateAdded.getCaseId(), Matchers.is(initiateHearingCommand.getCases().get(0).getCaseId()));
        assertThat(convictionDateAdded.getOffenceId(), Matchers.is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId()));
        assertThat(convictionDateAdded.getHearingId(), Matchers.is(initiateHearingCommand.getHearing().getId()));
        assertThat(convictionDateAdded.getConvictionDate(), Matchers.is(verdict.getVerdictDate()));
    }
    
    @Test
    public void updateVerdict_whenCategoryTypeNotStartsWithGuiltyShouldNotClearOrUpdateConvictionDate() {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Verdict verdict = Verdict.builder()
                .withId(randomUUID())
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withValue(VerdictValue.builder()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .withCode(STRING.next())
                        .withCategory(STRING.next())
                        .withCategoryType("NO_VERDICT")
                        .withVerdictTypeId(randomUUID())
                )
                .withNumberOfJurors(integer(9, 12).next())
                .withUnanimous(BOOLEAN.next())
                .build();

        NewModelHearingAggregate hearingAggregate = new NewModelHearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        List<Object> objects = hearingAggregate.updateVerdict(
                initiateHearingCommand.getHearing().getId(),
                initiateHearingCommand.getCases().get(0).getCaseId(),
                initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(),
                verdict
        ).collect(Collectors.toList());

        assertThat(objects.size(), Matchers.is(1));
        
        VerdictUpsert verdictUpsert = (VerdictUpsert) objects.get(0);
        
        assertThat(verdictUpsert.getCode(), Matchers.is(verdict.getValue().getCode()));
        assertThat(verdictUpsert.getDescription(), Matchers.is(verdict.getValue().getDescription()));
        assertThat(verdictUpsert.getVerdictTypeId(), Matchers.is(verdict.getValue().getVerdictTypeId()));
        assertThat(verdictUpsert.getCategory(), Matchers.is(verdict.getValue().getCategory()));
        assertThat(verdictUpsert.getCategoryType(), Matchers.is(verdict.getValue().getCategoryType()));
    }
    
    private CaseDefendantDetailsWithHearingCommand initiateDefendantCommandTemplate() {

        final Address.Builder address = Address.address()
                .withAddress1(STRING.next())
                .withAddress2(STRING.next())
                .withAddress3(STRING.next())
                .withAddress4(STRING.next())
                .withPostcode(STRING.next());

        return CaseDefendantDetailsWithHearingCommand.builder()
                .withCaseId(randomUUID())
                .withHearingIds(Collections.singletonList(randomUUID()))
                .withDefendants(Defendant.builder()
                        .withId(randomUUID())
                        .withPerson(Person.builder().withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .withNationality(STRING.next())
                            .withGender(STRING.next())
                            .withAddress(address)
                            .withDateOfBirth(PAST_LOCAL_DATE.next()))
                        .withBailStatus(STRING.next())
                        .withCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .withDefenceOrganisation(STRING.next())
                        .withInterpreter(Interpreter.builder(STRING.next())))
                .build();
    }
}