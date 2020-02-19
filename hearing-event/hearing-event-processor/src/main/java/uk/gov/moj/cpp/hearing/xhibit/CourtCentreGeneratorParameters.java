package uk.gov.moj.cpp.hearing.xhibit;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.XmlProducerType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.time.ZonedDateTime;
import java.util.Optional;

public class CourtCentreGeneratorParameters {

    private XmlProducerType xmlProducerType;
    private Optional<CurrentCourtStatus> currentCourtStatus;
    private ZonedDateTime latestCourtListUploadTime;
    private JsonEnvelope envelope;

    public CourtCentreGeneratorParameters(final XmlProducerType xmlProducerType, final Optional<CurrentCourtStatus> currentCourtStatus, final ZonedDateTime latestCourtListUploadTime) {
        this.xmlProducerType = xmlProducerType;
        this.currentCourtStatus = currentCourtStatus;
        this.latestCourtListUploadTime = latestCourtListUploadTime;
    }

    public CourtCentreGeneratorParameters(final XmlProducerType xmlProducerType, final Optional<CurrentCourtStatus> currentCourtStatus, final ZonedDateTime latestCourtListUploadTime, final JsonEnvelope envelope) {
        this.xmlProducerType = xmlProducerType;
        this.currentCourtStatus = currentCourtStatus;
        this.latestCourtListUploadTime = latestCourtListUploadTime;
        this.envelope = envelope;
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

    public JsonEnvelope getEnvelope() { return envelope; }
}
