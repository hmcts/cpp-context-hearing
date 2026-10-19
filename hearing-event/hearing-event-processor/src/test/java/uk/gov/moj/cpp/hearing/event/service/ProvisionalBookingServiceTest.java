package uk.gov.moj.cpp.hearing.event.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingFailureCode;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link ProvisionalBookingService} is not written to be unit-testable in isolation: it builds
 * its own {@code HttpClientBuilder} inline rather than accepting an injectable client, and its
 * collaborators are plain {@code @Inject} fields rather than constructor parameters. Rather than
 * reshaping the class to suit a test, these tests instantiate it directly and use the framework's
 * {@code ReflectionUtil.setField} (already the convention elsewhere in this module's tests, e.g.
 * {@link BookProvisionalHearingSlotsProcessorTest}) to inject {@code systemUserProvider} and
 * {@code baseUri}, then exercise the real HTTP call against either a closed port (to provoke a
 * genuine {@link java.io.IOException}) or a local {@link HttpServer} (to provoke a genuine
 * non-2xx response). This proves the swallow-the-failure contract end to end without mocking
 * away the thing under test.
 */
@ExtendWith(MockitoExtension.class)
public class ProvisionalBookingServiceTest {

    @Mock
    private SystemUserProvider systemUserProvider;

    private HttpServer httpServer;

    private final ProvisionalBookingService provisionalBookingService = new ProvisionalBookingService();

    @AfterEach
    public void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    public void shouldSwallowIOExceptionRatherThanPropagateWhenCourtSchedulerIsUnreachable() {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        // Nothing listens on port 1: the connection is refused immediately, giving a
        // deterministic IOException without needing a real courtscheduler double.
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:1");

        assertDoesNotThrow(() -> provisionalBookingService.releaseSlots(randomUUID().toString()));
    }

    @Test
    public void shouldLogRatherThanThrowOnNonAcceptedResponse() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/sessions", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        httpServer.start();

        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:" + httpServer.getAddress().getPort());

