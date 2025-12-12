package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.changeNextHearingDate;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ChangeNextHearingDateIT extends AbstractIT {

    @Test
    @Disabled("Temporarily disabled as Feature Toggle tests are not working on Jenkins master pipeline")
    public void shouldUpdateEarliestNextHearingDate() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(now().plusDays(1));
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        final UUID seedingHearingId = initiateHearingCommand.getHearing().getId();
        final UUID hearingId = UUID.randomUUID();

        final ZonedDateTime nextHearingStartDate = ZonedDateTime.of(2022, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));

        changeNextHearingDate(seedingHearingId, hearingId, nextHearingStartDate);

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(seedingHearingId))
                        .with(Hearing::getEarliestNextHearingDate, is(nextHearingStartDate))));
    }


}