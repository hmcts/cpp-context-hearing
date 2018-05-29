package uk.gov.moj.cpp.hearing.event.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;

import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.function.Function;

@RunWith(MockitoJUnitRunner.class)
public class NowsReferenceDataLoaderTester {

    @InjectMocks
    NowsReferenceDataLoader target;

    @Mock
    private Requester requester;

    @Mock
    private Enveloper enveloper;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Test
    public void testLoadAllData() {
        JsonEnvelope requestEnvelope = Mockito.mock(JsonEnvelope.class);
        Function<Object, JsonEnvelope> fout = (x) -> requestEnvelope;
        Mockito.when(enveloper.withMetadataFrom(Mockito.any(), Mockito.any())).thenReturn(fout);
        JsonEnvelope resultEnvelope = Mockito.mock(JsonEnvelope.class);
        JsonObject payloadJsonObject = Mockito.mock(JsonObject.class);
        Mockito.when(requester.request(requestEnvelope)).thenReturn(resultEnvelope);
        Mockito.when(resultEnvelope.payloadAsJsonObject()).thenReturn(payloadJsonObject);
        AllNows expected = new AllNows();
        Mockito.when(jsonObjectToObjectConverter.convert(payloadJsonObject, AllNows.class)).thenReturn(expected);
        AllNows actual =  target.loadAllNowsReference(LocalDate.now());
        Assert.assertTrue(actual==expected);
    }

}
