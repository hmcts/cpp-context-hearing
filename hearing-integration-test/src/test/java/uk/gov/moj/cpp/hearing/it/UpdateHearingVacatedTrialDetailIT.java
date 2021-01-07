package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VACATED_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

@SuppressWarnings("unchecked")
public class UpdateHearingVacatedTrialDetailIT extends AbstractIT {

    @Test
    public void shouldUpdateHearingVacatedTrialDetail() throws Exception {


        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final HearingVacatedTrialDetailsUpdateCommand hearingVacateTrialDetailsUpdateCommand = new HearingVacatedTrialDetailsUpdateCommand(hearingOne.getHearingId(), VACATED_TRIAL_TYPE_ID, true, true);
        HearingVacatedTrialDetailsUpdateCommand hearingVacatedTrialDetailsUpdateCommand = UseCases.updateHearingVacatedTrialDetail(hearingVacateTrialDetailsUpdateCommand);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
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
