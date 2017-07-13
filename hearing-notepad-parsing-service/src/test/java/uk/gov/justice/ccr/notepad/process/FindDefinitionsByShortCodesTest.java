package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.result.loader.FileResultLoader;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;

public class FindDefinitionsByShortCodesTest {
    ResultCache resultCache = new ResultCache();
    FileResultLoader fileResultLoader = new FileResultLoader();
    FindDefinitionsByShortCodes testObj = new FindDefinitionsByShortCodes();

    @Before
    public void init() throws ExecutionException {
        resultCache.setResultLoader(fileResultLoader);
        resultCache.lazyLoad(null);
        testObj.resultCache = resultCache;
    }

    @Test
    public void run1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Imp SHOPE PaRP pr sati stimp");

        Set<ResultDefinition> resultDefinitions = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()));

        assertThat(
                resultDefinitions.size()
                , is(5)
        );
        assertThat(
                Arrays.asList("imp", "parp","pr","shope", "stimp").containsAll(resultDefinitions.stream().map(ResultDefinition::getShortCode).collect(toList()))
                , is(true)
        );

    }

    @Test
    public void run2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("SHOPE");

        Set<ResultDefinition> resultDefinitions = testObj.run(parts.stream().map(part -> part.getValueAsString().toLowerCase()).collect(toList()));

        assertThat(
                resultDefinitions.size()
                , is(1)
        );
        assertThat(
                Arrays.asList("shope").containsAll(resultDefinitions.stream().map(ResultDefinition::getShortCode).collect(toList()))
                , is(true)
        );

    }
}