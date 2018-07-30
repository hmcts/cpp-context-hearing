package uk.gov.justice.ccr.notepad.result.loader.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptFixedList;

import org.junit.Test;

public class StringToResultPromptFixedListConverterTest {

    @Test
    public void shouldReturnNullForInvalidLine() {
        assertThat(new StringToResultPromptSynonymConverter().convert("ddd") == null, is(true));
    }

    @Test
    public void shouldReturnResultPromptFixedList() {
        final String line = "8dc70c95-fb09-4842-9138-bc579fceb605\tConviction / acquittal\tCON\tConvicted\t\t\t\t";

        final ResultPromptFixedList actual = new StringToResultPromptFixedListConverter().convert(line);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getId(), is(equalTo("8dc70c95-fb09-4842-9138-bc579fceb605")));
        assertThat(actual.getValue(), is(equalTo("Convicted")));
    }

}