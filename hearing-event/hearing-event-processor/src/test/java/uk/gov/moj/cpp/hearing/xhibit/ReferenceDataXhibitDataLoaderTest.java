package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappings;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappingsList;

import javax.json.JsonObject;

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

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private UtcClock utcClock;

    @InjectMocks
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    private static final String CREST_COURT_SITE_CODE = "123";


    @Test
    public void shouldLoadDataFromExhibit() {
        final String courtCentreId = randomUUID().toString();
        final CourtRoomMappingsList courtCentreCourtList = courtRoomMappingList();

        when(requester.request(any(JsonEnvelope.class), eq(CourtRoomMappingsList.class)).payload()).thenReturn(courtCentreCourtList);
        when(jsonObjectToObjectConverter.convert(any(JsonObject.class), any())).thenReturn(courtCentreCourtList);

        final String courtCentreCode = referenceDataXhibitDataLoader.getXhibitCourtCentreCodeBy(courtCentreId);

        assertThat(courtCentreCode, is(CREST_COURT_SITE_CODE));
    }

    private CourtRoomMappingsList courtRoomMappingList() {
        return new CourtRoomMappingsList(asList(new CourtRoomMappings(randomUUID(), "", "", "", "", CREST_COURT_SITE_CODE, "")));
    }
}