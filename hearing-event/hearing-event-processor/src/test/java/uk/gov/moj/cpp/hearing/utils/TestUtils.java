package uk.gov.moj.cpp.hearing.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtils {

    public static ZonedDateTime convertZonedDate(final ZonedDateTime datetime)
    {
        var dateString = datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        return ZonedDateTime.ofInstant(ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME).toInstant(), ZoneId.of("UTC"));
    }
}
