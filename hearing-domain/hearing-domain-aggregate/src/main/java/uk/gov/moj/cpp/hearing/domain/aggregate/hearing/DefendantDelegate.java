package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;

import uk.gov.justice.json.schemas.core.AttendanceDay;
import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.justice.json.schemas.core.DefendantAttendance;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefendantDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefendantDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefendantDetailsUpdated(final DefendantDetailsUpdated defendantDetailsUpdated) {
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> matchDefendant(defendant, defendantDetailsUpdated))
                .findFirst()
                .ifPresent(defendant -> setDefendant(defendant, defendantDetailsUpdated.getDefendant()));
    }

    public void handleDefendantAttendanceUpdated(final DefendantAttendanceUpdated defendantAttendanceUpdated) {

        List<DefendantAttendance> defendantAttendances = nonNull(this.momento.getHearing().getDefendantAttendance()) ? this.momento.getHearing().getDefendantAttendance() : new ArrayList<>();

        Map<UUID, DefendantAttendance> defendantAttendanceMap = defendantAttendances.stream()
                .collect(Collectors.toMap(DefendantAttendance::getDefendantId, Function.identity()));

        DefendantAttendance defendantAttendance = defendantAttendanceMap.computeIfAbsent(defendantAttendanceUpdated.getDefendantId(), (id) -> DefendantAttendance.defendantAttendance()
                .withDefendantId(id)
                .withAttendanceDays(new ArrayList<>())
                .build());

        Map<LocalDate, AttendanceDay> localDateAttendanceDayMap = defendantAttendance.getAttendanceDays().stream().collect(Collectors.toMap(AttendanceDay::getDay, Function.identity()));

        AttendanceDay attendanceDay = localDateAttendanceDayMap.computeIfAbsent(defendantAttendanceUpdated.getAttendanceDay().getDay(), (date) -> AttendanceDay.attendanceDay().withDay(date).build());

        attendanceDay.setIsInAttendance(defendantAttendanceUpdated.getAttendanceDay().getIsInAttendance());

        defendantAttendance.setAttendanceDays(new ArrayList<>(localDateAttendanceDayMap.values()));

        this.momento.getHearing().setDefendantAttendance(new ArrayList<>(defendantAttendanceMap.values()));

    }

    public Stream<Object> updateDefendantDetails(final UUID hearingId, final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendant) {

        if (!this.momento.isPublished()) {
            return Stream.of(DefendantDetailsUpdated.defendantDetailsUpdated()
                    .setHearingId(hearingId)
                    .setDefendant(defendant)
            );
        }

        return Stream.empty();
    }

    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDayP) {

        if (!this.momento.isPublished()) {
            final AttendanceDay attendanceDay = AttendanceDay.attendanceDay()
                    .withDay(attendanceDayP.getDay())
                    .withIsInAttendance(attendanceDayP.getIsInAttendance())
                    .build();
            return Stream.of(DefendantAttendanceUpdated.defendantAttendanceUpdated()
                    .setHearingId(hearingId)
                    .setDefendantId(defendantId)
                    .setAttendanceDay(attendanceDay));
        }
        return Stream.empty();
    }

    private boolean matchDefendant(final Defendant defendant, final DefendantDetailsUpdated defendantDetailsUpdated) {
        return defendant.getId().equals(defendantDetailsUpdated.getDefendant().getId()) &&
                defendant.getProsecutionCaseId().equals(defendantDetailsUpdated.getDefendant().getProsecutionCaseId());
    }

    private void setDefendant(final Defendant defendant, final uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantIn) {
        defendant.setAssociatedPersons(defendantIn.getAssociatedPersons())
                .setDefenceOrganisation(defendantIn.getDefenceOrganisation())
                .setLegalEntityDefendant(defendantIn.getLegalEntityDefendant())
                .setMitigation(defendantIn.getMitigation())
                .setMitigationWelsh(defendantIn.getMitigationWelsh())
                .setNumberOfPreviousConvictionsCited(defendantIn.getNumberOfPreviousConvictionsCited())
                .setPersonDefendant(defendantIn.getPersonDefendant())
                .setProsecutionAuthorityReference(defendantIn.getProsecutionAuthorityReference())
                .setWitnessStatement(defendantIn.getWitnessStatement())
                .setWitnessStatementWelsh(defendantIn.getWitnessStatementWelsh())
                .setProsecutionCaseId(defendantIn.getProsecutionCaseId());
    }
}