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

    public void handleConvictionDateAdded(final ConvictionDateAdded convictionDateAdded) {
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(convictionDateAdded.getCaseId()))
                .forEach(prosecutionCase ->
                        prosecutionCase.getDefendants().stream()
                                .flatMap(d -> d.getOffences().stream())
                                .filter(o -> o.getId().equals(convictionDateAdded.getOffenceId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                                .setConvictionDate(convictionDateAdded.getConvictionDate()));
        this.momento.getConvictionDates().computeIfAbsent(
                convictionDateAdded.getOffenceId(), offenceId -> convictionDateAdded.getConvictionDate());
    }

    public void handleConvictionDateRemoved(final ConvictionDateRemoved convictionDateRemoved) {
        this.momento.getHearing().getProsecutionCases().stream()
                .filter(prosecutionCase -> prosecutionCase.getId().equals(convictionDateRemoved.getCaseId()))
                .forEach(prosecutionCase ->
                        prosecutionCase.getDefendants().stream()
                                .flatMap(d -> d.getOffences().stream())
                                .filter(o -> o.getId().equals(convictionDateRemoved.getOffenceId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Invalid offence id on conviction date message"))
                                .setConvictionDate(null));
        this.momento.getConvictionDates().remove(convictionDateRemoved.getOffenceId());
    }
}
