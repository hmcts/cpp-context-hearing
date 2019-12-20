package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataXhibitDataLoaderTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Spy
    private UtcClock utcClock;

    @InjectMocks
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    private static final String CREST_COURT_SITE_CODE = "123";
    private static final UUID EVENT_ID = randomUUID();

    @Test
    public void shouldLoadXhibitCourtCentreCodeByCourtCentreId() {
        final XhibitEventMappingsList courtCentreCourtList = getExhibitEventMappingsList();

        when(requester.request(any(JsonEnvelope.class), eq(XhibitEventMappingsList.class)).payload()).thenReturn(courtCentreCourtList);

        final XhibitEventMappingsList xhibitEventMappingsList = referenceDataXhibitDataLoader.getEventMapping();

        assertThat(xhibitEventMappingsList.getCpXhibitHearingEventMappings().get(0).getCpHearingEventId(), is(EVENT_ID));
        assertThat(xhibitEventMappingsList.getCpXhibitHearingEventMappings().get(0).getXhibitHearingEventCode(), is(CREST_COURT_SITE_CODE));
    }

    private XhibitEventMappingsList getExhibitEventMappingsList() {
        return new XhibitEventMappingsList(asList(new XhibitEventMapping(EVENT_ID, CREST_COURT_SITE_CODE, "", "", ""), new XhibitEventMapping(randomUUID(), "sdfsdf", "", "", "")));

    }


}