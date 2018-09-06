package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;


public class TargetJPAMapperTest {

    private TargetJPAMapper mapperUnderTest = new TargetJPAMapper(new ResultLineJPAMapper(new PromptJPAMapper(), new DelegatedPowersJPAMapper()));

    private Target getTestJPATarget() {
        return aNewHearingJPADataTemplate().getHearing().getTargets().get(0);
    }

    private uk.gov.justice.json.schemas.core.Target getTestPojoTarget() {
        return mapperUnderTest.fromJPA(getTestJPATarget());
    }


    @Test
    public void testFromJPA() {
        Target targetIn = getTestJPATarget();
        uk.gov.justice.json.schemas.core.Target targetOut = mapperUnderTest.fromJPA(targetIn);
        assertThat(targetOut.getDefendantId(), is(targetIn.getDefendantId()));
        assertThat(targetOut.getDraftResult(), is(targetIn.getDraftResult()));
        assertThat(targetOut.getHearingId(), is(targetIn.getHearing().getId()));
        assertThat(targetOut.getOffenceId(), is(targetIn.getOffenceId()));
        assertThat(targetOut.getTargetId(), is(targetIn.getId()));
        //NOT testing child relations fully - tested elsewhere
        assertThat(targetOut.getResultLines().size(), is(targetIn.getResultLines().size()));
        assertThat(targetOut.getResultLines().get(0).getResultLineId(), is(targetIn.getResultLines().get(0).getId()));
    }

    @Test
    public void testToJPA() {
        uk.gov.justice.json.schemas.core.Target targetIn = getTestPojoTarget();
        Hearing hearingIn = (new Hearing().setId(targetIn.getHearingId()));
        Target targetOut = mapperUnderTest.toJPA(hearingIn, targetIn);
        assertThat(targetOut.getDefendantId(), is(targetIn.getDefendantId()));
        assertThat(targetOut.getDraftResult(), is(targetIn.getDraftResult()));
        assertThat(targetOut.getHearing(), is(hearingIn));
        assertThat(targetOut.getOffenceId(), is(targetIn.getOffenceId()));
        //NOT testing child relations fully - tested elsewhere
        assertThat(targetOut.getResultLines().size(), is(targetIn.getResultLines().size()));
        assertThat(targetOut.getResultLines().get(0).getId(), is(targetIn.getResultLines().get(0).getResultLineId()));
    }

}