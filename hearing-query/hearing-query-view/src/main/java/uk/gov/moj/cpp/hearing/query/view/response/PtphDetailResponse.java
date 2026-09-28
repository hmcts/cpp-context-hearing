package uk.gov.moj.cpp.hearing.query.view.response;

public class PtphDetailResponse {

    private final String tier;
    private final String listType;
    private final String keyReason;
    private final boolean finalised;

    public PtphDetailResponse(final String tier, final String listType, final String keyReason, final boolean finalised) {
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
        this.finalised = finalised;
    }

    public String getTier() {
        return tier;
    }

    public String getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }

    public boolean isFinalised() {
        return finalised;
    }
}
