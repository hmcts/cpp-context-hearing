package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateVerdict;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithDefaultApplicationTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdateVerdictCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class VerdictIT extends AbstractIT {
    public static final String PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED = "public.hearing.offence-conviction-date-removed";
    public static final String PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED = "public.hearing.offence-conviction-date-changed";

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryIsGuiltyType_shouldUpdateConvictionDateToVerdictDate() {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne = h(
                initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(null))));

        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener publicEventConvictionDateChangedListener = listenFor(
                PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getHearing().getProsecutionCases().get(0).getId().toString()))
                )))) {

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            TestTemplates.VerdictCategoryType.GUILTY)
            ));

            publicEventConvictionDateChangedListener.waitFor();
        }

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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
                                                                .with(VerdictType::getId, is(updateVerdict.getFirstVerdict().getVerdictType().getId()))
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
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne =
                h(initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next()))));

        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.it().getHearing().getProsecutionCases().get(0).getId().toString())))))) {

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            TestTemplates.VerdictCategoryType.NOT_GUILTY)
            ));

            publicEventOffenceConvictionDateRemovedListener.waitFor();
        }

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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
                                                                .with(VerdictType::getId, is(updateVerdict.getFirstVerdict().getVerdictType().getId()))
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

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();
        final LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), with(standardInitiateHearingTemplate(),
                i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(previousConvictionDate))));

        h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                with(updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY), t -> h(t).getFirstVerdict().setVerdictDate(currentConvictionDate))));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
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
    public void updateVerdictToOffenceUnderCourtApplication() {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final UUID offenceId = randomUUID();

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingWithDefaultApplicationTemplate(offenceId)));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getVerdict, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        )))))))

        );

        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener convictionDateChangedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.convictionDate", notNullValue())
                        ))
                )) {

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            offenceId,
                            TestTemplates.VerdictCategoryType.GUILTY)
            ));
            convictionDateChangedListener.waitFor();
        }

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getConvictionDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategoryType, is("GUILTY"))))
                                        )))))))

        );

        final UpdateVerdictCommandHelper updateVerdictSecond;
        try (EventListener convictionDateChangedListenerForRemove = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString()))
                        ))
                )) {

            updateVerdictSecond = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            offenceId,
                            TestTemplates.VerdictCategoryType.NOT_GUILTY)
            ));
            convictionDateChangedListenerForRemove.waitFor();
        }

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(updateVerdictSecond.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategoryType, is("NOT_GUILTY"))))
                                        )))))))

        );
    }

    @SuppressWarnings("squid:S2699")
    @Test
    public void updateClearVerdictToOffenceUnderCourtApplication(){
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final UUID offenceId = randomUUID();

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(),
                standardInitiateHearingWithDefaultApplicationTemplate(offenceId)));


        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getVerdict, is(nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        )))))))

        );

        final EventListener convictionDateChangedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.convictionDate", notNullValue())
                        ))
                );

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        offenceId,
                        TestTemplates.VerdictCategoryType.GUILTY)
        ));
        convictionDateChangedListener.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getConvictionDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictDate, is(updateVerdict.getFirstVerdict().getVerdictDate()))
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategoryType, is("GUILTY"))))
                                        )))))))

        );

        final EventListener convictionDateChangedListenerForRemove = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString()))
                        ))
                );

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdictSecond = h(UseCases.updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        offenceId)
        ));
        convictionDateChangedListenerForRemove.waitFor();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getCourtApplicationCases, first(isBean(CourtApplicationCase.class)
                                        .with(CourtApplicationCase::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(updateVerdictSecond.getFirstVerdict().getOffenceId()))
                                                .with(Offence::getVerdict, is(CoreMatchers.nullValue()))
                                                .with(Offence::getConvictionDate, is(nullValue()))
                                        )))))))

        );
    }

    @Test
    public void shouldUpdateCourtApplicationWithVerdict() {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne = h(
                initiateHearing(getRequestSpec(), standardInitiateHearingWithApplicationTemplate(Collections.singletonList((new HearingFactory()).courtApplication().build()))));

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .with(CourtApplication::getId, is(hearingOne.getCourtApplication().getId()))
                                .with(CourtApplication::getVerdict, is(Matchers.nullValue()))
                                .with(CourtApplication::getConvictionDate, is(Matchers.nullValue()))
                        ))
                )
        );

        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener convictionDateChangedListener = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                hasNoJsonPath("$.offenceId"),
                                withJsonPath("$.convictionDate", notNullValue())
                        ))
                )) {

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            null,
                            TestTemplates.VerdictCategoryType.GUILTY,
                            hearingOne.getCourtApplication().getId()
                    )
            ));
            convictionDateChangedListener.waitFor();
        }

        Verdict applicationVerdict = updateVerdict.getFirstVerdict();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                        .with(CourtApplication::getConvictionDate, is(Matchers.notNullValue()))
                                        .with(CourtApplication::getVerdict, isBean(Verdict.class)
                                                .with(Verdict::getApplicationId, is(applicationVerdict.getApplicationId()))
                                                .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                        .with(VerdictType::getCategory, is(applicationVerdict.getVerdictType().getCategory()))
                                                        .with(VerdictType::getCategoryType, is(applicationVerdict.getVerdictType().getCategoryType()))
                                                        .with(VerdictType::getId, is(applicationVerdict.getVerdictType().getId()))
                                                        .with(VerdictType::getDescription, is(applicationVerdict.getVerdictType().getDescription()))
                                                        .with(VerdictType::getSequence, is(applicationVerdict.getVerdictType().getSequence()))
                                                )
                                                .with(Verdict::getJurors, isBean(Jurors.class)
                                                        .with(Jurors::getNumberOfJurors, is(applicationVerdict.getJurors().getNumberOfJurors()))
                                                        .with(Jurors::getNumberOfSplitJurors, is(applicationVerdict.getJurors().getNumberOfSplitJurors()))
                                                        .with(Jurors::getUnanimous, is(applicationVerdict.getJurors().getUnanimous()))
                                                )
                                                .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                                        .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                                        .with(LesserOrAlternativeOffence::getOffenceCode, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceCode()))
                                                        .with(LesserOrAlternativeOffence::getOffenceTitle, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceTitle()))
                                                        .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                                        .with(LesserOrAlternativeOffence::getOffenceLegislation, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceLegislation()))
                                                        .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceLegislationWelsh()))
                                                )
                                                .with(Verdict::getVerdictDate, is(applicationVerdict.getVerdictDate())))
                                )
                        )
                )
        );

        final UpdateVerdictCommandHelper updateVerdictForRemove;
        try (EventListener convictionDateChangedListenerForRemove = listenFor(PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                                withJsonPath("$.courtApplicationId", is(hearingOne.getCourtApplication().getId().toString())),
                                hasNoJsonPath("$.offenceId"),
                                hasNoJsonPath("$.convictionDate")
                        ))
                )) {

            updateVerdictForRemove = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            null,
                            TestTemplates.VerdictCategoryType.NOT_GUILTY,
                            hearingOne.getCourtApplication().getId()
                    )
            ));
            convictionDateChangedListenerForRemove.waitFor();
        }

        applicationVerdict = updateVerdictForRemove.getFirstVerdict();

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                        .with(CourtApplication::getConvictionDate, is(Matchers.nullValue()))
                                        .with(CourtApplication::getVerdict, isBean(Verdict.class)
                                                .with(Verdict::getApplicationId, is(applicationVerdict.getApplicationId()))
                                                .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                        .with(VerdictType::getCategory, is(applicationVerdict.getVerdictType().getCategory()))
                                                        .with(VerdictType::getCategoryType, is(applicationVerdict.getVerdictType().getCategoryType()))
                                                        .with(VerdictType::getId, is(applicationVerdict.getVerdictType().getId()))
                                                        .with(VerdictType::getDescription, is(applicationVerdict.getVerdictType().getDescription()))
                                                        .with(VerdictType::getSequence, is(applicationVerdict.getVerdictType().getSequence()))
                                                )
                                                .with(Verdict::getJurors, isBean(Jurors.class)
                                                        .with(Jurors::getNumberOfJurors, is(applicationVerdict.getJurors().getNumberOfJurors()))
                                                        .with(Jurors::getNumberOfSplitJurors, is(applicationVerdict.getJurors().getNumberOfSplitJurors()))
                                                        .with(Jurors::getUnanimous, is(applicationVerdict.getJurors().getUnanimous()))
                                                )
                                                .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                                        .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                                        .with(LesserOrAlternativeOffence::getOffenceCode, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceCode()))
                                                        .with(LesserOrAlternativeOffence::getOffenceTitle, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceTitle()))
                                                        .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                                        .with(LesserOrAlternativeOffence::getOffenceLegislation, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceLegislation()))
                                                        .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(applicationVerdict.getLesserOrAlternativeOffence().getOffenceLegislationWelsh()))
                                                )
                                                .with(Verdict::getVerdictDate, is(applicationVerdict.getVerdictDate())))
                                )
                        )
                )
        );
    }

    @Test
    public void updateVerdict_shouldInheritAndAddConvictionDate_whenPreviousIsNotGuiltyAndCurrentIsGuilty() {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne = h(
                initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(null))));

        final InitiateHearingCommandHelper hearingTwo;
        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener publicEventConvictionDateChangedListener = listenFor(
                PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_CHANGED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getHearing().getProsecutionCases().get(0).getId().toString()))
                )))) {

            // Create another hearing for same case
            hearingTwo = h(
                    initiateHearing(getRequestSpec(),
                            with(standardInitiateHearingTemplate(), i -> h(i).getHearing().setProsecutionCases(hearingOne.getHearing().getProsecutionCases()))));

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            TestTemplates.VerdictCategoryType.GUILTY)
            ));

            publicEventConvictionDateChangedListener.waitFor();
        }

        getHearingPollForMatch(hearingTwo.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingTwo.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategory, is(updateVerdict.getFirstVerdictCategory()))
                                                                .with(VerdictType::getCategoryType, is(updateVerdict.getFirstVerdictCategoryType().name()))
                                                                .with(VerdictType::getId, is(updateVerdict.getFirstVerdict().getVerdictType().getId()))
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
    public void updateVerdict_shouldInheritAndRemoveConvictionDate_whenPreviousIsGuiltyAndCurrentIsNotGuilty() {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final InitiateHearingCommandHelper hearingOne =
                h(initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(PAST_LOCAL_DATE.next()))));

        final InitiateHearingCommandHelper hearingTwo;
        final UpdateVerdictCommandHelper updateVerdict;
        try (EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                PUBLIC_EVENT_OFFENCE_CONVICTION_DATE_REMOVED)
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId().toString())),
                        withJsonPath("$.caseId", is(hearingOne.it().getHearing().getProsecutionCases().get(0).getId().toString())))))) {

            // Create another hearing for same case
            hearingTwo = h(
                    initiateHearing(getRequestSpec(),
                            with(standardInitiateHearingTemplate(), i -> h(i).getHearing().setProsecutionCases(hearingOne.getHearing().getProsecutionCases()))));

            updateVerdict = h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                    updateVerdictTemplate(
                            hearingOne.getHearingId(),
                            hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                            TestTemplates.VerdictCategoryType.NOT_GUILTY)
            ));

            publicEventOffenceConvictionDateRemovedListener.waitFor();
        }

        getHearingPollForMatch(hearingTwo.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingTwo.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getVerdict, isBean(Verdict.class)
                                                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                                                .with(VerdictType::getCategory, is(updateVerdict.getFirstVerdictCategory()))
                                                                .with(VerdictType::getCategoryType, is(updateVerdict.getFirstVerdictCategoryType().name()))
                                                                .with(VerdictType::getId, is(updateVerdict.getFirstVerdict().getVerdictType().getId()))
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
    public void updateVerdict_shouldInheritAndNotUpdateConvictionDate_whenPreviousIsGuiltyAndCurrentIsGuilty() {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();
        final LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), with(standardInitiateHearingTemplate(),
                i -> h(i).getFirstOffenceForFirstDefendantForFirstCase().setConvictionDate(previousConvictionDate))));

        final Offence offence = hearingOne.getFirstOffenceForFirstDefendantForFirstCase();

        final InitiateHearingCommandHelper hearingTwo = h(
                initiateHearing(getRequestSpec(),
                        with(standardInitiateHearingTemplate(), i -> h(i).getFirstOffenceForFirstDefendantForFirstCase()
                                .setId(offence.getId()).setConvictionDate(previousConvictionDate))));

        h(updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                with(updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY), t -> h(t).getFirstVerdict().setVerdictDate(currentConvictionDate))));

        getHearingPollForMatch(hearingTwo.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingTwo.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingTwo.getFirstOffenceForFirstDefendantForFirstCase().getId()))
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

}
