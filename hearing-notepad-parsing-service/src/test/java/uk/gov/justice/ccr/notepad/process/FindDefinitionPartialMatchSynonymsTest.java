package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.shared.AbstractTest;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FindDefinitionPartialMatchSynonymsTest extends AbstractTest {

    @InjectMocks
    FindDefinitionPartialMatchSynonyms target;

    @Test
    public void run() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp sus Rehabilitation excep sati x xyz a v");

        Map<String, Set<String>> output = target.run(parts.stream().filter(v -> v.getValueAsString().length() > 1).map(part -> part.getValueAsString().toLowerCase()).collect(toList()), LocalDate.now());


        assertThat(
                output.size()
                , is(5)
        );

        List<String> expectesResult = Arrays.asList("rehabilitation",
                "imprisonment",
                "activate",
                "stimp",
                "sso",
                "stdet",
                "susps",
                "except",
                "ticcompensation",
                "extivs",
                "timp",
                "compensation",
                "suspended");

        Set<String> actualResult = output.entrySet().stream().map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        List<String> difference = expectesResult.stream().filter(v -> !actualResult.contains(v)).collect(Collectors.toList());
        assertThat(difference.toString(),
                difference.size()
                , is(1)
        );

    }


}
