package uk.gov.moj.cpp.hearing.query.view;

import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.query.view.service.SessionTimeService;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionTimeQueryViewTest {

    @Mock
    private SessionTimeService sessionTimeService;


    @InjectMocks
    private SessionTimeQueryView sessionTimeQueryView;


    @Test
    public void shouldGetSessionTime() {

        final SessionTimeResponse expectedSessionTimeResponse = SessionTimeResponse.builder().build();

        final Envelope<JsonObject> envelope = envelopeFrom(metadataWithDefaults(),
                createObjectBuilder().build());

        when(sessionTimeService.getSessionTime(envelope.payload())).thenReturn(expectedSessionTimeResponse);

        final Envelope<SessionTimeResponse> responseEnvelope = sessionTimeQueryView.getSessionTime(envelope);

        assertThat(responseEnvelope.payload(), is(expectedSessionTimeResponse));
    }
}