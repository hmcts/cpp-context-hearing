package uk.gov.moj.cpp.hearing.event.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingServiceResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

        setField(provisionalBookingService, "objectMapper", new ObjectMapper());

        assertDoesNotThrow(() -> provisionalBookingService.releaseUnconfirmedBookings(List.of(randomUUID().toString())));
    }

    @Test
    public void shouldLogRatherThanThrowOnNonAcceptedResponse() throws Exception {
        givenCourtSchedulerReleaseResponds(400);

        assertDoesNotThrow(() -> provisionalBookingService.releaseUnconfirmedBookings(List.of(randomUUID().toString())));
    }

    /**
     * The bulk release, never DELETE /sessions/{id}: that one also releases by hearing_id and
     * takes confirmed listings with it. This endpoint gives back only unconfirmed holds.
     */
    @Test
    public void shouldPostEveryBookingIdToCourtSchedulersBulkReleaseInOneRequest() throws Exception {
        givenCourtSchedulerReleaseResponds(202);
        final String bookingA = randomUUID().toString();
        final String bookingB = randomUUID().toString();

        provisionalBookingService.releaseUnconfirmedBookings(List.of(bookingA, bookingB));

        assertThat(receivedRequests, hasSize(1));
        final ReceivedRequest request = receivedRequests.get(0);
        assertThat(request.method(), is("POST"));
        assertThat(request.contentType(), is("application/vnd.courtscheduler.release.unconfirmed.bookings+json"));
        assertThat(new ObjectMapper().readTree(request.body()),
                is(new ObjectMapper().readTree("{\"bookingIds\":[\"" + bookingA + "\",\"" + bookingB + "\"]}")));
    }

    @Test
    public void shouldNotCallCourtSchedulerWithNothingToRelease() throws Exception {
        givenCourtSchedulerReleaseResponds(202);

        provisionalBookingService.releaseUnconfirmedBookings(List.of());

        assertThat(receivedRequests, is(empty()));
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
        assertThat(response.getStatus(), is(400));
        assertThat(response.getErrorMessage(), containsString("400"));
        assertThat(response.getErrorMessage(), containsString("duration is required"));
    }

    /**
     * Whatever status courtscheduler answers with is passed on as is. Hearing does not interpret
     * it - which refusal it was, and what to tell the clerk, is the consumer's call.
     */
    @ParameterizedTest
    @ValueSource(ints = {400, 404, 409, 422, 500, 502, 503, 504})
    public void shouldPassCourtSchedulersStatusThroughAsIs(final int status) throws Exception {
        givenCourtSchedulerResponds(status, "{\"error\":\"refused\"}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.hasError(), is(true));
        assertThat(response.getStatus(), is(status));
        assertThat(response.getErrorMessage(), containsString(String.valueOf(status)));
        assertThat(response.getErrorMessage(), containsString("refused"));
    }

    /** A 200 that somehow carried no bookingId is still an error, reported with the 200 it came with. */
    @Test
    public void shouldReportASuccessWithoutABookingIdAsAnError() throws Exception {
        givenCourtSchedulerResponds(200, "{}");

        final ProvisionalBookingServiceResponse response = provisionalBookingService.bookSlots(emptyMap());

        assertThat(response.hasError(), is(true));
        assertThat(response.getStatus(), is(200));
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
        assertThat(response.getStatus(), is(nullValue()));
    }

    /**
     * A gateway or sidecar can answer with HTML, or with nothing at all. {@code new JSONObject}
     * throws {@link org.json.JSONException} on both — unchecked, so before the guard it escaped
     * the IOException catch, rolled the JMS transaction back and, after redelivery, dropped the
     * command on the DLQ: no public event at all, and a clerk left watching a spinner that never
     * resolves. It must degrade to an ordinary error, still carrying the status as is.
     */
    @Test
    public void shouldNotPropagateWhenBodyIsNotJson() throws Exception {
        givenCourtSchedulerResponds(502, "<html><body>Bad Gateway</body></html>");

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getStatus(), is(502));
        assertThat(response.getErrorMessage(), containsString("502"));
        assertThat(response.getErrorMessage(), containsString("Bad Gateway"));
    }

    /** Same again for a bodiless response, where {@code getEntity()} itself is null. */
    @Test
    public void shouldNotPropagateWhenResponseHasNoBodyAtAll() throws Exception {
        givenCourtSchedulerResponds(503, null);

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getStatus(), is(503));
        assertThat(response.getErrorMessage(), containsString("503"));
    }

    /**
     * No answer at all. Nothing listens on port 1, so connect() is refused immediately and the
     * real IOException path runs. The test at the top of this class proves releaseUnconfirmedBookings SWALLOWS
     * that exception; this one proves bookSlots reports it - with no status, as there was none.
     */
    @Test
    public void shouldReportAConnectionFailureWithoutAStatus() {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        setField(provisionalBookingService, "objectMapper", new ObjectMapper());
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:1");

        final ProvisionalBookingServiceResponse response =
                assertDoesNotThrow(() -> provisionalBookingService.bookSlots(emptyMap()));

        assertThat(response.hasError(), is(true));
        assertThat(response.getStatus(), is(nullValue()));
        // getMessage() is null on some IOException subclasses and the event schema requires
        // message, so it must never come back blank.
        assertThat(response.getErrorMessage(), is(notNullValue()));
    }

    private record ReceivedRequest(String method, String contentType, String body) {
    }

    private final List<ReceivedRequest> receivedRequests = new CopyOnWriteArrayList<>();

    /** A local courtscheduler double on {@code /unconfirmedBooking/release} that records what it was sent. */
    private void givenCourtSchedulerReleaseResponds(final int status) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/unconfirmedBooking/release", exchange -> {
            receivedRequests.add(new ReceivedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    new String(exchange.getRequestBody().readAllBytes(), UTF_8)));
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        httpServer.start();

        // lenient: a call with nothing to release must not even look the system user up
        lenient().when(systemUserProvider.getContextSystemUserId()).thenReturn(of(randomUUID()));
        setField(provisionalBookingService, "systemUserProvider", systemUserProvider);
        setField(provisionalBookingService, "objectMapper", new ObjectMapper());
        setField(provisionalBookingService, "baseUri", "http://127.0.0.1:" + httpServer.getAddress().getPort());
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
