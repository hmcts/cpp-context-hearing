package uk.gov.justice.ccr.notepad.result.loader;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.service.ResultsQueryService;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

public class NameAddressRefDataEndPointMapper {

    //Reference Data Key
    private static final String PRISON_KEY = "prison".toLowerCase();
    private static final String CONVEYOR_CUSTODAIN_KEY = "conveyorcustodianname".toLowerCase();
    private static final String WARRANT_EXECUTION_ALLOCATED_KEY ="warrantExecutionAllocatedTo".toLowerCase();
    private static final String ATTENDANCE_CENTER_KEY ="attendanceCentreNameContact".toLowerCase();
    private static final String BASS_PROVIDER_KEY ="bASSProvider".toLowerCase();
    private static final String EMC_RESPONSIBLE_OFFICER_KEY = "electronicMonitoringContractorResponsibleOfficer".toLowerCase();
    private static final String EMC_KEY = "electronicmonitoringcontractor".toLowerCase();
    private static final String LOCAL_AUTHORITY_KEY  = "localAuthorityNameAndAddress".toLowerCase();
    private static final String ORIG_DESG_LOCAL_AUTHORITY_KEY  = "originalDesignatedLocalAuthorityForTheRemand".toLowerCase();
    private static final String NEW_DESG_LOCAL_AUTHORTY_KEY = "theNewDesignatedLocalAuthorityDesignatedIs".toLowerCase();
    private static final String NCES_KEY = "nCES".toLowerCase();
    private static final String PROBATION_TEAM_TO_BE_NOTIFIED_KEY = "probationTeamToBeNotified".toLowerCase();
    private static final String YOTS_KEY = "youthOffendingTeamToBeNotified".toLowerCase();
    private static final String PROBATION_YOT_TO_BE_NOTIFIED_KEY = "probationYOTToBeNotified".toLowerCase();
    private static final String HCHOUSE_KEY = "hCHOUSE".toLowerCase();
    private static final String COURT_HOUSE_NAME_KEY = "courthouseName".toLowerCase();
    private static final String HCHOUSE_ORGANISATION_NAME_KEY ="hCHOUSEOrganisationName".toLowerCase();
    private static final String SCOTTISH_COURT_KEY = "scottishCourt".toLowerCase();
    private static final String NAME_OF_COURSE_KEY = "nameOfCourse".toLowerCase();
    private static final String ORIGINAL_COURT_KEY = "originalCourt".toLowerCase();

    //Label
    private static final String NAME = "name";
    private static final String ADDRESS_LINE_1 = "addressLine1";
    private static final String ADDRESS_LINE_2 = "addressLine2";
    private static final String ADDRESS_LINE_3 = "addressLine3";
    private static final String ADDRESS_LINE_4 = "addressLine4";
    private static final String ADDRESS_LINE5 = "addressLine5";
    private static final String POST_CODE = "postCode";
    private static final String ORG_NAME = "orgName";
    private static final String ADDRESS_1 = "address1";
    private static final String ADDRESS_2 = "address2";
    private static final String ADDRESS_3 = "address3";
    private static final String ADDRESS_4 = "address4";
    private static final String ADDRESS_5 = "address5";
    private static final String POSTCODE = "postcode";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String ORG_TYPE ="organisationTypes";
    private static final String ORGANISATIONUNITS = "organisationunits";
    private static final String OUCODE_L_3_NAME = "oucodeL3Name";
    private static final String POSTCODE1 = "postcode";
    private static final String PECS_CONTRACTOR_EMAIL = "pecsContractorEmail";
    private static final String OUCODE_L_3_NAME1 = "oucodeL3Name";
    private static final String CPS_EMAIL_ADDRESS = "cpsEmailAddress";
    private static final String ADDRESS = "address";


    private ResultsQueryService resultsQueryService;


    private JsonEnvelope jsonEnvelope;

    public void setJsonEnvelope(final JsonEnvelope jsonEnvelope) {
        this.jsonEnvelope = jsonEnvelope;
    }

    public void setResultsQueryService(final ResultsQueryService resultsQueryService) {
        this.resultsQueryService = resultsQueryService;
    }

