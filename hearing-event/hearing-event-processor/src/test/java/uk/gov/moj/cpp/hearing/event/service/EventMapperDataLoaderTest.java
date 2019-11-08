package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventMapperDataLoaderTest {

    @Mock
    private Requester requester;

    @Mock
    private Enveloper enveloper;

    @InjectMocks
    private EventMapperDataLoader eventMapperDataLoader;

    @Test
    public void shouldLoadRefData() {
        //TODO: implement this logic once ref data endpoint ready
    }
}