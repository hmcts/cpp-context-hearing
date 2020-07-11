package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.ccr.notepad.shared.AbstractTest;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultPromptMatcherTest extends AbstractTest {

    @InjectMocks
    ResultPromptMatcher target = new ResultPromptMatcher();

    @Test
    public void shouldMatchEarlyReleaseProvisionsDoNotApplyPrompt() {
        List<Part> parts = new PartsResolver().getParts("imp 2 concurrent yr m w d early not release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getLabel()
                , is("Early release provisions do not apply")
        );
    }

    @Test
    public void shouldMatchConcurrentPrompt() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 conc");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getLabel()
                , is("Concurrent")
        );
    }

    @Test
    public void shouldResutPromptIsNull() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 concu");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultPromptMatchingOutput.getResultPrompt() == null
                , is(true)
        );
    }

    @Test
    public void shouldResultPromptHasDurationElementInYears() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 yr");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultPromptMatchingOutput resultPromptMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultPromptMatchingOutput.getResultPrompt().getDurationElement()
                , is("Years")
        );
    }

}
