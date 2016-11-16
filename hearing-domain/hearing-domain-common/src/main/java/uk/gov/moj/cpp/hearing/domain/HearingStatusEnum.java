package uk.gov.moj.cpp.hearing.domain;

public enum HearingStatusEnum {
    BOOKED("BOOKED"), VACATED("VACATED");

    private String value;

    private HearingStatusEnum(final String value) {
        this.value = value;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getValue() {
        return value;
    }

    public static HearingStatusEnum getHearingStatus(final String value) {
        final HearingStatusEnum[] hearingStatusArray = HearingStatusEnum.values();
        for (final HearingStatusEnum hearingStatus : hearingStatusArray) {
            if (hearingStatus.getValue().equalsIgnoreCase(value)) {
                return hearingStatus;
            }
        }
        return null;
    }

}
