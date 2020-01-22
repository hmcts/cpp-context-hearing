package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event20903OptionType;

import org.junit.Test;

public class PopulateComplexEventTypeTest {

    private PopulateComplexEventType populateComplexEventType;

    private Event event;

    private DefenceCounsel defenseCounsel;

    @Test
    public void shouldNotAddComplexEventTypeWhenNotComplexCode() {
        event = new Event();
        populateComplexEventType = new PopulateComplexEventType();
        populateComplexEventType.addComplexEventType(event, null, "x");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20606() {
        event = new Event();
        populateComplexEventType = new PopulateComplexEventType();
        populateComplexEventType.addComplexEventType(event, null, "20606");
        assertThat(event.getE20606AppellantCOName(), is(""));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCCName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }


    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20903() {
        event = new Event();
        populateComplexEventType = new PopulateComplexEventType();
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
        defenseCounsel = new DefenceCounsel(null, null, "John", null, "Jones", null, null, null);
        populateComplexEventType = new PopulateComplexEventType();
        populateComplexEventType.addComplexEventType(event, defenseCounsel, "20906");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is("John Jones"));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20906WithTitle() {
        event = new Event();
        defenseCounsel = new DefenceCounsel(null, null, "John", null, "Jones", null, null, "Mr");
        populateComplexEventType = new PopulateComplexEventType();
        populateComplexEventType.addComplexEventType(event, defenseCounsel, "20906");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is("Mr John Jones"));
        assertThat(event.getE20916LegalArgumentOptions(), is(nullValue()));
    }

    @Test
    public void shouldAddComplexEventTypeWhenComplexCode20916() {
        event = new Event();
        defenseCounsel = new DefenceCounsel(null, null, "John", null, "Jones", null, null, null);
        populateComplexEventType = new PopulateComplexEventType();
        populateComplexEventType.addComplexEventType(event, defenseCounsel, "20916");
        assertThat(event.getE20606AppellantCOName(), is(nullValue()));
        assertThat(event.getE20903ProsecutionCaseOptions(), is(nullValue()));
        assertThat(event.getE20906DefenceCOName(), is(nullValue()));
        assertThat(event.getE20916LegalArgumentOptions().getE20916Opt2JudgesRuling(), is("E20916_Opt2_Judges_Ruling"));
    }
}