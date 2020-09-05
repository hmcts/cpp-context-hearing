package uk.gov.moj.cpp.hearing.it;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.UUID;

import org.junit.Test;

public class SetHearingTrialTypeIT extends AbstractIT {
    private static final UUID USER_ID = randomUUID();

    @Test
    public void shouldSetIneffectiveTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);

        final Hearing hearing = initiateHearingCommand.getHearing();
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final TrialType addTrialType = TrialType.builder()
                .withHearingId(hearing.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearing.getId(), addTrialType);

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = INEFFECTIVE_TRIAL_TYPE;

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                ));
    }


    @Test
    public void shouldSetEffectiveTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));
        final TrialType addTrialType = TrialType.builder()
                .withIsEffectiveTrial(TRUE)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getIsEffectiveTrial, is(TRUE))
                ));
    }


    @Test
    public void shouldSetVacateTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));
        final TrialType addTrialType = TrialType.builder()
                .withVacatedTrialReasonId(VACATED_TRIAL_TYPE_ID)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = VACATED_TRIAL_TYPE;

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                ));
    }

}
