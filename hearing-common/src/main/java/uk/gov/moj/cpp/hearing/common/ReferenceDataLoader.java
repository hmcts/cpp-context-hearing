package uk.gov.moj.cpp.hearing.common;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.core.courts.LjaDetails;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.OrganisationunitsResult;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.common.exception.ReferenceDataNotFoundException;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ReferenceDataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDataLoader.class);

    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNITS = "referencedata.query.organisationunits";
    private static final String ENFORCEMENT_AREA_QUERY_NAME = "referencedata.query.enforcement-area";
    private static final String COURT_CODE_QUERY_PARAMETER = "localJusticeAreaNationalCourtCode";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_UNIT = "referencedata.query.organisation-unit.v2";

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    public Optional<OrganisationunitsResult> getOrganisationUnitList() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_ORGANISATION_UNITS)
                        .withId(randomUUID())
                        .build(),
                createObjectBuilder().build());

        final Envelope<OrganisationunitsResult> response = requester.requestAsAdmin(requestEnvelope, OrganisationunitsResult.class);

        if (isNull(response) || isNull(response.payload()) || isEmpty(response.payload().getOrganisationunits())) {
            throw new ReferenceDataNotFoundException("Cannot find organisationunits" + REFERENCEDATA_QUERY_ORGANISATION_UNITS);
        }

        return of(response.payload());
    }

    public OrganisationalUnit getOrganisationUnitById(final UUID courtCentreId) {
        final JsonObject payload = createObjectBuilder().add("id", courtCentreId.toString()).build();

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(REFERENCEDATA_QUERY_ORGANISATION_UNIT)
                        .withId(randomUUID())
                        .build(),
                payload);

        final Envelope<OrganisationalUnit> responseEnvelope = requester.requestAsAdmin(requestEnvelope, OrganisationalUnit.class);

        if (isNull(responseEnvelope) || isNull(responseEnvelope.payload())) {
            throw new ReferenceDataNotFoundException(format("Cannot find this organisationunit %s", courtCentreId + REFERENCEDATA_QUERY_ORGANISATION_UNIT));
        }

        LOGGER.debug("'referencedata.query.organisation-unit' response {}", responseEnvelope.metadata().asJsonObject());
        return responseEnvelope.payload();
    }

    public LjaDetails getLjaDetails(final OrganisationalUnit organisationUnit) {
        final EnforcementArea enforcementArea = getEnforcementAreaByLjaCode(organisationUnit.getLja(), organisationUnit.getId());

        return LjaDetails.ljaDetails()
                .withLjaCode(enforcementArea.getLocalJusticeArea().getNationalCourtCode())
                .withLjaName(enforcementArea.getLocalJusticeArea().getName())
                .withWelshLjaName(enforcementArea.getLocalJusticeArea().getWelshName())
                .build();
    }

    private EnforcementArea getEnforcementAreaByLjaCode(final String ljaCode, final String courtCentreId) {
        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(ENFORCEMENT_AREA_QUERY_NAME)
                        .withId(randomUUID())
                        .build(),
                createObjectBuilder().add(COURT_CODE_QUERY_PARAMETER, ljaCode).build());

        final Envelope<EnforcementArea> responseEnvelope = requester.requestAsAdmin(requestEnvelope, EnforcementArea.class);
        final EnforcementArea enforcementArea = responseEnvelope.payload();

        if (isNull(enforcementArea)) {
            throw new ReferenceDataNotFoundException(format("No enforcement area found for court centreId %s", courtCentreId + ENFORCEMENT_AREA_QUERY_NAME));
        }

        return enforcementArea;
    }
}
