package uk.gov.moj.cpp.hearing.helper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static uk.gov.moj.cpp.hearing.helper.FileUtil.getPayload;

import java.util.UUID;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

/**
 * Class to set up stub.
 */
public class StubUtil {

	private static final String HOST = System.getProperty("INTEGRATION_HOST_KEY", "localhost");
	private static final int HTTP_STATUS_OK = 200;

	static {
		configureFor(HOST, 8080);
		reset();
	}

	public static void setupUsersGroupDataActionClassificationStub() {
		InternalEndpointMockUtils.stubPingFor("usersgroups-query-api");
		stubFor(get(urlMatching("/usersgroups-query-api/query/api/rest/usersgroups/users/.*")).willReturn(aResponse()
				.withStatus(200).withHeader("CPPID", UUID.randomUUID().toString())
				.withHeader("Content-Type", "application/json").withBody(getPayload("users-groups-system-user.json"))));

	}

	

}
