package uk.gov.justice.ccr.notepad.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CurrencyMatcherTest {

    private CurrencyMatcher testObj = new CurrencyMatcher();

    @Test
    public void validCurrencyMatcherTest() {
        validCurrencyProvider()
                .stream()
                .forEach(v ->
                        assertThat(v,
                                testObj.match(v)
                                , is(true)
                        ));
    }

    @Test
    public void invalidCurrencyMatcherTest() {
        invalidCurrencyProvider()
                .stream()
                .forEach(v ->
                        assertThat(v,
                                testObj.match(v)
                                , is(false)
                        ));
    }

    private List<String> validCurrencyProvider() {
        return Arrays.asList("£56656", "£0", "£000", "£44.99", "£3434,00");
    }

    private List<String> invalidCurrencyProvider() {
        return Arrays.asList("222£", "bvbvb£7676", "£", "£d", "£444r", "$33333", "£-90", "£ ");
    }

}