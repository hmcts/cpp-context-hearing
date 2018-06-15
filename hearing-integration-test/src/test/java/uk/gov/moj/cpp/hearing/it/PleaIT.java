package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.UpdatePleaCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;
import uk.gov.moj.cpp.hearing.test.TestTemplates.PleaValueType;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdatePleaCommandTemplates.updatePleaTemplate;

@SuppressWarnings("unchecked")
public class PleaIT extends AbstractIT {

    @Test
    public void updatePlea_toGuilty_shouldHaveConvictionDate() throws Throwable {

        InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final String hearingDetailsQueryURL = getURL("hearing.get.hearing.v2", hearingOne.getHearingId());

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value")
                        )));


        final EventListener publicEventOffenceConvictionDateChangedListener = listenFor("public.hearing.offence-conviction-date-changed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString())))));

        final UpdatePleaCommandHelper pleaOne = new UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceIdForFirstDefendant(),
                        updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), PleaValueType.GUILTY).build())
        );

        publicEventOffenceConvictionDateChangedListener.waitFor();

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(pleaOne.getFirstPleaValue()))
                        )));
    }

    @Test
    public void updatePlea_toNotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        poll(requestParameters(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea"),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate"),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].plea.value")
                        )));

        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor("public.hearing.offence-conviction-date-removed")
                .withFilter(isJson(allOf(
                        withJsonPath("$.offenceId", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                        withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString())))));

        final UpdatePleaCommandHelper pleaOne = new UpdatePleaCommandHelper(
                UseCases.updatePlea(requestSpec, hearingOne.getHearingId(), hearingOne.getFirstOffenceIdForFirstDefendant(),
                        updatePleaTemplate(hearingOne.getFirstOffenceIdForFirstDefendant(), PleaValueType.NOT_GUILTY).build())
        );

        publicEventOffenceConvictionDateRemovedListener.waitFor();

        poll(requestParameters(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json"))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                hasNoJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", equalDate(pleaOne.getFirstPleaDate())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", equalEnum(pleaOne.getFirstPleaValue()))
                        )));
    }
}