package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.XmlProducerType.WEB_PAGE;

import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class CourtCentreXmlGeneratorProducerTest {

    @Mock
    private WebPageCourtCentreXmlGenerator webPageCourtCentreXmlGenerator;

    @Mock
    private EmptyWebPageCourtCentreXmlGenerator emptyWebPageCourtCentreXmlGenerator;

    @InjectMocks
    private CourtCentreXmlGeneratorProducer courtCentreXmlGeneratorProducer;

    @Test
    public void shouldGenerateInstanceOfPublicDisplayCourtCentreXmlGenerator() {
        final CurrentCourtStatus currentCourtStatus = mock(CurrentCourtStatus.class);

        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(WEB_PAGE, of(currentCourtStatus), now());

        final CourtCentreXmlGenerator courtCentreXmlGenerator = courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(courtCentreGeneratorParameters);

        assertThat(courtCentreXmlGenerator, is(instanceOf(WebPageCourtCentreXmlGenerator.class)));
    }

    @Test
    public void shouldGenerateInstanceOfDummyPublicDisplayCourtCentreXmlGenerator() {
        final CourtCentreGeneratorParameters courtCentreGeneratorParameters = new CourtCentreGeneratorParameters(WEB_PAGE, empty(), now());

        final CourtCentreXmlGenerator courtCentreXmlGenerator = courtCentreXmlGeneratorProducer.getCourtCentreXmlGenerator(courtCentreGeneratorParameters);

        assertThat(courtCentreXmlGenerator, is(instanceOf(EmptyWebPageCourtCentreXmlGenerator.class)));
    }
}