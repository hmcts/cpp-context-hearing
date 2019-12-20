package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.HearingEvent.hearingEvent;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventGeneratorTest {

    @Mock
    private XhibitEventMapperCache eventMapperCache;

    @InjectMocks
    private EventGenerator eventGenerator;


    @Test
    public void shouldGenerateCurrentStatus() {

        final DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");


        final UUID hearingEventId = randomUUID();
        final ZonedDateTime lastModifiedTime = now();
        final String eventDate = "123";
        final String type = "10100";

        final HearingEvent hearingEvent = hearingEvent()
                .withLastModifiedTime(lastModifiedTime)
                .withEventDate(eventDate)
                .withId(hearingEventId)
                .build();

        when(eventMapperCache.getXhibitEventCodeBy(hearingEventId.toString())).thenReturn(type);

        final Currentstatus currentstatus = eventGenerator.generate(hearingEvent);

        assertThat(currentstatus.getEvent().getDate(), is(eventDate));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.format(dateTimeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
    }
}