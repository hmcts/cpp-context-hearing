package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingPtphDetailDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Reference-data hearing type ids whose code is a Plea and Trial Preparation hearing:
     * {@code PTP} ("Plea and Trial Preparation") and the superseded {@code FPTP}
     * ("Further Plea &amp; Trial Preparation"). Held as ids because the core
     * {@code hearingType} carries only id, description and welshDescription — no code — and
     * an aggregate must stay free of I/O, so it cannot resolve the code from reference data.
     * Adding a new PTPH type to reference data means adding its id here.
     */
    private static final Set<UUID> PTPH_HEARING_TYPE_IDS = Set.of(
            UUID.fromString("06b0c2bf-3f98-46ed-ab7e-56efaf9ecced"),
            UUID.fromString("9cc41e45-b594-4ba6-906e-1a4626b08fed"));

    private final HearingAggregateMomento momento;

    public HearingPtphDetailDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    /**
     * Tier and list type are a Crown Court PTPH concern only, so a hearing that is not a
     * Crown PTPH must never acquire a record. Checked on save alone: it is the only command
     * that creates one, so an ineligible hearing can never hold a record for finalise or
     * delete to act on.
     *
     * <p>The hearing is passed in rather than read from this delegate's own momento: the
     * aggregate holds {@code momento} as a final field that tests replace by reflection, so a
     * delegate reading its captured reference would see a stale one.
     */
    public boolean isEligibleForPtphDetail(final Hearing hearing) {
        return nonNull(hearing)
                && CROWN.equals(hearing.getJurisdictionType())
                && nonNull(hearing.getType())
                && PTPH_HEARING_TYPE_IDS.contains(hearing.getType().getId());
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
