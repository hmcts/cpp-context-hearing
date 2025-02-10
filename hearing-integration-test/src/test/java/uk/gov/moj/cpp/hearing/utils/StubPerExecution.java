package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubFixedListForWelshValues;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForReferenceDataResults;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubReferenceDataResultDefinitionWithCategory;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubRelistReferenceDataResults;
import static uk.gov.moj.cpp.hearing.utils.StagingEnforcementStub.stubEnforceFinancialImposition;
import static uk.gov.moj.cpp.hearing.utils.SystemIdMapperStub.stubAddMapping;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.mockMaterialUpload;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.mockUpdateHmpsMaterialStatus;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsWildcardUserBelongingToAllGroups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubPerExecution {
    private final static Logger LOGGER = LoggerFactory.getLogger(StubPerExecution.class);

    /**
     * This is per Maven Integration Test Execution Mocks.
     */
    public static void stubWireMock() {
        String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
        configureFor(HOST, 8080);
        reset();

        mockMaterialUpload();
        mockUpdateHmpsMaterialStatus();
        stubForReferenceDataResults();
        stubRelistReferenceDataResults();
        stubFixedListForWelshValues();
        setupAsWildcardUserBelongingToAllGroups();
        stubAddMapping();
        stubEnforceFinancialImposition();
        stubOrganisationUnit();
        stubReferenceDataResultDefinitionWithCategory();
    }

    public static void main(String[] args) {
        LOGGER.info("***************Started Stub Per Execution*************************");
        System.out.println("***************Started Stub Per Execution*************************");
        stubWireMock();
        System.out.println("***************Finished Stub Per Execution*************************");

    }

}