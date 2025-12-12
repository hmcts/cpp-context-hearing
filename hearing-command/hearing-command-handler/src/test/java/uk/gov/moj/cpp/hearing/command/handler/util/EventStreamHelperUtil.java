package uk.gov.moj.cpp.hearing.command.handler.util;

import static org.mockito.Mockito.times;

import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class EventStreamHelperUtil {

    private EventStreamHelperUtil() {
    }

    public static List<JsonEnvelope> verifyAndGetEvents(final EventStream eventStream, int times) throws EventStreamException {
        ArgumentCaptor<Stream> argumentCaptor = ArgumentCaptor.forClass(Stream.class);
        ((EventStream) Mockito.verify(eventStream, times(times))).append((Stream) argumentCaptor.capture());

        List<JsonEnvelope> events = new ArrayList<>();
        for (Stream stream : argumentCaptor.getAllValues()) {
            events.addAll((List<JsonEnvelope>) stream.collect(Collectors.toList()));
        }

        return events;
    }
}
