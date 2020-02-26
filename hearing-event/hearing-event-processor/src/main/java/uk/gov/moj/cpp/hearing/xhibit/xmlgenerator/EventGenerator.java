package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.XhibitEvent.valueFor;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventGenerator {

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    @Inject
    private PopulateComplexEventType populateComplexEventType;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory();

    private static final DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");
    private static final DateTimeFormatter dateFormatter = ofPattern("dd/MM/yy");

    public Currentstatus generate(final CourtRoom courtRoom) {
        final HearingEvent hearingEvent = courtRoom.getHearingEvent();
        final Currentstatus currentstatus = webPageObjectFactory.createCurrentstatus();
        if (hearingEvent != null) {
            final String xhibitEventCode =
                    eventMapperCache.getXhibitEventCodeBy(hearingEvent.getHearingEventDefinitionId().toString());


            if (valueFor(xhibitEventCode).isPresent()) {
                final Event event = webPageObjectFactory.createEvent();

                event.setTime(hearingEvent.getLastModifiedTime().format(dateTimeFormatter));
                event.setDate(LocalDate.parse(hearingEvent.getEventDate()).format(dateFormatter));
                event.setFreeText(EMPTY);
                event.setType(xhibitEventCode);

                populateComplexEventType.addComplexEventType(event, courtRoom, xhibitEventCode);

                currentstatus.setEvent(event);
            }
        }
        return currentstatus;
    }
}
