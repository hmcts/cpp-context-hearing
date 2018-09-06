package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.UUID;


public class ResultLineJPAMapperTest {

    private ResultLineJPAMapper mapperUnderTest = new ResultLineJPAMapper(new PromptJPAMapper(), new DelegatedPowersJPAMapper());

    private ResultLine getTestJPAResultLine() {
        return aNewHearingJPADataTemplate().getHearing().getTargets().get(0).getResultLines().get(0);
    }

    private uk.gov.justice.json.schemas.core.ResultLine getTestPojoResultLine() {
        return mapperUnderTest.fromJPA(getTestJPAResultLine());
    }


    @Test
    public void testFromJPA() {
        ResultLine resultLineIn = getTestJPAResultLine();
        uk.gov.justice.json.schemas.core.ResultLine resultLineOut = mapperUnderTest.fromJPA(resultLineIn);
        assertThat(resultLineOut.getResultLabel(), is(resultLineIn.getResultLabel()));
        assertThat(resultLineOut.getIsComplete(), is(resultLineIn.getComplete()));
        assertThat(resultLineOut.getResultLineId(), is(resultLineIn.getId()));
        assertThat(resultLineOut.getIsModified(), is(resultLineIn.getModified()));
        assertThat(resultLineOut.getLevel(), is(resultLineIn.getLevel()));
        assertThat(resultLineOut.getOrderedDate(), is(resultLineIn.getOrderedDate()));
        assertThat(resultLineOut.getResultDefinitionId(), is(resultLineIn.getResultDefinitionId()));
        assertThat(resultLineOut.getSharedDate(), is(resultLineIn.getSharedDate()));
        //NOT testing child relations fully - tested elsewhere
        assertThat(resultLineOut.getPrompts().size(), is(resultLineIn.getPrompts().size()));
        assertThat(resultLineOut.getPrompts().get(0).getId(), is(resultLineIn.getPrompts().get(0).getId()));
        assertThat(resultLineOut.getDelegatedPowers().getUserId(), is(resultLineIn.getDelegatedPowers().getDelegatedPowersUserId()));
    }

    @Test
    public void testToJPA() {
        uk.gov.justice.json.schemas.core.ResultLine resultLineIn = getTestPojoResultLine();
        Target targetIn = (new Target().setId(UUID.randomUUID()));
        ResultLine resultLineOut = mapperUnderTest.toJPA(targetIn, resultLineIn);
        assertThat(resultLineOut.getResultLabel(), is(resultLineIn.getResultLabel()));
        assertThat(resultLineOut.getComplete(), is(resultLineIn.getIsComplete()));
        assertThat(resultLineOut.getId(), is(resultLineIn.getResultLineId()));
        assertThat(resultLineOut.getModified(), is(resultLineIn.getIsModified()));
        assertThat(resultLineOut.getLevel(), is(resultLineIn.getLevel()));
        assertThat(resultLineOut.getOrderedDate(), is(resultLineIn.getOrderedDate()));
        assertThat(resultLineOut.getResultDefinitionId(), is(resultLineIn.getResultDefinitionId()));
        assertThat(resultLineOut.getSharedDate(), is(resultLineIn.getSharedDate()));
        //NOT testing child relations fully - tested elsewhere
        assertThat(resultLineOut.getPrompts().size(), is(resultLineIn.getPrompts().size()));
        assertThat(resultLineOut.getPrompts().get(0).getId(), is(resultLineIn.getPrompts().get(0).getId()));
        assertThat(resultLineOut.getDelegatedPowers().getDelegatedPowersUserId(), is(resultLineIn.getDelegatedPowers().getUserId()));
        assertThat(resultLineOut.getTarget(), is(targetIn));
    }

}