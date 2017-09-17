package uk.gov.justice.ccr.notepad.result.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.mockito.Mock;

public class FileResultLoaderTest {

    @Mock
    ResultPromptsProcessor resultPromptsProcessor;

    @Test
    public void loadResultPromptWithFixedList() throws Exception {
        //given
        FileResultLoader testObject = new FileResultLoader();

        //when
        List<ResultPrompt> actualPrompts = testObject.loadResultPrompt();


        //then
        assertThat(actualPrompts.isEmpty(), is(false));
        List<ResultPrompt> actualFixedListPrompts = actualPrompts.stream()
                .filter(r -> r.getType() == ResultType.FIXL).collect(Collectors.toList());
        HashSet<String> expected = Sets.newHashSet("Acquitted", "Convicted");
        assertThat(actualFixedListPrompts.get(1).getFixedList(), is(new TreeSet<String>(expected)));
    }

}