        assertDoesNotThrow(() -> provisionalBookingService.releaseSlots(randomUUID().toString()));
    }

    /**
     * The failure that cost two debugging sessions on STE. courtscheduler refuses a booking with
     * 400 and says exactly why in the body; the service used to log only the status code and
     * report "{...} has no bookingId" downstream, so the reason reached nobody — the public event
     * carried no bookingId and the API test died on {@code JSONObject["bookingId"] not found}
     * with nothing to go on. The message must carry BOTH the status and courtscheduler's text.
     */
    @Test
    public void shouldCarryStatusAndCourtSchedulerMessageWhenBookingIsRejected() throws Exception {
        final String rejection =
                "{\"error\":[\"duration is required and must be greater than zero for duration-based session abc\"]}";
        givenCourtSchedulerResponds(400, rejection);

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.hasError(), is(true));
        assertThat(response.getErrorMessage(), containsString("400"));
        assertThat(response.getErrorMessage(), containsString("duration is required"));
        // A 400 means WE built a request courtscheduler's validator rejected. Blaming the
        // session would send the clerk off to re-pick a session that was never the problem.
        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
    }

    /**
     * 409 from {@code NoCapacityException}: the session genuinely cannot hold the booking. This
     * is the ONE code that entitles a consumer to tell the clerk to pick another session.
     */
    @Test
    public void shouldClassifyACapacityConflictAsNoCapacity() throws Exception {
        givenCourtSchedulerResponds(409,
                "{\"error\":\"Could not reserve sessions for booking b-1: no remaining capacity\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.NO_CAPACITY));
        assertThat(response.getErrorMessage(), containsString("409"));
    }

    /**
     * The other 409. courtscheduler maps {@code ConfirmedBookingExistsException} to the SAME
     * status as a capacity refusal and normalises both to {@code {"error":"<msg>"}}, so the only
     * discriminator is the message text - see ProvisionalBookingService.CONFIRMED_ALLOCATION_MARKER.
     * This test is what fails first if that message is reworded upstream.
     */
    @Test
    public void shouldDistinguishAConfirmedBookingConflictFromACapacityConflict() throws Exception {
        givenCourtSchedulerResponds(409,
                "{\"error\":\"Booking b-1 already has a confirmed allocation \\u2014 cannot reserve\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.CONFIRMED_BOOKING_EXISTS));
    }

    /** 422 from {@code NoSessionAvailableException}: the session is gone, not full. */
    @Test
    public void shouldClassifyAnUnknownSessionAsNoSessionAvailable() throws Exception {
        givenCourtSchedulerResponds(422, "{\"error\":\"No session found for sessionId abc\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.NO_SESSION_AVAILABLE));
    }

    /**
     * A 200 that somehow carried no bookingId is a contract violation, not a refusal of the
     * session - it must not reach the clerk as "pick another one".
     */
    @Test
    public void shouldClassifyASuccessWithoutABookingIdAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(200, "{}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.hasError(), is(true));
        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
    }

    /**
     * A 200 whose body carries a bookingId is the normal path, and must stay unaffected by the
     * error handling around it.
     */
    @Test
    public void shouldReturnBookingIdOnSuccess() throws Exception {
        final String bookingId = randomUUID().toString();
        givenCourtSchedulerResponds(200, "{\"bookingId\":\"" + bookingId + "\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.hasError(), is(false));
        assertThat(response.getBookingId(), is(bookingId));
    }

    /**
     * A gateway or sidecar can answer with HTML, or with nothing at all. {@code new JSONObject}
     * throws {@link org.json.JSONException} on both — unchecked, so before the guard it escaped
     * the IOException catch, rolled the JMS transaction back and, after redelivery, dropped the
     * command on the DLQ: no public event at all, and a clerk left watching a spinner that never
     * resolves. It must degrade to an ordinary error instead.
     */
    @Test
    public void shouldNotPropagateWhenBodyIsNotJson() throws Exception {
        givenCourtSchedulerResponds(502, "<html><body>Bad Gateway</body></html>");

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getErrorMessage(), containsString("502"));
        // The body is not JSON, so this is not courtscheduler answering and its status is not
        // read as if it were - only kept in the reason for the logs.
        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
    }

    /**
     * An unparseable body is COURTSCHEDULER_ERROR WHATEVER the status says. courtscheduler's own
     * refusals always carry the JSON error body, so a 409 that is not JSON came from something in
     * front of it and is no evidence at all that the session is full. This is the case that makes
     * the rule worth having: read as NO_CAPACITY it would tell the clerk a free session is full.
     */
    @Test
    public void shouldNotReadACapacityRefusalOutOfANonJsonConflict() throws Exception {
        givenCourtSchedulerResponds(409, "<html><body>Conflict</body></html>");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
    }

    /** Same again for a bodiless response, where {@code getEntity()} itself is null. */
    @Test
    public void shouldNotPropagateWhenResponseHasNoBodyAtAll() throws Exception {
        givenCourtSchedulerResponds(503, null);

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getErrorMessage(), containsString("503"));
        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
    }

    /**
     * Every status other than 409 and 422 is one technical code (LPT-2590); the status itself is
     * kept in the reason, so the logs still tell our bad request from their outage.
     */
    @Test
    public void shouldClassifyAServerErrorAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(500, "{\"error\":\"Internal Server Error\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        assertThat(response.getErrorMessage(), containsString("500"));
    }

    /** 502 with a JSON body: courtscheduler's own upstream call failed. */
    @Test
    public void shouldClassifyAnUpstreamFailureAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(502, "{\"error\":\"Upstream service responded with 500\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        assertThat(response.getErrorMessage(), containsString("502"));
    }

    /** 503 and 504: the request never reached courtscheduler's logic. */
    @Test
    public void shouldClassifyAnUnreachableServiceAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(503, "{\"error\":\"Service Unavailable\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        assertThat(response.getErrorMessage(), containsString("503"));
    }

    @Test
    public void shouldClassifyAGatewayTimeoutAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(504, "{\"error\":\"Gateway Timeout\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        assertThat(response.getErrorMessage(), containsString("504"));
    }

    /**
     * A status the mapping does not name - 404 here, but equally a 401 from a misconfigured
     * gateway. The code set has to stay closed, because it is a published schema enum.
     */
    @Test
    public void shouldClassifyAnUnmappedStatusAsCourtSchedulerError() throws Exception {
        givenCourtSchedulerResponds(404, "{\"error\":\"Not Found\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        assertThat(response.getErrorMessage(), containsString("404"));
    }

    /**
     * No answer at all. Nothing listens on port 1, so connect() is refused immediately and the
     * real IOException path runs. The test at the top of this class proves
     * releaseSlots SWALLOWS that exception; this one proves bookSlots CLASSIFIES it.
     */
    @Test
    public void shouldClassifyAConnectionFailureAsCourtSchedulerError() {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        setField(provisionalBookingService, "objectMapper", new ObjectMapper());
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:1");

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getErrorCode(), is(ProvisionalBookingFailureCode.COURTSCHEDULER_ERROR));
        // getMessage() is null on some IOException subclasses and the event schema requires
        // reason, so it must never come back blank.
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    /**
     * Stands up a local courtscheduler double on {@code /unconfirmedBooking} answering with the
     * given status and body, and points the service at it. A null body sends no entity at all.
     */
    private void givenCourtSchedulerResponds(final int status, final String body) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/unconfirmedBooking", exchange -> {
            if (body == null) {
                exchange.sendResponseHeaders(status, -1);
            } else {
                final byte[] bytes = body.getBytes(UTF_8);
                exchange.sendResponseHeaders(status, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        httpServer.start();

        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        setField(provisionalBookingService, "objectMapper", new ObjectMapper());
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:" + httpServer.getAddress().getPort());
    }
}
