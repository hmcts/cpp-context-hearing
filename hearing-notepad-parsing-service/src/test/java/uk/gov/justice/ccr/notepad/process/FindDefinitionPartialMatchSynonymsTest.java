package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;


public class FindDefinitionPartialMatchSynonymsTest {

    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    FindDefinitionPartialMatchSynonyms testObj = new FindDefinitionPartialMatchSynonyms();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        testObj.resultCache = resultCache;
    }

    @Test
    public void run() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus Rehabilitation excep sati x xyz a v");

        Map<String, Set<String>> output = testObj.run(parts.stream().filter(v -> v.getValueAsString().length() > 1).map(part -> part.getValueAsString().toLowerCase()).collect(toList()));


        assertThat(
                output.size()
                , is(5)
        );
        assertThat(
                Arrays.asList("rehabilitation",
                        "imprisonment",
                        "activate",
                        "stimp",
                        "sso",
                        "tsusp",
                        "susps",
                        "except",
                        "ticcompensation",
                        "extivs",
                        "timp",
                        "compensation",
                        "suspended").containsAll(output.entrySet().stream().map(Map.Entry::getValue)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet()))
                , is(true)
        );

    }


}