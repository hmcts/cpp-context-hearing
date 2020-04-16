package uk.gov.moj.cpp.hearing.domain.transformation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventMapper {

    private EventMapper() {
    }

    private static Map<String, List<String>> EVENT_MAP = new HashMap();

    static {
        EVENT_MAP.put("hearing.events.application-detail-changed", newArrayList(
                "$.hearingId"));

        EVENT_MAP.put("hearing.events.hearing-extended", newArrayList(
                "$.hearingId"));

        EVENT_MAP.put("hearing.case-defendants-updated-for-hearing", newArrayList(
                "$.hearingId"
        ));

        EVENT_MAP.put("hearing.case-defendants-updated", newArrayList(
                "$.hearingIds[0]"));

        EVENT_MAP.put("hearing.events.initiated", newArrayList(
                "$.hearing.id"
        ));

        EVENT_MAP.put("hearing.results-shared", newArrayList(
                "$.hearingId"));

        EVENT_MAP.put("hearing.events.pending-nows-requested", newArrayList(
                "$.createNowsRequest.hearing.id"));

        EVENT_MAP.put("hearing.events.nows-requested", newArrayList(
                "$.createNowsRequest.hearing.id"));
    }

    public static Collection getEventNames() {
        return EVENT_MAP.keySet();
    }

    public static List<String> getMappedJsonPaths(String eventName) {
        return EVENT_MAP.get(eventName);
    }

}
