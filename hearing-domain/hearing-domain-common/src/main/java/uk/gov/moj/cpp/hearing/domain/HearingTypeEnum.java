package uk.gov.moj.cpp.hearing.domain;

public enum HearingTypeEnum {
    TRIAL("Trial"), PTP("PTP"), SENTENCE("Sentence");

    private String value;

    HearingTypeEnum(final String value) {
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

    public static HearingTypeEnum getHearingType(final String value) {
        final HearingTypeEnum[] hearingTypeArray = HearingTypeEnum.values();
        for (final HearingTypeEnum hearingType : hearingTypeArray) {
            if (hearingType.getValue().equalsIgnoreCase(value)) {
                return hearingType;
            }
        }
        return null;
    }

}
