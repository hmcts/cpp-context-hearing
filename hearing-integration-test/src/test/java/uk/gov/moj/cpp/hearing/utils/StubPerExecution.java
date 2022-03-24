package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub.stubDocumentCreate;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubFixedListForWelshValues;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForReferenceDataResults;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubRelistReferenceDataResults;
import static uk.gov.moj.cpp.hearing.utils.StagingEnforcementStub.stubEnforceFinancialImposition;
import static uk.gov.moj.cpp.hearing.utils.SystemIdMapperStub.stubAddMapping;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.mockMaterialUpload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.mockUpdateHmpsMaterialStatus;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsWildcardUserBelongingToAllGroups;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubPerExecution {
    public static final String DOCUMENT_TEXT = "someDocumentText";
    private final static Logger LOGGER = LoggerFactory.getLogger(StubPerExecution.class);

    /**
     * This is per Maven Integration Test Execution Mocks.
     */
    public static void stubWireMock() {
        String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
        configureFor(HOST, 8080);
        WireMock.reset();

        InternalEndpointMockUtils.stubPingFor(SystemIdMapperStub.SERVICE_NAME);
        InternalEndpointMockUtils.stubPingFor("referencedata-service");
        InternalEndpointMockUtils.stubPingFor("authorisation-service-server");
        InternalEndpointMockUtils.stubPingFor("progression-service");
        InternalEndpointMockUtils.stubPingFor("material-service");
        InternalEndpointMockUtils.stubPingFor("resultinghmps-service");
        InternalEndpointMockUtils.stubPingFor("usersgroups-service");

        mockMaterialUpload();
        mockUpdateHmpsMaterialStatus();
        stubForReferenceDataResults();
        stubRelistReferenceDataResults();
        stubFixedListForWelshValues();
        stubDocumentCreate(DOCUMENT_TEXT);
        setupAsWildcardUserBelongingToAllGroups();
        stubAddMapping();
        stubEnforceFinancialImposition();
        stubOrganisationUnit();
    }

    public static void main(String[] args) {
        LOGGER.info("***************Started Stub Per Execution*************************");
        System.out.println("***************Started Stub Per Execution*************************");
        stubWireMock();
        System.out.println("***************Finished Stub Per Execution*************************");

    }

}