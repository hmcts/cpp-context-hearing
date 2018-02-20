package uk.gov.justice.ccr.notepad.view;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.BOOLEAN;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.CURR;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DATE;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.DURATION;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.IGNORED;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.INT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.RESULT;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TIME;
import static uk.gov.justice.ccr.notepad.result.cache.model.ResultType.TXT;
import static uk.gov.justice.ccr.notepad.view.Part.State.RESOLVED;
import static uk.gov.justice.ccr.notepad.view.Part.State.UNRESOLVED;

import uk.gov.justice.ccr.notepad.process.Knowledge;
import uk.gov.justice.ccr.notepad.process.Processor;
import uk.gov.justice.ccr.notepad.process.ProcessorTest;
import uk.gov.justice.ccr.notepad.view.parser.PartsResolver;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;


public class ResultDefinitionViewBuilderTest {
    Processor processor;
    ResultDefinitionViewBuilder testObj = new ResultDefinitionViewBuilder();
    ProcessorTest processorTest = new ProcessorTest();

    @Before
    public void setup() throws ExecutionException {
        processorTest.init();
        processor = processorTest.getProcessor();
    }

    @Test
    public void buildFromKnowledge() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2Y 4 M 9d sus 5 m 6 w 7 d ");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part part1 = result.getParts().get(0);
        Part part2 = result.getParts().get(1);
        Part part3 = result.getParts().get(2);
        Part part4 = result.getParts().get(3);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("O")
        );
        assertThat(result.getParts().size()
                , is(4)
        );
        assertThat(part2.getOriginalText()
                , is("2Y 4 M 9d")
        );
        assertThat(Arrays.asList(part1.getValue(), part1.getState(), part1.getType())
                , containsInAnyOrder(Arrays.asList("Suspended sentence order - imprisonment", RESOLVED, RESULT).toArray()));
        assertThat(Arrays.asList(part3.getValue(), part3.getState(), part3.getType())
                , containsInAnyOrder(Arrays.asList("sus", RESOLVED, IGNORED).toArray()));

        PartValue valueYearOfPart2 = ((List<PartValue>) part2.getValue()).get(0);
        assertThat(Arrays.asList(valueYearOfPart2.getValue(), valueYearOfPart2.getLabel(), valueYearOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(2, "Years", INT).toArray()));
        PartValue valueMonthsOfPart2 = ((List<PartValue>) part2.getValue()).get(1);
        assertThat(Arrays.asList(valueMonthsOfPart2.getValue(), valueMonthsOfPart2.getLabel(), valueMonthsOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(4, "Months", INT).toArray()));
        PartValue valueDaysOfPart2 = ((List<PartValue>) part2.getValue()).get(2);
        assertThat(Arrays.asList(valueDaysOfPart2.getValue(), valueDaysOfPart2.getLabel(), valueDaysOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(9, "Days", INT).toArray()));

        PartValue valueMonthsOfPart4 = ((List<PartValue>) part4.getValue()).get(0);
        assertThat(Arrays.asList(valueMonthsOfPart4.getValue(), valueMonthsOfPart4.getLabel(), valueMonthsOfPart4.getType())
                , containsInAnyOrder(Arrays.asList(5, "Months", INT).toArray()));
        PartValue valueWeeksOfPart4 = ((List<PartValue>) part4.getValue()).get(1);
        assertThat(Arrays.asList(valueWeeksOfPart4.getValue(), valueWeeksOfPart4.getLabel(), valueWeeksOfPart4.getType())
                , containsInAnyOrder(Arrays.asList(6, "Weeks", INT).toArray()));
        PartValue valueDaysOfPart4 = ((List<PartValue>) part4.getValue()).get(2);
        assertThat(Arrays.asList(valueDaysOfPart4.getValue(), valueDaysOfPart4.getLabel(), valueDaysOfPart4.getType())
                , containsInAnyOrder(Arrays.asList(7, "Days", INT).toArray()));
    }

    @Test
    public void buildFromKnowledge1() throws Exception {
        List<Part> parts = new PartsResolver().getParts("CrEdit bail reMand");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part part1 = result.getParts().get(0);
        Part part2 = result.getParts().get(1);
        Part part3 = result.getParts().get(2);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("D")
        );
        assertThat(Arrays.asList(part1.getValue(), part1.getState(), part1.getType())
                , containsInAnyOrder(Arrays.asList("Credit for time remanded on bail", RESOLVED, RESULT).toArray()));
        assertThat(Arrays.asList(part2.getValue(), part2.getState(), part2.getType())
                , containsInAnyOrder(Arrays.asList("bail", RESOLVED, IGNORED).toArray()));
        assertThat(Arrays.asList(part3.getValue(), part3.getState(), part3.getType())
                , containsInAnyOrder(Arrays.asList("reMand", RESOLVED, IGNORED).toArray()));
    }

    @Test
    public void buildFromKnowledge3() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 yr 8 m conc");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part part1 = result.getParts().get(0);
        Part part2 = result.getParts().get(1);
        Part part4 = result.getParts().get(2);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("O")
        );
        assertThat(result.getParts().size()
                , is(3)
        );
        assertThat(Arrays.asList(part1.getValue(), part1.getState(), part1.getType())
                , containsInAnyOrder(Arrays.asList("Imprisonment", RESOLVED, RESULT).toArray()));
        assertThat(Arrays.asList(part2.getOriginalText(), part2.getState(), part2.getType())
                , containsInAnyOrder(Arrays.asList("2 yr 8 m", UNRESOLVED, DURATION).toArray()));
        PartValue valueOfPart2 = ((List<PartValue>) part2.getValue()).get(0);
        assertThat(Arrays.asList(valueOfPart2.getValue(), valueOfPart2.getLabel(), valueOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(2, "Years", INT).toArray()));
        PartValue valueOfPart3 = ((List<PartValue>) part2.getValue()).get(1);
        assertThat(Arrays.asList(valueOfPart3.getValue(), valueOfPart3.getLabel(), valueOfPart3.getType())
                , containsInAnyOrder(Arrays.asList(8, "Months", INT).toArray()));
        assertThat(Arrays.asList(part4.getValue(), part4.getState(), part4.getType(), part4.getLabel())
                , containsInAnyOrder(Arrays.asList(true, UNRESOLVED, BOOLEAN, "Concurrent").toArray()));
    }

    @Test
    public void buildFromKnowledge4() throws Exception {
        List<Part> parts = new PartsResolver().getParts("2 yr m imp 18 $20 23:23 3/3/1980 [2y]");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        Part p2 = result.getParts().get(1);
        Part p3 = result.getParts().get(2);
        Part p4 = result.getParts().get(3);
        Part p5 = result.getParts().get(4);
        Part p6 = result.getParts().get(5);
        Part p7 = result.getParts().get(6);
        Part p8 = result.getParts().get(7);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("O")
        );
        assertThat(result.getParts().size()
                , is(8)
        );
        assertThat(Arrays.asList(p3.getValue(), p3.getState(), p3.getType())
                , containsInAnyOrder(Arrays.asList("Imprisonment", RESOLVED, RESULT).toArray()));
        PartValue valueOfPart1 = ((List<PartValue>) p1.getValue()).get(0);
        assertThat(Arrays.asList(valueOfPart1.getValue(), valueOfPart1.getLabel(), valueOfPart1.getType())
                , containsInAnyOrder(Arrays.asList(2, "Years", INT).toArray()));
        assertThat(Arrays.asList(p2.getValue(), p2.getState(), p2.getType())
                , containsInAnyOrder(Arrays.asList("m", UNRESOLVED, TXT).toArray()));
        assertThat(Arrays.asList(p4.getValue(), p4.getState(), p4.getType())
                , containsInAnyOrder(Arrays.asList(18, UNRESOLVED, INT).toArray()));
        assertThat(Arrays.asList(p5.getValue(), p5.getState(), p5.getType())
                , containsInAnyOrder(Arrays.asList("$20", UNRESOLVED, TXT).toArray()));
        assertThat(Arrays.asList(p6.getValue(), p6.getState(), p6.getType())
                , containsInAnyOrder(Arrays.asList("23:23", UNRESOLVED, TIME).toArray()));
        assertThat(Arrays.asList(p7.getValue(), p7.getState(), p7.getType())
                , containsInAnyOrder(Arrays.asList("3/3/1980", UNRESOLVED, DATE).toArray()));
        assertThat(Arrays.asList(p8.getValue(), p8.getState(), p8.getType())
                , containsInAnyOrder(Arrays.asList("[2y]", UNRESOLVED, TXT).toArray()));
    }

    @Test
    public void buildFromKnowledge5() throws Exception {
        List<Part> parts = new PartsResolver().getParts("alc req conc 78 £2,000 2 mo ");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        Part p2 = result.getParts().get(1);
        Part p3 = result.getParts().get(2);
        Part p4 = result.getParts().get(3);
        Part p5 = result.getParts().get(4);
        Part p6 = result.getParts().get(5);
        assertThat(result.getResultCode() == null
                , is(true)
        );
        assertThat(result.getResultLevel() == null
                , is(true)
        );
        assertThat(result.getParts().size()
                , is(6)
        );
        assertThat(Arrays.asList(p3.getValue(), p3.getState(), p3.getType())
                , containsInAnyOrder(Arrays.asList(true, UNRESOLVED, BOOLEAN).toArray()));
        ResultChoice resultChoice = p1.getResultChoices().stream().filter(v -> v.getLevel().equals("O")).findFirst().get();
        assertThat(Arrays.asList(p1.getValue(), p1.getState(), p1.getResultChoices().size(), resultChoice.getCode().length(), resultChoice.getLabel().length() > 0, resultChoice.getType() != null)
                , containsInAnyOrder(Arrays.asList("alc", UNRESOLVED, 5, 36, true, true).toArray()));
        assertThat(Arrays.asList(p2.getValue(), p2.getState(), p2.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("req", UNRESOLVED, 11).toArray()));
        assertThat(Arrays.asList(p4.getValue(), p4.getState(), p4.getType())
                , containsInAnyOrder(Arrays.asList(78, UNRESOLVED, INT).toArray()));
        assertThat(Arrays.asList( p5.getState(), p5.getValue(), p5.getType())
                , containsInAnyOrder(Arrays.asList( UNRESOLVED,"2,000", CURR).toArray()));
        assertThat(Arrays.asList(p6.getState(), p6.getType())
                , containsInAnyOrder(Arrays.asList(UNRESOLVED, DURATION).toArray()));
    }

    @Test
    public void buildFromKnowledgeHavingDuplicateResultDefinitionMapping() throws Exception {
        List<Part> parts = new PartsResolver().getParts("Curfew");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        assertThat(result.getResultCode() == null
                , is(true)
        );
        assertThat(result.getResultLevel() == null
                , is(true)
        );
        assertThat(result.getParts().size()
                , is(1)
        );
        assertThat(Arrays.asList(p1.getValue(), p1.getState(), p1.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("Curfew", UNRESOLVED, 7).toArray()));

    }

    @Test
    public void buildFromKnowledgeWithSameKeywordCountHavingMutipleResult() throws Exception {
        List<Part> parts = new PartsResolver().getParts("resTr Ord prd Fur");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        Part p2 = result.getParts().get(1);
        Part p3 = result.getParts().get(2);
        Part p4 = result.getParts().get(3);
        assertThat(result.getResultCode() == null
                , is(true)
        );
        assertThat(result.getResultLevel() == null
                , is(true)
        );
        assertThat(result.getParts().size()
                , is(4)
        );
        assertThat(Arrays.asList(p1.getValue(), p1.getState(), p1.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("resTr", UNRESOLVED, 2).toArray()));
        assertThat(Arrays.asList(p2.getValue(), p2.getState(), p2.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("Ord", UNRESOLVED, 15).toArray()));
        assertThat(Arrays.asList(p3.getValue(), p3.getState(), p3.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("prd", UNRESOLVED, 10).toArray()));
        assertThat(Arrays.asList(p4.getValue(), p4.getState(), p4.getResultChoices().size())
                , containsInAnyOrder(Arrays.asList("Fur", UNRESOLVED, 2).toArray()));

    }

    @Test
    public void buildFromKnowledgeKeywordCountHavingOneResult() throws Exception {
        List<Part> parts = new PartsResolver().getParts("resT Ord prd Furth");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        Part p2 = result.getParts().get(1);
        Part p3 = result.getParts().get(2);
        Part p4 = result.getParts().get(3);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("O")
        );
        assertThat(result.getParts().size()
                , is(4)
        );
        assertThat(Arrays.asList(p1.getValue(), p1.getState(), p1.getResultChoices())
                , containsInAnyOrder(Arrays.asList("Restraining order for period", RESOLVED, null).toArray()));
        assertThat(Arrays.asList(p2.getValue(), p2.getState(), p2.getType(), p2.getResultChoices())
                , containsInAnyOrder(Arrays.asList("Ord", RESOLVED, IGNORED, null).toArray()));
        assertThat(Arrays.asList(p3.getValue(), p2.getState(), p3.getType(), p3.getResultChoices())
                , containsInAnyOrder(Arrays.asList("prd", RESOLVED, IGNORED, null).toArray()));
        assertThat(Arrays.asList(p4.getValue(), p4.getState(), p4.getType())
                , containsInAnyOrder(Arrays.asList("Furth", UNRESOLVED, TXT).toArray()));

    }

    @Test
    public void buildFromKnowledge6() throws Exception {
        List<Part> parts = new PartsResolver().getParts("imp 2 y 8 m sus 6 y conc 7 y 8 m 89 w 90 d");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part part1 = result.getParts().get(0);
        Part part2 = result.getParts().get(1);
        Part part3 = result.getParts().get(2);
        Part part4 = result.getParts().get(3);
        Part part5 = result.getParts().get(4);
        Part part6 = result.getParts().get(5);
        assertThat(result.getResultCode().length()
                , is(36)
        );
        assertThat(result.getResultLevel()
                , is("O")
        );
        assertThat(result.getParts().size()
                , is(6)
        );
        assertThat(Arrays.asList(part1.getValue(), part1.getState(), part1.getType())
                , containsInAnyOrder(Arrays.asList("Suspended sentence order - imprisonment", RESOLVED, RESULT).toArray()));
        assertThat(Arrays.asList(part2.getOriginalText(), part2.getState(), part2.getType())
                , containsInAnyOrder(Arrays.asList("2 y 8 m", UNRESOLVED, DURATION).toArray()));
        PartValue valueYearOfPart2 = ((List<PartValue>) part2.getValue()).get(0);
        assertThat(Arrays.asList(valueYearOfPart2.getValue(), valueYearOfPart2.getLabel(), valueYearOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(2, "Years", INT).toArray()));
        PartValue valueMonthOfPart2 = ((List<PartValue>) part2.getValue()).get(1);
        assertThat(Arrays.asList(valueMonthOfPart2.getValue(), valueMonthOfPart2.getLabel(), valueMonthOfPart2.getType())
                , containsInAnyOrder(Arrays.asList(8, "Months", INT).toArray()));
        assertThat(Arrays.asList(part3.getValue(), part3.getState(), part3.getType())
                , containsInAnyOrder(Arrays.asList("sus", RESOLVED, IGNORED).toArray()));
        PartValue valueYearOfPart4 = ((List<PartValue>) part4.getValue()).get(0);
        assertThat(Arrays.asList(valueYearOfPart4.getValue(), valueYearOfPart4.getLabel(), valueYearOfPart4.getType())
                , containsInAnyOrder(Arrays.asList(6, "Years", INT).toArray()));
        assertThat(Arrays.asList(part5.getValue(), part5.getState(), part5.getType(), part5.getLabel())
                , containsInAnyOrder(Arrays.asList(true, UNRESOLVED, BOOLEAN, "Concurrent").toArray()));
        PartValue valueYearOfPart5 = ((List<PartValue>) part6.getValue()).get(0);
        assertThat(Arrays.asList(valueYearOfPart5.getValue(), valueYearOfPart5.getLabel(), valueYearOfPart5.getType())
                , containsInAnyOrder(Arrays.asList(7, "Years", INT).toArray()));
        PartValue valueMonthOfPart5 = ((List<PartValue>) part6.getValue()).get(1);
        assertThat(Arrays.asList(valueMonthOfPart5.getValue(), valueMonthOfPart5.getLabel(), valueMonthOfPart5.getType())
                , containsInAnyOrder(Arrays.asList(8, "Months", INT).toArray()));
        PartValue valueWeekOfPart5 = ((List<PartValue>) part6.getValue()).get(2);
        assertThat(Arrays.asList(valueWeekOfPart5.getValue(), valueWeekOfPart5.getLabel(), valueWeekOfPart5.getType())
                , containsInAnyOrder(Arrays.asList(89, "Weeks", INT).toArray()));
    }

    @Test
    public void testWhenNoResultFoundAllPartsShouldTXT() throws Exception {
        List<Part> parts = new PartsResolver().getParts("aaaAAaaaa 34 £23.00 conc");
        Knowledge knowledge = processor.processParts(parts.stream().map(Part::getValueAsString).collect(Collectors.toList()));

        ResultDefinitionView result = testObj.buildFromKnowledge(parts, knowledge);

        Part p1 = result.getParts().get(0);
        Part p2 = result.getParts().get(1);
        Part p3 = result.getParts().get(2);
        Part p4 = result.getParts().get(3);
        assertThat(result.getResultCode() == null
                , is(true)
        );
        assertThat(result.getResultLevel() == null
                , is(true)
        );
        assertThat(result.getParts().size()
                , is(4)
        );

        assertThat(Arrays.asList(p1.getValue(), p2.getState(), p3.getType())
                , containsInAnyOrder(Arrays.asList("aaaAAaaaa", UNRESOLVED, TXT).toArray()));
        assertThat(Arrays.asList( p2.getState(), p2.getValue(), p2.getType())
                , containsInAnyOrder(Arrays.asList( UNRESOLVED,"34", TXT).toArray()));
        assertThat(Arrays.asList(p3.getState(), p3.getType(),p3.getValue())
                , containsInAnyOrder(Arrays.asList(UNRESOLVED, TXT, "£23.00").toArray()));
        assertThat(Arrays.asList(p4.getState(), p4.getType(),p4.getValue())
                , containsInAnyOrder(Arrays.asList(UNRESOLVED, TXT, "conc").toArray()));
    }
}