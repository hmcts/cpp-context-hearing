package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class Time24HoursMatcherTest {
    private Time24HoursMatcher testObj = new Time24HoursMatcher();

    @Test
    public void ValidTime24HoursTest() {
        validTime24HoursProvider()
                .stream()
                .forEach(v ->
                        assertThat(
                                testObj.match(v)
                                , is(true)
                        ));
    }

    @Test
    public void InValidTime24HoursTest() {
        invalidTime24HoursProvider()
                .stream()
                .forEach(v ->
                        assertThat(
                                testObj.match(v)
                                , is(false)
                        ));
    }

    private List<String> validTime24HoursProvider() {
        return Arrays.asList("01:00", "02:00", "13:00", "1:00", "2:00", "13:01"
                , "23:59", "15:00", "00:00", "0:00");
    }

    private List<String> invalidTime24HoursProvider() {
        return Arrays.asList("24:00", "12:60", "0:0", "13:1", "101:00");
    }
}