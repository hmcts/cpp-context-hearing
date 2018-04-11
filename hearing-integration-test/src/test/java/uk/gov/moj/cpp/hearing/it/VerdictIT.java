package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

public class VerdictIT extends AbstractIT {

    @Test
    public void updateVerdict_shouldSetConvictionDateToStartOfHearing_givenGuiltyVerdict() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(Defendant.builder()
                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                        .withPersonId(randomUUID())
                        .addOffence(Offence.builder()
                                .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                .withVerdict(Verdict.builder()
                                        .withId(randomUUID())
                                        .withValue(
                                                VerdictValue.builder()
                                                        .withId(randomUUID())
                                                        .withCategory("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription(STRING.next())

                                        )
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .withVerdictDate(PAST_LOCAL_DATE.next())
                                )
                        )
                )
                .build();

        TestUtilities.EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", Matchers.is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate-hearing")
                .ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictId",
                                        equalStr(hearingUpdateVerdictCommand, "defendants[0].offences[0].verdict.id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", is(initiateHearingCommand.getHearing().getStartDateTime().toLocalDate().toString()))
                        )));
    }

    @Test
    public void updateVerdict_shouldSetConvictionDateToNull_givenNotGuiltyVerdict() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())

                .addDefendant(Defendant.builder()
                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                        .withPersonId(randomUUID())
                        .addOffence(Offence.builder()
                                .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                .withVerdict(Verdict.builder()
                                        .withId(randomUUID())
                                        .withValue(
                                                VerdictValue.builder()
                                                        .withId(randomUUID())
                                                        .withCategory("NOT_GUILTY")
                                                        .withCode("A1")
                                                        .withDescription(STRING.next())

                                        )
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .withVerdictDate(PAST_LOCAL_DATE.next())
                                )
                        )
                )
                .build();


        makeCommand(requestSpec, "hearing.initiate-hearing")
                .ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictId",
                                        equalStr(hearingUpdateVerdictCommand, "defendants[0].offences[0].verdict.id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description", is(verdict.getValue().getDescription())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),
                                withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate")
                        )));

    }
}
