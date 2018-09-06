package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;

import java.util.UUID;


public class PromptJPAMapperTest {

    private PromptJPAMapper mapperUnderTest = new PromptJPAMapper();

    private Prompt getTestJPAPrompt() {
        return aNewHearingJPADataTemplate().getHearing().getTargets().get(0).getResultLines().get(0).getPrompts().get(0);
    }

    private uk.gov.justice.json.schemas.core.Prompt getTestPojoPrompt() {
        return mapperUnderTest.fromJPA(getTestJPAPrompt());
    }

    @Test
    public void testFromJPA() {
        Prompt promptIn = getTestJPAPrompt();
        uk.gov.justice.json.schemas.core.Prompt promptOut = mapperUnderTest.fromJPA(promptIn);
        assertThat(promptOut.getFixedListCode(), is(promptIn.getFixedListCode()));
        assertThat(promptOut.getId(), is(promptIn.getId()));
        assertThat(promptOut.getLabel(), is(promptIn.getLabel()));
        assertThat(promptOut.getValue(), is(promptIn.getValue()));
        assertThat(promptOut.getWelshValue(), is(promptIn.getWelshValue()));
    }

    @Test
    public void testToJPA() {
        uk.gov.justice.json.schemas.core.Prompt promptIn = getTestPojoPrompt();
        ResultLine resultLineIn = (new ResultLine().setId(UUID.randomUUID()));
        Prompt promptOut = mapperUnderTest.toJPA(resultLineIn, promptIn);
        assertThat(promptOut.getFixedListCode(), is(promptIn.getFixedListCode()));
        assertThat(promptOut.getId(), is(promptIn.getId()));
        assertThat(promptOut.getLabel(), is(promptIn.getLabel()));
        assertThat(promptOut.getValue(), is(promptIn.getValue()));
        assertThat(promptOut.getWelshValue(), is(promptIn.getWelshValue()));
        assertThat(promptOut.getResultLine(), is(resultLineIn));
    }

}