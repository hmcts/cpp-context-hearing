package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.HearingEvent.hearingEvent;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event20903OptionType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublicDisplayEventGeneratorTest {
    @Mock
    private XhibitEventMapperCache eventMapperCache;

    @InjectMocks
    private PublicDisplayEventGenerator publicDisplayEventGenerator;

    private final DateTimeFormatter timeFormatter = ofPattern("HH:mm");
    private static final ZoneId localZoneId = ZoneId.of("Europe/London");
    private String eventDate = LocalDate.of(2020, 03, 19).toString();
    private ZonedDateTime lastModifiedTime;

    @BeforeEach
    public void setup() {
        lastModifiedTime = ZonedDateTime.parse("2020-03-30T15:00Z");
        final PopulateComplexEventTypeForPublicDisplay populateComplexEventTypeForPublicDisplay = new PopulateComplexEventTypeForPublicDisplay();
        final ComplexTypeDataProcessor complexTypeDataProcessor = new ComplexTypeDataProcessor();
        setField(populateComplexEventTypeForPublicDisplay, "complexTypeDataProcessor", complexTypeDataProcessor);
        setField(publicDisplayEventGenerator, "populateComplexEventTypeForPublicDisplay", populateComplexEventTypeForPublicDisplay);
    }

    @Test
    public void shouldGenerateCurrentStatus() {
        final HearingEvent hearingEvent = createHearingEvent(null);
        final String type = "10100";
        CaseDetail caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();


        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = publicDisplayEventGenerator.generate(caseDetail);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.withZoneSameInstant(localZoneId).format(timeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
    }

    @Test
    public void shouldGenerateCurrentStatusWithComplexEvent() {
        final HearingEvent hearingEvent = createHearingEvent(null);
        final String type = "20903";
        final CaseDetail caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();

        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = publicDisplayEventGenerator.generate(caseDetail);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.withZoneSameInstant(localZoneId).format(timeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
        assertThat(currentstatus.getEvent().getE20903ProsecutionCaseOptions().getE20903PCOType(), is(Event20903OptionType.E_20903_PROSECUTION_OPENING));
    }

    @Test
    public void shouldGenerateCurrentStatusWithComplexEventDefenceCounsel() {
        final UUID defenceCounselId = randomUUID();
        final HearingEvent hearingEvent = createHearingEvent(defenceCounselId);
        final String type = "20906";
        final DefenceCounsel defenceCounsel = DefenceCounsel.defenceCounsel().withId(defenceCounselId).withFirstName("Sid").withLastName("Sox").build();
        final  CaseDetail caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();
        caseDetail.setDefenceCounsels(Collections.singletonList(defenceCounsel));

        when(eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString())).thenReturn(type);

        final Currentstatus currentstatus = publicDisplayEventGenerator.generate(caseDetail);

        assertThat(currentstatus.getEvent().getDate(), is("19/03/20"));
        assertThat(currentstatus.getEvent().getTime(), is(lastModifiedTime.withZoneSameInstant(localZoneId).format(timeFormatter)));
        assertThat(currentstatus.getEvent().getType(), is(type));
        assertThat(currentstatus.getEvent().getFreeText(), is(EMPTY));
        assertThat(currentstatus.getEvent().getE20906DefenceCOName(), is("Sid Sox"));
    }

    private HearingEvent createHearingEvent(final UUID defenceCounselId) {
        final UUID hearingEventId = randomUUID();
        final UUID hearingEventDefinitionId = randomUUID();
        final ZonedDateTime lastModifiedTime = ZonedDateTime.parse("2020-03-30T15:00Z");

        return hearingEvent()
                .withLastModifiedTime(lastModifiedTime)
                .withEventDate(eventDate)
                .withDefenceCounselId(defenceCounselId)
                .withId(hearingEventId)
                .withHearingEventDefinitionId(hearingEventDefinitionId)
                .build();
    }
}