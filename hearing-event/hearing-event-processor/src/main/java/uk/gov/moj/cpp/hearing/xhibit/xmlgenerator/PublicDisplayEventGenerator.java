package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.XhibitEvent.valueFor;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.utils.DateUtils;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PublicDisplayEventGenerator {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    @Inject
    private PopulateComplexEventTypeForPublicDisplay populateComplexEventTypeForPublicDisplay;

    public Currentstatus generate(final CaseDetail cppCaseDetail) {
        final HearingEvent hearingEvent =  cppCaseDetail.getHearingEvent();
        final String xhibitEventCode =
                eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString());

        final Currentstatus currentstatus = webPageObjectFactory.createCurrentstatus();

        if (valueFor(xhibitEventCode).isPresent()){
            final Event event = webPageObjectFactory.createEvent();

            event.setTime(DateUtils.convertZonedDateTimeToLocalTime(hearingEvent.getLastModifiedTime()));
            event.setDate(DateUtils.convertToLocalDate(hearingEvent.getEventDate()));
            event.setFreeText(EMPTY);
            event.setType(xhibitEventCode);
            populateComplexEventTypeForPublicDisplay.addComplexEventType(event, cppCaseDetail, xhibitEventCode);

            currentstatus.setEvent(event);
        }
        return currentstatus;
    }
}
