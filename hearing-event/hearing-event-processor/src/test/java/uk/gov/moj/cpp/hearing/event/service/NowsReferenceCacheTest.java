package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceCacheTest {

    @Mock
    NowsReferenceDataLoader nowsReferenceDataLoader;

    @InjectMocks
    NowsReferenceCache target;

    final AllNows allNows = new AllNows();
    final ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setId(UUID.randomUUID());
    final AllResultDefinitions allResultDefinitions = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
            Arrays.asList(resultDefinition)
    );

    @Before
    public void before() {
        Mockito.when(nowsReferenceDataLoader.loadAllNowsReference(Mockito.any())).
                thenReturn(allNows);
        Mockito.when(nowsReferenceDataLoader.loadAllResultDefinitions(Mockito.any())).
                thenReturn(allResultDefinitions);
    }


    @Test
    public void testNowsDataLoad() throws Exception {
        target.reload();
        assertThat(target.getAllNows(), is(allNows));
    }

    @Test
    public void testResultDefinitionLoad() throws Exception {
        ResultDefinition result = target.getResultDefinitionById(resultDefinition.getId());
        assertThat(result, is(resultDefinition));
    }


    @Test
    public void testNowsDataLoadSucceed() throws Exception {
        AllNows allNowsIn = new AllNows();
        Mockito.when(nowsReferenceDataLoader.loadAllNowsReference(Mockito.any())).thenReturn(allNowsIn);
        target.reload();
        AllNows allNowsOut = target.getAllNows();
        Assert.assertTrue(allNowsIn == allNowsOut);
    }


}
