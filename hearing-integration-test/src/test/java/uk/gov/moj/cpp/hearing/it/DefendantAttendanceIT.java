package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Test;
import uk.gov.justice.json.schemas.core.AttendanceDay;
import uk.gov.justice.json.schemas.core.DefendantAttendance;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import java.time.LocalDate;
import java.util.UUID;


public class DefendantAttendanceIT extends AbstractIT {

    @Test
    public void updateDefendantAttendance() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        h(UseCases.updateDefendantAttendance(requestSpec, updateDefendantAttendanceTemplate(hearingId, defendantId, dateOfAttendance, Boolean.TRUE)));

        Queries.getHearingPollForMatch(hearingId, 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                .with(DefendantAttendance::getDefendantId, is(defendantId))
                                .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                        .with(AttendanceDay::getDay, is(dateOfAttendance))
                                        .with(AttendanceDay::getIsInAttendance, is(true))))))));

    }
}
