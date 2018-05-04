package uk.gov.moj.cpp.hearing.persist.entity.ex;

public enum NowsMaterialStatus {

    REQUESTED("Requested"),
    GENERATED("Generated");

    private String description;

    private NowsMaterialStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static NowsMaterialStatus getCaseStatusk(final String value) {
        final NowsMaterialStatus[] caseStatusArray = NowsMaterialStatus.values();
        for (final NowsMaterialStatus caseStatus : caseStatusArray) {
            if (caseStatus.getDescription().equals(value)) {
                return caseStatus;
            }
        }
        return null;
    }
}
