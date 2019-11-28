package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCode;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCourtList;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataXhibitDataLoaderTest {

    @Mock
    private Requester requester;

    @Mock
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @InjectMocks
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;


    @Test
    public void shouldLoadDataFromExhibit() {
        final JsonEnvelope query = envelope().with(metadataWithDefaults()).build();
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final String courtCentreId = randomUUID().toString();
        final CourtCentreCourtList courtCentreCourtList = courtMappingsEnvelope();

        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(envelope);
        when(jsonObjectToObjectConverter.convert(any(JsonObject.class), any())).thenReturn(courtCentreCourtList);

        final CourtCentreCode courtCentreCode = referenceDataXhibitDataLoader.getXhibitCourtCentreCodeBy(query, courtCentreId);

        assertThat(courtCentreCode.getCrestCodeId(), is(courtCentreCourtList.getCpXhibitCourtMappings().get(0).getCrestCodeId()));
        assertThat(courtCentreCode.getCrestCourtSiteId(), is(courtCentreCourtList.getCpXhibitCourtMappings().get(0).getCrestCourtSiteId()));
        assertThat(courtCentreCode.getCrestCourtSiteName(), is(courtCentreCourtList.getCpXhibitCourtMappings().get(0).getCrestCourtSiteName()));
        assertThat(courtCentreCode.getId(), is(courtCentreCourtList.getCpXhibitCourtMappings().get(0).getId()));
        assertThat(courtCentreCode.getOucode(), is(courtCentreCourtList.getCpXhibitCourtMappings().get(0).getOucode()));
    }

    private CourtCentreCourtList courtMappingsEnvelope() {
        return new CourtCentreCourtList(asList(new CourtCentreCode(randomUUID(), "", "", "", "", "")));
    }
}