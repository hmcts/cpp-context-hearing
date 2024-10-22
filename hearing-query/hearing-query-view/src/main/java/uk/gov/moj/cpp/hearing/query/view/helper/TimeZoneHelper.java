package uk.gov.moj.cpp.hearing.query.view.helper;

import java.util.Date;

public class TimeZoneHelper {

    public boolean isDayLightSavingOn() {
        return java.util.TimeZone.getTimeZone("Europe/London").inDaylightTime(new Date());
    }
}
