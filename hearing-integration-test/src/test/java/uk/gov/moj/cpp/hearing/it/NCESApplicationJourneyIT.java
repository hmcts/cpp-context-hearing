package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.util.UUID;

import org.junit.jupiter.api.Test;


public class NCESApplicationJourneyIT extends AbstractIT {
    private static final String APPEAL_AGAINST_CONVICTION_ID = "MC80802";

    @Test
    public void shouldRaisePublicEventWhenApplicationTypeIsAppeal() {

        final HearingFactory hearingFactory = new HearingFactory();
        final UUID masterDefendantId = randomUUID();

        final CourtApplication courtApplication = hearingFactory.courtApplicationForAppeal(
                hearingFactory.courtApplicationDefendant(masterDefendantId, randomUUID()).build(), 
                APPEAL_AGAINST_CONVICTION_ID).build();
        final UUID caseId = courtApplication.getCourtApplicationCases().get(0).getProsecutionCaseId();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.nces-email-notification-for-application")
                .withFilter(isJson(allOf(
                        withJsonPath("$.applicationType", is("APPEAL")),
                        withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                        withJsonPath("$.listingDate", notNullValue()),
                        withJsonPath("$.caseUrns[0]", is("prosecutionAuthorityReference")),
                        withJsonPath("$.caseIds[0]", is(caseId.toString())),
                        withJsonPath("$.hearingCourtCentreName", notNullValue()),
                        withJsonPath("$.hearingCourtCentreId", notNullValue())
                )));

        h(initiateHearing(getRequestSpec(),
                standardInitiateHearingWithApplicationTemplate(singletonList(courtApplication))));

        publicEventTopic.waitFor();
    }
}
