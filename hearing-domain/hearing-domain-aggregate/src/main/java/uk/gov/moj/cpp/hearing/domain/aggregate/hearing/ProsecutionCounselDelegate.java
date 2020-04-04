package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ProsecutionCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public ProsecutionCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleProsecutionCounselAdded(final ProsecutionCounselAdded prosecutionCounselAdded) {
        final ProsecutionCounsel prosecutionCounsel = prosecutionCounselAdded.getProsecutionCounsel();
        this.momento.getProsecutionCounsels().put(prosecutionCounsel.getId(), prosecutionCounsel);
    }

    public void handleProsecutionCounselRemoved(final ProsecutionCounselRemoved prosecutionCounselRemoved) {
        this.momento.getProsecutionCounsels().remove(prosecutionCounselRemoved.getId());
    }

    public void handleProsecutionCounselUpdated(final ProsecutionCounselUpdated prosecutionCounselUpdated) {
        final ProsecutionCounsel prosecutionCounsel = prosecutionCounselUpdated.getProsecutionCounsel();
        this.momento.getProsecutionCounsels().put(prosecutionCounsel.getId(), prosecutionCounsel);
    }
    public Stream<Object> addProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId, boolean isHearingEnded) {
        final Hearing hearing = this.momento.getHearing();

        if (this.momento.getProsecutionCounsels().containsKey(prosecutionCounsel.getId())) {
            return Stream.of(new ProsecutionCounselChangeIgnored(
                    String.format("Provided ProsecutionCounsel already exists, payload [%s]", prosecutionCounsel.toString()),
                    prosecutionCounsel, hearingId, getCaseReference(hearing)));
        } else if (isHearingEnded) {
            return Stream.of(new ProsecutionCounselChangeIgnored(
                    String.format("Hearing Ended. Failed to check-in , payload [%s]", prosecutionCounsel.toString()),
                    prosecutionCounsel, hearingId, getCaseReference(hearing)));
        }

        return Stream.of(new ProsecutionCounselAdded(prosecutionCounsel, hearingId));
    }

    public Stream<Object> removeProsecutionCounsel(final UUID id, final UUID hearingId) {
        return Stream.of(new ProsecutionCounselRemoved(id, hearingId));
    }

    public Stream<Object> updateProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {

        final Map<UUID, ProsecutionCounsel> prosecutionCounsels = this.momento.getProsecutionCounsels();
        final String caseRef = this.momento.getHearing() != null ? this.momento.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN():null;

        if (!(prosecutionCounsels.containsKey(prosecutionCounsel.getId()))) {
            return Stream.of(new ProsecutionCounselChangeIgnored(
                    String.format("Provided ProsecutionCounsel does not exists, payload [%s]", prosecutionCounsel.toString()),
                    prosecutionCounsel, hearingId, caseRef));
        }else if (prosecutionCounsels.get(prosecutionCounsel.getId()).equals(prosecutionCounsel)){
            return Stream.of(new ProsecutionCounselChangeIgnored(
                    String.format("No change in provided ProsecutionCounsel, payload [%s]", prosecutionCounsel.toString()),
                    prosecutionCounsel, hearingId, caseRef));
        }
        return Stream.of(new ProsecutionCounselUpdated(prosecutionCounsel, hearingId));
    }

    private String getCaseReference(Hearing hearing){
        final String prosecutionCaseURN = hearing!= null ? hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN():null;
        final String prosecutionAuthorityReference = hearing!= null ? hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference():null;
        return prosecutionCaseURN != null ? prosecutionCaseURN : prosecutionAuthorityReference;
    }
}
