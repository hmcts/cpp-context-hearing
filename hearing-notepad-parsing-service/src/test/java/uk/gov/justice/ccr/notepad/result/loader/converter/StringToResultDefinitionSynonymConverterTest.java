package uk.gov.justice.ccr.notepad.result.loader.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;


public class StringToResultDefinitionSynonymConverterTest {
    @Test
    public void convert() throws Exception {
        assertThat(new StringToResultDefinitionSynonymConverter().convert("ddd") == null, is(true));
    }

}