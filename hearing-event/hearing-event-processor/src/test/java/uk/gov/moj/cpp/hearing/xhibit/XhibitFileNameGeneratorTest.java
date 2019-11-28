package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.*;
import static java.util.UUID.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.CourtCentre;
import uk.gov.moj.cpp.hearing.xhibit.pojo.CourtCentreCode;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitFileNameGeneratorTest {

    @Mock
    private ReferenceDataXhibitDataLoaderService referenceDataXhibitDataLoaderService;

    @InjectMocks
    private XhibitFileNameGenerator xhibitFileNameGenerator;


    @Test
    public void shouldGenerateWebPageFileName() {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final ZonedDateTime requestedTime = now();
        final String courtCentreId = randomUUID().toString();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


        final CourtCentreCode courtCentreCode = new CourtCentreCode(randomUUID(), "", "123", "", "", "");

        when(referenceDataXhibitDataLoaderService.getXhibitCourtCentreCodeBy(jsonEnvelope, courtCentreId)).thenReturn(courtCentreCode);

        final String generateWebPageFileName = xhibitFileNameGenerator.generateWebPageFileName(jsonEnvelope, requestedTime, courtCentreId);

        assertThat(generateWebPageFileName, is("WebPage_123_".concat(requestedTime.format(formatter).concat(".xml"))));
    }

    @Test
    public void shouldGeneratePublicDisplayFileName() {

        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);
        final ZonedDateTime requestedTime = now();
        final String courtCentreId = randomUUID().toString();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


        final CourtCentreCode courtCentreCode = new CourtCentreCode(randomUUID(), "", "123", "", "", "");

        when(referenceDataXhibitDataLoaderService.getXhibitCourtCentreCodeBy(jsonEnvelope, courtCentreId)).thenReturn(courtCentreCode);

        final String generateWebPageFileName = xhibitFileNameGenerator.generatePublicDisplayFileName(jsonEnvelope, requestedTime, courtCentreId);

        assertThat(generateWebPageFileName, is("PD_123_".concat(requestedTime.format(formatter).concat(".xml"))));
    }
}