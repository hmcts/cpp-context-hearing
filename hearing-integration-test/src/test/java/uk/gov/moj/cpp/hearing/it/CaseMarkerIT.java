package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateCaseMarkers;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class CaseMarkerIT extends AbstractIT {

    @Test
    public void shouldUpdateCaseMarkers() throws Exception {


        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(now().plusDays(1));
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(requestSpec, initiateHearingCommand));
        final UUID hearingId = initiateHearingCommand.getHearing().getId();
        final UUID prosecutionCaseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();

        // Add one case marker to update to existing prosecution case
        final List<Marker> markers = hearingOne.getFirstCase().getCaseMarkers();
        updateCaseMarkers(prosecutionCaseId, hearingId, markers);
        // Matching Hearing Object with Case Marker
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getCaseMarkers, first(isBean(Marker.class)
                                    .with(Marker::getMarkerTypeid, is(markers.get(0).getMarkerTypeid()))
                                    .with(Marker::getMarkerTypeCode, is(markers.get(0).getMarkerTypeCode()))
                                    .with(Marker::getMarkerTypeDescription, is(markers.get(0).getMarkerTypeDescription()))))
                                ))));


        // Remove all the case markers which available in the prosecution case
        updateCaseMarkers(prosecutionCaseId, hearingId, new ArrayList<>());
        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getCaseMarkers, hasSize(0))))));

    }

}