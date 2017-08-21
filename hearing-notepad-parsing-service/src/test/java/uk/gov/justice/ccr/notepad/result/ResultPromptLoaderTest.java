package uk.gov.justice.ccr.notepad.result;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ResultPromptLoaderTest {

    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
    }

    @Test
    public void getResultPrompts() throws Exception {
        assertThat(resultCache.getResultPrompt().size(), is(1504));
    }

    @Test
    public void getResultPromptsWithFixedLists() throws Exception {
        List<ResultPrompt> resultPrompts = resultCache.getResultPrompt().stream().filter(r -> r.getType() == ResultType.FIXL).collect(Collectors.toList());
        assertThat(resultPrompts.size(), is(6));
        resultPrompts.stream().map(ResultPrompt::getFixedList).forEach(Assert::assertNotNull);
    }

    @Test
    public void promptResultDefinitionLabelShouldContainsInResultDefinition() throws Exception {
        List<String> resultPromptDefinitionLabel = resultCache.getResultPrompt()
                .stream().map(value -> value.getResultDefinitionLabel()).collect(Collectors.toList());
        List<String> resultDefinitionLabel = resultCache.getResultDefinition()
                .stream().map(value -> value.getLabel().toLowerCase()).collect(Collectors.toList());
        resultPromptDefinitionLabel.stream().forEach(value -> {
            assertThat(resultDefinitionLabel.stream().filter(s -> s.equalsIgnoreCase(value)).findFirst().orElse(""), is(value.toLowerCase()));
        });
    }

}