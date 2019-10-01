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

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.hearing.courts.CourtApplicationSummaries;
import uk.gov.justice.hearing.courts.Defendants;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.hearing.courts.ProsecutionCaseSummaries;
import uk.gov.moj.cpp.hearing.command.eject.EjectCaseOrApplicationCommand;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;

public class EjectCaseApplicationIT extends AbstractIT {
    private static final String CASE_STATUS_EJECTED = "EJECTED";
    private final String eventName = "public.progression.events.hearing-extended";
    private final String ejectEventName = "public.progression.events.case-or-application-ejected";
    private final String REMOVAL_REASON = "LEGAL";

    @Test
    public void shouldEjectCaseAndApplication() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, minimumInitiateHearingTemplate()));
        // Hearing initiated with standalone application
        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication standAloneApplication = hearing.getCourtApplications().get(0);
        final UUID standAloneApplicationId = standAloneApplication.getId();

        Queries.getHearingPollForMatch(hearing.getId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, standAloneApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, standAloneApplication.getApplicationReference())
                        ))
                )
        );
        // Add child to standalone so that when parent ejected, child ahousl also be ejected
        addChildApplication(standAloneApplicationId, hearing);
        final UUID prosecutionCaseId = hearingOne.getHearing().getProsecutionCases().stream().findAny().get().getId();
        // Add linked application
        addLinkedApplication(prosecutionCaseId, hearing);
        
        // Eject case will also eject linked application
        ejectCaseWithLinkedApplication(hearing.getId(), prosecutionCaseId, standAloneApplicationId);
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(),
                hearing.getHearingDays().get(0).getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", 30,
                isBean(GetHearings.class)
                        .withValue(h -> h.getHearingSummaries().size(), 1)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                ))
                        ))

        );
        // Eject standalone application
        ejectStandAloneApplication(hearing,hearing.getId(), standAloneApplicationId);
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(),
                hearing.getHearingDays().get(0).getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", 30,
                isBean(GetHearings.class)
                        // all applications ejected now including child and linked
                        .withValue(h -> h.getHearingSummaries().size(), 0)

        );


    }



    private  void addLinkedApplication(final UUID prosecutionCaseId, final Hearing hearing) throws Exception {

        final ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final CourtApplication newCourtApplication = (new HearingFactory()).linkedCourtApplication(prosecutionCaseId).build();
        extendHearingCommand.setCourtApplication(newCourtApplication);

        final JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

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
                        .withValue(h -> h.getCourtApplications().size(), 3)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, extendHearingCommand.getCourtApplication().getId())
                        ))
                )
        );
    }

    private void addChildApplication(final UUID parentApplicationId, final Hearing hearing) throws Exception {

        final ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearing.getId());
        final CourtApplication newCourtApplication = (new HearingFactory()).standAloneChildCourtApplication(parentApplicationId).build();
        extendHearingCommand.setCourtApplication(newCourtApplication);

        final JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(extendHearingCommand);

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
                        ))
                )
        );

    }
    private void ejectCaseWithLinkedApplication(final UUID hearingId, final UUID prosecutionCaseId, final UUID applicationId) throws Exception {
        final EjectCaseOrApplicationCommand command = EjectCaseOrApplicationCommand.EjectCaseOrApplicationCommandBuilder
                .anEjectCaseOrApplicationCommand()
                .withProsecutionCaseId(prosecutionCaseId)
                .withRemovalReason(REMOVAL_REASON)
                .withHearingIds(Collections.singletonList(hearingId))
                .build();
        final JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(command);

        sendMessage(publicEvents.createProducer(),
                ejectEventName,
                commandJson,
                metadataOf(randomUUID(), ejectEventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );
        Queries.getHearingPollForMatch(hearingId, 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .withValue(h -> h.getProsecutionCases().size(), 0)
                        .withValue(h -> h.getCourtApplications().size(), 2)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, applicationId)
                                .withValue(CourtApplication::getApplicationStatus, ApplicationStatus.DRAFT)
                        ))
                )
        );
    }
    private void ejectStandAloneApplication(final Hearing hearing, final UUID hearingId, final UUID standAloneApplicationId) throws Exception {
        final EjectCaseOrApplicationCommand command = EjectCaseOrApplicationCommand.EjectCaseOrApplicationCommandBuilder.anEjectCaseOrApplicationCommand()
                .withApplicationId(standAloneApplicationId)
                .withRemovalReason(REMOVAL_REASON)
                .withHearingIds(Collections.singletonList(hearingId))
                .build();
        final JsonObject commandJson = Utilities.JsonUtil.objectToJsonObject(command);

        sendMessage(publicEvents.createProducer(),
                ejectEventName,
                commandJson,
                metadataOf(randomUUID(), ejectEventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );
        Queries.getHearingPollForMatch(hearingId, 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .withValue(Hearing::getCourtApplications, null)
                )
        );

    }
}
