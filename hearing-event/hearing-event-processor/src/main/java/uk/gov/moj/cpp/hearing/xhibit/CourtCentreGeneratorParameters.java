package uk.gov.moj.cpp.hearing.xhibit;

import uk.gov.moj.cpp.hearing.XmlProducerType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

public class CourtCentreGeneratorParameters {

    private XmlProducerType xmlProducerType;
    private Optional<CurrentCourtStatus> currentCourtStatus;
    private ZonedDateTime latestCourtListUploadTime;

    public CourtCentreGeneratorParameters(final XmlProducerType xmlProducerType, final Optional<CurrentCourtStatus> currentCourtStatus, final ZonedDateTime latestCourtListUploadTime) {
        this.xmlProducerType = xmlProducerType;
        this.currentCourtStatus = currentCourtStatus;
        this.latestCourtListUploadTime = latestCourtListUploadTime;
    }

    public XmlProducerType getXmlProducerType() {
        return xmlProducerType;
    }

    public Optional<CurrentCourtStatus> getCurrentCourtStatus() {
        return currentCourtStatus;
    }

    public ZonedDateTime getLatestCourtListUploadTime() {
        return latestCourtListUploadTime;
    }
}
