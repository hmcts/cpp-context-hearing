package uk.gov.moj.cpp.hearing.it;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimalInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class InitiateHearingIT extends AbstractIT {

    @Test
    public void initiateHearing_withOnlyMandatoryFields() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, minimalInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.it().getHearing();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()),
                "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.hearingType", equalStr(hearing, "type")),
                                withJsonPath("$.courtCentreName", equalStr(hearing, "courtCentreName")),
                                withJsonPath("$.roomName", equalStr(hearing, "courtRoomName")),
                                withJsonPath("$.roomId", equalStr(hearing, "courtRoomId")),
                                withJsonPath("$.courtCentreId", equalStr(hearing, "courtCentreId")),
                                withJsonPath("$.hearingDays[0]", equalStr(hearing, "hearingDays[0]", ISO_INSTANT)),
                                withJsonPath("$.hearingDays[1]", equalStr(hearing, "hearingDays[1]", ISO_INSTANT)),
                                withJsonPath("$.judge.id", equalStr(hearing, "judge.id")),
                                withJsonPath("$.judge.title", equalStr(hearing, "judge.title")),
                                withJsonPath("$.judge.firstName", equalStr(hearing, "judge.firstName")),
                                withJsonPath("$.judge.lastName", equalStr(hearing, "judge.lastName")),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(hearingOne.getFirstCaseUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(hearing, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString()))
                        )));

        poll(requestParams(getURL("hearing.get.hearings-by-date", hearing.getHearingDays().get(0).withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString())
                , "application/vnd.hearing.get.hearings-by-date+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath(JsonPath.compile("$.hearings[?].hearingId", filter(
                                        where("hearingId").is(hearingOne.getHearingId().toString())
                                )), hasItem(hearingOne.getHearingId().toString()))
                        )));


    }

    @Test
    public void initiateHearing_shouldInitiateHearing_whenInitiateHearingCommandIsMade() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.it().getHearing();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()),
                "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.hearingType", equalStr(hearing, "type")),
                                withJsonPath("$.courtCentreName", equalStr(hearing, "courtCentreName")),
                                withJsonPath("$.roomName", equalStr(hearing, "courtRoomName")),
                                withJsonPath("$.roomId", equalStr(hearing, "courtRoomId")),
                                withJsonPath("$.courtCentreId", equalStr(hearing, "courtCentreId")),
                                withJsonPath("$.judge.id", equalStr(hearing, "judge.id")),
                                withJsonPath("$.judge.title", equalStr(hearing, "judge.title")),
                                withJsonPath("$.judge.firstName", equalStr(hearing, "judge.firstName")),
                                withJsonPath("$.judge.lastName", equalStr(hearing, "judge.lastName")),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(hearingOne.getFirstCaseUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", equalStr(hearing, "defendants[0].id")),
                                withJsonPath("$.cases[0].defendants[0].firstName", equalStr(hearing, "defendants[0].firstName")),
                                withJsonPath("$.cases[0].defendants[0].lastName", equalStr(hearing, "defendants[0].lastName")),
                                withJsonPath("$.cases[0].defendants[0].address.formattedAddress", equalStr(hearing, ' ', "defendants[0].address.address1", "defendants[0].address.address2", "defendants[0].address.address3", "defendants[0].address.address4", "defendants[0].address.postCode")),
                                withJsonPath("$.cases[0].defendants[0].address.address1", equalStr(hearing, "defendants[0].address.address1")),
                                withJsonPath("$.cases[0].defendants[0].address.address2", equalStr(hearing, "defendants[0].address.address2")),
                                withJsonPath("$.cases[0].defendants[0].address.address3", equalStr(hearing, "defendants[0].address.address3")),
                                withJsonPath("$.cases[0].defendants[0].address.address4", equalStr(hearing, "defendants[0].address.address4")),
                                withJsonPath("$.cases[0].defendants[0].address.postCode", equalStr(hearing, "defendants[0].address.postCode")),
                                withJsonPath("$.cases[0].defendants[0].dateOfBirth", equalStr(hearing, "defendants[0].dateOfBirth", ISO_LOCAL_DATE)),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", equalStr(hearing, "defendants[0].offences[0].id")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].count", equalInt(hearing, "defendants[0].offences[0].count")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].wording", equalStr(hearing, "defendants[0].offences[0].wording")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].title", equalStr(hearing, "defendants[0].offences[0].title")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].legislation", equalStr(hearing, "defendants[0].offences[0].legislation")),
                                withJsonPath("$.cases[0].defendants[0].bailStatus", is(hearing.getDefendants().get(0).getDefendantCases().get(0).getBailStatus()))
                        )));
    }

    @Test
    public void initiateHearing_withAPreviousPlea_Guilty_shouldHaveConvictionDate() throws Throwable {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final UpdatePleaCommandHelper pleaOne = h(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceIdForFirstDefendant(),
                        updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), TestTemplates.PleaValueType.GUILTY).build())
        );

        final InitiateHearingCommandHelper hearingTwo = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                    InitiateHearingCommandHelper h = h(i);

                    h.getFirstCase().setCaseId(hearingOne.getFirstCaseId());
                    h.getFirstDefendant().setId(hearingOne.getFirstDefendantId());
                    h.getFirstOffenceForFirstDefendant().setId(hearingOne.getFirstOffenceIdForFirstDefendant());
                    h.getFirstOffenceForFirstDefendant().setCaseId(hearingOne.getFirstCaseId());
                    h.getFirstDefendantCaseForFirstDefendant().setCaseId(hearingOne.getFirstCaseId());
                }))
        );

        poll(requestParameters(getURL("hearing.get.hearing", hearingTwo.getHearingId()), "application/vnd.hearing.get.hearing+json"))
                .timeout(10, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingTwo.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingTwo.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingTwo.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingTwo.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(pleaOne.getFirstPleaValue()))
                        )));
    }

    @Test
    public void initiateHearing_withAPreviousPlea_NotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate())
        );

        final UpdatePleaCommandHelper pleaOne = new UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceIdForFirstDefendant(),
                        updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), TestTemplates.PleaValueType.NOT_GUILTY).build())
        );

        final InitiateHearingCommandHelper hearingTwo = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                    InitiateHearingCommandHelper h = h(i);

                    h.getFirstCase().setCaseId(hearingOne.getFirstCaseId());
                    h.getFirstDefendant().setId(hearingOne.getFirstDefendantId());
                    h.getFirstOffenceForFirstDefendant().setId(hearingOne.getFirstOffenceIdForFirstDefendant());
                    h.getFirstOffenceForFirstDefendant().setCaseId(hearingOne.getFirstCaseId());
                    h.getFirstDefendantCaseForFirstDefendant().setCaseId(hearingOne.getFirstCaseId());
                }))
        );

        poll(requestParameters(getURL("hearing.get.hearing", hearingTwo.getHearingId()), "application/vnd.hearing.get.hearing+json"))
                .timeout(10, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingTwo.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingTwo.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingTwo.getFirstDefendantId().toString())),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingTwo.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(pleaOne.getFirstPleaValue()))
                        )));
    }
}