package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.stream.Collectors.toSet;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefenceCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public DefenceCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefenceCounselAdded(final DefenceCounselAdded defenceCounselAdded) {
        final DefenceCounsel defenceCounsel = defenceCounselAdded.getDefenceCounsel();
        this.momento.getDefenceCounsels().put(defenceCounsel.getId(), defenceCounsel);
    }

    public void handleDefenceCounselRemoved(final DefenceCounselRemoved defenceCounselRemoved) {
        this.momento.getDefenceCounsels().remove(defenceCounselRemoved.getId());
    }

    public void handleDefenceCounselUpdated(final DefenceCounselUpdated defenceCounselUpdated) {
        final DefenceCounsel defenceCounsel = defenceCounselUpdated.getDefenceCounsel();
        this.momento.getDefenceCounsels().put(defenceCounsel.getId(), defenceCounsel);
    }

    public Stream<Object> addDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId, boolean isHearingEnded) {
        final Hearing hearing = this.momento.getHearing();
        // Check for defendant(s) exists on the hearing
        if(isNoDfendantsOnTheHearing(defenceCounsel, hearing)) {
            return Stream.empty();
        }
        if (this.momento.getDefenceCounsels().containsKey(defenceCounsel.getId())) {
            return Stream.of(new DefenceCounselChangeIgnored(
                    String.format("Provided DefenceCounsel already exists, payload [%s]", defenceCounsel.toString()),
                    defenceCounsel, hearingId,  getCaseReference(hearing)));
        } else if (isHearingEnded) {
            return Stream.of(new DefenceCounselChangeIgnored(
                    String.format("Hearing Ended. Failed to check-in , payload [%s]", defenceCounsel.toString()),
                    defenceCounsel, hearingId, getCaseReference(hearing)));
        }

        return Stream.of(new DefenceCounselAdded(defenceCounsel, hearingId));
    }

    private boolean isNoDfendantsOnTheHearing(final DefenceCounsel defenceCounsel, final Hearing hearing) {
        final Set<UUID> defendantIds = hearing.getProsecutionCases().stream()
                .map(ProsecutionCase::getDefendants)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()).stream().map(Defendant::getId).collect(toSet());

        return defenceCounsel.getDefendants().stream().filter(aDefId -> defendantIds.contains(aDefId)).findFirst().isPresent() ? false:true;
    }

    public Stream<Object> removeDefenceCounsel(final UUID id, final UUID hearingId) {
        return Stream.of(new DefenceCounselRemoved(id, hearingId));
    }

    public Stream<Object> updateDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId) {

        final Map<UUID, DefenceCounsel> defenceCounsels = this.momento.getDefenceCounsels();
        final String caseRef = this.momento.getHearing() != null ? this.momento.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN():null;

        if (!(defenceCounsels.containsKey(defenceCounsel.getId()))) {
            return Stream.of(new DefenceCounselChangeIgnored(
                    String.format("Provided DefenceCounsel does not exists, payload [%s]", defenceCounsel.toString()),
                    defenceCounsel, hearingId, caseRef));
        }else if (defenceCounsels.get(defenceCounsel.getId()).equals(defenceCounsel)){
            return Stream.of(new DefenceCounselChangeIgnored(
                    String.format("No change in provided DefenceCounsel, payload [%s]", defenceCounsel.toString()),
                    defenceCounsel, hearingId, caseRef));
        }
        return Stream.of(new DefenceCounselUpdated(defenceCounsel, hearingId));
    }

    private String getCaseReference(Hearing hearing){
        final String prosecutionCaseURN = hearing!= null ? hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN():null;
        final String prosecutionAuthorityReference = hearing!= null ? hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference():null;
        return prosecutionCaseURN != null ? prosecutionCaseURN : prosecutionAuthorityReference;
    }
}
