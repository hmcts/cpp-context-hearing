package uk.gov.moj.cpp.hearing.it;

import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.stubHearingEventDefinitions;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsSystemUser;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.hearing.courts.referencedata.Address;
import uk.gov.justice.hearing.courts.referencedata.EnforcementArea;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeArea;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreas;
import uk.gov.justice.hearing.courts.referencedata.LocalJusticeAreasResult;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.justice.services.test.utils.persistence.TestJdbcConnectionProvider;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;
import uk.gov.moj.cpp.hearing.utils.StubPerExecution;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.google.common.io.Resources;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractIT {

    public static final Properties ENDPOINT_PROPERTIES = new Properties();
    protected static final UUID USER_ID_VALUE = randomUUID();
    protected static final UUID USER_ID_VALUE_AS_ADMIN = fromString("46986cb7-eefa-48b3-b7e2-34431c3265e5");
    protected static final Header CPP_UID_HEADER = new Header(USER_ID, USER_ID_VALUE.toString());
    protected static final Header CPP_UID_HEADER_AS_ADMIN = new Header(USER_ID, USER_ID_VALUE_AS_ADMIN.toString());
    protected static final String PUBLIC_EVENT_TOPIC = "public.event";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIT.class);
    private static final String ENDPOINT_PROPERTIES_FILE = "endpoint.properties";
    /**
     * todo this is not a good pattern, only for fixing parallel runs without changing existing
     * codes too much
     */
    private static final ThreadLocal<UUID> USER_ID_CONTEXT = ThreadLocal.withInitial(UUID::randomUUID);
    private static final ThreadLocal<UUID> ADMIN_USER_ID_CONTEXT = ThreadLocal.withInitial(UUID::randomUUID);
    protected static RequestSpecification requestSpec;
    private static final TestJdbcConnectionProvider testJdbcConnectionProvider = new TestJdbcConnectionProvider();
    private static String baseUri;

    protected static String ouId1 = "80921334-2cf0-4609-8a29-0921bf6b3520";
    protected static String ouId2 = "7e967376-eacf-4fca-9b30-21b0c5aad427";
    protected static String ouId3 = "7e967376-eacf-4fca-9b30-21b0c5aad428";
    protected static String ouId4 = "7e967376-eacf-4fca-9b30-21b0c5aad429";

    /**
     * In case of Single Test executions, initiation of Stubs Per Execution
     */
    static {
        if (Boolean.FALSE.toString().equalsIgnoreCase(
                System.getProperty("stubPerExecution", "false")))
            StubPerExecution.stubWireMock();
        setUpJvm();
    }

    public static UUID getLoggedInUser() {
        return USER_ID_CONTEXT.get();
    }

    protected static void setLoggedInUser(final UUID userId) {
        USER_ID_CONTEXT.set(userId);
    }

    public static UUID getLoggedInAdminUser() {
        return ADMIN_USER_ID_CONTEXT.get();
    }

    protected static void setLoggedInAdminUser(final UUID userId) {
        ADMIN_USER_ID_CONTEXT.set(userId);
    }


    public static Header getLoggedIdUserHeader() {
        return new Header(USER_ID, getLoggedInUser().toString());
    }

    protected static MultivaluedMap<String, Object> getLoggedInHeader() {
        final MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add(USER_ID, getLoggedInUser().toString());
        return header;
    }

    protected static MultivaluedMap<String, Object> getLoggedInSystemUserHeader() {
        final MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add(USER_ID, USER_ID_VALUE_AS_ADMIN);
        return header;
    }

    private static void readConfig() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = loader.getResourceAsStream(ENDPOINT_PROPERTIES_FILE)) {
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

    protected static Matcher<String> equalStr(final Object bean, final String name) {
        return equalTo(getString(bean, name));
    }

    protected static Matcher<String> equalStr(final Object bean, final char separator, final String... names) {
        return equalTo(StringUtils.join(Stream.of(names).map(name -> getString(bean, name).trim()).collect(toList()), separator).trim());
    }

    protected static Matcher<String> equalStr(final Object bean, final String name, final DateTimeFormatter dateTimeFormatter) {
        return equalTo(getString(bean, name, dateTimeFormatter));
    }

    protected static Matcher<Integer> equalInt(final Object bean, final String name) {
        return equalTo(getInteger(bean, name));
    }

    protected static Matcher<String> equalDate(final Temporal localDate) {
        return equalTo(ISO_LOCAL_DATE.format(localDate));
    }

    protected static Matcher<String> equalEnum(final Enum<?> e) {
        return equalTo(e.name());
    }

    protected static String getString(final Object bean, final String name, final DateTimeFormatter dateTimeFormatter) {
        try {
            final Temporal dateTime = (Temporal) PropertyUtils.getNestedProperty(bean, name);
            if (dateTime instanceof LocalDate) {
                return dateTimeFormatter.format(((LocalDate) dateTime).atStartOfDay());
            }
            return dateTimeFormatter.format(dateTime);
        } catch (final Exception e) {
            LOGGER.error("Cannot get string property: " + name + " from bean " + bean.getClass().getCanonicalName(), e.getMessage(), e);
            return EMPTY;
        }
    }

    protected static UUID getUUID(final Object bean, final String name) {
        return UUID.fromString(getString(bean, name));
    }

    protected static String getString(final Object bean, final String name) {
        try {
            return EMPTY + PropertyUtils.getNestedProperty(bean, name);
        } catch (final Exception e) {
            LOGGER.error("Cannot get string property: " + name + " from bean " + bean.getClass().getCanonicalName(), e.getMessage(), e);
            return EMPTY;
        }
    }

    protected static Integer getInteger(final Object bean, final String name) {
        try {
            return Integer.parseInt(getString(bean, name));
        } catch (final Exception e) {
            LOGGER.error("Cannot get integer property: " + name + " from bean " + bean.getClass().getCanonicalName(), e.getMessage(), e);
            return null;
        }
    }

    protected static String getStringFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }

    protected static String getURL(final String property, final Object... args) {
        return getBaseUri() + "/" + MessageFormat.format(ENDPOINT_PROPERTIES.getProperty(property), args);
    }

    public static Matcher<ResponseData> print() {
        return new BaseMatcher<ResponseData>() {
            @Override
            public boolean matches(final Object o) {
                if (o instanceof ResponseData) {
                    final ResponseData responseData = (ResponseData) o;
                }
                return true;
            }

            @Override
            public void describeTo(final Description description) {
            }
        };
    }

    /**
     * Per Jvm Setup
     */
    public static void setUpJvm() {
        truncateViewStoreTables("heda_hearing_event_definition");
        readConfig();
        setRequestSpecification();
        stubHearingEventDefinitions();
    }

    @BeforeClass
    public static void setUpClass() {

    }

    public static RequestSpecification getRequestSpec() {
        return requestSpec;
    }

    @Before
    public void setUpPerTest() {
        setupAsAuthorisedUser(getLoggedInUser());
        setupAsSystemUser(getLoggedInAdminUser());
    }

    protected JSONObject getExistingHearing(final String hearingId) {
        final String queryAPIEndPoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing"), hearingId.toString());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        final String mediaType = "application/vnd.hearing.get.hearing+json";

        final String payload = poll(requestParams(url, mediaType).withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .until(status().is(OK)).getPayload();
        return new JSONObject(payload);
    }

    protected void stubLjaDetails(final CourtCentre courtCentre, final UUID prosecutionAuthorityId) {
        final String ljaCode = String.format("%04d", Integer.valueOf(Double.valueOf(Math.random() * 10000).intValue()));

        final EnforcementAreaBacs enforcementAreaBacs = EnforcementAreaBacs.enforcementAreaBacs()
                .withBankAccntName("account name")
                .withBankAccntNum(1)
                .withBankAccntSortCode("867878")
                .withBankAddressLine1("address1")
                .withRemittanceAdviceEmailAddress("test@test.com")
                .build();

        final OrganisationalUnit organisationalUnit = OrganisationalUnit.organisationalUnit()
                .withOucode(ljaCode)
                .withLja(ljaCode)
                .withId(courtCentre.getId().toString())
                .withIsWelsh(true)
                .withWelshAddress1("Welsh address1")
                .withWelshAddress2("Welsh address2")
                .withWelshAddress3("Welsh address3")
                .withWelshAddress4("Welsh address4")
                .withWelshAddress4("Welsh address5")
                .withPostcode("Post Code")
                .withOucodeL3WelshName(courtCentre.getWelshName())
                .withEnforcementArea(enforcementAreaBacs)
                .build();

        final EnforcementArea enforcementArea = EnforcementArea.enforcementArea()
                .withLocalJusticeArea(LocalJusticeArea.localJusticeArea()
                        .withName("ljaName" + ljaCode)
                        .withNationalCourtCode("123")
                        .build())
                .withAccountDivisionCode(5673)
                .withEmail("enforcement" + ljaCode + "@gov.com")
                .withNationalPaymentPhone("07802683993")
                .withAddress1("address1 " + ljaCode)
                .withAddress2("address2 " + ljaCode)
                .withAddress3("address3 " + ljaCode)
                .withAddress4("address4 " + ljaCode)
                .withPostcode("AL4 9LG")
                .withPhone("123456789")
                .withNationalPaymentPhone("12344566")
                .build();

        LocalJusticeAreasResult localJusticeAreasResult = LocalJusticeAreasResult.localJusticeAreasResult()
                .withLocalJusticeAreas(Arrays.asList(
                        LocalJusticeAreas.localJusticeAreas()
                                .withNationalCourtCode("123")
                                .withWelshName("testetestetes")
                                .build()
                ))
                .build();

        Prosecutor prosecutor = Prosecutor.prosecutor()
                .withId(prosecutionAuthorityId.toString())
                .withFullName("Prosecutor Name")
                .withOucode("OuCode")
                .withMajorCreditorCode("Major creditor code")
                .withAddress(Address.address()
                        .withAddress1("Address1")
                        .withPostcode("AA30BB").build())
                .withInformantEmailAddress("dummy@email.com")
                .build();

        ReferenceDataStub.stub(organisationalUnit);

        ReferenceDataStub.stub(enforcementArea, ljaCode);

        ReferenceDataStub.stub(prosecutor);

        ReferenceDataStub.stub(localJusticeAreasResult, "123");

    }

    protected static void truncateViewStoreTables(final String... tableNameNames) {
        try (final Connection connection = testJdbcConnectionProvider.getViewStoreConnection("hearing")) {

            for (final String tableName : tableNameNames) {
                final String sql = String.format("truncate table %s cascade", tableName);

                try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.executeUpdate();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
