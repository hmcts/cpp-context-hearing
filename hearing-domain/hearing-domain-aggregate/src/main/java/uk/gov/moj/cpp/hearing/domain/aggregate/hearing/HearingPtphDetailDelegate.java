package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.io.Serializable;
import java.util.stream.Stream;

public class HearingPtphDetailDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingPtphDetailDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> savePtphDetail(final PtphDetailSaved event) {
        if (momento.isPtphDetailFinalised()) {
            throw new RuntimeException("Tier and list type is finalised and cannot be changed");
        }
        return Stream.of(event);
    }

    public Stream<Object> finalisePtphDetail(final PtphDetailFinalised event) {
        if (isNull(momento.getTier()) || isNull(momento.getListType())) {
            throw new RuntimeException("Both tier and list type are required to finalise");
        }
        if (momento.isPtphDetailFinalised()) {
            throw new RuntimeException("Tier and list type is already finalised");
        }
        return Stream.of(event);
    }

    public Stream<Object> deletePtphDetail(final PtphDetailDeleted event) {
        return Stream.of(event);
    }

    public void handlePtphDetailSaved(final PtphDetailSaved event) {
        momento.setTier(event.getTier());
        momento.setListType(event.getListType());
        momento.setPtphDetailKeyReason(event.getKeyReason());
    }

    public void handlePtphDetailFinalised(final PtphDetailFinalised event) {
        momento.setPtphDetailFinalised(true);
    }

    public void handlePtphDetailDeleted(final PtphDetailDeleted event) {
        momento.setTier(null);
        momento.setListType(null);
        momento.setPtphDetailKeyReason(null);
        momento.setPtphDetailFinalised(false);
    }
}
