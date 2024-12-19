package uk.gov.moj.cpp.hearing.it;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.command.TrialType.builder;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateHearingVacatedTrialDetail;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.UUID;

import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S2699")
class SetHearingTrialTypeIT extends AbstractIT {
    private static final UUID USER_ID = randomUUID();

    @Test
    void shouldSetIneffectiveTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);
        stubUsersAndGroupsUserRoles(USER_ID);

        final Hearing hearing = initiateHearingCommand.getHearing();
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final TrialType addTrialType = builder()
                .withHearingId(hearing.getId())
                .withTrialTypeId(INEFFECTIVE_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearing.getId(), addTrialType);

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = INEFFECTIVE_TRIAL_TYPE;

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getDate(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                ));
    }

    @Test
    void shouldSetEffectiveTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);
        stubUsersAndGroupsUserRoles(USER_ID);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final TrialType addTrialType = builder()
                .withIsEffectiveTrial(TRUE)
                .build();

        setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getIsEffectiveTrial, is(TRUE))
                ));
    }

    @Test
    void shouldSetVacateTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);
        stubUsersAndGroupsUserRoles(USER_ID);

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final TrialType addTrialType = builder()
                .withVacatedTrialReasonId(VACATED_TRIAL_TYPE_ID)
                .build();

        setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        final CrackedIneffectiveVacatedTrialType trialType = VACATED_TRIAL_TYPE;

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(trialType.getReasonCode(), trialType.getDate(), trialType.getReasonFullDescription(), trialType.getId(), trialType.getTrialType());

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getCrackedIneffectiveTrial, is(expectedTrialType))
                ));
    }

    @Test
    void shouldUpdateHearingWhenHearingIsVacatedInListing() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final HearingVacatedTrialDetailsUpdateCommand hearingVacateTrialDetailsUpdateCommand = new HearingVacatedTrialDetailsUpdateCommand(hearingOne.getHearingId(), VACATED_TRIAL_TYPE_ID, true, true);
        HearingVacatedTrialDetailsUpdateCommand hearingVacatedTrialDetailsUpdateCommand = updateHearingVacatedTrialDetail(hearingVacateTrialDetailsUpdateCommand);

        getHearingPollForMatch(hearingOne.getHearingId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getIsVacatedTrial, is(true))
                        .with(Hearing::getCrackedIneffectiveTrial, isBean(CrackedIneffectiveTrial.class)
                                .with(CrackedIneffectiveTrial::getId, is(hearingVacatedTrialDetailsUpdateCommand.getVacatedTrialReasonId()))
                        )
                )
        );
    }

}
