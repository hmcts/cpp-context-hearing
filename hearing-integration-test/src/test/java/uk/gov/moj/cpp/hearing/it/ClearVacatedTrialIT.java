package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.Boolean.FALSE;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.HearingVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ClearVacatedTrialIT extends AbstractIT {
    private static final UUID VACATE_REASON_ID = randomUUID();
    private static final String MEDIA_TYPE = "application/vnd.hearing.get.hearing+json";
    private static final String EVENT = "hearing.get.hearing";

    @Test
    public void shouldRescheduleHearing() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = buildVacatedTrialTypes(VACATE_REASON_ID);
        ReferenceDataStub.stubCrackedIOnEffectiveTrialTypes(crackedIneffectiveVacatedTrialTypes);

        final TrialType addTrialType = TrialType.builder()
                .withVacatedTrialReasonId(VACATE_REASON_ID)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType,true);
        UseCases.rescheduleHearing(new HearingVacatedTrialCleared(hearingOne.getHearingId()));

        poll(requestParams(getURL(EVENT, hearingOne.getHearingId()), MEDIA_TYPE)
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.id", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.hearing.isVacatedTrial", is(FALSE))
                        )));

    }

    private List<CrackedIneffectiveVacatedTrialType> buildVacatedTrialTypes(final UUID vacateReasonId) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(vacateReasonId, "code", "Vacated", null));

        return trialList;
    }
}
