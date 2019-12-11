package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.parse;
import static java.util.Optional.of;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.xhibit.XmlTestUtils.assertXmlEquals;

import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DummyPublicDisplayCourtCentreXmlGeneratorTest {

    private static final String DUMMY_PUBLIC_DISPLAY_FILE_PATH = "xhibit/expectedDummyPublicDisplay.xml";

    @Spy
    private XmlUtils xmlUtils;

    @InjectMocks
    private DummyPublicDisplayCourtCentreXmlGenerator dummyPublicDisplayCourtCentreXmlGenerator;

    @Test
    public void shouldGenerateDummyXml() throws IOException {
        final Optional<CurrentCourtStatus> currentCourtStatus = of(currentCourtStatus().build());

        final ZonedDateTime lastUpdatedTime = parse("2019-12-05T13:50:00Z");

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(PUBLIC_DISPLAY, currentCourtStatus, lastUpdatedTime);

        final String generatedPublicDisplayXml = dummyPublicDisplayCourtCentreXmlGenerator.generateXml(courtCentreGeneratorParameters);

        assertXmlEquals(generatedPublicDisplayXml, DUMMY_PUBLIC_DISPLAY_FILE_PATH);
    }
}