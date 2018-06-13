package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class VerdictIT extends AbstractIT {

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryIsGuiltyType_shouldUpdateConvictionDateToVerdictDate() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
                }).build())
        );

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString())))));

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                updateVerdictTemplate(hearingOne.getFirstCaseId(),
                        hearingOne.getFirstDefendantId(),
                        hearingOne.getFirstOffenceIdForFirstDefendant(),
                        TestTemplates.VerdictCategoryType.GUILTY
                ).build());

        publicEventConvictionDateChangedListener.waitFor();

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId", is(verdict.getValue().getVerdictTypeId().toString())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", is(verdict.getVerdictDate().toString()))
                        ))
                );
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsNotGuilty_shouldClearConvictionDateToNull() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(PAST_LOCAL_DATE.next());
                }).build())
        );

        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                "public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString()))
                )));

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(),
                updateVerdictTemplate(hearingOne.getFirstCaseId(),
                        hearingOne.getFirstDefendantId(),
                        hearingOne.getFirstOffenceIdForFirstDefendant(),
                        TestTemplates.VerdictCategoryType.NOT_GUILTY
                ).build());

        publicEventOffenceConvictionDateRemovedListener.waitFor();

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                .timeout(30,
                        TimeUnit.SECONDS)
                .until(status().is(OK), payload().isJson(allOf(
                        withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),

                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId", is(verdict.getValue().getVerdictTypeId().toString())),

                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                        withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),
                        withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate")
                        ))
                );

    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsGuilty_shouldNotUpdateConvictionDate()
            throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();
        LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(previousConvictionDate);
                }).build())
        );

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(), with(
                updateVerdictTemplate(hearingOne.getFirstCaseId(),
                        hearingOne.getFirstDefendantId(),
                        hearingOne.getFirstOffenceIdForFirstDefendant(),
                        TestTemplates.VerdictCategoryType.GUILTY
                ), verdict -> {
                    verdict.getDefendants().get(0).getOffences().get(0).getVerdict().withVerdictDate(currentConvictionDate);
                }).build());

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType", is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId", is(verdict.getValue().getVerdictTypeId().toString())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", is(previousConvictionDate.toString())))));
    }

    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryTypeIsNotGuiltyType_shouldNotUpdateOrClearTheConvictionDate() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
                }).build())
        );

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = UseCases.updateVerdict(requestSpec, hearingOne.getHearingId(), with(
                updateVerdictTemplate(hearingOne.getFirstCaseId(),
                        hearingOne.getFirstDefendantId(),
                        hearingOne.getFirstOffenceIdForFirstDefendant(),
                        TestTemplates.VerdictCategoryType.NO_VERDICT
                ), verdict -> {
                    verdict.getDefendants().get(0).getOffences().get(0).getVerdict().withVerdictDate(PAST_LOCAL_DATE.next());
                }).build());

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType", is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId", is(verdict.getValue().getVerdictTypeId().toString())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),

                                withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate")
                        ))
                );
    }
}
