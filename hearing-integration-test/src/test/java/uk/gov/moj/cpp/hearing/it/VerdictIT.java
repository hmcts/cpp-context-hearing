package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.customStructureInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Test;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Jurors;
import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.json.schemas.core.VerdictType;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;

@SuppressWarnings("unchecked")
public class VerdictIT extends AbstractIT {

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryIsGuiltyType_shouldUpdateConvictionDateToVerdictDate() {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(
                UseCases.initiateHearing(requestSpec,
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(null))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getHearing().getProsecutionCases().get(0).getId().toString()))
                )));

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY)
        ));

        publicEventConvictionDateChangedListener.waitFor();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategory, is(updateVerdict.getFirstVerdictCategory()))
                                                                .with(VerdictType::getCategoryType, is(updateVerdict.getFirstVerdictCategoryType().name()))
                                                                .with(VerdictType::getVerdictTypeId, is(updateVerdict.getFirstVerdict().getVerdictType().getVerdictTypeId()))
                                                                .with(VerdictType::getDescription, is(updateVerdict.getFirstVerdict().getVerdictType().getDescription()))
                                                                .with(VerdictType::getSequence, is(updateVerdict.getFirstVerdict().getVerdictType().getSequence()))
                                                        )
                                                        .with(Verdict::getOffenceId, is(updateVerdict.getFirstVerdict().getOffenceId()))
                                                        .with(Verdict::getJurors, isBean(Jurors.class)
                                                                .with(Jurors::getNumberOfJurors, is(updateVerdict.getFirstVerdict().getJurors().getNumberOfJurors()))
                                                                .with(Jurors::getNumberOfSplitJurors, is(updateVerdict.getFirstVerdict().getJurors().getNumberOfSplitJurors()))
                                                                .with(Jurors::getUnanimous, is(updateVerdict.getFirstVerdict().getJurors().getUnanimous()))
                                                        )
                                                        .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                                                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                                                .with(LesserOrAlternativeOffence::getOffenceCode, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceCode()))
                                                                .with(LesserOrAlternativeOffence::getOffenceTitle, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitle()))
                                                                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                                                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislation()))
                                                                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislationWelsh()))
                                                        )
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                )
                                                .with(Offence::getConvictionDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsNotGuilty_shouldClearConvictionDateToNull() {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final CommandHelpers.InitiateHearingCommandHelper hearingOne =
                h(UseCases.initiateHearing(requestSpec,
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next()))));

        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                "public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.it().getHearing().getProsecutionCases().get(0).getId().toString())))));

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.NOT_GUILTY)
        ));

        publicEventOffenceConvictionDateRemovedListener.waitFor();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategory, is(updateVerdict.getFirstVerdictCategory()))
                                                                .with(VerdictType::getCategoryType, is(updateVerdict.getFirstVerdictCategoryType().name()))
                                                                .with(VerdictType::getVerdictTypeId, is(updateVerdict.getFirstVerdict().getVerdictType().getVerdictTypeId()))
                                                                .with(VerdictType::getDescription, is(updateVerdict.getFirstVerdict().getVerdictType().getDescription()))
                                                                .with(VerdictType::getSequence, is(updateVerdict.getFirstVerdict().getVerdictType().getSequence()))
                                                        )
                                                        .with(Verdict::getOffenceId, is(updateVerdict.getFirstVerdict().getOffenceId()))
                                                        .with(Verdict::getJurors, isBean(Jurors.class)
                                                                .with(Jurors::getNumberOfJurors, is(updateVerdict.getFirstVerdict().getJurors().getNumberOfJurors()))
                                                                .with(Jurors::getNumberOfSplitJurors, is(updateVerdict.getFirstVerdict().getJurors().getNumberOfSplitJurors()))
                                                                .with(Jurors::getUnanimous, is(updateVerdict.getFirstVerdict().getJurors().getUnanimous()))
                                                        )
                                                        .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                                                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                                                .with(LesserOrAlternativeOffence::getOffenceCode, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceCode()))
                                                                .with(LesserOrAlternativeOffence::getOffenceTitle, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitle()))
                                                                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                                                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislation()))
                                                                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(updateVerdict.getFirstVerdict().getLesserOrAlternativeOffence().getOffenceLegislationWelsh()))
                                                        )
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                )
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsGuilty_shouldNotUpdateConvictionDate() {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();
        final LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(),
                i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(previousConvictionDate))));

        h(UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                with(updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY), t -> h(t).getFirstVerdict().setVerdictDate(currentConvictionDate))));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(currentConvictionDate))
                                                )
                                                .with(Offence::getConvictionDate, is(previousConvictionDate))
                                        ))
                                ))
                        ))
                )
        );
    }

    @Test
    public void shouldInheritVerdict() {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();

        final LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(),
                i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(previousConvictionDate))));

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                with(updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY), t -> h(t).getFirstVerdict().setVerdictDate(currentConvictionDate))));

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getOriginatingHearingId, is(hearingOne.getHearingId())))))))))));

        final CommandHelpers.InitiateHearingCommandHelper hearingTwo = h(UseCases.initiateHearing(requestSpec, customStructureInitiateHearingTemplate(
                toMap(hearingOne.getFirstCase().getId(), toMap(hearingOne.getFirstDefendantForFirstCase().getId(), asList(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())))
        )));

        Queries.getHearingPollForMatch(hearingTwo.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getOriginatingHearingId, is(hearingOne.getHearingId())))
                                        ))))))));
    }
}
