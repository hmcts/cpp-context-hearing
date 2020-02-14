package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtApplicationResponseType;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.application.SaveApplicationResponseCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.UUID;

import org.hamcrest.core.Is;
import org.junit.Test;

public class ApplicationResponseIT extends AbstractIT {

    @Test
    public void saveApplicationResponse() throws Exception {
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

        SaveApplicationResponseCommand saveApplicationResponseCommand = new SaveApplicationResponseCommand();
        saveApplicationResponseCommand.setHearingId(hearing.getId());
        saveApplicationResponseCommand.setApplicationPartyId(hearing.getCourtApplications().get(0).getRespondents().get(0).getPartyDetails().getId());
        saveApplicationResponseCommand.setApplicationResponse(CourtApplicationResponse.courtApplicationResponse()
                .withOriginatingHearingId(hearing.getId())
                .withApplicationId(hearing.getCourtApplications().get(0).getId())
                .withApplicationResponseType(CourtApplicationResponseType.courtApplicationResponseType()
                                            .withDescription("Admitted")
                                            .withId(UUID.randomUUID())
                                            .withSequence(1).build())
                .withApplicationResponseDate(LocalDate.now())
                .build());

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.application-response-saved")
                .withFilter(isJson(withJsonPath("$.courtApplicationResponse.originatingHearingId", Is.is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.save-application-response")
                .withArgs(hearing.getId())
                .ofType("application/vnd.hearing.save-application-response+json")
                .withPayload(saveApplicationResponseCommand)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                        .withValue(CourtApplication::getId, saveApplicationResponseCommand.getApplicationResponse().getApplicationId())
                                        .with(CourtApplication::getRespondents, first(isBean(CourtApplicationRespondent.class)
                                                .with(CourtApplicationRespondent::getApplicationResponse, isBean(CourtApplicationResponse.class)
                                                        .with(CourtApplicationResponse::getOriginatingHearingId, is(hearing.getId()))
                                                        .withValue(CourtApplicationResponse::getApplicationId, saveApplicationResponseCommand.getApplicationResponse().getApplicationId())
                                                        .withValue(CourtApplicationResponse::getApplicationResponseDate, saveApplicationResponseCommand.getApplicationResponse().getApplicationResponseDate())
                                                        .withValue(CourtApplicationResponse::getApplicationResponseType, saveApplicationResponseCommand.getApplicationResponse().getApplicationResponseType())
                                                )
                                        ))
                                )
                        )
                ));

    }

}
