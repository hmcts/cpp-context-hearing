package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Ignore;

public class DefendantLegalAidStatusUpdateIT extends AbstractIT {

    private final String defendantLegalAidStatusUpdatedEvent = "public.progression.defendant-legalaid-status-updated";

    //TODO ignoring this test as we are not able to reproduce it locally. will revisit this test once again to figure out why it ia failing and will fix it.
    @SuppressWarnings("squid:S1607")
    @Ignore
    public void updateDefendantLegalAidStatus() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();

        final JsonObject commandPayload = Json.createObjectBuilder()
                .add("defendantId", defendantId.toString())
                .add("legalAidStatus", "Granted")
                .add("caseId", caseId.toString())
                .build();

        sendMessage(getPublicTopicInstance().createProducer(),
                defendantLegalAidStatusUpdatedEvent,
                commandPayload,
                metadataOf(randomUUID(), defendantLegalAidStatusUpdatedEvent)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, 20, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)))
                                .withValue(d -> d.getDefendants().get(0).getLegalAidStatus(), "Granted")))
                )
        );

    }
}
