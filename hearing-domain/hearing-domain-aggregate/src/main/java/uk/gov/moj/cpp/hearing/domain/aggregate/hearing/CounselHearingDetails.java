package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Hearing details used by counsel check-in. A hearing for an application on an inactive case has no
 * prosecution cases; its case defendants and case reference come from the court application instead.
 */
final class CounselHearingDetails {

    private CounselHearingDetails() {
    }

    /**
     * Defendants of the hearing's prosecution cases, plus the case defendants linked to the parties of
     * its court applications.
     */
    static Set<UUID> defendantIds(final Hearing hearing) {
        final Stream<UUID> caseDefendantIds = prosecutionCases(hearing).stream()
                .map(ProsecutionCase::getDefendants)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(Defendant::getId);

        final Stream<UUID> applicationDefendantIds = courtApplications(hearing).stream()
                .flatMap(CounselHearingDetails::parties)
                .filter(party -> nonNull(party.getMasterDefendant()))
                .map(party -> party.getMasterDefendant().getDefendantCase())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(DefendantCase::getDefendantId);

        return Stream.concat(caseDefendantIds, applicationDefendantIds)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /**
     * The case URN (or prosecution authority reference) of the hearing's first prosecution case; for a
     * hearing without prosecution cases, the first application's reference — the same rule the hearing
     * event log uses ({@link HearingEventDelegate#getReference()}).
     */
    static String caseReference(final Hearing hearing) {
        if (hearing == null) {
            return null;
        }
        final List<ProsecutionCase> prosecutionCases = prosecutionCases(hearing);
        if (!prosecutionCases.isEmpty()) {
            return reference(prosecutionCases.get(0).getProsecutionCaseIdentifier());
        }
        final List<CourtApplication> courtApplications = courtApplications(hearing);
        if (courtApplications.isEmpty()) {
            return null;
        }
        return courtApplications.get(0).getApplicationReference();
    }

    private static String reference(final ProsecutionCaseIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return identifier.getCaseURN() != null ? identifier.getCaseURN() : identifier.getProsecutionAuthorityReference();
    }

    private static Stream<CourtApplicationParty> parties(final CourtApplication courtApplication) {
        return Stream.concat(
                        Stream.of(courtApplication.getApplicant(), courtApplication.getSubject()),
                        ofNullable(courtApplication.getRespondents()).orElse(emptyList()).stream())
                .filter(Objects::nonNull);
    }

    private static List<ProsecutionCase> prosecutionCases(final Hearing hearing) {
        return hearing == null ? emptyList() : ofNullable(hearing.getProsecutionCases()).orElse(emptyList());
    }

    private static List<CourtApplication> courtApplications(final Hearing hearing) {
        return hearing == null ? emptyList() : ofNullable(hearing.getCourtApplications()).orElse(emptyList());
    }
}
