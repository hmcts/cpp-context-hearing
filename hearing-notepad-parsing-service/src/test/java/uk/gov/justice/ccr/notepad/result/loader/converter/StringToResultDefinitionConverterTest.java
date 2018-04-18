package uk.gov.justice.ccr.notepad.result.loader.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;


public class StringToResultDefinitionConverterTest {
    
    @Test
    public void convert() {
        assertThat(new StringToResultDefinitionConverter().convert("ddd") == null, is(true));
    }

}
