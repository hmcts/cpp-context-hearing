package uk.gov.moj.cpp.hearing.it;

import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.AuthorisationServiceStub.stubEnableAllCapabilities;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import org.json.JSONObject;
import uk.gov.justice.services.test.utils.core.rest.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.io.Resources;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public class AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);
    protected static final UUID USER_ID_VALUE = randomUUID();
    protected static final Header CPP_UID_HEADER = new Header(USER_ID, USER_ID_VALUE.toString());

    private static final String ENDPOINT_PROPERTIES_FILE = "endpoint.properties";
    protected static final Properties ENDPOINT_PROPERTIES = new Properties();
    protected static final String PUBLIC_EVENT_TOPIC = "public.event";

    protected static RequestSpecification requestSpec;
    protected static String baseUri;
    protected static RestClient restClient = new RestClient();

    private static final ThreadLocal<UUID> USER_CONTEXT = new ThreadLocal<>();

    @Before
    public void setUp() {
        readConfig();
        setRequestSpecification();
        setupAsAuthorisedUser(USER_ID_VALUE);
        stubEnableAllCapabilities();
    }

    protected JSONObject getExistingHearing(final String hearingId) {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId.toString());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing+json";

        final String payload = poll(requestParams(url, mediaType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK)).getPayload();
        return new JSONObject(payload);
    }


    protected static void setLoggedInUser(final UUID userId) {
        USER_CONTEXT.set(userId);
    }

    protected static UUID getLoggedInUser() {
        return USER_CONTEXT.get();
    }

    protected static MultivaluedMap<String, Object> getLoggedInHeader() {
        final MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add(USER_ID, getLoggedInUser().toString());
        return header;
    }

    private static void readConfig() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try(final InputStream stream = loader.getResourceAsStream(ENDPOINT_PROPERTIES_FILE)) {
            ENDPOINT_PROPERTIES.load(stream);
        } catch (final IOException e) {
            LOGGER.warn("Error reading properties from {}", ENDPOINT_PROPERTIES_FILE, e);
        }
        final String baseUriProp = System.getProperty("INTEGRATION_HOST_KEY");
        baseUri = isNotEmpty(baseUriProp) ? format("http://%s:8080", baseUriProp) : ENDPOINT_PROPERTIES.getProperty("base-uri");
    }

    private static void setRequestSpecification() {
        requestSpec = new RequestSpecBuilder().setBaseUri(baseUri).build();
    }
    protected String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path),
                defaultCharset());
    }
}
