package uk.gov.justice.ccr.notepad.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Time24HoursMatcher {

    private static final Pattern TIME_24_HOUR_PATTERN = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");

    public boolean match(final String time) {
        Matcher matcher = TIME_24_HOUR_PATTERN.matcher(time);
        return matcher.matches();
    }

}
