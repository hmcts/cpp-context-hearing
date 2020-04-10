package uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransformFactory {

    private Map<String, List<HearingEventTransformer>> transformEventMap;


    public TransformFactory() {
        transformEventMap = new HashMap<>();

        addInstance(MasterDefendantIdEventTransformer.getEventAndJsonPaths().keySet(), new MasterDefendantIdEventTransformer());
        addInstance(CourtProceedingsInitiatedEventTransformer.getEventAndJsonPaths().keySet(), new CourtProceedingsInitiatedEventTransformer());

    }

    private void addInstance(final Set<String> keySet, final HearingEventTransformer eventTransformer) {
        keySet.forEach(key -> transformEventMap.compute(key, (s, hearingEventTransformers) -> {
                    if (hearingEventTransformers == null) {
                        hearingEventTransformers = new ArrayList<>();
                    }
                    hearingEventTransformers.add(eventTransformer);
                    return hearingEventTransformers;
                }
        ));
    }

    public List<HearingEventTransformer> getEventTransformer(String eventName) {
        return transformEventMap.get(eventName);
    }
}
