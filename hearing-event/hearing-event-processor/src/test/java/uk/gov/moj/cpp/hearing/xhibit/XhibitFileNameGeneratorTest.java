package uk.gov.moj.cpp.hearing.xhibit;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class XhibitFileNameGeneratorTest {

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMddHHmmss");
    @Mock
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;
    @InjectMocks
    private XhibitFileNameGenerator xhibitFileNameGenerator;
    @Mock
    private XhibitHelper xhibitHelper;
    private ZonedDateTime requestedTime;
    private String courtCentreId;

    @BeforeEach
    public void setup() {
        requestedTime = now();
        courtCentreId = randomUUID().toString();
        when(xhibitHelper.getCrestCourtId(any())).thenReturn("123");


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
