package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType.GUILTY;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VerdictCategoryType.NOT_GUILTY;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VerdictDelegateTest {

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
                verdict.getFirstVerdict()
        ).collect(Collectors.toList()).get(0);

        assertThat(verdictUpsert, isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearing.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Verdict::getOriginatingHearingId, is(verdict.getFirstVerdict().getOriginatingHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getFirstVerdict().getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getVerdictTypeId, is(verdict.getFirstVerdict().getVerdictType().getVerdictTypeId()))
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
                verdict.getFirstVerdict()
        ).collect(Collectors.toList()).get(0);

        assertThat(verdictUpsert, isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearing.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                        .with(Verdict::getOriginatingHearingId, is(verdict.getFirstVerdict().getOriginatingHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getFirstVerdict().getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getVerdictTypeId, is(verdict.getFirstVerdict().getVerdictType().getVerdictTypeId()))
                                .with(VerdictType::getCategory, is(verdict.getFirstVerdict().getVerdictType().getCategory()))
                                .with(VerdictType::getCategoryType, is(verdict.getFirstVerdict().getVerdictType().getCategoryType()))
                                .with(VerdictType::getDescription, is(verdict.getFirstVerdict().getVerdictType().getDescription()))
                                .with(VerdictType::getSequence, is(verdict.getFirstVerdict().getVerdictType().getSequence())))
                        .with(Verdict::getLesserOrAlternativeOffence, is(nullValue()))
                        .with(Verdict::getJurors, is(nullValue()))));
    }

    @Test
    public void updateVerdict_whenConvictionDateIsNull_AndCurrentCategoryTypeIsGuiltyType_shouldUpdateConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing =
                h(with(standardInitiateHearingTemplate(),
                        i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(null)));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(), GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict()
        ).collect(Collectors.toList()).get(1);

        assertThat(convictionDateAdded, isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getCaseId, is(hearing.getFirstCase().getId()))
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getHearingId, is(hearing.getHearingId()))
                .with(ConvictionDateAdded::getConvictionDate, is(verdict.getFirstVerdict().getVerdictDate()))
        );
    }

    @Test
    public void updateVerdict_whenConvictionDateIsNotNull_AndCurrentCategoryTypeIsNotGuiltyType_shouldClearConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing =
                h(with(standardInitiateHearingTemplate(),
                        i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next())));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        final ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict()
        ).collect(Collectors.toList()).get(1);

        assertThat(convictionDateRemoved, isBean(ConvictionDateRemoved.class)
                .with(ConvictionDateRemoved::getCaseId, is(hearing.getFirstCase().getId()))
                .with(ConvictionDateRemoved::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateRemoved::getHearingId, is(hearing.getHearingId()))
        );
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyTypeAndCurrentCategoryTypeIsGuiltyType_shouldNotUpdateConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing =
                h(with(standardInitiateHearingTemplate(),
                        i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next())));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        List<Object> events = hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict()
        ).collect(Collectors.toList());

        assertThat(events.size(), is(1));
        assertThat(events.get(0), instanceOf(VerdictUpsert.class));
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryTypeIsNotGuiltyType_shouldNotUpdateConvictionDate() {

        final CommandHelpers.InitiateHearingCommandHelper hearing =
                h(with(standardInitiateHearingTemplate(),
                        i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(null)));

        final CommandHelpers.UpdateVerdictCommandHelper verdict = h(updateVerdictTemplate(
                hearing.getHearingId(),
                hearing.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                NOT_GUILTY));

        final HearingAggregate hearingAggregate = new HearingAggregate();
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        List<Object> events = hearingAggregate.updateVerdict(
                hearing.getHearingId(),
                verdict.getFirstVerdict()
        ).collect(Collectors.toList());

        assertThat(events.size(), is(1));
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
                verdict.getFirstVerdict()
        );
    }

}