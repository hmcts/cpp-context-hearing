package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;
import static uk.gov.moj.cpp.hearing.XmlProducerType.WEB_PAGE;

import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.exception.GenerationFailedException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class CourtCentreXmlGeneratorProducerTest {

    @Mock
    private WebPageCourtCentreXmlGenerator webPageCourtCentreXmlGenerator;

    @Mock
    private EmptyWebPageCourtCentreXmlGenerator emptyWebPageCourtCentreXmlGenerator;

    @InjectMocks
    private CourtCentreXmlGeneratorProducer courtCentreXmlGeneratorProducer;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

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