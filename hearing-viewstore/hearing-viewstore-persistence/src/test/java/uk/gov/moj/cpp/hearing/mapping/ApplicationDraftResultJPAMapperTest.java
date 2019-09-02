package uk.gov.moj.cpp.hearing.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDraftResultJPAMapperTest {


    @InjectMocks
    private ApplicationDraftResultJPAMapper applicationDraftResultJPAMapper;

    @Test
    public void testToJPA() {

        Hearing hearing = mock(Hearing.class);
        UUID targetId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        String draftResult = "draft result";


        assertThat(applicationDraftResultJPAMapper.toJPA(hearing, targetId, applicationId, draftResult), isBean(ApplicationDraftResult.class)
                .with(ApplicationDraftResult::getId, is(targetId))
                .with(ApplicationDraftResult::getApplicationId, is(applicationId))
                .with(ApplicationDraftResult::getHearing, is(hearing))
                .with(ApplicationDraftResult::getDraftResult, is(draftResult))
        );
    }
}