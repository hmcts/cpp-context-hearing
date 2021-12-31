package uk.gov.justice.ccr.notepad.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.converter.LocalDates.to;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.json.JsonObject;

/**
 * Used for querying Results from Reference Data Context.
 */
public class ResultsQueryService {

    @ServiceComponent(Component.QUERY_API)
    @Inject
    private Requester requester;

    private static final String ORG_TYPE = "orgType";
    private static final String CROWN_COURT_OU_L1_CODE = "C";
    private static final String MAGISTRATE_COURT_OU_L1_CODE = "B";
    private static final String REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE = "referencedata.query.organisation.byorgtype";

    public Envelope<JsonObject> getAllDefinitions(final JsonEnvelope envelope, final LocalDate orderedDate) {
        return sendEnvelopeWithName(envelope, "referencedata.get-all-result-definitions", createObjectBuilder()
                .add("on", to(orderedDate))
                .build());
    }

    public Envelope<JsonObject> getAllDefinitionWordSynonyms(final JsonEnvelope envelope, final LocalDate orderedDate) {
        return sendEnvelopeWithName(envelope, "referencedata.get-all-result-word-synonyms", createObjectBuilder()
                .add("on", to(orderedDate))
                .build());
    }

    public Envelope<JsonObject> getAllFixedLists(final JsonEnvelope envelope, final LocalDate orderedDate) {
        return sendEnvelopeWithName(envelope, "referencedata.get-all-fixed-list", createObjectBuilder()
                .add("on", to(orderedDate))
                .build());
    }

    public Envelope<JsonObject> getOtherFixedValues(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.cracked-ineffective-vacated-trial-types", createObjectBuilder()
                .build());
    }

    public Envelope<JsonObject> getAllCourtCentre(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.courtrooms");
    }

    public Envelope<JsonObject> getHearingTypes(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.hearing-types");
    }

    public Envelope<JsonObject> getProsecutorsByMajorCreditorFlag(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.get.prosecutor.by.major-creditor-flag");
    }

    public Envelope<JsonObject> getAllResultPromptWordSynonyms(final JsonEnvelope envelope, final LocalDate orderedDate) {
        return sendEnvelopeWithName(envelope, "referencedata.get-all-result-prompt-word-synonyms", createObjectBuilder()
                .add("on", to(orderedDate))
                .build());
    }

    public Envelope<JsonObject> getLocalJusticeAreas(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.local-justice-areas");
    }

    public Envelope<JsonObject> getCountriesNames(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.country-nationality");
    }

    public Envelope<JsonObject> getLanguages(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.languages");
    }

    public Envelope<JsonObject> getPrisonNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.prisons");
    }

    public Envelope<JsonObject> getCrownCourtsNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.organisationunits", createObjectBuilder()
                .add("oucodeL1Code", CROWN_COURT_OU_L1_CODE)
                .build());
    }

    public Envelope<JsonObject> getMagistrateCourtsNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.organisationunits", createObjectBuilder()
                .add("oucodeL1Code", MAGISTRATE_COURT_OU_L1_CODE)
                .build());
    }

    public Envelope<JsonObject> getRegionalOrganisationNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.regional-organisations");
    }

    public Envelope<JsonObject> getAttendanceCenterNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "ATTC")
                .build());
    }

    public Envelope<JsonObject> getBASSProviderNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "BASS")
                .build());
    }

    public Envelope<JsonObject> getEMCOrganisationNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "EMC")
                .build());
    }

    public Envelope<JsonObject> getLocalAuthorityNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "DESLA")
                .build());
    }

    public Envelope<JsonObject> getNCESNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "NCESCOST")
                .build());
    }

    public Envelope<JsonObject> getDrinkDrivingCourseProvidersAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "DDRP")
                .build());
    }

    public Envelope<JsonObject> getProbationNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, REFERENCEDATA_QUERY_ORGANISATION_BYORGTYPE, createObjectBuilder()
                .add(ORG_TYPE, "NPS")
                .build());
    }

    public Envelope<JsonObject> getYOTSNameAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.youth-offending-teams");
    }

    public Envelope<JsonObject> getScottishCourtAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.scottish-ni-courts");

    }

    public Envelope<JsonObject> getYouthCourtAddress(final JsonEnvelope envelope) {
        return sendEnvelopeWithName(envelope, "referencedata.query.youth-courts");
    }

    /**
     * Updates the original envelope with the new name and payload, then sends.
     *
     * @param envelope - the original envelope
     * @param name     - the updated name to insert into the envelope being sent
     * @return the returned envelope from the synchronous request
     */
    private Envelope<JsonObject> sendEnvelopeWithName(final JsonEnvelope envelope, final String name, final JsonObject payload) {
        return requester.requestAsAdmin(Enveloper.envelop(payload)
                .withName(name)
                .withMetadataFrom(envelope), JsonObject.class);
    }

    /**
     * Updates the original envelope with the new name, then sends.
     *
     * @param envelope - the original envelope
     * @param name     - the updated name to insert into the envelope being sent
     * @return the returned envelope from the synchronous request
     */
    private Envelope<JsonObject> sendEnvelopeWithName(final JsonEnvelope envelope, final String name) {
        return sendEnvelopeWithName(envelope, name, createObjectBuilder().build());
    }
}
