package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.xhibit.xmlgenerator.XhibitEvent.*;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Event;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EventGenerator {

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory();

    private final static DateTimeFormatter dateTimeFormatter = ofPattern("HH:mm");

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
