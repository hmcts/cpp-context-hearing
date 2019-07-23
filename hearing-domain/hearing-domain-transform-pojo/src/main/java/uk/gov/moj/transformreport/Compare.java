package uk.gov.moj.transformreport;

import uk.gov.justice.v24.hearing.events.listener.ConvictionDateAdded;
import uk.gov.justice.v24.hearing.events.listener.ConvictionDateRemoved;
import uk.gov.justice.v24.hearing.events.listener.DefenceCounselAdded;
import uk.gov.justice.v24.hearing.events.listener.DefenceCounselRemoved;
import uk.gov.justice.v24.hearing.events.listener.DefenceCounselUpdated;
import uk.gov.justice.v24.hearing.events.listener.DefendantAttendanceUpdated;
import uk.gov.justice.v24.hearing.events.listener.DefendantDetailsUpdated;
import uk.gov.justice.v24.hearing.events.listener.DetailChanged;
import uk.gov.justice.v24.hearing.events.listener.DraftResultSaved;
import uk.gov.justice.v24.hearing.events.listener.HearingEventDefinitionsCreated;
import uk.gov.justice.v24.hearing.events.listener.HearingEventDefinitionsDeleted;
import uk.gov.justice.v24.hearing.events.listener.HearingEventDeleted;
import uk.gov.justice.v24.hearing.events.listener.HearingEventLogged;
import uk.gov.justice.v24.hearing.events.listener.HearingEventsUpdated;
import uk.gov.justice.v24.hearing.events.listener.HearingOffencePleaUpdated;
import uk.gov.justice.v24.hearing.events.listener.HearingOffenceVerdictUpdated;
import uk.gov.justice.v24.hearing.events.listener.InheritedPlea;
import uk.gov.justice.v24.hearing.events.listener.InheritedVerdictAdded;
import uk.gov.justice.v24.hearing.events.listener.Initiated;
import uk.gov.justice.v24.hearing.events.listener.NowsMaterialStatusUpdated;
import uk.gov.justice.v24.hearing.events.listener.OffenceAdded;
import uk.gov.justice.v24.hearing.events.listener.OffenceDeleted;
import uk.gov.justice.v24.hearing.events.listener.OffenceUpdated;
import uk.gov.justice.v24.hearing.events.listener.PendingNowsRequested;
import uk.gov.justice.v24.hearing.events.listener.ProsecutionCounselAdded;
import uk.gov.justice.v24.hearing.events.listener.ProsecutionCounselRemoved;
import uk.gov.justice.v24.hearing.events.listener.ProsecutionCounselUpdated;
import uk.gov.justice.v24.hearing.events.listener.ResultsShared;
import uk.gov.justice.v24.hearing.events.listener.SubscriptionsUploaded;
import uk.gov.moj.cpp.coredomain.tools.transform.SchemaExploreKt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import v24.uk.gov.justice.core.courts.HearingCaseNote;

public class Compare {

    private static final Logger LOGGER = LoggerFactory.getLogger(Compare.class.getName());

    public static void main(String[] args) {
        for (final Map.Entry<Class<?>, Class<?>> entry : getAllRoots().entrySet()) {
            LOGGER.info("************Checking==================== {}", entry.getKey().getName());
            SchemaExploreKt.exploreParralel(entry.getKey(), entry.getValue(), c -> c.getName().contains("uk.gov.justice")
            );
        }
    }

    private static Map<Class<?>, Class<?>> getAllRoots() {
        final Map<Class<?>, Class<?>> roots = new LinkedHashMap<>();
        roots.put(ConvictionDateAdded.class, uk.gov.justice.json.schemas.hearing.ConvictionDateAdded.class);
        roots.put(ConvictionDateRemoved.class, uk.gov.justice.json.schemas.hearing.ConvictionDateRemoved.class);
        roots.put(DefenceCounselAdded.class, uk.gov.justice.json.schemas.hearing.DefenceCounselAdded.class);
        roots.put(DefenceCounselRemoved.class, uk.gov.justice.json.schemas.hearing.DefenceCounselRemoved.class);
        roots.put(DefenceCounselUpdated.class, uk.gov.justice.json.schemas.hearing.DefenceCounselUpdated.class);
        roots.put(DefendantAttendanceUpdated.class, uk.gov.justice.json.schemas.hearing.DefendantAttendanceUpdated.class);
        roots.put(DefendantDetailsUpdated.class, uk.gov.justice.json.schemas.hearing.DefendantDetailsUpdated.class);
        roots.put(DetailChanged.class, uk.gov.justice.json.schemas.hearing.DetailChanged.class);
        roots.put(HearingEventDefinitionsCreated.class, uk.gov.justice.json.schemas.hearing.HearingEventDefinitionsCreated.class);
        roots.put(HearingEventDefinitionsDeleted.class, uk.gov.justice.json.schemas.hearing.HearingEventDefinitionsDeleted.class);
        roots.put(HearingEventDeleted.class, uk.gov.justice.json.schemas.hearing.HearingEventDeleted.class);
        roots.put(HearingEventLogged.class, uk.gov.justice.json.schemas.hearing.HearingEventLogged.class);
        roots.put(HearingEventsUpdated.class, uk.gov.justice.json.schemas.hearing.HearingEventsUpdated.class);
        roots.put(HearingOffencePleaUpdated.class, uk.gov.justice.json.schemas.hearing.HearingOffencePleaUpdated.class);
        roots.put(HearingOffenceVerdictUpdated.class, uk.gov.justice.json.schemas.hearing.HearingOffenceVerdictUpdated.class);
        roots.put(InheritedPlea.class, uk.gov.justice.json.schemas.hearing.InheritedPlea.class);
        roots.put(InheritedVerdictAdded.class, uk.gov.justice.json.schemas.hearing.InheritedVerdictAdded.class);
        roots.put(Initiated.class, uk.gov.justice.json.schemas.hearing.Initiated.class);
        roots.put(NowsMaterialStatusUpdated.class, uk.gov.justice.json.schemas.hearing.NowsMaterialStatusUpdated.class);
        roots.put(OffenceAdded.class, uk.gov.justice.json.schemas.hearing.OffenceAdded.class);
        roots.put(OffenceDeleted.class, uk.gov.justice.json.schemas.hearing.OffenceDeleted.class);
        roots.put(OffenceUpdated.class, uk.gov.justice.json.schemas.hearing.OffenceUpdated.class);
        roots.put(ProsecutionCounselAdded.class, uk.gov.justice.json.schemas.hearing.ProsecutionCounselAdded.class);
        roots.put(ProsecutionCounselRemoved.class, uk.gov.justice.json.schemas.hearing.ProsecutionCounselRemoved.class);
        roots.put(ProsecutionCounselUpdated.class, uk.gov.justice.json.schemas.hearing.ProsecutionCounselUpdated.class);
        roots.put(ResultsShared.class, uk.gov.justice.json.schemas.hearing.ResultsShared.class);
        roots.put(HearingCaseNote.class, uk.gov.justice.json.schemas.hearing.HearingCaseNoteSaved.class);
        roots.put(DraftResultSaved.class, uk.gov.justice.json.schemas.hearing.DraftResultSaved.class);
        roots.put(PendingNowsRequested.class, uk.gov.justice.json.schemas.hearing.PendingNowsRequested.class);
        roots.put(SubscriptionsUploaded.class, uk.gov.justice.json.schemas.hearing.SubscriptionsUploaded.class);
        return roots;
    }
}
