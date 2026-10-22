package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    public Stream<Object>  addDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId, boolean isHearingEnded) {
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
        // includes the case defendants of applications on inactive cases, which have no prosecution cases on the hearing
        final Set<UUID> defendantIds = CounselHearingDetails.defendantIds(hearing);

        return defenceCounsel.getDefendants().stream().filter(aDefId -> defendantIds.contains(aDefId)).findFirst().isPresent() ? false:true;
    }

    public Stream<Object> removeDefenceCounsel(final UUID id, final UUID hearingId, final boolean isHearingEnded) {
        final DefenceCounsel defenceCounsel = this.momento.getDefenceCounsels().get(id);
        if (defenceCounsel == null || isHearingEnded ) {
            return Stream.empty();
        }

        return Stream.of(new DefenceCounselRemoved(id, hearingId));
    }

    public Stream<Object> updateDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId, final boolean isHearingEnded) {

        final Map<UUID, DefenceCounsel> defenceCounsels = this.momento.getDefenceCounsels();
        if (defenceCounsel == null || isHearingEnded ) {
            return Stream.empty();
        }
        final Hearing hearing = this.momento.getHearing();
        final String caseRef = hearing != null && isNotEmpty(hearing.getProsecutionCases())
                ? hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()
                : CounselHearingDetails.caseReference(hearing);

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
        return CounselHearingDetails.caseReference(hearing);
    }
}
