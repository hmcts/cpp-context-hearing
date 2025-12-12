package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PromptJPAMapperTest {

    @InjectMocks
    private PromptJPAMapper promptJPAMapper = new PromptJPAMapper();


    @Test
    public void testFromJPA() {
        Prompt prompt = new Prompt();
        prompt.setId(UUID.randomUUID());
        prompt.setFixedListCode(RandomGenerator.STRING.next());
        prompt.setLabel(RandomGenerator.STRING.next());
        prompt.setValue(RandomGenerator.STRING.next());
        prompt.setWelshValue(RandomGenerator.STRING.next());

        assertThat(promptJPAMapper.fromJPA(prompt), isBean(uk.gov.justice.core.courts.Prompt.class)
                .with(uk.gov.justice.core.courts.Prompt::getId, is(prompt.getId()))
                .with(uk.gov.justice.core.courts.Prompt::getFixedListCode, is(prompt.getFixedListCode()))
                .with(uk.gov.justice.core.courts.Prompt::getLabel, is(prompt.getLabel()))
                .with(uk.gov.justice.core.courts.Prompt::getValue, is(prompt.getValue()))
                .with(uk.gov.justice.core.courts.Prompt::getWelshValue, is(prompt.getWelshValue()))
        );

    }

    @Test
    public void testToJPA() {
        ResultLine resultLine = mock(ResultLine.class);
        uk.gov.justice.core.courts.Prompt prompt = uk.gov.justice.core.courts.Prompt.prompt()
                .withId(UUID.randomUUID())
                .withFixedListCode(RandomGenerator.STRING.next())
                .withLabel(RandomGenerator.STRING.next())
                .withValue(RandomGenerator.STRING.next())
                .withWelshValue(RandomGenerator.STRING.next())
                .build();

        assertThat(promptJPAMapper.toJPA(resultLine, prompt), isBean(Prompt.class)
                .with(Prompt::getId, is(prompt.getId()))
                .with(Prompt::getFixedListCode, is(prompt.getFixedListCode()))
                .with(Prompt::getLabel, is(prompt.getLabel()))
                .with(Prompt::getValue, is(prompt.getValue()))
                .with(Prompt::getWelshValue, is(prompt.getWelshValue()))
                .with(Prompt::getResultLine, is(resultLine))
        );


    }

}