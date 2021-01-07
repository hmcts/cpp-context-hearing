package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

public class ExtendHearingIT extends AbstractIT {

    final String eventName = "public.progression.events.hearing-extended";

    @Test
    public void insertCourtApplication() throws Exception {
        extend(true);
    }

    @Test
    public void amendCourtApplication() throws Exception {
        extend(false);
    }

    @Test
    public void insertProsecutionCases() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                )
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final UUID caseId = UUID.randomUUID();
        extendHearingCommand.setProsecutionCases(cloneCase(hearing, caseId));

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                eventName,
                commandJson,
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getProsecutionCases().size(), 2)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, caseId)
                        ))
                )
        );

    }

    private List<ProsecutionCase> cloneCase(final Hearing hearing, final UUID caseId){
        final List<ProsecutionCase> prosecutionCases = hearing.getProsecutionCases();

        prosecutionCases.get(0).setId(caseId);
        prosecutionCases.get(0).getDefendants().get(0).setId(UUID.randomUUID());
        prosecutionCases.get(0).getDefendants().get(0).getOffences().get(0).setId(UUID.randomUUID());
        prosecutionCases.get(0).getDefendants().get(0).getOffences().get(0).getReportingRestrictions().get(0).setId(UUID.randomUUID());
        prosecutionCases.get(0).getCaseMarkers().get(0).setId(UUID.randomUUID());
        return prosecutionCases;
    }

    private void extend(boolean insert) throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication initialCourtApplication = hearing.getCourtApplications().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, initialCourtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, initialCourtApplication.getApplicationReference())
                        ))
                )
        );


        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final CourtApplication newCourtApplication = (new HearingFactory()).courtApplication().build();
        if (!insert) {
            newCourtApplication.setId(initialCourtApplication.getId());
        }
        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                eventName,
                commandJson,
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        int expectedApplicationCount = hearing.getCourtApplications().size() + (insert ? 1 : 0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getCourtApplications().size(), expectedApplicationCount)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                                .withValue(CourtApplication::getApplicationReference, extendHearingCommand.getCourtApplication().getApplicationReference())
                        ))
                )
        );


    }

}
