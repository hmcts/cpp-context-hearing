package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.core.dispatcher.SystemUserProvider;

import java.net.InetSocketAddress;

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
}
