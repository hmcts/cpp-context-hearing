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
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

public class CaseDefendantsUpdatedForHearingIT extends AbstractIT {

    private final String hearingResultedCaseUpdatedEvent = "public.progression.hearing-resulted-case-updated";

    @Test
    public void testCaseDefendantsUpdated() throws IOException {
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString());

        sendMessage(getPublicTopicInstance().createProducer(),
                hearingResultedCaseUpdatedEvent,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), hearingResultedCaseUpdatedEvent)
                        .withUserId(randomUUID().toString())
                        .build()
        );
        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(prosecutionCase -> prosecutionCase.getCaseStatus(), "CLOSED")
                                .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)))
                                .withValue(d -> d.getDefendants().get(0).getProceedingsConcluded(), true)))
                )
        );
    }
}



