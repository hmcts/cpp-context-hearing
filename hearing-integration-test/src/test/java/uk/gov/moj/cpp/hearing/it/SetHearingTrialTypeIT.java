package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.jayway.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.Test;

public class SetHearingTrialTypeIT extends AbstractIT {
    private static final UUID TRAIL_TYPE_ID_1 = randomUUID();
    private static final UUID VACATE_REASON_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();

    @Test
    public void shouldSetInEffectiveTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);

        final Hearing hearing = initiateHearingCommand.getHearing();
        h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final TrialType addTrialType = TrialType.builder()
                .withHearingId(hearing.getId())
                .withTrialTypeId(TRAIL_TYPE_ID_1)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearing.getId(), addTrialType);

        List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = buildCrackedIneffectiveVacatedTrialTypes(TRAIL_TYPE_ID_1);
        ReferenceDataStub.stubCrackedIOnEffectiveTrialTypes(crackedIneffectiveVacatedTrialTypes);

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = crackedIneffectiveVacatedTrialTypes.get(0);

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), crackedIneffectiveVacatedTrialType.getReasonFullDescription(), crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearing.getId()))
                        .with(Hearing::getCrackedIneffectiveTrial, Matchers.is(expectedTrialType))
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
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getIsEffectiveTrial, Matchers.is(TRUE))
                ));
    }


    @Test
    public void shouldSetVacateTrialTypeToHearing() {
        InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        givenAUserHasLoggedInAsACourtClerk(USER_ID);

        List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = buildVacatedTrialTypes(VACATE_REASON_ID);
        ReferenceDataStub.stubCrackedIOnEffectiveTrialTypes(crackedIneffectiveVacatedTrialTypes);
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));
        final TrialType addTrialType = TrialType.builder()
                .withVacatedTrialReasonId(VACATE_REASON_ID)
                .build();

        UseCases.setTrialType(getRequestSpec(), hearingOne.getHearingId(), addTrialType);

        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = crackedIneffectiveVacatedTrialTypes.get(0);

        CrackedIneffectiveTrial expectedTrialType = new CrackedIneffectiveTrial(crackedIneffectiveVacatedTrialType.getReasonCode(), "", crackedIneffectiveVacatedTrialType.getId(), crackedIneffectiveVacatedTrialType.getTrialType());

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getCrackedIneffectiveTrial, Matchers.is(expectedTrialType))
                ));
    }

    private List<CrackedIneffectiveVacatedTrialType> buildCrackedIneffectiveVacatedTrialTypes(final UUID trialTypeId) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(trialTypeId, "code", "InEffective", "fullDescription"));

        return trialList;
    }

    private List<CrackedIneffectiveVacatedTrialType> buildVacatedTrialTypes(final UUID vacatedTrialReasonId) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(vacatedTrialReasonId, "code", "Vacated", null));

        return trialList;
    }
}
