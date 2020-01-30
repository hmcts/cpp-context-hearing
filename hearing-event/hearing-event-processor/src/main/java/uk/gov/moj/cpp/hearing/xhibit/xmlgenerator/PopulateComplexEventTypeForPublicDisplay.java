package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.E20903ProsecutionCaseOptions;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.E20916LegalArgumentOptions;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event20903OptionType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

public class PopulateComplexEventTypeForPublicDisplay {

    @Inject
    private ComplexTypeDataProcessor complexTypeDataProcessor;

    public void addComplexEventType(final Event event, final CaseDetail cppCaseDetail, final String eventCode) {
        if (XhibitEvent.APPELLANT_OPENS.getValue().equals(eventCode) &&
                CollectionUtils.isNotEmpty(cppCaseDetail.getLinkedCaseIds())) {
            final Optional<String> appellantDisplayName = complexTypeDataProcessor.getAppellantDisplayName(cppCaseDetail.getLinkedCaseIds());
            if (appellantDisplayName.isPresent()) {
                populateE20606(event, appellantDisplayName.get());
            }
        }

        if (XhibitEvent.OPEN_CASE_PROSECUTION.getValue().equals(eventCode)) {
            populateE20903(event);
        }

        if (XhibitEvent.DEFENCE_COUNSEL_OPEN_CASE_DEFENDANT.getValue().equals(eventCode) &&
                cppCaseDetail.getDefenceCounsel() != null) {
            populateE20906(event, complexTypeDataProcessor.getGetDefenceCouncilFullName(cppCaseDetail.getDefenceCounsel()));
        }

        if (XhibitEvent.POINT_OF_LAW_DISCUSSION_PROSECUTION.getValue().equals(eventCode)) {
            populateE20916(event);
        }
    }

    private void populateE20606(final Event event, final String appellantDisplayName) {
        event.setE20606AppellantCOName(appellantDisplayName);
    }

    private void populateE20903(final Event event) {
        final E20903ProsecutionCaseOptions e20903ProsecutionCaseOptions = new E20903ProsecutionCaseOptions();
        e20903ProsecutionCaseOptions.setE20903PCOType(Event20903OptionType.E_20903_PROSECUTION_OPENING);
        event.setE20903ProsecutionCaseOptions(e20903ProsecutionCaseOptions);
    }

    private void populateE20906(final Event event, final String defenceCounsel) {
        event.setE20906DefenceCOName(defenceCounsel);
    }

    private void populateE20916(final Event event) {
        final E20916LegalArgumentOptions e20916LegalArgumentOptions = new E20916LegalArgumentOptions();
        final String e20916Opt2JudgesRuling = "E20916_Opt2_Judges_Ruling";
        e20916LegalArgumentOptions.setE20916Opt2JudgesRuling(e20916Opt2JudgesRuling);
        event.setE20916LegalArgumentOptions(e20916LegalArgumentOptions);
    }
}