    public  Map<String, Set<ResultPromptDynamicListNameAddress>> loadAllNameAddressFromRefData() {

        final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress = new HashMap<>();
        setPrisonNameAddress(resultPromptDynamicListNameAddress);
        setConveyorCustodianNameAddress(resultPromptDynamicListNameAddress);
        setNCESWarrantNameAddress(resultPromptDynamicListNameAddress);
        setAttendanceCenterNameAddress(resultPromptDynamicListNameAddress);
        setBassProviderNameAddress(resultPromptDynamicListNameAddress);
        setEMCOrganisationNameAddress(resultPromptDynamicListNameAddress);
        setLocalAuthorityNameAddress(resultPromptDynamicListNameAddress);
        setNCESCashDeptNameAddress(resultPromptDynamicListNameAddress);
        setProbationNameAddress(resultPromptDynamicListNameAddress);
        setYOTSNameAddress(resultPromptDynamicListNameAddress);
        setYOTSAndProbatiionNameAddress(resultPromptDynamicListNameAddress);
        setCourtAddress(resultPromptDynamicListNameAddress);
        setDrinkDrivingCourseProvidersAddress(resultPromptDynamicListNameAddress);
        setJudicialAuthorityNameAddress(resultPromptDynamicListNameAddress);
        return resultPromptDynamicListNameAddress;

    }

