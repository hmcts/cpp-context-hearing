package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import org.junit.Test;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import javax.json.JsonObject;
import java.util.UUID;

@SuppressWarnings({"squid:S2699"})
public class AddMasterDefendantIdToDefendantIT extends AbstractIT {

    @Test
    public void shouldAddMasterDefendantIdToDefendantAsSystemUser() {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID hearingId = hearingOne.getHearingId();
        final UUID prosecutionCaseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID masterDefendantId = randomUUID();

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                        ));

        addMasterDefendantIdToDefendantAndVerifyEventHasBeenCreated(hearingId, prosecutionCaseId, defendantId, masterDefendantId);

    }


    private void addMasterDefendantIdToDefendantAndVerifyEventHasBeenCreated(final UUID hearingId, final UUID prosecutionCaseId, final UUID defendantId, final UUID masterDefendantId) {

        try (final EventListener masterDefendantIdAddedEvent = listenFor("hearing.events.master-defendant-id-added", "hearing.event")
                .withFilter(convertStringTo(MasterDefendantIdAdded.class, isBean(MasterDefendantIdAdded.class)
                        .with(MasterDefendantIdAdded::getHearingId, is(hearingId))
                        .with(MasterDefendantIdAdded::getProsecutionCaseId, is(prosecutionCaseId))
                        .with(MasterDefendantIdAdded::getDefendantId, is(defendantId))
                        .with(MasterDefendantIdAdded::getMasterDefendantId, is(masterDefendantId))
                ))) {

            final JsonObject payload = createObjectBuilder()
                    .add("prosecutionCaseId", prosecutionCaseId.toString())
                    .add("defendantId", defendantId.toString())
                    .add("masterDefendantId", masterDefendantId.toString())
                    .build();
            makeCommand(getRequestSpec(), "hearing.master-defendant-id")
                    .ofType("application/vnd.hearing.master-defendant-id+json")
                    .withArgs(hearingId)
                    .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                    .withPayload(payload.toString())
                    .executeSuccessfully();

            masterDefendantIdAddedEvent.waitFor();

        }
    }

}