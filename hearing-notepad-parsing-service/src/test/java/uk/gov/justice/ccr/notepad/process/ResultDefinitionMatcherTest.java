package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.EQUALS;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.SHORT_CODE;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.UNKNOWN;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.shared.AbstractTest;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultDefinitionMatcherTest extends AbstractTest {

    @InjectMocks
    ResultDefinitionMatcher target = new ResultDefinitionMatcher();

    @Test
    public void shouldMatchRestrainingOrderForPeriodPattern1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Restraining order for period")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchMandatoryLifeImprisonmentPattern1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Mandatory life imprisonment")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchRestrainingOrderForPeriodPattern2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Restraining order for period")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchCommunityRequirementProhibitedActivityForDatePattern1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("restr ord pred pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Community requirement: Prohibited activity for dates")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(SHORT_CODE)
        );
    }

    @Test
    public void shouldNotMatchAnyPattern1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("restr ord prd fur");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }

    @Test
    public void shouldNotMatchAnyPattern2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("tes");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }

    @Test
    public void shouldMatchEqualsPattern1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("timp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchEqualsPattern2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("stimp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchEqualsPattern3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("community ord em curfew");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchEqualsPattern4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("community order em curfew");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchEqualsPattern5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldResultDefinitionIsNull() throws Exception {
        List<Part> parts = new PartsResolver().getParts("iquash pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition() == null
                , is(true)
        );
    }

    @Test
    public void shouldMatchIndictmentQuashed() throws Exception {
        List<Part> parts = new PartsResolver().getParts("iquash hello hi 2 5");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Indictment quashed")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(SHORT_CODE)
        );
    }

    @Test
    public void shouldMatchFinePatter1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("f f f f f f f f f f f f f f f f f f f f f");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = target.match(values, LocalDate.now());

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Fine")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }

    @Test
    public void shouldMatchEqual1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order for period")
        );
    }

    @Test
    public void shouldMatchEqual4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("com");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(resultDefinition.isPresent(), is(false));
    }

    @Test
    public void shouldMatchEqual5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("upw");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order for period")
        );
    }

    @Test
    public void shouldMatchEqual2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prdr further imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order until further order")
        );
    }

    @Test
    public void shouldMatchEqual3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Mandatory life imprisonment")
        );
    }

    @Test
    public void shouldMatchEqualWhenNoResultDefinitionFound() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mandi life impi release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchBySynonym(values, LocalDate.now());

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );
    }

    @Test
    public void shouldMatchContains1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("res ord pr su");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchContains(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order for period")
        );
    }

    @Test
    public void shouldMatchContains2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prdr further imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchContains(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order until further order")
        );
    }

    @Test
    public void shouldMatchContains3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchContains(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Mandatory life imprisonment")
        );
    }

    @Test
    public void shouldMatchContainsWhenNoResultDefinitionFound() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mandi life impi release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchContains(values, LocalDate.now());

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );
    }

    @Test
    public void shouldMatchShortCode1() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("pard 123 $123");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchByShortCode(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Community requirement: Prohibited activity for dates")
        );

    }

    @Test
    public void shouldMatchShortCode2() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("pard pr");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchByShortCode(values, LocalDate.now());

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );

    }

    @Test
    public void shouldMatchShortCode3() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("f f f f f f f f f f f f f f f f f");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = target.matchByShortCode(values, LocalDate.now());

        assertThat(
                resultDefinition.get().getLabel()
                , is("Fine")
        );

    }
}
