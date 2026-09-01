package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

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

    /**
     * Emits only. Whether a save is allowed at all — hearing exists, Crown Court, not already
     * finalised, key reason present for a fixed list type — is decided by {@code HearingAggregate},
     * which turns a rejection into {@code hearing.hearing-change-ignored} rather than an exception.
     * Throwing here would dead-letter the hearing's command queue; see
     * {@code HearingAggregate.ptphDetailIgnored}.
     */
    public Stream<Object> savePtphDetail(final PtphDetailSaved event) {
        return Stream.of(event);
    }

    /**
     * Emits only — see {@link #savePtphDetail}. The aggregate has already established that the
     * hearing exists, that tier and list type are both recorded, and that it is not yet finalised.
     */
    public Stream<Object> finalisePtphDetail(final PtphDetailFinalised event) {
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
        clearPtphDetail();
    }

    /**
     * A tier and list type only ever describe a hearing, so they lose their reason to exist the
     * moment the hearing does. Applied both to an explicit delete and to the deletion or
     * duplication of the hearing itself, which mirrors {@code HearingDeletedEventListener}
     * removing the {@code ha_ptph_detail} row alongside the hearing it belongs to.
     */
    public void clearPtphDetail() {
        momento.setTier(null);
        momento.setListType(null);
        momento.setPtphDetailKeyReason(null);
        momento.setPtphDetailFinalised(false);
    }
}
