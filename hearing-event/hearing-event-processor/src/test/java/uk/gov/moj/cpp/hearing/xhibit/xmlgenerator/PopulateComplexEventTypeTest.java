package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.LinkedApplicationsSummary;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event20903OptionType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.xhibit.ProgressionCaseRetriever;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PopulateComplexEventTypeTest {

    private PopulateComplexEventType populateComplexEventType;

    private Event event;

    private DefenceCounsel defenseCounsel;

    private CourtRoom courtRoom;

    @Mock
    private ProgressionCaseRetriever progressionCaseRetriever;

    private ComplexTypeDataProcessor complexTypeDataProcessor;

    @Before
    public void setup() {
        populateComplexEventType = new PopulateComplexEventType();
        complexTypeDataProcessor = new ComplexTypeDataProcessor();
        setField(complexTypeDataProcessor, "progressionCaseRetriever", progressionCaseRetriever);
        setField(populateComplexEventType, "complexTypeDataProcessor", complexTypeDataProcessor);
    }

    @Test
    public void shouldNotAddComplexEventTypeWhenNotComplexCode() {
        event = new Event();

        populateComplexEventType.addComplexEventType(event, null, "x");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20606() {
        event = new Event();
        final UUID caseId = randomUUID();
        courtRoom = CourtRoom.courtRoom().withLinkedCaseIds(Collections.singletonList(caseId)).build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withLinkedApplicationsSummary(Collections.singletonList(
                        LinkedApplicationsSummary.linkedApplicationsSummary()
                                .withIsAppeal(Boolean.TRUE)
                                .withRespondentDisplayNames(Collections.singletonList("Red Sox"))
                                .withApplicantDisplayName("John Jones")
                                .build()))
                .build();
        when(progressionCaseRetriever.getProsecutionCaseDetails(caseId)).thenReturn(prosecutionCase);

        populateComplexEventType.addComplexEventType(event, courtRoom, "20606");
        assertThat(event.getE20606AppellantCOName(), is("John Jones"));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20903() {
        event = new Event();

        populateComplexEventType.addComplexEventType(event, null, "20903");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(notNullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions().getE20903PCOType(), is(Event20903OptionType.E_20903_PROSECUTION_OPENING));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20906() {
        event = new Event();
        final UUID defenceCounselId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withDefenceCounselId(defenceCounselId).build();
        defenseCounsel = DefenceCounsel.defenceCounsel().withId(defenceCounselId).withFirstName("John").withLastName("Jones").build();
        courtRoom = CourtRoom.courtRoom().withHearingEvent(hearingEvent).withDefenceCouncil(Collections.singletonList(defenseCounsel)).build();

        populateComplexEventType.addComplexEventType(event, courtRoom, "20906");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is("John Jones"));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20906WithTitle() {
        event = new Event();
        final UUID defenceCounselId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withDefenceCounselId(defenceCounselId).build();
        defenseCounsel = DefenceCounsel.defenceCounsel().withId(defenceCounselId).withFirstName("John").withLastName("Jones").withTitle("Mr").build();
        courtRoom = CourtRoom.courtRoom().withHearingEvent(hearingEvent).withDefenceCouncil(Collections.singletonList(defenseCounsel)).build();

        populateComplexEventType.addComplexEventType(event, courtRoom,"20906");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is("Mr John Jones"));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20916() {
        event = new Event();
        final UUID defenceCounselId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().withDefenceCounselId(defenceCounselId).build();
        defenseCounsel = DefenceCounsel.defenceCounsel().withId(defenceCounselId).withFirstName("John").withLastName("Jones").build();
        courtRoom = CourtRoom.courtRoom().withHearingEvent(hearingEvent).withDefenceCouncil(Collections.singletonList(defenseCounsel)).build();

        populateComplexEventType.addComplexEventType(event, courtRoom, "20916");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions().getE20916Opt2JudgesRuling(), is("E20916_Opt2_Judges_Ruling"));
    }
}