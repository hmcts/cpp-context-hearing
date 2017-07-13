package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;


public class ResultPromptMatcherTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    ResultPromptMatcher testObj = new ResultPromptMatcher();
    FindPromptsIndexesByKeyword findPromptsIndexesByKeyword = new FindPromptsIndexesByKeyword();
    ComparePromptKeywordsUsingIndexes comparePromptKeywordsUsingIndexes = new ComparePromptKeywordsUsingIndexes();
    FindPromptSynonyms findPromptSynonyms = new FindPromptSynonyms();
    GroupResultByIndex groupResultByIndex = new GroupResultByIndex();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        findPromptsIndexesByKeyword.resultCache = resultCache;
        comparePromptKeywordsUsingIndexes.resultCache = resultCache;
        findPromptSynonyms.resultCache = resultCache;
        testObj.findPromptsIndexesByKeyword = findPromptsIndexesByKeyword;
        testObj.comparePromptKeywordsUsingIndexes = comparePromptKeywordsUsingIndexes;
        testObj.findPromptSynonyms = findPromptSynonyms;
        testObj.groupResultByIndex = groupResultByIndex;
    }

    @Test
    public void match1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 concurrent yr m w d early not release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = testObj.match(values);

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getLabel()
                , is("Early release provisions do not apply")
        );
    }

    @Test
    public void match2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 conc");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = testObj.match(values);

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getLabel()
                , is("Concurrent")
        );
    }

    @Test
    public void match3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 concu");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = testObj.match(values);

        assertThat(
                resultPromptMatchingOutput.getResultPrompt() == null
                , is(true)
        );
    }

    @Test
    public void match4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 yr");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = testObj.match(values);

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getDurationElement()
                , is("Years")
        );
    }

}