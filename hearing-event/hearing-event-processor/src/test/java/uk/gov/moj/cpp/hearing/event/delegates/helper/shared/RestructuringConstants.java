package uk.gov.moj.cpp.hearing.event.delegates.helper.shared;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;

import uk.gov.justice.core.courts.HearingType;

import java.time.LocalDate;

public class RestructuringConstants {
    public static final int COURT_ROOM_ID = 54321;
    public static final String HEARING_RESULTS_SHARED_EVENT = "hearing.results-shared";
    public static final String HEARING_RESULTS_SHARED_JSON = "hearing.results-shared.json";
    public static final String HEARING_RESULTS_CASE_LEVEL_SHARED_JSON = "hearing.results-shared-case-level.json";
    public static final String HEARING_RESULTS_DEFENDANT_LEVEL_SHARED_JSON = "hearing.results-shared-defendant-level.json";
    public static final String HEARING_RESULTS_SHARED_WITH_VERDICT_TYPE_JSON = "hearing.results-shared-with-verdict-type.json";
    public static final String HEARING_RESULTS_SHARED_WITH_CONVICTION_DATE_JURISDICTION_CROWN_JSON = "hearing.results-shared-with-conviction-date-jurisdiction-crown.json";
    public static final String HEARING_RESULTS_SHARED_WITH_CONVICTION_DATE_HEARING_DAY_WITHOUT_COURT_CENTRE_ID = "hearing.events.results-shared-v3-hearingDay-without-courtCentreId.json";
    public static final String HEARING_RESULTS_SHARED_WITH_CONVICTION_DATE_JURISDICTION_MAGISTRATES_JSON = "hearing.results-shared-with-conviction-date-jurisdiction-magistrates.json";
    public static final String HEARING_RESULTS_SHARED_WITH_CONVICTION_DATE_NOT_MATCHING_SITTING_DAY_JSON = "hearing.results-shared-with-conviction-date-not-matching-sitting-day.json";
    public static final String HEARING_RESULTS_SHARED_WITH_CONVICTION_DATE_MATCHING_SITTING_DAY_JSON = "hearing.results-shared-one-offence-with-conviction-court-second-offence-with-conviction-date.json";
    public static final String HEARING_RESULTS_SHARED_WITH_INDICATED_PLEA_JSON = "hearing.results-shared-with-indicated-plea.json";
    public static final String HEARING_RESULTS_SHARED_COURT_APPLICATION_WITH_INDICATED_PLEA_JSON = "hearing.results-shared-court-application-enrich-offence-data.json";
    public static final String HEARING_RESULTS_SHARED_WITH_OFFENCE_FACTS_JSON = "hearing.results-shared-with-offence-facts.json";
    public static final String HEARING_RESULTS_SHARED_OPTIONAL_PROMPT_REF_JSON = "hearing.results-shared-with-optional-promptRef.json";
    public static final String IMP_TIMP_HEARING_RESULTS_SHARED_JSON = "imp-timp-hearing-results-shared.json";
    public static final String HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON = "hearing.results-shared_multiple_defendant.json";
    public static final String HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_MULTIPLE_CASE_JSON = "hearing.results-shared_multiple_defendant_multiple_case.json";
    public static final String HEARING_RESULTS_NEW_REVIEW_HEARING_JSON = "hearing.results-shared_new_review_hearing.json";
    public static final String HEARING_RESULTS_DATEORDERS_HEARING_JSON = "hearing.results-shared_with_orderedDates_hearing.json";
    public static final String HEARING_RESULTS_HEARING_JSON = "hearing.events.results-shared-v3.json";
    public static final String HEARING_CASE_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON = "hearing.case-results-shared_with_drivinglicencenumber.json";
    public static final String HEARING_APPLICATION_RESULTS_SHARED_WITH_DRIVINGLICENCENUMBER_JSON = "hearing.application-results-shared_with_drivinglicencenumber.json";
    public static final String RESULT_DEFINITIONS_JSON = "result-definitions.json";
    public static final String SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON = "scenario1-shortCode-SendToCCOnCB.json";
    public static final String HEARING_RESULTS_SHARED_WITH_NO_PROMPTS_JSON = "hearing.results-shared-with-no-prompts.json";
    public static final String HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE = "hearing.results-shared-to-set-acquittal-date.json";
    public static final String HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES = "hearing.results-shared-to-set-acquittal-date-for-courtapplication-cases.json";
    public static final String HEARING_RESULTS_SHARED_TO_SET_ACQUITTAL_DATE_FOR_COURTORDER = "hearing.results-shared-to-set-acquittal-date-for-courtorder.json";
    public static final String HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE = "hearing.results-shared-with-acquittal-date.json";
    public static final String HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTAPPLICATIONCASES = "hearing.results-shared-with-acquittal-date-for-courtapplication-cases.json";
    public static final String HEARING_RESULTS_SHARED_WITH_ACQUITTAL_DATE_FOR_COURTORDER = "hearing.results-shared-with-acquittal-date-for-courtorder.json";
    public static final String HEARING_RESULTS_NEW_REVIEW_HEARING_ALWAYS_PUBLISHED_LEAF_NODE_JSON = "hearing.results-shared_new_review_hearing_leaf_nod_published.json";
    public static final String DIRS_HEARING_JSON = "DIRS-hearing.json";
    public static final String FIXED_LIST_JSON = "fixed-list.json";
    public static final String CO_HEARING_EVENT_JSON = "CO-hearing-event.json";
    public static final String RESULT_TEXT_SPLIT_REGEX = "\\R";
    public static final String COURT_ROOM_OU_CODE = "B47GL00";
    public static final String COURT_ROOM_NAME = "ROOM A";
    public static final String COURT_NAME = "Wimbledon Magistrates Court";
    public static final String HEARING_TYPE_DESCRIPTION = "Plea & Trial Preparation";
    public static final String REMANDED_IN_CUSTODY_ID = "d0a369c9-5a28-40ec-99cb-da7943550b18";
    public static final String REMANDED_IN_CUSTODY_TO_HOSPITAL_ID = "e3315a27-35fd-4c43-8ba6-8b5d69aa96fb";
    public static final String REMANDED_ON_CONDITIONAL_BAIL_ID = "3a529001-2f43-45ba-a0a8-d3ced7e9e7ad";
    public static final String NEXT_HEARING_ID = "f00359b5-7303-403b-b59e-0b1a1daa89bc";
    public static final String NEXT_HEARING_IN_CROWN_COURT_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    public static final String NEXT_HEARING_IN_MAGISTRATE_COURT_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";
    public static final String REMAND_IN_CUSTODY_ID = "0056b9e1-7585-4bfa-82ec-f06202670bb1";
    public static final String SEND_TO_CROWN_COURT_ON_CONDITIONAL_BAIL_ID = "e7e02d63-46c2-4603-8255-921427f410fe";
    public static final String BAIL_CONDITIONS_ID = "8cf3b54b-bec8-4bcf-aac4-62561dcc8080";
    public static final String BAIL_CONDITION_ASSESSMENTS_REPORTS_ID = "525c4660-9a0b-4a86-80fc-0efce539d6a5";
    public static final HearingType HEARING_TYPE = new HearingType(HEARING_TYPE_DESCRIPTION, randomUUID(), HEARING_TYPE_DESCRIPTION);
    public static final LocalDate REFERENCE_DATE = PAST_LOCAL_DATE.next();
    public static final String DUMMY_NAME = "something";

    private RestructuringConstants() {
    }
}
