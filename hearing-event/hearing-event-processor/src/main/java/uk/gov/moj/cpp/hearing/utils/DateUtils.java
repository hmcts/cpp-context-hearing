package uk.gov.moj.cpp.hearing.utils;

import static java.time.ZonedDateTime.parse;
import static java.time.format.DateTimeFormatter.ofPattern;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter dateFormatter = ofPattern("dd/MM/yy");
    private static final DateTimeFormatter timeFormatter = ofPattern("HH:mm");
    private static final ZoneId localZoneId = ZoneId.of("Europe/London");

    private DateUtils() {}

    public static String convertToLocalTime(String dateTime) {
        return timeFormatter.format(parse(dateTime).withZoneSameInstant(localZoneId));
    }

    public static String convertZonedDateTimeToLocalTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(localZoneId).format(timeFormatter);
    }

    public static String convertToLocalDate(String date) {
        return LocalDate.parse(date).format(DateUtils.dateFormatter);
    }
}
