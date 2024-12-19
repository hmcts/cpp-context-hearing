package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingWithApplicationTemplate;

import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.util.UUID;

import org.junit.jupiter.api.Test;


public class NCESApplicationJourneyIT extends AbstractIT {
    private static final UUID APPEAL_AGAINST_CONVICTION_ID = fromString("57810183-a5c2-3195-8748-c6b97eda1ebd");

    @Test
    public void shouldRaisePublicEventWhenApplicationTypeIsAppeal() {

        final HearingFactory hearingFactory = new HearingFactory();
        final UUID masterDefendantId = randomUUID();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.nces-email-notification-for-application")
                .withFilter(isJson(allOf(
                        withJsonPath("$.applicationType", is("APPEAL")),
                        withJsonPath("$.masterDefendantId", is(masterDefendantId.toString())),
                        withJsonPath("$.listingDate", notNullValue()),
                        withJsonPath("$.caseUrns[0]", is("prosecutionAuthorityReference")),
                        withJsonPath("$.hearingCourtCentreName", notNullValue())
                )));

        h(initiateHearing(getRequestSpec(),
                standardInitiateHearingWithApplicationTemplate(singletonList(
                        hearingFactory.courtApplication(hearingFactory.courtApplicationDefendant(masterDefendantId, randomUUID()).build(), APPEAL_AGAINST_CONVICTION_ID).build()))));

        publicEventTopic.waitFor();
    }
}
