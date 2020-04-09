package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import uk.gov.moj.cpp.hearing.query.view.service.ReferenceDataService;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitJudiciaryCacheTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private XhibitJudiciaryCache xhibitJudiciaryCache;


    @Test
    public void shouldGetJudiciaryName() {
        final UUID judiciaryId = UUID.randomUUID();
        when(referenceDataService.getJudiciaryFullName(judiciaryId)).thenReturn("Joe Doe");
        final String fullName = xhibitJudiciaryCache.getJudiciaryName(judiciaryId);
        assertThat(fullName, is("Joe Doe"));
        verify(referenceDataService, times(1)).getJudiciaryFullName(judiciaryId);
        xhibitJudiciaryCache.getJudiciaryName(judiciaryId);
        verifyNoMoreInteractions(referenceDataService);
    }
}
