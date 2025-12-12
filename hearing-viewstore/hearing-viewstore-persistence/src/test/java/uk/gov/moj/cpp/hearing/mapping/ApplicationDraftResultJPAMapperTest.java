package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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