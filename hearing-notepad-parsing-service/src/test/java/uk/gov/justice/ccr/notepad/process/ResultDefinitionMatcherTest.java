package uk.gov.justice.ccr.notepad.process;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.CONTAINS;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.EQUALS;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.SHORT_CODE;
import static uk.gov.justice.ccr.notepad.process.ResultDefinitionMatchingOutput.MatchingType.UNKNOWN;

import uk.gov.justice.ccr.notepad.result.cache.ResultCache;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultDefinition;
import uk.gov.justice.ccr.notepad.view.Part;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class ResultDefinitionMatcherTest {
    ResultCache resultCache = new ResultCache();
    ResultDefinitionMatcher testObj = new ResultDefinitionMatcher();
    FindDefinitionsIndexesByKeyword findDefinitionsIndexesByKeyword = new FindDefinitionsIndexesByKeyword();
    CompareDefinitionKeywordsUsingIndexes compareDefinitionKeywordsUsingIndexes = new CompareDefinitionKeywordsUsingIndexes();
    FindDefinitionPartialMatchSynonyms findDefinitionPartialMatchSynonyms = new FindDefinitionPartialMatchSynonyms();
    FindDefinitionsByShortCodes findDefinitionsByShortCodes = new FindDefinitionsByShortCodes();
    FindDefinitionExactMatchSynonyms findDefinitionExactMatchSynonyms = new FindDefinitionExactMatchSynonyms();
    GroupResultByIndex groupResultByIndex = new GroupResultByIndex();

    @Before
    public void init() throws ExecutionException {
        resultCache.loadResultCache();
        findDefinitionsIndexesByKeyword.resultCache = resultCache;
        compareDefinitionKeywordsUsingIndexes.resultCache = resultCache;
        findDefinitionExactMatchSynonyms.resultCache = resultCache;
        findDefinitionsByShortCodes.resultCache = resultCache;
        findDefinitionPartialMatchSynonyms.resultCache = resultCache;
        testObj.findDefinitionsIndexesByKeyword = findDefinitionsIndexesByKeyword;
        testObj.compareDefinitionKeywordsUsingIndexes = compareDefinitionKeywordsUsingIndexes;
        testObj.findDefinitionExactMatchSynonyms = findDefinitionExactMatchSynonyms;
        testObj.groupResultByIndex = groupResultByIndex;
        testObj.findDefinitionPartialMatchSynonyms = findDefinitionPartialMatchSynonyms;
        testObj.findDefinitionsByShortCodes = findDefinitionsByShortCodes;
    }

    @Test
    public void match1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

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
    public void match2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

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
    public void match3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

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
    public void match3_1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("restr ord pred pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

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
    public void match3_2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("restr ord prd fur");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }

    @Test
    public void match3_3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("tes");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }

    @Test
    public void match3_4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("timp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }
    @Test
    public void match3_5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("stimp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }
    @Test
    public void match3_6() throws Exception {
        List<Part> parts = new PartsResolver().getParts("community ord em curfew");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }

    @Test
    public void match3_6_1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("community order em curfew");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(UNKNOWN)
        );
    }
    @Test
    public void match3_7() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(EQUALS)
        );
    }
    @Test
    public void match4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("iquash pard");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition() == null
                , is(true)
        );
    }

    @Test
    public void match5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("iquash hello hi 2 5");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Indictment quashed")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(SHORT_CODE)
        );
    }
    @Ignore
    //Ignoring test case as it test synonym contains processing and we uncommented due to some reason check with BA before uncommenting
    public void match6() throws Exception {
        List<Part> parts = new PartsResolver().getParts("res ord prd im su");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());


        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Restraining order for period")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(CONTAINS)
        );
    }

    @Test
    public void match7() throws Exception {
        List<Part> parts = new PartsResolver().getParts("f f f f f f f f f f f f f f f f f f f f f");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());


        ResultDefinitionMatchingOutput resultDefinitionMatchingOutput = testObj.match(values);

        assertThat(
                resultDefinitionMatchingOutput.getResultDefinition().getLabel()
                , is("Fine")
        );
        assertThat(
                resultDefinitionMatchingOutput.getMatchingType()
                , is(SHORT_CODE)
        );
    }

    @Test
    public void matchEqual1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prd imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order for period")
        );
    }

    @Test
    public void matchEqual4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("com");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Community order England / Wales")
        );
    }
    @Test
    public void matchEqual5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("upw");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Community requirement: Unpaid work")
        );
    }
    @Test
    public void matchEqual2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prdr further imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order until further order")
        );
    }

    @Test
    public void matchEqual3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Mandatory life imprisonment")
        );
    }

    @Test
    public void matchEqual_When_No_Result_Definition_Found() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mandi life impi release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchEqual(values);

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );
    }

    @Test
    public void matchContains1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("res ord pr su");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchContains(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order for period")
        );
    }

    @Test
    public void matchContains2() throws Exception {
        List<Part> parts = new PartsResolver().getParts("rest ord prdr further imp sus");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchContains(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Restraining order until further order")
        );
    }

    @Test
    public void matchContains3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mand life imp release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchContains(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Mandatory life imprisonment")
        );
    }

    @Test
    public void matchContains_When_No_Result_Definition_Found() throws Exception {
        List<Part> parts = new PartsResolver().getParts("mandi life impi release");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchContains(values);

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );
    }

    @Test
    public void matchShortCode1() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("pard 123 $123");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchShortCode(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Community requirement: Prohibited activity for dates")
        );

    }

    @Test
    public void matchShortCode2() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("pard pr");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchShortCode(values);

        assertThat(
                resultDefinition.isPresent()
                , is(false)
        );

    }

    @Test
    public void matchShortCode3() throws ExecutionException {
        List<Part> parts = new PartsResolver().getParts("f f f f f f f f f f f f f f f f f");
        List<String> values = parts.stream().map(Part::getValueAsString).collect(toList());

        Optional<ResultDefinition> resultDefinition = testObj.matchShortCode(values);

        assertThat(
                resultDefinition.get().getLabel()
                , is("Fine")
        );

    }


}