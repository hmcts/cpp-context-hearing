package uk.gov.justice.ccr.notepad.result.loader.converter;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;


public class StringToResultPromptConverterTest {

    @Test
    public void convertForNonValidResultPrompt() {
        assertThat(new StringToResultPromptConverter(null).convert("ddd") == null, is(true));
    }

    @Test
    public void convertResultPromptWithFixedList() {
        final Map<String, Set<String>> fixedListMap = new HashMap<>();
        final Set<String> fixedListSet = newTreeSet(newHashSet("testFixedListItemA", "testFixedListItemC", "testFixedListItemB"));
        final String fixedListId = "8dc70c95-fb09-4842-9138-bc579fceb605";
        fixedListMap.put(fixedListId, fixedListSet);
        final StringToResultPromptConverter stringToResultPromptConverter = new StringToResultPromptConverter(fixedListMap);

        final ResultPrompt resultPrompt = stringToResultPromptConverter.convert("2\tb81ac80b-81a9-48b6-87b0-cc34610eec97\tRestraining order for period\t47337f1c-e343-4093-884f-035ba96c4db0\tConviction / acquittal\tY\tFIXL\t\t8dc70c95-fb09-4842-9138-bc579fceb605\tConviction / acquittal\t1\t1\t\t\t\tN\t40\t\t\t\t");

        assertThat(resultPrompt, is(notNullValue()));
        assertThat(resultPrompt.getLabel(), is(equalTo("Conviction / acquittal")));
        assertThat(resultPrompt.getFixedList(), is(equalTo(fixedListSet)));
    }

}
