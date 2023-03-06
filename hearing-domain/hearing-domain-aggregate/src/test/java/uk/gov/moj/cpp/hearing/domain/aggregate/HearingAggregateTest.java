package uk.gov.moj.cpp.hearing.domain.aggregate;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.core.courts.Target2.target2;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.HearingState.INITIALISED;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.guiltyPleaTypes;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithAllLevelJudicialResults;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithIsBoxHearing;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType.GUILTY;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateDefendantCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import com.google.common.collect.Lists;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.hearing.courts.CourtListRestricted;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingAmendCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.command.updateEvent.UpdateHearingEventsCommand;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.AddCaseDefendantsForHearing;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingLockedByOtherUser;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnallocated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestRejected;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.MultipleDraftResultsSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidationFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

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
    public void shouldRaiseSaveDraftResultsFailedEventWhenTargetIsInvalid() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithAllLevelJudicialResults();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        final HearingInitiated result = (HearingInitiated) hearingAggregate.initiate(initiateHearingCommand.getHearing()).collect(Collectors.toList()).get(0);

        final LocalDate hearingDay = result.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();
        final UUID hearingId = result.getHearing().getId();

        final List<Target> invalidTargets = Arrays.asList(Target.target()
                .withHearingId(hearingId)
                .withDefendantId(null)
                .withDraftResult(null)
                .withOffenceId(null)
                .withTargetId(UUID.randomUUID())
                .withShadowListed(null)
                .withHearingDay(hearingDay)
                .build());

        final SaveDraftResultFailed saveDraftResultFailed = (SaveDraftResultFailed) hearingAggregate.saveMultipleDraftResultsForHearingDay(invalidTargets, hearingDay, randomUUID()).collect(Collectors.toList()).get(0);

        assertThat(saveDraftResultFailed.getTarget().getHearingId(), is(hearingId));
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
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

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

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Already logged"));
        assertThat(hearingEventIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));

    }

    @Test
    public void shouldIgnoreLogHearingEventGivenHearingIsBoxHearing() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithIsBoxHearing(true);

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

        final HearingEventIgnored hearingEventIgnored = (HearingEventIgnored) hearingAggregate
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        assertThat(hearingEventIgnored.getReason(), is("Hearing Event Log not allowed for Box Hearing"));
        assertThat(hearingEventIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(hearingEventIgnored.getEventTime(), is(logEventCommand.getEventTime()));
        assertThat(hearingEventIgnored.getRecordedLabel(), is(logEventCommand.getRecordedLabel()));
        assertThat(hearingEventIgnored.getHearingEventDefinitionId(), is(logEventCommand.getHearingEventDefinitionId()));
        assertThat(hearingEventIgnored.getHearingEventId(), is(logEventCommand.getHearingEventId()));
        assertThat(hearingEventIgnored.isAlterable(), is(false));
    }

    @Test
    public void shouldAmendHearing() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAmendCommand hearingAmendCommand = new HearingAmendCommand(UUID.randomUUID(),SHARED_AMEND_LOCKED_ADMIN_ERROR);
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        final HearingAmended hearingAmended = (HearingAmended) hearingAggregate
                .amendHearing(hearingAmendCommand.getHearingId(), hearingAmendCommand.getHearingId(), hearingAmendCommand.getNewHearingState()).collect(Collectors.toList()).get(0);
        assertThat(hearingAmended.getHearingId(), is(hearingAmendCommand.getHearingId()));
        assertThat(hearingAmended.getNewHearingState(), is(hearingAmendCommand.getNewHearingState()));
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
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

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

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID()));


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
    public void shouldNotRaiseCTLClockStoppedEventForNewtonHearing() {

        final UUID previousHearingEventId = randomUUID();

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        initiateHearingCommand.getHearing().getType().setId(UUID.fromString("b4352aac-5c07-30c5-a7f5-7d123c80775a"));

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

        final List<Object> events = hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList());
        assertThat(events.size(), is(1));

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

        hearingAggregate.logHearingEvent(logEventCommandArbitrary.getHearingId(), logEventCommandArbitrary.getHearingEventDefinitionId(), logEventCommandArbitrary.getAlterable(), logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary, Arrays.asList(randomUUID()));

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

        hearingAggregate.logHearingEvent(logEventCommandArbitrary.getHearingId(), logEventCommandArbitrary.getHearingEventDefinitionId(), logEventCommandArbitrary.getAlterable(), logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary, Arrays.asList(randomUUID()));

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
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

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
                logEventCommandArbitrary.getDefenceCounselId(), hearingEventArbitrary, Arrays.asList(randomUUID()));

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

        hearingAggregate.apply(ResultsShared.builder().withHearing(Hearing.hearing().withId(randomUUID()).build()).build());

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

        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getFirstName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getFirstName()));
        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId(), Matchers.is(result.getDefendant().getMasterDefendantId()));
        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getLastName(), Matchers.is(result.getDefendant().getPersonDefendant().getPersonDetails().getLastName()));

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
                .logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList());

        final HearingEventLogged startHearingEventLogged = (HearingEventLogged) events.get(0);
        assertHearingEventLogged(startHearingEventLogged, logEventCommand, initiateHearingCommand);
    }

    @Test
    public void shouldNotRaiseEventWhenHearingResultHasAlreadyShared() {


        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(false);


        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(hearing));


        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);



        hearingAggregate.apply(ResultsSharedV3.builder()
                .withNewAmendmentResults(asList(new NewAmendmentResult(randomUUID(), ZonedDateTime.now())))
                .withHearingDay(hearingDay)
                .withHearing(Hearing.hearing()
                        .withHasSharedResults(true)
                        .withId(hearing.getId())
                        .build()
                )
                .build()
        );

        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing)
                hearingAggregate.addOrUpdateCaseDefendantsForHearing(initiateHearingCommand.getHearing().getId(), ProsecutionCase.prosecutionCase().build())
                        .findFirst()
                        .orElse(null);
        assertThat(caseDefendantsUpdatedForHearing, nullValue());
    }

    @Test
    public void shouldNotRaiseEventWhenHearingIsUnAllocated() {


        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final Hearing hearing = initiateHearingCommand.getHearing();

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(hearing));


        hearingAggregate.apply(new HearingUnallocated(asList(randomUUID()), asList(randomUUID()), asList(randomUUID()), hearing.getId()));

        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing)
                hearingAggregate.addOrUpdateCaseDefendantsForHearing(initiateHearingCommand.getHearing().getId(), ProsecutionCase.prosecutionCase().build())
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
                hearingAggregate.addOrUpdateCaseDefendantsForHearing(hearing.getId(), ProsecutionCase.prosecutionCase().build())
                        .findFirst()
                        .orElse(null);

        assertThat(caseDefendantsUpdatedForHearing.getHearingId(), is(hearing.getId()));
    }

    @Test
    public void shouldRaiseEventWhenNewDefendantAddedToHearing() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearing.setHasSharedResults(Boolean.FALSE);

        UUID prosecutionCaseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        Offence offence = Offence.offence().withId(offenceId).build();
        Defendant defendant = Defendant.defendant().withId(defendantId).withOffences(singletonList(offence)).build();
        ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withId(prosecutionCaseId)
                .withDefendants(singletonList(defendant))
                .build();
        hearing.setProsecutionCases(singletonList(prosecutionCase));

        hearingAggregate.apply(new HearingInitiated(hearing));

        UUID newDefendantId = randomUUID();
        Defendant newDefendant = Defendant.defendant().withId(newDefendantId).withOffences(singletonList(offence)).build();
        ProsecutionCase updatedProsecutionCase = ProsecutionCase.prosecutionCase()
                .withId(prosecutionCaseId)
                .withDefendants(singletonList(newDefendant))
                .build();

        final Stream<Object> stream = hearingAggregate.addOrUpdateCaseDefendantsForHearing(hearing.getId(), updatedProsecutionCase);
        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(2));

        CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = (CaseDefendantsUpdatedForHearing) objectList.get(0);
        assertThat(caseDefendantsUpdatedForHearing.getHearingId(), is(hearing.getId()));

        AddCaseDefendantsForHearing addCaseDefendantsForHearing = (AddCaseDefendantsForHearing) objectList.get(1);
        assertThat(addCaseDefendantsForHearing.getHearingId(), is(hearing.getId()));
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
    public void shouldAddDefenceCounselBeforeHearingEnded() {
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

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(), new ArrayList<>(),
                "Margaret", randomUUID(), "Brown", "H", "Y", "Ms", randomUUID());

        final List<Object> events = HEARING_AGGREGATE.addDefenceCounsel(defenceCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselAdded defenceCounselAdded = (DefenceCounselAdded) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselAdded.getHearingId(), is(logEventCommand.getHearingId()));
    }

    @Test
    public void shouldNotAllowedToAddDefenceCounsel_afterHearingEnded_forSPICases() {
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

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(), new ArrayList<>(),
                "Leigh", randomUUID(), "Ann", "H", "Y", "Ms", randomUUID());

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addDefenceCounsel(defenceCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselChangeIgnored defenceCounselChangeIgnored = (DefenceCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(defenceCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
    }

    @Test
    public void shouldNotAllowedToAddDefenceCounsel_afterHearingEnded_forSJPCases() {
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

        final DefenceCounsel defenceCounsel = new DefenceCounsel(new ArrayList<>(), new ArrayList<>(),
                "Leigh", randomUUID(), "Ann", "H", "Y", "Ms", randomUUID());

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addDefenceCounsel(defenceCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final DefenceCounselChangeIgnored defenceCounselChangeIgnored = (DefenceCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(defenceCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(defenceCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
    }

    @Test
    public void shouldAddProsecutionCounselBeforeHearingEnded() {
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
                singletonList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                singletonList(randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final List<Object> events = HEARING_AGGREGATE.addProsecutionCounsel(prosecutionCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselAdded prosecutionCounselAdded = (ProsecutionCounselAdded) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselAdded.getHearingId(), is(logEventCommand.getHearingId()));
    }

    @Test
    public void shouldNotAllowedToAddProsecutionCounsel_afterHearingEnded_forSPICases() {
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
                singletonList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                singletonList(randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

        final List<Object> events = hearingAggregate.addProsecutionCounsel(prosecutionCounsel, logEventCommand.getHearingId()).collect(Collectors.toList());
        final ProsecutionCounselChangeIgnored prosecutionCounselChangeIgnored = (ProsecutionCounselChangeIgnored) events.get(0);

        assertThat(events, notNullValue());
        assertThat(prosecutionCounselChangeIgnored.getHearingId(), is(logEventCommand.getHearingId()));
        assertThat(prosecutionCounselChangeIgnored.getCaseURN(), is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
    }

    @Test
    public void shouldNotAllowedToAddProsecutionCounsel_afterHearingEnded_forSJPCases() {
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
                singletonList(LocalDate.now()),
                STRING.next(),
                randomUUID(),
                STRING.next(),
                STRING.next(),
                singletonList(randomUUID()),
                STRING.next(),
                STRING.next(),
                randomUUID()

        );

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        hearingAggregate.logHearingEvent(logEventCommand.getHearingId(), logEventCommand.getHearingEventDefinitionId(), logEventCommand.getAlterable(), logEventCommand.getDefenceCounselId(), hearingEvent, Arrays.asList(randomUUID())).collect(Collectors.toList()).get(0);

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
        final Stream<Object> stream = hearingAggregate.bookProvisionalHearingSlots(hearingId, provisionalHearingSlotInfos, null, null, null);

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
    public void shouldRaiseMultipleDraftResultsSavedWhenAllTargetsAreValidDefendantOffence() {

        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final Target target2 = target().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(target2);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("multipleDraftResultsSaved is present", multipleDraftResultsSaved.isPresent());

    }

    @Test
    public void shouldRaiseMultipleDraftResultsSavedWhenAllTargetsAreValidApplication() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final Target target = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .build();
        final Target target2 = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(target2);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved present", multipleDraftResultsSaved.isPresent());

    }

    @Test
    public void shouldRaiseMultipleDraftResultsSavedWhenAllTargetsAreValidApplicationOffence() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final Target target = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withOffenceId(randomUUID())
                .withResultLines(new ArrayList<>())
                .build();
        final Target target2 = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withOffenceId(randomUUID())
                .withResultLines(new ArrayList<>())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(target2);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved present", multipleDraftResultsSaved.isPresent());

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenAnyTargetIsNotValid() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final Target invalidTarget = target().withTargetId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(invalidTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", not(multipleDraftResultsSaved.isPresent()));

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenNotMatchedWithAnExistingTargetDefendantOffence() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final UUID targetId = randomUUID();
        final Target target = target()
                .withTargetId(targetId)
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final Target dupTarget = target()
                .withTargetId(targetId)
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);

        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());

        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", !multipleDraftResultsSaved.isPresent());
    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenNotMatchedWithAnExistingTargetApplication() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final Target target = target()
                .withTargetId(targetId)
                .withHearingId(hearingId)
                .withApplicationId(randomUUID())
                .withResultLines(new ArrayList<>())
                .build();
        final Target dupTarget = target().withTargetId(targetId)
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", not(multipleDraftResultsSaved.isPresent()));

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenNotMatchedWithAnExistingTargetApplicationOffence() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final Target target = target()
                .withTargetId(targetId)
                .withHearingId(hearingId)
                .withApplicationId(randomUUID())
                .withOffenceId(randomUUID())
                .withResultLines(new ArrayList<>())
                .build();
        final Target dupTarget = target()
                .withTargetId(targetId)
                .withHearingId(hearingId)
                .withApplicationId(randomUUID())
                .withOffenceId(randomUUID())
                .withResultLines(new ArrayList<>())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", not(multipleDraftResultsSaved.isPresent()));

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenNewTargetIsNotValid() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final Target target = target()
                .withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .withApplicationId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", not(multipleDraftResultsSaved.isPresent()));

    }

    @Test
    public void shouldNotRaiseMultipleDraftResultsSavedWhenAllTargetsAreNotValid() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final Target validTarget = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final Target inValidTarget = target().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .withApplicationId(randomUUID())
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(validTarget);
        targetList.add(inValidTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<MultipleDraftResultsSaved> multipleDraftResultsSaved = eventStream.filter(x -> x instanceof MultipleDraftResultsSaved).map(x -> (MultipleDraftResultsSaved) x).findFirst();
        assertThat("MultipleDraftResultsSaved not present", not(multipleDraftResultsSaved.isPresent()));

    }

    @Test
    public void shouldRaiseSaveDraftResultFailedWhenAnyTargetIsInvalid() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID applicationId = randomUUID();

        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .withApplicationId(applicationId)
                .build();
        final List<Target> targets = new ArrayList<>();
        targets.add(target);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targets, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed) x).findFirst();
        assertThat("SaveDraftResultFailed present", saveDraftResultFailed.isPresent());
    }

    @Test
    public void shouldRaiseSaveDraftResultFailedWhenDefenceOffenceIsAlreadyUsedInAnotherTarget() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();

        final Target target2 = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final List<Target> targets = new ArrayList<>();
        targets.add(target);
        targets.add(target2);

        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targets, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed) x).findFirst();
        assertThat("SaveDraftResultFailed is present", saveDraftResultFailed.isPresent());
    }

    @Test
    public void shouldRaiseSaveDraftResultFailedWhenApplicationOffenceIsAlreadyUsedInAnotherTarget() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();

        final Target target = target().withTargetId(randomUUID())
                .withApplicationId(applicationId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();

        final Target target2 = target().withTargetId(randomUUID())
                .withApplicationId(applicationId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final List<Target> targets = new ArrayList<>();
        targets.add(target);
        targets.add(target2);

        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targets, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed) x).findFirst();
        assertThat("SaveDraftResultFailed is present", saveDraftResultFailed.isPresent());
    }

    @Test
    public void shouldRaiseSaveDraftResultFailedWhenApplicationIsAlreadyUsedInAnotherTarget() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID targetId = randomUUID();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        final UUID applicationId = randomUUID();

        final Target target = target().withTargetId(randomUUID())
                .withApplicationId(applicationId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .build();

        final Target target2 = target().withTargetId(randomUUID())
                .withApplicationId(applicationId)
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .build();
        final List<Target> targets = new ArrayList<>();
        targets.add(target);
        targets.add(target2);

        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targets, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed) x).findFirst();
        assertThat("SaveDraftResultFailed is present", saveDraftResultFailed.isPresent());
    }

    @Test
    public void shouldNotDeleteDraftResultWhenHearingIsNotLocked() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));
        final UUID userId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        hearingAggregate.amendHearing(hearingId,userId, INITIALISED);
        final Stream stream = hearingAggregate.deleteDraftResultV2(userId, hearingId,hearingDay);
        assertThat(stream.findFirst().get().getClass().getCanonicalName(), Matchers.is(HearingLockedByOtherUser.class.getCanonicalName()));

    }

    @Test
    public void shouldDeleteDraftResultWhenHearingIsLocked() {
        final UUID userId = UUID.randomUUID();
        final UUID hearingId = UUID.randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.amendHearing(hearingId,userId, SHARED_AMEND_LOCKED_ADMIN_ERROR);

        final Stream stream = hearingAggregate.deleteDraftResultV2(userId, hearingId,hearingDay);
        assertThat(stream.findFirst().get().getClass().getCanonicalName(), Matchers.is(DraftResultDeletedV2.class.getCanonicalName()));

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

        HearingDay hearingDay = new HearingDay(courtCentreId, courtRoomId, false, false, 0, 0, ZonedDateTime.now());
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
    public void shouldRaiseSaveDraftResultErrorWhenAllTargetsAreNotValid() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final LocalDate hearingDay = initiateHearingCommand.getHearing().getHearingDays().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hearing Day is not present"))
                .getSittingDay()
                .toLocalDate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        final HearingAggregate hearingAggregate = new HearingAggregate();

        Map<UUID, Target2> existingTargets = new HashMap<>();

        final Target2 previousTarget = target2().withTargetId(randomUUID())
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(randomUUID())
                .build();

        existingTargets.put(randomUUID(), previousTarget);

        HearingAggregateMomento hearingAggregateMomento = mock(HearingAggregateMomento.class);
        when(hearingAggregateMomento.getMultiDayTargets().get(hearingDay)).thenReturn(existingTargets);
        when(hearingAggregateMomento.getHearing()).thenReturn(hearing);

        setField(hearingAggregate, "momento", hearingAggregateMomento);
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final Target target = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final Target dupTarget = target().withTargetId(randomUUID())
                .withDefendantId(defendantId)
                .withHearingId(randomUUID())
                .withResultLines(new ArrayList<>())
                .withOffenceId(offenceId)
                .build();
        final List<Target> targetList = new ArrayList<>();
        targetList.add(target);
        targetList.add(dupTarget);
        final Stream<Object> eventStream = hearingAggregate.saveAllDraftResults(targetList, randomUUID());
        final Optional<SaveDraftResultFailed> saveDraftResultFailed = eventStream.filter(x -> x instanceof SaveDraftResultFailed).map(x -> (SaveDraftResultFailed) x).findFirst();
        assertThat("SaveDraftResultFailed not present", saveDraftResultFailed.orElse(null), nullValue());

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

    @Test
    public void shouldRaiseEventHearingDeleted() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final UUID hearingId = hearing.getId();

        final List<Object> eventStream = hearingAggregate.deleteHearing(hearingId).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingDeleted hearingDeleted = (HearingDeleted) eventStream.get(0);
        assertThat(hearingDeleted.getHearingId(), is(hearingId));
        assertThat(hearingDeleted.getProsecutionCaseIds().size(), is(1));
        assertThat(hearingDeleted.getDefendantIds().size(), is(1));
        assertThat(hearingDeleted.getOffenceIds().size(), is(1));
        assertThat(hearingDeleted.getCourtApplicationIds().size(), is(1));
    }

    @Test
    public void shouldRaiseHearingIgnoreEventforHearingDeletedIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();

        final List<Object> eventStream = hearingAggregate.deleteHearing(hearingId).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) eventStream.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));
    }


    @Test
    public void shouldRaiseHearingIgnoreEventForNextHearingDateChangedIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();

        final Stream<Object> stream = hearingAggregate.changeNextHearingStartDate(hearingId,randomUUID(),ZonedDateTime.now());

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) objectList.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldRaiseEventHearingUnallocated() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final UUID hearingId = hearing.getId();
        final UUID offenceId = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();
        final UUID defendantId = hearing.getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearing.getProsecutionCases().get(0).getId();
        hearingAggregate.apply(new HearingDeleted(asList(caseId), asList(defendantId), asList(offenceId), emptyList(), hearingId));

        final List<Object> eventStream = hearingAggregate.unAllocateHearing(hearingId, Arrays.asList(offenceId)).collect(toList());

        assertThat(eventStream.size(), is(1));
        final HearingUnallocated hearingUnallocated = (HearingUnallocated) eventStream.get(0);
        assertThat(hearingUnallocated.getHearingId(), is(hearingId));
        assertThat(hearingUnallocated.getProsecutionCaseIds().size(), is(1));
        assertThat(hearingUnallocated.getDefendantIds().size(), is(1));
        assertThat(hearingUnallocated.getOffenceIds().size(), is(1));
    }

    @Test
    public void shouldRaiseHearingIgnoredForHearingUnallocatedIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();
        final List<Object> eventStream = hearingAggregate.unAllocateHearing(hearingId, Arrays.asList(randomUUID())).collect(toList());
        assertThat(eventStream.size(), is(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) eventStream.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldRestrictCourtList() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingAggregate hearingAggregate = new HearingAggregate();

        final Hearing hearing = initiateHearingCommand.getHearing();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final UUID hearingId = hearing.getId();

        final List<UUID> caseIds = Arrays.asList(randomUUID(),  randomUUID());
        final CourtListRestricted courtListRestricted = CourtListRestricted.courtListRestricted()
                .withHearingId(hearingId)
                .withCaseIds(caseIds).build();

        final Stream<Object> stream = hearingAggregate.courtListRestrictions(courtListRestricted);

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));

        final uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted courtListRestrictedEvent = (uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted) objectList.get(0);
        assertThat(courtListRestrictedEvent.getHearingId(), is(hearingId));

        assertThat(caseIds.size(), is(courtListRestrictedEvent.getCaseIds().size()));
        assertThat(caseIds.get(0), is(courtListRestrictedEvent.getCaseIds().get(0)));
        assertThat(caseIds.get(1), is(courtListRestrictedEvent.getCaseIds().get(1)));
    }

    @Test
    public void shouldRaiseHearingIgnoredForRestrictCourtListIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();
        final List<UUID> caseIds = Arrays.asList(randomUUID(),  randomUUID());
        final CourtListRestricted courtListRestricted = CourtListRestricted.courtListRestricted()
                .withHearingId(hearingId)
                .withCaseIds(caseIds).build();
        final Stream<Object> stream = hearingAggregate.courtListRestrictions(courtListRestricted);

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) objectList.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldRaiseHearingIgnoredForRemoveOffencesFromExistingHearingIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = randomUUID();

        final Stream<Object> stream = hearingAggregate.removeOffencesFromExistingHearing(hearingId, Arrays.asList(randomUUID()));

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) objectList.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));
    }

    @Test
    public void shouldRaiseHearingIgnoredForUpdateHearingVacateTrialDetailsIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = UUID.randomUUID();

        final Stream<Object> stream = hearingAggregate.updateHearingVacateTrialDetails(hearingId, true, randomUUID());
        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) objectList.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));

    }

    @Test
    public void shouldRaiseHearingIgnoredForUpdateExistingHearingIfNoHearingExist() {
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final UUID hearingId = UUID.randomUUID();
        final List<UUID> shadowListedOffences = Arrays.asList(randomUUID(),  randomUUID());

        final Stream<Object> stream = hearingAggregate.updateExistingHearing(hearingId, null, shadowListedOffences);
        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingChangeIgnored hearingChangeIgnored = (HearingChangeIgnored) objectList.get(0);
        assertThat(hearingChangeIgnored.getHearingId(), is(hearingId));

    }
    @Test
    public void shouldRaiseOffencesRemovedFromExistingHearingWhenNotAllOffencesPassedForHearing() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        final UUID offence2Id = randomUUID();
        final Offence offence2 =Offence.offence().withId(offence2Id).build();
        ofNullable(hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> {
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        defendant.getOffences().add(offence2);
                    });
                });

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final Stream<Object> stream = hearingAggregate.removeOffencesFromExistingHearing(hearing.getId(), Arrays.asList(offence2Id));

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = (OffencesRemovedFromExistingHearing) objectList.get(0);
        assertThat(offencesRemovedFromExistingHearing.getHearingId(), is(hearing.getId()));
        assertThat(offencesRemovedFromExistingHearing.getOffenceIds().get(0), is(offence2Id));
        assertThat(offencesRemovedFromExistingHearing.getDefendantIds().size(), is(0));
        assertThat(offencesRemovedFromExistingHearing.getProsecutionCaseIds().size(), is(0));


    }

    @Test
    public void shouldRaiseHearingDeletedWhenAllOffencesPassedForHearing() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        final UUID offence2Id = randomUUID();
        final Offence offence2 =Offence.offence().withId(offence2Id).build();
        ofNullable(hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> {
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        defendant.getOffences().add(offence2);
                    });
                });

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing));


        final List<UUID> offenceIds = ofNullable( hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .flatMap(defendant -> defendant.getOffences().stream())
                .map(Offence::getId)
                .collect(toList());

        final Stream<Object> stream = hearingAggregate.removeOffencesFromExistingHearing(hearing.getId(), offenceIds);

        final List<Object> objectList = stream.collect(Collectors.toList());
        assertThat(objectList, hasSize(1));
        final HearingDeleted hearingDeleted = (HearingDeleted) objectList.get(0);
        assertThat(hearingDeleted.getHearingId(), is(hearing.getId()));
        assertThat(hearingDeleted.getOffenceIds().size(), is(2));
        assertThat(hearingDeleted.getProsecutionCaseIds().size(), is(1));
        assertThat(hearingDeleted.getDefendantIds().size(), is(1));


    }

    @Test
    public void shouldCheckOffencesOfHearingWhenOneofItisDeletedInOffencesRemovedFromHearing() {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final Hearing hearing = initiateHearingCommand.getHearing();
        final UUID offence2Id = randomUUID();
        final Offence offence2 =Offence.offence().withId(offence2Id).build();
        ofNullable(hearing.getProsecutionCases().stream()).orElseGet(Stream::empty)
                .forEach(prosecutionCase -> {
                    prosecutionCase.getDefendants().forEach(defendant -> {
                        defendant.getOffences().add(offence2);
                    });
                });

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing));

        hearingAggregate.removeOffencesFromExistingHearing(hearing.getId(), Arrays.asList(offence2Id));

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommand, LocalDate.now(), LocalDate.now());
        final Target targetDraft = saveDraftResultCommand.getTarget();
        final ResultLine resultLineIn = targetDraft.getResultLines().get(0);
        targetDraft.setResultLines(null);
        final SharedResultsCommandResultLine sharedResultsCommandResultLine = new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                resultLineIn.getOrderedDate(),
                resultLineIn.getSharedDate(),
                resultLineIn.getResultLineId(),
                targetDraft.getOffenceId(),
                targetDraft.getDefendantId(),
                resultLineIn.getResultDefinitionId(),
                resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                        p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef())).collect(Collectors.toList()),
                resultLineIn.getResultLabel(),
                resultLineIn.getLevel().name(),
                resultLineIn.getIsModified(),
                resultLineIn.getIsComplete(),
                targetDraft.getApplicationId(),
                resultLineIn.getAmendmentReasonId(),
                resultLineIn.getAmendmentReason(),
                resultLineIn.getAmendmentDate(),
                resultLineIn.getFourEyesApproval(),
                resultLineIn.getApprovedDate(),
                resultLineIn.getIsDeleted(),
                null,
                null,
                targetDraft.getShadowListed(),
                targetDraft.getDraftResult()
        );
        final ResultsShared resultsShared = (ResultsShared) hearingAggregate.shareResults(hearing.getId(), courtClerk1, ZonedDateTime.now(), Lists.newArrayList(sharedResultsCommandResultLine), HearingState.SHARED, null)
                .collect(Collectors.toList()).get(0);

        assertEquals(1, resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().size());
        assertEquals(1, resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().size());
        assertEquals(1, resultsShared.getHearing().getProsecutionCases().size());
        assertEquals(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId(), resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId());

    }


}
