package uk.gov.justice.ccr.notepad.result.loader;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import org.junit.Test;

public class FileResultLoaderTest {

    @Test
    public void loadResultPromptWithFixedList() {
        //given
        final FileResultLoader testObject = new FileResultLoader();

        //when
        final List<ResultPrompt> actualPrompts = testObject.loadResultPrompt();

        //then
        assertThat(actualPrompts.isEmpty(), is(false));
        final List<ResultPrompt> actualFixedListPrompts = actualPrompts.stream()
                .filter(r -> r.getType() == ResultType.FIXL).collect(toList());
        final HashSet<String> expected = Sets.newHashSet("Acquitted", "Convicted");
        assertThat(actualFixedListPrompts.get(2).getFixedList(), is(new TreeSet<>(expected)));
    }

}
