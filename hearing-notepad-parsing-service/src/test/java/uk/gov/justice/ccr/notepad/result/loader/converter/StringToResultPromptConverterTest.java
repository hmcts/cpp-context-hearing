package uk.gov.justice.ccr.notepad.result.loader.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;


public class StringToResultPromptConverterTest {
    @Test
    public void convertForNonValidResultPrompt() throws Exception {
        assertThat(new StringToResultPromptConverter(null).convert("ddd") == null, is(true));
    }

    @Test
    public void convertResultPromptWithFixedList() throws Exception {
        //given
        Map<String, Set<String>> fixedListMap = new HashMap<>();
        Set<String> fixedListSet = Sets.newTreeSet(Sets.newHashSet("testFixedListItemA", "testFixedListItemC", "testFixedListItemB"));
        String fixedListId = "test fixed list id";
        fixedListMap.put(fixedListId, fixedListSet);
        StringToResultPromptConverter stringToResultPromptConverter = new StringToResultPromptConverter(fixedListMap);

        //when
        ResultPrompt resultPrompt = stringToResultPromptConverter.convert("Definition id\tDefinition label\tY\tFIXL\t\t\ttest fixed list id");

        //then
        assertThat(resultPrompt == null, is(false));
        assertThat(resultPrompt.getLabel(), is(equalTo("Definition label")));
        assertThat(resultPrompt.getFixedList(), is(equalTo(fixedListSet)));



    }

}