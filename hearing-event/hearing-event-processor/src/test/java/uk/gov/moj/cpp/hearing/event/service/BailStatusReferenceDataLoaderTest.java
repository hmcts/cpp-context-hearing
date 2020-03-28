package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.moj.cpp.hearing.event.service.BailStatusReferenceDataLoader.GET_ALL_BAILSTATUSES_REQUEST_ID;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.AllBailStatuses;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BailStatusReferenceDataLoaderTest {

    @Mock
    private Envelope<Object> allBailStatusesEnvelope;

    @Mock
    private AllBailStatuses allBailStatuses;

    @Mock
    private Requester requester;

    @InjectMocks
    private BailStatusReferenceDataLoader target;

    @Captor
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;


    @Test
    public void testGetAllBailStatuses() {


        when(requester.request(any(), any())).thenReturn(allBailStatusesEnvelope);
        when(allBailStatusesEnvelope.payload()).thenReturn(allBailStatuses);

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(
                metadataWithDefaults().withName(GET_ALL_BAILSTATUSES_REQUEST_ID),
                createObjectBuilder()
                        .build()
        );

        target.getAllBailStatuses(jsonEnvelope);

        verify(requester).request(envelopeCaptor.capture(), any());

        assertThat(envelopeCaptor.getValue().metadata().name(), is(GET_ALL_BAILSTATUSES_REQUEST_ID));
    }
}
