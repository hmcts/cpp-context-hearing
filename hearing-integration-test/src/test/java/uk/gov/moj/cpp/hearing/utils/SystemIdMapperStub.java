package uk.gov.moj.cpp.hearing.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.jayway.restassured.path.json.JsonPath.from;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_OK;

import uk.gov.justice.service.wiremock.testutil.InternalEndpointMockUtils;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.restassured.path.json.JsonPath;

public class SystemIdMapperStub {

    public static final String SERVICE_NAME = "system-id-mapper-api";
    private static final String ADD_MAPPING_PATH = "/" + SERVICE_NAME + "/rest/systemid/mappings";

    public static void stubAddMapping() {

        stubFor(post(urlPathEqualTo(ADD_MAPPING_PATH))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withBody(createObjectBuilder().add("id", randomUUID().toString()).add("code", "OK").build().toString())
                ));
    }

    public static void stubMappingForNowsRequestId(String nowIdAsSource, String hearingIdAsTarget) {
        stubFor(get(urlPathEqualTo(ADD_MAPPING_PATH))
                .withQueryParam("sourceId", equalTo(nowIdAsSource))
                .willReturn(aResponse().withStatus(SC_OK)
                        .withHeader("CPPID", randomUUID().toString())
                        .withBody(createObjectBuilder().add("targetId", hearingIdAsTarget).build().toString())
                ));
    }

    public static List<String> findNowsIdForGivenHearingIdFromSystemMapper(final UUID hearingId, ZonedDateTime issuedAfter) {
        //fetching all requests issued against system mapper for the given hearing ID after a specific time
        final List<LoggedRequest> all = findAll(postRequestedFor(urlPathMatching(ADD_MAPPING_PATH)));
        return all.stream()
                .filter(lr -> {
                    final JsonPath requestBody = from(lr.getBodyAsString());
                    final boolean after = lr.getLoggedDate().after(Date.from(issuedAfter.toInstant()));
                    return hearingId.toString().equals(requestBody.get("targetId")) && after;

                })
                .map(lr -> (String) from(lr.getBodyAsString()).get("sourceId"))
                .collect(Collectors.toList()
                );
    }


}
