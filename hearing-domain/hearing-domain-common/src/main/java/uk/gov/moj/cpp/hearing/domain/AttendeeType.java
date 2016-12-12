package uk.gov.moj.cpp.hearing.domain;

public enum AttendeeType {
    DEFENCE_COUNSEL("Defence Counsel"),
    PROSECUTION_COUNSEL("Prosecution Counsel");

    private String value;

    private AttendeeType(final String value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    public static AttendeeType getAttendeeType(final String value) {
        final AttendeeType[] attendeeTypes = AttendeeType.values();
        for (final AttendeeType attendeeType : attendeeTypes) {
            if (attendeeType.getValue().equalsIgnoreCase(value)) {
                return attendeeType;
            }
        }
        return null;
    }

}

