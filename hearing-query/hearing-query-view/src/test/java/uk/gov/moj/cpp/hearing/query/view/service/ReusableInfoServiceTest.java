package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ReusableInfo;
import uk.gov.moj.cpp.hearing.repository.ReusableInfoRepository;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReusableInfoServiceTest {

    @Mock
    private ReusableInfoRepository reusableInfoRepository;

    @InjectMocks
    private ReusableInfoService reusableInfoService;

    @Test
    public void shouldGetReusableInfoForDefendantsIfDefendantListIsEmpty() {
        final List<ReusableInfo> result = reusableInfoService.getReusableInfoForDefendants(emptyList());
        verifyNoMoreInteractions(reusableInfoRepository);
        assertThat(result, is(emptyList()));
    }
}
