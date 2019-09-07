package uk.gov.moj.cpp.hearing.domain.transformation.mot.util;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.APPLICANT_COUNSELS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.APPLICATION_PARTY_COUNSELS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CASE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CASE_URN;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_APPLICATIONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_APPLICATION_PARTY_ATTENDANCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_CENTRE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_CENTRE_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.COURT_CENTRE_NAME;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.CRACKED_INEFFECTIVE_TRIAL;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENCE_COUNSELS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_ATTENDANCE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.DEFENDANT_REFERRAL_REASONS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HAS_SHARED_RESULTS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_CASE_NOTES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_DAYS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_LANGUAGE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.IS_BOX_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.JUDICIARY;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.JURISDICTION_TYPE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_CASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PROSECUTION_COUNSELS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.REPORTING_RESTRICTION_REASON;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.RESPONDENT_COUNSELS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.SENDING_COMMITTAL_DATE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.TYPE;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.DefendantHelper.transformDefendantsForSendingSheet;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.util.ProsecutionCaseHelper.transformProsecutionCases;

@SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S3776"})
public class HearingHelper {

    private HearingHelper() {
    }

    public static JsonObject transformHearingForSendingSheet(final JsonObject hearing) {

        return createObjectBuilder()
                .add(CASE_ID, hearing.getString(CASE_ID))
                .add(CASE_URN, hearing.getString(CASE_URN))
                .add(COURT_CENTRE_ID, hearing.getString(COURT_CENTRE_ID))
                .add(COURT_CENTRE_NAME, hearing.getString(COURT_CENTRE_NAME))
                .add(SENDING_COMMITTAL_DATE, hearing.getString(SENDING_COMMITTAL_DATE))
                .add(DEFENDANTS, transformDefendantsForSendingSheet(hearing.getJsonArray(DEFENDANTS)))
                .add(TYPE, hearing.getString(TYPE)).build();

    }

    public static JsonObject transformHearing(final JsonObject hearing) {

        final JsonObjectBuilder transformedPayloadObjectBuilder = createObjectBuilder()
                .add(ID, hearing.getString(ID))
                .add(JURISDICTION_TYPE, hearing.getString(JURISDICTION_TYPE))
                .add(COURT_CENTRE, hearing.getJsonObject(COURT_CENTRE))
                .add(HEARING_DAYS, hearing.getJsonArray(HEARING_DAYS))
                .add(TYPE, hearing.getJsonObject(TYPE));


        if (hearing.containsKey(REPORTING_RESTRICTION_REASON)) {
            transformedPayloadObjectBuilder.add(REPORTING_RESTRICTION_REASON, hearing.getString(REPORTING_RESTRICTION_REASON));
        }

        if (hearing.containsKey(HEARING_LANGUAGE)) {
            transformedPayloadObjectBuilder.add(HEARING_LANGUAGE, hearing.getString(HEARING_LANGUAGE));
        }

        if (hearing.containsKey(PROSECUTION_CASES)) {
            transformedPayloadObjectBuilder.add(PROSECUTION_CASES, transformProsecutionCases(hearing.getJsonArray(PROSECUTION_CASES), hearing.getString(ID)));
        }

        if (hearing.containsKey(HAS_SHARED_RESULTS)) {
            transformedPayloadObjectBuilder.add(HAS_SHARED_RESULTS, hearing.getBoolean(HAS_SHARED_RESULTS));
        }


        if (hearing.containsKey(COURT_APPLICATIONS)) {
            transformedPayloadObjectBuilder.add(COURT_APPLICATIONS, hearing.getJsonArray(COURT_APPLICATIONS));
        }

        if (hearing.containsKey(DEFENDANT_REFERRAL_REASONS)) {
            transformedPayloadObjectBuilder.add(DEFENDANT_REFERRAL_REASONS, hearing.getJsonArray(DEFENDANT_REFERRAL_REASONS));
        }

        if (hearing.containsKey(HEARING_CASE_NOTES)) {
            transformedPayloadObjectBuilder.add(HEARING_CASE_NOTES, hearing.getJsonArray(HEARING_CASE_NOTES));
        }

        if (hearing.containsKey(JUDICIARY)) {
            transformedPayloadObjectBuilder.add(JUDICIARY, hearing.getJsonArray(JUDICIARY));
        }

        if (hearing.containsKey(APPLICANT_COUNSELS)) {
            transformedPayloadObjectBuilder.add(APPLICANT_COUNSELS, hearing.getJsonArray(APPLICANT_COUNSELS));
        }

        if (hearing.containsKey(RESPONDENT_COUNSELS)) {
            transformedPayloadObjectBuilder.add(RESPONDENT_COUNSELS, hearing.getJsonArray(RESPONDENT_COUNSELS));
        }

        if (hearing.containsKey(PROSECUTION_COUNSELS)) {
            transformedPayloadObjectBuilder.add(PROSECUTION_COUNSELS, hearing.getJsonArray(PROSECUTION_COUNSELS));
        }

        if (hearing.containsKey(DEFENCE_COUNSELS)) {
            transformedPayloadObjectBuilder.add(DEFENCE_COUNSELS, hearing.getJsonArray(DEFENCE_COUNSELS));
        }

        if (hearing.containsKey(APPLICATION_PARTY_COUNSELS)) {
            transformedPayloadObjectBuilder.add(APPLICATION_PARTY_COUNSELS, hearing.getJsonArray(APPLICATION_PARTY_COUNSELS));
        }

        if (hearing.containsKey(DEFENDANT_ATTENDANCE)) {
            transformedPayloadObjectBuilder.add(DEFENDANT_ATTENDANCE, hearing.getJsonArray(DEFENDANT_ATTENDANCE));
        }

        if (hearing.containsKey(COURT_APPLICATION_PARTY_ATTENDANCE)) {
            transformedPayloadObjectBuilder.add(COURT_APPLICATION_PARTY_ATTENDANCE, hearing.getJsonArray(COURT_APPLICATION_PARTY_ATTENDANCE));
        }

        if (hearing.containsKey(CRACKED_INEFFECTIVE_TRIAL)) {
            transformedPayloadObjectBuilder.add(COURT_APPLICATIONS, hearing.getJsonObject(COURT_APPLICATIONS));
        }

        if (hearing.containsKey(IS_BOX_HEARING)) {
            transformedPayloadObjectBuilder.add(IS_BOX_HEARING, hearing.getBoolean(IS_BOX_HEARING));
        }

        return transformedPayloadObjectBuilder.build();
    }
}
