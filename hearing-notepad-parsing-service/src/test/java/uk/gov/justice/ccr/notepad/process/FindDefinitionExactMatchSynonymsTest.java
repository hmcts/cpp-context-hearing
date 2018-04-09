package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;


public class FindDefinitionExactMatchSynonymsTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    FindDefinitionExactMatchSynonyms testObj = new FindDefinitionExactMatchSynonyms();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        testObj.resultCache = resultCache;
    }

    @Test
    public void run() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus Rehabilitation excep");

        Map<String, Set<String>> output = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()));

        assertThat(
                output.size()
                , is(3)
        );

        assertThat(output.keySet(), hasItems("imp", "rehabilitation", "sus"));
    }

    @Test
    public void run_WhenNothingMatched() throws Exception {
        List<Part> parts = new PartsResolver().getParts("[ssssss]");

        Map<String, Set<String>> words = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()));

        assertThat(
                words.size()
                , is(0)
        );

    }

}
