package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;

import uk.gov.justice.core.courts.Hearing;
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

    /**
     * Tier and list type are a Crown Court concern, so a hearing outside the Crown Court must
     * never acquire a record. Checked on save alone: it is the only command that creates one,
     * so an ineligible hearing can never hold a record for finalise or delete to act on.
     *
     * <p>The hearing <em>type</em> is deliberately not checked. Tier and list type are captured
     * at a Plea and Trial Preparation hearing in practice, but the court may record them at any
     * Crown hearing, and pinning the rule to a set of reference-data type ids would have meant
     * editing this class every time a new PTPH type was added — the aggregate must stay free of
     * I/O, so it cannot resolve a type <em>code</em> from reference data.
     *
     * <p>The hearing is passed in rather than read from this delegate's own momento: the
     * aggregate holds {@code momento} as a final field that tests replace by reflection, so a
     * delegate reading its captured reference would see a stale one.
     */
    public boolean isEligibleForPtphDetail(final Hearing hearing) {
        return nonNull(hearing) && CROWN.equals(hearing.getJurisdictionType());
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
