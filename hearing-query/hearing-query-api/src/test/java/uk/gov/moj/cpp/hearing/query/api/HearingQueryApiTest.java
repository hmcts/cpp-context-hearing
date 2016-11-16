package uk.gov.moj.cpp.hearing.query.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

@RunWith(MockitoJUnitRunner.class)
public class HearingQueryApiTest {
    @Mock
    private JsonEnvelope query;
    @Mock
    private JsonEnvelope response;

    @Mock
    private Requester requester;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    @Test
    public void shouldFindHearings() {
        when(requester.request(query)).thenReturn(response);
        assertThat(hearingQueryApi.findHearings(query), equalTo(response));
    }

    @Test
    public void shouldFindHearing() {
        when(requester.request(query)).thenReturn(response);
        assertThat(hearingQueryApi.findHearing(query), equalTo(response));
    }
}
