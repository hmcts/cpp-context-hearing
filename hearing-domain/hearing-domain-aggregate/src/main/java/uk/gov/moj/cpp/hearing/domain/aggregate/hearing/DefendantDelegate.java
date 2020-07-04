package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class DefendantDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private HearingAggregateMomento momento;

    private List<UUID> defendantDetailsChanged = new ArrayList<>();


    public DefendantDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefendantDetailsUpdated(final DefendantDetailsUpdated defendantDetailsUpdated) {
        this.momento.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> matchDefendant(defendant, defendantDetailsUpdated.getDefendant()))
                .findFirst()
                .ifPresent(defendant -> {
                    final DefendantDetailsUtils verify = new DefendantDetailsUtils();

                    if(!verify.verifyDDCHOnRequiredAttributes(defendant, defendantDetailsUpdated.getDefendant())) {
                        defendantDetailsChanged.add(defendant.getId());
                    }
                    setDefendant(defendant, defendantDetailsUpdated.getDefendant());
                });
    }

    public List<UUID> getDefendantDetailsChanged() {
        return new ArrayList<>(defendantDetailsChanged);
    }

    public void clearDefendantDetailsChanged() {
        if(!defendantDetailsChanged.isEmpty()) {
            this.defendantDetailsChanged.clear();
        }
    }
    public void handleDefendantAttendanceUpdated(final DefendantAttendanceUpdated defendantAttendanceUpdated) {

        final List<DefendantAttendance> defendantAttendances = nonNull(this.momento.getHearing().getDefendantAttendance()) ? this.momento.getHearing().getDefendantAttendance() : new ArrayList<>();

        final Map<UUID, DefendantAttendance> defendantAttendanceMap = defendantAttendances.stream()
                .collect(Collectors.toMap(DefendantAttendance::getDefendantId, Function.identity()));

        final DefendantAttendance defendantAttendance = defendantAttendanceMap.computeIfAbsent(defendantAttendanceUpdated.getDefendantId(), id -> DefendantAttendance.defendantAttendance()
                .withDefendantId(id)
                .withAttendanceDays(new ArrayList<>())
                .build());

        final Map<LocalDate, AttendanceDay> localDateAttendanceDayMap = defendantAttendance.getAttendanceDays().stream().collect(Collectors.toMap(AttendanceDay::getDay, Function.identity()));

        final AttendanceDay attendanceDay = localDateAttendanceDayMap.computeIfAbsent(defendantAttendanceUpdated.getAttendanceDay().getDay(), date -> AttendanceDay.attendanceDay().withDay(date).build());

        attendanceDay.setAttendanceType(defendantAttendanceUpdated.getAttendanceDay().getAttendanceType());

        defendantAttendance.setAttendanceDays(new ArrayList<>(localDateAttendanceDayMap.values()));

        this.momento.getHearing().setDefendantAttendance(new ArrayList<>(defendantAttendanceMap.values()));

    }

    public Stream<Object> updateDefendantDetails(final UUID hearingId, final uk.gov.moj.cpp.hearing.command.defendant.Defendant newDefendant) {

        if(!this.momento.isPublished() && nonNull(momento.getHearing())) {

            final Optional<Defendant> previouslyStoredDefendant = momento.getHearing().getProsecutionCases().stream()
                    .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                    .filter(d -> d.getId().equals(newDefendant.getId()))
                    .findFirst();

            if (previouslyStoredDefendant.isPresent() && nonNull(newDefendant.getPersonDefendant()) && nonNull(newDefendant.getPersonDefendant().getPersonDetails())) {
                final String storedTitle = previouslyStoredDefendant.get().getPersonDefendant().getPersonDetails().getTitle();
                final String newTitle = newDefendant.getPersonDefendant().getPersonDetails().getTitle();
                if (newTitle == null) {
                    newDefendant.getPersonDefendant().getPersonDetails().setTitle(storedTitle);
                }
            }

            return Stream.of(DefendantDetailsUpdated.defendantDetailsUpdated()
                    .setHearingId(hearingId)
                    .setDefendant(newDefendant)
            );
        }

        return Stream.empty();
    }

    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDayP) {

        if (!this.momento.isPublished()) {
            final AttendanceDay attendanceDay = AttendanceDay.attendanceDay()
                    .withDay(attendanceDayP.getDay())
                    .withAttendanceType(attendanceDayP.getAttendanceType())
                    .build();
            return Stream.of(DefendantAttendanceUpdated.defendantAttendanceUpdated()
                    .setHearingId(hearingId)
                    .setDefendantId(defendantId)
                    .setAttendanceDay(attendanceDay));
        }
        return Stream.empty();
    }

    private boolean matchDefendant(final Defendant defendant, final uk.gov.moj.cpp.hearing.command.defendant.Defendant updatedDefendant) {
        return defendant.getId().equals(updatedDefendant.getId()) &&
                defendant.getProsecutionCaseId().equals(updatedDefendant.getProsecutionCaseId());
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
                .setProsecutionCaseId(defendantIn.getProsecutionCaseId())
                .setMasterDefendantId(defendantIn.getMasterDefendantId())
                .setAssociatedDefenceOrganisation(defendantIn.getAssociatedDefenceOrganisation());
    }

    @SuppressWarnings("squid:S2384")
    public void setDefendantDetailsChanged(final List<UUID> defendantDetailsChanged) {
        this.defendantDetailsChanged = defendantDetailsChanged;
    }

    public void setMomento(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public HearingAggregateMomento getMomento() {
        return momento;
    }
}
