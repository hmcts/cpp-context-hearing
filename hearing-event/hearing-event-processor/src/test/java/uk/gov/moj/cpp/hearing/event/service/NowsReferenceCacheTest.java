package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.handler.exception.MissingHandlerException;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceCacheTest {

    @Mock
    NowsReferenceDataLoader nowsReferenceDataLoader;

    @InjectMocks
    NowsReferenceCache target;

    @Test
    public void testNowsDataLoadFail() throws Exception{
        Mockito.when(nowsReferenceDataLoader.loadAllNowsReference(Mockito.any())).thenThrow(new MissingHandlerException("Refereence Service Not yet available"));
        target.reload();
        AllNows allNows = target.getAllNows();
        Assert.assertNotNull(allNows);
    }

    @Test
    public void testResultDefinitionLoadFail() throws Exception{
        Mockito.when(nowsReferenceDataLoader.getResultDefinitionById(Mockito.any())).thenThrow(new MissingHandlerException("Reference Service Not yet available"));
        ResultDefinition result =  target.getResultDefinitionById((new DefaultNowsReferenceData()).resultDefinitionId1);
        Assert.assertNotNull(result);
    }


    @Test
    public void testNowsDataLoadSucceed() throws Exception{
        AllNows allNowsIn = new AllNows();
        Mockito.when(nowsReferenceDataLoader.loadAllNowsReference(Mockito.any())).thenReturn(allNowsIn);
        target.reload();
        AllNows allNowsOut = target.getAllNows();
        Assert.assertTrue(allNowsIn==allNowsOut);
    }


}
