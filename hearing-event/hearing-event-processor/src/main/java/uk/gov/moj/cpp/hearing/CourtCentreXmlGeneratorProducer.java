package uk.gov.moj.cpp.hearing;

import static java.lang.String.format;
import static uk.gov.moj.cpp.hearing.XmlProducerType.PUBLIC_DISPLAY;

import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreXmlGenerator;
import uk.gov.moj.cpp.hearing.xhibit.PublicDisplayCourtCentreXmlGenerator;
import uk.gov.moj.cpp.hearing.xhibit.exception.GenerationFailedException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CourtCentreXmlGeneratorProducer {

    @Inject
    private PublicDisplayCourtCentreXmlGenerator publicDisplayCourtCentreXmlGenerator;

    @Inject
    private DummyPublicDisplayCourtCentreXmlGenerator dummyPublicDisplayCourtCentreXmlGenerator;

    public CourtCentreXmlGenerator getCourtCentreXmlGenerator(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {
        if (courtCentreGeneratorParameters.getCurrentCourtStatus().isPresent()) {
            if (courtCentreGeneratorParameters.getXmlProducerType().equals(PUBLIC_DISPLAY)) {
                return publicDisplayCourtCentreXmlGenerator;
            }
            throw new GenerationFailedException(format("Unknown xml generator type: %s", courtCentreGeneratorParameters.getXmlProducerType().name()));
        } else {
            return dummyPublicDisplayCourtCentreXmlGenerator;
        }
    }
}
