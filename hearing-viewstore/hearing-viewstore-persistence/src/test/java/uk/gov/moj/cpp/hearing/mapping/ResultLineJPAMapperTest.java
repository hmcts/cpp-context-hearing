package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.jgroups.protocols.RACKSPACE_PING;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.Level;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ResultLineJPAMapperTest {

    @Mock
    private PromptJPAMapper promptJPAMapper;

    @Mock
    private DelegatedPowersJPAMapper delegatedPowersJPAMapper;

    @InjectMocks
    private ResultLineJPAMapper resultLineJPAMapper;

    @Test
    public void testFromJPA() {

        ResultLine resultLine = new ResultLine();
        resultLine.setId(UUID.randomUUID());
        resultLine.setTarget(mock(Target.class));
        resultLine.setDelegatedPowers(mock(DelegatedPowers.class));
        resultLine.setComplete(RandomGenerator.BOOLEAN.next());
        resultLine.setModified(RandomGenerator.BOOLEAN.next());
        resultLine.setLevel(RandomGenerator.values(Level.values()).next());
        resultLine.setOrderedDate(RandomGenerator.PAST_LOCAL_DATE.next());
        resultLine.setPrompts(asSet(mock(Prompt.class)));
        resultLine.setResultDefinitionId(UUID.randomUUID());
        resultLine.setResultLabel(RandomGenerator.STRING.next());
        resultLine.setSharedDate(RandomGenerator.PAST_LOCAL_DATE.next());

        uk.gov.justice.json.schemas.core.Prompt promptMock = mock(uk.gov.justice.json.schemas.core.Prompt.class);
        when(promptJPAMapper.fromJPA(resultLine.getPrompts())).thenReturn(asList(promptMock));

        uk.gov.justice.json.schemas.core.DelegatedPowers delegatedPowersMock = mock(uk.gov.justice.json.schemas.core.DelegatedPowers.class);
        when(delegatedPowersJPAMapper.fromJPA(resultLine.getDelegatedPowers())).thenReturn(delegatedPowersMock);

        assertThat(resultLineJPAMapper.fromJPA(resultLine), isBean(uk.gov.justice.json.schemas.core.ResultLine.class)
                .with(uk.gov.justice.json.schemas.core.ResultLine::getResultLineId, is(resultLine.getId()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getDelegatedPowers, is(delegatedPowersMock))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getIsComplete, is(resultLine.getComplete()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getIsModified, is(resultLine.getModified()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getLevel, is(resultLine.getLevel()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getOrderedDate, is(resultLine.getOrderedDate()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getPrompts, first(is(promptMock)))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getResultDefinitionId, is(resultLine.getResultDefinitionId()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getResultLabel, is(resultLine.getResultLabel()))
                .with(uk.gov.justice.json.schemas.core.ResultLine::getSharedDate, is(resultLine.getSharedDate()))
        );
    }

    @Test
    public void testToJPA() {

        Target target = mock(Target.class);

        uk.gov.justice.json.schemas.core.ResultLine resultLine = uk.gov.justice.json.schemas.core.ResultLine.resultLine()
                .withResultLineId(UUID.randomUUID())
                .withDelegatedPowers(mock(uk.gov.justice.json.schemas.core.DelegatedPowers.class))
                .withIsComplete(RandomGenerator.BOOLEAN.next())
                .withIsModified(RandomGenerator.BOOLEAN.next())
                .withLevel(RandomGenerator.values(Level.values()).next())
                .withOrderedDate(RandomGenerator.PAST_LOCAL_DATE.next())
                .withPrompts(asList(mock(uk.gov.justice.json.schemas.core.Prompt.class)))
                .withResultDefinitionId(UUID.randomUUID())
                .withResultLabel(RandomGenerator.STRING.next())
                .withSharedDate(RandomGenerator.PAST_LOCAL_DATE.next())
                .build();

        DelegatedPowers delegatedPowersMock = mock(DelegatedPowers.class);
        when(delegatedPowersJPAMapper.toJPA(resultLine.getDelegatedPowers())).thenReturn(delegatedPowersMock);

        Prompt prompt = mock(Prompt.class);
        when(promptJPAMapper.toJPA(any(), eq(resultLine.getPrompts()))).thenReturn(asSet(prompt));

        assertThat(resultLineJPAMapper.toJPA(target, resultLine), isBean(ResultLine.class)
                .with(ResultLine::getId, is(resultLine.getResultLineId()))
                .with(ResultLine::getDelegatedPowers, is(delegatedPowersMock))
                .with(ResultLine::getComplete, is(resultLine.getIsComplete()))
                .with(ResultLine::getModified, is(resultLine.getIsModified()))
                .with(ResultLine::getLevel, is(resultLine.getLevel()))
                .with(ResultLine::getOrderedDate, is(resultLine.getOrderedDate()))
                .with(ResultLine::getPrompts, first(is(prompt)))
                .with(ResultLine::getResultDefinitionId, is(resultLine.getResultDefinitionId()))
                .with(ResultLine::getResultLabel, is(resultLine.getResultLabel()))
                .with(ResultLine::getSharedDate, is(resultLine.getSharedDate()))
        );
    }

}