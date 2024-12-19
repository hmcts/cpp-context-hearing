package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.Boolean.FALSE;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.rescheduleHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationalUnit;

import uk.gov.moj.cpp.hearing.command.HearingVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

@NotThreadSafe
public class ClearVacatedTrialIT extends AbstractIT {
    private final String OUCODE = "A46AF00";

    @Test
    public void shouldRescheduleHearing() throws Exception {
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID courCentreId = hearingOne.getCourtCentre().getId();
        final TrialType addTrialType = TrialType.builder()
                .withVacatedTrialReasonId(VACATED_TRIAL_TYPE_ID)
                .build();

        stubOrganisationalUnit(courCentreId, OUCODE);

        final UUID hearingId = hearingOne.getHearingId();
        try (EventListener publicEventTopic = listenFor("public.hearing.trial-vacated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {
            setTrialType(getRequestSpec(), hearingId, addTrialType, true);
            publicEventTopic.waitFor();
        }

        rescheduleHearing(new HearingVacatedTrialCleared(hearingId));

        pollForHearing(hearingId.toString(),
                withJsonPath("$.hearing.id", is(hearingId.toString())),
                withJsonPath("$.hearing.isVacatedTrial", is(FALSE))
        );

    }
}
