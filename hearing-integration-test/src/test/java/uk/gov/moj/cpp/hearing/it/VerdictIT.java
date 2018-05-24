package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

public class VerdictIT extends AbstractIT {

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryIsGuiltyType_shouldUpdateConvictionDateToVerdictDate() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Guilty, by jury on judge's direction")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing()
                                .getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing()
                                        .getDefendants().get(0).getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventConvictionDateChangedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(verdict.getVerdictDate().toString())))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsNotGuilty_shouldClearConvictionDateToNull() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(PAST_LOCAL_DATE.next());
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())

                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Not Guilty")
                                                        .withCategoryType("NOT_GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Not guilty, by jury on judge's direction")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));
        
        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                "public.hearing.offence-conviction-date-removed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventOffenceConvictionDateRemovedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),
                                withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"))));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsGuiltyAndCurrentCategoryTypeIsGuilty_shouldNotUpdateConvictionDate()
            throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        
        LocalDate previousConvictionDate = PAST_LOCAL_DATE.next();
        LocalDate currentConvictionDate = PAST_LOCAL_DATE.next();

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(previousConvictionDate);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Not guilty but guilty of lesser/alternative offence not charged namely")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(currentConvictionDate))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(previousConvictionDate.toString())))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_whenPreviousCategoryTypeIsNotGuiltyTypeAndCurrentCategoryTypeIsNotGuiltyType_shouldNotUpdateOrClearTheConvictionDate() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("No Verdict")
                                                        .withCategoryType("NO_VERDICT")
                                                        .withCode("A1")
                                                        .withDescription("Guilty by judge alone  (under DVC&V Act 2004)")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"))));
    }
}
