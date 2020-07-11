package uk.gov.justice.ccr.notepad.process;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.hamcrest.core.AnyOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.ccr.notepad.shared.AbstractTest;

@RunWith(MockitoJUnitRunner.class)
public class FindDefinitionsIndexesByKeywordTest extends AbstractTest {
    @InjectMocks
    FindDefinitionsIndexesByKeyword target;

    @Test
    public void run() throws Exception {
        Set<String> words = newHashSet("rehabilitation", "imprisonment", "suspended");

        List<Long> resultDefinitionsIndex = target.run(words, LocalDate.now());

        assertThat(
                resultDefinitionsIndex.size()
                , is(41)
        );
        resultDefinitionsIndex.forEach(resultDefinition -> {
//            try {
            assertThat(
                    resultCache.getResultDefinitions(LocalDate.now()).get(resultDefinition.intValue()).getKeywords().toString(),
                    AnyOf.anyOf(containsString("rehabilitation"), containsString("imprisonment"), containsString("suspended"))
            );
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
        });
    }

}
