package uk.gov.moj.cpp.hearing.query.view.service.ctl.model;

import java.util.Arrays;

public enum ModeOfTrial {
    SUMMARY_ONLY("Summary"),
    EITHER_WAY("Either Way"),
    INDICTABLE("Indictable");

    private String type;

    ModeOfTrial(final String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    public static ModeOfTrial getIfPresent(final String value) {
        return Arrays.stream(ModeOfTrial.values()).filter(ot -> ot.type().equalsIgnoreCase(value)).findFirst().orElse(null);
    }
}
