package uk.gov.moj.cpp.hearing.it.framework.util;

import static uk.gov.moj.cpp.hearing.it.framework.ContextNameProvider.CONTEXT_NAME;

import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;

public class ViewStoreCleaner {

    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();

    public void cleanViewstoreTables() {
        databaseCleaner.cleanViewStoreTables(CONTEXT_NAME,
                "application_draft_result",
                "ha_associated_person",
                "ha_defendant_case",
                "ha_defendant_referral_reason",
                "ha_defendant_witnesses",
                "ha_defendant_attendance",
                "ha_offence",
                "ha_defendant",
                "ha_case",
                "ha_hearing_day",
                "ha_hearing_case_note",
                "ha_hearing_company_representative",
                "ha_hearing_applicant_counsel",
                "ha_hearing_defence_counsel",
                "ha_hearing_prosecution_counsel",
                "ha_hearing_respondent_counsel",
                "ha_hearing_interpreter_intermediary",
                "ha_hearing_event",
                "ha_judicial_role",
                "ha_now",
                "ha_prompt",
                "ha_result_line",
                "ha_target",
                "ha_witness",
                "ha_hearing",
                "heda_hearing_event_definition",
                "not_subscription_courtcentre",
                "not_subscription_nowtype",
                "not_subscription_property",
                "not_subscription_usergroup",
                "not_subscription",
                "not_document",
                "nows",
                "result_line",
                "processed_event");
    }
}
