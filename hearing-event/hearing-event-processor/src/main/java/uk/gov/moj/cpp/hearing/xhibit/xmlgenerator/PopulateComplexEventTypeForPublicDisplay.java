package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.util.stream.Collectors.joining;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.E20903ProsecutionCaseOptions;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.E20916LegalArgumentOptions;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event20903OptionType;

import java.util.stream.Stream;

public class PopulateComplexEventTypeForPublicDisplay {
    public void addComplexEventType(final Event event, final DefenceCounsel defenceCounsel, final String eventCode) {
        if (XhibitEvent.APPELLANT_OPENS.getValue().equals(eventCode)) {
            populateE20606(event);
        }

        if (XhibitEvent.OPEN_CASE_PROSECUTION.getValue().equals(eventCode)) {
            populateE20903(event);
        }

        if (XhibitEvent.DEFENCE_COUNSEL_OPEN_CASE_DEFENDANT.getValue().equals(eventCode)) {
            populateE20906(event, defenceCounsel);
        }

        if (XhibitEvent.POINT_OF_LAW_DISCUSSION_PROSECUTION.getValue().equals(eventCode)) {
            populateE20916(event);
        }
    }

    private void populateE20606(final Event event) {
        //TODO add Appellant Name
        event.setE20606AppellantCOName("");
    }

    private void populateE20903(final Event event) {
        final E20903ProsecutionCaseOptions e20903ProsecutionCaseOptions = new E20903ProsecutionCaseOptions();
        e20903ProsecutionCaseOptions.setE20903PCOType(Event20903OptionType.E_20903_PROSECUTION_OPENING);
        event.setE20903ProsecutionCaseOptions(e20903ProsecutionCaseOptions);
    }

    private void populateE20906(final Event event, final DefenceCounsel defenceCounsel) {
        event.setE20906DefenceCOName(getGetDefenceCouncilFullName(defenceCounsel));
    }

    private void populateE20916(final Event event) {
        final E20916LegalArgumentOptions e20916LegalArgumentOptions = new E20916LegalArgumentOptions();
        //is this correct
        final String e20916Opt2JudgesRuling = "E20916_Opt2_Judges_Ruling";
        e20916LegalArgumentOptions.setE20916Opt2JudgesRuling(e20916Opt2JudgesRuling);
        event.setE20916LegalArgumentOptions(e20916LegalArgumentOptions);
    }

    private String getGetDefenceCouncilFullName(final DefenceCounsel defenceCounsel) {
        return Stream.of(defenceCounsel.getTitle(), defenceCounsel.getFirstName(),
                defenceCounsel.getMiddleName(), defenceCounsel.getLastName())
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining(" "));
    }
}
