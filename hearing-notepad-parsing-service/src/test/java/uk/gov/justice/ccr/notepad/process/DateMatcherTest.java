package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DateMatcherTest {
    private DateMatcher testObj = new DateMatcher();

    @Test
    public void validDateMatcherTest() {
        validDateProvider()
                .stream()
                .forEach(v ->
                        assertThat(v,
                                testObj.match(v)
                                , is(true)
                        ));
    }

    @Test
    public void invalidDateMatcherTest() {
        invalidDateProvider()
                .stream()
                .forEach(v ->
                        assertThat(v,
                                testObj.match(v)
                                , is(false)
                        ));
    }

    private List<String> validDateProvider() {
        return Arrays.asList("01/03/1980", "12/12/9999", "1-1-9999", "1.1.9999", "1/1/1600", "29/2/1600");
    }

    private List<String> invalidDateProvider() {
        return Arrays.asList("13/13/1980", "29/2/2017", "1/1/1599", "1.1.10000", "32/01/1980", "0/01/1980", "01/0/1980", "01/01/999");
    }
}