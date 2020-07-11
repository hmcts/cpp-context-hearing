package uk.gov.justice.ccr.notepad.result.loader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.justice.ccr.notepad.util.FileUtil.givenPayload;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultPromptDynamicListNameAddress;
import uk.gov.justice.ccr.notepad.service.ResultsQueryService;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Map;
import java.util.Set;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NameAddressRefDataEndPointMapperTest {


    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Envelope<JsonObject> jsonEnvelopePrisonAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeProbationAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeCrownCourtAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeMagsCourtAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeRegionalAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeNCESAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeAttendanceAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeBassProviderAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeEMCAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeLocalAuthorityAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopYOTSAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeScottishAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeDrinkDriverAddress;

    @Mock
    private Envelope<JsonObject> jsonEnvelopeLocalJusticeAreaAddress;

    @Mock
    private ResultsQueryService resultsQueryService;

    @InjectMocks
    private NameAddressRefDataEndPointMapper nameAddressRefDataEndPointMapper;

    private static final String PRISON_KEY = "prison";
    private static final String CONVEYOR_CUSTODAIN_KEY = "conveyorcustodianname";
    private static final String WARRANT_EXECUTION_ALLOCATED_KEY = "warrantexecutionallocatedto";
    private static final String ATTENDANCE_CENTER_KEY = "attendancecentrenamecontact";
    private static final String BASS_PROVIDER_KEY = "bassprovider";
    private static final String EMC_RESPONSIBLE_OFFICER_KEY = "electronicmonitoringcontractorresponsibleofficer";
    private static final String LOCAL_AUTHORITY_KEY = "localauthoritynameandaddress";
    private static final String ORIG_DESG_LOCAL_AUTHORITY_KEY = "originaldesignatedlocalauthorityfortheremand";
    private static final String NEW_DESG_LOCAL_AUTHORTY_KEY = "thenewdesignatedlocalauthoritydesignatedis";
    private static final String NCES_KEY = "nces";
    private static final String PROBATION_TEAM_TO_BE_NOTIFIED_KEY = "probationteamtobenotified";
    private static final String YOTS_KEY = "youthoffendingteamtobenotified";
    private static final String PROBATION_YOT_TO_BE_NOTIFIED_KEY = "probationyottobenotified";
    private static final String HCHOUSE_KEY = "hchouse";
    private static final String COURT_HOUSE_NAME_KEY = "courthousename";
    private static final String HCHOUSE_ORGANISATION_NAME_KEY = "hchouseorganisationname";
    private static final String SCOTTISH_COURT_KEY = "scottishcourt";
    private static final String NAME_OF_COURSE_KEY = "nameofcourse";
    private static final String ORIGINAL_COURT_KEY = "originalcourt";

    @Test
    public void loadAllNameAddressFromRefDataTest() throws Exception {

        given(resultsQueryService.getPrisonNameAddress(jsonEnvelope)).willReturn(jsonEnvelopePrisonAddress);
        given(resultsQueryService.getProbationNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeProbationAddress);
        given(resultsQueryService.getCrownCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeCrownCourtAddress);
        given(resultsQueryService.getMagistrateCourtsNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeMagsCourtAddress);
        given(resultsQueryService.getRegionalOrganisationNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeRegionalAddress);
        given(resultsQueryService.getAttendanceCenterNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeAttendanceAddress);
        given(resultsQueryService.getBASSProviderNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeBassProviderAddress);
        given(resultsQueryService.getEMCOrganisationNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeEMCAddress);
        given(resultsQueryService.getLocalAuthorityNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeLocalAuthorityAddress);
        given(resultsQueryService.getNCESNameAddress(jsonEnvelope)).willReturn(jsonEnvelopeNCESAddress);
        given(resultsQueryService.getYOTSNameAddress(jsonEnvelope)).willReturn(jsonEnvelopYOTSAddress);
        given(resultsQueryService.getScottishCourtAddress(jsonEnvelope)).willReturn(jsonEnvelopeScottishAddress);
        given(resultsQueryService.getDrinkDrivingCourseProvidersAddress(jsonEnvelope)).willReturn(jsonEnvelopeDrinkDriverAddress);
        given(resultsQueryService.getLocalJusticeAreas(jsonEnvelope)).willReturn(jsonEnvelopeLocalJusticeAreaAddress);



        given(jsonEnvelopePrisonAddress.payload())
                .willReturn(givenPayload("/referencedata.prisons.json"));
        given(jsonEnvelopeProbationAddress.payload())
                .willReturn(givenPayload("/referencedata.probation-address.json"));
        given(jsonEnvelopeCrownCourtAddress.payload())
                .willReturn(givenPayload("/referencedata.crown-court-name-address.json"));
        given(jsonEnvelopeMagsCourtAddress.payload())
                .willReturn(givenPayload("/referencedata.mags-court-name-address.json"));
        given(jsonEnvelopeRegionalAddress.payload())
                .willReturn(givenPayload("/referencedata.regional-address.json"));
        given(jsonEnvelopeAttendanceAddress.payload())
                .willReturn(givenPayload("/referencedata.attendance-center-address.json"));
        given(jsonEnvelopeBassProviderAddress.payload())
                .willReturn(givenPayload("/referencedata.bass-provider-address.json"));
        given(jsonEnvelopeEMCAddress.payload())
                .willReturn(givenPayload("/referencedata.EMC-address.json"));
        given(jsonEnvelopeLocalAuthorityAddress.payload())
                .willReturn(givenPayload("/referencedata.local-authority-address.json"));
        given(jsonEnvelopeNCESAddress.payload())
                .willReturn(givenPayload("/referencedata.NCES-address.json"));
        given(jsonEnvelopYOTSAddress.payload())
                .willReturn(givenPayload("/referencedata.youth-offending-teams-address.json"));
        given(jsonEnvelopeScottishAddress.payload())
                .willReturn(givenPayload("/referencedata.scottis-ni-courts.json"));
        given(jsonEnvelopeDrinkDriverAddress.payload())
                .willReturn(givenPayload("/referencedata.drink-driving-course-providers.json"));
        given(jsonEnvelopeLocalJusticeAreaAddress.payload())
                .willReturn(givenPayload("/referencedata.local-justice-area-address.json"));

        final Map<String, Set<ResultPromptDynamicListNameAddress>> resultPromptDynamicListNameAddress = nameAddressRefDataEndPointMapper.loadAllNameAddressFromRefData();

        assertThat(resultPromptDynamicListNameAddress.get(PRISON_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(CONVEYOR_CUSTODAIN_KEY).size(), is(6));
        assertThat(resultPromptDynamicListNameAddress.get(WARRANT_EXECUTION_ALLOCATED_KEY).size(), is(1));
        assertThat(resultPromptDynamicListNameAddress.get(ATTENDANCE_CENTER_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(BASS_PROVIDER_KEY).size(), is(1));
        assertThat(resultPromptDynamicListNameAddress.get(EMC_RESPONSIBLE_OFFICER_KEY).size(), is(1));
        assertThat(resultPromptDynamicListNameAddress.get(LOCAL_AUTHORITY_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(ORIG_DESG_LOCAL_AUTHORITY_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(NEW_DESG_LOCAL_AUTHORTY_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(NCES_KEY).size(), is(1));
        assertThat(resultPromptDynamicListNameAddress.get(PROBATION_TEAM_TO_BE_NOTIFIED_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(YOTS_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(PROBATION_YOT_TO_BE_NOTIFIED_KEY).size(), is(4));
        assertThat(resultPromptDynamicListNameAddress.get(COURT_HOUSE_NAME_KEY).size(), is(6));
        assertThat(resultPromptDynamicListNameAddress.get(HCHOUSE_KEY).size(), is(6));
        assertThat(resultPromptDynamicListNameAddress.get(HCHOUSE_ORGANISATION_NAME_KEY).size(), is(6));
        assertThat(resultPromptDynamicListNameAddress.get(SCOTTISH_COURT_KEY).size(), is(6));
        assertThat(resultPromptDynamicListNameAddress.get(NAME_OF_COURSE_KEY).size(), is(2));
        assertThat(resultPromptDynamicListNameAddress.get(ORIGINAL_COURT_KEY).size(), is(3));

    }

}
