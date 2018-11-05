package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import java.io.Serializable;

public class ConvictionDateDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ConvictionDateDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleConvictionDateAdded(ConvictionDateAdded convictionDateAdded) {
        this.momento.getHearing().getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().equals(convictionDateAdded.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                .setConvictionDate(convictionDateAdded.getConvictionDate());
    }

    public void handleConvictionDateRemoved(ConvictionDateRemoved convictionDateRemoved) {
        this.momento.getHearing().getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> o.getId().equals(convictionDateRemoved.getOffenceId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                .setConvictionDate(null);
    }

}
