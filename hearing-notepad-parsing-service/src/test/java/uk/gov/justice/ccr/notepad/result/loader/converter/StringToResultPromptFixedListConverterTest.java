package uk.gov.justice.ccr.notepad.result.loader.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;

import org.junit.Test;

public class StringToResultPromptFixedListConverterTest {
    @Test
    public void shouldReturnNullForInvalidLine() throws Exception {
        assertThat(new StringToResultPromptSynonymConverter().convert("ddd") == null, is(true));
    }

    @Test
    public void shouldReturnResultPromptFixedList() throws Exception {
        //given
        String line = "Imprisonment Reason\t001\tOffence so serious";

        //when
        ResultPromptFixedList actual = new StringToResultPromptFixedListConverter().convert(line);


        //then
        assertThat(actual.getId(), is(equalTo("Imprisonment Reason")));
        assertThat(actual.getValue(), is(equalTo("Offence so serious")));
    }

}