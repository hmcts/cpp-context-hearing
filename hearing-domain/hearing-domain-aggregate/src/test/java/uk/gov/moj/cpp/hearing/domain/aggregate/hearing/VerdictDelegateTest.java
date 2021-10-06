package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.guiltyPleaTypes;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithDefaultApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType.GUILTY;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType.NOT_GUILTY;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import com.google.common.collect.Lists;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VerdictDelegateTest {
    private static final String PLEA_GUILTY = "GUILTY";
    private static final String PLEA_NOT_GUILTY = "NOT_GUILTY";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void updateVerdict_shouldUpdateVerdict() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final VerdictUpsert verdictUpsert = (VerdictUpsert) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList()).get(0);

        assertThat(verdictUpsert, isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearing.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Verdict::getOriginatingHearingId, is(verdict.getFirstVerdict().getOriginatingHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getFirstVerdict().getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getId, is(verdict.getFirstVerdict().getVerdictType().getId()))
                                .with(VerdictType::getCategory, is(verdict.getFirstVerdict().getVerdictType().getCategory()))
                                .with(VerdictType::getCategoryType, is(verdict.getFirstVerdict().getVerdictType().getCategoryType()))
                                .with(VerdictType::getDescription, is(verdict.getFirstVerdict().getVerdictType().getDescription()))
                                .with(VerdictType::getSequence, is(verdict.getFirstVerdict().getVerdictType().getSequence())))
                        .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                .with(LesserOrAlternativeOffence::getOffenceCode, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceCode()))
                                .with(LesserOrAlternativeOffence::getOffenceTitle, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitle()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislation()))
                                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(verdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislationWelsh())))
                        .with(Verdict::getJurors, isBean(Jurors.class)
                                .with(Jurors::getNumberOfJurors, is(verdict.getFirstVerdict().getJurors().getNumberOfJurors()))
                                .with(Jurors::getNumberOfSplitJurors, is(verdict.getFirstVerdict().getJurors().getNumberOfSplitJurors()))
                                .with(Jurors::getUnanimous, is(verdict.getFirstVerdict().getJurors().getUnanimous()))
                        )));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenOffenceUnderCourtApplicationVerdictIsGuilty() {

        final UUID offenceId = randomUUID();

        final Hearing hearing = standardInitiateHearingWithDefaultApplicationTemplate(offenceId).getHearing();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getId(),
                offenceId,
                GUILTY));

        final List<Object> events = hearingAggregate.updateVerdict(
                hearing.getId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());

        final VerdictUpsert verdictUpsert = (VerdictUpsert) events.get(0);
        MatcherAssert.assertThat(verdictUpsert, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(verdictUpsert.getHearingId(), CoreMatchers.is(hearing.getId()));
        MatcherAssert.assertThat(verdictUpsert.getVerdict().getOffenceId(), CoreMatchers.is(offenceId));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        MatcherAssert.assertThat(convictionDateAdded, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(convictionDateAdded.getOffenceId(), CoreMatchers.is(offenceId));
        MatcherAssert.assertThat(convictionDateAdded.getConvictionDate(), CoreMatchers.is(verdict.getFirstVerdict().getVerdictDate()));
        MatcherAssert.assertThat(convictionDateAdded.getHearingId(), CoreMatchers.is(hearing.getId()));
        MatcherAssert.assertThat(convictionDateAdded.getCourtApplicationId(), CoreMatchers.is(hearing.getCourtApplications().get(0).getId()));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenCourtApplicationVerdictIsGuilty() {
        final UUID offenceId = randomUUID();
        final Hearing hearing = standardInitiateHearingWithDefaultApplicationTemplate(offenceId).getHearing();

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getId(),
                null,
                GUILTY,
                hearing.getCourtApplications().get(0).getId()));

        final List<Object> events = hearingAggregate.updateVerdict(
                hearing.getId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());

        final VerdictUpsert verdictUpsert = (VerdictUpsert) events.get(0);
        MatcherAssert.assertThat(verdictUpsert, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(verdictUpsert.getHearingId(), CoreMatchers.is(hearing.getId()));
        MatcherAssert.assertThat(verdictUpsert.getVerdict().getOffenceId(), CoreMatchers.is(nullValue()));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        MatcherAssert.assertThat(convictionDateAdded, CoreMatchers.is(notNullValue()));
        MatcherAssert.assertThat(convictionDateAdded.getOffenceId(), CoreMatchers.is(nullValue()));
        MatcherAssert.assertThat(convictionDateAdded.getConvictionDate(), CoreMatchers.is(verdict.getFirstVerdict().getVerdictDate()));
        MatcherAssert.assertThat(convictionDateAdded.getHearingId(), CoreMatchers.is(hearing.getId()));
        MatcherAssert.assertThat(convictionDateAdded.getCourtApplicationId(), CoreMatchers.is(hearing.getCourtApplications().get(0).getId()));
    }

    @Test
    public void updateVerdict_shouldHandleOptionalVerdictValues() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(with(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                GUILTY), v -> h(v)
                .getFirstVerdict().setJurors(null)
                .setLesserOrAlternativeOffence(null)));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final VerdictUpsert verdictUpsert = (VerdictUpsert) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList()).get(0);

        assertThat(verdictUpsert, isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearing.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Verdict::getOriginatingHearingId, is(verdict.getFirstVerdict().getOriginatingHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getFirstVerdict().getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getId, is(verdict.getFirstVerdict().getVerdictType().getId()))
                                .with(VerdictType::getCategory, is(verdict.getFirstVerdict().getVerdictType().getCategory()))
                                .with(VerdictType::getCategoryType, is(verdict.getFirstVerdict().getVerdictType().getCategoryType()))
                                .with(VerdictType::getDescription, is(verdict.getFirstVerdict().getVerdictType().getDescription()))
                                .with(VerdictType::getSequence, is(verdict.getFirstVerdict().getVerdictType().getSequence())))
                        .with(Verdict::getLesserOrAlternativeOffence, is(nullValue()))
                        .with(Verdict::getJurors, is(nullValue()))));
    }

    @Test
    public void updateVerdict_whenNoPriorGuiltyPleaOrVerdictSet_AndCurrentCategoryTypeIsGuiltyType_shouldUpdateConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(), GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList()).get(1);

        assertThat(convictionDateAdded, isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getCaseId, is(hearing.getFirstCase().getId()))
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getHearingId, is(hearing.getHearingId()))
                .with(ConvictionDateAdded::getConvictionDate, is(verdict.getFirstVerdict().getVerdictDate()))
        );
    }

    @Test
    public void updateVerdict_WhenGuiltyVerdictIsSet_AndCurrentVerdictCategoryTypeIsNotGuiltyType_shouldClearConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());

        final UUID hearingId = hearing.getHearingId();
        final CommandHelpers.UpdateVerdictCommandHelper guiltyVerdict = h(updateVerdictTemplate(
                hearingId,
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                GUILTY));

        final CommandHelpers.UpdateVerdictCommandHelper notGuiltyVerdict = h(updateVerdictTemplate(
                hearingId,
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) hearingAggregate.updateVerdict(hearingId, guiltyVerdict.getFirstVerdict(), guiltyPleaTypes())
                .collect(Collectors.toList()).get(1);
        hearingAggregate.apply(convictionDateAdded);

        // updated to not guilty verdict set after setting a guilty verdict
        final ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) hearingAggregate.updateVerdict(
                hearingId,
                notGuiltyVerdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList()).get(1);

        assertThat(convictionDateRemoved, isBean(ConvictionDateRemoved.class)
                .with(ConvictionDateRemoved::getCaseId, is(hearing.getFirstCase().getId()))
                .with(ConvictionDateRemoved::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateRemoved::getHearingId, is(hearingId))
        );
    }

    @Test
    public void updateVerdict_WhenGuiltyPleaIsSet_AndCurrentVerdictCategoryTypeIsNotGuiltyType_shouldNotClearConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final UUID offenceId = hearing.getFirstOffenceForFirstDefendantForFirstCase().getId();
        final UUID hearingId = hearing.getHearingId();

        final CommandHelpers.UpdateVerdictCommandHelper notGuiltyVerdict = h(updateVerdictTemplate(
                hearingId,
                offenceId,
                NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final List<Object> events = hearingAggregate.updatePlea(hearingId, getPleaModel(offenceId, PLEA_GUILTY), guiltyPleaTypes())
                .collect(Collectors.toList());
        events.forEach(hearingAggregate::apply);

        // updated to not guilty verdict after setting a guilty plea
        final List<Object> eventsAfterUpdatingVerdict = hearingAggregate.updateVerdict(
                hearingId,
                notGuiltyVerdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());

        assertThat(eventsAfterUpdatingVerdict, hasSize(1));
        assertThat(eventsAfterUpdatingVerdict.get(0), instanceOf(VerdictUpsert.class));
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyTypeAndCurrentCategoryTypeIsGuiltyType_shouldNotUpdateConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final UUID offenceId = hearing.getFirstOffenceForFirstDefendantForFirstCase().getId();

        final CommandHelpers.UpdateVerdictCommandHelper firstGuiltyVerdict = h(updateVerdictTemplate(
                hearingId,
                offenceId,
                GUILTY));
        firstGuiltyVerdict.getFirstVerdict().setVerdictDate(PAST_LOCAL_DATE.next());

        final CommandHelpers.UpdateVerdictCommandHelper secondGuiltyVerdict = h(updateVerdictTemplate(
                hearingId,
                offenceId,
                GUILTY));
        secondGuiltyVerdict.getFirstVerdict().setVerdictDate(LocalDate.now());

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        List<Object> events = hearingAggregate.updateVerdict(
                hearingId,
                firstGuiltyVerdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());

        assertThat(events.size(), is(2));
        assertThat(events.get(0), instanceOf(VerdictUpsert.class));
        assertThat(events.get(1), instanceOf(ConvictionDateAdded.class));
        assertThat(((ConvictionDateAdded) events.get(1)).getConvictionDate(), is(firstGuiltyVerdict.getFirstVerdict().getVerdictDate()));
        events.forEach(hearingAggregate::apply);

        // updated to another guilty verdict after initially setting a guilty verdict
        List<Object> eventsAfterSecondGuiltyVerdictUpdate = hearingAggregate.updateVerdict(
                hearingId,
                secondGuiltyVerdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());
        // should have not conviction added event
        assertThat(eventsAfterSecondGuiltyVerdictUpdate.size(), is(1));
        assertThat(eventsAfterSecondGuiltyVerdictUpdate.get(0), instanceOf(VerdictUpsert.class));

    }

    @Test
    public void updateVerdict_shouldAddConvictionDateWhenVerdictIsGuiltyType_AdjournedHearingWithNotGuiltyPleaSet() {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
        firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(PLEA_NOT_GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(), GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList()).get(1);

        assertThat(convictionDateAdded, isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getCaseId, is(hearing.getFirstCase().getId()))
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getHearingId, is(hearing.getHearingId()))
                .with(ConvictionDateAdded::getConvictionDate, is(verdict.getFirstVerdict().getVerdictDate()))
        );
    }

    @Test
    public void updateVerdict_shouldNotRemoveConvictionDateWhenVerdictIsNotGuiltyType_AdjournedHearingWithGuiltyPleaSet() {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
        firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(PLEA_GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
        firstOffenceForFirstDefendantForFirstCase.setConvictionDate(LocalDate.now());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(), NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final List<Object> events = hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        ).collect(Collectors.toList());

        assertThat(events, hasSize(1));
        assertThat(events.get(0), instanceOf(VerdictUpsert.class));
    }

    @Test
    public void updateVerdict_whenOffenceDoesNotExist_shouldThrowException() {

        exception.expect(RuntimeException.class);
        exception.expectMessage("Offence id is not present");

        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(hearing.getHearingId(), randomUUID(), NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict(),
                guiltyPleaTypes()
        );
    }

    @Test
    public void updateHearingAggregateAfterApplicationOffenceVerdictUpdateEventIsRaised() {
        final UUID offenceId = randomUUID();
        final List<CourtApplication> applications = singletonList(CourtApplication.courtApplication()
                .withId(UUID.randomUUID())
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(randomUUID())
                                .build())
                        .build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withOffences(singletonList(Offence.offence().withId(offenceId).build()))
                        .withCaseStatus("ACTIVE")
                        .build()))
                .build());
        CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingWithApplicationTemplate(applications));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                offenceId,
                GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        hearingAggregate.updateVerdict(hearing.getHearingId(), verdict.getFirstVerdict(),guiltyPleaTypes()).collect(Collectors.toList()).get(0);

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearing.it(), LocalDate.now(), LocalDate.now());
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
        final ResultsShared resultsShared = (ResultsShared) hearingAggregate.shareResults(hearing.getHearingId(), courtClerk1, ZonedDateTime.now(), Lists.newArrayList(sharedResultsCommandResultLine), HearingState.SHARED, null)
                .collect(Collectors.toList()).get(0);

        assertEquals("GUILTY", resultsShared.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict().getVerdictType().getCategoryType());
    }

    @Test
    public void updateHearingAggregateAfterApplicationVerdictUpdateEventIsRaised() {
        final UUID offenceId = randomUUID();
        final UUID applicationId = randomUUID();
        final List<CourtApplication> applications = singletonList(CourtApplication.courtApplication()
                .withId(applicationId)
                .withSubject(CourtApplicationParty.courtApplicationParty()
                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                .withMasterDefendantId(randomUUID())
                                .build())
                        .build())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withOffences(singletonList(Offence.offence().withId(offenceId).build()))
                        .withCaseStatus("ACTIVE")
                        .build()))
                .build());
        CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingWithApplicationTemplate(applications));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                null,
                GUILTY,
                applicationId));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        hearingAggregate.updateVerdict(hearing.getHearingId(), verdict.getFirstVerdict(),guiltyPleaTypes()).collect(Collectors.toList()).get(0);

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearing.it(), LocalDate.now(), LocalDate.now());
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
        final ResultsShared resultsShared = (ResultsShared) hearingAggregate.shareResults(hearing.getHearingId(), courtClerk1, ZonedDateTime.now(), Lists.newArrayList(sharedResultsCommandResultLine), HearingState.SHARED, null)
                .collect(Collectors.toList()).get(0);

        assertEquals("GUILTY", resultsShared.getHearing().getCourtApplications().get(0).getVerdict().getVerdictType().getCategoryType());
        assertEquals(null, resultsShared.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict());
    }


    private PleaModel getPleaModel(final UUID offenceId, final String pleaValue) {
        return PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(pleaValue).build())
                .build();
    }

}
