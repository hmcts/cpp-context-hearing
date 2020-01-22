package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.XhibitEvent.valueFor;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Event;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PublicDisplayEventGenerator {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    private final static DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    public Currentstatus generate(final HearingEvent hearingEvent) {
        final String xhibitEventCode = eventMapperCache.getXhibitEventCodeBy(hearingEvent.getId().toString());

        final Currentstatus currentstatus = webPageObjectFactory.createCurrentstatus();

        if (valueFor(xhibitEventCode).isPresent()){
            final Event event = webPageObjectFactory.createEvent();

            event.setTime(hearingEvent.getLastModifiedTime().format(dateTimeFormatter));
            event.setDate(hearingEvent.getEventDate());
            event.setFreeText(EMPTY);
            event.setType(xhibitEventCode);

            currentstatus.setEvent(event);
        }
        return currentstatus;
    }
}
