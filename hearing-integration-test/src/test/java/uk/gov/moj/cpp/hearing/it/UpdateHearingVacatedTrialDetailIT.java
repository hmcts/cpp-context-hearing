package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class UpdateHearingVacatedTrialDetailIT extends AbstractIT {

    @Test
    public void shouldUpdateHearingVacatedTrialDetail() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        final UUID vacatedTrialReasonId = randomUUID();
        HearingVacatedTrialDetailsUpdateCommand hearingVacatedTrialDetailsUpdateCommand = UseCases.updateHearingVacatedTrialDetail(new HearingVacatedTrialDetailsUpdateCommand(hearingOne.getHearingId(),vacatedTrialReasonId,true,true));
        List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = buildVacatedTrialTypes(vacatedTrialReasonId);
        ReferenceDataStub.stubCrackedIOnEffectiveTrialTypes(crackedIneffectiveVacatedTrialTypes);

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

    private List<CrackedIneffectiveVacatedTrialType> buildVacatedTrialTypes(final UUID vacatedTrialReasonId) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(vacatedTrialReasonId, "code", "Vacated", null));

        return trialList;
    }


}
