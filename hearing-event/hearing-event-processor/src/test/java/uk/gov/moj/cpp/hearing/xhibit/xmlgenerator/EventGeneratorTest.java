package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.HearingEvent.hearingEvent;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event20903OptionType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.Before;
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

    private final DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = ofPattern("dd/MM/yy");
    private String eventDate = LocalDate.of(2020, 03, 19).toString();
    private ZonedDateTime lastModifiedTime;

    @Before
    public void setup() {
        lastModifiedTime = now();
    }

    @Test
    public void shouldGenerateCurrentStatus() {
        final HearingEvent hearingEvent = createHearingEvent();
        final String type = "10100";
        final CourtRoom courtRoom = new CourtRoom(null, null, hearingEvent, null);
        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = eventGenerator.generate(courtRoom);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.format(dateTimeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
    }

    @Test
    public void shouldGenerateCurrentStatusWithComplexEvent() {
        final HearingEvent hearingEvent = createHearingEvent();
        final String type = "20903";
        final DefenceCounsel defenceCounsel = DefenceCounsel.defenceCounsel().build();
        final CourtRoom courtRoom = new CourtRoom(null, null, hearingEvent, defenceCounsel);
        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = eventGenerator.generate(courtRoom);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.format(dateTimeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
        assertThat(currentstatus.getEvent().getE20903ProsecutionCaseOptions().getE20903PCOType(), is(Event20903OptionType.E_20903_PROSECUTION_OPENING));
    }

    @Test
    public void shouldGenerateCurrentStatusWithComplexEventDefenceCounsel() {
        final HearingEvent hearingEvent = createHearingEvent();
        final String type = "20906";
        final DefenceCounsel defenceCounsel = DefenceCounsel.defenceCounsel().withFirstName("Sid").withLastName("Sox").build();
        final CourtRoom courtRoom = new CourtRoom(null, null, hearingEvent, defenceCounsel);
        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = eventGenerator.generate(courtRoom);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.format(dateTimeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
        assertThat(currentstatus.getEvent().getE20906DefenceCOName(), is("Sid Sox"));
    }

    private HearingEvent createHearingEvent() {
        final UUID hearingEventId = randomUUID();
        final UUID hearingEventDefinitionId = randomUUID();
        final ZonedDateTime lastModifiedTime = now();

        return hearingEvent()
                .withLastModifiedTime(lastModifiedTime)
                .withEventDate(eventDate)
                .withId(hearingEventId)
                .withHearingEventDefinitionId(hearingEventDefinitionId)
                .build();
    }
}