package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;
import uk.gov.moj.cpp.listing.domain.xhibit.CourtLocation;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitFileNameGeneratorTest {

    @Mock
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @InjectMocks
    private XhibitFileNameGenerator xhibitFileNameGenerator;

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");

    ZonedDateTime requestedTime;
    String courtCentreId;
    CourtLocation courtLocation;

    @Before
    public void setup() {
        requestedTime = now();
        courtCentreId = randomUUID().toString();

        courtLocation = new CourtLocation("ouCode",
                "123",
                "CrestCourtSiteId",
                "CourtName",
                "CourtShortName",
                "CourtSiteName",
                "CourtSiteCode",
                "CourtType");

        when(commonXhibitReferenceDataService.getCourtDetails(fromString(courtCentreId))).thenReturn(courtLocation);
    }

    @Test
    public void shouldGenerateWebPageFileName() {
        final String generateWebPageFileName = xhibitFileNameGenerator.generateWebPageFileName(requestedTime, courtCentreId);
        assertThat(generateWebPageFileName, is("WebPage_123_".concat(requestedTime.format(formatter).concat(".xml"))));
    }

    @Test
    public void shouldGeneratePublicDisplayFileName() {
        final String generateWebPageFileName = xhibitFileNameGenerator.generatePublicDisplayFileName(requestedTime, courtCentreId);
        assertThat(generateWebPageFileName, is("PublicDisplay_123_".concat(requestedTime.format(formatter).concat(".xml"))));
    }
}
