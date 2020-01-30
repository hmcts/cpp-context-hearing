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
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event20903OptionType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.xhibit.ProgressionCaseRetriever;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PopulateComplexEventTypeForPublicDisplayTest {
    private PopulateComplexEventTypeForPublicDisplay populateComplexEventTypeForPublicDisplay;

    private Event event;

    private DefenceCounsel defenseCounsel;

    private CaseDetail caseDetail;

    @Mock
    private ProgressionCaseRetriever progressionCaseRetriever;

    private ComplexTypeDataProcessor complexTypeDataProcessor;

    @Before
    public void setup() {
        populateComplexEventTypeForPublicDisplay = new PopulateComplexEventTypeForPublicDisplay();
        complexTypeDataProcessor = new ComplexTypeDataProcessor();
        setField(complexTypeDataProcessor, "progressionCaseRetriever", progressionCaseRetriever);
        setField(populateComplexEventTypeForPublicDisplay, "complexTypeDataProcessor", complexTypeDataProcessor);
    }

    @Test
    public void shouldNotAddComplexEventTypeWhenNotComplexCode() {
        event = new Event();

        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, null, "x");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20606() {
        event = new Event();
        final UUID caseId = randomUUID();
        caseDetail = CaseDetail.caseDetail().build();
        caseDetail.setLinkedCaseIds(Collections.singletonList(caseId));
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withLinkedApplicationsSummary(Collections.singletonList(
                        LinkedApplicationsSummary.linkedApplicationsSummary()
                                .withIsAppeal(Boolean.TRUE)
                                .withRespondentDisplayNames(Collections.singletonList("Red Sox"))
                                .withApplicantDisplayName("John Jones")
                                .build()))
                .build();
        when(progressionCaseRetriever.getProsecutionCaseDetails(caseId)).thenReturn(prosecutionCase);

        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, caseDetail, "20606");
        assertThat(event.getE20606AppellantCOName(), is("John Jones"));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20903() {
        event = new Event();

        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, null, "20903");
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
        caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();
        caseDetail.setDefenceCounsels(Collections.singletonList(defenseCounsel));

        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, caseDetail, "20906");
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
        caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();
        caseDetail.setDefenceCounsels(Collections.singletonList(defenseCounsel));

        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, caseDetail,"20906");
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
        caseDetail =  CaseDetail.caseDetail().withHearingEvent(hearingEvent).build();
        caseDetail.setDefenceCounsels(Collections.singletonList(defenseCounsel));



        populateComplexEventTypeForPublicDisplay.addComplexEventType(event, caseDetail, "20916");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions().getE20916Opt2JudgesRuling(), is("E20916_Opt2_Judges_Ruling"));
    }
}