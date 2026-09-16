package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ha_ptph_detail")
public class PtphDetail {

    @Id
    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "tier")
    private String tier;

    @Column(name = "list_type")
    private String listType;

    @Column(name = "key_reason")
    private String keyReason;

    @Column(name = "finalised", nullable = false)
    private boolean finalised;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(final String tier) {
        this.tier = tier;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(final String listType) {
        this.listType = listType;
    }

    public String getKeyReason() {
        return keyReason;
    }

    public void setKeyReason(final String keyReason) {
        this.keyReason = keyReason;
    }

    public boolean isFinalised() {
        return finalised;
    }

    public void setFinalised(final boolean finalised) {
        this.finalised = finalised;
    }
}