    private void setJudicialAuthorityNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        final Set<ResultPromptDynamicListNameAddress> judicialAuthorityNameAddress = new HashSet<>();
        judicialAuthorityNameAddress.addAll(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope).payload()
                .getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class)
                .stream()
                .map(crownCourt -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(crownCourt.getString(OUCODE_L_3_NAME1, null))
                        .withAddressLine1(crownCourt.getString(ADDRESS_1, null))
                        .withAddressLine2(crownCourt.getString(ADDRESS_2, null))
                        .withAddressLine3(crownCourt.getString(ADDRESS_3, null))
                        .withAddressLine4(crownCourt.getString(ADDRESS_4,null))
                        .withAddressLine5(crownCourt.getString(ADDRESS_5,null))
                        .withPostCode(crownCourt.getString(POSTCODE, null))
                        .withEmailAddress1(crownCourt.getString(CPS_EMAIL_ADDRESS, null))
                        .withEmailAddress2(crownCourt.getString(PECS_CONTRACTOR_EMAIL, null))
                        .build()).collect(toSet()));
        judicialAuthorityNameAddress.addAll(resultsQueryService.getLocalJusticeAreas(jsonEnvelope).payload()
                .getJsonArray("localJusticeAreas").getValuesAs(JsonObject.class)
                .stream()
                .map(lja-> lja.getJsonObject(ADDRESS) !=null ? ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(lja.getString("name", null))
                        .withAddressLine1(lja.getJsonObject(ADDRESS).getString(ADDRESS_1, null))
                        .withAddressLine2(lja.getJsonObject(ADDRESS).getString(ADDRESS_2, null))
                        .withAddressLine3(lja.getJsonObject(ADDRESS).getString(ADDRESS_3, null))
                        .withAddressLine4(lja.getJsonObject(ADDRESS).getString(ADDRESS_4, null))
                        .withAddressLine5(lja.getJsonObject(ADDRESS).getString(ADDRESS_5, null))
                        .withPostCode(lja.getJsonObject(ADDRESS).getString(POSTCODE, null))
                        .build()
                        : ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(lja.getString("name", null)).build()
                ).collect(toSet()));
        resultPromptDynamicListNameAddress.put(ORIGINAL_COURT_KEY, judicialAuthorityNameAddress);

    }

    private  void setPrisonNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(PRISON_KEY, resultsQueryService.getPrisonNameAddress(jsonEnvelope).payload().getJsonArray("prisons").getValuesAs(JsonObject.class).stream()
                .map(prison -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(prison.getString(NAME, null))
                        .withAddressLine1(prison.getString(ADDRESS_LINE_1, null))
                        .withAddressLine2(prison.getString(ADDRESS_LINE_2, null))
                        .withAddressLine3(prison.getString(ADDRESS_LINE_3, null))
                        .withAddressLine4(prison.getString(ADDRESS_LINE_4,null))
                        .withAddressLine5(prison.getString(ADDRESS_LINE5,null))
                        .withPostCode(prison.getString(POST_CODE, null))
                        .withEmailAddress1(prison.getString("omuEmailAddress", null))
                        .withEmailAddress2(prison.getString("receptionEmailAddress", null))
                        .build()).collect(toSet()));
    }

    private void setConveyorCustodianNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {

        final Set<ResultPromptDynamicListNameAddress> conveyorCustodainNameAddressList  = getProbationAddressList();
        final Set<ResultPromptDynamicListNameAddress> crownCourtNameAddressList = getCrownCourtNameEmailList();
        final Set<ResultPromptDynamicListNameAddress> magsCourtNameAddressList = getMagsCourtNameEmailList();
        conveyorCustodainNameAddressList.addAll(crownCourtNameAddressList);
        conveyorCustodainNameAddressList.addAll(magsCourtNameAddressList);
        resultPromptDynamicListNameAddress .put(CONVEYOR_CUSTODAIN_KEY, conveyorCustodainNameAddressList);

    }

    private void setNCESWarrantNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(WARRANT_EXECUTION_ALLOCATED_KEY, resultsQueryService.getRegionalOrganisationNameAddress(jsonEnvelope).payload().getJsonArray("regionalOrganisations").getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString("regionName", null))
                        .withEmailAddress1(regionalOrganisation.getString("cbwaEnforcerEmail", null))
                        .build()).collect(toSet()));
    }

    private void setAttendanceCenterNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(ATTENDANCE_CENTER_KEY, resultsQueryService.getAttendanceCenterNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withAddressLine1(regionalOrganisation.getString(ADDRESS_1, null))
                        .withAddressLine2(regionalOrganisation.getString(ADDRESS_2, null))
                        .withAddressLine3(regionalOrganisation.getString(ADDRESS_3, null))
                        .withAddressLine4(regionalOrganisation.getString(ADDRESS_4,null))
                        .withAddressLine5(regionalOrganisation.getString(ADDRESS_5,null))
                        .withPostCode(regionalOrganisation.getString(POSTCODE, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet()));
    }

    private void setBassProviderNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(BASS_PROVIDER_KEY, resultsQueryService.getBASSProviderNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet()));
    }

    private void setEMCOrganisationNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        final Set<ResultPromptDynamicListNameAddress> resultPromptDynamicListNameAddressSet = resultsQueryService.getEMCOrganisationNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet());
        resultPromptDynamicListNameAddress.put(EMC_RESPONSIBLE_OFFICER_KEY, resultPromptDynamicListNameAddressSet);
        resultPromptDynamicListNameAddress.put(EMC_KEY, resultPromptDynamicListNameAddressSet);
    }

    private void setLocalAuthorityNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
    final Set<ResultPromptDynamicListNameAddress> localAuthorityAddress = resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withAddressLine1(regionalOrganisation.getString(ADDRESS_1, null))
                        .withAddressLine2(regionalOrganisation.getString(ADDRESS_2, null))
                        .withAddressLine3(regionalOrganisation.getString(ADDRESS_3, null))
                        .withAddressLine4(regionalOrganisation.getString(ADDRESS_4,null))
                        .withAddressLine5(regionalOrganisation.getString(ADDRESS_5,null))
                        .withPostCode(regionalOrganisation.getString(POSTCODE, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet());
        resultPromptDynamicListNameAddress.put(LOCAL_AUTHORITY_KEY, localAuthorityAddress);
        resultPromptDynamicListNameAddress.put(ORIG_DESG_LOCAL_AUTHORITY_KEY, localAuthorityAddress);
        resultPromptDynamicListNameAddress.put(NEW_DESG_LOCAL_AUTHORTY_KEY, localAuthorityAddress);

    }

    private void setNCESCashDeptNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        final Set<ResultPromptDynamicListNameAddress> localAuthorityAddress = resultsQueryService.getNCESNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withAddressLine1(regionalOrganisation.getString(ADDRESS_1, null))
                        .withAddressLine2(regionalOrganisation.getString(ADDRESS_2, null))
                        .withAddressLine3(regionalOrganisation.getString(ADDRESS_3, null))
                        .withAddressLine4(regionalOrganisation.getString(ADDRESS_4,null))
                        .withAddressLine5(regionalOrganisation.getString(ADDRESS_5,null))
                        .withPostCode(regionalOrganisation.getString(POSTCODE, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet());
        resultPromptDynamicListNameAddress.put(NCES_KEY, localAuthorityAddress);

    }

    private void setProbationNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(PROBATION_TEAM_TO_BE_NOTIFIED_KEY, getProbationAddressList());
    }

    private void setYOTSNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        resultPromptDynamicListNameAddress.put(YOTS_KEY, getYOTSAddressList());
    }

    private void setYOTSAndProbatiionNameAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        final Set<ResultPromptDynamicListNameAddress> probationAddressList = getProbationAddressList();
        final Set<ResultPromptDynamicListNameAddress> yotsAddressList = getYOTSAddressList();
        final Set<ResultPromptDynamicListNameAddress> yotsAndProbationAddressList = new HashSet<>();

        yotsAndProbationAddressList.addAll(probationAddressList);
        yotsAndProbationAddressList.addAll(yotsAddressList);
        resultPromptDynamicListNameAddress.put(PROBATION_YOT_TO_BE_NOTIFIED_KEY, yotsAndProbationAddressList);
    }

    private void setCourtAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
        final Set<ResultPromptDynamicListNameAddress> magsCourtNameAddressis = getMagsCourtNameAddressList();
        final Set<ResultPromptDynamicListNameAddress> crownCourtNameAddressList = getCrownCourtNameAddressList();
        final Set<ResultPromptDynamicListNameAddress> scottishNICourtNameAddressList = getScottishNICourtAddressList();
        final Set<ResultPromptDynamicListNameAddress> courtAddressList = new HashSet<>();

        courtAddressList.addAll(magsCourtNameAddressis);
        courtAddressList.addAll(crownCourtNameAddressList);
        courtAddressList.addAll(scottishNICourtNameAddressList);
        resultPromptDynamicListNameAddress.put(HCHOUSE_KEY, courtAddressList);
        resultPromptDynamicListNameAddress.put(COURT_HOUSE_NAME_KEY, courtAddressList);
        resultPromptDynamicListNameAddress.put(SCOTTISH_COURT_KEY, courtAddressList);
        resultPromptDynamicListNameAddress.put(HCHOUSE_ORGANISATION_NAME_KEY,courtAddressList);
    }

    private void setDrinkDrivingCourseProvidersAddress(final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress) {
         final Set<ResultPromptDynamicListNameAddress> drinkDriverPridversAddress = resultsQueryService.getDrinkDrivingCourseProvidersAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(drinkDrivingProvider -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(drinkDrivingProvider.getString(ORG_NAME, null))
                        .withAddressLine1(drinkDrivingProvider.getString(ADDRESS_1, null))
                        .withAddressLine2(drinkDrivingProvider.getString(ADDRESS_2, null))
                        .withAddressLine3(drinkDrivingProvider.getString(ADDRESS_3, null))
                        .withAddressLine4(drinkDrivingProvider.getString(ADDRESS_4,null))
                        .withAddressLine5(drinkDrivingProvider.getString(ADDRESS_5,null))
                        .withPostCode(drinkDrivingProvider.getString(POSTCODE, null))
                        .withEmailAddress1(drinkDrivingProvider.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet());
        resultPromptDynamicListNameAddress.put(NAME_OF_COURSE_KEY, drinkDriverPridversAddress);
    }

    private Set<ResultPromptDynamicListNameAddress> getYOTSAddressList() {
        return resultsQueryService.getYOTSNameAddress(jsonEnvelope).payload().getJsonArray("youthOffendingTeams").getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString("yotName", null))
                        .withEmailAddress1(regionalOrganisation.getString("yotEmailAddress", null))
                        .build()).collect(toSet());
    }

    private Set<ResultPromptDynamicListNameAddress> getProbationAddressList() {
        return resultsQueryService.getProbationNameAddress(jsonEnvelope).payload().getJsonArray(ORG_TYPE).getValuesAs(JsonObject.class).stream()
                .map(regionalOrganisation -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(regionalOrganisation.getString(ORG_NAME, null))
                        .withEmailAddress1(regionalOrganisation.getString(EMAIL_ADDRESS, null))
                        .build()).collect(toSet());
    }


    private Set<ResultPromptDynamicListNameAddress> getMagsCourtNameEmailList() {
        return resultsQueryService.getMagistrateCourtsNameAddress(jsonEnvelope).
                payload().getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class).stream()
                .map(courtAddress ->ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(courtAddress.getString(OUCODE_L_3_NAME, null) != null ? courtAddress.getString(OUCODE_L_3_NAME) +": PECS" : "PECS")
                        .withEmailAddress1(courtAddress.getString(PECS_CONTRACTOR_EMAIL, null))
                        .build()).collect(toSet());
    }

    private Set<ResultPromptDynamicListNameAddress> getCrownCourtNameEmailList() {
        return resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)
                .payload().getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class).stream()
                .map(courtAddress ->ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(courtAddress.getString(OUCODE_L_3_NAME, null) != null ? courtAddress.getString(OUCODE_L_3_NAME) +": PECS" : "PECS")
                        .withEmailAddress1(courtAddress.getString(PECS_CONTRACTOR_EMAIL, null))
                        .build()).collect(toSet());
    }

    private Set<ResultPromptDynamicListNameAddress> getMagsCourtNameAddressList() {
        return resultsQueryService.getMagistrateCourtsNameAddress(jsonEnvelope).
                payload().getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class).stream()
                .map(getCourtAddress()).collect(toSet());
    }

    private Set<ResultPromptDynamicListNameAddress> getCrownCourtNameAddressList() {
        return resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)
                .payload().getJsonArray(ORGANISATIONUNITS).getValuesAs(JsonObject.class).stream()
                .map(getCourtAddress()).collect(toSet());
    }

    private Set<ResultPromptDynamicListNameAddress> getScottishNICourtAddressList() {
        return resultsQueryService.getScottishCourtAddress(jsonEnvelope)
                .payload().getJsonArray("scottish-ni-courts").getValuesAs(JsonObject.class).stream()
                .map(courtAddress -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                        .withName(courtAddress.getString("courtName", null))
                        .withAddressLine1(courtAddress.getString(ADDRESS_1, null))
                        .withAddressLine2(courtAddress.getString(ADDRESS_2, null))
                        .withAddressLine3(courtAddress.getString(ADDRESS_3, null))
                        .withAddressLine4(courtAddress.getString(ADDRESS_4, null))
                        .withAddressLine5(courtAddress.getString(ADDRESS_5, null))
                        .withPostCode(courtAddress.getString(POST_CODE, null))
                        .build()).collect(toSet());

    }

    private Function<JsonObject, ResultPromptDynamicListNameAddress> getCourtAddress() {
        return courtAddress -> ResultPromptDynamicListNameAddress.resultPromptDynamicListNameAddressBuilder()
                .withName(courtAddress.getString(OUCODE_L_3_NAME, null))
                .withAddressLine1(courtAddress.getString(ADDRESS_1, null))
                .withAddressLine2(courtAddress.getString(ADDRESS_2, null))
                .withAddressLine3(courtAddress.getString(ADDRESS_3, null))
                .withAddressLine4(courtAddress.getString(ADDRESS_4, null))
                .withAddressLine5(courtAddress.getString(ADDRESS_5, null))
                .withPostCode(courtAddress.getString(POSTCODE1, null))
                .withEmailAddress1(courtAddress.getString(CPS_EMAIL_ADDRESS,null))
                .withEmailAddress2(courtAddress.getString(PECS_CONTRACTOR_EMAIL, null))
                .build();
    }



}
