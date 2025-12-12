package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.CustodialEstablishment.custodialEstablishment;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.time.LocalDate;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

@NotThreadSafe
public class DefendantAttendanceIT extends AbstractIT {

    @Test
    public void updateDefendantAttendance() {

        InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0)
                .getPersonDefendant().setCustodialEstablishment(
                        custodialEstablishment()
                                .withCustody("POLICE")
                                .withName("East Croydon Police Station")
                                .withId(randomUUID())
                                .build());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), initiateHearing));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        try (EventListener publicDefendantAttendanceUpdated = listenFor("public.hearing.defendant-attendance-updated", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(DefendantAttendanceUpdated.class, isBean(DefendantAttendanceUpdated.class)
                        .with(DefendantAttendanceUpdated::getHearingId, is(hearingId))
                        .with(DefendantAttendanceUpdated::getDefendantId, is(defendantId))
                        .with(DefendantAttendanceUpdated::getAttendanceDay, isBean(AttendanceDay.class)
                                .with(AttendanceDay::getDay, is(dateOfAttendance))
                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON)))))) {

            h(UseCases.updateDefendantAttendance(getRequestSpec(), updateDefendantAttendanceTemplate(hearingId, defendantId, dateOfAttendance, AttendanceType.IN_PERSON)));

            publicDefendantAttendanceUpdated.waitFor();
        }

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getDefendantAttendance, first(isBean(DefendantAttendance.class)
                                .with(DefendantAttendance::getDefendantId, is(defendantId))
                                .with(DefendantAttendance::getAttendanceDays, first(isBean(AttendanceDay.class)
                                        .with(AttendanceDay::getDay, is(dateOfAttendance))
                                        .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON))))))));

    }

}
