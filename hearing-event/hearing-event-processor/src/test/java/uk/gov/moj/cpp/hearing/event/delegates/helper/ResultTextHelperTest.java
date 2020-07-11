package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.System.lineSeparator;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class ResultTextHelperTest {

    @Test
    public void shouldSortPrompts() {


        final UUID uuid0 = randomUUID();
        final UUID uuid1 = randomUUID();
        final UUID uuid2 = randomUUID();
        final UUID uuid3 = randomUUID();

        final Prompt prompt0 = Prompt.prompt().setSequence(3).setId(uuid0).setHidden(false);
        final Prompt prompt1 = Prompt.prompt().setSequence(4).setId(uuid1).setHidden(false);
        final Prompt prompt2 = Prompt.prompt().setSequence(1).setId(uuid2);
        final Prompt prompt3 = Prompt.prompt().setSequence(2).setId(uuid3);


        final List<Prompt> resultDefinitionPrompts = new ArrayList<>();
        resultDefinitionPrompts.add(prompt0);
        resultDefinitionPrompts.add(prompt1);
        resultDefinitionPrompts.add(prompt2);
        resultDefinitionPrompts.add(prompt3);

        final uk.gov.justice.core.courts.Prompt resultLinePromp0 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label1").withValue("value1").withId(uuid0).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp1 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label2").withValue("value2").withId(uuid1).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp2 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label3").withValue("value3").withId(uuid2).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp3 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label4").withValue("value4").withId(uuid3).build();

        final List<uk.gov.justice.core.courts.Prompt> resultLinePrompts = new ArrayList<>();
        resultLinePrompts.add(resultLinePromp0);
        resultLinePrompts.add(resultLinePromp1);
        resultLinePrompts.add(resultLinePromp2);
        resultLinePrompts.add(resultLinePromp3);

        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setLabel("Result definition label").setPrompts(resultDefinitionPrompts);
        final ResultLine resultLine = ResultLine.resultLine().withPrompts(resultLinePrompts).build();

        final String result = ResultTextHelper.getResultText(resultDefinition, resultLine);

        assertThat(result, is(String.format("Result definition label%slabel3 value3%slabel4 value4%slabel1 value1%slabel2 value2", lineSeparator(), lineSeparator(), lineSeparator(), lineSeparator())));
    }

    @Test
    public void shouldNotDisplayResultTextForHiddenPrompts() {

        final UUID uuid0 = randomUUID();
        final UUID uuid1 = randomUUID();
        final UUID uuid2 = randomUUID();
        final UUID uuid3 = randomUUID();

        final Prompt prompt0 = Prompt.prompt().setSequence(3).setId(uuid0);
        final Prompt prompt1 = Prompt.prompt().setSequence(4).setId(uuid1).setHidden(true);
        final Prompt prompt2 = Prompt.prompt().setSequence(1).setId(uuid2).setHidden(false);
        final Prompt prompt3 = Prompt.prompt().setSequence(2).setId(uuid3);


        final List<Prompt> resultDefinitionPrompts = new ArrayList<>();
        resultDefinitionPrompts.add(prompt0);
        resultDefinitionPrompts.add(prompt1);
        resultDefinitionPrompts.add(prompt2);
        resultDefinitionPrompts.add(prompt3);

        final uk.gov.justice.core.courts.Prompt resultLinePromp0 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label1").withValue("value1").withId(uuid0).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp1 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label2").withValue("value2").withId(uuid1).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp2 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label3").withValue("value3").withId(uuid2).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp3 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label4").withValue("value4").withId(uuid3).build();

        final List<uk.gov.justice.core.courts.Prompt> resultLinePrompts = new ArrayList<>();
        resultLinePrompts.add(resultLinePromp0);
        resultLinePrompts.add(resultLinePromp1);
        resultLinePrompts.add(resultLinePromp2);
        resultLinePrompts.add(resultLinePromp3);

        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setLabel("Result definition label").setPrompts(resultDefinitionPrompts);
        final ResultLine resultLine = ResultLine.resultLine().withPrompts(resultLinePrompts).build();

        final String result = ResultTextHelper.getResultText(resultDefinition, resultLine);

        assertThat(result, is(String.format("Result definition label%slabel3 value3%slabel4 value4%slabel1 value1", lineSeparator(), lineSeparator(), lineSeparator(), lineSeparator())));
    }

    @Test
    public void shouldDisplayEnglishValuesForBooleanPompts() {

        final UUID uuid0 = randomUUID();
        final UUID uuid1 = randomUUID();
        final UUID uuid2 = randomUUID();
        final UUID uuid3 = randomUUID();

        final Prompt prompt0 = Prompt.prompt().setSequence(3).setId(uuid0).setType("BOOLEAN");
        final Prompt prompt1 = Prompt.prompt().setSequence(4).setId(uuid1).setHidden(false).setType("BOOLEAN");
        final Prompt prompt2 = Prompt.prompt().setSequence(1).setId(uuid2).setHidden(false).setType("BOOLEAN");
        final Prompt prompt3 = Prompt.prompt().setSequence(2).setId(uuid3).setType("TEXT");


        final List<Prompt> resultDefinitionPrompts = new ArrayList<>();
        resultDefinitionPrompts.add(prompt0);
        resultDefinitionPrompts.add(prompt1);
        resultDefinitionPrompts.add(prompt2);
        resultDefinitionPrompts.add(prompt3);

        final uk.gov.justice.core.courts.Prompt resultLinePromp0 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label1").withValue("true").withId(uuid0).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp1 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label2").withValue("value2").withId(uuid1).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp2 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label3").withValue("false").withId(uuid2).build();
        final uk.gov.justice.core.courts.Prompt resultLinePromp3 = new uk.gov.justice.core.courts.Prompt.Builder().withLabel("label4").withValue("value4").withId(uuid3).build();

        final List<uk.gov.justice.core.courts.Prompt> resultLinePrompts = new ArrayList<>();
        resultLinePrompts.add(resultLinePromp0);
        resultLinePrompts.add(resultLinePromp1);
        resultLinePrompts.add(resultLinePromp2);
        resultLinePrompts.add(resultLinePromp3);

        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setLabel("Result definition label").setPrompts(resultDefinitionPrompts);
        final ResultLine resultLine = ResultLine.resultLine().withPrompts(resultLinePrompts).build();

        final String result = ResultTextHelper.getResultText(resultDefinition, resultLine);

        assertThat(result, is(String.format("Result definition label%slabel3 No%slabel4 value4%slabel1 Yes%slabel2 No", lineSeparator(), lineSeparator(), lineSeparator(), lineSeparator())));

    }
}