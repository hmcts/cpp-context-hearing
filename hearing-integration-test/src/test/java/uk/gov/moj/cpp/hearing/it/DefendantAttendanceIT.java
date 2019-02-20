package uk.gov.moj.cpp.hearing.it;

import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;


public class DefendantAttendanceIT extends AbstractIT {

    @Test
    public void updateDefendantAttendance() {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        final Utilities.EventListener publicDefendantAttendanceUpdated = listenFor("public.hearing.defendant-attendance-updated", 30000)
                .withFilter(convertStringTo(DefendantAttendanceUpdated.class, isBean(DefendantAttendanceUpdated.class)
                        .with(DefendantAttendanceUpdated::getHearingId, is(hearingId))
                        .with(DefendantAttendanceUpdated::getDefendantId, is(defendantId))
                        .with(DefendantAttendanceUpdated::getAttendanceDay, isBean(AttendanceDay.class)
                                .with(AttendanceDay::getDay, is(dateOfAttendance))
                                .with(AttendanceDay::getIsInAttendance, is(Boolean.TRUE)))));

        h(UseCases.updateDefendantAttendance(requestSpec, updateDefendantAttendanceTemplate(hearingId, defendantId, dateOfAttendance, Boolean.TRUE)));

        publicDefendantAttendanceUpdated.waitFor();

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
