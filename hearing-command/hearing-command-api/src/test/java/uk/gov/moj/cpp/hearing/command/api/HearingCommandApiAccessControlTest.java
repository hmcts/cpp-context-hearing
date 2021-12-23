package uk.gov.moj.cpp.hearing.command.api;

import static org.mockito.BDDMockito.given;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.hearing.providers.HearingProvider;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class HearingCommandApiAccessControlTest extends BaseDroolsAccessControlTest {

    private static final String ACTION_NAME_UPDATE_PLEA = "hearing.update-plea";
    private static final String ACTION_NAME_INITIATE_HEARING = "hearing.initiate";
    private static final String ACTION_NAME_UPDATE_VERDICT = "hearing.update-verdict";
    private static final String ACTION_NAME_ADD_PROSECUTION_COUNSEL = "hearing.add-prosecution-counsel";
    private static final String ACTION_NAME_REMOVE_PROSECUTION_COUNSEL = "hearing.remove-prosecution-counsel";
    private static final String ACTION_NAME_UPDATE_PROSECUTION_COUNSEL = "hearing.update-prosecution-counsel";
    private static final String ACTION_NAME_SAVE_DRAFT_RESULT = "hearing.save-draft-result";
    private static final String ACTION_NAME_LOG_HEARING_EVENT = "hearing.log-hearing-event";
    private static final String ACTION_NAME_CORRECT_HEARING_EVENT = "hearing.correct-hearing-event";
    private static final String ACTION_NAME_SHARE_RESULTS_EVENT = "hearing.share-results";
    private static final String ACTION_NAME_SHARE_RESULTS_V2_EVENT = "hearing.share-results-v2";
    private static final String ACTION_NAME_SHARE_DAYS_RESULTS_EVENT = "hearing.share-days-results";
    private static final String ACTION_NAME_CREATE_HEARING_EVENT_DEFINITIONS_EVENT = "hearing.create-hearing-event-definitions";
    private static final String ACTION_NAME_GENERATE_NOWS = "hearing.generate-nows";
    private static final String ACTION_NAME_UPDATE_NOWS_MATERIAL_STATUS = "hearing.update-nows-material-status";
    private static final String ACTION_NAME_DELETE_ATTENDEE = "hearing.delete-attendee";
    private static final String ACTION_NAME_SAVE_HEARING_CASE_NOTE = "hearing.save-hearing-case-note";
    private static final String ACTION_NAME_SAVE_APPLICATION_RESPONSE = "hearing.save-application-response";

    private static final String ACTION_NAME_ADD_RESPONDENT_COUNSEL = "hearing.add-respondent-counsel";
    private static final String ACTION_NAME_REMOVE_RESPONDENT_COUNSEL = "hearing.remove-respondent-counsel";
    private static final String ACTION_NAME_UPDATE_RESPONDENT_COUNSEL = "hearing.update-respondent-counsel";

    private static final String ACTION_NAME_ADD_APPLICANT_COUNSEL = "hearing.add-applicant-counsel";
    private static final String ACTION_NAME_REMOVE_APPLICANT_COUNSEL = "hearing.remove-applicant-counsel";
    private static final String ACTION_NAME_UPDATE_APPLICANT_COUNSEL = "hearing.update-applicant-counsel";
    private static final String ACTION_NAME_ADD_DEFENCE_COUNSEL = "hearing.add-defence-counsel";
    private static final String ACTION_NAME_COURT_LIST_PUBLISH_STATUS = "hearing.publish-court-list";
    private static final String ACTION_NAME_PUBLISH_HEARING_LISTS_FOR_CROWN_COURTS = "hearing.publish-hearing-lists-for-crown-courts";
    private static final String ACTION_NAME_COMPUTE_OUTSTANDING_FINES = "hearing.compute-outstanding-fines";
    private static final String ACTION_NAME_ADD_REQUEST_FOR_OUTSTANDING_FINES = "hearing.add-request-for-outstanding-fines";

    private static final String ACTION_NAME_RECORD_SESSION_TIME = "hearing.record-session-time";
    private static final String ACTION_NAME_BOOK_PROVISIONAL_HEARING_SLOTS = "hearing.book-provisional-hearing-slots";
    private static final String ACTION_NAME_SET_TRIAL_TYPE = "hearing.set-trial-type";
    private static final String ACTION_NAME_REMOVE_TARGET = "hearing.remove-targets";
    private static final String ACTION_NAME_REQUEST_APPROVAL = "hearing.request-approval";
    private static final String ACTION_NAME_MASTER_DEFENDANT_ID = "hearing.add-master-defendant-id-to-defendant";
    private static final String ACTION_NAME_VALIDATE_RESULT_AMENDMENTS = "hearing.validate-result-amendments";
    private static final String ACTION_NAME_UPDATE_RESULTLINE_SHARED_DATES = "hearing.update-resultline-shared-dates";
    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    @Mock
    private HearingProvider hearingProvider;

    @Test
    public void shouldAllowAuthorisedUserToInitiateHearing() {
        final Action action = createActionFor(ACTION_NAME_INITIATE_HEARING);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Probation Admin", "Listing Officers", "Court Clerks", "Legal Advisers", "Court Administrators", "Crown Court Admin", "System Users", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToInitiateHearing() {
        final Action action = createActionFor(ACTION_NAME_INITIATE_HEARING);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdatePlea() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_PLEA);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdatePlea() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_PLEA);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdateVerdict() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_VERDICT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateVerdict() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_VERDICT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAddProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Judiciary", "Court Associate", "CPS", "Advocates", "Non CPS Prosecutors"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAddProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRemoveProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAddDefenceCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_DEFENCE_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers","Defence Users","Advocates", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnAuthorisedUserToAddDefenceCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_DEFENCE_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToREMOVEProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdateProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateProsecutionCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_PROSECUTION_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToSaveDraftResult() {
        final Action action = createActionFor(ACTION_NAME_SAVE_DRAFT_RESULT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToSaveDraftResult() {
        final Action action = createActionFor(ACTION_NAME_SAVE_DRAFT_RESULT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToShareResult() {
        final Action action = createActionFor(ACTION_NAME_SHARE_RESULTS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToShareResult() {
        final Action action = createActionFor(ACTION_NAME_SHARE_RESULTS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToShareResultV2() {
        final Action action = createActionFor(ACTION_NAME_SHARE_RESULTS_V2_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToShareResultV2() {
        final Action action = createActionFor(ACTION_NAME_SHARE_RESULTS_V2_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToShareDaysResult() {
        final Action action = createActionFor(ACTION_NAME_SHARE_DAYS_RESULTS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToShareDaysResult() {
        final Action action = createActionFor(ACTION_NAME_SHARE_DAYS_RESULTS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToLogHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_LOG_HEARING_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Judiciary", "Court Associate", "Deputies", "DJMC", "Judge", "Recorders")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToLogHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_LOG_HEARING_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToCorrectHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_CORRECT_HEARING_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Judiciary", "Court Associate", "Deputies", "DJMC", "Judge", "Recorders")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToCorrectHearingEvent() {
        final Action action = createActionFor(ACTION_NAME_CORRECT_HEARING_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToCreateHearingEventDefinitions() {
        final Action action = createActionFor(ACTION_NAME_CREATE_HEARING_EVENT_DEFINITIONS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "System Users", "Court Associate")).willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToCreateHearingEventDefinitions() {
        final Action action = createActionFor(ACTION_NAME_CREATE_HEARING_EVENT_DEFINITIONS_EVENT);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToGenerateNows() {
        final Action action = createActionFor(ACTION_NAME_GENERATE_NOWS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToGenerateNows() {
        final Action action = createActionFor(ACTION_NAME_GENERATE_NOWS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdateNowsMaterialStatus() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_NOWS_MATERIAL_STATUS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateNowsMaterialStatus() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_NOWS_MATERIAL_STATUS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToDeleteAtendee() {
        final Action action = createActionFor(ACTION_NAME_DELETE_ATTENDEE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToDeleteAtendee() {
        final Action action = createActionFor(ACTION_NAME_DELETE_ATTENDEE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToSaveHearingCaseNote() {
        final Action action = createActionFor(ACTION_NAME_SAVE_HEARING_CASE_NOTE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Judiciary", "Court Associate", "Deputies", "DJMC", "Judge", "Recorders"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToSaveHearingCaseNote() {
        final Action action = createActionFor(ACTION_NAME_SAVE_HEARING_CASE_NOTE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToSaveApplicationResponse() {
        final Action action = createActionFor(ACTION_NAME_SAVE_APPLICATION_RESPONSE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToSaveApplicationResponse() {
        final Action action = createActionFor(ACTION_NAME_SAVE_APPLICATION_RESPONSE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToAddApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAddApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRemoveApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToRemoveApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdateApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateApplicantCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_APPLICANT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Override
    protected Map<Class, Object> getProviderMocks() {
        return ImmutableMap.<Class, Object>builder()
                .put(UserAndGroupProvider.class, this.userAndGroupProvider)
                .put(HearingProvider.class, this.hearingProvider)
                .build();
    }

    @Test
    public void shouldAllowAuthorisedUserToAddRespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToAddRespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_ADD_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRemoveRespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToREMOVERespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToUpdateRespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers", "Court Associate"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateRespondentCounsel() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_RESPONDENT_COUNSEL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToSearchForCourtListPublishStatus() {
        final Action action = createActionFor(ACTION_NAME_COURT_LIST_PUBLISH_STATUS);
        given(this.userAndGroupProvider.isSystemUser(action))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUserInAuthorisedGroupToSearchForCourtListPublishStatus() {
        final Action action = createActionFor(ACTION_NAME_COURT_LIST_PUBLISH_STATUS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToPublishHearingEventForAllCrownCourts() {
        final Action action = createActionFor(ACTION_NAME_PUBLISH_HEARING_LISTS_FOR_CROWN_COURTS);
        given(this.userAndGroupProvider.isSystemUser(action))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUserInAuthorisedGroupToPublishHearingEventForAllCrownCourts() {
        final Action action = createActionFor(ACTION_NAME_PUBLISH_HEARING_LISTS_FOR_CROWN_COURTS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowUserInAuthorisedGroupToBookProvisionalHearingSlots() {
        final Action action = createActionFor(ACTION_NAME_BOOK_PROVISIONAL_HEARING_SLOTS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Listing Officers", "Court Associate", "Legal Advisers", "Court Clerks"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUserInAuthorisedGroupToBookProvisionalHearingSlots() {
        final Action action = createActionFor(ACTION_NAME_BOOK_PROVISIONAL_HEARING_SLOTS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Test
    public void shouldAllowAuthorisedUserToComputeOutstandingFines() {
        final Action action = createActionFor(ACTION_NAME_COMPUTE_OUTSTANDING_FINES);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers", "Court Associate", "NCES"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToComputeOutstandingFines() {
        final Action action = createActionFor(ACTION_NAME_COMPUTE_OUTSTANDING_FINES);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "group1", "group2"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToInitiateAccountQuery() {
        final Action action = createActionFor(ACTION_NAME_ADD_REQUEST_FOR_OUTSTANDING_FINES);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "System Users"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToInitiateAccountQuery() {
        final Action action = createActionFor(ACTION_NAME_ADD_REQUEST_FOR_OUTSTANDING_FINES);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers", "Court Associate", "NCES"))
                .willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRecordSessionTime() {
        final Action action = createActionFor(ACTION_NAME_RECORD_SESSION_TIME);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Clerks", "Legal Advisers", "Court Associate", "Court Administrators", "Crown Court Admin"))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToRecordSessionTime() {
        final Action action = createActionFor(ACTION_NAME_RECORD_SESSION_TIME);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToSetTrialType() {
        final Action action = createActionFor(ACTION_NAME_SET_TRIAL_TYPE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action,  "Listing Officers", "Court Clerks", "Legal Advisers", "Judiciary", "Court Associate", "Deputies", "DJMC", "Judge", "Recorders"))
                .willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToSetTrialType() {
        final Action action = createActionFor(ACTION_NAME_SET_TRIAL_TYPE);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRemoveDraftTarget() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_TARGET);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "System Users"))
                .willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToRemoveDraftTarget() {
        final Action action = createActionFor(ACTION_NAME_REMOVE_TARGET);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToRequestApproval() {
        final Action action = createActionFor(ACTION_NAME_REQUEST_APPROVAL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Random group")).willReturn(false);

        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Test
    public void shouldAllowAuthorisedUserToValidateResultAmendments() {
        final Action action = createActionFor(ACTION_NAME_VALIDATE_RESULT_AMENDMENTS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Associate", "Legal Advisers", "Court Clerks"))
                .willReturn(true);
        given(this.hearingProvider.isUserAllowedToApproveResultAmendment(action)).willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldAllowAuthorisedUserToRequestApproval() {
        final Action action = createActionFor(ACTION_NAME_REQUEST_APPROVAL);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Associate", "Legal Advisers", "Court Clerks"))
                .willReturn(true);
        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToValidateResultAmendments() {
        final Action action = createActionFor(ACTION_NAME_VALIDATE_RESULT_AMENDMENTS);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Associate", "Legal Advisers", "Court Clerks"))
                .willReturn(true);
        given(this.hearingProvider.isUserAllowedToApproveResultAmendment(action)).willReturn(false);
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


    @Test
    public void shouldAllowSystemUserToAddMasterDefendantId() {
        final Action action = createActionFor(ACTION_NAME_MASTER_DEFENDANT_ID);
        given(this.userAndGroupProvider.isSystemUser(action))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldAllowSystemUserToUpdateResultLineSharedDates() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_RESULTLINE_SHARED_DATES);
        given(this.userAndGroupProvider.isSystemUser(action))
                .willReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }

    @Test
    public void shouldNotAllowUnauthorisedUserToUpdateResultLineSharedDates() {
        final Action action = createActionFor(ACTION_NAME_UPDATE_RESULTLINE_SHARED_DATES);
        given(this.userAndGroupProvider.isMemberOfAnyOfTheSuppliedGroups(action, "Court Associate", "Legal Advisers", "Court Clerks"))
                .willReturn(true);
        given(this.hearingProvider.isUserAllowedToApproveResultAmendment(action)).willReturn(false);
        final ExecutionResults results = executeRulesWith(action);
        assertFailureOutcome(results);
    }


}