package uk.gov.justice.ccr.notepad.result;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultPrompt;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        assertThat(resultCache.getResultPrompt().size(), is(2075));
    }

    @Test
    public void getResultPromptsWithFixedLists() throws Exception {
        List<ResultPrompt> resultPrompts = resultCache.getResultPrompt().stream().filter(r -> r.getType() == ResultType.FIXL).collect(toList());
        assertThat(resultPrompts.size(), is(7));
        resultPrompts.stream().map(ResultPrompt::getFixedList).forEach(Assert::assertNotNull);
    }

    @Test
    public void promptResultDefinitionIdShouldContainInResultDefinition() throws Exception {
        List<String> resultPromptDefinitionIds = resultCache.getResultPrompt()
                .stream().map(value -> value.getResultDefinitionId().toString()).collect(toList());
        List<String> resultDefinitionIds = resultCache.getResultDefinitions()
                .stream().map(ResultDefinition::getId).collect(toList());
        resultPromptDefinitionIds.forEach(value -> 
                assertThat(resultDefinitionIds.stream().filter(s -> s.equals(value)).findFirst().orElse(""), is(value)));
    }

}
