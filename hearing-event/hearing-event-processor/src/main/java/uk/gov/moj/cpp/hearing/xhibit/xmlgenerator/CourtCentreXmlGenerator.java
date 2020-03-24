package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;


import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;

public interface CourtCentreXmlGenerator {
    String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters);
}