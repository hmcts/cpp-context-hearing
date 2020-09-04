package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class ConvictionDateDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ConvictionDateDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleConvictionDateAdded(final ConvictionDateAdded convictionDateAdded) {
        final UUID offenceId = convictionDateAdded.getOffenceId();

        // this check shouldnt be needed but is there as a defensive mechanism for events raised prior to this change going in
        if (!this.momento.getConvictionDates().containsKey(offenceId)) {
            updateOffenceConvictionDate(convictionDateAdded.getCaseId(), offenceId, convictionDateAdded.getConvictionDate());
            this.momento.getConvictionDates().put(offenceId, convictionDateAdded.getConvictionDate());
        }
    }

    public void handleConvictionDateRemoved(final ConvictionDateRemoved convictionDateRemoved) {
        updateOffenceConvictionDate(convictionDateRemoved.getCaseId(), convictionDateRemoved.getOffenceId(), null);
        this.momento.getConvictionDates().remove(convictionDateRemoved.getOffenceId());
    }

    private void updateOffenceConvictionDate(final UUID caseId, final UUID offenceId, final LocalDate convictionDate) {
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(caseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid case id on conviction date message"))
                .getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().equals(offenceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                .setConvictionDate(convictionDate);
    }
}
