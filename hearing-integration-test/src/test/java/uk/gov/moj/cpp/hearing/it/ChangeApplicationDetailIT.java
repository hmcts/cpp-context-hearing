package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import javax.json.JsonObject;

import org.junit.Test;

public class ChangeApplicationDetailIT extends AbstractIT {
    private final String eventName = "public.progression.events.hearing-extended";
    private final String applicationChangeEventFromProgression = "public.progression.court-application-updated";

    @Test
    public void changeApplicationDetailShouldApplied_When_Progression_Announce() throws Exception {
        //Given Application already Exist
        final ExtendHearingCommand extendHearingCommand = addApplication();
        final CourtApplication existingCourtApplication = extendHearingCommand.getCourtApplication();

        //when progression announced application change
        final CourtApplication applicationChangeRequest = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate())).getHearing().getCourtApplications().get(0);
        applicationChangeRequest.setId(existingCourtApplication.getId());
        UseCases.sendPublicApplicationChangedMessage(applicationChangeRequest);

        //then application should  updated
        Queries.getHearingPollForMatch(extendHearingCommand.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(extendHearingCommand.getHearingId()))
                        .withValue(h -> h.getCourtApplications().size(), 2)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, applicationChangeRequest.getId())
                                .withValue(CourtApplication::getApplicationReference, applicationChangeRequest.getApplicationReference())
                        ))
                )
        );

    }

    private ExtendHearingCommand addApplication() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication initialCourtApplication = hearing.getCourtApplications().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
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
        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

        sendMessage(publicEvents.createProducer(),
                eventName,
                commandJson,
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .withValue(h -> h.getCourtApplications().size(), 2)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                                .withValue(CourtApplication::getApplicationReference, extendHearingCommand.getCourtApplication().getApplicationReference())
                        ))
                )
        );

        return extendHearingCommand;
    }
}