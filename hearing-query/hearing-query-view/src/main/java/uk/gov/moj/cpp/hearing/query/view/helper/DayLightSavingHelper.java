package uk.gov.moj.cpp.hearing.query.view.helper;

import java.time.ZonedDateTime;

public final class DayLightSavingHelper {

    private DayLightSavingHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static ZonedDateTime handleDST(boolean isDayLightSavingOn, ZonedDateTime eventTimeUTC) {
        return isDayLightSavingOn ? eventTimeUTC.plusHours(1) : eventTimeUTC;
    }
}

