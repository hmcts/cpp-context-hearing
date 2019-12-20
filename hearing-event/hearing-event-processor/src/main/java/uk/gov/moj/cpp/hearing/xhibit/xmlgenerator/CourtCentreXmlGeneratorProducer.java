package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.lang.String.format;
import static uk.gov.moj.cpp.hearing.XmlProducerType.WEB_PAGE;

import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.exception.GenerationFailedException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CourtCentreXmlGeneratorProducer {

    @Inject
    private WebPageCourtCentreXmlGenerator webPageCourtCentreXmlGenerator;

    @Inject
    private EmptyWebPageCourtCentreXmlGenerator emptyWebPageCourtCentreXmlGenerator;

    public CourtCentreXmlGenerator getCourtCentreXmlGenerator(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {
        if (courtCentreGeneratorParameters.getCurrentCourtStatus().isPresent()) {
            if (courtCentreGeneratorParameters.getXmlProducerType().equals(WEB_PAGE)) {
                return webPageCourtCentreXmlGenerator;
            }
            throw new GenerationFailedException(format("Unknown xml generator type: %s", courtCentreGeneratorParameters.getXmlProducerType().name()));
        } else {
            return emptyWebPageCourtCentreXmlGenerator;
        }
    }
}
