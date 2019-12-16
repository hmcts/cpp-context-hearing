package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitFileNameGeneratorTest {

    @Mock
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;

    @InjectMocks
    private XhibitFileNameGenerator xhibitFileNameGenerator;


    @Test
    public void shouldGenerateWebPageFileName() {
        final ZonedDateTime requestedTime = now();
        final String courtCentreId = randomUUID().toString();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        when(referenceDataXhibitDataLoader.getXhibitCourtCentreCodeBy(courtCentreId)).thenReturn("TEST");

        final String generateWebPageFileName = xhibitFileNameGenerator.generateWebPageFileName(requestedTime, courtCentreId);

        assertThat(generateWebPageFileName, is("WebPage_TEST_".concat(requestedTime.format(formatter).concat(".xml"))));
    }

    @Test
    public void shouldGeneratePublicDisplayFileName() {
        final ZonedDateTime requestedTime = now();
        final String courtCentreId = randomUUID().toString();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        when(referenceDataXhibitDataLoader.getXhibitCourtCentreCodeBy(courtCentreId)).thenReturn("TEST");

        final String generateWebPageFileName = xhibitFileNameGenerator.generatePublicDisplayFileName(requestedTime, courtCentreId);

        assertThat(generateWebPageFileName, is("PD_TEST_".concat(requestedTime.format(formatter).concat(".xml"))));
    }
}